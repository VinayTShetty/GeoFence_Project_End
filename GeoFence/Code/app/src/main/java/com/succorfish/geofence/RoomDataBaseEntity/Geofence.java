package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Geofence")
public class Geofence implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "name")
    private String name="";

    @ColumnInfo(name = "geofence_ID")
    private String geofence_ID="";

    @ColumnInfo(name = "type")
    private String type="";

    @ColumnInfo(name = "lat")
    private String lat="";

    @ColumnInfo(name = "long")
    private String longValue="";

    @ColumnInfo(name = "radiusOrvertices")
    private String radiusOrvertices="";

    @ColumnInfo(name = "number_of_rules")
    private String number_of_rules="";

    @ColumnInfo(name = "gsm_reporting")
    private String gsm_reporting="";

    @ColumnInfo(name = "iridium_reporting")
    private String iridium_reporting="";

    @ColumnInfo(name = "is_active")
    private String is_active="";

    @ColumnInfo(name = "firmware_timestamp")
    private String firmware_timestamp="";

    public Geofence(String name, String geofence_ID, String type, String lat, String longValue, String radiusOrvertices, String number_of_rules, String gsm_reporting, String iridium_reporting, String is_active, String firmware_timestamp) {
        this.name = name;
        this.geofence_ID = geofence_ID;
        this.type = type;
        this.lat = lat;
        this.longValue = longValue;
        this.radiusOrvertices = radiusOrvertices;
        this.number_of_rules = number_of_rules;
        this.gsm_reporting = gsm_reporting;
        this.iridium_reporting = iridium_reporting;
        this.is_active = is_active;
        this.firmware_timestamp = firmware_timestamp;
    }

    public Geofence() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGeofence_ID() {
        return geofence_ID;
    }

    public void setGeofence_ID(String geofence_ID) {
        this.geofence_ID = geofence_ID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

    public String getRadiusOrvertices() {
        return radiusOrvertices;
    }

    public void setRadiusOrvertices(String radiusOrvertices) {
        this.radiusOrvertices = radiusOrvertices;
    }

    public String getNumber_of_rules() {
        return number_of_rules;
    }

    public void setNumber_of_rules(String number_of_rules) {
        this.number_of_rules = number_of_rules;
    }

    public String getGsm_reporting() {
        return gsm_reporting;
    }

    public void setGsm_reporting(String gsm_reporting) {
        this.gsm_reporting = gsm_reporting;
    }

    public String getIridium_reporting() {
        return iridium_reporting;
    }

    public void setIridium_reporting(String iridium_reporting) {
        this.iridium_reporting = iridium_reporting;
    }

    public String getIs_active() {
        return is_active;
    }

    public void setIs_active(String is_active) {
        this.is_active = is_active;
    }

    public String getFirmware_timestamp() {
        return firmware_timestamp;
    }

    public void setFirmware_timestamp(String firmware_timestamp) {
        this.firmware_timestamp = firmware_timestamp;
    }
}
