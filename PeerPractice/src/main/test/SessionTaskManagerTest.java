import com.cab302.peerpractice.Model.daos.*;
import com.cab302.peerpractice.Model.entities.Session;
import com.cab302.peerpractice.Model.entities.SessionTask;
import com.cab302.peerpractice.Model.entities.User;
import com.cab302.peerpractice.Model.managers.SessionManager;
import com.cab302.peerpractice.Model.managers.SessionTaskManager;
import com.cab302.peerpractice.Model.managers.SessionCalendarManager;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class SessionTaskManagerTest {

    private SessionTaskManager manager;
    private SessionManager sessionManager;
    private ISessionTaskDAO taskDao;
    private ISessionCalendarDAO calendarDao;
    private IUserDAO userDao;

    private String sessionId;
    private User john;
    private User jane;
    private Session testSession;

    @BeforeEach
    void setUp() throws SQLException {
        // Use real DAOs (backed by SQLite)
        userDao = new UserDAO();
        calendarDao = new SessionCalendarDAO(userDao);
        taskDao = new SessionTaskDAO(userDao);

        // Managers wired with real DAOs
        SessionCalendarManager calendarManager = new SessionCalendarManager(calendarDao);
        sessionManager = new SessionManager(calendarManager);
        manager = new SessionTaskManager(taskDao, sessionManager);

        // Ensure tables start clean
        taskDao.clearAllTasks();
        calendarDao.clearAllSessions();

        // Seed users
        john = new User("John", "Doe", "john_doe", "john@email.com", "password123", "University");
        jane = new User("Jane", "Smith", "jane_smith", "jane@email.com", "password456", "University");
        userDao.addUser(john);
        userDao.addUser(jane);

        // Create session
        testSession = sessionManager.createSession(
                "Test Study Session",
                jane,
                LocalDateTime.now().plusHours(2),
                LocalDateTime.now().plusHours(4)
        );
        sessionId = testSession.getSessionId();

        testSession.addParticipant(john);
        testSession.addParticipant(jane);
    }

    @AfterEach
    void tearDown() {
        // Clean DB after each test
        taskDao.clearAllTasks();
        calendarDao.clearAllSessions();
        try {
            userDao.deleteUser(john.getUserId());
            userDao.deleteUser(jane.getUserId());
        } catch (Exception ignored) {}
    }

    @Test
    void canCreateValidTask() {
        SessionTask task = manager.createTask(
                sessionId,
                "Prepare presentation notes",
                LocalDateTime.now().plusDays(2),
                john.getUserId(),
                jane.getUserId()
        );

        assertNotNull(task);
        assertEquals("Prepare presentation notes", task.getTitle());
        assertEquals(john.getUserId(), task.getAssigneeId());
        assertEquals(jane.getUserId(), task.getCreatedBy());
    }

    @Test
    void rejectsTaskWithNonExistentSession() {
        assertThrows(IllegalArgumentException.class, () -> manager.createTask(
                "nonexistent-session",
                "Invalid task",
                LocalDateTime.now().plusDays(1),
                john.getUserId(),
                jane.getUserId()
        ));
    }

    @Test
    void rejectsTaskWithNonParticipantAssignee() {
        User outsider = new User("Bob", "Wilson", "bob_wilson", "bob@email.com", "password789", "University");
        userDao.addUser(outsider);

        assertThrows(IllegalArgumentException.class, () -> manager.createTask(
                sessionId,
                "Task for outsider",
                LocalDateTime.now().plusDays(1),
                outsider.getUserId(),
                jane.getUserId()
        ));
    }

    @Test
    void canUpdateTask() {
        SessionTask task = manager.createTask(
                sessionId,
                "Original",
                LocalDateTime.now().plusDays(1),
                john.getUserId(),
                jane.getUserId()
        );

        SessionTask updated = manager.updateTask(
                task.getTaskId(),
                "Updated",
                LocalDateTime.now().plusDays(3),
                john.getUserId(),
                jane.getUserId()
        );

        assertNotNull(updated);
        assertEquals("Updated", updated.getTitle());
    }

    @Test
    void canDeleteTask() {
        SessionTask task = manager.createTask(
                sessionId,
                "To delete",
                LocalDateTime.now().plusDays(1),
                john.getUserId(),
                jane.getUserId()
        );

        assertTrue(manager.deleteTask(task.getTaskId(), jane.getUserId()));
    }

    @Test
    void canMarkTaskCompleted() {
        SessionTask task = manager.createTask(
                sessionId,
                "To complete",
                LocalDateTime.now().plusDays(1),
                john.getUserId(),
                jane.getUserId()
        );

        assertTrue(manager.markTaskCompleted(task.getTaskId(), john.getUserId()));
    }

    @Test
    void canGetUserTasks() {
        manager.createTask(sessionId, "John's task", LocalDateTime.now().plusDays(1), john.getUserId(), jane.getUserId());
        manager.createTask(sessionId, "Jane's task", LocalDateTime.now().plusDays(1), jane.getUserId(), john.getUserId());

        assertEquals(1, manager.getUserTasks(john.getUserId()).size());
        assertEquals(1, manager.getUserTasks(jane.getUserId()).size());
    }

    @Test
    void handlesEmptyResults() {
        assertTrue(manager.getSessionTasks("nonexistent").isEmpty());
        assertTrue(manager.getUserTasks("fake-user").isEmpty());
    }

    @Test
    void canGetOverdueTasks() {
        assertNotNull(manager.getOverdueTasks());
    }
}
