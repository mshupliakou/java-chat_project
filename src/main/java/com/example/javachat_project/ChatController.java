package com.example.javachat_project;

import com.example.javachat_project.DB.SupabaseConnect;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Objects;

import static com.example.javachat_project.FileSendingController.readableFileSize;

/**
 * Controller class for handling chat logic and GUI.
 */
public class ChatController {

    // === FXML Elements ===
    @FXML
    private TextField messageInput;
    @FXML
    private Label chat_name;
    @FXML
    private ListView<Message> messageListView;
    @FXML
    private ImageView send;
    @FXML
    private Label editingLabel;
    @FXML
    private VBox inputContainer;

    // === Application Fields ===
    private Client client;
    private Connection connection;
    private Chat chat;
    private User myself;

    private final ObservableList<Message> messageList = FXCollections.observableArrayList();
    private Message currentlyEditedMessage;
    private File currentlySendedFile;
    private boolean editing_mode = false;

    /**
     * Called when the FXML is loaded. Adds the custom CSS if it's not already there.
     */
    public void initialize() {
        messageListView.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null && newScene.getStylesheets().stream().noneMatch(s -> s.contains("chat.css"))) {
                newScene.getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource("/css/chat.css")).toExternalForm()
                );
            }
        });
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    /**
     * Initializes the chat between current user and target user.
     * Loads messages from the database and starts the client listener.
     */
    public void initData(User me, User target) {
        myself = me;
        chat_name.setText(target.getName() + " " + target.getLastName());

        connection = SupabaseConnect.getConnection();
        messageListView.setItems(messageList);

        setupMessageRendering(target);
        loadMessagesFromDatabase(me, target);
        startClientMessageListener(target);
    }
    private boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return name.endsWith(".png") || name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".gif");
    }

    /**
     * Sets up the rendering of each message in the ListView with styles and context menu.
     */
    private void setupMessageRendering(User target) {
        messageListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Message message, boolean empty) {
                super.updateItem(message, empty);

                if (empty || message == null) {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                    setStyle("");
                    return;
                }



                VBox bubble = new VBox();
                bubble.setSpacing(4);


                File file = message.getF();

                if (file != null && file.exists()) {
                    if (isImageFile(file)||message.isImage()) {
                        Image image = new Image(file.toURI().toString());
                        ImageView imageView = new ImageView(image);

                        imageView.setPreserveRatio(true);
                        imageView.setFitWidth(300);

                        bubble.getChildren().add(imageView);

                        imageView.setOnMouseClicked(event -> {
                            Stage stage = new Stage();
                            ImageView bigImageView = new ImageView(image);
                            bigImageView.setPreserveRatio(true);
                            bigImageView.setFitWidth(800);

                            StackPane root = new StackPane(bigImageView);
                            root.setStyle("-fx-background-color: rgba(0, 0, 0, 0.8);");

                            Scene scene = new Scene(root);
                            stage.setScene(scene);
                            stage.initModality(Modality.APPLICATION_MODAL);
                            stage.setTitle("Image Preview");
                            stage.show();
                        });
                    } else {
                        HBox fileBox = new HBox(10);
                        fileBox.setAlignment(Pos.CENTER_LEFT);

                        ImageView fileIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/blank-page.png"))));
                        fileIcon.setFitWidth(24);
                        fileIcon.setFitHeight(24);

                        VBox fileInfo = new VBox(2);
                        Label fileNameLabel = new Label(file.getName());
                        Label fileSizeLabel = new Label(readableFileSize(file.length()));
                        fileSizeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");

                        fileInfo.getChildren().addAll(fileNameLabel, fileSizeLabel);
                        fileBox.getChildren().addAll(fileIcon, fileInfo);


                        fileBox.setOnMouseClicked(event -> {
                            try {
                                Desktop.getDesktop().open(file);
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        });

                        bubble.getChildren().add(fileBox);

                    }

                }



                Label textLabel = new Label();
                textLabel.setWrapText(true);
                textLabel.setTextOverrun(OverrunStyle.CLIP);
                textLabel.textProperty().bind(message.textProperty());

                Label timeLabel = new Label(message.getFormattedTime());
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");
                bubble.getChildren().addAll(textLabel, timeLabel);

                String baseStyle = """
                -fx-padding: 12 16;
                -fx-background-radius: 20;
                -fx-font-size: 15px;
                -fx-font-family: 'Inter', sans-serif;
                -fx-text-fill: #2c3e50;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 4, 0.5, 0, 2);
                """;

                bubble.setStyle(baseStyle + (message.isFromMe() ?
                        "-fx-background-color: #c6f6d5;" :
                        "-fx-background-color: #ffffff;"));

                HBox container = new HBox(bubble);
                container.setPadding(new Insets(5));
                container.setAlignment(message.isFromMe() ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);

                setGraphic(container);
                setBackground(Background.EMPTY);
                textLabel.setTextFill(Color.web("#2c3e50"));
                timeLabel.setTextFill(Color.web("#888888"));

                // Context Menu
                ContextMenu contextMenu = new ContextMenu();

                MenuItem copyItem = new MenuItem("Copy");
                copyItem.setOnAction(e -> {
                    ClipboardContent content = new ClipboardContent();
                    content.putString(message.getText());
                    Clipboard.getSystemClipboard().setContent(content);
                });

                MenuItem deleteItem = new MenuItem("Delete");
                deleteItem.setOnAction(e -> {
                    sending("DELETED", message);
                    messageList.remove(message);
                    try (PreparedStatement stmt = connection.prepareStatement(
                            "DELETE FROM message WHERE id_chat = ? AND text = ? AND time_of_sending = ?")) {
                        stmt.setLong(1, chat.getChatId());
                        stmt.setString(2, message.getText());
                        stmt.setObject(3, message.getTime());
                        stmt.executeUpdate();
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                    }
                });

                contextMenu.getItems().addAll(copyItem, deleteItem);

                // Add "Edit" only for own messages
                if (message.isFromMe()) {
                    MenuItem editItem = new MenuItem("Edit");
                    editItem.setOnAction(e -> {
                        messageInput.setText(message.getText());
                        editingLabel.setVisible(true);
                        inputContainer.setStyle("""
                            -fx-background-color: #e3fcec;
                            -fx-background-radius: 8px;
                            -fx-padding: 10;
                            """);
                        currentlyEditedMessage = message;
                        editing_mode = true;
                        send.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/check.png"))));
                    });

                    contextMenu.getItems().add(editItem);
                }

                setContextMenu(contextMenu);
            }
        });
    }

    /**
     * Loads messages from the database for this chat.
     */
    private void loadMessagesFromDatabase(User me, User target) {
        String query = """
        SELECT m.id_message, m.id_author, m.time_of_sending, m.text,
               a.file_name, a.extension, a.inside
        FROM message m
        LEFT JOIN attachments a ON m.id_attachment = a.id
        WHERE m.id_chat = ?
        ORDER BY m.time_of_sending
        """;

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setLong(1, chat.getChatId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                long authorId = rs.getLong("id_author");
                long messageID = rs.getLong("id_message");
                String text = rs.getString("text");
                OffsetDateTime time = rs.getObject("time_of_sending", OffsetDateTime.class);
                boolean isFromMe = authorId == me.getId();

                File file = null;
                byte[] fileBytes = rs.getBytes("inside");
                String extension = rs.getString("extension");

                if (fileBytes != null && fileBytes.length > 0 && extension != null) {
                    if (!extension.startsWith(".")) {
                        extension = "." + extension;
                    }

                    file = File.createTempFile("img_" + messageID, extension);
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(fileBytes);
                    }
                    file.deleteOnExit();
                }

                messageList.add(new Message(
                        text,
                        isFromMe,
                        isFromMe ? me.getLogin() : target.getLogin(),
                        time,
                        messageID,
                        file
                ));
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Starts a thread that listens for incoming messages via the Client.
     */
    private void startClientMessageListener(User target) {
        client.setMessageListener(message -> {
            String[] parts = message.split(";", 8);
            if (parts.length < 5) return;
            String flag = parts[1];
            String sender;
            String text;
            OffsetDateTime time;
            String messageID;
            if (flag.equals("NEW")) {
                sender = parts[2];
                text = parts[3];
                messageID = parts[4];
                time = OffsetDateTime.parse(parts[5]);

                File tempFile = null;
                boolean isImage = false;

                if (parts.length > 6) {
                    String base64File = parts[6];
                    String extension = parts[7].toLowerCase();

                    if (base64File != null && !base64File.isEmpty()) {
                        try {
                            byte[] fileBytes = Base64.getDecoder().decode(base64File);

                            String tempSuffix = extension;
                            if (!tempSuffix.startsWith(".")) {
                                tempSuffix = "." + tempSuffix;
                            }

                            tempFile = File.createTempFile("file_", tempSuffix);
                            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                                fos.write(fileBytes);
                            }
                            tempFile.deleteOnExit();

                            if (extension.equals(".png") || extension.equals(".jpg") || extension.equals(".jpeg") || extension.equals(".gif") || extension.equals(".bmp")) {
                                isImage = true;
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                            tempFile = null;
                        }
                    }
                }

                File finalTempFile = tempFile;
                boolean finalIsImage = isImage;

                Platform.runLater(() -> {
                    System.out.println("Received file: " + finalTempFile + " isImage=" + finalIsImage);

                    messageList.add(new Message(text, sender.equals(client.getMyLogin()), sender, time, Long.parseLong(messageID), finalTempFile, finalIsImage));
                });
            }


            else if (flag.equals("DELETED")) {
                messageID = parts[3];
                long deletedId = Long.parseLong(messageID);
                Platform.runLater(() -> {
                    Message foundMessage = messageList.stream()
                            .filter(m -> Objects.equals(m.getId(), deletedId))
                            .findFirst()
                            .orElse(null);

                    if (foundMessage != null) {
                        System.out.println("inside"+foundMessage.getText());
                        messageList.remove(foundMessage);
                    }
                });
            }
            else if(flag.equals("EDIT")){
                messageID = parts[4];
                text = parts[3];
                System.out.println(text);
                long editedID = Long.parseLong(messageID);
                Platform.runLater(() -> {
                    Message foundMessage = messageList.stream()
                            .filter(m -> Objects.equals(m.getId(), editedID))
                            .findFirst()
                            .orElse(null);

                    if (foundMessage != null) {
                        System.out.println("inside:"+foundMessage.getText());
                        foundMessage.setText(text);
                    }
                });
            }


        });

        new Thread(client::begin).start();
    }

    // === Sending Logic ===

    @FXML
    public void send(MouseEvent mouseEvent) {
        if (!editing_mode)
            sending("", null);
        else {
            System.out.println("Message is being changed");
            editing();
            editing_mode = false;

        }
    }

    @FXML
    public void sendFromEnter(ActionEvent actionEvent) {
        if (!editing_mode)
            sending("", null);

        else {
            System.out.println("Message is being changed");
            editing();

            editing_mode = false;
        }
    }

    /**
     * Handles sending a message both to the UI and to the server/database.
     */
    public void sending(String flag, Message message) {
        String messageToSend = "";
        OffsetDateTime t = OffsetDateTime.now();

        if (flag.isEmpty()) {
            System.out.println("trying to send " + currentlySendedFile);
            byte[] fileBytes = null;
            String extension = "";
            String fileName = "";
            Long attachmentId = null;

            if (currentlySendedFile != null) {
                try {
                    fileBytes = Files.readAllBytes(currentlySendedFile.toPath());
                    fileName = currentlySendedFile.getName();
                    int dotIndex = fileName.lastIndexOf('.');
                    extension = (dotIndex != -1) ? fileName.substring(dotIndex) : "";
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            String msg = messageInput.getText();
            if ((msg == null || msg.isBlank()) && currentlySendedFile == null) return;

            try {

                if (fileBytes != null) {
                    try (PreparedStatement attStmt = connection.prepareStatement(
                            "INSERT INTO attachments (file_name, extension, inside) VALUES (?, ?, ?)",
                            Statement.RETURN_GENERATED_KEYS)) {
                        attStmt.setString(1, fileName);
                        attStmt.setString(2, extension);
                        attStmt.setBytes(3, fileBytes);

                        attStmt.executeUpdate();
                        try (ResultSet keys = attStmt.getGeneratedKeys()) {
                            if (keys.next()) {
                                attachmentId = keys.getLong(1);
                            }
                        }
                    }
                }

                // Вставляем сообщение
                try (PreparedStatement msgStmt = connection.prepareStatement(
                        "INSERT INTO message (id_author, text, id_chat, time_of_sending, id_attachment) VALUES (?, ?, ?, ?, ?)",
                        Statement.RETURN_GENERATED_KEYS)) {

                    msgStmt.setLong(1, myself.getId());
                    msgStmt.setString(2, msg);
                    msgStmt.setLong(3, chat.getChatId());
                    msgStmt.setObject(4, t);
                    if (attachmentId != null) {
                        msgStmt.setLong(5, attachmentId);
                    } else {
                        msgStmt.setNull(5, Types.BIGINT);
                    }

                    msgStmt.executeUpdate();

                    try (ResultSet generatedKeys = msgStmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            long generatedId = generatedKeys.getLong(1);
                            String formattedTime = t.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                            String base64File = (fileBytes != null) ? Base64.getEncoder().encodeToString(fileBytes) : "";


                            messageToSend = "NEW;" + client.getMyLogin() + ";" + msg + ";" + generatedId + ";" + formattedTime + ";" + base64File + ";" + extension;

                            Message newMessage = new Message(msg, true, client.getMyLogin(), t, generatedId, currentlySendedFile);
                            messageList.add(newMessage);

                            client.sendMessage(messageToSend);
                            currentlySendedFile = null;
                        }
                    }

                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            messageInput.clear();

        } else if (flag.equals("DELETED") && message != null) {
            messageToSend = "DELETED;" + client.getMyLogin() + ";" + message.getId() + ";" + t;
            client.sendMessage(messageToSend);
        } else if (flag.equals("EDIT") && message != null) {
            if (messageInput.getText() == null || messageInput.getText().isBlank()) return;
            messageToSend = "EDIT;" + client.getMyLogin() + ";" + message.getText() + ";" + message.getId() + ";" + t;
            client.sendMessage(messageToSend);
        }
    }

    public void attach(MouseEvent mouseEvent) {
        FileDialog dialog = new FileDialog((Frame) null, "Select File to Open");
        dialog.setMode(FileDialog.LOAD);
        dialog.setVisible(true);

        String fileName = dialog.getFile();
        String dir = dialog.getDirectory();

        dialog.dispose();

        if (fileName == null || dir == null) {
            System.out.println("No file selected.");
            return;
        }

        File file = new File(dir, fileName);
        System.out.println(file + " chosen.");

        try {
            Path path = file.toPath();
            String mimeType = Files.probeContentType(path);
            System.out.println("MIME type: " + mimeType);

            if (mimeType != null && mimeType.startsWith("image")) {
                System.out.println("This is a photo");

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/image_sending.fxml"));
                Parent root = loader.load();


                ImageSendingController imageSendingController = loader.getController();


                Image image = new Image(file.toURI().toString());
                imageSendingController.setImageForSending(image);


                Stage primstage = new Stage();
                primstage.getIcons().add(new Image(
                        Objects.requireNonNull(MainLogin.class.getResourceAsStream("/img/mini_logo.png"))
                ));
                primstage.setTitle("Echo | Image sending");

                Scene scene = new Scene(root);
                primstage.setScene(scene);
                primstage.setHeight(435);
                primstage.setWidth(415);
                primstage.setResizable(false);
                primstage.initModality(Modality.APPLICATION_MODAL);
                primstage.showAndWait();

                if(imageSendingController.isSendConfirmed()){
                    currentlySendedFile = file;
                    sending("", null);
                }
            } else {
                System.out.println("This is a file");
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/file_sending.fxml"));
                Parent root = loader.load();


                FileSendingController fileSendingController = loader.getController();
                fileSendingController.setText_input_file_sending(messageInput.getText());

                fileSendingController.setFile(file);

                Stage primstage = new Stage();
                primstage.getIcons().add(new Image(
                        Objects.requireNonNull(MainLogin.class.getResourceAsStream("/img/mini_logo.png"))
                ));
                primstage.setTitle("Echo | File sending");

                Scene scene = new Scene(root);
                primstage.setScene(scene);
                primstage.setHeight(260);
                primstage.setWidth(415);
                primstage.setResizable(false);
                primstage.initModality(Modality.APPLICATION_MODAL);
                primstage.showAndWait();

                if(fileSendingController.isSendConfirmed()){
                    currentlySendedFile = file;
                    messageInput.setText(fileSendingController.getText_input_file_sending());
                    sending("", null);
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editing() {
        String newText = messageInput.getText();


        Message updatedMessage = new Message(
                newText,
                true,
                currentlyEditedMessage.getSenderLogin(),
                currentlyEditedMessage.getTime(),
                currentlyEditedMessage.getId()
        );
        sending("EDIT", updatedMessage);
        int index = messageList.indexOf(currentlyEditedMessage);
        if (index >= 0) {
            messageList.set(index, updatedMessage);
        }


        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE message SET text = ? WHERE id_message = ?")) {
            stmt.setString(1, newText);
            stmt.setLong(2, currentlyEditedMessage.getId());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        messageInput.clear();
        inputContainer.setStyle("");
        editingLabel.setVisible(false);
        send.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/img/sending.png"))));
        currentlyEditedMessage = null;
    }

}
