package com.vithamastech.smartlight.services;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.ContentValues;
import android.content.Context;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.vithamas.blecommmodule.Centralmodule.BleCentralManager;
import com.vithamas.blecommmodule.Centralmodule.interfaces.BleCentralManagerCallbacks;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocketAlaramDetails;
import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;
import com.vithamastech.smartlight.PowerSocketUtils.BLECommandUtils;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.PowerSocketUtils.Constants;
import com.vithamastech.smartlight.PowerSocketUtils.EncryptionUtils;
import com.vithamastech.smartlight.PowerSocketUtils.HashUtils;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketConstants;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.helper.PreferenceHelper;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import static com.vithamastech.smartlight.PowerSocketUtils.BLECommandUtils.FAILURE;
import static com.vithamastech.smartlight.PowerSocketUtils.BLECommandUtils.REQUEST_ALARM_DETAILS;
import static com.vithamastech.smartlight.PowerSocketUtils.BLECommandUtils.SUCESS;
import static com.vithamastech.smartlight.db.DBHelper.mTableAlarmPowerSocket;
import static com.vithamastech.smartlight.db.DBHelper.mfield_OffTimestamp;
import static com.vithamastech.smartlight.db.DBHelper.mfield_Off_original;
import static com.vithamastech.smartlight.db.DBHelper.mfield_OnTimestamp;
import static com.vithamastech.smartlight.db.DBHelper.mfield_On_original;
import static com.vithamastech.smartlight.db.DBHelper.mfield_alarm_id;
import static com.vithamastech.smartlight.db.DBHelper.mfield_alarm_state;
import static com.vithamastech.smartlight.db.DBHelper.mfield_ble_address;
import static com.vithamastech.smartlight.db.DBHelper.mfield_day_selected;
import static com.vithamastech.smartlight.db.DBHelper.mfield_day_value;
import static com.vithamastech.smartlight.db.DBHelper.mfield_socket_id;

public class PowerSocketBLEService {
    public static final String TAG = PowerSocketBLEService.class.getSimpleName();
    private static PowerSocketBLEService instance;
    private BleCentralManager bleCentralManager;
    private PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks;
    private PowerSocket selectedPowerSocket;
    private List<byte[]> wifiRawDataBuffer;
    private boolean wifiRequestStatus;
    private WifiDevice selectedWifiDevice;
    private String selectedPassword;
    private Queue<byte[]> passwordPackets;
    private int totalPasswordPacketSize = 0;
    private Handler handler;
    private WifiConfigTimeoutTask wifiConfigTimeoutTask;
    private DeleteTimeoutTask deleteTimeoutTask;
    private ResetTimeoutTask resetTimeoutTask;
    private FactorySocketTestTimeoutTask factorySocketTestTimeoutTask;
    private WifiSearchTimeoutTask wifiSearchTimeoutTask;

    private PreferenceHelper preferenceHelper;

    private static ArrayList<PowerSocketAlaramDetails> powerSocketAlaramDetailsArrayList = new ArrayList<PowerSocketAlaramDetails>();
    private ArrayList<PowerSocketAlaramDetails> powerSocketAlaramSetArrayList = new ArrayList<PowerSocketAlaramDetails>();
    private static final int MAX_WIFI_CONFIG_TIMEOUT = 30000;
    private static final int MAX_DELETE_DEVICE_TIMEOUT = 10000;
    private static final int MAX_RESET_DEVICE_TIMEOUT = 10000;
    private static final int MAX_FACTORY_SOCKET_TIMEOUT = 30000;
    private static final int MAX_WIFI_SEARCH_TIMEOUT = 50000;

    private boolean isUserSetTimeSelected = true;  // Default true

    private PowerSocketBLEService(Context context) {
        bleCentralManager = BleCentralManager.getInstance(context);
        bleCentralManager.setAllowOperationsOnUIThread(true);
        bleCentralManager.setRetryCount(0);
        handler = new Handler();
        wifiSearchTimeoutTask = new WifiSearchTimeoutTask(wifiSearchTimeoutCallback);
        wifiConfigTimeoutTask = new WifiConfigTimeoutTask(wifiConfigTimeoutCallback);
        deleteTimeoutTask = new DeleteTimeoutTask(onDeleteTimeoutCallbacks);
        resetTimeoutTask = new ResetTimeoutTask(resetTimeoutCallbacks);
        factorySocketTestTimeoutTask = new FactorySocketTestTimeoutTask(factorySocketTestTimeoutCallbacks);
        bleCentralManager.setOnBluetoothCentralListener(callbacks);
        preferenceHelper = new PreferenceHelper(context.getApplicationContext());
    }

    public static PowerSocketBLEService getInstance(Context context) {
        if (instance == null) {
            instance = new PowerSocketBLEService(context);
        }
        return instance;
    }

    public boolean isDeviceConnected(String deviceAddress) {
        console.log("PowerSocketBLEService_connect", "Querying is device connected for " + deviceAddress);
        return bleCentralManager.isDeviceConnected(deviceAddress);
    }

    public void setOnPowerSocketEventCallbacks(PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks) {
        this.powerSocketBLEEventCallbacks = powerSocketBLEEventCallbacks;
    }

    BleCentralManagerCallbacks callbacks = new BleCentralManagerCallbacks() {

        @Override
        public void onDeviceDisconnected(String deviceAddress, int errorCode) {
            console.log("PowerSocketBLEService_callbacks", "On Device Disconnected => " + deviceAddress + "Error Code = " + errorCode);
            handler.removeCallbacks(wifiConfigTimeoutTask);
//            handler.removeCallbacks(resetTimeoutTask);
//            handler.removeCallbacks(deleteDelayTask);
            handler.removeCallbacks(factorySocketTestTimeoutTask);
            handler.removeCallbacks(wifiSearchTimeoutTask);
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onDeviceDisconnected(deviceAddress);
            }
        }

