package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "Action_info_Table")
public class Action_info implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "action")
    private String action="";
    @ColumnInfo(name = "action_ID")
    private String action_ID="";
    @ColumnInfo(name = "description")
    private String description="";

    public Action_info(){

    }
    public Action_info(String action_loc,String action_ID_loc,String description_loc){
        this.action=action_loc;
        this.action_ID=action_ID_loc;
        this.description=description_loc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction_ID() {
        return action_ID;
    }

    public void setAction_ID(String action_ID) {
        this.action_ID = action_ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
