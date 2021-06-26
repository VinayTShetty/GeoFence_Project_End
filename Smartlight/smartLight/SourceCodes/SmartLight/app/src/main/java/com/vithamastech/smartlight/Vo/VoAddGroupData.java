package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

/* Add Group Getter Setter*/
public class VoAddGroupData implements Serializable {
    String response = "";
    String message = "";

    List<VoAddGroup> data;

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

    public List<VoAddGroup> getData() {
        return data;
    }

    public void setData(List<VoAddGroup> data) {
        this.data = data;
    }
}
