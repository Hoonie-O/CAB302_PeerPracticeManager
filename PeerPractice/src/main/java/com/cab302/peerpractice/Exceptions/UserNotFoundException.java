package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when a requested user cannot be found in the system.
 *
 * <p>This exception is thrown when operations attempt to access or manipulate
 * a user that does not exist in the database or user repository.
 *
 * <p> Common scenarios include:
 * <ul>
 *   <li>Login attempts with non-existent username/email</li>
 *   <li>User profile access for deleted or invalid users</li>
 *   <li>Friend requests to non-existent users</li>
 *   <li>Group operations referencing non-existent members</li>
 * </ul>
 *
 * @see RuntimeException
 * @see InvalidPasswordException
 * @see DuplicateUsernameException
 */
public class UserNotFoundException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new UserNotFoundException with the specified detail message.
     *
     * @param message the detail message explaining which user was not found
     */
    public UserNotFoundException(String message) {
        super(message);
    }
}