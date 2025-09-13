package com.cab302.peerpractice;

import com.cab302.peerpractice.Exceptions.ControllerFactoryFailedException;
import com.cab302.peerpractice.Model.UserSession;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.net.URL;
import java.util.Objects;

public final class Navigation {
    private static final String path = "/com/cab302/peerpractice/";
    private final Stage stage;
    private final AppContext ctx;

    Navigation(AppContext ctx, Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
        this.ctx = ctx;
    }

    public void Display(View view) {
        try {
            URL url = getClass().getResource(path + view.fxml());
            if (url == null) {
                throw new IllegalStateException("Missing FXML: " + view.fxml());
            }

            FXMLLoader fx = new FXMLLoader(url);
            /* Controller Factory:
            *  Performs constructor injection on all controllers,
            *  injecting the app context and this navigation class for easier access
            * type: the controller itself being injected (each controller will be)
            */
            fx.setControllerFactory(type -> {
                try{
                    return type.getDeclaredConstructor(AppContext.class, Navigation.class).newInstance(ctx,this);
                }catch(Exception e){
                    throw new ControllerFactoryFailedException("Controller " + type.getName() +
                        " failed, constructor must be declared as (AppContext ctx, Navigation nav) and be declared as public");
                }
            });


            Parent root = fx.load();
            Scene scene = new Scene(root);
            stage.setTitle(view.title());
            stage.setScene(scene);
            stage.sizeToScene();

            root.setFocusTraversable(true);
            Platform.runLater(() -> Platform.runLater(root::requestFocus));
        }

        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException("Failed to load: " + view, e);
        }
    }
}
