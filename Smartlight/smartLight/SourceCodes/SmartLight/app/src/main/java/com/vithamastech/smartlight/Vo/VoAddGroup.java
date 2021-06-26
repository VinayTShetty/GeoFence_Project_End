package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

/* Add Device Group Getter Setter*/
public class VoAddGroup implements Serializable {

    String device_group_id = "";
    String group_name = "";
    String user_id = "";
    String local_group_id = "";
    String local_group_hex_id = "";
    String created_date = "";
    String updated_date = "";
    String is_favourite = "";
    String status = "";

    List<VoAddDevice> devices;
    List<VoAddDevice> device_details;


    public String getDevice_group_id() {
        return device_group_id;
    }

    public void setDevice_group_id(String device_group_id) {
        this.device_group_id = device_group_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getLocal_group_id() {
        return local_group_id;
    }

    public void setLocal_group_id(String local_group_id) {
        this.local_group_id = local_group_id;
    }

    public String getLocal_group_hex_id() {
        return local_group_hex_id;
    }

    public void setLocal_group_hex_id(String local_group_hex_id) {
        this.local_group_hex_id = local_group_hex_id;
    }

    public String getCreated_date() {
        return created_date;
    }

    public void setCreated_date(String created_date) {
        this.created_date = created_date;
    }

    public String getUpdated_date() {
        return updated_date;
    }

    public void setUpdated_date(String updated_date) {
        this.updated_date = updated_date;
    }

    public String getIs_favourite() {
        return is_favourite;
    }

    public void setIs_favourite(String is_favourite) {
        this.is_favourite = is_favourite;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<VoAddDevice> getDevices() {
        return devices;
    }

    public void setDevices(List<VoAddDevice> devices) {
        this.devices = devices;
    }

    public List<VoAddDevice> getDevice_details() {
        return device_details;
    }

    public void setDevice_details(List<VoAddDevice> device_details) {
        this.device_details = device_details;
    }
}
