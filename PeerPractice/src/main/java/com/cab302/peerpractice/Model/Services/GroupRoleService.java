package com.cab302.peerpractice.Model.Services;

import com.cab302.peerpractice.Exceptions.InsufficientPermissionsException;
import com.cab302.peerpractice.Model.DAOs.IGroupDAO;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.ValueObjects.GroupRole;

import java.sql.SQLException;
import java.util.Objects;

/**
 * Service responsible for group role management.
 * Follows the Single Responsibility Principle by focusing exclusively on
 * role-related operations within groups.
 *
 * Responsibilities:
 * - Checking user roles (admin, member, owner)
 * - Promoting members to admin
 * - Demoting admins to member
 *
 * This service does NOT handle group operations (see {@link GroupOperationService})
 * or general permission checking (see {@link IAuthorizationService}).
 *
 * @see GroupOperationService
 * @see IAuthorizationService
 * @see GroupRole
 */
public class GroupRoleService {

    private final IGroupDAO groupDAO;

    /**
     * Creates a new GroupRoleService.
     *
     * @param groupDAO the group data access object
     */
    public GroupRoleService(IGroupDAO groupDAO) {
        this.groupDAO = Objects.requireNonNull(groupDAO, "GroupDAO cannot be null");
    }

    /**
     * Checks if a user has admin privileges in a group.
     * Returns true if the user is the owner or has been granted admin role.
     *
     * @param group the group to check
     * @param user the user to check
     * @return true if user is owner or admin, false otherwise
     */
    public boolean isAdmin(Group group, User user) {
        if (group == null || user == null) {
            return false;
        }

        // Check if user is the original owner
        if (group.getOwner().getUsername().equals(user.getUsername())) {
            return true;
        }

        // Check if user has admin role in database
        return groupDAO.isAdmin(group.getID(), user.getUserId());
    }

    /**
     * Gets the role of a user within a group.
     *
     * @param group the group
     * @param user the user
     * @return the user's GroupRole (OWNER, ADMIN, or MEMBER), or null if not a member
     */
    public GroupRole getUserRole(Group group, User user) {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(user, "User cannot be null");

        // Check if owner
        if (group.getOwner().getUserId().equals(user.getUserId())) {
            return GroupRole.OWNER;
        }

        // Check member role
        String roleString = group.getMemberRole(user.getUserId());
        if (roleString != null) {
            return GroupRole.fromValue(roleString);
        }

        // Not a member
        return null;
    }

    /**
     * Promotes a member to admin role.
     * Only existing admins can promote members.
     *
     * @param group the group
     * @param promoter the user performing the promotion (must be admin)
     * @param userToPromote the user to promote to admin
     * @throws SQLException if database error occurs
     * @throws InsufficientPermissionsException if promoter is not an admin
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalStateException if promotion fails
     */
    public void promoteToAdmin(Group group, User promoter, User userToPromote) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(promoter, "Promoter cannot be null");
        Objects.requireNonNull(userToPromote, "User to promote cannot be null");

        // Check permissions
        if (!isAdmin(group, promoter)) {
            throw new InsufficientPermissionsException("Only admins can promote members to admin");
        }

        // Promote in database
        boolean success = groupDAO.promoteToAdmin(
                group.getID(),
                userToPromote.getUserId(),
                promoter.getUserId()
        );

        if (!success) {
            throw new IllegalStateException("Failed to promote user to admin");
        }

        // Update local group object
        group.setMemberRole(userToPromote.getUserId(), "admin");
    }

    /**
     * Demotes an admin to regular member role.
     * Only existing admins can demote other admins.
     * The group owner cannot be demoted.
     *
     * @param group the group
     * @param demoter the user performing the demotion (must be admin)
     * @param userToDemote the admin to demote to member
     * @throws SQLException if database error occurs
     * @throws InsufficientPermissionsException if demoter is not an admin or trying to demote owner
     * @throws IllegalArgumentException if any parameter is null
     * @throws IllegalStateException if demotion fails
     */
    public void demoteAdmin(Group group, User demoter, User userToDemote) throws SQLException {
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(demoter, "Demoter cannot be null");
        Objects.requireNonNull(userToDemote, "User to demote cannot be null");

        // Check permissions
        if (!isAdmin(group, demoter)) {
            throw new InsufficientPermissionsException("Only admins can demote other admins");
        }

        // Cannot demote the original owner
        if (group.getOwner().getUsername().equals(userToDemote.getUsername())) {
            throw new InsufficientPermissionsException("Cannot demote the group owner");
        }

        // Demote in database
        boolean success = groupDAO.demoteAdmin(
                group.getID(),
                userToDemote.getUserId(),
                demoter.getUserId()
        );

        if (!success) {
            throw new IllegalStateException("Failed to demote admin");
        }

        // Update local group object
        group.setMemberRole(userToDemote.getUserId(), "member");
    }

    /**
     * Checks if a user is the owner of a group.
     *
     * @param group the group
     * @param user the user
     * @return true if user is the owner, false otherwise
     */
    public boolean isOwner(Group group, User user) {
        if (group == null || user == null) {
            return false;
        }
        return group.getOwner().getUserId().equals(user.getUserId());
    }

    /**
     * Checks if a user is a member of a group (any role).
     *
     * @param group the group
     * @param user the user
     * @return true if user is a member (owner, admin, or member), false otherwise
     */
    public boolean isMember(Group group, User user) {
        return getUserRole(group, user) != null;
    }
}
