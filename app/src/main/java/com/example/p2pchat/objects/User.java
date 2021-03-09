package com.example.p2pchat.objects;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.p2pchat.MainActivity;
import com.example.p2pchat.utils.CryptoHelper;

import java.security.*;
import java.security.cert.X509Certificate;

import static android.content.Context.MODE_PRIVATE;

public class User {
    private String id;
    private String username;
    private String password;
    private Key publicKey;
    private Key privateKey;

    public User(){
        this.username = "";
        //this.keys = KeyPair();
    }

    public User(String username) throws NoSuchAlgorithmException {
        this.username = username;

        //generate the user keys
        KeyPair keys = CryptoHelper.generateKeyPair();
        this.publicKey = keys.getPublic();
        this.privateKey = keys.getPrivate();

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Key getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(Key privateKey) {
        this.privateKey = privateKey;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(Key publicKey) {
        this.publicKey = publicKey;
    }
}
