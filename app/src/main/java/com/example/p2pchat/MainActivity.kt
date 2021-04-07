package com.example.p2pchat

import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.p2pchat.activities.HomePageActivity
import com.example.p2pchat.activities.RegistrationActivity
import com.example.p2pchat.utils.AESAlg
import com.example.p2pchat.utils.CryptoHelper

import com.example.p2pchat.objects.User
import com.example.p2pchat.utils.SharedPreferencesHandler
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.messaging.FirebaseMessaging
import java.security.Key
import java.util.*
import android.app.NotificationManager
import android.app.NotificationChannel
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.example.p2pchat.utils.Constants


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //initialize notifications
        createNotificationChannel()

        //set initial firebase settings
        //stop deleted documents from caching
        val db = FirebaseFirestore.getInstance()
        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(false)
            .build()
        db.firestoreSettings = settings

        checkIfLoggedIn()

        //set onclicks
        //onClick()
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val description = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                Constants.CHANNEL_ID, name,
                importance
            )
            channel.description = description
            // Register the channel with the system; you can't change the importance
            // or other notification behaviours after this
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    //check if user is already logged in
    private fun checkIfLoggedIn(){
        //if the user already is logged in, directly go. to the next page
        val user = FirebaseAuth.getInstance().currentUser
        if(user != null){
            // user is signed in

            // automatically bring the user to the next page

            // for accounts created with FirebaseAuth.signInWithCredential(AuthCredential)
            if (user.displayName != "" && user.displayName != null){
                loadPublicKeys(user!!.uid, user.displayName!!)
            }
            // for accounts created with FirebaseAuth.createUserWithEmailAndPassword(String, String)
            else if(user.email != "" && user.email != null){
                loadPublicKeys(user!!.uid, user.email!!)
            }
        }
        else{
            // no user is signed in

            //set onclicks
            onClick()
        }
    }

    fun onClick(){
        //to registration page
        val registerButton = findViewById<TextView>(R.id.toRegister)
        registerButton.setOnClickListener{
            //Redirect to registration activity
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }

        setupLogin()
    }

    fun setupLogin(){
        val submitButton: Button = findViewById<Button>(R.id.submitButton)
        val usernameText: TextView = findViewById<TextView>(R.id.usernameText)
        val passwordText: TextView = findViewById<TextView>(R.id.passwordText)

        submitButton.setOnClickListener {
            val usernameInput = usernameText.text.toString()
            val passwordInput = passwordText.text.toString()

            if(usernameInput.isEmpty()){
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
            else if(!Patterns.EMAIL_ADDRESS.matcher(usernameInput).matches()){
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
            else if(passwordInput.isEmpty()){
                Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show()
            }
            else{
                val mAuth = FirebaseAuth.getInstance();
                mAuth.signInWithEmailAndPassword(usernameInput, passwordInput).addOnCompleteListener {
                    if(it.isSuccessful()){
                        val userId = FirebaseAuth.getInstance().currentUser!!.uid

                        Log.d("MainActivity.kt", "Current userId: ${userId}")
                        Log.d("MainActivity.kt", "Current username: ${usernameInput}")

                        //redirect to main page
                        //val intent = Intent(this, HomePageActivity::class.java)

                        //get public keys from shared preferences
                        loadPublicKeys(userId, usernameInput)
                    }
                    else{
                        Toast.makeText(this, "Login failed", Toast.LENGTH_SHORT).show()

                    }
                }
            }

        }
    }

    private fun loadPublicKeys(userId: String, usernameInput: String){
        val sharedPrefHandler = SharedPreferencesHandler(this)
        val userPubKey = sharedPrefHandler.getPublicKey(userId)
        val signedPrekeyPublic = sharedPrefHandler.getSignedPrekeyPublic(userId)
        var currentUser = User(userId, usernameInput, userPubKey, signedPrekeyPublic)

        //get device token and then login
        FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
            var userWithToken = currentUser
            userWithToken.deviceToken = token

            loginUser(currentUser)
        }
    }

    //actually moves to the next page
    private fun loginUser(user:User){
        var currentUser = user
        //redirect to main page
        val intent = Intent(this, HomePageActivity::class.java)

        //if the user is using a new device, need to create new public and private keys
        if(usingNewDevice(currentUser)){
            val db = FirebaseFirestore.getInstance()
            //user new key data
            val userData = hashMapOf<String, Any>(
                "publicKey" to currentUser.encodedPublicKey,
                "signedPrekeyPublic" to currentUser.encodedSignedPrekeyPublic,
                "deviceToken" to currentUser.deviceToken //new device means new device token as well
            )
            val userRef = db.collection("users").document(currentUser.id)

            db.runTransaction{transaction ->
                //recreate pub-prv keys
                currentUser = regenerateUser(currentUser)

                //upload to the new keys to the database
                transaction.update(userRef, userData)
            }.addOnSuccessListener{
                //go to the next page
                intent.putExtra("user", currentUser)
                startActivity(intent)
            }
                .addOnFailureListener{
                    Log.d("Regenerating user", "failed")
                }
        }
        //otherwise, if not using a new device, go directly to the next page
        else{
            intent.putExtra("user", currentUser)
            startActivity(intent)
        }
    }

    //check if the user is logging in from a new device (public keys, private keys should be null)
    fun usingNewDevice(user: User): Boolean{
        if (user.publicKey == null && user.signedPrekeyPublic == null){
            return true
        }
        return false
    }

    //resets the user's public and private keys, and saves them to shared preferences
    fun regenerateUser(user:User): User{
        //regenerates the public and private keys
        val regeneratedUser = User(user.username)
        regeneratedUser.id = user.id
        regeneratedUser.regenerated = true

        //save all the information to shared preferences
        val sharedPrefHandler = SharedPreferencesHandler(this)
        sharedPrefHandler.saveUser(regeneratedUser)

        return regeneratedUser
    }

    //ensures that pressing the "back" button will close the app instead of going to previous activities
    override fun onBackPressed() {
        moveTaskToBack(true);
    }
}
