package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/* Logout Data Getter Setter*/
public class VoLogout implements Serializable {
    String response = "";
    String message = "";

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

}
