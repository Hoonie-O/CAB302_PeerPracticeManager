package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.Entities.Message;

import java.util.List;

/**
 * Base class for message managers (group or friend).
 * Handles shared business logic and enforces a consistent interface.
 */
public abstract class MessageManager<T extends Message> {

    /**
     * Send a message (validates and delegates to DAO).
     */
    public abstract String sendMessage(T message);

    /**
     * Get all messages for a given user or group.
     */
    public abstract List<T> getMessages(String receiverId);

    /**
     * Delete a message by ID.
     */
    public abstract boolean deleteMessage(String messageId);

    /**
     * (Optional) Fetch a single message by ID.
     */
    public abstract T getMessage(String messageId);
}
