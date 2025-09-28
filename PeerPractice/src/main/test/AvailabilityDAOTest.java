import com.cab302.peerpractice.Model.DAOs.AvailabilityDAO;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.DAOs.UserDAO;
import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDAOTest {

    private AvailabilityDAO storage;
    private IUserDAO userDao;
    private User testUser1;
    private User testUser2;
    private Availability testAvailability1;
    private Availability testAvailability2;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        storage = new AvailabilityDAO(userDao);

        testUser1 = new User("John", "Doe", "johndoe_avail", "john.avail@example.com", "hashedpass1", "Test University");
        testUser2 = new User("Jane", "Smith", "janesmith_avail", "jane.avail@example.com", "hashedpass2", "Test University");

        userDao.addUser(testUser1);
        userDao.addUser(testUser2);

        testAvailability1 = new Availability("Morning Study", testUser1,
                LocalDateTime.of(2024, 10, 15, 9, 0),
                LocalDateTime.of(2024, 10, 15, 11, 0),
                "GREEN");

        testAvailability2 = new Availability("Afternoon Review", testUser2,
                LocalDateTime.of(2024, 10, 15, 14, 0),
                LocalDateTime.of(2024, 10, 15, 16, 0),
                "BLUE");
    }

    @AfterEach
    void tearDown() {
        storage.clearAllAvailabilities();
        try {
            userDao.deleteUser(testUser1.getUserId());
            userDao.deleteUser(testUser2.getUserId());
        } catch (Exception ignored) {}
    }

    @Test
    void testAddAvailability() {
        boolean added = storage.addAvailability(testAvailability1);
        assertTrue(added);

        List<Availability> all = storage.getAllAvailabilities();
        assertEquals(1, all.size());
        assertEquals("Morning Study", all.get(0).getTitle());
    }

    @Test
    void testRemoveAvailability() {
        storage.addAvailability(testAvailability1);
        boolean removed = storage.removeAvailability(testAvailability1);
        assertTrue(removed);

        List<Availability> all = storage.getAllAvailabilities();
        assertTrue(all.isEmpty());
    }

    @Test
    void testRemoveAvailabilityNotInDB() {
        boolean removed = storage.removeAvailability(testAvailability1);
        assertFalse(removed);
    }

    @Test
    void testGetAllAvailabilities() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);

        List<Availability> all = storage.getAllAvailabilities();
        assertEquals(2, all.size());
    }

    @Test
    void testGetAvailabilitiesForDate() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);

        LocalDate testDate = LocalDate.of(2024, 10, 15);
        assertEquals(2, storage.getAvailabilitiesForDate(testDate).size());

        LocalDate differentDate = LocalDate.of(2024, 10, 16);
        assertTrue(storage.getAvailabilitiesForDate(differentDate).isEmpty());
    }

    @Test
    void testGetAvailabilitiesForUser() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);

        assertEquals(1, storage.getAvailabilitiesForUser(testUser1).size());
        assertEquals(1, storage.getAvailabilitiesForUser(testUser2).size());
    }

    @Test
    void testGetAvailabilitiesForWeek() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);

        LocalDate weekStart = LocalDate.of(2024, 10, 14);
        assertEquals(2, storage.getAvailabilitiesForWeek(weekStart).size());

        LocalDate differentWeek = LocalDate.of(2024, 10, 21);
        assertTrue(storage.getAvailabilitiesForWeek(differentWeek).isEmpty());
    }

    @Test
    void testGetAvailabilitiesForUsers() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);

        List<User> users = List.of(testUser1, testUser2);
        assertEquals(2, storage.getAvailabilitiesForUsers(users, LocalDate.of(2024, 10, 14)).size());

        List<User> singleUser = List.of(testUser1);
        assertEquals(1, storage.getAvailabilitiesForUsers(singleUser, LocalDate.of(2024, 10, 14)).size());

        List<User> emptyUsers = new ArrayList<>();
        assertTrue(storage.getAvailabilitiesForUsers(emptyUsers, LocalDate.of(2024, 10, 14)).isEmpty());
    }

    @Test
    void testUpdateAvailability() {
        storage.addAvailability(testAvailability1);

        testAvailability1.setTitle("Updated Morning Study");
        testAvailability1.setDescription("Updated description");
        testAvailability1.setStartTime(LocalDateTime.of(2024, 10, 15, 10, 0));
        testAvailability1.setEndTime(LocalDateTime.of(2024, 10, 15, 12, 0));
        testAvailability1.setColorLabel("RED");

        assertTrue(storage.updateAvailability(testAvailability1, testAvailability1));

        List<Availability> all = storage.getAllAvailabilities();
        assertEquals(1, all.size());
        assertEquals("Updated Morning Study", all.get(0).getTitle());
    }


    @Test
    void testUpdateAvailabilityNonExistent() {
        boolean updated = storage.updateAvailability(testAvailability1, testAvailability2);
        assertFalse(updated);
    }

    @Test
    void testClearAllAvailabilities() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);
        storage.clearAllAvailabilities();
        assertTrue(storage.getAllAvailabilities().isEmpty());
    }

    @Test
    void testClearAllAvailabilitiesOnEmptyTable() {
        storage.clearAllAvailabilities(); // should not throw
        assertTrue(storage.getAllAvailabilities().isEmpty());
    }

    @Test
    void testGetAvailabilityCount() {
        assertEquals(0, storage.getAvailabilityCount());
        storage.addAvailability(testAvailability1);
        assertEquals(1, storage.getAvailabilityCount());
    }

    @Test
    void testHasAvailabilityOnDate() {
        storage.addAvailability(testAvailability1);

        assertTrue(storage.hasAvailabilityOnDate(testUser1, LocalDate.of(2024, 10, 15)));
        assertFalse(storage.hasAvailabilityOnDate(testUser2, LocalDate.of(2024, 10, 15)));
    }

    @Test
    void testHasAvailabilityOnDateWithNullUser() {
        assertFalse(storage.hasAvailabilityOnDate(null, LocalDate.of(2024, 10, 15)));
    }

    @Test
    void testAddNullAvailability() {
        assertFalse(storage.addAvailability(null));
    }

    @Test
    void testRemoveNullAvailability() {
        assertFalse(storage.removeAvailability(null));
    }

    @Test
    void testUpdateWithNullAvailability() {
        storage.addAvailability(testAvailability1);
        assertFalse(storage.updateAvailability(null, testAvailability2));
        assertFalse(storage.updateAvailability(testAvailability1, null));
    }

    @Test
    void testAddMultipleAvailabilities() {
        for (int i = 0; i < 5; i++) {
            Availability a = new Availability("Session " + i, testUser1,
                    LocalDateTime.of(2024, 10, 15, 8 + i, 0),
                    LocalDateTime.of(2024, 10, 15, 9 + i, 0),
                    "GREEN");
            storage.addAvailability(a);
        }
        assertEquals(5, storage.getAvailabilityCount());
    }
}
