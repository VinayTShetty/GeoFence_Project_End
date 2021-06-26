package com.vithamastech.smartlight.PowerSocketUtils;

public class Constants {
    public static final byte[] defaultKey = new byte[]{0x3A, 0x09, 0x44, 0x62, (byte) 0xFD, 0x62, 0x10, (byte) 0xCD, (byte) 0xE8, 0x74, 0x42, (byte) 0xCA, (byte) 0xA9, (byte) 0xD7, 0x18, (byte) 0xF9};
    public static final String alertTitle = "Power Socket";
    public static final String ConnectionSuccessMessage = "Device Connected successfully";

    public static final int AUTHENTICATION_SUCCESS = 1;
    public static final int AUTHENTICATION_FAILURE = 0;

    public static final int SUCCESS = 1;
    public static final int FAILURE = 0;

    public static final int ACTIVE = 1;
    public static final int INACTIVE = 0;

    public static final int ASSOCIATED = 0x1700;
    public static final int NOT_ASSOCIATED = 0x3000;

    public static final int SCANNING_IN_PROGRESS = 1;
    public static final int RESET_IN_PROGRESS = 2;
    public static final int SCANNING_STOPPED = 3;

    // This is used to find out whether device is associated before decrypting the manufacturer data in advertisement packet
    public static final int SELF_DEVICE_ID_ASSOCIATED = 0x01;
    // This is used to find out whether device is associated before decrypting the manufacturer data in advertisement packet
    public static final int SELF_DEVICE_ID_NOT_ASSOCIATED = 0x00;

    // Delay simulation to delete a power socket as per H/w requirement
    public static final int DELETE_DELAY = 10000;

    private static final String mainKeySkipUserStr = "~vith";
    public static final byte[] mainKeySkipUser = ByteConverter.extractLSB(HashUtils.getSHA256Hash(mainKeySkipUserStr), 16);

    public static final String PUBLISH_TOPIC_BASE_URI = "/vps/app/";
    public static final String SUBSCRIBE_TOPIC_BASE_URI = "/vps/device/";
    public static final String DUMMY_PUBLISH_TOPIC = PUBLISH_TOPIC_BASE_URI + "7C9EBDD83E90";
    public static final String DUMMY_SUBSCRIBE_TOPIC = SUBSCRIBE_TOPIC_BASE_URI + "7C9EBDD83E90";
}