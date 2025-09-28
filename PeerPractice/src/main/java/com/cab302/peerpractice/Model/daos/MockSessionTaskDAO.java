package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.SessionTask;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock (in-memory) implementation of ISessionTaskDAO for unit testing.
 */
public class MockSessionTaskDAO implements ISessionTaskDAO {

    private final Map<String, SessionTask> tasks = new ConcurrentHashMap<>();

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

    @Override
    public List<SessionTask> getTasksForSession(String sessionId) {
        return tasks.values().stream()
                .filter(t -> t.getSessionId().equals(sessionId))
                .toList();
    }

    @Override
    public SessionTask getTaskById(String taskId) {
        return tasks.get(taskId);
    }

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

    @Override
    public boolean removeTask(String taskId) {
        return tasks.remove(taskId) != null;
    }

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

    @Override
    public List<SessionTask> getTasksForUser(String assigneeId) {
        return tasks.values().stream()
                .filter(t -> t.getAssigneeId().equals(assigneeId))
                .toList();
    }

    @Override
    public List<SessionTask> getOverdueTasks() {
        LocalDateTime now = LocalDateTime.now();
        return tasks.values().stream()
                .filter(t -> !t.isCompleted() && t.getDeadline().isBefore(now))
                .toList();
    }

    @Override
    public List<SessionTask> getCompletedTasks() {
        return tasks.values().stream()
                .filter(SessionTask::isCompleted)
                .toList();
    }

    @Override
    public List<SessionTask> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public void clearAllTasks() {
        tasks.clear();
    }

    @Override
    public int getTaskCount() {
        return tasks.size();
    }
}
