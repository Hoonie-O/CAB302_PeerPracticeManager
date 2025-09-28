package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.FriendMessage;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of IFriendMessageDAO.
 */
public class FriendMessageDAO implements IFriendMessageDAO {

    private final Connection connection;

    public FriendMessageDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS friend_messages (" +
                    "message_id TEXT PRIMARY KEY, " +
                    "sender_id TEXT NOT NULL, " +
                    "receiver_id TEXT NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "FOREIGN KEY(sender_id) REFERENCES users(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(receiver_id) REFERENCES users(username) ON DELETE CASCADE" +
                    ")");
        }
    }

    private FriendMessage mapRow(ResultSet rs) throws SQLException {
        return new FriendMessage(
                rs.getString("message_id"),
                rs.getString("sender_id"),
                rs.getString("content"),
                LocalDateTime.parse(rs.getString("timestamp")),
                rs.getString("receiver_id")
        );
    }

    @Override
    public boolean addMessage(FriendMessage message) {
        String sql = "INSERT INTO friend_messages (message_id, sender_id, receiver_id, content, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, message.getMessageId());
            ps.setString(2, message.getSenderId());
            ps.setString(3, message.getReceiverId());
            ps.setString(4, message.getContent());
            ps.setString(5, message.getTimestamp().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding friend message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMessage(String messageId) {
        String sql = "DELETE FROM friend_messages WHERE message_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting friend message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public FriendMessage getMessageById(String messageId) {
        String sql = "SELECT * FROM friend_messages WHERE message_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching friend message: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<FriendMessage> getAllMessages() {
        List<FriendMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM friend_messages ORDER BY timestamp ASC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching all friend messages: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<FriendMessage> getMessagesBetween(String user1Id, String user2Id) {
        List<FriendMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM friend_messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) " +
                "   OR (sender_id = ? AND receiver_id = ?) " +
                "ORDER BY timestamp ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user1Id);
            ps.setString(2, user2Id);
            ps.setString(3, user2Id);
            ps.setString(4, user1Id);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages between users: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteMessagesBetween(String user1Id, String user2Id) {
        String sql = "DELETE FROM friend_messages " +
                "WHERE (sender_id = ? AND receiver_id = ?) " +
                "   OR (sender_id = ? AND receiver_id = ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user1Id);
            ps.setString(2, user2Id);
            ps.setString(3, user2Id);
            ps.setString(4, user1Id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting messages between users: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<FriendMessage> getMessagesForUser(String userId) {
        List<FriendMessage> list = new ArrayList<>();
        if (userId == null || userId.isBlank()) return list;

        String sql = "SELECT * FROM friend_messages " +
                "WHERE sender_id = ? OR receiver_id = ? " +
                "ORDER BY timestamp ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, userId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages for user: " + e.getMessage());
        }
        return list;
    }
}
