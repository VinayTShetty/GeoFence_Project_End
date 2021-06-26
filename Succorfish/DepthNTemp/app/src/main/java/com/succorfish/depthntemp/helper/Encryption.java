package com.succorfish.depthntemp.helper;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/*Encryption*/
public class Encryption {
    private SecretKeySpec skeySpec;
    private Cipher cipher;
    byte[] bytesOfMessage;
    MessageDigest md;

    public Encryption(byte[] keyraw) throws Exception {
        if (keyraw == null) {
            bytesOfMessage = "".getBytes("UTF-8");
            md = MessageDigest.getInstance("MD5");
            skeySpec = new SecretKeySpec(md.digest(bytesOfMessage), "AES");
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
        } else {
            skeySpec = new SecretKeySpec(keyraw, "AES");
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
        }
    }

    private Encryption(String passphrase) throws Exception {
        byte[] bytesOfMessage = passphrase.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        skeySpec = new SecretKeySpec(md.digest(bytesOfMessage), "AES");
        cipher = Cipher.getInstance("AES/ECB/NoPadding");
    }

    private Encryption() throws Exception {
        byte[] bytesOfMessage = "".getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        skeySpec = new SecretKeySpec(md.digest(bytesOfMessage), "AES");

        skeySpec = new SecretKeySpec(new byte[16], "AES");
        cipher = Cipher.getInstance("AES/ECB/NoPadding");
    }

    public byte[] encrypt(byte[] plaintext) throws Exception {
        //returns byte array encrypted with key
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(plaintext);
    }

    public byte[] decrypt(byte[] ciphertext) throws Exception {
        //returns byte array decrypted with key
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(ciphertext);
    }

}
