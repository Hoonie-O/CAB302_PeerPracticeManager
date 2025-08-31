package com.cab302.peerpractice.Model;

public interface IUserDAO {
    boolean logIn(User user);
    boolean signUp(User user);
    User searchByUsername(String username);
}
