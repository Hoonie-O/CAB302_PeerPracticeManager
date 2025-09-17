package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Model.UserManager;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
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

    public EditProfileController(AppContext ctx, Navigation nav) { super(ctx, nav); }

    @FXML
    private void initialize() {
        // Ensure someone is logged in and close the dialog if not
        User u = ensureLoggedIn();
        if (u == null) return;

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
        if (u == null) return;

        // Read trimmed values from UI
        String newFirstname = trimOrEmpty(firstNameField);
        String newLastname = trimOrEmpty(lastNameField);
        String newUsername = trimOrEmpty(usernameField);
        String newInstitute = trimOrEmpty(instituteField);

        // New fields
        String newPhoneNumber = trimOrEmpty(phoneField);
        LocalDate newDateOfBirth = dateOfBirthField == null ? null : dateOfBirthField.getValue();
        String dobIso = newDateOfBirth == null ? "" : ISO.format(newDateOfBirth);
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
            UserManager um = ctx.getUserManager();
            boolean changed = false;

            if (!Objects.equals(newFirstname, u.getFirstName())) {
                um.updateFirstName(u.getUsername(), newFirstname);
                u.setFirstName(newFirstname);
                changed = true;
            }
            if (!Objects.equals(newLastname, u.getLastName())) {
                um.updateLastName(u.getUsername(), newLastname);
                u.setLastName(newLastname);
                changed = true;
            }
            if (!Objects.equals(newUsername, u.getUsername())) {
                um.changeUsername(u.getUsername(), newUsername);
                u.setUsername(newUsername);
                changed = true;
            }
            if (!Objects.equals(newInstitute, u.getInstitution())) {
                um.updateInstitution(u.getUsername(), newInstitute);
                u.setInstitution(newInstitute);
                changed = true;
            }

            // New columns
            if (!Objects.equals(newPhoneNumber, u.getPhone())) {
                um.updatePhone(u.getUsername(), newPhoneNumber);
                u.setPhone(newPhoneNumber);
                changed = true;
            }
            if (!Objects.equals(newAddress, u.getAddress())) {
                um.updateAddress(u.getUsername(), newAddress);
                u.setAddress(newAddress);
                changed = true;
            }
            if (!Objects.equals(dobIso, u.getDateOfBirth())) {
                um.updateDateOfBirth(u.getUsername(), dobIso);
                u.setDateOfBirth(dobIso);
                changed = true;
            }
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

    // Ensures there is a logged-in user
    private User ensureLoggedIn() {
        if (ctx.getUserSession() == null || !ctx.getUserSession().isLoggedIn()) {
            onClose();
            return null;
        }
        return ctx.getUserSession().getCurrentUser();
    }

    // Null sage setter for text fields
    private static void safeSet(TextField field, String value) {
        if (field != null) field.setText(value == null ? "" : value);
    }

    // Return trimmed empty text field text for nulls
    private static String trimOrEmpty(TextField tf) {
        return tf == null ? "" : (tf.getText() == null ? "" : tf.getText().trim());
    }

    // Alert creation
    private void showAlert(Alert.AlertType type, String header, String content) {
        Alert a = new Alert(type);
        a.setTitle("Edit Profile");
        a.setHeaderText(header);
        a.setContentText(content);
        if (root != null && root.getScene() != null) a.initOwner(root.getScene().getWindow());
        a.showAndWait();
    }
}
