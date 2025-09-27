package com.cab302.peerpractice.test;

import com.cab302.peerpractice.Model.daos.ISessionCalendarDAO;
import com.cab302.peerpractice.Model.daos.SessionCalendarDAO;
import com.cab302.peerpractice.Model.daos.UserDAO;
import com.cab302.peerpractice.Model.entities.User;
import com.cab302.peerpractice.Model.managers.SessionCalendarManager;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SessionCalendarManagerTest {
    private SessionCalendarManager manager;
    private ISessionCalendarDAO dao;
    private User testUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() throws SQLException {
        dao = new SessionCalendarDAO(new UserDAO());
        dao.clearAllSessions(); // make sure DB is clean before each test
        manager = new SessionCalendarManager(dao);

        testUser = new User("Test", "User", "testuser", "test@test.com", "hashedpass", "Test Uni");
        startTime = LocalDateTime.of(2024, 1, 15, 10, 0);
        endTime = LocalDateTime.of(2024, 1, 15, 12, 0);
    }

    @AfterEach
    void tearDown() {
        dao.clearAllSessions(); // clean after test to avoid leaks
    }

    @Test
    void testCreateSession() {
        boolean result = manager.createSession("Math Study", testUser, startTime, endTime, "BLUE");

        assertTrue(result);
        assertEquals(1, manager.getSessionCount());
    }

    @Test
    void testGetSessionsForDate() {
        manager.createSession("Math Study", testUser, startTime, endTime, "BLUE");

        var sessions = manager.getSessionsForDate(LocalDate.of(2024, 1, 15));
        assertEquals(1, sessions.size());
        assertEquals("Math Study", sessions.get(0).getTitle());
    }

    @Test
    void testHasSessionsOnDate() {
        manager.createSession("Math Study", testUser, startTime, endTime, "BLUE");

        assertTrue(manager.hasSessionsOnDate(LocalDate.of(2024, 1, 15)));
        assertFalse(manager.hasSessionsOnDate(LocalDate.of(2024, 1, 16)));
    }

    @Test
    void testDeleteSession() {
        manager.createSession("Math Study", testUser, startTime, endTime, "BLUE");
        var sessions = manager.getAllSessions();

        manager.deleteSession(sessions.get(0));
        assertEquals(0, manager.getSessionCount());
    }
}
