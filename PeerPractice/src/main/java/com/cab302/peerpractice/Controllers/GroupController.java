package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Group;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Navigation;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;
 

public class GroupController extends SidebarController {
    @FXML private TabPane groupTabs;
    @FXML private Label tabContentLabel;
    @FXML private ListView<Group> groupListView;
    @FXML private Label groupNameLabel;
    @FXML private Button addGroupButton;
    @FXML private Button sortGroupsButton;
    

    private boolean sortAlphabetical = false;

    public GroupController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    public void initialize() {
        super.initialize();

        if (groupTabs != null && tabContentLabel != null) {
            groupTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
                if (newTab != null) {
                    tabContentLabel.setText(newTab.getText().toUpperCase());
                }
            });
        }

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
            }
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

                    Group newGroup = ctx.getGroupDao()
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
                Group group = ctx.getGroupDao().getAllGroups().stream()
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
        List<Group> userGroups = ctx.getGroupDao().searchByUser(currentUser);

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
}
