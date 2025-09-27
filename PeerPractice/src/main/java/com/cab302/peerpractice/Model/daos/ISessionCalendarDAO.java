package com.cab302.peerpractice.Model.daos;

import com.cab302.peerpractice.Model.entities.Group;
import com.cab302.peerpractice.Model.entities.Session;
import com.cab302.peerpractice.Model.entities.User;

import java.time.LocalDate;
import java.util.List;

public interface ISessionCalendarDAO {
    boolean addSession(Session session);
    boolean removeSession(Session session);
    List<Session> getAllSessions();
    List<Session> getSessionsForDate(LocalDate date);
    List<Session> getSessionsForUser(User user);
    List<Session> getSessionsForWeek(LocalDate startOfWeek);
    List<Session> getSessionsForDateRange(LocalDate startDate, LocalDate endDate);
    boolean updateSession(Session oldSession, Session newSession);
    void clearAllSessions();
    int getSessionCount();
    boolean hasSessionsOnDate(LocalDate date);

    // Extra filtering
    List<Session> getSessionsForGroup(Group group);
    List<Session> getSessionsForDateAndGroup(LocalDate date, Group group);
}
