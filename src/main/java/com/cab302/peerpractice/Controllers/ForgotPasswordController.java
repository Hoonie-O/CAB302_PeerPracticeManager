package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ForgotPasswordController {
    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private Label messageLabel;

    private Navigation navigate() {
        return (Navigation) sendButton.getScene().getWindow().getUserData();
    }

    @FXML
    private void onSendResetLink() {
        messageLabel.setText("Reset link sent to: " + emailField.getText());
    }

    @FXML
    private void onBackToLogin() {
        navigate().Display(View.Login);
    }
}

