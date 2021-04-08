package com.example.p2pchat.objects;

import android.se.omapi.Session;

import com.google.firebase.Timestamp;

import java.util.Objects;

public class SessionKey {

    //uploader and decrypter values from database probably not needed

    String keyId;
    byte[] sessionKey; // should hold the decrypted version of the key
    Timestamp timeCreated;

    public SessionKey(){
        setKeyId("");
        setSessionKey("\0".getBytes()); //\0 should give an array with 0
        setTimeCreated(Timestamp.now());
    }

    public SessionKey(String chatId, byte[] sessionKey, Timestamp timeCreated){
        setKeyId(chatId + "_" + timeCreated.getSeconds());
        setSessionKey(sessionKey);
        setTimeCreated(timeCreated);
    }

    //if the key is older than n days, it is expired
    public boolean isExpired(){
        Timestamp currentTime = Timestamp.now();
        long keyDuration = currentTime.getSeconds() - this.timeCreated.getSeconds();

        double days = 7; //1.0/360.0; is 4 minutes //set to 2 minutes (1/720) for testing  //7; //set to expire after seven days
        double daysInSeconds = days * 24 * 60 * 60;

        if(keyDuration > daysInSeconds){
            return true;
        }

        return false;
    }

    /*
    Equality methods for use with ArrayList contains
     */
    @Override
    public boolean equals(Object object){
        if (object == null){
            return false;
        }

        if(object.getClass() != this.getClass()){
            return false;
        }

        final SessionKey other = (SessionKey)object;
        if(!this.keyId.equals(other.keyId)){
            return false;
        }
        if(!this.sessionKey.equals(other.sessionKey)){
            return false;
        }
        if(!this.timeCreated.equals(other.timeCreated)){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.keyId, this.sessionKey, this.timeCreated);
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public void setSessionKey(byte[] sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setTimeCreated(Timestamp timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getKeyId() {
        return keyId;
    }

    public byte[] getSessionKey() {
        return sessionKey;
    }

    public Timestamp getTimeCreated() {
        return timeCreated;
    }
}
