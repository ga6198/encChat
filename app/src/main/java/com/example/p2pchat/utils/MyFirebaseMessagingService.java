package com.example.p2pchat.utils;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.messaging.FirebaseMessagingService;

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
}
