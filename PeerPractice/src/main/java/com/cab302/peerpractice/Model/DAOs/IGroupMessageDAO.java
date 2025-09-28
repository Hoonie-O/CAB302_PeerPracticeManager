package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.GroupMessage;
import java.util.List;

/**
 * DAO for managing group messages.
 */
public interface IGroupMessageDAO extends IMessageDAO<GroupMessage> {
    List<GroupMessage> getMessagesForGroup(int groupId);
    boolean deleteMessagesForGroup(int groupId);
}