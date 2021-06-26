package com.succorfish.installer.interfaces;

/**
 * Created by JD on 8/3/18.
 * Fragment Back press callback
 */
public interface onBackPressWithAction {
    public void onBackWithAction(String scanResult);

    public void onBackWithAction(String value1, String value2);

    public void onBackWithAction(String imei, String deviceType, String warrantyStatus);

}
