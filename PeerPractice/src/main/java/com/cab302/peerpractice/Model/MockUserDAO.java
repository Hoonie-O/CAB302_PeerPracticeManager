package com.cab302.peerpractice.Model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MockUserDAO implements IUserDAO{

    private final List<User> users;

    public MockUserDAO(){
        users = new ArrayList<>();
    }

    @Override
    public boolean addUser(User user) {
        if (user == null) {
            return false; // don’t add null
        }

        // prevent duplicate email or username
        boolean exists = users.stream().anyMatch(u ->
                (u.getEmail() != null && u.getEmail().equalsIgnoreCase(user.getEmail())) ||
                        (u.getUsername() != null && u.getUsername().equalsIgnoreCase(user.getUsername()))
        );

        if (exists) {
            return false;
        }

        return users.add(user);
    }

    @Override
    public boolean deleteUser(User user) {
        return users.remove(user);
    }

    @Override
    public boolean deleteUser(String userID) throws SQLException {
        return users.removeIf(user -> userID.equals(user.getUserId()));
    }

    @Override
    public boolean updateUser(User user){
        for(int i = 0; i<users.size() ; i++){
            if(Objects.equals(users.get(i).getUsername(),user.getUsername())){
                users.set(i,user);
                return true;
            }
        }
        return false;
    }

    @Override
    public void update(User user) {
        for (int i = 0; i < users.size(); i++) {
            User u = users.get(i);
            if (u.getEmail().equalsIgnoreCase(user.getEmail())) {
                users.set(i, user);   // replace existing
                return;
            }
        }
        // or add if not found (upsert behavior)
        users.add(user);
    }

    @Override
    public User searchByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<User> searchByInstitution(String institution) {
        return users.stream()
                .filter(u -> u.getInstitution() != null && u.getInstitution().equalsIgnoreCase(institution))
                .toList();
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return users.stream()
                .filter(u -> Objects.equals(u.getEmail(), email))
                .findFirst();
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return users.stream()
                .filter(u -> Objects.equals(u.getUsername(), username))
                .findFirst();
    }


    @Override
    public boolean existsByEmail(String email) {
        for (User user : users) {
            if (Objects.equals(user.getEmail(), email)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean existsByUsername(String username) {
        for (User user : users) {
            if (Objects.equals(user.getUsername(), username)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> getAllUsers() {
        return users;
    }

    @Override
    public boolean storePassword(User user, String hash) {
        return false;
    }

    @Override
    public String getPassword(User user) {
        return "";
    }

    @Override
    public boolean addNotification(String username, Notification notification) {
        return false;
    }

    @Override
    public boolean removeNotification(String username, Notification notification) {
        return false;
    }

    @Override
    public User findUser(String column, String value) throws SQLException {
        switch (column.toLowerCase()) {
            case "username":
                return getUserByUsername(value).orElse(null);
            case "email":
                return getUserByEmail(value).orElse(null);
            case "user_id":
                return users.stream()
                        .filter(u -> value.equals(u.getUserId()))
                        .findFirst()
                        .orElse(null);
            default:
                return null;
        }
    }

    @Override
    public javafx.collections.ObservableList<User> findUsers() throws SQLException {
        return javafx.collections.FXCollections.observableArrayList(users);
    }

    @Override
    public User findUserById(String userId) throws SQLException {
        return users.stream()
                .filter(u -> userId.equals(u.getUserId()))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean createUser(String username, String password, String firstName, String lastName, String email, String institution) throws SQLException {
        User user = new User(firstName, lastName, username, email, password, institution);
        return addUser(user);
    }

    @Override
    public boolean addNotification(String sentFrom, String receivedBy, String message) throws SQLException {
        return false;
    }

    @Override
    public boolean updateValue(String username, String column, String value) throws SQLException {
        User user = getUserByUsername(username).orElse(null);
        if (user == null) return false;

        switch (column.toLowerCase()) {
            case "first_name":
                user.setFirstName(value);
                break;
            case "last_name":
                user.setLastName(value);
                break;
            case "email":
                user.setEmail(value);
                break;
            case "institution":
                user.setInstitution(value);
                break;
            case "password":
                user.setPassword(value);
                break;
            case "date_format":
                user.setDateFormat(value);
                break;
            case "time_format":
                user.setTimeFormat(value);
                break;
            default:
                return false;
        }
        return true;
    }
}
