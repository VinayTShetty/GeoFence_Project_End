package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 23-12-2017.
 */
/* Add device API Response Getter setter*/
public class VoAddDeviceData implements Serializable {
    String response = "";
    String message = "";
    VoAddDevice data;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public VoAddDevice getData() {
        return data;
    }

    public void setData(VoAddDevice data) {
        this.data = data;
    }
}
