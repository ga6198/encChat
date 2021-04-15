package com.example.p2pchat.services;

import android.app.NotificationManager;
import android.app.RemoteInput;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.example.p2pchat.utils.Constants;
import com.example.p2pchat.utils.CryptoHelper;
import com.example.p2pchat.utils.KeyType;
import com.example.p2pchat.utils.SharedPreferencesHandler;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Time;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Activated when responding to a notification
 */
public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        /*
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);

        if(remoteInput != null){
            //update the notification to show that it has been responded to
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle("You have responded to the push challenge");
            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(0, mBuilder.build());
        }

         */

        // get the data, like notification id
        int notifId = intent.getIntExtra("notificationId", 0);
        final String challengeId = intent.getStringExtra("challengeId");
        String userId = intent.getStringExtra("userId");
        String encryptedChallenge = intent.getStringExtra("challenge");
        String otherUserId = intent.getStringExtra("otherUserId");
        String chatId = intent.getStringExtra("chatId");
        String timeString = intent.getStringExtra("time");
        Timestamp time = new Timestamp(Integer.parseInt(timeString), 0);

        // get the code for the the notification action
        int keyIntentApprove = intent.getIntExtra(Constants.KEY_INTENT_APPROVE, 0);

        // if the "approve" button was pressed
        if(keyIntentApprove == Constants.REQUEST_CODE_APPROVE){
            //update the notification to show that it has been responded to
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_menu_info_details)
                    .setContentTitle("You have responded to the push challenge");
            NotificationManager notificationManager = (NotificationManager) context.
                    getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notifId, mBuilder.build());

            //decrypt the value
            SharedPreferencesHandler sharedPrefsHandler = new SharedPreferencesHandler(context);
            /*
            Key privateKey = null;
            try {
                privateKey = sharedPrefsHandler.getPrivateKey(userId); //get the private key from sharedPreferences
            } catch (Exception e) {
                e.printStackTrace();
            }
            final String challengeString = CryptoHelper.decryptChallenge(encryptedChallenge, (PrivateKey) privateKey);

             */
            byte[] key = sharedPrefsHandler.getCorrespondingChatKey(chatId, time);
            final String challengeString = CryptoHelper.decryptMessage(encryptedChallenge, key);

            //encrypt and send to the original sender
            /*
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("users").document(otherUserId).get()
                    .addOnSuccessListener(
                        new OnSuccessListener<DocumentSnapshot>() {
                          @Override
                          public void onSuccess(DocumentSnapshot documentSnapshot) {
                              Map data = documentSnapshot.getData();
                              String encodedKey = (String)data.get("publicKey");
                              try {
                                  Key publicKey = CryptoHelper.decodeKey(encodedKey, KeyType.PUBLIC, "RSA");
                                  String encryptedChallenge = CryptoHelper.encryptChallenge(challengeString, (PublicKey) publicKey);

                                  uploadChallengeResponse(challengeId, encryptedChallenge);
                              } catch (Exception e) {
                                  e.printStackTrace();
                              }
                          }
                        }

                    );

            */
            String newlyEncryptedChallenge = CryptoHelper.encryptMessage(challengeString, key);
            uploadChallengeResponse(challengeId, newlyEncryptedChallenge);
        }
    }

    private void uploadChallengeResponse(String challengeId, String encryptedChallenge){
        // update the challenge document in the database to "approved"
        // Cloud Functions should send a notification to the first user
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference challengesRef = db.collection("challenges").document(challengeId);
        challengesRef.update(
                "approved", true,
                "challengeResponseData", encryptedChallenge)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("ChallengeApproval", "Error updating document", e);
                    }
                });
    }
}
