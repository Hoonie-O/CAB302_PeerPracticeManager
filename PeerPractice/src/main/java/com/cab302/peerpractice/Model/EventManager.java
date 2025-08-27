package com.cab302.peerpractice.Model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class EventManager {
    private final EventStorage storage;

    public EventManager() {
        this.storage = new EventStorage();
    }

    public EventManager(EventStorage storage) {
        this.storage = Objects.requireNonNull(storage, "storage");
    }

    public boolean createEvent(String title, String description, LocalDateTime startTime, LocalDateTime endTime, String colorLabel) {
        try {
            Event event = new Event(title, description, startTime, endTime, colorLabel);
            storage.addEvent(event);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public List<Event> getAllEvents() {
        return storage.getAllEvents();
    }

    public List<Event> getEventsForDate(LocalDate date) {
        return storage.getEventsForDate(date);
    }

    public List<Event> getEventsForMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        return storage.getEventsForDateRange(startOfMonth, endOfMonth);
    }

    public List<Event> getEventsForWeek(LocalDate weekStartDate) {
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        return storage.getEventsForDateRange(weekStartDate, weekEndDate);
    }

    public boolean removeEvent(Event event) {
        if (event == null) {
            return false;
        }
        storage.removeEvent(event);
        return true;
    }

    // TODO: Add method to edit existing events
    // implement updateEvent(Event oldEvent, Event newEvent) method
    // allows users to modify event details after creation
    
    // TODO: Add method to find event by ID or unique identifier
    // we could add a UUID field to Event class to make it easier to find and edit events
    // helps with edit/delete operations when multiple events have similar names

    public void clearAllEvents() {
        storage.clearAllEvents();
    }

    public int getEventCount() {
        return storage.getEventCount();
    }

    public boolean hasEventsOnDate(LocalDate date) {
        return !getEventsForDate(date).isEmpty();
    }
}