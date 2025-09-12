package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.animation.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.*;


public class MainMenuController extends BaseController{
    @FXML private BorderPane menu;
    @FXML private BorderPane profile;
    @FXML private ComboBox<String> availabilityStatus;

    private boolean menuOpen = false;
    private boolean profileOpen = false;

    protected MainMenuController(AppContext ctx, Navigation nav) {
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
        Navigation navigate = (Navigation) ((javafx.scene.Node) event.getSource())
                .getScene().getWindow().getUserData();
        // Display Calendar view
        navigate.Display(View.Calendar);
    }

    @FXML
    private void onBackToLogin(javafx.event.ActionEvent event) {
        Navigation navigate = (Navigation) ((javafx.scene.Node) event.getSource())
                .getScene().getWindow().getUserData();
        // Display Login view
        navigate.Display(View.Login);
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
}
