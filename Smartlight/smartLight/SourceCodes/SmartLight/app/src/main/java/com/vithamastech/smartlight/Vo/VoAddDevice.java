package com.vithamastech.smartlight.Vo;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Jaydeep on 09-04-2018.
 */

/* Add device Getter setter*/
public class VoAddDevice implements Serializable {

    String server_device_id = "";
    String user_id = "";
    String device_id = "";
    String hex_device_id = "";
    String device_name = "";
    String ble_address = "";
    String device_type = "";
    String device_type_name = "";
    String device_type_value = "";
    String remember_last_color = "";
    String created_date = "";
    String updated_date = "";
    String is_favourite = "";
    String status = "";

    //    List<String> strip_details;
    // Group Device
    String device_group_details_id = "";
    String group_id = "";
    int wifi_configured;

    public String getServer_device_id() {
        return server_device_id;
    }

    public void setServer_device_id(String server_device_id) {
        this.server_device_id = server_device_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

    public String getHex_device_id() {
        return hex_device_id;
    }

    public void setHex_device_id(String hex_device_id) {
        this.hex_device_id = hex_device_id;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getBle_address() {
        return ble_address;
    }

    public void setBle_address(String ble_address) {
        this.ble_address = ble_address;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getDevice_type_name() {
        return device_type_name;
    }

    public void setDevice_type_name(String device_type_name) {
        this.device_type_name = device_type_name;
    }

    public String getIs_favourite() {
        return is_favourite;
    }

    public void setIs_favourite(String is_favourite) {
        this.is_favourite = is_favourite;
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

    public String getDevice_group_details_id() {
        return device_group_details_id;
    }

    public void setDevice_group_details_id(String device_group_details_id) {
        this.device_group_details_id = device_group_details_id;
    }

    public String getGroup_id() {
        return group_id;
    }

    public void setGroup_id(String group_id) {
        this.group_id = group_id;
    }

    //    public List<String> getStrip_details() {
//        return strip_details;
//    }
//
//    public void setStrip_details(List<String> strip_details) {
//        this.strip_details = strip_details;
//    }

    public String getRemember_last_color() {
        return remember_last_color;
    }

    public void setRemember_last_color(String remember_last_color) {
        this.remember_last_color = remember_last_color;
    }

    public int getWifi_configured() {
        return wifi_configured;
    }

    public void setWifi_configured(int wifi_configured) {
        this.wifi_configured = wifi_configured;
    }
}
