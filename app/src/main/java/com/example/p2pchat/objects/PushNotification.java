package com.example.p2pchat.objects;

public class PushNotification {
    private NotificationData data;
    private String recipient;

    PushNotification(NotificationData data, String recipient){
        setData(data);
        setRecipient(recipient);
    }

    public void setData(NotificationData data) {
        this.data = data;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public NotificationData getData() {
        return data;
    }

    public String getRecipient() {
        return recipient;
    }
}
