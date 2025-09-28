package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.SessionTask;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.DAOs.ISessionTaskDAO;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Manages session tasks with proper validation and permission checks.
 * Uses an ISessionTaskDAO for persistence (in-memory or DB).
 */
public class SessionTaskManager {

    private final ISessionTaskDAO storage;
    private final SessionManager sessionManager;

    public SessionTaskManager(ISessionTaskDAO storage, SessionManager sessionManager) {
        this.storage = storage;
        this.sessionManager = sessionManager;
    }

    /**
     * Creates a new task for a session, validating session existence and participants.
     */
    public SessionTask createTask(String sessionId, String title, LocalDateTime deadline,
                                  String assigneeId, String createdBy) {
        Session session = findSessionById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }

        if (!isUserParticipant(session, assigneeId)) {
            throw new IllegalArgumentException("Assignee must be a participant in the session");
        }

        if (!isUserParticipant(session, createdBy)) {
            throw new IllegalArgumentException("Only session participants can create tasks");
        }

        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        storage.addTask(task);
        return task;
    }

    /**
     * Updates an existing task with new details, validating participants and permissions.
     */
    public SessionTask updateTask(String taskId, String title, LocalDateTime deadline,
                                  String assigneeId, String updatedBy) {
        SessionTask existingTask = storage.getTaskById(taskId);
        if (existingTask == null) {
            throw new IllegalArgumentException("Task not found");
        }

        Session session = findSessionById(existingTask.getSessionId());
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }

        if (!isUserParticipant(session, assigneeId)) {
            throw new IllegalArgumentException("Assignee must be a participant in the session");
        }

        if (!isUserParticipant(session, updatedBy)) {
            throw new IllegalArgumentException("Only session participants can update tasks");
        }

        SessionTask updatedTask = new SessionTask(
                existingTask.getTaskId(),
                existingTask.getSessionId(),
                title,
                deadline,
                assigneeId,
                existingTask.getCreatedBy(),
                existingTask.getCreatedAt(),
                existingTask.isCompleted()
        );

        if (storage.updateTask(updatedTask)) {
            return updatedTask;
        } else {
            throw new IllegalStateException("Failed to update task");
        }
    }

    /**
     * Deletes a task. Only the creator or assignee can delete it.
     */
    public boolean deleteTask(String taskId, String deletedBy) {
        SessionTask task = storage.getTaskById(taskId);
        if (task == null) return false;

        if (!task.getCreatedBy().equals(deletedBy) && !task.getAssigneeId().equals(deletedBy)) {
            throw new IllegalArgumentException("Only the task creator or assignee can delete this task");
        }

        return storage.removeTask(taskId);
    }

    /**
     * Marks a task as completed. Only the assignee can do this.
     */
    public boolean markTaskCompleted(String taskId, String completedBy) {
        SessionTask task = storage.getTaskById(taskId);
        if (task == null) return false;

        if (!task.getAssigneeId().equals(completedBy)) {
            throw new IllegalArgumentException("Only the assignee can mark this task as completed");
        }

        task.setCompleted(true);
        return storage.updateTask(task);
    }

    // ---------------- Retrieval methods ----------------
    public List<SessionTask> getSessionTasks(String sessionId) {
        return storage.getTasksForSession(sessionId);
    }

    public List<SessionTask> getUserTasks(String userId) {
        return storage.getTasksForUser(userId);
    }

    public List<SessionTask> getOverdueTasks() {
        return storage.getOverdueTasks();
    }

    public boolean deleteAllTasksForSession(String sessionId) {
        return storage.removeAllTasksForSession(sessionId);
    }

    // ---------------- Helper methods ----------------
    private Session findSessionById(String sessionId) {
        return sessionManager.findSessionById(sessionId);
    }

    private boolean isUserParticipant(Session session, String userId) {
        if (userId == null) return false;
        for (User participant : session.getParticipants()) {
            if (userId.equals(participant.getUserId())) return true;
        }
        return false;
    }
}
