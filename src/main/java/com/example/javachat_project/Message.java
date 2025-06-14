package com.example.javachat_project;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class Message {

    // Message text content
    private StringProperty text;

    // Whether the message was sent by the current user
    private boolean fromMe;

    // Timestamp of when the message was sent
    private OffsetDateTime time;

    // Login of the sender
    private final String senderLogin;

    // Unique ID of the message in the database
    private Long id;

    public File getF() {
        return f;
    }

    public void setF(File f) {
        this.f = f;
    }

    private File f;

    /**
     * Constructor to initialize a Message object
     *
     * @param text         message text
     * @param fromMe       true if message is from current user
     * @param senderLogin  login of the sender
     * @param time         timestamp of the message
     * @param id           unique message ID
     */
    public Message(String text, boolean fromMe, String senderLogin, OffsetDateTime time, Long id) {
        this.text = new SimpleStringProperty(text);
        this.fromMe = fromMe;
        this.senderLogin = senderLogin;
        this.time = time;
        this.id = id;
    }

    public Message(String text, boolean fromMe, String senderLogin, OffsetDateTime time, Long id, File f) {
        this.text = new SimpleStringProperty(text);
        this.fromMe = fromMe;
        this.senderLogin = senderLogin;
        this.time = time;
        this.id = id;
        this.f=f;
    }


    public Message(String text, boolean fromMe, String senderLogin, OffsetDateTime time) {
        this.text = new SimpleStringProperty(text);
        this.fromMe = fromMe;
        this.senderLogin = senderLogin;
        this.time = time;
    }

    // Getter for message text
    public String getText() {
        return text.get();
    }

    // Setter for message text (to allow editing)
    public void setText(String text) {
        this.text.set(text);
    }

    // Returns true if this message was sent by the current user
    public boolean isFromMe() {
        return fromMe;
    }

    // Getter for sender's login
    public String getSenderLogin() {
        return senderLogin;
    }

    // Getter for message timestamp
    public OffsetDateTime getTime() {
        return time;
    }

    // Getter for message ID
    public Long getId() {
        return id;
    }

    /**
     * Returns the formatted time string (HH:mm) for display
     */
    public String getFormattedTime() {
        ZonedDateTime localTime = time.atZoneSameInstant(ZoneId.systemDefault());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        return localTime.format(formatter);
    }



    public StringProperty textProperty() {
        return text;
    }

}
