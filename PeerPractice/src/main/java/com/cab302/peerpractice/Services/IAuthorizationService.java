package com.cab302.peerpractice.Services;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.ValueObjects.GroupRole;

/**
 * Service interface for authorization and permission checks.
 * Centralizes all permission logic in one place following the
 * Single Responsibility Principle.
 *
 * This abstraction enables:
 * - Consistent permission checking across the application
 * - Easy modification of permission rules
 * - Testability through mocking
 * - Audit logging of permission checks
 */
public interface IAuthorizationService {

    /**
     * Checks if a user has administrative privileges in a group.
     *
     * @param user the user to check
     * @param group the group context
     * @return true if user is owner or admin
     */
    boolean hasAdminPrivileges(User user, Group group);

    /**
     * Checks if a user can manage members in a group.
     *
     * @param user the user to check
     * @param group the group context
     * @return true if user can add/remove/promote members
     */
    boolean canManageMembers(User user, Group group);

    /**
     * Checks if a user can delete a group.
     *
     * @param user the user to check
     * @param group the group context
     * @return true if user is the owner
     */
    boolean canDeleteGroup(User user, Group group);

    /**
     * Checks if a user can edit group settings.
     *
     * @param user the user to check
     * @param group the group context
     * @return true if user has admin privileges
     */
    boolean canEditGroupSettings(User user, Group group);

    /**
     * Checks if a user can create sessions in a group.
     *
     * @param user the user to check
     * @param group the group context
     * @return true if user is a member of the group
     */
    boolean canCreateSession(User user, Group group);

    /**
     * Checks if a user can upload files to a group.
     *
     * @param user the user to check
     * @param group the group context
     * @return true if user is a member of the group
     */
    boolean canUploadFiles(User user, Group group);

    /**
     * Gets the role of a user in a group.
     *
     * @param user the user
     * @param group the group
     * @return the user's GroupRole
     */
    GroupRole getUserRole(User user, Group group);

    /**
     * Checks if a user can perform a specific action.
     *
     * @param user the user
     * @param group the group
     * @param permission the permission to check
     * @return true if user has the permission
     */
    boolean hasPermission(User user, Group group, Permission permission);

    /**
     * Enum representing different permissions in the system.
     */
    enum Permission {
        VIEW_GROUP,
        EDIT_GROUP,
        DELETE_GROUP,
        MANAGE_MEMBERS,
        CREATE_SESSION,
        EDIT_SESSION,
        DELETE_SESSION,
        UPLOAD_FILES,
        DELETE_FILES,
        POST_MESSAGES
    }
}
