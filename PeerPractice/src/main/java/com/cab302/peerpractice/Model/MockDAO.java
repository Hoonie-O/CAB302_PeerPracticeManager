package com.cab302.peerpractice.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class MockDAO implements IUserDAO{

    private final List<User> users;

    public MockDAO(){
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

}
