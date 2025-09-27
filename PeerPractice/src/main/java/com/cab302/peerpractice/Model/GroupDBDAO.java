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

    // -------------------- TABLE CREATION --------------------
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
    @Override
    public int addGroup(Group group) {
        if (groupExists(group)) return -1;
        String sql = "INSERT INTO groups (name, description, require_approval, owner, created_at) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, group.getName());
            ps.setString(2, group.getDescription());
            ps.setInt(3, group.isRequire_approval() ? 1 : 0);
            ps.setString(4, group.getOwner());
            ps.setString(5, group.getCreated_at().toString());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int groupId = keys.getInt(1);
                    group.setID(groupId);
                    User owner = userDao.findUser("username", group.getOwner());
                    if (owner != null) addMemberWithRole(groupId, owner.getUserId(), "admin");
                    return groupId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add group", e);
        }
        return -1;
    }

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

    // -------------------- UPDATE --------------------
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

    @Override
    public boolean addToGroup(int groupId, User user) {
        if (user == null) return false;
        return addMemberWithRole(groupId, user.getUserId(), "member");
    }

    // -------------------- DELETE --------------------
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

    @Override
    public boolean groupExists(Group group) {
        return existsByName(group.getName());
    }

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

    @Override
    public void createJoinRequest(int id, String userId) {

    }

    // -------------------- MAP ROW --------------------
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
        String sql = "SELECT user_id FROM group_members WHERE group_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            try (ResultSet memberRs = ps.executeQuery()) {
                while (memberRs.next()) {
                    String userId = memberRs.getString("user_id");
                    User user = userDao.findUserById(userId);
                    if (user != null) members.add(user);
                }
            }
        }
        group.setMembers(members);
        return group;
    }
}
