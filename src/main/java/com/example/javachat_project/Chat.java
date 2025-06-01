package com.example.javachat_project;

public class Chat {
    private final int id;
    private final String login;
    private final String name;
    private final String last_name;


    Chat(int id , String login, String name, String lastName){
        this.id=id;
        this.login=login;
        this.name = name;
        last_name = lastName;
    }

    @Override
    public String toString() {
        return name+" "+last_name;
    }

    Chat get(){
        return this;
    }

    public String getLogin() {
        return login;
    }

    public int getOtherUserId() {
        return id;
    }
}
