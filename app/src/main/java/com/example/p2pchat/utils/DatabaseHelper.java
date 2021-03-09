package com.example.p2pchat.utils;

import com.example.p2pchat.objects.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {
    private FirebaseFirestore db;

    public DatabaseHelper(){
        db = FirebaseFirestore.getInstance();
    }

    public void saveUser(User newUser){
        //get the current user id
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        Map<String, Object> user = new HashMap<>();
        user.put("username", newUser.getUsername());
        user.put("publicKey", newUser.getPublicKey());

        //used "set" to create document with specific id
        db.collection("users").document(userId).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(Task<Void> task) {
                if(task.isSuccessful()){

                }
            }
        });
    }
}
