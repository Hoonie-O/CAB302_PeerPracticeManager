package com.cab302.peerpractice.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MockDAO implements IUserDAO {

    private final List<User> users;

    public MockDAO() {
        users = new ArrayList<>();
    }

    @Override
    public boolean addUser(User user) {
        users.add(user);
        return true;
    }

    @Override
    public boolean deleteUser(User user) {
        return users.remove(user);
    }

    @Override
    public boolean updateUser(User user) {
        for (int i = 0; i < users.size(); i++) {
            if (Objects.equals(users.get(i).getUsername(), user.getUsername())) {
                users.set(i, user);
                return true;
            }
        }
        return false;
    }

    @Override
    public List<User> searchByUsername(String username) {
        return users.stream()
                .filter(u -> u.getUsername() != null && u.getUsername().equalsIgnoreCase(username))
                .toList();
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
        return users.stream().anyMatch(u -> Objects.equals(u.getEmail(), email));
    }

    @Override
    public boolean existsByUsername(String username) {
        return users.stream().anyMatch(u -> Objects.equals(u.getUsername(), username));
    }

    @Override
    public List<User> getAllUsers() {
        return List.copyOf(users);
    }
}
