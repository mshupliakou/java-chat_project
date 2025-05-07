package com.example.javachat_project;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Objects;

public class MainLogin extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        stage.setResizable(false);
        stage.setTitle("Echo");
        stage.getIcons().add(new javafx.scene.image.Image(
                Objects.requireNonNull(MainLogin.class.getResourceAsStream("/img/mini_logo.png"))
        ));
        FXMLLoader fxmlLoader = new FXMLLoader(MainLogin.class.getResource("/fxml/login.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 900, 600);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}