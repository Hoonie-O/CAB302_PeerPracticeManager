package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.SessionTask;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <hr>
 * Database storage implementation of ISessionTaskDAO for persistent task storage.
 *
 * <p>This implementation provides SQLite-based persistent storage for session tasks
 * with comprehensive task management and querying capabilities.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Persistent task storage with full CRUD operations</li>
 *   <li>Task assignment and completion tracking</li>
 *   <li>Flexible querying by session, user, and status</li>
 *   <li>Overdue task detection and filtering</li>
 *   <li>Automatic timestamp management</li>
 * </ul>
 *
 * @see ISessionTaskDAO
 * @see SessionTask
 */
public class SessionTaskDAO implements ISessionTaskDAO {
    /** <hr> SQLite database connection instance. */
    private final Connection connection;

    /**
     * <hr>
     * Constructs a new SessionTaskDAO and initializes database tables.
     *
     * @param userDao the user DAO for user validation (currently unused but required by interface)
     * @throws SQLException if database connection or table creation fails
     */
    public SessionTaskDAO(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTables();
    }

    /**
     * <hr>
     * Creates the necessary database tables if they don't exist.
     *
     * @throws SQLException if table creation fails
     */
    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS session_tasks (" +
                    "task_id TEXT PRIMARY KEY, " +
                    "session_id TEXT NOT NULL, " +
                    "session TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "deadline TEXT NOT NULL, " +
                    "assignee_id TEXT NOT NULL, " +
                    "created_by TEXT NOT NULL, " +
                    "completed INTEGER DEFAULT 0, " +
                    "created_at TEXT NOT NULL, " +
                    "updated TEXT NOT NULL" +
                    ")");
        }
    }

    /**
     * <hr>
     * Maps a database ResultSet row to a SessionTask object.
     *
     * @param rs the ResultSet containing task data
     * @return a SessionTask object populated with data from the ResultSet
     * @throws SQLException if database access error occurs
     */
    private SessionTask mapRowToTask(ResultSet rs) throws SQLException {
        String taskId = rs.getString("task_id");
        String sessionId = rs.getString("session_id");
        String title = rs.getString("title");
        LocalDateTime deadline = LocalDateTime.parse(rs.getString("deadline"));
        String assigneeId = rs.getString("assignee_id");
        String createdBy = rs.getString("created_by");
        boolean completed = rs.getInt("completed") == 1;
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));

        SessionTask task = new SessionTask(taskId, sessionId, title, deadline, assigneeId, createdBy, createdAt, completed);
        return task;
    }

    /**
     * <hr>
     * Adds a new task to the database.
     *
     * @param task the task to add
     */
    @Override
    public void addTask(SessionTask task) {
        if (task == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO session_tasks " +
                        "(task_id, session_id, session, title, deadline, assignee_id, created_by, completed, created_at, updated) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            ps.setString(1, task.getTaskId());
            ps.setString(2, task.getSessionId());
            ps.setString(3, task.getSessionId()); // fallback session string
            ps.setString(4, task.getTitle());
            ps.setString(5, task.getDeadline().toString());
            ps.setString(6, task.getAssigneeId());
            ps.setString(7, task.getCreatedBy());
            ps.setInt(8, task.isCompleted() ? 1 : 0);
            ps.setString(9, task.getCreatedAt().toString());
            ps.setString(10, LocalDateTime.now().toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding task: " + e.getMessage());
        }
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
        List<SessionTask> tasks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM session_tasks WHERE session_id = ?")) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting tasks for session: " + e.getMessage());
        }
        return tasks;
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
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM session_tasks WHERE task_id = ?")) {
            ps.setString(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToTask(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting task by id: " + e.getMessage());
        }
        return null;
    }

    /**
     * <hr>
     * Updates an existing task in the database.
     *
     * @param updatedTask the task with updated information
     * @return true if the task was updated successfully
     */
    @Override
    public boolean updateTask(SessionTask updatedTask) {
        if (updatedTask == null) {
            return false; // or throw IllegalArgumentException
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE session_tasks SET title = ?, deadline = ?, assignee_id = ?, completed = ?, updated = ? " +
                        "WHERE task_id = ?")) {
            ps.setString(1, updatedTask.getTitle());
            ps.setString(2, updatedTask.getDeadline().toString());
            ps.setString(3, updatedTask.getAssigneeId());
            ps.setInt(4, updatedTask.isCompleted() ? 1 : 0);
            ps.setString(5, LocalDateTime.now().toString());
            ps.setString(6, updatedTask.getTaskId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
            return false;
        }
    }

    /**
     * <hr>
     * Removes a specific task from the database.
     *
     * @param taskId the ID of the task to remove
     * @return true if the task was removed successfully
     */
    @Override
    public boolean removeTask(String taskId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM session_tasks WHERE task_id = ?")) {
            ps.setString(1, taskId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing task: " + e.getMessage());
            return false;
        }
    }

    /**
     * <hr>
     * Removes all tasks associated with a specific session.
     *
     * @param sessionId the ID of the session whose tasks should be removed
     * @return true if the operation completed successfully
     */
    @Override
    public boolean removeAllTasksForSession(String sessionId) {
        if (sessionId == null) {
            return false; // or throw IllegalArgumentException
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM session_tasks WHERE session_id = ?")) {
            ps.setString(1, sessionId);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error removing all tasks: " + e.getMessage());
            return false;
        }
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
        List<SessionTask> tasks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM session_tasks WHERE assignee_id = ?")) {
            ps.setString(1, assigneeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting tasks for user: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * <hr>
     * Retrieves all overdue tasks (incomplete tasks with past deadlines).
     *
     * @return a list of overdue tasks
     */
    @Override
    public List<SessionTask> getOverdueTasks() {
        List<SessionTask> tasks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM session_tasks WHERE completed = 0 AND deadline < ?")) {
            ps.setString(1, LocalDateTime.now().toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting overdue tasks: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * <hr>
     * Retrieves all completed tasks.
     *
     * @return a list of completed tasks
     */
    @Override
    public List<SessionTask> getCompletedTasks() {
        List<SessionTask> tasks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM session_tasks WHERE completed = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) tasks.add(mapRowToTask(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting completed tasks: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * <hr>
     * Retrieves all tasks from the database.
     *
     * @return a list of all tasks
     */
    @Override
    public List<SessionTask> getAllTasks() {
        List<SessionTask> tasks = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM session_tasks")) {
            while (rs.next()) tasks.add(mapRowToTask(rs));
        } catch (SQLException e) {
            System.err.println("Error getting all tasks: " + e.getMessage());
        }
        return tasks;
    }

    /**
     * <hr>
     * Clears all tasks from the database.
     *
     * <p>Used primarily for testing cleanup.
     */
    @Override
    public void clearAllTasks() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM session_tasks");
        } catch (SQLException e) {
            System.err.println("Error clearing tasks: " + e.getMessage());
        }
    }

    /**
     * <hr>
     * Gets the total number of tasks in the database.
     *
     * @return the count of tasks
     */
    @Override
    public int getTaskCount() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM session_tasks")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }
}