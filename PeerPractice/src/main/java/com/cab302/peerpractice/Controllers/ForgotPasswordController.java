package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Model.MailService;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Model.UserSession;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

public class ForgotPasswordController extends BaseController{
    @FXML private TextField emailField;
    @FXML private Button sendButton;
    @FXML private Button backButton;
    @FXML private Label messageLabel;

    public ForgotPasswordController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    private void onSendResetLink() throws SQLException {
        IUserDAO userDAO = ctx.getUserDao();
        MailService mailService = ctx.getMailService();
        User user = userDAO.findUser("email",emailField.getText());
        if(user != null){
            String msg = String.format("Hello %s,%n" +
                    "Your PeerPractice password can be reset by accessing the folowing link: %s.%n" +
                    "If you did not request a new password, please ignore this email.",user.getFirstName(),"null");
            mailService.sendMessage(msg,emailField.getText());
            messageLabel.setText("Password reset link has been sent to your email. The link will expire in 10 minutes.");
            ctx.getUserSession().setCurrentUser(user);
        }
        else{
            messageLabel.setText("Email does not exist or is not valid.");
        }
        nav.Display(View.ResetPassword);

    }

    @FXML
    private void onBackToLogin() {
        nav.Display(View.Login);
    }
}

