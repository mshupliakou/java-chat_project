package com.example.javachat_project;
import com.example.javachat_project.DB.SupabaseConnect;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.util.Duration;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import static java.lang.Math.max;
import static java.lang.Math.min;

public class LoggedInController {
    @FXML private TextField search;
    private long currentUserId;
    @FXML
    private ListView<Chat> chatListView;

    private ObservableList<Chat> chatList;


    public void setCurrentUserId(long id) {
        this.currentUserId = id;
        try {
            loadChats();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }
    private Connection con;
    public void loadChats() throws SQLException {
        //chatList.clear();
        String sql = """
        SELECT id_chat, 
               CASE WHEN id_1 = ? THEN id_2 ELSE id_1 END AS other_user_id
        FROM chats_connections
        WHERE id_1 = ? OR id_2 = ?
    """;
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setLong(1, currentUserId);
        stmt.setLong(2, currentUserId);
        stmt.setLong(3, currentUserId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            int chatId = rs.getInt("id_chat");
            long otherUserId = rs.getLong("other_user_id");

            String loginQuery = "SELECT login FROM personInfo WHERE id = ?";
            PreparedStatement loginStmt = con.prepareStatement(loginQuery);
            loginStmt.setLong(1, otherUserId);
            ResultSet loginRs = loginStmt.executeQuery();

            String otherLogin = "Unknown";
            if (loginRs.next()) {
                otherLogin = loginRs.getString("login");
            }

            Chat chat = new Chat(chatId, otherLogin);
            chatList.add(chat);
            chatListView.setItems(chatList);

            loginStmt.close();
        }
        stmt.close();
    }


    public void initialize() {

        con = SupabaseConnect.getConnection();
        chatList = FXCollections.observableArrayList();
        chatListView.setItems(chatList);
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(100), event -> {
            try {
                refreshChats();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();



    }
    private void refreshChats() throws SQLException {
        if (currentUserId == 0) return;
        chatList.clear();
        loadChats();
    }


    public void newChat(String otherLogin, long currentUserId) {
        String getIdQuery = "SELECT id FROM personInfo WHERE login = ?";
        String insertSQL = "INSERT INTO chats_connections(id_1, id_2) VALUES (?, ?)";

        try (PreparedStatement pst = con.prepareStatement(getIdQuery)) {
            pst.setString(1, otherLogin);


            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    long otherUserId = rs.getLong("id");
                    System.out.println("User found: " + otherUserId);

                    try (PreparedStatement insertPst = con.prepareStatement(insertSQL)) {
                        insertPst.setLong(1, min(currentUserId, otherUserId));
                        insertPst.setLong(2, max(currentUserId,otherUserId));
                        insertPst.executeUpdate();
                        System.out.println("Chat connection created.");
                    }

                    int newId = rs.getInt(1);

                    Chat newChat = new Chat(newId, otherLogin);
                    chatList.add(newChat);

                } else {
                    System.out.println("There is no such login!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void handleEnter(ActionEvent actionEvent) {
        String login = search.getText();
        String query = "SELECT * FROM personInfo WHERE login = ?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, login);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Is found!");
                    newChat(login, currentUserId );
                } else {
                    System.out.println("There is no such login!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database error!");
        }
    }





}
