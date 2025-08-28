package com.cab302.peerpractice.Model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EventStorage {
    private final List<Event> events;

    public EventStorage() {
        this.events = new ArrayList<>();
    }

    public void addEvent(Event event) {
        if (event != null) {
            events.add(event);
        }
    }

    public List<Event> getAllEvents() {
        return new ArrayList<>(events);
    }

    public List<Event> getEventsForDate(LocalDate date) {
        return events.stream()
                .filter(event -> event.getStartTime().toLocalDate().equals(date))
                .collect(Collectors.toList());
    }

    public List<Event> getEventsForDateRange(LocalDate startDate, LocalDate endDate) {
        return events.stream()
                .filter(event -> {
                    LocalDate eventDate = event.getStartTime().toLocalDate();
                    return !eventDate.isBefore(startDate) && !eventDate.isAfter(endDate);
                })
                .collect(Collectors.toList());
    }

    public void removeEvent(Event event) {
        events.remove(event);
    }

    public void clearAllEvents() {
        events.clear();
    }

    public int getEventCount() {
        return events.size();
    }
}