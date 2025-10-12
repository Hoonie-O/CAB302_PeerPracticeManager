package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.DAOs.GroupDAO;
import com.cab302.peerpractice.Model.Entities.*;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static javax.swing.JColorChooser.showDialog;

/**
 * <hr>
 * Controller for managing comprehensive group functionality and user interface.
 *
 * <p>This controller serves as the main hub for group-related operations including
 * group creation, membership management, session scheduling, and collaborative features.
 * It coordinates multiple tabbed interfaces for different group functionalities.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Multi-tab group interface (Calendar, Sessions, Notes, Chat, Files)</li>
 *   <li>Group creation and membership management</li>
 *   <li>Session scheduling and task management</li>
 *   <li>Member invitation and role management</li>
 *   <li>Integration with various group management controllers</li>
 * </ul>
 *
 * @see Group
 * @see Session
 * @see SidebarController
 */
public class GroupController extends SidebarController {
    /** <hr> Tab pane containing all group management interfaces. */
    @FXML private TabPane groupTabs;
    /** <hr> List view for displaying user's groups. */
    @FXML private ListView<Group> groupListView;
    /** <hr> Label displaying the currently selected group name. */
    @FXML private Label groupNameLabel;
    /** <hr> Button for adding new groups or joining existing ones. */
    @FXML private Button addGroupButton;
    /** <hr> Button for toggling group sorting preferences. */
    @FXML private Button sortGroupsButton;

    /** <hr> Table view for displaying group members and their roles. */
    @FXML private TableView<GroupMember> membersTable;
    /** <hr> List view for displaying group study sessions. */
    @FXML private ListView<Session> sessionsListView;

    /**
     * <hr>
     * Controller for managing group calendar functionality.
     */
    private GroupCalendarController groupCalendarController;
    /**
     * <hr>
     * Controller for managing group notes functionality.
     */
    private NotesController notesController;

    private GroupChatController groupChatController;

    /**
     * <hr>
     * Flag indicating current group sorting preference (alphabetical vs date).
     */
    private boolean sortAlphabetical = false;

