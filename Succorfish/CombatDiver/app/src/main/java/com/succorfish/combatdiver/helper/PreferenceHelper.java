package com.succorfish.combatdiver.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    private final SharedPreferences mPrefs;
    private final SharedPreferences mPrefsGlobal;

    public PreferenceHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefsGlobal = context.getSharedPreferences("PreferencecCombatDiver", Context.MODE_PRIVATE);
    }

    private String PREF_DeviceToken = "DeviceToken";
    private String PREF_DeviceHeat1Msg = "DeviceHeat1Msg";
    private String PREF_DeviceHeat2Msg = "DeviceHeat2Msg";
    private String PREF_DeviceHeat3Msg = "DeviceHeat3Msg";
    private String PREF_DeviceHeat4Msg = "DeviceHeat4Msg";

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
        mEditor.commit();
    }

}
