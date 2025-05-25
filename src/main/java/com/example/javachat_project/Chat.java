package com.example.javachat_project;

public class Chat {
    private int id;
    private  String login;

    Chat(int id , String login){
        this.id=id;
        this.login=login;


    }

    @Override
    public String toString() {
        return login != null ? login : "Chat #" + id;
    }

}
