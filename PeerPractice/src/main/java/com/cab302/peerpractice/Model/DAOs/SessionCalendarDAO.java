package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <hr>
 * DB implementation of ISessionCalendarDAO
 *
 * <p>This implementation provides SQLite-based persistent storage for study sessions
 * with comprehensive calendar functionality and participant management.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Session storage with organizer and participant tracking</li>
 *   <li>Flexible querying by date, user, group, and date ranges</li>
 *   <li>Support for session priorities, locations, and color coding</li>
 *   <li>Automatic table creation with proper relationships</li>
 *   <li>Group-based session organization</li>
 * </ul>
 *
 * @see ISessionCalendarDAO
 * @see Session
 * @see User
 * @see Group
 */
public class SessionCalendarDAO implements ISessionCalendarDAO {
    /** <hr> SQLite database connection instance. */
    private final Connection connection;
    /** <hr> User DAO for user lookup and validation. */
    private final IUserDAO userDao;

    /**
     * <hr>
     * Constructs a new SessionCalendarDAO with the specified user DAO.
     *
     * @param userDao the user DAO for user lookup operations
     * @throws SQLException if database connection or table creation fails
     */
    public SessionCalendarDAO(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        this.userDao = userDao;
        createTables();
    }

    // -------------------- TABLE CREATION --------------------

    /**
     * <hr>
     * Creates the necessary database tables if they don't exist.
     *
     * <p>Creates tables for sessions and session participants with appropriate
     * relationships and constraints.
     *
     * @throws SQLException if table creation fails
     */
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

