package com.succorfish.geofence.blecalculation;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.PublicKey;

public class ByteConversion {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static byte[] convertToTwoBytes(int data){
        return new byte[]{(byte)(data & 0x00FF),(byte)((data & 0xFF00)>>8)};
    }
    public static byte[] convertToFourBytes(final int data) {
        return new byte[] {
                (byte)((data >> 0) & 0xff),
                (byte)((data >> 8) & 0xff),
                (byte)((data >> 16) & 0xff),
                (byte)((data >> 24) & 0xff),
        };
    }


  public static byte []convertIntegertTWOBytes(int number){
      BigInteger bigInt = BigInteger.valueOf(number);
      return bigInt.toByteArray();
  }

    public static byte[] convert_LongTo_4_bytes(long value){
        byte [] data = new byte[4];
        data[3] = (byte) value;
        data[2] = (byte) (value >>> 8);
        data[1] = (byte) (value >>> 16);
        data[0] = (byte) (value >>> 32);
        return data;
    }

    /**
     * Used to convert 7 bytes long value to long.
     *  Logic Used in this method is reversiong the bytes in String and the calcualting.
     */

    public static String convert7bytesToLong(String valueToConevrt){
     long value=   new BigInteger(valueToConevrt, 16).longValue();
     return ""+value;
    }

    public static byte[] convert_TimeStampTo_4bytes(int value){
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static String convertHexStringToString(String hexStringInput) {
        String result = new String();
        char[] charArray = hexStringInput.toCharArray();
        for(int i = 0; i < charArray.length; i=i+2) {
            String st = ""+charArray[i]+""+charArray[i+1];
            char ch = (char)Integer.parseInt(st, 16);
            result = result + ch;
        }
        return result;
    }
    public static BigInteger convertHexToBigIntegert(String  hexValuevalue){
        BigInteger bi = new BigInteger(hexValuevalue, 16);
        return bi;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    /**
     * link:-https://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
     *  inupt string s must be of even length.
     */
    public static byte[] byteConverstionHelper_hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
}


