package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class UserDAO implements IUserDAO{
    private final Connection connection;

    public UserDAO() throws SQLException, DuplicateUsernameException, DuplicateEmailException {
        connection = SQLiteConnection.getInstance();
        createTables();
        insertSampleData();
    }

    private void createTables() {
        // Create tables if they don't exist
        try {
            Statement stmt = connection.createStatement();

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "username VARCHAR(16) NOT NULL UNIQUE,"
                    + "password VARCHAR(24) NOT NULL,"
                    + "first_name VARCHAR(16) NOT NULL,"
                    + "last_name VARCHAR(16) NOT NULL,"
                    + "email VARCHAR(24) NOT NULL UNIQUE,"
                    + "institution VARCHAR(16) NOT NULL,"
                    + "biography VARCHAR(128) NOT NULL DEFAULT 'No biography given'"
                    + ");";
            stmt.execute(createUsersTable);

            String createFriendsTable = "CREATE TABLE IF NOT EXISTS friends ("
                    + "friendship_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "user VARCHAR(16) NOT NULL,"
                    + "friend VARCHAR(16) NOT NULL,"
                    + "CONSTRAINT fk_user"
                    + "FOREIGN KEY (user)"
                    + "REFERENCES users(username)"
                    + "ON UPDATE CASCADE"
                    + "ON DELETE CASCADE"
                    + ");";
            stmt.execute(createFriendsTable);

            String createEventsTable = "CREATE TABLE IF NOT EXISTS events ("
                    + "event_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT"
                    + "title VARCHAR(24) NOT NULL DEFAULT 'Untitled'"
                    + "description VARCHAR(128) NOT NULL DEFAULT 'No description given'"
                    + "colour_label ENUM('clear', 'red', 'blue', 'yellow', 'green') NOT NULL DEFAULT 'clear'"
                    + "start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + "end_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createEventsTable);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e);
        }
    }

    private void insertSampleData() {
        // Insert sample data into tables
        try {
            // Clear before inserting
            Statement stmt = connection.createStatement();
            String clearQuery = "DELETE FROM contacts";
            stmt.execute(clearQuery);

            String insertQuery = "INSERT INTO users (username, password, first_name, last_name, email, institution) VALUES "
                    + "('hollyfloweer', 'mypassword', 'Holly, 'Spain', 'n11618230@qut.edu.au', 'QUT');";
            stmt.execute(insertQuery);
        } catch (Exception e) {
            System.err.println("SQLException: " + e);
        }
    }

    // Select single user
    @Override
    public User findUser(String column, String value) throws SQLException {
        // Declare query statement
        Statement stmt = connection.createStatement();
        String searchQuery =
                "SELECT * FROM users " +
                "WHERE " + column + " = " + value + ";";

        // Execute query statement
        try {
            ResultSet searchResults = stmt.executeQuery(searchQuery);
            return getUserFromResults(searchResults);

        } catch (SQLException e) {
            System.out.println("Error occurred when searching user " + value + e);
            throw e;
        }
    }
    // Select all users
    @Override
    public ObservableList<User> findUsers() throws SQLException {
        // Declare query statement
        Statement stmt = connection.createStatement();
        String searchQuery = "SELECT * FROM users;";

        // Execute query statement
        try {
            ResultSet searchResults = stmt.executeQuery(searchQuery);
            // Convert the search results into an observable list
            return putUsersIntoList(searchResults);

        } catch (SQLException e) {
            System.out.println("Error occurred when searching users " + e);
            throw e;
        }
    }

    // Converts a query into user object
    private static User getUserFromResults(ResultSet searchResults) throws SQLException {
        // Create null user
        User user = null;
        // Fill user fields from results
        if (searchResults.next()) {
            String firstName = (searchResults.getString("FIRST_NAME"));
            String lastName = (searchResults.getString("LAST_NAME"));
            String username = (searchResults.getString("USERNAME"));
            String email = (searchResults.getString("EMAIL"));
            String passwordHash = (searchResults.getString("PASSWORD"));
            String institution = (searchResults.getString("INSTITUTION"));
            user = new User(firstName, lastName, username, email, passwordHash, institution);
        }
        return user;
    }
    // Converts query into an array of users
    private static ObservableList<User> putUsersIntoList(ResultSet searchResults) throws SQLException {
        // Create observable list of users
        ObservableList<User> userList = FXCollections.observableArrayList();

        while (searchResults.next()) {
            String firstName = (searchResults.getString("FIRST_NAME"));
            String lastName = (searchResults.getString("LAST_NAME"));
            String username = (searchResults.getString("USERNAME"));
            String email = (searchResults.getString("EMAIL"));
            String passwordHash = (searchResults.getString("PASSWORD"));
            String institution = (searchResults.getString("INSTITUTION"));
            User user = new User(firstName, lastName, username, email, passwordHash, institution);

            // Add user to observable list
            userList.add(user);
        }
        return userList;
    }

    // Creates a new user
    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException {
        // Declare statement and update query
        Statement stmt = connection.createStatement();
        String updateQuery =
                "INSERT INTO users (username, password, first_name, last_name, email, institution) " +
                "VALUES ('" + username + "','" + password + "','" + firstName + "','" + lastName + "','" + email + "','" + institution + "');";

        // Execute update statement, return true if successful, false if not, check for duplicate exception
        try {
            stmt.executeUpdate(updateQuery);
            return true;
        } catch (SQLException e) {
            String errorMsg = e.getMessage();
            if (errorMsg.contains("username")) {
                throw new DuplicateUsernameException("Username already exists");
            } else if (errorMsg.contains("email")) {
                throw new DuplicateEmailException("Email already exists");
            }
            System.err.println("Error occurred during update statement" + e);
            return false;
        }
    }

    // Updates the username of a user with userID
    @Override
    public boolean updateValue(String username, String column, String value) throws SQLException {
        // Declare statement and update query
        Statement stmt = connection.createStatement();
        String updateQuery =
                "UPDATE users " +
                "SET " + column + " = '" + value + "' " +
                "WHERE username = " + username + ";";

        // Execute update statement, return true if successful, false if not
        try {
            stmt.executeUpdate(updateQuery);
            return true;
        } catch (SQLException e) {
            System.err.println("Error occurred during update statement" + e);
            return false;
        }
    }

    // Deletes a user from the table
    @Override
    public boolean deleteUser(String userID) throws SQLException {
        // Declare statement and update query
        Statement stmt = connection.createStatement();
        String updateQuery =
                "DELETE FROM users " +
                "WHERE user_id = " + userID + ";";

        // Execute update statement, return true if successful, false if not
        try {
            stmt.executeUpdate(updateQuery);
            return true;
        } catch (SQLException e) {
            System.err.println("Error occurred during update statement" + e);
            return false;
        }
    }
}