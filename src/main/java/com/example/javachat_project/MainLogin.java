package com.example.javachat_project;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.io.IOException;
import java.util.Objects;

/**
 * Main entry point for the JavaFX chat login screen.
 */
public class MainLogin extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Prevent window resizing
        stage.setResizable(false);

        // Set window title
        stage.setTitle("Echo");

        // Set application icon (throws if icon not found)
        stage.getIcons().add(new Image(
                Objects.requireNonNull(MainLogin.class.getResourceAsStream("/img/mini_logo.png"))
        ));

        // Load login.fxml layout from resources
        FXMLLoader fxmlLoader = new FXMLLoader(
                Objects.requireNonNull(MainLogin.class.getResource("/fxml/login.fxml"))
        );

        // Create and configure the scene
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.setFill(Color.TRANSPARENT); // Optional: makes background transparent for styled windows

        // Set the scene and show the stage
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(); // Launches the JavaFX application
    }
}
