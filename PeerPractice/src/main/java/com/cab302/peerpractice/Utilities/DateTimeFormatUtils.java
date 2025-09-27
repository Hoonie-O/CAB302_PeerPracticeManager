package com.cab302.peerpractice.Utilities;

import com.cab302.peerpractice.Model.User;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateTimeFormatUtils {

    public static String formatDate(User user, LocalDateTime dateTime) {
        if (dateTime == null) return "";

        String pattern = (user != null && user.getDateFormat() != null)
                ? user.getDateFormat() : "dd/MM/yyyy";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (IllegalArgumentException e) {
            // Fallback to default format if pattern is invalid
            return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
        }
    }

    public static String formatTime(User user, LocalDateTime dateTime) {
        if (dateTime == null) return "";

        String pattern = (user != null && user.getTimeFormat() != null)
                ? user.getTimeFormat() : "HH:mm";

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return dateTime.format(formatter);
        } catch (IllegalArgumentException e) {
            // Fallback to default format
            return dateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        }
    }

    public static String formatDateTime(User user, LocalDateTime dateTime) {
        return formatDate(user, dateTime) + " " + formatTime(user, dateTime);
    }

    // Optional: Parse methods if you need to convert strings back to LocalDateTime
    public static LocalDateTime parseDateTime(User user, String dateTimeString) {
        if (dateTimeString == null || dateTimeString.trim().isEmpty()) return null;

        try {
            String datePattern = (user != null && user.getDateFormat() != null)
                    ? user.getDateFormat() : "dd/MM/yyyy";
            String timePattern = (user != null && user.getTimeFormat() != null)
                    ? user.getTimeFormat() : "HH:mm";

            String fullPattern = datePattern + " " + timePattern;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(fullPattern);
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
