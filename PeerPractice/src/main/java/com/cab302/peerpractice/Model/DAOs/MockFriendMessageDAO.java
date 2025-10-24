package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.FriendMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <hr>
 * Mock in-memory implementation of friend message Data Access Object for unit testing.
 *
 * <p>This class provides an in-memory implementation of IFriendMessageDAO
 * suitable for unit testing without requiring a physical database connection.
 * It simulates friend message operations using hash maps and stream processing
 * for realistic testing scenarios.
 *
 * <p> Key features include:
 * <ul>
 *   <li>In-memory message storage using hash maps</li>
 *   <li>Stream-based filtering for conversation simulation</li>
 *   <li>Chronological message ordering</li>
 *   <li>Identical interface to production DAO</li>
 * </ul>
 *
 * @see IFriendMessageDAO
 * @see FriendMessage
 */
public class MockFriendMessageDAO implements IFriendMessageDAO {

    /** <hr> In-memory storage for friend message entities using message ID as key. */
    private final Map<String, FriendMessage> messages = new HashMap<>();

    /**
     * <hr>
     * Adds a new friend message to in-memory storage.
     *
     * <p>Persists a friend message entity to the mock storage using the
     * message's unique identifier as the key, simulating database insert
     * operations for testing scenarios.
     *
     * @param message the FriendMessage object to be stored
     * @return true if the message was successfully added, false otherwise
     */
    @Override
    public boolean addMessage(FriendMessage message) {
        if (message == null) return false;
        messages.put(message.getMessageId(), message);
        return true;
    }

    /**
     * <hr>
     * Deletes a specific friend message from in-memory storage.
     *
     * <p>Removes a single message from the mock storage using its unique
     * message ID, simulating database delete operations for testing.
     *
     * @param messageId the unique identifier of the message to delete
     * @return true if the message was successfully deleted, false otherwise
     */
    @Override
    public boolean deleteMessage(String messageId) {
        return messages.remove(messageId) != null;
    }

    /**
     * <hr>
     * Retrieves a specific friend message by its unique identifier.
     *
     * <p>Fetches a single message from the mock storage using its unique
     * message ID, simulating database retrieval by primary key for testing.
     *
     * @param messageId the unique identifier of the message to retrieve
     * @return the FriendMessage object if found, null otherwise
     */
    @Override
    public FriendMessage getMessageById(String messageId) {
        return messages.get(messageId);
    }

    /**
     * <hr>
     * Retrieves all friend messages from in-memory storage.
     *
     * <p>Fetches every friend message entity stored in the mock storage,
     * providing comprehensive access to all test data for verification.
     *
     * @return a list of all FriendMessage objects in mock storage
     */
    @Override
    public List<FriendMessage> getAllMessages() {
        return new ArrayList<>(messages.values());
    }

    /**
     * <hr>
     * Retrieves all messages exchanged between two specific users.
     *
     * <p>Fetches the complete conversation history between user1 and user2
     * from mock storage using stream filtering, simulating bidirectional
     * conversation query operations for testing.
     *
     * @param user1Id the username of the first user in the conversation
     * @param user2Id the username of the second user in the conversation
     * @return a list of FriendMessage objects representing the conversation,
     *         ordered by timestamp in ascending order
     */
    @Override
    public List<FriendMessage> getMessagesBetween(String user1Id, String user2Id) {
        return messages.values().stream()
                .filter(m ->
                        (m.getSenderId().equals(user1Id) && m.getReceiverId().equals(user2Id)) ||
                                (m.getSenderId().equals(user2Id) && m.getReceiverId().equals(user1Id))
                )
                .sorted(Comparator.comparing(FriendMessage::getTimestamp))
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Deletes all messages exchanged between two specific users.
     *
     * <p>Removes the entire conversation history between user1 and user2
     * from mock storage using stream filtering and batch deletion,
     * simulating conversation clearance operations for testing.
     *
     * @param user1Id the username of the first user in the conversation
     * @param user2Id the username of the second user in the conversation
     * @return true if any messages were deleted, false otherwise
     */
    @Override
    public boolean deleteMessagesBetween(String user1Id, String user2Id) {
        List<String> toRemove = messages.values().stream()
                .filter(m ->
                        (m.getSenderId().equals(user1Id) && m.getReceiverId().equals(user2Id)) ||
                                (m.getSenderId().equals(user2Id) && m.getReceiverId().equals(user1Id))
                )
                .map(FriendMessage::getMessageId)
                .toList();

        toRemove.forEach(messages::remove);
        return !toRemove.isEmpty();
    }

    /**
     * <hr>
     * Retrieves all messages associated with a specific user.
     *
     * <p>Fetches all messages where the specified user is either the sender
     * or receiver from mock storage, simulating user-centric message
     * query operations for testing.
     *
     * @param userId the username of the user to fetch messages for
     * @return a list of all FriendMessage objects involving the user,
     *         ordered by timestamp in ascending order
     */
    @Override
    public List<FriendMessage> getMessagesForUser(String userId) {
        return messages.values().stream()
                .filter(m -> m.getSenderId().equals(userId) || m.getReceiverId().equals(userId))
                .sorted(Comparator.comparing(FriendMessage::getTimestamp))
                .collect(Collectors.toList());
    }
}