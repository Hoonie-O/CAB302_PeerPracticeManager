package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.Entities.User;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * <hr>
 * Base controller providing common functionality for all controllers.
 *
 * <p>This abstract controller follows the DRY (Don't Repeat Yourself) principle
 * by centralizing shared behavior and utility methods used across multiple
 * controller classes in the application.
 *
 * <p>Key features include:
 * <ul>
 *   <li>User session management and validation</li>
 *   <li>Safe UI component value handling</li>
 *   <li>Standardized alert dialog presentation</li>
 *   <li>Common navigation and context access</li>
 * </ul>
 *
 * @see AppContext
 * @see Navigation
 * @see SidebarController
 */
public abstract class BaseController {
    /** <hr> The application context providing access to user session and managers. */
    protected final AppContext ctx;
    /** <hr> The navigation controller for screen transitions and routing. */
    protected final Navigation nav;

    /**
     * <hr>
     * Constructs a new BaseController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    protected BaseController(AppContext ctx, Navigation nav) {
        this.ctx = ctx;
        this.nav = nav;
    }

    /**
     * <hr>
     * Ensures there is a logged-in user and returns it, or null if not logged in.
     *
     * <p>This method checks the current user session and returns the authenticated
     * user if available. It provides a centralized way to validate user authentication
     * across all controllers.
     *
     * @return the currently logged-in User object, or null if no user is authenticated
     */
    protected User ensureLoggedIn() {
        if (ctx.getUserSession() == null || !ctx.getUserSession().isLoggedIn()) {
            return null;
        }
        return ctx.getUserSession().getCurrentUser();
    }

    /**
     * <hr>
     * Safely sets text field value, handling null values.
     *
     * <p>This utility method prevents NullPointerExceptions by checking for null
     * field references and converting null values to empty strings before setting
     * text field content.
     *
     * @param field the TextField to set value for
     * @param value the string value to set (null values are converted to empty string)
     */
    protected static void safeSet(TextField field, String value) {
        if (field != null) {
            field.setText(value == null ? "" : value);
        }
    }

    /**
     * <hr>
     * Returns trimmed text from field or empty string if null.
     *
     * <p>This utility method safely extracts and trims text from a TextField,
     * providing a fallback empty string if the field or its text is null.
     *
     * @param tf the TextField to extract text from
     * @return the trimmed text content, or empty string if field or text is null
     */
    protected static String trimOrEmpty(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }

    /**
     * <hr>
     * Shows an alert dialog with standard styling.
     *
     * <p>This method provides a consistent way to display alert dialogs throughout
     * the application, with proper window ownership and standardized formatting.
     *
     * @param type the type of alert (e.g., INFORMATION, WARNING, ERROR)
     * @param header the header text to display in the alert
     * @param content the main content text of the alert
     * @param title the window title for the alert dialog
     * @param rootPane the parent pane for window ownership (can be null)
     */
    protected void showAlert(Alert.AlertType type, String header, String content, String title, Pane rootPane) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (rootPane != null && rootPane.getScene() != null) {
            alert.initOwner(rootPane.getScene().getWindow());
        }
        alert.showAndWait();
    }
}
