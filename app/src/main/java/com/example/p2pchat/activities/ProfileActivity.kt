package com.example.p2pchat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.example.p2pchat.MainActivity
import com.example.p2pchat.R
import com.example.p2pchat.adapters.NavigationBar
import com.example.p2pchat.adapters.ProfileView
import com.example.p2pchat.objects.User
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity() {

    var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        //get user data from intent
        val extras = getIntent().extras
        currentUser = extras?.getParcelable<User>("user")

        Log.d("ProfileActivity.kt", "Current user id: " + currentUser?.id)

        //set up the bottom navigation menu
        val navigationBar = NavigationBar(this)

        //set up the profile view
        val profileView = ProfileView(this, currentUser)

        //setup onclicks
        onClick()
    }

    fun onClick(){


        //sign out button onClick
        val signOutButton = findViewById<Button>(R.id.signOutButton)
        signOutButton.setOnClickListener{
            signOut()
        }
    }

    fun signOut(){
        //on signout completion, go to the login page
        FirebaseAuth.getInstance().addAuthStateListener {
            //if logged out, go to the login page
            if(it.currentUser == null){
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // ensure that this activity cannot be returned to by pressing the "back" button
            }
        }

        //actual signout
        FirebaseAuth.getInstance().signOut()
    }
}
