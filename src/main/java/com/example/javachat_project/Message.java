package com.example.javachat_project;

public class Message {
    private String text;
    private boolean fromMe;

    public String getSenderLogin() {
        return senderLogin;
    }

    private final String senderLogin;

    public Message(String text, boolean fromMe, String senderLogin) {
        this.text = text;
        this.fromMe = fromMe;
        this.senderLogin = senderLogin;
    }
    public String getText() { return text; }
    public boolean isFromMe() { return fromMe; }
}
