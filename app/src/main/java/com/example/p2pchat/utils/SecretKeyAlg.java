package com.example.p2pchat.utils;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class SecretKeyAlg implements CryptoAlg {
    private byte[] secretKey;

    private final String ENCRYPTION_ALG = "AES";

    public SecretKeyAlg(byte[] secretKey){
        setSecretKey(secretKey);
    }

    @Override
    public HashMap<String, byte[]> encrypt(String textToEncrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES/ECB/PKCS5Padding");
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        //encrypt using the secret key
        byte[] ciphertextBytes = cipher.doFinal(textToEncrypt.getBytes());

        //build hashmap
        HashMap<String, byte[]> ciphertext = new HashMap<String, byte[]>();
        ciphertext.put("ciphertext", ciphertextBytes);
        return ciphertext;
    }

    @Override
    public String decrypt(byte[] bytesToDecrypt) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        final SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES/ECB/PKCS5Padding");
        final Cipher cipher = Cipher.getInstance(ENCRYPTION_ALG);
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        //decrypt
        byte[] plaintextBytes = cipher.doFinal(bytesToDecrypt);

        //convert to string
        String plaintext = new String(plaintextBytes);

        return plaintext;
    }

    public void setSecretKey(byte[] secretKey) {
        this.secretKey = secretKey;
    }

    public byte[] getSecretKey() {
        return secretKey;
    }
}
