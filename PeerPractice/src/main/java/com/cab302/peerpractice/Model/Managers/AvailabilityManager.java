package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.DAOs.IAvailabilityDAO;
import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AvailabilityManager {
    private final IAvailabilityDAO availabilityDAO;

    public AvailabilityManager(IAvailabilityDAO availabilityDAO) {
        this.availabilityDAO = availabilityDAO;
    }

    public boolean createAvailability(String title, User user, LocalDateTime startTime,
                                      LocalDateTime endTime, String colorLabel) {
        try {
            Availability availability = new Availability(title, user, startTime, endTime, colorLabel);
            return availabilityDAO.addAvailability(availability);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Availability> getAllAvailabilities() {
        return availabilityDAO.getAllAvailabilities();
    }

    public List<Availability> getAvailabilitiesForDate(LocalDate date) {
        return availabilityDAO.getAvailabilitiesForDate(date);
    }

    public List<Availability> getAvailabilitiesForUser(User user) {
        return availabilityDAO.getAvailabilitiesForUser(user);
    }

    public List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek) {
        return availabilityDAO.getAvailabilitiesForWeek(startOfWeek);
    }

    public List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek) {
        return availabilityDAO.getAvailabilitiesForUsers(users, startOfWeek);
    }

    public boolean removeAvailability(Availability availability) {
        return availabilityDAO.removeAvailability(availability);
    }

    public boolean updateAvailability(Availability oldAvailability, Availability newAvailability) {
        return availabilityDAO.updateAvailability(oldAvailability, newAvailability);
    }

    public void clearAllAvailabilities() {
        availabilityDAO.clearAllAvailabilities();
    }

    public int getAvailabilityCount() {
        return availabilityDAO.getAvailabilityCount();
    }

    public boolean hasAvailabilityOnDate(User user, LocalDate date) {
        return availabilityDAO.hasAvailabilityOnDate(user, date);
    }
}