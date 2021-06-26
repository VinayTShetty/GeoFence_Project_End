package com.succorfish.geofence.interfaceActivityToFragment;

import android.bluetooth.BluetoothDevice;

public interface ConnectionStatus {
    public void connectedDevicePostion(BluetoothDevice bluetoothDevice, boolean status);
}
