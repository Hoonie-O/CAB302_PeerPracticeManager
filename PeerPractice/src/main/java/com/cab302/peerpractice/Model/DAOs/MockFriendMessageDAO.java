package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.FriendMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock in-memory implementation of IFriendMessageDAO.
 * Useful for testing without a real database.
 */
public class MockFriendMessageDAO implements IFriendMessageDAO {

    private final Map<String, FriendMessage> messages = new HashMap<>();

    @Override
    public boolean addMessage(FriendMessage message) {
        if (message == null) return false;
        messages.put(message.getMessageId(), message);
        return true;
    }

    @Override
    public boolean deleteMessage(String messageId) {
        return messages.remove(messageId) != null;
    }

    @Override
    public FriendMessage getMessageById(String messageId) {
        return messages.get(messageId);
    }

    @Override
    public List<FriendMessage> getAllMessages() {
        return new ArrayList<>(messages.values());
    }

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

    @Override
    public List<FriendMessage> getMessagesForUser(String userId) {
        return messages.values().stream()
                .filter(m -> m.getSenderId().equals(userId) || m.getReceiverId().equals(userId))
                .sorted(Comparator.comparing(FriendMessage::getTimestamp))
                .collect(Collectors.toList());
    }
}
