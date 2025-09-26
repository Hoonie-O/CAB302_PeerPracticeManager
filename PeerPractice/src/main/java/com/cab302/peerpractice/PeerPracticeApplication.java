package com.cab302.peerpractice;

import com.cab302.peerpractice.Exceptions.ControllerFactoryFailedException;
import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Model.UserSession;
import javafx.application.Application;

import javafx.stage.Stage;

public class PeerPracticeApplication extends Application {
    @Override
    public void start(Stage stage) {
        try {
            AppContext ctx = new AppContext();
            Navigation navigate = new Navigation(ctx,stage);
            stage.setUserData(navigate);

            // check for saved session
            com.cab302.peerpractice.Model.User savedUser =
                com.cab302.peerpractice.Model.SessionPersistence.loadSavedSession(ctx.getUserDao());

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
}
