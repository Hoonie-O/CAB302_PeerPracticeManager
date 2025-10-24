package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Exceptions.DuplicateEmailException;
import com.cab302.peerpractice.Model.Entities.Notification;
import com.cab302.peerpractice.Model.Entities.User;
import javafx.collections.ObservableList;
import java.sql.SQLException;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing user accounts and user-related operations.
 *
 * <p>This interface defines the contract for user data operations, providing
 * comprehensive methods for user management, authentication, notification
 * handling, and user profile operations in the peer practice system.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Complete user lifecycle management (registration, update, deletion)</li>
 *   <li>User authentication and password management</li>
 *   <li>Notification system integration</li>
 *   <li>Flexible user search and query capabilities</li>
 *   <li>Duplicate prevention and uniqueness enforcement</li>
 * </ul>
 *
 * @see User
 * @see Notification
 * @see DuplicateUsernameException
 * @see DuplicateEmailException
 */
public interface IUserDAO {
    /**
     * <hr>
     * Finds a user by searching a specific column for a value.
     *
     * <p>Searches the user database using a flexible column-based approach,
     * allowing queries by username, email, or other user attributes
     * with precise matching.
     *
     * @param column the database column to search (e.g., "username", "email")
     * @param value the value to match in the specified column
     * @return the User object if found, null otherwise
     * @throws SQLException if a database access error occurs
     */
    User findUser(String column, String value) throws SQLException;

    /**
     * <hr>
     * Retrieves all users from the system as an observable list.
     *
     * <p>Fetches every user entity stored in the database and returns them
     * in an observable collection suitable for UI binding and real-time
     * updates in JavaFX applications.
     *
     * @return an ObservableList of all User objects in the system
     * @throws SQLException if a database access error occurs
     */
    ObservableList<User> findUsers() throws SQLException;

    /**
     * <hr>
     * Finds a user by their unique identifier.
     *
     * <p>Searches for a user using their system-assigned unique ID,
     * providing precise user retrieval for user management operations.
     *
     * @param userId the unique identifier of the user to find
     * @return the User object if found, null otherwise
     * @throws SQLException if a database access error occurs
     */
    User findUserById(String userId) throws SQLException;

    /**
     * <hr>
     * Creates a new user account in the system.
     *
     * <p>Registers a new user with all required profile information and
     * performs duplicate checks to ensure username and email uniqueness
     * before account creation.
     *
     * @param username the unique username for the new account
     * @param password the password for the new account
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param institution the user's educational institution
     * @return true if the user was successfully created, false otherwise
     * @throws SQLException if a database access error occurs
     * @throws DuplicateUsernameException if the username already exists
     * @throws DuplicateEmailException if the email address already exists
     */
    boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException, DuplicateUsernameException, DuplicateEmailException;

    /**
     * <hr>
     * Adds a new notification for a user.
     *
     * <p>Creates and delivers a notification message from one user to another,
     * enabling user communication and system notification delivery.
     *
     * @param sentFrom the user sending the notification
     * @param receivedBy the user receiving the notification
     * @param message the notification content/message
     * @return true if the notification was successfully added, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean addNotification(User sentFrom, User receivedBy, String message) throws SQLException;

    /**
     * <hr>
     * Updates a specific attribute of a user's profile.
     *
     * <p>Modifies a single column/value pair for a user entity, allowing
     * targeted updates to user information without replacing the entire
     * profile.
     *
     * @param username the username of the user to update
     * @param column the specific column/attribute to modify
     * @param value the new value to assign to the specified column
     * @return true if the update was successful, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean updateValue(String username, String column, String value) throws SQLException;

    /**
     * <hr>
     * Deletes a user account by user identifier.
     *
     * <p>Removes a user entity from the database using their unique ID,
     * including cleanup of associated data through cascading operations.
     *
     * @param userID the unique identifier of the user to delete
     * @return true if the user was successfully deleted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    boolean deleteUser(String userID) throws SQLException;

    // === Additional methods for MockUserDAO compatibility ===

    /**
     * <hr>
     * Adds a pre-existing user object to the system.
     *
     * <p>Persists a complete user entity to the database, typically used
     * for testing or administrative user creation scenarios.
     *
     * @param user the User object to be added
     * @return true if the user was successfully added, false otherwise
     */
    boolean addUser(User user);

    /**
     * <hr>
     * Deletes a user account using User object reference.
     *
     * <p>Removes a user entity from the database using object reference,
     * providing an alternative deletion method for object-oriented workflows.
     *
     * @param user the User object to delete
     * @return true if the user was successfully deleted, false otherwise
     */
    boolean deleteUser(User user);

    /**
     * <hr>
     * Updates a user's complete profile information.
     *
     * <p>Modifies all attributes of a user entity with new information,
     * providing comprehensive profile update capabilities.
     *
     * @param user the User object containing updated information
     * @return true if the user was successfully updated, false otherwise
     */
    boolean updateUser(User user);

