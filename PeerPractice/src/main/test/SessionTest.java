import com.cab302.peerpractice.Model.*;
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
        assertEquals(SessionStatus.PLANNED, session.getStatus());
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
}