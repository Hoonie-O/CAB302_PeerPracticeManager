package com.cab302.peerpractice.Model.entities;

// Simple display model for group members table
public class GroupMember {
    private String name;
    private String role;
    private String availability;
    private String userId;

    public GroupMember(String name, String role, String availability, String userId) {
        this.name = name;
        this.role = role;
        this.availability = availability;
        this.userId = userId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}