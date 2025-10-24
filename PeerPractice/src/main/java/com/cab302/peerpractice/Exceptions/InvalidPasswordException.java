package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when password validation fails during authentication.
 *
 * <p>This exception is thrown when a user provides an incorrect password
 * during login attempts or when a password fails to meet security requirements.
 *
 * <p> Common scenarios include:
 * <ul>
 *   <li>Incorrect password during user login</li>
 *   <li>Password change with invalid current password</li>
 *   <li>Password that doesn't meet complexity requirements</li>
 *   <li>Password reset with invalid or expired token</li>
 * </ul>
 *
 * @see RuntimeException
 * @see UserNotFoundException
 */
public class InvalidPasswordException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new InvalidPasswordException with the specified detail message.
     *
     * @param message the detail message explaining the password validation failure
     */
    public InvalidPasswordException(String message) {
        super(message);
    }
}