package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "NewChat")
public class ChatInfo implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "from_name")
    private String from_name;
    @ColumnInfo(name = "to_name")
    private String to_name;
    @ColumnInfo(name = "msg_txt")
    private String msg_txt;
    @ColumnInfo(name = "time")
    private String time;
    @ColumnInfo(name = "status")
    private String status;
    @ColumnInfo(name = "sequence")
    private String sequence;
    @ColumnInfo(name = "identifier")
    private String identifier;
    @ColumnInfo(name = "timeStamp")
    private String timeStamp;
    @ColumnInfo(name = "isGSM")
    private String isGSM;

    public ChatInfo(){

    }
    public ChatInfo(String from_name, String to_name, String msg_txt, String time, String status, String sequence, String identifier, String timeStamp, String isGSM) {
        this.from_name = from_name;
        this.to_name = to_name;
        this.msg_txt = msg_txt;
        this.time = time;
        this.status = status;
        this.sequence = sequence;
        this.identifier = identifier;
        this.timeStamp = timeStamp;
        this.isGSM = isGSM;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFrom_name() {
        return from_name;
    }

    public void setFrom_name(String from_name) {
        this.from_name = from_name;
    }

    public String getTo_name() {
        return to_name;
    }

    public void setTo_name(String to_name) {
        this.to_name = to_name;
    }

    public String getMsg_txt() {
        return msg_txt;
    }

    public void setMsg_txt(String msg_txt) {
        this.msg_txt = msg_txt;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSequence() {
        return sequence;
    }

    public void setSequence(String sequence) {
        this.sequence = sequence;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getIsGSM() {
        return isGSM;
    }

    public void setIsGSM(String isGSM) {
        this.isGSM = isGSM;
    }
}
