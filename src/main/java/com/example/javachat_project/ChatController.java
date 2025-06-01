package com.example.javachat_project;

import com.example.javachat_project.DB.SupabaseConnect;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.OverrunStyle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChatController {

    @FXML
    private TextField messageInput;

    @FXML
    private  Label chat_name;
    @FXML
    private VBox vbox;

    @FXML
    private ListView<Message> messageListView;

    private Client client;
    private final ObservableList<Message> messageList = FXCollections.observableArrayList();

    public void setClient(Client client) {
        this.client = client;
    }
    /**
     * Initializes chat data by retrieving user IDs for both the current user and the target user.
     * Starts the client to begin chat communication.
     *
     * @param myLogin     The login of the current user.
     * @param targetLogin The login of the user to chat with.
     */
    public void initData(String myLogin, String targetLogin) {
        chat_name.setText(targetLogin);
        int id1 = -1;
        int id2 = -1;

        String query = "SELECT id, login FROM personInfo WHERE login = ? OR login = ?";
        Connection conn = SupabaseConnect.getConnection();

        try {
            System.out.println("Is connection closed? " + conn.isClosed());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, myLogin);
            stmt.setString(2, targetLogin);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String login = rs.getString("login");
                    long id = rs.getLong("id");

                    if (login.equals(myLogin)) {
                        id1 = (int) id;
                    } else if (login.equals(targetLogin)) {
                        id2 = (int) id;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error while fetching user IDs:");
            e.printStackTrace();
            return;
        }

        if (id1 == -1 || id2 == -1) {
            System.err.println("Failed to find one or both users in database.");
            return;
        }


        messageListView.setItems(messageList);



        messageListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);
                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Label label = new Label(message.getText());
                    label.setWrapText(true);
                    label.setTextOverrun(OverrunStyle.CLIP);


                    label.setStyle(
                            "-fx-padding: 12 16;" +
                                    "-fx-background-radius: 20;" +
                                    "-fx-font-size: 15px;" +
                                    "-fx-font-family: 'Inter', sans-serif;" +
                                    "-fx-text-fill: #2c3e50;" +
                                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.5, 0, 2);"
                    );
                    HBox container = new HBox(label);
                    container.setPadding(new Insets(5));
                    container.setMaxWidth(Double.MAX_VALUE);

                    if (message.isFromMe()) {
                        label.setStyle(label.getStyle() + "-fx-background-color: #c6f6d5;");
                        container.setAlignment(Pos.CENTER_RIGHT);
                    } else {
                        label.setStyle(label.getStyle() + "-fx-background-color: #ffffff;");
                        container.setAlignment(Pos.CENTER_LEFT);
                    }

                    setGraphic(container);


                }
            }
        });



        client.setMessageListener(message -> {
            Platform.runLater(() -> messageList.add(new Message(message, false)));
        });


        new Thread(client::begin).start();
    }

    public void send(MouseEvent mouseEvent) {
        String msg = messageInput.getText();
        if (msg == null || msg.isBlank()) return;

        client.sendMessage(msg);
        messageList.add(new Message(msg, true));
        messageInput.clear();
    }
}
