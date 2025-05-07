package com.example.javachat_project;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class MainController {

    public void signUp(MouseEvent mouseEvent) {
        Parent root = null;
        try {
            root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/fxml/sign.fxml")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Stage primstage = new Stage();
        primstage.getIcons().add(new javafx.scene.image.Image(
                Objects.requireNonNull(MainLogin.class.getResourceAsStream("/img/mini_logo.png"))
        ));
        primstage.setTitle("Echo | Sign Up");
        assert root != null;
        Scene scene = new Scene(root);
        primstage.setScene(scene);
        primstage.setHeight(570);
        primstage.setWidth(400);
        primstage.setResizable(false);
        primstage.initModality(Modality.APPLICATION_MODAL);
        primstage.show();
    }
}