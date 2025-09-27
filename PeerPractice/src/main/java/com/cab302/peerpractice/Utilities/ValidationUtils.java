package com.cab302.peerpractice.Utilities;

import com.cab302.peerpractice.Exceptions.InvalidPasswordException;
import java.util.regex.Pattern;

/**
 * Utility class for centralizing validation logic to follow DRY principle.
 * Contains static methods for common validation operations across the application.
 */
public final class ValidationUtils {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^(?!.*\\.\\.)"+ "[A-Za-z0-9._%+-]+"+ "@"+ "(?![.-])[A-Za-z0-9.-]+"+ "(?<![.-])"+ "\\.[A-Za-z]{2,}$");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^(?!\\d+$)[A-Za-z0-9._]{6,}$");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[\\p{L}\\s'-]+$");
    private static final Pattern SPECIAL_CHARS = Pattern.compile("[!@#$%^&*()\\-_=+\\[\\]{};:'\",.<>/?\\\\|`~]");
    private static final Pattern GROUP_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9 '_.-]+$");

    private ValidationUtils() {
        // Prevent instantiation of utility class
    }

    /**
     * Validates and cleans a name field (first name, last name).
     */
    public static String validateAndCleanName(String name, String fieldName) {
        if (name == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }

        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }

        if (!NAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException(fieldName + " can only contain letters and spaces");
        }

        return trimmed;
    }

    /**
     * Validates and cleans group name
     */
    public static String validateAndCleanOthersName(String name){
        if (name == null) throw new IllegalArgumentException("Group name can't be null");
        name = name.trim();
        if (name.isEmpty()) throw new IllegalArgumentException("Group name can't be blank");
        if (name.length() > 20) throw new IllegalArgumentException("Group name can't be longer than 20 characters");
        if (!GROUP_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Group name can only contain letters, numbers, spaces, dots, hyphens,underscores, or apostrophes");
        }
        return name;
    }

    /**
     * Validates and cleans group description
     */
    public static String validateAndCleanGroupDescription(String description){
        if (description == null) throw new IllegalArgumentException("Description can't be null");
        description = description.trim();
        if (description.isEmpty()) throw new IllegalArgumentException("Description can't be blank");
        if (description.length() > 200) throw new IllegalArgumentException("Description can't be longer than 200 characters");
        return description;
    }


    /**
     * Validates and cleans a username.
     */
    public static String validateAndCleanUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        String trimmed = username.trim();
        if (trimmed.length() < 6) {
            throw new IllegalArgumentException("Username must be at least 6 characters long");
        }

        if (!USERNAME_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Username must be at least 6 characters long, and only contain letters, numbers, . or _");
        }

        return trimmed;
    }

    /**
     * Validates and cleans an email address.
     */
    public static String validateAndCleanEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }

        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        return trimmed;
    }

    /**
     * Validates and cleans a bio field.
     */
    public static String validateAndCleanBio(String bio) {
        if (bio == null) {
            throw new IllegalArgumentException("Bio cannot be null");
        }

        if (bio.length() > 200) {
            throw new IllegalArgumentException("Bio cannot exceed 200 characters");
        }

        return bio.trim();
    }

    /**
     * Validates password strength requirements.
     */
    public static void validatePassword(String password) throws InvalidPasswordException {
        final int MIN_LENGTH = 8;
        final int MAX_LENGTH = 72;

        if (password == null || password.isEmpty()) {
            throw new InvalidPasswordException("Password can't be null or empty.");
        }

        if (password.length() < MIN_LENGTH) {
            throw new InvalidPasswordException("Password should be at least 8 characters long.");
        }

        if (password.length() > MAX_LENGTH) {
            throw new InvalidPasswordException("Password can't be longer than 72 characters long.");
        }

        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new InvalidPasswordException("Password must contain at least one upper case letter.");
        }

        if (!password.chars().anyMatch(Character::isLowerCase)) {
            throw new InvalidPasswordException("Password must contain at least one lower case letter.");
        }

        if (!password.chars().anyMatch(Character::isDigit)) {
            throw new InvalidPasswordException("Password must contain at least one numerical digit.");
        }

        if (!SPECIAL_CHARS.matcher(password).find()) {
            throw new InvalidPasswordException("Password must contain at least one special character");
        }
    }

    /**
     * Validates that a string is not null or blank after trimming.
     */
    public static void requireNotBlank(String value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null.");
        }
        boolean allBlank = value.codePoints()
                .allMatch(cp -> Character.isWhitespace(cp)
                        || Character.getType(cp) == Character.SPACE_SEPARATOR); // catches NBSP, thin space, etc.

        if (allBlank) {
            throw new IllegalArgumentException(fieldName + " cannot be empty.");
        }
    }
}