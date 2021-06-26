package com.succorfish.geofence.blecalculation;

import java.util.ArrayList;

public class IndustrySpecificConfiguration {
    public static byte [] industrySpecifConfigDataSet(byte flightMode,byte garageMode,byte GIGOmode,byte depthTemperatureMode){
        byte [] industrySpecificConfiguration=new byte[16];
        byte command= (byte) 0XC2;
        /**
         * Data length is Fixed so passing 4.
         * Flight Mode.
         * Garage Mode
         * GIGO  Mode
         * Depth temperature mode
         *
         */
        byte dataLength= (byte) 4;
        industrySpecificConfiguration[0]=command;
        industrySpecificConfiguration[1]=dataLength;
        industrySpecificConfiguration[2]=flightMode;
        industrySpecificConfiguration[3]=garageMode;
        industrySpecificConfiguration[4]=GIGOmode;
        industrySpecificConfiguration[5]=depthTemperatureMode;
        return industrySpecificConfiguration;
    }

    public static ArrayList<byte[]> industrySpecificConfigurationPacket(byte flightMode,byte garageMode,byte GIGOmode,byte depthTemperatureMode){
        ArrayList<byte[]> industrySpecifcList=new ArrayList<byte[]>();
        byte[] indistrySpecificArray=new byte[16];
        byte command= (byte) 0XC2;
        /**
         * Data length is Fixed so passing 4.
         * Flight Mode.
         * Garage Mode
         * GIGO  Mode
         * Depth temperature mode
         *
         */
        byte dataLength= (byte) 4;
        indistrySpecificArray[0]=command;
        indistrySpecificArray[1]=dataLength;
        indistrySpecificArray[2]=flightMode;
        indistrySpecificArray[3]=garageMode;
        indistrySpecificArray[4]=GIGOmode;
        indistrySpecificArray[5]=depthTemperatureMode;
        industrySpecifcList.add(indistrySpecificArray);
        return industrySpecifcList;
    }

}
