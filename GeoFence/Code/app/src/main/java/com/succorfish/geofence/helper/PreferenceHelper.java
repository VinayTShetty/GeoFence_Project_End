package com.succorfish.geofence.helper;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceHelper {
    private static PreferenceHelper mPreferenceHelper;
    private static SharedPreferences mPrefsGlobal;
    private static SharedPreferences.Editor mEditorPrefsGlobal;
    private String PREF_remember_me = "REMEMEMBER_ME";
    private String PREF_remember_me_username = "REMEMEMBER_ME_USERNAME";
    private String PREF_remember_me_password = "REMEMEMBER_ME_PASSWORD";
    private String PREF_USERNAME = "USER_NAME";
    private String PREF_PASSWORD = "PASSWORD";

    public PreferenceHelper(Context context) {
        mPrefsGlobal = context.getSharedPreferences("SuccorfishGeoFence", Context.MODE_PRIVATE);
        mEditorPrefsGlobal = mPrefsGlobal.edit();
    }

    public static PreferenceHelper getPreferenceInstance(Context context) {
        if (mPreferenceHelper == null) {
            mPreferenceHelper = new PreferenceHelper(context.getApplicationContext());
        }
        return mPreferenceHelper;
    }


    public void setBleBuzzerStamp(String bleAddress, String timeValue) {
        mEditorPrefsGlobal.putString(bleAddress, timeValue);
        mEditorPrefsGlobal.commit();
    }

    public String getBleBuzzerStamp(String bleaddress) {
        return mPrefsGlobal.getString(bleaddress, "");
    }

    public void setConfigutation_UART(String bleAddress_UART, String value) {
        mEditorPrefsGlobal.putString(bleAddress_UART, value);
        mEditorPrefsGlobal.commit();
    }

    public String getConfiguration_UART(String bleaddress_UART) {
        return mPrefsGlobal.getString(bleaddress_UART, "");
    }


    public void setConfigutation_BANDCONFIG(String bleAddress_bandConfig, String value) {
        mEditorPrefsGlobal.putString(bleAddress_bandConfig, value);
        mEditorPrefsGlobal.commit();
    }

    public String getConfiguration_BANDCONFIG(String bleaddress_bandConfigValue) {
        return mPrefsGlobal.getString(bleaddress_bandConfigValue, "");
    }


    public String get_PREF_remember_me_userName() {
        return mPrefsGlobal.getString(PREF_remember_me_username, "");
    }

    public void set_PREF_remember_me_userName(String loc_rememebr_me_username) {
        mEditorPrefsGlobal.putString(PREF_remember_me_username, loc_rememebr_me_username);
        mEditorPrefsGlobal.commit();
    }


    public String get_PREF_remember_password() {
        return mPrefsGlobal.getString(PREF_remember_me_password, "");
    }

    public void set_PREF_remember_me_password(String loc_rememebr_me_password) {
        mEditorPrefsGlobal.putString(PREF_remember_me_password, loc_rememebr_me_password);
        mEditorPrefsGlobal.commit();
    }

    public boolean get_Remember_me_Checked() {
        return mPrefsGlobal.getBoolean(PREF_remember_me, false);
    }

    public void set_Remember_me_Checked(boolean remember_me_checked) {
        mEditorPrefsGlobal.putBoolean(PREF_remember_me, remember_me_checked);
        mEditorPrefsGlobal.commit();
    }

    public String get_userName() {
        return mPrefsGlobal.getString(PREF_USERNAME, "");
    }

    public void set_userName(String loc_rememebr_me_username) {
        mEditorPrefsGlobal.putString(PREF_USERNAME, loc_rememebr_me_username);
        mEditorPrefsGlobal.commit();
    }


    public String get_password() {
        return mPrefsGlobal.getString(PREF_PASSWORD, "");
    }

    public void set_password(String loc_rememebr_me_username) {
        mEditorPrefsGlobal.putString(PREF_PASSWORD, loc_rememebr_me_username);
        mEditorPrefsGlobal.commit();
    }

    public void setSimSelected(String bleAddress_ESIM_NANOSIM,boolean selected){
        mEditorPrefsGlobal.putBoolean(bleAddress_ESIM_NANOSIM, selected);
        mEditorPrefsGlobal.commit();
    }

    public boolean getSimSelected(String bleAddress_ESIM_NANOSIM){
        return mPrefsGlobal.getBoolean(bleAddress_ESIM_NANOSIM,false);
    }

    public void resetPreferenceData() {
        mEditorPrefsGlobal.clear();
        mEditorPrefsGlobal.commit();
    }
}
