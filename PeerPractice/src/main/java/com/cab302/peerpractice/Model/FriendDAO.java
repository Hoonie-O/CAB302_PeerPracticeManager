package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.Objects;

public class FriendDAO implements IFriendDAO {

    private final Connection connection;
    private final IUserDAO userDAO;

    public FriendDAO(IUserDAO userDAO) throws SQLException {
        this.userDAO = userDAO;
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

    // -------------------- TABLE CREATION --------------------
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
    @Override
    public boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException {
        if (Objects.equals(user.getUsername(), friend.getUsername())) return false;
        if (friendExists(user, friend)) {
            if (acceptFriendRequest(user, friend)) return true;
            throw new DuplicateFriendException("Friend already exists");
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
    @Override
    public boolean acceptFriendRequest(User user, User friend) throws SQLException {
        return updateFriendStatus(user, friend, "accepted");
    }

    @Override
    public boolean denyFriendRequest(User user, User friend) throws SQLException {
        return updateFriendStatus(user, friend, "denied");
    }

    @Override
    public boolean blockUser(User user, User friend) throws SQLException {
        return updateFriendStatus(user, friend, "blocked");
    }

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
