package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.managers.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignupController extends BaseController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private TextField institutionField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Label messageLabel;

    private final UserManager userManager = ctx.getUserManager();

    public SignupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

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

    @FXML
    private void onBackToLogin() {
        nav.Display(View.Login);
    }
}
