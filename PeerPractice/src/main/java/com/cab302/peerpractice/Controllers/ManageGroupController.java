package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.*;
import com.cab302.peerpractice.Navigation;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

/**
 * Controller for comprehensive group management and administration functionality.
 * Handles member role management, join request processing, and admin operations.
 */
public class ManageGroupController extends BaseController {
    @FXML private Button saveButton;
    @FXML private TextField blockUserField;

    // Members table
    @FXML private TableView<MemberDisplay> membersTable;
    @FXML private TableColumn<MemberDisplay, String> nameMemberColumn;
    @FXML private TableColumn<MemberDisplay, String> roleColumn;
    @FXML private TableColumn<MemberDisplay, Void> removeColumn;

    // Member requests table
    @FXML private TableView<JoinRequestDisplay> memberRequestsTable;
    @FXML private TableColumn<JoinRequestDisplay, String> nameRequestColumn;
    @FXML private TableColumn<JoinRequestDisplay, Void> approveColumn;
    @FXML private TableColumn<JoinRequestDisplay, Void> declineColumn;

    // Blocked users table (will be removed)
    @FXML private TableView<Object> blockedUsersTable;
    @FXML private TableColumn<Object, String> nameBlockColumn;
    @FXML private TableColumn<Object, Void> unblockColumn;

    private Group currentGroup;
    private Stage dialogStage;

    public ManageGroupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * Sets the group to manage and initializes the UI
     */
    public void setGroup(Group group) {
        this.currentGroup = group;
        // Initialize UI after group is set
        Platform.runLater(() -> {
            setupMembersTable();
            setupMemberRequestsTable();
            setupBlockedUsersTable();
            loadData();
        });
    }

    public void initialize() {
        // Empty - actual initialization happens in setGroup after the group is set
    }

