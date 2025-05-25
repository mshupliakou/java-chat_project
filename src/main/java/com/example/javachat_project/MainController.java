package com.example.javachat_project;

import com.example.javachat_project.DB.DBConnection;
import com.example.javachat_project.DB.SupabaseConnect;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;

public class MainController {
    @FXML private TextField tfPassword;
    @FXML private TextField tfLogin;
    @FXML private Label loginWrg;
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
        primstage.setHeight(630);
        primstage.setWidth(410);
        primstage.setResizable(false);
        primstage.initModality(Modality.APPLICATION_MODAL);
        primstage.show();
    }
    private Connection remoteConnection;
    public void initialize() {
        remoteConnection = SupabaseConnect.getConnection();
    }
    public boolean verify_logging_data() {
        String login = tfLogin.getText();
        String password = tfPassword.getText();

        if (login.isEmpty() || password.isEmpty()) {
            loginWrg.setText("Please enter login and password!");
            return false;
        }

        String query = "SELECT * FROM personInfo WHERE login = ? AND password = ?";

        try (PreparedStatement pst = remoteConnection.prepareStatement(query)) {
            pst.setString(1, login);
            pst.setString(2, password);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    return true;
                } else {
                    loginWrg.setText("Incorrect login or password!");
                    return false;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            loginWrg.setText("Database error!");
            return false;
        }
    }

    public void logIn(MouseEvent mouseEvent) {
        if(verify_logging_data())
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/logged_in.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

            Scene scene = new Scene(root);

            stage.setScene(scene);
            stage.setTitle("Echo");

            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}