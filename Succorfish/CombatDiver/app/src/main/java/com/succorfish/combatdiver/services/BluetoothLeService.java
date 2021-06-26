/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.succorfish.combatdiver.services;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.succorfish.combatdiver.helper.BLEUtility;
import com.succorfish.combatdiver.helper.SampleGattAttributes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public static BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.succorfish.combatdiver.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.succorfish.combatdiver.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.succorfish.combatdiver.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.succorfish.combatdiver.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.succorfish.combatdiver.EXTRA_DATA";
    public final static String RSSI_DATA = "com.succorfish.combatdiver.RSSI_DATA";
    public final static String ERORR = "com.succorfish.combatdiver.ERORR";

    private BluetoothGattCharacteristic mCharStartStop;
    private BluetoothGattCharacteristic mCharSyncContact;
    private BluetoothGattCharacteristic mCharSyncSurfaceMessage;
    private BluetoothGattCharacteristic mCharSyncDiverMessage;
    private BluetoothGattCharacteristic mCharSendMessage;
    public int updateRssi = 0;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            final String intentAction;
            System.out.println("JD onConnectionStateChange newState..." + newState);
            System.out.println("JD onConnectionStateChange status..." + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (status != 0) {
                    intentAction = ERORR;
                    gatt.close();
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mConnectionState = STATE_DISCONNECTED;
//                            broadcastUpdate(intentAction);

                            broadcastUpdate(intentAction, gatt.getDevice().getName(), gatt.getDevice().getAddress());
                        }
                    }, 200);
                } else {
                    intentAction = ACTION_GATT_CONNECTED;
                    mConnectionState = STATE_CONNECTED;
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                mBluetoothGatt.discoverServices();
//                                broadcastUpdate(intentAction);
                                broadcastUpdate(intentAction, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                                Log.i(TAG, "Connected to GATT server.");
                                // Attempts to discover services after successful connection.
                                Log.i(TAG, "Attempting to start service discovery:" +
                                        mBluetoothGatt.discoverServices());
                            } catch (Exception e) {

                            }
                        }
                    }, 200);
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);
                broadcastUpdate(intentAction, gatt.getDevice().getAddress(), gatt.getDevice().getName());
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            System.out.println("JD onServicesDiscovered status..." + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, gatt.getDevice().getAddress(), gatt.getDevice().getName());
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            System.out.println("JD onCharacteristicRead..." + characteristic.getUuid());
            UUID serviceUUID = characteristic.getService().getUuid();
            UUID charUUID = characteristic.getUuid();
            System.out.println("serviceUUID-" + serviceUUID);
            System.out.println("charUUID-" + charUUID);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            System.out.println("onCharacteristicChanged..." + characteristic.getUuid());
            UUID serviceUUID = characteristic.getService().getUuid();
            UUID charUUID = characteristic.getUuid();
            System.out.println("serviceUUID-" + serviceUUID);
            System.out.println("charUUID-" + charUUID);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            updateRssi = rssi;
            broadcastUpdate(RSSI_DATA, gatt.getDevice().getAddress(), gatt.getDevice().getName());
        }
    };

