package com.example.p2pchat.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.example.p2pchat.MainActivity
import com.example.p2pchat.R
import com.example.p2pchat.utils.AESAlg
import com.example.p2pchat.utils.CryptoHelper
import com.example.p2pchat.utils.DatabaseHelper
import com.example.p2pchat.utils.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        //set onclicks
        onClick()
    }

    fun onClick(){
        //to registration page
        val loginButton = findViewById<TextView>(R.id.toLogin)
        loginButton.setOnClickListener{
            //Redirect to registration activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        //create account
        createAccount()
    }

    fun createAccount(){
        val submitButton: Button = findViewById<Button>(R.id.submitButton)
        val usernameText: TextView = findViewById<TextView>(R.id.usernameText)
        val passwordText: TextView = findViewById<TextView>(R.id.passwordText)
        val confirmPasswordText: TextView = findViewById<TextView>(R.id.confirmPasswordText)

        submitButton.setOnClickListener{
            val usernameInput = usernameText.text.toString()
            val passwordInput = passwordText.text.toString()
            val confirmPasswordInput = confirmPasswordText.text.toString()

            //If username not provided
            if(usernameInput.isEmpty()) {
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
            //email invalid format
            else if(!Patterns.EMAIL_ADDRESS.matcher(usernameInput).matches()){
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show()
            }
            //If password too short
            else if(passwordInput.length<6){
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()

            }
            //If passwords different
            else if(!passwordInput.equals(confirmPasswordInput)){
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
            }
            //If username is provided, create the user w/ pub/pri key pair
            else{
                var newUser = User(usernameInput)
                newUser.password = passwordInput

                //TODO: Upload user + public key to firebase
                registerUser(newUser)


                //Need to pass a user id, so sharedPref differs for users
                //Save the user info to shared preferences
                //saveToSharedPreferences(sharedPref, newUser)





                //Redirect to home page activity
                //val intent = Intent(this, HomePageActivity::class.java)
                //startActivity(intent)

            }
        }
    }

    //saves user to Firebase Auth
    fun registerUser(user: User){
        val mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(user.username, user.password).addOnCompleteListener {

            if(it.isSuccessful){
                //save the user to the database
                //val db = DatabaseHelper()
               // db.saveUser(user)
                saveUser(user)
            }
            else{
                Toast.makeText(this, "Failed to register", Toast.LENGTH_LONG).show()
            }

        }
    }

    //saves user document to Cloud Firestore
    fun saveUser(newUser: User) {
        //initialize Firestore
        val db = FirebaseFirestore.getInstance()

        //get the current user id
        val userId = FirebaseAuth.getInstance().currentUser!!.uid

        val user = hashMapOf<String, Any>(
            "username" to newUser.username,
            "publicKey" to CryptoHelper.encodeKey(newUser.publicKey)
        )

        //HashMap<String, Any>()
        //user["username"] = newUser.username
        //user["publicKey"] = CryptoHelper.encodeKey(newUser.publicKey)

        println(user["publicKey"])

        //used "set" to create document with specific id
        db.collection("users").document(userId).set(user)
            .addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registered successfully", Toast.LENGTH_LONG).show()

                    //Save the user's information to SharedPreferences, with Firebase userid
                    //get values from User Data from sharedpreferences
                    newUser.id = userId
                    val sharedPref = getSharedPreferences(
                        getString(R.string.preference_file_key), Context.MODE_PRIVATE)
                    saveToSharedPreferences(sharedPref, newUser)

                    //Redirect inside the app
                    val intent = Intent(this, HomePageActivity::class.java)
                    startActivity(intent)
                }
                else{
                    println(task.exception)
                    Toast.makeText(this, "Data upload failed", Toast.LENGTH_LONG).show()
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveToSharedPreferences(sharedPref: SharedPreferences, user: User){
        val editor = sharedPref.edit()

        //Keys need to be encoded before being saved
        val pubKeyStr = CryptoHelper.encodeKey(user.publicKey)
        val prvKeyStr = CryptoHelper.encodeKey(user.privateKey)

        //Private key needs to be encrypted before being saved
        val aes = AESAlg()
        val prvKeyEncryptions = aes.encrypt(prvKeyStr)
        val iv = prvKeyEncryptions.get("iv")
        val encryptedPrvKey = prvKeyEncryptions.get("ciphertext")
        //encode encrypted private key bytes to string again
        val ivStr = Base64.getEncoder().encodeToString(iv)
        val encryptedPrvKeyStr = Base64.getEncoder().encodeToString(encryptedPrvKey)

        //Save values to sharedpreferences
        editor.putString(user.id + "_username", user.username)
        editor.putString(user.id + "_public_key", pubKeyStr)
        editor.putString(user.id + "_private_key", encryptedPrvKeyStr)
        editor.putString(user.id + "_private_key_iv", ivStr)

        editor.apply()

        //logging
        println("username: ${user.username}")
        println("public_key: $pubKeyStr")
        println("private_key: $encryptedPrvKeyStr")
        println("private_key_iv: $ivStr")
    }
}
