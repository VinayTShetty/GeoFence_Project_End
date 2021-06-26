package com.vithamastech.smartlight.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DBHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/com.vithamastech.smartlight/databases/";
    public static final String TAG = DBHelper.class.getSimpleName();
    private final static String DB_NAME = "smartlight.sqlite";
    private final static int DB_VERSION = 1;
    private final Context myContext;

    private static SQLiteDatabase mSqLiteDatabase;

    /*Table Name*/
    public static String mTableDevice = "tbl_device";
    public static String mTableDeviceType = "tbl_device_type";
    public static String mTableGroup = "tbl_group";
    public static String mTableGroupDeviceList = "tbl_group_devices_list";
    public static String mTablePowerStripSocket = "tbl_power_strip_socket";
    public static String mTableAlarm = "tbl_alarm";
    public static String mTableAlarmDeviceList = "tbl_alarm_device_detail_list";
    public static String mTableColorSolid = "tbl_solid_color";
    public static String mTableColorVoice = "tbl_voice_color";
    public static String mTableColorFavourite = "tbl_favourite_color";
    public static String mTableUserAccount = "tbl_user_account";
    public static String mTableAlarmPowerSocket = "tbl_socket_Alarm_Table";
    public static String mTableDemoVideo = "tbl_demo_videos";
    public static String mTableSocketDeviceDtl = "tbl_socket_device_dtl";

    // TABLE SOCKET DEVICE DETAIL
    public static String mFieldTableSocketDeviceDtlRecordId = "record_id";
    public static String mFieldTableSocketDeviceDtlSocketId = "socket_id";
    public static String mFieldTableSocketDeviceDtlSocketName = "socket_name";
    public static String mFieldTableSocketDeviceDtlImageType = "image_type";
    public static String mFieldTableSocketDeviceDtlDeviceId = "device_id";

    // TABLE DEVICE
    public static String mFieldDeviceLocalId = "local_device_id";
    public static String mFieldDeviceServerId = "server_device_id";
    public static String mFieldDeviceUserId = "user_id";
    public static String mFieldDeviceCommID = "device_comm_id";
    public static String mFieldDeviceCommHexId = "device_comm_hex_id";
    public static String mFieldDeviceName = "device_name";
    public static String mFieldDeviceRealName = "device_real_name";
    public static String mFieldDeviceBleAddress = "device_ble_address";
    public static String mFieldDeviceType = "device_type";
    public static String mFieldDeviceTypeName = "device_type_name";
    public static String mFieldDeviceBrightness = "device_brightness";
    public static String mFieldDeviceColor = "device_color";
    public static String mFieldConnectStatus = "connect_status";
    public static String mFieldSwitchStatus = "switch_status";
    public static String mFieldDeviceIsFavourite = "is_favourite";
    public static String mFieldDeviceTimeStamp = "timestamp";
    public static String mFieldDeviceIsActive = "is_active";
    public static String mFieldDeviceLastState = "remember_last_state";
    public static String mFieldDeviceCreatedAt = "created_at";
    public static String mFieldDeviceUpdatedAt = "updated_at";
    public static String mFieldDeviceIsSync = "is_sync";
    public static String mFieldDeviceIsWifiConfigured = "is_wifi_configured";
    public static String mFieldDeviceSocketState = "socket_state";

    // TABLE GROUP
    public static String mFieldGroupLocalID = "group_local_id";
    public static String mFieldGroupServerID = "group_server_id";
    public static String mFieldGroupUserId = "user_id";
    public static String mFieldGroupCommId = "group_comm_id";
    public static String mFieldGroupCommHexId = "group_comm_hex_id";
    public static String mFieldGroupName = "group_name";
    public static String mFieldGroupDeviceSwitchStatus = "group_switch_status";
    public static String mFieldGroupIsFavourite = "group_is_favourite";
    public static String mFieldGroupTimeStamp = "group_timestamp";
    public static String mFieldGroupIsActive = "group_is_active";
    public static String mFieldGroupCreatedAt = "created_date";
    public static String mFieldGroupUpdatedAt = "updated_date";
    public static String mFieldGroupIsSync = "group_is_sync";

    // TABLE GROUPDeviceList
    public static String mFieldGDListLocalID = "group_device_id";
    public static String mFieldGDListUserID = "gd_user_id";
    public static String mFieldGDListLocalDeviceID = "gd_local_device_id";
    public static String mFieldGDListServerDeviceID = "gd_server_device_id";
    public static String mFieldGDListLocalGroupID = "gd_group_local_id";
    public static String mFieldGDListServerGroupID = "gd_group_server_id";
    public static String mFieldGDListStatus = "gd_group_device_status";
    public static String mFieldGDListCreatedDate = "gd_created_date";

    // TABLE SOCKET
    public static String mFieldSocketLocalID = "socket_local_id";
    public static String mFieldSocketServerD = "socket_server_id";
    public static String mFieldSocketUserId = "user_id";
    public static String mFieldSocketID = "socket_id";
    public static String mFieldSocketName = "socket_name";
    public static String mFieldSocketIEEE = "socket_ieee";
    public static String mFieldSocketDeviceLocalId = "device_local_id";
    public static String mFieldSocketDeviceServerId = "device_server_id";
    public static String mFieldSocketDeviceCommId = "device_comm_id";
    public static String mFieldSocketDeviceCommHexId = "device_comm_hex_id";
    public static String mFieldSocketDeviceName = "device_name";
    public static String mFieldSocketDeviceBLEAddress = "device_ble_address";
    public static String mFieldSocketDeviceType = "device_type";
    public static String mFieldSocketStatus = "switch_status";
    public static String mFieldSocketTimeStamp = "socket_timestamp";
    public static String mFieldSocketIsActive = "socket_is_active";
    public static String mFieldSocketCreatedAt = "created_at";
    public static String mFieldSocketUpdatedAt = "updated_at";
    public static String mFieldSocketIsSync = "socket_is_sync";

    // TABLE ALARM
    public static String mFieldAlarmLocalID = "alarm_local_id";
    public static String mFieldAlarmServerID = "alarm_server_id";
    public static String mFieldAlarmUserId = "user_id";
    public static String mFieldAlarmName = "alarm_name";
    public static String mFieldAlarmTime = "alarm_time";
    public static String mFieldAlarmStatus = "alarm_status";
    public static String mFieldAlarmDays = "alarm_days";
    public static String mFieldAlarmColor = "alarm_color";
    public static String mFieldAlarmLightOn = "alarm_light_on";
    public static String mFieldAlarmWakeUpSleep = "alarm_wakeup_sleep";
    public static String mFieldAlarmTimeStamp = "alarm_timestamp";
    public static String mFieldAlarmCountNo = "alarm_count";
    public static String mFieldAlarmIsActive = "alarm_is_active";
    public static String mFieldAlarmCreatedAt = "created_at";
    public static String mFieldAlarmUpdatedAt = "updated_at";
    public static String mFieldAlarmIsSync = "alarm_is_sync";

    // TABLE ALARM DEVICE LIST
    public static String mFieldADLocalID = "ad_detail_id";
    public static String mFieldADUserId = "ad_user_id";
    public static String mFieldADAlarmLocalID = "ad_alarm_local_id";
    public static String mFieldADAlarmServerID = "ad_alarm_server_id";
    public static String mFieldADDeviceLocalID = "ad_device_local_id";
    public static String mFieldADDeviceServerID = "ad_device_server_id";
    public static String mFieldADDeviceStatus = "ad_device_status";
    public static String mFieldADCreatedDate = "ad_created_date";

    // TABLE DEVICE TYPE
    public static String mFieldDeviceTypeLocalID = "device_type_local_id";
    public static String mFieldDeviceTypeServerID = "device_type_server_id";
    public static String mFieldDeviceTypeTypeName = "device_type_name";
    public static String mFieldDeviceTypeType = "device_type";
    public static String mFieldDeviceTypeIsActive = "is_active";
    public static String mFieldDeviceTypeCreatedAt = "created_at";
    public static String mFieldDeviceTypeUpdatedAt = "updated_at";

    // TABLE COLOR SOLID & VOICE
    public static String mFieldColorId = "id";
    public static String mFieldColorName = "color_name";
    public static String mFieldColorNameGujarati = "color_name_gujarati";
    public static String mFieldColorNameBengali = "color_name_bengali";
    public static String mFieldColorNameKannada = "color_name_kannada";
    public static String mFieldColorNameMalayalam = "color_name_malayalam";
    public static String mFieldColorNameMarathi = "color_name_marathi";
    public static String mFieldColorNameTelugu = "color_name_telugu";
    public static String mFieldColorNameHindi = "color_name_hindi";
    public static String mFieldColorNameTamil = "color_name_tamil";

    public static String mFieldColorRGB = "color_rgb";
    public static String mFieldColorRed = "color_red";
    public static String mFieldColorGreen = "color_green";
    public static String mFieldColorBlue = "color_blue";

    // TABLE FAVOURITE COLOR
    public static String mFieldColorFavId = "id";
    public static String mFieldColorFavRGB = "color_rgb";
    public static String mFieldColorFavIsActive = "is_active";
    public static String mFieldColorFavHasColor = "has_color";
    public static String mFieldColorUserId = "user_id";

    // TABLE USER ACCOUNT
    public static String mFieldUserLocalID = "local_user_id";
    public static String mFieldUserServerID = "server_user_id";
    public static String mFieldUserName = "user_name";
    public static String mFieldUserAccountName = "account_name";
    public static String mFieldUserEmail = "user_email";
    public static String mFieldUserMobileNo = "user_mobile_no";
    public static String mFieldUserPassword = "user_pw";
    public static String mFieldUserToken = "user_token";
    private static DBHelper sInstance;

    // TABLE ALARM.

    public static final String mfield_alarm_id = "alarm_id";
    public static final String mfield_socket_id = "socket_id";
    public static final String mfield_day_value = "day_value";
    public static final String mfield_OnTimestamp = "OnTimestamp";
    public static final String mfield_OffTimestamp = "OffTimestamp";
    public static final String mfield_On_original = "On_original";
    public static final String mfield_Off_original = "Off_original";
    public static final String mfield_alarm_state = "alarm_state";
    public static final String mfield_ble_address = "ble_address";
    public static final String mfield_day_selected = "day_selected";

    // TABLE YOUTUBE VIDEOS

    public static final String mFieldDemoVideoId = "id";
    public static final String mfield_demo_video_title_name = "video_title";
    public static final String mfield_demo_video_url = "video_url";
    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    /* Get Database Instance*/
    public static synchronized DBHelper getDBHelperInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DBHelper(context.getApplicationContext());
        }
        return sInstance;
    }

    public static synchronized DBHelper getDBHelperInstance() {
        if (sInstance != null) {
            return sInstance;
        } else {
            return null;
        }
    }

    /*Initialize database Path */
    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            DB_PATH = context.getApplicationInfo().dataDir + "/databases/";
        } else if (android.os.Build.VERSION.SDK_INT >= 17) {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        } else {
            DB_PATH = "/data/data/" + context.getPackageName() + "/databases/";
        }
        System.out.println("DATABASE-PATH=" + DB_PATH);
        this.myContext = context;
        //		mCommomMethod=new CommomMethod(myContext);
        //		myImageLoader=new MyImageLoader(myContext);
    }

    /**
     * Creates a empty database on the system and rewrites it with your own
     * database.
     */
    /*Create Database */
    public void createDataBase() throws IOException {
        boolean dbExist = checkDataBase();
        System.out.println("dbExist" + dbExist);
        if (dbExist) {

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

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        System.out.println("onUpgrade : " + oldVersion + "-" + newVersion);
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
//            e.printStackTrace();
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
//        String outFileName1 = DB_PATH + DB_NAME;
//        System.out.println("outFileName1-"+outFileName1);
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

    /* Open Database Connection*/
    public void openDatabase() throws SQLException {
        try {
            if (mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) {
                mSqLiteDatabase.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (mSqLiteDatabase != null && mSqLiteDatabase.isOpen()) {
                mSqLiteDatabase.close();
            }
        }

        // Open the database
        mSqLiteDatabase = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);

    }

    /*Close Database connection*/
    public synchronized void close() {
        if (mSqLiteDatabase != null)
            mSqLiteDatabase.close();
        super.close();
    }

    public void onCreate(SQLiteDatabase db) {

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
            mSqLiteDatabase.execSQL("update recipe set \"" + columnName + "\" = \""
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
            query = mSqLiteDatabase.query(table, columns, selection, selectionArgs, groupBy,
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
            rawquery = mSqLiteDatabase.rawQuery("SELECT " + expColumns + " from " + table
                    + " where " + whereColumn[0] + " like \"%" + keyword
                    + "%\" or " + whereColumn[1] + " like \"%" + keyword
                    + "%\" or " + whereColumn[2] + " like \"%" + keyword
                    + "%\"", null);
        } catch (Exception e) {
        }
        return rawquery;
    }

    /*Fetch Record From Database*/
    public int getCountRecordByQuery(String countQuery) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int cnt = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            cnt = cursor.getInt(0);
        }
        cursor.close();
//        System.out.println("countQueryUrl-" + countQuery + ":" + cnt);
        return cnt;
    }

    public Cursor Query(String sql) {
        Cursor c = null;
        try {
            c = mSqLiteDatabase.rawQuery(sql, null);
        } catch (Exception e) {
//            c.close();
        }
        return c;
    }

    public int getFirstRecordSqlQueryInt(String sql) {

        Cursor c = null;

        try {
            c = mSqLiteDatabase.rawQuery(sql, null);
        } catch (Exception e) {
        }
        c.moveToFirst();
        return c.getInt(0);
    }

//    public String getSingleRecordByQuery(String sql) {
//        System.out.println("url-" + sql);
//        Cursor c = null;
//        try {
//            c = db.rawQuery(sql, null);
//        } catch (Exception e) {
//        }
//        c.moveToFirst();
//        return c.getString(0);
//    }

    public String getQueryResult(String query) {
//        System.out.println("query-" + query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        String result = "";
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getString(0);
        }
        cursor.close();
//        System.out.println("result-" + result);
        return result;
    }

    public int getQueryIntResult(String query) {
//        System.out.println("query-" + query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);
        int result = 0;
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            result = cursor.getInt(0);
        }
        cursor.close();
