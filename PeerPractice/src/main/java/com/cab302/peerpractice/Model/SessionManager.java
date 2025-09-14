package com.cab302.peerpractice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages study sessions. Just did basic operations to get things going.
 * Will need expanding when we add groups and persistence.
 */
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
    
    /**
     * Creates a new study session and adds it to the list.
     */
    public Session createSession(String title, User organiser, LocalDateTime startTime, LocalDateTime endTime) {
        Session session = new Session(title, organiser, startTime, endTime);
        if (calendarManager != null) {
            calendarManager.addSession(session);
        } else {
            sessions.add(session);
        }
        return session;
    }
    
    /**
     * Gets all sessions, will probably want to filter this later.
     */
    public List<Session> getAllSessions() {
        if (calendarManager != null) return calendarManager.getAllSessions();
        return new ArrayList<>(sessions);
    }
    
    /**
     * Gets sessions for a specific user (as organiser or participant).
     */
    public List<Session> getSessionsForUser(User user) {
        if (calendarManager != null) return calendarManager.getSessionsForUser(user);
        List<Session> userSessions = new ArrayList<>();
        for (Session session : sessions) if (session.getParticipants().contains(user)) userSessions.add(session);
        return userSessions;
    }
    
    /**
     * Gets upcoming sessions (not completed or cancelled).
     */
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

    
    /**
     * Finds a session by its ID. Returns null if not found.
     */
    public Session findSessionById(String sessionId) {
        if (sessionId == null) return null;
        
        for (Session session : getAllSessions()) {
            if (sessionId.equals(session.getSessionId())) {
                return session;
            }
        }
        return null;
    }
}
