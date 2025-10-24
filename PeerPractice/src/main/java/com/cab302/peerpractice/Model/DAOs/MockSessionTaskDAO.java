package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.SessionTask;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <hr>
 * Mock (in-memory) implementation of ISessionTaskDAO for unit testing.
 *
 * <p>This implementation provides in-memory storage for session tasks
 * to facilitate testing without requiring a real database connection.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Thread-safe concurrent task storage</li>
 *   <li>Automatic UUID generation for missing task IDs</li>
 *   <li>Timestamp management for task updates</li>
 *   <li>Flexible task retrieval by session, user, and status</li>
 * </ul>
 *
 * @see ISessionTaskDAO
 * @see SessionTask
 */
public class MockSessionTaskDAO implements ISessionTaskDAO {

    /** <hr> In-memory storage for tasks by task ID. */
    private final Map<String, SessionTask> tasks = new ConcurrentHashMap<>();

    /**
     * <hr>
     * Adds a new task to the in-memory storage.
     *
     * <p>Automatically generates a task ID if missing and updates timestamps.
     *
     * @param task the task to add
     */
    @Override
    public void addTask(SessionTask task) {
        if (task == null) return;

        // Generate taskId if missing
        if (task.getTaskId() == null) {
            try {
                var idField = SessionTask.class.getDeclaredField("taskId");
                idField.setAccessible(true);
                idField.set(task, UUID.randomUUID().toString());
            } catch (Exception ignored) {}
        }

        // Update timestamps
        try {
            var updatedField = SessionTask.class.getDeclaredField("updated");
            updatedField.setAccessible(true);
            updatedField.set(task, LocalDateTime.now());
        } catch (Exception ignored) {}

        tasks.put(task.getTaskId(), task);
    }

    /**
     * <hr>
     * Retrieves all tasks for a specific session.
     *
     * @param sessionId the ID of the session
     * @return a list of tasks associated with the specified session
     */
    @Override
    public List<SessionTask> getTasksForSession(String sessionId) {
        return tasks.values().stream()
                .filter(t -> t.getSessionId().equals(sessionId))
                .toList();
    }

    /**
     * <hr>
     * Retrieves a specific task by its ID.
     *
     * @param taskId the ID of the task to retrieve
     * @return the task with the specified ID, or null if not found
     */
    @Override
    public SessionTask getTaskById(String taskId) {
        return tasks.get(taskId);
    }

    /**
     * <hr>
     * Updates an existing task in the in-memory storage.
     *
     * <p>Automatically updates the task's timestamp.
     *
     * @param updatedTask the task with updated information
     * @return true if the task was updated successfully
     */
    @Override
    public boolean updateTask(SessionTask updatedTask) {
        if (updatedTask == null || !tasks.containsKey(updatedTask.getTaskId())) return false;

        try {
            var updatedField = SessionTask.class.getDeclaredField("updated");
            updatedField.setAccessible(true);
            updatedField.set(updatedTask, LocalDateTime.now());
        } catch (Exception ignored) {}

        tasks.put(updatedTask.getTaskId(), updatedTask);
        return true;
    }

    /**
     * <hr>
     * Removes a specific task from the in-memory storage.
     *
     * @param taskId the ID of the task to remove
     * @return true if the task was removed successfully
     */
    @Override
    public boolean removeTask(String taskId) {
        return tasks.remove(taskId) != null;
    }

    /**
     * <hr>
     * Removes all tasks associated with a specific session.
     *
     * @param sessionId the ID of the session whose tasks should be removed
     * @return true if any tasks were removed
     */
    @Override
    public boolean removeAllTasksForSession(String sessionId) {
        boolean removed = false;
        Iterator<SessionTask> it = tasks.values().iterator();
        while (it.hasNext()) {
            if (it.next().getSessionId().equals(sessionId)) {
                it.remove();
                removed = true;
            }
        }
        return removed;
    }

    /**
     * <hr>
     * Retrieves all tasks assigned to a specific user.
     *
     * @param assigneeId the ID of the user
     * @return a list of tasks assigned to the specified user
     */
    @Override
    public List<SessionTask> getTasksForUser(String assigneeId) {
        return tasks.values().stream()
                .filter(t -> t.getAssigneeId().equals(assigneeId))
                .toList();
    }

    /**
     * <hr>
     * Retrieves all overdue tasks (incomplete tasks with past deadlines).
     *
     * @return a list of overdue tasks
     */
    @Override
    public List<SessionTask> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.values().stream()
                .filter(t -> !t.isCompleted() && t.getDeadline().isBefore(now))
                .toList();
    }

    /**
     * <hr>
     * Retrieves all completed tasks.
     *
     * @return a list of completed tasks
     */
    @Override
    public List<SessionTask> getCompletedTasks() {
        return tasks.values().stream()
                .filter(SessionTask::isCompleted)
                .toList();
    }

    /**
     * <hr>
     * Retrieves all tasks from the in-memory storage.
     *
     * @return a list of all tasks
     */
    @Override
    public List<SessionTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    /**
     * <hr>
     * Clears all tasks from the in-memory storage.
     *
     * <p>Used primarily for testing cleanup.
     */
    @Override
    public void clearAllTasks() {
        tasks.clear();
    }

    /**
     * <hr>
     * Gets the total number of tasks in the in-memory storage.
     *
     * @return the count of tasks
     */
    @Override
    public int getTaskCount() {
        return tasks.size();
    }
}