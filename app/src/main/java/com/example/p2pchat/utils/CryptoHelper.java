package com.example.p2pchat.utils;

//import com.example.p2pchat.R;
import android.util.Base64;
import android.util.Log;

import com.example.p2pchat.objects.SessionKey;

import javax.crypto.*;

import java.security.*;
import java.lang.Exception;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Random;

import com.google.firebase.Timestamp;

public class CryptoHelper {



    /**
     * Base64 encodes a public or private key
     * @param key - Any Key
     * @return String - The base64 encoded key.
     */
    static public String encodeKey(Key key){
        try {
            byte[] keyBytes = key.getEncoded();
            String keyStr = Base64.encodeToString(keyBytes, Base64.DEFAULT);
            return keyStr;
        }
        catch (NullPointerException e){
            //e.printStackTrace();
            Log.d("CryptoHelper encodeKey","Key was null");
            return null;
        }
    }

    /**
     * Base64 decodes an encoded key string
     * @param keyStr - Base64 encoded key string
     * @param keyType - PUBLIC or PRIVATE
     * @param alg
     * @return Key - The public or private key
     */
    static public Key decodeKey(String keyStr, KeyType keyType, String alg) throws NoSuchAlgorithmException, InvalidKeySpecException {
        try {
            byte[] sigBytes = Base64.decode(keyStr, Base64.DEFAULT);
            KeyFactory keyFact = KeyFactory.getInstance(alg); //KeyFactory keyFact = KeyFactory.getInstance("RSA");

            Key key = null;
            //creates a public key
            if (keyType == KeyType.PUBLIC) {
                X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(sigBytes);
                key = keyFact.generatePublic(x509KeySpec);
            }
            //creates a private key
            else if (keyType == KeyType.PRIVATE) {
                PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(sigBytes);
                key = keyFact.generatePrivate(pkcs8KeySpec);
            }
            return key;
        }
        catch (NullPointerException e){
            //e.printStackTrace();
            Log.d("CryptoHelper decodeKey","Key was null");
            return null;
        }
    }

    //Generates public/private key pair
    static public KeyPair generateKeyPair(String alg) throws NoSuchAlgorithmException {
        //KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        KeyPairGenerator kpg = KeyPairGenerator.getInstance(alg); //"DH"
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();

        //Public and private key
        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();

        return kp;
    }

