package com.cab302.peerpractice.test;

import com.cab302.peerpractice.Model.DAOs.AvailabilityDAO;
import com.cab302.peerpractice.Model.DAOs.SessionCalendarDAO;
import com.cab302.peerpractice.Model.DAOs.SessionTaskDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.SessionTask;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.AvailabilityManager;
import com.cab302.peerpractice.Model.Managers.SessionCalendarManager;
import com.cab302.peerpractice.Model.Managers.SessionManager;
import com.cab302.peerpractice.Model.Managers.SessionTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SimplifiedIntegrationTest {

    private UserDAO userDao;
    private SessionCalendarManager sessionCalendarManager;
    private SessionTaskManager sessionTaskManager;
    private SessionTaskDAO sessionTaskStorage;
    private AvailabilityManager availabilityManager;
    
    private User alice;
    private User bob;
    private Session studySession;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        
        var sessionStorage = new SessionCalendarDAO(userDao);
        sessionCalendarManager = new SessionCalendarManager(sessionStorage);
        var sessionManager = new SessionManager(sessionCalendarManager);
        sessionTaskStorage = new SessionTaskDAO(userDao);
        sessionTaskManager = new SessionTaskManager(sessionTaskStorage, sessionManager);
        
        sessionCalendarManager.setSessionTaskManager(sessionTaskManager);
        
        var availabilityStorage = new AvailabilityDAO(userDao);
        availabilityManager = new AvailabilityManager(availabilityStorage);
        
        alice = new User("Alice", "Johnson", "alice_simple", "alice.simple@example.com", "hashedpass1", "QUT");
        bob = new User("Bob", "Smith", "bob_simple", "bob.simple@example.com", "hashedpass2", "QUT");
        
        userDao.addUser(alice);
        userDao.addUser(bob);
    }
    
    @AfterEach
    void tearDown() {
        sessionTaskStorage.clearAllTasks();
        sessionCalendarManager.clearAllSessions();
        availabilityManager.clearAllAvailabilities();
        
        try {
            userDao.deleteUser(alice.getUserId());
            userDao.deleteUser(bob.getUserId());
        } catch (Exception e) {
        }
    }

    @Test
    void testSessionTaskCascadeDeletion() {
        studySession = new Session("Test Session", alice,
                                 LocalDateTime.now().plusDays(1),
                                 LocalDateTime.now().plusDays(1).plusHours(2));
        studySession.addParticipant(alice);
        studySession.addParticipant(bob);
        sessionCalendarManager.addSession(studySession);
        
        SessionTask task1 = sessionTaskManager.createTask(
            studySession.getSessionId(),
            "Task 1",
            LocalDateTime.now().plusDays(2),
            alice.getUserId(),
            alice.getUserId()
        );
        
        SessionTask task2 = sessionTaskManager.createTask(
            studySession.getSessionId(),
            "Task 2",
            LocalDateTime.now().plusDays(2),
            bob.getUserId(),
            alice.getUserId()
        );
        
        assertEquals(2, sessionTaskManager.getSessionTasks(studySession.getSessionId()).size());
        
        sessionCalendarManager.deleteSession(studySession);
        
        assertTrue(sessionTaskManager.getSessionTasks(studySession.getSessionId()).isEmpty());
        assertTrue(sessionCalendarManager.getAllSessions().isEmpty());
    }

    @Test
    void testAvailabilityManagement() {
        boolean created = availabilityManager.createAvailability("Morning Study", alice,
                                                                LocalDateTime.of(2024, 11, 15, 9, 0),
                                                                LocalDateTime.of(2024, 11, 15, 11, 0),
                                                                "GREEN");
        assertTrue(created);
        
        List<Availability> aliceAvail = availabilityManager.getAvailabilitiesForUser(alice);
        assertEquals(1, aliceAvail.size());
        assertEquals("Morning Study", aliceAvail.get(0).getTitle());
        
        LocalDate testDate = LocalDate.of(2024, 11, 15);
        assertTrue(availabilityManager.hasAvailabilityOnDate(alice, testDate));
        
        List<Availability> dateAvail = availabilityManager.getAvailabilitiesForDate(testDate);
        assertEquals(1, dateAvail.size());
    }

    @Test
    void testTaskPermissionsValidation() {
        studySession = new Session("Permission Test Session", alice,
                                 LocalDateTime.now().plusHours(1),
                                 LocalDateTime.now().plusHours(2));
        studySession.addParticipant(alice);
        sessionCalendarManager.addSession(studySession);
        
        assertThrows(IllegalArgumentException.class, () -> {
            sessionTaskManager.createTask(
                studySession.getSessionId(),
                "Task for non-participant",
                LocalDateTime.now().plusDays(1),
                bob.getUserId(),
                alice.getUserId()
            );
        });
    }

    @Test
    void testUserCRUDOperations() throws SQLException {
        User newUser = new User("Charlie", "Brown", "charlie_crud", "charlie@example.com", "hashedpass", "QUT");
        boolean added = userDao.addUser(newUser);
        assertTrue(added);
        
        User retrieved = userDao.findUserById(newUser.getUserId());
        assertNotNull(retrieved);
        assertEquals("Charlie", retrieved.getFirstName());
        
        boolean updated = userDao.updateValue(newUser.getUserId(), "first_name", "Charles");
        assertTrue(updated);
        
        User updatedUser = userDao.findUserById(newUser.getUserId());
        assertEquals("Charles", updatedUser.getFirstName());
        
        boolean deleted = userDao.deleteUser(newUser.getUserId());
        assertTrue(deleted);
        
        User deletedUser = userDao.findUserById(newUser.getUserId());
        assertNull(deletedUser);
    }

    @Test
    void testSessionManagement() {
        studySession = new Session("Session Management Test", alice,
                                 LocalDateTime.now().plusDays(1),
                                 LocalDateTime.now().plusDays(1).plusHours(2));
        studySession.addParticipant(alice);
        
        boolean added = sessionCalendarManager.addSession(studySession);
        assertTrue(added);
        assertNotNull(studySession.getSessionId());
        
        List<Session> allSessions = sessionCalendarManager.getAllSessions();
        assertEquals(1, allSessions.size());
        
        List<Session> userSessions = sessionCalendarManager.getSessionsForUser(alice);
        assertEquals(1, userSessions.size());
        
        boolean removed = sessionCalendarManager.removeSession(studySession);
        assertTrue(removed);
        
        assertTrue(sessionCalendarManager.getAllSessions().isEmpty());
    }

    @Test
    void testMultipleSessionsIndependence() {
        Session session1 = new Session("Session 1", alice,
                                     LocalDateTime.now().plusDays(1),
                                     LocalDateTime.now().plusDays(1).plusHours(1));
        session1.addParticipant(alice);
        
        Session session2 = new Session("Session 2", bob,
                                     LocalDateTime.now().plusDays(2),
                                     LocalDateTime.now().plusDays(2).plusHours(1));
        session2.addParticipant(bob);
        
        sessionCalendarManager.addSession(session1);
        sessionCalendarManager.addSession(session2);
        
        SessionTask task1 = sessionTaskManager.createTask(
            session1.getSessionId(),
            "Task for Session 1",
            LocalDateTime.now().plusDays(3),
            alice.getUserId(),
            alice.getUserId()
        );
        
        SessionTask task2 = sessionTaskManager.createTask(
            session2.getSessionId(),
            "Task for Session 2",
            LocalDateTime.now().plusDays(3),
            bob.getUserId(),
            bob.getUserId()
        );
        
        assertEquals(1, sessionTaskManager.getSessionTasks(session1.getSessionId()).size());
        assertEquals(1, sessionTaskManager.getSessionTasks(session2.getSessionId()).size());
        
        sessionCalendarManager.deleteSession(session1);
        
        assertTrue(sessionTaskManager.getSessionTasks(session1.getSessionId()).isEmpty());
        assertEquals(1, sessionTaskManager.getSessionTasks(session2.getSessionId()).size());
        
        List<Session> remainingSessions = sessionCalendarManager.getAllSessions();
        assertEquals(1, remainingSessions.size());
        assertEquals("Session 2", remainingSessions.get(0).getTitle());
    }

    @Test
    void testTaskLifecycle() {
        studySession = new Session("Task Lifecycle Test", alice,
                                 LocalDateTime.now().plusHours(1),
                                 LocalDateTime.now().plusHours(2));
        studySession.addParticipant(alice);
        studySession.addParticipant(bob);
        sessionCalendarManager.addSession(studySession);
        
        SessionTask task = sessionTaskManager.createTask(
            studySession.getSessionId(),
            "Lifecycle Test Task",
            LocalDateTime.now().plusDays(1),
            alice.getUserId(),
            alice.getUserId()
        );
        
        assertNotNull(task);
        assertFalse(task.isCompleted());
        
        boolean completed = sessionTaskManager.markTaskCompleted(task.getTaskId(), alice.getUserId());
        assertTrue(completed);
        
        SessionTask completedTask = sessionTaskStorage.getTaskById(task.getTaskId());
        assertTrue(completedTask.isCompleted());
        
        SessionTask updatedTask = sessionTaskManager.updateTask(
            task.getTaskId(),
            "Updated Lifecycle Task",
            LocalDateTime.now().plusDays(2),
            alice.getUserId(),
            alice.getUserId()
        );
        
        assertEquals("Updated Lifecycle Task", updatedTask.getTitle());
        
        boolean deleted = sessionTaskManager.deleteTask(task.getTaskId(), alice.getUserId());
        assertTrue(deleted);
        
        assertTrue(sessionTaskManager.getSessionTasks(studySession.getSessionId()).isEmpty());
    }

    @Test
    void testDataValidationAndErrorHandling() {
        assertThrows(IllegalArgumentException.class, () -> {
            sessionTaskManager.createTask(
                "non-existent-session",
                "Invalid Task",
                LocalDateTime.now().plusDays(1),
                alice.getUserId(),
                alice.getUserId()
            );
        });
        
        User duplicateUser = new User("Another", "User", alice.getUsername(), "different@email.com", "pass", "University");
        boolean addedDuplicate = userDao.addUser(duplicateUser);
        assertFalse(addedDuplicate);
        
        boolean removedNonExistent = sessionCalendarManager.removeSession(new Session("Non-existent", alice, LocalDateTime.now(), LocalDateTime.now().plusHours(1)));
        assertFalse(removedNonExistent);
    }
}