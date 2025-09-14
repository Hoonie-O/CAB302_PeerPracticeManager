package com.cab302.peerpractice.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SessionCalendarStorage {
    private final List<Session> sessions = new ArrayList<>();

    public boolean addSession(Session session) {
        if (session != null && !sessions.contains(session)) {
            sessions.add(session);
            return true;
        }
        return false;
    }

    public boolean removeSession(Session session) {
        return sessions.remove(session);
    }

    public List<Session> getAllSessions() {
        return new ArrayList<>(sessions);
    }

    public List<Session> getSessionsForDate(LocalDate date) {
        return sessions.stream()
                .filter(session -> session.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Session> getSessionsForUser(User user) {
        return sessions.stream()
                .filter(session -> session.isParticipant(user))
                .collect(Collectors.toList());
    }

    public List<Session> getSessionsForWeek(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return sessions.stream()
                .filter(session -> {
                    LocalDate sessionDate = session.getStartTime().toLocalDate();
                    return !sessionDate.isBefore(startOfWeek) && !sessionDate.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    public List<Session> getSessionsForDateRange(LocalDate startDate, LocalDate endDate) {
        return sessions.stream()
                .filter(session -> {
                    LocalDate sessionDate = session.getStartTime().toLocalDate();
                    return !sessionDate.isBefore(startDate) && !sessionDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    public boolean updateSession(Session oldSession, Session newSession) {
        int index = sessions.indexOf(oldSession);
        if (index >= 0) {
            sessions.set(index, newSession);
            return true;
        }
        return false;
    }

    public void clearAllSessions() {
        sessions.clear();
    }

    public int getSessionCount() {
        return sessions.size();
    }

    public boolean hasSessionsOnDate(LocalDate date) {
        return sessions.stream()
                .anyMatch(session -> session.getStartTime().toLocalDate().equals(date));
    }
}