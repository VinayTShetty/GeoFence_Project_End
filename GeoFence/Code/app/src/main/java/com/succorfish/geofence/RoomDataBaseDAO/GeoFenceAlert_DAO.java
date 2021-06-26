package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.Action_info;
import com.succorfish.geofence.RoomDataBaseEntity.Geofence;
import com.succorfish.geofence.RoomDataBaseEntity.GeofenceAlert;

import java.util.List;
@Dao
public interface GeoFenceAlert_DAO {
    @Query("Select * from Geofence_alert_Table ORDER BY timeStamp DESC")
    List<GeofenceAlert> getAll_GeoFence_Alert();
    @Insert
    void insert_GeoFence_Alert(GeofenceAlert geofenceAlert);
    @Update
    void update_GeoFence_Alert(GeofenceAlert geofenceAlert);
    @Delete
    void delete_DeleteGeoFence_Alert(GeofenceAlert geofenceAlert);
    @Query("Select COUNT(id) from geofence_alert_table where is_Read=:valueToCheck")
    int getCountNumberOfRecordsAvaliable(String valueToCheck);
    @Query("update geofence_alert_table set is_Read=1 where timeStamp=:SystemTimstamp")
    void updateNotificationTagRead(String SystemTimstamp);
    @Query("select * from geofence_alert_table where timeStamp=:SystemTimeStamp")
    List<GeofenceAlert>  getGeoFeneAlertFromTimeStamp(String SystemTimeStamp);
}
