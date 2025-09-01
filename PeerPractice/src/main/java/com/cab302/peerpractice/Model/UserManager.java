package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.*;

import java.util.regex.Pattern;

public class UserManager {

    private IUserDAO userDAO;
    private PasswordHasher hasher;

    public UserManager(IUserDAO userDAO){
        this.userDAO = userDAO;
    }

    public boolean signUp(String firstName, String lastName, String username, String email, String password, String institution){

        validateEmail(email);
        validateNames(firstName);
        validateNames(lastName);
        validateUsername(username);

        if(userDAO.existsByEmail(email)) throw new DuplicateEmailException("Email already exists");
        if(userDAO.existsByUsername(username)) throw new DuplicateUsernameException("Username already exists");

        User u = new User(firstName,lastName,username,email,password,institution);
        return userDAO.signUp(u);
    }

    private static void validateUsername(String username){
        if(username == null) throw new IllegalArgumentException("Username can't be null");
        username.trim();
        if(username.isEmpty()) throw new IllegalArgumentException("Username can't be blank");
        if(!Pattern.compile("^(?!\\\\d+$)[A-Za-z0-9._]{6,}$").matcher(username).find()){
            throw new IllegalArgumentException("Username must be at least 6 characters long, and only contain letter, numbers or . _");
        }
    }

    private static void validateEmail(String email){
        if(email == null) throw new IllegalArgumentException("Email can't be null");
        email.trim();
        if(email.isEmpty()) throw new IllegalArgumentException("Email can't be blank");
        if(!Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matcher(email).find()){
            throw new IllegalArgumentException("Invalid email");
        }
    }

    private static void validateNames(String name){
        if(name == null) throw new IllegalArgumentException("Name can't be null");
        name = name.trim();
        if(name.isEmpty()) throw new IllegalArgumentException("Name can't be blank");
        if(Pattern.compile("\\P{L}").matcher(name).find()) throw new IllegalArgumentException("Name can't contain non-letter characters");
    }


}
