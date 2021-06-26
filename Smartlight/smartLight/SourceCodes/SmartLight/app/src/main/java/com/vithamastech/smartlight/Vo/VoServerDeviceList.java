package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Jaydeep on 09-04-2018.
 * Api response Device List data getter setter
 */

public class VoServerDeviceList implements Serializable {

    String response = "";
    List<VoAddDevice> data;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public List<VoAddDevice> getData() {
        return data;
    }

    public void setData(List<VoAddDevice> data) {
        this.data = data;
    }
}
