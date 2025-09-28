package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Exceptions.InvalidPasswordException;
import com.cab302.peerpractice.Model.Managers.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * <hr>
 * Controller for handling password reset functionality.
 *
 * <p>This controller manages the password reset process, allowing users to set a new password
 * after verifying their identity. It includes validation for password matching and strength
 * requirements.
 *
 * <p> Key features include:
 * <ul>
 *   <li>New password and confirmation field validation</li>
 *   <li>Password strength requirement enforcement</li>
 *   <li>Secure password update process</li>
 *   <li>Automatic logout after successful password change</li>
 * </ul>
 *
 * @see BaseController
 * @see UserManager
 * @see InvalidPasswordException
 */
public class ResetPasswordController extends BaseController {

    /** <hr> Field for entering new password. */
    @FXML private PasswordField newPasswordField;
    /** <hr> Field for confirming new password. */
    @FXML private PasswordField confirmPassword;
    /** <hr> Button to confirm password reset. */
    @FXML private Button confirmButton;
    /** <hr> Button to return to login screen. */
    @FXML private Button backToLogin;
    /** <hr> Label for displaying status messages. */
    @FXML private Label messageLabel;

    /**
     * <hr>
     * Constructs a new ResetPasswordController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public ResetPasswordController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Handles the password confirmation and reset process.
     *
     * <p>Validates that the new password and confirmation match and meets system requirements,
     * then updates the user's password in the system. On success, logs the user out and
     * displays a success message.
     *
     * @param actionEvent the action event triggered by the confirm button
     */
    public void onConfirm(ActionEvent actionEvent) {
        try {
            UserManager userManager = ctx.getUserManager();
            String username = ctx.getUserSession().getCurrentUser().getUsername();
            if(newPasswordField.getText().isEmpty()) {
                messageLabel.setText("Password has not been filled");
            }
            else if(!confirmPassword.getText().equals(newPasswordField.getText())){
                messageLabel.setText("Passwords don't match");
            }
            if(userManager.changePassword(username,confirmPassword.getText())) {
                messageLabel.setText("Password has been changed");
                ctx.getUserSession().logout();
            }
        }catch(InvalidPasswordException e){
            messageLabel.setText(e.getMessage());
        } catch (Exception e) {
            System.err.println("SQLException: " + e);
        }
    }

    /**
     * <hr>
     * Navigates back to the login screen.
     *
     * <p>Returns the user to the login interface and clears the current user session
     * to ensure secure logout.
     *
     * @param actionEvent the action event triggered by the back button
     */
    public void onBackToLogin(ActionEvent actionEvent) {
        nav.Display(View.Login);
        ctx.getUserSession().logout();
    }
}