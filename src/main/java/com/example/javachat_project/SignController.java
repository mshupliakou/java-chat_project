package com.example.javachat_project;

import com.example.javachat_project.DB.SupabaseConnect;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import com.example.javachat_project.DB.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class SignController {
    @FXML private Label loginWrg;
    @FXML private Label confirmWrg;
    @FXML private Label passwordWrg;
    @FXML private Label nameWrg;
    @FXML private TextField tfLastName;
    @FXML private TextField tfLogin;
    @FXML private PasswordField tfConfirmPassword;
    @FXML private PasswordField tfPassword;
    @FXML private TextField tfName;

    private Connection localConnection;
    private Connection remoteConnection;

    public void initialize() {
        //localConnection = DBConnection.getConnection();
        remoteConnection = SupabaseConnect.getConnection();
    }

    private boolean isValidString(String input, int maxLength, Label errorLabel, String fieldName) {
        if (input == null || input.isEmpty()) {
            errorLabel.setText("Please enter " + fieldName + "!");
            return false;
        }
        if (input.length() > maxLength) {
            errorLabel.setText(fieldName + " is too long!");
            return false;
        }
        if (!input.matches("[\\w\\d_]+")) {
            errorLabel.setText("Invalid characters in " + fieldName + "!");
            return false;
        }
        errorLabel.setText("");
        return true;
    }

    private boolean validatePasswords(String password, String confirmPassword) {
        if (password == null || password.isEmpty()) {
            passwordWrg.setText("Please enter the password!");
            return false;
        }
        if (password.length() > 20) {
            passwordWrg.setText("Password is too long!");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            confirmWrg.setText("Passwords do not match!");
            return false;
        }
        passwordWrg.setText("");
        confirmWrg.setText("");
        return true;
    }

    private void insertUserIntoDatabase(String name, String lastName, String login, String password) {
        String insertSQL = "INSERT INTO personInfo(name, last_name, login, password) VALUES (?, ?, ?, ?)";
        // TODO: hash passwords are needed

        try (
                PreparedStatement pstRemote = remoteConnection.prepareStatement(insertSQL);
                //PreparedStatement pstLocal = localConnection.prepareStatement(insertSQL);
        ) {
            pstRemote.setString(1, name);
            pstRemote.setString(2, lastName);
            pstRemote.setString(3, login);
            pstRemote.setString(4, password);
            pstRemote.executeUpdate();

//            pstLocal.setString(1, name);
//            pstLocal.setString(2, lastName);
//            pstLocal.setString(3, login);
//            pstLocal.setString(4, password);
//            pstLocal.executeUpdate();


            tfName.getScene().getWindow().hide();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void signUp(MouseEvent event) {
        nameWrg.setText("");
        loginWrg.setText("");
        passwordWrg.setText("");
        confirmWrg.setText("");

        String name = tfName.getText();
        String lastName = tfLastName.getText();
        String login = tfLogin.getText();
        String password = tfPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();

        if (!isValidString(name, 20, nameWrg, "Name")) return;
        if (!isValidString(lastName, 20, nameWrg, "Last name")) return;
        if (!isValidString(login, 20, loginWrg, "Login")) return;
        if (!validatePasswords(password, confirmPassword)) return;

        insertUserIntoDatabase(name, lastName, login, password);
    }
}
