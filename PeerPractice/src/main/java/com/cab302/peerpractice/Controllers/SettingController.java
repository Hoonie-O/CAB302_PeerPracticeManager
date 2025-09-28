package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.Utils.DateTimeFormatUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;

/**
 * <hr>
 * Controller for managing user settings and preferences.
 *
 * <p>This controller handles user configuration options including date/time format preferences
 * and password changes. It provides live previews of format selections and validates
 * all changes before applying them.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Date and time format customization with live preview</li>
 *   <li>Secure password change with current password verification</li>
 *   <li>Input validation and error handling</li>
 *   <li>User preference persistence</li>
 * </ul>
 *
 * @see BaseController
 * @see UserManager
 * @see DateTimeFormatUtils
 */
public class SettingController extends BaseController {

    /** <hr> Combo box for selecting date format preference. */
    @FXML private ComboBox<String> dateFormatBox;
    /** <hr> Combo box for selecting time format preference. */
    @FXML private ComboBox<String> timeFormatBox;
    /** <hr> Label displaying date format preview. */
    @FXML private Label dateFormatPreview;
    /** <hr> Label displaying time format preview. */
    @FXML private Label timeFormatPreview;
    /** <hr> Field for entering current password. */
    @FXML private PasswordField oldPasswordField;
    /** <hr> Field for entering new password. */
    @FXML private PasswordField newPasswordField;
    /** <hr> Field for confirming new password. */
    @FXML private PasswordField confirmPasswordField;

    /** <hr> The stage containing this controller. */
    private Stage stage;
    /** <hr> Manager for handling user data operations. */
    private UserManager userManager;

    /**
     * <hr>
     * Constructs a new SettingController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public SettingController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Sets the stage for this controller.
     *
     * @param stage the stage containing this controller
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up format preview listeners and loads current user preferences
     * into the interface components.
     */
    @FXML
    private void initialize() {
        // Add listeners for live preview
        dateFormatBox.valueProperty().addListener((
                observable, oldValue, newValue) -> {
            updateDateFormatExample();
        });

        timeFormatBox.valueProperty().addListener((
                observable, oldValue, newValue) -> {
            updateTimeFormatExample();
        });

        // Load current user's preferences
        loadCurrentUserPreferences();
    }

    /**
     * <hr>
     * Updates the date format preview with the current selection.
     *
     * <p>Displays a live example of the selected date format using the current date
     * and validates the format pattern.
     */
    private void updateDateFormatExample() {
        String format = dateFormatBox.getValue();
        if (format == null) {
            dateFormatPreview.setText("Example: Please select a format");
            return;
        }

        String example = DateTimeFormatUtils.formatWithPattern(format, LocalDateTime.now(), "MMMM d, yyyy");
        if (DateTimeFormatUtils.isValidDateFormat(format)) {
            dateFormatPreview.setText("Preview: " + example);
        } else {
            dateFormatPreview.setText("Invalid format pattern");
        }
    }

    /**
     * <hr>
     * Updates the time format preview with the current selection.
     *
     * <p>Displays a live example of the selected time format using the current time
     * and validates the format pattern.
     */
    private void updateTimeFormatExample() {
        String format = timeFormatBox.getValue();
        if (format == null) {
            timeFormatPreview.setText("Preview: Please select a format");
            return;
        }

        String example = DateTimeFormatUtils.formatWithPattern(format, LocalDateTime.now(), "h:mm a");
        if (DateTimeFormatUtils.isValidTimeFormat(format)) {
            timeFormatPreview.setText("Current time: " + example);
        } else {
            timeFormatPreview.setText("Invalid format pattern");
        }
    }

    /**
     * <hr>
     * Loads current user preferences into the interface.
     *
     * <p>Retrieves the current user's date and time format preferences from the session
     * and populates the corresponding form fields with preview examples.
     */
    private void loadCurrentUserPreferences() {
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser != null) {
            // Set current formats from user object
            dateFormatBox.setValue(currentUser.getDateFormat());
            timeFormatBox.setValue(currentUser.getTimeFormat());
        } else {
            // Set defaults if no user is logged in
            dateFormatBox.setValue("MMMM d, yyyy");
            timeFormatBox.setValue("h:mm a");
        }

