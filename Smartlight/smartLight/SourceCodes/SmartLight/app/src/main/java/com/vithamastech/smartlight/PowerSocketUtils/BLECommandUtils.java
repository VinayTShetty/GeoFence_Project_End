package com.vithamastech.smartlight.PowerSocketUtils;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class BLECommandUtils {
    public static final UUID SERVICE_UUID = UUID.fromString("0000ab01-2687-4433-2208-abf9b34fb000");
    public static final UUID CONTROL_CHARACTERISTIC_UUID = UUID.fromString("0000ab00-2687-4433-2208-abf9b34fb000");
    public static final UUID RESET_CHARACTERISTIC_UUID = UUID.fromString("0000ab02-2687-4433-2208-abf9b34fb000");

    // Without encryption
    public static final byte READ_AUTHENTICATION_NUMBER = 0x01;
    public static final byte WRITE_AUTHENTICATION_VALUE = 0x02;

    // With encryption
    public static final byte SET_UTC_TIME = 0x03;
    public static final byte GET_UTC_TIME = 0x04;
    public static final byte CHECK_SOCKET_DIAGNOSTIC = 0x05;
    public static final byte ADD_DEVICE_REGISTRATION_INFO = 0x06;       //
    public static final byte DELETE_DEVICE_REGISTRATION_INFO = 0x07;
    public static final byte VERIFY_DEVICE_REGISTRATION_INFO = 0x08;    // Encryption with default/cpmmon key
    public static final byte POWER_ON_OFF_SINGLE_SOCKET = 0x09;
    public static final byte POWER_ON_OFF_ALL_SOCKET = 0x0A;
    public static final byte SET_WIFI_SSID = 0x0D;
    public static final byte SET_WIFI_PASSWORD = 0x0E;
    public static final byte SET_MQTT_URI = 0x0F;
    public static final byte WIFI_AND_MQTT_STATE_INFO = 0x10;
    public static final byte REQUEST_SSID_LIST = 0x12;
    public static final byte DELETE_WIFI_SSID_PASSWORD = 0x1A;
    public static final byte REQUEST_ALARM_SET = 0x0B;
    public static final byte DELETE_ALARM = 0x0C;
    public static final byte REQUEST_ALARM_DETAILS = 0x15;
    public static final byte RESET_POWER_SOCKET = 0x24;
    public static final byte GET_CURRENT_FIRMWARE_VERSION = 0x19;
    public static final byte DEVICE_RESET_STATE = 0x11;
    public static final byte FACTORY_SOCKET_TEST = 0x23;
    public static final byte OTA_UPDATE = 0x21;

    public static final byte SUCESS = 0X01;
    public static final byte FAILURE = 0X00;

    private static final String OTA_BASE_URL = "http://vithamastech.com/vps_ota/VPS-v";
    private static final String FILE_EXTENSION = ".bin";

    public static byte[] getAuthenticationNumberRequestPacket(byte[] encryptionKey) {
        return createPacket(READ_AUTHENTICATION_NUMBER, null, encryptionKey);
    }

    public static byte[] getAuthenticationValueVerificationPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(WRITE_AUTHENTICATION_VALUE, data, encryptionKey);
    }

    public static byte[] getDeviceRegistrationInfoRequestPacket(byte[] encryptionKey) {
        return createPacket(VERIFY_DEVICE_REGISTRATION_INFO, null, encryptionKey);
    }

    public static byte[] getDeviceRegistrationInfoDeleteRequestPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(DELETE_DEVICE_REGISTRATION_INFO, data, encryptionKey);
    }

    public static byte[] getWifiAndMqttStateRequestPacket(byte[] encryptionKey) {
        return createPacket(WIFI_AND_MQTT_STATE_INFO, null, encryptionKey);
    }

    public static byte[] getDeviceAddRegistrationPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(ADD_DEVICE_REGISTRATION_INFO, data, encryptionKey);
    }

    public static byte[] getAlarmDetails(byte[] data, byte[] encryptionKey) {
        return createPacket(REQUEST_ALARM_DETAILS, data, encryptionKey);
    }

    public static byte[] getURLConfigSetPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(SET_MQTT_URI, data, encryptionKey);
    }

    public static byte[] getWifiSSIDConfigSetPacket(int wifiIndex, byte[] encryptionKey) {
        byte[] data = ByteConverter.convertIntToByteArray(wifiIndex, 1);
        return createPacket(SET_WIFI_SSID, data, encryptionKey);
    }

    public static byte[] getWifiPasswordConfigSetPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(SET_WIFI_PASSWORD, data, encryptionKey);
    }

    public static byte[] getSocketStatesRequestPacket(int transactionId, byte[] encryptionKey) {
        byte[] data = ByteConverter.convertIntToByteArray(transactionId, 2);
        return createPacket(CHECK_SOCKET_DIAGNOSTIC, data, encryptionKey);
    }

    public static byte[] getSocketControlCommandPacket(byte state, int transactionId, byte[] encryptionKey) {
        byte[] switchControlState = new byte[1];
        switchControlState[0] = state;                                            // Control state ON/OFF

        byte[] transactionIdBytes = ByteConverter.convertIntToByteArray(transactionId, 2);
        byte[] data = new byte[3];
        System.arraycopy(switchControlState, 0, data, 0, switchControlState.length);
        System.arraycopy(transactionIdBytes, 0, data, 1, transactionIdBytes.length);

        return createPacket(POWER_ON_OFF_ALL_SOCKET, data, encryptionKey);
    }

    public static byte[] getSocketControlCommandPacket(byte socketId, byte state, int transactionId, byte[] encryptionKey) {
        byte[] switchControlState = new byte[2];
        switchControlState[0] = socketId;                                         // Socket ID
        switchControlState[1] = state;                                            // Control state ON/OFF

        byte[] transactionIdBytes = ByteConverter.convertIntToByteArray(transactionId, 2);
        byte[] data = new byte[4];
        System.arraycopy(switchControlState, 0, data, 0, switchControlState.length);
        System.arraycopy(transactionIdBytes, 0, data, 2, transactionIdBytes.length);

        return createPacket(POWER_ON_OFF_SINGLE_SOCKET, data, encryptionKey);
    }

    public static byte[] getPowerSocketResetCommandPacket(byte[] encryptionKey) {
        byte[] resetCommandData = new byte[2];
        resetCommandData[0] = 0x01;
        resetCommandData[1] = 0x01;
        return createPacket(RESET_POWER_SOCKET, resetCommandData, encryptionKey);
    }

    public static byte[] getSSIDListRequestPacket(byte[] encryptionKey) {
        return createPacket(REQUEST_SSID_LIST, null, encryptionKey);
    }

    public static byte[] getDeleteWifiSSIDPasswordPacket(byte[] encryptionKey) {
        return createPacket(DELETE_WIFI_SSID_PASSWORD, null, encryptionKey);
    }

    public static byte[] getConfigUTCTimeRequestPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(SET_UTC_TIME, data, encryptionKey);
    }

    public static byte[] getUTCTimeRequestPacket(byte[] encryptionKey) {
        return createPacket(GET_UTC_TIME, null, encryptionKey);
    }

    public static byte[] getCurrentFirmwareVersionRequestPacket(byte[] encryptionKey) {
        return createPacket(GET_CURRENT_FIRMWARE_VERSION, null, encryptionKey);
    }

    public static byte[] getDeviceFactorySocketsTestPacket(byte[] encryptionKey) {
        return createPacket(FACTORY_SOCKET_TEST, null, encryptionKey);
    }

    public static byte[] getOTAUpdateRequestPacket(String firmwareVersion, byte[] encryptionKey) {
        String finalUrl = OTA_BASE_URL + firmwareVersion + FILE_EXTENSION;
        byte[] finalUrlBytes = finalUrl.getBytes(StandardCharsets.UTF_8);
        return createPacket(OTA_UPDATE, finalUrlBytes, encryptionKey);
    }

    public static byte[] getAlaramSetPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(REQUEST_ALARM_SET, data, encryptionKey);
    }

    public static byte[] getDeleteAlarampPacket(byte[] data, byte[] encryptionKey) {
        return createPacket(DELETE_ALARM, data, encryptionKey);
    }

    private static byte[] createPacket(int opcode, byte[] data, byte[] encryptionKey) {
        byte[] retVal;
        switch (opcode) {
            case SET_UTC_TIME:
            case GET_UTC_TIME:
            case CHECK_SOCKET_DIAGNOSTIC:
            case ADD_DEVICE_REGISTRATION_INFO:
            case DELETE_DEVICE_REGISTRATION_INFO:
            case VERIFY_DEVICE_REGISTRATION_INFO:
            case POWER_ON_OFF_SINGLE_SOCKET:
            case POWER_ON_OFF_ALL_SOCKET:
            case SET_WIFI_SSID:
            case SET_WIFI_PASSWORD:
            case SET_MQTT_URI:
            case WIFI_AND_MQTT_STATE_INFO:
            case REQUEST_SSID_LIST:
            case DELETE_WIFI_SSID_PASSWORD:
            case REQUEST_ALARM_SET:
            case DELETE_ALARM:
            case REQUEST_ALARM_DETAILS:
            case RESET_POWER_SOCKET:
            case GET_CURRENT_FIRMWARE_VERSION:
            case DEVICE_RESET_STATE:
            case FACTORY_SOCKET_TEST:
            default:
                retVal = new byte[20];
                break;
            case OTA_UPDATE:
                retVal = new byte[50];
                break;
        }

        retVal[0] = (byte) opcode;                                               // index 0 - opcode
        if (data != null) {
            retVal[1] = (byte) data.length;                                      // index 1 - data length
            if (encryptionKey != null && encryptionKey.length == 16) {
                try {
                    byte[] dataWithPadding = new byte[16];
                    System.arraycopy(data, 0, dataWithPadding, 0, data.length);
                    byte[] encryptedData = EncryptionUtils.encryptData(dataWithPadding, encryptionKey);
                    System.arraycopy(encryptedData, 0, retVal, 2, encryptedData.length);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }
            } else {
                System.arraycopy(data, 0, retVal, 2, data.length);
            }
        } else {
            retVal[1] = 0;                                                     // index 1 - data length
        }
        return retVal;
    }
}