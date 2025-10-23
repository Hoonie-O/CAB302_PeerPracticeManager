package com.cab302.peerpractice.Model.Services;

import com.cab302.peerpractice.Exceptions.*;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Utils.PasswordHasher;
import com.cab302.peerpractice.Model.Utils.ValidationUtils;

import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

/**
 * Service responsible for user authentication operations.
 * Follows the Single Responsibility Principle by focusing exclusively on
 * authentication concerns: signup, login, and password management.
 *
 * Responsibilities:
 * - User registration with validation
 * - User authentication (login)
 * - Password changes with security validation
 *
 * This service does NOT handle profile updates (see {@link UserProfileService}).
 *
 * @see UserProfileService
 */
public class UserAuthenticationService {

    private final IUserDAO userDAO;
    private final PasswordHasher hasher;

    /**
     * Creates a new UserAuthenticationService.
     *
     * @param userDAO the user data access object
     * @param hasher the password hasher for secure password storage
     */
    public UserAuthenticationService(IUserDAO userDAO, PasswordHasher hasher) {
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO cannot be null");
        this.hasher = Objects.requireNonNull(hasher, "PasswordHasher cannot be null");
    }

    /**
     * Registers a new user account with validation.
     *
     * Validates:
     * - Email format and uniqueness
     * - Username format and uniqueness
     * - Password strength requirements
     * - Name format
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param username the desired username (must be unique)
     * @param email the user's email address (must be unique and valid)
     * @param rawPassword the plain password (will be hashed)
     * @param institution the user's institution
     * @return true if signup succeeded
     * @throws DuplicateEmailException if email already exists
     * @throws DuplicateUsernameException if username already exists
     * @throws InvalidPasswordException if password doesn't meet requirements
     * @throws Exception if validation fails or database error occurs
     */
    public boolean signUp(String firstName, String lastName, String username,
                          String email, String rawPassword, String institution) throws Exception {
        // Validate input
        ValidationUtils.validateAndCleanEmail(email);
        ValidationUtils.validateAndCleanName(firstName, "First name");
        ValidationUtils.validateAndCleanName(lastName, "Last name");
        ValidationUtils.validateAndCleanUsername(username);
        ValidationUtils.validatePassword(rawPassword);

        // Check uniqueness
        if (userDAO.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists");
        }
        if (userDAO.existsByUsername(username)) {
            throw new DuplicateUsernameException("Username already exists");
        }

        // Hash password and create user
        String hashedPassword = hasher.hasher(rawPassword);
        return userDAO.createUser(username, hashedPassword, firstName, lastName, email, institution);
    }

    /**
     * Authenticates a user with their credentials.
     *
     * Supports login with either email or username.
     * Uses secure password hashing for comparison.
     *
     * @param identifier the user's email or username
     * @param rawPassword the plain password to verify
     * @return true if authentication succeeded, false otherwise
     * @throws SQLException if database error occurs
     */
    public boolean authenticate(String identifier, String rawPassword) throws SQLException {
        Objects.requireNonNull(identifier, "Identifier cannot be null");
        Objects.requireNonNull(rawPassword, "Password cannot be null");

        // Try email first, then username
        Optional<User> userOpt = Optional.ofNullable(userDAO.findUser("email", identifier));
        if (userOpt.isEmpty()) {
            userOpt = Optional.ofNullable(userDAO.findUser("username", identifier));
        }

        return userOpt
                .map(user -> hasher.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    /**
     * Changes a user's password with validation.
     *
     * Validates the new password meets security requirements before updating.
     *
     * @param username the username of the account
     * @param rawPassword the new plain password (will be hashed)
     * @return true if password change succeeded
     * @throws InvalidPasswordException if password doesn't meet requirements
     * @throws SQLException if database error occurs
     */
    public boolean changePassword(String username, String rawPassword)
            throws InvalidPasswordException, SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        Objects.requireNonNull(rawPassword, "Password cannot be null");

        ValidationUtils.validatePassword(rawPassword);
        String hashedPassword = hasher.hasher(rawPassword);
        return userDAO.updateValue(username, "password", hashedPassword);
    }

    /**
     * Verifies if a username exists in the system.
     *
     * @param username the username to check
     * @return true if username exists
     * @throws SQLException if database error occurs
     */
    public boolean usernameExists(String username) throws SQLException {
        return userDAO.existsByUsername(username);
    }

    /**
     * Verifies if an email exists in the system.
     *
     * @param email the email to check
     * @return true if email exists
     * @throws SQLException if database error occurs
     */
    public boolean emailExists(String email) throws SQLException {
        return userDAO.existsByEmail(email);
    }
}
