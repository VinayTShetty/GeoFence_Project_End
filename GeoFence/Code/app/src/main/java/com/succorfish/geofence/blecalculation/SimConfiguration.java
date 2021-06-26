package com.succorfish.geofence.blecalculation;

import java.nio.ByteBuffer;

import static com.succorfish.geofence.blecalculation.ByteConversion.convert_LongTo_4_bytes;

public class SimConfiguration {

    public static byte [] StartSimConfigurationFristPacket(String Esim_NANOsim,byte UART_configValue,int band_configValue){
        byte [] startPacket=new byte[16];
        startPacket[0]=(byte)0XC3;//command.
        /**
         * data length:-
         * opcode-->1
         * sim-->1
         * uartconfiguration-->1
         * BandConfig-->4
         */
        startPacket[1]=(byte)0x07;//data length.Kept it as constant 7.as its fixed as per the documentation.
        startPacket[2]=(byte)0x01;
        if(Esim_NANOsim.equalsIgnoreCase("E_SIM")){
            startPacket[3]=(byte)0x00;
        }else if(Esim_NANOsim.equalsIgnoreCase("NANO_SIM")){
            startPacket[3]=(byte)0x01;
        }
        startPacket[4]=UART_configValue;
        int startIndex=5;
     //   byte [] bandConfigArray=convert_LongTo_4_bytes(band_configValue);
        Integer reversedbytes=Integer.reverseBytes(band_configValue);
        byte [] bandConfigArray= ByteBuffer.allocate(4).putInt(reversedbytes).array();


        for (int i = 0; i < bandConfigArray.length; i++) {
            startPacket[startIndex]=bandConfigArray[i];
                    startIndex++;
        }
        return startPacket;
    }

    public static byte[] endPacketSimConfiguration(){
        byte[] endPacket=new byte[16];
        endPacket[0]=(byte)0xc3;
        endPacket[1]=(byte)0x02;
        endPacket[2]=(byte)0x05;
        endPacket[3]=(byte)0x01;
        return endPacket;
    }

    public static byte[]  simConfigurationDataArray(byte opcode,int packetNumber,String dataTobeParsed){
        byte[] messageDataArray=new byte[16];
        byte [] stringDataArray=dataTobeParsed.getBytes();
        messageDataArray[0]=(byte)0xc3; // command
        /**
         * length:-
         * opcode
         * packetNumber
         * packetNumberOccupiedForStrings(stringDataArray.length())
         */
        byte dataLength= (byte) (1+1+stringDataArray.length);
        messageDataArray[1]=dataLength;
        messageDataArray[2]=opcode;
        messageDataArray[3]=(byte)packetNumber;
        int startIndex=4;
        for (int i = 0; i <stringDataArray.length ; i++) {
            messageDataArray[startIndex]=stringDataArray[i];
            startIndex++;
        }
        return messageDataArray;
    }
}
