package com.succorfish.geofence.customObjects;

import java.io.Serializable;

public class SimDetails implements Serializable {
    private String simDetails;
    private boolean isChecked;
    private byte simValue;
    public  SimDetails(String loc_simDetails,boolean loc_isChecked,byte loc_simValue){
        this.simDetails=loc_simDetails;
        this.isChecked=loc_isChecked;
        this.simValue=loc_simValue;
    }

    public String getSimDetails() {
        return simDetails;
    }

    public void setSimDetails(String simDetails) {
        this.simDetails = simDetails;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public byte getSimValue() {
        return simValue;
    }

    public void setSimValue(byte simValue) {
        this.simValue = simValue;
    }
}
