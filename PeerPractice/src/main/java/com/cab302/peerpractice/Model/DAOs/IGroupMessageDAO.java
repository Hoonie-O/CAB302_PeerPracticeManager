package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.GroupMessage;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing group message operations.
 *
 * <p>This interface defines the contract for group message data operations,
 * providing specialized methods for handling messages within group contexts
 * beyond the generic message operations.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Group-specific message retrieval and management</li>
 *   <li>Bulk message operations for group contexts</li>
 *   <li>Group message history clearing</li>
 *   <li>Integration with generic message operations</li>
 * </ul>
 *
 * @see GroupMessage
 * @see IMessageDAO
 * @see GroupMessageDAO
 */
public interface IGroupMessageDAO extends IMessageDAO<GroupMessage> {
    /**
     * <hr>
     * Retrieves all messages for a specific group.
     *
     * <p>Fetches the complete message history for a particular group,
     * providing access to the group's conversation thread and
     * collaborative discussion history.
     *
     * @param groupId the unique identifier of the group
     * @return a list of GroupMessage objects for the specified group
     */
    List<GroupMessage> getMessagesForGroup(int groupId);

    /**
     * <hr>
     * Deletes all messages for a specific group.
     *
     * <p>Removes the entire message history for a particular group,
     * typically used when a group is deleted or requires complete
     * conversation history clearance.
     *
     * @param groupId the unique identifier of the group
     * @return true if the operation completed successfully, false otherwise
     */
    boolean deleteMessagesForGroup(int groupId);
}