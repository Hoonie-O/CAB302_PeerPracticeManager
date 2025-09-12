package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.Model.PasswordHasher;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;

import java.awt.*;

public class ResetPasswordController {

    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPassword;
    @FXML private Button confirmButton;
    @FXML private Button backToLogin;

    public void onConfirm(ActionEvent actionEvent) {

    }

    public void onBackToLogin(ActionEvent actionEvent) {
    }
}
