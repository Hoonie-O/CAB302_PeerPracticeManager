package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.SessionTask;
import java.util.List;

public interface ISessionTaskDAO {
    void addTask(SessionTask task);

    List<SessionTask> getTasksForSession(String sessionId);
    SessionTask getTaskById(String taskId);
    boolean updateTask(SessionTask updatedTask);
    boolean removeTask(String taskId);
    boolean removeAllTasksForSession(String sessionId);

    List<SessionTask> getTasksForUser(String assigneeId);
    List<SessionTask> getOverdueTasks();
    List<SessionTask> getCompletedTasks();
    List<SessionTask> getAllTasks();

    void clearAllTasks();
    int getTaskCount();
}
