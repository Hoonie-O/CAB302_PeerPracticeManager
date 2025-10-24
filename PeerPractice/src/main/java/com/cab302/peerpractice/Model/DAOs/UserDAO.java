package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Model.Entities.Notification;
import com.cab302.peerpractice.Model.Entities.FriendRequestNotification;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * <hr>
 * Database implementation of IUserDAO for persistent user and notification storage.
 *
 * <p>This implementation provides SQLite-based persistent storage for users
 * and their notifications with comprehensive user management capabilities.
 *
 * <p> Key features include:
 * <ul>
 *   <li>User registration with duplicate validation</li>
 *   <li>Flexible user search and retrieval methods</li>
 *   <li>Notification system with read status tracking</li>
 *   <li>User profile management with extended fields</li>
 *   <li>Password storage and retrieval</li>
 * </ul>
 *
 * @see IUserDAO
 * @see User
 * @see Notification
 */
public class UserDAO implements IUserDAO {
    /** <hr> SQLite database connection instance. */
    private final Connection connection;

    /**
     * <hr>
     * Constructs a new UserDAO and initializes database tables.
     *
     * @throws SQLException if database connection or table creation fails
     */
    public UserDAO() throws SQLException {
        connection = SQLiteConnection.getInstance(); // persistent DB
        ensureTables(); // create tables if missing
    }

    /**
     * <hr>
     * Provides access to the database connection instance.
     *
     * @return the database connection instance
     */
    public Connection shareInstance() { return connection; }

    // -------------------- TABLE CREATION --------------------

