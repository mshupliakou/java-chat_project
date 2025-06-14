package com.example.javachat_project;

import com.example.javachat_project.DB.SupabaseConnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class LoggedInController {

    @FXML private TextField search; // Field for searching user by login
    @FXML private ListView<Chat> chatListView; // ListView to display chats
    @FXML private AnchorPane chat_holder; // Container for the selected chat content

    private static ObservableList<Chat> chatList; // ObservableList used by chatListView

    private Connection con; // Database connection
    private User currentUser;

    public static ObservableList<Chat> getChatList() {
        return chatList;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;

        if (con == null) {
            con = SupabaseConnect.getConnection();
        }

        try {
            loadChats();
        } catch (SQLException e) {
            showErrorAlert("Error loading chats", "Failed to load chats for user: " + currentUser.getLogin());
            e.printStackTrace();
        }
    }

    /**
     * Loads chats of the current user from the database.
     */
    public void loadChats() throws SQLException {
        if (chatList == null) {
            chatList = FXCollections.observableArrayList();
        }
        chatList.clear();

        String sql = """
                SELECT id_chat,
                       CASE WHEN id_1 = ? THEN id_2 ELSE id_1 END AS other_user_id
                FROM chats_connections
                WHERE id_1 = ? OR id_2 = ?
            """;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            stmt.setLong(1, currentUser.getId());
            stmt.setLong(2, currentUser.getId());
            stmt.setLong(3, currentUser.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int chatId = rs.getInt("id_chat");
                    long otherUserId = rs.getLong("other_user_id");

                    String loginQuery = "SELECT * FROM personInfo WHERE id = ?";

                    try (PreparedStatement loginStmt = con.prepareStatement(loginQuery)) {
                        loginStmt.setLong(1, otherUserId);

                        try (ResultSet loginRs = loginStmt.executeQuery()) {
                            if (loginRs.next()) {
                                String otherLogin = loginRs.getString("login");
                                String firstName = loginRs.getString("name");
                                String lastName = loginRs.getString("last_name");
                                String otherPassword = loginRs.getString("password");

                                User otherUser = new User(otherUserId, firstName, lastName, otherPassword, otherLogin);
                                Chat chat = new Chat(chatId, otherUser);
                                chatList.add(chat);
                            }
                        }
                    }
                }
            }
        }

        chatListView.setItems(chatList);
    }

    /**
     * Initializes the controller.
     */
    public void initialize() {
        con = SupabaseConnect.getConnection();
        chatList = FXCollections.observableArrayList();
        chatListView.setItems(chatList);
    }

    /**
     * Refreshes the list of chats.
     */
    public void refreshChats(MouseEvent mouseEvent) {
        if (currentUser == null || currentUser.getId() == 0) return;

        try {
            loadChats();
        } catch (SQLException e) {
            showErrorAlert("Update Error", "Failed to update the chat list.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new chat with a user by login.
     * If a chat already exists, it does not create a duplicate.
     */
    public void newChat(String otherLogin) {
        if (otherLogin == null || otherLogin.isBlank()) {
            showErrorAlert("Error", "Login cannot be empty.");
            return;
        }

        try {
            // First, find the user by login
            String getUserQuery = "SELECT * FROM personInfo WHERE login = ?";
            User newChatter = null;
            long otherUserId = -1;

            try (PreparedStatement pst = con.prepareStatement(getUserQuery)) {
                pst.setString(1, otherLogin);

                try (ResultSet rs = pst.executeQuery()) {
                    if (rs.next()) {
                        otherUserId = rs.getLong("id");
                        String firstName = rs.getString("name");
                        String lastName = rs.getString("last_name");
                        String password = rs.getString("password");
                        newChatter = new User(otherUserId, firstName, lastName, password, otherLogin);
                    } else {
                        showErrorAlert("User not found", "User with login '" + otherLogin + "' does not exist.");
                        return;
                    }
                }
            }

            if (newChatter == null) return;

            // Check if a chat between these users already exists
            String checkChatQuery = """
                SELECT id_chat FROM chats_connections
                WHERE (id_1 = ? AND id_2 = ?) OR (id_1 = ? AND id_2 = ?)
            """;

            long id1 = min(currentUser.getId(), otherUserId);
            long id2 = max(currentUser.getId(), otherUserId);

            try (PreparedStatement checkStmt = con.prepareStatement(checkChatQuery)) {
                checkStmt.setLong(1, id1);
                checkStmt.setLong(2, id2);
                checkStmt.setLong(3, id2);
                checkStmt.setLong(4, id1);

                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Chat already exists — add it to the list if not present
                        int chatId = rs.getInt("id_chat");
                        boolean chatExists = chatList.stream().anyMatch(c -> c.getChatId() == chatId);
                        if (!chatExists) {
                            Chat existingChat = new Chat(chatId, newChatter);
                            chatList.add(existingChat);
                        }
                        return;
                    }
                }
            }

            // Create a new chat if not found
            String insertSQL = "INSERT INTO chats_connections(id_1, id_2) VALUES (?, ?) RETURNING id_chat";
            try (PreparedStatement insertPst = con.prepareStatement(insertSQL)) {
                insertPst.setLong(1, id1);
                insertPst.setLong(2, id2);

                try (ResultSet rsInsert = insertPst.executeQuery()) {
                    if (rsInsert.next()) {
                        int chatId = rsInsert.getInt("id_chat");
                        Chat newChat = new Chat(chatId, newChatter);
                        chatList.add(newChat);
                    }
                }
            }

        } catch (SQLException e) {
            showErrorAlert("Database error", "An error occurred while working with the database.");
            e.printStackTrace();
        }
    }

    /**
     * Handles pressing Enter or search button — creates a new chat.
     */
    public void handleEnter(ActionEvent actionEvent) {
        String login = search.getText().trim();
        if (login.isEmpty()) {
            showErrorAlert("Error", "Please enter a login to search for.");
            return;
        }
        newChat(login);
    }

    /**
     * Handles selection of a chat from the list.
     */
    public void chooseChat(MouseEvent mouseEvent) {
        Chat selectedChat = chatListView.getSelectionModel().getSelectedItem();
        if (selectedChat == null) return;

        User otherUser = selectedChat.getOtherUser();
        if (otherUser == null || otherUser.getLogin() == null) return;
        if (otherUser.getLogin().equals(currentUser.getLogin())) return; // Prevent selecting self

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
            Parent chatContent = loader.load();
            chatContent.getStylesheets().clear();

            ChatController chatController = loader.getController();
            Client client = new Client(currentUser, otherUser);

            chatController.setClient(client);
            chatController.setChat(selectedChat);
            chatController.initData(currentUser, otherUser);

            chat_holder.getChildren().setAll(chatContent);
            AnchorPane.setTopAnchor(chatContent, 0.0);
            AnchorPane.setBottomAnchor(chatContent, 0.0);
            AnchorPane.setLeftAnchor(chatContent, 0.0);
            AnchorPane.setRightAnchor(chatContent, 0.0);

        } catch (IOException e) {
            showErrorAlert("Chat loading error", "Failed to load the chat window.");
            e.printStackTrace();
        }
    }

    /**
     * Helper method to show an error alert dialog.
     */
    private void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}
