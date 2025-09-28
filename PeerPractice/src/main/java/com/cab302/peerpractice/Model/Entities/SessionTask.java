package com.cab302.peerpractice.Model.Entities;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * This represents a task within a study session that can be assigned to participants.
 * Tasks have titles, deadlines, and assignees to help organize session responsibilities.
 */
public class SessionTask {
    
    private String taskId;
    private String sessionId;
    private String title;
    private LocalDateTime deadline;
    private String assigneeId; // User ID of the person this task is assigned to
    private String createdBy; // User ID who created the task
    private LocalDateTime createdAt;
    private boolean completed;
    
    public SessionTask(String sessionId, String title, LocalDateTime deadline, String assigneeId, String createdBy) {
        this.taskId = UUID.randomUUID().toString();
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        this.title = Objects.requireNonNull(title, "Task title cannot be null");
        this.deadline = Objects.requireNonNull(deadline, "Deadline cannot be null");
        this.assigneeId = Objects.requireNonNull(assigneeId, "Assignee ID cannot be null");
        this.createdBy = Objects.requireNonNull(createdBy, "Created by cannot be null");
        this.createdAt = LocalDateTime.now();
        this.completed = false;
    }
    
    // constructor for loading from storage (includes taskId and creation time)
    public SessionTask(String taskId, String sessionId, String title, LocalDateTime deadline, 
                      String assigneeId, String createdBy, LocalDateTime createdAt, boolean completed) {
        this.taskId = Objects.requireNonNull(taskId, "Task ID cannot be null");
        this.sessionId = Objects.requireNonNull(sessionId, "Session ID cannot be null");
        this.title = Objects.requireNonNull(title, "Task title cannot be null");
        this.deadline = Objects.requireNonNull(deadline, "Deadline cannot be null");
        this.assigneeId = Objects.requireNonNull(assigneeId, "Assignee ID cannot be null");
        this.createdBy = Objects.requireNonNull(createdBy, "Created by cannot be null");
        this.createdAt = Objects.requireNonNull(createdAt, "Created at cannot be null");
        this.completed = completed;
    }
    

    public String getTaskId() { return taskId; }
    public String getSessionId() { return sessionId; }
    public String getTitle() { return title; }
    public LocalDateTime getDeadline() { return deadline; }
    public String getAssigneeId() { return assigneeId; }
    public String getCreatedBy() { return createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public boolean isCompleted() { return completed; }

    public void setTitle(String title) {
        this.title = Objects.requireNonNull(title, "Task title cannot be null");
    }
    
    public void setDeadline(LocalDateTime deadline) {
        Objects.requireNonNull(deadline, "Deadline cannot be null");
        if (deadline.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Deadline cannot be in the past");
        }
        this.deadline = deadline;
    }
    
    public void setAssigneeId(String assigneeId) {
        this.assigneeId = Objects.requireNonNull(assigneeId, "Assignee ID cannot be null");
    }
    
    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
    

    public boolean isOverdue() {
        return !completed && deadline.isBefore(LocalDateTime.now());
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SessionTask that = (SessionTask) o;
        return Objects.equals(taskId, that.taskId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(taskId);
    }
    
    @Override
    public String toString() {
        return "SessionTask{" +
                "taskId='" + taskId + '\'' +
                ", sessionId='" + sessionId + '\'' +
                ", title='" + title + '\'' +
                ", deadline=" + deadline +
                ", assigneeId='" + assigneeId + '\'' +
                ", completed=" + completed +
                '}';
    }
}