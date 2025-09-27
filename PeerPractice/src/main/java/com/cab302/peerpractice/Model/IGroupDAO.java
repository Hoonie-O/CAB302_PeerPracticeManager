package com.cab302.peerpractice.Model;

import java.util.List;

public interface IGroupDAO {
    //Should return the group ID, or throw exception if failed (or -1 change GroupManager :) )
    int addGroup(Group group);

    boolean deleteGroup(Group group);
    boolean updateGroup(Group group);
    List<Group>searchByUser(User user);
    List<Group>searchByMembers(List<User> users);
    List<Group>searchByName(String name);
    Group searchByID(int id);
    boolean existsByName(String name);
    boolean existstByUser(User user);
    List<Group>getAllGroups();
    boolean setRequireApproval(int id,boolean require_approval);
    boolean addToGroup(int id, User user);
    boolean groupExists(Group group);
    boolean hasUserRequestedToJoin(int id, String userId);
    boolean isUserMemberOfGroup(int id, String userId);
    void createJoinRequest(int id, String userId);
    boolean isAdmin(int id, String userId);
    boolean promoteToAdmin(int id, String userId, String userId1);
    boolean removeMember(int id, String userId, String userId1);
    List<GroupMemberEntity> getGroupMembers(int id);
    List<GroupJoinRequest> getPendingJoinRequests(int id);
}
