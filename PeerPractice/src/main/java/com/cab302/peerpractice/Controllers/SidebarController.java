package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Managers.SessionPersistence;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.UiStateStore;
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

/**
 * <hr>
 * Abstract base controller providing sidebar navigation functionality.
 *
 * <p>This controller implements common sidebar navigation features including menu
 * and profile panels with sliding animations. It provides the foundation for
 * main application screens that require navigation capabilities.
 *
 * <p> Key features include:
 * <ul>
 *   <li>Sliding menu and profile panels with animation</li>
 *   <li>User profile display and management</li>
 *   <li>Navigation between main application views</li>
 *   <li>Session management and logout functionality</li>
 *   <li>Modal dialog support for profile editing and settings</li>
 * </ul>
 *
 * @see BaseController
 * @see SessionPersistence
 */
public abstract class SidebarController extends BaseController {

    // Root panels from includes
    /** <hr> The main menu sidebar panel. */
    @FXML private BorderPane menu;
    /** <hr> The user profile sidebar panel. */
    @FXML private BorderPane profile;
    /** <hr> The main header container. */
    @FXML private VBox header;
    /** <hr> Label displaying user's full name. */
    @FXML private Label userNameLabel;
    /** <hr> Label displaying user's username. */
    @FXML private Label userUsernameLabel;
    /** <hr> Label displaying user's biography. */
    @FXML private Label userBioLabel;

    /** <hr> Duration constant for slide animations. */
    private static final Duration SLIDE = Duration.millis(180);

    private final UiStateStore uiStateStore;

    /**
     * <hr>
     * Constructs a new SidebarController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    protected SidebarController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
        this.uiStateStore = ctx.getUiStateStore();
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Restores panel states, sets up event handlers, and populates user information.
     * Calls the parent class initialization for base setup.
     */
    @FXML
    public void initialize() {
        // Restore menu state and observe future updates
        if (menu != null) {
            uiStateStore.menuOpenProperty().addListener((obs, oldValue, newValue) -> applyMenuState(newValue, true));
            applyMenuState(uiStateStore.isMenuOpen(), false);
        }

        // Restore profile state and observe future updates
        if (profile != null) {
            uiStateStore.profileOpenProperty().addListener((obs, oldValue, newValue) -> applyProfileState(newValue, true));
            applyProfileState(uiStateStore.isProfileOpen(), false);
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
            Button availabilityBtn = (Button) menu.lookup("#availabilityButton");
            Button friendsBtn = (Button) menu.lookup("#friendsButton");

            if (studyGroupBtn != null) studyGroupBtn.setOnAction(e -> nav.DisplayMainMenuOrGroup());
            if (availabilityBtn != null) availabilityBtn.setOnAction(e -> nav.Display(View.Availability));
            if (friendsBtn != null) friendsBtn.setOnAction(e -> nav.Display(View.Friends));
        }

        // Profile panel controls
        if (profile != null) {
            Label userNameLbl = (Label) profile.lookup("#userNameLabel");
            this.userNameLabel = userNameLbl;
            Label userUsernameLbl = (Label) profile.lookup("#userUsernameLabel");
            this.userUsernameLabel = userUsernameLbl;
            Label userBioLbl = (Label) profile.lookup("#userBioLabel");
            this.userBioLabel = userBioLbl;

            @SuppressWarnings("unchecked") // Add suppression warning casting on combobox
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

    /**
     * <hr>
     * Sets the header title text.
     *
     * @param title the title text to display in the header
     */
    protected void setHeaderTitle(String title) {
        if (header != null) {
            Label titleLbl = (Label) header.lookup("#headerTitle");
            if (titleLbl != null) titleLbl.setText(title);
        }
    }

    /**
     * <hr>
     * Toggles the menu sidebar open/closed state.
     */
    @FXML
    protected void onToggleMenu() {
        uiStateStore.setMenuOpen(!uiStateStore.isMenuOpen());
    }

    /**
     * <hr>
     * Toggles the profile sidebar open/closed state.
     */
    @FXML
    protected void onToggleProfile() {
        uiStateStore.setProfileOpen(!uiStateStore.isProfileOpen());
    }

    private void applyMenuState(boolean open, boolean animate) {
        if (menu == null) {
            return;
        }
        if (animate) {
            if (open) {
                openMenu();
            } else {
                closeMenu();
            }
        } else {
            if (open) {
                menu.setVisible(true);
                menu.setManaged(true);
                menu.setTranslateX(0);
            } else {
                menu.setVisible(false);
                menu.setManaged(false);
                menu.setTranslateX(0);
            }
        }
    }

    private void applyProfileState(boolean open, boolean animate) {
        if (profile == null) {
            return;
        }
        if (animate) {
            if (open) {
                openProfile();
            } else {
                closeProfile();
            }
        } else {
            if (open) {
                profile.setVisible(true);
                profile.setManaged(true);
                profile.setTranslateX(0);
            } else {
                profile.setVisible(false);
                profile.setManaged(false);
                profile.setTranslateX(0);
            }
        }
    }

    /**
     * <hr>
     * Slides the menu sidebar open with animation.
     */
    private void openMenu() {
        menu.setVisible(true);
        menu.setManaged(true);
        menu.setTranslateX(-menu.getPrefWidth());
        animate(menu, 0, null);
    }

    /**
     * <hr>
     * Slides the menu sidebar closed with animation.
     */
    private void closeMenu() {
        double width = menu.getPrefWidth();
        animate(menu, -width, () -> {
            menu.setVisible(false);
            menu.setManaged(false);
            menu.setTranslateX(0);
        });
    }

    /**
     * <hr>
     * Slides the profile sidebar open with animation.
     */
    private void openProfile() {
        profile.setVisible(true);
        profile.setManaged(true);
        double width = safePrefWidth(profile, 180);
        profile.setTranslateX(width);
        animate(profile, 0, null);
    }

    /**
     * <hr>
     * Slides the profile sidebar closed with animation.
     */
    private void closeProfile() {
        double width = safePrefWidth(profile, 180);
        animate(profile, width, () -> {
            profile.setVisible(false);
            profile.setManaged(false);
            profile.setTranslateX(0);
        });
    }

    /**
     * <hr>
     * Animates a region's translation with a slide effect.
     *
     * @param node the region to animate
     * @param targetX the target X translation value
     * @param onComplete callback to execute after animation completes
     */
    private void animate(Region node, double targetX, Runnable onComplete) {
        TranslateTransition tt = new TranslateTransition(SLIDE, node);
        tt.setToX(targetX);
        tt.setOnFinished(e -> { if (onComplete != null) onComplete.run(); });
        tt.play();
    }

    /**
     * <hr>
     * Safely retrieves a region's preferred width with fallback.
     *
     * @param r the region to get width from
     * @param fallback the fallback width if region width is invalid
     * @return the region's preferred width or fallback value
     */
    private double safePrefWidth(Region r, double fallback) {
        double w = r.getPrefWidth();
        return Double.isNaN(w) || w <= 0 ? fallback : w;
    }

    /**
     * <hr>
     * Handles the logout confirmation process.
     *
     * <p>Displays a confirmation dialog and initiates logout if confirmed by the user.
     */
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

    /**
     * <hr>
     * Performs the user logout operation.
     *
     * <p>Clears user session, closes sidebars, and navigates to the login screen
     * with appropriate success messaging.
     */
    private void performLogout() {
        try {
            String currentUserName = ctx.getUserSession().getCurrentUser() != null
                    ? ctx.getUserSession().getCurrentUser().getFirstName()
                    : "User";

            ctx.getUserSession().logout();
            // clear saved session
            SessionPersistence.clearSession();

            uiStateStore.setMenuOpen(false);
            uiStateStore.setProfileOpen(false);

            showLogoutSuccessMessage(currentUserName);
            nav.Display(View.Login);

        } catch (Exception e) {
            handleLogoutError(e);
        }
    }

    /**
     * <hr>
     * Shows a success message after logout.
     *
     * @param userName the name of the user who logged out
     */
    private void showLogoutSuccessMessage(String userName) {
        Alert success = new Alert(Alert.AlertType.INFORMATION);
        success.setTitle("Logout Successful");
        success.setHeaderText("Goodbye, " + userName + "!");
        success.setContentText("You have been successfully logged out.");

        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> success.close()));
        timeline.play();
        success.show();
    }

