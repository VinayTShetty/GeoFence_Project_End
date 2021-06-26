package com.succorfish.geofence.blecalculation;
public class DeviceTokenPacket {

    public static byte[] deviceTokenpacketArray(int packetNumber,String deviceTokenData){
        byte [] deviceToken=new byte[16];
        byte [] deviceTokenArray=deviceTokenData.getBytes();
        deviceToken[0]=(byte) 0XE2;
        /**
         * length:-
         * packet number(As its fixed)
         * Token data packets.
         */
        int datalength_packetLength_TokenDataLength=deviceTokenArray.length+1;
        deviceToken[1]= (byte) datalength_packetLength_TokenDataLength;
        deviceToken[2]= (byte) packetNumber;

        int indexPositionToStart=3;
        for (int i = 0; i <deviceTokenArray.length ; i++) {
            deviceToken[indexPositionToStart]=deviceTokenArray[i];
            indexPositionToStart++;
        }
        return deviceToken;
    }
}
