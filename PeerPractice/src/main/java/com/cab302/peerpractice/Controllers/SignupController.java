package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Managers.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;

/**
 * <hr>
 * Controller for handling user registration and account creation.
 *
 * <p>This controller manages the user signup process, validating input fields
 * and creating new user accounts in the system. It provides immediate feedback
 * on validation errors and success states.
 *
 * <p> Key features include:
 * <ul>
 *   <li>User registration with comprehensive field validation</li>
 *   <li>Password confirmation and strength checking</li>
 *   <li>Duplicate username and email detection</li>
 *   <li>Automatic navigation to login upon successful registration</li>
 * </ul>
 *
 * @see BaseController
 * @see UserManager
 */
public class SignupController extends BaseController {
    /** <hr> Field for entering user's first name. */
    @FXML private TextField firstNameField;
    /** <hr> Field for entering user's last name. */
    @FXML private TextField lastNameField;
    /** <hr> Field for entering desired username. */
    @FXML private TextField usernameField;
    /** <hr> Field for entering user's email address. */
    @FXML private TextField emailField;
    /** <hr> Field for entering user's institution. */
    @FXML private TextField institutionField;
    /** <hr> Field for entering password. */
    @FXML private PasswordField passwordField;
    /** <hr> Field for confirming password. */
    @FXML private PasswordField confirmPasswordField;
    /** <hr> Button to submit registration. */
    @FXML private Button signupButton;
    /** <hr> Label for displaying status messages. */
    @FXML private Label messageLabel;

    /** <hr> Manager for handling user data operations. */
    private final UserManager userManager = ctx.getUserManager();

    /**
     * <hr>
     * Constructs a new SignupController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public SignupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    public void initialize() {
        makeInstant(passwordField.getTooltip());
        makeInstant(confirmPasswordField.getTooltip());
    }

    /**
     * <hr>
     * Handles the user registration process.
     *
     * <p>Validates all input fields, checks password confirmation, and attempts
     * to create a new user account. Provides immediate feedback on validation
     * errors and navigates to login on success.
     */
    @FXML
    private void onSignupButton() {
        messageLabel.setText("");

        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            messageLabel.setText("Passwords do not match.");
            return;
        }

        try {
            boolean success = userManager.signUp(
                    firstNameField.getText(),
                    lastNameField.getText(),
                    usernameField.getText(),
                    emailField.getText(),
                    passwordField.getText(),
                    institutionField.getText()
            );

            if (success) {
                messageLabel.setText("Account for '" + usernameField.getText() + "' has been created.");
                nav.Display(View.Login); // return to log in screen
            }
        } catch (Exception e) {
            // Catch validation errors (duplicate email, invalid username, etc.)
            messageLabel.setText(e.getMessage());
        }
    }

    private void makeInstant(Tooltip t) {
        if (t == null) return;
        t.setShowDelay(Duration.ZERO);          // show immediately
        t.setHideDelay(Duration.millis(80));    // optional: quick hide
        t.setShowDuration(Duration.INDEFINITE); // optional: stay while hovered
    }

    /**
     * <hr>
     * Navigates back to the login screen.
     *
     * <p>Returns the user to the login interface without completing registration.
     */
    @FXML
    private void onBackToLogin() {
        nav.Display(View.Login);
    }
}