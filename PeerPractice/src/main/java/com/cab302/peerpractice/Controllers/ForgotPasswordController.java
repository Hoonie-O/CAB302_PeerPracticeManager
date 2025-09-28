package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.DAOs.IUserDAO;
import com.cab302.peerpractice.Model.Managers.MailService;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;

/**
 * <hr>
 * Controller for handling password recovery functionality.
 *
 * <p>This controller manages the process of resetting forgotten user passwords
 * by verifying email addresses and sending password reset links. It provides
 * user verification and email notification services for account recovery.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Email validation and user verification</li>
 *   <li>Password reset link generation and email delivery</li>
 *   <li>Integration with MailService for email operations</li>
 *   <li>Navigation to password reset workflow</li>
 * </ul>
 *
 * @see User
 * @see MailService
 * @see BaseController
 */
public class ForgotPasswordController extends BaseController {
    /** <hr> Text field for entering email address for password recovery. */
    @FXML private TextField emailField;
    /** <hr> Button for initiating password reset process. */
    @FXML private Button sendButton;
    /** <hr> Button for returning to login screen. */
    @FXML private Button backButton;
    /** <hr> Label for displaying status messages to the user. */
    @FXML private Label messageLabel;

    /**
     * <hr>
     * Constructs a new ForgotPasswordController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public ForgotPasswordController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Handles the password reset request process.
     *
     * <p>Validates the provided email address, checks for user existence,
     * sends a password reset email, and navigates to the password reset screen
     * upon successful verification.
     *
     * <p>Displays appropriate status messages for both successful and failed
     * reset attempts. Sets the current user session upon successful email
     * verification for subsequent password reset operations.
     *
     * @throws SQLException if database access errors occur during user lookup
     */
    @FXML
    private void onSendResetLink() throws SQLException {
        IUserDAO userDAO = ctx.getUserDAO();
        MailService mailService = ctx.getMailService();
        User user = userDAO.findUser("email",emailField.getText());
        if(user != null){
            String msg = String.format("Hello %s,%n" +
                    "Your PeerPractice password can be reset by accessing the folowing link: %s.%n" +
                    "If you did not request a new password, please ignore this email.",user.getFirstName(),"null");
            mailService.sendMessage(msg,emailField.getText());
            messageLabel.setText("Password reset link has been sent to your email. The link will expire in 10 minutes.");

            ctx.getUserSession().setCurrentUser(user);
            nav.Display(View.ResetPassword);
        }
        else{
            messageLabel.setText("Email does not exist or is not valid.");
        }

    }

    /**
     * <hr>
     * Handles navigation back to the login screen.
     *
     * <p>Returns the user to the login view when the back button is clicked,
     * allowing them to attempt login or access other authentication options.
     */
    @FXML
    private void onBackToLogin() {
        nav.Display(View.Login);
    }
}