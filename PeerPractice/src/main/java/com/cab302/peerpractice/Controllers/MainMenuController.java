package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;


public class MainMenuController {
    @FXML private Button backButton;

    private Navigation navigate() {
        return (Navigation) backButton.getScene().getWindow().getUserData();
    }

    @FXML
    private void onBackToLogin() {
        navigate().Display(View.Login);
    }
}
