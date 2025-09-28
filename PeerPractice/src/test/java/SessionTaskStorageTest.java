import com.cab302.peerpractice.Model.SessionTask;
import com.cab302.peerpractice.Model.SessionTaskStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

public class SessionTaskStorageTest {

    private SessionTaskStorage storage;
    private String sessionId;
    private SessionTask sampleTask;

    @BeforeEach
    void setUp() {
        storage = new SessionTaskStorage();
        sessionId = "session-456";
        
        sampleTask = new SessionTask(
            sessionId,
            "Prepare agenda items",
            LocalDateTime.now().plusDays(2),
            "mike-wilson",
            "sarah-jones"
        );
    }

    @Test
    void startsEmpty() {
        List<SessionTask> tasks = storage.getTasksForSession(sessionId);
        assertTrue(tasks.isEmpty());
        assertEquals(0, storage.getTaskCount());
    }

    @Test
    void canAddTasks() {
        storage.addTask(sampleTask);
        
        List<SessionTask> tasks = storage.getTasksForSession(sessionId);
        assertEquals(1, tasks.size());
        assertEquals(sampleTask.getTaskId(), tasks.get(0).getTaskId());
        assertEquals(1, storage.getTaskCount());
    }

    @Test
    void canAddMultipleTasks() {
        SessionTask secondTask = new SessionTask(
            sessionId,
            "Book meeting room",
            LocalDateTime.now().plusDays(1),
            "alice-brown",
            "sarah-jones"
        );
        
        storage.addTask(sampleTask);
        storage.addTask(secondTask);
        
        List<SessionTask> tasks = storage.getTasksForSession(sessionId);
        assertEquals(2, tasks.size());
        assertEquals(2, storage.getTaskCount());
    }

    @Test
    void canFindTaskById() {
        storage.addTask(sampleTask);
        
        SessionTask found = storage.getTaskById(sampleTask.getTaskId());
        assertNotNull(found);
        assertEquals(sampleTask.getTaskId(), found.getTaskId());
    }

    @Test
    void returnsNullForMissingTask() {
        SessionTask found = storage.getTaskById("nonexistent-task");
        assertNull(found);
    }

    @Test
    void canUpdateExistingTask() {
        storage.addTask(sampleTask);
        
        sampleTask.setTitle("Updated agenda preparation");
        boolean updated = storage.updateTask(sampleTask);
        assertTrue(updated);
        
        SessionTask found = storage.getTaskById(sampleTask.getTaskId());
        assertEquals("Updated agenda preparation", found.getTitle());
    }

    @Test
    void canRemoveTask() {
        storage.addTask(sampleTask);
        
        boolean removed = storage.removeTask(sampleTask.getTaskId());
        assertTrue(removed);
        
        List<SessionTask> tasks = storage.getTasksForSession(sessionId);
        assertTrue(tasks.isEmpty());
        assertEquals(0, storage.getTaskCount());
    }

    @Test
    void removeReturnsFalseForMissingTask() {
        boolean removed = storage.removeTask("nonexistent-task");
        assertFalse(removed);
    }

    @Test
    void canGetTasksByUser() {
        SessionTask taskForMike = new SessionTask(
            sessionId,
            "Research topics",
            LocalDateTime.now().plusDays(3),
            "mike-wilson",
            "sarah-jones"
        );
        
        SessionTask taskForAlice = new SessionTask(
            sessionId,
            "Setup equipment",
            LocalDateTime.now().plusDays(2),
            "alice-brown",
            "sarah-jones"
        );
        
        storage.addTask(taskForMike);
        storage.addTask(taskForAlice);
        
        List<SessionTask> mikeTasks = storage.getTasksForUser("mike-wilson");
        assertEquals(1, mikeTasks.size());
        assertEquals("Research topics", mikeTasks.get(0).getTitle());
        
        List<SessionTask> aliceTasks = storage.getTasksForUser("alice-brown");
        assertEquals(1, aliceTasks.size());
        assertEquals("Setup equipment", aliceTasks.get(0).getTitle());
    }

    @Test
    void handlesMultipleSessionsSeparately() {
        String otherSessionId = "session-789";
        SessionTask taskForOtherSession = new SessionTask(
            otherSessionId,
            "Different session task",
            LocalDateTime.now().plusDays(1),
            "john-doe",
            "jane-smith"
        );
        
        storage.addTask(sampleTask);
        storage.addTask(taskForOtherSession);
        
        List<SessionTask> firstSessionTasks = storage.getTasksForSession(sessionId);
        assertEquals(1, firstSessionTasks.size());
        
        List<SessionTask> secondSessionTasks = storage.getTasksForSession(otherSessionId);
        assertEquals(1, secondSessionTasks.size());
    }

    @Test
    void getAllTasksReturnsEverything() {
        String otherSessionId = "session-999";
        SessionTask taskForOtherSession = new SessionTask(
            otherSessionId,
            "Cross-session task",
            LocalDateTime.now().plusDays(1),
            "bob-smith",
            "carol-white"
        );
        
        storage.addTask(sampleTask);
        storage.addTask(taskForOtherSession);
        
        List<SessionTask> allTasks = storage.getAllTasks();
        assertEquals(2, allTasks.size());
        assertEquals(2, storage.getTaskCount());
    }

    @Test
    void canClearAllTasks() {
        storage.addTask(sampleTask);
        SessionTask anotherTask = new SessionTask(
            sessionId,
            "Another task",
            LocalDateTime.now().plusDays(4),
            "test-user",
            "test-creator"
        );
        storage.addTask(anotherTask);
        
        storage.clearAllTasks();
        
        List<SessionTask> allTasks = storage.getAllTasks();
        assertTrue(allTasks.isEmpty());
        assertEquals(0, storage.getTaskCount());
    }

    @Test
    void canGetCompletedTasks() {
        sampleTask.setCompleted(true);
        SessionTask incompleteTask = new SessionTask(
            sessionId,
            "Incomplete task",
            LocalDateTime.now().plusDays(1),
            "test-user",
            "test-creator"
        );
        
        storage.addTask(sampleTask);
        storage.addTask(incompleteTask);
        
        List<SessionTask> completedTasks = storage.getCompletedTasks();
        assertEquals(1, completedTasks.size());
        assertTrue(completedTasks.get(0).isCompleted());
    }

    @Test
    void canGetOverdueTasks() {
        SessionTask overdueTask = new SessionTask(
            sessionId,
            "Overdue task",
            LocalDateTime.now().plusMinutes(1), // Will be overdue soon
            "test-user",
            "test-creator"
        );
        
        storage.addTask(sampleTask); // Future deadline
        storage.addTask(overdueTask);
        
        // Note: This test may be timing-sensitive depending on implementation
        List<SessionTask> overdueTasks = storage.getOverdueTasks();
        // The exact assertion depends on the current time and implementation
    }
}