package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 15-03-2018.
 */

public class VoScanDeviceData implements Serializable {

    String response = "";
    String message = "";

    VoServerInstallation data;

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

    public VoServerInstallation getData() {
        return data;
    }

    public void setData(VoServerInstallation data) {
        this.data = data;
    }
}