    /**
     * <hr>
     * Constructs a new GroupController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public GroupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Initializes the controller after FXML loading is complete.
     *
     * <p>Sets up the group management interface including group list population,
     * tab selection handling, and initial content loading for the first group.
     * Calls parent class initialization for common sidebar setup.
     */
    @FXML
    public void initialize() {
        super.initialize();
        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Group> userGroups = ctx.getGroupDAO().searchByUser(currentUser);
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
                // Preload all tabs in background to avoid delays when switching
                preloadAllTabs(newGroup);
                // Reload current tab content with new group
                Tab selectedTab = groupTabs.getSelectionModel().getSelectedItem();
                if (selectedTab != null) {
                    loadTabContent(selectedTab.getText(), newGroup);
                }
            }
        });

        if (!userGroups.isEmpty()) {
            groupListView.getSelectionModel().select(0);
        }

        // Add listener for tab selection to load appropriate content
        groupTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
                if (selectedGroup != null) {
                    loadTabContent(newTab.getText(), selectedGroup);
                }
            }
        });

        // Load initial calendar content if there are groups
        if (!userGroups.isEmpty()) {
            Platform.runLater(() -> {
                Tab selectedTab = groupTabs.getSelectionModel().getSelectedItem();
                if (selectedTab != null && selectedTab.getText().equals("Calendar")) {
                    loadTabContent("Calendar", userGroups.get(0));
                }
            });
        }
    }

    /**
     * <hr>
     * Loads appropriate content for the specified tab based on group selection.
     *
     * <p>Dynamically loads and configures tab content for different group
     * management functionalities including calendar, sessions, notes, chat, and files.
     *
     * @param tabName the name of the tab to load content for
     * @param group the group to load content for
     */
    private void loadTabContent(String tabName, Group group) {
        try {
            Tab selectedTab = groupTabs.getSelectionModel().getSelectedItem();
            if (selectedTab == null) return;

            switch (tabName) {
                case "Calendar":
                    loadCalendarContent(selectedTab, group);
                    break;
                case "Sessions":
                    loadSessionsContent(selectedTab, group);
                    break;
                case "Notes":
                    loadNotesContent(selectedTab, group);
                    break;
                case "Chat":
                    loadChatContent(selectedTab, group);
                    break;
                case "Files":
                    loadFilesContent(selectedTab, group);
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error loading tab content: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Preloads all tabs in the background to eliminate switching delays
     */
    private void preloadAllTabs(Group group) {
        // Run in background thread to avoid blocking UI
        new Thread(() -> {
            try {
                // Small delay to let the current tab load first
                Thread.sleep(50);

                // Preload each tab on the JavaFX thread
                Platform.runLater(() -> {
                    for (Tab tab : groupTabs.getTabs()) {
                        String tabName = tab.getText();
                        // Load each tab's content if not already loaded
                        switch (tabName) {
                            case "Calendar":
                                if (groupCalendarController == null) loadCalendarContent(tab, group);
                                break;
                            case "Notes":
                                if (notesController == null) loadNotesContent(tab, group);
                                break;
                            case "Sessions":
                                loadSessionsContent(tab, group);
                                break;
                            case "Chat":
                                loadChatContent(tab, group);
                                break;
                            case "Files":
                                loadFilesContent(tab, group);
                                break;
                        }
                    }
                });
            } catch (Exception e) {
                System.err.println("Error preloading tabs: " + e.getMessage());
            }
        }).start();
    }

    /**
     * <hr>
     * Loads calendar content for the specified group and tab.
     *
     * <p>Initializes and configures the group calendar interface with
     * session management capabilities for the selected group.
     *
     * @param tab the tab to load calendar content into
     * @param group the group to load calendar for
     */
    private void loadCalendarContent(Tab tab, Group group) {
        try {
            if (groupCalendarController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/peerpractice/group-calendar.fxml"));
                loader.setControllerFactory(cls -> new GroupCalendarController(ctx, nav));
                Parent calendarView = loader.load();
                groupCalendarController = loader.getController();
                tab.setContent(calendarView);
            }
            groupCalendarController.setGroup(group);
        } catch (Exception e) {
            System.err.println("Failed to load calendar: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * <hr>
     * Loads notes content for the specified group and tab.
     *
     * <p>Initializes and configures the group notes interface for
     * collaborative note-taking within the selected group.
     *
     * @param tab the tab to load notes content into
     * @param group the group to load notes for
     */
    private void loadNotesContent(Tab tab, Group group) {
        try {
            if (notesController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/peerpractice/group-notes.fxml"));
                loader.setControllerFactory(cls -> new NotesController(ctx, nav));
                Parent notesView = loader.load();
                notesController = loader.getController();
                tab.setContent(notesView);
            }
            notesController.setGroup(group);
        } catch (Exception e) {
            System.err.println("Failed to load notes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * <hr>
     * Loads sessions content for the specified group and tab.
     *
     * <p>Configures the sessions list interface with study session management
     * capabilities and navigation options for the selected group.
     *
     * @param tab the tab to load sessions content into
     * @param group the group to load sessions for
     */
    private void loadSessionsContent(Tab tab, Group group) {
        VBox sessionsContent = new VBox(10);
        sessionsContent.setPadding(new Insets(20));

        Label header = new Label("Study Sessions");
        header.setFont(Font.font("System", FontWeight.BOLD, 16));

        ListView<Session> sessionsList = new ListView<>();
        updateSessionsListView(sessionsList, group);

        Button addSessionButton = new Button("Add Session");
        addSessionButton.setOnAction(e -> {
            // Navigate to calendar tab to add session
            groupTabs.getSelectionModel().select(0); // Calendar is first tab
        });

        sessionsContent.getChildren().addAll(header, sessionsList, addSessionButton);
        tab.setContent(sessionsContent);
    }

    /**
     * <hr>
     * Loads chat content for the specified group and tab.
     *
     * <p>Configures the group chat interface placeholder with notification
     * about upcoming chat functionality.
     *
     * @param tab the tab to load chat content into
     * @param group the group to load chat for
     */
    private void loadChatContent(Tab tab, Group group) {
        try {
            if (groupChatController == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/cab302/peerpractice/group-chat.fxml"));
                loader.setControllerFactory(cls -> new GroupChatController(ctx, nav));
                Parent chatView = loader.load();
                groupChatController = loader.getController();
                tab.setContent(chatView);
            }
            groupChatController.setGroup(group);
        } catch (Exception e) {
            System.err.println("Failed to load group chat: " + e.getMessage());
            e.printStackTrace();
        }
    }


    /**
     * <hr>
     * Loads files content for the specified group and tab.
     *
     * <p>Configures the file sharing interface placeholder with notification
     * about upcoming file sharing functionality.
     *
     * @param tab the tab to load files content into
     * @param group the group to load files for
     */
    private void loadFilesContent(Tab tab, Group group) {
        VBox filesContent = new VBox(10);
        filesContent.setPadding(new Insets(20));

        Label comingSoon = new Label("File sharing functionality coming soon!");
        comingSoon.setFont(Font.font("System", FontWeight.BOLD, 14));

        filesContent.getChildren().add(comingSoon);
        tab.setContent(filesContent);
    }

    /**
     * <hr>
     * Updates the sessions list view with current group sessions.
     *
     * <p>Retrieves and displays all study sessions for the specified group
     * with interactive elements for session management.
     *
     * @param sessionsList the list view to update with session data
     * @param group the group to retrieve sessions for
     */
    private void updateSessionsListView(ListView<Session> sessionsList, Group group) {
        List<Session> groupSessions = ctx.getSessionCalendarManager().getSessionsForGroup(group);

        sessionsList.setCellFactory(listView -> new ListCell<Session>() {
            @Override
            protected void updateItem(Session session, boolean empty) {
                super.updateItem(session, empty);
                if (empty || session == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox sessionItem = new VBox(5);
                    sessionItem.setPadding(new Insets(8));
                    sessionItem.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

                    Label titleLabel = new Label(session.getTitle());
                    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

                    Label timeLabel = new Label(
                            session.getStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) +
                                    " - " + session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    );
                    timeLabel.setFont(Font.font("System", 10));

                    HBox buttonBox = new HBox(8);
                    Button viewTasksButton = new Button("View Tasks");
                    viewTasksButton.setOnAction(e -> nav.openSessionTasks(session.getSessionId()));

                    Button addTaskButton = new Button("Add Task");
                    addTaskButton.setOnAction(e -> showAddTaskDialog(session));

                    buttonBox.getChildren().addAll(viewTasksButton, addTaskButton);
                    sessionItem.getChildren().addAll(titleLabel, timeLabel, buttonBox);

                    setGraphic(sessionItem);
                    setText(null);
                }
            }
        });

        sessionsList.setItems(FXCollections.observableArrayList(groupSessions));
    }

    /**
     * <hr>
     * Opens the group creation dialog for creating new study groups.
     *
     * <p>Displays a modal dialog for users to input group details including
     * name, description, and join approval settings. Creates the group and
     * automatically adds the creator as a member upon successful completion.
     */
    private void openCreateGroupDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Create Study Group");
        dialog.setHeaderText("Enter group details:");

        TextField nameField = new TextField();
        nameField.setPromptText("Group name");

        TextArea descriptionField = new TextArea();
        descriptionField.setPromptText("Description");

        CheckBox approvalCheck = new CheckBox("Require approval to join");

        VBox content = new VBox(10,
                new Label("Group name:"), nameField,
                new Label("Description:"), descriptionField,
                approvalCheck);
        dialog.getDialogPane().setContent(content);

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == createButton) {
                try {
                    User currentUser = ctx.getUserSession().getCurrentUser();
                    ctx.getGroupManager().createGroup(
                            nameField.getText(),
                            descriptionField.getText(),
                            approvalCheck.isSelected(),
                            currentUser
                    );
                    Group newGroup = ctx.getGroupDAO()
                            .searchByName(nameField.getText())
                            .stream()
                            .findFirst()
                            .orElse(null);
                    if (newGroup != null) {
                        ctx.getGroupManager().joinGroup(newGroup, currentUser);
                    }
                    refreshGroupList();
                    groupListView.getSelectionModel().select(newGroup);
                    new Alert(Alert.AlertType.INFORMATION,
                            "Group created and joined successfully!").showAndWait();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR,
                            "Error creating group: " + e.getMessage()).showAndWait();
                }
            }
        });
    }

    /**
     * <hr>
     * Opens the group joining dialog for joining existing study groups.
     *
     * <p>Displays a modal dialog for users to enter group codes and attempts
     * to join the matching group. Provides feedback for invalid codes and
     * successful join operations.
     */
    private void openJoinGroupDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Join Study Group");
        dialog.setHeaderText("Enter the code to join the group:");
        dialog.setContentText("Group Code:");

        dialog.showAndWait().ifPresent(code -> {
            try {
                Group group = ctx.getGroupDAO().getAllGroups().stream()
                        .filter(g -> String.valueOf(g.getID()).equals(code))
                        .findFirst()
                        .orElse(null);
                if (group == null) {
                    new Alert(Alert.AlertType.ERROR,
                            "No group found with that code.").showAndWait();
                    return;
                }
                User currentUser = ctx.getUserSession().getCurrentUser();
                ctx.getGroupManager().joinGroup(group, currentUser);
                refreshGroupList();
                new Alert(Alert.AlertType.INFORMATION,
                        "Request to join sent / group joined successfully!").showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR,
                        "Error joining group: " + e.getMessage()).showAndWait();
            }
        });
    }

    /**
     * <hr>
     * Handles the add group button action.
     *
     * <p>Displays a confirmation dialog allowing users to choose between
     * creating a new group or joining an existing group, then launches
     * the appropriate dialog based on user selection.
     *
     * @param event the action event triggered by the button click
     */
    @FXML
    private void onAddGroup(ActionEvent event) {
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

    /**
     * <hr>
     * Refreshes the group list display with current user groups.
     *
     * <p>Retrieves the current user's groups from the database and updates
     * the list view. Applies current sorting preference and selects the
     * first group if available.
     */
    private void refreshGroupList() {
        User currentUser = ctx.getUserSession().getCurrentUser();
        List<Group> userGroups = ctx.getGroupDAO().searchByUser(currentUser);
        if (sortAlphabetical) {
            userGroups.sort(Comparator.comparing(Group::getName, String.CASE_INSENSITIVE_ORDER));
        } else {
            userGroups.sort(Comparator.comparing(Group::getCreated_at));
        }
        groupListView.setItems(FXCollections.observableArrayList(userGroups));
        if (!userGroups.isEmpty()) {
            groupListView.getSelectionModel().select(0);
            groupNameLabel.setText(userGroups.getFirst().getName());
        }
    }

    /**
     * <hr>
     * Handles group sorting toggle action.
     *
     * <p>Toggles between alphabetical and creation date sorting for the
     * group list and updates the sort button text to reflect current mode.
     *
     * @param event the action event triggered by the sort button click
     */
    @FXML
    private void onSortGroups(ActionEvent event) {
        sortAlphabetical = !sortAlphabetical;
        refreshGroupList();
        if (sortAlphabetical) {
            sortGroupsButton.setText("A-Z");
        } else {
            sortGroupsButton.setText("Date");
        }
    }

    /**
     * <hr>
     * Handles member invitation functionality.
     *
     * <p>Opens a modal dialog for inviting new members to the currently
     * selected group. Loads and displays the invite member interface.
     *
     * @param event the action event triggered by the invite button
     */
    @FXML
    public void onInviteMembers(ActionEvent event) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.InviteMember.url());
            loader.setControllerFactory(cls -> {
                try {
                    return cls.getDeclaredConstructor(AppContext.class, Navigation.class)
                            .newInstance(ctx, nav);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            InviteMemberController controller = loader.getController();

            // Open modal
            Stage dialog = new Stage();
            dialog.setTitle("Invite Members");
            dialog.initOwner(groupTabs.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.setStage(dialog);
            dialog.showAndWait();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <hr>
     * Handles group ID sharing functionality.
     *
     * <p>Opens a modal dialog for sharing the current group's ID with
     * other users. Loads and displays the share group ID interface.
     *
     * @param event the action event triggered by the share button
     */
    @FXML
    public void onShareGroupID(ActionEvent event) {
        try {
            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.ShareGroupID.url());
            loader.setControllerFactory(cls -> {
                try {
                    return cls.getDeclaredConstructor(AppContext.class, Navigation.class)
                            .newInstance(ctx, nav);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            ShareGroupIDController controller = loader.getController();

            // Open modal
            Stage dialog = new Stage();
            dialog.setTitle("Share Group ID");
            dialog.initOwner(groupTabs.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.setStage(dialog);
            dialog.showAndWait();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <hr>
     * Handles group management functionality.
     *
     * <p>Opens a modal dialog for advanced group management operations
     * including member role management and group settings. Loads and
     * displays the manage group interface.
     *
     * @param event the action event triggered by the manage group button
     */
    @FXML
    public void onManageGroup(ActionEvent event) {
        try {
            // Get currently selected group
            Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            if (selectedGroup == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Group Selected");
                alert.setHeaderText("Please select a group");
                alert.setContentText("Select a group from the list to manage it.");
                alert.showAndWait();
                return;
            }

            // Check if user is admin of this group
            User currentUser = ensureLoggedIn();
            if (currentUser == null) return;

            if (!ctx.getGroupManager().isAdmin(selectedGroup, currentUser)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Insufficient Permissions");
                alert.setHeaderText("Admin Access Required");
                alert.setContentText("Only group admins can manage group settings.");
                alert.showAndWait();
                return;
            }

            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.ManageGroup.url());
            loader.setControllerFactory(cls -> {
                try {
                    return cls.getDeclaredConstructor(AppContext.class, Navigation.class)
                            .newInstance(ctx, nav);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            ManageGroupController controller = loader.getController();

            // Set the group BEFORE opening the dialog
            controller.setGroup(selectedGroup);

            // Open modal
            Stage dialog = new Stage();
            dialog.setTitle("Manage Group - " + selectedGroup.getName());
            dialog.initOwner(groupTabs.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.setStage(dialog);
            dialog.showAndWait();

            // Refresh the group list after closing dialog (in case members were changed)
            refreshGroupList();

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the group settings dialog
     */
    @FXML
    public void onGroupSettings(ActionEvent event) {
        try {
            // Get currently selected group
            Group selectedGroup = groupListView.getSelectionModel().getSelectedItem();
            if (selectedGroup == null) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("No Group Selected");
                alert.setHeaderText("Please select a group");
                alert.setContentText("Select a group from the list to manage its settings.");
                alert.showAndWait();
                return;
            }

            // Check if user is admin of this group
            User currentUser = ensureLoggedIn();
            if (currentUser == null) return;

            if (!ctx.getGroupManager().isAdmin(selectedGroup, currentUser)) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Insufficient Permissions");
                alert.setHeaderText("Admin Access Required");
                alert.setContentText("Only group admins can modify group settings.");
                alert.showAndWait();
                return;
            }

            // Load FXML
            FXMLLoader loader = new FXMLLoader(View.GroupSettings.url());
            loader.setControllerFactory(cls -> {
                try {
                    return cls.getDeclaredConstructor(AppContext.class, Navigation.class)
                            .newInstance(ctx, nav);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });

            Parent root = loader.load();
            GroupSettingsController controller = loader.getController();

            // Set the group BEFORE opening the dialog
            controller.setGroup(selectedGroup);

            // Open modal
            Stage dialog = new Stage();
            dialog.setTitle("Group Settings - " + selectedGroup.getName());
            dialog.initOwner(groupTabs.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setResizable(false);
            dialog.setScene(new Scene(root));

            controller.setStage(dialog);
            dialog.showAndWait();

            // Refresh the group list after closing dialog (in case settings were changed)
            refreshGroupList();

            // Also reload the current tab to reflect any changes
            Tab selectedTab = groupTabs.getSelectionModel().getSelectedItem();
            if (selectedTab != null) {
                loadTabContent(selectedTab.getText(), selectedGroup);
            }

        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * <hr>
     * Handles group leaving functionality.
     *
     * <p>Initiates the process for the current user to leave the selected
     * group. Currently displays a placeholder implementation.
     *
     * @param event the action event triggered by the leave group button
     */
    @FXML
    public void onLeaveGroup(ActionEvent event) {

    }

    /**
     * <hr>
     * Handles group deletion functionality.
     *
     * <p>Initiates the process for deleting the selected group (admin only).
     * Currently displays a placeholder implementation.
     *
     * @param event the action event triggered by the delete group button
     */
    @FXML
    public void onDeleteGroup(ActionEvent event) {

    }

    /**
     * <hr>
     * Updates the members table with current group members and their roles.
     *
     * <p>Retrieves group membership data from the database and populates
     * the members table with user information, roles, and availability status.
     * Shows pending join requests for group administrators.
     *
     * @param group the group to update member information for
     */
    private void updateMembersTable(Group group) {
        if (membersTable == null || group == null) return;

        List<GroupMember> membersList = new ArrayList<>();

        // Get group members from database
        if (ctx.getGroupDAO() instanceof GroupDAO) {
            GroupDAO groupDAO = (GroupDAO) ctx.getGroupDAO();
            List<GroupMemberEntity> dbMembers = groupDAO.getGroupMembers(group.getID());

            for (GroupMemberEntity dbMember : dbMembers) {
                String role = dbMember.getRole();
                role = role.substring(0, 1).toUpperCase() + role.substring(1).toLowerCase();
                String availability = getUserAvailabilityStatus(dbMember.getUser());

                GroupMember displayMember = new GroupMember(
                        dbMember.getUser().getFirstName() + " " + dbMember.getUser().getLastName(),
                        role,
                        availability,
                        dbMember.getUserId()
                );
                membersList.add(displayMember);
            }
        }

        membersTable.setItems(FXCollections.observableArrayList(membersList));

        // Add admin functionality
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (ctx.getGroupDAO() instanceof GroupDAO) {
            GroupDAO groupDAO = (GroupDAO) ctx.getGroupDAO();
            if (groupDAO.isAdmin(group.getID(), currentUser.getUserId()) ||
                    group.getOwner().equals(currentUser.getUsername())) {
                showJoinRequestsIfAny(group);
            }
        }
    }

    /**
     * <hr>
     * Shows notification for pending join requests if any exist.
     *
     * <p>Displays an alert to group administrators when there are pending
     * join requests that require approval or rejection.
     *
     * @param group the group to check for pending join requests
     */
    private void showJoinRequestsIfAny(Group group) {
        if (ctx.getGroupDAO() instanceof GroupDAO) {
            GroupDAO groupDAO = (GroupDAO) ctx.getGroupDAO();
            List<GroupJoinRequest> pendingRequests = groupDAO.getPendingJoinRequests(group.getID());

            if (!pendingRequests.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Pending Join Requests");
                alert.setHeaderText(group.getName() + " has " + pendingRequests.size() + " pending join request(s)");
                alert.setContentText("Check the group management interface to approve or reject requests.");
                alert.showAndWait();
            }
        }
    }

    /**
     * <hr>
     * Updates the sessions list for the selected group.
     *
     * <p>Retrieves and displays all study sessions associated with the
     * specified group, including session details and management options.
     *
     * @param group the group to update session information for
     */
    private void updateSessionsList(Group group) {
        if (sessionsListView == null || group == null) return;

        List<Session> groupSessions = ctx.getSessionCalendarManager().getSessionsForGroup(group);

        // Setup cell factory for sessions list
        sessionsListView.setCellFactory(listView -> new ListCell<Session>() {
            @Override
            protected void updateItem(Session session, boolean empty) {
                super.updateItem(session, empty);
                if (empty || session == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox sessionItem = new VBox(5);
                    sessionItem.setPadding(new Insets(8));
                    sessionItem.setStyle("-fx-border-color: #ddd; -fx-border-radius: 5; -fx-background-radius: 5;");

                    Label titleLabel = new Label(session.getTitle());
                    titleLabel.setFont(Font.font("System", FontWeight.BOLD, 12));

                    Label timeLabel = new Label(
                            session.getStartTime().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")) +
                                    " - " + session.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"))
                    );
                    timeLabel.setFont(Font.font("System", 10));

                    HBox buttonBox = new HBox(8);
                    Button viewTasksButton = new Button("View Tasks");
                    viewTasksButton.setOnAction(e -> nav.openSessionTasks(session.getSessionId()));

                    Button addTaskButton = new Button("Add Task");
                    addTaskButton.setOnAction(e -> showAddTaskDialog(session));

                    buttonBox.getChildren().addAll(viewTasksButton, addTaskButton);
                    sessionItem.getChildren().addAll(titleLabel, timeLabel, buttonBox);

                    setGraphic(sessionItem);
                    setText(null);
                }
            }
        });

        sessionsListView.setItems(FXCollections.observableArrayList(groupSessions));
    }

    /**
     * <hr>
     * Shows dialog for adding tasks to study sessions.
     *
     * <p>Displays a modal dialog for creating new tasks associated with
     * specific study sessions, including task details and assignment options.
     *
     * @param session the session to add the task to
     */
    private void showAddTaskDialog(Session session) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add Task");
        dialog.setHeaderText("Add task to session: " + session.getTitle());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Task title");
        TextField deadlineField = new TextField();
        deadlineField.setPromptText("dd-MM-yyyy");

        // Get session participants for assignee dropdown
        ComboBox<User> assigneeBox = new ComboBox<>();
        List<User> participants = session.getParticipants();
        if (participants != null && !participants.isEmpty()) {
            assigneeBox.getItems().addAll(participants);
        }
        assigneeBox.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                setText(empty || user == null ? null : user.getUsername());
            }
        });
        assigneeBox.setButtonCell(assigneeBox.getCellFactory().call(null));

        grid.add(new Label("Task Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Deadline (dd-MM-yyyy):"), 0, 1);
        grid.add(deadlineField, 1, 1);
        grid.add(new Label("Assign to:"), 0, 2);
        grid.add(assigneeBox, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType createButton = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButton, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == createButton) {
                String title = titleField.getText().trim();
                String deadlineStr = deadlineField.getText().trim();
                User assignee = assigneeBox.getValue();

                if (!title.isEmpty() && !deadlineStr.isEmpty() && assignee != null) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                        LocalDateTime deadline = LocalDate.parse(deadlineStr, formatter).atStartOfDay();

                        User currentUser = ctx.getUserSession().getCurrentUser();
                        String createdBy = currentUser != null ? currentUser.getUserId() : assignee.getUserId();

                        ctx.getSessionTaskManager().createTask(
                                session.getSessionId(),
                                title,
                                deadline,
                                assignee.getUserId(),
                                createdBy
                        );

                        new Alert(Alert.AlertType.INFORMATION, "Task created successfully!").showAndWait();
                    } catch (Exception ex) {
                        new Alert(Alert.AlertType.ERROR, "Failed to create task: " + ex.getMessage()).showAndWait();
                    }
                }
            }
        });
    }

    /**
     * <hr>
     * Gets user's current availability status with detailed information.
     *
     * <p>Checks the user's availability schedule and returns a descriptive
     * status indicating current availability, upcoming free time, or
     * unavailability based on their scheduled availability blocks.
     *
     * @param user the user to check availability for
     * @return a string describing the user's current availability status
     */
    private String getUserAvailabilityStatus(User user) {
        if (user == null) return "Unknown";

        try {
            LocalDateTime now = LocalDateTime.now();
            List<Availability> userAvailabilities = ctx.getAvailabilityManager().getAvailabilitiesForUser(user);

            // Check if user has any availability blocks covering the current time
            for (Availability availability : userAvailabilities) {
                if (availability.getStartTime().isBefore(now) && availability.getEndTime().isAfter(now)) {
                    // Show until when they're available
                    String endTime = availability.getEndTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    return "Available until " + endTime;
                }
            }

            // Check for upcoming availability today
            LocalDate today = now.toLocalDate();
            for (Availability availability : userAvailabilities) {
                if (availability.getStartTime().toLocalDate().equals(today) &&
                        availability.getStartTime().isAfter(now)) {
                    String startTime = availability.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
                    return "Free from " + startTime;
                }
            }

            // If no current or upcoming availability found for today
            return "Unavailable";

        } catch (Exception e) {
            System.err.println("Error checking availability for user " + user.getUsername() + ": " + e.getMessage());
            return "Unknown";
        }
    }
}