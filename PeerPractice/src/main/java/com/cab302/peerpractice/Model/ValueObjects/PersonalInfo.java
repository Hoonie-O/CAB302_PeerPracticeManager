package com.cab302.peerpractice.Model.ValueObjects;

import java.util.Objects;

/**
 * Immutable value object representing personal information.
 * Groups related personal data that always appears together.
 */
public final class PersonalInfo {
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;
    private final String institution;

    /**
     * Creates personal information with validation.
     *
     * @param firstName the first name (required)
     * @param lastName the last name (required)
     * @param dateOfBirth the date of birth (optional)
     * @param institution the institution (optional)
     */
    public PersonalInfo(String firstName, String lastName, String dateOfBirth, String institution) {
        this.firstName = Objects.requireNonNull(firstName, "First name cannot be null");
        this.lastName = Objects.requireNonNull(lastName, "Last name cannot be null");
        this.dateOfBirth = dateOfBirth;
        this.institution = institution;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public String getInstitution() {
        return institution;
    }

    /**
     * Gets the full name.
     *
     * @return first name + last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Checks if the profile has complete information.
     *
     * @return true if first name, last name, DOB, and institution are provided
     */
    public boolean isComplete() {
        return firstName != null && !firstName.isBlank() &&
               lastName != null && !lastName.isBlank() &&
               dateOfBirth != null && !dateOfBirth.isBlank() &&
               institution != null && !institution.isBlank();
    }

    /**
     * Creates a new PersonalInfo with updated first name.
     *
     * @param newFirstName the new first name
     * @return a new PersonalInfo instance
     */
    public PersonalInfo withFirstName(String newFirstName) {
        return new PersonalInfo(newFirstName, lastName, dateOfBirth, institution);
    }

    /**
     * Creates a new PersonalInfo with updated last name.
     *
     * @param newLastName the new last name
     * @return a new PersonalInfo instance
     */
    public PersonalInfo withLastName(String newLastName) {
        return new PersonalInfo(firstName, newLastName, dateOfBirth, institution);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalInfo that = (PersonalInfo) o;
        return firstName.equals(that.firstName) &&
               lastName.equals(that.lastName) &&
               Objects.equals(dateOfBirth, that.dateOfBirth) &&
               Objects.equals(institution, that.institution);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth, institution);
    }

    @Override
    public String toString() {
        return "PersonalInfo{" + getFullName() + ", institution='" + institution + "'}";
    }
}
