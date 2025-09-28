import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Some basic tests for Session functionality, just getting started.
 */
public class SessionTest {

    private User organiser;
    private User participant;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    public void setUp() {
        organiser = new User("Alice", "Smith", "alice123", "alice@test.com", "password", "QUT");
        participant = new User("Bob", "Jones", "bob456", "bob@test.com", "password", "QUT");
        startTime = LocalDateTime.of(2024, 3, 15, 14, 0);
        endTime = LocalDateTime.of(2024, 3, 15, 16, 0);
    }

    @Test
    public void testCreateSession() {
        Session session = new Session("Java Study Group", organiser, startTime, endTime);
        
        assertEquals("Java Study Group", session.getTitle());
        assertEquals(organiser, session.getOrganiser());
        assertEquals(startTime, session.getStartTime());
        assertEquals(endTime, session.getEndTime());
        assertEquals("optional", session.getPriority());
    }

    @Test
    public void testOrganiserIsAutoAddedAsParticipant() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        
        assertTrue(session.getParticipants().contains(organiser));
        assertEquals(1, session.getParticipants().size());
    }

    @Test
    public void testAddParticipant() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        session.addParticipant(participant);
        
        assertTrue(session.getParticipants().contains(participant));
        assertEquals(2, session.getParticipants().size());
    }

    @Test
    public void testCannotRemoveOrganiser() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        session.addParticipant(participant);
        
        session.removeParticipant(organiser); // Should not remove
        
        assertTrue(session.getParticipants().contains(organiser));
        assertEquals(2, session.getParticipants().size());
    }

    @Test
    public void testInvalidTimeThrowsException() {
        LocalDateTime badEndTime = startTime.minusHours(1);

        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Bad Session", organiser, startTime, badEndTime);
        });
    }

    @Test
    public void testSessionConstructorNullTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session(null, organiser, startTime, endTime);
        });
    }

    @Test
    public void testSessionConstructorEmptyTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("", organiser, startTime, endTime);
        });
    }

    @Test
    public void testSessionConstructorBlankTitle() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("   ", organiser, startTime, endTime);
        });
    }

    @Test
    public void testSessionConstructorNullOrganiser() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Valid Title", null, startTime, endTime);
        });
    }

    @Test
    public void testSessionConstructorNullStartTime() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Valid Title", organiser, null, endTime);
        });
    }

    @Test
    public void testSessionConstructorNullEndTime() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Valid Title", organiser, startTime, null);
        });
    }

    @Test
    public void testSessionConstructorSameStartEndTime() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Session("Same Time Session", organiser, startTime, startTime);
        });
    }

    @Test
    public void testSetTitle() {
        Session session = new Session("Original Title", organiser, startTime, endTime);
        session.setTitle("New Title");
        assertEquals("New Title", session.getTitle());
    }

    @Test
    public void testSetTitleNull() {
        Session session = new Session("Original Title", organiser, startTime, endTime);
        assertThrows(IllegalArgumentException.class, () -> session.setTitle(null));
    }

    @Test
    public void testSetTitleEmpty() {
        Session session = new Session("Original Title", organiser, startTime, endTime);
        assertThrows(IllegalArgumentException.class, () -> session.setTitle(""));
    }

    @Test
    public void testSetTitleWithSpaces() {
        Session session = new Session("Original Title", organiser, startTime, endTime);
        session.setTitle("Title With Spaces");
        assertEquals("Title With Spaces", session.getTitle());
    }

    @Test
    public void testSetStartTime() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        LocalDateTime newStart = startTime.plusHours(1);
        session.setStartTime(newStart);
        assertEquals(newStart, session.getStartTime());
    }

    @Test
    public void testSetStartTimeNull() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        assertThrows(IllegalArgumentException.class, () -> session.setStartTime(null));
    }

    @Test
    public void testSetStartTimeAfterEndTime() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        LocalDateTime invalidStart = endTime.plusHours(1);
        assertThrows(IllegalArgumentException.class, () -> session.setStartTime(invalidStart));
    }

    @Test
    public void testSetEndTime() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        LocalDateTime newEnd = endTime.plusHours(1);
        session.setEndTime(newEnd);
        assertEquals(newEnd, session.getEndTime());
    }

    @Test
    public void testSetEndTimeNull() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        assertThrows(IllegalArgumentException.class, () -> session.setEndTime(null));
    }

    @Test
    public void testSetEndTimeBeforeStartTime() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        LocalDateTime invalidEnd = startTime.minusHours(1);
        assertThrows(IllegalArgumentException.class, () -> session.setEndTime(invalidEnd));
    }

    @Test
    public void testAddParticipantNull_returnsFalse() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        assertFalse(session.addParticipant(null));
    }

    @Test
    public void testAddParticipantDuplicate() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        session.addParticipant(participant);
        session.addParticipant(participant);
        assertEquals(2, session.getParticipants().size());
    }

    @Test
    public void testRemoveParticipant() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        session.addParticipant(participant);
        session.removeParticipant(participant);
        assertFalse(session.getParticipants().contains(participant));
        assertEquals(1, session.getParticipants().size());
    }

    @Test
    public void testRemoveParticipantNull() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        assertFalse(session.removeParticipant(null));
    }

    @Test
    public void testRemoveParticipantNotInSession() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        User outsideUser = new User("Outside", "User", "outside", "outside@test.com", "pass", "Uni");
        assertDoesNotThrow(() -> session.removeParticipant(outsideUser));
    }

    @Test
    public void testSetPriority() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        session.setPriority("urgent");
        assertEquals("urgent", session.getPriority());
    }

    @Test
    public void testSetPriorityNull_defaultsToOptional() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        session.setPriority(null);
        assertEquals("optional", session.getPriority());
    }

    @Test
    public void testAllPriorityValues() {
        Session session = new Session("Test Session", organiser, startTime, endTime);

        String[] priorities = {"optional", "important", "urgent"};
        for (String priority : priorities) {
            assertDoesNotThrow(() -> session.setPriority(priority));
            assertEquals(priority, session.getPriority());
        }
    }

    @Test
    public void testGetParticipantCount() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        assertEquals(1, session.getParticipantCount());

        session.addParticipant(participant);
        assertEquals(2, session.getParticipantCount());
    }

    @Test
    public void testIsParticipant() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        assertTrue(session.isParticipant(organiser));
        assertFalse(session.isParticipant(participant));

        session.addParticipant(participant);
        assertTrue(session.isParticipant(participant));
    }

    @Test
    public void testIsParticipantNull() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        assertFalse(session.isParticipant(null));
    }

    @Test
    public void testSessionToString() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        String sessionString = session.toString();
        assertNotNull(sessionString);
        assertTrue(sessionString.contains("Test Session"));
    }

    @Test
    public void testSessionEquals() {
        Session session1 = new Session("Test Session", organiser, startTime, endTime);
        Session session2 = new Session("Test Session", organiser, startTime, endTime);
        assertNotEquals(session1, session2);
    }

    @Test
    public void testSessionHashCode() {
        Session session = new Session("Test Session", organiser, startTime, endTime);
        int hashCode = session.hashCode();
        assertTrue(hashCode != 0);
    }

    @Test
    public void testMaxParticipantLimit() {
        Session session = new Session("Test Session", organiser, startTime, endTime);

        for (int i = 0; i < 100; i++) {
            User user = new User("FirstName", "LastName", "User." + i, "user" + i + "@test.com", "Password1!", "Uni");
            assertDoesNotThrow(() -> session.addParticipant(user));
        }

        assertEquals(10, session.getParticipantCount());
    }

    @Test
    public void testSessionWithLongTitle() {
        String longTitle = "This is a very long session title that contains many words and should be handled properly".repeat(5);
        assertDoesNotThrow(() -> new Session(longTitle, organiser, startTime, endTime));
    }

    @Test
    public void testSessionMinimalDuration() {
        LocalDateTime minimalEnd = startTime.plusMinutes(1);
        assertDoesNotThrow(() -> new Session("Minimal Session", organiser, startTime, minimalEnd));
    }

    @Test
    public void testSessionMaximalDuration() {
        LocalDateTime maximalEnd = startTime.plusDays(7);
        assertDoesNotThrow(() -> new Session("Week Long Session", organiser, startTime, maximalEnd));
    }

}