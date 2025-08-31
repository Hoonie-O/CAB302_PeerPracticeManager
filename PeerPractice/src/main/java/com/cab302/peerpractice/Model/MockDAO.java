package com.cab302.peerpractice.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MockDAO implements IUserDAO{

    private final List<User> users;

    public MockDAO(){
        users = new ArrayList<>();
    }

    @Override
    public boolean logIn(User user) {
        users.contains(user);
        return true;
    }

    @Override
    public boolean signUp(User user) {
        users.add(user);
        return true;
    }

    @Override
    public Optional<User> searchByUsername(String username) {
        return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }
}
