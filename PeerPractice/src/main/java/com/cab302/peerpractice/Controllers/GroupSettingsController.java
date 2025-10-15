package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Model.Entities.Group;
import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Controller for managing group settings.
 * Allows admins to edit group name, description, visibility, and delete the group.
 */
public class GroupSettingsController extends BaseController {

    @FXML private TextField groupNameField;
    @FXML private TextArea groupDescriptionArea;
    @FXML private RadioButton publicRadio;
    @FXML private RadioButton privateRadio;
    @FXML private Button deleteGroupButton;
    @FXML private Button cancelButton;
    @FXML private Button saveButton;
    @FXML private Label nameErrorLabel;

    private Group currentGroup;
    private Stage dialogStage;
    private boolean changesMade = false;

    public GroupSettingsController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    /**
     * Sets the group to manage settings for
     */
    public void setGroup(Group group) {
        this.currentGroup = group;
        loadGroupData();
    }

    /**
     * Sets the dialog stage
     */
    public void setStage(Stage stage) {
        this.dialogStage = stage;
    }

    @FXML
    public void initialize() {
        // Setup radio button toggle group
        ToggleGroup visibilityToggle = new ToggleGroup();
        publicRadio.setToggleGroup(visibilityToggle);
        privateRadio.setToggleGroup(visibilityToggle);

        // Add listeners to detect changes
        groupNameField.textProperty().addListener((obs, oldVal, newVal) -> validateName());
        groupDescriptionArea.textProperty().addListener((obs, oldVal, newVal) -> changesMade = true);
        visibilityToggle.selectedToggleProperty().addListener((obs, oldVal, newVal) -> changesMade = true);
    }

    /**
     * Loads group data into the form
     */
    private void loadGroupData() {
        if (currentGroup == null) return;

        groupNameField.setText(currentGroup.getName());
        groupDescriptionArea.setText(currentGroup.getDescription());

        // Set visibility radio
        if (currentGroup.isRequire_approval()) {
            privateRadio.setSelected(true);
        } else {
            publicRadio.setSelected(true);
        }

        // Only owner can delete group
        User currentUser = ctx.getUserSession().getCurrentUser();
        if (currentUser == null || !currentGroup.getOwner().equals(currentUser.getUsername())) {
            deleteGroupButton.setVisible(false);
            deleteGroupButton.setManaged(false);
        }

        changesMade = false;
    }

    /**
     * Validates the group name
     */
    private void validateName() {
        String name = groupNameField.getText().trim();

        if (name.isEmpty()) {
            nameErrorLabel.setText("Group name cannot be empty");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            saveButton.setDisable(true);
            return;
        }

        if (name.length() < 3) {
            nameErrorLabel.setText("Group name must be at least 3 characters");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            saveButton.setDisable(true);
            return;
        }

        if (name.length() > 50) {
            nameErrorLabel.setText("Group name must be less than 50 characters");
            nameErrorLabel.setVisible(true);
            nameErrorLabel.setManaged(true);
            saveButton.setDisable(true);
            return;
        }

        // Valid name
        nameErrorLabel.setVisible(false);
        nameErrorLabel.setManaged(false);
        saveButton.setDisable(false);
        changesMade = true;
    }

    /**
     * Saves the group settings
     */
    @FXML
    private void onSave() {
        try {
            User currentUser = ensureLoggedIn();
            if (currentUser == null) return;

            // Check admin permissions
            if (!ctx.getGroupManager().isAdmin(currentGroup, currentUser)) {
                showAlert(Alert.AlertType.ERROR, "Permission Denied",
                    "Only admins can modify group settings", "Error", null);
                return;
            }

            // Validate inputs
            String newName = groupNameField.getText().trim();
            if (newName.isEmpty() || newName.length() < 3) {
                showAlert(Alert.AlertType.WARNING, "Invalid Input",
                    "Please enter a valid group name (at least 3 characters)", "Warning", null);
                return;
            }

            // Update group object
            currentGroup.setName(newName);
            currentGroup.setDescription(groupDescriptionArea.getText().trim());
            currentGroup.setRequire_approval(privateRadio.isSelected());

            // Save to database
            boolean success = ctx.getGroupDAO().updateGroup(currentGroup);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Success",
                    "Group settings updated successfully", "Success", null);
                changesMade = false;
                closeDialog();
            } else {
                showAlert(Alert.AlertType.ERROR, "Error",
                    "Failed to update group settings", "Error", null);
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "An error occurred: " + e.getMessage(), "Error", null);
        }
    }

    /**
     * Cancels and closes the dialog
     */
    @FXML
    private void onCancel() {
        if (changesMade) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Unsaved Changes");
            confirm.setHeaderText("You have unsaved changes");
            confirm.setContentText("Are you sure you want to discard your changes?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                closeDialog();
            }
        } else {
            closeDialog();
        }
    }

    /**
     * Handles group deletion
     */
    @FXML
    private void onDeleteGroup() {
        try {
            User currentUser = ensureLoggedIn();
            if (currentUser == null) return;

            // Only owner can delete
            if (!currentGroup.getOwner().equals(currentUser.getUsername())) {
                showAlert(Alert.AlertType.ERROR, "Permission Denied",
                    "Only the group owner can delete the group", "Error", null);
                return;
            }

            // Confirmation dialog
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Delete Group");
            confirm.setHeaderText("Are you sure you want to delete \"" + currentGroup.getName() + "\"?");
            confirm.setContentText("This action cannot be undone. All group data, sessions, and members will be removed.");

            ButtonType deleteButton = new ButtonType("Delete Group", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirm.getButtonTypes().setAll(deleteButton, cancelButton);

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == deleteButton) {
                // Delete the group
                ctx.getGroupManager().deleteGroup(currentGroup, currentUser);

                showAlert(Alert.AlertType.INFORMATION, "Group Deleted",
                    "The group has been deleted successfully", "Success", null);

                closeDialog();
            }

        } catch (Exception e) {
            showAlert(Alert.AlertType.ERROR, "Error",
                "Failed to delete group: " + e.getMessage(), "Error", null);
        }
    }

    /**
     * Closes the dialog
     */
    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close();
        }
    }

    /**
     * Returns whether changes were made
     */
    public boolean hasChanges() {
        return changesMade;
    }
}
