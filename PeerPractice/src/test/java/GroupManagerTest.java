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

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class GroupManagerTest {

    private GroupManager groupManager;
    private IGroupDAO groupDAO;
    private Notifier notifier;
    private IUserDAO userDAO;
    private User user;
    private String NAME;
    private String DESCRIPTION = "This is the description of a group";
    private String USERNAME;
    private Group group;

    @BeforeEach
    public void setUp() throws SQLException {
        // Use mocks
        userDAO = new MockUserDAO();
        groupDAO = new MockGroupDAO(userDAO);
        notifier = new Notifier(userDAO);
        groupManager = new GroupManager(groupDAO, notifier, userDAO);

        // Use unique values to avoid clashes across runs
        String ts = String.valueOf(System.currentTimeMillis());
        USERNAME = "sati2030_" + ts;
        NAME = "Group1_" + ts;

        user = new User("Seiji", "Sato", USERNAME, "email" + ts + "@email.com", "masfsa", "qut");
        userDAO.addUser(user);

        group = new Group(NAME, DESCRIPTION, false, user.getUsername(), LocalDateTime.now());
        groupDAO.addGroup(group);
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
        Group duplicate = new Group(NAME, DESCRIPTION, false, USERNAME, LocalDateTime.now());
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
            assertEquals(user.getUsername(), retrievedGroup.getOwner());
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
            groupManager.requireApproval(group, true);
            assertTrue(group.isRequire_approval());
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void testAddMemberNormal() {
        User user1 = new User("Cristiano", "Ronaldo", "cristiano7", "cr7@email.com", "asfasfaf", "QUT");
        userDAO.addUser(user1);
        assertDoesNotThrow(() -> groupManager.addMember(group, user, "cristiano7"));
    }

    @Test
    void testAddMemberNonExistent() {
        assertThrows(UserNotFoundException.class, () -> groupManager.addMember(group, user, "someone"));
    }

    @Test
    void testAddMemberNotOwner() {
        User user1 = new User("Cristiano", "Ronaldo", "CRistiano7", "cr7@email.com", "asfsfafa", "QUT");
        assertThrows(InsufficientPermissionsException.class, () -> groupManager.addMember(group, user1, USERNAME));
    }

    @Test
    void testAddMemberNullOwner() {
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(group, null, "someone"));
    }

    @Test
    void testAddMemberNullGroup() {
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(null, user, "someone"));
    }

    @Test
    void testAddMemberNullToAdd() {
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(group, user, null));
    }

    @Test
    void testAddMemberEmptyToAdd() {
        assertThrows(IllegalArgumentException.class, () -> groupManager.addMember(group, user, ""));
    }
}
