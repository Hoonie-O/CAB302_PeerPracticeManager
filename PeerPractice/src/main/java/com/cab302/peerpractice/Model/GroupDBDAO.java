package com.cab302.peerpractice.Model;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class GroupDBDAO implements IGroupDAO {
    private final Connection connection;
    private final IUserDAO userDao;

    public GroupDBDAO(IUserDAO userDao) throws SQLException {
        this.connection = SQLiteConnection.getInstance();
        this.userDao = userDao;
        createTables();
    }

    private void createTables() throws SQLException {
        try (Statement st = connection.createStatement()) {
            // Create groups table
            st.execute("CREATE TABLE IF NOT EXISTS groups (" +
                    "group_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT NOT NULL UNIQUE, " +
                    "description TEXT DEFAULT '', " +
                    "require_approval INTEGER DEFAULT 0, " +
                    "owner TEXT NOT NULL, " +
                    "created_at TEXT NOT NULL" +
                    ")");

            // Create group_members table with roles
            st.execute("CREATE TABLE IF NOT EXISTS group_members (" +
                    "group_id INTEGER NOT NULL, " +
                    "user_id TEXT NOT NULL, " +
                    "role TEXT DEFAULT 'member' CHECK(role IN ('member', 'admin')), " +
                    "joined_at TEXT NOT NULL, " +
                    "PRIMARY KEY(group_id, user_id), " +
                    "FOREIGN KEY(group_id) REFERENCES groups(group_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");

            // Create group_join_requests table
            st.execute("CREATE TABLE IF NOT EXISTS group_join_requests (" +
                    "request_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "group_id INTEGER NOT NULL, " +
                    "user_id TEXT NOT NULL, " +
                    "status TEXT DEFAULT 'pending' CHECK(status IN ('pending', 'approved', 'rejected')), " +
                    "requested_at TEXT NOT NULL, " +
                    "processed_at TEXT, " +
                    "processed_by TEXT, " +
                    "FOREIGN KEY(group_id) REFERENCES groups(group_id) ON DELETE CASCADE, " +
                    "FOREIGN KEY(user_id) REFERENCES users(user_id) ON DELETE CASCADE" +
                    ")");
        }
    }

    private Group mapRowToGroup(ResultSet rs) throws SQLException {
        int groupId = rs.getInt("group_id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        boolean requireApproval = rs.getInt("require_approval") == 1;
        String owner = rs.getString("owner");
        LocalDateTime createdAt = LocalDateTime.parse(rs.getString("created_at"));

        Group group = new Group(name, description, requireApproval, owner, createdAt);
        group.setID(groupId);

        // Load members
        List<User> members = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT gm.user_id, gm.role FROM group_members gm WHERE gm.group_id = ?")) {
            ps.setInt(1, groupId);
            try (ResultSet memberRs = ps.executeQuery()) {
                while (memberRs.next()) {
                    String userId = memberRs.getString("user_id");
                    // String role = memberRs.getString("role");
                    try {
                        User user = userDao.findUserById(userId);
                        if (user != null) {
                            members.add(user);
                        }
                    } catch (SQLException ignored) {}
                }
            }
        }
        group.setMembers(members);

        return group;
    }

    @Override
    public int addGroup(Group group) {
        if (groupExists(group)) {
            return -1;
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO groups (name, description, require_approval, owner, created_at) VALUES (?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.setInt(3, group.isRequire_approval() ? 1 : 0);
            ps.setString(4, group.getOwner());
            ps.setString(5, group.getCreated_at().toString());

            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (keys.next()) {
                        int groupId = keys.getInt(1);
                        group.setID(groupId);

                        // Add owner as admin member
                        try {
                            System.out.println("[DEBUG] GroupDBDAO.addGroup -> Looking for owner: " + group.getOwner());
                            User owner = userDao.findUser("username", group.getOwner());
                            if (owner != null) {
                                System.out.println("[DEBUG] GroupDBDAO.addGroup -> Found owner: " + owner.getUserId() + " (" + owner.getUsername() + ")");
                                addMemberWithRole(groupId, owner.getUserId(), "admin");
                            } else {
                                System.out.println("[DEBUG] GroupDBDAO.addGroup -> Owner not found for username: " + group.getOwner());
                            }
                        } catch (SQLException e) {
                            System.err.println("[DEBUG] GroupDBDAO.addGroup -> SQLException adding owner as admin: " + e.getMessage());
                        }

                        return groupId;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error adding group: " + e.getMessage());
        }
        return -1;
    }

    @Override
    public boolean deleteGroup(Group group) {
        try (PreparedStatement ps = connection.prepareStatement("DELETE FROM groups WHERE group_id = ?")) {
            ps.setInt(1, group.getID());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting group: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateGroup(Group group) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE groups SET name = ?, description = ?, require_approval = ? WHERE group_id = ?")) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.setInt(3, group.isRequire_approval() ? 1 : 0);
            ps.setInt(4, group.getID());

            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating group: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Group> searchByUser(User user) {
        if (user == null) return new ArrayList<>();

        List<Group> groups = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT g.* FROM groups g JOIN group_members gm ON g.group_id = gm.group_id WHERE gm.user_id = ?")) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(mapRowToGroup(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching groups by user: " + e.getMessage());
        }
        return groups;
    }

    @Override
    public List<Group> searchByMembers(List<User> users) {
        if (users == null || users.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Group> groups = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT DISTINCT g.* FROM groups g " +
            "JOIN group_members gm ON g.group_id = gm.group_id " +
            "WHERE gm.user_id IN (");
        
        for (int i = 0; i < users.size(); i++) {
            if (i > 0) sql.append(",");
            sql.append("?");
        }
        sql.append(")");
        
        try (PreparedStatement ps = connection.prepareStatement(sql.toString())) {
            for (int i = 0; i < users.size(); i++) {
                ps.setString(i + 1, users.get(i).getUserId());
            }
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(mapRowToGroup(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        return groups;
    }

    @Override
    public List<Group> searchByName(String name) {
        List<Group> groups = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement("SELECT * FROM groups WHERE name LIKE ?")) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    groups.add(mapRowToGroup(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error searching groups by name: " + e.getMessage());
        }
        return groups;
    }

    @Override
    public boolean existsByName(String name) {
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM groups WHERE name = ?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if group exists by name: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existstByUser(User user) {
        if (user == null) return false;

        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM group_members WHERE user_id = ? LIMIT 1")) {
            ps.setString(1, user.getUserId());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user has groups: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<Group> getAllGroups() {
        List<Group> groups = new ArrayList<>();
        try (Statement st = connection.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM groups")) {
            while (rs.next()) {
                groups.add(mapRowToGroup(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all groups: " + e.getMessage());
        }
        return groups;
    }

    @Override
    public boolean setRequireApproval(int id, boolean require_approval) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE groups SET require_approval = ? WHERE group_id = ?")) {
            ps.setInt(1, require_approval ? 1 : 0);
            ps.setInt(2, id);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error setting require approval: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addToGroup(int id, User user) {
        return addMemberWithRole(id, user.getUserId(), "member");
    }

    public boolean addMemberWithRole(int groupId, String userId, String role) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT OR IGNORE INTO group_members (group_id, user_id, role, joined_at) VALUES (?, ?, ?, ?)")) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            ps.setString(3, role);
            ps.setString(4, LocalDateTime.now().toString());
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error adding member to group: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean groupExists(Group group) {
        return existsByName(group.getName());
    }

    // Admin functionality methods
    public boolean isAdmin(int groupId, String userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM group_members WHERE group_id = ? AND user_id = ? AND role = 'admin'")) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user is admin: " + e.getMessage());
            return false;
        }
    }

    public String getMemberRole(int groupId, String userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT role FROM group_members WHERE group_id = ? AND user_id = ?")) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                } else {
                    return "member"; // default role if not found
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting member role: " + e.getMessage());
            return "member"; // default role on error
        }
    }

    public boolean promoteToAdmin(int groupId, String userId, String promoterUserId) {
        if (!isAdmin(groupId, promoterUserId)) {
            return false; // Only admins can promote
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE group_members SET role = 'admin' WHERE group_id = ? AND user_id = ?")) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error promoting user to admin: " + e.getMessage());
            return false;
        }
    }

    public boolean removeMember(int groupId, String userIdToRemove, String adminUserId) {
        if (!isAdmin(groupId, adminUserId)) {
            return false; // Only admins can remove members
        }

        try (PreparedStatement ps = connection.prepareStatement(
                "DELETE FROM group_members WHERE group_id = ? AND user_id = ?")) {
            ps.setInt(1, groupId);
            ps.setString(2, userIdToRemove);
            int rowsAffected = ps.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error removing member from group: " + e.getMessage());
            return false;
        }
    }

    public int createJoinRequest(int groupId, String userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO group_join_requests (group_id, user_id, status, requested_at) VALUES (?, ?, 'pending', ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            ps.setString(3, LocalDateTime.now().toString());
            
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error creating join request: " + e.getMessage());
            return -1;
        }
    }

    public List<GroupJoinRequest> getPendingJoinRequests(int groupId) {
        List<GroupJoinRequest> requests = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT r.*, u.first_name, u.last_name, u.username, u.email " +
                "FROM group_join_requests r " +
                "JOIN users u ON r.user_id = u.user_id " +
                "WHERE r.group_id = ? AND r.status = 'pending' " +
                "ORDER BY r.requested_at ASC")) {
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
                    
                    User user = new User(
                        rs.getString("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        "",
                        ""
                    );
                    request.setUser(user);
                    requests.add(request);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting pending join requests: " + e.getMessage());
        }
        return requests;
    }

    public boolean processJoinRequest(int requestId, String status, String processedBy) {
        try (PreparedStatement ps = connection.prepareStatement(
                "UPDATE group_join_requests SET status = ?, processed_at = ?, processed_by = ? WHERE request_id = ?")) {
            ps.setString(1, status);
            ps.setString(2, LocalDateTime.now().toString());
            ps.setString(3, processedBy);
            ps.setInt(4, requestId);
            
            int rowsAffected = ps.executeUpdate();
            
            if (rowsAffected > 0 && "approved".equals(status)) {
                try (PreparedStatement getRequestPs = connection.prepareStatement(
                        "SELECT group_id, user_id FROM group_join_requests WHERE request_id = ?")) {
                    getRequestPs.setInt(1, requestId);
                    try (ResultSet rs = getRequestPs.executeQuery()) {
                        if (rs.next()) {
                            int groupId = rs.getInt("group_id");
                            String userId = rs.getString("user_id");
                            
                            try (PreparedStatement addMemberPs = connection.prepareStatement(
                                    "INSERT OR IGNORE INTO group_members (group_id, user_id, role, joined_at) VALUES (?, ?, 'member', ?)")) {
                                addMemberPs.setInt(1, groupId);
                                addMemberPs.setString(2, userId);
                                addMemberPs.setString(3, LocalDateTime.now().toString());
                                addMemberPs.executeUpdate();
                            }
                        }
                    }
                }
            }
            
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error processing join request: " + e.getMessage());
            return false;
        }
    }

    public boolean hasUserRequestedToJoin(int groupId, String userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM group_join_requests WHERE group_id = ? AND user_id = ? AND status = 'pending'")) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user requested to join: " + e.getMessage());
            return false;
        }
    }

    public boolean isUserMemberOfGroup(int groupId, String userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT 1 FROM group_members WHERE group_id = ? AND user_id = ?")) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user is member: " + e.getMessage());
            return false;
        }
    }

    public String getUserRoleInGroup(int groupId, String userId) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT role FROM group_members WHERE group_id = ? AND user_id = ?")) {
            ps.setInt(1, groupId);
            ps.setString(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("role");
                }
                return null;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user role: " + e.getMessage());
            return null;
        }
    }

    public List<GroupMemberEntity> getGroupMembers(int groupId) {
        List<GroupMemberEntity> members = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT gm.*, u.first_name, u.last_name, u.username, u.email " +
                "FROM group_members gm " +
                "JOIN users u ON gm.user_id = u.user_id " +
                "WHERE gm.group_id = ? " +
                "ORDER BY gm.role DESC, gm.joined_at ASC")) {
            ps.setInt(1, groupId);
            
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    GroupMemberEntity member = new GroupMemberEntity();
                    member.setGroupId(rs.getInt("group_id"));
                    member.setUserId(rs.getString("user_id"));
                    member.setRole(rs.getString("role"));
                    member.setJoinedAt(LocalDateTime.parse(rs.getString("joined_at")));
                    
                    User user = new User(
                        rs.getString("user_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("username"),
                        rs.getString("email"),
                        "",
                        ""
                    );
                    member.setUser(user);
                    members.add(member);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting group members: " + e.getMessage());
        }
        return members;
    }
}