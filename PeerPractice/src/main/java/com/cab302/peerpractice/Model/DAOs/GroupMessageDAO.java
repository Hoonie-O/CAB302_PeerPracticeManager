package com.cab302.peerpractice.Model.DAOs;
import com.cab302.peerpractice.Model.Entities.GroupMessage;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <hr>
 * SQLite implementation of group message Data Access Object.
 *
 * <p>This class provides concrete SQLite database operations for group message
 * management, handling persistence and retrieval of messages sent to groups.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Group message storage and retrieval</li>
 *   <li>Message management by group ID</li>
 *   <li>Automatic table creation on initialization</li>
 *   <li>Chronological message ordering</li>
 * </ul>
 *
 * @see GroupMessage
 * @see IGroupMessageDAO
 * @see SQLiteConnection
 */
public class GroupMessageDAO implements IGroupMessageDAO {

    /** <hr> Database connection instance for SQLite operations. */
    private final Connection connection;

    /**
     * <hr>
     * Constructs a new GroupMessageDAO with database connection.
     *
     * <p>Initializes the SQLite connection and ensures the required
     * database table exists by calling createTable().
     *
     * @throws SQLException if database connection or table creation fails
     */
    public GroupMessageDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

    /**
     * <hr>
     * Creates the group_messages table if it doesn't exist.
     *
     * <p>Defines the database schema for storing group messages with
     * appropriate foreign key constraints and indexes.
     *
     * @throws SQLException if table creation fails
     */
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

    /**
     * <hr>
     * Maps a database ResultSet row to a GroupMessage object.
     *
     * <p>Converts SQL result set data into a structured GroupMessage entity
     * with proper type conversions for timestamp fields.
     *
     * @param rs the ResultSet containing database row data
     * @return a populated GroupMessage object
     * @throws SQLException if data extraction fails
     */
    private GroupMessage mapRow(ResultSet rs) throws SQLException {
        return new GroupMessage(
                rs.getString("message_id"),
                rs.getString("sender_id"),
                rs.getString("content"),
                LocalDateTime.parse(rs.getString("timestamp")),
                rs.getInt("group_id")
        );
    }

    /**
     * <hr>
     * Adds a new group message to the database.
     *
     * <p>Persists a group message entity to the SQLite database with
     * all required message attributes including sender, group, and timestamp.
     *
     * @param message the GroupMessage object to be stored
     * @return true if the message was successfully added, false otherwise
     */
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

    /**
     * <hr>
     * Deletes a specific group message by its unique identifier.
     *
     * <p>Removes a single group message from the database using its
     * unique message ID, ensuring precise message deletion.
     *
     * @param messageId the unique identifier of the message to delete
     * @return true if the message was successfully deleted, false otherwise
     */
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

    /**
     * <hr>
     * Retrieves a specific group message by its unique identifier.
     *
     * <p>Fetches a single group message from the database using its
     * unique message ID, returning the complete message entity.
     *
     * @param messageId the unique identifier of the message to retrieve
     * @return the GroupMessage object if found, null otherwise
     */
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

    /**
     * <hr>
     * Retrieves all group messages from the database.
     *
     * <p>Fetches every group message stored in the database, ordered
     * chronologically by timestamp for consistent historical viewing.
     *
     * @return a list of all GroupMessage objects in chronological order
     */
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

    /**
     * <hr>
     * Retrieves all messages for a specific group.
     *
     * <p>Fetches the complete message history for a particular group,
     * ordered chronologically to provide a coherent conversation view.
     *
     * @param groupId the unique identifier of the group
     * @return a list of GroupMessage objects for the specified group
     */
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

    /**
     * <hr>
     * Deletes all messages for a specific group.
     *
     * <p>Removes the entire message history for a particular group,
     * typically used when a group is deleted or needs message clearance.
     *
     * @param groupId the unique identifier of the group
     * @return true if the operation completed successfully, false otherwise
     */
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