package com.cab302.peerpractice.Model.ValueObjects;

/**
 * Value object representing a user's role within a group.
 * Provides type safety and encapsulation for role-based authorization.
 */
public enum GroupRole {
    OWNER("owner"),
    ADMIN("admin"),
    MEMBER("member");

    private final String value;

    GroupRole(String value) {
        this.value = value;
    }

    /**
     * Gets the string representation of the role for database storage.
     *
     * @return the role value
     */
    public String getValue() {
        return value;
    }

    /**
     * Converts a string value to a GroupRole enum.
     *
     * @param value the role string from database
     * @return the corresponding GroupRole
     * @throws IllegalArgumentException if the value is not a valid role
     */
    public static GroupRole fromValue(String value) {
        if (value == null) {
            return MEMBER; // Default role
        }

        for (GroupRole role : GroupRole.values()) {
            if (role.value.equalsIgnoreCase(value)) {
                return role;
            }
        }

        throw new IllegalArgumentException("Invalid role: " + value);
    }

    /**
     * Checks if this role has administrative privileges.
     *
     * @return true if role is OWNER or ADMIN
     */
    public boolean hasAdminPrivileges() {
        return this == OWNER || this == ADMIN;
    }

    /**
     * Checks if this role can manage members.
     *
     * @return true if role is OWNER or ADMIN
     */
    public boolean canManageMembers() {
        return hasAdminPrivileges();
    }

    /**
     * Checks if this role can delete the group.
     *
     * @return true if role is OWNER
     */
    public boolean canDeleteGroup() {
        return this == OWNER;
    }

    @Override
    public String toString() {
        return value;
    }
}
