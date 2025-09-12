package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class SignupController extends BaseController{
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button signupButton;
    @FXML private Label messageLabel;

    public SignupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    private void onSignupButton() {
        messageLabel.setText("Account for '" + usernameField.getText() + "' has been created");
    }

    @FXML
    private void onBackToLogin() {
        nav.Display(View.Login);
    }
}
