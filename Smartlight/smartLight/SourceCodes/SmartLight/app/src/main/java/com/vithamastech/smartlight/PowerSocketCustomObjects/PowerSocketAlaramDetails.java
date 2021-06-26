package com.vithamastech.smartlight.PowerSocketCustomObjects;

/**
 * This class is used to fetch all alaram details when we connect to the application.And ask for alaram details.
 * It will send all 12 scokets alaram details.
 *
 */
public class PowerSocketAlaramDetails {
    private byte alarmaId;
    private byte socketId;
    private byte daybyte; //dayValue--->In DataBase
    private int onTime;   //onTimeStamp
    private int offTime; //offTimeStamp
    private byte alaramState;
    // For Database
    private String daysInBinary;
    private String bleAddress;
    private String onOriginal;
    private String offOriginal;

    public PowerSocketAlaramDetails(byte alarmaId, byte socketId, byte daybyte, int onTimeStamp, int offTimeStamp, byte alaramState, String binayryDays, String bleAddress, String onOriginal, String offOriginal) {
        this.alarmaId = alarmaId;
        this.socketId = socketId;
        this.daybyte = daybyte;
        this.onTime = onTimeStamp;
        this.offTime = offTimeStamp;
        this.alaramState = alaramState;
        this.daysInBinary = binayryDays;
        this.bleAddress = bleAddress;
        this.onOriginal = onOriginal;
        this.offOriginal = offOriginal;
    }

    public PowerSocketAlaramDetails() {
    }

    public byte getAlarmaId() {
        return alarmaId;
    }

    public void setAlarmaId(byte alarmaId) {
        this.alarmaId = alarmaId;
    }

    public byte getSocketId() {
        return socketId;
    }

    public void setSocketId(byte socketId) {
        this.socketId = socketId;
    }

    public byte getDaybyte() {
        return daybyte;
    }

    public void setDaybyte(byte daybyte) {
        this.daybyte = daybyte;
    }

    public int getOnTime() {
        return onTime;
    }

    public void setOnTime(int onTime) {
        this.onTime = onTime;
    }

    public int getOffTime() {
        return offTime;
    }

    public void setOffTime(int offTime) {
        this.offTime = offTime;
    }

    public byte getAlaramState() {
        return alaramState;
    }

    public void setAlaramState(byte alaramState) {
        this.alaramState = alaramState;
    }

    public String getDaysInBinary() {
        return daysInBinary;
    }

    public void setDaysInBinary(String daysInBinary) {
        this.daysInBinary = daysInBinary;
    }

    public String getBleAddress() {
        return bleAddress;
    }

    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }

    public String getOnOriginal() {
        return onOriginal;
    }

    public void setOnOriginal(String onOriginal) {
        this.onOriginal = onOriginal;
    }

    public String getOffOriginal() {
        return offOriginal;
    }

    public void setOffOriginal(String offOriginal) {
        this.offOriginal = offOriginal;
    }
}
