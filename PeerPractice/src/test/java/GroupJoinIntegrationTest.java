import com.cab302.peerpractice.Model.DAOs.GroupDAO;
import com.cab302.peerpractice.Model.DAOs.SessionCalendarDAO;
import com.cab302.peerpractice.Model.DAOs.SessionTaskDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.*;
import com.cab302.peerpractice.Model.Managers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class GroupJoinIntegrationTest {

    private UserDAO userDao;
    private GroupDAO groupDao;
    private GroupManager groupManager;
    private SessionCalendarManager sessionCalendarManager;
    private SessionTaskManager sessionTaskManager;
    private SessionTaskDAO sessionTaskStorage;
    
    private User alice;
    private User bob;
    private Group testGroup;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        groupDao = new GroupDAO(userDao);
        
        Notifier notifier = new Notifier(userDao, null);
        groupManager = new GroupManager(groupDao, notifier, userDao);
        
        var sessionStorage = new SessionCalendarDAO(userDao);
        sessionCalendarManager = new SessionCalendarManager(sessionStorage);
        var sessionManager = new SessionManager(sessionCalendarManager);
        sessionTaskStorage = new SessionTaskDAO(userDao);
        sessionTaskManager = new SessionTaskManager(sessionTaskStorage, sessionManager);
        sessionCalendarManager.setSessionTaskManager(sessionTaskManager);
        
        // Use unique usernames to avoid database pollution from previous test runs
        String timestamp = String.valueOf(System.currentTimeMillis());
        alice = new User("Alice", "Leader", "alice_join_" + timestamp, "alice.join" + timestamp + "@example.com", "hashedpass1", "QUT");
        bob = new User("Bob", "Member", "bob_join_" + timestamp, "bob.join" + timestamp + "@example.com", "hashedpass2", "QUT");
        
        userDao.addUser(alice);
        userDao.addUser(bob);
        
        testGroup = new Group("Join Test Group " + timestamp, "Testing group join functionality", false, alice, LocalDateTime.now());
        groupDao.addGroup(testGroup);
    }
    
    @AfterEach
    void tearDown() {
        sessionTaskStorage.clearAllTasks();
        sessionCalendarManager.clearAllSessions();
        
        try {
            groupDao.deleteGroup(testGroup);
            userDao.deleteUser(alice.getUserId());
            userDao.deleteUser(bob.getUserId());
        } catch (Exception e) {
        }
    }

    @Test
    void testUserCanJoinGroupAndAccessSessions() throws Exception {
        // Bob joins the group
        groupManager.joinGroup(testGroup, bob);
        
        // Verify Bob is now a member in the database
        assertTrue(groupDao.isUserMemberOfGroup(testGroup.getID(), bob.getUserId()));
        
        // Verify Bob can see the group in his group list
        List<Group> bobGroups = groupDao.searchByUser(bob);
        assertEquals(1, bobGroups.size());
        assertEquals(testGroup.getName(), bobGroups.get(0).getName());
        
        // Alice creates a session in the group
        Session groupSession = new Session("Group Study Session", alice,
                                         LocalDateTime.now().plusDays(1),
                                         LocalDateTime.now().plusDays(1).plusHours(2));
        groupSession.setGroup(testGroup);
        groupSession.addParticipant(alice);
        groupSession.addParticipant(bob);
        sessionCalendarManager.addSession(groupSession, testGroup);
        
        // Verify Bob can see the session
        List<Session> groupSessions = sessionCalendarManager.getSessionsForGroup(testGroup);
        assertEquals(1, groupSessions.size());
        assertEquals("Group Study Session", groupSessions.get(0).getTitle());
        
        // Bob creates a task in the session
        SessionTask bobTask = sessionTaskManager.createTask(
            groupSession.getSessionId(),
            "Bob's task in group session",
            LocalDateTime.now().plusDays(2),
            bob.getUserId(),
            bob.getUserId()
        );
        
        assertNotNull(bobTask);
        assertEquals("Bob's task in group session", bobTask.getTitle());
        
        // Verify task exists
        List<SessionTask> sessionTasks = sessionTaskManager.getSessionTasks(groupSession.getSessionId());
        assertEquals(1, sessionTasks.size());
        assertEquals(bob.getUserId(), sessionTasks.get(0).getAssigneeId());
    }

    @Test
    void testGroupMembershipPersistsAfterNavigation() throws Exception {
        // Bob joins the group
        groupManager.joinGroup(testGroup, bob);
        
        // Verify Bob is a member
        assertTrue(groupDao.isUserMemberOfGroup(testGroup.getID(), bob.getUserId()));
        
        // Simulate navigation away and back (reload user's groups)
        List<Group> bobGroupsAfterNavigation = groupDao.searchByUser(bob);
        assertEquals(1, bobGroupsAfterNavigation.size());
        assertEquals(testGroup.getName(), bobGroupsAfterNavigation.get(0).getName());
        
        // Verify Bob still has access to group functionality
        Group bobGroup = bobGroupsAfterNavigation.get(0);
        
        Session testSession = new Session("Navigation Test Session", alice,
                                        LocalDateTime.now().plusDays(1),
                                        LocalDateTime.now().plusDays(1).plusHours(1));
        testSession.setGroup(bobGroup);
        testSession.addParticipant(bob);
        sessionCalendarManager.addSession(testSession, bobGroup);
        
        // Bob should be able to create tasks
        assertDoesNotThrow(() -> {
            sessionTaskManager.createTask(
                testSession.getSessionId(),
                "Post-navigation task",
                LocalDateTime.now().plusDays(3),
                bob.getUserId(),
                bob.getUserId()
            );
        });
    }

    @Test
    void testApprovalRequiredGroups() throws Exception {
        // Create a group requiring approval
        String testTimestamp = String.valueOf(System.currentTimeMillis());
        Group approvalGroup = new Group("Approval Required Group " + testTimestamp, "Need approval to join", true, alice, LocalDateTime.now());
        groupDao.addGroup(approvalGroup);
        
        try {
            // Bob requests to join
            groupManager.joinGroup(approvalGroup, bob);
            
            // Bob should not be a member yet
            assertFalse(groupDao.isUserMemberOfGroup(approvalGroup.getID(), bob.getUserId()));
            
            // There should be a pending request
            assertTrue(groupDao.hasUserRequestedToJoin(approvalGroup.getID(), bob.getUserId()));
            
            // Get pending requests for admin
            List<GroupJoinRequest> pendingRequests = groupDao.getPendingJoinRequests(approvalGroup.getID());
            assertEquals(1, pendingRequests.size());
            assertEquals(bob.getUserId(), pendingRequests.get(0).getUserId());
            
            // Alice approves the request
            GroupJoinRequest request = pendingRequests.get(0);
            boolean approved = groupDao.processJoinRequest(request.getRequestId(), "approved", alice.getUserId());
            assertTrue(approved);
            
            // Now Bob should be a member
            assertTrue(groupDao.isUserMemberOfGroup(approvalGroup.getID(), bob.getUserId()));
            
            // Bob should see the group in his list
            List<Group> bobGroups = groupDao.searchByUser(bob);
            assertTrue(bobGroups.stream().anyMatch(g -> g.getName().equals("Approval Required Group " + testTimestamp)));
            
        } finally {
            groupDao.deleteGroup(approvalGroup);
        }
    }

    @Test
    void testGroupMemberRoles() throws Exception {
        // Bob joins as a regular member
        groupManager.joinGroup(testGroup, bob);
        
        // Check Bob's role
        String bobRole = groupDao.getUserRoleInGroup(testGroup.getID(), bob.getUserId());
        assertEquals("member", bobRole);
        
        // Check Alice's role (she's the owner, should be admin)
        String aliceRole = groupDao.getUserRoleInGroup(testGroup.getID(), alice.getUserId());
        assertEquals("admin", aliceRole);
        
        // Get all members
        List<GroupMemberEntity> members = groupDao.getGroupMembers(testGroup.getID());
        assertEquals(2, members.size());
        
        // Verify member details
        boolean foundAlice = false, foundBob = false;
        for (GroupMemberEntity member : members) {
            if (member.getUserId().equals(alice.getUserId())) {
                assertEquals("admin", member.getRole());
                foundAlice = true;
            } else if (member.getUserId().equals(bob.getUserId())) {
                assertEquals("member", member.getRole());
                foundBob = true;
            }
        }
        assertTrue(foundAlice && foundBob);
    }
}