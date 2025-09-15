package com.cab302.peerpractice.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class FriendDAO implements IFriendDAO{
    private final Connection connection;
    private final UserDAO userDAO;

    public FriendDAO() throws SQLException {
        // get database connection and userDAO
        userDAO = new UserDAO();
        connection = userDAO.shareInstance();
    }

    @Override
    public List<Friend> getFriends(User user) throws SQLException {
        // set up statement to search friends table
        String searchQuery = "SELECT friend FROM friends WHERE user = ?";
        PreparedStatement pstmt = connection.prepareStatement(searchQuery);
        pstmt.setString(1, user.getUsername());
        ResultSet results;

        // Execute query statement
        try {
            results = pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Create list of friends
        ObservableList<Friend> friends = FXCollections.observableArrayList();

        while (Objects.requireNonNull(results).next()) {
            String username1 = results.getString("user");
            String username2 = results.getString("friend");
            FriendStatus status = FriendStatus.valueOf(results.getString("status"));

            // Get the user data for associated username for listed friendship
            User user1 = userDAO.findUser("username", username1);
            User user2 = userDAO.findUser("username", username2);

            Friend friend = new Friend(user1, user2, status);
            // Only return accepted friends
            if (friend.getStatus() == FriendStatus.ACCEPTED) {
                friends.add(friend);
            }
        }
        return friends;
    }

    @Override
    public boolean addFriend(User user, User friend) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("INSERT INTO friends (user, friend, status) VALUES (?, ?, ?);");
        stmt.setString(1, user.getUsername());
        stmt.setString(2, friend.getUsername());
        stmt.setString(3, "pending");

        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error adding friend: " + e);
            return false;
        }
    }

    @Override
    public boolean removeFriend(User user, User friend) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM friends WHERE user = ? && friend = ?;");
        stmt.setString(1, user.getUsername());
        stmt.setString(2, friend.getUsername());

        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error removing friend: " + e);
            return false;
        }
    }

    @Override
    public boolean blockUser(User user, User friend) {
        return false;
    }

    @Override
    public boolean acceptFriendRequest(User user, User friend) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE friends SET status = 'accepted' WHERE user = ? && friend = ?;");
        stmt.setString(1, user.getUsername());
        stmt.setString(2, friend.getUsername());

        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error accepting friend: " + e);
            return false;
        }
    }

    @Override
    public boolean denyFriendRequest(User user, User friend) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE friends SET status = 'denied' WHERE user = ? && friend = ?;");
        stmt.setString(1, user.getUsername());
        stmt.setString(2, friend.getUsername());

        try {
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error denying friend: " + e);
            return false;
        }
    }
}
