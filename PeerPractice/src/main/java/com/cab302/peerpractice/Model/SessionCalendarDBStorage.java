package com.cab302.peerpractice.Model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionCalendarDBStorage extends SessionCalendarStorage {
    private final Connection connection;
    private final IUserDAO userDao;

    public SessionCalendarDBStorage(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        this.userDao = userDao;
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS sessions (" +
                    "session_id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT DEFAULT '', " +
                    "start_time TEXT NOT NULL, " +
                    "end_time TEXT NOT NULL, " +
                    "organiser_user_id TEXT NOT NULL, " +
                    "priority TEXT DEFAULT 'optional', " +
                    "location TEXT DEFAULT 'TBD', " +
                    "color_label TEXT DEFAULT 'BLUE', " +
                    "subject TEXT DEFAULT '', " +
                    "max_participants INTEGER DEFAULT 10, " +
                    "group_id INTEGER" +
                    ")");

            // Migration: Check if old 'status' column exists and migrate to 'priority'
            migrateSessions(st);

            st.execute("CREATE TABLE IF NOT EXISTS session_participants (" +
                    "session_id TEXT NOT NULL, " +
                    "user_id TEXT NOT NULL, " +
                    "PRIMARY KEY(session_id, user_id)" +
                    ")");
        }
    }

    private void migrateSessions(Statement st) throws SQLException {
        try {
            // Check if the sessions table has a 'status' column
            ResultSet tableInfo = st.executeQuery("PRAGMA table_info(sessions)");
            boolean hasStatusColumn = false;
            boolean hasPriorityColumn = false;

            while (tableInfo.next()) {
                String columnName = tableInfo.getString("name");
                if ("status".equals(columnName)) {
                    hasStatusColumn = true;
                } else if ("priority".equals(columnName)) {
                    hasPriorityColumn = true;
                }
            }
            tableInfo.close();

            // If we have status but not priority, we need to migrate
            if (hasStatusColumn && !hasPriorityColumn) {

                // Add the priority column with default value
                st.execute("ALTER TABLE sessions ADD COLUMN priority TEXT DEFAULT 'optional'");

                // Map old status values to new priority values
                st.execute("UPDATE sessions SET priority = " +
                          "CASE " +
                          "WHEN status = 'PLANNED' THEN 'optional' " +
                          "WHEN status = 'ACTIVE' THEN 'important' " +
                          "WHEN status = 'COMPLETED' THEN 'optional' " +
                          "WHEN status = 'CANCELLED' THEN 'optional' " +
                          "ELSE 'optional' END");

            } else if (hasStatusColumn && hasPriorityColumn) {
                // Both columns exist, we can drop the old status column after ensuring data is migrated
            } else if (!hasStatusColumn && !hasPriorityColumn) {
                // This is a fresh table, add priority column
                st.execute("ALTER TABLE sessions ADD COLUMN priority TEXT DEFAULT 'optional'");
            }
        } catch (SQLException e) {
            System.err.println("[DEBUG] Migration error (this may be normal for fresh installs): " + e.getMessage());
            // Don't throw the exception - the table creation will handle fresh installations
        }
    }

    private Session mapRowToSession(ResultSet rs) throws SQLException {
        String id = rs.getString("session_id");
        String title = rs.getString("title");
        LocalDateTime start = LocalDateTime.parse(rs.getString("start_time"));
        LocalDateTime end = LocalDateTime.parse(rs.getString("end_time"));
        String organiserId = rs.getString("organiser_user_id");
        User organiser;
        try {
            organiser = userDao.findUserById(organiserId);
        } catch (SQLException e) {
            organiser = null;
        }
        if (organiser == null) {
            organiser = new User(organiserId, "Unknown", "User", "unknown_user", "unknown@example.com", "", "");
        }
        Session s = new Session(title, organiser, start, end);

        try {
            var field = Session.class.getDeclaredField("sessionId");
            field.setAccessible(true);
            field.set(s, id);
        } catch (Exception ignored) {}

        s.setDescription(rs.getString("description"));
        s.setColorLabel(rs.getString("color_label"));
        s.setLocation(rs.getString("location"));
        s.setSubject(rs.getString("subject"));
        s.setPriority(rs.getString("priority"));
        s.setMaxParticipants(rs.getInt("max_participants"));

        int groupId = rs.getInt("group_id");
        if (!rs.wasNull()) {
            Group g = new Group("Unknown", "", false, "unknown", LocalDateTime.now());
            g.setID(groupId);
            s.setGroup(g);
        }

        List<User> participants = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT user_id FROM session_participants WHERE session_id = ?")) {
            ps.setString(1, id);
            try (ResultSet prs = ps.executeQuery()) {
                while (prs.next()) {
                    String uid = prs.getString("user_id");
                    try {
                        User u = userDao.findUserById(uid);
                        if (u != null) participants.add(u);
                    } catch (SQLException ignored) {}
                }
            }
        }
        for (User u : participants) s.addParticipant(u);
        return s;
    }

    @Override
    public boolean addSession(Session session) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO sessions (session_id, title, description, start_time, end_time, organiser_user_id, priority, location, color_label, subject, max_participants, group_id) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, session.getSessionId());
            ps.setString(2, session.getTitle());
            ps.setString(3, session.getDescription());
            ps.setString(4, session.getStartTime().toString());
            ps.setString(5, session.getEndTime().toString());
            ps.setString(6, session.getOrganiser() != null ? session.getOrganiser().getUserId() : null);
            ps.setString(7, session.getPriority());
            ps.setString(8, session.getLocation());
            ps.setString(9, session.getColorLabel());
            ps.setString(10, session.getSubject());
            ps.setInt(11, session.getMaxParticipants());
            ps.setObject(12, session.getGroup() != null ? session.getGroup().getID() : null);
            int rows = ps.executeUpdate();
            
            // Save participants to session_participants table
            try (PreparedStatement participantPs = connection.prepareStatement("DELETE FROM session_participants WHERE session_id = ?")) {
                participantPs.setString(1, session.getSessionId());
                participantPs.executeUpdate();
            }
            
            for (User participant : session.getParticipants()) {
                try (PreparedStatement participantPs = connection.prepareStatement("INSERT INTO session_participants (session_id, user_id) VALUES (?, ?)")) {
                    participantPs.setString(1, session.getSessionId());
                    participantPs.setString(2, participant.getUserId());
                    participantPs.executeUpdate();
                } catch (SQLException participantError) {
                    System.err.println("[DEBUG] Failed to add participant " + participant.getUsername() + ": " + participantError.getMessage());
                }
            }
            
            return true;
        } catch (SQLException e) {
            System.err.println("[DEBUG] DBStorage.addSession FAILED: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeSession(Session session) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM sessions WHERE session_id = ?")) {
            ps.setString(1, session.getSessionId());
            ps.executeUpdate();
            try (PreparedStatement del = connection.prepareStatement("DELETE FROM session_participants WHERE session_id = ?")) {
                del.setString(1, session.getSessionId());
                del.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public List<Session> getAllSessions() {
        List<Session> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM sessions")) {
            while (rs.next()) list.add(mapRowToSession(rs));
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Session> getSessionsForDate(LocalDate date) {
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM sessions WHERE substr(start_time,1,10)=?")) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Session> getSessionsForUser(User user) {
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT s.* FROM sessions s JOIN session_participants p ON s.session_id=p.session_id WHERE p.user_id=?")) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Session> getSessionsForWeek(LocalDate startOfWeek) {
        LocalDate end = startOfWeek.plusDays(6);
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM sessions WHERE date(start_time) BETWEEN ? AND ?")) {
            ps.setString(1, startOfWeek.toString());
            ps.setString(2, end.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public List<Session> getSessionsForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM sessions WHERE date(start_time) BETWEEN ? AND ?")) {
            ps.setString(1, startDate.toString());
            ps.setString(2, endDate.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    @Override
    public boolean updateSession(Session oldSession, Session newSession) {
        return addSession(newSession);
    }

    @Override
    public void clearAllSessions() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM session_participants");
            st.executeUpdate("DELETE FROM sessions");
        } catch (SQLException ignored) {}
    }

    @Override
    public int getSessionCount() {
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sessions")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    @Override
    public boolean hasSessionsOnDate(LocalDate date) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM sessions WHERE substr(start_time,1,10)=? LIMIT 1")) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public List<Session> getSessionsForGroup(Group group) {
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM sessions WHERE group_id=?")) {
            ps.setInt(1, group.getID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    public List<Session> getSessionsForDateAndGroup(LocalDate date, Group group) {
        List<Session> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM sessions WHERE substr(start_time,1,10)=? AND group_id=?")) {
            ps.setString(1, date.toString());
            ps.setInt(2, group.getID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }
}