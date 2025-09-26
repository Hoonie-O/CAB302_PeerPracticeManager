package com.cab302.peerpractice;

import javafx.application.Application;
import javafx.stage.Stage;

public class PeerPracticeApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            AppContext ctx = new AppContext();
            Navigation navigate = new Navigation(ctx,stage);
            stage.setUserData(navigate);
            navigate.Display(View.Login);
            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
