package com.cab302.peerpractice.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import com.cab302.peerpractice.ConnectionDB;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User data access object
 */
public abstract class UserDAO implements IUserDAO{
    // Select single user
    public User findUser(String userID) throws ClassNotFoundException, SQLException {
        // Declare query statement
        String queryStatement =
                "SELECT * FROM students " +
                "WHERE userID = " + userID + ";";

        // Execute query statement
        try {
            ResultSet searchResults = ConnectionDB.executeQuery(queryStatement);
            return getUserFromResults(searchResults);

        } catch (SQLException e) {
            System.out.println("Error occurred when searching user " + userID + e);
            throw e;
        }
    }
    // Select all users
    public ObservableList<User> findUsers() throws ClassNotFoundException, SQLException {
        // Declare query statement
        String queryStatement = "SELECT * FROM students;";

        // Execute query statement
        try {
            ResultSet searchResults = ConnectionDB.executeQuery(queryStatement);
            // Convert the search results into an observable list
            return putUsersIntoList(searchResults);

        } catch (SQLException e) {
            System.out.println("Error occurred when searching user " + e);
            throw e;
        }
    }

    // Converts a query into user object
    private static User getUserFromResults(ResultSet searchResults) throws SQLException {
        // Create null user
        User user = null;
        // Fill user fields from results
        if (searchResults.next()) {
            user = new User();
            user.setUserID(searchResults.getInt("USER_ID"));
            user.setUsername(searchResults.getString("USERNAME"));
            user.setPassword(searchResults.getString("PASSWORD"));
            user.setFirstName(searchResults.getString("FIRST_NAME"));
            user.setLastName(searchResults.getString("LAST_NAME"));
            user.setEmail(searchResults.getString("EMAIL"));
            user.setInstitution(searchResults.getString("INSTITUTION"));
            user.setBiography(searchResults.getString("BIOGRAPHY"));
        }
        return user;
    }
    // Converts query into an array of users
    private static ObservableList<User> putUsersIntoList(ResultSet searchResults) throws SQLException {
        // Create observable list of users
        ObservableList<User> userList = FXCollections.observableArrayList();

        while (searchResults.next()) {
            User user = new User();
            user.setUserID(searchResults.getInt("USER_ID"));
            user.setUsername(searchResults.getString("USERNAME"));
            user.setPassword(searchResults.getString("PASSWORD"));
            user.setFirstName(searchResults.getString("FIRST_NAME"));
            user.setLastName(searchResults.getString("LAST_NAME"));
            user.setEmail(searchResults.getString("EMAIL"));
            user.setInstitution(searchResults.getString("INSTITUTION"));
            user.setBiography(searchResults.getString("BIOGRAPHY"));

            // Add user to observable list
            userList.add(user);
        }
        return userList;
    }

    // Creates a new user
    public void createUser(String username, String password, String firstName, String email, String institution) throws ClassNotFoundException, SQLException {
        // Declare update statement
        String updateStatement =
                "INSERT INTO students (username, password, first_name, email, institution) " +
                "VALUES ('" + username + "','" + password + "','" + firstName + "','" + email + "','" + institution + "');";

        // Execute update statement
        try {
            ConnectionDB.executeUpdate(updateStatement);
        } catch (SQLException e) {
            System.out.println("Error occurred during update statement" + e);
            throw e;
        }
    }

    // Updates the username of a user with userID
    public void updateUsername(String userID, String username) throws ClassNotFoundException, SQLException {
        // Declare update statement
        String updateStatement =
                "UPDATE students " +
                "SET username = '" + username + "' " +
                "WHERE user_id = " + userID + ";";

        // Execute update statement
        try {
            ConnectionDB.executeUpdate(updateStatement);
        } catch (SQLException e) {
            System.out.println("Error occurred during update statement" + e);
            throw e;
        }
    }

    // Deletes a user from the table
    public void deleteUser(String userID) throws ClassNotFoundException, SQLException {
        // Declare update statement
        String updateStatement =
                "DELETE FROM students " +
                "WHERE user_id = " + userID + ";";

        // Execute update statement
        try {
            ConnectionDB.executeUpdate(updateStatement);
        } catch (SQLException e) {
            System.out.println("Error occurred during update statement" + e);
            throw e;
        }
    }
}