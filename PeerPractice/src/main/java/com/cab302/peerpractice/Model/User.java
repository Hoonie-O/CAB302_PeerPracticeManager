package com.cab302.peerpractice.Model;

import java.util.List;
import java.util.ArrayList;

public class User {

    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String passwordHash;
    private String institution;
    private String bio;
    private List<User> friendsList;
    private List<Notification> notifications;

    public User(String firstName, String lastName, String username, String email, String passwordHash, String institution) {
        setFirstName(firstName);
        setLastName(lastName);
        setUsername(username);
        setEmail(email);
        this.passwordHash = passwordHash;
        this.institution = institution;
        this.friendsList = new ArrayList<>();
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { 
        if (firstName == null) throw new IllegalArgumentException("First name cannot be null");
        if (firstName.trim().isEmpty()) throw new IllegalArgumentException("First name cannot be empty");
        if (!firstName.matches("[\\p{L}\\s]+")) throw new IllegalArgumentException("First name can only contain letters and spaces");
        this.firstName = firstName.trim();
    }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { 
        if (lastName == null) throw new IllegalArgumentException("Last name cannot be null");
        if (lastName.trim().isEmpty()) throw new IllegalArgumentException("Last name cannot be empty");
        if (!lastName.matches("[\\p{L}\\s]+")) throw new IllegalArgumentException("Last name can only contain letters and spaces");
        this.lastName = lastName.trim();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { 
        if (username == null) throw new IllegalArgumentException("Username cannot be null");
        if (username.length() < 6) throw new IllegalArgumentException("Username must be at least 6 characters long");
        if (!username.matches("[a-zA-Z0-9._]+") || username.matches("[0-9]+")) throw new IllegalArgumentException("Username can only contain letters, numbers, dots and underscores");
        this.username = username.trim();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { 
        if (email == null) throw new IllegalArgumentException("Email cannot be null");
        if (!email.contains("@") || !email.contains(".")) throw new IllegalArgumentException("Invalid email format");
        this.email = email.trim();
    }

    public String getInstitution() { return institution; }
    public void setInstitution(String institution) { this.institution = institution; }

    public void setBio(String bio) { 
        if (bio == null) throw new IllegalArgumentException("Bio cannot be null");
        if (bio.length() > 200) throw new IllegalArgumentException("Bio cannot exceed 200 characters");
        this.bio = bio; 
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
}