    /**
     * <hr>
     * Creates the necessary database tables if they don't exist.
     *
     * <p>Creates tables for users and notifications with appropriate
     * constraints and relationships.
     *
     * @throws SQLException if table creation fails
     */
    private void ensureTables() throws SQLException {
        String usersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "user_id TEXT PRIMARY KEY, " +
                "username TEXT UNIQUE NOT NULL, " +
                "password TEXT NOT NULL, " +
                "first_name TEXT, " +
                "last_name TEXT, " +
                "email TEXT UNIQUE NOT NULL, " +
                "institution TEXT, " +
                "biography TEXT, " +
                "phone TEXT, " +
                "address TEXT, " +
                "date_of_birth TEXT, " +
                "date_format TEXT, " +
                "time_format TEXT" +
                ");";

        String notificationsTable = "CREATE TABLE IF NOT EXISTS notifications (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "sent_from TEXT, " +
                "received_by TEXT, " +
                "message TEXT, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "read_status INTEGER DEFAULT 0, " +
                "notification_type TEXT DEFAULT 'friend_request', " +
                "FOREIGN KEY(sent_from) REFERENCES users(user_id), " +
                "FOREIGN KEY(received_by) REFERENCES users(user_id)" +
                ");";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(usersTable);
            stmt.execute(notificationsTable);
        }
    }

    // -------------------- MAP --------------------

    /**
     * <hr>
     * Maps a database ResultSet row to a User object.
     *
     * @param rs the ResultSet containing user data
     * @return a User object populated with data from the ResultSet
     * @throws SQLException if database access error occurs
     */
    private User mapUser(ResultSet rs) throws SQLException {
        String userId = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String firstName = rs.getString("first_name");
        String lastName = rs.getString("last_name");
        String email = rs.getString("email");
        String institution = rs.getString("institution");
        String bio = rs.getString("biography");

        User u = new User(userId, firstName, lastName, username, email, password, institution);

        try { u.setPhone(rs.getString("phone")); } catch (SQLException ignored) {}
        try { u.setAddress(rs.getString("address")); } catch (SQLException ignored) {}
        try { u.setDateOfBirth(rs.getString("date_of_birth")); } catch (SQLException ignored) {}
        try { u.setDateFormat(rs.getString("date_format")); } catch (SQLException ignored) {}
        try { u.setTimeFormat(rs.getString("time_format")); } catch (SQLException ignored) {}
        try { u.setBio(bio); } catch (Exception ignored) {}

        return u;
    }

    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Creates a new user with a specified user ID.
     *
     * @param userId the ID to assign to the new user
     * @param username the username for the new user
     * @param password the password for the new user
     * @param firstName the first name of the new user
     * @param lastName the last name of the new user
     * @param email the email address of the new user
     * @param institution the institution of the new user
     * @return true if the user was created successfully
     * @throws SQLException if database operation fails
     * @throws DuplicateUsernameException if the username already exists
     * @throws DuplicateEmailException if the email already exists
     */
    public boolean createUserWithId(String userId, String username, String password, String firstName,
                                    String lastName, String email, String institution)
            throws SQLException, DuplicateUsernameException, DuplicateEmailException {

        String sql = "INSERT INTO users (user_id, username, password, first_name, last_name, email, institution) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?);";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, username);
            stmt.setString(3, password);
            stmt.setString(4, firstName);
            stmt.setString(5, lastName);
            stmt.setString(6, email);
            stmt.setString(7, institution);

            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            String errorMsg = e.getMessage().toLowerCase();
            if (errorMsg.contains("username")) throw new DuplicateUsernameException("Username exists");
            if (errorMsg.contains("email")) throw new DuplicateEmailException("Email exists");
            throw e;
        }
    }

    /**
     * <hr>
     * Creates a new user with auto-generated user ID.
     *
     * @param username the username for the new user
     * @param password the password for the new user
     * @param firstName the first name of the new user
     * @param lastName the last name of the new user
     * @param email the email address of the new user
     * @param institution the institution of the new user
     * @return true if the user was created successfully
     */
    @Override
    public boolean createUser(String username, String password, String firstName,
                              String lastName, String email, String institution) {
        String userId = java.util.UUID.randomUUID().toString();
        try {
            return createUserWithId(userId, username, password, firstName, lastName, email, institution);
        } catch (SQLException | DuplicateUsernameException | DuplicateEmailException e) {
            return false;
        }
    }

    // -------------------- READ --------------------

    /**
     * <hr>
     * Finds a user by searching a specific column for a value.
     *
     * @param column the column to search ("username", "email", or "user_id")
     * @param value the value to search for
     * @return the user matching the criteria, or null if not found
     * @throws SQLException if database operation fails
     */
    @Override
    public User findUser(String column, String value) throws SQLException {
        String sql = "SELECT * FROM users WHERE " + column + " = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapUser(rs) : null;
            }
        }
    }

    /**
     * <hr>
     * Retrieves all users as an observable list.
     *
     * @return an observable list of all users
     * @throws SQLException if database operation fails
     */
    @Override
    public ObservableList<User> findUsers() throws SQLException {
        String sql = "SELECT * FROM users;";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            ObservableList<User> list = FXCollections.observableArrayList();
            while (rs.next()) list.add(mapUser(rs));
            return list;
        }
    }

    /**
     * <hr>
     * Finds a user by their user ID.
     *
     * @param userId the ID of the user to find
     * @return the user with the specified ID, or null if not found
     * @throws SQLException if database operation fails
     */
    @Override
    public User findUserById(String userId) throws SQLException { return findUser("user_id", userId); }

    /**
     * <hr>
     * Retrieves a user by their email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        try { return Optional.ofNullable(findUser("email", email)); }
        catch (SQLException e) { return Optional.empty(); }
    }

    /**
     * <hr>
     * Retrieves a user by their username.
     *
     * @param username the username to search for
     * @return an Optional containing the user if found
     */
    @Override
    public Optional<User> getUserByUsername(String username) {
        try { return Optional.ofNullable(findUser("username", username)); }
        catch (SQLException e) { return Optional.empty(); }
    }

    /**
     * <hr>
     * Searches for users by institution.
     *
     * @param institution the institution to search for
     * @return a list of users from the specified institution
     */
    @Override
    public List<User> searchByInstitution(String institution) {
        try { return findUsers().stream()
                .filter(u -> institution.equals(u.getInstitution()))
                .collect(Collectors.toList()); }
        catch (SQLException e) { return new ArrayList<>(); }
    }

    /**
     * <hr>
     * Checks if a user exists with the specified email.
     *
     * @param email the email to check
     * @return true if a user with the email exists
     */
    @Override
    public boolean existsByEmail(String email) { return getUserByEmail(email).isPresent(); }

    /**
     * <hr>
     * Checks if a user exists with the specified username.
     *
     * @param username the username to check
     * @return true if a user with the username exists
     */
    @Override
    public boolean existsByUsername(String username) { return getUserByUsername(username).isPresent(); }

    /**
     * <hr>
     * Retrieves all users from the database.
     *
     * @return a list of all users
     */
    @Override
    public List<User> getAllUsers() {
        try { return new ArrayList<>(findUsers()); }
        catch (SQLException e) { return new ArrayList<>(); }
    }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Updates a specific field for a user.
     *
     * @param username the username of the user to update
     * @param column the field to update ("first_name", "last_name", "username", "password", "institution", "email", etc.)
     * @param value the new value for the field
     * @return true if the field was updated successfully
     * @throws SQLException if an invalid column is specified or database operation fails
     */
    @Override
    public boolean updateValue(String username, String column, String value) throws SQLException {
        switch (column) {
            case "first_name": case "last_name": case "username": case "password":
            case "institution": case "phone": case "address": case "date_of_birth":
            case "date_format": case "time_format": case "email": break;
            default: throw new SQLException("Invalid column: " + column);
        }

        String sql = "UPDATE users SET " + column + " = ? WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * <hr>
     * Updates a user's information in the database.
     *
     * @param user the user with updated information
     * @return true if the user was updated successfully
     */
    @Override
    public boolean updateUser(User user) {
        try {
            return updateValue(user.getUsername(), "first_name", user.getFirstName()) &&
                    updateValue(user.getUsername(), "last_name", user.getLastName()) &&
                    updateValue(user.getUsername(), "email", user.getEmail()) &&
                    updateValue(user.getUsername(), "institution", user.getInstitution());
        } catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Updates a user (alias for updateUser).
     *
     * @param user the user to update
     */
    @Override
    public void update(User user) { updateUser(user); }

    /**
     * <hr>
     * Stores a password hash for a user.
     *
     * @param user the user to store the password for
     * @param hash the password hash to store
     * @return true if the password was stored successfully
     */
    @Override
    public boolean storePassword(User user, String hash) {
        try { return updateValue(user.getUsername(), "password", hash); }
        catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Retrieves the password hash for a user.
     *
     * @param user the user to retrieve the password for
     * @return the password hash
     */
    @Override
    public String getPassword(User user) { return user.getPassword(); }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Deletes a user by their user ID.
     *
     * @param userID the ID of the user to delete
     * @return true if the user was deleted successfully
     * @throws SQLException if database operation fails
     */
    @Override
    public boolean deleteUser(String userID) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userID);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * <hr>
     * Adds an existing user object to the database.
     *
     * @param user the user to add
     * @return true if the user was added successfully
     */
    @Override
    public boolean addUser(User user) {
        try { return createUserWithId(user.getUserId(), user.getUsername(), user.getPassword(),
                user.getFirstName(), user.getLastName(), user.getEmail(), user.getInstitution()); }
        catch (SQLException | DuplicateUsernameException | DuplicateEmailException e) { return false; }
    }

    /**
     * <hr>
     * Deletes a user by user object.
     *
     * @param user the user to delete
     * @return true if the user was deleted successfully
     */
    @Override
    public boolean deleteUser(User user) {
        try { return deleteUser(user.getUserId()); }
        catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Searches for a user by username.
     *
     * @param username the username to search for
     * @return the user with the specified username, or null if not found
     */
    @Override
    public User searchByUsername(String username) {
        try { return findUser("username", username); }
        catch (SQLException e) { return null; }
    }

    // -------------------- NOTIFICATIONS --------------------

    /**
     * <hr>
     * Adds a notification for a user.
     *
     * @param sentFrom the user who sent the notification
     * @param receivedBy the user who receives the notification
     * @param message the notification message
     * @return true if the notification was added successfully
     */
    @Override
    public boolean addNotification(User sentFrom, User receivedBy, String message) {
        String sql = "INSERT INTO notifications (sent_from, received_by, message) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, sentFrom.getUserId());
            ps.setString(2, receivedBy.getUserId());
            ps.setString(3, message);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Retrieves all notifications for a user.
     *
     * @param user the user to retrieve notifications for
     * @return a list of notifications for the specified user
     */
    @Override
    public List<Notification> getNotificationsForUser(User user) {
        String sql = "SELECT n.*, u.* FROM notifications n " +
                "JOIN users u ON n.sent_from = u.user_id " +
                "WHERE n.received_by = ? " +
                "ORDER BY n.created_at DESC";
        List<Notification> notifications = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    User from = mapUser(rs);
                    FriendRequestNotification notif = new FriendRequestNotification(from, user);

                    // Set timestamp
                    try {
                        String createdAt = rs.getString("created_at");
                        if (createdAt != null) {
                            notif.setCreatedAt(java.time.LocalDateTime.parse(createdAt));
                        }
                    } catch (Exception ignored) {}

                    // Set read status
                    try {
                        int readStatus = rs.getInt("read_status");
                        if (readStatus == 1) {
                            notif.markAsRead();
                        }
                    } catch (Exception ignored) {}

                    notifications.add(notif);
                }
            }
        } catch (SQLException e) {
            // Return empty list if query fails
        }
        return notifications;
    }

    /**
     * <hr>
     * Removes a notification for a user.
     *
     * @param user the user who owns the notification
     * @param notification the notification to remove
     * @return true if the notification was removed successfully
     */
    @Override
    public boolean removeNotification(User user, Notification notification) {
        String sql = "DELETE FROM notifications WHERE received_by = ? AND sent_from = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, notification.getFrom().getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Mark a notification as read.
     *
     * @param user The user receiving the notification
     * @param notification The notification to mark as read
     * @return true if successfully updated
     */
    public boolean markNotificationAsRead(User user, Notification notification) {
        String sql = "UPDATE notifications SET read_status = 1 WHERE received_by = ? AND sent_from = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            ps.setString(2, notification.getFrom().getUserId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Mark all notifications for a user as read.
     *
     * @param user The user
     * @return true if successfully updated
     */
    public boolean markAllNotificationsAsRead(User user) {
        String sql = "UPDATE notifications SET read_status = 1 WHERE received_by = ? AND read_status = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            return ps.executeUpdate() >= 0;
        } catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Get count of unread notifications for a user.
     *
     * @param user The user
     * @return Count of unread notifications
     */
    public int getUnreadNotificationCount(User user) {
        String sql = "SELECT COUNT(*) as count FROM notifications WHERE received_by = ? AND read_status = 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("count");
                }
            }
        } catch (SQLException e) {
            // Return 0 if query fails
        }
        return 0;
    }
}