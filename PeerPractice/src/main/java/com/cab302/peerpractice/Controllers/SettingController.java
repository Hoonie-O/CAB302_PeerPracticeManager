package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Utilities.DateTimeFormatUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class SettingController extends BaseController {

    @FXML private ComboBox<String> dateFormatBox;
    @FXML private ComboBox<String> timeFormatBox;
    @FXML private Label dateFormatPreview;
    @FXML private Label timeFormatPreview;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private Stage stage;
    private UserManager userManager;

    public SettingController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

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

    private void showAlert(String title, String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void onClose() {
        if (stage != null)
            stage.close();
    }
}
