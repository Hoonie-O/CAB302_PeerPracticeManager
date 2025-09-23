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

    @Test
    void testNullTitleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Availability(null, testUser, startTime, endTime, "GREEN");
        });
    }

    @Test
    void testEmptyTitleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Availability("", testUser, startTime, endTime, "GREEN");
        });
    }

    @Test
    void testBlankTitleThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Availability("   ", testUser, startTime, endTime, "GREEN");
        });
    }

    @Test
    void testNullStartTimeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Availability("Valid Title", testUser, null, endTime, "GREEN");
        });
    }

    @Test
    void testNullEndTimeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Availability("Valid Title", testUser, startTime, null, "GREEN");
        });
    }

    @Test
    void testSameStartEndTimeThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            new Availability("Same Time", testUser, startTime, startTime, "GREEN");
        });
    }

    @Test
    void testSetTitle() {
        Availability availability = new Availability("Original", testUser, startTime, endTime, "GREEN");
        availability.setTitle("New Title");
        assertEquals("New Title", availability.getTitle());
    }

    @Test
    void testSetTitleNull() {
        Availability availability = new Availability("Original", testUser, startTime, endTime, "GREEN");
        assertThrows(NullPointerException.class, () -> availability.setTitle(null));
    }

    @Test
    void testSetTitleEmpty() {
        Availability availability = new Availability("Original", testUser, startTime, endTime, "GREEN");
        assertThrows(IllegalArgumentException.class, () -> availability.setTitle(""));
    }

    @Test
    void testSetStartTime() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        LocalDateTime newStart = startTime.plusHours(1);
        availability.setStartTime(newStart);
        assertEquals(newStart, availability.getStartTime());
    }

    @Test
    void testSetStartTimeNull() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        assertThrows(NullPointerException.class, () -> availability.setStartTime(null));
    }

    @Test
    void testSetStartTimeAfterEndTime() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        LocalDateTime invalidStart = endTime.plusHours(1);
        assertThrows(IllegalArgumentException.class, () -> availability.setStartTime(invalidStart));
    }

    @Test
    void testSetEndTime() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        LocalDateTime newEnd = endTime.plusHours(1);
        availability.setEndTime(newEnd);
        assertEquals(newEnd, availability.getEndTime());
    }

    @Test
    void testSetEndTimeNull() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        assertThrows(NullPointerException.class, () -> availability.setEndTime(null));
    }

    @Test
    void testSetEndTimeBeforeStartTime() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        LocalDateTime invalidEnd = startTime.minusHours(1);
        assertThrows(IllegalArgumentException.class, () -> availability.setEndTime(invalidEnd));
    }

    @Test
    void testSetColorLabel() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        availability.setColorLabel("RED");
        assertEquals("RED", availability.getColorLabel());
    }

    @Test
    void testSetColorLabelNull() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        assertDoesNotThrow(() -> availability.setColorLabel(null));
        assertEquals(availability.getColorLabel(), "BLUE"); // Default value
    }

    @Test
    void testSetColorLabelEmpty() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        assertDoesNotThrow(() -> availability.setColorLabel(""));
        assertEquals("", availability.getColorLabel());
    }

    @Test
    void testRecurringPatternValues() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");

        String[] patterns = {"DAILY", "WEEKLY", "MONTHLY", "YEARLY", "FORTNIGHTLY"};
        for (String pattern : patterns) {
            availability.setRecurringPattern(pattern);
            assertEquals(pattern, availability.getRecurringPattern());
            assertTrue(availability.isRecurring());
        }
    }

    @Test
    void testSetRecurringPatternNull() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        availability.setRecurringPattern(null);
        assertEquals("NONE", availability.getRecurringPattern());
        assertFalse(availability.isRecurring());
    }

    @Test
    void testSetRecurringPatternEmpty() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        availability.setRecurringPattern("");
        assertEquals("NONE", availability.getRecurringPattern());
        assertFalse(availability.isRecurring());
    }

    @Test
    void testSetRecurringPatternNone() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        availability.setRecurringPattern("WEEKLY");
        assertTrue(availability.isRecurring());

        availability.setRecurringPattern("NONE");
        assertFalse(availability.isRecurring());
    }


    @Test
    void testToString() {
        Availability availability = new Availability("Study Session", testUser, startTime, endTime, "GREEN");
        String availString = availability.toString();
        assertNotNull(availString);
        assertTrue(availString.contains("Study Session"));
    }

    @Test
    void testEquals() {
        Availability availability1 = new Availability("Test", testUser, startTime, endTime, "GREEN");
        Availability availability2 = new Availability("Test", testUser, startTime, endTime, "GREEN");
        assertEquals(availability1, availability2);
    }

    @Test
    void testHashCode() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "GREEN");
        int hashCode = availability.hashCode();
        assertTrue(hashCode != 0);
    }

    @Test
    void testColorLabelCaseInsensitive() {
        Availability availability = new Availability("Test", testUser, startTime, endTime, "green");
        assertEquals("green", availability.getColorLabel());
    }

    @Test
    void testMinimalDuration() {
        LocalDateTime minimalEnd = startTime.plusMinutes(1);
        assertDoesNotThrow(() -> new Availability("Minimal", testUser, startTime, minimalEnd, "GREEN"));
    }

    @Test
    void testMaximalDuration() {
        LocalDateTime maximalEnd = startTime.plusDays(365);
        assertDoesNotThrow(() -> new Availability("Year Long", testUser, startTime, maximalEnd, "GREEN"));
    }

    @Test
    void testTitleWithSpecialCharacters() {
        String specialTitle = "Study Session #1 - Math & Physics (2024)!";
        assertDoesNotThrow(() -> new Availability(specialTitle, testUser, startTime, endTime, "GREEN"));
    }

    @Test
    void testLongTitle() {
        String longTitle = "This is a very long availability title that contains many words and should be handled properly by the system".repeat(3);
        assertDoesNotThrow(() -> new Availability(longTitle, testUser, startTime, endTime, "GREEN"));
    }

}