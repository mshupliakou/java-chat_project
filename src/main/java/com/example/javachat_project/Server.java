package com.example.javachat_project;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * A multi-client chat server supporting private two-way conversations.
 */
public class Server {
    private static final int PORT = 12345;

    // Maps chat key (e.g., "alice:bob") to a list of ClientHandlers in that chat
    private static final Map<String, List<ClientHandler>> activeChats = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for connections...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                new Thread(clientHandler).start();
            }

        } catch (IOException e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }

    /**
     * Creates a chat key that uniquely identifies a chat between two users, regardless of order.
     */
    private static String getChatKey(String user1, String user2) {
        return user1.compareTo(user2) < 0 ? user1 + ":" + user2 : user2 + ":" + user1;
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String targetUsername;
        private String chatKey;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Expect LOGIN:sender:receiver
                String loginCommand = in.readLine();
                if (loginCommand == null || !loginCommand.startsWith("LOGIN:")) {
                    out.println("[Server]: LOGIN command expected.");
                    return;
                }

                String[] parts = loginCommand.split(":");
                if (parts.length != 3) {
                    out.println("[Server]: Invalid LOGIN format.");
                    return;
                }

                username = parts[1].trim();
                targetUsername = parts[2].trim();
                chatKey = getChatKey(username, targetUsername);

                // Register this client into the chat
                activeChats.computeIfAbsent(chatKey, k -> new CopyOnWriteArrayList<>()).add(this);

                out.println("[Server]: Connected as " + username + ". Chatting with " + targetUsername + ".");

                // Read messages and forward to other participant(s)
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("[" + username + " -> " + targetUsername + "]: " + inputLine);
                    sendPrivate(inputLine);
                }

            } catch (IOException e) {
                System.err.println("Connection error with user " + username + ":");
                e.printStackTrace();
            } finally {
                cleanup();
            }
        }

        /**
         * Sends a message to this client.
         */
        public void sendMessage(String message) {
            if (out != null) {
                out.println(message);
            }
        }

        /**
         * Sends the message to other users in the same chat.
         */
        public void sendPrivate(String message) {
            List<ClientHandler> chatHandlers = activeChats.get(chatKey);

            if (chatHandlers != null) {
                for (ClientHandler client : chatHandlers) {
                    if (client != this) {
                        client.sendMessage(username + ";" + message);

                    }
                }
            } else {
                sendMessage("[Server]: Chat not found.");
            }
        }

        /**
         * Removes the user from the active chat and cleans up resources.
         */
        private void cleanup() {
            if (chatKey != null) {
                List<ClientHandler> handlers = activeChats.get(chatKey);
                if (handlers != null) {
                    handlers.remove(this);
                    if (handlers.isEmpty()) {
                        activeChats.remove(chatKey);
                    }
                }
            }

            System.out.println("User " + username + " disconnected.");

            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();
            } catch (IOException e) {
                System.err.println("Error closing resources for user " + username + ":");
                e.printStackTrace();
            }
        }




    }


}
