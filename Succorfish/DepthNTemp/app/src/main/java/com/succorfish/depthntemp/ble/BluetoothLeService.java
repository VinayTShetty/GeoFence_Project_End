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

package com.succorfish.depthntemp.ble;

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
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import com.succorfish.depthntemp.helper.BLEUtility;
import com.succorfish.depthntemp.helper.URLCLASS;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
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
    public static BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.succorfish.DepthNTemp.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.succorfish.DepthNTemp.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.succorfish.DepthNTemp.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.succorfish.DepthNTemp.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.succorfish.DepthNTemp.EXTRA_DATA";
    public final static String BLE_NOTIFICATION_TYPE =
            "com.succorfish.DepthNTemp.BLE_NOTIFICATION_TYPE";
    public final static String RSSI_DATA = "com.succorfish.DepthNTemp.RSSI_DATA";
    public final static String ERORR = "com.succorfish.DepthNTemp.ERORR";

    private final static int BMASK_READ_BATT_LEVEL = 0x0001;
    private final static int BMASK_READ_STAT_MEAS_INT = 0x0002;
    private final static int BMASK_READ_MOV_MEAS_INT = 0x0004;
    private final static int BMASK_READ_GPS_TRACK_INT = 0x0008;
    private final static int BMASK_READ_GPS_BLE_CUTOFF = 0x0010;
    private final static int BMASK_READ_EXPORT_MODE = 0x0020;
    private final static int BMASK_READ_ALL = 0x003F;

    private BluetoothGattCharacteristic mGetDataChar;
    private BluetoothGattCharacteristic mSettingSetCommandChar;
    private BluetoothGattCharacteristic mSettingGetDataChar;

    public int updateRSSI = 0;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
//            final String intentAction;
            System.out.println("JD onConnectionStateChange newState..." + newState);
            System.out.println("JD onConnectionStateChange status..." + status);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                if (status != 0) {
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mBluetoothGatt.close();
                            mBluetoothGatt = null;
                            mConnectionState = STATE_DISCONNECTED;
//                            broadcastUpdate(intentAction);
                            broadcastUpdate(ERORR, gatt.getDevice().getName(), gatt.getDevice().getAddress());
                        }
                    }, 500);
                } else {
                    mConnectionState = STATE_CONNECTED;
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                mBluetoothGatt.discoverServices();
//                                broadcastUpdate(intentAction);
                                broadcastUpdate(ACTION_GATT_CONNECTED, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                                Log.i(TAG, "Connected to GATT server.");
                                // Attempts to discover services after successful connection.
                                Log.i(TAG, "Attempting to start service discovery:" +
                                        mBluetoothGatt.discoverServices());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 500);
                }

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                mConnectionState = STATE_DISCONNECTED;
                Timer innerTimer = new Timer();
                innerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            mBluetoothGatt.close();
                            mBluetoothGatt = null;
                            Log.i(TAG, "Disconnected from GATT server.");
