package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.Objects;

public class Event {
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String colorLabel;

    public Event(String title, String description, LocalDateTime startTime, LocalDateTime endTime, String colorLabel) {
        this.title = Objects.requireNonNull(title, "title");
        this.description = description != null ? description : "";
        this.startTime = Objects.requireNonNull(startTime, "startTime");
        this.endTime = Objects.requireNonNull(endTime, "endTime");
        this.colorLabel = colorLabel != null ? colorLabel : "BLUE";
        
        if (endTime.isBefore(startTime)) {
            throw new IllegalArgumentException("End time cannot be before start time");
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Event event = (Event) o;
        return Objects.equals(title, event.title) &&
               Objects.equals(description, event.description) &&
               Objects.equals(startTime, event.startTime) &&
               Objects.equals(endTime, event.endTime) &&
               Objects.equals(colorLabel, event.colorLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, startTime, endTime, colorLabel);
    }

    @Override
    public String toString() {
        return "Event{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", colorLabel='" + colorLabel + '\'' +
                '}';
    }
}