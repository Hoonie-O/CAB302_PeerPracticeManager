package com.cab302.peerpractice.Controllers;

import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ShareGroupIDController extends BaseController {
    @FXML public Label titleLabel;
    @FXML public TextField groupIDField;
    @FXML public Button copyButton;


    public ShareGroupIDController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    private void initialize() {

    }

    @FXML
    private void onCopyGroupID() {

    }

    public void setStage(Stage dialog) {

    }
}
