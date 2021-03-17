package com.example.p2pchat.utils;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public class AESAlg implements CryptoAlg {
    private SecretKey key;
    private byte[] iv;
    private String keyId;

    //used when creating the key for the first time
    public AESAlg(String userId) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CertificateException, KeyStoreException, IOException {
        setKeyId(userId);

        this.key = generateAESKey(keyId);

        //For debugging the keystore
        getAllAliasesInTheKeystore();
    }

    //used when you need to decrypt
    public AESAlg(byte[] iv, String userId) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException, UnrecoverableEntryException {
        setKeyId(userId);

        //get the keystore, which has the key
        KeyStore keyStore = KeyStore.getInstance(Constants.KEYSTORE_ALIAS);
        keyStore.load(null);
        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(keyId, null);
        this.key = secretKeyEntry.getSecretKey();

        this.iv = iv;

        //For debugging the keystore
        getAllAliasesInTheKeystore();
    }

    private ArrayList<String> getAllAliasesInTheKeystore() throws KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(Constants.KEYSTORE_ALIAS);
        keyStore.load(null);
        ArrayList<String> aliases = Collections.list(keyStore.aliases());
        for (String alias : aliases) {
            Log.d("Alias", alias);
        }


        return aliases;
    }

    //create a key for encrypting private keys to the key store
    @RequiresApi(api = Build.VERSION_CODES.M)
    private SecretKey generateAESKey(String keyAlias) throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, Constants.KEYSTORE_ALIAS);

        //set key parameters
        final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(keyAlias,
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM) //requires GCM mode to encrypt/decrypt
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();

        //create the key
        keyGenerator.init(keyGenParameterSpec);
        final SecretKey secretKey = keyGenerator.generateKey();
        return secretKey;
    }

    /**
     * AES encryption of a string
     * @param textToEncrypt - String that needs to be encrypted w/ AES
     * @return HashMap<String, byte[]> - Returns <"iv", iv-value> and <"ciphertext", encrypted-text>
     */
    @Override
    public HashMap<String, byte[]> encrypt(String textToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, this.key);

        byte[] iv = cipher.getIV();

        byte[] ciphertext = cipher.doFinal(textToEncrypt.getBytes());

        HashMap<String, byte[]> encryption = new HashMap<String, byte[]>();
        encryption.put("iv", iv);
        encryption.put("ciphertext", ciphertext);

        return encryption;
    }

    @Override
    public String decrypt(byte[] bytesToDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final GCMParameterSpec spec = new GCMParameterSpec(128, this.iv);
        cipher.init(Cipher.DECRYPT_MODE, this.key, spec);
        final byte[] decryptionBytes = cipher.doFinal(bytesToDecrypt);
        String decryption = new String(decryptionBytes);

        return decryption;
    }

    public void setIv(byte[] iv) {
        this.iv = iv;
    }

    public void setKey(SecretKey key) {
        this.key = key;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public byte[] getIv() {
        return iv;
    }

    public SecretKey getKey() {
        return key;
    }

    public String getKeyId() {
        return keyId;
    }
}
