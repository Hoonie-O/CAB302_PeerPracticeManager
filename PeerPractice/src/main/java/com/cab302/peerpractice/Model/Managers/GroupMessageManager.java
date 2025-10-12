package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.DAOs.IGroupMessageDAO;
import com.cab302.peerpractice.Model.Entities.GroupMessage;

import java.util.List;
import java.util.Objects;

/**
 * Manager class for handling group messages.
 * Acts as the middle layer between entities and the DAO.
 */
public class GroupMessageManager extends MessageManager<GroupMessage> {

    private final IGroupMessageDAO groupMessageDAO;

    public GroupMessageManager(IGroupMessageDAO groupMessageDAO) {
        this.groupMessageDAO = Objects.requireNonNull(groupMessageDAO, "GroupMessageDAO cannot be null");
    }

    @Override
    public String sendMessage(GroupMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (message.getGroupId() <= 0) {
            throw new IllegalArgumentException("Invalid group ID: " + message.getGroupId());
        }

        groupMessageDAO.addMessage(message);
        return message.getMessageId();
    }

    @Override
    public List<GroupMessage> getMessages(int groupId) {
        try {
            return groupMessageDAO.getMessagesForGroup((groupId));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Group ID must be a valid integer", e);
        }
    }

    @Override
    public List<GroupMessage> getMessages(String receiverId) {
        throw new IllegalArgumentException("Group ID must be a valid integer", new NumberFormatException());
    }

    @Override
    public boolean deleteMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("Message ID cannot be null or blank");
        }
        return groupMessageDAO.deleteMessage(messageId);
    }

    @Override
    public GroupMessage getMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("Message ID cannot be null or blank");
        }
        return groupMessageDAO.getMessageById(messageId);
    }

    /**
     * Deletes all messages belonging to a specific group.
     *
     * @param groupId ID of the group
     * @return true if messages were deleted, false otherwise
     */
    public boolean deleteMessagesForGroup(int groupId) {
        if (groupId <= 0) {
            throw new IllegalArgumentException("Invalid group ID: " + groupId);
        }
        return groupMessageDAO.deleteMessagesForGroup(groupId);
    }
}
