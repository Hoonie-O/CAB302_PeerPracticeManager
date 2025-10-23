package com.cab302.peerpractice.Model.Services;

import com.cab302.peerpractice.Exceptions.DuplicateUsernameException;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Utils.ValidationUtils;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Service responsible for user profile management operations.
 * Follows the Single Responsibility Principle by focusing exclusively on
 * profile data updates (name, contact info, bio, etc.).
 *
 * Responsibilities:
 * - Update personal information (name, date of birth, institution)
 * - Update contact information (phone, address)
 * - Update username
 * - Update biography
 *
 * This service does NOT handle authentication (see {@link UserAuthenticationService}).
 *
 * @see UserAuthenticationService
 */
public class UserProfileService {

    private final IUserDAO userDAO;

    /**
     * Creates a new UserProfileService.
     *
     * @param userDAO the user data access object
     */
    public UserProfileService(IUserDAO userDAO) {
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO cannot be null");
    }

    /**
     * Updates a user's first name.
     *
     * @param username the username of the user
     * @param firstName the new first name (cannot be blank)
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if firstName is blank
     */
    public void updateFirstName(String username, String firstName) throws SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        ValidationUtils.requireNotBlank(firstName, "First name");
        userDAO.updateValue(username, "first_name", firstName.trim());
    }

    /**
     * Updates a user's last name.
     *
     * @param username the username of the user
     * @param lastName the new last name (cannot be blank)
     * @throws SQLException if database error occurs
     * @throws IllegalArgumentException if lastName is blank
     */
    public void updateLastName(String username, String lastName) throws SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        ValidationUtils.requireNotBlank(lastName, "Last name");
        userDAO.updateValue(username, "last_name", lastName.trim());
    }

    /**
     * Updates a user's institution.
     *
     * @param username the username of the user
     * @param institution the new institution (can be empty)
     * @throws SQLException if database error occurs
     */
    public void updateInstitution(String username, String institution) throws SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        String cleanValue = institution == null ? "" : institution.trim();
        userDAO.updateValue(username, "institution", cleanValue);
    }

    /**
     * Changes a user's username with uniqueness validation.
     *
     * @param currentUsername the current username
     * @param newUsername the desired new username (must be unique)
     * @throws SQLException if database error occurs
     * @throws DuplicateUsernameException if new username is already taken
     * @throws IllegalArgumentException if newUsername is blank
     */
    public void changeUsername(String currentUsername, String newUsername)
            throws SQLException, DuplicateUsernameException {
        Objects.requireNonNull(currentUsername, "Current username cannot be null");
        ValidationUtils.requireNotBlank(newUsername, "Username");

        if (userDAO.existsByUsername(newUsername)) {
            throw new DuplicateUsernameException("Username already taken.");
        }

        userDAO.updateValue(currentUsername, "username", newUsername.trim());
    }

    /**
     * Updates a user's phone number.
     * Strips all non-digit characters from the input.
     *
     * @param username the username of the user
     * @param phone the new phone number (only digits will be stored)
     * @throws SQLException if database error occurs
     */
    public void updatePhone(String username, String phone) throws SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        userDAO.updateValue(username, "phone", digits);
    }

    /**
     * Updates a user's address.
     *
     * @param username the username of the user
     * @param address the new address (can be empty)
     * @throws SQLException if database error occurs
     */
    public void updateAddress(String username, String address) throws SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        String cleanValue = address == null ? "" : address.trim();
        userDAO.updateValue(username, "address", cleanValue);
    }

    /**
     * Updates a user's date of birth.
     *
     * @param username the username of the user
     * @param isoDate the date in ISO format (YYYY-MM-DD) or empty
     * @throws SQLException if database error occurs
     */
    public void updateDateOfBirth(String username, String isoDate) throws SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        String cleanValue = isoDate == null ? "" : isoDate.trim();
        userDAO.updateValue(username, "date_of_birth", cleanValue);
    }

    /**
     * Updates a user's biography text.
     *
     * @param username the username of the user
     * @param bio the new biography text (can be empty)
     * @throws SQLException if database error occurs
     */
    public void updateBio(String username, String bio) throws SQLException {
        Objects.requireNonNull(username, "Username cannot be null");
        String cleanValue = bio == null ? "" : bio.trim();
        userDAO.updateValue(username, "biography", cleanValue);
    }
}
