package com.vithamastech.smartlight.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.evergreen.ble.advertisement.ManufactureData;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketAdvData;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;

/**
 * Created by Jaydeep on 26-12-2017.
 * Ble scanning, connect, disconnect, error callback
 */

public interface onDeviceConnectionStatusChange {

    public void addScanDevices();

    public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices);

    public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices, ManufactureData manufactureData);

    public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice);

    public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice);

    public void onError();
}
