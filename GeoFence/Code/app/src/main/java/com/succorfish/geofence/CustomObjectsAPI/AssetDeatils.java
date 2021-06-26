package com.succorfish.geofence.CustomObjectsAPI;

import java.io.Serializable;

public class AssetDeatils implements Serializable {
    String deviceId = "";
    String filtered = "";
    String generated = "";
    String received = "";
    String source = "";
    String provider = "";
    String lng = "";
    String lat = "";
    String altitude = "";
    String speed = "";
    String course = "";
    String hdop = "";
    String vdop = "";
    String pdop = "";
    String lowPowerFlag = "";
    String notDuplicated = "";
    String satelliteFlag = "";
    String correlationId = "";
    String gpsSatNo = "";
    String generatedDate = "";

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFiltered() {
        return filtered;
    }

    public void setFiltered(String filtered) {
        this.filtered = filtered;
    }

    public String getGenerated() {
        return generated;
    }

    public void setGenerated(String generated) {
        this.generated = generated;
    }

    public String getReceived() {
        return received;
    }

    public void setReceived(String received) {
        this.received = received;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public String getSpeed() {
        return speed;
    }

    public void setSpeed(String speed) {
        this.speed = speed;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getHdop() {
        return hdop;
    }

    public void setHdop(String hdop) {
        this.hdop = hdop;
    }

    public String getVdop() {
        return vdop;
    }

    public void setVdop(String vdop) {
        this.vdop = vdop;
    }

    public String getPdop() {
        return pdop;
    }

    public void setPdop(String pdop) {
        this.pdop = pdop;
    }

    public String getLowPowerFlag() {
        return lowPowerFlag;
    }

    public void setLowPowerFlag(String lowPowerFlag) {
        this.lowPowerFlag = lowPowerFlag;
    }

    public String getNotDuplicated() {
        return notDuplicated;
    }

    public void setNotDuplicated(String notDuplicated) {
        this.notDuplicated = notDuplicated;
    }

    public String getSatelliteFlag() {
        return satelliteFlag;
    }

    public void setSatelliteFlag(String satelliteFlag) {
        this.satelliteFlag = satelliteFlag;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public String getGpsSatNo() {
        return gpsSatNo;
    }

    public void setGpsSatNo(String gpsSatNo) {
        this.gpsSatNo = gpsSatNo;
    }

    public String getGeneratedDate() {
        return generatedDate;
    }

    public void setGeneratedDate(String generatedDate) {
        this.generatedDate = generatedDate;
    }
}
