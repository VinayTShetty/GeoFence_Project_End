package com.succorfish.geofence.blecalculation;

import java.math.BigInteger;

public class Blecalculation {


    public static byte[] intTobyteArray(final int data) {
        return new byte[] {
                (byte)((data >> 24) & 0xff),
                (byte)((data >> 16) & 0xff),
                (byte)((data >> 8) & 0xff),
                (byte)((data >> 0) & 0xff),
        };
    }

    public static int hexToint(String hex) {
        return Integer.parseInt(hex, 16);
    }

    public static int calculateAlgorithmValue(int m_auth_key ){
        return ((((m_auth_key * 75) + 8294) * 19) - (573*m_auth_key + 989));
    }

    /**
     *
     * Upcode 02,length 04,4 bytes of Data after calculating the magic Number
     */
    public static byte [] getConnectionMainCode(int value){
        short mDataLength = (short) 0x04;
        short upcode=(short)0x02;
        byte byte_value[] = new byte[6];
        byte_value[0] = (byte) (upcode & 0xFF);
        byte_value[1] = (byte) (mDataLength & 0xFF);
        byte_value[2] = (byte) (value & 0xFF);
        byte_value[3] = (byte) ((value >> 8) & 0xFF);
        byte_value[4] = (byte) ((value >> 16) & 0xFF);
        byte_value[5] = (byte) ((value >> 24) & 0xFF);
        return byte_value;
    }

    /**
     *
     * Used for authenication of the Device.
     */
    public static byte[] WriteValue01() {
        byte byte_value[] = new byte[1];
        byte_value[0]=0X01;
        return byte_value;
    }

   /* public static byte[] getGeoFenceAlertsFromId(int id){
        short upCode=(short)0xa2;
        short datalength=(short)0x02;
        byte byte_value []=new byte[3];
        byte_value[0]=(byte) upCode;
        byte_value[1]=(byte)datalength;
        byte_value[2]=(byte)id;
        return  byte_value;
    }*/


    public static byte[] geofenceId(int geoFenceValue){
        byte opcode= (byte) 0XA2;
        byte length=(byte)0x02;
        byte [] geoFenceValueArray=intToBytes(geoFenceValue);
        byte [] geoFenceIdEndArray=new byte[16];
        geoFenceIdEndArray[0]=opcode;
        geoFenceIdEndArray[1]=length;
        geoFenceIdEndArray[2]=geoFenceValueArray[0];
        geoFenceIdEndArray[3]=geoFenceValueArray[1];
        return geoFenceIdEndArray;
    }

    public static byte[] geoFenceId(int id) {
        int processvalue =id;
        /**
         * Convert Geofence Id to byte array.
         */
        short opCode=(short)0xa2;
        short length=(short)0x02;
        /**
         * Take a 16 byte array needed for encryption.
         */
        byte [] sendEncryptedGeoFenceId=new byte[16];
        byte[] geoFenceIdArray = intToBytes(processvalue);
        sendEncryptedGeoFenceId[0]= (byte) opCode;
        sendEncryptedGeoFenceId[1]= (byte) length;


        int geoFenceIdArray_index=0;
        for (int i = 2; i <geoFenceIdArray.length ; i++) {
            sendEncryptedGeoFenceId[i]=geoFenceIdArray[geoFenceIdArray_index];
            geoFenceIdArray_index++;
        }
        byte startingCount= (byte) (3+geoFenceIdArray.length); //Upcode ,Length.
        for (int i = startingCount; i < sendEncryptedGeoFenceId.length; i++) {
            sendEncryptedGeoFenceId[i]=0;
        }
      /*  byte[] convertedArray = intToBytes(processvalue);
        byte byte_value []=new byte[convertedArray.length+2];

        short upCode=(short)0xa2;
        byte_value[0]=(byte) upCode;
        byte_value[1]= (byte) (2);
        int count=2;
        *//**
         * fill the converted geoFenceId byte array to "byte_value" array.
         *//*
        for (int i = 0; i<convertedArray.length ; i++) {
            byte_value[count]=convertedArray[i];
            count++;
        }

        *//**
         * Take alias encryption array and fill it with the End result = "byte_value" array.
         *//*
        byte encryptionArray[]=new byte[20];
        for (int i = 0; i <=byte_value.length ; i++) {
            encryptionArray[i]=byte_value[i];
        }

        *//**
         * Fill the remaining values of the encryptionArray with 0x00;
         *//*
        for (int i = byte_value.length+1; i==encryptionArray.length ; i++) {
            encryptionArray[i]=0x00;
        }
*/
       // return byte_value;
        return sendEncryptedGeoFenceId;
    }

    /*public static byte[] integerTobyteArray(int value) {
        return new byte[]{
                (byte) (value >> 24),
                (byte) (value >> 16),
                (byte) (value >> 8),
                (byte) value
        };
    }*/

