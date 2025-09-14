package com.cab302.peerpractice.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AvailabilityStorage {
    private final List<Availability> availabilities = new ArrayList<>();

    public boolean addAvailability(Availability availability) {
        if (availability != null && !availabilities.contains(availability)) {
            availabilities.add(availability);
            return true;
        }
        return false;
    }

    public boolean removeAvailability(Availability availability) {
        return availabilities.remove(availability);
    }

    public List<Availability> getAllAvailabilities() {
        return new ArrayList<>(availabilities);
    }

    public List<Availability> getAvailabilitiesForDate(LocalDate date) {
        return availabilities.stream()
                .filter(availability -> availability.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Availability> getAvailabilitiesForUser(User user) {
        return availabilities.stream()
                .filter(availability -> availability.getUser().equals(user))
                .collect(Collectors.toList());
    }

    // get availabilities for a week starting from given date
    public List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return availabilities.stream()
                .filter(availability -> {
                    LocalDate availDate = availability.getStartTime().toLocalDate();
                    return !availDate.isBefore(startOfWeek) && !availDate.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    // for friend availability view - get availabilities for multiple users
    public List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return availabilities.stream()
                .filter(availability -> {
                    LocalDate availDate = availability.getStartTime().toLocalDate();
                    return users.contains(availability.getUser()) && 
                           !availDate.isBefore(startOfWeek) && 
                           !availDate.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    public boolean updateAvailability(Availability oldAvailability, Availability newAvailability) {
        int index = availabilities.indexOf(oldAvailability);
        if (index >= 0) {
            availabilities.set(index, newAvailability);
            return true;
        }
        return false;
    }

    public void clearAllAvailabilities() {
        availabilities.clear();
    }

    public int getAvailabilityCount() {
        return availabilities.size();
    }

    public boolean hasAvailabilityOnDate(User user, LocalDate date) {
        return availabilities.stream()
                .anyMatch(availability -> 
                    availability.getUser().equals(user) && 
                    availability.getStartTime().toLocalDate().equals(date));
    }
}