package com.example.p2pchat.utils;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
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

    public AESAlg() throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException {
        this.key = generateAESKey();
    }

    public AESAlg(SecretKey key, byte[] iv){
        this.key = key;
        this.iv = iv;
    }

    //create a key for encrypting private keys to the key store
    @RequiresApi(api = Build.VERSION_CODES.M)
    public SecretKey generateAESKey() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {

        final KeyGenerator keyGenerator = KeyGenerator
                .getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");

        //set key parameters
        final KeyGenParameterSpec keyGenParameterSpec = new KeyGenParameterSpec.Builder(Constants.KEYSTORE_ALIAS,
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
    public byte[] decrypt(byte[] bytesToDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        final GCMParameterSpec spec = new GCMParameterSpec(128, this.iv);
        cipher.init(Cipher.DECRYPT_MODE, this.key, spec);
        final byte[] decryption = cipher.doFinal(bytesToDecrypt);

        return decryption;
    }
}
