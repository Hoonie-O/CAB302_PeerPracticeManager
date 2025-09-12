package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Exceptions.InvalidPasswordException;
import com.cab302.peerpractice.Model.*;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Button;
import javafx.scene.control.Label;


public class ResetPasswordController extends BaseController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPassword;
    @FXML private Button confirmButton;
    @FXML private Button backToLogin;
    @FXML private Label messageLabel;
    private final PasswordValidator passwordValidator = new PasswordValidator();

    public ResetPasswordController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    public void onConfirm(ActionEvent actionEvent) {
        try {
            UserManager userManager = ctx.getUserManager();
            User user = ctx.getUserSession().getCurrentUser();

            if(newPasswordField.getText().isEmpty()) {
                messageLabel.setText("Password has not been filled");
            }
            else if(!confirmPassword.getText().equals(newPasswordField.getText())){
                messageLabel.setText("Passwords don't match");
            }
            else if(PasswordValidator.validate(newPasswordField.getText())){
                userManager.changePassword(user,confirmPassword.getText());
                messageLabel.setText("Password has been changed");
            }
        }catch(InvalidPasswordException e){
            messageLabel.setText(e.getMessage());
        }
    }

    public void onBackToLogin(ActionEvent actionEvent) {
        nav.Display(View.Login);
    }
}
