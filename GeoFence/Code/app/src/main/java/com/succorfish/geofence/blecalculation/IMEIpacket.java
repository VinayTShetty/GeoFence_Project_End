package com.succorfish.geofence.blecalculation;

import java.util.ArrayList;

public class IMEIpacket {
    public static byte [] askIMEI_number(){
        byte [] imei=new byte[16];
        imei[0]=(byte)0xe1;
        return  imei;
    }
}
