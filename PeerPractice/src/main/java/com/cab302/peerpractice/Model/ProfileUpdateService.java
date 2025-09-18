package com.cab302.peerpractice.Model;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Service class responsible for handling profile update operations.
 * Follows Single Responsibility Principle by separating business logic from UI concerns.
 */
public class ProfileUpdateService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final UserManager userManager;

    public ProfileUpdateService(UserManager userManager) {
        this.userManager = userManager;
    }

    /**
     * Updates user profile with the provided information.
     * Returns true if any changes were made, false otherwise.
     */
    public boolean updateProfile(User user, ProfileUpdateRequest request) throws SQLException {
        boolean changed = false;

        changed |= updateIfChanged(user.getFirstName(), request.getFirstName(),
            () -> userManager.updateFirstName(user.getUsername(), request.getFirstName()),
            () -> user.setFirstName(request.getFirstName()));

        changed |= updateIfChanged(user.getLastName(), request.getLastName(),
            () -> userManager.updateLastName(user.getUsername(), request.getLastName()),
            () -> user.setLastName(request.getLastName()));

        changed |= updateIfChanged(user.getUsername(), request.getUsername(),
            () -> userManager.changeUsername(user.getUsername(), request.getUsername()),
            () -> user.setUsername(request.getUsername()));

        changed |= updateIfChanged(user.getInstitution(), request.getInstitution(),
            () -> userManager.updateInstitution(user.getUsername(), request.getInstitution()),
            () -> user.setInstitution(request.getInstitution()));

        changed |= updateIfChanged(user.getPhone(), request.getPhone(),
            () -> userManager.updatePhone(user.getUsername(), request.getPhone()),
            () -> user.setPhone(request.getPhone()));

        changed |= updateIfChanged(user.getAddress(), request.getAddress(),
            () -> userManager.updateAddress(user.getUsername(), request.getAddress()),
            () -> user.setAddress(request.getAddress()));

        String dobIso = request.getDateOfBirth() == null ? "" : ISO.format(request.getDateOfBirth());
        changed |= updateIfChanged(user.getDateOfBirth(), dobIso,
            () -> userManager.updateDateOfBirth(user.getUsername(), dobIso),
            () -> user.setDateOfBirth(dobIso));

        return changed;
    }

    private boolean updateIfChanged(String currentValue, String newValue,
                                   DatabaseUpdater dbUpdater, UserUpdater userUpdater) throws SQLException {
        if (!Objects.equals(currentValue, newValue)) {
            dbUpdater.update();
            userUpdater.update();
            return true;
        }
        return false;
    }

    @FunctionalInterface
    private interface DatabaseUpdater {
        void update() throws SQLException;
    }

    @FunctionalInterface
    private interface UserUpdater {
        void update();
    }

    /**
     * Data class containing profile update information.
     */
    public static class ProfileUpdateRequest {
        private final String firstName;
        private final String lastName;
        private final String username;
        private final String institution;
        private final String phone;
        private final String address;
        private final LocalDate dateOfBirth;

        public ProfileUpdateRequest(String firstName, String lastName, String username,
                                  String institution, String phone, String address,
                                  LocalDate dateOfBirth) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.username = username;
            this.institution = institution;
            this.phone = phone;
            this.address = address;
            this.dateOfBirth = dateOfBirth;
        }

        public String getFirstName() { return firstName; }
        public String getLastName() { return lastName; }
        public String getUsername() { return username; }
        public String getInstitution() { return institution; }
        public String getPhone() { return phone; }
        public String getAddress() { return address; }
        public LocalDate getDateOfBirth() { return dateOfBirth; }
    }
}