package com.succorfish.depthntemp.db;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.succorfish.depthntemp.interfaces.BleDeviceDao;
import com.succorfish.depthntemp.interfaces.DepthCutOffDao;
import com.succorfish.depthntemp.interfaces.DiveDao;
import com.succorfish.depthntemp.interfaces.TempPressDao;

/*Create room database instance*/
@Database(entities = {TableDive.class, TablePressureTemperature.class, TablePressureDepthCutOff.class, TableBleDevice.class}, version = 1)
public abstract class AppRoomDatabase extends android.arch.persistence.room.RoomDatabase {
    private static volatile AppRoomDatabase INSTANCE;

    public abstract DiveDao diveDao();

    public abstract TempPressDao tempPressDao();

    public abstract DepthCutOffDao depthCutOffDao();

    public abstract BleDeviceDao bleDeviceDao();

    public static AppRoomDatabase getDatabaseInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppRoomDatabase.class, "DepthNTemp.db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyDatabaseInstance() {
        INSTANCE = null;
    }

//    private void getUsersFromDB() {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                users = userService.getAll();
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void agentsCount) {
//                usersTextView.setText("Users \n\n " + users);
//            }
//        }.execute();
//    }
}
