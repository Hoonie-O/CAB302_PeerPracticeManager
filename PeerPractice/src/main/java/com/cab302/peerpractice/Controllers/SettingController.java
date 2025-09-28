package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Utilities.DateTimeFormatUtils;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

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

                // Use the generic updateValue method
                ctx.getUserDAO().updateValue(currentUser.getUsername(),
                        "date_format", dateFormatBox.getValue());
                ctx.getUserDAO().updateValue(currentUser.getUsername(),
                        "time_format", timeFormatBox.getValue());

                // Update local user object
                currentUser.setDateFormat(dateFormatBox.getValue());
                currentUser.setTimeFormat(timeFormatBox.getValue());

                showAlert("Success", "Date and time settings saved successfully!",
                        Alert.AlertType.INFORMATION);
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