    public static byte[] intToBytes(final int data) {
        /**
         * It will convert the data to only 4 bytes.
         */
        return new byte[] {
                (byte)((data >> 0) & 0xff),
                (byte)((data >> 8) & 0xff),
                (byte)((data >> 16) & 0xff),
                (byte)((data >> 24) & 0xff),
        };
    }


    /**
     *
     * Convert the int value to 2 bytes.
     *
     */
    public static byte[] into2Bytes(final int data){
        return new byte[] {
                (byte)((data >> 8) & 0xff),
        };
    }


    public static String checkType(String typeStatus){
        String type="";
        if(typeStatus.equalsIgnoreCase("00")){
            type="Circular";
        }else {
            type="Polygon";
        }
        return type;
    }


    public static BigInteger convert4bytes(String value4bytes){
        return new BigInteger(value4bytes, 16);
    }

    /**
     *Conversion of hex to Floating point.
     * It s used to convert Lat/Long to Time Stamp.
     * link:-
     * https://gregstoll.com/~gregstoll/floattohex/
     */
    public static String getFloatingPointValueFromHex(String hexinput){
        int intbytes=(int) Long.parseLong(hexinput, 16);
        float floatbytes = Float.intBitsToFloat(intbytes);
        return ""+floatbytes;
    }

    /**
     * This method is used to send Ack to Firmware that data is avaliable in Database.
     * Application is Ready to process the Next GeoFence alert packet.
     */

    public static byte[] sendAckReadyForNextPacket(){
        byte byte_value []=new byte[16]; // make constant that packet size should be 20 bytes.
        byte_value[0]= (byte) 0xa5;
        byte_value[1]=0x01;
        /**
         * Logic to fill the remaining Bit to 0 for Encryption.
         */
        for (int i = 2; i<byte_value.length ; i++) {
            byte_value[i]=0x00;
        }
        return  byte_value;
    }


    /**
     *
     * This packet is used for asking Id_timeStamp from the geoFence
     */
    public static byte[] askForGeoFenceId_timeStamp(){
        byte byte_value []=new byte[16]; // make constant that packet size should be 20 bytes.
        byte_value[0]= (byte) 0xa4;
        byte_value[1]=0x01;
        for (int i = 2; i <byte_value.length ; i++) {
            byte_value[i]=0x00;
        }
        return  byte_value;
    }

    /**
     * Convert 4 byte HexValue to long.
     * Its generally used to convert HexValue to Timestamp.
     */
    public static long convertHexToLong(String hexValue){
        return  new BigInteger(hexValue, 16).longValue();
    }

    /**
     * send acknoledgement to the firmware.
     * i.e application is ready to recieve the alerts sent by the firmware.
     */

    public static byte[] sendAckReadToRecieveGeoFenceAlerts(){
        byte byte_value []=new byte[16]; // make constant that packet size should be 20 bytes.
        byte_value[0]= (byte) 0xa4;
        byte_value[1]= (byte) 0xff;
        /**
         * Logic to fill the remaining Bit to 0 for Encryption.
         */
        for (int i = 2; i<byte_value.length ; i++) {
            byte_value[i]=0x00;
        }
        return  byte_value;
    }

    /**
     * Finished A8 Packet alert Acknoledgement.
     */

    public static byte [] sendAckFinishedA8Packet(){
        byte byte_value []=new byte[20]; // make constant that packet size should be 20 bytes.
        byte_value[0]= (byte) 0xa8;
        byte_value[1]= (byte) 0xff;
        /**
         * Logic to fill the remaining Bit to 0 for Encryption.
         */
        for (int i = 2; i==byte_value.length ; i++) {
            byte_value[i]=0x00;
        }

        return  byte_value;
    }


    /**
     *
     * Send A4FF packet after finishing and sync all geoFence Id.
     */
    public static  byte [] send_Geo_fenceID_fetched_finished_Acknoledgement(byte ack_byte){
        byte byte_value []=new byte[16]; // make constant that packet size should be 20 bytes.
        byte_value[0]= ack_byte;
        byte_value[1]= (byte) 0xff;
        /**
         * Logic to fill the remaining Bit to 0 for Encryption.
         */
        for (int i = 2; i<byte_value.length ; i++) {
            byte_value[i]=0x00;
        }
        return  byte_value;
    }


    public static byte[] set_firmwareTimeStamp(int timeStamp){
        byte byte_value []=new byte[16]; // make constant that packet size should be 20 bytes.
        byte_value[0]= (byte) 0xa7;
        byte_value[1]= (byte) 3;
        byte_value[2]= (byte) timeStamp;
        /**
         * Logic to fill the remaining Bit to 0 for Encryption.
         */
        for (int i = 3; i==byte_value.length ; i++) {
            byte_value[i]=0x00;
        }
        return byte_value;
    }
//test changes
}
