package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Navigation;
import com.cab302.peerpractice.View;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

/**
 * <hr>
 * Controller for the default/main dashboard functionality.
 *
 * <p>This controller handles the primary user interface for group management
 * operations including creating new study groups and joining existing groups.
 * It serves as the main entry point for group-related activities.
 *
 * <p>Key features include:
 * <ul>
 *   <li>Study group creation with customizable settings</li>
 *   <li>Group joining via unique group codes</li>
 *   <li>Dialog-based group management interfaces</li>
 *   <li>Integration with GroupManager for data operations</li>
 * </ul>
 *
 * @see Group
 * @see SidebarController
 * @see View
 */
public class DefaultController extends SidebarController {
    /** <hr> Vertical box container for the default menu options. */
    @FXML public VBox defaultMenu;
    /** <hr> Button for initiating group creation process. */
    @FXML public Button createGroupButton;
    /** <hr> Button for initiating group joining process. */
    @FXML  public Button joinGroupButton;

    /**
     * <hr>
     * Constructs a new DefaultController with the specified context and navigation.
     *
     * @param ctx the application context providing access to user session and managers
     * @param nav the navigation controller for screen transitions
     */
    public DefaultController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * <hr>
     * Handles the group creation process.
     *
     * <p>Displays a dialog for users to input group details including name,
     * description, and approval requirements. Validates input and creates
     * a new study group upon successful completion.
     *
     * <p>Upon successful group creation, navigates to the groups view and
     * displays a confirmation message. Shows error alerts for any failures
     * during the creation process.
     */
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

    /**
     * <hr>
     * Handles the group joining process.
     *
     * <p>Displays a dialog for users to input group codes and attempts to
     * join matching groups. Supports repeated attempts if invalid codes
     * are provided or groups are not found.
     *
     * <p>Process includes group lookup by code, validation checks, and
     * group membership requests. Navigates to groups view upon successful
     * join and displays appropriate status messages.
     */
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
                Group group = ctx.getGroupDAO().getAllGroups().stream()
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
