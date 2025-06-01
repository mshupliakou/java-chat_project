package com.example.javachat_project;

import com.example.javachat_project.DB.SupabaseConnect;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.time.OffsetDateTime;

public class ChatController {
    @FXML
    private TextField messageInput;
    @FXML
    private  Label chat_name;
    @FXML
    private ListView<Message> messageListView;

    private Client client;
    private Connection connection;
    private Chat chat;


    private final ObservableList<Message> messageList = FXCollections.observableArrayList();

    public void setClient(Client client) {
        this.client = client;
    }


    /**
     * Initializes chat data by retrieving user IDs for both the current user and the target user.
     * Starts the client to begin chat communication.
     *
     */
    public void initData(User me, User target) {
        chat_name.setText(target.getName()+" "+target.getLastName());
        connection=SupabaseConnect.getConnection();
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
            System.out.println(message);
            String[] parts = message.split(":", 3);
            if (parts.length < 3) return;

            String sender = parts[0];
            String text = parts[2];
            System.out.println("Got a message "+text);


            if (sender.equals(target.getLogin()) || sender.equals(client.getMyLogin())) {

                Platform.runLater(() -> messageList.add(new Message(text, sender.equals(client.getMyLogin()), sender)));
                try (PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO message (id_author, text, id_chat, time_of_sending) VALUES (?, ?, ?, ?)")) {
                    if(sender.equals(client.getMyLogin()))
                    stmt.setLong(1, me.getId());
                    else{
                        stmt.setLong(1, target.getId());
                    }
                    stmt.setString(2, text);
                    stmt.setLong(3, chat.getChatId());
                    stmt.setObject(4, OffsetDateTime.now());
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        });
        new Thread(client::begin).start();
    }

    public void send(MouseEvent mouseEvent) {
        sending();
    }


    public void sending(){
        String msg = messageInput.getText();
        if (msg == null || msg.isBlank()) return;

        String messageToSend = client.getMyLogin() + ":" + msg;
        client.sendMessage(messageToSend);

        messageList.add(new Message(msg, true, client.getMyLogin()));
        messageInput.clear();
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public void sendFromEnter(ActionEvent actionEvent) {
        sending();
    }
}
