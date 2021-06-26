package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 14-03-2018.
 */

public class VoAddPhotoInspection implements Serializable {

    String response = "";
    String message = "";

    VoServerInspectionPhoto data;

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

    public VoServerInspectionPhoto getData() {
        return data;
    }

    public void setData(VoServerInspectionPhoto data) {
        this.data = data;
    }
}
