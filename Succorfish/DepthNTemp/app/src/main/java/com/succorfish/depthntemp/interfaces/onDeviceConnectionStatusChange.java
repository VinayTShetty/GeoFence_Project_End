package com.succorfish.depthntemp.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.succorfish.depthntemp.vo.VoBluetoothDevices;

/**
 * Created by Jaydeep on 26-12-2017.
 * ble connection callback
 */

public interface onDeviceConnectionStatusChange {

    public void addScanDevices();

    public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices);

    public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice);

    public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice);

    public void onError();

}
