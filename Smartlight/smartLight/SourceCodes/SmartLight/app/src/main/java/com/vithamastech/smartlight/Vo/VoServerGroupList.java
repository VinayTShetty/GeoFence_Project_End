package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

/*Api Service response group getter setter*/
public class VoServerGroupList implements Serializable {

    String response = "";
    String message = "";
    List<VoAddGroup> group;

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

    public List<VoAddGroup> getGroup() {
        return group;
    }

    public void setGroup(List<VoAddGroup> group) {
        this.group = group;
    }
}
