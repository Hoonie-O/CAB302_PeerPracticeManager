import com.cab302.peerpractice.Model.DAOs.MockGroupMessageDAO;
import com.cab302.peerpractice.Model.Entities.GroupMessage;
import com.cab302.peerpractice.Model.Managers.GroupMessageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GroupMessageManagerTest {

    private GroupMessageManager manager;
    private MockGroupMessageDAO dao;

    @BeforeEach
    void setUp() {
        dao = new MockGroupMessageDAO();
        manager = new GroupMessageManager(dao);
    }

    @Test
    void sendValidMessage_storesMessage() {
        GroupMessage msg = new GroupMessage("1", "alice", "Welcome team", LocalDateTime.now(), 101);
        String id = manager.sendMessage(msg);

        assertEquals("1", id);
        assertNotNull(dao.getMessageById("1"));
    }

    @Test
    void sendMessage_invalidGroupId_throws() {
        GroupMessage msg = new GroupMessage("2", "bob", "Bad group", LocalDateTime.now(), -5);
        assertThrows(IllegalArgumentException.class, () -> manager.sendMessage(msg));
    }

    @Test
    void getMessages_returnsGroupMessages() {
        dao.addMessage(new GroupMessage("1", "alice", "Hey team", LocalDateTime.now(), 101));
        dao.addMessage(new GroupMessage("2", "bob", "Hello all", LocalDateTime.now(), 101));
        dao.addMessage(new GroupMessage("3", "carol", "Unrelated", LocalDateTime.now(), 202));

        List<GroupMessage> group101Msgs = manager.getMessages(101);
        assertEquals(2, group101Msgs.size());
        assertTrue(group101Msgs.stream().anyMatch(m -> m.getSenderId().equals("alice")));
    }

    @Test
    void deleteMessage_removesCorrectMessage() {
        GroupMessage msg = new GroupMessage("1", "alice", "To be deleted", LocalDateTime.now(), 101);
        dao.addMessage(msg);

        assertTrue(manager.deleteMessage("1"));
        assertNull(dao.getMessageById("1"));
    }

    @Test
    void getMessage_returnsMessageIfExists() {
        GroupMessage msg = new GroupMessage("77", "alice", "Hello", LocalDateTime.now(), 101);
        dao.addMessage(msg);

        GroupMessage retrieved = manager.getMessage("77");
        assertNotNull(retrieved);
        assertEquals("Hello", retrieved.getContent());
    }

    @Test
    void deleteMessagesForGroup_removesAllGroupMessages() {
        dao.addMessage(new GroupMessage("1", "alice", "Msg1", LocalDateTime.now(), 101));
        dao.addMessage(new GroupMessage("2", "bob", "Msg2", LocalDateTime.now(), 101));

        assertTrue(manager.deleteMessagesForGroup(101));
        assertTrue(dao.getMessagesForGroup(101).isEmpty());
    }
}
