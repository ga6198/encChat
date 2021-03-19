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
import com.example.p2pchat.utils.CryptoHelper
import com.example.p2pchat.utils.RSAAlg
import com.example.p2pchat.utils.SecretKeyAlg
import com.example.p2pchat.utils.SharedPreferencesHandler
import kotlinx.android.synthetic.main.chat_preview.*
import java.security.PrivateKey
import java.security.PublicKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey


class ChatActivity : AppCompatActivity() {

    var currentUser: User? = null
    var otherUser: User? = null
    var chatId: String? = null
    var secretKey: ByteArray? = null //holds the secret key the two users will use

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val extras = getIntent().extras

        //get chat data
        chatId = extras?.getString("chatId")
        val chat = Chat(chatId)

        //get current user data
        currentUser = extras?.getParcelable<User>("user")
        //get other user data
        otherUser = extras?.getParcelable<User>("otherUser")

        //change the header for the chat
        val usernameTextView = findViewById<TextView>(R.id.usernameText)
        usernameTextView.setText(otherUser?.username) //usernameTextView.setText(otherUsername)

        //establish the secret key for this chat
        val sharedPrefsHandler = SharedPreferencesHandler(this)
        currentUser?.privateKey = sharedPrefsHandler.getPrivateKey(currentUser?.id)
        secretKey = CryptoHelper.generateCommonSecretKey(currentUser?.privateKey as PrivateKey?, otherUser?.publicKey as PublicKey?)

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

        //load messages/set adapter for NEW messages
        //This adapter only needs to update for each SINGLE NEW message

        messagesRef.orderBy("time").addSnapshotListener{ value, error ->
            if (error != null) {
                Log.w("ChatActivity.kt", "Listen failed.", error)
                return@addSnapshotListener
            }

            for (doc in value!!){

                val messageData = doc.getData()
                var ownMessage = false
                if(messageData["senderId"]?.equals(currentUser?.id)!!){
                    ownMessage = true
                }

                //decrypt message
                var decryptedMessage = ""
                if(messageData["message"] != null) {
                    decryptedMessage = CryptoHelper.decryptMessage(messageData["message"] as String, secretKey)
                }

                val message = ChatMessage(
                    messageData["senderId"] as String?,
                    decryptedMessage, //messageData["message"] as String?,
                    messageData["time"] as Timestamp?,
                    ownMessage
                )

                //if the adapter does not contain the message, then add
                if(!chatArrayAdapter.contains(message)){
                    chatArrayAdapter.add(message)
                }
            }
        }