//    private void broadcastUpdate(final String action) {
//        final Intent intent = new Intent(action);
//        sendBroadcast(intent);
//    }

    private void broadcastUpdate(final String action, String mStringDevicesAddress, String mStringDevicesName) {
        final Intent intent = new Intent(action);
        //		//System.out.println("mStringDevicesAddress"+mStringDevicesAddress);
        intent.putExtra("mStringDevicesAddress", mStringDevicesAddress);
        intent.putExtra("mStringDevicesName", mStringDevicesName);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action, byte[] dataReceived, String mStringDevicesAddress, String mStringDevicesName) {

        final Intent intent = new Intent(action);
        intent.putExtra("mStringDevicesAddress", mStringDevicesAddress);
        intent.putExtra("mStringDevicesName", mStringDevicesName);

        char[] hexArray = "0123456789ABCDEF".toCharArray();

        final char[] hexChars = new char[dataReceived.length * 2];
        for (int j = 0; j < dataReceived.length; j++) {
            int v = dataReceived[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        // For all other profiles, writes the data formatted in HEX.
        final byte[] data = characteristic.getValue();
        if (data != null && data.length > 0) {
            final StringBuilder stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
            System.out.println("JDDDDDDDDDD-stringBuilder.toString()-" + stringBuilder.toString());
//            System.out.println("JDDDDDDDDDD-new String(data)-" + new String(data));
            intent.putExtra(EXTRA_DATA, stringBuilder.toString());
//            String mStringLatLong = stringBuilder.toString();
//            if (mStringLatLong != null && !mStringLatLong.equals("") && !mStringLatLong.equals("null")) {
//                System.out.println("mStringLatLong-" + mStringLatLong);
//                mStringLatLong = mStringLatLong.replaceAll("\\s+", "");
//                System.out.println("mStringLatLong-" + mStringLatLong);
//                if (mStringLatLong.toLowerCase().startsWith("0a0a")) {
//                    if (mStringLatLong != null && mStringLatLong.length() >= 24) {
//                        String strLatPrefix = mStringLatLong.substring(4, 6);
//                        String strLatPostfix = mStringLatLong.substring(6, 14);
//                        String strLonPrefix = mStringLatLong.substring(14, 16);
//                        String strLonPostfix = mStringLatLong.substring(16, 24);
//                        System.out.println("strLatPrefix-" + strLatPrefix);
//                        System.out.println("strLatPostfix-" + strLatPostfix);
//                        System.out.println("strLonPrefix-" + strLonPrefix);
//                        System.out.println("strLonPostfix-" + strLonPostfix);
//                        final String Latitude = BLEUtility.hexToDecimal(strLatPrefix) + "." + BLEUtility.hexToDecimal(strLatPostfix);
//                        final String Longitude = BLEUtility.hexToDecimal(strLonPrefix) + "." + BLEUtility.hexToDecimal(strLonPostfix);
//                        System.out.println("Latitude-" + Latitude);
//                        System.out.println("Longitude-" + Longitude);
//
//                    }
//                }
//            }
        }
        sendBroadcast(intent);


    }

    public void setStartStopCharacteristic(BluetoothGattCharacteristic startStop) {
        mCharStartStop = startStop;
    }

    public void setSyncContactCharacteristic(BluetoothGattCharacteristic syncContact) {
        mCharSyncContact = syncContact;
    }

    public void setSyncSurfaceMsgCharacteristic(BluetoothGattCharacteristic syncSurfaceMsg) {
        mCharSyncSurfaceMessage = syncSurfaceMsg;
    }

    public void setSyncDiverMsgCharacteristic(BluetoothGattCharacteristic syncDiverMsg) {
        mCharSyncDiverMessage = syncDiverMsg;
    }

    public void setSendMessageCharacteristic(BluetoothGattCharacteristic sendMsg) {
        mCharSendMessage = sendMsg;
    }


    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param device The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final BluetoothDevice device) {
//        if (mBluetoothAdapter == null || deviceAddress.equalsIgnoreCase("")||deviceAddress==null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }

//        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && device.getAddress().equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
//                System.out.println("AUTO Connect");
//                mConnectionState = STATE_CONNECTING;
//                return true;
//            } else {
//                return false;
//            }
//        }
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.

//        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddress);
        if (isSDK21()) {
            Method connectGattMethod = null;
            try {
                Log.d(TAG, " prepare for connection");
                connectGattMethod = device.getClass().getMethod("connectGatt", Context.class, boolean.class, BluetoothGattCallback.class, int.class);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            Timer innerTimer = new Timer();
            final Method finalConnectGattMethod = connectGattMethod;
            innerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        mBluetoothGatt = (BluetoothGatt) finalConnectGattMethod.invoke(device, BluetoothLeService.this, false, mGattCallback, 2);
                        Log.d(TAG, " got connection");
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            }, 200);
        } else {
            Timer innerTimer = new Timer();
            innerTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        mBluetoothGatt = device.connectGatt(BluetoothLeService.this, false, mGattCallback);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 200);

        }
        Log.d(TAG, " new connection successfully.");
        mBluetoothDeviceAddress = device.getAddress();

        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void removeDevices(BluetoothDevice device) {
        try {
            if (mBluetoothAdapter == null)
                mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                Log.w(TAG, "BluetoothAdapter not initialized");
                return;
            }
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("STATE_DISCONNECTED..." + STATE_DISCONNECTED);
            String intentAction = ACTION_GATT_DISCONNECTED;
            mConnectionState = STATE_DISCONNECTED;
//			Log.i(TAG, "Disconnected from GATT server.");
            broadcastUpdate(intentAction, device.getAddress(), device.getName());
        } catch (Exception e) {
        }

    }

    static boolean isSDK21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("STATE_DISCONNECTED..." + STATE_DISCONNECTED);
        String intentAction = ACTION_GATT_DISCONNECTED;
        mConnectionState = STATE_DISCONNECTED;
