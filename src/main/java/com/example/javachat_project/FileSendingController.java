package com.example.javachat_project;

import javafx.fxml.FXML;

import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.File;

public class FileSendingController {
    @FXML private Button sendButton;
    @FXML private Label file_name_label;
    @FXML private Label file_size_label;

    public void setText_input_file_sending(String text) {
        text_input_file_sending.setText(text);
    }

    @FXML private TextField text_input_file_sending;

    private File file;
    private boolean sendConfirmed = false;

    public static String readableFileSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format("%.1f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }
    public void setFile(File new_file){
        file=new_file;
        if (file != null) {
            file_name_label.setText(file.getName());
            file_size_label.setText( readableFileSize(file.length()));
        } else {
            file_name_label.setText("File is not chosen");
            file_size_label.setText("");
        }

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

    public String getText_input_file_sending() {
        return text_input_file_sending.getText();
    }
}
