package com.cab302.peerpractice.Model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a direct message sent between two users (friends).
 */
public class FriendMessage extends Message {
    private final String recipientId;

    public FriendMessage(String messageId,
                         String senderId,
                         String content,
                         LocalDateTime timestamp,
                         String recipientId) {
        super(
                Objects.requireNonNull(messageId, "Message ID cannot be null"),
                Objects.requireNonNull(senderId, "Sender ID cannot be null"),
                Objects.requireNonNull(content, "Content cannot be null"),
                Objects.requireNonNull(timestamp, "Timestamp cannot be null")
        );
        this.recipientId = recipientId;
    }

    public String getReceiverId() {
        return recipientId;
    }
}
