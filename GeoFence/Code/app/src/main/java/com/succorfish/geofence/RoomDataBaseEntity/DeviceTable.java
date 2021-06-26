package com.succorfish.geofence.RoomDataBaseEntity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;

@Entity(tableName = "tbl_Device")
public class DeviceTable implements Serializable {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "BLE_Address")
    private String BLE_Address="";
    @ColumnInfo(name = "name")
    private String name="";
    @ColumnInfo(name = "imei")
    private String imei="";
    @ColumnInfo(name = "device_token")
    private String device_token="";
    @ColumnInfo(name = "is_active")
    private String is_active="";

    public DeviceTable(String BLE_Address, String name, String imei, String device_token, String is_active) {
        this.BLE_Address = BLE_Address;
        this.name = name;
        this.imei = imei;
        this.device_token = device_token;
        this.is_active = is_active;
    }

    public DeviceTable() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBLE_Address() {
        return BLE_Address;
    }

    public void setBLE_Address(String BLE_Address) {
        this.BLE_Address = BLE_Address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getIs_active() {
        return is_active;
    }

    public void setIs_active(String is_active) {
        this.is_active = is_active;
    }
}