    /**
     * Loads all data for the current group
     */
    private void loadData() {
        try {
            // Reload group from database to get fresh data
            currentGroup = ctx.getGroupDAO().searchByID(currentGroup.getID());
            if (currentGroup == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Group not found", "Error", null);
                closeDialog();
                return;
            }

            loadMembers();
            loadJoinRequests();
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Data",
                e.getMessage(), "Error", null);
        }
    }

    /**
     * Loads group members into the table
     */
    private void loadMembers() {
        try {
            List<GroupMemberEntity> members = ctx.getGroupDAO().getGroupMembers(currentGroup.getID());
            ObservableList<MemberDisplay> memberDisplays = FXCollections.observableArrayList();

            for (GroupMemberEntity member : members) {
                if (member.getUser() != null) {
                    memberDisplays.add(new MemberDisplay(
                        member.getUser(),
                        member.getRole(),
                        currentGroup.getOwner().equals(member.getUser().getUsername())
                    ));
                }
            }

            membersTable.setItems(memberDisplays);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Members",
                e.getMessage(), "Error", null);
        }
    }

    /**
     * Loads pending join requests
     */
    private void loadJoinRequests() {
        try {
            List<GroupJoinRequest> requests = ctx.getGroupDAO().getPendingJoinRequests(currentGroup.getID());
            ObservableList<JoinRequestDisplay> requestDisplays = FXCollections.observableArrayList();

            for (GroupJoinRequest request : requests) {
                if (request.getUser() != null) {
                    requestDisplays.add(new JoinRequestDisplay(request));
                }
            }

            memberRequestsTable.setItems(requestDisplays);
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error Loading Requests",
                e.getMessage(), "Error", null);
        }
    }

    /**
     * Sets up the members table
     */
    private void setupMembersTable() {
        nameMemberColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Role column with ComboBox for admins to change roles
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        roleColumn.setCellFactory(column -> new TableCell<MemberDisplay, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();
            {
                comboBox.getItems().addAll("Admin", "Member");
                comboBox.setMaxWidth(Double.MAX_VALUE);
                comboBox.setOnAction(event -> {
                    MemberDisplay member = getTableRow().getItem();
                    if (member != null && !member.isOwner()) {
                        String newRole = comboBox.getValue();
                        handleRoleChange(member, newRole);
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                MemberDisplay member = getTableRow() != null ? getTableRow().getItem() : null;

                if (empty || member == null) {
                    setGraphic(null);
                } else {
                    // Owner role can't be changed
                    if (member.isOwner()) {
                        setText("Owner");
                        setGraphic(null);
                    } else {
                        comboBox.setValue(item);
                        setGraphic(comboBox);
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });

        // Remove column with buttons
        removeColumn.setCellFactory(column -> new TableCell<MemberDisplay, Void>() {
            private final Button removeBtn = new Button("Kick");
            {
                removeBtn.setMaxWidth(Double.MAX_VALUE);
                removeBtn.setOnAction(event -> {
                    MemberDisplay member = getTableView().getItems().get(getIndex());
                    handleKickMember(member);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                MemberDisplay member = getTableRow() != null ? getTableRow().getItem() : null;

                if (empty || member == null || member.isOwner()) {
                    setGraphic(null); // Can't kick owner
                } else {
                    setGraphic(removeBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    /**
     * Sets up the join requests table
     */
    private void setupMemberRequestsTable() {
        nameRequestColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        // Approve button
        approveColumn.setCellFactory(column -> new TableCell<JoinRequestDisplay, Void>() {
            private final Button approveBtn = new Button("Approve");
            {
                approveBtn.setMaxWidth(Double.MAX_VALUE);
                approveBtn.setOnAction(event -> {
                    JoinRequestDisplay request = getTableView().getItems().get(getIndex());
                    handleApproveRequest(request, true);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(approveBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // Decline button
        declineColumn.setCellFactory(column -> new TableCell<JoinRequestDisplay, Void>() {
            private final Button declineBtn = new Button("Decline");
            {
                declineBtn.setMaxWidth(Double.MAX_VALUE);
                declineBtn.setOnAction(event -> {
                    JoinRequestDisplay request = getTableView().getItems().get(getIndex());
                    handleApproveRequest(request, false);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(declineBtn);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    /**
     * Sets up blocked users table (placeholder - functionality will be removed)
     */
    private void setupBlockedUsersTable() {
        // Blocking functionality not implemented in backend - leave table empty
        blockedUsersTable.setItems(FXCollections.observableArrayList());
    }

    /**
     * Handles role changes for members
     */
    private void handleRoleChange(MemberDisplay memberDisplay, String newRole) {
        try {
            User currentUser = ensureLoggedIn();
            if (currentUser == null) return;

            User targetUser = memberDisplay.user();
            String currentRole = memberDisplay.role();

            // No change needed
            if (currentRole.equalsIgnoreCase(newRole)) return;

            if (newRole.equalsIgnoreCase("Admin")) {
                ctx.getGroupManager().promoteToAdmin(currentGroup, currentUser, targetUser);
                showAlert(Alert.AlertType.INFORMATION, "Success",
                    targetUser.getUsername() + " promoted to admin", "Success", null);
            } else {
                ctx.getGroupManager().demoteAdmin(currentGroup, currentUser, targetUser);
                showAlert(Alert.AlertType.INFORMATION, "Success",
                    targetUser.getUsername() + " demoted to member", "Success", null);
            }

            loadData(); // Refresh
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage(), "Error", null);
            loadData(); // Refresh to revert UI
        }
    }

    /**
     * Handles kicking a member
     */
    private void handleKickMember(MemberDisplay memberDisplay) {
        // Confirmation dialog
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirm Kick");
        confirm.setHeaderText("Kick " + memberDisplay.getName() + "?");
        confirm.setContentText("This will remove them from the group. They can rejoin if the group is public or request to join again if private.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                User currentUser = ensureLoggedIn();
                if (currentUser == null) return;

                ctx.getGroupManager().kickMember(currentGroup, currentUser, memberDisplay.user());
                showAlert(Alert.AlertType.INFORMATION, "Success",
                    memberDisplay.getName() + " has been removed from the group", "Success", null);
                loadData(); // Refresh
            } catch (Exception e) {
                showAlert(Alert.AlertType.ERROR, "Error", e.getMessage(), "Error", null);
            }
        }
    }

    /**
     * Handles approving/declining join requests
     */
    private void handleApproveRequest(JoinRequestDisplay requestDisplay, boolean approve) {
        try {
            User currentUser = ensureLoggedIn();
            if (currentUser == null) return;

            ctx.getGroupManager().processJoinRequest(
                currentGroup, currentUser, requestDisplay.request().getRequestId(), approve
            );

            String action = approve ? "approved" : "declined";
            showAlert(Alert.AlertType.INFORMATION, "Success",
                requestDisplay.getName() + "'s request has been " + action, "Success", null);
            loadData(); // Refresh
        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error", e.getMessage(), "Error", null);
        }
    }

    @FXML
    private void onSave() {
        closeDialog();
    }

    @FXML
    private void onBlockUser() {
        // Blocking not implemented - show message
        showAlert(Alert.AlertType.INFORMATION, "Not Available",
            "User blocking functionality is coming soon", "Info", null);
    }

    public void setStage(Stage dialog) {
        this.dialogStage = dialog;
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    // ========== Display Classes ==========

    /**
         * Display wrapper for group members
         */
        public record MemberDisplay(User user, String role, boolean isOwner) {

        public String getName() {
                return user.getUsername();
            }

            @Override
            public String role() {
                return role.substring(0, 1).toUpperCase() + role.substring(1);
            }
        }

    /**
         * Display wrapper for join requests
         */
        public record JoinRequestDisplay(GroupJoinRequest request) {

        public String getName() {
                return request.getUser() != null ? request.getUser().getUsername() : "Unknown";
            }
        }
}
