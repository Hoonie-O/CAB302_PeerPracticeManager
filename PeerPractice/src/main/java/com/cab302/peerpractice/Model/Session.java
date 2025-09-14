package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Session {
    
    private String sessionId;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private User organiser;
    private List<User> participants;
    private SessionStatus status;
    private String location;
    private String colorLabel;
    private String subject; // what subject this session is for
    private int maxParticipants;
    
    public Session(String title, User organiser, LocalDateTime startTime, LocalDateTime endTime) {
        this.sessionId = java.util.UUID.randomUUID().toString(); // generate unique session ID
        this.title = Objects.requireNonNull(title, "Session title cant be null");
        this.organiser = Objects.requireNonNull(organiser, "Organiser cant be null");
        this.startTime = Objects.requireNonNull(startTime, "Start time cant be null");
        this.endTime = Objects.requireNonNull(endTime, "End time cant be null");
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Session cant end before it starts mate");
        }
        
        this.participants = new ArrayList<>();
        this.status = SessionStatus.PLANNED;
        this.description = "";
        this.location = "TBD";
        this.colorLabel = "BLUE";
        this.subject = "";
        this.maxParticipants = 10; // reasonable default
        
        // organiser is always a participant
        this.participants.add(organiser);
    }
    
    // getters
    public String getSessionId() { return sessionId; }
    public String getTitle() { return title; }
    public User getOrganiser() { return organiser; }
    public LocalDateTime getStartTime() { return startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public List<User> getParticipants() { return new ArrayList<>(participants); }
    public SessionStatus getStatus() { return status; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public String getColorLabel() { return colorLabel; }
    public String getSubject() { return subject; }
    public int getMaxParticipants() { return maxParticipants; }
    
    // setters for calendar integration
    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "title cannot be null");
    }
    
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        if (endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
    }
    
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }
    
    public void setLocation(String location) {
        this.location = location != null ? location : "TBD";
    }
    
    public void setColorLabel(String colorLabel) {
        this.colorLabel = colorLabel != null ? colorLabel : "BLUE";
    }
    
    public void setSubject(String subject) {
        this.subject = subject != null ? subject : "";
    }
    
    public void setMaxParticipants(int maxParticipants) {
        // cant be less than current participant count
        this.maxParticipants = Math.max(maxParticipants, participants.size());
    }
    
    // participant management
    public boolean addParticipant(User user) {
        if (user == null || participants.contains(user)) {
            return false;
        }
        
        // check if we have space
        if (participants.size() >= maxParticipants) {
            return false;
        }
        
        participants.add(user);
        return true;
    }
    
    public boolean removeParticipant(User user) {
        // cant remove the organiser
        if (user == null || user.equals(organiser)) {
            return false;
        }
        
        return participants.remove(user);
    }
    
    public void setStatus(SessionStatus status) {
        this.status = status != null ? status : SessionStatus.PLANNED;
    }
    
    public int getParticipantCount() {
        return participants.size();
    }
    
    public boolean hasSpace() {
        return participants.size() < maxParticipants;
    }
    
    public boolean isParticipant(User user) {
        return user != null && participants.contains(user);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(sessionId, session.sessionId) ||
               (Objects.equals(title, session.title) &&
                Objects.equals(startTime, session.startTime) &&
                Objects.equals(organiser, session.organiser));
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, startTime, organiser);
    }
    
    @Override
    public String toString() {
        return "Session{" +
                "title='" + title + '\'' +
                ", organiser=" + (organiser != null ? organiser.getUsername() : "null") +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", participants=" + participants.size() +
                ", status=" + status +
                '}';
    }
}