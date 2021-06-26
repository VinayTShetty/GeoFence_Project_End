package com.vithamastech.smartlight.interfaces_dialog;

import android.content.DialogInterface;

public interface WifiDialogSSIDCallBack {
    public abstract void PositiveMethod(DialogInterface dialog, int id,String SSIS,String password);

    public abstract void NegativeMethod(DialogInterface dialog, int id);
}
