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

public class MockUserDAO implements IUserDAO {

    private final Map<String, User> users = new ConcurrentHashMap<>(); // keyed by username
    private final Map<String, List<String>> notifications = new ConcurrentHashMap<>();

    // -------------------- CREATE --------------------
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

    @Override
    public ObservableList<User> findUsers() {
        return FXCollections.observableArrayList(users.values());
    }

    @Override
    public User findUserById(String userId) { return findUser("user_id", userId); }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(findUser("email", email));
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(findUser("username", username));
    }

    @Override
    public List<User> searchByInstitution(String institution) {
        return users.values().stream()
                .filter(u -> institution.equals(u.getInstitution()))
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) { return getUserByEmail(email).isPresent(); }

    @Override
    public boolean existsByUsername(String username) { return getUserByUsername(username).isPresent(); }

    @Override
    public List<User> getAllUsers() { return new ArrayList<>(users.values()); }

    // -------------------- UPDATE --------------------
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

    @Override
    public boolean updateUser(User user) {
        if (!users.containsKey(user.getUsername())) return false;
        users.put(user.getUsername(), user);
        return true;
    }

    @Override
    public void update(User user) { updateUser(user); }

    @Override
    public boolean storePassword(User user, String hash) {
        user.setPassword(hash);
        return true;
    }

    @Override
    public String getPassword(User user) { return user.getPassword(); }

    // -------------------- DELETE --------------------
    @Override
    public boolean deleteUser(String userId) {
        Optional<String> username = users.values().stream()
                .filter(u -> u.getUserId().equals(userId))
                .map(User::getUsername)
                .findFirst();
        username.ifPresent(users::remove);
        return username.isPresent();
    }

    @Override
    public boolean deleteUser(User user) {
        return users.remove(user.getUsername()) != null;
    }

    @Override
    public User searchByUsername(String username) { return users.get(username); }

    // -------------------- NOTIFICATIONS --------------------


    @Override
    public boolean addNotification(String sentFrom, String receivedBy, String message) {
        notifications.computeIfAbsent(receivedBy, k -> new ArrayList<>()).add(message);
        return true;
    }

    @Override
    public boolean addNotification(String username, Notification notification) {
        if (notification == null) return false;
        String message = notification.getMessage();  // extract text only
        notifications.computeIfAbsent(username, k -> new ArrayList<>()).add(message);
        return true;
    }

    @Override
    public boolean removeNotification(String username, Notification notification) {
        if (notification == null) return false;
        List<String> list = notifications.get(username);
        if (list == null) return false;
        return list.remove(notification.getMessage());  // compare by message text
    }
}
