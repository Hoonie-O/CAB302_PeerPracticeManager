package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * This is a study session that can be linked to a study group.
 * This is just the basic structure to get things rolling.
 */
public class Session {
    
    private String sessionId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private User organiser;
    private List<User> participants;
    private SessionStatus status;
    private String location; // Could be "Online" or actual location
    
    public Session(String title, User organiser, LocalDateTime startTime, LocalDateTime endTime) {
        this.title = Objects.requireNonNull(title, "Session title can't be null");
        this.organiser = Objects.requireNonNull(organiser, "Organiser can't be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time can't be null");
        this.endTime = Objects.requireNonNull(endTime, "End time can't be null");
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Session can't end before it starts mate");
        }
        
        // Set up the basics
        this.participants = new ArrayList<>();
        this.status = SessionStatus.PLANNED;
        this.description = "";
        this.location = "TBD";
        
        // Auto-add organiser as a participant
        this.participants.add(organiser);
    }
    
    // Basic getters for now
    public String getTitle() { return title; }
    public User getOrganiser() { return organiser; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<User> getParticipants() { return new ArrayList<>(participants); }
    public SessionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    
    // Some basic functionality to get started
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }
    
    public void setLocation(String location) {
        this.location = location != null ? location : "TBD";
    }
    
    public void addParticipant(User user) {
        if (user != null && !participants.contains(user)) {
            participants.add(user);
        }
    }
    
    public void removeParticipant(User user) {
        // Can't remove the organiser though
        if (user != null && !user.equals(organiser)) {
            participants.remove(user);
        }
    }
    
    public void setStatus(SessionStatus status) {
        this.status = status != null ? status : SessionStatus.PLANNED;
    }
}