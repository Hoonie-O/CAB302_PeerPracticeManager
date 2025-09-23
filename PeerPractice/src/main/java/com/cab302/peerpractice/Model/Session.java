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
    private String subject;
    private int maxParticipants;
    private Group group;

    public Session(String title, User organiser, LocalDateTime startTime, LocalDateTime endTime) {
        this.sessionId = java.util.UUID.randomUUID().toString();

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Session title cannot be null or blank");
        }
        if (organiser == null) {
            throw new IllegalArgumentException("Organiser cannot be null");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and End times cannot be null");
        }
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        this.title = title;
        this.organiser = organiser;
        this.startTime = startTime;
        this.endTime = endTime;

        this.participants = new ArrayList<>();
        this.status = SessionStatus.PLANNED;
        this.description = "";
        this.location = "TBD";
        this.colorLabel = "BLUE";
        this.subject = "";
        this.maxParticipants = 10;

        // organiser is always a participant
        this.participants.add(organiser);

        this.group = null; // default: not tied to a group
    }

    // Convenience constructor to set group immediately
    public Session(String title, User organiser, LocalDateTime startTime, LocalDateTime endTime, Group group) {
        this(title, organiser, startTime, endTime);
        this.group = group;
    }

    // === Getters ===
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
    public Group getGroup() { return group; }

    // === Setters ===
    public void setTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Invalid title");
        }
        this.title = title;
    }

    public void setStartTime(LocalDateTime startTime) {
        if (startTime == null) throw new IllegalArgumentException("startTime cannot be null");
        if (endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
        this.startTime = startTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        if (endTime == null) throw new IllegalArgumentException("endTime cannot be null");
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
        this.endTime = endTime;
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
        this.maxParticipants = Math.max(maxParticipants, participants.size());
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    public void setStatus(SessionStatus status) {
        this.status = status != null ? status : SessionStatus.PLANNED;
    }

    // === Participant management ===
    public boolean addParticipant(User user) {
        if (user == null || participants.contains(user)) {
            return false;
        }
        if (participants.size() >= maxParticipants) {
            return false;
        }
        participants.add(user);
        return true;
    }

    public boolean removeParticipant(User user) {
        if (user == null || user.equals(organiser)) {
            return false;
        }
        return participants.remove(user);
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

    // === Equality ===
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Session)) return false;
        Session session = (Session) o;
        return Objects.equals(sessionId, session.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId);
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
                ", group=" + (group != null ? group.getName() : "null") +
                '}';
    }
}
