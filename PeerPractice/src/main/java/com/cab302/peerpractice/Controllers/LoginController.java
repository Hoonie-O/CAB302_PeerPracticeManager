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

/**
 * <hr>
 * Controller for handling user authentication and login functionality.
 *
 * <p>This controller manages the user login process including credential validation,
 * session management, and navigation to appropriate application views based on
 * user roles and group memberships.
 *
 * <p>Key features include:
 * <ul>
 *   <li>User credential validation and authentication</li>
 *   <li>Session persistence with remember-me functionality</li>
 *   <li>Navigation to password recovery and signup flows</li>
 *   <li>Automatic redirection based on user group memberships</li>
 * </ul>
 *
 * @see User
 * @see UserManager
 * @see BaseController
 */
public class LoginController extends BaseController{
    /** <hr> Text field for entering username or email address. */
    @FXML private TextField IDField;
    /** <hr> Password field for entering user password. */
    @FXML private PasswordField passwordField;
    /** <hr> Button for initiating login process. */
    @FXML private Button loginButton;
    /** <hr> Checkbox for enabling session persistence. */
    @FXML private CheckBox rememberMe;
    /** <hr> Hyperlink for navigating to password recovery. */
    @FXML private Hyperlink forgotpasswordlink;
    /** <hr> Hyperlink for navigating to user registration. */
    @FXML private Hyperlink signupLink;
    /** <hr> Label for displaying authentication status messages. */
    @FXML private Label messageLabel;

    /**
     * <hr>
     * Manager for handling user authentication and management operations.
     */
    private final UserManager userManager = ctx.getUserManager();

    /**
     * <hr>
     * Constructs a new LoginController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public LoginController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up form validation, button bindings, and event handlers for
     * login functionality and navigation options.
     */
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
                System.err.println("Database error during login: " + ex.getMessage());
                ex.printStackTrace();
                messageLabel.setText("Unable to connect to database. Please try again later.");
            }
        });
        forgotpasswordlink.setOnAction(e -> nav.Display(View.ForgotPassword));
        signupLink.setOnAction(e -> nav.Display(View.Signup));
    }

    /**
     * <hr>
     * Handles the user authentication process.
     *
     * <p>Validates user credentials, manages user session creation, and
     * navigates to appropriate application views upon successful authentication.
     * Provides feedback for failed login attempts and handles session persistence.
     *
     * @throws SQLException if database access errors occur during authentication
     */
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
            System.err.println("SQLException during authentication: " + e.getMessage());
            e.printStackTrace();
            messageLabel.setText("Database error. Please try again later.");
            throw e; // Re-throw to be caught by the outer handler
        }
    }
}