package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.List;

/**
 * This manages session tasks with proper validation
 * Responsible for creation, updating, and deletion of tasks with checks
 */
public class SessionTaskManager {
    
    private final SessionTaskStorage storage;
    private final SessionManager sessionManager;
    
    public SessionTaskManager(SessionTaskStorage storage, SessionManager sessionManager) {
        this.storage = storage;
        this.sessionManager = sessionManager;
    }
    
    /**
     * Creates a new task for a session. checks that the session exists and
     * the assignee is a participant in the session.
     */
    public SessionTask createTask(String sessionId, String title, LocalDateTime deadline, 
                                 String assigneeId, String createdBy) throws IllegalArgumentException {
        
        // find the session to validate it exists and check whos in it
        Session session = findSessionById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }
        
        // validate that assignee is a participant
        if (!isUserParticipant(session, assigneeId)) {
            throw new IllegalArgumentException("Assignee must be a participant in the session");
        }
        
        // validate that creator is a participant (basic permission check)
        if (!isUserParticipant(session, createdBy)) {
            throw new IllegalArgumentException("Only session participants can create tasks");
        }
        
        // create and store the task
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        storage.addTask(task);
        
        return task;
    }
    
    /**
     * Updates an existing task then checks permissions and participant status
     */
    public SessionTask updateTask(String taskId, String title, LocalDateTime deadline, 
                                 String assigneeId, String updatedBy) throws IllegalArgumentException {
        
        SessionTask existingTask = storage.getTaskById(taskId);
        if (existingTask == null) {
            throw new IllegalArgumentException("Task not found");
        }
        
        // find the session to validate participants
        Session session = findSessionById(existingTask.getSessionId());
        if (session == null) {
            throw new IllegalArgumentException("Session not found");
        }
        
        // validate that new assignee is a participant
        if (!isUserParticipant(session, assigneeId)) {
            throw new IllegalArgumentException("Assignee must be a participant in the session");
        }
        
        // validate that updater is a participant
        if (!isUserParticipant(session, updatedBy)) {
            throw new IllegalArgumentException("Only session participants can update tasks");
        }
        
        // create updated task (keeping original creation info)
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
     * Deletes a task. Only the creator or assignee can delete their tasks.
     */
    public boolean deleteTask(String taskId, String deletedBy) {
        SessionTask task = storage.getTaskById(taskId);
        if (task == null) {
            return false;
        }
        
        // validate permission only the creator or assignee can do this
        if (!task.getCreatedBy().equals(deletedBy) && !task.getAssigneeId().equals(deletedBy)) {
            throw new IllegalArgumentException("Only the task creator or assignee can delete this task");
        }
        
        return storage.removeTask(taskId);
    }
    
    /**
     * Marks a task as completed, only the assignee can mark their own tasks complete.
     */
    public boolean markTaskCompleted(String taskId, String completedBy) {
        SessionTask task = storage.getTaskById(taskId);
        if (task == null) {
            return false;
        }

        if (!task.getAssigneeId().equals(completedBy)) {
            throw new IllegalArgumentException("Only the assignee can mark this task as completed");
        }
        
        task.setCompleted(true);
        return storage.updateTask(task);
    }
    
    /**
     * Gets all tasks for a session
     */
    public List<SessionTask> getSessionTasks(String sessionId) {
        return storage.getTasksForSession(sessionId);
    }
    
    /**
     * Gets tasks assigned to a specific user
     */
    public List<SessionTask> getUserTasks(String userId) {
        return storage.getTasksForUser(userId);
    }
    
    /**
     * Gets overdue tasks
     */
    public List<SessionTask> getOverdueTasks() {
        return storage.getOverdueTasks();
    }
    
    /**
     * Helper method to find a session by ID from session manager
     */
    private Session findSessionById(String sessionId) {
        return sessionManager.findSessionById(sessionId);
    }
    
    /**
     * Helper method to check if a user is a participant in a session
     */
    private boolean isUserParticipant(Session session, String userId) {
        if (userId == null) return false;
        
        for (User participant : session.getParticipants()) {
            if (userId.equals(participant.getUserId())) {
                return true;
            }
        }
        return false;
    }
}