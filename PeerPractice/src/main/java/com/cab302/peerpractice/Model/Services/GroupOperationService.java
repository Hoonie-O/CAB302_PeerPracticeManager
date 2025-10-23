package com.cab302.peerpractice.Model.Services;

import com.cab302.peerpractice.Exceptions.DuplicateGroupException;
import com.cab302.peerpractice.Exceptions.InsufficientPermissionsException;
import com.cab302.peerpractice.Exceptions.UserNotFoundException;
import com.cab302.peerpractice.Model.DAOs.IGroupDAO;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupApprovalNotification;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.Notifier;
import com.cab302.peerpractice.Model.Utils.ValidationUtils;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Service responsible for group CRUD and member management operations.
 * Follows the Single Responsibility Principle by focusing exclusively on
 * group lifecycle operations.
 *
 * Responsibilities:
 * - Group creation and deletion
 * - Group settings (approval requirements)
 * - Member addition and removal
 * - Join request processing
 *
 * This service does NOT handle role management (see {@link GroupRoleService})
 * or permission checking (see {@link IAuthorizationService}).
 *
 * @see GroupRoleService
 * @see IAuthorizationService
 */
public class GroupOperationService {

    private final IGroupDAO groupDAO;
    private final IUserDAO userDAO;
    private final Notifier notifier;

    /**
     * Creates a new GroupOperationService.
     *
     * @param groupDAO the group data access object
     * @param userDAO the user data access object
     * @param notifier the notification service
     */
    public GroupOperationService(IGroupDAO groupDAO, IUserDAO userDAO, Notifier notifier) {
        this.groupDAO = Objects.requireNonNull(groupDAO, "GroupDAO cannot be null");
        this.userDAO = Objects.requireNonNull(userDAO, "UserDAO cannot be null");
        this.notifier = Objects.requireNonNull(notifier, "Notifier cannot be null");
    }

    /**
     * Creates a new group with validation.
     *
     * @param name the group name
     * @param description the group description
     * @param requireApproval whether the group requires approval to join
     * @param owner the user creating the group (becomes owner)
     * @throws Exception if validation fails or group already exists
     */
    public void createGroup(String name, String description, boolean requireApproval, User owner) throws Exception {
        Objects.requireNonNull(owner, "Creating user cannot be null");

        // Validate and clean input
        name = ValidationUtils.validateAndCleanOthersName(name);
        description = ValidationUtils.validateAndCleanGroupDescription(description);

        // Create group entity
        Group group = new Group(name, description, requireApproval, owner, LocalDateTime.now());
        group.addMember(owner);

        // Check for duplicates
        if (groupDAO.groupExists(group)) {
            throw new DuplicateGroupException("Group already exists");
        }

        // Persist to database
        int id = groupDAO.addGroup(group);
        group.setID(id);
    }

    /**
     * Sets whether a group requires approval for new members.
     *
     * @param group the group to modify
     * @param requireApproval true if approval should be required
     */
    public void setRequireApproval(Group group, boolean requireApproval) {
        Objects.requireNonNull(group, "Group cannot be null");

        group.setRequire_approval(requireApproval);
        groupDAO.setRequireApproval(group.getID(), requireApproval);
    }

    /**
     * Adds a member to a group directly (owner only).
     *
     * @param group the group to add to
     * @param requestingUser the user making the request (must be owner)
     * @param usernameToAdd the username of the user to add
     * @throws SQLException if database error occurs
     * @throws UserNotFoundException if user to add doesn't exist
     * @throws InsufficientPermissionsException if requesting user is not the owner
     */
    public void addMember(Group group, User requestingUser, String usernameToAdd) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(requestingUser, "User cannot be null");
        ValidationUtils.requireNotBlank(usernameToAdd, "Username to add");

        // Find user to add
        User userToAdd = userDAO.findUser("username", usernameToAdd);
        if (userToAdd == null) {
            throw new UserNotFoundException("User to add couldn't be found");
        }

        // Check permissions (only owner can directly add)
        if (!group.getOwner().getUsername().equals(requestingUser.getUsername())) {
            throw new InsufficientPermissionsException("You are not the owner of the group");
        }

