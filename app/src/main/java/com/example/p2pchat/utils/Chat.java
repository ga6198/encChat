package com.example.p2pchat.utils;

public class Chat {
    private String id;

    public Chat(){
        setId("");
    }

    public Chat(String id){
        setId(id);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
