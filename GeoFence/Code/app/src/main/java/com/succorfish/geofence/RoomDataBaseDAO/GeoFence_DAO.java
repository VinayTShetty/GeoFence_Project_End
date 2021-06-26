package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.Action_info;
import com.succorfish.geofence.RoomDataBaseEntity.Geofence;

import java.util.List;

@Dao
public interface GeoFence_DAO {
    @Query("Select * from Geofence")
    List<Geofence> getAll_GeoFence();
    @Insert
    void insert_GeoFence(Geofence geofence);
    @Update
    void update_GeoFence(Geofence geofence);
    @Delete
    void delete_GeoFence(Geofence geofence);
    @Query("Select * from Geofence where geofence_ID=:columnValue")
    boolean isGeoFenceId_Avaliable(String columnValue);
    @Query("Delete from geofence where geofence_ID=:geoFenceId")
    void deleteRecordFromGeoFenceId(String geoFenceId);
    @Query("select firmware_timestamp from geofence where geofence_ID=:geoFeneIdValue")
    String getFirmwareTimeStampFromGeoFenceId(String geoFeneIdValue);
    @Query("select lat from geofence where geofence_ID=:geoFeneIdValue")
    String getLatitudeFromGeoFenceId(String geoFeneIdValue);
    @Query("select long from geofence where geofence_ID=:geoFeneIdValue")
    String getLongitudeFromGeoFenceId(String geoFeneIdValue);
    @Query("select radiusOrvertices from geofence where geofence_ID=:geoFeneIdValue")
    String getRadiusVerticesFromGeoFenceId(String geoFeneIdValue);
    @Query("select type from geofence where geofence_ID=:geoFenceIdValue")
    String getGeoFenceTypeFromGeoFenceId(String geoFenceIdValue);
}
