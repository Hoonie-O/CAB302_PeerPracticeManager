package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Group;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.*;

import java.io.IOException;
import java.net.URL;


public class MainMenuController extends BaseController{
    public Button createGroupButton;
    public Button joinGroupButton;
    @FXML private BorderPane menu;
    @FXML private BorderPane profile;
    @FXML private ComboBox<String> availabilityStatus;
    @FXML private Label userNameLabel;
    @FXML private Label userUsernameLabel;
    @FXML private VBox studygroupPane;
    @FXML private AnchorPane calendarPane;

    private boolean menuOpen = false;
    private boolean profileOpen = false;

    public MainMenuController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    private void initialize() {
        // Menu hidden by default
        menu.setVisible(false);
        menu.setManaged(false);

        // Profile hidden by default
        profile.setVisible(false);
        profile.setManaged(false);

        // Initialise user profile information
        initializeUserProfile();

        // This is the availability status dropdown
        if (availabilityStatus != null) {
            // Ensure dropdown is not empty
            if (availabilityStatus.getItems().isEmpty()) {
                availabilityStatus.getItems().setAll();
            }
            // Default selection
            availabilityStatus.getSelectionModel().select("Online");
            // Changes in availability selection
            availabilityStatus.getSelectionModel().selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                System.out.println("Status changed to: " + newValue);
            });
        }
    }

    /**
     * This part initialises the user profile section with current user information.
     * Safely handles cases where user might not be logged in.
     */
    private void initializeUserProfile() {
        var currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser != null) {
            // Update UI with actual user data
            if (userNameLabel != null) {
                userNameLabel.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
            }
            if (userUsernameLabel != null) {
                userUsernameLabel.setText("@" + currentUser.getUsername());
            }
        } else {
            // Case where user is not logged in (this shouldnt happen in normal flow tho)
            if (userNameLabel != null) {
                userNameLabel.setText("Not logged in");
            }
            if (userUsernameLabel != null) {
                userUsernameLabel.setText("@unknown");
            }
        }
    }
    @FXML
    private void onOpenCalendar(javafx.event.ActionEvent event) {
        // Display Calendar view
        onOpenCalendarCenter();
    }

    @FXML
    private void onLogout(javafx.event.ActionEvent event) {
        handleLogout();
    }

    /**
     * This handles the logout process with proper session cleanup and user confirmation.
     * Shows a confirmation dialog before logging out to prevent accidental logouts.
     */
    private void handleLogout() {
        // Show confirmation dialog
        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmationAlert.setTitle("Confirm Logout");
        confirmationAlert.setHeaderText("Are you sure you want to logout?");
        confirmationAlert.setContentText("You will be returned to the login screen.");
        
        // Customise button text
        confirmationAlert.getButtonTypes().setAll(
            new ButtonType("Logout", ButtonType.OK.getButtonData()),
            new ButtonType("Cancel", ButtonType.CANCEL.getButtonData())
        );
        
        confirmationAlert.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonType.OK.getButtonData()) {
                performLogout();
            }
        });
    }

    /**
     * This section performs the actual logout operation with proper session cleanup.
     * Shows success message and handles errors gracefully.
     */
    private void performLogout() {
        try {
            String currentUserName = ctx.getUserSession().getCurrentUser() != null ? 
                ctx.getUserSession().getCurrentUser().getFirstName() : "User";
            
            // Clear the user session
            ctx.getUserSession().logout();
            
            // Close any open sidebars for clean state
            if (menuOpen) {
                closeMenu();
            }
            if (profileOpen) {
                closeProfile();
            }
            
            // Show success message
            showLogoutSuccessMessage(currentUserName);
            
            // Navigate back to login screen
            nav.Display(View.Login);
            
        }
        catch (Exception e) {
            handleLogoutError(e);
        }
    }

    /**
     * Shows a success message after successful logout.
     */
    private void showLogoutSuccessMessage(String userName) {
        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
        successAlert.setTitle("Logout Successful");
        successAlert.setHeaderText("Goodbye, " + userName + "!");
        successAlert.setContentText("You have been successfully logged out. Thank you for using Peer Practice.");
        
        // Auto-close after 2 seconds
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> successAlert.close()));
        timeline.play();
        
        successAlert.show();
    }

    /**
     * Handles logout errors with appropriate user feedback.
     */
    private void handleLogoutError(Exception e) {
        System.err.println("Error during logout: " + e.getMessage());
        
        // Force logout for security even if error occurred
        ctx.getUserSession().logout();
        
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Logout Error");
        errorAlert.setHeaderText("Logout encountered an issue");
        errorAlert.setContentText("You have been logged out for security, but some cleanup may not have completed properly.");
        errorAlert.showAndWait();
        
        // Still navigate to login screen for security
        nav.Display(View.Login);
    }
    // Duration of the slide-in and slide out animation
    private static final Duration SLIDE = Duration.millis(180);

    private void animate(Region sidebar, double targetX, Runnable onComplete) {
        TranslateTransition transition = new TranslateTransition(SLIDE, sidebar);
        transition.setToX(targetX);                     // Make changes to target value
        transition.setOnFinished(event -> {
            if (onComplete != null) onComplete.run();   // Make sure animation ends with hiding sidebar
        });
        transition.play();
    }

    @FXML
    private void onToggleMenu() {
        if (!menuOpen) openMenu();      // Open if closed
        else closeMenu();               // Close if opened
    }

    private void openMenu() {
        menu.setVisible(true);
        menu.setManaged(true);
        double width = menu.getPrefWidth();             // Get width of sidebar
        menu.setTranslateX(-width);                     // Start hidden and opens from the left
        animate(menu, 0, () -> menuOpen = true); // Animate to view the menu
    }

    private void closeMenu() {
        double width = menu.getPrefWidth();
        animate(menu, -width, () -> {
            menuOpen = false;
            // Hide the menu when it is closed
            menu.setVisible(false);
            menu.setManaged(false);
            menu.setTranslateX(0);
        });
    }

    @FXML
    private void onToggleProfile() {
        if (profileOpen) closeProfile();    // Open if closed
        else openProfile();                 // Close if opened
    }

    private void openProfile() {
        profile.setVisible(true);
        profile.setManaged(true);
        double width = profile.getPrefWidth();                  // Get width of sidebar
        profile.setTranslateX(width);                           // Start hidden and opens from the right
        animate(profile, 0, () -> profileOpen = true);   // Animate to view the profile
    }

    private void closeProfile() {
        double width = profile.getPrefWidth();
        animate(profile, width, () -> {
            profileOpen = false;
            // Hide the profile when it is closed
            profile.setVisible(false);
            profile.setManaged(false);
            profile.setTranslateX(0);
        });
    }

    @FXML
    private void onOpenCalendarCenter() {
        studygroupPane.setVisible(false);
        studygroupPane.setManaged(false);
        calendarPane.setVisible(true);
        calendarPane.setManaged(true);
    }

    @FXML
    private void onOpenStudyGroup() {
        calendarPane.setVisible(false);
        calendarPane.setManaged(false);
        studygroupPane.setVisible(true);
        studygroupPane.setManaged(true);
    }

    @FXML
    private void onEditProfile(ActionEvent event) {
        try {
            // Locate FXML file
            URL fxml = java.util.Objects
                    .requireNonNull(View.EditProfile.url());
            // Create FXMLLoader
            FXMLLoader loader = new FXMLLoader(fxml);
            loader.setControllerFactory(cls -> {
                try {
                    if (cls == EditProfileController.class) {
                        return new EditProfileController(ctx, nav);
                    }
                    return cls.getDeclaredConstructor().newInstance();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });

            // Load view and get controller
            Parent root = loader.load();
            EditProfileController controller = loader.getController();

            // Create and configure a modal dialog 'Stage'
            Stage dialog = new Stage();
            dialog.setTitle("Edit profile");
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow()); // Ensure that dialog stays on top of main window
            dialog.initModality(Modality.WINDOW_MODAL); // Blocks main window until this window is closed
            dialog.setResizable(false); // Blocks window resize
            dialog.setScene(new Scene(root)); // Load FXML content inside the opened window
            controller.setStage(dialog); // Allow controller to handle this window
            dialog.showAndWait();   // Show the popup window and wait until it closes
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }
    }
    @FXML
    private void onSetting(ActionEvent event) {
        try {
            // Locate FXML file
            URL fxml = java.util.Objects
                    .requireNonNull(View.SettingProfile.url());
            // Create FXMLLoader
            FXMLLoader loader = new javafx.fxml.FXMLLoader(fxml);
            loader.setControllerFactory(cls -> {
                try {
                    if (cls == SettingProfileController.class) return new SettingProfileController(ctx, nav);
                    return cls.getDeclaredConstructor().newInstance();
                } catch (Exception exception) {
                    throw new RuntimeException(exception);
                }
            });

            // Load view and get controller
            Parent root = loader.load();
            SettingProfileController controller = loader.getController();

            // Create and configure a modal dialog 'Stage'
            var dialog = new javafx.stage.Stage();
            dialog.setTitle("Settings");
            dialog.initOwner(((javafx.scene.Node) event.getSource()).getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new javafx.scene.Scene(root));
            controller.setStage(dialog);
            dialog.showAndWait();
        }
        catch (java.io.IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    private void onCreateGroup() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Study Group");
        dialog.setHeaderText("Enter group details:");

        TextField nameField = new TextField();
        nameField.setPromptText("Group name");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");

        CheckBox approvalCheck = new CheckBox("Require approval to join");

        VBox content = new VBox(10, new Label("Group name:"), nameField,
                new Label("Description:"), descriptionField,
                approvalCheck);
        dialog.getDialogPane().setContent(content);

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == createButton) {
                String name = nameField.getText();
                String description = descriptionField.getText();
                boolean requireApproval = approvalCheck.isSelected();

                try {
                    User currentUser = ctx.getUserSession().getCurrentUser();
                    ctx.getGroupManager().createGroup(name, description, requireApproval, currentUser);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Group created successfully!");
                    alert.showAndWait();
                    nav.Display(View.Groups);
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating group: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    @FXML
    private void onJoinGroup() {
        boolean joined = false;

        while (!joined) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Join Study Group");
            dialog.setHeaderText("Enter the code to join the group:");
            dialog.setContentText("Group Code:");

            var result = dialog.showAndWait();
            if (result.isEmpty()) {
                break;
            }

            String code = result.get();
            try {
                Group group = ctx.getGroupDao().getAllGroups().stream()
                        .filter(g -> String.valueOf(g.getID()).equals(code))
                        .findFirst()
                        .orElse(null);

                if (group == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "No group found with that code. Please try again.");
                    alert.showAndWait();
                    continue;
                }

                User currentUser = ctx.getUserSession().getCurrentUser();
                ctx.getGroupManager().joinGroup(group, currentUser);

                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Request to join sent / group joined successfully!");
                alert.showAndWait();
                joined = true;
                nav.Display(View.Groups);
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error joining group: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}