    /**
     * Used to generate secret key for Diffie Hellman key exchange.
     * The user uses his private key and the other person's public key
     * @param privateKey - The current user's own private key
     * @param receivedPublicKey - The other user's public key
     * @return String - The base64 encoded key.
     */
    static public byte[] generateCommonSecretKey(PrivateKey privateKey, PublicKey receivedPublicKey){
        try {
            final KeyAgreement keyAgreement = KeyAgreement.getInstance("DH");
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(receivedPublicKey, true);

            byte [] secretKey = CryptoHelper.shortenSecretKey(keyAgreement.generateSecret()); // The implementation I used had the following: shortenSecretKey(keyAgreement.generateSecret());
            return secretKey;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 1024 bit symmetric key size is so big for DES so we must shorten the key size. You can get first 8 longKey of the
     * byte array or can use a key factory
     *
     * @param   longKey
     *
     * @return
     */
    static private byte[] shortenSecretKey(final byte[] longKey) {

        try {

            // Use 32 bytes (256 bits) for AES
            final byte[] shortenedKey = new byte[32];

            System.arraycopy(longKey, 0, shortenedKey, 0, shortenedKey.length);

            return shortenedKey;

            // Below lines can be more secure
            // final SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
            // final DESKeySpec       desSpec    = new DESKeySpec(longKey);
            //
            // return keyFactory.generateSecret(desSpec).getEncoded();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Generates a random symmetric key
     * @return byte[] - The symmetric key.
     */
    static public byte[] generateSessionKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();
        byte[] keyBytes = secretKey.getEncoded();
        return keyBytes;
    }

    /**
     * Encrypts a session key with the receiver's public key
     * @param sessionKey - The session key to encrypt
     * @param receiverPublicKey - The message receiver's public key
     * @return String - The encrypted symmetric key, base64 encoded to a String
     */
    static public String encryptSessionKey(byte[] sessionKey, PublicKey receiverPublicKey) throws NoSuchAlgorithmException, IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchPaddingException {
        //base64 encode the key for encryption
        String base64EncodedKey = Base64.encodeToString(sessionKey, Base64.DEFAULT);

        RSAAlg rsaAlg = new RSAAlg(receiverPublicKey, RSAAlg.RSAMode.CONFIDENTIALITY_MODE);
        byte[] encryptedKey = rsaAlg.encrypt(base64EncodedKey).get("ciphertext");

        //base64 encode the key so it can be saved to the database
        String encodedEncryptedKey = Base64.encodeToString(encryptedKey, Base64.DEFAULT);
        return encodedEncryptedKey;
    }

    /**
     * Decrypts a session key with the receiver's private key
     * @param encryptedSessionKey - The session key to decrypt
     * @param receiverPrivateKey - The message receiver's private key
     * @return byte[] - The symmetric decrypted, in a usable form
     */
    static public byte[] decryptSessionKey(String encryptedSessionKey, PrivateKey receiverPrivateKey) throws NoSuchPaddingException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException {
        byte[] decodedEncryptedSessionKey = Base64.decode(encryptedSessionKey, Base64.DEFAULT);

        //decrypt the session key
        RSAAlg rsaAlg = new RSAAlg(receiverPrivateKey, RSAAlg.RSAMode.CONFIDENTIALITY_MODE);
        String encodedSessionKey = rsaAlg.decrypt(decodedEncryptedSessionKey);

        //decode back to byte form to get a usable session key
        byte[] sessionKey = Base64.decode(encodedSessionKey, Base64.DEFAULT);
        return sessionKey;
    }

    static public SessionKey selectSessionKey(List<SessionKey> sessionKeyList, Timestamp messageTime){
        // Start from highest and iterate down.
        for (int i = sessionKeyList.size() - 1; i >= 0; i--){
            SessionKey currentSessionKey = sessionKeyList.get(i);

            //If message timestamp higher than the key timestamp, use it
            if(messageTime.getSeconds() >= currentSessionKey.getTimeCreated().getSeconds()){
                return currentSessionKey;
            }
        }
        return null;
    }

    /**
     * Symmetrically encrypt a text message
     * @param message - The text message to encrypt
     * @param secretKey - Symmetric key for encryption
     * @return String - The encrypted message after base64 encoding
     */
    static public String encryptMessage(String message, byte[] secretKey){
        //perform the encryption
        SecretKeyAlg secretKeyAlg = new SecretKeyAlg(secretKey);
        byte[] encryptedMessageBytes = new byte[0];
        try {
            encryptedMessageBytes = secretKeyAlg.encrypt(message).get("ciphertext");
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        String encryptedMessage = Base64.encodeToString(encryptedMessageBytes, Base64.DEFAULT);

        System.out.println("Encrypted Message: ${encryptedMessage}");

        return encryptedMessage;
    }

    /**
     * Symmetrically decrypt a base64 encoded text message
     * @param encodedMessage - The text message to decrypt. It must be base64 decoded first
     * @param secretKey - Symmetric key for encryption/decryption
     * @return String - The decrypted plaintext
     */
    static public String decryptMessage(String encodedMessage, byte[] secretKey){
        //base 64 decode the message
        byte[] message = Base64.decode(encodedMessage, Base64.DEFAULT);

        SecretKeyAlg secretKeyAlg = new SecretKeyAlg(secretKey);
        String plaintext = null;
        try {
            plaintext = secretKeyAlg.decrypt(message);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return plaintext;
    }

    /**
     * Creates a challenge number as a base64-encoded string
     * @param receiverPublicKey
     * @return
     */
    static public String createChallengeNumber(PublicKey receiverPublicKey){
        Random rand = new Random();
        //random values from 0.0-1.0
        double challenge = rand.nextDouble();
        String challengeString = Double.toString(challenge);

        RSAAlg rsaAlg = new RSAAlg(receiverPublicKey, RSAAlg.RSAMode.CONFIDENTIALITY_MODE);
        String encryptedChallengeString = "";
        try {
            byte[] encryptedChallenge = rsaAlg.encrypt(challengeString).get("ciphertext");
            encryptedChallengeString = Base64.encodeToString(encryptedChallenge, Base64.DEFAULT);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return encryptedChallengeString;
    }

    /**
     *
     * @param challenge
     * @param publicKey
     * @return
     */
    static public String encryptChallenge(String challenge, PublicKey publicKey){
        RSAAlg rsaAlg = new RSAAlg(publicKey, RSAAlg.RSAMode.CONFIDENTIALITY_MODE);
        String encryptedChallengeString = "";
        try {
            byte[] encryptedChallenge = rsaAlg.encrypt(challenge).get("ciphertext");
            encryptedChallengeString = Base64.encodeToString(encryptedChallenge, Base64.DEFAULT);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return encryptedChallengeString;
    }

    /**
     * Gets back the challenge number as a string
     * @param encodedChallenge
     * @param privateKey
     * @return
     */
    static public String decryptChallenge(String encodedChallenge, PrivateKey privateKey){
        //base 64 decode the message
        byte[] message = Base64.decode(encodedChallenge, Base64.DEFAULT);

        RSAAlg rsaAlg = new RSAAlg(privateKey, RSAAlg.RSAMode.CONFIDENTIALITY_MODE);
        String plaintext = null;
        try {
            plaintext = rsaAlg.decrypt(message);
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return plaintext;
    }

    /**
     * Encrypts a plaintext message to ciphertext
     * @param userid - used to get AES key and private key from sharedPreferences
     * @return String - Base64 encoded ciphertext
     */
    //need to get AES key first and then get sharedPreferences private key

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
