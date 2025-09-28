package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.SessionPersistence;
import com.cab302.peerpractice.Model.Managers.UserManager;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.SQLException;
import java.util.Optional;

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
                // Fetch user username and if not found then fetch from email then login
                Optional<User> loggedIn = ctx.getUserDAO().getUserByUsername(ID);
                if (loggedIn.isEmpty()) {
                    loggedIn = ctx.getUserDAO().getUserByEmail(ID);
                }
                // If logged in then put user into the session
                loggedIn.ifPresent(u -> {
                    ctx.getUserSession().setCurrentUser(u);
                    // save persistent session if remember me checked
                    SessionPersistence.saveSession(u, rememberMe.isSelected());
                });

                nav.DisplayMainMenuOrGroup();
            } else {
                messageLabel.setText("Invalid email/username or password.");
            }
        } catch (SQLException e) {
            System.err.println("SQLException: " + e);
        }
    }
}
