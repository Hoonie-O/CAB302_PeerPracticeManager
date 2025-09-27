package com.cab302.peerpractice.test;

import com.cab302.peerpractice.Model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AvailabilityDBStorageTest {

    private AvailabilityDBStorage storage;
    private IUserDAO userDao;
    private User testUser1;
    private User testUser2;
    private Availability testAvailability1;
    private Availability testAvailability2;

    @BeforeEach
    void setUp() throws SQLException {
        userDao = new UserDAO();
        storage = new AvailabilityDBStorage(userDao);
        
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
        } catch (Exception e) {
        }
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
        List<Availability> forDate = storage.getAvailabilitiesForDate(testDate);
        assertEquals(2, forDate.size());
        
        LocalDate differentDate = LocalDate.of(2024, 10, 16);
        List<Availability> forDifferentDate = storage.getAvailabilitiesForDate(differentDate);
        assertTrue(forDifferentDate.isEmpty());
    }

    @Test
    void testGetAvailabilitiesForUser() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);
        
        List<Availability> user1Availabilities = storage.getAvailabilitiesForUser(testUser1);
        assertEquals(1, user1Availabilities.size());
        assertEquals("Morning Study", user1Availabilities.get(0).getTitle());
        
        List<Availability> user2Availabilities = storage.getAvailabilitiesForUser(testUser2);
        assertEquals(1, user2Availabilities.size());
        assertEquals("Afternoon Review", user2Availabilities.get(0).getTitle());
    }

    @Test
    void testGetAvailabilitiesForWeek() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);
        
        LocalDate weekStart = LocalDate.of(2024, 10, 14);
        List<Availability> weekAvailabilities = storage.getAvailabilitiesForWeek(weekStart);
        assertEquals(2, weekAvailabilities.size());
        
        LocalDate differentWeek = LocalDate.of(2024, 10, 21);
        List<Availability> differentWeekAvailabilities = storage.getAvailabilitiesForWeek(differentWeek);
        assertTrue(differentWeekAvailabilities.isEmpty());
    }

    @Test
    void testGetAvailabilitiesForUsers() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);
        
        List<User> users = List.of(testUser1, testUser2);
        LocalDate weekStart = LocalDate.of(2024, 10, 14);
        
        List<Availability> multiUserAvailabilities = storage.getAvailabilitiesForUsers(users, weekStart);
        assertEquals(2, multiUserAvailabilities.size());
        
        List<User> singleUser = List.of(testUser1);
        List<Availability> singleUserAvailabilities = storage.getAvailabilitiesForUsers(singleUser, weekStart);
        assertEquals(1, singleUserAvailabilities.size());
        assertEquals("Morning Study", singleUserAvailabilities.get(0).getTitle());
    }

    @Test
    void testGetAvailabilitiesForEmptyUsersList() {
        storage.addAvailability(testAvailability1);
        
        List<User> emptyUsers = new ArrayList<>();
        LocalDate weekStart = LocalDate.of(2024, 10, 14);
        
        List<Availability> result = storage.getAvailabilitiesForUsers(emptyUsers, weekStart);
        assertTrue(result.isEmpty());
    }

    @Test
    void testUpdateAvailability() {
        storage.addAvailability(testAvailability1);
        
        Availability updatedAvailability = new Availability("Updated Morning Study", testUser1,
                                                           LocalDateTime.of(2024, 10, 15, 10, 0),
                                                           LocalDateTime.of(2024, 10, 15, 12, 0),
                                                           "RED");
        updatedAvailability.setDescription("Updated description");
        
        boolean updated = storage.updateAvailability(testAvailability1, updatedAvailability);
        assertTrue(updated);
        
        List<Availability> all = storage.getAllAvailabilities();
        assertEquals(1, all.size());
        assertEquals("Updated Morning Study", all.get(0).getTitle());
    }

    @Test
    void testClearAllAvailabilities() {
        storage.addAvailability(testAvailability1);
        storage.addAvailability(testAvailability2);
        
        assertEquals(2, storage.getAllAvailabilities().size());
        
        storage.clearAllAvailabilities();
        
        assertTrue(storage.getAllAvailabilities().isEmpty());
    }

    @Test
    void testGetAvailabilityCount() {
        assertEquals(0, storage.getAvailabilityCount());
        
        storage.addAvailability(testAvailability1);
        assertEquals(1, storage.getAvailabilityCount());
        
        storage.addAvailability(testAvailability2);
        assertEquals(2, storage.getAvailabilityCount());
    }

    @Test
    void testHasAvailabilityOnDate() {
        storage.addAvailability(testAvailability1);
        
        LocalDate testDate = LocalDate.of(2024, 10, 15);
        assertTrue(storage.hasAvailabilityOnDate(testUser1, testDate));
        assertFalse(storage.hasAvailabilityOnDate(testUser2, testDate));
        
        LocalDate differentDate = LocalDate.of(2024, 10, 16);
        assertFalse(storage.hasAvailabilityOnDate(testUser1, differentDate));
    }

    @Test
    void testAddNullAvailability() {
        boolean added = storage.addAvailability(null);
        assertFalse(added);
    }

    @Test
    void testRemoveNullAvailability() {
        boolean removed = storage.removeAvailability(null);
        assertFalse(removed);
    }

    @Test
    void testUpdateWithNullAvailability() {
        storage.addAvailability(testAvailability1);
        
        boolean updated = storage.updateAvailability(null, testAvailability2);
        assertFalse(updated);
        
        updated = storage.updateAvailability(testAvailability1, null);
        assertFalse(updated);
    }
}