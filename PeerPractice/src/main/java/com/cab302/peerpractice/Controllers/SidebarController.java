package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public abstract class SidebarController extends BaseController {

    // Root panels from includes
    @FXML private BorderPane menu;
    @FXML private BorderPane profile;
    @FXML private VBox header;
    @FXML private Label userNameLabel;
    @FXML private Label userUsernameLabel;

    private static final Duration SLIDE = Duration.millis(180);

    protected SidebarController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    public void initialize() {
        // Restore menu state
        if (menu != null) {
            if (ctx.isMenuOpen()) {
                menu.setVisible(true);
                menu.setManaged(true);
                menu.setTranslateX(0);
            } else {
                menu.setVisible(false);
                menu.setManaged(false);
                menu.setTranslateX(0);
            }
        }

        // Restore profile state
        if (profile != null) {
            if (ctx.isProfileOpen()) {
                profile.setVisible(true);
                profile.setManaged(true);
                profile.setTranslateX(0);
            } else {
                profile.setVisible(false);
                profile.setManaged(false);
                profile.setTranslateX(0);
            }
        }

        // Header buttons
        if (header != null) {
            Button menuBtn = (Button) header.lookup("#menuToggleButton");
            Button profileBtn = (Button) header.lookup("#profileToggleButton");
            Label title = (Label) header.lookup("#headerTitle");

            if (menuBtn != null) menuBtn.setOnAction(e -> onToggleMenu());
            if (profileBtn != null) profileBtn.setOnAction(e -> onToggleProfile());
            if (title != null) setHeaderTitle("Peer Practice - GROUPTEN");
        }

        // Sidebar menu buttons
        if (menu != null) {
            Button studyGroupBtn = (Button) menu.lookup("#studyGroupButton");
            Button calendarBtn = (Button) menu.lookup("#calendarButton");
            Button friendsBtn = (Button) menu.lookup("#friendsButton");

            if (studyGroupBtn != null) studyGroupBtn.setOnAction(e -> nav.DisplayMainMenuOrGroup());
            if (calendarBtn != null) calendarBtn.setOnAction(e -> nav.Display(View.Calendar));
            if (friendsBtn != null) {
                friendsBtn.setOnAction(e ->
                        new Alert(Alert.AlertType.INFORMATION, "Friends view coming soon!").showAndWait()
                );
            }
        }

        // Profile panel controls
        if (profile != null) {
            Label userNameLbl = (Label) profile.lookup("#userNameLabel");
            this.userNameLabel = userNameLbl;
            Label userUsernameLbl = (Label) profile.lookup("#userUsernameLabel");
            this.userUsernameLabel = userUsernameLbl;
            ComboBox<String> status = (ComboBox<String>) profile.lookup("#availabilityStatus");
            Button editBtn = (Button) profile.lookup("#editProfileButton");
            Button settingsBtn = (Button) profile.lookup("#settingsButton");
            Button logoutBtn = (Button) profile.lookup("#logoutButton");

            // User info
            var currentUser = ctx.getUserSession().getCurrentUser();
            if (currentUser != null) {
                if (userNameLbl != null) userNameLbl.setText(currentUser.getFirstName() + " " + currentUser.getLastName());
                if (userUsernameLbl != null) userUsernameLbl.setText("@" + currentUser.getUsername());
            } else {
                if (userNameLbl != null) userNameLbl.setText("Not logged in");
                if (userUsernameLbl != null) userUsernameLbl.setText("@unknown");
            }

            // Availability
            if (status != null && status.getItems().isEmpty()) {
                status.getItems().addAll("Online", "Away", "Busy", "Offline");
                status.getSelectionModel().select("Online");
            }

            // Profile buttons
            if (editBtn != null) editBtn.setOnAction(this::onEditProfile);
            if (settingsBtn!= null) settingsBtn.setOnAction(this::onSetting);
            if (logoutBtn != null) logoutBtn.setOnAction(e -> handleLogout());
        }
    }

    // Set header title
    protected void setHeaderTitle(String title) {
        if (header != null) {
            Label titleLbl = (Label) header.lookup("#headerTitle");
            if (titleLbl != null) titleLbl.setText(title);
        }
    }

    // Toggle menu
    @FXML
    protected void onToggleMenu() {
        if (ctx.isMenuOpen()) closeMenu();
        else openMenu();
    }

    // Toggle profile
    @FXML
    protected void onToggleProfile() {
        if (ctx.isProfileOpen()) closeProfile();
        else openProfile();
    }

    // Slide open menu
    private void openMenu() {
        menu.setVisible(true);
        menu.setManaged(true);
        menu.setTranslateX(-menu.getPrefWidth());
        animate(menu, 0, () -> ctx.setMenuOpen(true));
    }

    // Slide close menu
    private void closeMenu() {
        double width = menu.getPrefWidth();
        animate(menu, -width, () -> {
            ctx.setMenuOpen(false);
            menu.setVisible(false);
            menu.setManaged(false);
            menu.setTranslateX(0);
        });
    }

    // Slide open profile
    private void openProfile() {
        profile.setVisible(true);
        profile.setManaged(true);
        double width = safePrefWidth(profile, 180);
        profile.setTranslateX(width);
        animate(profile, 0, () -> ctx.setProfileOpen(true));
    }

    // Slide close profile
    private void closeProfile() {
        double width = safePrefWidth(profile, 180);
        animate(profile, width, () -> {
            ctx.setProfileOpen(false);
            profile.setVisible(false);
            profile.setManaged(false);
            profile.setTranslateX(0);
        });
    }

    // Animate sliding
    private void animate(Region node, double targetX, Runnable onComplete) {
        TranslateTransition tt = new TranslateTransition(SLIDE, node);
        tt.setToX(targetX);
        tt.setOnFinished(e -> { if (onComplete != null) onComplete.run(); });
        tt.play();
    }

    private double safePrefWidth(Region r, double fallback) {
        double w = r.getPrefWidth();
        return Double.isNaN(w) || w <= 0 ? fallback : w;
    }

    // Handle logout
    private void handleLogout() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Logout");
        confirm.setHeaderText("Are you sure you want to logout?");
        confirm.setContentText("You will be returned to the login screen.");

        confirm.getButtonTypes().setAll(
                new ButtonType("Logout", ButtonType.OK.getButtonData()),
                new ButtonType("Cancel", ButtonType.CANCEL.getButtonData())
        );

        confirm.showAndWait().ifPresent(response -> {
            if (response.getButtonData() == ButtonType.OK.getButtonData()) {
                performLogout();
            }
        });
    }

    // Perform logout
    private void performLogout() {
        try {
            String currentUserName = ctx.getUserSession().getCurrentUser() != null
                    ? ctx.getUserSession().getCurrentUser().getFirstName()
                    : "User";

            ctx.getUserSession().logout();

            if (ctx.isMenuOpen()) closeMenu();
            if (ctx.isProfileOpen()) closeProfile();

            showLogoutSuccessMessage(currentUserName);
            nav.Display(View.Login);

        } catch (Exception e) {
            handleLogoutError(e);
        }
    }

    // Show logout message
    private void showLogoutSuccessMessage(String userName) {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Logout Successful");
        success.setHeaderText("Goodbye, " + userName + "!");
        success.setContentText("You have been successfully logged out.");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> success.close()));
        timeline.play();
        success.show();
    }

    // Handle logout errors
    private void handleLogoutError(Exception e) {
        ctx.getUserSession().logout();
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Logout Error");
        error.setHeaderText("Logout failed");
        error.setContentText("You have been logged out for security, but some cleanup may not have completed.");
        error.showAndWait();
        nav.Display(View.Login);
    }

    @FXML
    private void onEditProfile(ActionEvent event) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.EditProfile.url());
            loader.setControllerFactory(cls -> {
                try {
                    return cls.getDeclaredConstructor(AppContext.class, Navigation.class)
                            .newInstance(ctx, nav);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            EditProfileController controller = loader.getController();

            // Open modal
            Stage dialog = new Stage();
            dialog.setTitle("Edit Profile");
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.setStage(dialog);
            dialog.showAndWait();
            renderProfile();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void onSetting(ActionEvent event) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.SettingProfile.url());
            loader.setControllerFactory(cls -> {
                try {
                    return cls.getDeclaredConstructor(AppContext.class, Navigation.class)
                            .newInstance(ctx, nav);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            SettingProfileController controller = loader.getController();

            // Open modal
            Stage dialog = new Stage();
            dialog.setTitle("Settings");
            dialog.initOwner(((Node) event.getSource()).getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.setStage(dialog);
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Updates profile firstname, lastname, and username on sidebar
    private void renderProfile() {
        if (userNameLabel == null || userUsernameLabel == null) {
            // included profile pane not resolved yet
            return;
        }
        var s = ctx.getUserSession();
        if (s == null || !s.isLoggedIn()) return;
        var u = s.getCurrentUser();
        if (u == null) return;

        String first = u.getFirstName() == null ? "" : u.getFirstName().trim();
        String last  = u.getLastName()  == null ? "" : u.getLastName().trim();
        userNameLabel.setText((first + " " + last).trim().replaceAll("\\s+", " "));
        userUsernameLabel.setText(u.getUsername() == null || u.getUsername().isBlank()
                ? "" : "@" + u.getUsername().trim());
    }

}
