package com.example.javachat_project;

import com.example.javachat_project.DB.SupabaseConnect;
import javafx.event.ActionEvent;
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

    @FXML private TextField tfPassword; // Password input field
    @FXML private TextField tfLogin;    // Login input field
    @FXML private Label loginWrg;       // Label to show login errors

    private User me;                    // Stores the current user upon successful login

    public static LoggedInController getLoggedInController() {
        return loggedInController;
    }

    private static LoggedInController loggedInController;
    private Connection remoteConnection; // Database connection

    // Handles "Sign Up" link/button click and opens the sign-up window
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
        primstage.initModality(Modality.APPLICATION_MODAL); // Modal window
        primstage.show();
    }

    // Initializes the controller (called automatically after FXML is loaded)
    public void initialize() {
        remoteConnection = SupabaseConnect.getConnection(); // Establish database connection

        // When ENTER is pressed in the login field, focus moves to the password field
        tfLogin.setOnAction(event -> {
            tfPassword.requestFocus();
        });
    }

    // Verifies login and password against the database
    public boolean verify_logging_data() {
        String login = tfLogin.getText();
        String password = tfPassword.getText();

        // Show message if login or password field is empty
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
                    // Create user if credentials match
                    long myId = rs.getLong("id");
                    String myName = rs.getString("name");
                    String myLastName = rs.getString("last_name");
                    me = new User(myId, myName, myLastName, password, login);
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

    // General login method for both mouse and keyboard events
    public void logIn(Object eventSource) {
        if (!verify_logging_data()) return;

        try {
            // Load the "logged in" scene
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/logged_in.fxml"));
            Parent root = loader.load();

            loggedInController = loader.getController();
            loggedInController.setCurrentUser(me);

            Stage stage;
            // Determine the type of event source and retrieve the current stage
            if (eventSource instanceof MouseEvent) {
                stage = (Stage) ((Node) ((MouseEvent) eventSource).getSource()).getScene().getWindow();
            } else if (eventSource instanceof ActionEvent) {
                stage = (Stage) ((Node) ((ActionEvent) eventSource).getSource()).getScene().getWindow();
            } else {
                return; // Unsupported event type
            }

            // Switch to the new scene
            stage.setScene(new Scene(root));
            stage.setTitle("Echo");
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Called when login button is clicked with mouse
    public void logIn(MouseEvent mouseEvent) {
        logIn((Object) mouseEvent);
    }

    // Called when ENTER is pressed in the password field
    public void logInFromEnter(ActionEvent actionEvent) {
        logIn((Object) actionEvent);
    }
}
