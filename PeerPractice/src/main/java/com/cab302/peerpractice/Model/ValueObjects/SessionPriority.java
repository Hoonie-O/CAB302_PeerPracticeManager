package com.cab302.peerpractice.Model.ValueObjects;

/**
 * Value object representing session priority levels.
 * Provides type safety for priority-based filtering and display.
 */
public enum SessionPriority {
    URGENT("urgent", 1),
    HIGH("high", 2),
    MEDIUM("medium", 3),
    LOW("low", 4),
    OPTIONAL("optional", 5);

    private final String value;
    private final int sortOrder;

    SessionPriority(String value, int sortOrder) {
        this.value = value;
        this.sortOrder = sortOrder;
    }

    /**
     * Gets the string representation for database storage.
     *
     * @return the priority value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the numeric sort order (lower = higher priority).
     *
     * @return the sort order
     */
    public int getSortOrder() {
        return sortOrder;
    }

    /**
     * Converts a string value to a SessionPriority enum.
     *
     * @param value the priority string
     * @return the corresponding SessionPriority
     */
    public static SessionPriority fromValue(String value) {
        if (value == null || value.isBlank()) {
            return OPTIONAL; // Default priority
        }

        for (SessionPriority priority : SessionPriority.values()) {
            if (priority.value.equalsIgnoreCase(value)) {
                return priority;
            }
        }

        return OPTIONAL; // Fallback to optional for unknown values
    }

    /**
     * Checks if this priority is urgent or high.
     *
     * @return true if priority is URGENT or HIGH
     */
    public boolean isHighPriority() {
        return this == URGENT || this == HIGH;
    }

    /**
     * Gets the display color for this priority level.
     *
     * @return hex color code
     */
    public String getDisplayColor() {
        return switch (this) {
            case URGENT -> "#f44336";  // Red
            case HIGH -> "#FF9800";    // Orange
            case MEDIUM -> "#2196F3";  // Blue
            case LOW -> "#4CAF50";     // Green
            case OPTIONAL -> "#757575"; // Gray
        };
    }

    @Override
    public String toString() {
        return value;
    }
}
