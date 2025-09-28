import com.cab302.peerpractice.Model.daos.GroupDAO;
import com.cab302.peerpractice.Model.daos.GroupMessageDAO;
import com.cab302.peerpractice.Model.daos.UserDAO;
import com.cab302.peerpractice.Model.entities.Group;
import com.cab302.peerpractice.Model.entities.GroupMessage;
import com.cab302.peerpractice.Model.entities.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupMessageDAOTest {

    private GroupMessageDAO dao;
    private UserDAO userDao;
    private GroupDAO groupDao;

    private User alice;
    private Group testGroup;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        groupDao = new GroupDAO(userDao);
        dao = new GroupMessageDAO();

        // Clean DB
        dao.getAllMessages().forEach(m -> dao.deleteMessage(m.getMessageId()));
        groupDao.getAllGroups().forEach(g -> {
            try { groupDao.deleteGroup(g.getID()); } catch (Exception ignored) {}
        });
        userDao.getAllUsers().forEach(u -> {
            try { userDao.deleteUser(u.getUserId()); } catch (Exception ignored) {}
        });

        // Seed user and group
        alice = new User("Alice", "Wonder", "alice123", "alice@mail.com", "Password1!", "QUT");
        userDao.addUser(alice);

        testGroup = new Group("Study Group", "Group for testing", false, alice.getUsername(), LocalDateTime.now());
        groupDao.addGroup(testGroup);
    }

    @AfterEach
    void tearDown() {
        dao.getAllMessages().forEach(m -> dao.deleteMessage(m.getMessageId()));
        try {
            groupDao.deleteGroup(testGroup.getID());
            userDao.deleteUser(alice.getUserId());
        } catch (Exception ignored) {}
    }

    @Test
    void addAndGetMessageById() {
        GroupMessage msg = new GroupMessage("1", "alice123", "Hello group!", LocalDateTime.now(), testGroup.getID());
        assertTrue(dao.addMessage(msg));

        GroupMessage retrieved = dao.getMessageById("1");
        assertNotNull(retrieved);
        assertEquals("Hello group!", retrieved.getContent());
        assertEquals(testGroup.getID(), retrieved.getGroupId());
    }

    @Test
    void getAllMessages_returnsOrderedByTimestamp() {
        GroupMessage m1 = new GroupMessage("1", "alice123", "First", LocalDateTime.now().minusMinutes(5), testGroup.getID());
        GroupMessage m2 = new GroupMessage("2", "alice123", "Second", LocalDateTime.now(), testGroup.getID());
        dao.addMessage(m1);
        dao.addMessage(m2);

        List<GroupMessage> all = dao.getAllMessages();
        assertEquals(2, all.size());
        assertEquals("First", all.get(0).getContent()); // ordered ascending
    }

    @Test
    void getMessagesForGroup_onlyThatGroup() {
        GroupMessage msg1 = new GroupMessage("1", "alice123", "Message 1", LocalDateTime.now(), testGroup.getID());
        dao.addMessage(msg1);

        List<GroupMessage> groupMsgs = dao.getMessagesForGroup(testGroup.getID());
        assertEquals(1, groupMsgs.size());
        assertEquals("Message 1", groupMsgs.get(0).getContent());

        // Non-existent group should return empty
        assertTrue(dao.getMessagesForGroup(99999).isEmpty());
    }

    @Test
    void deleteMessage_removesFromDb() {
        GroupMessage msg = new GroupMessage("1", "alice123", "Bye group", LocalDateTime.now(), testGroup.getID());
        dao.addMessage(msg);

        assertTrue(dao.deleteMessage("1"));
        assertNull(dao.getMessageById("1"));
        assertFalse(dao.deleteMessage("1")); // already gone
    }

    @Test
    void deleteMessagesForGroup_removesAll() {
        GroupMessage m1 = new GroupMessage("1", "alice123", "First", LocalDateTime.now(), testGroup.getID());
        GroupMessage m2 = new GroupMessage("2", "alice123", "Second", LocalDateTime.now(), testGroup.getID());
        dao.addMessage(m1);
        dao.addMessage(m2);

        assertTrue(dao.deleteMessagesForGroup(testGroup.getID()));

        List<GroupMessage> remaining = dao.getAllMessages();
        assertTrue(remaining.isEmpty());
    }
}