//        System.out.println("result-" + result);
        return result;
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
            mSqLiteDatabase.delete(table, whereClause, whereArgs);
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
//        openDatabase();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            db.update(tableName, cValue, WhereField, complareValue);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Insert the record in the database.
     *
     * @param tableName
     * @param cValue
     */
    public int insertRecord(String tableName, ContentValues cValue) {
//        openDatabase();
        try {
            SQLiteDatabase db = this.getWritableDatabase();
            return (int) db.insert(tableName, null, cValue);
        } catch (SQLException e) {
            e.printStackTrace();
			System.out.println("Call Insert err...."+e.toString());
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

    public int getTableCount(String tableName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
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
            mSqLiteDatabase.replaceOrThrow(tableName, null, cValue);
        } catch (SQLException e) {
        }
    }

    public void exeQuery(String sql) {
        System.out.println("Query-" + sql);
        try {
            mSqLiteDatabase.execSQL(sql);
        } catch (Exception e) {
            Log.d(TAG, "exeQuery: ");
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

//        Cursor mCursor = Query(query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery(query, null);
        DataHolder _holder = new DataHolder();
        try {
            if (mCursor != null && !mCursor.isClosed()) {
                if (mCursor.moveToFirst()) {
                    do {
                        _holder.CreateRow();
                        for (int i = 0; i < mCursor.getColumnCount(); i++) {
                            _holder.set_Lmap(mCursor.getColumnName(i), mCursor.getString(i));
                        }
                        _holder.AddRow();
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
        return _holder;
    }

    public DataHolder readData(String query) {

        // openDatabase();

//		System.out.println("query...san.."+query);

//        Cursor mCursor = Query(query);
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor mCursor = db.rawQuery(query, null);
        DataHolder _holder = new DataHolder();
        try {
            if (mCursor != null && !mCursor.isClosed()) {
                if (mCursor.moveToFirst()) {
                    do {
                        _holder.CreateRow();
                        for (int i = 0; i < mCursor.getColumnCount(); i++) {
                            _holder.set_Lmap(mCursor.getColumnName(i), mCursor.getString(i));
                        }
                        _holder.AddRow();
                    } while (mCursor.moveToNext());
                }
                mCursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mCursor != null) {
                mCursor.close();
            }
        }
        return _holder;
    }

    public boolean checkRecordAvaliableInTable(String tableName, String columnName_1, String columnName_2, String columnName_3, String columnValue_1, String columnValue_2, String columnValue_3) {
        SQLiteDatabase db = this.getReadableDatabase();
        String querey = "SELECT * from " + tableName + " WHERE " + columnName_1 + " = " + "'" + columnValue_1 + "'" + " AND " + columnName_2 + "= " + "'" + columnValue_2 + "'" + " AND " + columnName_3 + "=" + "'" + columnValue_3 + "'";
        Cursor cursor = db.rawQuery(querey, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return true;
        }
        cursor.close();
        return false;
    }
}
