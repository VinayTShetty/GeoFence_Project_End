package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;


@Entity(tableName = "Rules_Table")
public class Rules implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "name")
    private String name="";
    @ColumnInfo(name = "geofence_ID")
    private String geofence_ID="";
    @ColumnInfo(name = "rule_ID")
    private String rule_ID="";
    @ColumnInfo(name = "rule_value")
    private String rule_value="";

    public Rules(String name, String geofence_ID, String rule_ID, String rule_value) {
        this.name = name;
        this.geofence_ID = geofence_ID;
        this.rule_ID = rule_ID;
        this.rule_value = rule_value;
    }

    public Rules() {
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

    public String getRule_ID() {
        return rule_ID;
    }

    public void setRule_ID(String rule_ID) {
        this.rule_ID = rule_ID;
    }

    public String getRule_value() {
        return rule_value;
    }

    public void setRule_value(String rule_value) {
        this.rule_value = rule_value;
    }
}
