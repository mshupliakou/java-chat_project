package com.example.javachat_project;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.File;

public class FileSendingController {
    @FXML private ImageView image;
    @FXML private Button sendButton;

    private File file;
    private boolean sendConfirmed = false;

    public void setFile(File new_file){
        file=new_file;
    }

    public void setImageForSending(Image new_image){
        image.setImage(new_image);
    }

    @FXML
    private void handleSend(MouseEvent mouseEvent) {
        sendConfirmed = true;
        ((Stage) sendButton.getScene().getWindow()).close();
    }

    public boolean isSendConfirmed() {
        return sendConfirmed;
    }

    public void handelCancel(MouseEvent mouseEvent) {
        ((Stage) sendButton.getScene().getWindow()).close();
    }
}
