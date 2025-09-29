package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupJoinRequest;
import com.cab302.peerpractice.Model.Entities.GroupMemberEntity;
import com.cab302.peerpractice.Model.Entities.User;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Mock (in-memory) implementation of IGroupDAO for unit testing without a database.
 */
public class MockGroupDAO implements IGroupDAO {

    private final Map<Integer, Group> groups = new ConcurrentHashMap<>();
    private final Map<Integer, List<GroupMemberEntity>> groupMembers = new ConcurrentHashMap<>();
    private final Map<Integer, GroupJoinRequest> joinRequests = new ConcurrentHashMap<>();
    private final AtomicInteger groupIdGen = new AtomicInteger(1);
    private final AtomicInteger requestIdGen = new AtomicInteger(1);
    private final IUserDAO userDao;

    public MockGroupDAO(IUserDAO userDao) {
        this.userDao = userDao;
    }

    // -------------------- CREATE --------------------
    @Override
    public int addGroup(Group group) throws SQLException {
        if (groupExists(group)) return -1;

        int groupId = groupIdGen.getAndIncrement();
        group.setID(groupId);
        groups.put(groupId, group);

        // Add owner as admin if exists
        User owner = userDao.getUserByUsername(group.getOwner().getUsername()).orElse(null);
        if (owner != null) {
            addMemberWithRole(groupId, owner.getUserId(), "admin");
        }
        return groupId;
    }

    public boolean addMemberWithRole(int groupId, String userId, String role) throws SQLException {
        GroupMemberEntity member = new GroupMemberEntity();
        member.setGroupId(groupId);
        member.setUserId(userId);
        member.setRole(role);
        member.setJoinedAt(LocalDateTime.now());
        member.setUser(userDao.findUserById(userId));

        groupMembers.computeIfAbsent(groupId, k -> new ArrayList<>()).add(member);
        return true;
    }

    // -------------------- READ --------------------
    @Override
    public Group searchByID(int id) {
        return groups.get(id);
    }

    @Override
    public List<Group> searchByUser(User user) {
        if (user == null) return Collections.emptyList();
        return groupMembers.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(m -> m.getUserId().equals(user.getUserId())))
                .map(e -> groups.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> searchByMembers(List<User> users) {
        if (users == null || users.isEmpty()) return Collections.emptyList();
        Set<String> userIds = users.stream().map(User::getUserId).collect(Collectors.toSet());
        return groupMembers.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(m -> userIds.contains(m.getUserId())))
                .map(e -> groups.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> searchByName(String name) {
        return groups.values().stream()
                .filter(g -> g.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Group> getAllGroups() {
        return new ArrayList<>(groups.values());
    }

    @Override
    public List<GroupMemberEntity> getGroupMembers(int groupId) {
        return new ArrayList<>(groupMembers.getOrDefault(groupId, Collections.emptyList()));
    }

    @Override
    public String getUserRoleInGroup(int groupId, String userId) {
        return groupMembers.getOrDefault(groupId, Collections.emptyList())
                .stream()
                .filter(m -> m.getUserId().equals(userId))
                .map(GroupMemberEntity::getRole)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<GroupJoinRequest> getPendingJoinRequests(int groupId) {
        return joinRequests.values().stream()
                .filter(r -> r.getGroupId() == groupId && r.getStatus().equals("pending"))
                .collect(Collectors.toList());
    }

    @Override
    public boolean processJoinRequest(int requestId, String status, String approverUserId) throws SQLException {
        GroupJoinRequest req = joinRequests.get(requestId);
        if (req == null || !"pending".equals(req.getStatus())) return false;
        if (!status.equals("approved") && !status.equals("rejected")) return false;

        req.setStatus(status);
        req.setProcessedAt(LocalDateTime.now());
        req.setProcessedBy(approverUserId);

        if (status.equals("approved")) {
            User user = userDao.findUserById(req.getUserId());
            return addToGroup(req.getGroupId(), user);
        }
        return true;
    }

    // -------------------- UPDATE --------------------
    @Override
    public boolean updateGroup(Group group) {
        if (!groups.containsKey(group.getID())) return false;
        groups.put(group.getID(), group);
        return true;
    }

    @Override
    public boolean setRequireApproval(int groupId, boolean requireApproval) {
        Group group = groups.get(groupId);
        if (group == null) return false;
        group.setRequire_approval(requireApproval);
        return true;
    }

    @Override
    public boolean addToGroup(int groupId, User user) throws SQLException {
        if (user == null) return false;
        return addMemberWithRole(groupId, user.getUserId(), "member");
    }

    // -------------------- DELETE --------------------
    @Override
    public boolean deleteGroup(Group group) {
        return deleteGroup(group.getID());
    }

    @Override
    public boolean deleteGroup(int groupId) {
        groups.remove(groupId);
        groupMembers.remove(groupId);
        joinRequests.entrySet().removeIf(e -> e.getValue().getGroupId() == groupId);
        return true;
    }

    @Override
    public boolean removeMember(int groupId, String userIdToRemove, String adminUserId) {
        if (!isAdmin(groupId, adminUserId)) return false;
        return groupMembers.getOrDefault(groupId, new ArrayList<>())
                .removeIf(m -> m.getUserId().equals(userIdToRemove));
    }

    // -------------------- HELPERS --------------------
    @Override
    public boolean existsByName(String name) {
        return groups.values().stream().anyMatch(g -> g.getName().equalsIgnoreCase(name));
    }

    @Override
    public boolean existstByUser(User user) {
        if (user == null) return false;
        return groupMembers.values().stream()
                .anyMatch(list -> list.stream().anyMatch(m -> m.getUserId().equals(user.getUserId())));
    }

    @Override
    public boolean groupExists(Group group) {
        return existsByName(group.getName());
    }

    @Override
    public boolean isAdmin(int groupId, String userId) {
        return groupMembers.getOrDefault(groupId, Collections.emptyList())
                .stream()
                .anyMatch(m -> m.getUserId().equals(userId) && "admin".equals(m.getRole()));
    }

    @Override
    public boolean promoteToAdmin(int groupId, String userId, String promoterUserId) {
        if (!isAdmin(groupId, promoterUserId)) return false;
        for (GroupMemberEntity m : groupMembers.getOrDefault(groupId, Collections.emptyList())) {
            if (m.getUserId().equals(userId)) {
                m.setRole("admin");
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean hasUserRequestedToJoin(int groupId, String userId) {
        return joinRequests.values().stream()
                .anyMatch(r -> r.getGroupId() == groupId && r.getUserId().equals(userId) && r.getStatus().equals("pending"));
    }

    @Override
    public boolean isUserMemberOfGroup(int groupId, String userId) {
        return groupMembers.getOrDefault(groupId, Collections.emptyList())
                .stream()
                .anyMatch(m -> m.getUserId().equals(userId));
    }

    @Override
    public void createJoinRequest(int groupId, String userId) throws SQLException {
        int reqId = requestIdGen.getAndIncrement();
        GroupJoinRequest req = new GroupJoinRequest(
                reqId,
                groupId,
                userId,
                "pending",
                LocalDateTime.now(),
                null,
                null
        );
        req.setUser(userDao.findUserById(userId));
        joinRequests.put(reqId, req);
    }

    /**
     * Clear all data from the mock DAO for testing purposes
     */
    public void clear() {
        groups.clear();
        groupMembers.clear();
        joinRequests.clear();
        groupIdGen.set(1);
        requestIdGen.set(1);
    }
}
