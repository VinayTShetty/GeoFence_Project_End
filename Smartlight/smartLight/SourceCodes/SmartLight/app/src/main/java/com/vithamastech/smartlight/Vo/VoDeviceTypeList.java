package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

/* Device Type List Response Getter Setter*/
public class VoDeviceTypeList implements Serializable {

    String response = "";
    String message = "";

    List<VoDeviceType> data;

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

    public List<VoDeviceType> getData() {
        return data;
    }

    public void setData(List<VoDeviceType> data) {
        this.data = data;
    }
}
