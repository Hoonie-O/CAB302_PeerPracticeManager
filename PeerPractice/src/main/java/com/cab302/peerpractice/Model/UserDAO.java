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
                    + "user_id VARCHAR(36) NOT NULL PRIMARY KEY,"
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
                    + "event_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "title VARCHAR(24) NOT NULL DEFAULT 'Untitled',"
                    + "description VARCHAR(128) NOT NULL DEFAULT 'No description given',"
                    + "colour_label ENUM('clear', 'red', 'blue', 'yellow', 'green') NOT NULL DEFAULT 'clear',"
                    + "start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "end_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createEventsTable);

            String createNotifsTable = "CREATE TABLE IF NOT EXISTS notifications ("
                    + "notif_id INT NOT NULL PRIMARY KEY AUTO_INCREMENT,"
                    + "sent_from VARCHAR(16) NOT NULL,"
                    + "received_by VARCHAR(16) NOT NULL,"
                    + "message VARCHAR(128) NOT NULL DEFAULT 'No message given',"
                    + "checked BOOLEAN DEFAULT 'false',"
                    + "CONSTRAINT fk_received"
                    + "FOREIGN KEY (received)"
                    + "REFERENCES users(username)"
                    + "ON UPDATE CASCADE"
                    + "ON DELETE CASCADE"
                    + ");";
            stmt.execute(createNotifsTable);
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
                    + "('hollyfloweer', 'mypassword', 'Holly', 'Spain', 'n11618230@qut.edu.au', 'QUT');";
            stmt.execute(insertQuery);
        } catch (Exception e) {
            System.err.println("SQLException: " + e);
        }
    }

    // Select single user
    @Override
    public User findUser(String column, String value) throws SQLException {
        // Use parameterized query to prevent SQL injection
        String searchQuery = "SELECT * FROM users WHERE " + column + " = ?";
        PreparedStatement pstmt = connection.prepareStatement(searchQuery);
        pstmt.setString(1, value);

        // Execute query statement
        try {
            ResultSet searchResults = pstmt.executeQuery();
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

    @Override
    public User findUserById(String userId) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE user_id = ?");
        statement.setString(1, userId);
        ResultSet resultSet = statement.executeQuery();
        return getUserFromResults(resultSet);
    }

    // Converts a query into user object
    private static User getUserFromResults(ResultSet searchResults) throws SQLException {
        // Create null user
        User user = null;
        // Fill user fields from results
        if (searchResults.next()) {
            String userId = searchResults.getString("USER_ID");
            String firstName = searchResults.getString("FIRST_NAME");
            String lastName = searchResults.getString("LAST_NAME");
            String username = searchResults.getString("USERNAME");
            String email = searchResults.getString("EMAIL");
            String passwordHash = searchResults.getString("PASSWORD");
            String institution = searchResults.getString("INSTITUTION");
            user = new User(userId, firstName, lastName, username, email, passwordHash, institution);
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
        // Generate UUID for the new user
        String userId = java.util.UUID.randomUUID().toString();
        
        // Use prepared statement to prevent SQL injection
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO users (user_id, username, password, first_name, last_name, email, institution) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)");
        
        stmt.setString(1, userId);
        stmt.setString(2, username);
        stmt.setString(3, password);
        stmt.setString(4, firstName);
        stmt.setString(5, lastName);
        stmt.setString(6, email);
        stmt.setString(7, institution);

        // Execute update statement, return true if successful, false if not, check for duplicate exception
        try {
            stmt.executeUpdate();
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

    // Adds a notification
    public boolean addNotification(String sentFrom, String receivedBy, String message) throws SQLException {
        // Declare statement and update query
        Statement stmt = connection.createStatement();
        String updateQuery =
                "INSERT INTO notifications [sent_from, received_by, message) " +
                "VALUES ('" + sentFrom + "','" + receivedBy + "','" + message + "');";

        // Execute update statement, return true if successful, false if not
        try {
            stmt.executeUpdate(updateQuery);
            return true;
        } catch (SQLException e) {
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

    @Override
    public boolean addUser(User user) {
        try {
            return createUser(user.getUsername(), user.getPassword(), user.getFirstName(), 
                            user.getLastName(), user.getEmail(), user.getInstitution());
        } catch (SQLException | DuplicateUsernameException | DuplicateEmailException e) {
            return false;
        }
    }

    @Override
    public boolean deleteUser(User user) {
        try {
            return deleteUser(user.getUserId());
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean updateUser(User user) {
        try {
            return updateValue(user.getUsername(), "first_name", user.getFirstName()) &&
                   updateValue(user.getUsername(), "last_name", user.getLastName()) &&
                   updateValue(user.getUsername(), "email", user.getEmail()) &&
                   updateValue(user.getUsername(), "institution", user.getInstitution());
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void update(User user) {
        updateUser(user);
    }

    @Override
    public User searchByUsername(String username) {
        try {
            return findUser("username", username);
        } catch (SQLException e) {
            return null;
        }
    }

    @Override
    public java.util.List<User> searchByInstitution(String institution) {
        try {
            ObservableList<User> allUsers = findUsers();
            return allUsers.stream()
                    .filter(u -> institution.equals(u.getInstitution()))
                    .collect(java.util.stream.Collectors.toList());
        } catch (SQLException e) {
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public java.util.Optional<User> getUserByEmail(String email) {
        try {
            User user = findUser("email", email);
            return java.util.Optional.ofNullable(user);
        } catch (SQLException e) {
            return java.util.Optional.empty();
        }
    }

    @Override
    public java.util.Optional<User> getUserByUsername(String username) {
        try {
            User user = findUser("username", username);
            return java.util.Optional.ofNullable(user);
        } catch (SQLException e) {
            return java.util.Optional.empty();
        }
    }

    @Override
    public boolean existsByEmail(String email) {
        try {
            return findUser("email", email) != null;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try {
            return findUser("username", username) != null;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public java.util.List<User> getAllUsers() {
        try {
            ObservableList<User> users = findUsers();
            return new java.util.ArrayList<>(users);
        } catch (SQLException e) {
            return new java.util.ArrayList<>();
        }
    }

    @Override
    public boolean storePassword(User user, String hash) {
        try {
            return updateValue(user.getUsername(), "password", hash);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public String getPassword(User user) {
        return user.getPassword();
    }

    @Override
    public boolean addNotification(String username, Notification notification) {
        try {
            return addNotification(username, username, notification.toString());
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public boolean removeNotification(String username, Notification notification) {
        return false;
    }
}