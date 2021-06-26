package com.succorfish.depthntemp.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/*Table pressure and temperature getter and setter*/
@Entity(tableName = "tbl_pre_temp")
public class TablePressureTemperature {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "pre_temp_id")
    private int preTempId;
    @ColumnInfo(name = "pre_temp_dive_id")
    private int diveIdFk;
    @ColumnInfo(name = "pressure")
    private String pressure;
    @ColumnInfo(name = "pressure_depth")
    private String pressure_depth;
    @ColumnInfo(name = "temperature")
    private String temperature;
    @ColumnInfo(name = "temperature_far")
    private String temperature_far;
    @ColumnInfo(name = "utc_time")
    private long utcTime;
    @ColumnInfo(name = "full_packet")
    private String packets;
    @ColumnInfo(name = "created_at")
    private long createdAt;
    @ColumnInfo(name = "updated_at")
    private long updatedAt;

    public TablePressureTemperature() {
    }

    public TablePressureTemperature(@NonNull int preTempId, int diveIdFk, String pressure, String pressureDepth, String temperature, String temperatureFar, long utcTime, long createdAt, long updatedAt) {
        this.preTempId = preTempId;
        this.diveIdFk = diveIdFk;
        this.pressure = pressure;
        this.pressure_depth = pressureDepth;
        this.temperature = temperature;
        this.temperature_far = temperatureFar;
        this.utcTime = utcTime;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public int getPreTempId() {
        return preTempId;
    }

    public void setPreTempId(@NonNull int preTempId) {
        this.preTempId = preTempId;
    }

    public int getDiveIdFk() {
        return diveIdFk;
    }

    public void setDiveIdFk(int diveIdFk) {
        this.diveIdFk = diveIdFk;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getTemperature_far() {
        return temperature_far;
    }

    public void setTemperature_far(String temperature_far) {
        this.temperature_far = temperature_far;
    }

    public String getPressure_depth() {
        return pressure_depth;
    }

    public void setPressure_depth(String pressure_depth) {
        this.pressure_depth = pressure_depth;
    }

    public long getUtcTime() {
        return utcTime;
    }

    public void setUtcTime(long utcTime) {
        this.utcTime = utcTime;
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
