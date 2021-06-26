package com.succorfish.geofence.blecalculation;

import java.util.ArrayList;

public class LiveLocationPacketManufacturer {
    public static final  byte LIVE_TRACKING_UP_CODE= (byte) 0XE3;
    public static final  byte LIVE_TRACKING_START= (byte) 0X01;
    public static final  byte LIVE_TRACKING_STOP= (byte) 0X00;

    public static byte [] Start_Stop_LIVE_LOCATION(boolean start_stop){
        byte [] startLiveLocationPacket=new byte[16];
        startLiveLocationPacket[0]=LIVE_TRACKING_UP_CODE;
        /**
         * Data length
         */
        startLiveLocationPacket[1]=0X01;
        if(start_stop){
            startLiveLocationPacket[2]=0X01;
        }else {
            startLiveLocationPacket[2]=0X00;
        }
        return startLiveLocationPacket;
    }
}
