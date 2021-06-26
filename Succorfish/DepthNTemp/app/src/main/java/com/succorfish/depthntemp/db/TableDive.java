package com.succorfish.depthntemp.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/*Table dive getter and setter*/
@Entity(tableName = "tbl_dive")
public class TableDive {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "dive_id")
    private int diveId;
    @ColumnInfo(name = "ble_address")
    private String bleAddress;
    @ColumnInfo(name = "device_name")
    private String deviceName;
    @ColumnInfo(name = "dive_no")
    private int diveNo;
    @ColumnInfo(name = "utc_time")
    private long utcTime;
    @ColumnInfo(name = "gps_latitude")
    private String gpsLatitude;
    @ColumnInfo(name = "gps_longitude")
    private String gpsLongitude;
    @ColumnInfo(name = "stationary_interval")
    private int stationaryInterval;
    @ColumnInfo(name = "moving_interval")
    private int movingInterval;
    @ColumnInfo(name = "full_packets")
    private String packets;
    @ColumnInfo(name = "created_at")
    private long createdAt;
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public TableDive() {

    }

    public TableDive(@NonNull int diveId, String deviceName, String bleAddress, int diveNo, long utcTime, String gpsLatitude, String gpsLongitude, int statInterval, int movInterval, long createdAt, long updatedAt) {
        this.diveId = diveId;
        this.deviceName = deviceName;
        this.bleAddress = bleAddress;
        this.diveNo = diveNo;
        this.utcTime = utcTime;
        this.gpsLatitude = gpsLatitude;
        this.gpsLongitude = gpsLongitude;
        this.stationaryInterval = statInterval;
        this.movingInterval = movInterval;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    @NonNull
    public int getDiveId() {
        return diveId;
    }

    public void setDiveId(@NonNull int diveId) {
        this.diveId = diveId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getBleAddress() {
        return bleAddress;
    }

    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }

    public int getDiveNo() {
        return diveNo;
    }

    public void setDiveNo(int diveNo) {
        this.diveNo = diveNo;
    }

    public long getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(long utcTime) {
        this.utcTime = utcTime;
    }

    public String getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(String gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public String getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(String gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    public int getStationaryInterval() {
        return stationaryInterval;
    }

    public void setStationaryInterval(int stationaryInterval) {
        this.stationaryInterval = stationaryInterval;
    }

    public int getMovingInterval() {
        return movingInterval;
    }

    public void setMovingInterval(int movingInterval) {
        this.movingInterval = movingInterval;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getPackets() {
        return packets;
    }

    public void setPackets(String packets) {
        this.packets = packets;
    }
}
