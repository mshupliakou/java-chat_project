package com.example.javachat_project;

public class Message {
    private String text;
    private boolean fromMe;

    public Message(String text, boolean fromMe) {
        this.text = text;
        this.fromMe = fromMe;
    }
    public String getText() { return text; }
    public boolean isFromMe() { return fromMe; }
}
