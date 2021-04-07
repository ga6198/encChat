package com.example.p2pchat.utils;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.p2pchat.R;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.util.Map;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
 * In order to make this Java sample functional, you must remove the following from the Kotlin messaging
 * service in the AndroidManifest.xml:
 *
 * <intent-filter>
 *   <action android:name="com.google.firebase.MESSAGING_EVENT" />
 * </intent-filter>
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * When receiving a token, write it to the database?
     *
     * There are two scenarios when onNewToken is called:
     *  * 1) When a new token is generated on initial app startup
     *  * 2) Whenever an existing token is changed
     *  * Under #2, there are three scenarios when the existing token is changed:
     *  * A) App is restored to a new device
     *  * B) User uninstalls/reinstalls the app
     *  * C) User clears app data
     *
     * @param token
     */
    @Override
    public void onNewToken(@NonNull String token) {
        Log.d("FirebaseMessaging", "Refreshed token: " + token);

        //sendRegistrationToServer(token);
    }

    /**
     * When receiving a data payload, actually build the notification
     *
     * @param remoteMessage
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        Map<String, String> data = remoteMessage.getData();
        sendNotification(notification, data);
    }

    /**3
     * The function that actually builds and sends the notification
     * @param notification
     * @param data
     */
    private void sendNotification(RemoteMessage.Notification notification, Map<String, String> data){
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setSmallIcon(R.drawable.ic_action_chats);
                //.addAction(acceptAction)

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, notificationBuilder.build());
    }
}
