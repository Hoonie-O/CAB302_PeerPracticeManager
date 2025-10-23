package com.cab302.peerpractice.Services;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.ValueObjects.GroupRole;

import java.util.Objects;

/**
 * Default implementation of authorization service.
 * Provides centralized permission checking logic.
 */
public class AuthorizationService implements IAuthorizationService {

    @Override
    public boolean hasAdminPrivileges(User user, Group group) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");

        GroupRole role = getUserRole(user, group);
        return role.hasAdminPrivileges();
    }

    @Override
    public boolean canManageMembers(User user, Group group) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");

        GroupRole role = getUserRole(user, group);
        return role.canManageMembers();
    }

    @Override
    public boolean canDeleteGroup(User user, Group group) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");

        GroupRole role = getUserRole(user, group);
        return role.canDeleteGroup();
    }

    @Override
    public boolean canEditGroupSettings(User user, Group group) {
        return hasAdminPrivileges(user, group);
    }

    @Override
    public boolean canCreateSession(User user, Group group) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");

        // Any member can create sessions
        return getUserRole(user, group) != null;
    }

    @Override
    public boolean canUploadFiles(User user, Group group) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");

        // Any member can upload files
        return getUserRole(user, group) != null;
    }

    @Override
    public GroupRole getUserRole(User user, Group group) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");

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

    @Override
    public boolean hasPermission(User user, Group group, Permission permission) {
        Objects.requireNonNull(user, "User cannot be null");
        Objects.requireNonNull(group, "Group cannot be null");
        Objects.requireNonNull(permission, "Permission cannot be null");

        GroupRole role = getUserRole(user, group);
        if (role == null) {
            return false; // Not a member
        }

        return switch (permission) {
            case VIEW_GROUP -> true; // All members can view
            case EDIT_GROUP, MANAGE_MEMBERS -> role.hasAdminPrivileges();
            case DELETE_GROUP -> role.canDeleteGroup();
            case CREATE_SESSION, UPLOAD_FILES, POST_MESSAGES -> true; // All members
            case EDIT_SESSION, DELETE_SESSION -> role.hasAdminPrivileges(); // Admins only
            case DELETE_FILES -> role.hasAdminPrivileges(); // Admins can delete any file
        };
    }
}
