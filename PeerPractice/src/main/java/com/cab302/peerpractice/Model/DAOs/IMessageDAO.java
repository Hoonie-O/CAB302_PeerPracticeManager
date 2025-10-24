package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Message;
import java.util.List;

/**
 * <hr>
 * Generic Data Access Object interface for message management operations.
 *
 * <p>This interface defines the generic contract for message data operations
 * that are common across all message types in the peer practice system,
 * providing a foundation for specialized message DAO implementations.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Basic message CRUD operations</li>
 *   <li>Type-safe message handling through generics</li>
 *   <li>Foundation for specialized message implementations</li>
 *   <li>System-wide message retrieval capabilities</li>
 * </ul>
 *
 * @see Message
 * @see FriendMessage
 * @see GroupMessage
 * @see IFriendMessageDAO
 * @see IGroupMessageDAO
 */
public interface IMessageDAO<T extends Message> {
    /**
     * <hr>
     * Adds a new message to persistent storage.
     *
     * <p>Persists a message entity to the database with all required
     * message attributes and metadata for reliable message delivery
     * and historical tracking.
     *
     * @param message the message object of type T to be stored
     * @return true if the message was successfully added, false otherwise
     */
    boolean addMessage(T message);

    /**
     * <hr>
     * Deletes a specific message by its unique identifier.
     *
     * <p>Removes a single message from the database using its unique
     * message ID, allowing for precise message management and
     * content moderation.
     *
     * @param messageId the unique identifier of the message to delete
     * @return true if the message was successfully deleted, false otherwise
     */
    boolean deleteMessage(String messageId);

    /**
     * <hr>
     * Retrieves a specific message by its unique identifier.
     *
     * <p>Fetches a single message from the database using its unique
     * message ID, returning the complete message entity with all
     * conversation details and metadata.
     *
     * @param messageId the unique identifier of the message to retrieve
     * @return the message object of type T if found, null otherwise
     */
    T getMessageById(String messageId);

    /**
     * <hr>
     * Retrieves all messages from the system.
     *
     * <p>Fetches every message stored in the database across all
     * conversation types and contexts, providing comprehensive
     * system-wide message access for administrative purposes.
     *
     * @return a list of all message objects of type T in the system
     */
    List<T> getAllMessages();
}