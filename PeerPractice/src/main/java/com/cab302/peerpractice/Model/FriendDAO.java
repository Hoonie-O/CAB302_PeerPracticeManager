package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateFriendException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javax.sound.midi.SysexMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class FriendDAO implements IFriendDAO{
    private final Connection connection;
    private final IUserDAO userDAO;

    public FriendDAO(IUserDAO userDAO) throws SQLException {
        // get database connection and userDAO
        this.userDAO = userDAO;
        connection = SQLiteConnection.getInstance();
    }

    @Override
    public ObservableList<Friend> getFriends(User user) throws SQLException {
        String searchQuery = String.format("SELECT * FROM friends WHERE user = '%s' ORDER BY status;", user.getUsername());
        //System.out.println(searchQuery);
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
            } else {
                String searchQuery = String.format("INSERT INTO friends (user, friend, status) VALUES ('%s', '%s', 'pending');", user.getUsername(), friend.getUsername());
                //System.out.println(searchQuery);
                return resultsToList(SQLQuery(searchQuery)) != null;
            }
        }
    }

    @Override
    public boolean removeFriend(User user, User friend) throws SQLException {
        String updateAction = String.format("DELETE FROM friends WHERE user = '%s' AND friend = '%s';", user.getUsername(), friend.getUsername());
        //System.out.println(searchQuery);
        // return true if update changed rows
        return SQLUpdate(updateAction) > 0;
    }

    @Override
    public boolean acceptFriendRequest(User user, User friend) throws SQLException {
        String updateAction = String.format("UPDATE friends SET status = 'accepted' WHERE user = '%s' AND friend = '%s';", user.getUsername(), friend.getUsername());
        //System.out.println(searchQuery);
        // return true if update changed rows
        System.out.println(SQLUpdate(updateAction) > 0);
        return SQLUpdate(updateAction) > 0;
    }

    @Override
    public boolean denyFriendRequest(User user, User friend) throws SQLException {
        String updateAction = String.format("UPDATE friends SET status = 'denied' WHERE user = '%s' AND friend = '%s';", user.getUsername(), friend.getUsername());
        //System.out.println(searchQuery);
        // return true if update changed rows
        return SQLUpdate(updateAction) > 0;
    }

    @Override
    public boolean blockUser(User user, User friend) throws SQLException{
        String updateAction = String.format("UPDATE friends SET status = 'blocked' WHERE user = '%s' AND friend = '%s';", user.getUsername(), friend.getUsername());
        //System.out.println(searchQuery);
        // return true if update changed rows
        return SQLUpdate(updateAction) > 0;
    }

    private boolean checkFriendExists(User user, User friend) throws SQLException {
        String searchQuery = String.format("SELECT * FROM friends WHERE user = '%s' AND friend = '%s';", user.getUsername(), friend.getUsername());
        //System.out.println(searchQuery);
        // return true if no matching results, false otherwise
        return SQLQuery(searchQuery) == null;
    }

    private ResultSet SQLQuery(String searchQuery) throws SQLException {
        // create statement and resultset
        PreparedStatement preparedStatement = connection.prepareStatement(searchQuery);
        ResultSet resultSet = null;
        boolean isInsert = searchQuery.contains("INSERT");

        // execute query statement
        try {
            if (!isInsert) {
                resultSet = preparedStatement.executeQuery();
            } else {
                preparedStatement.executeQuery();
            }
        } catch (SQLException e) {
            System.err.println("Query could not be executed: " + searchQuery + e);
        }

        return resultSet;
    }

    private int SQLUpdate(String searchQuery) throws SQLException {
        // create statement
        PreparedStatement preparedStatement = connection.prepareStatement(searchQuery);
        int rowsChanged = 0; // initial value 0 = no rows affected

        // execute update statement
        try {
            rowsChanged = preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Update could not be executed: " + searchQuery + e);
        }

        return rowsChanged;
    }

    private ObservableList<Friend> resultsToList(ResultSet results) throws SQLException {
        // create list of friends
        ObservableList<Friend> friends = FXCollections.observableArrayList();

        // return null if no results found
        if (results == null) {
            return null;
        }

        // add each object to list
        while (results.next()) {
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
