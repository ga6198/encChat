package com.example.p2pchat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

//import com.example.p2pchat.R;
import android.content.res.Resources;
import android.util.Base64;

import com.example.p2pchat.R;

import java.net.*;
import java.io.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import java.security.*;
import java.lang.Exception;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class CryptoHelper {



    /**
     * Base64 encodes a public or private key
     * @param key - Any Key
     * @return String - The base64 encoded key.03
     */
    static public String encodeKey(Key key){
        try {
            byte[] keyBytes = key.getEncoded();
            String keyStr = Base64.encodeToString(keyBytes, Base64.DEFAULT);
            return keyStr;
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Base64 decodes an encoded key string
     * @param keyStr - Base64 encoded key string
     * @param keyType - PUBLIC or PRIVATE
     * @return Key - The public or private key
     */
    static public Key decodeKey(String keyStr, KeyType keyType) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            byte[] sigBytes = Base64.decode(keyStr, Base64.DEFAULT);

            X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sigBytes);
            KeyFactory keyFact = null;
            keyFact = KeyFactory.getInstance("RSA");

            Key key = null;
            //creates a public key
            if (keyType == KeyType.PUBLIC) {
                key = keyFact.generatePublic(x509KeySpec);
            }
            //creates a private key
            else if (keyType == KeyType.PRIVATE) {
                key = keyFact.generatePrivate(x509KeySpec);
            }
            return key;
        }
        catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }
    }

    //Generates public/private key pair
    static public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        //Public and private key
        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();

        return kp;
    }

    /*
    static public void saveKeysToSharedPreferences(Context context){
        //SharedPreferences sharedPref;
        //sharedPref = context.getSharedPreferences(
        //        getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    static public void savePrivateKey(){

    }

     */
}