//			Log.i(TAG, "Disconnected from GATT server.");
        broadcastUpdate(intentAction, "", "");
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    public void getBatteryLevel() {
        BluetoothGattService batteryService = mBluetoothGatt.getService(UUID.fromString(SampleGattAttributes.Battery_Service_UUID));
        if (batteryService == null) {
            System.out.println("Battery service not found!");
            return;
        }

        BluetoothGattCharacteristic batteryLevel = batteryService.getCharacteristic(UUID.fromString(SampleGattAttributes.Battery_Level_UUID));
        if (batteryLevel == null) {
            System.out.println("Battery level not found!");
            return;
        }
        mBluetoothGatt.readCharacteristic(batteryLevel);
        System.out.println("batteryLevel = " + mBluetoothGatt.readCharacteristic(batteryLevel));
    }

    public int generateRandomNo() {
//        char[] chars = "0123456789".toCharArray();
//        Random rnd = new Random();
//        StringBuilder mStringBuilderRandom = new StringBuilder();
//        for (int i = 0; i < 4; i++)
//            mStringBuilderRandom.append(chars[rnd.nextInt(chars.length)]);
        Random random = new Random();
        String generatedPassword = String.format("%04d", random.nextInt(9999));
        int generatedNo = Integer.parseInt(generatedPassword.toString());
        System.out.println("generatedNoSequenc" + generatedNo);
        if (generatedNo < 999) {
            return generateRandomNo();
        }
        return Integer.parseInt(generatedPassword.toString());
    }

    public void sendDataToDevice(byte[] allPackets, String serviceType) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        try {
            System.out.println("allPackets Length-" + allPackets.length);
            System.out.println("serviceType-" + serviceType);
            /* write the message onto the characteristics based on its size */
            if (allPackets.length <= 21) {
                System.out.println("--SENT PACKETS--" + BLEUtility.toHexString(allPackets, true));
                if (serviceType.equalsIgnoreCase("Type_StartStop")) {
                    mCharStartStop.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mCharStartStop);
                } else if (serviceType.equalsIgnoreCase("Type_SyncContact")) {
                    mCharSyncContact.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mCharSyncContact);
                } else if (serviceType.equalsIgnoreCase("Type_SyncSurfaceMsg")) {
                    mCharSyncSurfaceMessage.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mCharSyncSurfaceMessage);
                } else if (serviceType.equalsIgnoreCase("Type_SyncDiverMsg")) {
                    mCharSyncDiverMessage.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mCharSyncDiverMessage);
                } else if (serviceType.equalsIgnoreCase("Type_SendMessage")) {
                    System.out.println("SendMessage Length-" + allPackets.length);
                    mCharSendMessage.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mCharSendMessage);
                } else {
                    mCharStartStop.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mCharStartStop);
                }
//                mContPartChar.setValue(allPackets);
//                mBluetoothGatt.writeCharacteristic(mContPartChar);
            } else {
                System.out.println("No any packet sent-");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;
        return mBluetoothGatt.getServices();
    }

    public BluetoothGattService getGattService(String mBluetoothDeviceAddress, String uuid) {
        if (mBluetoothGatt != null) {
            final UUID serviceUuid = UUID.fromString(uuid);
            return mBluetoothGatt.getService(serviceUuid);
        } else {
            return null;
        }
    }

    public void setCharacteristicNotifications(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        System.out.println(TAG + "--1 ");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        System.out.println(TAG + "--2 ");
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        System.out.println(TAG + "--3 ");
        // This is specific to Location.
        System.out.println("UUID-" + characteristic.getUuid());
        System.out.println("UUID-" + SampleGattAttributes.ELITE_SEND_MSG_CHAR.toLowerCase());
//        if (SampleGattAttributes.ELITE_SEND_MSG_CHAR.toLowerCase().equals(characteristic.getUuid())) {
        System.out.println(TAG + "--4 ");
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
        System.out.println(TAG + "--5 ");
//        }
    }

    public void setCharacteristicNotification(BluetoothGattService gattService, String mStringUDID, boolean enabled, String mBluetoothDeviceAddress) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        UUID characteristicUuid = UUID.fromString(mStringUDID);
        System.out.println(TAG + "--characteristicUuid " + characteristicUuid.toString());
        BluetoothGattCharacteristic dataCharacteristic = gattService.getCharacteristic(characteristicUuid);


        final UUID clientCharacteristicConfiguration = UUID.fromString(SampleGattAttributes.SERVICE_ELITE_SERVICE);
        System.out.println(TAG + "--clientCharacteristicConfiguration " + clientCharacteristicConfiguration.toString());
        final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(clientCharacteristicConfiguration);
        System.out.println(TAG + "--1 ");
        if (config == null)
            return;
        System.out.println(TAG + "--2 ");
        // enableNotification/disable remotely
        config.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        System.out.println(TAG + "--3 ");
        mBluetoothGatt.writeDescriptor(config);
        System.out.println(TAG + "--4 ");
//        if(dataCharacteristic != null)
//            mGatta.setCharacteristicNotification(dataCharacteristic, enabled);
//
//        // This is specific to Genuino 101 ledService.
//        if (UUID_GENUINO101_ledService.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID_GENUINO101_switchChare);
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }

}
