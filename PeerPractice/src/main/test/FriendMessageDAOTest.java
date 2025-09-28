package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.FriendMessage;
import com.cab302.peerpractice.Model.entities.User;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FriendMessageDAOTest {

    private FriendMessageDAO dao;
    private UserDAO userDao;

    private User alice;
    private User bob;
    private User someone;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        dao = new FriendMessageDAO();

        // clean state
        dao.getAllMessages().forEach(m -> dao.deleteMessage(m.getMessageId()));
        userDao.getAllUsers().forEach(u -> {
            try { userDao.deleteUser(u.getUserId()); } catch (Exception ignored) {}
        });

        // seed users with valid usernames + passwords
        alice = new User("Alice", "Wonder", "alice123", "alice@mail.com", "Password1!", "QUT");
        bob = new User("Bob", "Builder", "bob1234", "bob@mail.com", "Secure1@", "QUT");
        someone = new User("Some", "One", "someone1", "someone@mail.com", "Valid1$", "QUT");

        userDao.addUser(alice);
        userDao.addUser(bob);
        userDao.addUser(someone);
    }

    @AfterEach
    void tearDown() {
        dao.getAllMessages().forEach(m -> dao.deleteMessage(m.getMessageId()));
        try {
            userDao.deleteUser(alice.getUserId());
            userDao.deleteUser(bob.getUserId());
            userDao.deleteUser(someone.getUserId());
        } catch (Exception ignored) {}
    }

    @Test
    void addAndGetMessageById() {
        FriendMessage msg = new FriendMessage("1", "alice123", "Hello Bob", LocalDateTime.now(), "bob1234");
        assertTrue(dao.addMessage(msg));

        FriendMessage retrieved = dao.getMessageById("1");
        assertNotNull(retrieved);
        assertEquals("Hello Bob", retrieved.getContent());
        assertEquals("bob1234", retrieved.getReceiverId());
    }

    @Test
    void getAllMessages_returnsOrderedByTimestamp() {
        FriendMessage m1 = new FriendMessage("1", "alice123", "First", LocalDateTime.now().minusMinutes(5), "bob1234");
        FriendMessage m2 = new FriendMessage("2", "bob1234", "Second", LocalDateTime.now(), "alice123");
        dao.addMessage(m1);
        dao.addMessage(m2);

        List<FriendMessage> all = dao.getAllMessages();
        assertEquals(2, all.size());
        assertEquals("First", all.get(0).getContent()); // ordered ascending
    }

    @Test
    void getMessagesBetween_twoUsers_onlyReturnsTheirConversation() {
        FriendMessage ab = new FriendMessage("1", "alice123", "Hi Bob", LocalDateTime.now(), "bob1234");
        FriendMessage ba = new FriendMessage("2", "bob1234", "Hi Alice", LocalDateTime.now(), "alice123");
        FriendMessage other = new FriendMessage("3", "alice123", "Hi world", LocalDateTime.now(), "someone1");
        dao.addMessage(ab);
        dao.addMessage(ba);
        dao.addMessage(other);

        List<FriendMessage> conv = dao.getMessagesBetween("alice123", "bob1234");
        assertEquals(2, conv.size());
        assertTrue(conv.stream().anyMatch(m -> m.getContent().equals("Hi Bob")));
    }

    @Test
    void deleteMessage_removesFromDb() {
        FriendMessage msg = new FriendMessage("1", "alice123", "Bye", LocalDateTime.now(), "bob1234");
        dao.addMessage(msg);

        assertTrue(dao.deleteMessage("1"));
        assertNull(dao.getMessageById("1"));
        assertFalse(dao.deleteMessage("1")); // already gone
    }

    @Test
    void deleteMessagesBetween_removesConversationOnly() {
        FriendMessage ab = new FriendMessage("1", "alice123", "Hi", LocalDateTime.now(), "bob1234");
        FriendMessage ba = new FriendMessage("2", "bob1234", "Yo", LocalDateTime.now(), "alice123");
        FriendMessage other = new FriendMessage("3", "alice123", "Yo", LocalDateTime.now(), "someone1");
        dao.addMessage(ab);
        dao.addMessage(ba);
        dao.addMessage(other);

        assertTrue(dao.deleteMessagesBetween("alice123", "bob1234"));

        List<FriendMessage> remaining = dao.getAllMessages();
        assertEquals(1, remaining.size());
        assertEquals("someone1", remaining.get(0).getReceiverId());
    }

    @Test
    void getMessagesForUser_returnsAllInvolved() {
        FriendMessage ab = new FriendMessage("1", "alice123", "Hi Bob", LocalDateTime.now(), "bob1234");
        FriendMessage ba = new FriendMessage("2", "bob1234", "Hey Alice", LocalDateTime.now(), "alice123");
        dao.addMessage(ab);
        dao.addMessage(ba);

        List<FriendMessage> aliceMsgs = dao.getMessagesForUser("alice123");
        assertEquals(2, aliceMsgs.size());

        List<FriendMessage> bobMsgs = dao.getMessagesForUser("bob1234");
        assertEquals(2, bobMsgs.size());
    }

    @Test
    void getMessagesForUser_nullOrBlank_returnsEmpty() {
        assertTrue(dao.getMessagesForUser(null).isEmpty());
        assertTrue(dao.getMessagesForUser("").isEmpty());
    }
}
