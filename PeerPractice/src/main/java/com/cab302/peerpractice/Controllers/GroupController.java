package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Group;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.animation.*;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.*;

import java.util.Comparator;
import java.util.List;


public class GroupController extends BaseController{
    public Button createGroupButton;
    public Button joinGroupButton;
    @FXML private BorderPane menu;
    @FXML private BorderPane profile;
    @FXML private ComboBox<String> availabilityStatus;
    @FXML private TabPane groupTabs;
    @FXML private Label tabContentLabel;
    @FXML private ListView<Group> groupListView;
    @FXML private Label groupNameLabel;
    @FXML private Button addGroupButton;
    @FXML private Button sortGroupsButton;

    private boolean menuOpen = false;
    private boolean profileOpen = false;
    private boolean sortAlphabetical = false;

    public GroupController(AppContext ctx, Navigation nav) {
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
            if (availabilityStatus.getItems().isEmpty()) {
                availabilityStatus.getItems().setAll();
            }
            availabilityStatus.getSelectionModel().select("Online");
            availabilityStatus.getSelectionModel().selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> {
                        System.out.println("Status changed to: " + newValue);
                    });
        }

        // Update placeholder text when switching tabs
        if (groupTabs != null && tabContentLabel != null) {
            groupTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    tabContentLabel.setText(newTab.getText().toUpperCase());
                }
            });
        }

        // Load groups into the sidebar
        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Group> userGroups = ctx.getGroupDao().searchByUser(currentUser);
        groupListView.setItems(FXCollections.observableArrayList(userGroups));

        groupListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Group group, boolean empty) {
                super.updateItem(group, empty);
                setText(empty || group == null ? null : group.getName());
            }
        });

        groupListView.getSelectionModel().selectedItemProperty().addListener((obs, oldGroup, newGroup) -> {
            if (newGroup != null) {
                groupNameLabel.setText(newGroup.getName());
                // TODO: load this group's data into the tabs later
            }
        });
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

    private void openCreateGroupDialog() {
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
                try {
                    User currentUser = ctx.getUserSession().getCurrentUser();

                    // Create group
                    ctx.getGroupManager().createGroup(
                            nameField.getText(),
                            descriptionField.getText(),
                            approvalCheck.isSelected(),
                            currentUser
                    );

                    // Auto-join group (safe to call again — GroupManager will handle logic)
                    Group newGroup = ctx.getGroupDao().searchByName(nameField.getText()).stream().findFirst().orElse(null);
                    if (newGroup != null) {
                        ctx.getGroupManager().joinGroup(newGroup, currentUser);
                    }

                    refreshGroupList();
                    groupListView.getSelectionModel().select(newGroup); // auto-select new group

                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Group created and joined successfully!");
                    alert.showAndWait();
                } catch (Exception e) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Error creating group: " + e.getMessage());
                    alert.showAndWait();
                }
            }
        });
    }

    private void openJoinGroupDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Join Study Group");
        dialog.setHeaderText("Enter the code to join the group:");
        dialog.setContentText("Group Code:");

        dialog.showAndWait().ifPresent(code -> {
            try {
                Group group = ctx.getGroupDao().getAllGroups().stream()
                        .filter(g -> String.valueOf(g.getID()).equals(code))
                        .findFirst()
                        .orElse(null);

                if (group == null) {
                    Alert alert = new Alert(Alert.AlertType.ERROR, "No group found with that code.");
                    alert.showAndWait();
                    return;
                }

                User currentUser = ctx.getUserSession().getCurrentUser();
                ctx.getGroupManager().joinGroup(group, currentUser);

                refreshGroupList();
                Alert alert = new Alert(Alert.AlertType.INFORMATION, "Request to join sent / group joined successfully!");
                alert.showAndWait();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Error joining group: " + e.getMessage());
                alert.showAndWait();
            }
        });
    }

    @FXML
    private void onAddGroup() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Add Group");
        alert.setHeaderText("Would you like to create a new group or join an existing one?");

        ButtonType createButton = new ButtonType("Create Group");
        ButtonType joinButton = new ButtonType("Join Group");
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(createButton, joinButton, cancelButton);

        alert.showAndWait().ifPresent(response -> {
            if (response == createButton) {
                openCreateGroupDialog();
            } else if (response == joinButton) {
                openJoinGroupDialog();
            }
        });
    }

    private void refreshGroupList() {
        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Group> userGroups = ctx.getGroupDao().searchByUser(currentUser);

        if (sortAlphabetical) {
            userGroups.sort(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER));
        } else {
            userGroups.sort(Comparator.comparing(Group::getCreated_at)); // assumes your Group has created_at
        }

        groupListView.setItems(FXCollections.observableArrayList(userGroups));

        // Auto-select first group if available
        if (!userGroups.isEmpty()) {
            groupListView.getSelectionModel().select(0);
            groupNameLabel.setText(userGroups.getFirst().getName());
        }
    }

    @FXML
    private void onSortGroups() {
        sortAlphabetical = !sortAlphabetical;
        refreshGroupList();

        // Toggle icon
        if (sortAlphabetical) {
            sortGroupsButton.setText("A-Z"); // Alphabetical
        } else {
            sortGroupsButton.setText("Date"); // Join date
        }
    }

}
