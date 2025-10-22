import com.cab302.peerpractice.Exceptions.DuplicateGroupException;
import com.cab302.peerpractice.Exceptions.InsufficientPermissionsException;
import com.cab302.peerpractice.Exceptions.UserNotFoundException;
import com.cab302.peerpractice.Model.DAOs.*;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.GroupManager;
import com.cab302.peerpractice.Model.Managers.Notifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupManagerTest {

    private GroupManager groupManager;
    private IGroupDAO groupDAO;
    private IUserDAO userDAO;
    private User user;
    private String NAME;
    private final String DESCRIPTION = "This is the description of a group";
    private String USERNAME;
    private Group group;

    @BeforeEach
    public void setUp() throws SQLException {
        // Use mocks
        userDAO = new MockUserDAO();
        groupDAO = new MockGroupDAO(userDAO);

        // Clear mock state to prevent pollution between tests
        if (groupDAO instanceof MockGroupDAO) {
            ((MockGroupDAO) groupDAO).clear();
        }
        if (userDAO instanceof MockUserDAO) {
            ((MockUserDAO) userDAO).clear();
        }

        Notifier notifier = new Notifier(userDAO, null);
        groupManager = new GroupManager(groupDAO, notifier, userDAO);

        // Use unique values to avoid clashes across runs
        String ts = String.valueOf(System.currentTimeMillis());
        USERNAME = "sati2030_" + ts;
        NAME = "Group1_" + ts;

        user = new User("Seiji", "Sato", USERNAME, "email" + ts + "@email.com", "masfsa", "qut");
        userDAO.addUser(user);

        // Don't create the group in setup - let individual tests create it
        group = new Group(NAME, DESCRIPTION, false, user, LocalDateTime.now());
    }

    @Test
    void testCreateGroupNormal() {
        assertDoesNotThrow(() -> groupManager.createGroup(NAME, DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupNullCreator() {
        assertThrows(IllegalArgumentException.class, () -> groupManager.createGroup(NAME, DESCRIPTION, false, null));
    }

    @Test
    void testCreateGroupNullName() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(null, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupBlankName() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup("   ", DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupNameTooLong() {
        String tooLong = "ABCDEFGHIJKLMNOPQRSTU"; // 21 chars
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(tooLong, DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupInvalidChars() {
        String invalid = "Bad@Name!";
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(invalid, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupExactly20Chars() {
        String twenty = "ABCDEFGHIJKLMNOPQRST"; // 20 chars
        assertDoesNotThrow(() ->
                groupManager.createGroup(twenty, DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupValidChars() {
        String valid = "Team_Name-1.2 test";
        assertDoesNotThrow(() ->
                groupManager.createGroup(valid, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupNullDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(NAME, null, true, user));
    }

    @Test
    void testCreateGroupBlankDescription() {
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(NAME, "   ", false, user));
    }

    @Test
    void testCreateGroupDescriptionTooLong() {
        String tooLong = "a".repeat(201); // 201 chars
        assertThrows(IllegalArgumentException.class,
                () -> groupManager.createGroup(NAME, tooLong, false, user));
    }

    @Test
    void testCreateGroupDescriptionExactly200Chars() {
        String twoHundred = "a".repeat(200);
        assertDoesNotThrow(() ->
                groupManager.createGroup(NAME, twoHundred, true, user));
    }

    @Test
    void testCreateGroupValidDescription() {
        assertDoesNotThrow(() ->
                groupManager.createGroup(NAME, DESCRIPTION, true, user));
    }

    @Test
    void testCreateGroupDuplicateGroup() throws SQLException {
        Group duplicate = new Group(NAME, DESCRIPTION, false, user, LocalDateTime.now());
        groupDAO.addGroup(duplicate);
        assertThrows(DuplicateGroupException.class,
                () -> groupManager.createGroup(NAME, DESCRIPTION, false, user));
    }

    @Test
    void testCreateGroupReflectedInDAO() {
        try {
            groupManager.createGroup(NAME, DESCRIPTION, false, user);
            List<Group> groups = groupDAO.getAllGroups();
            Group retrievedGroup = groups.get(0);
            assertEquals(NAME, retrievedGroup.getName());
            assertEquals(DESCRIPTION, retrievedGroup.getDescription());
            assertFalse(retrievedGroup.isRequire_approval());
            assertEquals(user.getUsername(), retrievedGroup.getOwner().getUsername());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testCreateGroupContainsCreator() {
        try {
            groupManager.createGroup(NAME, DESCRIPTION, false, user);
            List<Group> groups = groupDAO.getAllGroups();
            Group g = groups.get(0);
            assertTrue(g.getMembers().contains(user));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testRequireApprovalNormal() {
        try {
            groupManager.createGroup(NAME, DESCRIPTION, false, user);
            // Find the created group to get its ID
            List<Group> groups = groupDAO.getAllGroups();
            Group createdGroup = groups.stream()
                .filter(g -> g.getName().equals(NAME))
                .findFirst()
                .orElse(null);
            assertNotNull(createdGroup);

            groupManager.requireApproval(createdGroup, true);
            assertTrue(createdGroup.isRequire_approval());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testAddMemberNormal() {
        try {
            groupManager.createGroup(NAME, DESCRIPTION, false, user);
            List<Group> groups = groupDAO.getAllGroups();
            Group createdGroup = groups.get(0);

            User user1 = new User("Cristiano", "Ronaldo", "cristiano7", "cr7@email.com", "asfasfaf", "QUT");
            userDAO.addUser(user1);
            assertDoesNotThrow(() -> groupManager.addMember(createdGroup, user, "cristiano7"));
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testAddMemberNonExistent() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);
        assertThrows(UserNotFoundException.class, () -> groupManager.addMember(createdGroup, user, "someone"));
    }

    @Test
    void testAddMemberNotOwner() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);
        User user1 = new User("Cristiano", "Ronaldo", "CRistiano7", "cr7@email.com", "asfsfafa", "QUT");
        assertThrows(InsufficientPermissionsException.class, () -> groupManager.addMember(createdGroup, user1, USERNAME));
    }

    @Test
    void testAddMemberNullOwner() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(createdGroup, null, "someone"));
    }

    @Test
    void testAddMemberNullGroup() {
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(null, user, "someone"));
    }

    @Test
    void testAddMemberNullToAdd() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(createdGroup, user, null));
    }

    @Test
    void testAddMemberEmptyToAdd() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(createdGroup, user, ""));
    }

    // ========== Admin Role Management Tests ==========

    @Test
    void testOwnerIsAdminByDefault() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        // Owner should be admin by default
        assertTrue(groupManager.isAdmin(createdGroup, user));
    }

    @Test
    void testPromoteToAdminSuccess() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User member = new User("Jane", "Doe", "janedoe", "jane@email.com", "pass123", "QUT");
        userDAO.addUser(member);
        groupDAO.addToGroup(createdGroup.getID(), member);

        // Member should not be admin initially
        assertFalse(groupManager.isAdmin(createdGroup, member));

        // Owner promotes member to admin
        assertDoesNotThrow(() -> groupManager.promoteToAdmin(createdGroup, user, member));

        // Now member should be admin
        assertTrue(groupManager.isAdmin(createdGroup, member));
    }

    @Test
    void testPromoteToAdminNonAdminCannotPromote() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User member1 = new User("Jane", "Doe", "janedoe", "jane@email.com", "pass123", "QUT");
        User member2 = new User("John", "Smith", "johnsmith", "john@email.com", "pass456", "QUT");
        userDAO.addUser(member1);
        userDAO.addUser(member2);
        groupDAO.addToGroup(createdGroup.getID(), member1);
        groupDAO.addToGroup(createdGroup.getID(), member2);

        // Regular member tries to promote another member
        assertThrows(InsufficientPermissionsException.class,
            () -> groupManager.promoteToAdmin(createdGroup, member1, member2));
    }

    @Test
    void testPromoteToAdminNullChecks() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User member = new User("Jane", "Doe", "janedoe", "jane@email.com", "pass123", "QUT");
        userDAO.addUser(member);

        assertThrows(IllegalArgumentException.class,
            () -> groupManager.promoteToAdmin(null, user, member));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.promoteToAdmin(createdGroup, null, member));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.promoteToAdmin(createdGroup, user, null));
    }

    @Test
    void testAdminCanPromoteOthers() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User admin1 = new User("Admin", "One", "admin1", "admin1@email.com", "pass123", "QUT");
        User member = new User("Regular", "Member", "member1", "member1@email.com", "pass456", "QUT");
        userDAO.addUser(admin1);
        userDAO.addUser(member);
        groupDAO.addToGroup(createdGroup.getID(), admin1);
        groupDAO.addToGroup(createdGroup.getID(), member);

        // Owner promotes admin1
        groupManager.promoteToAdmin(createdGroup, user, admin1);

        // admin1 should be able to promote member
        assertDoesNotThrow(() -> groupManager.promoteToAdmin(createdGroup, admin1, member));
        assertTrue(groupManager.isAdmin(createdGroup, member));
    }

    @Test
    void testIsAdminReturnsFalseForNonMember() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User nonMember = new User("Not", "Member", "notmember", "not@email.com", "pass123", "QUT");
        userDAO.addUser(nonMember);

        assertFalse(groupManager.isAdmin(createdGroup, nonMember));
    }

    @Test
    void testIsAdminNullSafety() {
        assertFalse(groupManager.isAdmin(null, user));
        assertFalse(groupManager.isAdmin(group, null));
        assertFalse(groupManager.isAdmin(null, null));
    }

    // ========== Demote Admin Tests ==========

    @Test
    void testDemoteAdminSuccess() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User admin = new User("Admin", "User", "adminuser", "admin@email.com", "pass123", "QUT");
        userDAO.addUser(admin);
        groupDAO.addToGroup(createdGroup.getID(), admin);

        // Promote to admin first
        groupManager.promoteToAdmin(createdGroup, user, admin);
        assertTrue(groupManager.isAdmin(createdGroup, admin));

        // Now demote
        assertDoesNotThrow(() -> groupManager.demoteAdmin(createdGroup, user, admin));
        assertFalse(groupManager.isAdmin(createdGroup, admin));
    }

    @Test
    void testCannotDemoteOwner() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        // Try to demote the owner
        assertThrows(InsufficientPermissionsException.class,
            () -> groupManager.demoteAdmin(createdGroup, user, user));
    }

    @Test
    void testNonAdminCannotDemote() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User admin = new User("Admin", "User", "adminuser", "admin@email.com", "pass123", "QUT");
        User member = new User("Regular", "Member", "member1", "member1@email.com", "pass456", "QUT");
        userDAO.addUser(admin);
        userDAO.addUser(member);
        groupDAO.addToGroup(createdGroup.getID(), admin);
        groupDAO.addToGroup(createdGroup.getID(), member);

        // Promote admin
        groupManager.promoteToAdmin(createdGroup, user, admin);

        // Regular member tries to demote admin
        assertThrows(InsufficientPermissionsException.class,
            () -> groupManager.demoteAdmin(createdGroup, member, admin));
    }

    @Test
    void testDemoteAdminNullChecks() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User admin = new User("Admin", "User", "adminuser", "admin@email.com", "pass123", "QUT");
        userDAO.addUser(admin);

        assertThrows(IllegalArgumentException.class,
            () -> groupManager.demoteAdmin(null, user, admin));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.demoteAdmin(createdGroup, null, admin));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.demoteAdmin(createdGroup, user, null));
    }

    // ========== Kick Member Tests ==========

    @Test
    void testKickMemberSuccess() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User member = new User("Jane", "Doe", "janedoe", "jane@email.com", "pass123", "QUT");
        userDAO.addUser(member);
        groupDAO.addToGroup(createdGroup.getID(), member);

        // Verify member is in group
        assertTrue(groupDAO.isUserMemberOfGroup(createdGroup.getID(), member.getUserId()));

        // Owner kicks member
        assertDoesNotThrow(() -> groupManager.kickMember(createdGroup, user, member));

        // Member should no longer be in group
        assertFalse(groupDAO.isUserMemberOfGroup(createdGroup.getID(), member.getUserId()));
    }

    @Test
    void testKickMemberNonAdminCannotKick() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User member1 = new User("Jane", "Doe", "janedoe", "jane@email.com", "pass123", "QUT");
        User member2 = new User("John", "Smith", "johnsmith", "john@email.com", "pass456", "QUT");
        userDAO.addUser(member1);
        userDAO.addUser(member2);
        groupDAO.addToGroup(createdGroup.getID(), member1);
        groupDAO.addToGroup(createdGroup.getID(), member2);

        // Regular member tries to kick another member
        assertThrows(InsufficientPermissionsException.class,
            () -> groupManager.kickMember(createdGroup, member1, member2));
    }

    @Test
    void testCannotKickOwner() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User admin = new User("Admin", "User", "adminuser", "admin@email.com", "pass123", "QUT");
        userDAO.addUser(admin);
        groupDAO.addToGroup(createdGroup.getID(), admin);
        groupManager.promoteToAdmin(createdGroup, user, admin);

        // Even another admin cannot kick the owner
        assertThrows(InsufficientPermissionsException.class,
            () -> groupManager.kickMember(createdGroup, admin, user));
    }

    @Test
    void testKickMemberNullChecks() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User member = new User("Jane", "Doe", "janedoe", "jane@email.com", "pass123", "QUT");
        userDAO.addUser(member);

        assertThrows(IllegalArgumentException.class,
            () -> groupManager.kickMember(null, user, member));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.kickMember(createdGroup, null, member));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.kickMember(createdGroup, user, null));
    }

    @Test
    void testAdminCanKickRegularMembers() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User admin = new User("Admin", "User", "adminuser", "admin@email.com", "pass123", "QUT");
        User member = new User("Regular", "Member", "member1", "member1@email.com", "pass456", "QUT");
        userDAO.addUser(admin);
        userDAO.addUser(member);
        groupDAO.addToGroup(createdGroup.getID(), admin);
        groupDAO.addToGroup(createdGroup.getID(), member);

        // Promote admin
        groupManager.promoteToAdmin(createdGroup, user, admin);

        // Admin kicks member
        assertDoesNotThrow(() -> groupManager.kickMember(createdGroup, admin, member));
        assertFalse(groupDAO.isUserMemberOfGroup(createdGroup.getID(), member.getUserId()));
    }

    // ========== Delete Group Tests ==========

    @Test
    void testDeleteGroupSuccess() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        // Verify group exists
        assertNotNull(groupDAO.searchByID(createdGroup.getID()));

        // Owner deletes group
        assertDoesNotThrow(() -> groupManager.deleteGroup(createdGroup, user));

        // Group should no longer exist
        assertNull(groupDAO.searchByID(createdGroup.getID()));
    }

    @Test
    void testDeleteGroupOnlyOwnerCan() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, false, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User admin = new User("Admin", "User", "adminuser", "admin@email.com", "pass123", "QUT");
        userDAO.addUser(admin);
        groupDAO.addToGroup(createdGroup.getID(), admin);
        groupManager.promoteToAdmin(createdGroup, user, admin);

        // Even an admin cannot delete the group, only owner can
        assertThrows(InsufficientPermissionsException.class,
            () -> groupManager.deleteGroup(createdGroup, admin));

        // Group should still exist
        assertNotNull(groupDAO.searchByID(createdGroup.getID()));
    }

    @Test
    void testDeleteGroupNullChecks() {
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.deleteGroup(null, user));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.deleteGroup(group, null));
    }

    // ========== Process Join Request Tests ==========

    @Test
    void testProcessJoinRequestApprove() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, true, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User requester = new User("Request", "User", "requester", "req@email.com", "pass123", "QUT");
        userDAO.addUser(requester);

        // User requests to join
        groupManager.joinGroup(createdGroup, requester);

        // Get the join request ID
        List<com.cab302.peerpractice.Model.Entities.GroupJoinRequest> requests = groupDAO.getPendingJoinRequests(createdGroup.getID());
        assertEquals(1, requests.size());
        int requestId = requests.get(0).getRequestId();

        // Verify user is not a member yet
        assertFalse(groupDAO.isUserMemberOfGroup(createdGroup.getID(), requester.getUserId()));

        // Admin approves
        assertDoesNotThrow(() -> groupManager.processJoinRequest(createdGroup, user, requestId, true));

        // User should now be a member
        assertTrue(groupDAO.isUserMemberOfGroup(createdGroup.getID(), requester.getUserId()));
    }

    @Test
    void testProcessJoinRequestDeny() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, true, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User requester = new User("Request", "User", "requester", "req@email.com", "pass123", "QUT");
        userDAO.addUser(requester);

        // User requests to join
        groupManager.joinGroup(createdGroup, requester);

        // Get the join request ID
        List<com.cab302.peerpractice.Model.Entities.GroupJoinRequest> requests = groupDAO.getPendingJoinRequests(createdGroup.getID());
        assertEquals(1, requests.size());
        int requestId = requests.get(0).getRequestId();

        // Admin denies - should not throw but user won't be added
        assertDoesNotThrow(() -> groupManager.processJoinRequest(createdGroup, user, requestId, false));

        // User should not be a member
        assertFalse(groupDAO.isUserMemberOfGroup(createdGroup.getID(), requester.getUserId()));
    }

    @Test
    void testProcessJoinRequestNonAdminCannotProcess() throws Exception {
        groupManager.createGroup(NAME, DESCRIPTION, true, user);
        Group createdGroup = groupDAO.getAllGroups().get(0);

        User member = new User("Regular", "Member", "member1", "member1@email.com", "pass456", "QUT");
        User requester = new User("Request", "User", "requester", "req@email.com", "pass123", "QUT");
        userDAO.addUser(member);
        userDAO.addUser(requester);
        groupDAO.addToGroup(createdGroup.getID(), member);

        // User requests to join
        groupManager.joinGroup(createdGroup, requester);

        // Get the join request ID
        List<com.cab302.peerpractice.Model.Entities.GroupJoinRequest> requests = groupDAO.getPendingJoinRequests(createdGroup.getID());
        assertEquals(1, requests.size());
        int requestId = requests.get(0).getRequestId();

        // Regular member tries to process request
        assertThrows(InsufficientPermissionsException.class,
            () -> groupManager.processJoinRequest(createdGroup, member, requestId, true));
    }

    @Test
    void testProcessJoinRequestNullChecks() {
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.processJoinRequest(null, user, 1, true));
        assertThrows(IllegalArgumentException.class,
            () -> groupManager.processJoinRequest(group, null, 1, true));
    }
}
