package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <hr>
 * Mock (in-memory) implementation of ISessionCalendarDAO for testing without a DB.
 *
 * <p>This implementation provides in-memory storage for study sessions and calendar
 * functionality to facilitate testing without requiring a real database connection.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Thread-safe concurrent session storage</li>
 *   <li>Flexible session retrieval by date, user, group, and date ranges</li>
 *   <li>Support for weekly and custom date range queries</li>
 *   <li>Session counting and existence checking capabilities</li>
 * </ul>
 *
 * @see ISessionCalendarDAO
 * @see Session
 * @see Group
 * @see User
 */
public class MockSessionCalendarDAO implements ISessionCalendarDAO {

    /** <hr> In-memory storage for sessions by session ID. */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // -------------------- DAO METHODS --------------------

    /**
     * <hr>
     * Adds a new session to the in-memory storage.
     *
     * @param session the session to add
     * @return true if the session was added successfully
     */
    @Override
    public boolean addSession(Session session) {
        if (session == null || session.getSessionId() == null) return false;
        sessions.put(session.getSessionId(), session);
        return true;
    }

    /**
     * <hr>
     * Removes a session from the in-memory storage.
     *
     * @param session the session to remove
     * @return true if the session was removed successfully
     */
    @Override
    public boolean removeSession(Session session) {
        if (session == null) return false;
        return sessions.remove(session.getSessionId()) != null;
    }

    /**
     * <hr>
     * Retrieves all sessions from the in-memory storage.
     *
     * @return a list of all sessions
     */
    @Override
    public List<Session> getAllSessions() {
        return new ArrayList<>(sessions.values());
    }

    /**
     * <hr>
     * Retrieves all sessions scheduled for a specific date.
     *
     * @param date the date to retrieve sessions for
     * @return a list of sessions occurring on the specified date
     */
    @Override
    public List<Session> getSessionsForDate(LocalDate date) {
        List<Session> list = new ArrayList<>();
        for (Session s : sessions.values()) {
            if (s.getStartTime().toLocalDate().equals(date)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves all sessions that a specific user is participating in.
     *
     * @param user the user to retrieve sessions for
     * @return a list of sessions the user is participating in
     */
    @Override
    public List<Session> getSessionsForUser(User user) {
        if (user == null) return Collections.emptyList();
        List<Session> list = new ArrayList<>();
        for (Session s : sessions.values()) {
            if (s.getParticipants().contains(user)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves all sessions scheduled for a specific week.
     *
     * @param startOfWeek the starting date of the week
     * @return a list of sessions occurring during the specified week
     */
    @Override
    public List<Session> getSessionsForWeek(LocalDate startOfWeek) {
        LocalDate end = startOfWeek.plusDays(6);
        List<Session> list = new ArrayList<>();
        for (Session s : sessions.values()) {
            LocalDate d = s.getStartTime().toLocalDate();
            if (!d.isBefore(startOfWeek) && !d.isAfter(end)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves all sessions scheduled within a specific date range.
     *
     * @param startDate the start date of the range (inclusive)
     * @param endDate the end date of the range (inclusive)
     * @return a list of sessions occurring within the specified date range
     */
    @Override
    public List<Session> getSessionsForDateRange(LocalDate startDate, LocalDate endDate) {
        List<Session> list = new ArrayList<>();
        for (Session s : sessions.values()) {
            LocalDate d = s.getStartTime().toLocalDate();
            if (!d.isBefore(startDate) && !d.isAfter(endDate)) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * <hr>
     * Updates an existing session with new session data.
     *
     * @param oldSession the original session to update
     * @param newSession the new session data
     * @return true if the session was updated successfully
     */
    @Override
    public boolean updateSession(Session oldSession, Session newSession) {
        if (oldSession == null || newSession == null) return false;
        if (!sessions.containsKey(oldSession.getSessionId())) return false;
        sessions.put(oldSession.getSessionId(), newSession);
        return true;
    }

    /**
     * <hr>
     * Clears all sessions from the in-memory storage.
     *
     * <p>Used primarily for testing cleanup.
     */
    @Override
    public void clearAllSessions() {
        sessions.clear();
    }

    /**
     * <hr>
     * Gets the total number of sessions in the in-memory storage.
     *
     * @return the count of sessions
     */
    @Override
    public int getSessionCount() {
        return sessions.size();
    }

    /**
     * <hr>
     * Checks if any sessions exist on a specific date.
     *
     * @param date the date to check
     * @return true if at least one session exists on the specified date
     */
    @Override
    public boolean hasSessionsOnDate(LocalDate date) {
        return sessions.values().stream()
                .anyMatch(s -> s.getStartTime().toLocalDate().equals(date));
    }

    /**
     * <hr>
     * Retrieves all sessions associated with a specific group.
     *
     * @param group the group to retrieve sessions for
     * @return a list of sessions associated with the specified group
     */
    @Override
    public List<Session> getSessionsForGroup(Group group) {
        if (group == null) return Collections.emptyList();
        List<Session> list = new ArrayList<>();
        for (Session s : sessions.values()) {
            if (s.getGroup() != null && group.getID() == s.getGroup().getID()) {
                list.add(s);
            }
        }
        return list;
    }

    /**
     * <hr>
     * Retrieves all sessions for a specific group on a specific date.
     *
     * @param date the date to retrieve sessions for
     * @param group the group to retrieve sessions for
     * @return a list of sessions for the specified group and date
     */
    @Override
    public List<Session> getSessionsForDateAndGroup(LocalDate date, Group group) {
        if (group == null) return Collections.emptyList();
        List<Session> list = new ArrayList<>();
        for (Session s : sessions.values()) {
            if (s.getGroup() != null &&
                    group.getID() == s.getGroup().getID() &&
                    s.getStartTime().toLocalDate().equals(date)) {
                list.add(s);
            }
        }
        return list;
    }
}