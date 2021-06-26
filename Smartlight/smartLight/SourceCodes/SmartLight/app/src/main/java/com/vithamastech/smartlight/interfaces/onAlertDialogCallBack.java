package com.vithamastech.smartlight.interfaces;

import android.content.DialogInterface;

/**
 * Created by Jaydeep on 31-03-2018.
 * Alert Dialog button action call back
 */

public interface onAlertDialogCallBack {
    public abstract void PositiveMethod(DialogInterface dialog, int id);

    public abstract void NegativeMethod(DialogInterface dialog, int id);
}