        @Override
        public void onBluetoothRestart() {
            super.onBluetoothRestart();
            handler.removeCallbacks(wifiConfigTimeoutTask);
            handler.removeCallbacks(deleteTimeoutTask);
            handler.removeCallbacks(resetTimeoutTask);
            handler.removeCallbacks(factorySocketTestTimeoutTask);
            handler.removeCallbacks(wifiSearchTimeoutTask);
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onBluetoothRestart();
            }
        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {
            console.log("PowerSocketBLEService_callbacks", "On Device Connection Failed => " + deviceAddress);
            handler.removeCallbacks(wifiConfigTimeoutTask);
            handler.removeCallbacks(deleteTimeoutTask);
            handler.removeCallbacks(resetTimeoutTask);
            handler.removeCallbacks(factorySocketTestTimeoutTask);
            handler.removeCallbacks(wifiSearchTimeoutTask);
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onDeviceConnectionFailed(deviceAddress);
            }
        }

        @Override
        public void onDeviceConnectedAndReadyToCommunicate(String deviceAddress, Map<String, ArrayList<String>> serviceTable) {
            console.log("PowerSocketBLEService_callbacks", "On Device Connected => " + deviceAddress);
            console.log("PowerSocketBLEService_Authentication", "Initiating Authentication");
            // Initiate Authentication
            byte[] encryptionKey = getEncryptionKey(selectedPowerSocket.isAssociated);
            byte[] authRequestPacket = BLECommandUtils.getAuthenticationNumberRequestPacket(encryptionKey);
            bleCentralManager.sendData(deviceAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                    authRequestPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onCharacteristicUpdate(String deviceAddress, UUID serviceUUID, UUID characteristicUUID, byte[] rawData) {
            super.onCharacteristicUpdate(deviceAddress, serviceUUID, characteristicUUID, rawData);
            console.log("PowerSocketBLEService_callbacks", "onCharacteristicUpdate = " + ByteConverter.getHexStringFromByteArray(rawData, true));
            if (rawData != null) {

                byte opcode = rawData[0];                           // Index 0 - Opcode
                int dataLength = rawData[1] & 0xFF;                 // Index 1 - Data Length

                // This is during Searching for Wi-Fi SSIDs the Power socket want to connect to
                if (dataLength == 0xEE || dataLength == 0xFF) {
                    dataLength = 2;
                }
                int dataSourceIdx = 0;
                int dataDestIdx = dataSourceIdx + dataLength;

                int encryptedDataSrcIdx = 2;
                int encryptedDataLength = 16;
                int encryptedDataDestIdx = encryptedDataSrcIdx + encryptedDataLength;

                byte[] encryptedData = ByteConverter.copyOfRange(rawData, encryptedDataSrcIdx, encryptedDataDestIdx);

                console.log("PowerSocketBLEService_Received_Data", "Is Associated = " + selectedPowerSocket.isAssociated);
                byte[] encryptionKey = getEncryptionKey(selectedPowerSocket.isAssociated);

                byte[] decryptedData = null;
                try {
                    decryptedData = EncryptionUtils.decryptData(encryptedData, encryptionKey);
                } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                    e.printStackTrace();
                }

                byte[] data = null;
                if (decryptedData != null) {
                    data = ByteConverter.copyOfRange(decryptedData, dataSourceIdx, dataDestIdx);
                    console.log("PowerSocketBLEService_Received Data", "Decrypted Data = " + ByteConverter.getHexStringFromByteArray(data, true));
                }

                switch (opcode) {
                    // Receives Auth key from BLE device/peripheral
                    case BLECommandUtils.READ_AUTHENTICATION_NUMBER: {
                        // Get Auth Key int value
                        int authKey = ByteConverter.convertByteArrayToInt(data);    // data => AuthKeyBytes
                        // Get Authentication value (Hash) from Auth Number
                        int authValue = HashUtils.getAuthValue(authKey);
                        // Convert authValue into byte array
                        byte[] authValueBytes = ByteConverter.convertIntToByteArray(authValue);
                        byte[] authValueBytesReversed = ByteConverter.reverseBytes(authValueBytes);
                        // Construct a packet to send Authentication value calculated.
                        byte[] authValueVerificationPacket = BLECommandUtils.getAuthenticationValueVerificationPacket(authValueBytesReversed, encryptionKey);
                        bleCentralManager.sendData(deviceAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                                authValueVerificationPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                    }
                    break;

                    case BLECommandUtils.WRITE_AUTHENTICATION_VALUE: {
                        int authResponseStatus = ByteConverter.convertByteArrayToInt(data);
                        switch (authResponseStatus) {
                            case Constants.AUTHENTICATION_SUCCESS:
//                                // Set UTC Timestamp
                                setCurrentTime(selectedPowerSocket, false);
                                break;
                            case Constants.AUTHENTICATION_FAILURE:
                            default:
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onAuthenticationFailure(deviceAddress);
                                }
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.SET_UTC_TIME: {
                        int currentTimeResponseStatus = ByteConverter.convertByteArrayToInt(data);
                        switch (currentTimeResponseStatus) {
                            case Constants.SUCCESS: // Success
                                if (powerSocketBLEEventCallbacks != null) {
                                    if (isUserSetTimeSelected) {
                                        powerSocketBLEEventCallbacks.onCurrentTimeSet(deviceAddress);
                                    } else {
                                        powerSocketBLEEventCallbacks.onAuthenticationSuccess(deviceAddress);
                                    }
                                }
                                break;
                            case Constants.FAILURE: //  Failure
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.VERIFY_DEVICE_REGISTRATION_INFO: {
                        int deviceRegistrationStatus = ByteConverter.convertByteArrayToInt(data);
                        switch (deviceRegistrationStatus) {
                            case Constants.SUCCESS: // Successfully Registered
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceAlreadyRegistered(deviceAddress);
                                }
                                break;
                            case Constants.FAILURE: // Not Registered
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceNotRegistered(deviceAddress);
                                }
                        }
                    }
                    break;

                    case BLECommandUtils.ADD_DEVICE_REGISTRATION_INFO: {
                        int addDeviceStatus = ByteConverter.convertByteArrayToInt(data);
                        switch (addDeviceStatus) {
                            case Constants.SUCCESS:
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceAddedSuccessfully(selectedPowerSocket);
                                }
                                break;
                            case Constants.FAILURE:
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceAddingFailed(deviceAddress);
                                }
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.WIFI_AND_MQTT_STATE_INFO: {
                        byte wifiState = data[0];  // Index 0 - Wifi state
                        byte mqttState = data[1];  // Index 1 - Mqtt state
                        // 1 = Connected
                        // 0 = Not Connected
                        // Check Wifi state
                        if (wifiState == 1) {  // Success, Connected
                            // Check Mqtt state
                            if (mqttState == 1) {  //MQTT Connected
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onWifiMqttNotSubscribed(deviceAddress);
                                }
                            } else if (mqttState == 2) { //MQTT Subscribed
                                handler.removeCallbacks(wifiConfigTimeoutTask);
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onWifiConfigured(deviceAddress);
                                }
                            } else { // Not connected
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onWifiConfigFailed(deviceAddress);
                                }
                            }
                        } else { // Wifi Not connected
                            if (powerSocketBLEEventCallbacks != null) {
                                powerSocketBLEEventCallbacks.onWifiNotConfigured(deviceAddress);
                            }
                        }
                    }
                    break;

                    case BLECommandUtils.REQUEST_SSID_LIST: {
                        if (data.length == 1) { // To extract Wifi SSID status Response
                            int status = ByteConverter.convertByteArrayToInt(data);
                            switch (status) {
                                case Constants.SUCCESS:
                                    wifiRequestStatus = true;
                                    break;
                                case Constants.FAILURE:
                                    wifiRequestStatus = false;
                                    handler.removeCallbacks(wifiSearchTimeoutTask);
                                    break;
                            }
                        } else {
                            if (wifiRequestStatus) {
                                // Parse Wifi Raw data to get Wifi SSIDs List
                                try {
                                    int packetIndex = data[0] & 0xFF;
                                    int wifiSSIDFixedLength = data[1] & 0xFF;

                                    // 0xEE indicates that Wi-Fi devices are found and finished sending SSIDs to app from power socket
                                    // packet[0] -> Data Length = 0xEE   (line 121)
                                    // packet[1] -> Packet Index = 0xEE
                                    // packet[2] -> WifiSSIDFixedLength = 0xEE
                                    if (packetIndex == 0xEE && wifiSSIDFixedLength == 0xEE) {
                                        handler.removeCallbacks(wifiSearchTimeoutTask);
                                        ArrayList<WifiDevice> wifiDevicesList = extractWifiDevicesList();
                                        console.log("wifiDevices", new Gson().toJson(wifiDevicesList));
                                        if (powerSocketBLEEventCallbacks != null) {
                                            powerSocketBLEEventCallbacks.onWifiDeviceListFound(deviceAddress, wifiDevicesList);
                                        }
                                    }
                                    // 0xFF indicates that Wi-Fi devices are not found by power socket.
                                    // packet[0] -> Data Length = 0xFF (line 121)
                                    // packet[1] -> Packet Index = 0xFF
                                    // packet[2] -> WifiSSIDFixedLength = 0xFF
                                    else if (packetIndex == 0xFF && wifiSSIDFixedLength == 0xFF) {
                                        handler.removeCallbacks(wifiSearchTimeoutTask);
                                        ArrayList<WifiDevice> wifiDevicesList = extractWifiDevicesList();
                                        if (wifiDevicesList.size() == 0) {
                                            if (powerSocketBLEEventCallbacks != null) {
                                                powerSocketBLEEventCallbacks.onWifiDevicesNotFound(deviceAddress);
                                            }
                                        } else {
                                            console.log("wifiDevices", new Gson().toJson(wifiDevicesList));
                                            if (powerSocketBLEEventCallbacks != null) {
                                                powerSocketBLEEventCallbacks.onWifiDeviceListFound(deviceAddress, wifiDevicesList);
                                            }
                                        }
                                    } else {
                                        // Add data into WifiRawDataBuffer until Wifi scan completed byte 0xEE of 0xFF is received
                                        wifiRawDataBuffer.add(data);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                    break;

                    case BLECommandUtils.SET_WIFI_SSID: {
                        int wifiSSIDStatus = ByteConverter.convertByteArrayToInt(data);
                        switch (wifiSSIDStatus) {
                            case Constants.SUCCESS:
                                byte[] passwordByte = selectedPassword.getBytes();
                                passwordPackets = getPasswordPackets(passwordByte);
                                byte[] txPacket = BLECommandUtils.getWifiPasswordConfigSetPacket(passwordPackets.remove(), encryptionKey);
                                bleCentralManager.sendData(deviceAddress, BLECommandUtils.SERVICE_UUID,
                                        BLECommandUtils.CONTROL_CHARACTERISTIC_UUID, txPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                                totalPasswordPacketSize = passwordPackets.size();
                                break;
                            case Constants.FAILURE:
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onWifiConfigFailed(deviceAddress);
                                }
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.SET_WIFI_PASSWORD: {
                        int packetSubIndex = data[0];  // packet sub-index
                        int wifiPasswordStatus = data[1];          // status

                        // Check if the packet sub-index of the response matches with the one sent
                        // totalPasswordPacketSize = The total size of the packet Queue at the time it was created before sending
                        // passwordPackets = The size of the queue after the packet is removed for transmission in FIFO manner.
                        if (packetSubIndex == totalPasswordPacketSize - passwordPackets.size()) {
                            switch (wifiPasswordStatus) {
                                case Constants.SUCCESS:
                                    // Transmit another packet in Queue in FIFO manner
                                    if (passwordPackets.size() > 0) {
                                        byte[] txPacket = BLECommandUtils.getWifiPasswordConfigSetPacket(passwordPackets.remove(), encryptionKey);
                                        bleCentralManager.sendData(deviceAddress, BLECommandUtils.SERVICE_UUID,
                                                BLECommandUtils.CONTROL_CHARACTERISTIC_UUID, txPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                                    } else {
                                        handler.postDelayed(wifiConfigTimeoutTask, MAX_WIFI_CONFIG_TIMEOUT);
                                    }
                                    break;
                                case Constants.FAILURE:
                                    if (powerSocketBLEEventCallbacks != null) {
                                        powerSocketBLEEventCallbacks.onWifiConfigFailed(deviceAddress);
                                    }
                                    break;
                            }
                        }
                    }
                    break;

                    case BLECommandUtils.DELETE_WIFI_SSID_PASSWORD: {
                        int status = ByteConverter.convertByteArrayToInt(data);
                        switch (status) {
                            case Constants.SUCCESS:
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onWifiConfigRemoved(deviceAddress);
                                }
                                break;
                            case Constants.FAILURE:
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onWifiConfigRemoveFailure(deviceAddress);
                                }
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.CHECK_SOCKET_DIAGNOSTIC: {
                        int transactionIdStartIdx = data.length - 2;
                        int transactionIdEndIdx = data.length;
                        byte[] transactionIdBytes = ByteConverter.copyOfRange(data, transactionIdStartIdx, transactionIdEndIdx);
                        int transactionId = ByteConverter.convertByteArrayToInt(transactionIdBytes);

                        int switchDataStartIdx = 0;
                        int switchDataEndIdx = data.length - 2;
                        byte[] switchData = ByteConverter.copyOfRange(data, switchDataStartIdx, switchDataEndIdx);

                        if (powerSocketBLEEventCallbacks != null) {
                            powerSocketBLEEventCallbacks.onCheckSocketDiagnostics(deviceAddress, transactionId, switchData);
                        }
                    }
                    break;

                    case BLECommandUtils.POWER_ON_OFF_SINGLE_SOCKET: {
                        //                        01 01 01 01 01 01 00 00 01
                        int transactionIdStartIdx = data.length - 3;
                        int transactionIdEndIdx = data.length - 1;
                        byte[] transactionIdBytes = ByteConverter.copyOfRange(data, transactionIdStartIdx, transactionIdEndIdx);
                        int transactionId = ByteConverter.convertByteArrayToInt(transactionIdBytes);

                        int switchDataStartIdx = 0;
                        int switchDataEndIdx = data.length - 3;
                        byte[] switchData = ByteConverter.copyOfRange(data, switchDataStartIdx, switchDataEndIdx);

                        if (powerSocketBLEEventCallbacks != null) {
                            powerSocketBLEEventCallbacks.onSingleSocketStateChange(deviceAddress, transactionId, switchData);
                        }
                    }
                    break;

                    case BLECommandUtils.POWER_ON_OFF_ALL_SOCKET: {
//                        01 01 01 01 01 01 00 00 01
                        int transactionIdStartIdx = data.length - 3;
                        int transactionIdEndIdx = data.length - 1;
                        byte[] transactionIdBytes = ByteConverter.copyOfRange(data, transactionIdStartIdx, transactionIdEndIdx);
                        int transactionId = ByteConverter.convertByteArrayToInt(transactionIdBytes);

                        int switchDataStartIdx = 0;
                        int switchDataEndIdx = data.length - 3;
                        byte[] switchData = ByteConverter.copyOfRange(data, switchDataStartIdx, switchDataEndIdx);

                        if (powerSocketBLEEventCallbacks != null) {
                            powerSocketBLEEventCallbacks.onAllSocketsStateChange(deviceAddress, transactionId, switchData);
                        }
                    }
                    break;

                    case BLECommandUtils.DELETE_DEVICE_REGISTRATION_INFO: {
                        handler.removeCallbacks(deleteTimeoutTask);
                        int deleteStatus = ByteConverter.convertByteArrayToInt(data);
                        switch (deleteStatus) {
                            case Constants.SUCCESS: // Success
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceRemoved(deviceAddress);
                                }
                                disconnect(selectedPowerSocket.getBleAddress());
                                break;
                            case Constants.FAILURE: //  Failure
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceRemovalFailed(deviceAddress);
                                }
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.RESET_POWER_SOCKET: {
                        int resetStatus = ByteConverter.convertByteArrayToInt(data);
                        switch (resetStatus) {
                            case Constants.SUCCESS: // Success
                                handler.removeCallbacks(resetTimeoutTask);
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceResetCompleted(deviceAddress);
                                }
                                disconnect(selectedPowerSocket.getBleAddress());
                                break;
                            case Constants.FAILURE: //  Failure
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onDeviceResetFailed(deviceAddress);
                                }
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.GET_CURRENT_FIRMWARE_VERSION: {
                        int majorVersion = data[0] & 0xFF;    // Extract Major Version
                        int minorVersion = data[1] & 0xFF;    // Extract Minor Version
                        String firmwareVersion = majorVersion + "." + minorVersion;

                        if (powerSocketBLEEventCallbacks != null) {
                            powerSocketBLEEventCallbacks.onReceivedCurrentFirmwareVersion(deviceAddress, firmwareVersion);
                        }
                    }
                    break;

                    case BLECommandUtils.FACTORY_SOCKET_TEST: {
                        handler.removeCallbacks(factorySocketTestTimeoutTask);
                        int status = ByteConverter.convertByteArrayToInt(data);
                        switch (status) {
                            case Constants.SUCCESS: // Success
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onFactorySocketTestSuccessful(deviceAddress);
                                }
                                break;
                            case Constants.FAILURE: //  Failure
                                if (powerSocketBLEEventCallbacks != null) {
                                    powerSocketBLEEventCallbacks.onFactorySocketTestFailed(deviceAddress);
                                }
                                break;
                        }
                    }
                    break;

                    case BLECommandUtils.REQUEST_ALARM_SET: {
                        byte[] inValidTimeStamp = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
                        Log.d(TAG, "REQUEST_ALARM_SET " + ByteConverter.getHexStringFromByteArray(data, true));
                        if ((data.length == 14) && (data[13] == SUCESS)) {
                            /**
                             * Checking 3 conditions.
                             * 1)Data length
                             * 2)Alaram Set Sucess or Failure in 11 th index.
                             */
                            String bleAddress = deviceAddress.toUpperCase().replace(":", "");
                            /**
                             * Modify Date as per the Day byte..
                             */
                            byte alaramId = data[0];
                            byte socketId = data[1];
                            byte sucess_failure = data[13];
                            byte[] ontimeStamp = Arrays.copyOfRange(data, 3, 7);
                            byte[] offtimeStamp = Arrays.copyOfRange(data, 7, 11);
                            String onTimeStampInHex = ByteConverter.getHexStringFromByteArray(ontimeStamp, false);
                            String offTimeStampInHex = ByteConverter.getHexStringFromByteArray(offtimeStamp, false);
                            byte[] transactionIdArray = {data[11], data[12]};
                            String hexConvertedTransactionId = ByteConverter.getHexStringFromByteArray(transactionIdArray, false);
                            int transactionId = -1;
                            transactionId = Integer.parseInt("" + convert4bytes(hexConvertedTransactionId));
                            int onTimeStamp = 0;
                            int offTimeStamp = 0;
                            String onOriginalTime = "NA";
                            String offOriginalTime = "NA";
                            byte alaramState = data[13];
                            int dayByte = -1;
                            String binayDfaultValues="0000000";
                            if (data[2] > 0) {
                                /**
                                 * its a repeat alaram
                                 */
                                dayByte = data[2];// taking the day byte..
                                String binayNumberInReverse= new StringBuilder(Integer.toBinaryString(dayByte)).reverse().toString();
                                String combinedValue=binayNumberInReverse+binayDfaultValues.substring(binayNumberInReverse.length());
                                binayDfaultValues=combinedValue;
                                if (!Arrays.equals(ontimeStamp, inValidTimeStamp)) {
                                    onTimeStamp = Integer.parseInt("" + convert4bytes(onTimeStampInHex));
                                    onOriginalTime = getTimeFromTimeStamp(onTimeStamp, true);
                                }
                                if (!Arrays.equals(offtimeStamp, inValidTimeStamp)) {
                                    offTimeStamp = Integer.parseInt("" + convert4bytes(offTimeStampInHex));
                                    offOriginalTime = getTimeFromTimeStamp(offTimeStamp, true);
                                }
                            } else {
                                /**
                                 * Normal Alaram
                                 */
                                dayByte = data[2];
                                if (!Arrays.equals(ontimeStamp, inValidTimeStamp)) {
                                    onTimeStamp = Integer.parseInt("" + convert4bytes(onTimeStampInHex));
                                    onOriginalTime = getTimeFromTimeStamp(onTimeStamp, false);
                                }
                                if (!Arrays.equals(offtimeStamp, inValidTimeStamp)) {
                                    offTimeStamp = Integer.parseInt("" + convert4bytes(offTimeStampInHex));
                                    offOriginalTime = getTimeFromTimeStamp(offTimeStamp, false);
                                }
                            }
                            powerSocketAlaramSetArrayList = new ArrayList<PowerSocketAlaramDetails>();
                            powerSocketAlaramSetArrayList.add(new PowerSocketAlaramDetails(data[0], data[1], (byte) dayByte, onTimeStamp, offTimeStamp, alaramState, binayDfaultValues, bleAddress, onOriginalTime, offOriginalTime));
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int i = 0; i < powerSocketAlaramSetArrayList.size(); i++) {
                                                PowerSocketAlaramDetails powerSocketAlaramDetails = powerSocketAlaramSetArrayList.get(i);
                                                int alaramId = powerSocketAlaramDetails.getAlarmaId();
                                                int socketId = powerSocketAlaramDetails.getSocketId();
                                                int numberOfDaysSelected = powerSocketAlaramDetails.getDaybyte();
                                                int onTimeStamp = powerSocketAlaramDetails.getOnTime();// ON-TimeStamp
                                                int offTimeStamp = powerSocketAlaramDetails.getOffTime();// Off-TimeStamp
                                                String onOriginal = powerSocketAlaramDetails.getOnOriginal();
                                                String offOriginal = powerSocketAlaramDetails.getOffOriginal();
                                                String bleAddress = powerSocketAlaramDetails.getBleAddress();
                                                String daysInBinnary = powerSocketAlaramDetails.getDaysInBinary();
                                                int alaramState = powerSocketAlaramDetails.getAlaramState();
                                                update_Insert_AlaramDataBase(alaramId, socketId, numberOfDaysSelected, "" + onTimeStamp, "" + offTimeStamp, onOriginal, offOriginal, bleAddress, daysInBinnary, alaramState);
                                            }
                                            deletePoweSocketAlaram_If_TimeElapsed("" + System.currentTimeMillis() / 1000);

                                        }
                                    });
                                }
                            });
                            if (powerSocketBLEEventCallbacks != null) {
                                powerSocketBLEEventCallbacks.onAlaramSetBLE(deviceAddress, transactionId, alaramId, socketId, sucess_failure);
                            }
                        } else if ((data.length == 14) && (data[13] == FAILURE)) {
                            if (powerSocketBLEEventCallbacks != null) {
                                byte[] transactionIdArray = {data[11], data[12]};
                                String hexConvertedTransactionId = ByteConverter.getHexStringFromByteArray(transactionIdArray, false);
                                int transactionId = -1;
                                transactionId = Integer.parseInt("" + convert4bytes(hexConvertedTransactionId));
                                byte alaramId = data[0];
                                byte socketId = data[1];
                                byte sucess_failure = data[13];
                                powerSocketBLEEventCallbacks.onAlaramSetBLE(deviceAddress, transactionId, alaramId, socketId, sucess_failure);
                            }
                        }
                    }
                    break;

                    case BLECommandUtils.DELETE_ALARM: {
                        /**
                         *        String bleAddress = powerSocket.bleAddress.replace(":", "");
                         *         String quereyDelete_Alaram_1 = "DELETE FROM " + mTablePowerSocket + " WHERE " + mfield_socket_id + "= '" + selected_ScoketdId + "'" + " AND " + mfield_alarm_id + "= '" + alaramId_1 + "'" + " AND " + mfield_ble_address + " ='" + bleAddress + "'";
                         *         mActivity.mDbHelper.exeQuery(quereyDelete_Alaram_1);
                         */
                        Log.d(TAG, "DeleteIssue Recieved Alaram Packets BLE " + ByteConverter.getHexStringFromByteArray(data, true));
                        String bleAddress = deviceAddress.toUpperCase().replace(":", "");
                        console.log("Delete Alaram", ByteConverter.getHexStringFromByteArray(data, true));
                        byte alaramId = (byte) (data[0] & 0xff);
                        byte[] transactionIdArray = {data[1], data[2]};
                        byte deleteAlaramStatus = (byte) (data[3] & 0xff);
                        String hexConvertedTransactionId = ByteConverter.getHexStringFromByteArray(transactionIdArray, false);
                        final int transactionId = Integer.parseInt("" + convert4bytes(hexConvertedTransactionId));
                        if ((deleteAlaramStatus == SUCESS) && (transactionId > 0)) {
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    String quereyDelete_Alaram = "DELETE FROM " + mTableAlarmPowerSocket + " WHERE " + mfield_alarm_id + "= " + alaramId + "" + " AND " + mfield_ble_address + " = '" + bleAddress + "'";
                                    DBHelper mDbHelper = DBHelper.getDBHelperInstance();
                                    mDbHelper.exeQuery(quereyDelete_Alaram);
                                    if (powerSocketBLEEventCallbacks != null) {
                                        powerSocketBLEEventCallbacks.onAlaramDeleteBLE(deviceAddress, transactionId, alaramId, deleteAlaramStatus);
                                    }
                                }
                            });
                        } else if (deleteAlaramStatus == FAILURE) {
                            if (powerSocketBLEEventCallbacks != null) {
                                powerSocketBLEEventCallbacks.onAlaramDeleteBLE(deviceAddress, transactionId, alaramId, deleteAlaramStatus);
                            }
                        }
                    }
                    break;

                    case REQUEST_ALARM_DETAILS: {
                        Log.d("Alaram_Fetch_Details", ByteConverter.getHexStringFromByteArray(data, true));
                        byte[] inValidTimeStamp = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
                        if ((data.length == 12) && (data[0] != 0) && (data[11] != 0)) {
                            /**
                             * Checking 3 conditions.
                             * 1)Data length
                             * 2)Intial Packet i.e Frist pacekt is not zero.
                             * 3)Alaram State is not zero.i.e if 0=already finished,1=Active Still
                             * Keep adding all the data to the list
                             */
                            String bleAddress = deviceAddress.toUpperCase().replace(":", "");
                            /**
                             * Modify Date as per the Day byte..
                             */
                            byte[] ontimeStamp = Arrays.copyOfRange(data, 3, 7);
                            byte[] offtimeStamp = Arrays.copyOfRange(data, 7, 11);
                            String onTimeStampInHex = ByteConverter.getHexStringFromByteArray(ontimeStamp, false);
                            String offTimeStampInHex = ByteConverter.getHexStringFromByteArray(offtimeStamp, false);
                            int onTimeStamp = 0;
                            int offTimeStamp = 0;
                            String onOriginalTime = "NA";
                            String offOriginalTime = "NA";
                            byte alaramState = data[11];
                            int dayByte = -1;
                            String binayDfaultValues="0000000";
                            if (data[2] > 0) {
                                /**
                                 * its a repeat alaram
                                 */
                                dayByte = data[2];// taking the day byte..
                                String binayNumberInReverse= new StringBuilder(Integer.toBinaryString(dayByte)).reverse().toString();
                                String combinedValue=binayNumberInReverse+binayDfaultValues.substring(binayNumberInReverse.length());
                                binayDfaultValues=combinedValue;

                                if (!Arrays.equals(ontimeStamp, inValidTimeStamp)) {
                                    onTimeStamp = Integer.parseInt("" + convert4bytes(onTimeStampInHex));
                                    onOriginalTime = getTimeFromTimeStamp(onTimeStamp, true);
                                }
                                if (!Arrays.equals(offtimeStamp, inValidTimeStamp)) {
                                    offTimeStamp = Integer.parseInt("" + convert4bytes(offTimeStampInHex));
                                    offOriginalTime = getTimeFromTimeStamp(offTimeStamp, true);
                                }
                            } else {
                                /**
                                 * Normal Alaram
                                 */
                                dayByte = data[2];
                                if (!Arrays.equals(ontimeStamp, inValidTimeStamp)) {
                                    onTimeStamp = Integer.parseInt("" + convert4bytes(onTimeStampInHex));
                                    onOriginalTime = getTimeFromTimeStamp(onTimeStamp, false);
                                }
                                if (!Arrays.equals(offtimeStamp, inValidTimeStamp)) {
                                    offTimeStamp = Integer.parseInt("" + convert4bytes(offTimeStampInHex));
                                    offOriginalTime = getTimeFromTimeStamp(offTimeStamp, false);
                                }
                            }
                            powerSocketAlaramDetailsArrayList.add(new PowerSocketAlaramDetails(data[0], data[1], (byte) dayByte, onTimeStamp, offTimeStamp, alaramState, binayDfaultValues, bleAddress, onOriginalTime, offOriginalTime));
                        } else if (data.length == 1 && data[0] == 01) {
                            /**
                             * Ending packet data.Process All data
                             */
                            AsyncTask.execute(() -> {
                                for (int i = 0; i < powerSocketAlaramDetailsArrayList.size(); i++) {
                                    PowerSocketAlaramDetails powerSocketAlaramDetails = powerSocketAlaramDetailsArrayList.get(i);
                                    int alaramId = powerSocketAlaramDetails.getAlarmaId();
                                    int socketId = powerSocketAlaramDetails.getSocketId();
                                    int numberOfDaysSelected = powerSocketAlaramDetails.getDaybyte();
                                    int onTimeStamp = powerSocketAlaramDetails.getOnTime();// ON-TimeStamp
                                    int offTimeStamp = powerSocketAlaramDetails.getOffTime();// Off-TimeStamp
                                    String onOriginal = powerSocketAlaramDetails.getOnOriginal();
                                    String offOriginal = powerSocketAlaramDetails.getOffOriginal();
                                    String bleAddress = powerSocketAlaramDetails.getBleAddress();
                                    String daysInBinnary = powerSocketAlaramDetails.getDaysInBinary();
                                    int alaramState = powerSocketAlaramDetails.getAlaramState();
                                    update_Insert_AlaramDataBase(alaramId, socketId, numberOfDaysSelected, "" + onTimeStamp, "" + offTimeStamp, onOriginal, offOriginal, bleAddress, daysInBinnary, alaramState);
                                }
                                deletePoweSocketAlaram_If_TimeElapsed("" + System.currentTimeMillis() / 1000);
                                powerSocketAlaramDetailsArrayList = new ArrayList<PowerSocketAlaramDetails>();
                            });
                        }
                    }
                    break;
                }
            }
        }

        @Override
        public void onCharacteristicWrite(String deviceAddress, UUID serviceUUID, UUID characteristicUUID, byte[] data) {
            super.onCharacteristicWrite(deviceAddress, serviceUUID, characteristicUUID, data);
            if (data != null) {
                int opcode = data[0] & 0xFF;
                switch (opcode) {
                    case BLECommandUtils.ADD_DEVICE_REGISTRATION_INFO:
                        if (!selectedPowerSocket.isAssociated) {
                            selectedPowerSocket.isAssociated = true;
                        }
                        break;
                    case BLECommandUtils.DELETE_DEVICE_REGISTRATION_INFO:
                        handler.postDelayed(deleteTimeoutTask, MAX_DELETE_DEVICE_TIMEOUT);
                        break;
                    case BLECommandUtils.RESET_POWER_SOCKET:
                        handler.postDelayed(resetTimeoutTask, MAX_RESET_DEVICE_TIMEOUT);
                        break;
                    case BLECommandUtils.FACTORY_SOCKET_TEST:
                        handler.postDelayed(factorySocketTestTimeoutTask, MAX_FACTORY_SOCKET_TIMEOUT);
                        break;
                    case BLECommandUtils.REQUEST_SSID_LIST:
                        handler.postDelayed(wifiSearchTimeoutTask, MAX_WIFI_SEARCH_TIMEOUT);
                        break;
                    default:
                        break;
                }
            }
        }

        @Override
        public void onCharacteristicWriteFailed(String deviceAddress, UUID serviceUUID, UUID characteristicUUID, byte[] lastSentData, int errorCode) {
            super.onCharacteristicWriteFailed(deviceAddress, serviceUUID, characteristicUUID, lastSentData, errorCode);
            bleCentralManager.sendData(deviceAddress, serviceUUID, characteristicUUID, lastSentData, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        }
    };

    private void update_Insert_AlaramDataBase(int alaramId_1_2, int socketId, int number_of_days_Selected, String ontimeStamp, String offtimeStamp, String onOriginal, String offOriginal, String bleAddress, String daysSelectedInBinary, int alaramState) {
        DBHelper mDbHelper = DBHelper.getDBHelperInstance();
        ContentValues contentValues = new ContentValues();
        contentValues.put(mfield_alarm_id, alaramId_1_2);
        contentValues.put(mfield_socket_id, socketId);
        contentValues.put(mfield_day_value, number_of_days_Selected);
        if (ontimeStamp.equalsIgnoreCase("-1")) {
            contentValues.put(mfield_OnTimestamp, "NA");
        } else {
            contentValues.put(mfield_OnTimestamp, ontimeStamp);
        }
        if (offtimeStamp.equalsIgnoreCase("-1")) {
            contentValues.put(mfield_OffTimestamp, "NA");
        } else {
            contentValues.put(mfield_OffTimestamp, offtimeStamp);
        }
        if (onOriginal.equalsIgnoreCase("NA")) {
            contentValues.put(mfield_On_original, "NA");
        } else {
            contentValues.put(mfield_On_original, onOriginal);
        }
        if (offOriginal.equalsIgnoreCase("NA")) {
            contentValues.put(mfield_Off_original, "NA");
        } else {
            contentValues.put(mfield_Off_original, offOriginal);
        }
        contentValues.put(mfield_alarm_state, alaramState);
        contentValues.put(mfield_ble_address, bleAddress);
        contentValues.put(mfield_day_selected, daysSelectedInBinary);
        boolean recordExists = mDbHelper.checkRecordAvaliableInTable(mTableAlarmPowerSocket, mfield_socket_id, mfield_alarm_id, mfield_ble_address, "" + socketId, "" + alaramId_1_2, bleAddress);
        if (recordExists) {
            /**
             * Insert Record
             */
            mDbHelper.insertRecord(mTableAlarmPowerSocket, contentValues);
        } else {
            /**
             * Update the Record for mulitple where Condition.
             */
            mDbHelper.updateRecord(mTableAlarmPowerSocket, contentValues, "alarm_id=? and socket_id=? and ble_address=?", new String[]{"" + alaramId_1_2, "" + socketId, bleAddress});
        }
    }

    /**
     * Delete the alaram which has already Elapsed.
     *
     * @param timStamp
     */
    public void deletePoweSocketAlaram_If_TimeElapsed(String timStamp) {
        DBHelper mDbHelper = DBHelper.getDBHelperInstance();
        String querey = "DELETE FROM " + mTableAlarmPowerSocket + " WHERE " + mfield_day_value + " = 0 AND " + mfield_OnTimestamp + " < " + timStamp + " AND " + mfield_OffTimestamp + " < " + timStamp;
        mDbHelper.exeQuery(querey);
    }

    DeleteTimeoutTask.DeleteTimeoutCallbacks onDeleteTimeoutCallbacks = new DeleteTimeoutTask.DeleteTimeoutCallbacks() {
        @Override
        public void onTimeout() {
            console.log("PowerSocketBLEService_Delete_Timer", "Timeout");
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onDeviceRemovalFailed(selectedPowerSocket.bleAddress);
            }
        }
    };

    WifiSearchTimeoutTask.WifiSearchTimeoutCallback wifiSearchTimeoutCallback = new WifiSearchTimeoutTask.WifiSearchTimeoutCallback() {
        @Override
        public void onWifiSearchTimeout() {
            console.log("PowerSocketBLEService_WifiConfig_Timeout", "Timeout");
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onWifiDevicesNotFound(selectedPowerSocket.bleAddress);
            }
        }
    };

    WifiConfigTimeoutTask.WifiConfigTimeoutCallback wifiConfigTimeoutCallback = new WifiConfigTimeoutTask.WifiConfigTimeoutCallback() {
        @Override
        public void onWifiConfigTimeout() {
            console.log("PowerSocketBLEService_WifiConfig_Timeout", "Timeout");
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onWifiConfigFailed(selectedPowerSocket.bleAddress);
            }
        }
    };

    ResetTimeoutTask.ResetTimeoutCallbacks resetTimeoutCallbacks = new ResetTimeoutTask.ResetTimeoutCallbacks() {
        @Override
        public void onTimeout() {
            console.log("PowerSocketBLEService_Reset_Timeout", "Timeout");
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onDeviceResetFailed(selectedPowerSocket.bleAddress);
            }
        }
    };

    FactorySocketTestTimeoutTask.FactorySocketTestCallbacks factorySocketTestTimeoutCallbacks = new FactorySocketTestTimeoutTask.FactorySocketTestCallbacks() {
        @Override
        public void onTimeout() {
            console.log("PowerSocketBLEService_Reset_Timeout", "Timeout");
            if (powerSocketBLEEventCallbacks != null) {
                powerSocketBLEEventCallbacks.onFactorySocketTestFailed(selectedPowerSocket.bleAddress);
            }
        }
    };

    public void checkDeviceRegistration(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(selectedPowerSocket.isAssociated);
        // Verify Device Registration Info => Check whether a power socket is connected to wifi/MQTT broker
        byte[] deviceRegistrationInfoRequestPacket = BLECommandUtils.getDeviceRegistrationInfoRequestPacket(encryptionKey);
        bleCentralManager.sendData(selectedPowerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                deviceRegistrationInfoRequestPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void addPowerSocket(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(selectedPowerSocket.isAssociated);
        // Add device
        byte[] mainKey = null;
        String secretKey = preferenceHelper.getSecretKey();
        if (secretKey != null && !secretKey.isEmpty()) {
            mainKey = ByteConverter.getByteArrayFromHexString(secretKey);
        }

        byte[] addDeviceRegistrationRequestPacket = BLECommandUtils.getDeviceAddRegistrationPacket(mainKey, encryptionKey);
        bleCentralManager.sendData(selectedPowerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                addDeviceRegistrationRequestPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void checkDeviceWifiConfigStatus(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        // Check for Device WIFI and MQTT state
        byte[] encryptionKey = getEncryptionKey(selectedPowerSocket.isAssociated);
        byte[] wifiAndMqttStateRequestPacket = BLECommandUtils.getWifiAndMqttStateRequestPacket(encryptionKey);
        bleCentralManager.sendData(selectedPowerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                wifiAndMqttStateRequestPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void scanWifiDevices(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        wifiRawDataBuffer = new ArrayList<byte[]>();
        if (selectedPowerSocket != null) {
            String bleAddress = selectedPowerSocket.bleAddress;
            if (bleAddress != null && !bleAddress.isEmpty()) {
                // Request WifiSSID List
                byte[] wifiSSIDListRequestPacket = BLECommandUtils.getSSIDListRequestPacket(null);
                bleCentralManager.sendData(selectedPowerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID, wifiSSIDListRequestPacket,
                        BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            } else {
                console.log("asjxbasjkbxkjasbx", "BLE Address is null");
            }
        } else {
            console.log("ashvxjhvasjhxvahx", "Power socket is null");
        }
    }

    public ArrayList<WifiDevice> extractWifiDevicesList() {
        // Extract data from WifiRawDataBuffer, create Wifi object and display in List
        ArrayList<WifiDevice> wifiDevicesList = new ArrayList<>();
        byte[] wifiSSIDBytes = new byte[32];
        WifiDevice wifiDevice = null;
        int x = 0;
        for (byte[] rawWifiData : wifiRawDataBuffer) {
            int wifiSSIDIndex = rawWifiData[0] & 0xFF;
            int ssidTotalLen = rawWifiData[1] & 0xFF;
            int packetCount = (int) Math.ceil((double) ssidTotalLen / 12);   // 12 - Data length

            if (ssidTotalLen <= 12) {
                // Extract Wifi SSID and RSSI
                wifiDevice = new WifiDevice();
                int wifiRSSI = rawWifiData[3];
                wifiDevice.deviceIndex = wifiSSIDIndex;
                wifiDevice.wifiRSSI = "-" + wifiRSSI + "dBm";
                int srcIdx = 4;
                int dstIdx = srcIdx + (rawWifiData.length - 4);
                wifiSSIDBytes = ByteConverter.copyOfRange(rawWifiData, srcIdx, dstIdx);
                String wifiSSID = new String(wifiSSIDBytes, StandardCharsets.US_ASCII);
                wifiSSID = wifiSSID.replace("\u0000", "");
                wifiDevice.wifiSSID = wifiSSID;
                wifiDevicesList.add(wifiDevice);
            } else {
                int packetSubIndex = rawWifiData[2] & 0xFF;
                if (packetSubIndex == 0) {
                    wifiSSIDBytes = new byte[32];
                    wifiDevice = new WifiDevice();
                    int wifiRSSI = rawWifiData[3];
                    wifiDevice.wifiRSSI = "-" + wifiRSSI + "dBm";
                    wifiDevice.deviceIndex = wifiSSIDIndex;
                    int srcIdx = 4;//
                    System.arraycopy(rawWifiData, srcIdx, wifiSSIDBytes, 0,
                            rawWifiData.length - 4);
                    x = rawWifiData.length - 4;
                } else if (packetSubIndex < packetCount - 1) {
                    int srcIdx = 3;
                    System.arraycopy(rawWifiData, srcIdx, wifiSSIDBytes, x,
                            (rawWifiData.length - 3));
                } else if (packetSubIndex == packetCount - 1) {
                    int srcIdx = 3;
                    System.arraycopy(rawWifiData, srcIdx, wifiSSIDBytes, x,
                            (rawWifiData.length - 3));
                    wifiSSIDBytes = ByteConverter.trim(wifiSSIDBytes);
                    String wifiSSID = new String(wifiSSIDBytes, StandardCharsets.US_ASCII);
                    wifiSSID = wifiSSID.replace("\u0000", "");
                    wifiDevice.wifiSSID = wifiSSID;
                    x = 0;
                    wifiDevicesList.add(wifiDevice);
                }
            }
        }
        return wifiDevicesList;
    }

    public void configureWifi(PowerSocket powerSocket, WifiDevice wifiDevice, String password) {
        this.selectedPowerSocket = powerSocket;
        this.selectedWifiDevice = wifiDevice;
        this.selectedPassword = password;

        try {
            byte[] encryptionKey = getEncryptionKey(selectedPowerSocket.isAssociated);
            // Send WifiSSID config packet
            byte[] wifiSSIDBytesConfigPacket = BLECommandUtils.getWifiSSIDConfigSetPacket(wifiDevice.deviceIndex, encryptionKey);
            bleCentralManager.sendData(selectedPowerSocket.bleAddress, BLECommandUtils.SERVICE_UUID,
                    BLECommandUtils.CONTROL_CHARACTERISTIC_UUID, wifiSSIDBytesConfigPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeWifiConfig(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] deleteWifiConfigRequestPacket = BLECommandUtils.getDeleteWifiSSIDPasswordPacket(getEncryptionKey(powerSocket.isAssociated));
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                deleteWifiConfigRequestPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void controlAllSwitches(PowerSocket powerSocket, byte state, int transactionId) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] bleControlCommandPacket = BLECommandUtils.getSocketControlCommandPacket(state, transactionId, encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                bleControlCommandPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void controlSingleSwitch(PowerSocket powerSocket, byte socketID, byte state, int transactionId) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] bleControlCommandPacket = BLECommandUtils.getSocketControlCommandPacket(socketID, state, transactionId, encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                bleControlCommandPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void checkSocketDiagnostics(PowerSocket powerSocket, int transactionId) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] socketDiagnosticsRequestPacket = BLECommandUtils.getSocketStatesRequestPacket(transactionId, encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                socketDiagnosticsRequestPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void removeDevice(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] deleteDeviceData = new byte[1];
        deleteDeviceData[0] = 0x00;              // De-register value

        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] deleteDeviceRequestPacket = BLECommandUtils.getDeviceRegistrationInfoDeleteRequestPacket(deleteDeviceData, encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                deleteDeviceRequestPacket, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void resetPowerSocket(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] factoryResetCommand = BLECommandUtils.getPowerSocketResetCommandPacket(encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.RESET_CHARACTERISTIC_UUID,
                factoryResetCommand, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void getUTCTime(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] getUTCTimeCommand = BLECommandUtils.getUTCTimeRequestPacket(encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                getUTCTimeCommand, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void setCurrentTime(PowerSocket powerSocket, boolean isUserSetTimeSelected) {
        this.isUserSetTimeSelected = isUserSetTimeSelected;
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        byte[] currentTimeBytes = ByteConverter.convertIntToByteArray(currentTime);
        byte[] getUTCConfigCommand = BLECommandUtils.getConfigUTCTimeRequestPacket(currentTimeBytes, encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                getUTCConfigCommand, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void getCurrentFirmwareVersion(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] getCurrentFirmwareVersionCommand = BLECommandUtils.getCurrentFirmwareVersionRequestPacket(encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                getCurrentFirmwareVersionCommand, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void performFactoryResetTest(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] getDeviceFactorySocketsTestCommand = BLECommandUtils.getDeviceFactorySocketsTestPacket(encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                getDeviceFactorySocketsTestCommand, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void setAlarmForSocket(int onTime, int offTime, int alarmId, int selectedDay, PowerSocket powerSocket, int socketId, int transactionId) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] dataForAlaram = getAlarampacketData(onTime, offTime, selectedDay, alarmId, socketId, false, transactionId);
        byte[] alaramRequest_packet = BLECommandUtils.getAlaramSetPacket(dataForAlaram, encryptionKey);
        //     Log.d(TAG, "Alaram_Packet Final Alaram Packet Sending Encrypted " + ByteConverter.getHexStringFromByteArray(alaramRequest_packet, true));
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID,
                alaramRequest_packet, BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public void askAlaramDetailsFrom_socket(PowerSocket powerSocket) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] dataForAlaram = alaramDetails();
        byte[] alaramDetials_data = BLECommandUtils.getAlarmDetails(dataForAlaram, encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID, alaramDetials_data,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public static byte[] alaramDetails() {
        byte[] dataArray = new byte[2];
        dataArray[0] = REQUEST_ALARM_DETAILS;
        dataArray[1] = 00;
        Log.d(TAG, "Alaram_Packet asking alaramDetails " + ByteConverter.getHexStringFromByteArray(dataArray, true));
        return dataArray;
    }

    public void deleteAlaram(int alaramId, PowerSocket powerSocket, int trasactionId) {
        this.selectedPowerSocket = powerSocket;
        byte[] encryptionKey = getEncryptionKey(powerSocket.isAssociated);
        byte[] dataForAlaram = deleteAlarma(alaramId, trasactionId);
        byte[] deleteAlaram = BLECommandUtils.getDeleteAlarampPacket(dataForAlaram, encryptionKey);
        bleCentralManager.sendData(powerSocket.bleAddress, BLECommandUtils.SERVICE_UUID, BLECommandUtils.CONTROL_CHARACTERISTIC_UUID, deleteAlaram,
                BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
    }

    public static byte[] deleteAlarma(int alaramId, int trasactionId) {
        byte[] dataArray = new byte[3];
        dataArray[0] = (byte) alaramId;//
        byte[] arrayValue = intToByteArray(trasactionId);
        dataArray[1] = arrayValue[2];
        dataArray[2] = arrayValue[3];
        Log.d(TAG, "Alaram_Packet Delete Alaram Packet " + ByteConverter.getHexStringFromByteArray(dataArray, true));
        return dataArray;
    }

    private static byte[] convertToLittleEndian(byte[] data, int dataLength) {
        byte[] reverseArray = new byte[dataLength];
        int j = dataLength;
        for (int i = 0; i < dataLength; i++) {
            reverseArray[j - 1] = data[i];
            j = j - 1;
        }

        return reverseArray;
    }

    public static final byte[] intToByteArray(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static byte[] getAlarampacketData(int onTime, int offtime, int daySelected, int alaramId, int socketId, boolean convertToLittleEndian, int transactionId) {
        byte[] ontimeArray;
        byte[] offtimeArray;
        byte[] ontimeArrayBingEndian = convert_TimeStampTo_4bytes(onTime);
        byte[] offtimeArrayBigEndian = convert_TimeStampTo_4bytes(offtime);
        /**
         * Convert to
         */

        byte[] arrayValueTransactionId = intToByteArray(transactionId);

        if (convertToLittleEndian) {
            ontimeArray = convertToLittleEndian(ontimeArrayBingEndian, ontimeArrayBingEndian.length);
            offtimeArray = convertToLittleEndian(offtimeArrayBigEndian, offtimeArrayBigEndian.length);
        } else {
            ontimeArray = ontimeArrayBingEndian;
            offtimeArray = offtimeArrayBigEndian;
        }
        byte[] alaramData = new byte[13];
        alaramData[0] = (byte) alaramId;// alaram id
        alaramData[1] = (byte) socketId;//socket id
        alaramData[2] = (byte) daySelected;//daybyte
        int arrayCount = 3;
        for (int i = 0; i < ontimeArray.length; i++) {
            alaramData[arrayCount] = ontimeArray[i];
            arrayCount++;
        }
        for (int i = 0; i < offtimeArray.length; i++) {
            alaramData[arrayCount] = offtimeArray[i];
            arrayCount++;
        }
        alaramData[11] = arrayValueTransactionId[2];
        alaramData[12] = arrayValueTransactionId[3];
        Log.d(TAG, "Alaram_Packet Prepared Alaram Packet " + ByteConverter.getHexStringFromByteArray(alaramData, true) + " Little_BigEndian " + convertToLittleEndian);
        return alaramData;
    }

    public static byte[] convert_TimeStampTo_4bytes(int value) {
        if (value > 0) {
            return ByteBuffer.allocate(4).putInt(value).array();
        } else {
            byte[] timeNotSelected = new byte[4];
            timeNotSelected[0] = (byte) 0XFF;
            timeNotSelected[1] = (byte) 0XFF;
            timeNotSelected[2] = (byte) 0XFF;
            timeNotSelected[3] = (byte) 0XFF;
            return timeNotSelected;
        }
    }

    public void connect(BluetoothDevice bluetoothDevice) {
        console.log("PowerSocketBLEService_connect", "connecting using Bluetooth Device Object");
        bleCentralManager.connect(bluetoothDevice);
    }

    public void connect(String deviceAddress) {
        console.log("PowerSocketBLEService_connect", "connecting using Device Address");
        bleCentralManager.connect(deviceAddress);
    }

    public void connect(PowerSocket powerSocket) {
        console.log("PowerSocketBLEService_connect", "connecting using Power Socket");
        this.selectedPowerSocket = powerSocket;
        bleCentralManager.connect(powerSocket.getBleAddress());
    }

    public void disconnect(String deviceAddress) {
        console.log("PowerSocketBLEService_disconnect", "Disconnecting using Device address = " + deviceAddress);
        handler.removeCallbacks(wifiConfigTimeoutTask);
        handler.removeCallbacks(deleteTimeoutTask);
        bleCentralManager.disconnect(deviceAddress);
    }

    public void disconnectAllDevices() {
        List<BluetoothDevice> devicesList = bleCentralManager.getConnectedDevicesList();
        for (BluetoothDevice device : devicesList) {
            bleCentralManager.disconnect(device.getAddress());
        }
    }

    private byte[] getEncryptionKey(boolean isAssociated) {
        byte[] encryptionKey = null;
        if (!isAssociated) {
            encryptionKey = PowerSocketConstants.defaultKey;
        } else {
            String secretKey = preferenceHelper.getSecretKey();
            if (secretKey != null && !secretKey.isEmpty()) {
                encryptionKey = ByteConverter.getByteArrayFromHexString(secretKey);
            }
        }
        return encryptionKey;
    }

    private static Queue<byte[]> getPasswordPackets(byte[] passwordBytes) {
        Queue<byte[]> packetQueue = new ArrayDeque<byte[]>();
        List<byte[]> segmentList = ByteConverter.getDataChunks(passwordBytes, 14);
        for (int i = 0; i < segmentList.size(); i++) {
            byte[] segment = segmentList.get(i);
            byte[] packetData = new byte[segment.length + 2];

            packetData[0] = (byte) passwordBytes.length;  // Total data Length
            packetData[1] = (byte) i;                     // Segment Index
            System.arraycopy(segment, 0, packetData, 2, segment.length);
            packetQueue.add(packetData);
        }
        return packetQueue;
    }

    public List<String> getConnectingDevicesAddressList() {
        return bleCentralManager.getConnectingDevicesAddressList();
    }

    private static class WifiSearchTimeoutTask implements Runnable {
        WifiSearchTimeoutCallback wifiConfigTimeoutCallback;

        private WifiSearchTimeoutTask(WifiSearchTimeoutCallback wifiConfigTimeoutCallback) {
            this.wifiConfigTimeoutCallback = wifiConfigTimeoutCallback;
        }

        @Override
        public void run() {
            // Timeout occurred
            if (this.wifiConfigTimeoutCallback != null) {
                wifiConfigTimeoutCallback.onWifiSearchTimeout();
            }
        }

        public interface WifiSearchTimeoutCallback {
            void onWifiSearchTimeout();
        }
    }

    private static class WifiConfigTimeoutTask implements Runnable {
        WifiConfigTimeoutCallback wifiConfigTimeoutCallback;

        private WifiConfigTimeoutTask(WifiConfigTimeoutCallback wifiConfigTimeoutCallback) {
            this.wifiConfigTimeoutCallback = wifiConfigTimeoutCallback;
        }

        @Override
        public void run() {
            // Timeout occurred
            if (this.wifiConfigTimeoutCallback != null) {
                wifiConfigTimeoutCallback.onWifiConfigTimeout();
            }
        }

        public interface WifiConfigTimeoutCallback {
            void onWifiConfigTimeout();
        }
    }

    private static class DeleteTimeoutTask implements Runnable {
        DeleteTimeoutCallbacks deleteTimeoutCallbacks;

        private DeleteTimeoutTask(DeleteTimeoutCallbacks deleteTimeoutCallbacks) {
            this.deleteTimeoutCallbacks = deleteTimeoutCallbacks;
        }

        @Override
        public void run() {
            if (deleteTimeoutCallbacks != null) {
                deleteTimeoutCallbacks.onTimeout();
            }
        }

        public interface DeleteTimeoutCallbacks {
            void onTimeout();
        }
    }

    private static class ResetTimeoutTask implements Runnable {
        ResetTimeoutCallbacks resetTimeoutCallbacks;

        private ResetTimeoutTask(ResetTimeoutCallbacks resetTimeoutCallbacks) {
            this.resetTimeoutCallbacks = resetTimeoutCallbacks;
        }

        @Override
        public void run() {
            if (resetTimeoutCallbacks != null) {
                resetTimeoutCallbacks.onTimeout();
            }
        }

        public interface ResetTimeoutCallbacks {
            void onTimeout();
        }
    }

    private static class FactorySocketTestTimeoutTask implements Runnable {
        FactorySocketTestCallbacks factorySocketTestCallbacks;

        private FactorySocketTestTimeoutTask(FactorySocketTestCallbacks factorySocketTestCallbacks) {
            this.factorySocketTestCallbacks = factorySocketTestCallbacks;
        }

        @Override
        public void run() {
            if (factorySocketTestCallbacks != null) {
                factorySocketTestCallbacks.onTimeout();
            }
        }

        public interface FactorySocketTestCallbacks {
            void onTimeout();
        }
    }

    public static BigInteger convert4bytes(String value4bytes) {
        return new BigInteger(value4bytes, 16);
    }

    /**
     * Time Stamp Conversion.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTimeFromTimeStamp(long timeStamp, boolean repeatAlaram_true) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        if (repeatAlaram_true) {
            cal.setTimeInMillis(timeStamp * 1000);
            SimpleDateFormat time_12_hours = new SimpleDateFormat("hh.mm aa");
            Date currenTimeZone = (Date) cal.getTime();
            return time_12_hours.format(currenTimeZone);
        } else {
            cal.setTimeInMillis(timeStamp * 1000);
            String date = "";
            String time = "";
            SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm a");
            time = time_sdf.format(cal.getTime());
            /**
             * Date Fetching.
             */
            SimpleDateFormat date_sdf = new SimpleDateFormat("dd/MM/yyyy");
            date = date_sdf.format(cal.getTime());
            return date + "\n" + time;
        }
    }

    /**
     * Used to Convert Timestamp to GMT timmings.
     * Used for repeat alaram
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getTimeFromGMT_TimeStamp(int timeStamp) {
        android.icu.util.Calendar cal = android.icu.util.Calendar.getInstance(android.icu.util.TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(timeStamp * 1000);
        Date currentLocalTime = cal.getTime();
//        DateFormat date = new SimpleDateFormat("dd-MM-yyy HH:mm:ss z");  // To get all details including year etc..
        DateFormat date = new SimpleDateFormat("hh.mm aa");
        date.setTimeZone(android.icu.util.TimeZone.getTimeZone("GMT"));
        String localTime = date.format(currentLocalTime);
        System.out.println("Time ----------->" + localTime);
        return localTime;
    }

    public void cancelDeleteDeviceTimeout() {
        handler.removeCallbacks(deleteTimeoutTask);
    }
}