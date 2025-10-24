package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.GroupMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <hr>
 * Mock in-memory implementation of IGroupMessageDAO.
 *
 * <p>This implementation provides in-memory storage for group messages
 * to facilitate testing without requiring a real database connection.
 *
 * <p> Key features include:
 * <ul>
 *   <li>In-memory message storage with UUID-based keys</li>
 *   <li>Support for basic CRUD operations on messages</li>
 *   <li>Message retrieval by group with chronological sorting</li>
 *   <li>Bulk deletion of messages for specific groups</li>
 * </ul>
 *
 * @see IGroupMessageDAO
 * @see GroupMessage
 */
public class MockGroupMessageDAO implements IGroupMessageDAO {

    /** <hr> In-memory storage for messages by message ID. */
    private final Map<String, GroupMessage> messages = new HashMap<>();

    /**
     * <hr>
     * Adds a new message to the in-memory storage.
     *
     * @param message the message to add
     * @return true if the message was added successfully
     */
    @Override
    public boolean addMessage(GroupMessage message) {
        if (message == null) return false;
        messages.put(message.getMessageId(), message);
        return true;
    }

    /**
     * <hr>
     * Deletes a message from the in-memory storage.
     *
     * @param messageId the ID of the message to delete
     * @return true if the message was deleted successfully
     */
    @Override
    public boolean deleteMessage(String messageId) {
        return messages.remove(messageId) != null;
    }

    /**
     * <hr>
     * Retrieves a message by its ID.
     *
     * @param messageId the ID of the message to retrieve
     * @return the message with the specified ID, or null if not found
     */
    @Override
    public GroupMessage getMessageById(String messageId) {
        return messages.get(messageId);
    }

    /**
     * <hr>
     * Retrieves all messages from the in-memory storage.
     *
     * @return a list of all messages
     */
    @Override
    public List<GroupMessage> getAllMessages() {
        return new ArrayList<>(messages.values());
    }

    /**
     * <hr>
     * Retrieves all messages for a specific group, sorted by timestamp.
     *
     * @param groupId the ID of the group
     * @return a chronologically sorted list of messages for the group
     */
    @Override
    public List<GroupMessage> getMessagesForGroup(int groupId) {
        return messages.values().stream()
                .filter(m -> m.getGroupId() == groupId)
                .sorted(Comparator.comparing(GroupMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Deletes all messages for a specific group.
     *
     * @param groupId the ID of the group whose messages should be deleted
     * @return true if any messages were deleted
     */
    @Override
    public boolean deleteMessagesForGroup(int groupId) {
        List<String> toRemove = messages.values().stream()
                .filter(m -> m.getGroupId() == groupId)
                .map(GroupMessage::getMessageId)
                .toList();

        toRemove.forEach(messages::remove);
        return !toRemove.isEmpty();
    }
}