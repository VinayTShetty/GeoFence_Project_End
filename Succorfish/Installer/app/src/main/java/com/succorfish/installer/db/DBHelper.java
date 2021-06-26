package com.succorfish.installer.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.succorfish.installer.Vo.VoVessel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


public class DBHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/com.succorfish.installer/databases/";

    private final static String DB_NAME = "SuccorfishInstallers.sqlite";
    public final static int DB_VESRION = 4;
    private final Context myContext;

    private static SQLiteDatabase db;

    ContentValues mContentValues;

    /*Database table*/
    public static String mTableVesselAsset = "tbl_vessel_asset";
    public static String mTableInstall = "tbl_install";
    public static String mTableInstallerPhoto = "tbl_installer_photo";
    public static String mTableUnInstall = "tbl_uninstall";
    public static String mTableInspection = "tbl_inspection";
    public static String mTableInspectionPhoto = "tbl_inspection_photo";
    public static String mTableQuestionAnswer = "tbl_question_ans";

    // TABLE VESSEL ASSET
    public static String mFieldVesselLocalId = "vessel_local_id";
    public static String mFieldVesselServerId = "vessel_server_id";
    public static String mFieldVesselSuccorfishId = "vessel_succorfish_id";
    public static String mFieldVesselAccountId = "vessel_account_id";
    public static String mFieldVesselName = "vessel_name";
    public static String mFieldVesselRegNo = "vessel_regi_no";
    public static String mFieldVesselPortNo = "vessel_port_no";
    public static String mFieldVesselCreatedDate = "vessel_created_date";
    public static String mFieldVesselUpdatedDate = "vessel_update_date";
    public static String mFieldVesselIsSync = "is_sync";

    // TABLE INSTALL
    public static String mFieldInstallLocalId = "inst_local_id";
    public static String mFieldInstallServerId = "inst_server_id";
    public static String mFieldInstallUserId = "inst_user_id";
    public static String mFieldInstallDeviceIMEINo = "inst_device_iemi_no";
    public static String mFieldInstallDeviceServerId = "inst_device_server_id";
    public static String mFieldInstallDeviceLocalId = "inst_device_local_id";
    public static String mFieldInstallDeviceAccountId = "inst_device_account_id";
    public static String mFieldInstallDevicName = "inst_device_name";
    public static String mFieldInstallDeviceWarranty_status = "inst_device_warranty_status";
    public static String mFieldInstallDeviceTypeName = "inst_device_type_name";
    public static String mFieldInstallHelpNo = "inst_help_no";
    public static String mFieldInstallDateTime = "inst_date_time";
    public static String mFieldInstallLatitude = "inst_latitude";
    public static String mFieldInstallLongitude = "inst_longitude";
    public static String mFieldInstallCountryCode = "inst_country_code";
    public static String mFieldInstallCountryName = "inst_country_name";
    public static String mFieldInstallVesselLocalId = "inst_vessel_local_id";
    public static String mFieldInstallVesselServerId = "inst_vessel_server_id";
    public static String mFieldInstallVesselName = "inst_vessel_name";
    public static String mFieldInstallVesselRegNo = "inst_vessel_regi_no";
    public static String mFieldInstallPower = "inst_power";
    public static String mFieldInstallLocation = "inst_location";
    public static String mFieldInstallOwnerName = "inst_owner_name";
    public static String mFieldInstallOwnerAddress = "inst_owner_address";
    public static String mFieldInstallOwnerCity = "inst_owner_city";
    public static String mFieldInstallOwnerState = "inst_owner_state";
    public static String mFieldInstallOwnerZipcode = "inst_owner_zipcode";
    public static String mFieldInstallOwnerEmail = "inst_owner_email";
    public static String mFieldInstallOwnerMobileNo = "inst_owner_mobile_no";
    public static String mFieldInstallLocalSignUrl = "inst_local_sign_url";
    public static String mFieldInstallServerSignUrl = "inst_server_sign_url";
    public static String mFieldInstallLocalInstallerSignUrl = "inst_local_installer_sign_url";
    public static String mFieldInstallServerInstallerSignUrl = "inst_server_installer_sign_url";
    public static String mFieldInstallPdfUrl = "inst_pdf_url";
    public static String mFieldInstallCreatedDate = "inst_created_date";
    public static String mFieldInstallUpdatedDate = "inst_updated_date";
    public static String mFieldInstallIsSync = "inst_is_sync";
    public static String mFieldInstallIsSyncOwnerSign = "inst_owner_sign_sync";
    public static String mFieldInstallIsSyncInstallerSign = "inst_installer_sign_sync";

    // 0=partially Install, 1= full install
    public static String mFieldInstallStatus = "inst_install_status";
    public static String mFieldInstallDateTimeStamp = "inst_date_timestamp";

    // TABLE INSTALLER PHOTO
    public static String mFieldInstPhotoLocalID = "inst_photo_local_id";
    public static String mFieldInstPhotoServerID = "inst_photo_server_id";
    public static String mFieldInstPhotoLocalURL = "inst_photo_local_url";
    public static String mFieldInstPhotoServerURL = "inst_photo_server_url";
    public static String mFieldInstPhotoType = "inst_photo_type";
    public static String mFieldInstLocalId = "inst_local_id";
    public static String mFieldInstServerId = "inst_server_id";
    public static String mFieldInstPhotoUserId = "inst_photo_user_id";
    public static String mFieldInstPhotoCreatedDate = "inst_photo_created_date";
    public static String mFieldInstPhotoUpdateDate = "inst_photo_update_date";
    public static String mFieldInstPhotoIsSync = "is_sync";

    // TABLE UNINSTALL
    public static String mFieldUnInstallLocalId = "uninst_local_id";
    public static String mFieldUnInstallServerId = "uninst_server_id";
    public static String mFieldUnInstallUserId = "uninst_user_id";
    public static String mFieldUnInstallDeviceIMEINo = "uninst_device_type_iemi_no";
    public static String mFieldUnInstallDeviceServerId = "uninst_device_server_id";
    public static String mFieldUnInstallDeviceLocalId = "uninst_device_local_id";
    public static String mFieldUnInstallDeviceAccountId = "uninst_device_account_id";
    public static String mFieldUnInstallDeviceName = "uninst_device_name";
    public static String mFieldUnInstallDeviceWarrantStatus = "uninst_device_warranty_status";
    public static String mFieldUnInstallDeviceTypeName = "uninst_device_type_name";
    public static String mFieldUnInstallVesselLocalId = "uninst_vessel_local_id";
    public static String mFieldUnInstallVesselServerId = "uninst_vessel_server_id";
    public static String mFieldUnInstallVesselName = "uninst_vessel_name";
    public static String mFieldUnInstallVesselRegNo = "uninst_vessel_regi_no";
    public static String mFieldUnInstallOwnerName = "uninst_owner_name";
    public static String mFieldUnInstallOwnerAddress = "uninst_owner_address";
    public static String mFieldUnInstallOwnerCity = "uninst_owner_city";
    public static String mFieldUnInstallOwnerState = "uninst_owner_state";
    public static String mFieldUnInstallOwnerZipcode = "uninst_owner_zipcode";
    public static String mFieldUnInstallOwnerEmail = "uninst_owner_email";
    public static String mFieldUnInstallOwnerMobileNo = "uninst_owner_mobile_no";
    public static String mFieldUnInstallLocalSignUrl = "uninst_local_sign_url";
    public static String mFieldUnInstallServerSignUrl = "uninst_server_sign_url";
    public static String mFieldUnInstallLocalUninstallerSignUrl = "uninst_local_uninstaller_sign_url";
    public static String mFieldUnInstallServerUninstallerSignUrl = "uninst_server_uninstaller_sign_url";
    public static String mFieldUnInstallPdfUrl = "uninst_pdf_url";
    public static String mFieldUnInstallCreatedDate = "uninst_created_date";
    public static String mFieldUnInstallUpdatedDate = "uninst_updated_date";
    public static String mFieldUnInstallIsSync = "uninst_is_sync";
    // 0=partially Install, 1= full install
    public static String mFieldUnInstallStatus = "uninst_status";
    public static String mFieldUnInstallDateTime = "uninst_date_time";
    public static String mFieldUnInstallSDateTimeStamp = "uninst_date_timestamp";
    public static String mFieldUnInstallIsSyncOwnerSign = "uninst_owner_sign_sync";
    public static String mFieldUnInstallIsSyncInstallerSign = "uninst_installer_sign_sync";


    // TABLE INSPECTION
    public static String mFieldInspectionLocalId = "insp_local_id";
    public static String mFieldInspectionServerId = "insp_server_id";
    public static String mFieldInspectionUserId = "insp_user_id";
    public static String mFieldInspectionDeviceIMEINo = "insp_device_iemi_no";
    public static String mFieldInspectionDeviceServerId = "insp_device_server_id";
    public static String mFieldInspectionDeviceLocalId = "insp_device_local_id";
    public static String mFieldInspectionDeviceAccountId = "insp_device_account_id";
    public static String mFieldInspectionDevicName = "insp_device_name";
    public static String mFieldInspectionDeviceWarranty_status = "insp_device_warranty_status";
    public static String mFieldInspectionDeviceTypeName = "insp_device_type_name";
    public static String mFieldInspectionVesselLocalId = "insp_vessel_local_id";
    public static String mFieldInspectionVesselServerId = "insp_vessel_server_id";
    public static String mFieldInspectionVesselName = "insp_vessel_name";
    public static String mFieldInspectionVesselRegNo = "insp_vessel_regi_no";
    public static String mFieldInspectionOwnerName = "insp_owner_name";
    public static String mFieldInspectionOwnerAddress = "insp_owner_address";
    public static String mFieldInspectionOwnerCity = "insp_owner_city";
    public static String mFieldInspectionOwnerState = "insp_owner_state";
    public static String mFieldInspectionOwnerZipcode = "insp_owner_zipcode";
    public static String mFieldInspectionOwnerEmail = "insp_owner_email";
    public static String mFieldInspectionOwnerMobileNo = "insp_owner_mobile_no";
    public static String mFieldInspectionResult = "insp_result";
    public static String mFieldInspectionActionTaken = "insp_action_taken";
    public static String mFieldInspectionWarrentyReturn = "insp_warranty_return";
    public static String mFieldInspectionLocalSignUrl = "insp_local_sign_url";
    public static String mFieldInspectionServerSignUrl = "insp_server_sign_url";
    public static String mFieldInspectionLocalInspectorSignUrl = "insp_local_inspector_sign_url";
    public static String mFieldInspectionServerInspectorSignUrl = "insp_server_inspector_sign_url";
    public static String mFieldInspectionPdfUrl = "insp_pdf_url";
    public static String mFieldInspectionCreatedDate = "insp_created_date";
    public static String mFieldInspectionUpdatedDate = "insp_updated_date";
    public static String mFieldInspectionIsSync = "insp_is_sync";
    // 0=partially Install, 1= full install
    public static String mFieldInspectionStatus = "insp_status";
    public static String mFieldInspectionDateTime = "insp_date_time";
    public static String mFieldInspectionDateTimeStamp = "insp_date_timestamp";
    public static String mFieldInspectionIsSyncOwnerSign = "insp_owner_sign_sync";
    public static String mFieldInspectionIsSyncInstallerSign = "insp_installer_sign_sync";

    // TABLE INSPECTION PHOTO
    public static String mFieldInspcPhotoLocalID = "insp_photo_local_id";
    public static String mFieldInspcPhotoServerID = "insp_photo_server_id";
    public static String mFieldInspcPhotoLocalURL = "insp_photo_local_url";
    public static String mFieldInspcPhotoServerURL = "insp_photo_server_url";
    public static String mFieldInspcPhotoType = "insp_photo_type";
    public static String mFieldInspcLocalId = "insp_local_id";
    public static String mFieldInspcServerId = "insp_server_id";
    public static String mFieldInspcPhotoUserId = "insp_photo_user_id";
    public static String mFieldInspcPhotoCreatedDate = "insp_photo_created_date";
    public static String mFieldInspcPhotoUpdateDate = "insp_photo_update_date";
    public static String mFieldInspcPhotoIsSync = "insp_photo_is_sync";

    // TABLE QUESTION ANS
    public static String mFieldQuesAnsLocalID = "ques_ans_local_id";
    public static String mFieldQuesAnsUserID = "ques_ans_user_id";
    // 0=Install, 1= UnInstall, 2=Inspect
    public static String mFieldQuesAnsInsUninsInspType = "ques_ans_ins_unins_insp_type";
    public static String mFieldQuesAnsInsUninsInspLocalID = "ques_ans_ins_unins_insp_local_id";
    public static String mFieldQuesAnsInsUninsInspServerID = "ques_ans_ins_unins_insp_server_id";
    public static String mFieldQuesAnsText = "ques_ans_text";
    public static String mFieldQuesAnsCreatedDate = "ques_ans_created_date";
    public static String mFieldQuesAnsUpdatedDate = "ques_ans_updated_date";
    public static String mFieldQuesAnsIsSync = "is_sync";

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VESRION);
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        System.out.println("DATABASE-PATH=" + DB_PATH);
        this.myContext = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("oldVersion-" + oldVersion + " newVersion-" + newVersion);
        int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion) {
            String sql = "";
            switch (upgradeTo) {
                case 2:
                    // It's
                    // INSTALL
                    sql = "ALTER TABLE " + mTableInstall + " RENAME TO " +
                            mTableInstall + "_OLD";
                    System.out.println("RENAME INSTALL TABLE -" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableInstall + "_OLD" + " ADD COLUMN " +
                            mFieldInstallIsSyncOwnerSign + " INTEGER DEFAULT 0";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableInstall + "_OLD" + " ADD COLUMN " +
                            mFieldInstallIsSyncInstallerSign + " INTEGER DEFAULT 0";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);

                    sql = "CREATE TABLE " + mTableInstall + "(" + mFieldInstallLocalId + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," + mFieldInstallServerId + " VARCHAR ( 255 )," + mFieldInstallUserId + " VARCHAR ( 255 )," + mFieldInstallDeviceIMEINo + " VARCHAR ( 255 )," + mFieldInstallDeviceServerId + " VARCHAR ( 255 )," + mFieldInstallDeviceLocalId + " VARCHAR ( 255 )," + mFieldInstallDevicName + " VARCHAR ( 255 )," + mFieldInstallDeviceWarranty_status + " VARCHAR ( 255 )," + mFieldInstallDeviceTypeName + " VARCHAR ( 255 )," + mFieldInstallHelpNo + " VARCHAR ( 255 )," + mFieldInstallDateTime + " VARCHAR ( 255 )," + mFieldInstallLatitude + " VARCHAR ( 255 )," + mFieldInstallLongitude + " VARCHAR ( 255 )," + mFieldInstallCountryCode + " VARCHAR ( 255 )," + mFieldInstallCountryName + " VARCHAR ( 255 )," + mFieldInstallVesselLocalId + " INTEGER," + mFieldInstallVesselServerId + " VARCHAR ( 255 )," + mFieldInstallVesselName + " VARCHAR ( 255 )," + mFieldInstallVesselRegNo + " VARCHAR ( 255 )," + mFieldInstallPower + " VARCHAR ( 255 )," + mFieldInstallLocation + " TEXT," + mFieldInstallOwnerName + " VARCHAR ( 255 )," + mFieldInstallOwnerAddress + " TEXT," + mFieldInstallOwnerCity + " VARCHAR ( 255 )," + mFieldInstallOwnerState + " VARCHAR ( 255 )," + mFieldInstallOwnerZipcode + " VARCHAR ( 255 )," + mFieldInstallOwnerEmail + " VARCHAR ( 255 )," + mFieldInstallOwnerMobileNo + " VARCHAR ( 255 )," + mFieldInstallLocalSignUrl + " VARCHAR ( 255 )," + mFieldInstallServerSignUrl + " VARCHAR ( 255 )," + mFieldInstallLocalInstallerSignUrl + " VARCHAR ( 255 )," + mFieldInstallServerInstallerSignUrl + " VARCHAR ( 255 )," + mFieldInstallPdfUrl + " VARCHAR ( 255 )," + mFieldInstallCreatedDate + " VARCHAR ( 255 )," + mFieldInstallUpdatedDate + " VARCHAR ( 255 )," + mFieldInstallIsSync + " INTEGER DEFAULT 0," + mFieldInstallStatus + " INTEGER DEFAULT 0," + mFieldInstallDateTimeStamp + " VARCHAR ( 255 )," + mFieldInstallIsSyncOwnerSign + " INTEGER DEFAULT 0," + mFieldInstallIsSyncInstallerSign + " INTEGER DEFAULT 0 )";
                    System.out.println("CREATE TABLE -" + sql);
                    db.execSQL(sql);

                    sql = "INSERT INTO " + mTableInstall + " SELECT * FROM " +
                            mTableInstall + "_OLD";
                    System.out.println("INSERT TABLE DATA -" + sql);
                    db.execSQL(sql);

                    sql = "drop table " + mTableInstall + "_OLD";
                    System.out.println("DROP TABLE -" + sql);
                    db.execSQL(sql);

                    // INSTALL PHOTO
                    sql = "ALTER TABLE " + mTableInstallerPhoto + " RENAME TO " +
                            mTableInstallerPhoto + "_OLD";
                    System.out.println("RENAME INSTALL PHOTO TABLE -" + sql);
                    db.execSQL(sql);

                    sql = "CREATE TABLE " + mTableInstallerPhoto + "(" + mFieldInstPhotoLocalID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," + mFieldInstPhotoServerID + " VARCHAR ( 255 )," + mFieldInstPhotoLocalURL + " VARCHAR ( 255 )," + mFieldInstPhotoServerURL + " VARCHAR ( 255 )," + mFieldInstPhotoType + " INTEGER," + mFieldInstLocalId + " INTEGER," + mFieldInstServerId + " VARCHAR ( 255 )," + mFieldInstPhotoUserId + " VARCHAR ( 255 )," + mFieldInstPhotoCreatedDate + " VACHAR ( 255 )," + mFieldInstPhotoUpdateDate + " VACHAR ( 255 )," + mFieldInstPhotoIsSync + " INTEGER DEFAULT 0 )";
                    System.out.println("CREATE TABLE -" + sql);
                    db.execSQL(sql);

                    sql = "INSERT INTO " + mTableInstallerPhoto + " SELECT * FROM " +
                            mTableInstallerPhoto + "_OLD";
                    System.out.println("INSERT TABLE DATA -" + sql);
                    db.execSQL(sql);

                    sql = "drop table " + mTableInstallerPhoto + "_OLD";
                    System.out.println("DROP TABLE -" + sql);
                    db.execSQL(sql);

                    // UNINSTALL

                    sql = "ALTER TABLE " + mTableUnInstall + " RENAME TO " +
                            mTableUnInstall + "_OLD";
                    System.out.println("RENAME UNINSTALL TABLE -" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableUnInstall + "_OLD" + " ADD COLUMN " +
                            mFieldUnInstallIsSyncOwnerSign + " INTEGER DEFAULT 0";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableUnInstall + "_OLD" + " ADD COLUMN " +
                            mFieldUnInstallIsSyncInstallerSign + " INTEGER DEFAULT 0";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);

                    sql = "CREATE TABLE " + mTableUnInstall + "(" + mFieldUnInstallLocalId + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," + mFieldUnInstallServerId + " VARCHAR ( 255 )," + mFieldUnInstallUserId + " VARCHAR ( 255 )," + mFieldUnInstallDeviceIMEINo + " VARCHAR ( 255 )," + mFieldUnInstallDeviceServerId + " VARCHAR ( 255 )," + mFieldUnInstallDeviceLocalId + " VARCHAR ( 255 )," + mFieldUnInstallDeviceName + " VARCHAR ( 255 )," + mFieldUnInstallDeviceWarrantStatus + " VARCHAR ( 255 )," + mFieldUnInstallDeviceTypeName + " VARCHAR ( 255 )," + mFieldUnInstallVesselLocalId + " INTEGER," + mFieldUnInstallVesselServerId + " VARCHAR ( 255 )," + mFieldUnInstallVesselName + " VARCHAR ( 255 )," + mFieldUnInstallVesselRegNo + " VARCHAR ( 255 )," + mFieldUnInstallOwnerName + " VARCHAR ( 255 )," + mFieldUnInstallOwnerAddress + " TEXT," + mFieldUnInstallOwnerCity + " VARCHAR ( 255 )," + mFieldUnInstallOwnerState + " VARCHAR ( 255 )," + mFieldUnInstallOwnerZipcode + " VARCHAR ( 255 )," + mFieldUnInstallOwnerEmail + " VARCHAR ( 255 )," + mFieldUnInstallOwnerMobileNo + " VARCHAR ( 255 )," + mFieldUnInstallLocalSignUrl + " VARCHAR ( 255 )," + mFieldUnInstallServerSignUrl + " VARCHAR ( 255 )," + mFieldUnInstallLocalUninstallerSignUrl + " VARCHAR ( 255 )," + mFieldUnInstallServerUninstallerSignUrl + " VARCHAR ( 255 )," + mFieldUnInstallPdfUrl + " VARCHAR ( 255 )," + mFieldUnInstallCreatedDate + " VARCHAR ( 255 )," + mFieldUnInstallUpdatedDate + " VARCHAR ( 255 )," + mFieldUnInstallIsSync + " INTEGER DEFAULT 0," + mFieldUnInstallStatus + " INTEGER DEFAULT 0," + mFieldUnInstallDateTime + " VARCHAR ( 255 )," + mFieldUnInstallSDateTimeStamp + " TEXT," + mFieldUnInstallIsSyncOwnerSign + " INTEGER DEFAULT 0," + mFieldUnInstallIsSyncInstallerSign + " INTEGER DEFAULT 0)";
                    System.out.println("CREATE TABLE -" + sql);
                    db.execSQL(sql);

                    sql = "INSERT INTO " + mTableUnInstall + " SELECT * FROM " +
                            mTableUnInstall + "_OLD";
                    System.out.println("INSERT TABLE DATA -" + sql);
                    db.execSQL(sql);

                    sql = "drop table " + mTableUnInstall + "_OLD";
                    System.out.println("DROP TABLE -" + sql);
                    db.execSQL(sql);

                    // INSPECTION

                    sql = "ALTER TABLE " + mTableInspection + " RENAME TO " +
                            mTableInspection + "_OLD";
                    System.out.println("RENAME INSPECTION TABLE -" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableInspection + "_OLD" + " ADD COLUMN " +
                            mFieldInspectionIsSyncOwnerSign + " INTEGER DEFAULT 0";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableInspection + "_OLD" + " ADD COLUMN " +
                            mFieldInspectionIsSyncInstallerSign + " INTEGER DEFAULT 0";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);

                    sql = "CREATE TABLE " + mTableInspection + "(" + mFieldInspectionLocalId + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," + mFieldInspectionServerId + " VARCHAR ( 255 )," + mFieldInspectionUserId + " VARCHAR ( 255 )," + mFieldInspectionDeviceIMEINo + " VARCHAR ( 255 )," + mFieldInspectionDeviceLocalId + " INTEGER," + mFieldInspectionDeviceServerId + " VARCHAR ( 255 )," + mFieldInspectionDevicName + " VARCHAR ( 255 )," + mFieldInspectionDeviceTypeName + " VARCHAR ( 255 )," + mFieldInspectionDeviceWarranty_status + " VARCHAR ( 255 )," + mFieldInspectionVesselLocalId + " INTEGER," + mFieldInspectionVesselServerId + " VARCHAR ( 255 )," + mFieldInspectionVesselName + " VARCHAR ( 255 )," + mFieldInspectionVesselRegNo + " VARCHAR ( 255 )," + mFieldInspectionOwnerName + " VARCHAR ( 255 )," + mFieldInspectionOwnerAddress + " VARCHAR ( 255 )," + mFieldInspectionOwnerCity + " VARCHAR ( 255 )," + mFieldInspectionOwnerState + " VARCHAR ( 255 )," + mFieldInspectionOwnerZipcode + " VARCHAR ( 255 )," + mFieldInspectionOwnerEmail + " VARCHAR ( 255 )," + mFieldInspectionOwnerMobileNo + " VARCHAR ( 255 )," + mFieldInspectionResult + " TEXT," + mFieldInspectionActionTaken + " VARCHAR ( 255 )," + mFieldInspectionWarrentyReturn + " VARCHAR ( 255 )," + mFieldInspectionLocalSignUrl + " VARCHAR ( 255 )," + mFieldInspectionServerSignUrl + " VARCHAR ( 255 )," + mFieldInspectionLocalInspectorSignUrl + " VARCHAR ( 255 )," + mFieldInspectionServerInspectorSignUrl + " VARCHAR ( 255 )," + mFieldInspectionPdfUrl + " VARCHAR ( 255 )," + mFieldInspectionCreatedDate + " VARCHAR ( 255 )," + mFieldInspectionUpdatedDate + " VARCHAR ( 255 )," + mFieldInspectionIsSync + " INTEGER DEFAULT 0," + mFieldInspectionStatus + " INTEGER DEFAULT 0," + mFieldInspectionDateTime + " VARCHAR ( 255 )," + mFieldInspectionDateTimeStamp + " VARCHAR ( 255 )," + mFieldInspectionIsSyncOwnerSign + " INTEGER DEFAULT 0," + mFieldInspectionIsSyncInstallerSign + " INTEGER DEFAULT 0)";
                    System.out.println("CREATE TABLE -" + sql);
                    db.execSQL(sql);

                    sql = "INSERT INTO " + mTableInspection + " SELECT * FROM " +
                            mTableInspection + "_OLD";
                    System.out.println("INSERT TABLE DATA -" + sql);
                    db.execSQL(sql);

                    sql = "drop table " + mTableInspection + "_OLD";
                    System.out.println("DROP TABLE -" + sql);
                    db.execSQL(sql);

                    // INSPECTION PHOTO
                    sql = "ALTER TABLE " + mTableInspectionPhoto + " RENAME TO " +
                            mTableInspectionPhoto + "_OLD";
                    System.out.println("RENAME INSPECTION PHOTO TABLE -" + sql);
                    db.execSQL(sql);

                    sql = "CREATE TABLE " + mTableInspectionPhoto + "(" + mFieldInspcPhotoLocalID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," + mFieldInspcPhotoServerID + " VARCHAR ( 255 )," + mFieldInspcPhotoLocalURL + " VARCHAR ( 255 )," + mFieldInspcPhotoServerURL + " VARCHAR ( 255 )," + mFieldInspcPhotoType + " INTEGER," + mFieldInspcLocalId + " INTEGER," + mFieldInspcServerId + " VARCHAR ( 255 )," + mFieldInspcPhotoUserId + " VARCHAR ( 255 )," + mFieldInspcPhotoCreatedDate + " VARCHAR ( 255 )," + mFieldInspcPhotoUpdateDate + " VARCHAR ( 255 )," + mFieldInspcPhotoIsSync + " INTEGER DEFAULT 0)";
                    System.out.println("CREATE TABLE -" + sql);
                    db.execSQL(sql);

                    sql = "INSERT INTO " + mTableInspectionPhoto + " SELECT * FROM " +
                            mTableInspectionPhoto + "_OLD";
                    System.out.println("INSERT TABLE DATA -" + sql);
                    db.execSQL(sql);

                    sql = "drop table " + mTableInspectionPhoto + "_OLD";
                    System.out.println("DROP TABLE -" + sql);
                    db.execSQL(sql);

                    break;
                case 3:
                    sql = "ALTER TABLE " + mTableInstall + " ADD COLUMN " +
                            mFieldInstallDeviceAccountId + " VARCHAR(255)";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableUnInstall + " ADD COLUMN " +
                            mFieldUnInstallDeviceAccountId + " VARCHAR(255)";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);
                    sql = "ALTER TABLE " + mTableInspection + " ADD COLUMN " +
                            mFieldInspectionDeviceAccountId + " VARCHAR(255)";
                    System.out.println("ALTER TABLE sql-" + sql);
                    db.execSQL(sql);
                    break;
                case 4:
                    sql = "CREATE TABLE " + mTableQuestionAnswer + "(" + mFieldQuesAnsLocalID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT UNIQUE," + mFieldQuesAnsUserID + " VARCHAR ( 255 )," + mFieldQuesAnsInsUninsInspType + " INTEGER DEFAULT 0," + mFieldQuesAnsInsUninsInspLocalID + " INTEGER," + mFieldQuesAnsInsUninsInspServerID + " VARCHAR ( 255 )," + mFieldQuesAnsText + " TEXT," + mFieldQuesAnsCreatedDate + " VARCHAR ( 255 )," + mFieldQuesAnsUpdatedDate + " VARCHAR ( 255 )," + mFieldQuesAnsIsSync + " INTEGER DEFAULT 0)";
                    System.out.println("CREATE TABLE -" + sql);
                    db.execSQL(sql);
                    break;
            }
            upgradeTo++;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
    }

    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        System.out.println("dbExist" + dbExist);
        if (dbExist) {
            // do nothing - database already exist
        } else {

            // By calling this method and empty database will be created into
            // the default system path
            // of your application so we are gonna be able to overwrite that
            // database with our database.
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {

                throw new Error("Error copying database");
            }
        }
    }

    /**
     * Check if the database already exist to avoid re-copying the file each
     * time you open the application.
     *
     * @return true if it exists, false if it doesn't
     */
    private boolean checkDataBase() {
        File databasePath = this.myContext.getDatabasePath(DB_PATH + DB_NAME);
        if (databasePath != null) {
            return databasePath.exists();
        } else {
            return false;
        }

//        SQLiteDatabase checkDB = null;
//        try {
//            String myPath = DB_PATH + DB_NAME;
//            checkDB = SQLiteDatabase.openDatabase(myPath, null,
//                    SQLiteDatabase.OPEN_READWRITE);
//
//            checkDB = getWritableDatabase();
//
//        } catch (SQLiteException e) {
//            // database does't exist yet.
//        }
//        if (checkDB != null) {
//            checkDB.close();
//        }
//        return checkDB != null ? true : false;
    }

    /**
     * Copies your database from your local assets-folder to the just created
     * empty database in the system folder, from where it can be accessed and
     * handled. This is done by transfering bytestream.
     */
    private void copyDataBase() throws IOException {

        // Open your local db as the input stream
        InputStream myInput = myContext.getAssets().open(DB_NAME);

        // Path to the just created empty db
        SQLiteDatabase database = this.getReadableDatabase();
        String outFileName = database.getPath();
        database.close();
        System.out.println("outFileName-" + outFileName);
        // Path to the just created empty db
//        String outFileName = DB_PATH + DB_NAME;

        // Open the empty db as the output stream
        OutputStream myOutput = new FileOutputStream(outFileName);

        // transfer bytes from the inputfile to the outputfile
        byte[] buffer = new byte[2048];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        // Close the streams
        myOutput.flush();
        myOutput.close();
        myInput.close();

    }

    /*Open database connection*/
    public void openDatabase() throws SQLException {
        try {
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Open the database
        db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);

    }

    /*Close database connection*/
    public synchronized void close() {
        if (db != null)
            db.close();
        super.close();
    }


    /**
     * Use this function to set the value of a particular column
     *
     * @param columnName       The column name whose value is to be changed
     * @param newColumnValue   The value to be replaced in the column
     * @param whereColumnName  The column name to be compared with the where clause
     * @param whereColumnValue The value to be compared in the where clause
     */
    void onUpdateSet(String columnName, String newColumnValue,
                     String[] whereColumnName, String[] whereColumnValue) {
        String expanded_ColumnNames = new String(whereColumnName[0]);
        String expanded_ColumnValues = new String(whereColumnValue[0]);
        for (int i = 1; i < whereColumnName.length; i++) {
            expanded_ColumnNames = expanded_ColumnNames + ","
                    + whereColumnName[i];
            expanded_ColumnValues = expanded_ColumnValues + ","
                    + whereColumnValue[i];
        }
        try {
            openDatabase();
            db.execSQL("update recipe set \"" + columnName + "\" = \""
                    + newColumnValue + "\" where \"" + expanded_ColumnNames
                    + "\" = \"" + expanded_ColumnValues + "\"");
        } catch (Exception e) {
        }

    }

    /**
     * Query the given table, returning a Cursor over the result set.
     *
     * @param table         The table name to compile the query against.
     * @param columns       A list of which columns to return. Passing null will return
     *                      all columns, which is discouraged to prevent reading data from
     *                      storage that isn't going to be used.
     * @param selection     A filter declaring which rows to return, formatted as an SQL
     *                      WHERE clause (excluding the WHERE itself). Passing null will
     *                      return all rows for the given table.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the
     *                      values from selectionArgs, in order that they appear in the
     *                      selection. The values will be bound as Strings.
     * @param groupBy       A filter declaring how to group rows, formatted as an SQL
     *                      GROUP BY clause (excluding the GROUP BY itself). Passing null
     *                      will cause the rows to not be grouped.
     * @param having        A filter declare which row groups to include in the cursor, if
     *                      row grouping is being used, formatted as an SQL HAVING clause
     *                      (excluding the HAVING itself). Passing null will cause all row
     *                      groups to be included, and is required when row grouping is
     *                      not being used.
     * @param orderBy       How to order the rows, formatted as an SQL ORDER BY clause
     *                      (excluding the ORDER BY itself). Passing null will use the
     *                      default sort order, which may be unordered.
     * @return A Cursor object, which is positioned before the first entry
     */
    public Cursor onQueryGetCursor(String table, String[] columns, String selection,
                                   String[] selectionArgs, String groupBy, String having, String orderBy) {
        Cursor query = null;
        try {
            openDatabase();
            query = db.query(table, columns, selection, selectionArgs, groupBy,
                    having, orderBy);
        } catch (Exception e) {
        }
        return query;
    }

    /**
     * Use this method to search a particular String in the provided field.
     *
     * @param columns     The array of columns to be returned
     * @param table       The table name
     * @param whereColumn The where clause specifying a particular columns
     * @param keyword     The keyword which is to be searched
     * @return The cursor containing the result of the query
     */
    Cursor onSearchGetCursor(String[] columns, String table,
                             String[] whereColumn, String keyword) {
        String expColumns = new String(columns[0]);
        Cursor rawquery = null;
        for (int i = 1; i < columns.length; i++)
            expColumns = expColumns + "," + columns[i];
        try {
            openDatabase();
            rawquery = db.rawQuery("SELECT " + expColumns + " from " + table
                    + " where " + whereColumn[0] + " like \"%" + keyword
                    + "%\" or " + whereColumn[1] + " like \"%" + keyword
                    + "%\" or " + whereColumn[2] + " like \"%" + keyword
                    + "%\"", null);
        } catch (Exception e) {
        }
        return rawquery;
    }

    /*Call custom query for database*/
    public Cursor Query(String sql) {
        Cursor c = null;
        try {
            System.out.println("Query-" + sql);
            c = db.rawQuery(sql, null);
        } catch (Exception e) {
        }
        return c;
    }

    /*Query based on your requirement*/
    public String getQueryResult(String query) {
        System.out.println("query-" + query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String result = "";
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getString(0);
        }
        cursor.close();
        System.out.println("result-" + result);
        return result;
    }

    public int getFirstRecordSqlQueryInt(String sql) {

        Cursor c = null;

        try {
            c = db.rawQuery(sql, null);
        } catch (Exception e) {
        }
        c.moveToFirst();
        return c.getInt(0);
    }

    public String getFirstRecordSqlQueryString(String sql) {

        Cursor c = null;

        try {
            c = db.rawQuery(sql, null);
        } catch (Exception e) {
        }
        c.moveToFirst();
        return c.getString(0);
    }


    /**
     * update particular record in the database.
     *
     * @param table
     * @param whereClause
     * @param whereArgs
     */
    public void onDelete(String table, String whereClause, String[] whereArgs) {
        try {
            db.delete(table, whereClause, whereArgs);
        } catch (Exception e) {
        }
    }

    /**
     * update particular record in the database.
     *
     * @param tableName
     * @param cValue
     * @param WhereField
     * @param complareValue
     */
    public void updateRecord(String tableName, ContentValues cValue, String WhereField, String[] complareValue) {
        openDatabase();
        try {
            db.update(tableName, cValue, WhereField, complareValue);
        } catch (SQLException e) {
        }
    }

    /**
     * Insert the record in the database.
     *
     * @param tableName
     * @param cValue
     */
    public int insertRecord(String tableName, ContentValues cValue) {
        openDatabase();
        try {
            return (int) db.insert(tableName, null, cValue);
        } catch (SQLException e) {
//			System.out.println("Call Insert err...."+e.toString());
        }

        return -1;
    }

    /**
     * Insert the record in the database.
     *
     * @param query
     * @param mVoVesselList
     */
    public int insertMultipleRecord(String query, List<VoVessel> mVoVesselList) {
        openDatabase();
        try {
//            System.out.println("QUERY-" + query);
            db.beginTransaction();
            SQLiteStatement insert = db.compileStatement(query);
            for (int i = 0; i < mVoVesselList.size(); i++) {
//                String checkQuery="select " + mFieldVesselSuccorfishId + " from " + mTableVesselAsset + " where " + mFieldVesselSuccorfishId + "= '" + mVoVesselList.get(i).getId() + "'";
//                System.out.println("checkQuery-" + checkQuery);
                insert.bindString(1, "");
                if (mVoVesselList.get(i).getId() != null && !mVoVesselList.get(i).getId().equals("") && !mVoVesselList.get(i).getId().equals("null")) {
                    insert.bindString(2, mVoVesselList.get(i).getId());
                } else {
                    insert.bindString(2, "NA");
                }
                if (mVoVesselList.get(i).getAccountId() != null && !mVoVesselList.get(i).getAccountId().equals("") && !mVoVesselList.get(i).getAccountId().equals("null")) {
                    insert.bindString(3, mVoVesselList.get(i).getAccountId());
                } else {
                    insert.bindString(3, "NA");
                }
                if (mVoVesselList.get(i).getName() != null && !mVoVesselList.get(i).getName().equals("") && !mVoVesselList.get(i).getName().equals("null")) {
                    insert.bindString(4, mVoVesselList.get(i).getName());
                } else {
                    insert.bindString(4, "NA");
                }
                if (mVoVesselList.get(i).getRegNo() != null && !mVoVesselList.get(i).getRegNo().equals("") && !mVoVesselList.get(i).getRegNo().equals("null")) {
                    insert.bindString(5, mVoVesselList.get(i).getRegNo());
                    insert.bindString(6, mVoVesselList.get(i).getRegNo());
                } else {
                    insert.bindString(5, "NA");
                    insert.bindString(6, "NA");
                }
                insert.bindString(7, "");
                insert.bindString(8, "");
                insert.bindString(9, "1");
                insert.execute();
            }
            db.setTransactionSuccessful();
        } catch (SQLException e) {
//			System.out.println("Call Insert err...."+e.toString());
        } finally {
            db.endTransaction();
        }

        return -1;
    }

    public int getLastRecord(String countQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    /*Get count from the database*/
    public int getCountRecordByQuery(String countQuery) {
        System.out.println("countQuery-" + countQuery);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            cnt = cursor.getInt(0);
        }
        cursor.close();
        return cnt;
    }

    public int getMaxVesselRecord(String query) {
        System.out.println("countQuery-" + query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int cnt = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            cnt = cursor.getInt(0);
        }
        cursor.close();
        return cnt;
    }

    public int getTableCount(String tableName) {
        String countQuery = "SELECT  * FROM " + tableName;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

    /**
     * Insert the record in the database.
     *
     * @param tableName
     * @param cValue
     */
    public void insertUpdateRecord(String tableName, ContentValues cValue) {
        openDatabase();
        try {
            db.replaceOrThrow(tableName, null, cValue);
        } catch (SQLException e) {
        }
    }


    public void exeQuery(String sql) {
        System.out.println("Query-" + sql);
        try {
            db.execSQL(sql);
        } catch (Exception e) {
        }
    }


    public DataHolder readCursor(Cursor mCursor) {

        // openDatabase();
        DataHolder _holder = null;

//		System.out.println("cursor read...."+mCursor.getCount());

        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            _holder = new DataHolder();

            while (!mCursor.isAfterLast()) {
                int count = mCursor.getColumnCount();
                _holder.CreateRow();
                for (int i = 0; i < count; i++) {
                    _holder.set_Lmap(mCursor.getColumnName(i), mCursor.getString(i));
                }
                _holder.AddRow();
                mCursor.moveToNext();
            }
        }
        return _holder;
    }

    /*Read data from database*/
    public DataHolder read(String query) {

        // openDatabase();

//		System.out.println("query...san.."+query);

        Cursor mCursor = Query(query);
        DataHolder _holder = null;
        if (mCursor != null && mCursor.getCount() > 0) {
            mCursor.moveToFirst();
            _holder = new DataHolder();

            while (!mCursor.isAfterLast()) {
                int count = mCursor.getColumnCount();
                _holder.CreateRow();
                for (int i = 0; i < count; i++) {
                    _holder.set_Lmap(mCursor.getColumnName(i), mCursor.getString(i));
                }
                _holder.AddRow();
                mCursor.moveToNext();
            }
        }
        mCursor.close();
        return _holder;
    }
}
