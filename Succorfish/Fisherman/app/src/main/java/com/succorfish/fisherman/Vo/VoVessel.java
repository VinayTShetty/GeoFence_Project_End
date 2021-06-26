package com.succorfish.fisherman.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 28-02-2018.
 */

public class VoVessel implements Serializable {

    String id = "";
    String name = "";
    String registry = "";
    String port = "";
    String regNo = "";
    String accountId = "";
    String type = "";
    String disabled = "";
    String imageId = "";
    String system = "";
    String deviceId = "";

    //
    int status = 0;
    //    VoLastInstallation mVoLastInstallation;
    String lastInstallatDate = "";
    String lng = "";
    String lat = "";

    // Event Service Use Only
    String battery = "";
    String powerApply = "";
    String gearOut = "";
    String rfid = "";
    String gigoType = "0";
    String batteryPercentage = "0";
    String powerSourceType = "X00";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegistry() {
        return registry;
    }

    public void setRegistry(String registry) {
        this.registry = registry;
    }

    public String getVessel_port_no() {
        return port;
    }

    public void setVessel_port_no(String vessel_port_no) {
        this.port = vessel_port_no;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDisabled() {
        return disabled;
    }

    public void setDisabled(String disabled) {
        this.disabled = disabled;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

//    public VoLastInstallation getLastInstallation() {
//        return mVoLastInstallation;
//    }
//
//    public void setLastInstallation(VoLastInstallation mVoLastInstallation) {
//        this.mVoLastInstallation = mVoLastInstallation;
//    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLastInstallatDate() {
        return lastInstallatDate;
    }

    public void setLastInstallatDate(String lastInstallatDate) {
        this.lastInstallatDate = lastInstallatDate;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getBattery() {
        return battery;
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public String getPowerApply() {
        return powerApply;
    }

    public void setPowerApply(String powerApply) {
        this.powerApply = powerApply;
    }

    public String getGearOut() {
        return gearOut;
    }

    public void setGearOut(String gearOut) {
        this.gearOut = gearOut;
    }

    public String getRfid() {
        return rfid;
    }

    public void setRfid(String rfid) {
        this.rfid = rfid;
    }

    public String getGigoType() {
        return gigoType;
    }

    public void setGigoType(String gigoType) {
        this.gigoType = gigoType;
    }

    public String getPowerSourceType() {
        return powerSourceType;
    }

    public void setPowerSourceType(String powerSourceType) {
        this.powerSourceType = powerSourceType;
    }

    public String getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(String batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }
}
