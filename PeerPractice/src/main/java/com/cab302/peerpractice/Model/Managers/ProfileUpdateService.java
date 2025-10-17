package com.cab302.peerpractice.Model.Managers;

import com.cab302.peerpractice.Model.Entities.User;

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

        changed |= updateIfChanged(user.getFirstName(), request.firstName(),
            () -> userManager.updateFirstName(user.getUsername(), request.firstName()),
            () -> user.setFirstName(request.firstName()));

        changed |= updateIfChanged(user.getLastName(), request.lastName(),
            () -> userManager.updateLastName(user.getUsername(), request.lastName()),
            () -> user.setLastName(request.lastName()));

        changed |= updateIfChanged(user.getUsername(), request.username(),
            () -> userManager.changeUsername(user.getUsername(), request.username()),
            () -> user.setUsername(request.username()));

        changed |= updateIfChanged(user.getInstitution(), request.institution(),
            () -> userManager.updateInstitution(user.getUsername(), request.institution()),
            () -> user.setInstitution(request.institution()));

        changed |= updateIfChanged(user.getPhone(), request.phone(),
            () -> userManager.updatePhone(user.getUsername(), request.phone()),
            () -> user.setPhone(request.phone()));

        changed |= updateIfChanged(user.getAddress(), request.address(),
            () -> userManager.updateAddress(user.getUsername(), request.address()),
            () -> user.setAddress(request.address()));

        String dobIso = request.dateOfBirth() == null ? "" : ISO.format(request.dateOfBirth());
        changed |= updateIfChanged(user.getDateOfBirth(), dobIso,
            () -> userManager.updateDateOfBirth(user.getUsername(), dobIso),
            () -> user.setDateOfBirth(dobIso));

        changed |= updateIfChanged(user.getBio(), request.bio(),
            () -> userManager.updateBio(user.getUsername(), request.bio()),
            () -> user.setBio(request.bio()));

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
        public record ProfileUpdateRequest(String firstName, String lastName, String username, String institution,
                                           String phone, String address, LocalDate dateOfBirth, String bio) {
    }
}