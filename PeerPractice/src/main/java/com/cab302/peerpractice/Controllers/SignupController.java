package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignupController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Label messageLabel;

    private Navigation navigate() {
        return (Navigation) signupButton.getScene().getWindow().getUserData();
    }

    @FXML
    private void onSignupButton() {
        messageLabel.setText("Account for '" + usernameField.getText() + "' has been created");
    }

    @FXML
    private void onBackToLogin() {
        navigate().Display(View.Login);
    }
}
