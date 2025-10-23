package com.cab302.peerpractice.Model.ValueObjects;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Immutable value object representing contact information with validation.
 */
public final class ContactInfo {
    private static final Pattern EMAIL_PATTERN =
        Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private static final Pattern PHONE_PATTERN =
        Pattern.compile("^[+]?[0-9]{10,15}$");

    private final String email;
    private final String phone;
    private final String address;

    /**
     * Creates contact information with validation.
     *
     * @param email the email address (required, must be valid)
     * @param phone the phone number (optional)
     * @param address the physical address (optional)
     * @throws IllegalArgumentException if email is invalid
     */
    public ContactInfo(String email, String phone, String address) {
        this.email = validateEmail(email);
        this.phone = phone;
        this.address = address;
    }

    private String validateEmail(String email) {
        Objects.requireNonNull(email, "Email cannot be null");

        if (email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be blank");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + email);
        }

        return email.toLowerCase(); // Normalize to lowercase
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    /**
     * Checks if phone number is valid (if provided).
     *
     * @return true if phone is null/empty or matches pattern
     */
    public boolean hasValidPhone() {
        return phone == null || phone.isBlank() || PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Checks if contact information is complete.
     *
     * @return true if email, phone, and address are all provided
     */
    public boolean isComplete() {
        return email != null && !email.isBlank() &&
               phone != null && !phone.isBlank() &&
               address != null && !address.isBlank();
    }

    /**
     * Creates a new ContactInfo with updated phone.
     *
     * @param newPhone the new phone number
     * @return a new ContactInfo instance
     */
    public ContactInfo withPhone(String newPhone) {
        return new ContactInfo(email, newPhone, address);
    }

    /**
     * Creates a new ContactInfo with updated address.
     *
     * @param newAddress the new address
     * @return a new ContactInfo instance
     */
    public ContactInfo withAddress(String newAddress) {
        return new ContactInfo(email, phone, newAddress);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ContactInfo that = (ContactInfo) o;
        return email.equals(that.email) &&
               Objects.equals(phone, that.phone) &&
               Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, phone, address);
    }

    @Override
    public String toString() {
        return "ContactInfo{email='" + email + "', phone='" + phone + "', address='" + address + "'}";
    }
}
