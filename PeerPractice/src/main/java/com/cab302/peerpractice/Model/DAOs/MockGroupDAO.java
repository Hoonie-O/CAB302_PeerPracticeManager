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
 * <hr>
 * Mock (in-memory) implementation of IGroupDAO for unit testing without a database.
 *
 * <p>This implementation provides in-memory storage for groups, group members,
 * and join requests to facilitate testing without requiring a real database connection.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Thread-safe concurrent data structures</li>
 *   <li>Automatic ID generation for groups and requests</li>
 *   <li>Support for all group management operations</li>
 *   <li>Integration with user DAO for member validation</li>
 * </ul>
 *
 * @see IGroupDAO
 * @see Group
 * @see GroupMemberEntity
 */
public class MockGroupDAO implements IGroupDAO {

    /** <hr> In-memory storage for groups by ID. */
    private final Map<Integer, Group> groups = new ConcurrentHashMap<>();
    /** <hr> In-memory storage for group members by group ID. */
    private final Map<Integer, List<GroupMemberEntity>> groupMembers = new ConcurrentHashMap<>();
    /** <hr> In-memory storage for join requests by request ID. */
    private final Map<Integer, GroupJoinRequest> joinRequests = new ConcurrentHashMap<>();
    /** <hr> Atomic counter for generating unique group IDs. */
    private final AtomicInteger groupIdGen = new AtomicInteger(1);
    /** <hr> Atomic counter for generating unique request IDs. */
    private final AtomicInteger requestIdGen = new AtomicInteger(1);
    /** <hr> User DAO for user validation and retrieval. */
    private final IUserDAO userDao;

    /**
     * <hr>
     * Constructs a new MockGroupDAO with the specified user DAO.
     *
     * @param userDao the user DAO for user validation and retrieval
     */
    public MockGroupDAO(IUserDAO userDao) {
        this.userDao = userDao;
    }

    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Adds a new group to the in-memory storage.
     *
     * <p>Generates a unique ID for the group and adds the owner as an admin member.
     * Returns -1 if a group with the same name already exists.
     *
     * @param group the group to add
     * @return the generated group ID, or -1 if group already exists
     * @throws SQLException if there's an error accessing user data
     */
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

    /**
     * <hr>
     * Adds a member with a specific role to a group.
     *
     * @param groupId the ID of the group to add the member to
     * @param userId the ID of the user to add as a member
     * @param role the role to assign to the member
     * @return true if the member was added successfully
     * @throws SQLException if there's an error accessing user data
     */
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

    /**
     * <hr>
     * Searches for a group by its ID.
     *
     * @param id the group ID to search for
     * @return the group with the specified ID, or null if not found
     */
    @Override
    public Group searchByID(int id) {
        return groups.get(id);
    }

