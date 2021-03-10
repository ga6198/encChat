package com.example.p2pchat.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import com.example.p2pchat.R
import com.example.p2pchat.objects.Chat
import com.example.p2pchat.objects.ChatMessage
import com.example.p2pchat.objects.User
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*
import android.database.DataSetObserver
import android.widget.AbsListView
import com.example.p2pchat.adapters.ChatMessageArrayAdapter


class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val extras = getIntent().extras

        //get chat data
        val chatId = extras?.getString("chatId")
        val chat = Chat(chatId)

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
        otherUser.username = otherUsername

        //change the header for the chat
        val usernameTextView = findViewById<TextView>(R.id.usernameText)
        usernameTextView.setText(otherUsername)

        /*
        chat adapter
        */
        val chatArrayAdapter = ChatMessageArrayAdapter(
            applicationContext,
            R.layout.my_message
        )
        val listView = findViewById(R.id.messages_view) as ListView
        listView.adapter = chatArrayAdapter
        listView.transcriptMode = AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL
        listView.adapter = chatArrayAdapter
        //to scroll the list view to bottom on data change
        chatArrayAdapter.registerDataSetObserver(object : DataSetObserver() {
            override fun onChanged() {
                super.onChanged()
                listView.setSelection(chatArrayAdapter.count - 1)
            }
        })

        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("chats")
            .document(chat.id)
            .collection("messages")

        /*
        //load existing messages
        messagesRef.get().addOnCompleteListener{task->
            if(task.isSuccessful()){
                val result = task.result?.documents
                for (doc in result!!){
                    val messageData = doc.getData()
                    var ownMessage = false
                    if(messageData?.get("senderId")?.equals(currentUser.id)!!){
                        ownMessage = true
                    }

                    val message = ChatMessage(
                        messageData["senderId"] as String?,
                        messageData["message"] as String?, messageData["time"] as Timestamp?,
                        ownMessage
                    )
                    //messages.add(message)
                    chatArrayAdapter.add(message)
                }
            }
            else{
                Log.d("ChatActivity.kt", "Messages were not loaded properly")
            }
        }

         */


        //load messages/set adapter for NEW messages
        //This adapter only needs to update for each SINGLE NEW message

        messagesRef.orderBy("time").addSnapshotListener{ value, error ->
            if (error != null) {
                Log.w("ChatActivity.kt", "Listen failed.", error)
                return@addSnapshotListener
            }

            /*
            if (value != null) {
                if(!value.isEmpty) {
                    val newDoc = value.documents[0]
                    val messageData = newDoc.getData()
                    var ownMessage = false
                    if (messageData?.get("senderId")?.equals(currentUser.id)!!) {
                        ownMessage = true
                    }

                    val message = ChatMessage(
                        messageData["senderId"] as String?,
                        messageData["message"] as String?, messageData["time"] as Timestamp?,
                        ownMessage
                    )

                    //TODO: modify this to add the newest message only
                    chatArrayAdapter.add(message)

                }
            }

             */

            //array of messages, in time order
            val messages = ArrayList<ChatMessage>()


            for (doc in value!!){

                val messageData = doc.getData()
                var ownMessage = false
                if(messageData["senderId"]?.equals(currentUser.id)!!){
                    ownMessage = true
                }

                val message = ChatMessage(
                    messageData["senderId"] as String?,
                    messageData["message"] as String?, messageData["time"] as Timestamp?,
                    ownMessage
                )
                //messages.add(message)

                //if the adapter does not contain the message, then add
                if(!chatArrayAdapter.contains(message)){
                    chatArrayAdapter.add(message)
                }
            }
        }


        //onclicks
        onClick(currentUser, otherUser, chat)
    }

    fun onClick(currentUser: User, otherUser: User, chat: Chat){
        //send button
        val sendButton = findViewById<ImageButton>(R.id.sendButton)
        val sendText = findViewById<EditText>(R.id.sendText)

        sendButton.setOnClickListener{
            val sendInput = sendText.text.toString()

            //if there was text given
            if (sendInput != ""){
                //data, like time, author
                //val date = Calendar.getInstance().time
                val currentTime = Timestamp.now()

                val messageData = hashMapOf<String, Any>(
                    "time" to currentTime,
                    "message" to sendInput,
                    "senderId" to currentUser.id,
                    "senderUsername" to currentUser.username
                )

                //create another map with the same data for saving to the usersChats collection
                val currentUserLastMessageData = hashMapOf<String, Any>(
                    "lastMessageTime" to currentTime,
                    "lastMessage" to sendInput,
                    "lastSenderId" to currentUser.id,
                    "lastUsername" to currentUser.username,
                    "currentUserId" to currentUser.id,
                    "otherUserId" to otherUser.id,
                    "otherUserUsername" to otherUser.username
                )

                val otherUserLastMessageData = hashMapOf<String, Any>(
                    "lastMessageTime" to currentTime,
                    "lastMessage" to sendInput,
                    "lastSenderId" to currentUser.id,
                    "lastUsername" to currentUser.username,
                    "currentUserId" to otherUser.id,
                    "otherUserId" to currentUser.id,
                    "otherUserUsername" to currentUser.username
                )

                //generate a new document reference for a message, with a new id
                val db = FirebaseFirestore.getInstance()
                val newMessageRef = db.collection("chats").document(chat.id).collection("messages").document()
                val newMessageId = newMessageRef.id

                //write the message to the database
                val usersRef = db.collection("users")
                val currentUserRef = usersRef.document(currentUser.id).collection("usersChats").document(chat.id)
                val otherUserRef = usersRef.document(otherUser.id).collection("usersChats").document(chat.id)

                db.runTransaction{transaction ->
                    //write the message to the chats collection
                    transaction.set(newMessageRef, messageData)

                    //save the last chat message data for users
                    transaction.update(currentUserRef, currentUserLastMessageData)
                    transaction.update(otherUserRef, otherUserLastMessageData)
                }

                //write the message to the database
                /*
                val db = FirebaseFirestore.getInstance()
                db.collection("chats")
                    .document(chat.id)
                    .collection("messages")
                    .add(messageData) //no oncomplete really necessary for this

                 */

                //reset text
                sendText.setText("")
            }
        }
    }
}
