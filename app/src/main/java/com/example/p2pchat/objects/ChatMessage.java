package com.example.p2pchat.objects;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.Objects;

public class ChatMessage {
    private Date time;
    private String message;
    private String senderId;
    private boolean ownMessage;

    public ChatMessage(){
        setTime(new Date());
        setMessage("");
        setSenderId("");
        setOwnMessage(false);
    }

    public ChatMessage(String senderId, String message, Timestamp time, boolean ownMessage){
        setTime(time.toDate());
        setMessage(message);
        setSenderId(senderId);
        setOwnMessage(ownMessage);
    }

    //overriding equals and hashcode, so they can be used with ArrayList contains() method
    @Override
    public boolean equals(Object object){
        if (object == null){
            return false;
        }

        if(object.getClass() != this.getClass()){
            return false;
        }

        final ChatMessage other = (ChatMessage)object;
        if (!this.time.equals(other.time)){
            return false;
        }
        if(this.ownMessage != other.ownMessage){
            return false;
        }
        if((this.senderId==null) ? (other.senderId!=null): (!this.senderId.equals(other.senderId))){
            return false;
        }
        if((this.message==null) ? (other.message!=null): (!this.message.equals(other.message))){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        /*
        int hash = 3;
        hash = 53 * hash + (this.message != null ? this.message.hashCode() : 0);
        hash = 53 * hash + (this.senderId != null ? this.senderId.hashCode() : 0);
        hash = 53 * hash + this.time.hashCode();
        hash = 53 * hash + Boolean.hashCode(this.ownMessage);
        return hash;


         */
        return Objects.hash(this.message, this.senderId, this.time, this.ownMessage);
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getTime() {
        return time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public boolean getOwnMessage(){
        return ownMessage;
    }

    public void setOwnMessage(boolean ownMessage) {
        this.ownMessage = ownMessage;
    }
}
