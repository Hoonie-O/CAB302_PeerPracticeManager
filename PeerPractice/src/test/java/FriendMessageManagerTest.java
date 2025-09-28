import com.cab302.peerpractice.Model.DAOs.MockFriendMessageDAO;
import com.cab302.peerpractice.Model.Entities.FriendMessage;
import com.cab302.peerpractice.Model.Managers.FriendMessageManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FriendMessageManagerTest {

    private FriendMessageManager manager;
    private MockFriendMessageDAO dao;

    @BeforeEach
    void setUp() {
        dao = new MockFriendMessageDAO();
        manager = new FriendMessageManager(dao);
    }

    @Test
    void sendValidMessage_storesMessage() {
        FriendMessage msg = new FriendMessage("1", "alice", "Hello Bob", LocalDateTime.now(), "bob");
        String id = manager.sendMessage(msg);

        assertEquals("1", id);
        assertNotNull(dao.getMessageById("1"));
    }

    @Test
    void sendMessage_nullRecipient_throws() {
        FriendMessage msg = new FriendMessage("2", "alice", "Hi", LocalDateTime.now(), null);
        assertThrows(IllegalArgumentException.class, () -> manager.sendMessage(msg));
    }

    @Test
    void getMessages_returnsCorrectConversation() {
        dao.addMessage(new FriendMessage("1", "alice", "Hey", LocalDateTime.now(), "bob"));
        dao.addMessage(new FriendMessage("2", "bob", "Hi", LocalDateTime.now(), "alice"));
        dao.addMessage(new FriendMessage("3", "alice", "Yo", LocalDateTime.now(), "charlie")); // different user

        List<FriendMessage> conv = manager.getMessages("alice");
        assertEquals(3, dao.getAllMessages().size()); // DAO has all
        assertEquals(2, dao.getMessagesBetween("alice", "bob").size());
        assertTrue(conv.stream().anyMatch(m -> m.getContent().equals("Hey")));
    }

    @Test
    void deleteMessage_removesCorrectMessage() {
        FriendMessage msg = new FriendMessage("1", "alice", "Hi", LocalDateTime.now(), "bob");
        dao.addMessage(msg);

        assertTrue(manager.deleteMessage("1"));
        assertNull(dao.getMessageById("1"));
    }

    @Test
    void getMessage_returnsMessageIfExists() {
        FriendMessage msg = new FriendMessage("42", "alice", "Hi", LocalDateTime.now(), "bob");
        dao.addMessage(msg);

        FriendMessage retrieved = manager.getMessage("42");
        assertNotNull(retrieved);
        assertEquals("alice", retrieved.getSenderId());
    }
}
