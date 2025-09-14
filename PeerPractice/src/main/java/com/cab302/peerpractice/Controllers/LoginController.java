package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import com.cab302.peerpractice.Model.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.List;

public class LoginController extends BaseController{
    @FXML private TextField IDField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private CheckBox rememberMe;
    @FXML private Hyperlink forgotpasswordlink;
    @FXML private Hyperlink signupLink;
    @FXML private Label messageLabel;

    public LoginController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    private final UserManager userManager = ctx.getUserManager();

    @FXML
    private void initialize() {
        // Disable login button until fields filled
        loginButton.disableProperty()
                .bind(IDField.textProperty().isEmpty()
                        .or(passwordField.textProperty().isEmpty()));

        // Event handlers
        loginButton.setOnAction(e -> {
            try {
                login();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        forgotpasswordlink.setOnAction(e -> nav.Display(View.ForgotPassword));
        signupLink.setOnAction(e -> nav.Display(View.Signup));
    }

    private void login() throws SQLException {
        messageLabel.setText("");
        String ID = IDField.getText();
        String password = passwordField.getText();

        try {
            if (userManager.authenticate(ID, password)) {
                // Fetch user from DAO (since authenticate only returns boolean)
                ctx.getUserDao().findUsers().stream()
                        .filter(u -> u.getEmail().equalsIgnoreCase(ID) ||
                                u.getUsername().equalsIgnoreCase(ID))
                        .findFirst().ifPresent(loggedIn -> ctx.getUserSession().setCurrentUser(loggedIn));

                nav.DisplayMainMenuOrGroup();
            } else {
                messageLabel.setText("Invalid email/username or password.");
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e);
        }
    }
}
