package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupJoinRequest;
import com.cab302.peerpractice.Model.Entities.GroupMemberEntity;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <hr>
 * SQLite implementation of group Data Access Object.
 *
 * <p>This class provides concrete SQLite database operations for group management,
 * handling persistence and retrieval of groups, members, and join requests.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Complete group lifecycle management (create, read, update, delete)</li>
 *   <li>Member role management and permission controls</li>
 *   <li>Join request workflow with approval system</li>
 *   <li>Complex group search and membership queries</li>
 * </ul>
 *
 * @see Group
 * @see IGroupDAO
 * @see User
 * @see GroupJoinRequest
 */
public class GroupDAO implements IGroupDAO {

    /** <hr> Database connection instance for SQLite operations. */
    private final Connection connection;
    /** <hr> User DAO for user entity resolution and operations. */
    private final IUserDAO userDao;

    /**
     * <hr>
     * Constructs a new GroupDAO with database connection and user DAO dependency.
     *
     * <p>Initializes the SQLite connection and ensures all required database
     * tables exist by calling createTables() during construction.
     *
     * @param userDao the User DAO for user entity operations
     * @throws SQLException if database connection or table creation fails
     */
    public GroupDAO(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        this.userDao = userDao;
        createTables();
    }

    /**
     * <hr>
     * Creates all required database tables if they don't exist.
     *
     * <p>Defines the complete database schema for group management including
     * groups table, group_members table, and group_join_requests table
     * with appropriate constraints and relationships.
     *
     * @throws SQLException if table creation fails
     */
    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            st.execute("CREATE TABLE IF NOT EXISTS groups (" +
                    "group_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT DEFAULT '', " +
                    "require_approval INTEGER DEFAULT 0, " +
                    "owner TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL" +
                    ")");
            st.execute("CREATE TABLE IF NOT EXISTS group_members (" +
                    "group_id INTEGER NOT NULL, " +
                    "user_id TEXT NOT NULL, " +
                    "role TEXT DEFAULT 'member' CHECK(role IN ('member','admin')), " +
                    "joined_at TEXT NOT NULL, " +
                    "PRIMARY KEY(group_id, user_id), " +
                    "FOREIGN KEY(group_id) REFERENCES groups(group_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");
            st.execute("CREATE TABLE IF NOT EXISTS group_join_requests (" +
                    "request_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "group_id INTEGER NOT NULL, " +
                    "user_id TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'pending' CHECK(status IN ('pending','approved','rejected')), " +
                    "requested_at TEXT NOT NULL, " +
                    "processed_at TEXT, " +
                    "processed_by TEXT, " +
                    "FOREIGN KEY(group_id) REFERENCES groups(group_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");
        }
    }
    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Adds a new group to the database and sets up initial membership.
     *
     * <p>Persists a new group entity to the database, automatically assigning
     * the group owner as an admin member. Returns the generated group ID
     * for future reference.
     *
     * @param group the Group object to be added to the database
     * @return the auto-generated group ID if successful, -1 otherwise
     * @throws SQLException if database operation fails
     */
    @Override
    public int addGroup(Group group) {
        if (groupExists(group)) return -1;
        String sql = "INSERT INTO groups (name, description, require_approval, owner, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.setInt(3, group.isRequire_approval() ? 1 : 0);
            ps.setString(4, group.getOwner().getUsername());
            ps.setString(5, group.getCreated_at().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int groupId = keys.getInt(1);
                    group.setID(groupId);
                    User owner = userDao.findUser("username", group.getOwner().getUsername());
                    if (owner != null) addMemberWithRole(groupId, owner.getUserId(), "admin");
                    return groupId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add group", e);
        }
        return -1;
    }

    /**
     * <hr>
     * Adds a user to a group with a specific role.
     *
     * <p>Creates a group membership record with the specified role (admin or member)
     * and sets the join timestamp to the current time.
     *
     * @param groupId the ID of the group to add the user to
     * @param userId the ID of the user to add to the group
     * @param role the role to assign to the user ('admin' or 'member')
     * @return true if the member was successfully added, false otherwise
     */
    public boolean addMemberWithRole(int groupId, String userId, String role) {
        String sql = "INSERT OR IGNORE INTO group_members (group_id, user_id, role, joined_at) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            ps.setString(3, role);
            ps.setString(4, LocalDateTime.now().toString());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add member with role", e);
        }
    }

    // -------------------- READ --------------------

    /**
     * <hr>
     * Searches for a group by its unique identifier.
     *
     * <p>Retrieves a complete group entity including members and their roles
     * using the group's primary key.
     *
     * @param id the unique identifier of the group to find
     * @return the Group object if found, null otherwise
     */
    @Override
    public Group searchByID(int id) {
        String sql = "SELECT * FROM groups WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRowToGroup(rs);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search group by ID", e);
        }
        return null;
    }

    /**
     * <hr>
     * Searches for groups that a specific user belongs to.
     *
     * <p>Fetches all groups where the specified user is a member, regardless
     * of their role in the group.
     *
     * @param user the user whose groups are being searched
     * @return a list of Group objects that the user belongs to
     */
    @Override
    public List<Group> searchByUser(User user) {
        if (user == null) return new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT g.* FROM groups g JOIN group_members gm ON g.group_id = gm.group_id WHERE gm.user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) groups.add(mapRowToGroup(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search groups by user", e);
        }
        return groups;
    }

    /**
     * <hr>
     * Searches for groups that contain all specified users as members.
     *
     * <p>Finds groups where every user in the provided list is a member,
     * useful for finding common groups among multiple users.
     *
     * @param users the list of users that must all be group members
     * @return a list of Group objects containing all specified users
     */
    @Override
    public List<Group> searchByMembers(List<User> users) {
        if (users == null || users.isEmpty()) return new ArrayList<>();
        List<Group> groups = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT DISTINCT g.* FROM groups g JOIN group_members gm ON g.group_id = gm.group_id WHERE gm.user_id IN (");
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");

        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < users.size(); i++) ps.setString(i + 1, users.get(i).getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) groups.add(mapRowToGroup(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search groups by members", e);
        }
        return groups;
    }

    /**
     * <hr>
     * Searches for groups by name using partial matching.
     *
     * <p>Finds groups whose names contain the specified search string,
     * supporting wildcard searches for flexible group discovery.
     *
     * @param name the name or partial name to search for
     * @return a list of Group objects matching the search criteria
     */
    @Override
    public List<Group> searchByName(String name) {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups WHERE name LIKE ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) groups.add(mapRowToGroup(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search groups by name", e);
        }
        return groups;
    }

    /**
     * <hr>
     * Retrieves all groups from the database.
     *
     * <p>Fetches every group entity stored in the system, including
     * their complete member and role information.
     *
     * @return a list of all Group objects in the database
     */
    @Override
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        String sql = "SELECT * FROM groups";
        try (Statement st = connection.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) groups.add(mapRowToGroup(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get all groups", e);
        }
        return groups;
    }

    /**
     * <hr>
     * Retrieves all members of a specific group.
     *
     * <p>Fetches complete member information including user details and roles
     * for all members belonging to the specified group.
     *
     * @param groupId the ID of the group to get members for
     * @return a list of GroupMemberEntity objects representing group members
     */
    @Override
    public List<GroupMemberEntity> getGroupMembers(int groupId) {
        List<GroupMemberEntity> members = new ArrayList<>();
        String sql = "SELECT * FROM group_members WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupMemberEntity member = new GroupMemberEntity();
                    member.setGroupId(rs.getInt("group_id"));
                    member.setUserId(rs.getString("user_id"));
                    member.setRole(rs.getString("role"));
                    member.setJoinedAt(LocalDateTime.parse(rs.getString("joined_at")));
                    member.setUser(userDao.findUserById(rs.getString("user_id")));
                    members.add(member);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get group members", e);
        }
        return members;
    }

    /**
     * <hr>
     * Retrieves the role of a specific user in a specific group.
     *
     * <p>Determines whether a user is an admin or regular member of the group,
     * or returns null if the user is not a member.
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return the user's role ('admin' or 'member') if found, null otherwise
     */
    @Override
    public String getUserRoleInGroup(int groupId, String userId) {
        String sql = "SELECT role FROM group_members WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get user role in group", e);
        }
        return null; // not found
    }

    /**
     * <hr>
     * Retrieves all pending join requests for a specific group.
     *
     * <p>Fetches join requests that are awaiting approval or rejection
     * for the specified group, including user details and request timestamps.
     *
     * @param groupId the ID of the group to get pending requests for
     * @return a list of GroupJoinRequest objects with 'pending' status
     */
    @Override
    public List<GroupJoinRequest> getPendingJoinRequests(int groupId) {
        List<GroupJoinRequest> requests = new ArrayList<>();
        String sql = "SELECT * FROM group_join_requests WHERE group_id = ? AND status = 'pending'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupJoinRequest request = new GroupJoinRequest(
                            rs.getInt("request_id"),
                            rs.getInt("group_id"),
                            rs.getString("user_id"),
                            rs.getString("status"),
                            LocalDateTime.parse(rs.getString("requested_at")),
                            rs.getString("processed_at") != null ? LocalDateTime.parse(rs.getString("processed_at")) : null,
                            rs.getString("processed_by")
                    );
                    request.setUser(userDao.findUserById(rs.getString("user_id")));
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get pending join requests", e);
        }
        return requests;
    }

    /**
     * <hr>
     * Processes a group join request by approving or rejecting it.
     *
     * <p>Updates the join request status and, if approved, automatically
     * adds the user to the group as a member. Records the approver and
     * processing timestamp.
     *
     * @param requestId the ID of the join request to process
     * @param status the new status ('approved' or 'rejected')
     * @param approverUserId the ID of the user processing the request
     * @return true if the request was successfully processed, false otherwise
     * @throws SQLException if database operation fails
     */
    @Override
    public boolean processJoinRequest(int requestId, String status, String approverUserId) {
        if (!status.equals("approved") && !status.equals("rejected")) {
            throw new IllegalArgumentException("Invalid status: " + status);
        }

        String sql = "UPDATE group_join_requests SET status = ?, processed_at = ?, processed_by = ? " +
                "WHERE request_id = ? AND status = 'pending'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setString(3, approverUserId);
            ps.setInt(4, requestId);

            int updated = ps.executeUpdate();

            // if approved, also add the user as a member
            if (updated > 0 && status.equals("approved")) {
                GroupJoinRequest req = getJoinRequestById(requestId);
                if (req != null) {
                    User user = userDao.findUserById(req.getUserId());
                    return addToGroup(req.getGroupId(), user);
                }
            }
            return updated > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to process join request", e);
        }
    }

    /**
     * <hr>
     * Helper method to fetch a join request by its unique identifier.
     *
     * <p>Retrieves a specific join request entity using its primary key,
     * used internally for request processing operations.
     *
     * @param requestId the unique identifier of the join request
     * @return the GroupJoinRequest object if found, null otherwise
     */
    private GroupJoinRequest getJoinRequestById(int requestId) {
        String sql = "SELECT * FROM group_join_requests WHERE request_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, requestId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new GroupJoinRequest(
                            rs.getInt("request_id"),
                            rs.getInt("group_id"),
                            rs.getString("user_id"),
                            rs.getString("status"),
                            LocalDateTime.parse(rs.getString("requested_at")),
                            rs.getString("processed_at") != null ? LocalDateTime.parse(rs.getString("processed_at")) : null,
                            rs.getString("processed_by")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get join request by id", e);
        }
        return null;
    }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Updates an existing group's basic information.
     *
     * <p>Modifies the group's name, description, and approval requirements
     * while preserving the group ID, owner, and creation timestamp.
     *
     * @param group the Group object with updated information
     * @return true if the group was successfully updated, false otherwise
     */
    @Override
    public boolean updateGroup(Group group) {
        String sql = "UPDATE groups SET name = ?, description = ?, require_approval = ? WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.setInt(3, group.isRequire_approval() ? 1 : 0);
            ps.setInt(4, group.getID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update group", e);
        }
    }

    /**
     * <hr>
     * Sets the approval requirement flag for a group.
     *
     * <p>Controls whether new members require admin approval to join the group.
     * When enabled, users must request to join and be approved by an admin.
     *
     * @param groupId the ID of the group to modify
     * @param requireApproval true to require approval for new members, false for open joining
     * @return true if the setting was successfully updated, false otherwise
     */
    @Override
    public boolean setRequireApproval(int groupId, boolean requireApproval) {
        String sql = "UPDATE groups SET require_approval = ? WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, requireApproval ? 1 : 0);
            ps.setInt(2, groupId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set require approval", e);
        }
    }

    /**
     * <hr>
     * Adds a user to a group as a regular member.
     *
     * <p>Creates a group membership record with 'member' role and current
     * timestamp. Used for direct group additions without join requests.
     *
     * @param groupId the ID of the group to add the user to
     * @param user the User object to add to the group
     * @return true if the user was successfully added, false otherwise
     */
    @Override
    public boolean addToGroup(int groupId, User user) {
        if (user == null) return false;
        return addMemberWithRole(groupId, user.getUserId(), "member");
    }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Deletes a group from the database using Group object.
     *
     * <p>Removes the group entity and all associated membership records
     * and join requests through cascading delete operations.
     *
     * @param group the Group object to delete
     * @return true if the group was successfully deleted, false otherwise
     */
    @Override
    public boolean deleteGroup(Group group) {
        String sql = "DELETE FROM groups WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, group.getID());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete group", e);
        }
    }

    /**
     * <hr>
     * Deletes a group from the database using group ID.
     *
     * <p>Removes the group entity by its primary key, including all
     * associated membership records and join requests.
     *
     * @param groupid the ID of the group to delete
     * @return true if the group was successfully deleted, false otherwise
     */
    @Override
    public boolean deleteGroup(int groupid) {
        String sql = "DELETE FROM groups WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupid);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete group", e);
        }
    }

    /**
     * <hr>
     * Removes a member from a group (admin operation).
     *
     * <p>Allows group admins to remove members from the group. Verifies
     * that the acting user has admin privileges before permitting the removal.
     *
     * @param groupId the ID of the group to remove the member from
     * @param userIdToRemove the ID of the user to remove from the group
     * @param adminUserId the ID of the admin user performing the removal
     * @return true if the member was successfully removed, false otherwise
     */
    @Override
    public boolean removeMember(int groupId, String userIdToRemove, String adminUserId) {
        if (!isAdmin(groupId, adminUserId)) return false;
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userIdToRemove);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove member", e);
        }
    }

    // -------------------- HELPERS / CHECKS --------------------

    /**
     * <hr>
     * Checks if a group with the specified name already exists.
     *
     * <p>Verifies group name uniqueness by checking for existing groups
     * with the same name in the database.
     *
     * @param name the group name to check for existence
     * @return true if a group with this name exists, false otherwise
     */
    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT 1 FROM groups WHERE name = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check group exists by name", e);
        }
    }

    /**
     * <hr>
     * Checks if a user belongs to any group in the system.
     *
     * <p>Determines whether the specified user is a member of at least
     * one group, regardless of their role in those groups.
     *
     * @param user the user to check for group membership
     * @return true if the user belongs to any group, false otherwise
     */
    @Override
    public boolean existstByUser(User user) {
        if (user == null) return false;
        String sql = "SELECT 1 FROM group_members WHERE user_id = ? LIMIT 1";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if user exists in any group", e);
        }
    }

    /**
     * <hr>
     * Checks if a group entity already exists in the database.
     *
     * <p>Uses the group's name to verify existence, typically used
     * before attempting to create a new group to prevent duplicates.
     *
     * @param group the Group object to check for existence
     * @return true if an equivalent group exists, false otherwise
     */
    @Override
    public boolean groupExists(Group group) {
        return existsByName(group.getName());
    }

    /**
     * <hr>
     * Checks if a user has admin privileges in a specific group.
     *
     * <p>Verifies whether the specified user has the 'admin' role
     * in the given group, used for permission checks.
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return true if the user is an admin in the group, false otherwise
     */
    @Override
    public boolean isAdmin(int groupId, String userId) {
        String sql = "SELECT 1 FROM group_members WHERE group_id = ? AND user_id = ? AND role = 'admin'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check admin status", e);
        }
    }

    /**
     * <hr>
     * Promotes a group member to admin role.
     *
     * <p>Elevates a regular member to admin status, granting them
     * administrative privileges. Requires the promoter to be an admin.
     *
     * @param groupId the ID of the group where the promotion occurs
     * @param userId the ID of the user to promote to admin
     * @param promoterUserId the ID of the admin user performing the promotion
     * @return true if the promotion was successful, false otherwise
     */
    @Override
    public boolean promoteToAdmin(int groupId, String userId, String promoterUserId) {
        if (!isAdmin(groupId, promoterUserId)) return false;
        String sql = "UPDATE group_members SET role = 'admin' WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to promote member", e);
        }
    }

    /**
     * <hr>
     * Demotes a group admin to regular member role.
     *
     * <p>Removes admin privileges from a user, reducing them to regular
     * member status. Requires the demoter to be an admin.
     *
     * @param groupId the ID of the group where the demotion occurs
     * @param userId the ID of the user to demote from admin
     * @param demoterUserId the ID of the admin user performing the demotion
     * @return true if the demotion was successful, false otherwise
     */
    @Override
    public boolean demoteAdmin(int groupId, String userId, String demoterUserId) {
        if (!isAdmin(groupId, demoterUserId)) return false;
        String sql = "UPDATE group_members SET role = 'member' WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to demote admin", e);
        }
    }

    /**
     * <hr>
     * Checks if a user has a pending join request for a group.
     *
     * <p>Verifies whether the specified user has already submitted a
     * join request that is still awaiting approval or rejection.
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return true if the user has a pending join request, false otherwise
     */
    @Override
    public boolean hasUserRequestedToJoin(int groupId, String userId) {
        String sql = "SELECT 1 FROM group_join_requests WHERE group_id = ? AND user_id = ? AND status = 'pending'";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check join request", e);
        }
    }

    /**
     * <hr>
     * Checks if a user is currently a member of a specific group.
     *
     * <p>Verifies active group membership regardless of the user's role
     * in the group (both admin and regular members return true).
     *
     * @param groupId the ID of the group to check
     * @param userId the ID of the user to check
     * @return true if the user is a member of the group, false otherwise
     */
    @Override
    public boolean isUserMemberOfGroup(int groupId, String userId) {
        String sql = "SELECT 1 FROM group_members WHERE group_id = ? AND user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) { return rs.next(); }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check if user is member", e);
        }
    }

    /**
     * <hr>
     * Creates a new join request for a user to a group.
     *
     * <p>Submits a join request with 'pending' status, allowing group
     * admins to review and approve or reject the request later.
     *
     * @param id the ID of the group to request joining
     * @param userId the ID of the user requesting to join
     * @throws SQLException if database operation fails
     */
    @Override
    public void createJoinRequest(int id, String userId) {
        String sql = "INSERT INTO group_join_requests (group_id, user_id, status, requested_at) VALUES (?, ?, 'pending', ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, userId);
            ps.setString(3, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create join request", e);
        }
    }

    // -------------------- MAP ROW --------------------

    /**
     * <hr>
     * Maps a database ResultSet row to a fully populated Group object.
     *
     * <p>Converts SQL result set data into a complete Group entity,
     * including loading all members with their respective roles and
     * resolving user entities from the user DAO.
     *
     * @param rs the ResultSet containing group data from the database
     * @return a fully populated Group object with members and roles
     * @throws SQLException if data extraction or member loading fails
     */
    private Group mapRowToGroup(ResultSet rs) throws SQLException {
        int groupId = rs.getInt("group_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        boolean requireApproval = rs.getInt("require_approval") == 1;
        String ownerName = rs.getString("owner");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));

        User owner = userDao.findUser("username", ownerName);

        Group group = new Group(name, description, requireApproval, owner, createdAt);
        group.setID(groupId);

        // Load members and their roles
        List<User> members = new ArrayList<>();
        String sql = "SELECT user_id, role FROM group_members WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet memberRs = ps.executeQuery()) {
                while (memberRs.next()) {
                    String userId = memberRs.getString("user_id");
                    String role = memberRs.getString("role");
                    User user = userDao.findUserById(userId);
                    if (user != null) {
                        members.add(user);
                        // Sync the role into the Group's memberRoles map
                        group.setMemberRole(userId, role);
                    }
                }
            }
        }
        group.setMembers(members);
        return group;
    }
}
