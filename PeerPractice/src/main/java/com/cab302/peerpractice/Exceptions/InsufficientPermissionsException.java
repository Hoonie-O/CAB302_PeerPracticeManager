package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when a user lacks required permissions for an operation.
 *
 * <p>This exception is thrown when a user attempts to perform an action
 * that requires specific permissions or privileges that they do not possess.
 *
 * <p> Common scenarios include:
 * <ul>
 *   <li>Non-admin users attempting administrative actions</li>
 *   <li>Group members trying to modify group settings without proper role</li>
 *   <li>Users accessing restricted resources or features</li>
 *   <li>Session or availability management without proper authorization</li>
 * </ul>
 *
 * @see RuntimeException
 * @see UserNotFoundException
 */
public class InsufficientPermissionsException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new InsufficientPermissionsException with the specified detail message.
     *
     * @param message the detail message explaining the permission deficiency
     */
    public InsufficientPermissionsException(String message) {
        super(message);
    }
}