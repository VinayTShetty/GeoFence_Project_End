package com.vithamastech.smartlight.services;

import android.content.ContentValues;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.gson.Gson;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocketAlaramDetails;
import com.vithamastech.smartlight.PowerSocketUtils.BLECommandUtils;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.PowerSocketUtils.Constants;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;


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
import static com.vithamastech.smartlight.services.PowerSocketBLEService.deleteAlarma;
import static com.vithamastech.smartlight.services.PowerSocketBLEService.getAlarampacketData;

public class PowerSocketMQTTService {
    public static final String TAG = PowerSocketMQTTService.class.getSimpleName();
    private static PowerSocketMQTTService instance;
    final String serverUri = "ssl://iot.vithamastech.com:8883";
    final String username = "vithamas";
    final String password = "vith@123";
    private String firmwareVersion;
    private Context context;
    private PowerSocket powerSocket;

    private MqttAndroidClient mqttAndroidClient;
    private PowerSocketMQTTEventCallbacks callbacks;
    private Handler handler;
    private DeleteTimeoutTask deleteTimeoutTask;
    private SocketControlTimeoutTask socketControlTimeoutTask;
    private OTAUpdateTimeoutTask otaUpdateTimeoutTask;

    private static ArrayList<PowerSocketAlaramDetails> powerSocketAlarmDetailsArrayList = new ArrayList<PowerSocketAlaramDetails>();
    private ArrayList<PowerSocketAlaramDetails> powerSocketAlarmSetArrayList = new ArrayList<PowerSocketAlaramDetails>();
    private static final int MAX_DELETE_DEVICE_TIMEOUT = 10000;
    private static final int MAX_SOCKET_CONTROL_TIMEOUT = 10000;
    private static final int MAX_OTA_UPDATE_TIMEOUT = 30 * 60 * 1000;
    private boolean isUserSetTimeSelected = true;  // Default true

    private PowerSocketMQTTService(Context context) {
        this.context = context.getApplicationContext();
        handler = new Handler();
        deleteTimeoutTask = new DeleteTimeoutTask(deleteTimeoutCallbacks);
        socketControlTimeoutTask = new SocketControlTimeoutTask(socketControlTimeoutCallbacks);
        otaUpdateTimeoutTask = new OTAUpdateTimeoutTask(otaUpdateTimeoutCallbacks);
    }

    public static PowerSocketMQTTService getInstance(Context context) {
        if (instance == null) {
            instance = new PowerSocketMQTTService(context);
        }
        return instance;
    }

    public void initialize() {
        String clientId = UUID.randomUUID().toString();
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(mqttCallback);
        connectToMqtt();
    }

    public void initialize(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        String clientId = UUID.randomUUID().toString();
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(mqttCallback);
        connectToMqtt();
    }

    /*
    1. For enabling Automatic Reconnection => Set Automatic Reconnect to true
                                           => Set Clean Session to false -> This is to maintain connection state for reconnects

    2. For disabling Automatic connection  => Set Automatic Reconnect to false
                                           => Set Clean session to true -> This is to clear connection state for reconnects
     */
    private void connectToMqtt() {
        try {
            MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
            mqttConnectOptions.setAutomaticReconnect(false);
            mqttConnectOptions.setCleanSession(true);
            mqttConnectOptions.setConnectionTimeout(20000);

            mqttConnectOptions.setUserName(username);
            mqttConnectOptions.setPassword(password.toCharArray());

            InputStream input = context.getAssets().open("keystore.bks");

            mqttConnectOptions.setSocketFactory(mqttAndroidClient.getSSLSocketFactory(input, "server"));

            if (!mqttAndroidClient.isConnected()) {
                mqttAndroidClient.connect(mqttConnectOptions, null, null);
            }
        } catch (MqttException | IOException ex) {
            ex.printStackTrace();
            if (callbacks != null) {
                callbacks.onMQTTConnectionFailed();
            }
        }
    }

