package com.cab302.peerpractice.Model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public abstract class UserDAO implements IUserDAO{
    private Connection connection;

    public UserDAO() throws SQLException {
        connection = SQLiteConnection.getInstance();
        createTables();
        insertSampleData();
    }

    private void createTables() {
        // Create tables if they don't exist
        try {
            Statement stmt = connection.createStatement();

            String createUsersQuery = "CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "username VARCHAR(16) NOT NULL UNIQUE,"
                    + "password VARCHAR(24) NOT NULL,"
                    + "first_name VARCHAR(16) NOT NULL,"
                    + "last_name VARCHAR(16) NOT NULL,"
                    + "email VARCHAR(24) NOT NULL,"
                    + "institution VARCHAR(16) NOT NULL,"
                    + "biography VARCHAR(128) NOT NULL DEFAULT 'No biography given'"
                    + ");";
            stmt.execute(createUsersQuery);

            String createFriendsQuery = "CREATE TABLE IF NOT EXISTS friends ("
                    + "friendship_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "user VARCHAR(16) NOT NULL,"
                    + "friend VARCHAR(16) NOT NULL,"
                    + "CONSTRAINT fk_user"
                    + "FOREIGN KEY (user)"
                    + "REFERENCES users(username)"
                    + "ON UPDATE CASCADE"
                    + "ON DELETE CASCADE"
                    + ");";
            stmt.execute(createFriendsQuery);

            String createEventsQuery = "CREATE TABLE IF NOT EXISTS events ("
                    + "event_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT"
                    + "title VARCHAR(24) NOT NULL DEFAULT 'Untitled'"
                    + "description VARCHAR(128) NOT NULL DEFAULT 'No description given'"
                    + "colour_label ENUM('clear', 'red', 'blue', 'yellow', 'green') NOT NULL DEFAULT 'clear'"
                    + "start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + "end_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createEventsQuery);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e);
        }
    }

    private void insertSampleData() {
        // Insert sample data into tables
        try {
            // Clear before inserting
            Statement clearStatement = connection.createStatement();
            String clearQuery = "DELETE FROM contacts";
            clearStatement.execute(clearQuery);

            Statement insertStatement = connection.createStatement();
            String insertQuery = "INSERT INTO users (username, password, first_name, last_name, email, institution) VALUES "
                    + "(),"
                    + "();";
            insertStatement.execute(insertQuery);
        } catch (Exception e) {
            System.err.println("SQLException: " + e);
        }
    }

    // Select single user
    public User findUser(String userID) throws ClassNotFoundException, SQLException {
        // Declare query statement
        String queryStatement =
                "SELECT * FROM users " +
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
        String queryStatement = "SELECT * FROM users;";

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
                "INSERT INTO users (username, password, first_name, email, institution) " +
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
                "UPDATE users " +
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
                "DELETE FROM users " +
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