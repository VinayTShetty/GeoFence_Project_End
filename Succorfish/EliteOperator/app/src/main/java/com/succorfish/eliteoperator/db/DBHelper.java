package com.succorfish.eliteoperator.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class DBHelper extends SQLiteOpenHelper {

    private static String DB_PATH = "/data/data/com.succorfish.eliteoperator/databases/";

    private final static String DB_NAME = "EliteOperators.sqlite";

    private final Context myContext;

    private static SQLiteDatabase db;

    ContentValues mContentValues;

    public static String mTableAddressBook = "tbl_address_book";
    public static String mTableCannedDiverMessage = "tbl_canned_diver_message";
    public static String mTableCannedSurfaceMessage = "tbl_canned_surface_message";
    public static String mTableHistory = "tbl_history";

    // TABLE Address Book
    public static String mFieldAddressBookId = "id";
    public static String mFieldAddressBookUserId = "user_id";
    public static String mFieldAddressBookUserName = "user_name";
    public static String mFieldAddressBookUserPhoto = "user_photo";
    public static String mFieldAddressBookUserType = "user_type";
    public static String mFieldAddressBookDeviceId = "device_id";
    public static String mFieldAddressBookLastMessageId = "last_message_id";
    public static String mFieldAddressBookLastMessageName = "last_message";
    public static String mFieldAddressBookLastMessageTime = "last_message_time";
    public static String mFieldAddressBookCreatedDate = "created_date";
    public static String mFieldAddressBookUpdatedDate = "updated_date";
    public static String mFieldAddressBookIsSync = "is_sync";

    // TABLE Canned Diver Message
    public static String mFieldDiverMessageId = "id";
    public static String mFieldDiverMessageMSGId = "driver_message_id";
    public static String mFieldDiverMessageMessage = "message";
    public static String mFieldDiverMessageIsEmergency = "is_emergency";
    public static String mFieldDiverMessageCreatedDate = "created_date";
    public static String mFieldDiverMessageUpdatedDate = "updated_date";
    public static String mFieldDiverMessageIsSync = "is_sync";

    // TABLE Canned Surface Message
    public static String mFieldSurfaceMessageId = "id";
    public static String mFieldSurfaceMessageMSGId = "surface_message_id";
    public static String mFieldSurfaceMessageMessage = "message";
    public static String mFieldSurfaceMessageIsEmergency = "is_emergency";
    public static String mFieldSurfaceMessageCreatedDate = "created_date";
    public static String mFieldSurfaceMessageUpdatedDate = "updated_date";
    public static String mFieldSurfaceMessageIsSync = "is_sync";

    // TABLE HISTORY
    public static String mFieldHistoryID = "id";
    public static String mFieldHistoryFromId = "from_id";
    public static String mFieldHistoryFromName = "from_name";
    public static String mFieldHistoryToId = "to_id";
    public static String mFieldHistoryToName = "to_name";
    public static String mFieldHistoryMessageId = "message_id";
    public static String mFieldHistoryMessageName = "message_name";
    public static String mFieldHistoryTime = "time";
    public static String mFieldHistoryStatus = "status";
    public static String mFieldHistoryUpdatedDate = "updated_date";

    /**
     * Constructor Takes and keeps a reference of the passed context in order to
     * access to the application assets and resources.
     *
     * @param context
     */
    public DBHelper(Context context) {

        super(context, DB_NAME, null, 1);
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
//        // Path to the just created empty db
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

    public void openDatabase() throws SQLException {
        try {
            if (db != null && db.isOpen()) {
                db.close();
            }
        } catch (Exception e) {
        }
        // Open the database
        db = SQLiteDatabase.openDatabase(DB_PATH + DB_NAME, null,
                SQLiteDatabase.OPEN_READWRITE);

    }

    public synchronized void close() {
        if (db != null)
            db.close();
        super.close();
    }

    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

    public Cursor Query(String sql) {

        Cursor c = null;

        try {
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
