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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

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

        // get the notification id
        int notifId = intent.getIntExtra("notificationId", 0);

        String challengeId = intent.getStringExtra("challengeId");

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

            // update the challenge document in the database to "approved"
            // Cloud Functions should send a notification to the first user
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            DocumentReference challengesRef = db.collection("challenges").document(challengeId);
            challengesRef.update("approved", true)
            .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.w("ChallengeApproval", "Error updating document", e);
                }
            });
        }

        //update the challenge in the database to "approved"
    }
}
