package com.example.p2pchat.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.p2pchat.MainActivity;

import java.security.*;
import java.security.cert.X509Certificate;

import static android.content.Context.MODE_PRIVATE;

public class User {
    private String username;
    private Key publicKey;
    private Key privateKey;

    public User(){
        this.username = "";
        //this.keys = KeyPair();
    }

    public User(String username) throws NoSuchAlgorithmException {
        this.username = username;

        //generate the user keys
        KeyPair keys = generateKeyPair();
        this.publicKey = keys.getPublic();
        this.privateKey = keys.getPrivate();

        //get an X509 Certificate

    }

    //Generates public/private key pair and saves them to file
    //Only the public key is returned
    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        //Public and private key
        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();

        return kp;
    }

    /*
    private X509Certificate generateCertification(KeyPair keyPair){
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
        return certificate
    }
     */

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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
