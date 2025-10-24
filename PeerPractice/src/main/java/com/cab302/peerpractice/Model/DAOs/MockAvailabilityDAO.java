package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * <hr>
 * Mock (in-memory) implementation of availability Data Access Object for unit testing.
 *
 * <p>This class provides an in-memory implementation of IAvailabilityDAO
 * suitable for unit testing without requiring a physical database connection.
 * It simulates database operations using concurrent hash maps for thread-safe
 * testing scenarios.
 *
 * <p> Key features include:
 * <ul>
 *   <li>In-memory data storage using concurrent collections</li>
 *   <li>Thread-safe operations for parallel testing</li>
 *   <li>Identical interface to production DAO</li>
 *   <li>Automatic ID generation for test entities</li>
 * </ul>
 *
 * @see IAvailabilityDAO
 * @see Availability
 * @see User
 */
public class MockAvailabilityDAO implements IAvailabilityDAO {

    /** <hr> In-memory storage for availability entities using concurrent map for thread safety. */
    private final Map<String, Availability> availabilities = new ConcurrentHashMap<>();

    /**
     * <hr>
     * Constructs a new MockAvailabilityDAO with user DAO dependency.
     *
     * <p>Initializes the mock DAO with the provided user DAO for user entity
     * resolution, maintaining compatibility with the production DAO interface.
     *
     * @param userDao the User DAO for user entity operations (unused in mock)
     */
    public MockAvailabilityDAO(IUserDAO userDao) {
    }

    /**
     * <hr>
     * Generates a unique identifier for new availability entities.
     *
     * <p>Creates a random UUID string to serve as primary key for mock
     * availability entities, simulating database auto-generation.
     *
     * @return a unique identifier string
     */
    private String generateId() {
        return UUID.randomUUID().toString();
    }

    // -------------------- CREATE --------------------

    /**
     * <hr>
     * Adds a new availability slot to in-memory storage.
     *
     * <p>Persists an availability entity to the mock storage with automatic
     * ID generation if not provided, simulating database insert operations
     * for testing scenarios.
     *
     * @param availability the Availability object to be stored
     * @return true if the availability was successfully added, false otherwise
     */
    @Override
    public boolean addAvailability(Availability availability) {
        if (availability == null || availability.getUser() == null) {
            return false;
        }

        String id = availability.getAvailabilityId();
        if (id == null || id.isBlank()) {
            // Require test code to set it in constructor
            id = UUID.randomUUID().toString();
            // Recreate availability with ID if needed
            availability = new Availability(
                    id,
                    availability.getTitle(),
                    availability.getUser(),
                    availability.getStartTime(),
                    availability.getEndTime(),
                    availability.getColorLabel()
            );
            availability.setDescription(availability.getDescription());
        }

        availabilities.put(id, availability);
        return true;
    }

    // -------------------- DELETE --------------------

    /**
     * <hr>
     * Removes an existing availability slot from in-memory storage.
     *
     * <p>Deletes a specific availability record using its unique identifier,
     * simulating database delete operations for testing scenarios.
     *
     * @param availability the Availability object to be removed
     * @return true if the availability was successfully removed, false otherwise
     */
    @Override
    public boolean removeAvailability(Availability availability) {
        if (availability == null) return false;
        return availabilities.remove(availability.getAvailabilityId()) != null;
    }

    /**
     * <hr>
     * Removes all availability slots from in-memory storage.
     *
     * <p>Clears the entire mock availability storage, typically used between
     * test cases to ensure test isolation and clean state.
     */
    @Override
    public void clearAllAvailabilities() {
        availabilities.clear();
    }

    // -------------------- READ --------------------

    /**
     * <hr>
     * Retrieves all availability slots from in-memory storage.
     *
     * <p>Fetches every availability entity stored in the mock storage,
     * providing comprehensive access to all test data for verification.
     *
     * @return a list of all Availability objects in mock storage
     */
    @Override
    public List<Availability> getAllAvailabilities() {
        return new ArrayList<>(availabilities.values());
    }

