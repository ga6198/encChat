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
        Key publicKey = CryptoHelper.decodeKey(encodedPublicKey, KeyType.PUBLIC, Constants.identityKeyAlg);
        return publicKey;
    }

    public Key getPrivateKey(String userId) throws CertificateException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeySpecException {
        String encodedPrivateKey = sharedPref.getString(userId + "_private_key", "");
        Log.d("Encoded private key", encodedPrivateKey);
        Key privateKey = CryptoHelper.decodeKey(encodedPrivateKey, KeyType.PRIVATE, Constants.identityKeyAlg);

        return privateKey;
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
        Key publicKey = CryptoHelper.decodeKey(encodedPublicKey, KeyType.PUBLIC, Constants.signedPrekeyAlg);
        return publicKey;
    }

    public Key getSignedPrekeyPrivate(String userId) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String encodedPrivateKey = sharedPref.getString(userId + "_signed_prekey_private", "");
        Log.d("Encoded private key", encodedPrivateKey);
        Key privateKey = CryptoHelper.decodeKey(encodedPrivateKey, KeyType.PRIVATE, Constants.signedPrekeyAlg);
        return privateKey;
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
        //if you didn't get any value back
        if(encodedSessionKey.equals("")){
            return null; //return new byte[]{0x00}; //should be a 0 array
        }
        else{
            byte[] sessionKey = Base64.decode(encodedSessionKey, Base64.DEFAULT);
            return sessionKey;
        }
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
