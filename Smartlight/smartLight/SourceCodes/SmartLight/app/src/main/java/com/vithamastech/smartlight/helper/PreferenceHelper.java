package com.vithamastech.smartlight.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    private final SharedPreferences mPrefs;
    private final SharedPreferences mPrefsGlobal;
    private static SharedPreferences.Editor mEditorPrefs;
    private static SharedPreferences.Editor mEditorPrefsGlobal;

    public PreferenceHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefsGlobal = context.getSharedPreferences("PreferencecSmartLightGlobal", Context.MODE_PRIVATE);
        mEditorPrefs = mPrefs.edit();
        mEditorPrefsGlobal = mPrefsGlobal.edit();
    }


    private String PREF_DeviceToken = "DeviceToken";
    private String PREF_IS_FIRST_TIME = "is_first_time";
    private String PREF_IS_SHOW_INTRO = "is_show_intro";
    private String PREF_DEVICE_SEQUENCE_NO = "device_seq_no";

    private String PREF_UserId = "UserId";
    private String PREF_UserFirstName = "UserFirstName";
    private String PREF_AccountName = "AccountName";
    private String PREF_UserEmail = "UserEmail";
    private String PREF_UserPhoneNo = "UserPhoneNo";
    private String PREF_UserPassword = "UserPassword";
    private String PREF_UserIsRegistered = "UserIsRegistered";
    private String PREF_AccessToken = "AccessToken";
    private String PREF_IsSkipUser = "Is_Skip_User";
    private String PREF_IsAllDeviceOn = "IsAllDeviceOn";
    private String PREF_SecretKey = "secret_key";
    private String PREF_IS_SYNC_DEVICE = "is_device_sync";
    private String PREF_IS_SYNC_GROUP = "is_group_sync";
    private String PREF_SelfDeviceId = "SelfDeviceId";

    private String PREF_SELECTED_BACKGROUND = "AppBackground";
    private String PREF_SELECTED_LANGUAGE = "Selected_Voice_Language";
    private String PREF_WHEEL_BACKGROUND = "WheelBackground";
    private String PREF_WHITE_BACKGROUND = "WhiteBackground";

    private String PREF_LIGHT_LAST_STATE = "all_light_last_state";
    // Remember Password
    private String PREF_G_RememberMe = "RememberMe";
    private String PREF_G_RememberMe_Username = "RememberMe_Username";
    private String PREF_G_RememberMe_Password = "RememberMe_Password";


    public boolean getIsRememberMe() {
        return mPrefsGlobal.getBoolean(PREF_G_RememberMe, false);
    }

    public void setIsRememberMe(boolean isRememberMe) {
        mEditorPrefsGlobal.putBoolean(PREF_G_RememberMe, isRememberMe);
        mEditorPrefsGlobal.commit();
    }

    public String getRememberMeUsername() {
        return mPrefsGlobal.getString(PREF_G_RememberMe_Username, "");
    }

    public void setRememberMeUsername(String rememberMeUsername) {
        mEditorPrefsGlobal.putString(PREF_G_RememberMe_Username, rememberMeUsername);
        mEditorPrefsGlobal.commit();
    }

    public String getRememberMePassword() {
        return mPrefsGlobal.getString(PREF_G_RememberMe_Password, "");
    }

    public void setRememberMepassword(String rememberMePassword) {
        mEditorPrefsGlobal.putString(PREF_G_RememberMe_Password, rememberMePassword);
        mEditorPrefsGlobal.commit();
    }

    public String getDeviceToken() {
        return mPrefs.getString(PREF_DeviceToken, "");
    }

    public void setDeviceToken(String DeviceToken) {
        mEditorPrefs.putString(PREF_DeviceToken, DeviceToken);
        mEditorPrefs.commit();
    }

    public String getAccessToken() {
        return mPrefs.getString(PREF_AccessToken, "");
    }

    public void setAccessToken(String pREF_AppToken) {
        mEditorPrefs.putString(PREF_AccessToken, pREF_AppToken);
        mEditorPrefs.commit();
    }

    public String getUserId() {
        return mPrefs.getString(PREF_UserId, "");
    }

    public void setUserId(String pREF_AppToken) {
        mEditorPrefs.putString(PREF_UserId, pREF_AppToken);
        mEditorPrefs.commit();
    }

    public String getUserFirstName() {
        return mPrefs.getString(PREF_UserFirstName, "");
    }

    public void setUserFirstName(String _UserFirstName) {
        mEditorPrefs.putString(PREF_UserFirstName, _UserFirstName);
        mEditorPrefs.commit();
    }

    public void setUserEmail(String pREF_UserEmail) {
        mEditorPrefs.putString(PREF_UserEmail, pREF_UserEmail);
        mEditorPrefs.commit();
    }


    public String getUser_email() {
        return mPrefs.getString(PREF_UserEmail, "");
    }

    public String getUserContactNo() {
        return mPrefs.getString(PREF_UserPhoneNo, "");
    }

    public void setUserContactNo(String pref_UserContactNo) {
        mEditorPrefs.putString(PREF_UserPhoneNo, pref_UserContactNo);
        mEditorPrefs.commit();
    }

    public String getUserIsActive() {
        return mPrefs.getString(PREF_UserIsRegistered, "");
    }

    public void setUserIsActive(String pref_UserIsActive) {
        mEditorPrefs.putString(PREF_UserIsRegistered, pref_UserIsActive);
        mEditorPrefs.commit();
    }

    public String getUserPassword() {
        return mPrefs.getString(PREF_UserPassword, "");
    }

    public void setUserPassword(String pref_UserPassword) {
        mEditorPrefs.putString(PREF_UserPassword, pref_UserPassword);
        mEditorPrefs.commit();
    }

    public String getAppBackground() {
        return mPrefs.getString(PREF_SELECTED_BACKGROUND, "1");
    }

    public void setAppBackground(String bg) {
        mEditorPrefs.putString(PREF_SELECTED_BACKGROUND, bg);
        mEditorPrefs.commit();
    }

    public String getWhiteBackground() {
        return mPrefs.getString(PREF_WHITE_BACKGROUND, "1");
    }

    public void setWhiteBackground(String white_bg) {
        mEditorPrefs.putString(PREF_WHITE_BACKGROUND, white_bg);
        mEditorPrefs.commit();
    }

    public String getWheelBackground() {
        return mPrefs.getString(PREF_WHEEL_BACKGROUND, "1");
    }

    public void setWheelBackground(String wheel_bg) {
        mEditorPrefs.putString(PREF_WHEEL_BACKGROUND, wheel_bg);
        mEditorPrefs.commit();
    }

    public boolean getIsSkipUser() {
        return mPrefs.getBoolean(PREF_IsSkipUser, false);
    }

    public void setIsSkipUser(boolean isSkip) {
        mEditorPrefs.putBoolean(PREF_IsSkipUser, isSkip);
        mEditorPrefs.commit();
    }

    public boolean getIsDeviceSync() {
        return mPrefs.getBoolean(PREF_IS_SYNC_DEVICE, false);
    }

    public void setIsDeviceSync(boolean isSync) {
        mEditorPrefs.putBoolean(PREF_IS_SYNC_DEVICE, isSync);
        mEditorPrefs.commit();
    }

    public boolean getIsGroupSync() {
        return mPrefs.getBoolean(PREF_IS_SYNC_GROUP, false);
    }

    public void setIsGroupSync(boolean isSync) {
        mEditorPrefs.putBoolean(PREF_IS_SYNC_GROUP, isSync);
        mEditorPrefs.commit();
    }

    public boolean getIsFirstTime() {
        return mPrefs.getBoolean(PREF_IS_FIRST_TIME, false);
    }

    public void setFirstTime(boolean isSync) {
        mEditorPrefs.putBoolean(PREF_IS_FIRST_TIME, isSync);
        mEditorPrefs.commit();
    }

    public boolean getIsShowIntro() {
        return mPrefs.getBoolean(PREF_IS_SHOW_INTRO, true);
    }

    public void setIsShowIntro(boolean isSync) {
        mEditorPrefs.putBoolean(PREF_IS_SHOW_INTRO, isSync);
        mEditorPrefs.commit();
    }

    public boolean getIsAllDeviceOn() {
        return mPrefs.getBoolean(PREF_IsAllDeviceOn, false);
    }

    public void setIsAllDeviceOn(boolean isRememberMe) {
        mEditorPrefs.putBoolean(PREF_IsAllDeviceOn, isRememberMe);
        mEditorPrefs.commit();
    }

    public int getSelectedLanguage() {
        return mPrefs.getInt(PREF_SELECTED_LANGUAGE, 0);
    }

    public void setSelectedLanguage(int languagePosition) {
        mEditorPrefs.putInt(PREF_SELECTED_LANGUAGE, languagePosition);
        mEditorPrefs.commit();
    }

    public int getDeviceSequenceNo() {
        return mPrefs.getInt(PREF_DEVICE_SEQUENCE_NO, 0);
    }

    public void setDeviceSequenceNo(int seq_no) {
        mEditorPrefs.putInt(PREF_DEVICE_SEQUENCE_NO, seq_no);
        mEditorPrefs.commit();
    }

    public String getSecretKey() {
        return mPrefs.getString(PREF_SecretKey, "");
    }

    public void setSecretKey(String secretKey) {
        mEditorPrefs.putString(PREF_SecretKey, secretKey);
        mEditorPrefs.commit();
    }

    public String getAccountName() {
        return mPrefs.getString(PREF_AccountName, "");
    }

    public void setAccountName(String accountName) {
        mEditorPrefs.putString(PREF_AccountName, accountName);
        mEditorPrefs.commit();
    }


    public int getLightLastState() {
        return mPrefs.getInt(PREF_LIGHT_LAST_STATE, 0);
    }

    public void setLightLastState(int lastState) {
        mEditorPrefs.putInt(PREF_LIGHT_LAST_STATE, lastState);
        mEditorPrefs.commit();
    }

    public String getSelfDeviceId() {
        return mPrefs.getString(PREF_SelfDeviceId, "1000");
    }

    public void setSelfDeviceId(String deviceId) {
        mEditorPrefs.putString(PREF_SelfDeviceId, deviceId);
        mEditorPrefs.commit();
    }


    public void ClearAllData() {
        mEditorPrefs.clear();
        mEditorPrefs.commit();
    }

    public void ResetPrefData() {
        mEditorPrefs.putString(PREF_UserId, "");
        mEditorPrefs.putString(PREF_UserFirstName, "");
        mEditorPrefs.putString(PREF_AccountName, "");
        mEditorPrefs.putString(PREF_UserEmail, "");
        mEditorPrefs.putString(PREF_UserPhoneNo, "");
        mEditorPrefs.putString(PREF_UserPassword, "");
        mEditorPrefs.putString(PREF_UserIsRegistered, "");
        mEditorPrefs.putString(PREF_AccessToken, "");
        mEditorPrefs.putString(PREF_SecretKey, "");
        mEditorPrefs.putString(PREF_WHITE_BACKGROUND, "1");
        mEditorPrefs.putString(PREF_WHEEL_BACKGROUND, "1");
        mEditorPrefs.putInt(PREF_LIGHT_LAST_STATE, 0);
        mEditorPrefs.putBoolean(PREF_IsSkipUser, false);
        mEditorPrefs.putBoolean(PREF_IS_SYNC_DEVICE, false);
        mEditorPrefs.putBoolean(PREF_IS_SYNC_GROUP, false);
        mEditorPrefs.putBoolean(PREF_IsAllDeviceOn, false);

        mEditorPrefs.putInt(PREF_SELECTED_LANGUAGE, 0);

        mEditorPrefs.commit();

    }


}
