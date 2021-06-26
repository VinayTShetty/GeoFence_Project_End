package com.succorfish.geofence.interfaces;

import android.content.DialogInterface;

public interface onDeviceNameAlert {
    public abstract void PositiveMethod(DialogInterface dialog, int id,String deviceName);
    public abstract void NegativeMethod(DialogInterface dialog, int id);
}
