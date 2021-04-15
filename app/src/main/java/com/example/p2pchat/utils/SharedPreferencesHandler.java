package com.example.p2pchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import com.example.p2pchat.objects.User;
import com.google.firebase.Timestamp;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 * Temporarily removing all things with the AES alg. Just saving private key directly
 *
 */

public class SharedPreferencesHandler {
    private Context context; //app context
    private SharedPreferences sharedPref; //actual sharedPreferences handler

    private final String SHARED_PREF_NAME = "p2pPreferences";

    public SharedPreferencesHandler(Context context){
        setContext(context);
        setSharedPref(context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE));

        //logging
        printAllSharedPreferences();
    }

    //used to check all shared preferences
    public void printAllSharedPreferences(){
        Map<String, ?> allEntries = sharedPref.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            Log.d("map values", entry.getKey() + ": " + entry.getValue().toString());
        }
    }

    public void saveUser(User user) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, CertificateException, KeyStoreException, IOException {
        SharedPreferences.Editor editor = sharedPref.edit();

        //Keys need to be encoded before being saved
        String pubKeyStr = CryptoHelper.encodeKey(user.getPublicKey());
        String prvKeyStr = CryptoHelper.encodeKey(user.getPrivateKey());

        //Save values to sharedpreferences
        editor.putString(user.getId() + "_username", user.getUsername());
        editor.putString(user.getId() + "_public_key", pubKeyStr);
        editor.putString(user.getId() + "_private_key", prvKeyStr);

        editor.apply();

        //logging
        System.out.println("username: " + user.getUsername());
        System.out.println("public_key: " + pubKeyStr);
        System.out.println("private_key: " + prvKeyStr);

        //save signed prekeys as well. Placed here because saving signedPrekeys may need to be done separately from saving the whole user
        saveSignedPrekeys(user);
    }

    //save both public and private prekeys
    public void saveSignedPrekeys(User user){
        saveSignedPrekeyPublic(user);
        saveSignedPrekeyPrivate(user);
    }

    public void saveSignedPrekeyPublic(User user){
        SharedPreferences.Editor editor = sharedPref.edit();
        //Keys need to be encoded before being saved
        String pubKeyStr = user.getEncodedSignedPrekeyPublic();

        //Save values to sharedPreferences
        editor.putString(user.getId() + "_signed_prekey_public", pubKeyStr);

        editor.apply();

        //logging
        System.out.println("signed_prekey_public: " + pubKeyStr);
    }

    public void saveSignedPrekeyPrivate(User user){
        SharedPreferences.Editor editor = sharedPref.edit();
        //Keys need to be encoded before being saved
        String prvKeyStr = user.getEncodedSignedPrekeyPrivate();

        //Save values to sharedPreferences
        editor.putString(user.getId() + "_signed_prekey_private", prvKeyStr);

        editor.apply();

        //logging
        System.out.println("signed_prekey_private: " + prvKeyStr);
    }

    /**
     * Saves a chat's session key. Needs a timestamp
     * @param chatId - the id of the chat the session key is used for
     * @param keyCreationTime - the Timestamp for when the session key was generated
     * @param sessionKey - the actual session key used for encrypting the chat
     */
    public void saveSessionKey(String chatId, Timestamp keyCreationTime, byte[] sessionKey){
        SharedPreferences.Editor editor = sharedPref.edit();
        //encode key
        String encodedKey = Base64.encodeToString(sessionKey, Base64.DEFAULT);

        //save to sharedPreferences. id is chat id + timestamp as seconds
        String sessionKeyId = chatId + "_" + keyCreationTime.getSeconds();
        editor.putString(sessionKeyId, encodedKey);
        editor.apply();
        Log.d("saved session key id", sessionKeyId);
    }

    /*
    public void saveUser(User user) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException, CertificateException, KeyStoreException, IOException {
        SharedPreferences.Editor editor = sharedPref.edit();

        //Keys need to be encoded before being saved
        String pubKeyStr = CryptoHelper.encodeKey(user.getPublicKey());
        String prvKeyStr = CryptoHelper.encodeKey(user.getPrivateKey());

        //Private key needs to be encrypted before being saved
        AESAlg aes = new AESAlg(user.getId());
        HashMap<String, byte[]> prvKeyEncryptions = aes.encrypt(prvKeyStr);
        byte[] iv = prvKeyEncryptions.get("iv");
        byte[] encryptedPrvKey = prvKeyEncryptions.get("ciphertext");
        //encode encrypted private key bytes to string again
        String ivStr = Base64.getEncoder().encodeToString(iv);
        String encryptedPrvKeyStr = Base64.getEncoder().encodeToString(encryptedPrvKey);

        //Save values to sharedpreferences
        editor.putString(user.getId() + "_username", user.getUsername());
        editor.putString(user.getId() + "_public_key", pubKeyStr);
        editor.putString(user.getId() + "_private_key", encryptedPrvKeyStr);
        editor.putString(user.getId() + "_private_key_iv", ivStr);

        editor.apply();

        //logging
        System.out.println("username: " + user.getUsername());
        System.out.println("public_key: " + pubKeyStr);
        System.out.println("private_key: " + prvKeyStr);
        System.out.println("private_key_iv: " + ivStr);
    }

    */

    /*
    TODO: if encodedPublicKey.equals(""), return null. If both public key is null, then regenerate the public/private keys and alert the user
    Maybe save the device token to the database. Would need to use Firebase Cloud Messaging
    Use kevinzhangdz for testing on the sub device
     */
    public Key getPublicKey(String userId) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String encodedPublicKey = sharedPref.getString(userId + "_public_key", "");
        Log.d("Encoded public key", encodedPublicKey);
        if(!encodedPublicKey.equals("")) {
            Key publicKey = CryptoHelper.decodeKey(encodedPublicKey, KeyType.PUBLIC, Constants.identityKeyAlg);
            return publicKey;
        }
        return null;
    }

    public Key getPrivateKey(String userId) throws CertificateException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeySpecException {
        String encodedPrivateKey = sharedPref.getString(userId + "_private_key", "");
        Log.d("Encoded private key", encodedPrivateKey);
        if(!encodedPrivateKey.equals("")) {
            Key privateKey = CryptoHelper.decodeKey(encodedPrivateKey, KeyType.PRIVATE, Constants.identityKeyAlg);
            return privateKey;
        }
        return null;
    }

    /*
    public Key getPrivateKey(String userId) throws CertificateException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeySpecException {
        //grab the private key iv from sharedpreferences
        String privateKeyIvStr = sharedPref.getString(userId + "_private_key_iv", "");
        byte[] privateKeyIv = Base64.getDecoder().decode(privateKeyIvStr);

        //get the AES key first
        AESAlg aes = new AESAlg(privateKeyIv, userId);

        //decrypt the privateKey with the AES key and iv
        String privateKeyEncStr = sharedPref.getString(userId + "_private_key", "");
        byte[] privateKeyCiphertext = Base64.getDecoder().decode(privateKeyEncStr);
        String privateKeyPlain = aes.decrypt(privateKeyCiphertext); //still base64 encoded
        //String base64EncodedPrivateKeyString = new String(privateKeyPlain, StandardCharsets.UTF_8);

        Log.d("Encoded private key", privateKeyPlain);

        //convert from base64 to Key form
        //Key privateKey = CryptoHelper.decodeKey(base64EncodedPrivateKeyString, KeyType.PRIVATE);
        Key privateKey = CryptoHelper.decodeKey(privateKeyPlain, KeyType.PRIVATE);
        return privateKey;
    }
     */

    public Key getSignedPrekeyPublic(String userId) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String encodedPublicKey = sharedPref.getString(userId + "_signed_prekey_public", "");
        Log.d("Encoded public key", encodedPublicKey);
        if(!encodedPublicKey.equals("")) {
            Key publicKey = CryptoHelper.decodeKey(encodedPublicKey, KeyType.PUBLIC, Constants.signedPrekeyAlg);
            return publicKey;
        }
        return null;
    }

    public Key getSignedPrekeyPrivate(String userId) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String encodedPrivateKey = sharedPref.getString(userId + "_signed_prekey_private", "");
        Log.d("Encoded private key", encodedPrivateKey);
        if(!encodedPrivateKey.equals("")) {
            Key privateKey = CryptoHelper.decodeKey(encodedPrivateKey, KeyType.PRIVATE, Constants.signedPrekeyAlg);
            return privateKey;
        }
        return null;
    }

    /**
     * Gets a chat's session key. Needs a timestamp
     * @param chatId - the id of the chat the session key is used for
     * @param keyCreationTime - the Timestamp for when the session key was generated
     * @return byte[] - the actual session key used for encrypting the chat
     */
    public byte[] getSessionKey(String chatId, Timestamp keyCreationTime){
        String encodedSessionKey = sharedPref.getString(chatId + "_" + keyCreationTime.getSeconds(), "");
        Log.d("Encoded session key", encodedSessionKey);
        //if you didn't get any value back, don't try to decode
        if(!encodedSessionKey.equals("")){
            byte[] sessionKey = Base64.decode(encodedSessionKey, Base64.DEFAULT);
            return sessionKey;
        }
        return null;
    }

    /**
     * Searches shared preference's for a chat's newest session key. Done by extracting the keyCreationTime and checking which one is the latest
     * @param chatId
     * @return
     */
    public byte[] getLatestChatKey(String chatId){
        Map<String, ?> allEntries = sharedPref.getAll();

        //get all the keys for the specific chat
        HashMap<String, String> chatKeys = new HashMap<String, String>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            //if the key is for the chatid, get it
            String keyId = entry.getKey();
            if(keyId.contains(chatId)){
                String encodedKey = (String)entry.getValue();

                //add the key and encodedKey to the keys map
                chatKeys.put(keyId, encodedKey);
            }
        }

        //search for the key with the latest timestamp
        String currentEncodedSessionKey = "";
        int maxTimeInSeconds = 0;
        for(Map.Entry<String, String> key : chatKeys.entrySet()){
            //extract timestamp, which is after the last semicolon
            String keyId = key.getKey();
            String timeInSecondsStr = keyId.substring(keyId.lastIndexOf("_") + 1);
            int timeInSeconds = Integer.parseInt(timeInSecondsStr);

            //The max timestamp should be the key that is used
            if(timeInSeconds > maxTimeInSeconds){
                maxTimeInSeconds = timeInSeconds;
                //update the key value that is currently being used
                currentEncodedSessionKey = key.getValue();
            }
        }

        if(!currentEncodedSessionKey.equals("")){
            byte[] sessionKey = Base64.decode(currentEncodedSessionKey, Base64.DEFAULT);
            return sessionKey;
        }

        return null;
    }

    /**
     * Searches shared preferences for a the correct session key based on the message time
     * @param chatId
     * @param messageTime
     */
    public byte[] getCorrespondingChatKey(String chatId, Timestamp messageTime){
        Map<String, ?> allEntries = sharedPref.getAll();

        //get all the keys for the specific chat
        HashMap<String, String> chatKeys = new HashMap<String, String>();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            //if the key is for the chatid, get it
            String keyId = entry.getKey();
            if(keyId.contains(chatId) && !keyId.contains("challenge")){
                String encodedKey = (String)entry.getValue();

                //add the key and encodedKey to the keys map
                chatKeys.put(keyId, encodedKey);
            }
        }

        //sort the hashmap, with oldest keys first
        List<String> keyIds = new ArrayList(chatKeys.keySet());
        Collections.sort(keyIds);

        // get the correct key
        // if only one key, use it
        for (int i = keyIds.size() - 1; i >= 0; i--){
            String keyId = keyIds.get(i);

            //extract the timestamp from the key
            String timeInSecondsStr = keyId.substring(keyId.lastIndexOf("_") + 1);
            int keyCreationTimeInSeconds = Integer.parseInt(timeInSecondsStr);

            //If message timestamp higher than the key timestamp, use it
            if(messageTime.getSeconds() >= keyCreationTimeInSeconds){
                String encodedSessionKey = chatKeys.get(keyId);
                if (!encodedSessionKey.equals("")) {
                    byte[] sessionKey = Base64.decode(encodedSessionKey, Base64.DEFAULT);
                    return sessionKey;
                }
                else{
                    return null;
                }
            }
        }
        return null;

        /*
        if(keyIds.size() == 1){
            String keyId = keyIds.get(0);
            String encodedSessionKey = chatKeys.get(keyId);
            if (!encodedSessionKey.equals("")) {
                byte[] sessionKey = Base64.decode(encodedSessionKey, Base64.DEFAULT);
                return sessionKey;
            }
        }
        // otherwise, if there are multiple keys, loop through
        else if (keyIds.size() > 1) {
            for (int i = 1; i < keyIds.size(); i++) {
                String keyId = keyIds.get(i);

                //extract the timestamp from the key
                String timeInSecondsStr = keyId.substring(keyId.lastIndexOf("_") + 1);
                int keyCreationTimeInSeconds = Integer.parseInt(timeInSecondsStr);

                //if the messageTime is less than the time the current key was created, use the previous key
                if (messageTime.getSeconds() < keyCreationTimeInSeconds) {
                    String previousKeyId = keyIds.get(i - 1);

                    String encodedSessionKey = chatKeys.get(previousKeyId);
                    if (!encodedSessionKey.equals("")) {
                        byte[] sessionKey = Base64.decode(encodedSessionKey, Base64.DEFAULT);
                        return sessionKey;
                    } else {
                        return null;
                    }
                }
            }
            //if it's the last key, and no key was returned prior, use it
            String lastKeyId = keyIds.get(keyIds.size() - 1);
            String encodedSessionKey = chatKeys.get(lastKeyId);
            if (!encodedSessionKey.equals("")) {
                byte[] sessionKey = Base64.decode(encodedSessionKey, Base64.DEFAULT);
                return sessionKey;
            } else {
                return null;
            }
        }

        return null;

         */
    }

    /**
     * Saves a push challenge value to sharedPreferences
     * @param chatId
     * @param challengeValue
     */
    public void saveChallengeValue(String chatId, String challengeValue){
        SharedPreferences.Editor editor = sharedPref.edit();

        //Save values to sharedpreferences
        editor.putString(chatId + "_challenge", challengeValue);

        editor.apply();

    }

    /**
     * Gets the last push challenge value
     * @param chatId
     * @return
     */
    public String getChallengeValue(String chatId){
        String challengeValue = sharedPref.getString(chatId + "_challenge", "");
        return challengeValue;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setSharedPref(SharedPreferences sharedPref) {
        this.sharedPref = sharedPref;
    }

    public Context getContext() {
        return context;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }
}
