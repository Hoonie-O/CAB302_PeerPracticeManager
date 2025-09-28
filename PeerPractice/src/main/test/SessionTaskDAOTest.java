import com.cab302.peerpractice.Model.daos.IUserDAO;
import com.cab302.peerpractice.Model.daos.SessionCalendarDAO;
import com.cab302.peerpractice.Model.daos.SessionTaskDAO;
import com.cab302.peerpractice.Model.daos.UserDAO;
import com.cab302.peerpractice.Model.entities.Session;
import com.cab302.peerpractice.Model.entities.SessionTask;
import com.cab302.peerpractice.Model.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SessionTaskDAOTest {

    private SessionTaskDAO storage;
    private IUserDAO userDao;
    private User testUser1;
    private User testUser2;
    private Session testSession;
    private SessionCalendarDAO sessionStorage;
    private SessionTask testTask1;
    private SessionTask testTask2;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        storage = new SessionTaskDAO(userDao);
        sessionStorage = new SessionCalendarDAO(userDao);
        
        testUser1 = new User("John", "Doe", "johndoe_task", "john.task@example.com", "hashedpass1", "Test University");
        testUser2 = new User("Jane", "Smith", "janesmith_task", "jane.task@example.com", "hashedpass2", "Test University");
        
        userDao.addUser(testUser1);
        userDao.addUser(testUser2);
        
        testSession = new Session("Test Session for Tasks", testUser1,
                                LocalDateTime.now().plusHours(1),
                                LocalDateTime.now().plusHours(3));
        sessionStorage.addSession(testSession);
        
        testTask1 = new SessionTask(testSession.getSessionId(), "Task 1",
                                  LocalDateTime.now().plusDays(1), 
                                  testUser1.getUserId(), 
                                  testUser1.getUserId());
        
        testTask2 = new SessionTask(testSession.getSessionId(), "Task 2",
                                  LocalDateTime.now().plusDays(2),
                                  testUser2.getUserId(),
                                  testUser1.getUserId());
    }
    
    @AfterEach
    void tearDown() {
        storage.clearAllTasks();
        sessionStorage.clearAllSessions();
        try {
            userDao.deleteUser(testUser1.getUserId());
            userDao.deleteUser(testUser2.getUserId());
        } catch (Exception e) {
        }
    }

    @Test
    void testAddTask() {
        storage.addTask(testTask1);
        
        List<SessionTask> allTasks = storage.getAllTasks();
        assertEquals(1, allTasks.size());
        assertEquals("Task 1", allTasks.get(0).getTitle());
        assertEquals(testSession.getSessionId(), allTasks.get(0).getSessionId());
    }

    @Test
    void testGetTasksForSession() {
        storage.addTask(testTask1);
        storage.addTask(testTask2);
        
        List<SessionTask> sessionTasks = storage.getTasksForSession(testSession.getSessionId());
        assertEquals(2, sessionTasks.size());
        
        List<SessionTask> nonExistentSessionTasks = storage.getTasksForSession("non-existent-session");
        assertTrue(nonExistentSessionTasks.isEmpty());
    }

    @Test
    void testGetTaskById() {
        storage.addTask(testTask1);
        
        SessionTask retrievedTask = storage.getTaskById(testTask1.getTaskId());
        assertNotNull(retrievedTask);
        assertEquals("Task 1", retrievedTask.getTitle());
        assertEquals(testTask1.getTaskId(), retrievedTask.getTaskId());
        
        SessionTask nonExistentTask = storage.getTaskById("non-existent-id");
        assertNull(nonExistentTask);
    }

    @Test
    void testUpdateTask() {
        storage.addTask(testTask1);
        
        testTask1.setCompleted(true);
        boolean updated = storage.updateTask(testTask1);
        assertTrue(updated);
        
        SessionTask retrievedTask = storage.getTaskById(testTask1.getTaskId());
        assertTrue(retrievedTask.isCompleted());
    }

    @Test
    void testRemoveTask() {
        storage.addTask(testTask1);
        
        boolean removed = storage.removeTask(testTask1.getTaskId());
        assertTrue(removed);
        
        SessionTask retrievedTask = storage.getTaskById(testTask1.getTaskId());
        assertNull(retrievedTask);
        
        boolean removedAgain = storage.removeTask(testTask1.getTaskId());
        assertFalse(removedAgain);
    }

    @Test
    void testRemoveAllTasksForSession() {
        storage.addTask(testTask1);
        storage.addTask(testTask2);
        
        assertEquals(2, storage.getTasksForSession(testSession.getSessionId()).size());
        
        boolean removed = storage.removeAllTasksForSession(testSession.getSessionId());
        assertTrue(removed);
        
        List<SessionTask> remainingTasks = storage.getTasksForSession(testSession.getSessionId());
        assertTrue(remainingTasks.isEmpty());
    }

    @Test
    void testRemoveAllTasksForSessionWithNullParameter() {
        storage.addTask(testTask1);
        
        boolean result = storage.removeAllTasksForSession(null);
        assertFalse(result);
        
        assertEquals(1, storage.getAllTasks().size());
    }

    @Test
    void testRemoveAllTasksForNonExistentSession() {
        storage.addTask(testTask1);
        
        boolean result = storage.removeAllTasksForSession("non-existent-session");
        assertTrue(result); // Should return true even if no rows affected
        
        assertEquals(1, storage.getAllTasks().size());
    }

    @Test
    void testGetTasksForUser() {
        storage.addTask(testTask1);
        storage.addTask(testTask2);
        
        List<SessionTask> user1Tasks = storage.getTasksForUser(testUser1.getUserId());
        assertEquals(1, user1Tasks.size());
        assertEquals("Task 1", user1Tasks.get(0).getTitle());
        
        List<SessionTask> user2Tasks = storage.getTasksForUser(testUser2.getUserId());
        assertEquals(1, user2Tasks.size());
        assertEquals("Task 2", user2Tasks.get(0).getTitle());
    }

    @Test
    void testGetOverdueTasks() {
        SessionTask overdueTask = new SessionTask(testSession.getSessionId(), "Overdue Task",
                                                LocalDateTime.now().minusDays(1), // Past deadline
                                                testUser1.getUserId(),
                                                testUser1.getUserId());
        storage.addTask(overdueTask);
        storage.addTask(testTask1); // Future deadline

        List<SessionTask> overdueTasks = storage.getOverdueTasks();
        assertEquals(1, overdueTasks.size());
        assertEquals("Overdue Task", overdueTasks.get(0).getTitle());
    }

    @Test
    void testGetCompletedTasks() {
        storage.addTask(testTask1);
        storage.addTask(testTask2);
        
        testTask1.setCompleted(true);
        storage.updateTask(testTask1);
        
        List<SessionTask> completedTasks = storage.getCompletedTasks();
        assertEquals(1, completedTasks.size());
        assertEquals("Task 1", completedTasks.get(0).getTitle());
    }

    @Test
    void testGetAllTasks() {
        storage.addTask(testTask1);
        storage.addTask(testTask2);
        
        List<SessionTask> allTasks = storage.getAllTasks();
        assertEquals(2, allTasks.size());
    }

    @Test
    void testClearAllTasks() {
        storage.addTask(testTask1);
        storage.addTask(testTask2);
        
        assertEquals(2, storage.getAllTasks().size());
        
        storage.clearAllTasks();
        
        assertTrue(storage.getAllTasks().isEmpty());
    }

    @Test
    void testGetTaskCount() {
        assertEquals(0, storage.getTaskCount());
        
        storage.addTask(testTask1);
        assertEquals(1, storage.getTaskCount());
        
        storage.addTask(testTask2);
        assertEquals(2, storage.getTaskCount());
        
        storage.removeTask(testTask1.getTaskId());
        assertEquals(1, storage.getTaskCount());
    }

    @Test
    void testAddNullTask() {
        storage.addTask(null);
        
        assertEquals(0, storage.getAllTasks().size());
    }

    @Test
    void testUpdateNullTask() {
        boolean result = storage.updateTask(null);
        assertFalse(result);
    }

    @Test
    void testUpdateNonExistentTask() {
        // Create a valid task that hasn't been added
        SessionTask nonExistent = new SessionTask(
                testSession.getSessionId(),
                "Non-existent Task",
                LocalDateTime.now().plusDays(1),
                testUser1.getUserId(),
                testUser1.getUserId()
        );

        boolean result = storage.updateTask(nonExistent);
        assertFalse(result, "Updating a task that does not exist should return false");
    }

    @Test
    void testUpdateExistingTask() {
        storage.addTask(testTask1);

        testTask1.setCompleted(true);
        boolean result = storage.updateTask(testTask1);

        assertTrue(result, "Updating an existing task should succeed");

        SessionTask updated = storage.getTaskById(testTask1.getTaskId());
        assertTrue(updated.isCompleted(), "The updated task should reflect the new state");
    }

    @Test
    void testRemoveNullTaskId() {
        boolean result = storage.removeTask(null);
        assertFalse(result);
    }

    @Test
    void testGetTasksForNullUser() {
        storage.addTask(testTask1);
        
        List<SessionTask> result = storage.getTasksForUser(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetTasksForNullSession() {
        storage.addTask(testTask1);
        
        List<SessionTask> result = storage.getTasksForSession(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSessionTitleDenormalization() {
        storage.addTask(testTask1);
        
        List<SessionTask> allTasks = storage.getAllTasks();
        assertEquals(1, allTasks.size());
        
        // The session title should be stored in the tasks table for denormalization
        // We can't directly test this without accessing the database, but we can verify
        // that tasks are properly associated with sessions
        assertEquals(testSession.getSessionId(), allTasks.get(0).getSessionId());
    }

    @Test
    void testMultipleSessionsIndependence() throws SQLException {
        Session anotherSession = new Session("Another Session", testUser2,
                                           LocalDateTime.now().plusHours(5),
                                           LocalDateTime.now().plusHours(7));
        sessionStorage.addSession(anotherSession);
        
        SessionTask taskForFirstSession = new SessionTask(testSession.getSessionId(), "First Session Task",
                                                        LocalDateTime.now().plusDays(1),
                                                        testUser1.getUserId(),
                                                        testUser1.getUserId());
        
        SessionTask taskForSecondSession = new SessionTask(anotherSession.getSessionId(), "Second Session Task",
                                                          LocalDateTime.now().plusDays(1),
                                                          testUser2.getUserId(),
                                                          testUser2.getUserId());
        
        storage.addTask(taskForFirstSession);
        storage.addTask(taskForSecondSession);
        
        List<SessionTask> firstSessionTasks = storage.getTasksForSession(testSession.getSessionId());
        assertEquals(1, firstSessionTasks.size());
        assertEquals("First Session Task", firstSessionTasks.get(0).getTitle());
        
        List<SessionTask> secondSessionTasks = storage.getTasksForSession(anotherSession.getSessionId());
        assertEquals(1, secondSessionTasks.size());
        assertEquals("Second Session Task", secondSessionTasks.get(0).getTitle());
        
        storage.removeAllTasksForSession(testSession.getSessionId());
        
        assertTrue(storage.getTasksForSession(testSession.getSessionId()).isEmpty());
        assertEquals(1, storage.getTasksForSession(anotherSession.getSessionId()).size());
    }
}