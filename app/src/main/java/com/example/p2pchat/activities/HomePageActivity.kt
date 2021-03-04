package com.example.p2pchat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.p2pchat.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import com.example.p2pchat.utils.CryptoHelper
import com.example.p2pchat.utils.KeyType
import com.example.p2pchat.utils.User


class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //set onclicks
        onClick()
    }

    fun onClick(){
        //dialog to add user
        val addButton: FloatingActionButton = findViewById<FloatingActionButton>(R.id.addChatButton)
        addButton.setOnClickListener{
            openDialog()
        }
    }

    //open the add user dialog
    fun openDialog(){
        val builder = AlertDialog.Builder(this)

        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.activity_add_user_dialog, null)

        builder.setView(view)
        builder.setTitle("Start a Chat")
        builder.setNegativeButton("Cancel"){dialog, which->
            //automatically closes dialog

        }

        builder.setPositiveButton("Confirm") { dialog, which ->
            //note how we specifically specified "view"
            val usernameText = view.findViewById<TextView>(R.id.usernameText)

            val usernameInput = usernameText.text.toString()


            //check if the current user is the same as the desired user
            val extras = getIntent().extras
            val currentUsername = extras?.getString("username")

            if (currentUsername.equals(usernameInput)){
                Toast.makeText(this, "Cannot choose yourself", Toast.LENGTH_LONG).show()
            }
            else {
                //open confirm user dialog
                findUser(usernameInput)
            }
        }

        builder.show()
    }

    //dialog for choosing user
    fun findUser(username: String){
        //initialize Firestore
        val db = FirebaseFirestore.getInstance()

        //get the user with that username
        db.collection("users").whereEqualTo("username", username)
            .get()
            .addOnCompleteListener{task->
                if(task.isSuccessful()){
                    //since it's email, there should only be one document
                    for (document in task.result!!) {
                        val userData = document.data

                        val userPublicKeyEncoded = userData.get("publicKey").toString()
                        val userPublicKey = CryptoHelper.decodeKey(userPublicKeyEncoded, KeyType.PUBLIC)

                        val user = User()
                        user.id = document.id
                        user.username = username
                        user.publicKey = userPublicKey

                        //open the alert dialog
                        openConfirmUserDialog(user)
                    }
                }
                else{
                    Toast.makeText(this, "Could not find the user", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun openConfirmUserDialog(user: User){
        val builder = AlertDialog.Builder(this)

        val inflater = this.layoutInflater
        val view = inflater.inflate(R.layout.activity_confirm_user_dialog, null)

        println(user.username)
        println(user.publicKey.toString())

        //change the view values
        val usernameDisplay = view.findViewById<TextView>(R.id.usernameText)
        usernameDisplay?.setText(user.username)

        //TODO: modify public key format. right now it displays object data
        val publicKeyDisplay = view.findViewById<TextView>(R.id.publicKeyText)
        publicKeyDisplay?.setText(user.publicKey.toString())

        builder.setView(view)
        builder.setTitle("Confirm User")
        builder.setNegativeButton("Cancel"){dialog, which->
            //automatically closes dialog

        }

        builder.setPositiveButton("Confirm") { dialog, which ->
            //initiate chat
            // follow the process on your proposal (start by getting the other user's keys

            //get current user data
            val extras = getIntent().extras
            val currentUsername = extras?.getString("username")
            val currentId = extras?.getString("userId")
            val currentUser = User()
            currentUser.id = currentId
            currentUser.username = currentUsername

            //for now, just directly opening a chat
            createChat(currentUser, user)
        }

        builder.show()
    }

    fun createChat(currentUser: User, otherUser: User){
        val db = FirebaseFirestore.getInstance()
        val chatsRef = db.collection("chats")

        //check if the chat exists
        //chatsRef.whereArrayContains("members", listOf<String>(currentUser.id, otherUser.id))
        chatsRef.whereEqualTo("members.${currentUser.id}", true)
            .whereEqualTo("members.${otherUser.id}", true)
            .get()
            .addOnCompleteListener{task->
                if(task.isSuccessful()){
                    val returnedSnapshot = task.result

                    if (returnedSnapshot != null) {
                        //if the chat does not exist, create it
                        if(returnedSnapshot.isEmpty()){
                            //save new message document to the database
                            val chatData = hashMapOf(
                                "members" to hashMapOf(
                                    currentUser.id to true,
                                    otherUser.id to true
                                ) //listOf<String>(currentUser.id, otherUser.id)
                            )
                            chatsRef.add(chatData)
                                .addOnCompleteListener{task->
                                    if(task.isSuccessful()){
                                        openChat(currentUser, otherUser)
                                    } else{
                                        Toast.makeText(this, "Could not create the chat", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                        //chat already exists, so open it
                        else{
                            openChat(currentUser, otherUser)
                        }
                    }
                }
            }

    }

    fun openChat(currentUser: User, otherUser: User){
        val intent = Intent(this, ChatActivity::class.java)
        //id can be retrieved with the firebaseauth, but saving to the intent is fine as well

        intent.putExtra("userId", currentUser.id)
        intent.putExtra("username", currentUser.username)
        intent.putExtra("otherUserId", otherUser.id)
        intent.putExtra("otherUsername", otherUser.username)

        startActivity(intent)
    }
}
