package com.succorfish.geofence.customObjects;

import java.util.ArrayList;

public class MessageData {
    private ArrayList<byte [] > messageDataArray;
    public MessageData(ArrayList<byte[]> loc_messageDataArray){
        this.messageDataArray=loc_messageDataArray;
    }

    public ArrayList<byte[]> getMessageDataArray() {
        return messageDataArray;
    }

    public void setMessageDataArray(ArrayList<byte[]> messageDataArray) {
        this.messageDataArray = messageDataArray;
    }
}
