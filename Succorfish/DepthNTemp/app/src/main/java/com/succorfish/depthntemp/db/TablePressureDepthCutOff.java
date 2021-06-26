package com.succorfish.depthntemp.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

/*Table pressure and depth cut off getter and setter*/
@Entity(tableName = "tbl_pre_depth_cut_off")
public class TablePressureDepthCutOff {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "pre_cutoff_id")
    private int cutOffId;
    @ColumnInfo(name = "pre_depth_meter")
    private double depthInMeter;

    @ColumnInfo(name = "pre_depth_milibar")
    private double depthInMillBar;

    public TablePressureDepthCutOff() {
    }

    public TablePressureDepthCutOff(@NonNull int cutOffId, double depthInMeter, double depthInMillBar) {
        this.cutOffId = cutOffId;
        this.depthInMeter = depthInMeter;
        this.depthInMillBar = depthInMillBar;
    }

    @NonNull
    public int getCutOffId() {
        return cutOffId;
    }

    public void setCutOffId(@NonNull int cutOffId) {
        this.cutOffId = cutOffId;
    }

    public double getDepthInMeter() {
        return depthInMeter;
    }

    public void setDepthInMeter(double depthInMeter) {
        this.depthInMeter = depthInMeter;
    }

    public double getDepthInMillBar() {
        return depthInMillBar;
    }

    public void setDepthInMillBar(double depthInMillBar) {
        this.depthInMillBar = depthInMillBar;
    }
}
