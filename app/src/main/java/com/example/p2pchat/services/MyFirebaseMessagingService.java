package com.example.p2pchat.services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.p2pchat.R;
import com.example.p2pchat.utils.Constants;
import com.example.p2pchat.utils.CryptoHelper;
import com.example.p2pchat.utils.SharedPreferencesHandler;
import com.google.firebase.Timestamp;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * NOTE: There can only be one service in each app that receives FCM messages. If multiple
 * are declared in the Manifest then the first one will be chosen.
 *
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

        //for push challenge notifications
        if("true".equals(data.get("approved"))){
            sendChallengeNotification(notification, data);
        }

        //for push challenge response notifications
        if("true".equals(data.get("challengeResponse"))){
            sendChallengeResponseNotification(notification, data);
        }
    }

    /**
     * The function that actually builds and sends the notification
     * @param notification
     * @param data
     */
    private void sendChallengeNotification(RemoteMessage.Notification notification, Map<String, String> data){
        //generate a notification id
        int notifId = createNotifId();

        //Pending intent for push challenge approval
        PendingIntent acceptPendingIntent = PendingIntent.getBroadcast(
                this,
                    Constants.REQUEST_CODE_APPROVE,
                new Intent(this, NotificationReceiver.class)
                        .putExtra(Constants.KEY_INTENT_APPROVE, Constants.REQUEST_CODE_APPROVE)
                        .putExtra("userId", data.get("receiverId"))
                        .putExtra("otherUserId", data.get("senderId"))
                        .putExtra("challenge", data.get("challenge"))
                        .putExtra("notificationId", notifId)
                        .putExtra("challengeId", data.get("challengeId"))
                        .putExtra("chatId", data.get("chatId"))
                        .putExtra("time", data.get("time")),
                PendingIntent.FLAG_UPDATE_CURRENT
        );

        //action to approve the push challenge
        NotificationCompat.Action acceptAction =
                new NotificationCompat.Action.Builder(R.id.action_chats,
                        "Accept", acceptPendingIntent)
        //.addRemoteInput(remoteInput)
                .build();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setSmallIcon(R.drawable.ic_action_chats)
                .addAction(acceptAction);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifId, notificationBuilder.build());
    }

    /**
     * The function that shows notifications once a challenge was approved inside the application
     * @param notification
     * @param data
     */
    private void sendChallengeResponseNotification(RemoteMessage.Notification notification, Map<String, String> data){
        //check if the received challenge data is correct
        String challengeResponseData = data.get("challengeResponseData");
        String userId = data.get("receiverId");
        SharedPreferencesHandler sharedPrefsHandler = new SharedPreferencesHandler(this);
        /*
        Key privateKey = null;
        try {
            privateKey = sharedPrefsHandler.getPrivateKey(userId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String challengeResult = CryptoHelper.decryptChallenge(challengeResponseData, (PrivateKey) privateKey);

         */

        //need to check if the challenge result is the same as the one that was sent
        String timeString = data.get("time");
        Timestamp time = new Timestamp(Integer.parseInt(timeString), 0);
        String chatId = data.get("chatId");
        String sentChallengeValue = sharedPrefsHandler.getChallengeValue(chatId);
        byte[] key = sharedPrefsHandler.getCorrespondingChatKey(chatId, time);

        String challengeResult = CryptoHelper.decryptMessage(challengeResponseData, key);

        //building notification message body
        String otherUsername = data.get("senderUsername");
        String messageBody = "";
        if(sentChallengeValue.equals(challengeResult)){
            messageBody = otherUsername + " correctly responded to your push";
        }
        else{
            messageBody = otherUsername + " incorrectly responded to your push";
        }

        //generate a notification id
        int notifId = createNotifId();

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, Constants.CHANNEL_ID)
                .setContentTitle(notification.getTitle())
                .setContentText(messageBody) //.setContentText(notification.getBody())
                .setSmallIcon(R.drawable.ic_action_chats);

        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(notifId, notificationBuilder.build());
    }

    /**
     * Generate a notification id based on the timestamp
     * @return
     */
    private int createNotifId(){
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss").format(now));
        return id;
    }
}