//                broadcastUpdate(intentAction);
                            broadcastUpdate(ACTION_GATT_DISCONNECTED, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, 500);

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
//            System.out.println("JD onCharacteristicRead Status..." + status);
//            UUID serviceUUID = characteristic.getService().getUuid();
//            UUID charUUID = characteristic.getUuid();
//            System.out.println("serviceUUID-" + serviceUUID);
//            System.out.println("charUUID-" + charUUID);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            System.out.println("onCharacteristicChanged..." + characteristic.getUuid());
//            UUID serviceUUID = characteristic.getService().getUuid();
//            UUID charUUID = characteristic.getUuid();
//            System.out.println("serviceUUID-" + serviceUUID);
//            System.out.println("charUUID-" + charUUID);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
            updateRSSI = rssi;
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

    StringBuilder stringBuilder;
    byte[] data;

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        // For all other profiles, writes the data formatted in HEX.
//        final String name = characteristic.getStringValue(0);
        data = characteristic.getValue();
        if (data != null && data.length > 0) {
            stringBuilder = new StringBuilder(data.length);
            for (byte byteChar : data)
                stringBuilder.append(String.format("%02X ", byteChar));
//            System.out.println("JD-stringBuilder-" + stringBuilder.toString());
            intent.putExtra(EXTRA_DATA, stringBuilder.toString());
        }
        UUID charUUID = characteristic.getUuid();

        if (charUUID.equals(SampleGattAttributes.UUID_DNT_GET_DATA)) {
            // Fetch Mission Data
            intent.putExtra(BLE_NOTIFICATION_TYPE, 1);
        } else if (charUUID.equals(SampleGattAttributes.UUID_DNT_SET_SETTING_COMMAND_DATA)) {
            // Fetch Setting Data
            intent.putExtra(BLE_NOTIFICATION_TYPE, 2);
        } else {
            // Fetch Other Data
            intent.putExtra(BLE_NOTIFICATION_TYPE, 3);
        }
        data = null;
        stringBuilder = null;
        sendBroadcast(intent);
    }

    public void setGetDataChar(BluetoothGattCharacteristic dataChar) {
        mGetDataChar = dataChar;
    }

    public void setSettingSetCommandChar(BluetoothGattCharacteristic dataChar) {
        mSettingSetCommandChar = dataChar;
    }

    public BluetoothGattCharacteristic getSettingSetCommandChar() {
        return mSettingSetCommandChar;
    }

    public void setSettingGetDataChar(BluetoothGattCharacteristic dataChar) {
        mSettingGetDataChar = dataChar;
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
    public boolean connect(final BluetoothDevice device, boolean isAutoConnect) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

//        // Previously connected device.  Try to reconnect.
//        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
//                && mBluetoothGatt != null) {
//            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
//            if (mBluetoothGatt.connect()) {
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
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
            mBluetoothGatt.close();
            mBluetoothGatt = null;
        }
        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                // We want to directly connect to the device, so we are setting the autoConnect
                // parameter to false.
                if (isSDK21()) {
                    Method connectGattMethod = null;
                    try {
                        Log.d(TAG, " prepare for connection");
                        connectGattMethod = device.getClass().getMethod("connectGatt", Context.class, boolean.class, BluetoothGattCallback.class, int.class);
                        Timer innerTimer = new Timer();
                        final Method finalConnectGattMethod = connectGattMethod;
                        innerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    mBluetoothGatt = (BluetoothGatt) finalConnectGattMethod.invoke(device, BluetoothLeService.this, false, mGattCallback, 2);
                                    Log.d(TAG, " got connection");
                                    Log.d(TAG, " new connection successfully.");
                                    // mBluetoothDeviceAddress = device.getAddress();
                                    mConnectionState = STATE_CONNECTING;
                                } catch (IllegalAccessException e) {
                                    e.printStackTrace();
                                } catch (InvocationTargetException e) {
                                    e.printStackTrace();
                                } catch (IllegalArgumentException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 200);
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    }

                } else {
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                mBluetoothGatt = device.connectGatt(BluetoothLeService.this, false, mGattCallback);
                                Log.d(TAG, " new connection successfully.");
                                // mBluetoothDeviceAddress = device.getAddress();
                                mConnectionState = STATE_CONNECTING;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, 200);
                }
            }
        });
        return true;
    }

    public boolean connect(final BluetoothDevice device) {
        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

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
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    public void disconnectDevices(final BluetoothDevice device) {

        try {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothAdapter == null)
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                        Log.w(TAG, "Remove Device B-Adapter not initialized");
                        return;
                    }
                    mBluetoothGatt.disconnect();

                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("STATE_DISCONNECTED..." + STATE_DISCONNECTED);
                            String intentAction = ACTION_GATT_DISCONNECTED;
                            mConnectionState = STATE_DISCONNECTED;
//			Log.i(TAG, "Disconnected from GATT server.");
                            broadcastUpdate(intentAction, device.getAddress(), device.getName());
                        }
                    }, 200);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void removeDevices(final BluetoothDevice device) {

        try {
            new Handler(getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if (mBluetoothAdapter == null)
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    if (mBluetoothAdapter == null || mBluetoothGatt == null) {
                        Log.w(TAG, "Remove Device B-Adapter not initialized");
                        return;
                    }
                    mBluetoothGatt.disconnect();

                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("STATE_DISCONNECTED..." + STATE_DISCONNECTED);
                            String intentAction = ACTION_GATT_DISCONNECTED;
                            mConnectionState = STATE_DISCONNECTED;
//			Log.i(TAG, "Disconnected from GATT server.");
                            broadcastUpdate(intentAction, device.getAddress(), device.getName());
                        }
                    }, 200);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();

        Timer innerTimer = new Timer();
        innerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                System.out.println("STATE_DISCONNECTED..." + STATE_DISCONNECTED);
                String intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
