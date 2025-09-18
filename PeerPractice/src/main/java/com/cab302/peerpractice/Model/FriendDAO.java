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
        String searchQuery = String.format("SELECT * FROM friends WHERE user = %s ORDER BY status;", user.getUsername());
        // return list of friends of user
        return resultsToList(SQLQuery(searchQuery));
    }

    // returns false if operation failed
    @Override
    public boolean addFriend(User user, User friend) throws SQLException, DuplicateFriendException {
        // checks: user not adding themselves, friends exists as a user,
        // and friend request not already pending (which accepts if it is)
        if (user == friend) {
            return false;
        } else {
            if (checkFriendExists(user, friend)) {
                if (acceptFriendRequest(user, friend)) {
                    return true;
                } else {
                    System.err.println("DuplicateFriendException: " + new DuplicateFriendException("Friend already exists"));
                    return false;
                }
            }
        }

        String searchQuery = String.format("INSERT INTO friends (user, friend, status) VALUES (%s, %s, %s);", user.getUsername(), friend.getUsername(), "pending");
        return !resultsToList(SQLQuery(searchQuery)).isEmpty();
    }

    @Override
    public boolean removeFriend(User user, User friend) throws SQLException {
        String searchQuery = String.format("DELETE FROM friends WHERE (user = %s AND friend = %s);", user.getUsername(), friend.getUsername());
        return !resultsToList(SQLQuery(searchQuery)).isEmpty();
    }

    @Override
    public boolean acceptFriendRequest(User user, User friend) throws SQLException {
        String searchQuery = String.format("UPDATE friends SET status = 'accepted' WHERE (user = ? AND friend = ?);", user.getUsername(), friend.getUsername());
        return !resultsToList(SQLQuery(searchQuery)).isEmpty();
    }

    @Override
    public boolean denyFriendRequest(User user, User friend) throws SQLException {
        String searchQuery = String.format("UPDATE friends SET status = 'denied' WHERE (user = ? AND friend = ?);", user.getUsername(), friend.getUsername());
        return !resultsToList(SQLQuery(searchQuery)).isEmpty();
    }

    @Override
    public boolean blockUser(User user, User friend) throws SQLException{
        String searchQuery = String.format("UPDATE friends SET status = 'blocked' WHERE (user = ? AND friend = ?);", user.getUsername(), friend.getUsername());
        return !resultsToList(SQLQuery(searchQuery)).isEmpty();
    }

    private boolean checkFriendExists(User user, User friend) throws SQLException {
        String searchQuery = String.format("SELECT * FROM friends WHERE (user = %s AND friend = %s);", user.getUsername(), friend.getUsername());
        // return true if no matching results, false otherwise
        return resultsToList(SQLQuery(searchQuery)).isEmpty();
    }

    private ResultSet SQLQuery(String searchQuery) throws SQLException {
        // create statement and resultset
        PreparedStatement preparedStatement = connection.prepareStatement(searchQuery);
        ResultSet resultSet;

        // execute query statement
        try {
            resultSet = preparedStatement.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return resultSet;
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
}
