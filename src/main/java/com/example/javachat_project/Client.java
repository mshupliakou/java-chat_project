package com.example.javachat_project;

import java.io.*;
import java.net.*;

/**
 * A simple client that connects to a chat server using sockets.
 * Sends login info on connection and listens for incoming messages asynchronously.
 */
public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 12345;

    private final String login;
    private final String login2;

    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;

    private MessageListener messageListener;

    public Client(String login, String login2) {
        this.login = login;
        this.login2 = login2;
    }

    /**
     * Sets a listener to handle incoming messages from the server.
     */
    public void setMessageListener(MessageListener listener) {
        this.messageListener = listener;
    }

    /**
     * Begins connection to the server, sends login info,
     * and listens for incoming messages in a new thread.
     */
    public void begin() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            System.out.println("Connected to the chat server!");

            // Set up output stream to send messages to server
            out = new PrintWriter(socket.getOutputStream(), true);

            // Set up input stream to receive messages from server
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Send login information to server
            out.println("LOGIN:" + login + ":" + login2);

            // Start a separate thread to continuously listen for server messages
            new Thread(() -> {
                try {
                    String serverResponse;
                    while ((serverResponse= in.readLine()) != null) {
                        System.out.println("Server: " + serverResponse);
                        if (messageListener != null) {
                            messageListener.onMessageReceived(serverResponse);
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Connection lost or error reading from server:");
                    e.printStackTrace();
                } finally {
                    close();  // Ensure resources are cleaned up if connection closes
                }
            }).start();

        } catch (IOException e) {
            System.err.println("Failed to connect to server:");
            e.printStackTrace();
            close();
        }
    }

    /**
     * Sends a message to the server.
     */
    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        } else {
            System.err.println("Cannot send message, output stream is null.");
        }
    }

    /**
     * Closes the socket and associated streams.
     */
    public void close() {
        try {
            if (in != null) in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (out != null) {
            out.close();
        }

        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLogin() {
        return login;
    }

    public String getLogin2() {
        return login2;
    }

    /**
     * Listener interface for receiving messages from the server.
     */
    public interface MessageListener {
        void onMessageReceived(String message);
    }
}
