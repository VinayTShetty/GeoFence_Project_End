package com.succorfish.installer.Vo;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Jaydeep on 28-02-2018.
 */

public class VoVesselList implements Serializable {

    String response = "";
    String message = "";
    ArrayList<VoVessel> data;

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

    public ArrayList<VoVessel> getData() {
        return data;
    }

    public void setData(ArrayList<VoVessel> data) {
        this.data = data;
    }
}
