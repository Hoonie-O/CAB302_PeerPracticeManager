package com.cab302.peerpractice.Model.managers;

import com.cab302.peerpractice.Model.daos.ISessionCalendarDAO;
import com.cab302.peerpractice.Model.entities.Group;
import com.cab302.peerpractice.Model.entities.Session;
import com.cab302.peerpractice.Model.entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Manager for session calendar logic. Wraps an ISessionCalendarDAO
 * so the underlying storage (DB or in-memory) can be swapped easily.
 */
public class SessionCalendarManager {
    private final ISessionCalendarDAO storage;
    private SessionTaskManager sessionTaskManager;

    public SessionCalendarManager(ISessionCalendarDAO storage) {
        this.storage = storage;
    }

    public void setSessionTaskManager(SessionTaskManager sessionTaskManager) {
        this.sessionTaskManager = sessionTaskManager;
    }

    public boolean createSession(String title, User organiser, LocalDateTime startTime,
                                 LocalDateTime endTime, String colorLabel) {
        try {
            Session session = new Session(title, organiser, startTime, endTime);
            session.setColorLabel(colorLabel);
            return storage.addSession(session);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean addSession(Session session) {
        return storage.addSession(session);
    }

    public boolean addSession(Session session, Group group) {
        if (session != null) {
            session.setGroup(group);
        }
        return storage.addSession(session);
    }

    public List<Session> getAllSessions() {
        return storage.getAllSessions();
    }

    public List<Session> getSessionsForDate(LocalDate date) {
        return storage.getSessionsForDate(date);
    }

    public List<Session> getSessionsForUser(User user) {
        return storage.getSessionsForUser(user);
    }

    public List<Session> getSessionsForWeek(LocalDate startOfWeek) {
        return storage.getSessionsForWeek(startOfWeek);
    }

    public List<Session> getSessionsForMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        return storage.getSessionsForDateRange(startOfMonth, endOfMonth);
    }

    public boolean removeSession(Session session) {
        if (session != null && session.getSessionId() != null && sessionTaskManager != null) {
            sessionTaskManager.deleteAllTasksForSession(session.getSessionId());
        }
        return storage.removeSession(session);
    }

    public boolean updateSession(Session oldSession, Session newSession) {
        return storage.updateSession(oldSession, newSession);
    }

    public void clearAllSessions() {
        storage.clearAllSessions();
    }

    public int getSessionCount() {
        return storage.getSessionCount();
    }

    public boolean hasSessionsOnDate(LocalDate date) {
        return storage.hasSessionsOnDate(date);
    }

    public void deleteSession(Session session) {
        if (session != null && session.getSessionId() != null && sessionTaskManager != null) {
            sessionTaskManager.deleteAllTasksForSession(session.getSessionId());
        }
        storage.removeSession(session);
    }

    public List<Session> getSessionsForGroup(Group group) {
        return storage.getSessionsForGroup(group);
    }

    public List<Session> getSessionsForDateAndGroup(LocalDate date, Group group) {
        return storage.getSessionsForDateAndGroup(date, group);
    }
}
