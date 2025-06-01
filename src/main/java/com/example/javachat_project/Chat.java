package com.example.javachat_project;

public class Chat {
    private final int chatId;
    private final User otherUser;

    public Chat(int chatId, User otherUser) {
        this.chatId = chatId;
        this.otherUser = otherUser;
    }

    public int getChatId() {
        return chatId;
    }

    public User getOtherUser() {
        return otherUser;
    }

    @Override
    public String toString() {
        return otherUser.getName() + " " + otherUser.getLastName();
    }
}
