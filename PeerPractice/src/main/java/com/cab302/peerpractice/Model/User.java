package com.cab302.peerpractice.Model;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class User {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private char[] password;
    private String institution;

    public User(String firstName, String lastName, String username, String email, char[] password, String institution) {
        this.firstName = validateNames(firstName);
        this.lastName = validateNames(lastName);
        this.username = validateUsername(username);
        this.email = validateEmail(email);
        this.password = password;
        this.institution = institution;
    }

    public String getFirstName() {
        return firstName;
    }


    public void setFirstName(String firstName) {
        this. firstName = validateNames(firstName);
    }


    public String getLastName() {
        return lastName;
    }


    public void setLastName(String lastName) {
        this.lastName = validateNames(lastName);
    }

    private static String validateNames(String name){
        if(name == null) throw new IllegalArgumentException("Name can't be null");
        name = name.trim();
        if(name.isEmpty()) throw new IllegalArgumentException("Name can't be blank");
        if(Pattern.compile("\\P{L}").matcher(name).find()) throw new IllegalArgumentException("Name can't contain non-letter characters");
        return name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = validateUsername(username);
    }

    private static String validateUsername(String username){
        if(username == null) throw new IllegalArgumentException("Username can't be null");
        username.trim();
        if(username.isEmpty()) throw new IllegalArgumentException("Username can't be blank");
        if(!Pattern.compile("^(?!\\\\d+$)[A-Za-z0-9._]{6,}$").matcher(username).find()){
            throw new IllegalArgumentException("Username must be at least 6 characters long, and only contain letter, numbers or . _");
        }
        return username;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    private static String validateEmail(String email){
        if(email == null) throw new IllegalArgumentException("Email can't be null");
        email.trim();
        if(email.isEmpty()) throw new IllegalArgumentException("Email can't be blank");
        if(!Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$").matcher(email).find()){
            throw new IllegalArgumentException("Invalid email");
        }
        return email;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public List<Object> getFriendList() {
        
    }

    public void addFriend(User user2) {
    }

    public void setBio(String bio) {
    }

    public String getBio() {
    }

    public List<Object> getEvents() {
    }

    public void addEvent(Event ev) {
    }
}