        //onclicks
        if(currentUser != null && otherUser != null){
            onClick(currentUser!!, otherUser!!, chat)
        }
        else{
            Log.d("ChatActivity.kt", "currentUser or otherUser seem to be null")
        }
    }

    private fun onClick(currentUser: User, otherUser: User, chat: Chat){
        //send button
        val sendButton = findViewById<ImageButton>(R.id.sendButton)
        val sendText = findViewById<EditText>(R.id.sendText)

        sendButton.setOnClickListener{
            val sendInput = sendText.text.toString()

            //if there was text given
            if (sendInput != ""){
                sendMessage(sendInput, currentUser, otherUser, chat)

                //reset text
                sendText.setText("")
            }
        }
    }
    
    private fun sendMessage(message: String, currentUser: User, otherUser: User, chat: Chat){
        //encrypt the message
        //val encryptedMessage = encryptMessage(message, currentUser, otherUser)
        val encryptedMessage = CryptoHelper.encryptMessage(message, secretKey)

        //data, like time, author
        val currentTime = Timestamp.now()

        val messageData = hashMapOf<String, Any>(
            "time" to currentTime,
            "message" to encryptedMessage,
            "senderId" to currentUser.id,
            "senderUsername" to currentUser.username
        )

        //create another map with the same data for saving to the usersChats collection
        val currentUserLastMessageData = hashMapOf<String, Any>(
            "lastMessageTime" to currentTime,
            "lastMessage" to encryptedMessage,
            "lastSenderId" to currentUser.id,
            "lastUsername" to currentUser.username,
            "currentUserId" to currentUser.id,
            "otherUserId" to otherUser.id,
            "otherUserUsername" to otherUser.username,
            "otherUserPublicKey" to otherUser.encodedPublicKey
        )

        val otherUserLastMessageData = hashMapOf<String, Any>(
            "lastMessageTime" to currentTime,
            "lastMessage" to encryptedMessage,
            "lastSenderId" to currentUser.id,
            "lastUsername" to currentUser.username,
            "currentUserId" to otherUser.id,
            "otherUserId" to currentUser.id,
            "otherUserUsername" to currentUser.username,
            "otherUserPublicKey" to currentUser.encodedPublicKey
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
    }

    /*
    private fun encryptMessage(message: String): String{
        //perform the encryption
        val secretKeyAlg = SecretKeyAlg(secretKey)
        val encryptedMessageBytes = secretKeyAlg.encrypt(message).get("ciphertext")!!;
        val encryptedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes) //TODO: might need to base64 encode

        println("Encrypted Message: ${encryptedMessage}")

        return encryptedMessage;
    }

     */

    /*
    private fun encryptMessage(message: String, currentUser: User, otherUser: User): String{
        //get the receiver's public key
        val receiverPubKey = otherUser.publicKey as RSAPublicKey

        //useful debugging information
        val pubKeyLength = receiverPubKey.modulus.bitLength()
        println("Public Key Length: ${pubKeyLength}")
        println("Message Length in Bytes: ${message.toByteArray().size}")

        //perform the RSA double encryption
        val rsaAlg = RSAAlg(receiverPubKey)
        val encryptedMessageBytes = rsaAlg.encrypt(message).get("ciphertext")!!;
        val encryptedMessage = String(encryptedMessageBytes)

        return encryptedMessage;
    }

     */

    /*
    private fun encryptMessage(message: String, currentUser: User, otherUser: User): String{
        //get the receiver's public key
        val receiverPubKey = otherUser.publicKey as RSAPublicKey

        //useful debugging information
        val pubKeyLength = receiverPubKey.modulus.bitLength()
        println("Public Key Length: ${pubKeyLength}")
        println("Message Length in Bytes: ${message.toByteArray().size}")

        //perform the RSA double encryption
        val rsaAlg = RSAAlg(receiverPubKey)
        val encryptedMessageBytes = rsaAlg.encrypt(message).get("ciphertext")!!;
        val encryptedMessage = String(encryptedMessageBytes)

        return encryptedMessage;
    }
     */

    /*
    private fun decryptMessage(encodedMessage: String): String{
        //base 64 decode the message
         val message = Base64.getDecoder().decode(encodedMessage)

        val secretKeyAlg = SecretKeyAlg(secretKey)
        val plaintext = secretKeyAlg.decrypt(message) //secretKeyAlg.decrypt(message.toByteArray())

        return plaintext
    }

     */

    //old function, used with double encryption scheme
/*
    private fun encryptMessage(message: String, currentUser: User, otherUser: User): String{
        //get the sender's private key.
        val sharedPrefsHandler = SharedPreferencesHandler(this)
        val senderPrvKey = sharedPrefsHandler.getPrivateKey(currentUser.id) as RSAPrivateKey

        //get the receiver's public key
        val receiverPubKey = otherUser.publicKey as RSAPublicKey

        //useful debugging information
        val prvKeyLength = senderPrvKey.modulus.bitLength()
        val pubKeyLength = receiverPubKey.modulus.bitLength()
        println("Private Key Length: ${prvKeyLength}")
        println("Public Key Length: ${pubKeyLength}")
        println("Message Length in Bytes: ${message.toByteArray().size}")

        //perform the RSA double encryption
        val rsaAlg = RSAAlg(receiverPubKey, senderPrvKey)
        val encryptedMessageBytes = rsaAlg.encrypt(message).get("ciphertext")!!;
        val encryptedMessage = String(encryptedMessageBytes)

        return encryptedMessage;
    }

 */
}
