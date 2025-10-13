import com.cab302.peerpractice.Model.DAOs.GroupDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupJoinRequest;
import com.cab302.peerpractice.Model.Entities.GroupMemberEntity;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupDAOTest {

    private Connection memConn;
    private UserDAO userDao;
    private GroupDAO groupDao;

    private User alice;
    private User bob;

    @BeforeEach
    void setUp() throws SQLException {
        // In-memory DB
        memConn = DriverManager.getConnection("jdbc:sqlite::memory:");
        SQLiteConnection.setInstance(memConn);

        userDao = new UserDAO();
        groupDao = new GroupDAO(userDao);

        // Seed users
        alice = new User("1", "Alice", "Wonder", "alice123",
                "alice@mail.com", "Password1!", "QUT");
        bob = new User("2", "Bob", "Builder", "bob123",
                "bob@mail.com", "Password2!", "QUT");

        userDao.addUser(alice);
        userDao.addUser(bob);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (memConn != null && !memConn.isClosed()) {
            memConn.close();
        }
    }

    @Test
    void addGroup_assignsIdAndOwnerIsAdmin() {
        Group g = new Group("Study Group", "Testing group", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        assertTrue(groupId > 0);
        assertEquals(groupId, g.getID());
        assertTrue(groupDao.isAdmin(groupId, alice.getUserId()));
    }

    @Test
    void searchById_returnsCorrectGroup() {
        Group g = new Group("Math Group", "Desc", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        Group found = groupDao.searchByID(groupId);
        assertNotNull(found);
        assertEquals("Math Group", found.getName());
    }

    @Test
    void searchByUser_returnsGroupsWhereUserIsMember() {
        Group g = new Group("Physics Group", "Desc", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        assertTrue(groupDao.addToGroup(groupId, bob));

        List<Group> groupsForBob = groupDao.searchByUser(bob);
        assertEquals(1, groupsForBob.size());
        assertEquals("Physics Group", groupsForBob.get(0).getName());
    }

    @Test
    void searchByName_findsGroupsWithPartialMatch() {
        groupDao.addGroup(new Group("Chemistry Club", "Desc", false,
                alice, LocalDateTime.now()));
        groupDao.addGroup(new Group("Chess Club", "Desc", false,
                alice, LocalDateTime.now()));

        List<Group> results = groupDao.searchByName("Club");
        assertEquals(2, results.size());
    }

    @Test
    void updateGroup_changesNameAndDescription() {
        Group g = new Group("Old Name", "Old desc", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        g.setName("New Name");
        g.setDescription("Updated desc");
        assertTrue(groupDao.updateGroup(g));

        Group updated = groupDao.searchByID(groupId);
        assertEquals("New Name", updated.getName());
        assertEquals("Updated desc", updated.getDescription());
    }

    @Test
    void setRequireApproval_andExistsByName() {
        Group g = new Group("Approval Group", "Desc", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        assertTrue(groupDao.existsByName("Approval Group"));
        assertTrue(groupDao.setRequireApproval(groupId, true));

        Group updated = groupDao.searchByID(groupId);
        assertTrue(updated.isRequire_approval());
    }

    @Test
    void getGroupMembers_includesOwnerAndAddedMembers() {
        Group g = new Group("Music Group", "Desc", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        groupDao.addToGroup(groupId, bob);

        List<GroupMemberEntity> members = groupDao.getGroupMembers(groupId);
        assertEquals(2, members.size());
    }

    @Test
    void promoteAndRemoveMember_flow() {
        Group g = new Group("Sports Group", "Desc", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        groupDao.addToGroup(groupId, bob);

        assertTrue(groupDao.promoteToAdmin(groupId, bob.getUserId(), alice.getUserId()));
        assertTrue(groupDao.removeMember(groupId, bob.getUserId(), alice.getUserId()));
        assertFalse(groupDao.isUserMemberOfGroup(groupId, bob.getUserId()));
    }

    @Test
    void processJoinRequest_approvesAndAddsMember() {
        Group g = new Group("Joinable Group", "Desc", true,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        // Manually insert join request
        String sql = "INSERT INTO group_join_requests (group_id, user_id, status, requested_at) VALUES (?,?,?,?)";
        try (PreparedStatement ps = memConn.prepareStatement(sql)) {
            ps.setInt(1, groupId);
            ps.setString(2, bob.getUserId());
            ps.setString(3, "pending");
            ps.setString(4, LocalDateTime.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            fail("Failed to insert join request: " + e.getMessage());
        }

        List<GroupJoinRequest> pending = groupDao.getPendingJoinRequests(groupId);
        assertEquals(1, pending.size());

        GroupJoinRequest req = pending.get(0);
        assertTrue(groupDao.processJoinRequest(req.getRequestId(), "approved", alice.getUserId()));
        assertTrue(groupDao.isUserMemberOfGroup(groupId, bob.getUserId()));
    }

    @Test
    void deleteGroup_removesIt() {
        Group g = new Group("Delete Group", "Desc", false,
                alice, LocalDateTime.now());
        int groupId = groupDao.addGroup(g);

        assertTrue(groupDao.deleteGroup(groupId));
        assertNull(groupDao.searchByID(groupId));
    }
}
