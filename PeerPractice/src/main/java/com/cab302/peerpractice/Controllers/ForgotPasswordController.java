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

    @FXML
    private void onSendResetLink() {
        IUserDAO userDAO = ctx.getUserDao();
        if(userDAO.existsByEmail(emailField.getText())){
            messageLabel.setText("Password reset link has been sent to your email. The link will expire in 10 minutes.");
        }
        else{
            messageLabel.setText("Email does not exist or is not valid.");
        }
    }

    @FXML
    private void onBackToLogin() {
        nav.Display(View.Login);
    }
}

