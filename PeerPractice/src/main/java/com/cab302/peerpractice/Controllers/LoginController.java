package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController extends BaseController {
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMe;
    @FXML private Hyperlink forgotpasswordlink;
    @FXML private Hyperlink signupLink;
    @FXML private Label messageLabel;

    protected LoginController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    private Navigation navigate() {
        return (Navigation) loginButton.getScene().getWindow().getUserData();
    }

    @FXML
    private void initialize() {
        // Disables login button until email and password field are filled
        loginButton.disableProperty()
                .bind(emailField.textProperty().isEmpty()
                .or(passwordField.textProperty().isEmpty()));

        // Event handlers
        loginButton.setOnAction(e -> login());
        forgotpasswordlink.setOnAction(e -> navigate().Display(View.ForgotPassword));
        signupLink.setOnAction(e -> navigate().Display(View.Signup));
    }

    private void login() {
        messageLabel.setText("");
        navigate().Display(View.MainMenu);
    }
}
