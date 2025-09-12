package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import com.cab302.peerpractice.Model.*;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {
    @FXML private TextField IDField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMe;
    @FXML private Hyperlink forgotpasswordlink;
    @FXML private Hyperlink signupLink;
    @FXML private Label messageLabel;

    private final UserManager userManager =
            new UserManager(new MockDAO(), new SimplePasswordHasher());

    private Navigation navigate() {
        return (Navigation) loginButton.getScene().getWindow().getUserData();
    }

    @FXML
    private void initialize() {
        // Disable login button until fields filled
        loginButton.disableProperty()
                .bind(IDField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty()));

        // Seed a test user
        try {
            userManager.signUp("John", "Doe", "username", "email@email.com", "password", "QUT");
        } catch (Exception e) {
            throw new IllegalStateException("Failed to seed test user", e);
        }

        // Event handlers
        loginButton.setOnAction(e -> login());
        forgotpasswordlink.setOnAction(e -> navigate().Display(View.ForgotPassword));
        signupLink.setOnAction(e -> navigate().Display(View.Signup));
    }

    private void login() {
        messageLabel.setText("");
        String ID = IDField.getText();
        String password = passwordField.getText();

        if (userManager.authenticate(ID, password)) {
            navigate().Display(View.MainMenu);
        } else {
            messageLabel.setText("Invalid email/username or password.");
        }
    }
}
