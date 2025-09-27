package com.cab302.peerpractice.Model.entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Availability {
    private final String availabilityId; // unique identifier
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String colorLabel;
    private User user;
    private boolean isRecurring;
    private String recurringPattern;

    // Constructor with auto-generated ID
    public Availability(String title, User user, LocalDateTime startTime, LocalDateTime endTime, String colorLabel) {
        this(UUID.randomUUID().toString(), title, user, startTime, endTime, colorLabel);
    }

    // Constructor with explicit ID (used when loading from DB)
    public Availability(String availabilityId, String title, User user,
                        LocalDateTime startTime, LocalDateTime endTime, String colorLabel) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Availability title cannot be null or blank");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and End times cannot be null");
        }
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        this.availabilityId = Objects.requireNonNull(availabilityId, "availabilityId");
        this.title = title;
        this.user = Objects.requireNonNull(user, "user cannot be null");
        this.startTime = startTime;
        this.endTime = endTime;
        this.colorLabel = colorLabel != null ? colorLabel : "BLUE";
        this.description = "";
        this.isRecurring = false;
        this.recurringPattern = "NONE";
    }

    // --- Getters & Setters ---
    public String getAvailabilityId() {
        return availabilityId;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        if (title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
        this.title = title;
    }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description != null ? description : ""; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) {
        if (endTime != null && endTime.isBefore(startTime)) throw new IllegalArgumentException("Start time cannot be after end time");
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) {
        if (endTime.isBefore(startTime)) throw new IllegalArgumentException("End time cannot be before start time");
        this.endTime = endTime;
    }

    public String getColorLabel() { return colorLabel; }
    public void setColorLabel(String colorLabel) { this.colorLabel = colorLabel != null ? colorLabel : "BLUE"; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = Objects.requireNonNull(user, "user cannot be null"); }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getRecurringPattern() { return recurringPattern; }
    public void setRecurringPattern(String recurringPattern) {
        if (recurringPattern == null || recurringPattern.trim().isEmpty()) {
            this.recurringPattern = "NONE";
            this.isRecurring = false;
            return;
        }
        switch (recurringPattern.toUpperCase()) {
            case "DAILY": case "WEEKLY": case "FORTNIGHTLY": case "MONTHLY": case "YEARLY":
                this.recurringPattern = recurringPattern.toUpperCase();
                this.isRecurring = true;
                break;
            default:
                this.recurringPattern = "NONE";
                this.isRecurring = false;
                break;
        }
    }

    // --- Equality & Debugging ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Availability)) return false;
        Availability that = (Availability) o;
        return Objects.equals(availabilityId, that.availabilityId);
    }

    @Override
    public int hashCode() { return Objects.hash(availabilityId); }

    @Override
    public String toString() {
        return "Availability{" +
                "id='" + availabilityId + '\'' +
                ", title='" + title + '\'' +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", start=" + startTime +
                ", end=" + endTime +
                ", color='" + colorLabel + '\'' +
                '}';
    }
}
