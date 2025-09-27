package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.entities.User;
import com.cab302.peerpractice.Model.managers.ProfileUpdateService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

public class EditProfileController extends BaseController {

    private Stage stage;
    @FXML private StackPane root;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField instituteField;
    @FXML private DatePicker dateOfBirthField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;

    // Formatter used to parse the DatePicker value consistently.
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    public EditProfileController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    private void initialize() {
        // Ensure someone is logged in and close the dialog if not
        User u = ensureLoggedIn();
        if (u == null) {
            onClose();
            return;
        }

        // Prefill existing fields
        // safeSet is used to handle nulls
        safeSet(firstNameField, u.getFirstName());
        safeSet(lastNameField, u.getLastName());
        safeSet(usernameField, u.getUsername());
        safeSet(emailField, u.getEmail());
        safeSet(instituteField, u.getInstitution());

        // Blocks invalid character in phone field
        if (phoneField != null) {
            UnaryOperator<TextFormatter.Change> digitsOnly = change -> {
                String next = change.getControlNewText();
                return next.matches("\\d*") ? change : null; // allow only 0-9
            };
            phoneField.setTextFormatter(new TextFormatter<>(digitsOnly));
            // Prefill value (can be empty if not set yet)
            phoneField.setText(u.getPhone());
        }

        // Disable future dates and date of birth information prefill
        if (dateOfBirthField != null) {
            // Disable choosing dates after "today".
            dateOfBirthField.setDayCellFactory(picker -> new DateCell() {
                @Override
                public void updateItem(LocalDate date, boolean empty) {
                    super.updateItem(date, empty);
                    if (!empty && date != null && date.isAfter(LocalDate.now())) {
                        setDisable(true);
                        setStyle("-fx-opacity: 0.5;");
                    }
                }
            });

            // Prefill from stored ISO string
            try {
                String dob = u.getDateOfBirth();
                if (dob != null && !dob.isBlank()) {
                    dateOfBirthField.setValue(LocalDate.parse(dob, ISO));
                }
            } catch (Exception ignored) {}
        }

        //  Address information prefill
        if (addressField != null) {
            addressField.setText(u.getAddress());
        }
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML private void onClose() {
        if (stage != null) stage.close();
    }

    @FXML
    private void onSave() {
        User u = ensureLoggedIn();
        if (u == null) {
            onClose();
            return;
        }

        // Read trimmed values from UI
        String newFirstname = trimOrEmpty(firstNameField);
        String newLastname = trimOrEmpty(lastNameField);
        String newUsername = trimOrEmpty(usernameField);
        String newInstitute = trimOrEmpty(instituteField);
        String newPhoneNumber = trimOrEmpty(phoneField);
        LocalDate newDateOfBirth = dateOfBirthField == null ? null : dateOfBirthField.getValue();
        String newAddress = trimOrEmpty(addressField);

        // Validate required fields
        if (newFirstname.isBlank() || newLastname.isBlank() || newUsername.isBlank()) {
            showAlert(Alert.AlertType.ERROR, "Missing required fields",
                    "First name, last name, and username cannot be empty.");
            return;
        }

        // Validate DateOfBirth not in future
        if (newDateOfBirth != null && newDateOfBirth.isAfter(LocalDate.now())) {
            showAlert(Alert.AlertType.ERROR, "Invalid date of birth",
                    "Date of birth cannot be in the future.");
            return;
        }

        try {
            ProfileUpdateService updateService = new ProfileUpdateService(ctx.getUserManager());
            ProfileUpdateService.ProfileUpdateRequest request = new ProfileUpdateService.ProfileUpdateRequest(
                newFirstname, newLastname, newUsername, newInstitute, newPhoneNumber, newAddress, newDateOfBirth
            );

            boolean changed = updateService.updateProfile(u, request);

            if (changed) {
                showAlert(Alert.AlertType.INFORMATION,
                        "Profile updated",
                        "Your profile was saved successfully.");
                onClose();
            } else {
                showAlert(Alert.AlertType.NONE,
                        "No changes",
                        "Nothing to save.");
            }
        } catch (SQLException ex) {
            showAlert(Alert.AlertType.ERROR,
                    "Database error", ex.getMessage());
        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR,
                    "Error", ex.getMessage());
        }
    }

    // Override showAlert to provide default title for this controller
    private void showAlert(Alert.AlertType type, String header, String content) {
        showAlert(type, header, content, "Edit Profile", root);
    }
}
