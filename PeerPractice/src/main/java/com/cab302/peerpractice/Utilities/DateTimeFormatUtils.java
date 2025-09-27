package com.cab302.peerpractice.Utilities;

import com.cab302.peerpractice.Model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class DateTimeFormatUtils {

    public static String formatDate(User user, LocalDateTime dateTime) {
        if (dateTime == null) return "";

        String pattern = (user != null && user.getDateFormat() != null)
                ? user.getDateFormat() : "MMMM d, yyyy"; // Updated default

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (IllegalArgumentException e) {
            // Fallback to default format if pattern is invalid
            return dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
        }
    }

    public static String formatTime(User user, LocalDateTime dateTime) {
        if (dateTime == null) return "";

        String pattern = (user != null && user.getTimeFormat() != null)
                ? user.getTimeFormat() : "h:mm a"; // Updated default

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (IllegalArgumentException e) {
            // Fallback to default format
            return dateTime.format(DateTimeFormatter.ofPattern("h:mm a"));
        }
    }

    public static String formatDateTime(User user, LocalDateTime dateTime) {
        return formatDate(user, dateTime) + " " + formatTime(user, dateTime);
    }

    // New method for previewing formats without a User object
    public static String formatWithPattern(String pattern, LocalDateTime dateTime, String defaultPattern) {
        if (dateTime == null) return "";
        if (pattern == null || pattern.trim().isEmpty()) {
            pattern = defaultPattern;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (IllegalArgumentException e) {
            // Fallback to default format
            return dateTime.format(DateTimeFormatter.ofPattern(defaultPattern));
        }
    }

    // Validation methods
    public static boolean isValidDateFormat(String pattern) {
        if (pattern == null || pattern.trim().isEmpty()) return false;

        try {
            DateTimeFormatter.ofPattern(pattern);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public static boolean isValidTimeFormat(String pattern) {
        return isValidDateFormat(pattern); // Same validation logic
    }
}