package com.cab302.peerpractice.Model;

import java.util.List;
import java.util.Optional;

public interface IUserDAO {
    boolean addUser(User user);
    boolean deleteUser(User user);
    boolean updateUser(User user);
    List<User> searchByUsername(String username);
    List<User> searchByInstitution(String institution);
    Optional<User> getUserByEmail(String email);
    Optional<User> getUserByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
    List<User> getAllUsers();
}
