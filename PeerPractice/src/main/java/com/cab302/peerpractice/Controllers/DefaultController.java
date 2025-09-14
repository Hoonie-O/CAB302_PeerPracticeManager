package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Group;
import com.cab302.peerpractice.Model.User;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

public class DefaultController extends SidebarController {

    @FXML public VBox defaultMenu;
    @FXML public Button createGroupButton;
    @FXML  public Button joinGroupButton;

    public DefaultController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
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
                new Label("Description:"), descriptionField, approvalCheck);
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
                    new Alert(Alert.AlertType.INFORMATION, "Group created successfully!").showAndWait();
                    nav.Display(View.Groups);
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Error creating group: " + e.getMessage()).showAndWait();
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
            if (result.isEmpty()) break;

            String code = result.get();
            try {
                Group group = ctx.getGroupDao().getAllGroups().stream()
                        .filter(g -> String.valueOf(g.getID()).equals(code))
                        .findFirst()
                        .orElse(null);

                if (group == null) {
                    new Alert(Alert.AlertType.ERROR, "No group found with that code.").showAndWait();
                    continue;
                }

                User currentUser = ctx.getUserSession().getCurrentUser();
                ctx.getGroupManager().joinGroup(group, currentUser);

                new Alert(Alert.AlertType.INFORMATION, "Request to join sent / group joined successfully!").showAndWait();
                joined = true;
                nav.Display(View.Groups);
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error joining group: " + e.getMessage()).showAndWait();
            }
        }
    }
}
