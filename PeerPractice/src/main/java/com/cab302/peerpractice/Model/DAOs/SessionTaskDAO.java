package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.SessionTask;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database storage implementation of ISessionTaskDAO for persistent task storage.
 */
public class SessionTaskDAO implements ISessionTaskDAO {
    private final Connection connection;

    public SessionTaskDAO(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTables();
    }

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

    @Override
    public void clearAllTasks() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM session_tasks");
        } catch (SQLException e) {
            System.err.println("Error clearing tasks: " + e.getMessage());
        }
    }

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
