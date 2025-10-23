package com.cab302.peerpractice.Model.ValueObjects;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Immutable value object representing a time range with validation.
 * Encapsulates start/end time logic and ensures validity.
 */
public final class TimeRange {
    private final LocalDateTime start;
    private final LocalDateTime end;

    /**
     * Creates a new TimeRange with validation.
     *
     * @param start the start time
     * @param end the end time
     * @throws IllegalArgumentException if start is after end or if either is null
     */
    public TimeRange(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "Start time cannot be null");
        Objects.requireNonNull(end, "End time cannot be null");

        if (end.isBefore(start)) {
            throw new IllegalArgumentException(
                "End time (" + end + ") cannot be before start time (" + start + ")"
            );
        }

        this.start = start;
        this.end = end;
    }

    /**
     * Gets the start time.
     *
     * @return the start time
     */
    public LocalDateTime getStart() {
        return start;
    }

    /**
     * Gets the end time.
     *
     * @return the end time
     */
    public LocalDateTime getEnd() {
        return end;
    }

    /**
     * Calculates the duration of this time range.
     *
     * @return the duration between start and end
     */
    public Duration getDuration() {
        return Duration.between(start, end);
    }

    /**
     * Checks if this time range overlaps with another.
     *
     * @param other the other time range
     * @return true if the ranges overlap
     */
    public boolean overlapsWith(TimeRange other) {
        Objects.requireNonNull(other, "Other time range cannot be null");
        return start.isBefore(other.end) && other.start.isBefore(end);
    }

    /**
     * Checks if a specific time falls within this range.
     *
     * @param time the time to check
     * @return true if the time is within this range (inclusive)
     */
    public boolean contains(LocalDateTime time) {
        Objects.requireNonNull(time, "Time cannot be null");
        return !time.isBefore(start) && !time.isAfter(end);
    }

    /**
     * Creates a new TimeRange by extending this one by a duration.
     *
     * @param duration the duration to add
     * @return a new TimeRange with extended end time
     */
    public TimeRange extend(Duration duration) {
        Objects.requireNonNull(duration, "Duration cannot be null");
        return new TimeRange(start, end.plus(duration));
    }

    /**
     * Creates a new TimeRange by shifting this one forward in time.
     *
     * @param duration the duration to shift
     * @return a new TimeRange shifted forward
     */
    public TimeRange shift(Duration duration) {
        Objects.requireNonNull(duration, "Duration cannot be null");
        return new TimeRange(start.plus(duration), end.plus(duration));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeRange timeRange = (TimeRange) o;
        return start.equals(timeRange.start) && end.equals(timeRange.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "TimeRange{" + start + " to " + end +
               " (duration: " + getDuration().toMinutes() + " minutes)}";
    }
}
