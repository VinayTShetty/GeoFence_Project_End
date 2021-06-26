package com.succorfish.geofence.blecalculation;

import java.util.ArrayList;

public class ResetDevice {
    public static ArrayList<byte[]> resetDeviceFirmware(){
        ArrayList<byte[]> resetArraylist=new ArrayList<byte[]>();
        byte[] resetPacket=new byte[16];
        resetPacket[0]= (byte) 0XC5;// command
        resetPacket[1]= (byte) 0X01;// DataLength
        resetPacket[2]= (byte) 0X01;// data
        resetArraylist.add(resetPacket);
        return  resetArraylist;
    }
}
