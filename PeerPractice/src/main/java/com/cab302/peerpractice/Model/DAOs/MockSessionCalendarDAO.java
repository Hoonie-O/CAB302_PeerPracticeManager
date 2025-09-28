package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.Session;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mock (in-memory) implementation of ISessionCalendarDAO for testing without a DB.
 */
public class MockSessionCalendarDAO implements ISessionCalendarDAO {

    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    // -------------------- DAO METHODS --------------------
    @Override
    public boolean addSession(Session session) {
        if (session == null || session.getSessionId() == null) return false;
        sessions.put(session.getSessionId(), session);
        return true;
    }

    @Override
    public boolean removeSession(Session session) {
        if (session == null) return false;
        return sessions.remove(session.getSessionId()) != null;
    }

    @Override
    public List<Session> getAllSessions() {
        return new ArrayList<>(sessions.values());
    }

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

    @Override
    public boolean updateSession(Session oldSession, Session newSession) {
        if (oldSession == null || newSession == null) return false;
        if (!sessions.containsKey(oldSession.getSessionId())) return false;
        sessions.put(oldSession.getSessionId(), newSession);
        return true;
    }

    @Override
    public void clearAllSessions() {
        sessions.clear();
    }

    @Override
    public int getSessionCount() {
        return sessions.size();
    }

    @Override
    public boolean hasSessionsOnDate(LocalDate date) {
        return sessions.values().stream()
                .anyMatch(s -> s.getStartTime().toLocalDate().equals(date));
    }

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
