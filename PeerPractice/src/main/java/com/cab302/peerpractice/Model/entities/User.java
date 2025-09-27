package com.cab302.peerpractice.Model.entities;

import com.cab302.peerpractice.Utilities.ValidationUtils;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class User {

    private String userId; // unique identifier for the user
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String passwordHash;
    private String phone;
    private String address;
    private String dateOfBirth;
    private String institution;
    private String bio;
    private List<User> friendsList;
    private List<Notification> notifications;
    private String dateFormat;
    private String timeFormat;

    public User(String firstName, String lastName, String username, String email, String passwordHash, String institution) {
        this.userId = java.util.UUID.randomUUID().toString(); // generate unique ID
        setFirstName(firstName);
        setLastName(lastName);
        setUsername(username);
        setEmail(email);
        this.passwordHash = passwordHash;
        this.institution = institution;
        this.friendsList = new ArrayList<>();
        this.dateFormat = "dd/MM/yyyy"; // Default format
        this.timeFormat = "HH:mm";     // Default format
    }

    // constructor for loading users with existing ID (from database)
    public User(String userId, String firstName, String lastName, String username, String email, String passwordHash, String institution) {
        this.userId = userId;
        setFirstName(firstName);
        setLastName(lastName);
        setUsername(username);
        setEmail(email);
        this.passwordHash = passwordHash;
        this.institution = institution;
        this.friendsList = new ArrayList<>();
        this.dateFormat = "dd/MM/yyyy"; // Default format
        this.timeFormat = "HH:mm";     // Default format
    }

    public String getUserId() { return userId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) {
        this.firstName = ValidationUtils.validateAndCleanName(firstName, "First name");
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) {
        this.lastName = ValidationUtils.validateAndCleanName(lastName, "Last name");
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = ValidationUtils.validateAndCleanUsername(username);
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = ValidationUtils.validateAndCleanEmail(email);
    }
    public String getPhone() {return phone;}
    public void setPhone(String phone) {
        this.phone = phone == null ? "" : phone.trim();
    }

    public String getAddress() {return address;}
    public void setAddress(String address) {
        this.address = address == null ? "" : address.trim();
    }

    public String getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(String dateOfBirth) { this.dateOfBirth = dateOfBirth == null ? "" : dateOfBirth.trim(); }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public void setBio(String bio) {
        this.bio = ValidationUtils.validateAndCleanBio(bio);
    }
    public String getBio() { return bio; }


    public String getPassword() { return passwordHash; }
    public void setPassword(String passwordHash) { this.passwordHash = passwordHash; }

    public List<User> getFriendList() { return friendsList; }

    public void addFriend(User user) { 
        if (user == null) throw new IllegalArgumentException("Cannot add null user as friend");
        friendsList.add(user); 
    }

    public List<Notification> getNotifications() { return notifications; }
    
    public void addNotification(Notification n) {notifications.add(n); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(String dateFormat) {
        this.dateFormat = dateFormat != null ? dateFormat : "dd/MM/yyyy";
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat != null ? timeFormat : "HH:mm";
    }
}
