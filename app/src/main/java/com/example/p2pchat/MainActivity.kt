package com.example.p2pchat

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.example.p2pchat.activities.HomePageActivity
import com.example.p2pchat.utils.AESAlg
import com.example.p2pchat.utils.CryptoHelper

import com.example.p2pchat.utils.User
import java.security.Key


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //get values from User Data from sharedpreferences
        val sharedPref = getSharedPreferences(
            getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        val pubKeyString = sharedPref.getString("public_key", null) //default value of null

        //if no data, createAccount()
        if (pubKeyString == null){
            createAccount(sharedPref)
        }

        //else if there is data, go to the home page
        else{
            println("There is user data")
        }
    }

    fun createAccount(sharedPref: SharedPreferences){
        val submitButton: Button = findViewById<Button>(R.id.submitButton)
        val usernameText: TextView = findViewById<TextView>(R.id.usernameText)

        submitButton.setOnClickListener{
            val usernameInput = usernameText.text.toString()

            //If username not provided
            if(usernameInput.isEmpty()){
                Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show()
            }
            //If username is provided, create the user w/ pub/pri key pair
            else{
                val newUser = User(usernameInput)

                //Save the user info to shared preferences
                saveToSharedPreferences(sharedPref, newUser)

                //Redirect to home page activity
                val intent = Intent(this, HomePageActivity::class.java)
                startActivity(intent)

            }
        }
    }

    fun saveToSharedPreferences(sharedPref: SharedPreferences, user: User){
        val editor = sharedPref.edit()
        editor.putString("username", user.username)

        //Keys need to be encoded before being saved
        val pubKeyStr = CryptoHelper.encodeKey(user.publicKey)
        val prvKeyStr = CryptoHelper.encodeKey(user.privateKey)

        //Private key needs to be encrypted before being saved
        val aes = AESAlg();



        //editor.putString("public_key", user.publicKey.toString())
        //editor.putString("private_key", newUser.privateKey.toString())
        //save private key to keystore

        editor.apply()
    }

    fun savePrivateKey(publicKey: Key, privateKey: Key){
        //KeyPair keyPair =
        //val certificate = generateCertificate(keyPair)
        //val certChain = arrayOfNulls<Certificate>(1)
        //certChain[0] = certificate

        //val ks = KeyStore.getInstance("AndroidKeyStore")
        //ks.setKeyEntry("private_key", key, null, certChain)


    }
}
