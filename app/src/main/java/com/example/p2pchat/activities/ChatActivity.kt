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
import android.database.DataSetObserver
import android.widget.AbsListView
import com.example.p2pchat.adapters.ChatMessageArrayAdapter
import com.example.p2pchat.objects.SessionKey
import com.example.p2pchat.utils.CryptoHelper
import com.example.p2pchat.utils.SharedPreferencesHandler
import com.google.firebase.firestore.Query
import java.security.PrivateKey
import java.security.PublicKey


class ChatActivity : AppCompatActivity() {

    var currentUser: User? = null
    var otherUser: User? = null
    var chatId: String? = null
    var secretKey: ByteArray? = null //holds the secret key the two users will use
    var sessionKeys: MutableList<SessionKey> = mutableListOf<SessionKey>() //holds all of the secret keys for this chat

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

        /*
        //establish the secret key for this chat
        val sharedPrefsHandler = SharedPreferencesHandler(this)
        currentUser?.privateKey = sharedPrefsHandler.getPrivateKey(currentUser?.id)
        secretKey = CryptoHelper.generateCommonSecretKey(currentUser?.privateKey as PrivateKey?, otherUser?.publicKey as PublicKey?)

         */

        /*
        chat adapter
        settings for how the chat messages are displayed
        */
        val chatArrayAdapter = setUpChatAdapter()


        //TODO: Sometimes, the chats load before the keys are retrieved, causing the program to crash

        //set up key listener
        if(currentUser != null && otherUser!= null) {
            loadSessionKeys(currentUser!!, otherUser!!, chat, chatArrayAdapter);
        }

        //set up chat message listener
        //setUpChatListener(chat, chatArrayAdapter)