//			Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction, "", "");
            }
        }, 200);
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

    public void transmitMessageToDevice(byte[] allPackets, String serviceType) {
//        System.out.println("OVER BLE");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        try {
//            System.out.println("allPackets Length-" + allPackets.length);
            System.out.println("serviceType-" + serviceType);
            /* write the message onto the characteristics based on its size */
            if (allPackets.length <= 20) {
                System.out.println(TAG + "--SEND PACKET-" + BLEUtility.toHexString(allPackets, true));
                if (serviceType.equalsIgnoreCase(URLCLASS.TYPE_COMMAND)) {
                    mSettingSetCommandChar.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mSettingSetCommandChar);
                } else {
                    mSettingGetDataChar.setValue(allPackets);
                    mBluetoothGatt.writeCharacteristic(mSettingGetDataChar);
                }
            } else {
                System.out.println("No any packet sent-");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the battery level on the target device
     */
    public void getValue(int type) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

//        if (type == BMASK_READ_BATT_LEVEL) {
//            mBluetoothGatt.readCharacteristic(mBatteryLevelChar);
//        } else if (type == BMASK_READ_EXPORT_MODE) {
//            mBluetoothGatt.readCharacteristic(mExportModeChar);
//        } else if (type == BMASK_READ_GPS_BLE_CUTOFF) {
//            mBluetoothGatt.readCharacteristic(mGPSBLECutoffChar);
//        } else if (type == BMASK_READ_STAT_MEAS_INT) {
//            mBluetoothGatt.readCharacteristic(mStatPresMeasIntChar);
//        } else if (type == BMASK_READ_MOV_MEAS_INT) {
//            mBluetoothGatt.readCharacteristic(mMovPresMeasIntChar);
//        } else if (type == BMASK_READ_GPS_TRACK_INT) {
//            mBluetoothGatt.readCharacteristic(mGPSTrackingIntChar);
//        }
        Log.d(TAG, "READ TYPE VALUE IS -> " + type);
    }

    public int getConnectionState() {
        return mConnectionState;
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
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // This is specific to Location.
        System.out.println("UUID-" + characteristic.getUuid());
//        System.out.println("UUID-" + SampleGattAttributes.DNT_SET_SETTING_COMMAND_CHAR.toLowerCase());
        BluetoothGattDescriptor descriptor = null;
        if (SampleGattAttributes.UUID_DNT_GET_DATA.equals(characteristic.getUuid())) {
            descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        } else if (SampleGattAttributes.UUID_DNT_SET_SETTING_COMMAND_DATA.equals(characteristic.getUuid())) {
            System.out.println("MATCH");
            descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            if (descriptor == null) {
                System.out.println("NOTIFICATION NOT ENABLE");
                return;
            }
            System.out.println("NOTIFICATION ENABLE");
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }

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


        final UUID clientCharacteristicConfiguration = UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
        System.out.println(TAG + "--clientCharacteristicConfiguration " + clientCharacteristicConfiguration.toString());
        final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(clientCharacteristicConfiguration);
        if (config == null)
            return;
        // enableNotification/disable remotely
        config.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(config);
    }

    static boolean isSDK21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }
}
