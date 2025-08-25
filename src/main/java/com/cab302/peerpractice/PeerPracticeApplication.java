package com.cab302.peerpractice;

import javafx.application.Application;
import javafx.stage.Stage;

public class PeerPracticeApplication extends Application {
    @Override
    public void start(Stage stage) {
        Navigation navigate = new Navigation(stage);
        stage.setUserData(navigate);
        navigate.Display(View.Login);
        stage.show();
    }
}
