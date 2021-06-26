package com.vithamastech.smartlight.interfaces;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;

import com.vithamastech.smartlight.Vo.VoBluetoothDevices;

/**
 * Created by Jaydeep on 26-12-2017.
 * on Hard reset success call back.
 */

public interface OnHardResetListener {

    public void onHardResetRequestSuccess();

}
