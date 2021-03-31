package com.example.p2pchat.objects;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;

import com.example.p2pchat.utils.Constants;
import com.example.p2pchat.utils.CryptoHelper;
import com.example.p2pchat.utils.KeyType;
import com.example.p2pchat.utils.RSAAlg;
import com.example.p2pchat.utils.SharedPreferencesHandler;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class User implements Parcelable {
    private String id;
    private String username;
    private String password;
    private Key publicKey;
    private Key privateKey;
    private Key signedPrekeyPublic;
    private Key signedPrekeyPrivate;
    private boolean regenerated; //represents if the user has been recreated with new keys

    public User(){
        this.username = "";
        //this.keys = KeyPair();
    }

    //creates a user and generates keys
    public User(String username) throws NoSuchAlgorithmException {
        this.username = username;

        //generate the user keys
        KeyPair keys = CryptoHelper.generateKeyPair(Constants.identityKeyAlg);
        this.publicKey = keys.getPublic();
        this.privateKey = keys.getPrivate();

        //generate the signed prekeys (prekeys that WILL be signed
        KeyPair signedPrekeys = CryptoHelper.generateKeyPair(Constants.signedPrekeyAlg);
        this.signedPrekeyPublic = signedPrekeys.getPublic();
        this.signedPrekeyPrivate = signedPrekeys.getPrivate();

        setRegenerated(false);
    }

    //main constructor used for creating this object class
    public User(String id, String username, Key publicKey, Key signedPrekeyPublic){
        setId(id);
        setUsername(username);
        setPublicKey(publicKey);
        setSignedPrekeyPublic(signedPrekeyPublic);
        setRegenerated(false);
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
        out.writeString(CryptoHelper.encodeKey(this.signedPrekeyPublic));
        out.writeString(CryptoHelper.encodeKey(this.signedPrekeyPrivate));
        out.writeInt(this.regenerated ? 1 : 0); //out.writeBoolean(this.regenerated);
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
            setPublicKey(CryptoHelper.decodeKey(in.readString(), KeyType.PUBLIC, Constants.identityKeyAlg));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        try {
            setPrivateKey(CryptoHelper.decodeKey(in.readString(), KeyType.PRIVATE, Constants.identityKeyAlg));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        //setting the up signed prekeys requires exception checks as well
        try {
            setSignedPrekeyPublic(CryptoHelper.decodeKey(in.readString(), KeyType.PUBLIC, Constants.signedPrekeyAlg));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        try {
            setSignedPrekeyPrivate(CryptoHelper.decodeKey(in.readString(), KeyType.PRIVATE, Constants.signedPrekeyAlg));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }

        setRegenerated(in.readInt() == 1); //setRegenerated(in.readBoolean());
    }

    public String getEncodedPublicKey(){
        return CryptoHelper.encodeKey(publicKey);
    }

    public String getEncodedPrivateKey(){
        return CryptoHelper.encodeKey(privateKey);
    }

    public String getEncodedSignedPrekeyPublic(){
        return CryptoHelper.encodeKey(this.signedPrekeyPublic);
    }

    public String getEncodedSignedPrekeyPrivate(){
        return CryptoHelper.encodeKey(this.signedPrekeyPrivate);
    }

    /**
     * A signature that helps prove the user is legitimately the user
     * Is the public signed prekey, signed by the user's identity private key
     * @param context - current activity context, needed to access sharedPreferences
     * @return String - base64 encoded signature
     */
    public String getSignature(Context context){
        // get the identity private key from shared preferences
        SharedPreferencesHandler sharedPrefsHandler = new SharedPreferencesHandler(context);
        try {
            Key identityPrvKey = sharedPrefsHandler.getPrivateKey(this.getId());

            //need to encode the public signed prekey before encrypting
            String encodedSignedPrekeyPublic = getEncodedSignedPrekeyPublic();

            //!!!!!!Can't encrypt w/ the identityPrvKey because it is a DH key!!!!!
            //generate the common shared key and encrypt
            //x: instead of uploading the signature at registration, create the signature
            //get bytes from the private key, shorten, and use the secretkeyalg

            //sign the public signed prekey by encrypting it w/ the identity private key
            RSAAlg rsaAlg = new RSAAlg((PrivateKey)identityPrvKey, RSAAlg.RSAMode.AUTHENTICATION_MODE);
            HashMap<String, byte[]> encryptedPrekey = rsaAlg.encrypt(encodedSignedPrekeyPublic);

            byte[] prekeySignature = encryptedPrekey.get("ciphertext");

            //base64 encode the signature, so it can be used as a string
            String encodedPrekeySignature = Base64.encodeToString(prekeySignature, Base64.DEFAULT);

            return encodedPrekeySignature;

        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (UnrecoverableEntryException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
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

    public Key getSignedPrekeyPrivate() {
        return signedPrekeyPrivate;
    }

    public void setSignedPrekeyPrivate(Key signedPrekeyPrivate) {
        this.signedPrekeyPrivate = signedPrekeyPrivate;
    }

    public Key getSignedPrekeyPublic() {
        return signedPrekeyPublic;
    }

    public void setSignedPrekeyPublic(Key signedPrekeyPublic) {
        this.signedPrekeyPublic = signedPrekeyPublic;
    }

    public boolean getRegenerated(){
        return this.regenerated;
    }

    public void setRegenerated(boolean regenerated) {
        this.regenerated = regenerated;
    }

}
