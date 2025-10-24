package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.FriendMessage;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <hr>
 * SQLite implementation of friend message Data Access Object.
 *
 * <p>This class provides concrete SQLite database operations for friend message
 * management, handling persistence and retrieval of private messages between
 * individual users in the peer practice system.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Private message storage and retrieval between friends</li>
 *   <li>Conversation history management between user pairs</li>
 *   <li>Message deletion and conversation clearing</li>
 *   <li>User-specific message querying</li>
 * </ul>
 *
 * @see FriendMessage
 * @see IFriendMessageDAO
 * @see SQLiteConnection
 */
public class FriendMessageDAO implements IFriendMessageDAO {

    /** <hr> Database connection instance for SQLite operations. */
    private final Connection connection;

    /**
     * <hr>
     * Constructs a new FriendMessageDAO with database connection.
     *
     * <p>Initializes the SQLite connection and ensures the required
     * database table exists by calling createTable() during construction.
     *
     * @throws SQLException if database connection or table creation fails
     */
    public FriendMessageDAO() throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        createTable();
    }

    /**
     * <hr>
     * Creates the friend_messages table if it doesn't exist.
     *
     * <p>Defines the database schema for storing private messages between
     * users with appropriate foreign key constraints and indexes.
     *
     * @throws SQLException if table creation fails
     */
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

    /**
     * <hr>
     * Maps a database ResultSet row to a FriendMessage object.
     *
     * <p>Converts SQL result set data into a structured FriendMessage entity
     * with proper type conversions for timestamp fields and message attributes.
     *
     * @param rs the ResultSet containing database row data
     * @return a populated FriendMessage object
     * @throws SQLException if data extraction fails
     */
    private FriendMessage mapRow(ResultSet rs) throws SQLException {
        return new FriendMessage(
                rs.getString("message_id"),
                rs.getString("sender_id"),
                rs.getString("content"),
                LocalDateTime.parse(rs.getString("timestamp")),
                rs.getString("receiver_id")
        );
    }

    /**
     * <hr>
     * Adds a new friend message to the database.
     *
     * <p>Persists a private message entity to the SQLite database with
     * all required message attributes including sender, receiver, content,
     * and timestamp for conversation tracking.
     *
     * @param message the FriendMessage object to be stored
     * @return true if the message was successfully added, false otherwise
     */
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

    /**
     * <hr>
     * Deletes a specific friend message by its unique identifier.
     *
     * <p>Removes a single private message from the database using its
     * unique message ID, allowing for precise message deletion in
     * conversation management.
     *
     * @param messageId the unique identifier of the message to delete
     * @return true if the message was successfully deleted, false otherwise
     */
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

    /**
     * <hr>
     * Retrieves a specific friend message by its unique identifier.
     *
     * <p>Fetches a single private message from the database using its
     * unique message ID, returning the complete message entity with
     * all conversation details.
     *
     * @param messageId the unique identifier of the message to retrieve
     * @return the FriendMessage object if found, null otherwise
     */
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

    /**
     * <hr>
     * Retrieves all friend messages from the database.
     *
     * <p>Fetches every private message stored in the database across all
     * user conversations, ordered chronologically by timestamp for
     * comprehensive system-wide message viewing.
     *
     * @return a list of all FriendMessage objects in chronological order
     */
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

    /**
     * <hr>
     * Retrieves all messages exchanged between two specific users.
     *
     * <p>Fetches the complete conversation history between user1 and user2,
     * including messages sent in both directions, ordered chronologically
     * to provide a coherent conversation view for the chat interface.
     *
     * @param user1Id the username of the first user in the conversation
     * @param user2Id the username of the second user in the conversation
     * @return a list of FriendMessage objects representing the conversation,
     *         ordered by timestamp in ascending order
     */
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

    /**
     * <hr>
     * Deletes all messages exchanged between two specific users.
     *
     * <p>Removes the entire conversation history between user1 and user2
     * from persistent storage, including messages sent in both directions.
     * This operation effectively clears the chat history between the users.
     *
     * @param user1Id the username of the first user in the conversation
     * @param user2Id the username of the second user in the conversation
     * @return true if the operation completed successfully, false otherwise
     */
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

    /**
     * <hr>
     * Retrieves all messages associated with a specific user.
     *
     * <p>Fetches all messages where the specified user is either the sender
     * or receiver, providing a complete view of the user's message activity
     * across all conversations for inbox-style message management.
     *
     * @param userId the username of the user to fetch messages for
     * @return a list of all FriendMessage objects involving the user,
     *         ordered by timestamp in ascending order
     */
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