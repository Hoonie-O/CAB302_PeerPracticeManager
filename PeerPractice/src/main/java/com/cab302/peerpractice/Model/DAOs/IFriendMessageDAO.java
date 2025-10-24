package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.FriendMessage;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing friend-to-friend messages.
 *
 * <p>This interface defines the contract for friend message data operations,
 * providing methods to retrieve, manage, and delete private messages
 * between individual users.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Retrieval of message history between two users</li>
 *   <li>Bulk deletion of conversation history</li>
 *   <li>User-specific message queries</li>
 *   <li>Extends generic message operations</li>
 * </ul>
 *
 * @see FriendMessage
 * @see IMessageDAO
 * @see FriendMessageDAO
 */
public interface IFriendMessageDAO extends IMessageDAO<FriendMessage> {
    /**
     * <hr>
     * Retrieves all messages exchanged between two specific users.
     *
     * <p>Fetches the complete conversation history between user1 and user2,
     * including messages sent in both directions, ordered chronologically.
     *
     * @param user1Id the username of the first user in the conversation
     * @param user2Id the username of the second user in the conversation
     * @return a list of FriendMessage objects representing the conversation,
     *         ordered by timestamp in ascending order
     */
    List<FriendMessage> getMessagesBetween(String user1Id, String user2Id);

    /**
     * <hr>
     * Deletes all messages exchanged between two specific users.
     *
     * <p>Removes the entire conversation history between user1 and user2
     * from persistent storage, including messages sent in both directions.
     *
     * @param user1Id the username of the first user in the conversation
     * @param user2Id the username of the second user in the conversation
     * @return true if the operation completed successfully, false otherwise
     */
    boolean deleteMessagesBetween(String user1Id, String user2Id);

    /**
     * <hr>
     * Retrieves all messages associated with a specific user.
     *
     * <p>Fetches all messages where the specified user is either the sender
     * or receiver, providing a complete view of the user's message activity.
     *
     * @param userId the username of the user to fetch messages for
     * @return a list of all FriendMessage objects involving the user,
     *         ordered by timestamp in ascending order
     */
    List<FriendMessage> getMessagesForUser(String userId);
}