package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when attempting to add a friend relationship that already exists.
 *
 * <p>This exception is thrown when a user tries to establish a friendship
 * with another user when such relationship already exists in the system.
 *
 * <p> Common scenarios include:
 * <ul>
 *   <li>Adding an already existing friend</li>
 *   <li>Duplicate friend request submissions</li>
 *   <li>Reciprocal friend addition conflicts</li>
 * </ul>
 *
 * @see RuntimeException
 * @see DuplicateUsernameException
 * @see DuplicateEmailException
 */
public class DuplicateFriendException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new DuplicateFriendException with the specified detail message.
     *
     * @param message the detail message explaining the duplicate friend relationship
     */
    public DuplicateFriendException(String message) {
        super(message);
    }
}