            st.execute("CREATE TABLE IF NOT EXISTS session_participants (" +
                    "session_id TEXT NOT NULL, " +
                    "user_id TEXT NOT NULL, " +
                    "PRIMARY KEY(session_id, user_id))");
        }
    }

    // -------------------- MAPPING --------------------

    /**
     * <hr>
     * Maps a database ResultSet row to a Session object.
     *
     * <p>Populates the session with organizer, participants, and all session properties.
     *
     * @param rs the ResultSet containing session data
     * @return a fully populated Session object
     * @throws SQLException if database access error occurs
     */
    private Session mapRowToSession(ResultSet rs) throws SQLException {
        String id = rs.getString("session_id");
        String title = rs.getString("title");
        LocalDateTime start = LocalDateTime.parse(rs.getString("start_time"));
        LocalDateTime end = LocalDateTime.parse(rs.getString("end_time"));
        String organiserId = rs.getString("organiser_user_id");

        User organiser = null;
        try {
            organiser = userDao.findUserById(organiserId);
        } catch (SQLException ignored) {}
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
            Group g = new Group("Unknown", "", false, null, LocalDateTime.now());
            g.setID(groupId);
            s.setGroup(g);
        }

        // participants
        List<User> participants = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT user_id FROM session_participants WHERE session_id = ?")) {
            ps.setString(1, id);
            try (ResultSet prs = ps.executeQuery()) {
                while (prs.next()) {
                    String uid = prs.getString("user_id");
                    User u = userDao.findUserById(uid);
                    if (u != null) participants.add(u);
                }
            }
        }
        for (User u : participants) s.addParticipant(u);

        return s;
    }

    // -------------------- DAO METHODS --------------------

    /**
     * <hr>
     * Adds a new session to the database.
     *
     * <p>Stores session details and all participants in the database.
     *
     * @param session the session to add
     * @return true if the session was added successfully
     */
    @Override
    public boolean addSession(Session session) {
        if (session == null) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO sessions " +
                        "(session_id, title, description, start_time, end_time, organiser_user_id, priority, location, color_label, subject, max_participants, group_id) " +
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
            ps.executeUpdate();

            // Participants
            try (PreparedStatement del = connection.prepareStatement("DELETE FROM session_participants WHERE session_id=?")) {
                del.setString(1, session.getSessionId());
                del.executeUpdate();
            }
            for (User u : session.getParticipants()) {
                try (PreparedStatement ins = connection.prepareStatement("INSERT INTO session_participants (session_id, user_id) VALUES (?, ?)")) {
                    ins.setString(1, session.getSessionId());
                    ins.setString(2, u.getUserId());
                    ins.executeUpdate();
                }
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * <hr>
     * Removes a session from the database.
     *
     * <p>Deletes both the session and all its participant associations.
     *
     * @param session the session to remove
     * @return true if the session was removed successfully
     */
    @Override
    public boolean removeSession(Session session) {
        if (session == null) {
            return false;
        }
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM sessions WHERE session_id=?")) {
            ps.setString(1, session.getSessionId());
            int rowsAffected = ps.executeUpdate();
            try (PreparedStatement del = connection.prepareStatement("DELETE FROM session_participants WHERE session_id=?")) {
                del.setString(1, session.getSessionId());
                del.executeUpdate();
            }
            return rowsAffected > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * <hr>
     * Retrieves all sessions from the database.
     *
     * @return a list of all sessions
     */
    @Override
    public List<Session> getAllSessions() {
        List<Session> list = new ArrayList<>();
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT * FROM sessions")) {
            while (rs.next()) list.add(mapRowToSession(rs));
        } catch (SQLException ignored) {}
        return list;
    }

    /**
     * <hr>
     * Retrieves all sessions scheduled for a specific date.
     *
     * @param date the date to retrieve sessions for
     * @return a list of sessions occurring on the specified date
     */
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

    /**
     * <hr>
     * Retrieves all sessions that a specific user is participating in.
     *
     * @param user the user to retrieve sessions for
     * @return a list of sessions the user is participating in
     */
    @Override
    public List<Session> getSessionsForUser(User user) {
        List<Session> list = new ArrayList<>();
        if (user == null) {
            return list;
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT s.* FROM sessions s JOIN session_participants p ON s.session_id=p.session_id WHERE p.user_id=?")) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    /**
     * <hr>
     * Retrieves all sessions scheduled for a specific week.
     *
     * @param startOfWeek the starting date of the week
     * @return a list of sessions occurring during the specified week
     */
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

    /**
     * <hr>
     * Retrieves all sessions scheduled within a specific date range.
     *
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return a list of sessions occurring within the specified date range
     */
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

    /**
     * <hr>
     * Updates an existing session with new session data.
     *
     * <p>Preserves the original session ID while updating all other properties.
     *
     * @param oldSession the original session to update
     * @param newSession the new session data
     * @return true if the session was updated successfully
     */
    @Override
    public boolean updateSession(Session oldSession, Session newSession) {
        if (oldSession == null || newSession == null) {
            return false;
        }
        // Set the ID of the new session to match the old one for proper updating
        try {
            var field = Session.class.getDeclaredField("sessionId");
            field.setAccessible(true);
            field.set(newSession, oldSession.getSessionId());
        } catch (Exception e) {
            return false;
        }
        return addSession(newSession);
    }

    /**
     * <hr>
     * Clears all sessions from the database.
     *
     * <p>Removes both sessions and participant associations.
     */
    @Override
    public void clearAllSessions() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM session_participants");
            st.executeUpdate("DELETE FROM sessions");
        } catch (SQLException ignored) {}
    }

    /**
     * <hr>
     * Gets the total number of sessions in the database.
     *
     * @return the count of sessions
     */
    @Override
    public int getSessionCount() {
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM sessions")) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) {
            return 0;
        }
    }

    /**
     * <hr>
     * Checks if any sessions exist on a specific date.
     *
     * @param date the date to check
     * @return true if at least one session exists on the specified date
     */
    @Override
    public boolean hasSessionsOnDate(LocalDate date) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM sessions WHERE substr(start_time,1,10)=? LIMIT 1")) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) { return false; }
    }

    /**
     * <hr>
     * Retrieves all sessions associated with a specific group.
     *
     * @param group the group to retrieve sessions for
     * @return a list of sessions associated with the specified group
     */
    @Override
    public List<Session> getSessionsForGroup(Group group) {
        List<Session> list = new ArrayList<>();
        if (group == null) {
            return list;
        }
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM sessions WHERE group_id=?")) {
            ps.setInt(1, group.getID());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToSession(rs));
            }
        } catch (SQLException ignored) {}
        return list;
    }

    /**
     * <hr>
     * Retrieves all sessions for a specific group on a specific date.
     *
     * @param date the date to retrieve sessions for
     * @param group the group to retrieve sessions for
     * @return a list of sessions for the specified group and date
     */
    @Override
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