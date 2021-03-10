package com.example.p2pchat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.p2pchat.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import android.util.Log
import android.widget.AbsListView
import android.widget.ListView
import com.example.p2pchat.adapters.ChatListArrayAdapter
import com.example.p2pchat.adapters.NavigationBar
import com.example.p2pchat.objects.Chat
import com.example.p2pchat.utils.CryptoHelper
import com.example.p2pchat.utils.KeyType
import com.example.p2pchat.objects.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Transaction
import java.util.*

class HomePageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        //get current user data
        val extras = getIntent().extras
        val currentUsername = extras?.getString("username")
        val currentId = extras?.getString("userId")
        val currentUser = User()
        currentUser.id = currentId
        currentUser.username = currentUsername

        //Bottom navigation menu
        val navigationBar = NavigationBar(this)

        /*
        existing chats list adapter
         */
        val chatArrayAdapter = ChatListArrayAdapter(
            applicationContext,
            R.layout.chat_preview
        )
        val listView = findViewById(R.id.chats) as ListView
        listView.adapter = chatArrayAdapter
        listView.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        listView.adapter = chatArrayAdapter
        listView.setOnItemClickListener{parent, view, position, id ->
            val clickedChat = parent.getItemAtPosition(position) as Chat // the chat that was clicked

            //query for the other user's information and then open the chat in the onComplete
            val db = FirebaseFirestore.getInstance()
            db.collection("users").document(clickedChat.otherUserId).get().addOnCompleteListener{task->
                if(task.isSuccessful()){
                    val otherUserData = task.result
                    val otherUser = User()
                    if (otherUserData != null) {
                        otherUser.id = otherUserData.id
                    }
                    otherUser.username = otherUserData?.get("username") as String?

                    //open the chat
                    openChat(currentUser, otherUser, clickedChat.id)
                }
                else{
                    Log.d("HomePageActivity.kt", "Searching for other user failed")
                }
            }

        }

        //load the user's existing chats
        val db = FirebaseFirestore.getInstance()
        val chatsRef = db.collection("chats")
        val usersChatsRef = db.collection("users").document(currentUser.id).collection("usersChats")

        usersChatsRef.orderBy("lastMessageTime").get()
            .addOnCompleteListener{task->
                if(task.isSuccessful()){
                    val chats = task.result

                    for (doc in chats!!){
                        val chatData = doc.getData()


                        val chat = Chat(doc.id,
                            chatData["lastUsername"] as String?,
                            chatData["lastMessage"] as String?,
                            chatData["lastMessageTime"] as Timestamp?,
                            chatData["otherUserId"] as String?,
                            chatData["otherUserUsername"] as String?)

                        if(!chatArrayAdapter.contains(chat)){
                            //need to pass an onclick function
                            chatArrayAdapter.add(chat)
                        }
                    }
                }
                else{
                    Log.w("HomePageActivity.kt", "Chat retrieval failed", task.exception)
                }
            }

        /*
        usersChatsRef.orderBy("lastMessageTime").addSnapshotListener{value, error ->
                if (error != null) {
                    Log.w("HomePageActivity.kt", "Listen failed.", error)
                    return@addSnapshotListener
                }

                for (doc in value!!) {
                    //get the chat objects

                    val chatData = doc.getData()


                    val chat = Chat(doc.id,
                        chatData["lastUsername"] as String?,
                        chatData["lastMessage"] as String?,
                        chatData["lastMessageTime"] as Timestamp?,
                        chatData["otherUserId"] as String?,
                        chatData["otherUserUsername"] as String?)


                    //val chat = Chat(doc.id)

                    //TODO: Add chat to the adapter
                    if(!chatArrayAdapter.contains(chat)){
                        //need to pass an onclick function
                        chatArrayAdapter.add(chat)
                    }
                }

            }

         */

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

                            //save new chat document to the Chats collection
                            val timeCreated = Timestamp.now()
                            val chatData = hashMapOf(
                                "creator" to currentUser.id,
                                "timeCreated" to timeCreated,
                                "members" to hashMapOf(
                                    currentUser.id to true,
                                    otherUser.id to true
                                ) //listOf<String>(currentUser.id, otherUser.id)
                            )

                            //create a new chat reference
                            //the chat id is the combination of two user's ids
                            val newChatId = currentUser.id + otherUser.id
                            val newChatRef = chatsRef.document(newChatId)

                            //save the chat id for both users
                            val chatUploadData = hashMapOf(
                                "creator" to currentUser.id,
                                "chatId" to newChatId,
                                "timeCreated" to timeCreated
                            )
                            val usersRef = db.collection("users")
                            val currentUserRef = usersRef.document(currentUser.id).collection("usersChats").document(newChatId)
                            val otherUserRef = usersRef.document(otherUser.id).collection("usersChats").document(newChatId)

                            db.runTransaction{transaction ->
                                //add to the chats collection
                                transaction.set(newChatRef, chatData)

                                //save the chat data for users
                                transaction.set(currentUserRef, chatUploadData)
                                transaction.set(otherUserRef, chatUploadData)
                            }
                                .addOnCompleteListener{
                                    if(task.isSuccessful()){
                                        val chatId = newChatId //task.result?.id
                                        if (chatId != null) {
                                            openChat(currentUser, otherUser, chatId)
                                        }
                                        else{
                                            println("Chat was not found")
                                        }
                                    } else{
                                        Toast.makeText(this, "Could not create the chat", Toast.LENGTH_LONG).show()
                                    }
                                }
                        }
                        //chat already exists, so open it
                        else{
                            val chatId = returnedSnapshot.documents[0].id //just getting the first document, which should be the only chat
                            openChat(currentUser, otherUser, chatId)
                        }
                    }
                }
            }

    }

    fun openChat(currentUser: User, otherUser: User, chatId: String){
        val intent = Intent(this, ChatActivity::class.java)
        //id can be retrieved with the firebaseauth, but saving to the intent is fine as well

        intent.putExtra("userId", currentUser.id)
        intent.putExtra("username", currentUser.username)
        intent.putExtra("otherUserId", otherUser.id)
        intent.putExtra("otherUsername", otherUser.username)

        intent.putExtra("chatId", chatId)

        startActivity(intent)
    }
}