package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when attempting to register with an email that already exists.
 *
 * <p>This exception is thrown during user registration or email update operations
 * when the provided email address is already associated with an existing user account.
 *
 * <p> Typical scenarios include:
 * <ul>
 *   <li>User registration with duplicate email</li>
 *   <li>Email address change to an already registered email</li>
 *   <li>Bulk user import with conflicting email addresses</li>
 * </ul>
 *
 * @see RuntimeException
 * @see DuplicateUsernameException
 */
public class DuplicateEmailException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new DuplicateEmailException with the specified detail message.
     *
     * @param msg the detail message explaining the email conflict
     */
    public DuplicateEmailException(String msg) {
        super(msg);
    }
}