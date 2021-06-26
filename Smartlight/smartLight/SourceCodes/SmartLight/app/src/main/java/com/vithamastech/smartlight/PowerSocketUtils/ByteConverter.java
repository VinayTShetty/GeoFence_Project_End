package com.vithamastech.smartlight.PowerSocketUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class ByteConverter {

    private static final int INT_LENGTH = 4;
    private static final int LONG_LENGTH = 8;

    private static final char[] UPPER_HEX_CHARS = "0123456789ABCDEF".toCharArray();

    public static byte[] convertIntToByteArray(int input,int byteLength){
        byte[] retVal = new byte[byteLength];
        for (int i = 0; i < byteLength; i++) {
            retVal[(byteLength - 1) - i] = (byte) (input >> (i * 8));
        }
        return retVal;
    }

    public static byte[] convertIntToByteArray(int input) {
        byte[] retVal = new byte[INT_LENGTH];
        for (int i = 0; i < INT_LENGTH; i++) {
            retVal[(INT_LENGTH - 1) - i] = (byte) (input >> (i * 8));
        }
        return retVal;
    }

    public static byte[] convertLongToByteArray(long input) {
        byte[] retVal = new byte[LONG_LENGTH];
        for (int i = 0; i < LONG_LENGTH; i++) {
            retVal[(LONG_LENGTH - 1) - i] = (byte) (input >> (i * 8));
        }
        return retVal;
    }

    public static int convertByteArrayToInt(byte[] input) {
        int retVal = 0;
        if (input.length > INT_LENGTH) {
            throw new RuntimeException("Integer length is 4 bytes");
        }

        for (byte b : input) {
            retVal = retVal << 8;
            retVal = retVal | (b & 0xFF);
        }
        return retVal;
    }

    public static long convertByteArrayToLong(byte[] input) {
        int retVal = 0;
        if (input.length > LONG_LENGTH) {
            throw new RuntimeException("Long length is 8 bytes");
        }

        for (byte b : input) {
            retVal = retVal << 8;
            retVal = retVal | (b & 0xFF);
        }
        return retVal;
    }

    public static byte[] reverseBytes(byte[] input) {
        byte[] retVal = new byte[input.length];
        for (int i = 0; i < input.length; i++) {
            retVal[(input.length - 1) - i] = input[i];
        }
        return retVal;
    }

    /**
     * Convert a byte to a boolean
     */
    public static boolean byteToBool(byte val) {
        return val == 0x0 ? false : true;
    }

    /**
     * Convert a byte to an int
     */
    public static int byteToInt(byte b) {
        int i = b & 255;
        return i;
    }

    /**
     * Returns a short, from the given byte, ensuring that it is unsigned.
     */
    public static short unsignedByte(byte value) {
        return (short) (value & 0xff);
    }

    public static String getHexStringFromByteArray(byte[] input, boolean isSpacingRequired) {
        StringBuilder HexBuilder = new StringBuilder(2 * input.length);
        for (int i = 0; i < input.length; i++) {
            HexBuilder.append(UPPER_HEX_CHARS[(input[i] & 0xF0) >> 4]); // Extract Higher Nibble
            HexBuilder.append(UPPER_HEX_CHARS[(input[i] & 0x0F)]); // Extract Lower Nibble

            if (isSpacingRequired) {
                if (i != input.length - 1) {
                    HexBuilder.append(" ");
                }
            }
        }
        return HexBuilder.toString();
    }

    public static byte[] getByteArrayFromHexString(String hexString) {
        int len = hexString.length();
        if (len % 2 > 0) {
            throw new RuntimeException();
        }

        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] extractMSB(byte[] input, int length) {
        byte[] retVal = null;
        if (length <= input.length) {
            retVal = new byte[length];
            for (int i = 0; i < length; i++) {
                retVal[i] = (byte) (input[i] & 0xFF);
            }
        }
        return retVal;
    }

    public static byte[] extractLSB(byte[] input, int length) {
        byte[] retVal = null;
        if (length <= input.length) {
            retVal = new byte[length];
            for (int i = 0; i < length; i++) {
                retVal[(length - 1) - i] = (byte) (input[(input.length - 1) - i] & 0xFF);
            }
        }
        return retVal;
    }

    public static byte[] padZeros(byte[] inputBytes, int totalDataLength) {
        byte[] retVal = new byte[totalDataLength];
        System.arraycopy(inputBytes, 0, retVal, 0, inputBytes.length); // Copy contents from inputBytes to retVal
        return retVal;
    }

    /**
     * Create a new byte array from the given source, with the given ranges
     */
    public static byte[] subBytes(byte[] source, int startIndex, int endIndex) {
        byte[] destination = new byte[endIndex - startIndex];
        System.arraycopy(source, startIndex, destination, 0, endIndex - startIndex);
        return destination;
    }

    public static byte[] copyOfRange(byte[] source, int from, int to) {
        if (source == null || from < 0 || to < 0) {
            return null;
        }

        int length = to - from;

        if (length < 0) {
            return null;
        }

        if (source.length < from + length) {
            return null;
        }

        byte[] destination = new byte[length];
        System.arraycopy(source, from, destination, 0, length);
        return destination;
    }

    public static Queue<byte[]> getDataQueue(byte[] data, int maxPacketLen) {
        Queue<byte[]> segmentQueue = new ArrayDeque<byte[]>();

        int dataLen = data.length;
        int segmentSize = (int) Math.ceil((dataLen / (double) maxPacketLen));

        int remaining = dataLen;
        int offset = 0;

        for (int i = 0; i < segmentSize; i++) {
            offset = i * (int) maxPacketLen;
            int length = 0;
            if (i == segmentSize - 1) {
                length = remaining;
            } else {
                length = maxPacketLen;
            }

            byte[] segment = new byte[length];
            System.arraycopy(data, offset, segment, 0, length);
            segmentQueue.add(segment);
            remaining = remaining - maxPacketLen;
        }

        return segmentQueue;
    }

    public static List<byte[]> getDataChunks(byte[] data, int maxPacketLen) {
        List<byte[]> segmentList = new ArrayList<byte[]>();

        int dataLen = data.length;
        int segmentSize = (int) Math.ceil((dataLen / (double) maxPacketLen));

        int remaining = dataLen;
        int offset = 0;

        for (int i = 0; i < segmentSize; i++) {
            offset = i * (int) maxPacketLen;
            int length = 0;
            if (i == segmentSize - 1) {
                length = remaining;
            } else {
                length = maxPacketLen;
            }

            byte[] segment = new byte[length];
            System.arraycopy(data, offset, segment, 0, length);
            segmentList.add(segment);
            remaining = remaining - maxPacketLen;
        }

        return segmentList;
    }

    public static String convertByteToBinary(byte b) {
        // Convert byte to unsigned integer to prevent sign extension
        int val = b & 0xFF;
        String binaryString = Integer.toBinaryString(val);
        String binaryStringPadded = String.format("%8s", binaryString);
        return binaryStringPadded.replace(' ', '0');
    }

    public static byte convertBinaryToByte(String binaryString) {
        int val = 0;
        try {
            val = Integer.parseInt(binaryString, 2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return (byte) val;
    }

    public static byte[] trim(byte[] input) {
        int srcIdx = -1;
        for(int i=input.length-1;i>=0;i--) {
            if(input[i]!=0) {
                srcIdx = i+1;
                break;
            }
        }

        byte[] trimmedBytes = new byte[srcIdx+1];
        if(srcIdx>0) {
            trimmedBytes = copyOfRange(input,0,srcIdx);
        }else {
            trimmedBytes = copyOfRange(input,0,input.length);
        }
        return trimmedBytes;
    }

//    public static int crc16ByteCalculation(byte[] data_p, int length) {
//        int i, j = 0;
//        int data_temp;
//        int crc = 0xffff;
//        if (length == 0)
//            return (short) ~crc;
//        do {
//            data_temp = (int) 0xff & data_p[j];
//            for (i = 0; i < 8; i++, data_temp >>= 1) {
//                if (((crc & 0x0001) != (data_temp & 0x0001)))
//                    crc = (crc >> 1) ^ 0x8408;
//                else crc >>= 1;
//            }
//            j++;
//        } while (j < length);
//        crc = ~crc;
////        System.out.println("CRC=" + (crc & 0xFFFF));
//        return (crc);
//    }
}