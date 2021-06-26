package com.vithamastech.smartlight.PowerSocketUtils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
    public static int getAuthValue(int authKey) {
        return ((((authKey * 49) + 1239) * 99) - (129 * authKey + 779));
    }

    public static byte[] getSHA256Hash(String data) {
        byte[] retVal = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(data.getBytes());
            retVal = md.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return retVal;
    }
}
