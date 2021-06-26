package com.succorfish.eliteoperator.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    private final SharedPreferences mPrefs;
    private final SharedPreferences mPrefsGlobal;

    public PreferenceHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefsGlobal = context.getSharedPreferences("PreferencecEliteOperator", Context.MODE_PRIVATE);
    }

    private String PREF_DeviceToken = "DeviceToken";
    private String PREF_DeviceHeat1Msg = "DeviceHeat1Msg";
    private String PREF_DeviceHeat2Msg = "DeviceHeat2Msg";
    private String PREF_DeviceHeat3Msg = "DeviceHeat3Msg";
    private String PREF_DeviceHeat4Msg = "DeviceHeat4Msg";
    private String PREF_GSM_Interval = "gsm_interval";
    private String PREF_GSM_TimeOut = "gsm_timeout";
    private String PREF_GPS_Interval = "gps_interval";
    private String PREF_GPS_TimeOut = "gps_timeout";
    private String PREF_Iridium_Interval = "iridium_interval";
    private String PREF_Iridium_TimeOut = "iridium_timeout";

    public String getDeviceToken() {
        String str = mPrefs.getString(PREF_DeviceToken, "");
        return str;
    }

    public void setDeviceToken(String DeviceToken) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_DeviceToken, DeviceToken);
        mEditor.commit();
    }

    public String getDeviceHeat1Msg() {
        return mPrefs.getString(PREF_DeviceHeat1Msg, "1");
    }

    public void setDeviceHeat1Msg(String MSG1) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_DeviceHeat1Msg, MSG1);
        mEditor.commit();
    }

    public String getDeviceHeat2Msg() {
        return mPrefs.getString(PREF_DeviceHeat2Msg, "2");
    }

    public void setDeviceHeat2Msg(String MSG1) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_DeviceHeat2Msg, MSG1);
        mEditor.commit();
    }

    public String getDeviceHeat3Msg() {
        return mPrefs.getString(PREF_DeviceHeat3Msg, "3");
    }

    public void setDeviceHeat3Msg(String MSG1) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_DeviceHeat3Msg, MSG1);
        mEditor.commit();
    }

    public String getDeviceHeat4Msg() {
        return mPrefs.getString(PREF_DeviceHeat4Msg, "4");
    }

    public void setDeviceHeat4Msg(String MSG1) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_DeviceHeat4Msg, MSG1);
        mEditor.commit();
    }

    public String getGSMInterval() {
        return mPrefs.getString(PREF_GSM_Interval, "");
    }

    public void setGSMInterval(String gsm_interval) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_GSM_Interval, gsm_interval);
        mEditor.commit();
    }

    public String getGSMTimeOut() {
        return mPrefs.getString(PREF_GSM_TimeOut, "");
    }

    public void setGSMTimeOut(String GSM_TimeOut) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_GSM_TimeOut, GSM_TimeOut);
        mEditor.commit();
    }

    public String getGPSInterval() {
        return mPrefs.getString(PREF_GPS_Interval, "");
    }

    public void setGPSInterval(String gps_interval) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_GPS_Interval, gps_interval);
        mEditor.commit();
    }

    public String getGPSTimeOut() {
        return mPrefs.getString(PREF_GPS_TimeOut, "");
    }

    public void setGPSTimeOut(String GPS_TimeOut) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_GPS_TimeOut, GPS_TimeOut);
        mEditor.commit();
    }

    public String getIridiumInterval() {
        return mPrefs.getString(PREF_Iridium_Interval, "");
    }

    public void setIridiumInterval(String Iridium_interval) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_Iridium_Interval, Iridium_interval);
        mEditor.commit();
    }

    public String getIridiumTimeOut() {
        return mPrefs.getString(PREF_Iridium_TimeOut, "");
    }

    public void setIridiumTimeOut(String Iridium_TimeOut) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_Iridium_TimeOut, Iridium_TimeOut);
        mEditor.commit();
    }

    public void ClearAllData() {
        Editor mEditor = mPrefs.edit();
        mEditor.clear();
        mEditor.commit();
    }

    public void ResetPrefData() {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_DeviceHeat1Msg, "");
        mEditor.putString(PREF_DeviceHeat2Msg, "");
        mEditor.putString(PREF_DeviceHeat3Msg, "");
        mEditor.putString(PREF_DeviceHeat4Msg, "");
        mEditor.putString(PREF_GSM_Interval, "");
        mEditor.putString(PREF_GSM_Interval, "");
        mEditor.putString(PREF_GPS_Interval, "");
        mEditor.putString(PREF_GPS_TimeOut, "");
        mEditor.putString(PREF_Iridium_Interval, "");
        mEditor.putString(PREF_Iridium_TimeOut, "");
        mEditor.commit();
    }

}
