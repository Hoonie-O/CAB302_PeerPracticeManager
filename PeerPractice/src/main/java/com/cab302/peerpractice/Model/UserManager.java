package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.*;

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
    public boolean signUp(String firstName, String lastName, String username,
                          String email, String rawPassword, String institution) {

        validateEmail(email);
        validateNames(firstName);
        validateNames(lastName);
        validateUsername(username);

        if (userDAO.existsByEmail(email)) throw new DuplicateEmailException("Email already exists");
        if (userDAO.existsByUsername(username)) throw new DuplicateUsernameException("Username already exists");

        String hashed = hasher.hasher(rawPassword);
        User u = new User(firstName, lastName, username, email, hashed, institution);
        return userDAO.addUser(u);
    }

    // Login/authenticate
    public boolean authenticate(String identifier, String rawPassword) {
        // Try email first, then username
        Optional<User> userOpt = userDAO.getUserByEmail(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userDAO.getUserByUsername(identifier);
        }

        return userOpt
                .map(u -> hasher.matches(rawPassword, u.getPassword()))
                .orElse(false);
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


}
