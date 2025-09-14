import com.cab302.peerpractice.Model.Availability;
import com.cab302.peerpractice.Model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AvailabilityTest {
    private User testUser;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        testUser = new User("Test", "User", "testuser", "test@test.com", "hashedpass", "Test Uni");
        startTime = LocalDateTime.of(2024, 1, 15, 9, 0);
        endTime = LocalDateTime.of(2024, 1, 15, 17, 0);
    }

    @Test
    void testAvailabilityCreation() {
        Availability availability = new Availability("Study Session", testUser, startTime, endTime, "GREEN");
        
        assertEquals("Study Session", availability.getTitle());
        assertEquals(testUser, availability.getUser());
        assertEquals(startTime, availability.getStartTime());
        assertEquals(endTime, availability.getEndTime());
        assertEquals("GREEN", availability.getColorLabel());
        assertFalse(availability.isRecurring());
        assertEquals("NONE", availability.getRecurringPattern());
    }

    @Test
    void testRecurringAvailability() {
        Availability availability = new Availability("Weekly Study", testUser, startTime, endTime, "BLUE");
        availability.setRecurringPattern("WEEKLY");
        
        assertTrue(availability.isRecurring());
        assertEquals("WEEKLY", availability.getRecurringPattern());
    }

    @Test
    void testInvalidTimeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Availability("Invalid", testUser, endTime, startTime, "RED"); // end before start
        });
    }

    @Test
    void testNullUserThrowsException() {
        assertThrows(NullPointerException.class, () -> {
            new Availability("No User", null, startTime, endTime, "BLUE");
        });
    }
}