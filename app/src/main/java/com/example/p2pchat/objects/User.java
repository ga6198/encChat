package com.example.p2pchat.objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.p2pchat.MainActivity;
import com.example.p2pchat.utils.CryptoHelper;
import com.example.p2pchat.utils.KeyType;

import java.security.*;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

import static android.content.Context.MODE_PRIVATE;

public class User implements Parcelable {
    private String id;
    private String username;
    private String password;
    private Key publicKey;
    private Key privateKey;

    public User(){
        this.username = "";
        //this.keys = KeyPair();
    }

    //creates a user and generates keys
    public User(String username) throws NoSuchAlgorithmException {
        this.username = username;

        //generate the user keys
        KeyPair keys = CryptoHelper.generateKeyPair();
        this.publicKey = keys.getPublic();
        this.privateKey = keys.getPrivate();

    }

    //main constructor used for creating this object class
    public User(String id, String username, Key publicKey){
        setId(id);
        setUsername(username);
        setPublicKey(publicKey);
    }

    // 99.9% of the time you can just ignore this
    @Override
    public int describeContents() {
        return 0;
    }

    // write your object's data to the passed-in Parcel
    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.id);
        out.writeString(this.username);
        out.writeString(this.password);
        out.writeString(CryptoHelper.encodeKey(this.publicKey));
        out.writeString(CryptoHelper.encodeKey(this.privateKey));
    }

    // this is used to regenerate your object. All Parcelables must have a CREATOR that implements these two methods
    public static final Parcelable.Creator<User> CREATOR = new Parcelable.Creator<User>() {
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        public User[] newArray(int size) {
            return new User[size];
        }
    };

    // example constructor that takes a Parcel and gives you an object populated with it's values
    //seems like reads have to be in the same order as writes
    private User(Parcel in) {
        setId(in.readString());
        setUsername(in.readString());
        setPassword(in.readString());

        //setting the keys requires exception checks
        try {
            setPublicKey(CryptoHelper.decodeKey(in.readString(), KeyType.PUBLIC));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        try {
            setPrivateKey(CryptoHelper.decodeKey(in.readString(), KeyType.PRIVATE));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    public String getEncodedPublicKey(){
        return CryptoHelper.encodeKey(publicKey);
    }

    public String getEncodedPrivateKey(){
        return CryptoHelper.encodeKey(privateKey);
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
