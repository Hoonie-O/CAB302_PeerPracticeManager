package com.cab302.peerpractice;

import com.cab302.peerpractice.Model.Entities.User;
import com.cab302.peerpractice.Model.Managers.SessionPersistence;
import javafx.application.Application;
import javafx.stage.Stage;


public class PeerPracticeApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            AppContext ctx = new AppContext();
            Navigation navigate = new Navigation(ctx,stage);
            stage.setUserData(navigate);

            User savedUser =
                SessionPersistence.loadSavedSession(ctx.getUserDAO());

            if (savedUser != null) {
                ctx.getUserSession().setCurrentUser(savedUser);
                navigate.DisplayMainMenuOrGroup();
            } else {
                navigate.Display(View.Login);
            }

            stage.show();
        } catch (Exception e) {
            System.err.println("Failed to start application: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
