package com.succorfish.geofence.customObjects;

public class ChattingObject {


    private String mode;
    private String message;
    private String date;
    private String delivery_status;
    private String timeStamp;
    private String time_chat;
    private String sequenceNumber;
    private String bleAddress;

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getTimeStamp() {
        return timeStamp;
    }
    public String getTime_chat() {
        return time_chat;
    }
    public void setTime_chat(String time_chat) {
        this.time_chat = time_chat;
    }
    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMessage() {
        return message;
    }


    public void setMessage(String message) {
        this.message = message;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getDelivery_status() {
        return delivery_status;
    }
    public void setDelivery_status(String delivery_status) {
        this.delivery_status = delivery_status;
    }
    public String getSequenceNumber() {
        return sequenceNumber;
    }
    public void setSequenceNumber(String sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }
    public String getBleAddress() {
        return bleAddress;
    }
    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }
}
