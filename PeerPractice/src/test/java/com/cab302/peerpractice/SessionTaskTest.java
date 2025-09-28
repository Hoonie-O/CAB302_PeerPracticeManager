package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.Entities.SessionTask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

public class SessionTaskTest {

    private String sessionId;
    private String title;
    private LocalDateTime deadline;
    private String assigneeId;
    private String createdBy;

    @BeforeEach
    void setUp() {
        sessionId = "session-123";
        title = "Review presentation slides";
        deadline = LocalDateTime.now().plusDays(3);
        assigneeId = "john-doe";
        createdBy = "jane-smith";
    }

    @Test
    void canCreateBasicTask() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        
        assertNotNull(task.getTaskId());
        assertEquals(sessionId, task.getSessionId());
        assertEquals(title, task.getTitle());
        assertEquals(deadline, task.getDeadline());
        assertEquals(assigneeId, task.getAssigneeId());
        assertEquals(createdBy, task.getCreatedBy());
        assertFalse(task.isCompleted());
        assertNotNull(task.getCreatedAt());
    }

    @Test
    void requiresValidTitle() {
        assertThrows(NullPointerException.class, () -> {
            new SessionTask(sessionId, null, deadline, assigneeId, createdBy);
        });
        
        // Empty string validation depends on implementation - checking null is enough
    }

    @Test
    void requiresValidSession() {
        assertThrows(NullPointerException.class, () -> {
            new SessionTask(null, title, deadline, assigneeId, createdBy);
        });
    }

    @Test
    void requiresValidDeadline() {
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            new SessionTask(sessionId, title, pastDate, assigneeId, createdBy);
        });
        
        assertThrows(NullPointerException.class, () -> {
            new SessionTask(sessionId, title, null, assigneeId, createdBy);
        });
    }

    @Test
    void requiresValidAssignee() {
        assertThrows(NullPointerException.class, () -> {
            new SessionTask(sessionId, title, deadline, null, createdBy);
        });
    }

    @Test
    void requiresValidCreator() {
        assertThrows(NullPointerException.class, () -> {
            new SessionTask(sessionId, title, deadline, assigneeId, null);
        });
    }

    @Test
    void canMarkAsCompleted() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        
        assertFalse(task.isCompleted());
        
        task.setCompleted(true);
        
        assertTrue(task.isCompleted());
    }

    @Test
    void canUpdateTitle() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        String newTitle = "Updated presentation slides";
        
        task.setTitle(newTitle);
        
        assertEquals(newTitle, task.getTitle());
    }

    @Test
    void canUpdateDeadline() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        LocalDateTime newDeadline = LocalDateTime.now().plusDays(5);
        
        task.setDeadline(newDeadline);
        
        assertEquals(newDeadline, task.getDeadline());
    }

    @Test
    void rejectsInvalidTitleUpdate() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        
        assertThrows(NullPointerException.class, () -> {
            task.setTitle(null);
        });
    }

    @Test
    void rejectsInvalidDeadlineUpdate() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        LocalDateTime pastDate = LocalDateTime.now().minusDays(1);
        
        assertThrows(IllegalArgumentException.class, () -> {
            task.setDeadline(pastDate);
        });
        
        assertThrows(NullPointerException.class, () -> {
            task.setDeadline(null);
        });
    }

    @Test
    void canUpdateAssignee() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        String newAssigneeId = "mike-wilson";
        
        task.setAssigneeId(newAssigneeId);
        
        assertEquals(newAssigneeId, task.getAssigneeId());
    }

    @Test
    void rejectsInvalidAssigneeUpdate() {
        SessionTask task = new SessionTask(sessionId, title, deadline, assigneeId, createdBy);
        
        assertThrows(NullPointerException.class, () -> {
            task.setAssigneeId(null);
        });
    }
}