package com.cab302.peerpractice.Model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Database storage implementation for SessionTask with full CRUD operations
 */
public class SessionTaskDBStorage extends SessionTaskStorage {
    private final Connection connection;
    private final IUserDAO userDao;

    public SessionTaskDBStorage(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        this.userDao = userDao;
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

            // Migrate existing tables to new schema if needed
            migrateSessionTasks(st);
        }
    }

    private void migrateSessionTasks(Statement st) throws SQLException {
        try {
            // Check if old schema exists (missing session column or using created_by_id)
            ResultSet rs = st.executeQuery("PRAGMA table_info(session_tasks)");
            boolean hasSessionColumn = false;
            boolean hasCreatedByIdColumn = false;
            boolean hasUpdatedAtColumn = false;

            while (rs.next()) {
                String columnName = rs.getString("name");
                if ("session".equals(columnName)) {
                    hasSessionColumn = true;
                } else if ("created_by_id".equals(columnName)) {
                    hasCreatedByIdColumn = true;
                } else if ("updated_at".equals(columnName)) {
                    hasUpdatedAtColumn = true;
                }
            }
            rs.close();

            // Add session column if missing
            if (!hasSessionColumn) {
                st.execute("ALTER TABLE session_tasks ADD COLUMN session TEXT DEFAULT ''");
                // Populate session column with session titles
                st.execute("UPDATE session_tasks SET session = (SELECT title FROM sessions WHERE sessions.session_id = session_tasks.session_id)");
                // Make session column NOT NULL after populating
                // SQLite doesn't support ALTER COLUMN, so we'll handle it in the application
            }

            // Rename created_by_id to created_by if needed
            if (hasCreatedByIdColumn) {
                st.execute("ALTER TABLE session_tasks RENAME COLUMN created_by_id TO created_by");
            }

            // Rename updated_at to updated if needed
            if (hasUpdatedAtColumn) {
                st.execute("ALTER TABLE session_tasks RENAME COLUMN updated_at TO updated");
            }

        } catch (SQLException e) {
            // Table might not exist yet, which is fine for new installations
            System.out.println("[INFO] Session tasks table migration: " + e.getMessage());
        }
    }

    private SessionTask mapRowToTask(ResultSet rs) throws SQLException {
        String taskId = rs.getString("task_id");
        String sessionId = rs.getString("session_id");
        String title = rs.getString("title");
        LocalDateTime deadline = LocalDateTime.parse(rs.getString("deadline"));
        String assigneeId = rs.getString("assignee_id");

        // Handle both old and new column names for backward compatibility
        String createdById;
        try {
            createdById = rs.getString("created_by");
        } catch (SQLException e) {
            createdById = rs.getString("created_by_id"); // fallback to old name
        }

        boolean completed = rs.getInt("completed") == 1;
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));

        // Handle both old and new column names for backward compatibility
        LocalDateTime updatedAt;
        try {
            updatedAt = LocalDateTime.parse(rs.getString("updated"));
        } catch (SQLException e) {
            updatedAt = LocalDateTime.parse(rs.getString("updated_at")); // fallback to old name
        }

        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdById);

        // Use reflection to set private fields
        try {
            var taskIdField = SessionTask.class.getDeclaredField("taskId");
            taskIdField.setAccessible(true);
            taskIdField.set(task, taskId);

            var completedField = SessionTask.class.getDeclaredField("completed");
            completedField.setAccessible(true);
            completedField.set(task, completed);

            var createdAtField = SessionTask.class.getDeclaredField("createdAt");
            createdAtField.setAccessible(true);
            createdAtField.set(task, createdAt);

            // Try both old and new field names for backward compatibility
            try {
                var updatedField = SessionTask.class.getDeclaredField("updated");
                updatedField.setAccessible(true);
                updatedField.set(task, updatedAt);
            } catch (NoSuchFieldException e) {
                var updatedAtField = SessionTask.class.getDeclaredField("updatedAt");
                updatedAtField.setAccessible(true);
                updatedAtField.set(task, updatedAt);
            }
        } catch (Exception ignored) {}

        return task;
    }

    @Override
    public void addTask(SessionTask task) {
        if (task == null) return;

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO session_tasks (task_id, session_id, session, title, deadline, assignee_id, created_by, completed, created_at, updated) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {

            // Get session title from sessions table
            String sessionTitle = "";
            try (PreparedStatement sessionPs = connection.prepareStatement("SELECT title FROM sessions WHERE session_id = ?")) {
                sessionPs.setString(1, task.getSessionId());
                ResultSet rs = sessionPs.executeQuery();
                if (rs.next()) {
                    sessionTitle = rs.getString("title");
                }
                rs.close();
            }

            ps.setString(1, task.getTaskId());
            ps.setString(2, task.getSessionId());
            ps.setString(3, sessionTitle); // session title
            ps.setString(4, task.getTitle());
            ps.setString(5, task.getDeadline().toString());
            ps.setString(6, task.getAssigneeId());
            ps.setString(7, task.getCreatedBy());
            ps.setInt(8, task.isCompleted() ? 1 : 0);
            ps.setString(9, task.getCreatedAt().toString());
            ps.setString(10, LocalDateTime.now().toString()); // current time as updated
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error adding task: " + e.getMessage());
        }
    }

    @Override
    public List<SessionTask> getTasksForSession(String sessionId) {
        if (sessionId == null) return new ArrayList<>();

        List<SessionTask> tasks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM session_tasks WHERE session_id = ?")) {
            ps.setString(1, sessionId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting tasks for session: " + e.getMessage());
        }
        return tasks;
    }

    @Override
    public SessionTask getTaskById(String taskId) {
        if (taskId == null) return null;

        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM session_tasks WHERE task_id = ?")) {
            ps.setString(1, taskId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRowToTask(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting task by id: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean updateTask(SessionTask updatedTask) {
        if (updatedTask == null || updatedTask.getTaskId() == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE session_tasks SET title = ?, deadline = ?, assignee_id = ?, completed = ?, updated = ? WHERE task_id = ?")) {
            ps.setString(1, updatedTask.getTitle());
            ps.setString(2, updatedTask.getDeadline().toString());
            ps.setString(3, updatedTask.getAssigneeId());
            ps.setInt(4, updatedTask.isCompleted() ? 1 : 0);
            ps.setString(5, LocalDateTime.now().toString()); // auto-update timestamp
            ps.setString(6, updatedTask.getTaskId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating task: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeTask(String taskId) {
        if (taskId == null) return false;

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM session_tasks WHERE task_id = ?")) {
            ps.setString(1, taskId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing task: " + e.getMessage());
            return false;
        }
    }
    
    @Override
    public boolean removeAllTasksForSession(String sessionId) {
        if (sessionId == null) return false;

        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM session_tasks WHERE session_id = ?")) {
            ps.setString(1, sessionId);
            int rowsAffected = ps.executeUpdate();
            System.out.println("[DEBUG] Deleted " + rowsAffected + " tasks for session " + sessionId);
            return rowsAffected >= 0;
        } catch (SQLException e) {
            System.err.println("Error removing tasks for session: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<SessionTask> getTasksForUser(String assigneeId) {
        if (assigneeId == null) return new ArrayList<>();

        List<SessionTask> tasks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM session_tasks WHERE assignee_id = ?")) {
            ps.setString(1, assigneeId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
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
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting overdue tasks: " + e.getMessage());
        }
        return tasks;
    }

    @Override
    public List<SessionTask> getCompletedTasks() {
        List<SessionTask> tasks = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM session_tasks WHERE completed = 1")) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tasks.add(mapRowToTask(rs));
                }
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
            while (rs.next()) {
                tasks.add(mapRowToTask(rs));
            }
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
            System.err.println("Error clearing all tasks: " + e.getMessage());
        }
    }

    @Override
    public int getTaskCount() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM session_tasks")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting task count: " + e.getMessage());
        }
        return 0;
    }
}