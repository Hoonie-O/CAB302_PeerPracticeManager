package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class EditProfileController extends BaseController {
    private Stage stage;
    @FXML private StackPane root;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private TextField usernameField;
    @FXML private TextField phoneField;
    @FXML private TextField addressField;
    @FXML private TextField instituteField;
    @FXML private DatePicker dateOfBirthField;

    public EditProfileController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML private void onClose() {
        if (stage != null)
            stage.close();
    }

    @FXML private void onSave() {
        // TODO: validate & persist changes
        onClose();
    }

}