    /**
     * <hr>
     * Updates a user entity with new information.
     *
     * <p>Alternative update method that modifies a user entity in-place,
     * typically used in different architectural patterns or testing scenarios.
     *
     * @param user the User object to update
     */
    void update(User user);

    /**
     * <hr>
     * Searches for a user by username.
     *
     * <p>Finds a user entity using their username with exact matching,
     * commonly used for login and user identification operations.
     *
     * @param username the username to search for
     * @return the User object if found, null otherwise
     */
    User searchByUsername(String username);

    /**
     * <hr>
     * Searches for users by educational institution.
     *
     * <p>Finds all users associated with a particular educational institution,
     * enabling institution-based user grouping and discovery.
     *
     * @param institution the institution name to search for
     * @return a list of User objects from the specified institution
     */
    java.util.List<User> searchByInstitution(String institution);

    /**
     * <hr>
     * Retrieves a user by email address.
     *
     * <p>Finds a user entity using their email address with exact matching,
     * commonly used for account recovery and email-based operations.
     *
     * @param email the email address to search for
     * @return an Optional containing the User if found, empty otherwise
     */
    java.util.Optional<User> getUserByEmail(String email);

    /**
     * <hr>
     * Retrieves a user by username.
     *
     * <p>Finds a user entity using their username with exact matching,
     * returning the result in an Optional for null-safe handling.
     *
     * @param username the username to search for
     * @return an Optional containing the User if found, empty otherwise
     */
    java.util.Optional<User> getUserByUsername(String username);

    /**
     * <hr>
     * Checks if a user with the specified email already exists.
     *
     * <p>Verifies email uniqueness within the system to prevent duplicate
     * accounts and ensure data integrity.
     *
     * @param email the email address to check
     * @return true if a user with this email exists, false otherwise
     */
    boolean existsByEmail(String email);

    /**
     * <hr>
     * Checks if a user with the specified username already exists.
     *
     * <p>Verifies username uniqueness within the system to prevent duplicate
     * accounts and ensure data integrity.
     *
     * @param username the username to check
     * @return true if a user with this username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * <hr>
     * Retrieves all users from the system.
     *
     * <p>Fetches every user entity stored in the database, providing
     * comprehensive system-wide user access for administrative purposes.
     *
     * @return a list of all User objects in the system
     */
    java.util.List<User> getAllUsers();

    /**
     * <hr>
     * Stores a password hash for a user.
     *
     * <p>Persists a hashed password for user authentication, typically
     * using secure hashing algorithms for password protection.
     *
     * @param user the User object to store the password for
     * @param hash the hashed password to store
     * @return true if the password was successfully stored, false otherwise
     */
    boolean storePassword(User user, String hash);

    /**
     * <hr>
     * Retrieves the stored password hash for a user.
     *
     * <p>Fetches the hashed password for a user, used during authentication
     * to verify user credentials.
     *
     * @param user the User object to retrieve the password for
     * @return the hashed password string
     */
    String getPassword(User user);

    /**
     * <hr>
     * Retrieves all notifications for a specific user.
     *
     * <p>Fetches the complete notification history for a particular user,
     * including both read and unread notifications.
     *
     * @param user the user whose notifications are being retrieved
     * @return a list of Notification objects for the specified user
     */
    List<Notification> getNotificationsForUser(User user);

    /**
     * <hr>
     * Marks a specific notification as read for a user.
     *
     * <p>Updates the read status of a notification, typically when a user
     * views or acknowledges the notification.
     *
     * @param user the user who owns the notification
     * @param notification the Notification object to mark as read
     * @return true if the notification was successfully marked as read, false otherwise
     */
    boolean markNotificationAsRead(User user, Notification notification);

    /**
     * <hr>
     * Marks all notifications as read for a user.
     *
     * <p>Updates the read status of all unread notifications for a particular
     * user, providing bulk notification management.
     *
     * @param user the user whose notifications are being marked as read
     * @return true if all notifications were successfully marked as read, false otherwise
     */
    boolean markAllNotificationsAsRead(User user);

    /**
     * <hr>
     * Retrieves the count of unread notifications for a user.
     *
     * <p>Provides a quick count of unread notifications, useful for
     * notification badges and user interface indicators.
     *
     * @param user the user to check for unread notifications
     * @return the number of unread notifications for the specified user
     */
    int getUnreadNotificationCount(User user);

    /**
     * <hr>
     * Removes a specific notification for a user.
     *
     * <p>Deletes a notification entity from the user's notification history,
     * allowing users to clear individual notifications.
     *
     * @param username the user who owns the notification
     * @param notification the Notification object to remove
     * @return true if the notification was successfully removed, false otherwise
     */
    boolean removeNotification(User username, Notification notification);
}