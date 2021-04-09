package com.example.p2pchat.utils;

public class Constants {
    private Constants() {
        // restrict instantiation
    }

    static public final String KEYSTORE_ALIAS = "AndroidKeyStore";
    static public final String identityKeyAlg = "RSA";
    static public final String signedPrekeyAlg = "RSA";

    //for firebase cloud messaging
    static public final String BASE_URL = "https://fcm.googleapis.com";
    static public final String SERVER_KEY = "AAAAZZ7okxg:APA91bHgScFcQ3HxNna6W9TgQm3flA8uYHpS_VszRDor3kfNq6RVYkYBt3mtu-QvBPLR58L8cxT-AORcXt2F5UNqMqOUAeSu9WvIW07Wt2juhJfvBnk1dsU4a_LqzOs3ihFxTajG4g6N";
    static public final String CONTENT_TYPE = "application/json";

    static public final String CHANNEL_ID = "notifications";

    //for accepting push challenges
    public static final String NOTIFICATION_REPLY = "notificationReply";
    public static final int REQUEST_CODE_APPROVE = 101;
    public static final String KEY_INTENT_APPROVE = "keyIntentAccept";
}

