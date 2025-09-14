package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class SettingProfileController extends BaseController {

    @FXML private ComboBox<String> dateFormatBox;
    @FXML private ComboBox<String> timeFormatBox;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;

    private Stage stage;

    public SettingProfileController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    @FXML
    private void initialize() {

    }

    @FXML
    private void onClose() {
        if (stage != null)
            stage.close();
    }

    @FXML
    private void onSave() {
        onClose();
    }
}
