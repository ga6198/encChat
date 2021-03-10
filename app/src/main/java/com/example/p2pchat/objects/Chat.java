package com.example.p2pchat.objects;

import com.google.firebase.Timestamp;

import java.util.Date;
import java.util.Objects;

public class Chat {
    private String id; //firestore document id
    private String lastUsername;
    private String lastMessage;
    private Date lastMessageTime;
    private String otherUserId;
    private String otherUserUsername;

    public Chat(){
        setId("");
    }

    public Chat(String id){
        setId(id);
    }

    public Chat(String id, String lastUsername, String lastMessage, Timestamp lastMessageTime, String otherUserId, String otherUserUsername){
        setId(id);
        setLastMessage(lastMessage);
        setLastUsername(lastUsername);
        setLastMessageTime(lastMessageTime.toDate());
        setOtherUserId(otherUserId);
        setOtherUserUsername(otherUserUsername);
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

        final Chat other = (Chat)object;
        if(!this.id.equals(other.id)){
            return false;
        }
        if(!this.lastMessage.equals(other.lastMessage)){
            return false;
        }
        if(!this.lastMessageTime.equals(other.lastMessageTime)){
            return false;
        }
        if(!this.lastUsername.equals(other.lastUsername)){
            return false;
        }

        return true;
    }

    @Override
    public int hashCode(){
        return Objects.hash(this.lastUsername, this.lastMessageTime, this.lastMessage, this.id);
    }

    public String getId() {
        return id;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastUsername() {
        return lastUsername;
    }

    public Date getLastMessageTime() {
        return lastMessageTime;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public String getOtherUserUsername() {
        return otherUserUsername;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(Date lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setLastUsername(String lastUsername) {
        this.lastUsername = lastUsername;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public void setOtherUserUsername(String otherUserUsername) {
        this.otherUserUsername = otherUserUsername;
    }
}
