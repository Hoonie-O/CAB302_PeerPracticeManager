package com.cab302.peerpractice.Model.Entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Base class for all message types (e.g., friend-to-friend or group messages).
 * Provides common fields like ID, sender, content, and timestamp.
 */
public abstract class Message {
    private final String messageId;
    private final String senderId;
    private final String content;
    private final LocalDateTime timestamp;

    public Message(String messageId, String senderId, String content, LocalDateTime timestamp) {
        this.messageId = Objects.requireNonNull(messageId, "Message ID cannot be null");
        this.senderId = Objects.requireNonNull(senderId, "Sender ID cannot be null");
        this.content = Objects.requireNonNull(content, "Content cannot be null");
        this.timestamp = Objects.requireNonNull(timestamp, "Timestamp cannot be null");
    }

    public String getMessageId() {
        return messageId;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Message{" +
                "messageId='" + messageId + '\'' +
                ", senderId='" + senderId + '\'' +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
