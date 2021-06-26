package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/* Device Type Data Getter Setter*/
public class VoDeviceType implements Serializable {

    String id = "";
    String device_type_name = "";
    String device_type_value = "";
    String status = "";
    String created_date = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDevice_type_name() {
        return device_type_name;
    }

    public void setDevice_type_name(String device_type_name) {
        this.device_type_name = device_type_name;
    }

    public String getDevice_type_value() {
        return device_type_value;
    }

    public void setDevice_type_value(String device_type_value) {
        this.device_type_value = device_type_value;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }
}
