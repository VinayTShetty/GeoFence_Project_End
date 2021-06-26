package com.succorfish.depthntemp.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.succorfish.depthntemp.vo.VoHeatMap;

import java.lang.reflect.Type;
import java.util.ArrayList;

/*Preference Helper*/
public class PreferenceHelper {

    private static SharedPreferences mPrefs;
    private static PreferenceHelper mPreferenceHelper;
    private static SharedPreferences.Editor mEditorPrefs;

    public PreferenceHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mEditorPrefs = mPrefs.edit();
    }

    public static PreferenceHelper getPreferenceInstance(Context context) {
        if (mPreferenceHelper == null) {
            mPreferenceHelper = new PreferenceHelper(context.getApplicationContext());
        }
        return mPreferenceHelper;
    }

    private String PREF_DeviceToken = "DeviceToken";
    private String PREF_AccessToken = "AccessToken";
    private String PREF_UserId = "UserId";
    private String PREF_IS_FIRST_TIME = "is_first_time_install";

    private String PREF_SelectedDateFormat = "Selected_date_format";
    private String PREF_SelectedTemperatureType = "Selected_temperature_type";
    private String PREF_SelectedTimeUFC = "Selected_time_ufc";
    private String PREF_AutoSync = "is_auto_sync";

    private String PREF_HEAT_MAP_DATA = "heat_map_data";


//    public String getAccessToken() {
//        String str = mPrefs.getString(PREF_AccessToken, "");
//        return str;
//    }
//
//    public void setAccessToken(String pREF_AppToken) {
//        mEditorPrefs.putString(PREF_AccessToken, pREF_AppToken);
//        mEditorPrefs.commit();
//    }
//
//    public String getDeviceToken() {
//        String str = mPrefs.getString(PREF_DeviceToken, "");
//        return str;
//    }
//
//    public void setDeviceToken(String DeviceToken) {
//        mEditorPrefs.putString(PREF_DeviceToken, DeviceToken);
//        mEditorPrefs.commit();
//    }

    public void setSelectedDateFormat(String dateFormat) {
        mEditorPrefs.putString(PREF_SelectedDateFormat, dateFormat).commit();
    }

    public String getSelectedDateFormat() {
        return mPrefs.getString(PREF_SelectedDateFormat, "yyyy-MM-dd");
    }


    public int getSelectedTimeUFC() {
        return mPrefs.getInt(PREF_SelectedTimeUFC, 2);
    }

    public void setSelectedTimeUFC(int ufcTime) {
        mEditorPrefs.putInt(PREF_SelectedTimeUFC, ufcTime).commit();
    }


    public int getSelectedTemperatureType() {
        return mPrefs.getInt(PREF_SelectedTemperatureType, 0);
    }

    public void setSelectedTemperatureType(int temperatureType) {
        mEditorPrefs.putInt(PREF_SelectedTemperatureType, temperatureType).commit();
    }

    public boolean getIsAutoSync() {
        return mPrefs.getBoolean(PREF_AutoSync, true);
    }

    public void setIsAutoSync(boolean autoSync) {
        mEditorPrefs.putBoolean(PREF_AutoSync, autoSync).commit();
    }

    public boolean getFirstTimeInstall() {
        return mPrefs.getBoolean(PREF_IS_FIRST_TIME, true);
    }

    public void setFirstTimeInstall(boolean autoSync) {
        mEditorPrefs.putBoolean(PREF_IS_FIRST_TIME, autoSync).commit();
    }

    public ArrayList<VoHeatMap> getAllHeatMapSettingData() {
        Gson gson = new Gson();
        String str = mPrefs.getString(PREF_HEAT_MAP_DATA, null);
        Type type = new TypeToken<ArrayList<VoHeatMap>>() {
        }.getType();
        if (str != null) {
            return gson.fromJson(str, type);
        } else {
            return new ArrayList<>();
        }
    }

    public void setAllHeatMapSettingData(ArrayList<VoHeatMap> listHeatMapSetting) {
        Gson gson = new Gson();
        String json = gson.toJson(listHeatMapSetting);
        mEditorPrefs.putString(PREF_HEAT_MAP_DATA, json);
        mEditorPrefs.commit();
        mEditorPrefs.apply();
    }

    public VoHeatMap getHeatMapSettingData(int position) {
        return getAllHeatMapSettingData().get(position);
    }

    public void setHeatMapSettingData(VoHeatMap mVoHeatMapSettings, int position) {
        ArrayList<VoHeatMap> mHeatMapList = getAllHeatMapSettingData();
        mHeatMapList.set(position, mVoHeatMapSettings);
        setAllHeatMapSettingData(mHeatMapList);
    }

    public void ClearAllData() {
        mEditorPrefs.clear();
        mEditorPrefs.commit();
    }

    public void ResetPrefData() {
        mEditorPrefs.putString(PREF_AccessToken, "");
        mEditorPrefs.putString(PREF_UserId, "");
        mEditorPrefs.putString(PREF_SelectedDateFormat, "yyyy-MM-dd");
        mEditorPrefs.putInt(PREF_SelectedTimeUFC, 2);
        mEditorPrefs.putInt(PREF_SelectedTemperatureType, 0);
        mEditorPrefs.putBoolean(PREF_AutoSync, true);
        mEditorPrefs.commit();
    }


}
