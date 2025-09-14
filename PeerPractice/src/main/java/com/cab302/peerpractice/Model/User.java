package com.cab302.peerpractice.Model;

import java.util.List;

public class User {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String passwordHash;
    private String institution;
    private String bio;
    private List<User> friendsList;
    private List<Event> events;

    public User(String firstName, String lastName, String username, String email, String passwordHash, String institution) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.institution = institution;
    }

    // Getters/setters

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public String getPassword() { return passwordHash; }
    public void setPassword(String passwordHash) { this.passwordHash = passwordHash; }

    public List<User> getFriendList() { return friendsList; }
    public void addFriend(User user) { friendsList.add(user); }

    public void setBio(String bio) { this.bio = bio; } // fixed bug
    public String getBio() { return bio; }

    public List<Event> getEvents() { return events; }
    public void addEvent(Event ev) { events.add(ev); }
}