        // Add member
        group.addMember(userToAdd);
        groupDAO.addToGroup(group.getID(), userToAdd);
    }

    /**
     * Processes a user's request to join a group.
     * If approval is required, creates a join request.
     * Otherwise, adds the user immediately.
     *
     * @param group the group to join
     * @param user the user requesting to join
     * @throws SQLException if database error occurs
     */
    public void joinGroup(Group group, User user) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(user, "User cannot be null");

        if (group.isRequire_approval()) {
            // Create join request if not already requested/member
            if (!groupDAO.hasUserRequestedToJoin(group.getID(), user.getUserId()) &&
                !groupDAO.isUserMemberOfGroup(group.getID(), user.getUserId())) {
                groupDAO.createJoinRequest(group.getID(), user.getUserId());
            }
        } else {
            // Add immediately
            group.addMember(user);
            groupDAO.addToGroup(group.getID(), user);
        }
    }

    /**
     * Approves a join request and adds the user to the group.
     *
     * @param group the group being joined
     * @param admin the admin approving the request (must be owner)
     * @param notification the approval notification to process
     * @throws SQLException if database error occurs
     * @throws InsufficientPermissionsException if admin is not the owner
     */
    public void approveRequest(Group group, User admin, GroupApprovalNotification notification) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(notification, "Notification cannot be null");

        // Check permissions (only owner can approve)
        if (!group.getOwner().getUsername().equals(admin.getUsername())) {
            throw new InsufficientPermissionsException("You are not the owner of the group");
        }

        // Approve via notifier
        notifier.approveNotification(admin, notification);

        if (!notification.isApproved()) {
            throw new IllegalStateException("Notification has not been approved");
        }

        // Add user to group
        User from = notification.getFrom();
        group.addMember(from);
        groupDAO.addToGroup(group.getID(), from);
    }

    /**
     * Denies a join request.
     *
     * @param group the group being requested
     * @param admin the admin denying the request (must be owner)
     * @param notification the notification to deny
     * @throws SQLException if database error occurs
     * @throws InsufficientPermissionsException if admin is not the owner
     */
    public void denyRequest(Group group, User admin, GroupApprovalNotification notification) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(notification, "Notification cannot be null");

        // Check permissions (only owner can deny)
        if (!group.getOwner().getUsername().equals(admin.getUsername())) {
            throw new InsufficientPermissionsException("You are not the owner of the group");
        }

        notifier.denyNotification(admin, notification);
    }

    /**
     * Processes a join request (approve or reject).
     *
     * @param group the group being requested
     * @param admin the admin processing the request
     * @param requestId the ID of the join request
     * @param approve true to approve, false to reject
     * @throws SQLException if database error occurs
     * @throws InsufficientPermissionsException if user is not an admin
     */
    public void processJoinRequest(Group group, User admin, int requestId, boolean approve) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(admin, "Admin cannot be null");

        // Check permissions (must be admin) - delegate to GroupRoleService
        // For now, we check using the DAO
        if (!isAdmin(group, admin)) {
            throw new InsufficientPermissionsException("Only admins can process join requests");
        }

        String status = approve ? "approved" : "rejected";
        boolean success = groupDAO.processJoinRequest(requestId, status, admin.getUserId());

        if (!success) {
            throw new IllegalStateException("Failed to process join request");
        }

        // If approved, reload members from database
        if (approve) {
            List<com.cab302.peerpractice.Model.Entities.GroupMemberEntity> members =
                    groupDAO.getGroupMembers(group.getID());
            List<User> users = new ArrayList<>();
            for (com.cab302.peerpractice.Model.Entities.GroupMemberEntity member : members) {
                if (member.getUser() != null) {
                    users.add(member.getUser());
                    // Sync roles
                    group.setMemberRole(member.getUserId(), member.getRole());
                }
            }
            group.setMembers(users);
        }
    }

    /**
     * Removes a member from a group (admin only).
     *
     * @param group the group to remove from
     * @param admin the admin performing the removal
     * @param memberToKick the member to remove
     * @throws SQLException if database error occurs
     * @throws InsufficientPermissionsException if admin doesn't have permission or trying to kick owner
     */
    public void kickMember(Group group, User admin, User memberToKick) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(admin, "Admin cannot be null");
        Objects.requireNonNull(memberToKick, "Member to kick cannot be null");

        // Check permissions
        if (!isAdmin(group, admin)) {
            throw new InsufficientPermissionsException("Only admins can kick members");
        }

        // Cannot kick the owner
        if (group.getOwner().getUsername().equals(memberToKick.getUsername())) {
            throw new InsufficientPermissionsException("Cannot kick the group owner");
        }

        // Remove from database
        boolean success = groupDAO.removeMember(group.getID(), memberToKick.getUserId(), admin.getUserId());
        if (!success) {
            throw new IllegalStateException("Failed to remove member from group");
        }

        // Update local group object
        List<User> updatedMembers = new ArrayList<>(group.getMembers());
        updatedMembers.remove(memberToKick);
        group.setMembers(updatedMembers);
    }

    /**
     * Deletes a group (owner only).
     *
     * @param group the group to delete
     * @param user the user requesting deletion (must be owner)
     * @throws SQLException if database error occurs
     * @throws InsufficientPermissionsException if user is not the owner
     */
    public void deleteGroup(Group group, User user) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(user, "User cannot be null");

        // Only owner can delete
        if (!group.getOwner().getUsername().equals(user.getUsername())) {
            throw new InsufficientPermissionsException("Only the group owner can delete the group");
        }

        boolean success = groupDAO.deleteGroup(group);
        if (!success) {
            throw new IllegalStateException("Failed to delete group");
        }
    }

    /**
     * Helper method to check if a user is an admin.
     * TODO: Replace with AuthorizationService in future refactoring.
     *
     * @param group the group
     * @param user the user
     * @return true if user is owner or admin
     */
    private boolean isAdmin(Group group, User user) {
        if (group == null || user == null) return false;

        // Check if owner
        if (group.getOwner().getUsername().equals(user.getUsername())) {
            return true;
        }

        // Check admin role
        return groupDAO.isAdmin(group.getID(), user.getUserId());
    }
}
