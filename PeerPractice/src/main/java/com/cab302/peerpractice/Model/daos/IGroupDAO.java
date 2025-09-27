package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.Group;
import com.cab302.peerpractice.Model.entities.GroupJoinRequest;
import com.cab302.peerpractice.Model.entities.GroupMemberEntity;
import com.cab302.peerpractice.Model.entities.User;

import java.util.List;

public interface IGroupDAO {

    // === Group Lifecycle ===
    int addGroup(Group group); // returns group ID, or -1 / exception if failed
    boolean updateGroup(Group group);
    boolean deleteGroup(Group group);
    boolean deleteGroup(int groupId);

    // -------------------- HELPERS / CHECKS --------------------
    boolean existsByName(String name);

    boolean existstByUser(User user);

    boolean groupExists(Group group);

    // === Group Search ===
    Group searchByID(int id);
    List<Group> searchByName(String name);
    List<Group> searchByUser(User user);
    List<Group> searchByMembers(List<User> users);
    List<Group> getAllGroups();

    // === Group Membership ===
    boolean addToGroup(int groupId, User user);
    boolean removeMember(int groupId, String targetUserId, String actingUserId);
    boolean isUserMemberOfGroup(int groupId, String userId);
    List<GroupMemberEntity> getGroupMembers(int groupId);
    String getUserRoleInGroup(int groupId, String userId); // NEW

    // === Admin / Roles ===
    boolean isAdmin(int groupId, String userId);
    boolean promoteToAdmin(int groupId, String targetUserId, String actingUserId);

    // === Join Requests ===
    boolean setRequireApproval(int groupId, boolean requireApproval);
    void createJoinRequest(int groupId, String userId);
    boolean hasUserRequestedToJoin(int groupId, String userId);
    List<GroupJoinRequest> getPendingJoinRequests(int groupId);
    boolean processJoinRequest(int requestId, String status, String approverUserId); // NEW
}
