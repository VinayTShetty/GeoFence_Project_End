package com.succorfish.depthntemp.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/*Sql database helper*/
public class DBHelper extends SQLiteOpenHelper {

    private final static String DB_PATH = "/data/data/com.succorfish.DepthNTemp/databases/";

    private final static String DB_NAME = "SuccorfishInstallers.sqlite";
    private final static int DB_VESRION=1;
    private final Context myContext;

    private static SQLiteDatabase db;

    ContentValues mContentValues;

    public static String mTableVesselAsset = "tbl_vessel_asset";
    public static String mTableInstall = "tbl_install";
    public static String mTableInstallerPhoto = "tbl_installer_photo";
    public static String mTableUnInstall = "tbl_uninstall";
    public static String mTableInspection = "tbl_inspection";
    public static String mTableInspectionPhoto = "tbl_inspection_photo";

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


    // TABLE INSPECTION
    public static String mFieldInspectionLocalId = "insp_local_id";
    public static String mFieldInspectionServerId = "insp_server_id";
    public static String mFieldInspectionUserId = "insp_user_id";
    public static String mFieldInspectionDeviceIMEINo = "insp_device_iemi_no";
    public static String mFieldInspectionDeviceServerId = "insp_device_server_id";
    public static String mFieldInspectionDeviceLocalId = "insp_device_local_id";
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

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VESRION);
        this.myContext = context;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("oldVersion-" + oldVersion + " newVersion-" + newVersion);

//        if (oldVersion <= newVersion) {
//            if (oldVersion == 1) {
//                String sql = "ALTER TABLE " + mTableInstall + " ADD COLUMN " +
//                        mFieldInstallLocalInstallerSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableInstall + " ADD COLUMN " +
//                        mFieldInstallServerInstallerSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableUnInstall + " ADD COLUMN " +
//                        mFieldUnInstallLocalUninstallerSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableUnInstall + " ADD COLUMN " +
//                        mFieldUnInstallServerUninstallerSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableInspection + " ADD COLUMN " +
//                        mFieldInspectionLocalInspectorSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableInspection + " ADD COLUMN " +
//                        mFieldInspectionServerInspectorSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableInspection + " ADD COLUMN " +
//                        mFieldInspectionLocalSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableInspection + " ADD COLUMN " +
//                        mFieldInspectionServerSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableVesselAsset + " ADD COLUMN " +
//                        mFieldVesselSuccorfishId + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableVesselAsset + " ADD COLUMN " +
//                        mFieldVesselAccountId + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//            } else if (oldVersion == 2) {
//                String sql = "ALTER TABLE " + mTableInspection + " ADD COLUMN " +
//                        mFieldInspectionLocalSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableInspection + " ADD COLUMN " +
//                        mFieldInspectionServerSignUrl + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableVesselAsset + " ADD COLUMN " +
//                        mFieldVesselSuccorfishId + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//                sql = "ALTER TABLE " + mTableVesselAsset + " ADD COLUMN " +
//                        mFieldVesselAccountId + " VARCHAR(255)";
//                System.out.println("ALTER TABLE sql-" + sql);
//                db.execSQL(sql);
//            } else if (oldVersion == 3) {
//
//            }
//        }
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

        SQLiteDatabase checkDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            checkDB = SQLiteDatabase.openDatabase(myPath, null,
                    SQLiteDatabase.OPEN_READWRITE);

            checkDB = getWritableDatabase();

        } catch (SQLiteException e) {
            // database does't exist yet.
        }
        if (checkDB != null) {
            checkDB.close();
        }
        return checkDB != null ? true : false;
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
        String outFileName = DB_PATH + DB_NAME;

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

    public void openDatabase() throws SQLException {
        try {
            db.close();
        } catch (Exception e) {
        }
        // Open the database
        String myPath = DB_PATH + DB_NAME;
        db = SQLiteDatabase.openDatabase(myPath, null,
                SQLiteDatabase.OPEN_READWRITE);

    }

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


    public Cursor Query(String sql) {
        Cursor c = null;
        try {
            System.out.println("Query-" + sql);
            c = db.rawQuery(sql, null);
        } catch (Exception e) {
        }
        return c;
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

    public int getLastRecord(String countQuery) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = cursor.getCount();
        cursor.close();
        return cnt;
    }

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
        return _holder;
    }
}
