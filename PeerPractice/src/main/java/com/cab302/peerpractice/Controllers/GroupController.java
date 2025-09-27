package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.daos.GroupDAO;
import com.cab302.peerpractice.Model.entities.*;
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

public class GroupController extends SidebarController {
    @FXML private TabPane groupTabs;
    @FXML private ListView<Group> groupListView;
    @FXML private Label groupNameLabel;
    @FXML private Button addGroupButton;
    @FXML private Button sortGroupsButton;
  
    @FXML private TableView<GroupMember> membersTable;
    @FXML private ListView<Session> sessionsListView;
    private GroupCalendarController groupCalendarController;
    private NotesController notesController;


    private boolean sortAlphabetical = false;

    public GroupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

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

    private void loadChatContent(Tab tab, Group group) {
        VBox chatContent = new VBox(10);
        chatContent.setPadding(new Insets(20));

        Label comingSoon = new Label("Chat functionality coming soon!");
        comingSoon.setFont(Font.font("System", FontWeight.BOLD, 14));

        chatContent.getChildren().add(comingSoon);
        tab.setContent(chatContent);
    }

    private void loadFilesContent(Tab tab, Group group) {
        VBox filesContent = new VBox(10);
        filesContent.setPadding(new Insets(20));

        Label comingSoon = new Label("File sharing functionality coming soon!");
        comingSoon.setFont(Font.font("System", FontWeight.BOLD, 14));

        filesContent.getChildren().add(comingSoon);
        tab.setContent(filesContent);
    }

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

    @FXML
    public void onManageGroup(ActionEvent event) {
        try {
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

            // Open modal
            Stage dialog = new Stage();
            dialog.setTitle("Manage Group");
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

    @FXML
    public void onLeaveGroup(ActionEvent event) {

    }

    @FXML
    public void onDeleteGroup(ActionEvent event) {

    }

    // Update members table with group members and their roles
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

    // Update sessions list for selected group
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

    // Show dialog to add task to session
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

    // Get user's current availability status with detailed information
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
