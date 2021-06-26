package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 28-02-2018.
 */

public class VoChangePassword implements Serializable {

    String response = "";
    String message = "";
    String user_id = "";
    String data = "";

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

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
