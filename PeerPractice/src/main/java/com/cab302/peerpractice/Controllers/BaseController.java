package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.User;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

/**
 * Base controller providing common functionality for all controllers.
 * Follows DRY principle by centralizing shared behavior.
 */
public abstract class BaseController {
    protected final AppContext ctx;
    protected final Navigation nav;

    protected BaseController(AppContext ctx, Navigation nav) {
        this.ctx = ctx;
        this.nav = nav;
    }

    /**
     * Ensures there is a logged-in user and returns it, or null if not logged in.
     */
    protected User ensureLoggedIn() {
        if (ctx.getUserSession() == null || !ctx.getUserSession().isLoggedIn()) {
            return null;
        }
        return ctx.getUserSession().getCurrentUser();
    }

    /**
     * Safely sets text field value, handling null values.
     */
    protected static void safeSet(TextField field, String value) {
        if (field != null) {
            field.setText(value == null ? "" : value);
        }
    }

    /**
     * Returns trimmed text from field or empty string if null.
     */
    protected static String trimOrEmpty(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }

    /**
     * Shows an alert dialog with standard styling.
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
