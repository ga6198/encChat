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
    private PublicKey publicKey;
    private PrivateKey privateKey;

    private final String ENCRYPTION_ALG = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    /**
     * set up RSA encryption/decryption using a public and private key. They are NOT a key pair
     * @param publicKey - If encrypting, pass in the receiver's public key. If decrypting, pass in the sender's public key
     * @param privateKey - If encrypting, pass in the sender's public key. If decrypting, pass in the receiver's public key
     */
    /*
    public RSAAlg(Key publicKey, Key privateKey){
        setPublicKey(publicKey);
        setPrivateKey(privateKey);
    }
     */

    public RSAAlg(PublicKey publicKey){
        setPublicKey(publicKey);
    }

    public RSAAlg(PrivateKey privateKey){
        setPrivateKey(privateKey);
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

        //encrypt with receiver public key
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] ciphertextFinal = cipher.doFinal(textToEncrypt.getBytes());

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

        //receiver private key
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] plaintextFinal = cipher.doFinal(bytesToDecrypt);

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
}
