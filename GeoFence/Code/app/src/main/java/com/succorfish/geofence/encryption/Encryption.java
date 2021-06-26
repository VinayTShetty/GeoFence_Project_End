package com.succorfish.geofence.encryption;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private static final byte[] KeyBytes = {0x34,(byte) 0x99,(byte) 0xab,0x12,0x13,(byte) 0x88,(byte) 0x98,0x47,0x33,0x08,0x49,0x21,0x12,(byte) 0xd4,0x65,0x74};
    public static byte[] encryptData(byte[] inputData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
// Create key and cipher
        Key aesKey = new SecretKeySpec(KeyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");  // AES/ECB/NoPadding    // AES/CBC/PKCS5Padding
// encrypt the text
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(inputData);
        return encrypted;
    }

    public static byte[] decryptData(byte[] encryptedData) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
// Decrypt the text
// Create key and cipher
        Key aesKey = new SecretKeySpec(KeyBytes, "AES");
        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(encryptedData);
        return decrypted;
    }
}


/***
 * Key Points on Encryption.
 * 1)Encryption Key Dont consider as String.Take it in byte array.
 */

