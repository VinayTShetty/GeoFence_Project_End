package com.succorfish.geofence.RoomDataBaseDAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.succorfish.geofence.RoomDataBaseEntity.PolygonEnt;
import java.util.List;

@Dao
public interface Polygon_DAO {
    @Query("Select * from Polygon_Lat_Long")
    List<PolygonEnt> getAll_Polygon();
    @Insert
    void insert_Polygon(PolygonEnt polyGon);
    @Update
    void update_Polygon(PolygonEnt polyGon);
    @Delete
    void delete_Polygon(PolygonEnt polyGon);
    @Query("select * from Polygon_Lat_Long where geofence_ID=:geoFenceIdValue AND geofence_timeStamp=:timeStamp")
    List<PolygonEnt> getAllPolygonFromTimeStapGeoFenceId(String geoFenceIdValue,String timeStamp);
}
