package com.cab302.peerpractice.Model;

import com.cab302.peerpractice.Exceptions.InvalidPasswordException;

public class PasswordValidator {

    public static final int MIN_LEN = 8;
    public static final int MAX_LEN = 72;

    public static boolean validate(String password) throws InvalidPasswordException{

        if(password == null || password.isEmpty()){
            throw new InvalidPasswordException("Password can't be null or empty.");
        }
        if(password.length() < MIN_LEN){
            throw  new InvalidPasswordException("Password should be at least 8 characters long.");
        }
        if(password.length() > MAX_LEN){
            throw new InvalidPasswordException("Password can't be longer than 72 characters long.");
        }
        if(!password.chars().anyMatch(Character::isUpperCase)){
            throw new InvalidPasswordException("Password must contain at least one upper case letter.");
        }
        if(!password.chars().anyMatch(Character::isLowerCase)){
            throw new InvalidPasswordException("Password must contain at least one lower case letter.");
        }
        if(!password.chars().anyMatch(Character::isDigit)){
            throw new InvalidPasswordException("Password must contain at least one numerical digit.");
        }
        if(password.chars().noneMatch(c -> "!@#$%^&*()-_=+[]{};:'\",.<>/?\\|`~".indexOf(c) >= 0)){
            throw new InvalidPasswordException("Password must contain at least one special character");
        }

        return true;
    }

}
