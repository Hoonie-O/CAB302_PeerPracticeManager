package com.cab302.peerpractice.Controllers;


import com.cab302.peerpractice.AppContext;
import com.cab302.peerpractice.Navigation;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class InviteMemberController extends BaseController {
    @FXML private TextField inviteeField;
    @FXML private ListView inviteesList;


    public InviteMemberController(AppContext ctx, Navigation nav) {
        super(ctx, nav);
    }

    @FXML
    private void initialize() {

    }

    @FXML private void onAdd() {

    }

    @FXML private void onInvite() {

    }


    public void setStage(Stage dialog) {
    }
}
