package com.vithamastech.smartlight.PowerSocketCustomObjects;

import java.io.Serializable;
import java.util.Objects;

public class WifiDevice implements Serializable {
    public String wifiSSID;
    public String wifiRSSI;
    public int deviceIndex;

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        WifiDevice that = (WifiDevice) o;
        return Objects.equals(wifiSSID, that.wifiSSID);
    }

    @Override
    public int hashCode() {
        return wifiSSID.hashCode();
    }

    @Override
    public String toString() {
        return "WifiDevice{" +
                "Wifi SSID='" + wifiSSID + '\'' +
                ", Wifi RSSI='" + wifiRSSI + '\'' +
                '}';
    }

    public String getWifiSSID() {
        return wifiSSID;
    }

    public void setWifiSSID(String wifiSSID) {
        this.wifiSSID = wifiSSID;
    }

    public String getWifiRSSI() {
        return wifiRSSI;
    }

    public void setWifiRSSI(String wifiRSSI) {
        this.wifiRSSI = wifiRSSI;
    }

    public int getDeviceIndex() {
        return deviceIndex;
    }

    public void setDeviceIndex(int deviceIndex) {
        this.deviceIndex = deviceIndex;
    }
}
