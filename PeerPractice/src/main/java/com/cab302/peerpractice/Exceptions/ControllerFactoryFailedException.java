package com.cab302.peerpractice.Exceptions;

/**
 * <hr>
 * Exception thrown when controller factory fails to create a controller instance.
 *
 * <p>This exception indicates a failure in the controller creation process,
 * typically due to configuration issues, missing dependencies, or instantiation problems.
 *
 * <p> Common causes include:
 * <ul>
 *   <li>Missing FXML files or incorrect paths</li>
 *   <li>Dependency injection failures</li>
 *   <li>Invalid controller configuration</li>
 *   <li>Class loading or instantiation errors</li>
 * </ul>
 *
 * @see RuntimeException
 */
public class ControllerFactoryFailedException extends RuntimeException {
    /**
     * <hr>
     * Constructs a new ControllerFactoryFailedException with the specified detail message.
     *
     * @param message the detail message explaining the factory failure
     */
    public ControllerFactoryFailedException(String message) {
        super(message);
    }
}