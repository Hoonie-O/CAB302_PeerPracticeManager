package com.cab302.peerpractice.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class SessionCalendarManager {
    private final SessionCalendarStorage storage;

    public SessionCalendarManager() {
        this.storage = new SessionCalendarStorage();
    }

    public SessionCalendarManager(SessionCalendarStorage storage) {
        this.storage = storage;
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

    // add a complete session that was already created
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

    // convenience method for calendar controller
    public void deleteSession(Session session) {
        storage.removeSession(session);
    }
}