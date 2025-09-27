package com.cab302.peerpractice.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SessionCalendarManager {
    private final SessionCalendarStorage storage;
    private SessionTaskManager sessionTaskManager;

    public SessionCalendarManager() {
        this.storage = new SessionCalendarStorage();
    }

    public SessionCalendarManager(SessionCalendarStorage storage) {
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
        if (session != null && session.getSessionId() != null) {
            if (sessionTaskManager != null) {
                sessionTaskManager.deleteAllTasksForSession(session.getSessionId());
            }
        }
        storage.removeSession(session);
    }

    public List<Session> getSessionsForGroup(Group group) {
        return storage.getAllSessions().stream()
                .filter(s -> s.getGroup() != null && s.getGroup().equals(group))
                .collect(Collectors.toList());
    }

    public List<Session> getSessionsForDateAndGroup(LocalDate date, Group group) {
        return storage.getSessionsForDate(date).stream()
                .filter(s -> s.getGroup() != null && s.getGroup().equals(group))
                .collect(Collectors.toList());
    }

    public boolean addSession(Session session, Group group) {
        if (session != null) {
            session.setGroup(group);
        }
        return storage.addSession(session);
    }
}