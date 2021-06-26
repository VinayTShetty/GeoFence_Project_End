package com.vithamastech.smartlight.services;

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

import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.SampleGattAttributes;

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

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.vithamastech.smartlight.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.vithamastech.smartlight.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.vithamastech.smartlight.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.vithamastech.smartlight.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.vithamastech.smartlight.EXTRA_DATA";
    public final static String BLE_NOTIFICATION_TYPE =
            "com.vithamastech.smartlight.BLE_NOTIFICATION_TYPE";
    public final static String RSSI_DATA = "com.vithamastech.smartlight.RSSI_DATA";
    public final static String ERORR = "com.vithamastech.smartlight.ERORR";

    private BluetoothGattCharacteristic mContPartChar;
    private BluetoothGattCharacteristic mDeviceAuthChar;
    private BluetoothGattCharacteristic mDeviceHardResetChar;
    public int updateRSSI = 0;
    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.

    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {
            console.log("BluetoothLEService_ConnectionState", "New State " + newState);
            console.log("BluetoothLEService_ConnectionState", "Status " + status);

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                try {
                    console.log("BluetoothLEService_ConnectionState", "BLE Device Connected");

                    BluetoothDevice connectedDevice = gatt.getDevice();
                    String deviceName = connectedDevice.getName();
                    String deviceAddress = connectedDevice.getAddress();

                    console.log("BluetoothLEService_DeviceName", "Device Name = " + deviceName);
                    console.log("BluetoothLEService_DeviceName", "Device Address = " + deviceAddress);

                    // Broadcast to UI Layer (Activity)
                    broadcastUpdate(ACTION_GATT_CONNECTED, deviceName, deviceAddress);

                    boolean isDiscoveringServices = mBluetoothGatt.discoverServices();
                    // Attempts to discover services after successful connection.
                    console.log("BluetoothLEService", "Attempting to start service discovery: " + isDiscoveringServices);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                try {
                    console.log("BluetoothLEService_ConnectionState", "BLE Device Disconnected");
                    mBluetoothGatt.close();
                    refresh(mBluetoothGatt);
                    mBluetoothGatt = null;

                    broadcastUpdate(ACTION_GATT_DISCONNECTED, gatt.getDevice().getAddress(), gatt.getDevice().getName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            console.log("BluetoothLEService_ServiceDiscovery", "Gatt Status = " + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                console.log("BluetoothLEService_ServiceDiscovery", "Services discovered");
                BluetoothDevice connectedDevice = gatt.getDevice();     // Extract Bluetooth Device from Bluetooth Gatt Object
                String deviceName = connectedDevice.getName();          // Extract Device Name from Bluetooth Device
                String deviceAddress = connectedDevice.getAddress();    // Extract Device Address from Bluetooth Device

                // Broadcast to UI Layer (Activity)
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED, deviceName, deviceAddress);
            } else {
                console.log("BluetoothLEService_ServiceDiscovery", "Service Discovery Failed!!!");
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            System.out.println("JD onCharacteristicRead Status..." + status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                System.out.println("JD onCharacteristicRead..." + characteristic.getUuid());
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
//            System.out.println("onCharacteristicChanged..." + characteristic.getUuid());
//            UUID serviceUUID = characteristic.getService().getUuid();
//            UUID charUUID = characteristic.getUuid();
//            System.out.println("serviceUUID-" + serviceUUID);
//            System.out.println("charUUID-" + charUUID);
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int mIntRssi, int status) {
            updateRSSI = mIntRssi;
            broadcastUpdate(RSSI_DATA, gatt.getDevice().getAddress(), gatt.getDevice().getName());
        }
    };

//    private void broadcastUpdate(final String action) {
//        final Intent intent = new Intent(action);
//        sendBroadcast(intent);
//    }

    /*Send Broadcast message */
    private void broadcastUpdate(final String action, String mStringDevicesAddress, String mStringDevicesName) {
        final Intent intent = new Intent(action);
        intent.putExtra("mStringDevicesAddress", mStringDevicesAddress);
        intent.putExtra("mStringDevicesName", mStringDevicesName);
        sendBroadcast(intent);
    }

    /*Send Broadcast message */
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

    // set ble communication characteristic
    public void setContPartCharacteristic(BluetoothGattCharacteristic contPartChar) {
        mContPartChar = contPartChar;
    }

    // get ble communication characteristic
    public BluetoothGattCharacteristic getContPartCharacteristic() {
        return mContPartChar;
    }

    // set ble connection authentication characteristic
    public void setDeviceAuthCharacteristic(BluetoothGattCharacteristic deviceChar) {
        mDeviceAuthChar = deviceChar;
    }

    // get ble connection authentication characteristic
    public BluetoothGattCharacteristic getDeviceAuthCharacteristic() {
        return mDeviceAuthChar;
    }

    // set ble hard reset characteristic
    public void setDeviceHardResetCharacteristic(BluetoothGattCharacteristic resetChar) {
        mDeviceHardResetChar = resetChar;
    }

    // get ble hard reset characteristic
    public BluetoothGattCharacteristic getDeviceHardResetCharacteristic() {
        return mDeviceHardResetChar;
    }

    String stringPacket;
    byte[] data;
    UUID charUUID;

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        // For all other profiles, writes the data formatted in HEX.
//        final String name = characteristic.getStringValue(0);
        data = characteristic.getValue();
        charUUID = characteristic.getUuid();
        stringPacket = BLEUtility.toHexString(data, true);
        intent.putExtra(EXTRA_DATA, stringPacket);

        if (charUUID.equals(SampleGattAttributes.UUID_SMART_MESH_CONT_CHAR)) {
            // Authentication Data
            intent.putExtra(BLE_NOTIFICATION_TYPE, 1);
        } else {
            intent.putExtra(BLE_NOTIFICATION_TYPE, 0);
        }
        data = null;
        sendBroadcast(intent);
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
//        close();
        disconnect();
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
                                    // mBluetoothDeviceAddress = device.getAddress();
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

    /*Disconnect Ble Device*/
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
                    if (device == null) {
                        Log.w(TAG, "Device not found.  Unable to connect.");
                        return;
                    }
                    mBluetoothGatt.disconnect();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
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
        if (mBluetoothAdapter == null)
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


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
            System.out.println("BluetoothAdapter not initialized");
            return;
        }
        console.log("scsscsdc_read","here");
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /* Transmit Device authentication message*/
    public void sendDeviceAuthMsg(byte[] authPackets) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("BluetoothAdapter not initialized");
            return;
        }
        try {
            System.out.println(TAG + "SEND Auth Length-" + authPackets.length + "-PACKET-" + BLEUtility.toHexString(authPackets, true));
            /* write the message onto the characteristics based on its size */
            if (authPackets.length <= 20) {
                mDeviceAuthChar.setValue(authPackets);
                mBluetoothGatt.writeCharacteristic(mDeviceAuthChar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Transmit Device hard reset message*/
    public void sendDeviceHardResetMsg(byte[] resetPackets) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("BluetoothAdapter not initialized");
            return;
        }
        try {
            System.out.println(TAG + "SEND Hard Reset Length-" + resetPackets.length + "-PACKET-" + BLEUtility.toHexString(resetPackets, true));
            /* write the message onto the characteristics based on its size */
            if (resetPackets.length <= 20) {
                mDeviceHardResetChar.setValue(resetPackets);
                mBluetoothGatt.writeCharacteristic(mDeviceHardResetChar);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* Transmit message over ble connection*/
    public void TransmitMessageOverMesh(byte[] allPackets) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("BluetoothAdapter not initialized");
            return;
        }
        try {
            System.out.println(TAG + "SEND Length-" + allPackets.length + "-PACKET-" + BLEUtility.toHexString(allPackets, true));
            /* write the message onto the characteristics based on its size */
            if (allPackets.length <= 20) {
                mContPartChar.setValue(allPackets);
                mBluetoothGatt.writeCharacteristic(mContPartChar);
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

    /* Get Bluetooth Gatt Service*/
    public BluetoothGattService getGattService(String mBluetoothDeviceAddress, String uuid) {
        if (mBluetoothGatt != null) {
            final UUID serviceUuid = UUID.fromString(uuid);
            return mBluetoothGatt.getService(serviceUuid);
        } else {
            return null;
        }
    }

    /*Enable characteristics notification*/
    public void setCharacteristicNotifications(BluetoothGattCharacteristic characteristic,
                                               boolean enabled) {
        System.out.println(TAG + "--1 ");
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            System.out.println("BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
        // This is specific to Location.
        System.out.println("UUID-" + characteristic.getUuid());
//        System.out.println("UUID-" + SampleGattAttributes.ELITE_SEND_MSG_CHAR.toLowerCase());
//        if (SampleGattAttributes.ELITE_SEND_MSG_CHAR.toLowerCase().equals(characteristic.getUuid())) {
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        descriptor.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(descriptor);
//        }
    }

    /*Enable characteristics notification*/
    public void setCharacteristicNotification(BluetoothGattService gattService, String mStringUDID, boolean enabled, String mBluetoothDeviceAddress) {

        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        UUID characteristicUuid = UUID.fromString(mStringUDID);
        System.out.println(TAG + "--characteristicUuid " + characteristicUuid.toString());
        BluetoothGattCharacteristic dataCharacteristic = gattService.getCharacteristic(characteristicUuid);


        final UUID clientCharacteristicConfiguration = UUID.fromString(SampleGattAttributes.SERVICE_SMART_MESH_SERVICE);
        System.out.println(TAG + "--clientCharacteristicConfiguration " + clientCharacteristicConfiguration.toString());
        final BluetoothGattDescriptor config = dataCharacteristic.getDescriptor(clientCharacteristicConfiguration);
        if (config == null)
            return;
        // enableNotification/disable remotely
        config.setValue(enabled ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        mBluetoothGatt.writeDescriptor(config);
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

    /**
     * Clears the GATT services cache, so that new services can be discovered
     *
     * @param bluetoothGatt GATT Client to clear service's discovery cache
     */
    private void refresh(BluetoothGatt bluetoothGatt) {
        try {
            Method method = bluetoothGatt.getClass().getMethod("refresh", (Class[]) null);
            method.invoke(bluetoothGatt, (Object[]) null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<BluetoothDevice> getConnectedDevicesList() {
        // GATT Server Profile - 8
        return mBluetoothManager.getConnectedDevices(BluetoothGatt.GATT_SERVER);
    }
}