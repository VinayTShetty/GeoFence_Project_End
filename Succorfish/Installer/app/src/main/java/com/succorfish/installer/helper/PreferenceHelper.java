package com.succorfish.installer.helper;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/*Local preference for store value*/
public class PreferenceHelper {

    private static SharedPreferences mPrefs;
    private static SharedPreferences mPrefsGlobal;
    private static PreferenceHelper mPreferenceHelper;
    private static SharedPreferences.Editor mEditorPrefs;
    private static SharedPreferences.Editor mEditorPrefsGlobal;

    private PreferenceHelper(Context context) {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        mPrefsGlobal = context.getSharedPreferences("PrefInstallerGlobal", Context.MODE_PRIVATE);
        mEditorPrefs = mPrefs.edit();
        mEditorPrefsGlobal = mPrefsGlobal.edit();
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
        return mPrefs.getString(PREF_AccessToken, "");
    }

    public void setAccessToken(String pREF_AppToken) {
        mEditorPrefs.putString(PREF_AccessToken, pREF_AppToken);
        mEditorPrefs.commit();
    }

    public String getDeviceToken() {
        return mPrefs.getString(PREF_DeviceToken, "");
    }

    public void setDeviceToken(String DeviceToken) {
        mEditorPrefs.putString(PREF_DeviceToken, DeviceToken);
        mEditorPrefs.commit();
    }

    public boolean getIsRememberMe() {
        return mPrefsGlobal.getBoolean(PREF_RememberMe, false);
    }

    public void setIsRememberMe(boolean isRememberMe) {
        mEditorPrefsGlobal.putBoolean(PREF_RememberMe, isRememberMe);
        mEditorPrefsGlobal.commit();
    }

    public String getRememberMeUsername() {
        return mPrefsGlobal.getString(PREF_RememberMe_Email, "");
    }

    public void setRememberMeUsername(String remmberMeUsername) {
        mEditorPrefsGlobal.putString(PREF_RememberMe_Email, remmberMeUsername);
        mEditorPrefsGlobal.commit();
    }

    public String getRememberMePassword() {
        return mPrefsGlobal.getString(PREF_RememberMe_Password, "");
    }

    public void setRememberMepassword(String remmberMePassword) {
        mEditorPrefsGlobal.putString(PREF_RememberMe_Password, remmberMePassword);
        mEditorPrefsGlobal.commit();
    }

    public String getUserId() {
        return mPrefs.getString(PREF_UserId, "");
    }

    public void setUserId(String pREF_AppToken) {
        mEditorPrefs.putString(PREF_UserId, pREF_AppToken);
        mEditorPrefs.commit();
    }


    public String getUserName() {
        return mPrefs.getString(PREF_UserName, "");
    }

    public void setUserName(String _UserFirstName) {
        mEditorPrefs.putString(PREF_UserName, _UserFirstName);
        mEditorPrefs.commit();
    }

    public String getUserFirstName() {
        return mPrefs.getString(PREF_FirstName, "");
    }

    public void setUserFirstName(String _UserFirstName) {
        mEditorPrefs.putString(PREF_FirstName, _UserFirstName);
        mEditorPrefs.commit();
    }

    public String getUserLastName() {
        return mPrefs.getString(PREF_LastName, "");
    }

    public void setUserLastName(String _UserAddress) {
        mEditorPrefs.putString(PREF_LastName, _UserAddress);
        mEditorPrefs.commit();
    }

    public void setUserEmail(String pREF_UserEmail) {
        mEditorPrefs.putString(PREF_UserEmail, pREF_UserEmail);
        mEditorPrefs.commit();
    }


    public String getUser_email() {
        return mPrefs.getString(PREF_UserEmail, "");
    }

    public void setUserPassword(String pREF_UserPassword) {
        mEditorPrefs.putString(PREF_UserPassword, pREF_UserPassword);
        mEditorPrefs.commit();
    }


    public String getUserPassword() {
        return mPrefs.getString(PREF_UserPassword, "");
    }


    public String getAccountId() {
        return mPrefs.getString(PREF_AccountId, "");
    }

    public void setAccountId(String pref_accountId) {
        mEditorPrefs.putString(PREF_AccountId, pref_accountId);
        mEditorPrefs.commit();
    }

    public String getUserCountry() {
        return mPrefs.getString(PREF_UserCountry, "");
    }

    public void setUserContry(String pref_Country) {
        mEditorPrefs.putString(PREF_UserCountry, pref_Country);
        mEditorPrefs.commit();
    }

    public void setInstallUpId(String installUpId) {
        mEditorPrefs.putString(PREF_InstallUpId, installUpId);
        mEditorPrefs.commit();
    }


    public String getInstallUpID() {
        return mPrefs.getString(PREF_InstallUpId, "0");
    }

