package com.cab302.peerpractice.Model.DAOs;

import com.cab302.peerpractice.Model.Entities.Availability;
import com.cab302.peerpractice.Model.Entities.User;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Mock (in-memory) implementation of IAvailabilityDAO for unit testing without a database.
 */
public class MockAvailabilityDAO implements IAvailabilityDAO {

    private final Map<String, Availability> availabilities = new ConcurrentHashMap<>();

    public MockAvailabilityDAO(IUserDAO userDao) {
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }

    // -------------------- CREATE --------------------
    @Override
    public boolean addAvailability(Availability availability) {
        if (availability == null || availability.getUser() == null) {
            return false;
        }

        String id = availability.getAvailabilityId();
        if (id == null || id.isBlank()) {
            // Require test code to set it in constructor
            id = UUID.randomUUID().toString();
            // Recreate availability with ID if needed
            availability = new Availability(
                    id,
                    availability.getTitle(),
                    availability.getUser(),
                    availability.getStartTime(),
                    availability.getEndTime(),
                    availability.getColorLabel()
            );
            availability.setDescription(availability.getDescription());
        }

        availabilities.put(id, availability);
        return true;
    }

    // -------------------- DELETE --------------------
    @Override
    public boolean removeAvailability(Availability availability) {
        if (availability == null) return false;
        return availabilities.remove(availability.getAvailabilityId()) != null;
    }

    @Override
    public void clearAllAvailabilities() {
        availabilities.clear();
    }

    // -------------------- READ --------------------
    @Override
    public List<Availability> getAllAvailabilities() {
        return new ArrayList<>(availabilities.values());
    }

    @Override
    public List<Availability> getAvailabilitiesForDate(LocalDate date) {
        return availabilities.values().stream()
                .filter(a -> a.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    @Override
    public List<Availability> getAvailabilitiesForUser(User user) {
        if (user == null) return Collections.emptyList();
        return availabilities.values().stream()
                .filter(a -> a.getUser().getUserId().equals(user.getUserId()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Availability> getAvailabilitiesForWeek(LocalDate startOfWeek) {
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return availabilities.values().stream()
                .filter(a -> {
                    LocalDate date = a.getStartTime().toLocalDate();
                    return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Availability> getAvailabilitiesForUsers(List<User> users, LocalDate startOfWeek) {
        if (users == null || users.isEmpty()) return Collections.emptyList();
        Set<String> userIds = users.stream().map(User::getUserId).collect(Collectors.toSet());
        LocalDate endOfWeek = startOfWeek.plusDays(6);
        return availabilities.values().stream()
                .filter(a -> userIds.contains(a.getUser().getUserId()))
                .filter(a -> {
                    LocalDate date = a.getStartTime().toLocalDate();
                    return !date.isBefore(startOfWeek) && !date.isAfter(endOfWeek);
                })
                .collect(Collectors.toList());
    }

    @Override
    public int getAvailabilityCount() {
        return availabilities.size();
    }

    @Override
    public boolean hasAvailabilityOnDate(User user, LocalDate date) {
        if (user == null || date == null) return false;
        return availabilities.values().stream()
                .anyMatch(a -> a.getUser().getUserId().equals(user.getUserId())
                        && a.getStartTime().toLocalDate().equals(date));
    }

    // -------------------- UPDATE --------------------
    @Override
    public boolean updateAvailability(Availability oldAvailability, Availability newAvailability) {
        if (oldAvailability == null || newAvailability == null) return false;
        if (!availabilities.containsKey(oldAvailability.getAvailabilityId())) return false;

        // Recreate with same ID
        Availability updated = new Availability(
                oldAvailability.getAvailabilityId(),
                newAvailability.getTitle(),
                newAvailability.getUser(),
                newAvailability.getStartTime(),
                newAvailability.getEndTime(),
                newAvailability.getColorLabel()
        );
        updated.setDescription(newAvailability.getDescription());

        availabilities.put(oldAvailability.getAvailabilityId(), updated);
        return true;
    }
}
