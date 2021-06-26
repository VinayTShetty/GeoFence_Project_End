package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Geofence_alert_Table")
public class GeofenceAlert implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "geofence_ID")
    private String geofence_ID="";
    @ColumnInfo(name = "Geo_name")
    private String Geo_name="";
    @ColumnInfo(name = "Geo_Type")
    private String Geo_Type="";
    @ColumnInfo(name = "Breach_Type")
    private String Breach_Type="";
    @ColumnInfo(name = "Breach_Lat")
    private String Breach_Lat="";
    @ColumnInfo(name = "Breach_Long")
    private String Breach_Long="";
    @ColumnInfo(name = "BreachRule_ID")
    private String BreachRule_ID="";
    @ColumnInfo(name = "BreachRuleValue")
    private String BreachRuleValue="";
    @ColumnInfo(name = "date_Time")
    private String date_Time="";
    @ColumnInfo(name = "timeStamp")
    private String timeStamp="";
    @ColumnInfo(name = "Rule_Name")
    private String Rule_Name="";
    @ColumnInfo(name = "is_Read")
    private String is_Read="";
    @ColumnInfo(name = "OriginalRuleValue")
    private String OriginalRuleValue="";
    @ColumnInfo(name = "bleAddress")
    private String bleAddress="";
    @ColumnInfo(name = "Message_one")
    private String Message_one="";
    @ColumnInfo(name = "Message_two")
    private String Message_two="";
    @ColumnInfo(name = "alias_name_alert")
    private String alias_name_alert="";
    @ColumnInfo(name = "geoFence_timestamp")
    private String geoFence_timestamp="";
    @ColumnInfo(name = "geoFence_lat")
    private String geoFence_lat="";
    @ColumnInfo(name = "geoFence_long")
    private String geoFence_long="";
    @ColumnInfo(name = "geoFence_radius_vertices")
    private String geoFence_radius_vertices="";

    public GeofenceAlert(String geofence_ID, String geo_name, String geo_Type, String breach_Type, String breach_Lat, String breach_Long, String breachRule_ID, String breachRuleValue, String date_Time, String timeStamp, String rule_Name, String is_Read, String originalRuleValue, String bleAddress, String message_one, String message_two, String alias_name_alert, String geoFence_timestamp, String geoFence_lat, String geoFence_long, String geoFence_radius_vertices) {
        this.geofence_ID = geofence_ID;
        this.Geo_name = geo_name;
        this.Geo_Type = geo_Type;
        this.Breach_Type = breach_Type;
        this.Breach_Lat = breach_Lat;
        this.Breach_Long = breach_Long;
        this.BreachRule_ID = breachRule_ID;
        this.BreachRuleValue = breachRuleValue;
        this.date_Time = date_Time;
        this.timeStamp = timeStamp;
        this.Rule_Name = rule_Name;
        this.is_Read = is_Read;
        this.OriginalRuleValue = originalRuleValue;
        this.bleAddress = bleAddress;
        this.Message_one = message_one;
        this.Message_two = message_two;
        this.alias_name_alert = alias_name_alert;
        this.geoFence_timestamp = geoFence_timestamp;
        this.geoFence_lat = geoFence_lat;
        this.geoFence_long = geoFence_long;
        this.geoFence_radius_vertices = geoFence_radius_vertices;
    }

    public GeofenceAlert() {
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

    public String getGeo_name() {
        return Geo_name;
    }

    public void setGeo_name(String geo_name) {
        Geo_name = geo_name;
    }

    public String getGeo_Type() {
        return Geo_Type;
    }

    public void setGeo_Type(String geo_Type) {
        Geo_Type = geo_Type;
    }

    public String getBreach_Type() {
        return Breach_Type;
    }

    public void setBreach_Type(String breach_Type) {
        Breach_Type = breach_Type;
    }

    public String getBreach_Lat() {
        return Breach_Lat;
    }

    public void setBreach_Lat(String breach_Lat) {
        Breach_Lat = breach_Lat;
    }

    public String getBreach_Long() {
        return Breach_Long;
    }

    public void setBreach_Long(String breach_Long) {
        Breach_Long = breach_Long;
    }

    public String getBreachRule_ID() {
        return BreachRule_ID;
    }

    public void setBreachRule_ID(String breachRule_ID) {
        BreachRule_ID = breachRule_ID;
    }

    public String getBreachRuleValue() {
        return BreachRuleValue;
    }

    public void setBreachRuleValue(String breachRuleValue) {
        BreachRuleValue = breachRuleValue;
    }

    public String getDate_Time() {
        return date_Time;
    }

    public void setDate_Time(String date_Time) {
        this.date_Time = date_Time;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getRule_Name() {
        return Rule_Name;
    }

    public void setRule_Name(String rule_Name) {
        Rule_Name = rule_Name;
    }

    public String getIs_Read() {
        return is_Read;
    }

    public void setIs_Read(String is_Read) {
        this.is_Read = is_Read;
    }

    public String getOriginalRuleValue() {
        return OriginalRuleValue;
    }

    public void setOriginalRuleValue(String originalRuleValue) {
        OriginalRuleValue = originalRuleValue;
    }

    public String getBleAddress() {
        return bleAddress;
    }

    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }

    public String getMessage_one() {
        return Message_one;
    }

    public void setMessage_one(String message_one) {
        Message_one = message_one;
    }

    public String getMessage_two() {
        return Message_two;
    }

    public void setMessage_two(String message_two) {
        Message_two = message_two;
    }

    public String getAlias_name_alert() {
        return alias_name_alert;
    }

    public void setAlias_name_alert(String alias_name_alert) {
        this.alias_name_alert = alias_name_alert;
    }

    public String getGeoFence_timestamp() {
        return geoFence_timestamp;
    }

    public void setGeoFence_timestamp(String geoFence_timestamp) {
        this.geoFence_timestamp = geoFence_timestamp;
    }

    public String getGeoFence_lat() {
        return geoFence_lat;
    }

    public void setGeoFence_lat(String geoFence_lat) {
        this.geoFence_lat = geoFence_lat;
    }

    public String getGeoFence_long() {
        return geoFence_long;
    }

    public void setGeoFence_long(String geoFence_long) {
        this.geoFence_long = geoFence_long;
    }

    public String getGeoFence_radius_vertices() {
        return geoFence_radius_vertices;
    }

    public void setGeoFence_radius_vertices(String geoFence_radius_vertices) {
        this.geoFence_radius_vertices = geoFence_radius_vertices;
    }
}
