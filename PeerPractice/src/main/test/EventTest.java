import com.cab302.peerpractice.Model.Event;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;

public class EventTest {
    
    @Test
    public void testEventCreation() {
        LocalDateTime start = LocalDateTime.of(2025, 8, 27, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 8, 27, 11, 0);
        
        Event event = new Event("Practice Session", "Violin practice", start, end, "BLUE");
        
        assertEquals("Practice Session", event.getTitle());
        assertEquals("Violin practice", event.getDescription());
        assertEquals(start, event.getStartTime());
        assertEquals(end, event.getEndTime());
        assertEquals("BLUE", event.getColorLabel());
    }
    
    @Test
    public void testInvalidTimeThrowsException() {
        LocalDateTime start = LocalDateTime.of(2025, 8, 27, 11, 0);
        LocalDateTime end = LocalDateTime.of(2025, 8, 27, 10, 0);
        
        assertThrows(IllegalArgumentException.class, () -> {
            new Event("Invalid Event", "End before start", start, end, "RED");
        });
    }
    
    @Test
    public void testNullTitleThrowsException() {
        LocalDateTime start = LocalDateTime.of(2025, 8, 27, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 8, 27, 11, 0);
        
        assertThrows(NullPointerException.class, () -> {
            new Event(null, "Description", start, end, "GREEN");
        });
    }
}