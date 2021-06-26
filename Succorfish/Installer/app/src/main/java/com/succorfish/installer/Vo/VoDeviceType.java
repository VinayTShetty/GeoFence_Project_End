package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 26-02-2018.
 */

public class VoDeviceType implements Serializable {

    String device_local_id = "";
    String device_server_id = "";
    String device_type_name = "";
    String device_type_imei = "";
    String is_checked = "";

    public String getDevice_local_id() {
        return device_local_id;
    }

    public void setDevice_local_id(String device_local_id) {
        this.device_local_id = device_local_id;
    }

    public String getDevice_server_id() {
        return device_server_id;
    }

    public void setDevice_server_id(String device_server_id) {
        this.device_server_id = device_server_id;
    }

    public String getDevice_type_name() {
        return device_type_name;
    }

    public void setDevice_type_name(String device_type_name) {
        this.device_type_name = device_type_name;
    }

    public String getDevice_type_imei() {
        return device_type_imei;
    }

    public void setDevice_type_imei(String device_type_imei) {
        this.device_type_imei = device_type_imei;
    }

    public String getIs_checked() {
        return is_checked;
    }

    public void setIs_checked(String is_checked) {
        this.is_checked = is_checked;
    }
}
