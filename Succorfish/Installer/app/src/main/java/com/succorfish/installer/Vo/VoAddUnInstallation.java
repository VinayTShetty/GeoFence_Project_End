package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 13-03-2018.
 */

public class VoAddUnInstallation implements Serializable {

    String response = "";
    String message = "";

    VoServerUnInstallation data;

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

    public VoServerUnInstallation getData() {
        return data;
    }

    public void setData(VoServerUnInstallation data) {
        this.data = data;
    }
}