    public void setInstallDownId(String installUpId) {
        mEditorPrefs.putString(PREF_InstallDownId, installUpId);
        mEditorPrefs.commit();
    }


    public String getInstallDownID() {
        return mPrefs.getString(PREF_InstallDownId, "0");
    }

    public void setUnInstallUpId(String uninstallUpId) {
        mEditorPrefs.putString(PREF_UnInstallUpId, uninstallUpId);
        mEditorPrefs.commit();
    }


    public String getUnInstallUpID() {
        return mPrefs.getString(PREF_UnInstallUpId, "0");
    }

    public void setUnInstallDownId(String uninstallUpId) {
        mEditorPrefs.putString(PREF_UnInstallDownId, uninstallUpId);
        mEditorPrefs.commit();
    }


    public String getUnInstallDownID() {
        return mPrefs.getString(PREF_UnInstallDownId, "0");
    }

    public void setInspectionUpId(String uninstallUpId) {
        mEditorPrefs.putString(PREF_InspectionUpId, uninstallUpId);
        mEditorPrefs.commit();
    }


    public String getInspectionUpID() {
        return mPrefs.getString(PREF_InspectionUpId, "0");
    }

    public void setInspectionDownId(String InspectionDownId) {
        mEditorPrefs.putString(PREF_InspectionDownId, InspectionDownId);
        mEditorPrefs.commit();
    }


    public String getInspectionDownID() {
        return mPrefs.getString(PREF_InspectionDownId, "0");
    }

    public void setInstallationCount(String installationCount) {
        mEditorPrefs.putString(PREF_InstallationCount, installationCount);
        mEditorPrefs.commit();
    }


    public String getInstallationCount() {
        return mPrefs.getString(PREF_InstallationCount, "0");
    }

    public void setSelectedDateFormat(String dateFormat) {
        mEditorPrefs.putString(PREF_SelectedDateFormat, dateFormat);
        mEditorPrefs.commit();
    }


    public String getSelectedDateFormat() {
        return mPrefs.getString(PREF_SelectedDateFormat, "yyyy-MM-dd");
    }

    public String getRoleId() {
        return mPrefs.getString(PREF_RoleId, "");
    }

    public void setRoleId(String roleId) {
        mEditorPrefs.putString(PREF_RoleId, roleId);
        mEditorPrefs.commit();
    }

    public String getRoleName() {
        return mPrefs.getString(PREF_RoleName, "");
    }

    public void setRoleName(String roleName) {
        mEditorPrefs.putString(PREF_RoleName, roleName);
        mEditorPrefs.commit();
    }

    public String getUName() {
        return mPrefs.getString(PREF_USERNAME, "");
    }

    public void setUName(String uName) {
        mEditorPrefs.putString(PREF_USERNAME, uName);
        mEditorPrefs.commit();
    }

    public String getUPassword() {
        return mPrefs.getString(PREF_PASSWORD, "");
    }

    public void setUPassword(String pw) {
        mEditorPrefs.putString(PREF_PASSWORD, pw);
        mEditorPrefs.commit();
    }

    public void ClearAllData() {
        mEditorPrefs.clear();
        mEditorPrefs.commit();
    }

    /*Clear all preference */
    public void ResetPrefData() {
        mEditorPrefs.putString(PREF_AccessToken, "");
        mEditorPrefs.putString(PREF_UserId, "");
        mEditorPrefs.putString(PREF_UserName, "");
        mEditorPrefs.putString(PREF_FirstName, "");
        mEditorPrefs.putString(PREF_UserEmail, "");
        mEditorPrefs.putString(PREF_LastName, "");
        mEditorPrefs.putString(PREF_AccountId, "");
        mEditorPrefs.putString(PREF_UserCountry, "");
        mEditorPrefs.putString(PREF_UserPassword, "");
        mEditorPrefs.putString(PREF_InstallUpId, "0");
        mEditorPrefs.putString(PREF_InstallDownId, "0");
        mEditorPrefs.putString(PREF_UnInstallUpId, "0");
        mEditorPrefs.putString(PREF_UnInstallDownId, "0");
        mEditorPrefs.putString(PREF_InspectionUpId, "0");
        mEditorPrefs.putString(PREF_InspectionDownId, "0");
        mEditorPrefs.putString(PREF_InstallationCount, "0");
        mEditorPrefs.putString(PREF_SelectedDateFormat, "yyyy-MM-dd");
        mEditorPrefs.putString(PREF_RoleId, "");
        mEditorPrefs.putString(PREF_RoleName, "");
        mEditorPrefs.putString(PREF_USERNAME, "");
        mEditorPrefs.putString(PREF_PASSWORD, "");
        mEditorPrefs.commit();
    }


}
