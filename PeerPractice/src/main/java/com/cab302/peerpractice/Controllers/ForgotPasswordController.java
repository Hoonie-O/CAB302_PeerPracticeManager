package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class ForgotPasswordController extends BaseController{
    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private Label messageLabel;

    public ForgotPasswordController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

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

