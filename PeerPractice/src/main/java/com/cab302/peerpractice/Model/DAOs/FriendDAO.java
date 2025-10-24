package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.Entities.Friend;
import com.cab302.peerpractice.Model.Entities.FriendStatus;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Objects;

/**
 * <hr>
 * SQLite implementation of friend relationship Data Access Object.
 *
 * <p>This class provides concrete SQLite database operations for managing
 * friend relationships, friend requests, and user blocking functionality.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Friend request workflow management</li>
 *   <li>Bidirectional friendship establishment</li>
 *   <li>User blocking and unblocking operations</li>
 *   <li>Observable collections for real-time UI updates</li>
 * </ul>
 *
 * @see Friend
 * @see IFriendDAO
 * @see User
 * @see FriendStatus
 */
public class FriendDAO implements IFriendDAO {

    /** <hr> Database connection instance for SQLite operations. */
    private final Connection connection;
    /** <hr> User DAO for user entity resolution and operations. */
    private final IUserDAO userDAO;

    /**
     * <hr>
     * Constructs a new FriendDAO with database connection and user DAO dependency.
     *
     * <p>Initializes the SQLite connection and ensures the required database
     * table exists by calling createTable() during construction.
     *
     * @param userDAO the User DAO for user entity operations
     * @throws SQLException if database connection or table creation fails
     */
    public FriendDAO(IUserDAO userDAO) throws SQLException {
        this.userDAO = userDAO;
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

    /**
     * <hr>
     * Creates the friends table if it doesn't exist.
     *
     * <p>Defines the database schema for storing friend relationships with
     * appropriate foreign key constraints and status enumeration.
     *
     * @throws SQLException if table creation fails
     */
    private void createTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS friends (" +
                    "user TEXT NOT NULL, " +
                    "friend TEXT NOT NULL, " +
                    "status TEXT NOT NULL DEFAULT 'pending' CHECK(status IN ('pending','accepted','denied','blocked'))," +
                    "PRIMARY KEY(user, friend), " +
                    "FOREIGN KEY(user) REFERENCES users(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(friend) REFERENCES users(username) ON DELETE CASCADE" +
                    ")");
        }
    }

    // -------------------- READ --------------------

    /**
     * <hr>
     * Retrieves all friend relationships for a specific user.
     *
     * <p>Fetches the complete list of friends, friend requests, and blocked users
     * associated with the specified user, ordered by relationship status.
     *
     * @param user the user whose friends are being retrieved
     * @return an ObservableList of Friend objects representing all relationships
     * @throws SQLException if a database access error occurs
     */
    @Override
    public ObservableList<Friend> getFriends(User user) throws SQLException {
        String sql = "SELECT * FROM friends WHERE user = ? ORDER BY status";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                return resultsToList(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get friends for user: " + user.getUsername(), e);
        }
    }

    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Sends a friend request from one user to another.
     *
     * <p>Creates a new friend relationship with 'pending' status. Checks for
     * duplicate requests and prevents self-friending. Also verifies that
     * neither user has blocked the other before allowing the request.
     *
     * @param user the user sending the friend request
     * @param friend the user receiving the friend request
     * @return true if the friend request was created successfully, false otherwise
     * @throws SQLException if a database access error occurs
     * @throws DuplicateFriendException if a friend relationship already exists
     */
    @Override
    public boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException {
        if (Objects.equals(user.getUsername(), friend.getUsername())) return false;
        if (friendExists(user, friend)) return false;

        // Check if either user has blocked the other
        if (isBlocked(user, friend) || isBlocked(friend, user)) {
            return false; // Cannot add friend if either has blocked the other
        }

        String sql = "INSERT INTO friends (user, friend, status) VALUES (?, ?, 'pending')";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, friend.getUsername());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add friend", e);
        }
    }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Accepts a pending friend request.
     *
     * <p>Updates the friend request status to 'accepted' and creates a
     * reciprocal friendship record to ensure both users see each other
     * as friends in their respective friend lists.
     *
     * @param user the user accepting the friend request
     * @param friend the user whose request is being accepted
     * @return true if the friend request was successfully accepted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean acceptFriendRequest(User user, User friend) throws SQLException {
        // Update the original request to accepted
        boolean updated = updateFriendStatus(user, friend, "accepted");

        // Create or update the reciprocal friendship so both users see each other as friends
        if (updated) {
            // Check if reciprocal entry exists
            if (friendExists(friend, user)) {
                // If it exists, update its status to accepted as well
                updateFriendStatus(friend, user, "accepted");
            } else {
                // If it doesn't exist, create it with accepted status
                String sql = "INSERT INTO friends (user, friend, status) VALUES (?, ?, 'accepted')";
                try (PreparedStatement ps = connection.prepareStatement(sql)) {
                    ps.setString(1, friend.getUsername());
                    ps.setString(2, user.getUsername());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to create reciprocal friendship", e);
                }
            }
        }

        return updated;
    }

    /**
     * <hr>
     * Denies a pending friend request.
     *
     * <p>Updates the friend request status to 'denied', effectively
     * rejecting the friend request without creating a friendship.
     *
     * @param user the user denying the friend request
     * @param friend the user whose request is being denied
     * @return true if the friend request was successfully denied, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean denyFriendRequest(User user, User friend) throws SQLException {
        return updateFriendStatus(user, friend, "denied");
    }

    /**
     * <hr>
     * Blocks a user, preventing future friend requests and interactions.
     *
     * <p>Updates the friend relationship status to 'blocked'. Blocked users
     * cannot send friend requests or messages to the blocking user.
     *
     * @param user the user initiating the block action
     * @param friend the user being blocked
     * @return true if the user was successfully blocked, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean blockUser(User user, User friend) throws SQLException {
        return updateFriendStatus(user, friend, "blocked");
    }

    /**
     * <hr>
     * Unblocks a previously blocked user.
     *
     * <p>Removes the blocked status by deleting the friend relationship record,
     * allowing the previously blocked user to send friend requests again.
     *
     * @param user the user initiating the unblock action
     * @param friend the user being unblocked
     * @return true if the user was successfully unblocked, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean unblockUser(User user, User friend) throws SQLException {
        // Remove the blocked status entry
        return removeFriend(user, friend);
    }

    /**
     * <hr>
     * Retrieves all users blocked by a specific user.
     *
     * <p>Fetches the list of users that the specified user has blocked,
     * ordered by the blocked user's username.
     *
     * @param user the user whose blocked list is being retrieved
     * @return an ObservableList of Friend objects with 'blocked' status
     * @throws SQLException if a database access error occurs
     */
    @Override
    public ObservableList<Friend> getBlockedUsers(User user) throws SQLException {
        String sql = "SELECT * FROM friends WHERE user = ? AND status = 'blocked' ORDER BY friend";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                return resultsToList(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get blocked users for user: " + user.getUsername(), e);
        }
    }

    /**
     * <hr>
     * Checks if one user has blocked another user.
     *
     * <p>Verifies whether a blocking relationship exists between the two users
     * in the specified direction (user has blocked friend).
     *
     * @param user the potential blocking user
     * @param friend the potentially blocked user
     * @return true if user has blocked friend, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean isBlocked(User user, User friend) throws SQLException {
        String sql = "SELECT 1 FROM friends WHERE user = ? AND friend = ? AND status = 'blocked'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, friend.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * <hr>
     * Updates the status of a friend relationship.
     *
     * <p>Modifies the relationship status between two users, supporting
     * transitions between pending, accepted, denied, and blocked states.
     *
     * @param user the user initiating the status change
     * @param friend the other user in the relationship
     * @param status the new status to set for the relationship
     * @return true if the status was successfully updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    private boolean updateFriendStatus(User user, User friend, String status) throws SQLException {
        String sql = "UPDATE friends SET status = ? WHERE user = ? AND friend = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, user.getUsername());
            ps.setString(3, friend.getUsername());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update friend status", e);
        }
    }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Removes a friend relationship between two users.
     *
     * <p>Deletes the friendship record from persistent storage, effectively
     * unfriending the users. This operation is bidirectional and affects
     * both users' friend lists.
     *
     * @param user the user initiating the unfriend action
     * @param friend the user being unfriended
     * @return true if the friendship was successfully removed, false otherwise
     * @throws SQLException if a database access error occurs
     */
    @Override
    public boolean removeFriend(User user, User friend) throws SQLException {
        String sql = "DELETE FROM friends WHERE user = ? AND friend = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, friend.getUsername());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove friend", e);
        }
    }

    // -------------------- HELPERS --------------------

    /**
     * <hr>
     * Checks if a friend relationship already exists between two users.
     *
     * <p>Verifies whether any type of relationship (pending, accepted, denied, blocked)
     * already exists between the specified users in the database.
     *
     * @param user the first user in the potential relationship
     * @param friend the second user in the potential relationship
     * @return true if any relationship exists between the users, false otherwise
     * @throws SQLException if a database access error occurs
     */
    private boolean friendExists(User user, User friend) throws SQLException {
        String sql = "SELECT 1 FROM friends WHERE user = ? AND friend = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, friend.getUsername());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * <hr>
     * Converts a database ResultSet to an ObservableList of Friend objects.
     *
     * <p>Processes SQL query results and transforms them into fully populated
     * Friend entities with resolved User objects and proper status enumeration.
     *
     * @param results the ResultSet containing friend relationship data
     * @return an ObservableList of Friend objects from the ResultSet
     * @throws SQLException if data extraction or user resolution fails
     */
    private ObservableList<Friend> resultsToList(ResultSet results) throws SQLException {
        ObservableList<Friend> friends = FXCollections.observableArrayList();
        if (results == null) return friends;

        while (results.next()) {
            String username1 = results.getString("user");
            String username2 = results.getString("friend");
            FriendStatus status = FriendStatus.valueOf(results.getString("status").toUpperCase());

            User user1 = userDAO.findUser("username", username1);
            User user2 = userDAO.findUser("username", username2);

            if (user1 != null && user2 != null) {
                friends.add(new Friend(user1, user2, status));
            }
        }
        return friends;
    }
}