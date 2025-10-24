package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing user availability schedules.
 *
 * <p>This interface defines the contract for availability data operations,
 * providing methods to create, retrieve, update, and delete availability
 * slots for users in the peer practice system.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Comprehensive availability lifecycle management</li>
 *   <li>Flexible querying by date, user, and time ranges</li>
 *   <li>Multi-user availability coordination for session planning</li>
 *   <li>Statistical and existence checking operations</li>
 * </ul>
 *
 * @see Availability
 * @see User
 * @see AvailabilityDAO
 */
public interface IAvailabilityDAO {
    /**
     * <hr>
     * Adds a new availability slot to persistent storage.
     *
     * <p>Persists a complete availability entity including time range,
     * user association, and descriptive metadata for calendar display
     * and session scheduling purposes.
     *
     * @param availability the Availability object to be stored
     * @return true if the availability was successfully added, false otherwise
     */
    boolean addAvailability(Availability availability);

    /**
     * <hr>
     * Removes an existing availability slot from persistent storage.
     *
     * <p>Deletes a specific availability record, typically when a user
     * needs to cancel or modify their available time slots.
     *
     * @param availability the Availability object to be removed
     * @return true if the availability was successfully removed, false otherwise
     */
    boolean removeAvailability(Availability availability);

    /**
     * <hr>
     * Retrieves all availability slots from the system.
     *
     * <p>Fetches every availability record across all users, providing
     * a comprehensive view of all scheduled availability for administrative
     * oversight or system-wide analysis.
     *
     * @return a list of all Availability objects in the system
     */
    List<Availability> getAllAvailabilities();

    /**
     * <hr>
     * Retrieves availability slots for a specific calendar date.
     *
     * <p>Fetches all availability records that occur on the specified date,
     * regardless of time or user, enabling daily calendar views and
     * date-specific scheduling operations.
     *
     * @param date the date to retrieve availabilities for
     * @return a list of Availability objects occurring on the specified date
     */
    List<Availability> getAvailabilitiesForDate(LocalDate date);

    /**
     * <hr>
     * Retrieves all availability slots for a specific user.
     *
     * <p>Fetches the complete availability schedule for an individual user,
     * showing all time periods when they have indicated availability for
     * peer practice sessions.
     *
     * @param user the user whose availabilities are being retrieved
     * @return a list of Availability objects belonging to the specified user
     */
    List<Availability> getAvailabilitiesForUser(User user);

    /**
     * <hr>
     * Retrieves availability slots for a specific calendar week.
     *
     * <p>Fetches all availability records that fall within the specified
     * week (starting from the provided date), supporting weekly planning
     * views and longer-term scheduling coordination.
     *
     * @param startOfWeek the starting date of the week to search
     * @return a list of Availability objects occurring during the specified week
     */
    List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek);

    /**
     * <hr>
     * Retrieves availability slots for multiple users during a specific week.
     *
     * <p>Fetches availability records for the specified list of users that
     * occur within the given week, enabling comparison of schedules for
     * coordinated session planning and availability matching.
     *
     * @param users the list of users to retrieve availabilities for
     * @param startOfWeek the starting date of the week to search
     * @return a list of Availability objects for the specified users during the week
     */
    List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek);

    /**
     * <hr>
     * Updates an existing availability slot with new information.
     *
     * <p>Modifies the attributes of an existing availability record while
     * preserving its identity, allowing users to adjust their availability
     * details without creating new database entries.
     *
     * @param oldAvailability the original Availability object to be updated
     * @param newAvailability the Availability object containing updated information
     * @return true if the availability was successfully updated, false otherwise
     */
    boolean updateAvailability(Availability oldAvailability, Availability newAvailability);

    /**
     * <hr>
     * Removes all availability slots from the system.
     *
     * <p>Clears the entire availability database, typically used for
     * system maintenance, testing scenarios, or administrative resets.
     */
    void clearAllAvailabilities();

    /**
     * <hr>
     * Retrieves the total count of availability slots in the system.
     *
     * <p>Provides a quick statistical count of all availability records,
     * useful for system monitoring, reporting, and administrative dashboards.
     *
     * @return the total number of availability records in the system
     */
    int getAvailabilityCount();

    /**
     * <hr>
     * Checks if a user has any availability on a specific date.
     *
     * <p>Provides a quick existence check to determine if a user has
     * scheduled any availability slots for a particular date, useful
     * for calendar indicators and quick availability assessments.
     *
     * @param user the user to check for availability
     * @param date the date to check for availability
     * @return true if the user has availability on the specified date, false otherwise
     */
    boolean hasAvailabilityOnDate(User user, LocalDate date);
}