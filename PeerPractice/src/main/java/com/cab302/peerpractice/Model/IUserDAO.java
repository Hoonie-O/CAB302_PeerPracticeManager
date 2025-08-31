package com.cab302.peerpractice.Model;

import java.util.List;

public interface IUserDAO {
    boolean logIn(User user);
    boolean signUp(User user);
    List<User> searchByUsername(String username);
    List<User> searchByInstitution(String institution);
    boolean deleteUser(User user);
}
