package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.GroupMessage;
import com.cab302.peerpractice.Model.utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of IGroupMessageDAO.
 */
public class GroupMessageDAO implements IGroupMessageDAO {

    private final Connection connection;

    public GroupMessageDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

    private void createTable() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS group_messages (" +
                    "message_id TEXT PRIMARY KEY, " +
                    "sender_id TEXT NOT NULL, " +
                    "group_id INTEGER NOT NULL, " +
                    "content TEXT NOT NULL, " +
                    "timestamp TEXT NOT NULL, " +
                    "FOREIGN KEY(sender_id) REFERENCES users(username) ON DELETE CASCADE, " +
                    "FOREIGN KEY(group_id) REFERENCES groups(group_id) ON DELETE CASCADE" +
                    ")");
        }
    }

    private GroupMessage mapRow(ResultSet rs) throws SQLException {
        return new GroupMessage(
                rs.getString("message_id"),
                rs.getString("sender_id"),
                rs.getString("content"),
                LocalDateTime.parse(rs.getString("timestamp")),
                rs.getInt("group_id")
        );
    }

    @Override
    public boolean addMessage(GroupMessage message) {
        String sql = "INSERT INTO group_messages (message_id, sender_id, group_id, content, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, message.getMessageId());
            ps.setString(2, message.getSenderId());
            ps.setInt(3, message.getGroupId());
            ps.setString(4, message.getContent());
            ps.setString(5, message.getTimestamp().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error adding group message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteMessage(String messageId) {
        String sql = "DELETE FROM group_messages WHERE message_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, messageId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting group message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public GroupMessage getMessageById(String messageId) {
        String sql = "SELECT * FROM group_messages WHERE message_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, messageId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching group message: " + e.getMessage());
        }
        return null;
    }

    @Override
    public List<GroupMessage> getAllMessages() {
        List<GroupMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM group_messages ORDER BY timestamp ASC";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.err.println("Error fetching all group messages: " + e.getMessage());
        }
        return list;
    }

    @Override
    public List<GroupMessage> getMessagesForGroup(int groupId) {
        List<GroupMessage> list = new ArrayList<>();
        String sql = "SELECT * FROM group_messages WHERE group_id = ? ORDER BY timestamp ASC";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching messages for group: " + e.getMessage());
        }
        return list;
    }

    @Override
    public boolean deleteMessagesForGroup(int groupId) {
        String sql = "DELETE FROM group_messages WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting messages for group: " + e.getMessage());
            return false;
        }
    }
}
