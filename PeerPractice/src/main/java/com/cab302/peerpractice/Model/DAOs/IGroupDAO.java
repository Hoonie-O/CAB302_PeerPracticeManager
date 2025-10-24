package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupJoinRequest;
import com.cab302.peerpractice.Model.Entities.GroupMemberEntity;
import com.cab302.peerpractice.Model.Entities.User;

import java.sql.SQLException;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing user groups and group operations.
 *
 * <p>This interface defines the contract for group management data operations,
 * providing methods to create, manage, and query groups, memberships, and
 * group-related workflows in the peer practice system.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Complete group lifecycle management (creation, modification, deletion)</li>
 *   <li>Member role management and permission controls</li>
 *   <li>Join request workflow with approval system</li>
 *   <li>Flexible group search and membership queries</li>
 *   <li>Admin privilege management and role transitions</li>
 * </ul>
 *
 * @see Group
 * @see GroupMemberEntity
 * @see GroupJoinRequest
 * @see User
 * @see GroupDAO
 */
public interface IGroupDAO {

    // === Group Lifecycle ===

    /**
     * <hr>
     * Creates a new group in the system and sets up initial membership.
     *
     * <p>Persists a new group entity to the database and automatically
     * assigns the group creator as an admin member. Returns the generated
     * group identifier for future reference and operations.
     *
     * @param group the Group object to be created
     * @return the auto-generated group ID if successful, -1 otherwise
     * @throws SQLException if database operation fails
     */
    int addGroup(Group group) throws SQLException; // returns group ID, or -1 / exception if failed

    /**
     * <hr>
     * Updates an existing group's information and settings.
     *
     * <p>Modifies group attributes such as name, description, and approval
     * requirements while preserving the group structure and membership.
     *
     * @param group the Group object with updated information
     * @return true if the group was successfully updated, false otherwise
     */
    boolean updateGroup(Group group);

    /**
     * <hr>
     * Deletes a group from the system using Group object reference.
     *
     * <p>Removes the group entity and all associated membership records
     * and join requests through cascading delete operations.
     *
     * @param group the Group object to delete
     * @return true if the group was successfully deleted, false otherwise
     */
    boolean deleteGroup(Group group);

    /**
     * <hr>
     * Deletes a group from the system using group identifier.
     *
     * <p>Removes the group entity by its primary key, including all
     * associated membership records and administrative data.
     *
     * @param groupId the ID of the group to delete
     * @return true if the group was successfully deleted, false otherwise
     */
    boolean deleteGroup(int groupId);

    // -------------------- HELPERS / CHECKS --------------------

    /**
     * <hr>
     * Checks if a group with the specified name already exists.
     *
     * <p>Verifies group name uniqueness within the system to prevent
     * duplicate group creation and naming conflicts.
     *
     * @param name the group name to check for existence
     * @return true if a group with this name exists, false otherwise
     */
    boolean existsByName(String name);

    /**
     * <hr>
     * Checks if a user belongs to any group in the system.
     *
     * <p>Determines whether the specified user has membership in at least
     * one group, regardless of their role or group type.
     *
     * @param user the user to check for group membership
     * @return true if the user belongs to any group, false otherwise
     */
    boolean existstByUser(User user);

    /**
     * <hr>
     * Checks if a group entity already exists in the database.
     *
     * <p>Uses the group's identifying attributes to verify existence,
     * typically used before group creation or modification operations.
     *
     * @param group the Group object to check for existence
     * @return true if an equivalent group exists, false otherwise
     */
    boolean groupExists(Group group);

    // === Group Search ===

    /**
     * <hr>
     * Searches for a group by its unique identifier.
     *
     * <p>Retrieves a complete group entity including members and settings
     * using the group's primary key for precise group identification.
     *
     * @param id the unique identifier of the group to find
     * @return the Group object if found, null otherwise
     */
    Group searchByID(int id);

    /**
     * <hr>
     * Searches for groups by name using partial matching.
     *
     * <p>Finds groups whose names contain the specified search string,
     * supporting wildcard searches for flexible group discovery and browsing.
     *
     * @param name the name or partial name to search for
     * @return a list of Group objects matching the search criteria
     */
    List<Group> searchByName(String name);

    /**
     * <hr>
     * Searches for groups that a specific user belongs to.
     *
     * <p>Fetches all groups where the specified user is a member, providing
     * a complete view of the user's group affiliations and memberships.
     *
     * @param user the user whose groups are being searched
     * @return a list of Group objects that the user belongs to
     */
    List<Group> searchByUser(User user);

    /**
     * <hr>
     * Searches for groups that contain all specified users as members.
     *
     * <p>Finds groups where every user in the provided list is a member,
     * useful for finding common groups among multiple users for collaborative
     * activities or messaging.
     *
     * @param users the list of users that must all be group members
     * @return a list of Group objects containing all specified users
     */
    List<Group> searchByMembers(List<User> users);

    /**
     * <hr>
     * Retrieves all groups from the system.
     *
     * <p>Fetches every group entity stored in the database, including
     * their complete member information and group settings.
     *
     * @return a list of all Group objects in the system
     */
    List<Group> getAllGroups();

    // === Group Membership ===

