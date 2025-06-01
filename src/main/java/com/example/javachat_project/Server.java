package com.example.javachat_project;

import java.io.*;
import java.net.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * A simple multi-client chat server that supports private messaging.
 */
public class Server {
    private static final int PORT = 12345;
    private static final CopyOnWriteArrayList<ClientHandler> clients = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server is running and waiting for connections...");

            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket);

                ClientHandler clientHandler = new ClientHandler(clientSocket);
                clients.add(clientHandler);
                new Thread(clientHandler).start();


            }

        } catch (IOException e) {
            System.err.println("Server exception:");
            e.printStackTrace();
        }
    }

    /**
     * Broadcast a message to all clients except the sender.
     */
    public static void broadcast(String message, ClientHandler sender) {
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendMessage(message);
            }
        }
    }

    private static class ClientHandler implements Runnable {
        private final Socket clientSocket;
        private PrintWriter out;
        private BufferedReader in;
        private String username;
        private String targetUsername;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try {
                // Initialize streams inside the try block to ensure they're closed properly
                out = new PrintWriter(clientSocket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

                // Expect LOGIN command in format: LOGIN:sender:receiver
                String loginCommand = in.readLine();
                if (loginCommand == null || !loginCommand.startsWith("LOGIN:")) {
                    out.println("[Server]: LOGIN command expected.");
                    return;
                }

                String[] parts = loginCommand.split(":");
                if (parts.length != 3) {
                    out.println("[Server]: Invalid LOGIN command format.");
                    return;
                }

                username = parts[1].trim();
                targetUsername = parts[2].trim();

               // System.out.println("User " + username + " connected to chat with " + targetUsername);
                //out.println("[Server]: Connected as " + username + ", chatting with " + targetUsername);

                // Listen for messages from this client and forward them privately
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("[" + username + " -> " + targetUsername + "]: " + inputLine);
                    sendPrivate(  inputLine, targetUsername);
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
         * Sends a private message to the recipient client.
         * If recipient not found, notifies sender.
         */
        public void sendPrivate(String message, String recipient) {
            boolean delivered = false;
            for (ClientHandler client : clients) {
                if (client.username != null && client.username.equals(recipient)) {
                    client.sendMessage(message);
                    delivered = true;
                    break;
                }
            }
            if (!delivered) {
                //sendMessage("[Server]: User " + recipient + " not found or offline.");
            }
        }

        /**
         * Clean up resources and remove this client from the list.
         */
        private void cleanup() {
            clients.remove(this);
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
