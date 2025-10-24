package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <hr>
 * SQLite implementation of availability Data Access Object.
 *
 * <p>This class provides concrete SQLite database operations for managing
 * user availability schedules, handling persistence and retrieval of
 * time slots when users are available for peer practice sessions.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Availability slot creation, modification, and deletion</li>
 *   <li>Date-based and user-based availability queries</li>
 *   <li>Weekly availability planning and coordination</li>
 *   <li>Multi-user availability matching for session scheduling</li>
 * </ul>
 *
 * @see Availability
 * @see IAvailabilityDAO
 * @see User
 * @see SQLiteConnection
 */
@SuppressWarnings("ALL")
public class AvailabilityDAO implements IAvailabilityDAO {
    /** <hr> Database connection instance for SQLite operations. */
    private final Connection connection;
    /** <hr> User DAO for user entity resolution and operations. */
    private final IUserDAO userDao;

    /**
     * <hr>
     * Constructs a new AvailabilityDAO with database connection and user DAO dependency.
     *
     * <p>Initializes the SQLite connection and ensures all required database
     * tables exist by calling createTables() during construction.
     *
     * @param userDao the User DAO for user entity operations
     * @throws SQLException if database connection or table creation fails
     */
    public AvailabilityDAO(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        this.userDao = userDao;
        createTables();
    }