        // Update examples with initial values
        updateDateFormatExample();
        updateTimeFormatExample();
    }

    /**
     * <hr>
     * Saves user settings and preferences.
     *
     * <p>Validates all input fields, processes password changes if requested,
     * and persists all settings to the database. Displays appropriate success
     * or error messages to the user.
     */
    @FXML
    private void onSave() {
        try {
            User currentUser = ctx.getUserSession().getCurrentUser();
            if (currentUser != null) {
                // Validate formats before saving
                if (!DateTimeFormatUtils.isValidDateFormat(dateFormatBox.getValue())) {
                    showAlert("Error", "Invalid date format selected",
                            Alert.AlertType.ERROR);
                    return;
                }

                if (!DateTimeFormatUtils.isValidTimeFormat(timeFormatBox.getValue())) {
                    showAlert("Error", "Invalid time format selected",
                            Alert.AlertType.ERROR);
                    return;
                }

                // Handle password change if fields are filled
                boolean passwordChanged = handlePasswordChange(currentUser);

                // Use the generic updateValue method
                ctx.getUserDAO().updateValue(currentUser.getUsername(),
                        "date_format", dateFormatBox.getValue());
                ctx.getUserDAO().updateValue(currentUser.getUsername(),
                        "time_format", timeFormatBox.getValue());

                // Update local user object
                currentUser.setDateFormat(dateFormatBox.getValue());
                currentUser.setTimeFormat(timeFormatBox.getValue());

                if (passwordChanged) {
                    showAlert("Success", "Settings and password updated successfully!",
                            Alert.AlertType.INFORMATION);
                } else {
                    showAlert("Success", "Date and time settings saved successfully!",
                            Alert.AlertType.INFORMATION);
                }
            }
            else {
                showAlert("Error", "No user logged in",
                        Alert.AlertType.ERROR);
            }
        }
        catch (Exception e) {
            showAlert("Error", "Failed to save settings: " + e.getMessage(),
                    Alert.AlertType.ERROR);
            e.printStackTrace();
        }
        onClose();
    }

    /**
     * <hr>
     * Handles password change validation and execution.
     *
     * <p>Validates all password fields meet requirements, verifies the current password,
     * and updates the user's password if all validations pass.
     *
     * @param currentUser the current user changing their password
     * @return true if password was successfully changed, false if no change was requested
     * @throws Exception if password validation fails or change operation fails
     */
    private boolean handlePasswordChange(User currentUser) throws Exception {
        String oldPassword = oldPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // If no password fields are filled, skip password change
        if ((oldPassword == null || oldPassword.trim().isEmpty()) &&
                (newPassword == null || newPassword.trim().isEmpty()) &&
                (confirmPassword == null || confirmPassword.trim().isEmpty())) {
            return false;
        }

        // Validate all password fields are filled
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            showAlert("Error", "Please enter your current password", Alert.AlertType.ERROR);
            throw new Exception("Missing current password");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            showAlert("Error", "Please enter a new password", Alert.AlertType.ERROR);
            throw new Exception("Missing new password");
        }

        if (newPassword.length() < 6) {
            showAlert("Error", "Password must be at least 6 characters long", Alert.AlertType.ERROR);
            throw new Exception("Password too short");
        }

        if (!newPassword.equals(confirmPassword)) {
            showAlert("Error", "New password and confirmation password must match", Alert.AlertType.ERROR);
            throw new Exception("Passwords don't match");
        }

        // Verify current password
        if (!ctx.getUserManager().authenticate(currentUser.getUsername(), oldPassword)) {
            showAlert("Error", "Current password is incorrect", Alert.AlertType.ERROR);
            throw new Exception("Incorrect current password");
        }

        // Check if new password is same as current
        if (oldPassword.equals(newPassword)) {
            showAlert("Error", "New password must be different from current password", Alert.AlertType.ERROR);
            throw new Exception("Same password");
        }

        // Change password
        ctx.getUserManager().changePassword(currentUser.getUsername(), newPassword);

        // Clear password fields
        oldPasswordField.clear();
        newPasswordField.clear();
        confirmPasswordField.clear();

        return true;
    }

    /**
     * <hr>
     * Displays an alert dialog with the specified parameters.
     *
     * @param title the alert dialog title
     * @param message the alert dialog message content
     * @param type the type of alert to display
     */
    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * <hr>
     * Closes the settings dialog.
     *
     * <p>Closes the stage containing this controller, returning the user
     * to the previous interface.
     */
    @FXML
    private void onClose() {
        if (stage != null)
            stage.close();
    }
}