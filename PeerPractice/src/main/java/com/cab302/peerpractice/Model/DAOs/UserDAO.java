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

public class UserDAO implements IUserDAO {
    private final Connection connection;

    public UserDAO() throws SQLException {
        connection = SQLiteConnection.getInstance(); // persistent DB
        ensureTables(); // create tables if missing
    }

    public Connection shareInstance() { return connection; }

    // -------------------- TABLE CREATION --------------------
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

    @Override
    public User findUserById(String userId) throws SQLException { return findUser("user_id", userId); }

    @Override
    public Optional<User> getUserByEmail(String email) {
        try { return Optional.ofNullable(findUser("email", email)); }
        catch (SQLException e) { return Optional.empty(); }
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        try { return Optional.ofNullable(findUser("username", username)); }
        catch (SQLException e) { return Optional.empty(); }
    }

    @Override
    public List<User> searchByInstitution(String institution) {
        try { return findUsers().stream()
                .filter(u -> institution.equals(u.getInstitution()))
                .collect(Collectors.toList()); }
        catch (SQLException e) { return new ArrayList<>(); }
    }

    @Override
    public boolean existsByEmail(String email) { return getUserByEmail(email).isPresent(); }

    @Override
    public boolean existsByUsername(String username) { return getUserByUsername(username).isPresent(); }

    @Override
    public List<User> getAllUsers() {
        try { return new ArrayList<>(findUsers()); }
        catch (SQLException e) { return new ArrayList<>(); }
    }

    // -------------------- UPDATE --------------------
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

    @Override
    public boolean updateUser(User user) {
        try {
            return updateValue(user.getUsername(), "first_name", user.getFirstName()) &&
                    updateValue(user.getUsername(), "last_name", user.getLastName()) &&
                    updateValue(user.getUsername(), "email", user.getEmail()) &&
                    updateValue(user.getUsername(), "institution", user.getInstitution());
        } catch (SQLException e) { return false; }
    }

    @Override
    public void update(User user) { updateUser(user); }

    @Override
    public boolean storePassword(User user, String hash) {
        try { return updateValue(user.getUsername(), "password", hash); }
        catch (SQLException e) { return false; }
    }

    @Override
    public String getPassword(User user) { return user.getPassword(); }

    // -------------------- DELETE --------------------
    @Override
    public boolean deleteUser(String userID) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userID);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public boolean addUser(User user) {
        try { return createUserWithId(user.getUserId(), user.getUsername(), user.getPassword(),
                user.getFirstName(), user.getLastName(), user.getEmail(), user.getInstitution()); }
        catch (SQLException | DuplicateUsernameException | DuplicateEmailException e) { return false; }
    }

    @Override
    public boolean deleteUser(User user) {
        try { return deleteUser(user.getUserId()); }
        catch (SQLException e) { return false; }
    }

    @Override
    public User searchByUsername(String username) {
        try { return findUser("username", username); }
        catch (SQLException e) { return null; }
    }

    // -------------------- NOTIFICATIONS --------------------
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
     * Mark a notification as read
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
     * Mark all notifications for a user as read
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
     * Get count of unread notifications for a user
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