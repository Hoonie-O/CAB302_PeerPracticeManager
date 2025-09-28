package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.ProfileUpdateService;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.UnaryOperator;

/**
 * <hr>
 * Controller for managing user profile editing functionality.
 *
 * <p>This controller handles the display and modification of user profile information
 * including personal details, contact information, and biographical data. It provides
 * validation and data persistence for profile updates.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Comprehensive profile form with validation</li>
 *   <li>Date of birth restrictions to prevent future dates</li>
 *   <li>Phone number input filtering for digits only</li>
 *   <li>Integration with ProfileUpdateService for data persistence</li>
 * </ul>
 *
 * @see User
 * @see ProfileUpdateService
 * @see BaseController
 */
public class EditProfileController extends BaseController {
    /** <hr> The dialog stage for this controller. */
    private Stage stage;
    /** <hr> Root stack pane for the edit profile view. */
    @FXML private StackPane root;
    /** <hr> Text field for entering first name. */
    @FXML private TextField firstNameField;
    /** <hr> Text field for entering last name. */
    @FXML private TextField lastNameField;
    /** <hr> Text field for entering username. */
    @FXML private TextField usernameField;
    /** <hr> Text field for entering email address. */
    @FXML private TextField emailField;
    /** <hr> Text field for entering educational institution. */
    @FXML private TextField instituteField;
    /** <hr> Date picker for selecting date of birth. */
    @FXML private DatePicker dateOfBirthField;
    /** <hr> Text field for entering phone number. */
    @FXML private TextField phoneField;
    /** <hr> Text field for entering physical address. */
    @FXML private TextField addressField;
    /** <hr> Text area for entering biographical information. */
    @FXML private TextArea bioField;

    /**
     * <hr>
     * Formatter used to parse the DatePicker value consistently.
     */
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    /**
     * <hr>
     * Constructs a new EditProfileController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public EditProfileController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up the profile form by prefilling existing user data and configuring
     * input validation. Ensures user authentication and closes dialog if no user
     * is logged in.
     *
     * <p>Configures phone field for digits-only input and date picker to disable
     * future dates for date of birth selection.
     */
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

        // Biography information prefill
        if (bioField != null) {
            bioField.setText(u.getBio() != null ? u.getBio() : "");
        }
    }

    /**
     * <hr>
     * Sets the stage for this controller's dialog.
     *
     * @param stage the dialog stage to set
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * <hr>
     * Handles the close action for the edit profile dialog.
     *
     * <p>Closes the dialog stage when the user cancels or completes editing
     * without saving changes.
     */
    @FXML
    private void onClose() {
        if (stage != null) stage.close();
    }

    /**
     * <hr>
     * Handles the save action for profile updates.
     *
     * <p>Validates user input, processes profile changes through the update service,
     * and displays appropriate feedback messages. Closes the dialog on successful
     * save or shows error messages for validation failures.
     *
     * <p>Performs validation for required fields and date of birth constraints
     * before attempting to persist changes to the database.
     */
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
        String newBio = bioField != null ? bioField.getText().trim() : "";

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
                    newFirstname, newLastname, newUsername, newInstitute, newPhoneNumber, newAddress, newDateOfBirth, newBio
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

    /**
     * <hr>
     * Shows an alert dialog with default title for this controller.
     *
     * <p>Overloaded convenience method that provides a default title
     * specific to the edit profile context.
     *
     * @param type the type of alert to display
     * @param header the header text for the alert
     * @param content the content text for the alert
     */
    private void showAlert(Alert.AlertType type, String header, String content) {
        showAlert(type, header, content, "Edit Profile", root);
    }
}