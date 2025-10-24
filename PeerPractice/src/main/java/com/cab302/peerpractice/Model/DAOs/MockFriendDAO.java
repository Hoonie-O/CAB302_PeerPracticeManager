package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import com.cab302.peerpractice.Model.Entities.Friend;
import com.cab302.peerpractice.Model.Entities.FriendStatus;
import com.cab302.peerpractice.Model.Entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <hr>
 * Mock (in-memory) implementation of friend Data Access Object for unit testing.
 *
 * <p>This class provides an in-memory implementation of IFriendDAO
 * suitable for unit testing without requiring a physical database connection.
 * It simulates friend relationship operations using concurrent hash maps
 * and user DAO integration for realistic testing scenarios.
 *
 * <p> Key features include:
 * <ul>
 *   <li>In-memory friend relationship storage</li>
 *   <li>Concurrent map for thread-safe testing</li>
 *   <li>Friend status management and transitions</li>
 *   <li>User DAO integration for entity resolution</li>
 * </ul>
 *
 * @see IFriendDAO
 * @see Friend
 * @see FriendStatus
 * @see User
 */
public class MockFriendDAO implements IFriendDAO {

    /** <hr> In-memory storage for friend relationships using composite keys. */
    // Key = "user:friend", Value = FriendStatus
    private final Map<String, FriendStatus> friendRelations = new ConcurrentHashMap<>();
    /** <hr> User DAO for user entity resolution and operations. */
    private final IUserDAO userDAO;