    /**
     * <hr>
     * Handles errors that occur during logout.
     *
     * @param e the exception that occurred during logout
     */
    private void handleLogoutError(Exception e) {
        ctx.getUserSession().logout();
        uiStateStore.setMenuOpen(false);
        uiStateStore.setProfileOpen(false);
        Alert error = new Alert(Alert.AlertType.ERROR);
        error.setTitle("Logout Error");
        error.setHeaderText("Logout failed");
        error.setContentText("You have been logged out for security, but some cleanup may not have completed.");
        error.showAndWait();
        nav.Display(View.Login);
    }

    /**
     * <hr>
     * Opens the edit profile dialog.
     *
     * @param event the action event that triggered this operation
     */
    @FXML
    private void onEditProfile(ActionEvent event) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.EditProfileDialog.url());
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

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <hr>
     * Opens the settings dialog.
     *
     * @param event the action event that triggered this operation
     */
    @FXML
    private void onSetting(ActionEvent event) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.SettingProfileDialog.url());
            loader.setControllerFactory(cls -> {
                try {
                    return cls.getDeclaredConstructor(AppContext.class, Navigation.class)
                            .newInstance(ctx, nav);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            SettingController controller = loader.getController();

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

    /**
     * <hr>
     * Updates the profile information displayed in the sidebar.
     *
     * <p>Refreshes the user's name, username, and biography in the profile panel
     * with current session data.
     */
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
        String last = u.getLastName()  == null ? "" : u.getLastName().trim();
        userNameLabel.setText((first + " " + last).trim().replaceAll("\\s+", " "));
        userUsernameLabel.setText(u.getUsername() == null ||
                u.getUsername().isBlank() ? "" : "@" + u.getUsername().trim());

        // Set biography if available
        if (userBioLabel != null) {
            String bio = u.getBio();
            if (bio != null && !bio.trim().isEmpty()) {
                userBioLabel.setText(bio.trim());
                userBioLabel.setVisible(true);
                userBioLabel.setManaged(true);
            } else {
                userBioLabel.setText("");
                userBioLabel.setVisible(false);
                userBioLabel.setManaged(false);
            }
        }
    }

}