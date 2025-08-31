package com.cab302.peerpractice.Model;

import java.util.Optional;

public interface IUserDAO {
    boolean logIn(User user);
    boolean signUp(User user);
    Optional<User> searchByUsername(String username);
}
