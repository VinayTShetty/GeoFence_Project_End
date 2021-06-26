package com.succorfish.installer.Vo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jaydeep on 15-03-2018.
 */

public class VoInstallationHistory implements Serializable {

    String response = "";
    String message = "";
    List<VoServerInstallation> data;

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

    public List<VoServerInstallation> getData() {
        return data;
    }

    public void setData(ArrayList<VoServerInstallation> data) {
        this.data = data;
    }
}
