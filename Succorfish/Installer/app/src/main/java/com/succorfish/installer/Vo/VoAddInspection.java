package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 13-03-2018.
 */

public class VoAddInspection implements Serializable {

    String response = "";
    String message = "";

    VoServerInspection data;

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

    public VoServerInspection getData() {
        return data;
    }

    public void setData(VoServerInspection data) {
        this.data = data;
    }
}
