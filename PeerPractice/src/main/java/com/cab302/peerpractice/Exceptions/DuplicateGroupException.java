package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when attempting to create a group with a name that already exists.
 *
 * <p>This exception is thrown during group creation operations when the provided
 * group name is already used by another group in the system.
 *
 * <p> Typical scenarios include:
 * <ul>
 *   <li>Group creation with duplicate name</li>
 *   <li>Group name change to an existing group name</li>
 *   <li>Bulk group import with conflicting names</li>
 * </ul>
 *
 * @see RuntimeException
 * @see DuplicateUsernameException
 * @see DuplicateEmailException
 */
public class DuplicateGroupException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new DuplicateGroupException with the specified detail message.
     *
     * @param message the detail message explaining the group name conflict
     */
    public DuplicateGroupException(String message) {
        super(message);
    }
}