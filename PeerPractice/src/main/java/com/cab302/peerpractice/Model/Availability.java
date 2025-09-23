package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Availability {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String colorLabel;
    private User user;
    private boolean isRecurring;
    private String recurringPattern; // DAILY WEEKLY MONTHLY

    public Availability(String title, User user, LocalDateTime startTime, LocalDateTime endTime, String colorLabel) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Session title cannot be null or blank");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and End times cannot be null");
        }
        if (endTime.isBefore(startTime) || endTime.equals(startTime)) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        this.title = Objects.requireNonNull(title, "title");
        this.user = Objects.requireNonNull(user, "user cannot be null");
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        this.colorLabel = colorLabel != null ? colorLabel : "BLUE";
        this.description = "";
        this.isRecurring = false;
        this.recurringPattern = "NONE";
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        if (title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be null or blank");
        }
        this.title = Objects.requireNonNull(title, "title");
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        if (endTime != null && endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("Start time cannot be after end time");
        }
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    public String getColorLabel() {
        return colorLabel;
    }

    public void setColorLabel(String colorLabel) {
        this.colorLabel = colorLabel != null ? colorLabel : "BLUE";
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = Objects.requireNonNull(user, "user cannot be null");
    }

    public boolean isRecurring() {
        return isRecurring;
    }

    public void setRecurring(boolean recurring) {
        isRecurring = recurring;
    }

    public String getRecurringPattern() {
        return recurringPattern;
    }

    public void setRecurringPattern(String recurringPattern) {
        if (recurringPattern == null || recurringPattern.trim().isEmpty()) {
            this.recurringPattern = "NONE";
            this.isRecurring = false;
            return;
        }

        switch (recurringPattern.toUpperCase()) {
            case "DAILY":
            case "WEEKLY":
            case "FORTNIGHTLY":
            case "MONTHLY":
            case "YEARLY":
                this.recurringPattern = recurringPattern.toUpperCase();
                this.isRecurring = true;
                break;
            default:
                this.recurringPattern = "NONE";
                this.isRecurring = false;
                break;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Availability that = (Availability) o;
        return isRecurring == that.isRecurring &&
               Objects.equals(title, that.title) &&
               Objects.equals(description, that.description) &&
               Objects.equals(startTime, that.startTime) &&
               Objects.equals(endTime, that.endTime) &&
               Objects.equals(colorLabel, that.colorLabel) &&
               Objects.equals(user, that.user) &&
               Objects.equals(recurringPattern, that.recurringPattern);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, startTime, endTime, colorLabel, user, isRecurring, recurringPattern);
    }

    @Override
    public String toString() {
        return "Availability{" +
                "title='" + title + '\'' +
                ", user=" + (user != null ? user.getUsername() : "null") +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", colorLabel='" + colorLabel + '\'' +
                ", isRecurring=" + isRecurring +
                '}';
    }
}