package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.util.List;

public interface IAvailabilityDAO {
    boolean addAvailability(Availability availability);
    boolean removeAvailability(Availability availability);
    List<Availability> getAllAvailabilities();
    List<Availability> getAvailabilitiesForDate(LocalDate date);
    List<Availability> getAvailabilitiesForUser(User user);
    List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek);
    List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek);
    boolean updateAvailability(Availability oldAvailability, Availability newAvailability);
    void clearAllAvailabilities();
    int getAvailabilityCount();
    boolean hasAvailabilityOnDate(User user, LocalDate date);
}
