package com.cab302.peerpractice.Model;

import javafx.collections.ObservableList;

import com.cab302.peerpractice.ConnectionDB;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User data access object
 */
public abstract class UserDAO implements IUserDAO{
    // Select single user
    public static User findUser(String userID) throws ClassNotFoundException, SQLException {
        // Declare query statement
        String queryStatement = "SELECT * FROM students WHERE userID = " + userID;

        // Execute query statement
        try {
            ResultSet searchResults = ConnectionDB.executeQuery(queryStatement);

            return singleUserFromResults(searchResults);
        } catch (SQLException e) {
            System.out.println("Error occured when searching user " + userID + e);
            throw e;
        }
    }

    public static ObservableList<User> findUsers() throws ClassNotFoundException, SQLException {

    }

    public static User singleUserFromResults(ResultSet searchResults) throws SQLException {
        User user = null;
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
}
