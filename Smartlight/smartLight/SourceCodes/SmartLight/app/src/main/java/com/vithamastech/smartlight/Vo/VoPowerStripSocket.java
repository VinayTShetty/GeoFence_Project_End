package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 05-02-2018.
 * Power Strip Socket Data Getter Setter
 */

public class VoPowerStripSocket implements Serializable {
    String socketLocalId = "";
    String socketId = "";
    String socketName = "";
    String socketIeee = "";
    String deviceId = "";
    String deviceHexId = "";
    String deviceName = "";
    String deviceBleAddress = "";
    String deviceType = "";
    String switchStatus = "ON";

    boolean isChecked = false;

    public String getSocketName() {
        return socketName;
    }

    public void setSocketName(String socketName) {
        this.socketName = socketName;
    }

    public String getSocketIeee() {
        return socketIeee;
    }

    public void setSocketIeee(String socketIeee) {
        this.socketIeee = socketIeee;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean checked) {
        isChecked = checked;
    }

    public String getSocketLocalId() {
        return socketLocalId;
    }

    public void setSocketLocalId(String socketLocalId) {
        this.socketLocalId = socketLocalId;
    }

    public String getSocketId() {
        return socketId;
    }

    public void setSocketId(String socketId) {
        this.socketId = socketId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceHexId() {
        return deviceHexId;
    }

    public void setDeviceHexId(String deviceHexId) {
        this.deviceHexId = deviceHexId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceBleAddress() {
        return deviceBleAddress;
    }

    public void setDeviceBleAddress(String deviceBleAddress) {
        this.deviceBleAddress = deviceBleAddress;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getSwitchStatus() {
        return switchStatus;
    }

    public void setSwitchStatus(String switchStatus) {
        this.switchStatus = switchStatus;
    }
}
