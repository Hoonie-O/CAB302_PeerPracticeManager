package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.SessionTask;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing session tasks and assignments.
 *
 * <p>This interface defines the contract for session task data operations,
 * providing methods to create, assign, track, and manage tasks associated
 * with practice sessions in the peer practice system.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Task creation and assignment for practice sessions</li>
 *   <li>User-specific task tracking and management</li>
 *   <li>Task status monitoring (overdue, completed)</li>
 *   <li>Bulk task operations and cleanup</li>
 * </ul>
 *
 * @see SessionTask
 */
public interface ISessionTaskDAO {
    /**
     * <hr>
     * Adds a new task to the system.
     *
     * <p>Creates and persists a new task entity associated with a practice session,
     * including task details, assignee information, and due date for
     * comprehensive task management.
     *
     * @param task the SessionTask object to be added
     */
    void addTask(SessionTask task);

    /**
     * <hr>
     * Retrieves all tasks for a specific session.
     *
     * <p>Fetches the complete task list for a particular practice session,
     * providing access to all assignments and responsibilities associated
     * with that session.
     *
     * @param sessionId the unique identifier of the session
     * @return a list of SessionTask objects for the specified session
     */
    List<SessionTask> getTasksForSession(String sessionId);

    /**
     * <hr>
     * Retrieves a specific task by its unique identifier.
     *
     * <p>Fetches a single task entity using its primary key, returning
     * the complete task details with all associated information.
     *
     * @param taskId the unique identifier of the task to retrieve
     * @return the SessionTask object if found, null otherwise
     */
    SessionTask getTaskById(String taskId);

    /**
     * <hr>
     * Updates an existing task with new information.
     *
     * <p>Modifies the attributes of an existing task record, allowing
     * for task progress updates, status changes, and detail modifications
     * during the task lifecycle.
     *
     * @param updatedTask the SessionTask object containing updated information
     * @return true if the task was successfully updated, false otherwise
     */
    boolean updateTask(SessionTask updatedTask);

    /**
     * <hr>
     * Removes a specific task from the system.
     *
     * <p>Deletes a task entity using its unique identifier, typically
     * when a task is cancelled, completed, or no longer relevant.
     *
     * @param taskId the unique identifier of the task to remove
     * @return true if the task was successfully removed, false otherwise
     */
    boolean removeTask(String taskId);

    /**
     * <hr>
     * Removes all tasks for a specific session.
     *
     * <p>Deletes the entire task list associated with a particular session,
     * typically used when a session is cancelled or requires complete
     * task cleanup.
     *
     * @param sessionId the unique identifier of the session
     * @return true if all tasks were successfully removed, false otherwise
     */
    boolean removeAllTasksForSession(String sessionId);

    /**
     * <hr>
     * Retrieves all tasks assigned to a specific user.
     *
     * <p>Fetches the complete task assignment list for a particular user,
     * providing a personalized view of their responsibilities across
     * all sessions.
     *
     * @param assigneeId the unique identifier of the user
     * @return a list of SessionTask objects assigned to the specified user
     */
    List<SessionTask> getTasksForUser(String assigneeId);

    /**
     * <hr>
     * Retrieves all overdue tasks in the system.
     *
     * <p>Fetches tasks that have passed their due dates without completion,
     * enabling proactive task management and deadline monitoring.
     *
     * @return a list of overdue SessionTask objects
     */
    List<SessionTask> getOverdueTasks();

    /**
     * <hr>
     * Retrieves all completed tasks in the system.
     *
     * <p>Fetches tasks that have been marked as completed, providing
     * access to task history and completion tracking for reporting
     * and analysis.
     *
     * @return a list of completed SessionTask objects
     */
    List<SessionTask> getCompletedTasks();

    /**
     * <hr>
     * Retrieves all tasks from the system.
     *
     * <p>Fetches every task entity stored in the database across all
     * sessions and users, providing comprehensive system-wide task
     * access for administrative purposes.
     *
     * @return a list of all SessionTask objects in the system
     */
    List<SessionTask> getAllTasks();

    /**
     * <hr>
     * Removes all tasks from the system.
     *
     * <p>Clears the entire task database, typically used for system
     * maintenance, testing scenarios, or administrative resets.
     */
    void clearAllTasks();

    /**
     * <hr>
     * Retrieves the total count of tasks in the system.
     *
     * <p>Provides a quick statistical count of all task records,
     * useful for system monitoring, reporting, and administrative
     * dashboards.
     *
     * @return the total number of task records in the system
     */
    int getTaskCount();
}