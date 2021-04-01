package com.example.p2pchat.objects;

public class NotificationData {
    private String title;
    private String message;

    public NotificationData(String title, String message){
        setTitle(title);
        setMessage(message);
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }
}
