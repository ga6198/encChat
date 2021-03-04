package com.example.p2pchat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.example.p2pchat.R
import com.example.p2pchat.utils.User

class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val extras = getIntent().extras

        //get current user data
        val currentUsername = extras?.getString("username")
        val currentId = extras?.getString("userId")
        val currentUser = User()
        currentUser.id = currentId
        currentUser.username = currentUsername

        //get other user data
        val otherUsername = extras?.getString("otherUsername")
        val otherId = extras?.getString("otherUserId")
        val otherUser = User()
        otherUser.id = otherId
        currentUser.username = otherUsername

        //change the header for the chat
        val usernameTextView = findViewById<TextView>(R.id.usernameText)
        usernameTextView.setText(otherUsername)

        //load messages/set adapter for messages
    }
}
