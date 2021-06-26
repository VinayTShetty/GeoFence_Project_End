package com.vithamastech.smartlight.helper;

import java.security.MessageDigest;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption {
    private SecretKeySpec skeySpec;
    private Cipher cipher;

    /* Secret key Initialize*/
    public Encryption(byte [] keyraw) throws Exception{
        if(keyraw == null){
            byte[] bytesOfMessage = "".getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            skeySpec = new SecretKeySpec(md.digest(bytesOfMessage), "AES");
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
        }
        else{
            skeySpec = new SecretKeySpec(keyraw, "AES");
            cipher = Cipher.getInstance("AES/ECB/NoPadding");
        }
    }

    private Encryption(String passphrase) throws Exception{
        byte[] bytesOfMessage = passphrase.getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        skeySpec = new SecretKeySpec(md.digest(bytesOfMessage), "AES");
        cipher = Cipher.getInstance("AES/ECB/NoPadding");
    }

    private Encryption() throws Exception{
        byte[] bytesOfMessage = "".getBytes("UTF-8");
        MessageDigest md = MessageDigest.getInstance("MD5");
        skeySpec = new SecretKeySpec(md.digest(bytesOfMessage), "AES");

        skeySpec = new SecretKeySpec(new byte[16], "AES");
        cipher = Cipher.getInstance("AES/ECB/NoPadding");
    }

    /*Encrypt plain data*/
    public byte[] encrypt (byte[] plaintext) throws Exception{
        //returns byte array encrypted with key
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        return cipher.doFinal(plaintext);
    }
    /*Decrypt cipher data*/
    private byte[] decrypt (byte[] ciphertext) throws Exception{
        //returns byte array decrypted with key
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        return cipher.doFinal(ciphertext);
    }

}
