package com.succorfish.combatdiver.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.succorfish.combatdiver.Vo.VoBluetoothDevices;


/**
 * Created by Jaydeep on 26-12-2017.
 */

public interface onDeviceConnectionStatusChange {

    public void addScanDevices();

    public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices);

    public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice);

    public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice);

    public void onError();

}
