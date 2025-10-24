package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.util.List;

/**
 * <hr>
 * Data Access Object interface for managing practice session scheduling and calendar operations.
 *
 * <p>This interface defines the contract for session calendar data operations,
 * providing methods to schedule, retrieve, and manage peer practice sessions
 * across various timeframes and organizational contexts.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Session scheduling and calendar management</li>
 *   <li>Multi-dimensional session querying (date, user, group)</li>
 *   <li>Date range and timeframe-based session retrieval</li>
 *   <li>Session statistics and existence checking</li>
 * </ul>
 *
 * @see Session
 * @see User
 * @see Group
 */
public interface ISessionCalendarDAO {
    /**
     * <hr>
     * Adds a new practice session to the calendar.
     *
     * <p>Persists a session entity to the database with all scheduling
     * details, participant information, and session metadata for
     * comprehensive calendar management.
     *
     * @param session the Session object to be added
     * @return true if the session was successfully added, false otherwise
     */
    boolean addSession(Session session);

    /**
     * <hr>
     * Removes an existing practice session from the calendar.
     *
     * <p>Deletes a session entity from the database, typically when
     * a session is cancelled or needs to be rescheduled.
     *
     * @param session the Session object to be removed
     * @return true if the session was successfully removed, false otherwise
     */
    boolean removeSession(Session session);

    /**
     * <hr>
     * Retrieves all practice sessions from the system.
     *
     * <p>Fetches every session entity stored in the database across
     * all timeframes and contexts, providing comprehensive system-wide
     * session access for administrative purposes.
     *
     * @return a list of all Session objects in the system
     */
    List<Session> getAllSessions();

    /**
     * <hr>
     * Retrieves all practice sessions for a specific date.
     *
     * <p>Fetches sessions scheduled for a particular calendar date,
     * enabling daily calendar views and date-specific scheduling
     * overviews.
     *
     * @param date the date to retrieve sessions for
     * @return a list of Session objects occurring on the specified date
     */
    List<Session> getSessionsForDate(LocalDate date);

    /**
     * <hr>
     * Retrieves all practice sessions for a specific user.
     *
     * <p>Fetches the complete session schedule for a particular user,
     * showing all sessions they are participating in across all
     * timeframes and groups.
     *
     * @param user the user whose sessions are being retrieved
     * @return a list of Session objects involving the specified user
     */
    List<Session> getSessionsForUser(User user);

    /**
     * <hr>
     * Retrieves all practice sessions for a specific week.
     *
     * <p>Fetches sessions scheduled within the specified week
     * (starting from the provided date), supporting weekly
     * calendar views and weekly planning overviews.
     *
     * @param startOfWeek the starting date of the week to retrieve sessions for
     * @return a list of Session objects occurring during the specified week
     */
    List<Session> getSessionsForWeek(LocalDate startOfWeek);

    /**
     * <hr>
     * Retrieves all practice sessions for a specific date range.
     *
     * <p>Fetches sessions scheduled within a custom date range,
     * providing flexible timeframe-based session retrieval for
     * advanced calendar queries and reporting.
     *
     * @param startDate the starting date of the range
     * @param endDate the ending date of the range
     * @return a list of Session objects occurring within the specified date range
     */
    List<Session> getSessionsForDateRange(LocalDate startDate, LocalDate endDate);

    /**
     * <hr>
     * Updates an existing practice session with new information.
     *
     * <p>Modifies the attributes of an existing session record while
     * preserving its identity, allowing for session rescheduling
     * and detail updates without creating new entries.
     *
     * @param oldSession the original Session object to be updated
     * @param newSession the Session object containing updated information
     * @return true if the session was successfully updated, false otherwise
     */
    boolean updateSession(Session oldSession, Session newSession);

    /**
     * <hr>
     * Removes all practice sessions from the system.
     *
     * <p>Clears the entire session calendar database, typically used
     * for system maintenance, testing scenarios, or administrative
     * resets.
     */
    void clearAllSessions();

    /**
     * <hr>
     * Retrieves the total count of practice sessions in the system.
     *
     * <p>Provides a quick statistical count of all session records,
     * useful for system monitoring, reporting, and administrative
     * dashboards.
     *
     * @return the total number of session records in the system
     */
    int getSessionCount();

    /**
     * <hr>
     * Checks if any sessions exist on a specific date.
     *
     * <p>Provides a quick existence check to determine if any
     * sessions are scheduled for a particular date, useful for
     * calendar indicators and quick scheduling assessments.
     *
     * @param date the date to check for sessions
     * @return true if any sessions exist on the specified date, false otherwise
     */
    boolean hasSessionsOnDate(LocalDate date);

    // === Extra Filtering Methods ===

    /**
     * <hr>
     * Retrieves all practice sessions for a specific group.
     *
     * <p>Fetches sessions associated with a particular group,
     * enabling group-specific session management and
     * collaborative scheduling within group contexts.
     *
     * @param group the group whose sessions are being retrieved
     * @return a list of Session objects associated with the specified group
     */
    List<Session> getSessionsForGroup(Group group);

    /**
     * <hr>
     * Retrieves practice sessions for a specific date and group combination.
     *
     * <p>Fetches sessions that occur on a particular date and are
     * associated with a specific group, providing highly targeted
     * session queries for precise calendar management.
     *
     * @param date the date to retrieve sessions for
     * @param group the group to filter sessions by
     * @return a list of Session objects matching both date and group criteria
     */
    List<Session> getSessionsForDateAndGroup(LocalDate date, Group group);
}