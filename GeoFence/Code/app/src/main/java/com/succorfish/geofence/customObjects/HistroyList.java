package com.succorfish.geofence.customObjects;

import java.io.Serializable;

public class HistroyList implements Serializable {
    private String brachMessage;
    private String dateTime;
    private String message_one;
    private String message_two;

    private String geoFenceType;
    private int geoFenceId;
    private double breachlatitude;
    private double breachLongitude;

    private String timeStamp;
    private String isRead;
    private String aliasName_forAlert;

    private String geoFenceTimStamp;



    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }



    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBrachMessage() {
        return brachMessage;
    }

    public void setBrachMessage(String brachMessage) {
        this.brachMessage = brachMessage;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public String getMessage_one() {
        return message_one;
    }

    public void setMessage_one(String message_one) {
        this.message_one = message_one;
    }

    public String getMessage_two() {
        return message_two;
    }

    public void setMessage_two(String message_two) {
        this.message_two = message_two;
    }


    public String getGeoFenceType() {
        return geoFenceType;
    }

    public void setGeoFenceType(String geoFenceType) {
        this.geoFenceType = geoFenceType;
    }

    public int getGeoFenceId() {
        return geoFenceId;
    }

    public void setGeoFenceId(int geoFenceId) {
        this.geoFenceId = geoFenceId;
    }

    public double getBreachlatitude() {
        return breachlatitude;
    }

    public void setBreachlatitude(double breachlatitude) {
        this.breachlatitude = breachlatitude;
    }

    public double getBreachLongitude() {
        return breachLongitude;
    }

    public void setBreachLongitude(double breachLongitude) {
        this.breachLongitude = breachLongitude;
    }

    public String getAliasName_forAlert() {
        return aliasName_forAlert;
    }

    public void setAliasName_forAlert(String aliasName_forAlert) {
        this.aliasName_forAlert = aliasName_forAlert;
    }

    public String getGeoFenceTimStamp() {
        return geoFenceTimStamp;
    }

    public void setGeoFenceTimStamp(String geoFenceTimStamp) {
        this.geoFenceTimStamp = geoFenceTimStamp;
    }

}
