package com.cab302.peerpractice.Model.entities;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a message sent within a group.
 * Extends the base {@link Message} with a group ID field.
 */
public class GroupMessage extends Message {
    private final int groupId;

    public GroupMessage(String messageId, String senderId, String content, LocalDateTime timestamp, int groupId) {
        super(messageId, senderId, content, timestamp);
        this.groupId = groupId;
    }

    public int getGroupId() {
        return groupId;
    }

    @Override
    public String toString() {
        return "GroupMessage{" +
                "messageId='" + getMessageId() + '\'' +
                ", senderId='" + getSenderId() + '\'' +
                ", content='" + getContent() + '\'' +
                ", timestamp=" + getTimestamp() +
                ", groupId=" + groupId +
                '}';
    }
}
