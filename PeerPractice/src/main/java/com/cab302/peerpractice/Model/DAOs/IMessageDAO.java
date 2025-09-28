package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Message;
import java.util.List;

/**
 * Generic DAO interface for messages.
 */
public interface IMessageDAO<T extends Message> {
    boolean addMessage(T message);
    boolean deleteMessage(String messageId);
    T getMessageById(String messageId);
    List<T> getAllMessages();
}
