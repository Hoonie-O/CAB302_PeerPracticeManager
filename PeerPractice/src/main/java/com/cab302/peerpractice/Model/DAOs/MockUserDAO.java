package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Model.Entities.Notification;
import com.cab302.peerpractice.Model.Entities.User;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <hr>
 * Mock (in-memory) implementation of IUserDAO for unit testing without a database.
 *
 * <p>This implementation provides in-memory storage for users and notifications
 * to facilitate testing without requiring a real database connection.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Thread-safe concurrent user and notification storage</li>
 *   <li>User creation with duplicate username/email validation</li>
 *   <li>Flexible user search by various criteria</li>
 *   <li>Basic notification management functionality</li>
 *   <li>Password storage and retrieval support</li>
 * </ul>
 *
 * @see IUserDAO
 * @see User
 * @see Notification
 */
public class MockUserDAO implements IUserDAO {

    /** <hr> In-memory storage for users by username. */
    private final Map<String, User> users = new ConcurrentHashMap<>(); // keyed by username
    /** <hr> In-memory storage for notifications by username. */
    private final Map<String, List<String>> notifications = new ConcurrentHashMap<>();

    // -------------------- CREATE --------------------

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
        String userId = UUID.randomUUID().toString();
        try {
            return createUserWithId(userId, username, password, firstName, lastName, email, institution);
        } catch (Exception e) {
            return false;
        }
    }

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
     * @throws DuplicateUsernameException if the username already exists
     * @throws DuplicateEmailException if the email already exists
     */
    public boolean createUserWithId(String userId, String username, String password, String firstName,
                                    String lastName, String email, String institution)
            throws DuplicateUsernameException, DuplicateEmailException {

        if (users.containsKey(username)) {
            throw new DuplicateUsernameException("Username exists: " + username);
        }
        if (users.values().stream().anyMatch(u -> u.getEmail().equals(email))) {
            throw new DuplicateEmailException("Email exists: " + email);
        }

        User user = new User(userId, firstName, lastName, username, email, password, institution);
        users.put(username, user);
        return true;
    }

    /**
     * <hr>
     * Adds an existing user object to the in-memory storage.
     *
     * @param user the user to add
     * @return true if the user was added successfully
     */
    @Override
    public boolean addUser(User user) {
        try {
            return createUserWithId(user.getUserId(), user.getUsername(), user.getPassword(),
                    user.getFirstName(), user.getLastName(), user.getEmail(), user.getInstitution());
        } catch (Exception e) {
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
     */
    @Override
    public User findUser(String column, String value) {
        switch (column) {
            case "username": return users.get(value);
            case "email":
                return users.values().stream()
                        .filter(u -> u.getEmail().equals(value))
                        .findFirst().orElse(null);
            case "user_id":
                return users.values().stream()
                        .filter(u -> u.getUserId().equals(value))
                        .findFirst().orElse(null);
            default: return null;
        }
    }

    /**
     * <hr>
     * Retrieves all users as an observable list.
     *
     * @return an observable list of all users
     */
    @Override
    public ObservableList<User> findUsers() {
        return FXCollections.observableArrayList(users.values());
    }

    /**
     * <hr>
     * Finds a user by their user ID.
     *
     * @param userId the ID of the user to find
     * @return the user with the specified ID, or null if not found
     */
    @Override
    public User findUserById(String userId) { return findUser("user_id", userId); }

    /**
     * <hr>
     * Retrieves a user by their email address.
     *
     * @param email the email address to search for
     * @return an Optional containing the user if found
     */
    @Override
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(findUser("email", email));
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
        return Optional.ofNullable(findUser("username", username));
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
        return users.values().stream()
                .filter(u -> institution.equals(u.getInstitution()))
                .collect(Collectors.toList());
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
     * Retrieves all users from the in-memory storage.
     *
     * @return a list of all users
     */
    @Override
    public List<User> getAllUsers() { return new ArrayList<>(users.values()); }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Updates a specific field for a user.
     *
     * @param username the username of the user to update
     * @param column the field to update ("first_name", "last_name", "username", "password", "institution", "email")
     * @param value the new value for the field
     * @return true if the field was updated successfully
     * @throws SQLException if an invalid column is specified
     */
    @Override
    public boolean updateValue(String username, String column, String value) throws SQLException {
        User u = users.get(username);
        if (u == null) return false;

        switch (column) {
            case "first_name": u.setFirstName(value); break;
            case "last_name": u.setLastName(value); break;
            case "username":
                users.remove(username);
                u.setUsername(value);
                users.put(value, u);
                break;
            case "password": u.setPassword(value); break;
            case "institution": u.setInstitution(value); break;
            case "email": u.setEmail(value); break;
            default: throw new SQLException("Invalid column: " + column);
        }
        return true;
    }

    /**
     * <hr>
     * Updates a user's information in the in-memory storage.
     *
     * @param user the user with updated information
     * @return true if the user was updated successfully
     */
    @Override
    public boolean updateUser(User user) {
        if (!users.containsKey(user.getUsername())) return false;
        users.put(user.getUsername(), user);
        return true;
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
        user.setPassword(hash);
        return true;
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
     * @param userId the ID of the user to delete
     * @return true if the user was deleted successfully
     */
    @Override
    public boolean deleteUser(String userId) {
        Optional<String> username = users.values().stream()
                .filter(u -> u.getUserId().equals(userId))
                .map(User::getUsername)
                .findFirst();
        username.ifPresent(users::remove);
        return username.isPresent();
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
        return users.remove(user.getUsername()) != null;
    }

    /**
     * <hr>
     * Searches for a user by username.
     *
     * @param username the username to search for
     * @return the user with the specified username, or null if not found
     */
    @Override
    public User searchByUsername(String username) { return users.get(username); }

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
        notifications.computeIfAbsent(receivedBy.getUsername(), k -> new ArrayList<>()).add(message);
        return true;
    }

    /**
     * <hr>
     * Retrieves all notifications for a user (mock implementation).
     *
     * @param user the user to retrieve notifications for
     * @return an empty list (mock implementation)
     */
    @Override
    public List<Notification> getNotificationsForUser(User user) {
        return new ArrayList<>(); // Mock implementation - returns empty list
    }

    /**
     * <hr>
     * Marks a notification as read (mock implementation).
     *
     * @param user the user who owns the notification
     * @param notification the notification to mark as read
     * @return true (mock implementation)
     */
    @Override
    public boolean markNotificationAsRead(User user, Notification notification) {
        return true; // Mock implementation
    }

    /**
     * <hr>
     * Marks all notifications as read for a user (mock implementation).
     *
     * @param user the user whose notifications should be marked as read
     * @return true (mock implementation)
     */
    @Override
    public boolean markAllNotificationsAsRead(User user) {
        return true; // Mock implementation
    }

    /**
     * <hr>
     * Gets the count of unread notifications for a user (mock implementation).
     *
     * @param user the user to check unread notifications for
     * @return 0 (mock implementation)
     */
    @Override
    public int getUnreadNotificationCount(User user) {
        return 0; // Mock implementation
    }

    /**
     * <hr>
     * Removes a notification for a user.
     *
     * @param username the user who owns the notification
     * @param notification the notification to remove
     * @return true if the notification was removed successfully
     */
    @Override
    public boolean removeNotification(User username, Notification notification) {
        if (notification == null) return false;
        List<String> list = notifications.get(username.getUsername());
        if (list == null) return false;
        return list.remove(notification.getMsg());  // compare by message text
    }

    /**
     * <hr>
     * Clears all data from the mock DAO for testing purposes.
     *
     * <p>Resets all storage maps to their initial state.
     */
    public void clear() {
        users.clear();
        notifications.clear();
    }
}