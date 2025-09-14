package com.cab302.peerpractice.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AvailabilityManager {
    private final AvailabilityStorage storage;

    public AvailabilityManager() {
        this.storage = new AvailabilityStorage();
    }

    public AvailabilityManager(AvailabilityStorage storage) {
        this.storage = storage;
    }

    public boolean createAvailability(String title, User user, LocalDateTime startTime, 
                                    LocalDateTime endTime, String colorLabel) {
        try {
            Availability availability = new Availability(title, user, startTime, endTime, colorLabel);
            return storage.addAvailability(availability);
        } catch (Exception e) {
            return false;
        }
    }

    public List<Availability> getAllAvailabilities() {
        return storage.getAllAvailabilities();
    }

    public List<Availability> getAvailabilitiesForDate(LocalDate date) {
        return storage.getAvailabilitiesForDate(date);
    }

    public List<Availability> getAvailabilitiesForUser(User user) {
        return storage.getAvailabilitiesForUser(user);
    }

    public List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek) {
        return storage.getAvailabilitiesForWeek(startOfWeek);
    }

    // get availability for multiple users for friend availability view
    public List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek) {
        return storage.getAvailabilitiesForUsers(users, startOfWeek);
    }

    public boolean removeAvailability(Availability availability) {
        return storage.removeAvailability(availability);
    }

    public boolean updateAvailability(Availability oldAvailability, Availability newAvailability) {
        return storage.updateAvailability(oldAvailability, newAvailability);
    }

    public void clearAllAvailabilities() {
        storage.clearAllAvailabilities();
    }

    public int getAvailabilityCount() {
        return storage.getAvailabilityCount();
    }

    public boolean hasAvailabilityOnDate(User user, LocalDate date) {
        return storage.hasAvailabilityOnDate(user, date);
    }
}