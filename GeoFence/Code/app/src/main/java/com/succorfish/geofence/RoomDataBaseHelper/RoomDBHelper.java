package com.succorfish.geofence.RoomDataBaseHelper;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.succorfish.geofence.RoomDataBaseDAO.Action_info_DAO;
import com.succorfish.geofence.RoomDataBaseDAO.ChatInfo_DAO;
import com.succorfish.geofence.RoomDataBaseDAO.GeoFenceAlert_DAO;
import com.succorfish.geofence.RoomDataBaseDAO.GeoFence_DAO;
import com.succorfish.geofence.RoomDataBaseDAO.Polygon_DAO;
import com.succorfish.geofence.RoomDataBaseDAO.RulesInfo_DAO;
import com.succorfish.geofence.RoomDataBaseDAO.RulesTable_DAO;
import com.succorfish.geofence.RoomDataBaseDAO.TableDevice_DAO;
import com.succorfish.geofence.RoomDataBaseEntity.Action_info;
import com.succorfish.geofence.RoomDataBaseEntity.ChatInfo;
import com.succorfish.geofence.RoomDataBaseEntity.DeviceTable;
import com.succorfish.geofence.RoomDataBaseEntity.Geofence;
import com.succorfish.geofence.RoomDataBaseEntity.GeofenceAlert;
import com.succorfish.geofence.RoomDataBaseEntity.PolygonEnt;
import com.succorfish.geofence.RoomDataBaseEntity.Rules;
import com.succorfish.geofence.RoomDataBaseEntity.RulesInformation;
@Database(entities ={
        Action_info.class,
        ChatInfo.class,
        DeviceTable.class,
        Geofence.class,
        GeofenceAlert.class,
        PolygonEnt.class,
        Rules.class,
        RulesInformation.class}
        ,version =1,exportSchema = false)
public abstract  class RoomDBHelper  extends RoomDatabase {
    private final static String TAG = RoomDBHelper.class.getSimpleName();
    private final static String DB_NAME = "sc2_companinon.db";
    private static RoomDBHelper roomDBHelperINSTACE=null;
    /**
     * Creating a Single Instance of DataBase
     */
    public static synchronized RoomDBHelper getRoomDBInstance(Context context){
        if(roomDBHelperINSTACE==null){
            roomDBHelperINSTACE = buildDatabaseInstance(context);
        }
        return roomDBHelperINSTACE;
    }
    private static RoomDBHelper buildDatabaseInstance(Context context) {
        return Room.databaseBuilder(context, RoomDBHelper.class, DB_NAME)
                    .build();
    }
    /**
     * interface DAO
     */
    public abstract Action_info_DAO get_Action_info_dao();
    public abstract ChatInfo_DAO get_Chat_info_dao();
    public abstract GeoFence_DAO get_GeoFence_info_dao();
    public abstract GeoFenceAlert_DAO get_GeoFenceAlert_info_dao();
    public abstract Polygon_DAO get_Polygon_info_dao();
    public abstract RulesInfo_DAO get_RulesInfo_info_dao();
    public abstract RulesTable_DAO get_RulesTable_dao();
    public abstract TableDevice_DAO get_TableDevice_dao();
}
