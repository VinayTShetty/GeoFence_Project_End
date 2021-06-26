package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Polygon_Lat_Long")
public class PolygonEnt implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "geofence_ID")
    private String geofence_ID="";
    @ColumnInfo(name = "lat")
    private String lat="";
    @ColumnInfo(name = "long")
    private String longValue="";
    @ColumnInfo(name = "geofence_timeStamp")
    private String geofence_timeStamp="";

    public PolygonEnt(String geofence_ID, String lat, String longValue, String geofence_timeStamp) {
        this.geofence_ID = geofence_ID;
        this.lat = lat;
        this.longValue = longValue;
        this.geofence_timeStamp = geofence_timeStamp;
    }

    public PolygonEnt() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGeofence_ID() {
        return geofence_ID;
    }

    public void setGeofence_ID(String geofence_ID) {
        this.geofence_ID = geofence_ID;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLongValue() {
        return longValue;
    }

    public void setLongValue(String longValue) {
        this.longValue = longValue;
    }

    public String getGeofence_timeStamp() {
        return geofence_timeStamp;
    }

    public void setGeofence_timeStamp(String geofence_timeStamp) {
        this.geofence_timeStamp = geofence_timeStamp;
    }




}
