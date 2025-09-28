import com.cab302.peerpractice.Model.DAOs.GroupDAO;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.DAOs.SessionCalendarDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Utils.SQLiteConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SessionCalendarDAOTest {

    private Connection connection;
    private SessionCalendarDAO storage;
    private IUserDAO userDao;
    private GroupDAO groupDao;
    private User testUser1;
    private User testUser2;
    private Group testGroup;
    private Session testSession1;
    private Session testSession2;

    @BeforeEach
    void setUp() throws SQLException {
        // fresh in-memory DB
        connection = DriverManager.getConnection("jdbc:sqlite::memory:");
        SQLiteConnection.setInstance(connection);

        userDao = new UserDAO();
        groupDao = new GroupDAO(userDao);
        storage = new SessionCalendarDAO(userDao);
        
        testUser1 = new User("Alice", "Cooper", "alice_session", "alice.session@example.com", "hashedpass1", "Test University");
        testUser2 = new User("Bob", "Dylan", "bob_session", "bob.session@example.com", "hashedpass2", "Test University");
        
        userDao.addUser(testUser1);
        userDao.addUser(testUser2);
        
        testGroup = new Group("Study Group", "Test group for sessions", false, testUser1.getUsername(), LocalDateTime.now());
        groupDao.addGroup(testGroup);
        
        testSession1 = new Session("Morning Study", testUser1,
                                 LocalDateTime.of(2024, 10, 15, 9, 0),
                                 LocalDateTime.of(2024, 10, 15, 11, 0));
        testSession1.setGroup(testGroup);
        
        testSession2 = new Session("Evening Review", testUser2,
                                 LocalDateTime.of(2024, 10, 15, 19, 0),
                                 LocalDateTime.of(2024, 10, 15, 21, 0));
    }

    @AfterEach
    void tearDown() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close(); // wipes the in-memory DB
        }
    }

    @Test
    void testAddSession() {
        boolean added = storage.addSession(testSession1);
        assertTrue(added);
        assertNotNull(testSession1.getSessionId());
        
        List<Session> allSessions = storage.getAllSessions();
        assertEquals(1, allSessions.size());
        assertEquals("Morning Study", allSessions.get(0).getTitle());
    }

    @Test
    void testAddSessionWithParticipants() {
        testSession1.addParticipant(testUser1);
        testSession1.addParticipant(testUser2);
        
        boolean added = storage.addSession(testSession1);
        assertTrue(added);
        
        List<Session> allSessions = storage.getAllSessions();
        assertEquals(1, allSessions.size());
        
        Session retrievedSession = allSessions.get(0);
        assertEquals(2, retrievedSession.getParticipants().size());
    }

    @Test
    void testRemoveSession() {
        storage.addSession(testSession1);
        
        boolean removed = storage.removeSession(testSession1);
        assertTrue(removed);
        
        List<Session> allSessions = storage.getAllSessions();
        assertTrue(allSessions.isEmpty());
    }

    @Test
    void testUpdateSession() {
        storage.addSession(testSession1);
        
        Session updatedSession = new Session("Updated Morning Study", testUser1,
                                           LocalDateTime.of(2024, 10, 15, 10, 0),
                                           LocalDateTime.of(2024, 10, 15, 12, 0));
        // Note: Session IDs cannot be set directly after creation for security
        updatedSession.setDescription("Updated description");
        updatedSession.setColorLabel("RED");
        
        boolean updated = storage.updateSession(testSession1, updatedSession);
        assertTrue(updated);
        
        List<Session> allSessions = storage.getAllSessions();
        assertEquals(1, allSessions.size());
        assertEquals("Updated Morning Study", allSessions.get(0).getTitle());
        assertEquals("Updated description", allSessions.get(0).getDescription());
    }

    @Test
    void testGetAllSessions() {
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        List<Session> allSessions = storage.getAllSessions();
        assertEquals(2, allSessions.size());
    }

    @Test
    void testGetSessionsForDate() {
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        LocalDate testDate = LocalDate.of(2024, 10, 15);
        List<Session> sessionsForDate = storage.getSessionsForDate(testDate);
        assertEquals(2, sessionsForDate.size());
        
        LocalDate differentDate = LocalDate.of(2024, 10, 16);
        List<Session> sessionsForDifferentDate = storage.getSessionsForDate(differentDate);
        assertTrue(sessionsForDifferentDate.isEmpty());
    }

    @Test
    void testGetSessionsForUser() {
        testSession1.addParticipant(testUser1);
        testSession2.addParticipant(testUser2);
        
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        List<Session> user1Sessions = storage.getSessionsForUser(testUser1);
        assertEquals(1, user1Sessions.size());
        assertEquals("Morning Study", user1Sessions.get(0).getTitle());
        
        List<Session> user2Sessions = storage.getSessionsForUser(testUser2);
        assertEquals(1, user2Sessions.size());
        assertEquals("Evening Review", user2Sessions.get(0).getTitle());
    }

    @Test
    void testGetSessionsForWeek() {
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        LocalDate startOfWeek = LocalDate.of(2024, 10, 14); // Monday
        List<Session> weekSessions = storage.getSessionsForWeek(startOfWeek);
        assertEquals(2, weekSessions.size());
        
        LocalDate differentWeek = LocalDate.of(2024, 10, 21);
        List<Session> differentWeekSessions = storage.getSessionsForWeek(differentWeek);
        assertTrue(differentWeekSessions.isEmpty());
    }

    @Test
    void testGetSessionsForDateRange() {
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        LocalDate startDate = LocalDate.of(2024, 10, 15);
        LocalDate endDate = LocalDate.of(2024, 10, 15);
        
        List<Session> rangeSessions = storage.getSessionsForDateRange(startDate, endDate);
        assertEquals(2, rangeSessions.size());
        
        LocalDate narrowStartDate = LocalDate.of(2024, 10, 16);
        LocalDate narrowEndDate = LocalDate.of(2024, 10, 16);
        List<Session> narrowRangeSessions = storage.getSessionsForDateRange(narrowStartDate, narrowEndDate);
        assertTrue(narrowRangeSessions.isEmpty());
    }

    @Test
    void testGetSessionsForGroup() {
        storage.addSession(testSession1); // Has testGroup
        storage.addSession(testSession2); // No group
        
        List<Session> groupSessions = storage.getSessionsForGroup(testGroup);
        assertEquals(1, groupSessions.size());
        assertEquals("Morning Study", groupSessions.get(0).getTitle());
    }

    @Test
    void testClearAllSessions() {
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        assertEquals(2, storage.getAllSessions().size());
        
        storage.clearAllSessions();
        
        assertTrue(storage.getAllSessions().isEmpty());
    }

    @Test
    void testGetSessionCount() {
        assertEquals(0, storage.getSessionCount());
        
        storage.addSession(testSession1);
        assertEquals(1, storage.getSessionCount());
        
        storage.addSession(testSession2);
        assertEquals(2, storage.getSessionCount());
    }

    @Test
    void testHasSessionsOnDate() {
        LocalDate testDate = LocalDate.of(2024, 10, 15);
        assertFalse(storage.hasSessionsOnDate(testDate));
        
        storage.addSession(testSession1);
        assertTrue(storage.hasSessionsOnDate(testDate));
        
        LocalDate differentDate = LocalDate.of(2024, 10, 16);
        assertFalse(storage.hasSessionsOnDate(differentDate));
    }

    @Test
    void testSessionParticipantsCascadeDelete() {
        testSession1.addParticipant(testUser1);
        testSession1.addParticipant(testUser2);
        
        storage.addSession(testSession1);
        
        List<Session> sessions = storage.getAllSessions();
        assertEquals(1, sessions.size());
        assertEquals(2, sessions.get(0).getParticipants().size());
        
        storage.removeSession(testSession1);
        
        // Participants should be automatically removed due to cascade delete
        assertTrue(storage.getAllSessions().isEmpty());
    }

    @Test
    void testSessionPriorityMigration() {
        // This tests the migration from 'status' to 'priority' column
        storage.addSession(testSession1);
        
        List<Session> sessions = storage.getAllSessions();
        assertEquals(1, sessions.size());
        
        // Default priority should be set
        Session retrievedSession = sessions.get(0);
        assertNotNull(retrievedSession.getPriority());
    }

    @Test
    void testSessionWithAllProperties() {
        testSession1.setDescription("Comprehensive study session");
        testSession1.setPriority("high");
        testSession1.setLocation("Library Room 101");
        testSession1.setColorLabel("PURPLE");
        testSession1.setSubject("Computer Science");
        testSession1.setMaxParticipants(15);
        
        storage.addSession(testSession1);
        
        List<Session> sessions = storage.getAllSessions();
        assertEquals(1, sessions.size());
        
        Session retrieved = sessions.get(0);
        assertEquals("Comprehensive study session", retrieved.getDescription());
        assertEquals("high", retrieved.getPriority());
        assertEquals("Library Room 101", retrieved.getLocation());
        assertEquals("PURPLE", retrieved.getColorLabel());
        assertEquals("Computer Science", retrieved.getSubject());
        assertEquals(15, retrieved.getMaxParticipants());
    }

    @Test
    void testAddNullSession() {
        boolean result = storage.addSession(null);
        assertFalse(result);
    }

    @Test
    void testRemoveNullSession() {
        boolean result = storage.removeSession(null);
        assertFalse(result);
    }

    @Test
    void testUpdateWithNullSession() {
        storage.addSession(testSession1);
        
        boolean result1 = storage.updateSession(null, testSession2);
        assertFalse(result1);
        
        boolean result2 = storage.updateSession(testSession1, null);
        assertFalse(result2);
    }

    @Test
    void testGetSessionsForNullUser() {
        storage.addSession(testSession1);
        
        List<Session> result = storage.getSessionsForUser(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSessionsForNullGroup() {
        storage.addSession(testSession1);
        
        List<Session> result = storage.getSessionsForGroup(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSessionIdGeneration() {
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        assertNotNull(testSession1.getSessionId());
        assertNotNull(testSession2.getSessionId());
        assertNotEquals(testSession1.getSessionId(), testSession2.getSessionId());
    }

    @Test
    void testComplexParticipantQueries() {
        testSession1.addParticipant(testUser1);
        testSession1.addParticipant(testUser2);
        
        testSession2.addParticipant(testUser1); // User1 in both sessions
        
        storage.addSession(testSession1);
        storage.addSession(testSession2);
        
        List<Session> user1Sessions = storage.getSessionsForUser(testUser1);
        assertEquals(2, user1Sessions.size());
        
        List<Session> user2Sessions = storage.getSessionsForUser(testUser2);
        assertEquals(1, user2Sessions.size());
    }
}