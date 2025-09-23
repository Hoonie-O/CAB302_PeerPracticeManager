package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private List<Session> sessions;
    private final SessionCalendarManager calendarManager;

    public SessionManager() {
        this.sessions = new ArrayList<>();
        this.calendarManager = null;
    }

    public SessionManager(SessionCalendarManager calendarManager) {
        this.sessions = new ArrayList<>();
        this.calendarManager = calendarManager;
    }

    public Session createSession(String title, User organiser, LocalDateTime startTime, LocalDateTime endTime) {
        Session session = new Session(title, organiser, startTime, endTime);
        if (calendarManager != null) {
            calendarManager.addSession(session);
        } else {
            sessions.add(session);
        }
        return session;
    }

    public Session createSession(String title, User organiser, LocalDateTime startTime, LocalDateTime endTime, Group group) {
        Session session = new Session(title, organiser, startTime, endTime, group);
        if (calendarManager != null) {
            calendarManager.addSession(session, group);
        } else {
            sessions.add(session);
        }
        return session;
    }

    public List<Session> getAllSessions() {
        if (calendarManager != null) return calendarManager.getAllSessions();
        return new ArrayList<>(sessions);
    }

    public List<Session> getSessionsForUser(User user) {
        if (calendarManager != null) return calendarManager.getSessionsForUser(user);
        List<Session> userSessions = new ArrayList<>();
        for (Session session : sessions) if (session.getParticipants().contains(user)) userSessions.add(session);
        return userSessions;
    }

    public List<Session> getUpcomingSessions() {
        List<Session> upcoming = new ArrayList<>();
        for (Session session : getAllSessions()) {
            if (session.getStatus() == SessionStatus.PLANNED ||
                    session.getStatus() == SessionStatus.ACTIVE) {
                upcoming.add(session);
            }
        }
        return upcoming;
    }

    public Session findSessionById(String sessionId) {
        if (sessionId == null) return null;
        for (Session session : getAllSessions()) {
            if (sessionId.equals(session.getSessionId())) {
                return session;
            }
        }
        return null;
    }

    public List<Session> getSessionsForGroup(Group group) {
        if (calendarManager != null) return calendarManager.getSessionsForGroup(group);
        List<Session> groupSessions = new ArrayList<>();
        for (Session session : sessions) if (session.getGroup() != null && session.getGroup().equals(group)) groupSessions.add(session);
        return groupSessions;
    }
}