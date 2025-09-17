package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    public ObservableList<Friend> getFriends(User user) throws SQLException {
        // set up statement to search friends table
        String searchQuery = "SELECT * FROM friends WHERE user = ?;";
        PreparedStatement pstmt = connection.prepareStatement(searchQuery);
        pstmt.setString(1, user.getUsername());
        ResultSet results;

        // Execute query statement
        try {
            results = pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return resultsToList(results);
    }

    // returns false if operation failed
    @Override
    public boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException {
        // checks if friend already on list, and if request not accepted, accept it
        if (checkFriendExists(user, friend)) {
            if (!acceptFriendRequest(user, friend)) {
                System.err.println("DuplicateFriendException: " + new DuplicateFriendException("Friend already exists"));
                return false;
            }
        }

        String searchQuery = "INSERT INTO friends (user, friend, status) VALUES (?, ?, ?);";
        PreparedStatement stmt = connection.prepareStatement(searchQuery);
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
    public void removeFriend(User user, User friend) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("DELETE FROM friends WHERE user = ? AND friend = ?;");
        stmt.setString(1, user.getUsername());
        stmt.setString(2, friend.getUsername());

        try {
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error removing friend: " + e);
        }
    }

    @Override
    public boolean blockUser(User user, User friend) {
        return false;
    }

    @Override
    public boolean acceptFriendRequest(User user, User friend) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement("UPDATE friends SET status = 'accepted' WHERE user = ? AND friend = ?;");
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

    private ObservableList<Friend> resultsToList(ResultSet results) throws SQLException {
        // create list of friends
        ObservableList<Friend> friends = FXCollections.observableArrayList();

        // add each object to list
        while (Objects.requireNonNull(results).next()) {
            String username1 = results.getString("user");
            String username2 = results.getString("friend");
            FriendStatus status = FriendStatus.valueOf(results.getString("status").toUpperCase());

            // Get the user data for associated username for listed friendship
            User user1 = userDAO.findUser("username", username1);
            User user2 = userDAO.findUser("username", username2);

            Friend friend = new Friend(user1, user2, status);
            friends.add(friend);
        }

        return friends;
    }

    private boolean checkFriendExists(User user, User friend) throws SQLException {
        String searchQuery = "SELECT * FROM friends WHERE user = ? AND friend = ?;";
        PreparedStatement pstmt = connection.prepareStatement(searchQuery);
        pstmt.setString(1, user.getUsername());
        pstmt.setString(2, friend.getUsername());
        ResultSet results;

        // Execute query statement
        try {
            results = pstmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        ObservableList<Friend> friends = resultsToList(results);
        return (!friends.isEmpty());
    }
}