    /**
     * <hr>
     * Constructs a new MockFriendDAO with user DAO dependency.
     *
     * <p>Initializes the mock DAO with the provided user DAO for user entity
     * resolution, maintaining compatibility with the production DAO interface.
     *
     * @param userDAO the User DAO for user entity operations
     */
    public MockFriendDAO(IUserDAO userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * <hr>
     * Generates a composite key for friend relationship storage.
     *
     * <p>Creates a unique string key combining two usernames for storing
     * friend relationships in the mock storage map.
     *
     * @param user the first user in the relationship
     * @param friend the second user in the relationship
     * @return a composite key string in "user:friend" format
     */
    private String key(User user, User friend) {
        return user.getUsername() + ":" + friend.getUsername();
    }

    // -------------------- READ --------------------

    /**
     * <hr>
     * Retrieves all friend relationships for a specific user.
     *
     * <p>Fetches the complete list of friends, friend requests, and blocked users
     * associated with the specified user from mock storage, simulating
     * user-centric friend query operations for testing.
     *
     * @param user the user whose friends are being retrieved
     * @return an ObservableList of Friend objects representing all relationships
     * @throws SQLException if a database access error occurs (in mock, wraps runtime exceptions)
     */
    @Override
    public ObservableList<Friend> getFriends(User user) {
        ObservableList<Friend> list = FXCollections.observableArrayList();

        friendRelations.forEach((k, status) -> {
            String[] parts = k.split(":");
            if (parts[0].equals(user.getUsername())) {
                User u1 = null;
                try {
                    u1 = userDAO.findUser("username", parts[0]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                User u2 = null;
                try {
                    u2 = userDAO.findUser("username", parts[1]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                if (u1 != null && u2 != null) {
                    list.add(new Friend(u1, u2, status));
                }
            }
        });

        return list;
    }

    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Sends a friend request from one user to another in mock storage.
     *
     * <p>Creates a new friend relationship with 'pending' status in the mock
     * storage, checking for duplicate requests and preventing self-friending.
     * Simulates friend request operations for testing scenarios.
     *
     * @param user the user sending the friend request
     * @param friend the user receiving the friend request
     * @return true if the friend request was created successfully, false otherwise
     * @throws SQLException if a database access error occurs (in mock, wraps runtime exceptions)
     * @throws DuplicateFriendException if a friend relationship already exists
     */
    @Override
    public boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException {
        if (Objects.equals(user.getUsername(), friend.getUsername())) return false;

        if (friendExists(user, friend)) {
            if (acceptFriendRequest(user, friend)) return true;
            throw new DuplicateFriendException("Friend already exists");
        }

        friendRelations.put(key(user, friend), FriendStatus.PENDING);
        return true;
    }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Accepts a pending friend request in mock storage.
     *
     * <p>Updates the friend request status to 'accepted' in the mock storage,
     * simulating friend request acceptance operations for testing.
     *
     * @param user the user accepting the friend request
     * @param friend the user whose request is being accepted
     * @return true if the friend request was successfully accepted, false otherwise
     */
    @Override
    public boolean acceptFriendRequest(User user, User friend) {
        return updateFriendStatus(user, friend, FriendStatus.ACCEPTED);
    }

    /**
     * <hr>
     * Denies a pending friend request in mock storage.
     *
     * <p>Updates the friend request status to 'denied' in the mock storage,
     * simulating friend request rejection operations for testing.
     *
     * @param user the user denying the friend request
     * @param friend the user whose request is being denied
     * @return true if the friend request was successfully denied, false otherwise
     */
    @Override
    public boolean denyFriendRequest(User user, User friend) {
        return updateFriendStatus(user, friend, FriendStatus.DENIED);
    }

    /**
     * <hr>
     * Blocks a user in mock storage.
     *
     * <p>Updates the friend relationship status to 'blocked' in the mock storage,
     * simulating user blocking operations for testing.
     *
     * @param user the user initiating the block action
     * @param friend the user being blocked
     * @return true if the user was successfully blocked, false otherwise
     */
    @Override
    public boolean blockUser(User user, User friend) {
        return updateFriendStatus(user, friend, FriendStatus.BLOCKED);
    }

    /**
     * <hr>
     * Unblocks a previously blocked user in mock storage.
     *
     * <p>Removes the blocked status by deleting the friend relationship record
     * from mock storage, simulating user unblocking operations for testing.
     *
     * @param user the user initiating the unblock action
     * @param friend the user being unblocked
     * @return true if the user was successfully unblocked, false otherwise
     * @throws SQLException if a database access error occurs (in mock, wraps runtime exceptions)
     */
    @Override
    public boolean unblockUser(User user, User friend) throws SQLException {
        return removeFriend(user, friend);
    }

    /**
     * <hr>
     * Retrieves all users blocked by a specific user from mock storage.
     *
     * <p>Fetches the list of users that the specified user has blocked from
     * the mock storage, simulating blocked user query operations for testing.
     *
     * @param user the user whose blocked list is being retrieved
     * @return an ObservableList of Friend objects with 'blocked' status
     * @throws SQLException if a database access error occurs (in mock, wraps runtime exceptions)
     */
    @Override
    public ObservableList<Friend> getBlockedUsers(User user) throws SQLException {
        ObservableList<Friend> list = FXCollections.observableArrayList();

        friendRelations.forEach((k, status) -> {
            if (status == FriendStatus.BLOCKED) {
                String[] parts = k.split(":");
                if (parts[0].equals(user.getUsername())) {
                    User u1 = null;
                    try {
                        u1 = userDAO.findUser("username", parts[0]);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    User u2 = null;
                    try {
                        u2 = userDAO.findUser("username", parts[1]);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                    if (u1 != null && u2 != null) {
                        list.add(new Friend(u1, u2, status));
                    }
                }
            }
        });

        return list;
    }

    /**
     * <hr>
     * Checks if one user has blocked another user in mock storage.
     *
     * <p>Verifies whether a blocking relationship exists between the two users
     * in the specified direction in the mock storage, simulating block
     * status checking operations for testing.
     *
     * @param user the potential blocking user
     * @param friend the potentially blocked user
     * @return true if user has blocked friend, false otherwise
     * @throws SQLException if a database access error occurs (in mock, wraps runtime exceptions)
     */
    @Override
    public boolean isBlocked(User user, User friend) throws SQLException {
        String k = key(user, friend);
        return friendRelations.containsKey(k) && friendRelations.get(k) == FriendStatus.BLOCKED;
    }

    /**
     * <hr>
     * Updates the status of a friend relationship in mock storage.
     *
     * <p>Modifies the relationship status between two users in the mock storage,
     * supporting transitions between pending, accepted, denied, and blocked states
     * for comprehensive testing.
     *
     * @param user the user initiating the status change
     * @param friend the other user in the relationship
     * @param status the new status to set for the relationship
     * @return true if the status was successfully updated, false otherwise
     */
    private boolean updateFriendStatus(User user, User friend, FriendStatus status) {
        String k = key(user, friend);
        if (friendRelations.containsKey(k)) {
            friendRelations.put(k, status);
            return true;
        }
        return false;
    }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Removes a friend relationship between two users from mock storage.
     *
     * <p>Deletes the friendship record from the mock storage, effectively
     * unfriending the users. Simulates friend removal operations for testing.
     *
     * @param user the user initiating the unfriend action
     * @param friend the user being unfriended
     * @return true if the friendship was successfully removed, false otherwise
     */
    @Override
    public boolean removeFriend(User user, User friend) {
        return friendRelations.remove(key(user, friend)) != null;
    }

    // -------------------- HELPERS --------------------

    /**
     * <hr>
     * Checks if a friend relationship already exists between two users in mock storage.
     *
     * <p>Verifies whether any type of relationship (pending, accepted, denied, blocked)
     * already exists between the specified users in the mock storage.
     *
     * @param user the first user in the potential relationship
     * @param friend the second user in the potential relationship
     * @return true if any relationship exists between the users, false otherwise
     */
    private boolean friendExists(User user, User friend) {
        return friendRelations.containsKey(key(user, friend));
    }
}