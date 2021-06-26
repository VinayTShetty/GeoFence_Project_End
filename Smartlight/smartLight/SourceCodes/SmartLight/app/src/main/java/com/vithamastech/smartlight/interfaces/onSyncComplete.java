package com.vithamastech.smartlight.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.vithamastech.smartlight.Vo.VoBluetoothDevices;

/**
 * Created by Jaydeep on 26-12-2017.
 * Call back device sync complete and group device sync complete
 */

public interface onSyncComplete {

    public void onDeviceSyncComplete();

    public void onGroupSyncComplete();


}
