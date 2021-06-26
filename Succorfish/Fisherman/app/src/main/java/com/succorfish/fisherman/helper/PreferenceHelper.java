package com.succorfish.fisherman.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PreferenceHelper {

    private final SharedPreferences mPrefs;
    private final SharedPreferences mPrefsGlobal;

    public PreferenceHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefsGlobal = context.getSharedPreferences("PrefFishermanGlobal", Context.MODE_PRIVATE);
    }

    private String PREF_DeviceToken = "DeviceToken";
    private String PREF_AccessToken = "AccessToken";
    private String PREF_UserId = "UserId";
    private String PREF_UserName = "UserName";
    private String PREF_FirstName = "fName";
    private String PREF_LastName = "lName";
    private String PREF_AccountId = "accountID";
    private String PREF_UserEmail = "UserEmail";
    private String PREF_RoleId = "UserRoleId";
    private String PREF_RoleName = "UserRoleName";
    private String PREF_UserCountry = "UserCountry";
    private String PREF_UserPassword = "UserPassword";

    private String PREF_InstallationCount = "InstallationCount";
    private String PREF_SelectedDateFormat = "Selected_dateformat";

    private String PREF_InstallUpId = "InstallUpId";
    private String PREF_InstallDownId = "InstallDownId";
    private String PREF_UnInstallUpId = "UnInstallUpId";
    private String PREF_UnInstallDownId = "UnInstallDownId";
    private String PREF_InspectionUpId = "InspectionUpId";
    private String PREF_InspectionDownId = "InspectionDownId";

    private String PREF_USERNAME = "uNAME";
    private String PREF_PASSWORD = "uPassword";

    // Remember Password
    private String PREF_RememberMe = "RememberMe";
    private String PREF_RememberMe_Email = "RememberMe_Email";
    private String PREF_RememberMe_Password = "RememberMe_Password";


    public String getAccessToken() {
        String str = mPrefs.getString(PREF_AccessToken, "");
        return str;
    }

    public void setAccessToken(String pREF_AppToken) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_AccessToken, pREF_AppToken);
        mEditor.commit();
    }

    public String getDeviceToken() {
        String str = mPrefs.getString(PREF_DeviceToken, "");
        return str;
    }

    public void setDeviceToken(String DeviceToken) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_DeviceToken, DeviceToken);
        mEditor.commit();
    }

    public boolean getIsRememberMe() {
        return mPrefsGlobal.getBoolean(PREF_RememberMe, false);
    }

    public void setIsRememberMe(boolean isRememberMe) {
        Editor mEditor = mPrefsGlobal.edit();
        mEditor.putBoolean(PREF_RememberMe, isRememberMe);
        mEditor.commit();
    }

    public String getRememberMeUsername() {
        String str = mPrefsGlobal.getString(PREF_RememberMe_Email, "");
        return str;
    }

    public void setRememberMeUsername(String remmberMeUsername) {
        Editor mEditor = mPrefsGlobal.edit();
        mEditor.putString(PREF_RememberMe_Email, remmberMeUsername);
        mEditor.commit();
    }

    public String getRememberMePassword() {
        String str = mPrefsGlobal.getString(PREF_RememberMe_Password, "");
        return str;
    }

    public void setRememberMepassword(String remmberMePassword) {
        Editor mEditor = mPrefsGlobal.edit();
        mEditor.putString(PREF_RememberMe_Password, remmberMePassword);
        mEditor.commit();
    }

    public String getUserId() {
        String str = mPrefs.getString(PREF_UserId, "");
        return str;
    }

    public void setUserId(String pREF_AppToken) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_UserId, pREF_AppToken);
        mEditor.commit();
    }


    public String getUserName() {
        String str = mPrefs.getString(PREF_UserName, "");
        return str;
    }

    public void setUserName(String _UserFirstName) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_UserName, _UserFirstName);
        mEditor.commit();
    }

    public String getUserFirstName() {
        String str = mPrefs.getString(PREF_FirstName, "");
        return str;
    }

    public void setUserFirstName(String _UserFirstName) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_FirstName, _UserFirstName);
        mEditor.commit();
    }

    public String getUserLastName() {
        String str = mPrefs.getString(PREF_LastName, "");
        return str;
    }

    public void setUserLastName(String _UserAddress) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_LastName, _UserAddress);
        mEditor.commit();
    }

    public void setUserEmail(String pREF_UserEmail) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_UserEmail, pREF_UserEmail);
        mEditor.commit();
    }


    public String getUser_email() {
        String str = mPrefs.getString(PREF_UserEmail, "");
        return str;
    }

    public void setUserPassword(String pREF_UserPassword) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_UserPassword, pREF_UserPassword);
        mEditor.commit();
    }


    public String getUserPassword() {
        String str = mPrefs.getString(PREF_UserPassword, "");
        return str;
    }


    public String getAccountId() {
        String str = mPrefs.getString(PREF_AccountId, "");
        return str;
    }

    public void setAccountId(String pref_accountId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_AccountId, pref_accountId);
        mEditor.commit();
    }

    public String getUserCountry() {
        String str = mPrefs.getString(PREF_UserCountry, "");
        return str;
    }

    public void setUserContry(String pref_Country) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_UserCountry, pref_Country);
        mEditor.commit();
    }

    public void setInstallUpId(String installUpId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_InstallUpId, installUpId);
        mEditor.commit();
    }


    public String getInstallUpID() {
        String str = mPrefs.getString(PREF_InstallUpId, "0");
        return str;
    }

    public void setInstallDownId(String installUpId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_InstallDownId, installUpId);
        mEditor.commit();
    }


    public String getInstallDownID() {
        String str = mPrefs.getString(PREF_InstallDownId, "0");
        return str;
    }

    public void setUnInstallUpId(String uninstallUpId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_UnInstallUpId, uninstallUpId);
        mEditor.commit();
    }


    public String getUnInstallUpID() {
        String str = mPrefs.getString(PREF_UnInstallUpId, "0");
        return str;
    }

    public void setUnInstallDownId(String uninstallUpId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_UnInstallDownId, uninstallUpId);
        mEditor.commit();
    }


    public String getUnInstallDownID() {
        String str = mPrefs.getString(PREF_UnInstallDownId, "0");
        return str;
    }

    public void setInspectionUpId(String uninstallUpId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_InspectionUpId, uninstallUpId);
        mEditor.commit();
    }


    public String getInspectionUpID() {
        String str = mPrefs.getString(PREF_InspectionUpId, "0");
        return str;
    }

    public void setInspectionDownId(String InspectionDownId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_InspectionDownId, InspectionDownId);
        mEditor.commit();
    }


    public String getInspectionDownID() {
        String str = mPrefs.getString(PREF_InspectionDownId, "0");
        return str;
    }

    public void setInstallationCount(String installationCount) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_InstallationCount, installationCount);
        mEditor.commit();
    }


    public String getInstallationCount() {
        String str = mPrefs.getString(PREF_InstallationCount, "0");
        return str;
    }

    public void setSelectedDateFormat(String dateFormat) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_SelectedDateFormat, dateFormat);
        mEditor.commit();
    }


    public String getSelectedDateFormat() {
        String str = mPrefs.getString(PREF_SelectedDateFormat, "YYYY-MM-dd");
        return str;
    }

    public String getRoleId() {
        String str = mPrefs.getString(PREF_RoleId, "");
        return str;
    }

    public void setRoleId(String roleId) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_RoleId, roleId);
        mEditor.commit();
    }

    public String getRoleName() {
        String str = mPrefs.getString(PREF_RoleName, "");
        return str;
    }

    public void setRoleName(String roleName) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_RoleName, roleName);
        mEditor.commit();
    }

    public String getUName() {
        String str = mPrefs.getString(PREF_USERNAME, "");
        return str;
    }

    public void setUName(String uName) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_USERNAME, uName);
        mEditor.commit();
    }

    public String getUPassword() {
        String str = mPrefs.getString(PREF_PASSWORD, "");
        return str;
    }

    public void setUPassword(String pw) {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_PASSWORD, pw);
        mEditor.commit();
    }

    public void ClearAllData() {
        Editor mEditor = mPrefs.edit();
        mEditor.clear();
        mEditor.commit();
    }

    public void ResetPrefData() {
        Editor mEditor = mPrefs.edit();
        mEditor.putString(PREF_AccessToken, "");
        mEditor.putString(PREF_UserId, "");
        mEditor.putString(PREF_UserName, "");
        mEditor.putString(PREF_FirstName, "");
        mEditor.putString(PREF_UserEmail, "");
        mEditor.putString(PREF_LastName, "");
        mEditor.putString(PREF_AccountId, "");
        mEditor.putString(PREF_UserCountry, "");
        mEditor.putString(PREF_UserPassword, "");
        mEditor.putString(PREF_InstallUpId, "0");
        mEditor.putString(PREF_InstallDownId, "0");
        mEditor.putString(PREF_UnInstallUpId, "0");
        mEditor.putString(PREF_UnInstallDownId, "0");
        mEditor.putString(PREF_InspectionUpId, "0");
        mEditor.putString(PREF_InspectionDownId, "0");
        mEditor.putString(PREF_InstallationCount, "0");
        mEditor.putString(PREF_SelectedDateFormat, "YYYY-MM-dd");
        mEditor.putString(PREF_RoleId, "");
        mEditor.putString(PREF_RoleName, "");
        mEditor.putString(PREF_USERNAME, "");
        mEditor.putString(PREF_PASSWORD, "");
        mEditor.commit();
    }


}
