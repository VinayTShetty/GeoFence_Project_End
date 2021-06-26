package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

public class VoAddGroupDataDelete implements Serializable {
    String response = "";
    String message = "";

    VoAddGroup data;

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

    public VoAddGroup getData() {
        return data;
    }

    public void setData(VoAddGroup data) {
        this.data = data;
    }
}
