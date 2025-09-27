package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.PasswordHasher;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Model.UserDAO;
import com.cab302.peerpractice.Model.UserManager;
import com.cab302.peerpractice.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;

public class SettingController extends BaseController {

    @FXML private ComboBox<String> dateFormatBox;
    @FXML private ComboBox<String> timeFormatBox;
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
        // Populate format options
        dateFormatBox.getItems().addAll(
                "dd/MM/yyyy",
                "MM/dd/yyyy",
                "yyyy-MM-dd",
                "dd MMM yyyy",
                "MMM dd, yyyy"
        );

        timeFormatBox.getItems().addAll(
                "HH:mm",
                "hh:mm a",
                "HH:mm:ss",
                "hh:mm:ss a"
        );

        // Load current user's preferences
        loadCurrentUserPreferences();
    }

    private void loadCurrentUserPreferences() {
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser != null) {
            // Set current formats from user object
            dateFormatBox.setValue(currentUser.getDateFormat());
            timeFormatBox.setValue(currentUser.getTimeFormat());
        } else {
            // Set defaults if no user is logged in
            dateFormatBox.setValue("dd/MM/yyyy");
            timeFormatBox.setValue("HH:mm");
        }
    }

    @FXML
    private void onSave() {
        try {
            User currentUser = ctx.getUserSession().getCurrentUser();
            if (currentUser != null) {
                // Get the UserManager from AppContext
                UserManager userManager = ctx.getUserManager();

                // Update date/time formats using UserManager
                ctx.getUserDao().updateValue(currentUser.getUsername(),
                        "date_format", dateFormatBox.getValue());
                ctx.getUserDao().updateValue(currentUser.getUsername(),
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