    MqttCallbackExtended mqttCallback = new MqttCallbackExtended() {
        @Override
        public void connectComplete(boolean reconnect, String serverURI) {
            console.log("PowerSocketControlActivity_Mqtt", "reconnect = " + reconnect);
            console.log("PowerSocketControlActivity_Mqtt", "Connected to = " + serverURI);
            if (callbacks != null) {
                callbacks.onMQTTConnected();
            }
        }

        @Override
        public void connectionLost(Throwable cause) {
            console.log("PowerSocketControlActivity_Mqtt", "Disconnected");
            handler.removeCallbacks(deleteTimeoutTask);
            handler.removeCallbacks(socketControlTimeoutTask);
            handler.removeCallbacks(otaUpdateTimeoutTask);
            if (callbacks != null) {
                callbacks.onMQTTDisconnected();
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void messageArrived(String topic, MqttMessage message) throws Exception {
            console.log("PowerSocketControlActivity_Mqtt", "Message Received");
            byte[] rawData = message.getPayload();
            console.log("PowerSocketControlActivity_Mqtt", ByteConverter.getHexStringFromByteArray(rawData, true));

            // Data is not encrypted as we use SSL to encrypt messages sent to MQTT broker
            if (rawData != null) {
                byte opcode = rawData[0];                    // Index 0 - Opcode
                int dataLength = rawData[1];                 // Index 1 - Data Length

                byte[] data = null;
                int dataSourceIdx = 2;         // Exclude opcode and data length -> Opcode = Index 0 ; Data Length = Index 1
                int dataDestIdx = dataSourceIdx + dataLength;

                data = ByteConverter.copyOfRange(rawData, dataSourceIdx, dataDestIdx);
                console.log("asxnasxlkanslxans_MQTT", ByteConverter.getHexStringFromByteArray(data, true));

                try {
                    // Extract device address from topic
                    String[] topicContents = topic.split("/");
                    console.log("asxbajksxbjk", new Gson().toJson(topicContents));

                    String deviceAddress = topicContents[3].replaceAll("(\\w{2})(?!$)", "$1:");
                    console.log("aaxjbakjsbx", topic);
                    console.log("yvyuxsvyusx", deviceAddress);

                    switch (opcode) {
                        case BLECommandUtils.SET_UTC_TIME: {
                            handler.removeCallbacks(socketControlTimeoutTask);
                            if (callbacks != null) {
                                callbacks.onCurrentTimeSet(deviceAddress);
                            }
                        }
                        break;
                        case BLECommandUtils.CHECK_SOCKET_DIAGNOSTIC: {
                            handler.removeCallbacks(socketControlTimeoutTask);

                            int transactionIdStartIdx = data.length - 2;
                            int transactionIdEndIdx = data.length;
                            byte[] transactionIdBytes = ByteConverter.copyOfRange(data, transactionIdStartIdx, transactionIdEndIdx);
                            int transactionId = ByteConverter.convertByteArrayToInt(transactionIdBytes);

                            int switchDataStartIdx = 0;
                            int switchDataEndIdx = data.length - 2;
                            byte[] switchData = ByteConverter.copyOfRange(data, switchDataStartIdx, switchDataEndIdx);

                            if (callbacks != null) {
                                callbacks.onCheckSocketDiagnostics(deviceAddress, transactionId, switchData);
                            }
                        }
                        break;
                        case BLECommandUtils.POWER_ON_OFF_SINGLE_SOCKET: {
                            handler.removeCallbacks(socketControlTimeoutTask);

                            //                        01 01 01 01 01 01 00 00 01
                            int transactionIdStartIdx = data.length - 3;
                            int transactionIdEndIdx = data.length - 1;
                            byte[] transactionIdBytes = ByteConverter.copyOfRange(data, transactionIdStartIdx, transactionIdEndIdx);
                            int transactionId = ByteConverter.convertByteArrayToInt(transactionIdBytes);

                            int switchDataStartIdx = 0;
                            int switchDataEndIdx = data.length - 3;
                            byte[] switchData = ByteConverter.copyOfRange(data, switchDataStartIdx, switchDataEndIdx);

                            if (callbacks != null) {
                                callbacks.onSingleSocketStateChange(deviceAddress, transactionId, switchData);
                            }
                        }
                        break;
                        case BLECommandUtils.POWER_ON_OFF_ALL_SOCKET: {
                            handler.removeCallbacks(socketControlTimeoutTask);

                            int transactionIdStartIdx = data.length - 3;
                            int transactionIdEndIdx = data.length - 1;
                            byte[] transactionIdBytes = ByteConverter.copyOfRange(data, transactionIdStartIdx, transactionIdEndIdx);
                            int transactionId = ByteConverter.convertByteArrayToInt(transactionIdBytes);

                            int switchDataStartIdx = 0;
                            int switchDataEndIdx = data.length - 3;
                            byte[] switchData = ByteConverter.copyOfRange(data, switchDataStartIdx, switchDataEndIdx);

                            if (callbacks != null) {
                                callbacks.onAllSocketsStateChange(deviceAddress, transactionId, switchData);
                            }
                        }
                        break;
                        case BLECommandUtils.DELETE_DEVICE_REGISTRATION_INFO: {
                            handler.removeCallbacks(deleteTimeoutTask);
                            int deleteStatus = ByteConverter.convertByteArrayToInt(data);
                            switch (deleteStatus) {
                                case Constants.SUCCESS: // Success
                                    if (callbacks != null) {
                                        callbacks.onDeviceRemoved(deviceAddress);
                                    }
                                    break;
                                case Constants.FAILURE: //  Failure
                                    if (callbacks != null) {
                                        callbacks.onDeviceRemovalFailed(deviceAddress);
                                    }
                                    break;
                            }
                        }
                        break;

                        case BLECommandUtils.OTA_UPDATE:
                            handler.removeCallbacks(otaUpdateTimeoutTask);
                            console.log("askxaksxbjkasbxkjbasjkx", ByteConverter.getHexStringFromByteArray(data, true));
                            if (callbacks != null) {
                                callbacks.onOTAUpdateSuccessful(deviceAddress, firmwareVersion);
                            }
                            break;

                        case BLECommandUtils.DELETE_ALARM:
                         /*   handler.removeCallbacks(runnableInMs);
                            byte socketId_delete = (byte) (data[0] & 0xFF);          // Socket ID - Index 0
                            byte AlarmStatus_delete = (byte) (data[1] & 0xFF);
                            callbacks.onAlaramDelete(socketId_delete, AlarmStatus_delete);
                            break;*/
                        {
                            /**
                             *        String bleAddress = powerSocket.bleAddress.replace(":", "");
                             *         String quereyDelete_Alaram_1 = "DELETE FROM " + mTablePowerSocket + " WHERE " + mfield_socket_id + "= '" + selected_ScoketdId + "'" + " AND " + mfield_alarm_id + "= '" + alaramId_1 + "'" + " AND " + mfield_ble_address + " ='" + bleAddress + "'";
                             *         mActivity.mDbHelper.exeQuery(quereyDelete_Alaram_1);
                             */
                            Log.d(TAG, "DeleteIssue Recieved Alaram Packets MQTT " + ByteConverter.getHexStringFromByteArray(data, true));
                            String bleAddress = deviceAddress.toUpperCase().replace(":", "");
                            console.log("Delete Alaram", ByteConverter.getHexStringFromByteArray(data, true));
                            byte alaramId = (byte) (data[0] & 0xff);
                            byte[] transactionIdArray = {data[1], data[2]};
                            byte deleteAlaramStatus = (byte) (data[3] & 0xff);
                            String hexConvertedTransactionId = ByteConverter.getHexStringFromByteArray(transactionIdArray, false);

                            final int transactionId = Integer.parseInt("" + convert4bytes(hexConvertedTransactionId));
                            if (deleteAlaramStatus == SUCESS) {
                                AsyncTask.execute(() -> {
                                    String quereyDelete_Alaram = "DELETE FROM " + mTableAlarmPowerSocket + " WHERE " + mfield_alarm_id + "= " + alaramId + "" + " AND " + mfield_ble_address + " = '" + bleAddress + "'";
                                    DBHelper mDbHelper = DBHelper.getDBHelperInstance();
                                    mDbHelper.exeQuery(quereyDelete_Alaram);
                                    if (callbacks != null) {
                                        callbacks.onAlaramDeleteMqtt(deviceAddress, transactionId, alaramId, deleteAlaramStatus);
                                    }
                                });

                            } else if (deleteAlaramStatus == FAILURE) {
                                if (callbacks != null) {
                                    callbacks.onAlaramDeleteMqtt(deviceAddress, transactionId, alaramId, deleteAlaramStatus);
                                }
                            }
                            break;
                        }
                        case BLECommandUtils.REQUEST_ALARM_SET:
                          /*  handler.removeCallbacks(runnableInMs);
                            byte socketId = (byte) (data[0] & 0xFF);          // Socket ID - Index 0
                            byte setAlarmStatus = (byte) (data[1] & 0xFF);    // Alarm Status - Index 1
                            callbacks.onAlaramSet(socketId, setAlarmStatus);
                            break;*/
                        {
                            byte[] inValidTimeStamp = {(byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff};
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
                                final int transactionId = Integer.parseInt("" + convert4bytes(hexConvertedTransactionId));
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
                                powerSocketAlarmSetArrayList = new ArrayList<PowerSocketAlaramDetails>();
                                powerSocketAlarmSetArrayList.add(new PowerSocketAlaramDetails(data[0], data[1], (byte) dayByte, onTimeStamp, offTimeStamp, alaramState, binayDfaultValues, bleAddress, onOriginalTime, offOriginalTime));
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (int i = 0; i < powerSocketAlarmSetArrayList.size(); i++) {
                                                    PowerSocketAlaramDetails powerSocketAlaramDetails = powerSocketAlarmSetArrayList.get(i);
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
                                                if (callbacks != null) {
                                                    callbacks.onAlaramSetMqtt(deviceAddress, transactionId, alaramId, socketId, sucess_failure);
                                                }

                                            }
                                        });
                                    }
                                });

                            } else if ((data.length == 14) && (data[13] == FAILURE)) {
                                if (callbacks != null) {
                                    byte[] transactionIdArray = {data[11], data[12]};
                                    String hexConvertedTransactionId = ByteConverter.getHexStringFromByteArray(transactionIdArray, false);
                                    int transactionId = -1;
                                    transactionId = Integer.parseInt("" + convert4bytes(hexConvertedTransactionId));
                                    byte alaramId = data[0];
                                    byte socketId = data[1];
                                    byte sucess_failure = data[13];
                                    callbacks.onAlaramSetMqtt(deviceAddress, transactionId, alaramId, socketId, sucess_failure);
                                }
                            }
                            break;
                        }
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
                                powerSocketAlarmDetailsArrayList.add(new PowerSocketAlaramDetails(data[0], data[1], (byte) dayByte, onTimeStamp, offTimeStamp, alaramState, binayDfaultValues, bleAddress, onOriginalTime, offOriginalTime));
                            } else if (data.length == 1 && data[0] == 01) {
                                /**
                                 * Ending packet data.Process All data
                                 */
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        for (int i = 0; i < powerSocketAlarmDetailsArrayList.size(); i++) {
                                            PowerSocketAlaramDetails powerSocketAlaramDetails = powerSocketAlarmDetailsArrayList.get(i);
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
                                        powerSocketAlarmDetailsArrayList = new ArrayList<PowerSocketAlaramDetails>();
                                        console.log("asxkjbaskjxbajkxb", "Alarm Updated");
                                        if (callbacks != null) {
                                            callbacks.onAlaramUpdatedFromDeviceMQTT(true);
                                        }
                                    }
                                });
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void deliveryComplete(IMqttDeliveryToken token) {
            console.log("PowerSocketControlActivity_Mqtt", "Message Delivered");
            try {
                byte[] data = token.getMessage().getPayload();
                console.log("asxasxasx1111", ByteConverter.getHexStringFromByteArray(data, true));
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    };

    public void subscribe(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        try {
            String subscriptionTopic = getSubscribeTopic(powerSocket.bleAddress);
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.subscribe(subscriptionTopic, 2, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        console.log("asxkjbasjxakjbx", "subscribed = " + asyncActionToken.getTopics()[0]);
                        try {
                            String topic = asyncActionToken.getTopics()[0];
                            String[] topicContents = topic.split("/");
                            console.log("asxbajksxbjk", new Gson().toJson(topicContents));

                            String deviceAddress = topicContents[3].replaceAll("(\\w{2})(?!$)", "$1:");
                            console.log("aaxjbakjsbx", topic);
                            console.log("yvyuxsvyusx", deviceAddress);
                            if (callbacks != null) {
                                callbacks.onTopicSubscribed(deviceAddress);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("MQTT_DEMO  exception occurred");
            ex.printStackTrace();
        }
    }

    public void subscribeOTA(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        try {
            String subscriptionTopic = "vps/ota_confirm/"+powerSocket.bleAddress.replace(":","");
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.subscribe(subscriptionTopic, 2, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        console.log("asxkjbasjxakjbx", "subscribed = " + asyncActionToken.getTopics()[0]);
                        try {
                            String topic = asyncActionToken.getTopics()[0];
                            String[] topicContents = topic.split("/");
                            console.log("asxbajksxbjk", new Gson().toJson(topicContents));

//                            String deviceAddress = topicContents[2].replaceAll("(\\w{2})(?!$)", "$1:");
//                            console.log("aaxjbakjsbx", topic);
//                            console.log("yvyuxsvyusx", deviceAddress);
                            if (callbacks != null) {
                                callbacks.onOTASubscribed(powerSocket.bleAddress);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("MQTT_DEMO  exception occurred");
            ex.printStackTrace();
        }
    }

    public void unsubscribe(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        try {
            String subscriptionTopic = "vps/ota_confirm/"+powerSocket.bleAddress.replace(":","");
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.unsubscribe(subscriptionTopic, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("MQTT_DEMO  exception occurred");
            ex.printStackTrace();
        }
    }

    public void unsubscribeOTA(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        try {
            String subscriptionTopic = getSubscribeTopic(powerSocket.bleAddress);
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.unsubscribe(subscriptionTopic, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {

                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {

                    }
                });
            }
        } catch (Exception ex) {
            System.out.println("MQTT_DEMO  exception occurred");
            ex.printStackTrace();
        }
    }

    private String getSubscribeTopic(String address) {
        String hexString = address.replace(":", "");
        byte[] addressBytes = ByteConverter.getByteArrayFromHexString(hexString);
        hexString = ByteConverter.getHexStringFromByteArray(addressBytes, false);
        return "/vps/app/" + hexString;
    }

    private String getPublishTopic(String address) {
        String hexString = address.replace(":", "");
        byte[] addressBytes = ByteConverter.getByteArrayFromHexString(hexString);
        hexString = ByteConverter.getHexStringFromByteArray(addressBytes, false);
        return "/vps/device/" + hexString;
    }

    public void controlSingleSwitch(PowerSocket powerSocket, byte switchId, byte state, int transactionId) {
        this.powerSocket = powerSocket;
        byte[] bleControlCommandPacket;
        // Check Wifi Connection. Wifi connection is given last priority.
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            String publishTopic = getPublishTopic(powerSocket.bleAddress);
            try {
                bleControlCommandPacket = BLECommandUtils.getSocketControlCommandPacket(switchId, state, transactionId, null);
                mqttAndroidClient.publish(publishTopic, bleControlCommandPacket, 2, false);
                handler.postDelayed(socketControlTimeoutTask, MAX_SOCKET_CONTROL_TIMEOUT);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            // Todo
        }
    }

    public void controlAllSwitches(PowerSocket powerSocket, byte state, int transactionId) {
        this.powerSocket = powerSocket;
        byte[] bleControlCommandPacket;
        // Check Wifi Connection. Wifi connection is given last priority.
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            String publishTopic = getPublishTopic(powerSocket.bleAddress);
            try {
                bleControlCommandPacket = BLECommandUtils.getSocketControlCommandPacket(state, transactionId, null);
                mqttAndroidClient.publish(publishTopic, bleControlCommandPacket, 2, false);
                handler.postDelayed(socketControlTimeoutTask, MAX_SOCKET_CONTROL_TIMEOUT);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        } else {
            // Todo
        }
    }

    public void checkSocketDiagnostics(PowerSocket powerSocket, int transactionId) {
        this.powerSocket = powerSocket;
        byte[] socketDiagnosticsRequestPacket = BLECommandUtils.getSocketStatesRequestPacket(transactionId, null);
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            String publishTopic = getPublishTopic(powerSocket.bleAddress);
            try {
                mqttAndroidClient.publish(publishTopic, socketDiagnosticsRequestPacket, 2, false);
                handler.postDelayed(socketControlTimeoutTask, MAX_SOCKET_CONTROL_TIMEOUT);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void removeDevice(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        byte[] deleteDeviceData = new byte[1];
        deleteDeviceData[0] = 0x00;              // De-register value

        byte[] deleteDeviceRequestPacket = BLECommandUtils.getDeviceRegistrationInfoDeleteRequestPacket(deleteDeviceData, null);
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            String publishTopic = getPublishTopic(powerSocket.bleAddress);
            try {
                mqttAndroidClient.publish(publishTopic, deleteDeviceRequestPacket, 2, false);
                handler.postDelayed(deleteTimeoutTask, MAX_DELETE_DEVICE_TIMEOUT);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void getUTCTime(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        byte[] getUTCTimeCommand = BLECommandUtils.getUTCTimeRequestPacket(null);
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            String publishTopic = getPublishTopic(powerSocket.bleAddress);
            try {
                mqttAndroidClient.publish(publishTopic, getUTCTimeCommand, 2, false);
                handler.postDelayed(deleteTimeoutTask, MAX_SOCKET_CONTROL_TIMEOUT);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCurrentTime(PowerSocket powerSocket, boolean isUserSetTimeSelected) {
        this.isUserSetTimeSelected = isUserSetTimeSelected;
        this.powerSocket = powerSocket;
        int currentTime = (int) (System.currentTimeMillis() / 1000);
        byte[] currentTimeBytes = ByteConverter.convertIntToByteArray(currentTime);
        byte[] getUTCConfigCommand = BLECommandUtils.getConfigUTCTimeRequestPacket(currentTimeBytes, null);
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            String publishTopic = getPublishTopic(powerSocket.bleAddress);
            try {
                mqttAndroidClient.publish(publishTopic, getUTCConfigCommand, 2, false);
                handler.postDelayed(deleteTimeoutTask, MAX_SOCKET_CONTROL_TIMEOUT);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void performOTAUpdate(PowerSocket powerSocket, String firmwareVersion) {
        this.powerSocket = powerSocket;
        this.firmwareVersion = firmwareVersion;
        byte[] requestOTALinkPacket = BLECommandUtils.getOTAUpdateRequestPacket(firmwareVersion, null);
        if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
            String publishTopic = getPublishTopic(powerSocket.bleAddress);
            try {
                mqttAndroidClient.publish(publishTopic, requestOTALinkPacket, 2, false);
                handler.postDelayed(otaUpdateTimeoutTask, MAX_OTA_UPDATE_TIMEOUT);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void setOnPowerSocketMQTTEventCallbacks(PowerSocketMQTTEventCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    public void dispose() {
        try {
            if (mqttAndroidClient != null && mqttAndroidClient.isConnected()) {
                mqttAndroidClient.unregisterResources();
                mqttAndroidClient.close();
                mqttAndroidClient.disconnect();
                mqttAndroidClient = null;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        if (mqttAndroidClient != null) {
            try {
                boolean result = mqttAndroidClient.isConnected();
                if (result) {
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
    }

    DeleteTimeoutTask.DeleteTimeoutCallbacks deleteTimeoutCallbacks = new DeleteTimeoutTask.DeleteTimeoutCallbacks() {
        @Override
        public void onTimeout() {
            if (callbacks != null) {
                callbacks.onMQTTTimeout(powerSocket.bleAddress);
            }
        }
    };

    SocketControlTimeoutTask.SocketControlTimeoutCallbacks socketControlTimeoutCallbacks = new SocketControlTimeoutTask.SocketControlTimeoutCallbacks() {
        @Override
        public void onTimeout() {
            if (callbacks != null) {
                callbacks.onMQTTTimeout(powerSocket.bleAddress);
            }
        }
    };

    OTAUpdateTimeoutTask.OTAUpdateTimeoutTaskCallbacks otaUpdateTimeoutCallbacks = new OTAUpdateTimeoutTask.OTAUpdateTimeoutTaskCallbacks() {
        @Override
        public void onTimeout() {
            if (callbacks != null) {
                callbacks.onMQTTTimeout(powerSocket.bleAddress);
            }
        }
    };


    private static class DeleteTimeoutTask implements Runnable {
        DeleteTimeoutTask.DeleteTimeoutCallbacks deleteTimeoutCallbacks;

        private DeleteTimeoutTask(DeleteTimeoutTask.DeleteTimeoutCallbacks deleteTimeoutCallbacks) {
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

    private static class SocketControlTimeoutTask implements Runnable {
        SocketControlTimeoutCallbacks socketControlTimeoutCallbacks;

        private SocketControlTimeoutTask(SocketControlTimeoutCallbacks socketControlTimeoutCallbacks) {
            this.socketControlTimeoutCallbacks = socketControlTimeoutCallbacks;
        }

        @Override
        public void run() {
            if (socketControlTimeoutCallbacks != null) {
                socketControlTimeoutCallbacks.onTimeout();
            }
        }

        public interface SocketControlTimeoutCallbacks {
            void onTimeout();
        }
    }

    private static class OTAUpdateTimeoutTask implements Runnable {
        OTAUpdateTimeoutTaskCallbacks otaUpdateTimeoutTaskCallbacks;

        private OTAUpdateTimeoutTask(OTAUpdateTimeoutTaskCallbacks otaUpdateTimeoutTaskCallbacks) {
            this.otaUpdateTimeoutTaskCallbacks = otaUpdateTimeoutTaskCallbacks;
        }

        @Override
        public void run() {
            if (otaUpdateTimeoutTaskCallbacks != null) {
                otaUpdateTimeoutTaskCallbacks.onTimeout();
            }
        }

        public interface OTAUpdateTimeoutTaskCallbacks {
            void onTimeout();
        }
    }

    /**
     * Alaram part
     */

    public void setAlaram(int onTimeStamp_alaram_1, int offTimeStamp_alaram_1, int alaramId_1, int numbeOfdaysCheckedValue_alaram_1, PowerSocket powerSocket, int selected_scoketdId, int transactionNumber) {
        this.powerSocket = powerSocket;
        byte[] dataForAlaram = getAlarampacketData(onTimeStamp_alaram_1, offTimeStamp_alaram_1, numbeOfdaysCheckedValue_alaram_1, alaramId_1, selected_scoketdId, false, transactionNumber);
        byte[] alaramRequest_packet = BLECommandUtils.getAlaramSetPacket(dataForAlaram, null);
        String deviceAddress = this.powerSocket.bleAddress;
        String publishTopic = getPublishTopic(deviceAddress);

        try {
            mqttAndroidClient.publish(publishTopic, alaramRequest_packet, 2, false);
            Log.d(TAG, "Alaram_Packet Final MQTT PACKET SENT= " + ByteConverter.getHexStringFromByteArray(alaramRequest_packet, true));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void deleteAlarma_mqtt(PowerSocket powerSocket, int alaramId, int transactionNumber) {
        this.powerSocket = powerSocket;
        byte[] dataForAlaram = deleteAlarma(alaramId, transactionNumber);
        byte[] deleteAlaram_packet = BLECommandUtils.getDeleteAlarampPacket(dataForAlaram, null);
        String deviceAddress = this.powerSocket.bleAddress;
        String publishTopic = getPublishTopic(deviceAddress);
        try {
            mqttAndroidClient.publish(publishTopic, deleteAlaram_packet, 2, false);
            Log.d(TAG, "Alaram_Packet Final MQTT PACKET SENT= " + ByteConverter.getHexStringFromByteArray(deleteAlaram_packet, true));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void askAlaramDetailsFrom_socket_Mqtt(PowerSocket powerSocket) {
        this.powerSocket = powerSocket;
        byte[] dataForAlaram = alaramDetails();
        byte[] alaramDetials_data = BLECommandUtils.getAlarmDetails(dataForAlaram, null);
        String deviceAddress = this.powerSocket.bleAddress;
        String publishTopic = getPublishTopic(deviceAddress);
        try {
            mqttAndroidClient.publish(publishTopic, alaramDetials_data, 2, false);
            Log.d(TAG, "Alaram Request Packet" + ByteConverter.getHexStringFromByteArray(alaramDetials_data, true));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public static byte[] alaramDetails() {
        byte[] dataArray = new byte[2];
        dataArray[0] = REQUEST_ALARM_DETAILS;
        dataArray[1] = 00;
        Log.d(TAG, "Alaram_Packet asking alaramDetails " + ByteConverter.getHexStringFromByteArray(dataArray, true));
        return dataArray;
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

    private static BigInteger convert4bytes(String value4bytes) {
        return new BigInteger(value4bytes, 16);
    }

    /**
     * Data Base operation for saving the alarma details for the application.
     */
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

    public void cancelPendingMQTTTimeouts() {
        handler.removeCallbacks(deleteTimeoutTask);
        handler.removeCallbacks(socketControlTimeoutTask);
    }
}