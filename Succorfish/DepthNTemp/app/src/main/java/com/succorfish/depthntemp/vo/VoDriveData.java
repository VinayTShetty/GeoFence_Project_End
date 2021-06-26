package com.succorfish.depthntemp.vo;

import java.io.Serializable;

/*Dive data getter setter*/
public class VoDriveData implements Serializable {

    String driveName = "";
    String time = "";
    String depth = "";
    String temp = "";

    public String getDriveName() {
        return driveName;
    }

    public void setDriveName(String driveName) {
        this.driveName = driveName;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDepth() {
        return depth;
    }

    public void setDepth(String depth) {
        this.depth = depth;
    }

    public String getTemp() {
        return temp;
    }

    public void setTemp(String temp) {
        this.temp = temp;
    }
}
