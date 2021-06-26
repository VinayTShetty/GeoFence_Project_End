package com.succorfish.geofence.blecalculation;

import java.util.ArrayList;

public class WifiConfiguration {
    public static byte[] wifiConfigurationDataSet(byte enable_disableWIFI,byte logFimware_WIFI){
        byte[] dataArray=new byte[16];
        dataArray[0]=(byte)0XC6;
        /**
         * Data length
         * Enable/Disbale wifi
         * WifiLogging Firmware
         *
         * Here Data length is fixed so.Making it to 2.
         */
        byte dataLength=2;
        dataArray[1]=(byte)0X02;
        dataArray[2]=(byte)enable_disableWIFI;
        dataArray[3]=(byte)logFimware_WIFI;
        return dataArray;
    }


    public static ArrayList<byte[]> wifiConfigurationDataArrayList(byte enable_disableWIFI,byte logFimware_WIFI){
        ArrayList<byte[]> wifiConfigArrayList=new ArrayList<byte[]>();
        byte[] dataArray=new byte[16];
        dataArray[0]=(byte)0XC6;
        /**
         * Data length
         * Enable/Disbale wifi
         * WifiLogging Firmware
         *
         * Here Data length is fixed so.Making it to 2.
         */
        byte dataLength=2;
        dataArray[1]=(byte)0X02;
        dataArray[2]=(byte)enable_disableWIFI;
        dataArray[3]=(byte)logFimware_WIFI;
        wifiConfigArrayList.add(dataArray);
        return  wifiConfigArrayList;
    }
}
