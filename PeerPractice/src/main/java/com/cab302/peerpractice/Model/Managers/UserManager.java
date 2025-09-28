package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Exceptions.*;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Utils.PasswordHasher;
import com.cab302.peerpractice.Model.Utils.ValidationUtils;

import java.sql.SQLException;
import java.util.Optional;

public class UserManager {

    private final IUserDAO userDAO;
    private final PasswordHasher hasher;

    public UserManager(IUserDAO userDAO, PasswordHasher hasher) {
        this.userDAO = userDAO;
        this.hasher = hasher;
    }

    // Signup
    public boolean signUp(String firstName, String lastName, String username, String email, String rawPassword, String institution) throws Exception {
        ValidationUtils.validateAndCleanEmail(email);
        ValidationUtils.validateAndCleanName(firstName, "First name");
        ValidationUtils.validateAndCleanName(lastName, "Last name");
        ValidationUtils.validateAndCleanUsername(username);
        ValidationUtils.validatePassword(rawPassword);
        if (userDAO.existsByEmail(email)) {
            throw new DuplicateEmailException("Email already exists");
        }
        if (userDAO.existsByUsername(username)) {
            throw new DuplicateUsernameException("Username already exists");
        }
        String hashed = hasher.hasher(rawPassword);
        return userDAO.createUser(username, hashed, firstName, lastName, email, institution);
    }

    // Login/authenticate
    public boolean authenticate(String identifier, String rawPassword) throws SQLException {
        // Try email first, then username
        Optional<User> userOpt = Optional.ofNullable(userDAO.findUser("email", identifier));
        if (userOpt.isEmpty()) {
            userOpt = Optional.ofNullable(userDAO.findUser("username", identifier));
        }

        return userOpt
                .map(u -> hasher.matches(rawPassword, u.getPassword()))
                .orElse(false);
    }

    //Password changer
    public boolean changePassword(String username, String rawPassword) throws InvalidPasswordException, SQLException {
        ValidationUtils.validatePassword(rawPassword);
        String hashed = hasher.hasher(rawPassword);
        return userDAO.updateValue(username, "password", hashed);
    }

    public void updateFirstName(String username, String first) throws SQLException {
        ValidationUtils.requireNotBlank(first, "First name");
        userDAO.updateValue(username, "first_name", first.trim());
    }

    public void updateLastName(String username, String last) throws SQLException {
        ValidationUtils.requireNotBlank(last, "Last name");
        userDAO.updateValue(username, "last_name", last.trim());
    }

    public void updateInstitution(String username, String inst) throws SQLException {
        userDAO.updateValue(username, "institution", inst == null ? "" : inst.trim());
    }

    public void changeUsername(String currentUsername, String newUsername)
            throws SQLException, DuplicateUsernameException {
        ValidationUtils.requireNotBlank(newUsername, "Username");
        if (userDAO.existsByUsername(newUsername)) {
            throw new DuplicateUsernameException("Username already taken.");
        }
        userDAO.updateValue(currentUsername, "username", newUsername.trim());
    }

    public void updatePhone(String username, String phone)
            throws SQLException {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        userDAO.updateValue(username, "phone", digits);
    }

    public void updateAddress(String username, String address)
            throws SQLException {
        userDAO.updateValue(username, "address", address == null ? "" : address.trim());
    }

    public void updateDateOfBirth(String username, String isoDate)
            throws SQLException {
        userDAO.updateValue(username, "date_of_birth", isoDate == null ? "" : isoDate.trim());
    }

    public void updateBio(String username, String bio)
            throws SQLException {
        userDAO.updateValue(username, "biography", bio == null ? "" : bio.trim());
    }
}
