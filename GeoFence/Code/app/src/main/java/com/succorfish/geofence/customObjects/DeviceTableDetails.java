package com.succorfish.geofence.customObjects;

import java.io.Serializable;
public class DeviceTableDetails implements Serializable {
    String deviceName;
    String bleAddress;

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
}
