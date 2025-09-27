package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.Availability;
import com.cab302.peerpractice.Model.entities.User;
import com.cab302.peerpractice.Model.utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDAO implements IAvailabilityDAO {
    private final Connection connection;
    private final IUserDAO userDao;

    public AvailabilityDAO(IUserDAO userDao) throws SQLException {
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


    public void clearAllAvailabilities() {
        try (Statement st = connection.createStatement()) {
            st.executeUpdate("DELETE FROM availabilities");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
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