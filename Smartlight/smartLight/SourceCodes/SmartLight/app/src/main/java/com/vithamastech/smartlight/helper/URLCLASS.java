package com.vithamastech.smartlight.helper;

import com.vithamastech.smartlight.BuildConfig;

/**
 * Created by Jaydeep on 13-03-2018.
 * /*Global constant used in app
 */
public class URLCLASS {

    // Constant Filed
    public static String MAIN_URL = "http://vithamastech.com/smartlight/api/";
//    public static String DIRECTORY_FOLDER_NAME = "/VithamasGloSmart";
    public static long DOORBELL_APP_ID = 9306;
    public static String DOORBELL_API_KEY = BuildConfig.DoorbellApiKey;
    public static String APP_SKIP_PW = "~vith";
    public static long ADVERTISE_MANUAL_TIMEOUT = 820;

    // Light Sync States
    public static String LIGHT_SYNC_STATE = "1800";
    // Association Msg
    public static short ASSOC_REQ_PART_1 = 0x0031;
    public static short ASSOC_REQ_PART_2 = 0x0032;
    public static String ASSOC_COMPLETE_RSP = "3600";
    public static short ASSOC_REMOVE_REQ = 0x0037;
    public static String ASSOC_REMOVE_RSP = "3800";
    public static String ASSOC_ALREADY_ASS_RSP = "1700";
    public static String ASSOC_NON_CONNECTABLE_RSP = "3000";
    public static short DEVICE_CHECK_REQ = 0x0034;
    public static short DEVICE_CHECK_RSP = 0x0035;

    // GROUP Msg
    public static short GROUP_ADD_REQ = 0x0008;
    public static String GROUP_ADD_RSP = "0900";
    public static short GROUP_REMOVE_REQ = 0x000A;
    public static String GROUP_REMOVE_RSP = "0b00";

    // ALARM Msg
    public static short ALARM_ADD_REQ = 0x0061;
    public static String ALARM_ADD_RSP = "6200";
    public static short ALARM_REMOVE_REQ = 0x0063;
    public static String ALARM_REMOVE_RSP = "6400";
    public static short ALARM_SET_CURRENT_TIME_REQ = 0x0060;

    // COLOR Msg
    public static short COLOR_WHITE_LIGHT_LEVEL = 0x0052;
    public static short COLOR_CHANGE_COLOR = 0x0042;
    public static short COLOR_CHANGE_PATTERN = 0x0043;
    public static short COLOR_WHITE_LIGHT = 0x0046;
    public static short COLOR_WHITE_LIGHT_45 = 0x0045;
    public static short COLOR_ON_OFF_LIGHT = 0x0055;
    public static short COLOR_MUSIC_LIGHT = 0x0048;

    public static short COLOR_LIGHT_STATE = 0x0047;
    //  Ping Msg
    public static short PING_REQ = 0x0070;
    public static String PING_RSP = "0071";

    //

    // Device Type

}
