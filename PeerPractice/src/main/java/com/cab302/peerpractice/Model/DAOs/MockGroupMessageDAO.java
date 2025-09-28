package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.GroupMessage;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mock in-memory implementation of IGroupMessageDAO.
 * Useful for testing without a real database.
 */
public class MockGroupMessageDAO implements IGroupMessageDAO {

    private final Map<String, GroupMessage> messages = new HashMap<>();

    @Override
    public boolean addMessage(GroupMessage message) {
        if (message == null) return false;
        messages.put(message.getMessageId(), message);
        return true;
    }

    @Override
    public boolean deleteMessage(String messageId) {
        return messages.remove(messageId) != null;
    }

    @Override
    public GroupMessage getMessageById(String messageId) {
        return messages.get(messageId);
    }

    @Override
    public List<GroupMessage> getAllMessages() {
        return new ArrayList<>(messages.values());
    }

    @Override
    public List<GroupMessage> getMessagesForGroup(int groupId) {
        return messages.values().stream()
                .filter(m -> m.getGroupId() == groupId)
                .sorted(Comparator.comparing(GroupMessage::getTimestamp))
                .collect(Collectors.toList());
    }

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
