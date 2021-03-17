package com.example.p2pchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.ContextMenu;

import com.example.p2pchat.objects.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import java.util.HashMap;
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

    public Key getPublicKey(String userId) throws InvalidKeySpecException, NoSuchAlgorithmException {
        String encodedPublicKey = sharedPref.getString(userId + "_public_key", "");
        Log.d("Encoded public key", encodedPublicKey);
        Key publicKey = CryptoHelper.decodeKey(encodedPublicKey, KeyType.PUBLIC);
        return publicKey;
    }

    public Key getPrivateKey(String userId) throws CertificateException, UnrecoverableEntryException, NoSuchAlgorithmException, KeyStoreException, IOException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, InvalidAlgorithmParameterException, NoSuchPaddingException, InvalidKeySpecException {
        String encodedPrivateKey = sharedPref.getString(userId + "_private_key", "");
        Log.d("Encoded private key", encodedPrivateKey);
        Key privateKey = CryptoHelper.decodeKey(encodedPrivateKey, KeyType.PRIVATE);

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
