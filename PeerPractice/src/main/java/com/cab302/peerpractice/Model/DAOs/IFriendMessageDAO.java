package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.FriendMessage;
import java.util.List;

/**
 * DAO for managing friend-to-friend messages.
 */
public interface IFriendMessageDAO extends IMessageDAO<FriendMessage> {
    List<FriendMessage> getMessagesBetween(String user1Id, String user2Id);
    boolean deleteMessagesBetween(String user1Id, String user2Id);
    List<FriendMessage> getMessagesForUser(String userId);
}
