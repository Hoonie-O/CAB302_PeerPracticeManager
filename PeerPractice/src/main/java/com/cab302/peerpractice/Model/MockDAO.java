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
        if (username == null || username.isBlank()) return List.of();

        String target = username.trim();

        List<User> matches = users.stream()
                .filter(Objects::nonNull)
                .filter(u -> {
                    String un = u.getUsername();
                    return un != null && un.equalsIgnoreCase(target);
                })
                .toList();   // use .toList() on Java 16+

        return List.copyOf(matches); // return unmodifiable list (optional)
    }

    @Override
    public List<User> searchByInstitution(String institution) {
        if (institution == null || institution.isBlank()) return List.of();
        String target = institution.trim();

        return users.stream()
                .filter(Objects::nonNull)
                .filter(u -> {
                    String inst = u.getInstitution();
                    return inst != null && inst.equalsIgnoreCase(target);
                })
                .toList(); // or .toList() on Java 16+
    }

    public boolean deleteUser(User user){
        users.remove(user);
    }

}
