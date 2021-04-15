package com.example.p2pchat.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAAlg implements CryptoAlg {
    public enum RSAMode{
        CONFIDENTIALITY_MODE, //encrypt with other person's public key
        AUTHENTICATION_MODE //encrypt with your own private key
    }

    private PublicKey publicKey;
    private PrivateKey privateKey;
    private RSAMode mode;

    private final String ENCRYPTION_ALG = "RSA/ECB/PKCS1Padding";

    /**
     * set up RSA encryption/decryption using a public and private key. They are NOT a key pair
     * @param privateKey - If encrypting, pass in the sender's public key. If decrypting, pass in the receiver's public key
     */
    /*
    public RSAAlg(Key publicKey, Key privateKey){
        setPublicKey(publicKey);
        setPrivateKey(privateKey);
    }
     */

    public RSAAlg(PublicKey publicKey, RSAMode mode){
        setPublicKey(publicKey);
        setMode(mode);
    }

    public RSAAlg(PrivateKey privateKey, RSAMode mode){
        setPrivateKey(privateKey);
        setMode(mode);
    }

    /**
     * encrypts a string w/ RSA double encryption (sender prv key + receiver pub key)
     * @param textToEncrypt - the text to encrypt
     * @return HashMap<String, byte[]> - The string is a key, like "ciphertext" or "iv" if needed. The byte[] is the ciphertext data, and other data
     */
    /*
    @Override
    public HashMap<String, byte[]> encrypt(String textToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //set up cipher
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);

        //encrypt with sender private key
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] ciphertextAfterEnc1 = cipher.doFinal(textToEncrypt.getBytes());

        //Length of current ciphertext
        System.out.println("Length after encrypting with prv key: " + ciphertextAfterEnc1.length);

        //encrypt with receiver public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] ciphertextFinal = cipher.doFinal(ciphertextAfterEnc1);

        //build hashmap
        HashMap<String, byte[]> ciphertext = new HashMap<String, byte[]>();
        ciphertext.put("ciphertext", ciphertextFinal);
        return ciphertext;
    }

     */
    @Override
    public HashMap<String, byte[]> encrypt(String textToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //set up cipher
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);

        byte[] ciphertextFinal = null;
        if(mode.equals(RSAMode.CONFIDENTIALITY_MODE)){
            //encrypt with receiver public key
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            ciphertextFinal = cipher.doFinal(textToEncrypt.getBytes());
        }
        else if(mode.equals(RSAMode.AUTHENTICATION_MODE)){
            //encrypt with sender private key
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            ciphertextFinal = cipher.doFinal(textToEncrypt.getBytes());
        }

        //build hashmap
        HashMap<String, byte[]> ciphertext = new HashMap<String, byte[]>();
        ciphertext.put("ciphertext", ciphertextFinal);
        return ciphertext;
    }

    /**
     * encrypts a string w/ RSA encryption
     * @param textToEncrypt - the text to encrypt
     * @param mode - represents that mode/key you are encrypting with
     * @return HashMap<String, byte[]> - The string is a key, like "ciphertext" or "iv" if needed. The byte[] is the ciphertext data, and other data
     */
    public HashMap<String, byte[]> encrypt(String textToEncrypt, RSAMode mode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //set up cipher
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);

        byte[] ciphertextFinal = null;
        if(mode.equals(RSAMode.CONFIDENTIALITY_MODE)){
            //encrypt with receiver public key
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            ciphertextFinal = cipher.doFinal(textToEncrypt.getBytes());
        }
        else if(mode.equals(RSAMode.AUTHENTICATION_MODE)){
            //encrypt with sender private key
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            ciphertextFinal = cipher.doFinal(textToEncrypt.getBytes());
        }

        //build hashmap
        HashMap<String, byte[]> ciphertext = new HashMap<String, byte[]>();
        ciphertext.put("ciphertext", ciphertextFinal);
        return ciphertext;
    }

    /*
    @Override
    public String decrypt(byte[] bytesToDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //set up cipher
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);

        //receiver private key
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] textAfterDec1 = cipher.doFinal(bytesToDecrypt);

        //sender public key
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] plaintextFinal = cipher.doFinal(textAfterDec1);

        //convert to string
        String plaintext = new String(plaintextFinal);

        return plaintext;
    }

     */

    @Override
    public String decrypt(byte[] bytesToDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //set up cipher
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);

        byte[] plaintextFinal = null;

        if(mode.equals(RSAMode.CONFIDENTIALITY_MODE)){
            //receiver private key
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            plaintextFinal = cipher.doFinal(bytesToDecrypt);
        }
        else if (mode.equals(RSAMode.AUTHENTICATION_MODE)){
            //sender public key
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            plaintextFinal = cipher.doFinal(bytesToDecrypt);
        }

        //convert to string
        String plaintext = new String(plaintextFinal);

        return plaintext;
    }

    /**
     * decrypts a string w/ RSA encryption
     * @param bytesToDecrypt - the bytes to decrypt to a String
     * @param mode - represents that mode/key you are decrypting with
     * @return String - plaintext
     */
    public String decrypt(byte[] bytesToDecrypt, RSAMode mode) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        //set up cipher
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);

        byte[] plaintextFinal = null;

        if(mode.equals(RSAMode.CONFIDENTIALITY_MODE)){
            //receiver private key
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            plaintextFinal = cipher.doFinal(bytesToDecrypt);
        }
        else if (mode.equals(RSAMode.AUTHENTICATION_MODE)){
            //sender public key
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            plaintextFinal = cipher.doFinal(bytesToDecrypt);
        }

        //convert to string
        String plaintext = new String(plaintextFinal);

        return plaintext;
    }

    public Key getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public Key getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public RSAMode getMode() {
        return mode;
    }

    public void setMode(RSAMode mode) {
        this.mode = mode;
    }
}