    /**
     * <hr>
     * Searches for groups that a specific user belongs to.
     *
     * @param user the user to search groups for
     * @return a list of groups the user belongs to
     */
    @Override
    public List<Group> searchByUser(User user) {
        if (user == null) return Collections.emptyList();
        return groupMembers.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(m -> m.getUserId().equals(user.getUserId())))
                .map(e -> groups.get(e.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Searches for groups that contain any of the specified users.
     *
     * @param users the list of users to search for
     * @return a list of groups containing any of the specified users
     */
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

    /**
     * <hr>
     * Searches for groups by name (case-insensitive partial match).
     *
     * @param name the name to search for
     * @return a list of groups whose names contain the search string
     */
    @Override
    public List<Group> searchByName(String name) {
        return groups.values().stream()
                .filter(g -> g.getName().toLowerCase().contains(name.toLowerCase()))
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Retrieves all groups from the in-memory storage.
     *
     * @return a list of all groups
     */
    @Override
    public List<Group> getAllGroups() {
        return new ArrayList<>(groups.values());
    }

    /**
     * <hr>
     * Retrieves all members of a specific group.
     *
     * @param groupId the ID of the group
     * @return a list of group members
     */
    @Override
    public List<GroupMemberEntity> getGroupMembers(int groupId) {
        return new ArrayList<>(groupMembers.getOrDefault(groupId, Collections.emptyList()));
    }

    /**
     * <hr>
     * Gets the role of a specific user in a group.
     *
     * @param groupId the ID of the group
     * @param userId the ID of the user
     * @return the user's role, or null if user is not a member
     */
    @Override
    public String getUserRoleInGroup(int groupId, String userId) {
        return groupMembers.getOrDefault(groupId, Collections.emptyList())
                .stream()
                .filter(m -> m.getUserId().equals(userId))
                .map(GroupMemberEntity::getRole)
                .findFirst()
                .orElse(null);
    }

    /**
     * <hr>
     * Retrieves all pending join requests for a group.
     *
     * @param groupId the ID of the group
     * @return a list of pending join requests
     */
    @Override
    public List<GroupJoinRequest> getPendingJoinRequests(int groupId) {
        return joinRequests.values().stream()
                .filter(r -> r.getGroupId() == groupId && r.getStatus().equals("pending"))
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Processes a join request by approving or rejecting it.
     *
     * <p>If approved, adds the user to the group. If rejected, only updates the request status.
     *
     * @param requestId the ID of the join request to process
     * @param status the new status ("approved" or "rejected")
     * @param approverUserId the ID of the user processing the request
     * @return true if the request was processed successfully
     * @throws SQLException if there's an error adding the user to the group
     */
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

    /**
     * <hr>
     * Updates an existing group's information.
     *
     * @param group the group with updated information
     * @return true if the group was updated successfully
     */
    @Override
    public boolean updateGroup(Group group) {
        if (!groups.containsKey(group.getID())) return false;
        groups.put(group.getID(), group);
        return true;
    }

    /**
     * <hr>
     * Sets whether a group requires approval for new members.
     *
     * @param groupId the ID of the group
     * @param requireApproval true if approval is required, false otherwise
     * @return true if the setting was updated successfully
     */
    @Override
    public boolean setRequireApproval(int groupId, boolean requireApproval) {
        Group group = groups.get(groupId);
        if (group == null) return false;
        group.setRequire_approval(requireApproval);
        return true;
    }

    /**
     * <hr>
     * Adds a user to a group as a regular member.
     *
     * @param groupId the ID of the group
     * @param user the user to add to the group
     * @return true if the user was added successfully
     * @throws SQLException if there's an error accessing user data
     */
    @Override
    public boolean addToGroup(int groupId, User user) throws SQLException {
        if (user == null) return false;
        return addMemberWithRole(groupId, user.getUserId(), "member");
    }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Deletes a group from the in-memory storage.
     *
     * @param group the group to delete
     * @return true if the group was deleted successfully
     */
    @Override
    public boolean deleteGroup(Group group) {
        return deleteGroup(group.getID());
    }

    /**
     * <hr>
     * Deletes a group by ID from the in-memory storage.
     *
     * <p>Also removes all group members and join requests associated with the group.
     *
     * @param groupId the ID of the group to delete
     * @return true if the group was deleted successfully
     */
    @Override
    public boolean deleteGroup(int groupId) {
        groups.remove(groupId);
        groupMembers.remove(groupId);
        joinRequests.entrySet().removeIf(e -> e.getValue().getGroupId() == groupId);
        return true;
    }

    /**
     * <hr>
     * Removes a member from a group.
     *
     * <p>Only group admins can remove members.
     *
     * @param groupId the ID of the group
     * @param userIdToRemove the ID of the user to remove
     * @param adminUserId the ID of the admin performing the removal
     * @return true if the member was removed successfully
     */
    @Override
    public boolean removeMember(int groupId, String userIdToRemove, String adminUserId) {
        if (!isAdmin(groupId, adminUserId)) return false;
        return groupMembers.getOrDefault(groupId, new ArrayList<>())
                .removeIf(m -> m.getUserId().equals(userIdToRemove));
    }

    // -------------------- HELPERS --------------------

    /**
     * <hr>
     * Checks if a group with the specified name exists.
     *
     * @param name the group name to check
     * @return true if a group with the name exists
     */
    @Override
    public boolean existsByName(String name) {
        return groups.values().stream().anyMatch(g -> g.getName().equalsIgnoreCase(name));
    }

    /**
     * <hr>
     * Checks if a user belongs to any group.
     *
     * @param user the user to check
     * @return true if the user belongs to at least one group
     */
    @Override
    public boolean existstByUser(User user) {
        if (user == null) return false;
        return groupMembers.values().stream()
                .anyMatch(list -> list.stream().anyMatch(m -> m.getUserId().equals(user.getUserId())));
    }

    /**
     * <hr>
     * Checks if a group already exists (by name).
     *
     * @param group the group to check
     * @return true if a group with the same name exists
     */
    @Override
    public boolean groupExists(Group group) {
        return existsByName(group.getName());
    }

    /**
     * <hr>
     * Checks if a user is an admin of a specific group.
     *
     * @param groupId the ID of the group
     * @param userId the ID of the user to check
     * @return true if the user is an admin of the group
     */
    @Override
    public boolean isAdmin(int groupId, String userId) {
        return groupMembers.getOrDefault(groupId, Collections.emptyList())
                .stream()
                .anyMatch(m -> m.getUserId().equals(userId) && "admin".equals(m.getRole()));
    }

    /**
     * <hr>
     * Promotes a group member to admin role.
     *
     * <p>Only existing admins can promote other members.
     *
     * @param groupId the ID of the group
     * @param userId the ID of the user to promote
     * @param promoterUserId the ID of the admin performing the promotion
     * @return true if the user was promoted successfully
     */
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

    /**
     * <hr>
     * Demotes an admin to regular member role.
     *
     * <p>Only admins can demote other admins.
     *
     * @param groupId the ID of the group
     * @param userId the ID of the admin to demote
     * @param demoterUserId the ID of the admin performing the demotion
     * @return true if the user was demoted successfully
     */
    @Override
    public boolean demoteAdmin(int groupId, String userId, String demoterUserId) {
        if (!isAdmin(groupId, demoterUserId)) return false;
        for (GroupMemberEntity m : groupMembers.getOrDefault(groupId, Collections.emptyList())) {
            if (m.getUserId().equals(userId)) {
                m.setRole("member");
                return true;
            }
        }
        return false;
    }

    /**
     * <hr>
     * Checks if a user has a pending join request for a group.
     *
     * @param groupId the ID of the group
     * @param userId the ID of the user to check
     * @return true if the user has a pending join request
     */
    @Override
    public boolean hasUserRequestedToJoin(int groupId, String userId) {
        return joinRequests.values().stream()
                .anyMatch(r -> r.getGroupId() == groupId && r.getUserId().equals(userId) && r.getStatus().equals("pending"));
    }

    /**
     * <hr>
     * Checks if a user is a member of a specific group.
     *
     * @param groupId the ID of the group
     * @param userId the ID of the user to check
     * @return true if the user is a member of the group
     */
    @Override
    public boolean isUserMemberOfGroup(int groupId, String userId) {
        return groupMembers.getOrDefault(groupId, Collections.emptyList())
                .stream()
                .anyMatch(m -> m.getUserId().equals(userId));
    }

    /**
     * <hr>
     * Creates a new join request for a user to join a group.
     *
     * @param groupId the ID of the group to join
     * @param userId the ID of the user requesting to join
     * @throws SQLException if there's an error accessing user data
     */
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
     * <hr>
     * Clears all data from the mock DAO for testing purposes.
     *
     * <p>Resets all storage maps and ID generators to their initial state.
     */
    public void clear() {
        groups.clear();
        groupMembers.clear();
        joinRequests.clear();
        groupIdGen.set(1);
        requestIdGen.set(1);
    }
}