        //onclicks
        if(currentUser != null && otherUser != null){
            onClick(currentUser!!, otherUser!!, chat)
        }
        else{
            Log.d("ChatActivity.kt", "currentUser or otherUser seem to be null")
        }
    }

    private fun setUpChatAdapter(): ChatMessageArrayAdapter{
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
        return chatArrayAdapter
    }

    //loads all existing secret keys and listens for new secret keys
    private fun loadSessionKeys(currentUser: User, otherUser: User, chat: Chat, chatArrayAdapter: ChatMessageArrayAdapter){
        val db = FirebaseFirestore.getInstance()
        val keysRef = db.collection("chats").document(chat.id).collection("sessionKeys")

        keysRef.orderBy("timeCreated", Query.Direction.ASCENDING).get().addOnSuccessListener{values ->

            //get the timestamps, and load the corresponding keys from SharedPreferences. Add them to the secretKeys HashMap
            //if the key does not exist, that means it is new and must be decrypted and saved to SharedPreferences
            //TODO: This may add values to the list multiple times. Make it so only keys not in the list are added
            //TODO: refactor so that this data is all retrieved when the page is loaded. On complete, set up the message listener. If a new key is added, reload the page
            for (doc in values!!){
                val keyData = doc.getData()

                val timeCreated = keyData["timeCreated"] as Timestamp?
                if(timeCreated != null){
                    //get the key from sharedPreferences
                    val sharedPrefsHandler = SharedPreferencesHandler(this)
                    val currentSessionKey = sharedPrefsHandler.getSessionKey(chat.id, timeCreated)
                    //if the session key is nonexistent, that means the key should be retrieved from the db
                    if(currentSessionKey == null){ //if(currentSessionKey.equals(byteArrayOf(0x00))){
                        //need to decrypt the key from the server
                        val encryptedSessionKey = keyData["sessionKey"] as String?

                        //need to get the private key from sharedPreferences to decrypt
                        val privateKey = sharedPrefsHandler.getPrivateKey(currentUser.id)

                        //TODO: this decryption won't work with regenerated keys. Maybe upload a field to each of the user's chats, saying the session keys must be regenerated
                        //TODO: add cloud functions? If a user uploads a new public key,
                        //decrypt the session key
                        try {
                            val sessionKeyBytes = CryptoHelper.decryptSessionKey(
                                encryptedSessionKey,
                                privateKey as PrivateKey
                            )

                            //add the key to the sessionKeys list
                            val sessionKey = SessionKey(chat.id, sessionKeyBytes, timeCreated)
                            if (!sessionKeys.contains(sessionKey)) {
                                sessionKeys.add(sessionKey)
                            }

                            //save the key to sharedPreferences
                            sharedPrefsHandler.saveSessionKey(chat.id, timeCreated, sessionKeyBytes)
                        }
                        catch(e: Exception){
                            //If this is run, it means that the person's account was regenerated
                            Log.d("loadSessionKeys()", "Certain session key decryptions skipped")
                        }
                    }
                    else{
                        val sessionKey = SessionKey(chat.id, currentSessionKey, timeCreated)
                        if(!sessionKeys.contains(sessionKey)) {
                            sessionKeys.add(sessionKey)
                        }
                    }
                }
            }

            //if there are no keys in the sessionKeys array, this means a new key must be generated for this chat
            if(sessionKeys.size == 0){
                //new session key data
                val sessionKey = CryptoHelper.generateSessionKey()
                val encryptedSessionKey = CryptoHelper.encryptSessionKey(sessionKey, otherUser.publicKey as PublicKey) //CryptoHelper.generateEncryptedSessionKey(otherUser.publicKey as PublicKey)

                //correct sessionKey collection references stored in keysRef variable
                val sessionKeyRef = keysRef.document() //auto-generated id
                val timeCreated = Timestamp.now()
                val sessionKeyData = hashMapOf(
                    "sessionKey" to encryptedSessionKey,
                    "uploader" to currentUser.id,
                    "decrypter" to otherUser.id, //the person who will use his private key to decrypt the session key
                    "timeCreated" to timeCreated
                )

                //upload the keys
                /*
                db.runTransaction{transaction ->
                    //save the session key data
                    transaction.set(sessionKeyRef, sessionKeyData)

                    //save the session key to sharedPreferences
                    val sharedPrefHandler = SharedPreferencesHandler(this)
                    sharedPrefHandler.saveSessionKey(chat.id, timeCreated, sessionKey)

                    //add the key to the sessionKeys list
                    val newSessionKey = SessionKey(chat.id, sessionKey, timeCreated)
                    if(!sessionKeys.contains(newSessionKey)) {
                        sessionKeys.add(newSessionKey)
                    }
                }.addOnSuccessListener {
                    //once the new key is retrieved,

                    //set up the chat listener after all keys are loaded
                    setUpChatListener(chat, chatArrayAdapter)
                }
                */
                setUpChatListener(chat, chatArrayAdapter)
            }
            else{
                //set up the chat listener after all keys are loaded
                setUpChatListener(chat, chatArrayAdapter)
            }
        }
    }

    //listen for the chat messages
    private fun setUpChatListener(chat: Chat, chatArrayAdapter: ChatMessageArrayAdapter){
        val db = FirebaseFirestore.getInstance()
        val messagesRef = db.collection("chats")
            .document(chat.id)
            .collection("messages")

        //load messages/set adapter for chat messages
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

                val messageTime = messageData["time"] as Timestamp?
                //select the correct session key by looking at the timestamp
                val currentSessionKey = CryptoHelper.selectSessionKey(sessionKeys, messageTime)

                //decrypt message
                var decryptedMessage = ""
                //if the key is valid, decrypt the message
                if(messageData["message"] != null && currentSessionKey != null) {
                    decryptedMessage = CryptoHelper.decryptMessage(messageData["message"] as String, currentSessionKey.sessionKey); //decryptedMessage = CryptoHelper.decryptMessage(messageData["message"] as String, secretKey)
                }
                //if the key is not valid, just display the encrypted message
                else if(messageData["message"] != null){
                    decryptedMessage = messageData["message"] as String
                }

                val message = ChatMessage(
                    messageData["senderId"] as String?,
                    decryptedMessage, //messageData["message"] as String?,
                    messageTime,
                    ownMessage
                )

                //if the adapter does not contain the message, then add
                if(!chatArrayAdapter.contains(message)){
                    chatArrayAdapter.add(message)
                }
            }
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

    //TODO: refactor this to pass in the current secret key. The latest key should be the last one in the list
    private fun sendMessage(message: String, currentUser: User, otherUser: User, chat: Chat){
        //encrypt the message
        //val encryptedMessage = encryptMessage(message, currentUser, otherUser)

        //get the last key
        val currentSessionKey = sessionKeys.last()
        val encryptedMessage = CryptoHelper.encryptMessage(message, currentSessionKey.sessionKey) //val encryptedMessage = CryptoHelper.encryptMessage(message, secretKey)

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
        val encryptedMessage = Base64.getEncoder().encodeToString(encryptedMessageBytes)

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
