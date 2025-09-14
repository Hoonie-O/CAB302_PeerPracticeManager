package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles storage and retrieval of session tasks. Currently uses in-memory storage
 */
public class SessionTaskStorage {
    
    private final List<SessionTask> tasks;
    
    public SessionTaskStorage() {
        this.tasks = new ArrayList<>();
    }
    
    /**
     * Adds a new task to storage
     */
    public void addTask(SessionTask task) {
        if (task != null) {
            tasks.add(task);
        }
    }
    
    /**
     * Gets all tasks for a specific session
     */
    public List<SessionTask> getTasksForSession(String sessionId) {
        if (sessionId == null) return new ArrayList<>();
        
        return tasks.stream()
                .filter(task -> sessionId.equals(task.getSessionId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets a specific task by its ID
     */
    public SessionTask getTaskById(String taskId) {
        if (taskId == null) return null;
        
        return tasks.stream()
                .filter(task -> taskId.equals(task.getTaskId()))
                .findFirst()
                .orElse(null);
    }
    
    /**
     * Updates an existing task. Returns true if successful, false if task not found
     */
    public boolean updateTask(SessionTask updatedTask) {
        if (updatedTask == null || updatedTask.getTaskId() == null) {
            return false;
        }
        
        for (int i = 0; i < tasks.size(); i++) {
            if (updatedTask.getTaskId().equals(tasks.get(i).getTaskId())) {
                tasks.set(i, updatedTask);
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Removes a task from storage
     */
    public boolean removeTask(String taskId) {
        if (taskId == null) return false;
        
        return tasks.removeIf(task -> taskId.equals(task.getTaskId()));
    }
    
    /**
     * Gets all tasks assigned to a specific user
     */
    public List<SessionTask> getTasksForUser(String assigneeId) {
        if (assigneeId == null) return new ArrayList<>();
        
        return tasks.stream()
                .filter(task -> assigneeId.equals(task.getAssigneeId()))
                .collect(Collectors.toList());
    }
    
    /**
     * Gets overdue tasks (past deadline and not completed)
     */
    public List<SessionTask> getOverdueTasks() {
        return tasks.stream()
                .filter(SessionTask::isOverdue)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets completed tasks
     */
    public List<SessionTask> getCompletedTasks() {
        return tasks.stream()
                .filter(SessionTask::isCompleted)
                .collect(Collectors.toList());
    }
    
    /**
     * Gets all tasks (mainly for debugging or admin views)
     */
    public List<SessionTask> getAllTasks() {
        return new ArrayList<>(tasks);
    }
    
    /**
     * Clears all tasks (mainly for testing)
     */
    public void clearAllTasks() {
        tasks.clear();
    }
    
    /**
     * Gets total task count
     */
    public int getTaskCount() {
        return tasks.size();
    }
}