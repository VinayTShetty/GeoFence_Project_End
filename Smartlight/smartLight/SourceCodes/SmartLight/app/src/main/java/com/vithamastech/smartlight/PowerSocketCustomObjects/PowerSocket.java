package com.vithamastech.smartlight.PowerSocketCustomObjects;

import androidx.annotation.Nullable;

import com.vithamastech.smartlight.Vo.VoDeviceList;

import java.io.Serializable;

public class PowerSocket implements Serializable {
    public int id;
    public String bleName;
    public String bleAddress;
    public boolean isWifiConfigured;
    public boolean isAssociated;
    public int deviceType;
    public String addButton;

    public PowerSocket() {
    }

    public PowerSocket(VoDeviceList device) {
        String deviceAddress = device.getDevice_BleAddress();
        deviceAddress = deviceAddress.replaceAll("(\\w{2})(?!$)", "$1:");
        this.id = Integer.parseInt(device.getDevicLocalId());
        this.bleName = device.getDevice_name();
        this.bleAddress = deviceAddress;
        this.deviceType = Integer.parseInt(device.getDevice_Type(), 16);
        this.isAssociated = true;
        this.isWifiConfigured = device.getIsWifiConfigured();
    }

    public PowerSocket(String bleName, String bleAddress) {
        this.bleName = bleName;
        this.bleAddress = bleAddress;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isWifiConfigured() {
        return isWifiConfigured;
    }

    public void setWifiConfigured(boolean wifiConfigured) {
        isWifiConfigured = wifiConfigured;
    }

    public boolean isAssociated() {
        return isAssociated;
    }

    public void setAssociated(boolean associated) {
        isAssociated = associated;
    }

    public String getBleName() {
        return bleName;
    }

    public void setBleName(String bleName) {
        this.bleName = bleName;
    }

    public String getAddButton() {
        return addButton;
    }

    public void setAddButton(String addButton) {
        this.addButton = addButton;
    }

    public String getBleAddress() {
        return bleAddress;
    }

    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof PowerSocket) {
            return this.bleAddress.equalsIgnoreCase(((PowerSocket) obj).bleAddress);
        } else if (obj instanceof String) {
            return this.bleAddress.equalsIgnoreCase((String) obj);
        }
        return false;
    }
}