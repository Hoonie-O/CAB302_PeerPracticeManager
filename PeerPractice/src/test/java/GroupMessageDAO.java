import com.cab302.peerpractice.Model.DAOs.GroupDAO;
import com.cab302.peerpractice.Model.DAOs.GroupMessageDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.GroupMessage;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;
import org.junit.jupiter.api.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupMessageDAOTest {

    private Connection connection;
    private GroupMessageDAO dao;

    private Group testGroup;

    @BeforeEach
    void setUp() throws SQLException {
        // fresh in-memory DB
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        SQLiteConnection.setInstance(connection);

        // init DAOs (will create tables in memory)
        UserDAO userDao = new UserDAO();
        GroupDAO groupDao = new GroupDAO(userDao);
        dao = new GroupMessageDAO();

        // seed user and group
        User alice = new User("Alice", "Wonder", "alice123", "alice@mail.com", "Password1!", "QUT");
        userDao.addUser(alice);

        testGroup = new Group("Study Group", "Group for testing", false,
                alice.getUsername(), LocalDateTime.now());
        groupDao.addGroup(testGroup);
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close(); // wipes the in-memory DB
        }
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
