package com.succorfish.depthntemp.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/*Table ble device getter and setter*/
@Entity(tableName = "tbl_ble_device")
public class TableBleDevice {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "device_id")
    private int deviceId;

    @ColumnInfo(name = "ble_address")
    private String bleAddress;
    @ColumnInfo(name = "device_name")
    private String deviceName;
    @ColumnInfo(name = "created_at")
    private String createdAt;
    @ColumnInfo(name = "updated_at")
    private String updatedAt;

    public TableBleDevice() {
    }

    public TableBleDevice(@NonNull int deviceId, String bleAddress, String deviceName, String createdAt, String updatedAt) {
        this.deviceId = deviceId;
        this.bleAddress = bleAddress;
        this.deviceName = deviceName;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(@NonNull int deviceId) {
        this.deviceId = deviceId;
    }

    public String getBleAddress() {
        return bleAddress;
    }

    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
