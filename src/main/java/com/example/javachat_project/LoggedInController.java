package com.example.javachat_project;
import com.example.javachat_project.DB.SupabaseConnect;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
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

    @FXML private TextField search; // TextField for entering login to search for
    @FXML private ListView<Chat> chatListView; // ListView to display user chats
    @FXML private AnchorPane chat_holder; // Pane to hold chat UI content

    private ObservableList<Chat> chatList; // Observable list backing the ListView
    private Connection con; // Database connection object
    private User currentUser;


    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        try {
            loadChats();
        } catch (SQLException e) {
            throw new RuntimeException("Error loading chats for user: " + currentUser, e);
        }
    }

    /**
     * Loads all chats for the current user from the database.
     * It finds all chats where the user is either participant id_1 or id_2.
     */
    public void loadChats() throws SQLException {
        chatList.clear(); // Clear current chat list

        String sql = """
            SELECT id_chat, 
                   CASE WHEN id_1 = ? THEN id_2 ELSE id_1 END AS other_user_id
            FROM chats_connections
            WHERE id_1 = ? OR id_2 = ?
        """;

        try (PreparedStatement stmt = con.prepareStatement(sql)) {
            // Set parameters for currentUserId in all three places
            stmt.setLong(1, currentUser.getId());
            stmt.setLong(2, currentUser.getId());
            stmt.setLong(3, currentUser.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int chatId = rs.getInt("id_chat");
                    long otherUserId = rs.getLong("other_user_id");

                    // Query to get user info for the other user in the chat
                    String loginQuery = "SELECT * FROM personInfo WHERE id = ?";

                    try (PreparedStatement loginStmt = con.prepareStatement(loginQuery)) {
                        loginStmt.setLong(1, otherUserId);
                        try (ResultSet loginRs = loginStmt.executeQuery()) {
                            if (loginRs.next()) {
                                String otherLogin = loginRs.getString("login");
                                String firstName = loginRs.getString("name");
                                String lastName = loginRs.getString("last_name");
                                String other_password = loginRs.getString("password");
                                User otherUser= new User(otherUserId, firstName, lastName, other_password, otherLogin);
                                Chat chat = new Chat(chatId, otherUser);
                                chatList.add(chat);
                            }
                        }
                    }
                }
            }
        }
        chatListView.setItems(chatList); // Update the ListView items
    }

    /**
     * JavaFX initialize method called automatically after FXML loading.
     * Here we initialize the DB connection and chat list.
     */
    public void initialize() {
        con = SupabaseConnect.getConnection(); // Get DB connection
        chatList = FXCollections.observableArrayList();
        chatListView.setItems(chatList);
    }

    /**
     * Refreshes the chat list by reloading the chats from the DB.
     * @throws SQLException if DB error occurs
     */
    private void refreshChats() throws SQLException {
        if (currentUser.getId() == 0) return;
        chatList.clear();
        loadChats();
    }

    /**
     * Creates a new chat connection with another user identified by their login.
     */
    public void newChat(String otherLogin) {
        String getIdQuery = "SELECT id, name, last_name FROM personInfo WHERE login = ?";
        String insertSQL = "INSERT INTO chats_connections(id_1, id_2) VALUES (?, ?)";

        try (PreparedStatement pst = con.prepareStatement(getIdQuery)) {
            pst.setString(1, otherLogin);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    long otherUserId = rs.getLong("id");
                    String firstName = rs.getString("name");
                    String lastName = rs.getString("last_name");
                    String password = rs.getString("password");
                    User new_chatter = new User(otherUserId, firstName, lastName, password, otherLogin);
                    System.out.println("User found: " + otherUserId);

                    try (PreparedStatement insertPst = con.prepareStatement(insertSQL)) {
                        // Always store smaller ID first to avoid duplicates (convention)
                        insertPst.setLong(1, min(currentUser.getId(), otherUserId));
                        insertPst.setLong(2, max(currentUser.getId(), otherUserId));
                        insertPst.executeUpdate();
                        System.out.println("Chat connection created.");
                    }

                    // Note: chatId may not be the same as personInfo.id; consider querying the chat ID again if needed
                    Chat newChat = new Chat(rs.getInt("id"), new_chatter);
                    chatList.add(newChat);

                } else {
                    System.out.println("There is no such login!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Consider user notification or UI feedback for errors
        }
    }

    /**
     * Handler for Enter key or button press to search for a user by login and create a new chat.
     */
    public void handleEnter(ActionEvent actionEvent) {
        String login = search.getText();
        String query = "SELECT * FROM personInfo WHERE login = ?";

        try (PreparedStatement pst = con.prepareStatement(query)) {
            pst.setString(1, login);

            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    System.out.println("User found!");
                    newChat(login);
                } else {
                    System.out.println("There is no such login!");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("Database error!");
        }
    }

    /**
     * Handler for selecting a chat from the list.
     * Loads the chat view for the selected user.
     */
    public void chooseChat(MouseEvent mouseEvent) {
        Chat selectedUser = chatListView.getSelectionModel().getSelectedItem();

        // Ignore invalid selections or selecting self
        if (selectedUser == null || selectedUser.getOtherUser().getLogin() == null || selectedUser.getOtherUser().getLogin().equals(currentUser.getLogin())) {
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/chat.fxml"));
            Parent chatContent = loader.load();
            chatContent.getStylesheets().clear();
            ChatController chatController = loader.getController();
            Client client = new Client(currentUser, selectedUser.getOtherUser());
            chatController.setClient(client);
            System.out.println(currentUser.getLogin()+" "+selectedUser.getOtherUser().getLogin() );
            chatController.initData(currentUser, selectedUser.getOtherUser());
            chatController.setChat(selectedUser);
            // Replace current chat holder content with loaded chat UI
            chat_holder.getChildren().setAll(chatContent);

            // Anchor the loaded chat content to fill the AnchorPane
            AnchorPane.setTopAnchor(chatContent, 0.0);
            AnchorPane.setBottomAnchor(chatContent, 0.0);
            AnchorPane.setLeftAnchor(chatContent, 0.0);
            AnchorPane.setRightAnchor(chatContent, 0.0);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
