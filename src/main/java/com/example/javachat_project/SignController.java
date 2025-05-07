package com.example.javachat_project;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import com.example.javachat_project.DB.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SignController {
    @FXML
    private Label loginWrg;
    @FXML
    private Label confirmWrg;
    @FXML
    private Label passwordWrg;
    @FXML
    private Label nameWrg;
    @FXML
    private TextField tfLastName;
    @FXML
    private TextField tfLogin;
    @FXML
    private TextField tfConfirmPassword;
    @FXML
    private TextField tfPassword;
    @FXML
    private TextField tfMail;
    @FXML
    private TextField tfName;



    private Connection con;
    public void initialize() {
        con = DBConnection.getConnection();
    }
    private boolean checkTheName(String firstName){
        if (firstName.length() > 20) {
            nameWrg.setText("The name is too long!");
            return false;
        }
        else if(firstName.isEmpty()){
            nameWrg.setText("Please enter the name!");
            return false;
        }
        String possible_signs = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm_";
        for (char value : firstName.toCharArray()) {
            if (!possible_signs.contains(String.valueOf(value))) {
                nameWrg.setText("You can not use those signs! Please change the name!");
                return false;
            }
        }
        return  true;
    }
    private boolean checkTheLogin(String login){
        if (login.length() > 20) {
            loginWrg.setText("The login is too long!");
            return false;
        }
        else if(login.isEmpty()){
            loginWrg.setText("Please enter the login!");
            return false;
        }
        String possible_signs = "1234567890QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjklzxcvbnm_";
        for (char value : login.toCharArray()) {
            if (!possible_signs.contains(String.valueOf(value))) {
                loginWrg.setText("You can not use those signs! Please change the login!");
                return false;
            }
        }
        return  true;
    }
    private boolean checkPassword(String password, String confirmPassword){
        if (password.length() > 20) {
            passwordWrg.setText("The password is too long!");
            return false;
        }
        else if(password.isEmpty()){
            passwordWrg.setText("Please enter the password!");
            return false;
        }
        else if(!password.equals(confirmPassword)){
            confirmWrg.setText("The password you have entered are different!");
            return false;
        }
        return  true;
    }
    private void date_base(){
        String insert = "INSERT INTO personInfo(name, last_name, login, password) Values (?,?,?,?)";
        try {
            PreparedStatement pst = con.prepareStatement(insert);
            pst.setString(1, tfName.getText());
            pst.setString(2, tfLastName.getText());
            pst.setString(3, tfLogin.getText());
            pst.setString(4, tfPassword.getText());
            pst.executeUpdate();
            tfMail.getScene().getWindow().hide();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void signUp(MouseEvent mouseEvent) {
        nameWrg.setText("");
        loginWrg.setText("");
        passwordWrg.setText("");
        confirmWrg.setText("");
        String login = tfLogin.getText();
        String password = tfPassword.getText();
        String confirmPassword = tfConfirmPassword.getText();
        String firstName = tfName.getText();
        String lastName = tfLastName.getText();
        String mail = tfMail.getText();
        if(!checkTheName(firstName)){
            return;
        }
        if(!checkTheName(lastName)){
            return;
        }
        if(!checkTheLogin(login)){
            return;
        }
        if(!checkPassword(password, confirmPassword)){
            return;
        }
        date_base();


    }
}
