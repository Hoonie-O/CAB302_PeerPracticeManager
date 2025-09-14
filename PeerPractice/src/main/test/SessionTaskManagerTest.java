package com.cab302.peerpractice.test;

import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

public class SessionTaskManagerTest {

    private SessionTaskManager manager;
    private SessionTaskStorage storage;
    private SessionManager sessionManager;
    private String sessionId;
    private User john;
    private User jane;
    private Session testSession;

    @BeforeEach
    void setUp() {
        storage = new SessionTaskStorage();
        sessionManager = new SessionManager();
        manager = new SessionTaskManager(storage, sessionManager);
        
        // Create test users with proper constructor (firstName, lastName, username, email, passwordHash, institution)
        john = new User("John", "Doe", "john_doe", "john@email.com", "password123", "University");
        jane = new User("Jane", "Smith", "jane_smith", "jane@email.com", "password456", "University");

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
        assertNotNull(task.getTaskId());
        assertEquals("Prepare presentation notes", task.getTitle());
        assertEquals(john.getUserId(), task.getAssigneeId());
        assertEquals(jane.getUserId(), task.getCreatedBy());
    }

    @Test
    void rejectsTaskWithNonExistentSession() {
        assertThrows(IllegalArgumentException.class, () -> {
            manager.createTask(
                "nonexistent-session",
                "Invalid session task",
                LocalDateTime.now().plusDays(1),
                john.getUserId(),
                jane.getUserId()
            );
        });
    }

    @Test
    void rejectsTaskWithNonParticipantAssignee() {
        User outsider = new User("Bob", "Wilson", "bob_wilson", "bob@email.com", "password789", "University");
        
        assertThrows(IllegalArgumentException.class, () -> {
            manager.createTask(
                sessionId,
                "Task for non-participant",
                LocalDateTime.now().plusDays(1),
                outsider.getUserId(),
                jane.getUserId()
            );
        });
    }

    @Test
    void rejectsTaskWithNonParticipantCreator() {
        User outsider = new User("Carol", "White", "carol_white", "carol@email.com", "password789", "University");
        
        assertThrows(IllegalArgumentException.class, () -> {
            manager.createTask(
                sessionId,
                "Task from non-participant",
                LocalDateTime.now().plusDays(1),
                john.getUserId(),
                outsider.getUserId()
            );
        });
    }

    @Test
    void canUpdateTask() {
        SessionTask task = manager.createTask(
            sessionId,
            "Original title",
            LocalDateTime.now().plusDays(3),
            john.getUserId(),
            jane.getUserId()
        );
        
        SessionTask updatedTask = manager.updateTask(
            task.getTaskId(),
            "Updated title",
            LocalDateTime.now().plusDays(5),
            john.getUserId(),
            jane.getUserId()
        );
        
        assertNotNull(updatedTask);
        assertEquals("Updated title", updatedTask.getTitle());
    }

    @Test
    void canDeleteTask() {
        SessionTask task = manager.createTask(
            sessionId,
            "Task to delete",
            LocalDateTime.now().plusDays(1),
            john.getUserId(),
            jane.getUserId()
        );
        
        boolean deleted = manager.deleteTask(task.getTaskId(), jane.getUserId());
        assertTrue(deleted);
    }

    @Test
    void canMarkTaskCompleted() {
        SessionTask task = manager.createTask(
            sessionId,
            "Task to complete",
            LocalDateTime.now().plusDays(1),
            john.getUserId(),
            jane.getUserId()
        );
        
        boolean completed = manager.markTaskCompleted(task.getTaskId(), john.getUserId());
        assertTrue(completed);
    }

    @Test
    void canGetSessionTasks() {
        manager.createTask(
            sessionId,
            "First task",
            LocalDateTime.now().plusDays(1),
            john.getUserId(),
            jane.getUserId()
        );
        
        manager.createTask(
            sessionId,
            "Second task",
            LocalDateTime.now().plusDays(2),
            john.getUserId(),
            jane.getUserId()
        );
        
        List<SessionTask> tasks = manager.getSessionTasks(sessionId);
        assertEquals(2, tasks.size());
    }

    @Test
    void canGetUserTasks() {
        manager.createTask(
            sessionId,
            "John's task",
            LocalDateTime.now().plusDays(1),
            john.getUserId(),
            jane.getUserId()
        );
        
        manager.createTask(
            sessionId,
            "Jane's task",
            LocalDateTime.now().plusDays(2),
            jane.getUserId(),
            john.getUserId()
        );
        
        List<SessionTask> johnsTasks = manager.getUserTasks(john.getUserId());
        assertEquals(1, johnsTasks.size());
        assertEquals("John's task", johnsTasks.get(0).getTitle());
        
        List<SessionTask> janesTasks = manager.getUserTasks(jane.getUserId());
        assertEquals(1, janesTasks.size());
        assertEquals("Jane's task", janesTasks.get(0).getTitle());
    }

    @Test
    void handlesEmptyResults() {
        List<SessionTask> tasks = manager.getSessionTasks("nonexistent-session");
        assertTrue(tasks.isEmpty());
        
        List<SessionTask> userTasks = manager.getUserTasks("nonexistent-user");
        assertTrue(userTasks.isEmpty());
    }

    @Test
    void canGetOverdueTasks() {
        List<SessionTask> overdueTasks = manager.getOverdueTasks();
        assertNotNull(overdueTasks);
    }
}