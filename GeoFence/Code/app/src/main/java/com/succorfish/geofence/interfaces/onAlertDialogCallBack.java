package com.succorfish.geofence.interfaces;

import android.content.DialogInterface;

/**
 * Created by Jaydeep on 31-03-2018.
 */

public interface onAlertDialogCallBack {
    public abstract void PositiveMethod(DialogInterface dialog, int id);

    public abstract void NegativeMethod(DialogInterface dialog, int id);
}
