package com.cab302.peerpractice.test;

import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.DAOs.SessionCalendarDAO;
import com.cab302.peerpractice.Model.DAOs.SessionTaskDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.SessionTask;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.SessionCalendarManager;
import com.cab302.peerpractice.Model.Managers.SessionManager;
import com.cab302.peerpractice.Model.Managers.SessionTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class SessionDeletionIntegrationTest {

    private SessionCalendarManager sessionCalendarManager;
    private SessionTaskManager sessionTaskManager;
    private SessionTaskDAO sessionTaskStorage;
    private SessionCalendarDAO sessionCalendarStorage;
    private SessionManager sessionManager;
    private IUserDAO userDao;
    private User testUser;
    private Session testSession;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        sessionCalendarStorage = new SessionCalendarDAO(userDao);
        sessionCalendarManager = new SessionCalendarManager(sessionCalendarStorage);
        sessionManager = new SessionManager(sessionCalendarManager);
        sessionTaskStorage = new SessionTaskDAO(userDao);
        sessionTaskManager = new SessionTaskManager(sessionTaskStorage, sessionManager);
        
        sessionCalendarManager.setSessionTaskManager(sessionTaskManager);
        
        testUser = new User("Test", "User", "testuser_session_del", "testuser_del@example.com", "hashedpass", "Test University");
        userDao.addUser(testUser);
        
        testSession = new Session("Test Session for Deletion", testUser, 
                                LocalDateTime.now().plusHours(1), 
                                LocalDateTime.now().plusHours(3));
        sessionCalendarManager.addSession(testSession);
    }
    
    @AfterEach
    void tearDown() {
        sessionTaskStorage.clearAllTasks();
        sessionCalendarStorage.clearAllSessions();
        try {
            userDao.deleteUser(testUser.getUserId());
        } catch (Exception e) {
        }
    }

    @Test
    void testSessionDeletionCascadesToTasks() {
        sessionTaskManager.createTask(
            testSession.getSessionId(),
            "Task 1",
            LocalDateTime.now().plusDays(1),
            testUser.getUserId(),
            testUser.getUserId()
        );
        
        sessionTaskManager.createTask(
            testSession.getSessionId(),
            "Task 2", 
            LocalDateTime.now().plusDays(2),
            testUser.getUserId(),
            testUser.getUserId()
        );
        
        List<SessionTask> beforeDeletion = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertEquals(2, beforeDeletion.size());
        
        sessionCalendarManager.deleteSession(testSession);
        
        List<SessionTask> afterDeletion = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertTrue(afterDeletion.isEmpty(), "All tasks should be deleted when session is deleted");
    }

    @Test
    void testRemoveSessionCascadesToTasks() {
        sessionTaskManager.createTask(
            testSession.getSessionId(),
            "Task for removal test",
            LocalDateTime.now().plusDays(1),
            testUser.getUserId(),
            testUser.getUserId()
        );
        
        List<SessionTask> beforeRemoval = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertEquals(1, beforeRemoval.size());
        
        boolean removed = sessionCalendarManager.removeSession(testSession);
        assertTrue(removed);
        
        List<SessionTask> afterRemoval = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertTrue(afterRemoval.isEmpty(), "All tasks should be deleted when session is removed");
    }

    @Test
    void testSessionDeletionWithNoTasks() {
        List<SessionTask> beforeDeletion = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertTrue(beforeDeletion.isEmpty());
        
        sessionCalendarManager.deleteSession(testSession);
        
        List<Session> sessions = sessionCalendarManager.getAllSessions();
        assertFalse(sessions.contains(testSession), "Session should be deleted");
    }

    @Test
    void testMultipleSessionsDeletionIndependence() throws SQLException {
        Session anotherSession = new Session("Another Session", testUser,
                                           LocalDateTime.now().plusHours(5), 
                                           LocalDateTime.now().plusHours(7));
        sessionCalendarManager.addSession(anotherSession);
        
        sessionTaskManager.createTask(
            testSession.getSessionId(),
            "Task in first session",
            LocalDateTime.now().plusDays(1),
            testUser.getUserId(),
            testUser.getUserId()
        );
        
        sessionTaskManager.createTask(
            anotherSession.getSessionId(),
            "Task in second session",
            LocalDateTime.now().plusDays(1),
            testUser.getUserId(),
            testUser.getUserId()
        );
        
        sessionCalendarManager.deleteSession(testSession);
        
        List<SessionTask> firstSessionTasks = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertTrue(firstSessionTasks.isEmpty(), "First session tasks should be deleted");
        
        List<SessionTask> secondSessionTasks = sessionTaskManager.getSessionTasks(anotherSession.getSessionId());
        assertEquals(1, secondSessionTasks.size(), "Second session tasks should remain");
        assertEquals("Task in second session", secondSessionTasks.get(0).getTitle());
    }

    @Test
    void testDeleteAllTasksForSessionDirectly() {
        sessionTaskManager.createTask(
            testSession.getSessionId(),
            "Direct deletion test task 1",
            LocalDateTime.now().plusDays(1),
            testUser.getUserId(),
            testUser.getUserId()
        );
        
        sessionTaskManager.createTask(
            testSession.getSessionId(),
            "Direct deletion test task 2",
            LocalDateTime.now().plusDays(2),
            testUser.getUserId(),
            testUser.getUserId()
        );
        
        List<SessionTask> beforeDeletion = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertEquals(2, beforeDeletion.size());
        
        boolean deleted = sessionTaskManager.deleteAllTasksForSession(testSession.getSessionId());
        assertTrue(deleted);
        
        List<SessionTask> afterDeletion = sessionTaskManager.getSessionTasks(testSession.getSessionId());
        assertTrue(afterDeletion.isEmpty());
    }

    @Test
    void testDeleteAllTasksForNonExistentSession() {
        boolean result = sessionTaskManager.deleteAllTasksForSession("non-existent-session");
        // Returns true even if no rows affected (valid SQL operation)
        assertTrue(result);
    }

    @Test
    void testDeleteAllTasksForNullSession() {
        boolean result = sessionTaskManager.deleteAllTasksForSession(null);
        assertFalse(result);
    }
}