    /**
     * <hr>
     * Retrieves availability slots for a specific calendar date.
     *
     * <p>Fetches availability records that occur on the specified date from
     * mock storage, simulating date-based query operations for testing.
     *
     * @param date the date to retrieve availabilities for
     * @return a list of Availability objects occurring on the specified date
     */
    @Override
    public List<Availability> getAvailabilitiesForDate(LocalDate date) {
        return availabilities.values().stream()
                .filter(a -> a.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Retrieves all availability slots for a specific user.
     *
     * <p>Fetches the complete availability schedule for a particular user
     * from mock storage, simulating user-based query operations for testing.
     *
     * @param user the user whose availabilities are being retrieved
     * @return a list of Availability objects belonging to the specified user
     */
    @Override
    public List<Availability> getAvailabilitiesForUser(User user) {
        if (user == null) return Collections.emptyList();
        return availabilities.values().stream()
                .filter(a -> a.getUser().getUserId().equals(user.getUserId()))
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Retrieves availability slots for a specific week.
     *
     * <p>Fetches availability records that occur within the specified week
     * from mock storage, simulating weekly query operations for testing.
     *
     * @param startOfWeek the starting date of the week to retrieve availabilities for
     * @return a list of Availability objects occurring during the specified week
     */
    @Override
    public List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return availabilities.values().stream()
                .filter(a -> {
                    LocalDate date = a.getStartTime().toLocalDate();
                    return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Retrieves availability slots for multiple users during a specific week.
     *
     * <p>Fetches availability records for the specified list of users that
     * occur within the given week from mock storage, simulating complex
     * multi-user query operations for testing.
     *
     * @param users the list of users to retrieve availabilities for
     * @param startOfWeek the starting date of the week to search
     * @return a list of Availability objects for the specified users during the week
     */
    @Override
    public List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek) {
        if (users == null || users.isEmpty()) return Collections.emptyList();
        Set<String> userIds = users.stream().map(User::getUserId).collect(Collectors.toSet());
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return availabilities.values().stream()
                .filter(a -> userIds.contains(a.getUser().getUserId()))
                .filter(a -> {
                    LocalDate date = a.getStartTime().toLocalDate();
                    return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    /**
     * <hr>
     * Retrieves the total count of availability slots in mock storage.
     *
     * <p>Provides a quick count of all availability records in the mock
     * storage, useful for test assertions and verification.
     *
     * @return the total number of availability records in mock storage
     */
    @Override
    public int getAvailabilityCount() {
        return availabilities.size();
    }

    /**
     * <hr>
     * Checks if a user has any availability on a specific date.
     *
     * <p>Verifies whether the specified user has scheduled any availability
     * for the given date in mock storage, simulating existence check
     * operations for testing.
     *
     * @param user the user to check for availability
     * @param date the date to check for availability
     * @return true if the user has availability on the specified date, false otherwise
     */
    @Override
    public boolean hasAvailabilityOnDate(User user, LocalDate date) {
        if (user == null || date == null) return false;
        return availabilities.values().stream()
                .anyMatch(a -> a.getUser().getUserId().equals(user.getUserId())
                        && a.getStartTime().toLocalDate().equals(date));
    }

    // -------------------- UPDATE --------------------

    /**
     * <hr>
     * Updates an existing availability slot with new information.
     *
     * <p>Modifies the attributes of an existing availability record in
     * mock storage while preserving its identity, simulating database
     * update operations for testing.
     *
     * @param oldAvailability the original Availability object to be updated
     * @param newAvailability the Availability object containing updated information
     * @return true if the availability was successfully updated, false otherwise
     */
    @Override
    public boolean updateAvailability(Availability oldAvailability, Availability newAvailability) {
        if (oldAvailability == null || newAvailability == null) return false;
        if (!availabilities.containsKey(oldAvailability.getAvailabilityId())) return false;

        // Recreate with same ID
        Availability updated = new Availability(
                oldAvailability.getAvailabilityId(),
                newAvailability.getTitle(),
                newAvailability.getUser(),
                newAvailability.getStartTime(),
                newAvailability.getEndTime(),
                newAvailability.getColorLabel()
        );
        updated.setDescription(newAvailability.getDescription());

        availabilities.put(oldAvailability.getAvailabilityId(), updated);
        return true;
    }
}