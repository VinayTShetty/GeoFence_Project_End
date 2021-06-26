package com.succorfish.geofence.interfaces;

import android.content.DialogInterface;
public interface ResetDeviceDialogCallBack {
    public abstract void PositiveMethod(DialogInterface dialog, int id);
    public abstract void NegativeMethod(DialogInterface dialog, int id);
}

