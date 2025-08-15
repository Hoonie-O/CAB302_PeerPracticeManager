package com.example.addressbook;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Button;
import javafx.scene.control.*;


public class HelloController {
    @FXML
    private Label welcomeText;
    @FXML
    private TextArea termsAndConditions;
    @FXML
    private CheckBox agreeCheckBox;
    @FXML
    private Button nextButton;

    @FXML
    public void initialize() {
        termsAndConditions.setText("""
                Lorem ipsum dolor sit amet, consectetur adipiscing elit,
                sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
                Eget dolor morbi non arcu risus. Quis lectus nulla at volutpat diam
                ut venenatis tellus in. Feugiat in fermentum posuere urna nec tincidunt
                praesent semper. Turpis tincidunt id aliquet risus feugiat in.
                Libero volutpat sed cras ornare. Facilisi morbi tempus iaculis urna.
                Bibendum est ultricies integer quis auctor. Eu augue ut lectus arcu.
                Tincidunt tortor aliquam nulla facilisi cras fermentum odio eu.
                Gravida neque convallis a cras. Elit ut aliquam purus sit.
                Suspendisse ultrices gravida dictum fusce ut placerat.
                Integer feugiat scelerisque varius morbi enim nunc.
                Amet justo donec enim diam vulputate ut pharetra.
                Sapien pellentesque habitant morbi tristique.
                Lorem sed risus ultricies tristique nulla aliquet.
                Elementum nibh tellus molestie nunc non blandit massa. """);
    }
    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to Address Book Application!");
    }
    @FXML
    protected void onAgreeCheckBoxClick() {
        boolean accepted = agreeCheckBox.isSelected();
        nextButton.setDisable(!accepted);
    }
}