    /**
     * <hr>
     * Creates the availabilities table if it doesn't exist.
     *
     * <p>Defines the database schema for storing user availability slots with
     * appropriate fields for time ranges, descriptions, and color coding.
     *
     * @throws SQLException if table creation fails
     */
    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS availabilities (" +
                    "availability_id TEXT PRIMARY KEY, " +
                    "title TEXT NOT NULL, " +
                    "description TEXT DEFAULT '', " +
                    "start_time TEXT NOT NULL, " +
                    "end_time TEXT NOT NULL, " +
                    "user_id TEXT NOT NULL, " +
                    "color_label TEXT DEFAULT 'GREEN'" +
                    ")");
        }
    }

    /**
     * <hr>
     * Maps a database ResultSet row to a fully populated Availability object.
     *
     * <p>Converts SQL result set data into a complete Availability entity,
     * including resolving the associated User object and handling proper
     * type conversions for date-time fields.
     *
     * @param rs the ResultSet containing availability data from the database
     * @return a fully populated Availability object with user reference
     * @throws SQLException if data extraction or user resolution fails
     */
    private Availability mapRowToAvailability(ResultSet rs) throws SQLException {
        String id = rs.getString("availability_id");
        String title = rs.getString("title");
        String description = rs.getString("description");
        LocalDateTime start = LocalDateTime.parse(rs.getString("start_time"));
        LocalDateTime end = LocalDateTime.parse(rs.getString("end_time"));
        String color = rs.getString("color_label");
        String userId = rs.getString("user_id");

        User user = userDao.findUserById(userId);
        if (user == null) {
            user = new User(userId, "Unknown", "User", "unknown", "unknown@example.com", "", "");
        }

        Availability availability = new Availability(id, title, user, start, end, color);
        availability.setDescription(description);
        return availability;
    }

    /**
     * <hr>
     * Adds a new availability slot to the database.
     *
     * <p>Persists an availability entity to the SQLite database with all
     * required attributes including time range, user association, and
     * visual customization options.
     *
     * @param availability the Availability object to be stored
     * @return true if the availability was successfully added, false otherwise
     */
    public boolean addAvailability(Availability availability) {
        if (availability == null || availability.getUser() == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO availabilities " +
                        "(availability_id, title, description, start_time, end_time, user_id, color_label) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, availability.getAvailabilityId());
            ps.setString(2, availability.getTitle());
            ps.setString(3, availability.getDescription());
            ps.setString(4, availability.getStartTime().toString());
            ps.setString(5, availability.getEndTime().toString());
            ps.setString(6, availability.getUser().getUserId());
            ps.setString(7, availability.getColorLabel());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * <hr>
     * Removes an availability slot from the database.
     *
     * <p>Deletes a specific availability record using a combination of
     * title, start time, and user identifier to ensure precise removal
     * of the intended availability slot.
     *
     * @param availability the Availability object to be removed
     * @return true if the availability was successfully removed, false otherwise
     */
    public boolean removeAvailability(Availability availability) {
        if (availability == null || availability.getUser() == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM availabilities WHERE title = ? AND start_time = ? AND user_id = ?")) {
            ps.setString(1, availability.getTitle());
            ps.setString(2, availability.getStartTime().toString());
            ps.setString(3, availability.getUser().getUserId());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * <hr>
     * Retrieves all availability slots from the database.
     *
     * <p>Fetches every availability record stored in the system across all
     * users, providing a comprehensive view of all scheduled availability
     * for administrative or reporting purposes.
     *
     * @return a list of all Availability objects in the database
     */
    public List<Availability> getAllAvailabilities() {
        List<Availability> list = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM availabilities")) {
            while (rs.next()) list.add(mapRowToAvailability(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves all availability slots for a specific date.
     *
     * <p>Fetches availability records that occur on the specified date,
     * regardless of the specific time or user, useful for daily calendar
     * views and date-specific scheduling.
     *
     * @param date the date to retrieve availabilities for
     * @return a list of Availability objects occurring on the specified date
     */
    public List<Availability> getAvailabilitiesForDate(LocalDate date) {
        List<Availability> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM availabilities WHERE substr(start_time,1,10)=?")) {
            ps.setString(1, date.toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToAvailability(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves all availability slots for a specific user.
     *
     * <p>Fetches the complete availability schedule for a particular user,
     * showing all time slots when they have indicated they are available
     * for peer practice sessions.
     *
     * @param user the user whose availabilities are being retrieved
     * @return a list of Availability objects belonging to the specified user
     */
    public List<Availability> getAvailabilitiesForUser(User user) {
        List<Availability> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM availabilities WHERE user_id = ?")) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToAvailability(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves all availability slots for a specific week.
     *
     * <p>Fetches availability records that occur within the specified week
     * (starting from the provided date and spanning 7 days), useful for
     * weekly calendar views and weekly planning.
     *
     * @param startOfWeek the starting date of the week to retrieve availabilities for
     * @return a list of Availability objects occurring during the specified week
     */
    public List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek) {
        List<Availability> list = new ArrayList<>();
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT * FROM availabilities WHERE start_time >= ? AND start_time <= ?")) {
            ps.setString(1, startOfWeek.atStartOfDay().toString());
            ps.setString(2, endOfWeek.atTime(23, 59, 59).toString());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToAvailability(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves availability slots for multiple users during a specific week.
     *
     * <p>Fetches availability records for the specified list of users that
     * occur within the given week, enabling comparison of multiple users'
     * schedules for coordinated session planning.
     *
     * @param users the list of users to retrieve availabilities for
     * @param startOfWeek the starting date of the week to search
     * @return a list of Availability objects for the specified users during the week
     */
    public List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek) {
        if (users.isEmpty()) return new ArrayList<>();

        List<Availability> list = new ArrayList<>();
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        StringBuilder sql = new StringBuilder("SELECT * FROM availabilities WHERE user_id IN (");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(") AND start_time >= ? AND start_time <= ?");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < users.size(); i++) {
                ps.setString(i + 1, users.get(i).getUserId());
            }
            ps.setString(users.size() + 1, startOfWeek.atStartOfDay().toString());
            ps.setString(users.size() + 2, endOfWeek.atTime(23, 59, 59).toString());

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRowToAvailability(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * <hr>
     * Updates an existing availability slot with new information.
     *
     * <p>Modifies the attributes of an existing availability record while
     * preserving its unique identifier, allowing users to adjust their
     * availability schedules without creating new entries.
     *
     * @param oldAvailability the original Availability object to be updated
     * @param newAvailability the Availability object containing updated information
     * @return true if the availability was successfully updated, false otherwise
     */
    public boolean updateAvailability(Availability oldAvailability, Availability newAvailability) {
        if (oldAvailability == null || newAvailability == null) {
            return false;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE availabilities SET title = ?, description = ?, start_time = ?, end_time = ?, color_label = ? " +
                        "WHERE availability_id = ?")) {
            ps.setString(1, newAvailability.getTitle());
            ps.setString(2, newAvailability.getDescription());
            ps.setString(3, newAvailability.getStartTime().toString());
            ps.setString(4, newAvailability.getEndTime().toString());
            ps.setString(5, newAvailability.getColorLabel());
            ps.setString(6, oldAvailability.getAvailabilityId()); // probably should use ID, not toString()

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * <hr>
     * Removes all availability slots from the database.
     *
     * <p>Clears the entire availability table, typically used for system
     * reset, testing, or administrative maintenance purposes.
     */
    public void clearAllAvailabilities() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM availabilities");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * <hr>
     * Retrieves the total count of availability slots in the database.
     *
     * <p>Provides a quick count of all availability records stored in the
     * system, useful for reporting, statistics, or system monitoring.
     *
     * @return the total number of availability records in the database
     */
    public int getAvailabilityCount() {
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM availabilities")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * <hr>
     * Checks if a user has any availability slots on a specific date.
     *
     * <p>Verifies whether the specified user has scheduled any availability
     * for the given date, useful for quick availability checks and
     * calendar indicator population.
     *
     * @param user the user to check for availability
     * @param date the date to check for availability
     * @return true if the user has availability on the specified date, false otherwise
     */
    public boolean hasAvailabilityOnDate(User user, LocalDate date) {
        if (user == null || date == null) {
            return false; // or throw IllegalArgumentException if that's preferred
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT COUNT(*) FROM availabilities WHERE user_id = ? AND substr(start_time,1,10) = ?")) {
            ps.setString(1, user.getUserId());
            ps.setString(2, date.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}