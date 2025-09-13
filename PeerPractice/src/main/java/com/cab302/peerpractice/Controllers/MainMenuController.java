package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Group;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.*;


public class MainMenuController extends BaseController{
    public Button createGroupButton;
    public Button joinGroupButton;
    @FXML private BorderPane menu;
    @FXML private BorderPane profile;
    @FXML private ComboBox<String> availabilityStatus;

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

        // Availability status dropdown
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
    @FXML
    private void onOpenCalendar(javafx.event.ActionEvent event) {
        // Display Calendar view
        nav.Display(View.Calendar);
    }

    @FXML
    private void onBackToLogin(javafx.event.ActionEvent event) {
        // Display Login view
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
