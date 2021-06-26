package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 27-02-2018.
 */
/* Login Response Data Getter Setter*/
public class VoLoginData implements Serializable {

    String response = "";
    String message = "";
    VoUserData data;

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

    public VoUserData getData() {
        return data;
    }

    public void setData(VoUserData data) {
        this.data = data;
    }
}