    /**
     * <hr>
     * Adds a user to a group as a regular member.
     *
     * <p>Creates a group membership record with default member role,
     * typically used for direct group additions without join requests
     * or for automated member assignments.
     *
     * @param groupId the ID of the group to add the user to
     * @param user the User object to add to the group
     * @return true if the user was successfully added, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean addToGroup(int groupId, User user) throws SQLException;

    /**
     * <hr>
     * Removes a member from a group (admin-only operation).
     *
     * <p>Allows group administrators to remove members from the group.
     * Verifies that the acting user has admin privileges before
     * permitting the member removal operation.
     *
     * @param groupId the ID of the group to remove the member from
     * @param targetUserId the ID of the user to remove from the group
     * @param actingUserId the ID of the admin user performing the removal
     * @return true if the member was successfully removed, false otherwise
     */
    boolean removeMember(int groupId, String targetUserId, String actingUserId);

    /**
     * <hr>
     * Checks if a user is currently a member of a specific group.
     *
     * <p>Verifies active group membership regardless of the user's role
     * in the group, used for permission checks and group access control.
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return true if the user is a member of the group, false otherwise
     */
    boolean isUserMemberOfGroup(int groupId, String userId);

    /**
     * <hr>
     * Retrieves all members of a specific group.
     *
     * <p>Fetches complete member information including user details and roles
     * for all users belonging to the specified group.
     *
     * @param groupId the ID of the group to get members for
     * @return a list of GroupMemberEntity objects representing group members
     */
    List<GroupMemberEntity> getGroupMembers(int groupId);

    /**
     * <hr>
     * Retrieves the role of a specific user in a specific group.
     *
     * <p>Determines whether a user is an admin or regular member of the group,
     * or returns null if the user is not a member. Used for role-based
     * permission checks and UI customization.
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return the user's role ('admin' or 'member') if found, null otherwise
     */
    String getUserRoleInGroup(int groupId, String userId); // NEW

    // === Admin / Roles ===

    /**
     * <hr>
     * Checks if a user has admin privileges in a specific group.
     *
     * <p>Verifies whether the specified user has the 'admin' role
     * in the given group, used for administrative permission checks
     * and privileged operation authorization.
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return true if the user is an admin in the group, false otherwise
     */
    boolean isAdmin(int groupId, String userId);

    /**
     * <hr>
     * Promotes a group member to admin role.
     *
     * <p>Elevates a regular member to admin status, granting them
     * administrative privileges for group management. Requires
     * the promoting user to be an existing admin.
     *
     * @param groupId the ID of the group where the promotion occurs
     * @param targetUserId the ID of the user to promote to admin
     * @param actingUserId the ID of the admin user performing the promotion
     * @return true if the promotion was successful, false otherwise
     */
    boolean promoteToAdmin(int groupId, String targetUserId, String actingUserId);

    /**
     * <hr>
     * Demotes a group admin to regular member role.
     *
     * <p>Removes admin privileges from a user, reducing them to regular
     * member status. Requires the demoting user to be an admin and
     * ensures at least one admin remains in the group.
     *
     * @param groupId the ID of the group where the demotion occurs
     * @param targetUserId the ID of the user to demote from admin
     * @param actingUserId the ID of the admin user performing the demotion
     * @return true if the demotion was successful, false otherwise
     */
    boolean demoteAdmin(int groupId, String targetUserId, String actingUserId);

    // === Join Requests ===

    /**
     * <hr>
     * Sets the approval requirement flag for a group.
     *
     * <p>Controls whether new members require admin approval to join the group.
     * When enabled, users must request to join and be approved by an admin;
     * when disabled, users can join groups directly.
     *
     * @param groupId the ID of the group to modify
     * @param requireApproval true to require approval for new members, false for open joining
     * @return true if the setting was successfully updated, false otherwise
     */
    boolean setRequireApproval(int groupId, boolean requireApproval);

    /**
     * <hr>
     * Creates a new join request for a user to a group.
     *
     * <p>Submits a join request with 'pending' status for groups that
     * require admin approval, allowing administrators to review and
     * process membership requests.
     *
     * @param groupId the ID of the group to request joining
     * @param userId the ID of the user requesting to join
     * @throws SQLException if database operation fails
     */
    void createJoinRequest(int groupId, String userId) throws SQLException;

    /**
     * <hr>
     * Checks if a user has a pending join request for a group.
     *
     * <p>Verifies whether the specified user has already submitted a
     * join request that is still awaiting approval or rejection,
     * preventing duplicate join requests.
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return true if the user has a pending join request, false otherwise
     */
    boolean hasUserRequestedToJoin(int groupId, String userId);

    /**
     * <hr>
     * Retrieves all pending join requests for a specific group.
     *
     * <p>Fetches join requests that are awaiting administrative review
     * for the specified group, including user details and request timestamps
     * for the approval workflow.
     *
     * @param groupId the ID of the group to get pending requests for
     * @return a list of GroupJoinRequest objects with 'pending' status
     */
    List<GroupJoinRequest> getPendingJoinRequests(int groupId);

    /**
     * <hr>
     * Processes a group join request by approving or rejecting it.
     *
     * <p>Updates the join request status and, if approved, automatically
     * adds the user to the group as a member. Records the approver and
     * processing timestamp for audit purposes.
     *
     * @param requestId the ID of the join request to process
     * @param status the new status ('approved' or 'rejected')
     * @param approverUserId the ID of the user processing the request
     * @return true if the request was successfully processed, false otherwise
     * @throws SQLException if database operation fails
     */
    boolean processJoinRequest(int requestId, String status, String approverUserId) throws SQLException; // NEW
}