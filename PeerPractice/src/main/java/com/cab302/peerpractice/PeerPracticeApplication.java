package com.cab302.peerpractice;

import com.cab302.peerpractice.Exceptions.ControllerFactoryFailedException;
import com.cab302.peerpractice.Model.IUserDAO;
import com.cab302.peerpractice.Model.UserSession;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class PeerPracticeApplication extends Application {
    @Override
    public void start(Stage stage) {
        AppContext ctx = new AppContext();
        Navigation navigate = new Navigation(ctx,stage);
        stage.setUserData(navigate);
        navigate.Display(View.Login);
        stage.show();
    }
}
