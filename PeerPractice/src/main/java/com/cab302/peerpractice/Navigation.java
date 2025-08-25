package com.cab302.peerpractice;

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

    Navigation(Stage stage) {
        this.stage = Objects.requireNonNull(stage, "stage");
    }

    public void Display(View view) {
        try {
            URL url = getClass().getResource(path + view.fxml());
            if (url == null) {
                throw new IllegalStateException("Missing FXML: " + view.fxml());
            }

            Parent root = FXMLLoader.load(url);
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
