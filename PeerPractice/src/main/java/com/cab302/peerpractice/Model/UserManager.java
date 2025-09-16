package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.*;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserManager {

    private final IUserDAO userDAO;
    private final PasswordHasher hasher;

    public UserManager(IUserDAO userDAO, PasswordHasher hasher) {
        this.userDAO = userDAO;
        this.hasher = hasher;
    }

    // Signup
    public boolean signUp(String firstName, String lastName, String username, String email, String rawPassword, String institution) throws Exception {
        validateEmail(email);
        validateNames(firstName);
        validateNames(lastName);
        validateUsername(username);
        validatePassword(rawPassword);
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
        validatePassword(rawPassword);
        String hashed = hasher.hasher(rawPassword);

        return userDAO.updateValue(username, "password", hashed);
    }

    // Validation helpers
    private static void validateUsername(String username) {
        if (username == null) throw new IllegalArgumentException("Username can't be null");
        username = username.trim();
        if (username.isEmpty()) throw new IllegalArgumentException("Username can't be blank");
        if (!Pattern.compile("^(?!\\d+$)[A-Za-z0-9._]{6,}$").matcher(username).find()) {
            throw new IllegalArgumentException("Username must be at least 6 characters long, and only contain letters, numbers, . or _");
        }
    }

    private static void validatePassword(String password) throws InvalidPasswordException{
        int min_len = 8;
        int max_len = 72;
        if(password == null || password.isEmpty()){
            throw new InvalidPasswordException("Password can't be null or empty.");
        }
        if(password.length() < min_len){
            throw  new InvalidPasswordException("Password should be at least 8 characters long.");
        }
        if(password.length() > max_len){
            throw new InvalidPasswordException("Password can't be longer than 72 characters long.");
        }
        if(!password.chars().anyMatch(Character::isUpperCase)){
            throw new InvalidPasswordException("Password must contain at least one upper case letter.");
        }
        if(!password.chars().anyMatch(Character::isLowerCase)){
            throw new InvalidPasswordException("Password must contain at least one lower case letter.");
        }
        if(!password.chars().anyMatch(Character::isDigit)){
            throw new InvalidPasswordException("Password must contain at least one numerical digit.");
        }
        if(password.chars().noneMatch(c -> "!@#$%^&*()-_=+[]{};:'\",.<>/?\\|`~".indexOf(c) >= 0)){
            throw new InvalidPasswordException("Password must contain at least one special character");
        }
    }

    private static void validateEmail(String email) {
        if (email == null) throw new IllegalArgumentException("Email can't be null");
        email = email.trim();
        if (email.isEmpty()) throw new IllegalArgumentException("Email can't be blank");
        if (!Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matcher(email).find()) {
            throw new IllegalArgumentException("Invalid email");
        }
    }

    private static void validateNames(String name) {
        if (name == null) throw new IllegalArgumentException("Name can't be null");
        name = name.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Name can't be blank");
        if (Pattern.compile("\\P{L}").matcher(name).find())
            throw new IllegalArgumentException("Name can't contain non-letter characters");
    }
    public void updateFirstName(String username, String first) throws SQLException {
        requireNotBlank(first, "First name");
        userDAO.updateValue(username, "first_name", first.trim());
    }

    public void updateLastName(String username, String last) throws SQLException {
        requireNotBlank(last, "Last name");
        userDAO.updateValue(username, "last_name", last.trim());
    }

    public void updateInstitution(String username, String inst) throws SQLException {
        userDAO.updateValue(username, "institution", inst == null ? "" : inst.trim());
    }

    public void changeUsername(String currentUsername, String newUsername)
            throws SQLException, DuplicateUsernameException {
        requireNotBlank(newUsername, "Username");
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

    private static void requireNotBlank(String s, String label) {
        if (s == null || s.trim().isEmpty())
            throw new IllegalArgumentException(label + " cannot be empty.");
    }

}
