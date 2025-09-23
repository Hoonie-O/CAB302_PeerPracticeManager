package com.cab302.peerpractice.Model;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDBStorage extends AvailabilityStorage {
    private final Connection connection;
    private final IUserDAO userDao;

    public AvailabilityDBStorage(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        this.userDao = userDao;
        createTables();
    }

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

    private Availability mapRowToAvailability(ResultSet rs) throws SQLException {
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

        Availability availability = new Availability(title, user, start, end, color);
        availability.setDescription(description);
        return availability;
    }

    @Override
    public boolean addAvailability(Availability availability) {
        System.out.println("[DEBUG] Storage adding availability: "
                + availability.getTitle()
                + " user=" + availability.getUser().getUsername()
                + " start=" + availability.getStartTime());
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR REPLACE INTO availabilities " +
                        "(availability_id, title, description, start_time, end_time, user_id, color_label) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)")) {
            ps.setString(1, availability.toString());
            ps.setString(2, availability.getTitle());
            ps.setString(3, availability.getDescription());
            ps.setString(4, availability.getStartTime().toString());
            ps.setString(5, availability.getEndTime().toString());
            ps.setString(6, availability.getUser().getUserId());
            ps.setString(7, availability.getColorLabel());
            ps.executeUpdate();
            System.out.println("[DEBUG] Storage SUCCESS inserting " + availability.getTitle());
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("[DEBUG] Storage ERROR inserting: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean removeAvailability(Availability availability) {
        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM availabilities WHERE title = ? AND start_time = ? AND user_id = ?")) {
            ps.setString(1, availability.getTitle());
            ps.setString(2, availability.getStartTime().toString());
            ps.setString(3, availability.getUser().getUserId());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
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

    @Override
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
}