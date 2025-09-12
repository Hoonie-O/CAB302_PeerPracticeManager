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
    public List<User> searchByUsername(String username) {
        List<User> matches = users.stream()
                .filter(u -> {
                    String un = u.getUsername();
                    return un != null && un.equalsIgnoreCase(username);
                })
                .toList();
        return List.copyOf(matches); // return unmodifiable list
    }

    @Override
    public List<User> searchByInstitution(String institution) {
        List<User> matches = users.stream()
                .filter(u -> {
                    String inst = u.getInstitution();
                    return inst != null && inst.equalsIgnoreCase(institution);
                })
                .toList();
        return List.copyOf(matches);
    }

    public boolean deleteUser(User user){
        users.remove(user);
        return true;
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

    public List<User> getAllUsers() {
        return users;
    }

    @Override
    public boolean storePassword(User user, String hash) {
        user.setPassword(hash);
        return true;
    }

    public String getPassword(User user){
        return user.getPassword();
    }

}
