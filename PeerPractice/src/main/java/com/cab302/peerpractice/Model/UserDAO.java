package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserDAO implements IUserDAO{
    private final Connection connection;

    public UserDAO() throws SQLException, DuplicateUsernameException, DuplicateEmailException {
        connection = SQLiteConnection.getInstance();
        createTables();
    }

    public Connection shareInstance() {
        return connection;
    }

    private void createTables() {
        // Create tables if they don't exist
        try {
            Statement stmt = connection.createStatement();
            stmt.execute("DROP TABLE friends;");

            String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                    + "user_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + "username VARCHAR(16) NOT NULL UNIQUE,"
                    + "password VARCHAR(24) NOT NULL,"
                    + "first_name VARCHAR(16) NOT NULL,"
                    + "last_name VARCHAR(16) NOT NULL,"
                    + "email VARCHAR(24) NOT NULL UNIQUE,"
                    + "institution VARCHAR(16) NOT NULL,"
                    + "biography VARCHAR(128) NOT NULL DEFAULT 'No biography given'"
                    + ");";
            stmt.execute(createUsersTable);

            addColumnIfNotExist("users", "phone", "TEXT NOT NULL DEFAULT ''");
            addColumnIfNotExist("users", "address", "TEXT NOT NULL DEFAULT ''");
            addColumnIfNotExist("users", "date_of_birth", "TEXT DEFAULT ''");
            addColumnIfNotExist("users", "date_format", "TEXT NOT NULL DEFAULT 'dd/MM/yyyy'");
            addColumnIfNotExist("users", "time_format", "TEXT NOT NULL DEFAULT 'HH:mm'");

            String createFriendsTable = "CREATE TABLE IF NOT EXISTS friends ("
                    + "friendship_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + "user VARCHAR(16) NOT NULL,"
                    + "friend VARCHAR(16) NOT NULL,"
                    + "status VARCHAR(16) NOT NULL DEFAULT 'pending' CHECK(status IN ('pending', 'accepted', 'denied', 'blocked')),"
                    + "CONSTRAINT fk_user "
                    + "FOREIGN KEY (user) "
                    + "REFERENCES users(username) "
                    + "ON UPDATE CASCADE "
                    + "ON DELETE CASCADE "
                    + ");";
            stmt.execute(createFriendsTable);

            String createEventsTable = "CREATE TABLE IF NOT EXISTS events ("
                    + "event_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + "title VARCHAR(24) NOT NULL DEFAULT 'Untitled',"
                    + "description VARCHAR(128) NOT NULL DEFAULT 'No description given',"
                    + "colour_label VARCHAR(10) NOT NULL DEFAULT 'clear' CHECK(colour_label IN ('clear', 'red', 'blue', 'yellow', 'green')),"
                    + "start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,"
                    + "end_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP"
                    + ");";
            stmt.execute(createEventsTable);

            String createNotifsTable = "CREATE TABLE IF NOT EXISTS notifications ("
                    + "notif_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,"
                    + "sent_from VARCHAR(16) NOT NULL,"
                    + "received_by VARCHAR(16) NOT NULL,"
                    + "message VARCHAR(128) NOT NULL DEFAULT 'No message given',"
                    + "checked BOOLEAN DEFAULT 'false',"
                    + "CONSTRAINT fk_received "
                    + "FOREIGN KEY (received_by) "
                    + "REFERENCES users(username) "
                    + "ON UPDATE CASCADE "
                    + "ON DELETE CASCADE "
                    + ");";
            stmt.execute(createNotifsTable);
        } catch (SQLException e) {
            System.err.println("SQLException: " + e);
        }
    }

    // Checks if a column exists in the specified table, return true if column exists, false otherwise
    private boolean doesColumnExist(String table, String column) throws SQLException {
        String sql = "PRAGMA table_info(" + table + ");";
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                String name = rs.getString("name");
                if (column.equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Adds a column to the table if it doesn't already exist
    private void addColumnIfNotExist(String table, String column, String typeAndDefault) throws SQLException {
        if (!doesColumnExist(table, column)) {
            try (Statement st = connection.createStatement()) {
                st.executeUpdate("ALTER TABLE " + table + " ADD COLUMN " + column + " " + typeAndDefault);
            }
        }
    }

    // Ensures all columns (and new columns) are mapped into the user
    private User mapUser(ResultSet rs) throws SQLException {
        // Read the columns
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String institution = rs.getString("institution");
        String bio = rs.getString("biography");

        // Construct user using constructor signature in User.java
        User u = new User(userId, firstName, lastName, username, email, password, institution);

        // Avoid failing if a column is missing
        try { u.setPhone(rs.getString("PHONE")); }
            catch (SQLException ignored) {}
        try { u.setAddress(rs.getString("ADDRESS")); }
            catch (SQLException ignored) {}
        try { u.setDateOfBirth(rs.getString("DATE_OF_BIRTH")); }
            catch (SQLException ignored) {}
        try { u.setDateFormat(rs.getString("date_format")); }
            catch (SQLException ignored) {}
        try { u.setTimeFormat(rs.getString("time_format")); }
            catch (SQLException ignored) {}

        return u;
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
            if (searchResults == null) {
                return null;
            } else {
                return getUserFromResults(searchResults);
            }

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
    private User getUserFromResults(ResultSet searchResults) throws SQLException {
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
            user = mapUser(searchResults);
        }
        return user;
    }
    // Converts query into an array of users
    private ObservableList<User> putUsersIntoList(ResultSet searchResults) throws SQLException {
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
            userList.add(mapUser(searchResults));

            // Add user to observable list
            userList.add(user);
        }
        return userList;
    }

    // Creates a new user with a specific ID
    public boolean createUserWithId(String userId, String username, String password, String firstName, String lastName, String email, String institution) throws SQLException {
        // Use the provided user ID
        
        // Use prepared statement to prevent SQL injection
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO users (user_id, username, password, first_name, last_name, email, institution) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);");
        
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

    // Creates a new user with auto-generated ID
    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException {
        // Generate UUID for the new user
        String userId = java.util.UUID.randomUUID().toString();
        return createUserWithId(userId, username, password, firstName, lastName, email, institution);
    }

    // Adds a notification
    public boolean addNotification(String sentFrom, String receivedBy, String message) throws SQLException {
        String sql = "INSERT INTO notifications (sent_from, received_by, message) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sentFrom);
            ps.setString(2, receivedBy);
            ps.setString(3, message);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error occurred during update statement" + e);
            return false;
        }
    }

    // Updates the username of a user with userID
    @Override
    public boolean updateValue(String username, String column, String value) throws SQLException {
        // Whitelist allowed columns to prevent SQL injection via column name
        switch (column) {
            case "first_name":
            case "last_name":
            case "username":
            case "password":
            case "institution":
            case "phone":
            case "address":
            case "date_of_birth":
            case "date_format":
            case "time_format":
                break;
            default:
                throw new SQLException("Invalid column: " + column);
        }
        String sql = "UPDATE users SET " + column + " = ? WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, username);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("Error occurred during update statement" + e);
            return false;
        }
    }

    // Deletes a user from the table
    @Override
    public boolean deleteUser(String userID) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userID);
            int updated = ps.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            System.err.println("Error occurred during update statement" + e);
            return false;
        }
    }

    @Override
    public boolean addUser(User user) {
        try {
            return createUserWithId(user.getUserId(), user.getUsername(), user.getPassword(), user.getFirstName(), 
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
    public List<User> searchByInstitution(String institution) {
        try {
            ObservableList<User> allUsers = findUsers();
            return allUsers.stream()
                    .filter(u -> institution.equals(u.getInstitution()))
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            return new ArrayList<>();
        }
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        // Parameterized SELECT to prevent SQL injection
        String sql = "SELECT * FROM users WHERE email = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                // Map it to user, else return Optional.empty()
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        // Parameterized SELECT to prevent SQL injection
        String sql = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                // Map it to user, else return Optional.empty()
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            return Optional.empty();
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
    public List<User> getAllUsers() {
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
