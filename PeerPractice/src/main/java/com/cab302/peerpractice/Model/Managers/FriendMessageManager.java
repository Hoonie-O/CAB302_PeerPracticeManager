package com.cab302.peerpractice.Model.managers;

import com.cab302.peerpractice.Model.daos.IFriendMessageDAO;
import com.cab302.peerpractice.Model.daos.MockFriendMessageDAO;
import com.cab302.peerpractice.Model.entities.FriendMessage;

import java.util.List;
import java.util.Objects;

/**
 * Manager layer for handling friend-to-friend messages.
 * Acts as the middle layer between the FriendMessage entity and DAO.
 */
public class FriendMessageManager extends MessageManager<FriendMessage> {

    private final IFriendMessageDAO friendMessageDAO;

    public FriendMessageManager(IFriendMessageDAO friendMessageDAO) {
        this.friendMessageDAO = Objects.requireNonNull(friendMessageDAO, "DAO cannot be null");
    }

    @Override
    public String sendMessage(FriendMessage message) {
        if (message == null) {
            throw new IllegalArgumentException("Message cannot be null");
        }
        if (message.getReceiverId() == null || message.getReceiverId().isBlank()) {
            throw new IllegalArgumentException("Recipient ID cannot be null or empty");
        }

        boolean added = friendMessageDAO.addMessage(message);
        if (!added) {
            throw new IllegalStateException("Failed to send message");
        }

        return message.getMessageId();
    }

    @Override
    public List<FriendMessage> getMessages(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or empty");
        }
        return ((MockFriendMessageDAO) friendMessageDAO).getMessagesForUser(userId);
    }

    @Override
    public boolean deleteMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("Message ID cannot be null or empty");
        }
        return friendMessageDAO.deleteMessage(messageId);
    }

    @Override
    public FriendMessage getMessage(String messageId) {
        if (messageId == null || messageId.isBlank()) {
            throw new IllegalArgumentException("Message ID cannot be null or empty");
        }
        return friendMessageDAO.getMessageById(messageId);
    }
}
