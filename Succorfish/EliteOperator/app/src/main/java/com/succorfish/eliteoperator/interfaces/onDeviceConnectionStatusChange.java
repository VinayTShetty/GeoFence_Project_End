package com.succorfish.eliteoperator.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.succorfish.eliteoperator.Vo.VoBluetoothDevices;


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
