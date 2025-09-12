package com.cab302.peerpractice.Model;

import java.util.List;

public interface IUserDAO {
    boolean logIn(User user);
    boolean signUp(User user);
    List<User> searchByUsername(String username);
    List<User> searchByInstitution(String institution);
    boolean deleteUser(User user);
    boolean updateUser(User user);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> getAllUsers();
    boolean storePassword(User user, String hash);
    String getPassword(User user);
}
