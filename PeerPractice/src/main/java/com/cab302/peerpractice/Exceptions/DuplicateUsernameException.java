package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when attempting to register with a username that already exists.
 *
 * <p>This exception is thrown during user registration or username update operations
 * when the provided username is already taken by another user in the system.
 *
 * <p> Common scenarios include:
 * <ul>
 *   <li>User registration with duplicate username</li>
 *   <li>Username change to an already taken username</li>
 *   <li>Bulk user import with conflicting usernames</li>
 * </ul>
 *
 * @see RuntimeException
 * @see DuplicateEmailException
 */
public class DuplicateUsernameException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new DuplicateUsernameException with the specified detail message.
     *
     * @param message the detail message explaining the username conflict
     */
    public DuplicateUsernameException(String message) {
        super(message);
    }
}