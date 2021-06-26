package com.succorfish.combatdiver.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 20-01-2018.
 */

public class VoAddressBook implements Serializable {

    String id = "";
    String user_id = "";
    String user_name = "";
    String user_photo = "";
    String user_type = "";
    String device_id = "";
    String last_msg_id = "";
    String last_msg_name = "";
    String last_msg_time = "";
    String created_date = "";
    String updated_date = "";
    String is_sync = "";

    // Dashboard
    String air_life_percentage = "";


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_photo() {
        return user_photo;
    }

    public void setUser_photo(String user_photo) {
        this.user_photo = user_photo;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
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

    public String getIs_sync() {
        return is_sync;
    }

    public void setIs_sync(String is_sync) {
        this.is_sync = is_sync;
    }

    public String getAir_life_percentage() {
        return air_life_percentage;
    }

    public void setAir_life_percentage(String air_life_percentage) {
        this.air_life_percentage = air_life_percentage;
    }

    public String getLast_msg_id() {
        return last_msg_id;
    }

    public void setLast_msg_id(String last_msg_id) {
        this.last_msg_id = last_msg_id;
    }

    public String getLast_msg_name() {
        return last_msg_name;
    }

    public void setLast_msg_name(String last_msg_name) {
        this.last_msg_name = last_msg_name;
    }

    public String getLast_msg_time() {
        return last_msg_time;
    }

    public void setLast_msg_time(String last_msg_time) {
        this.last_msg_time = last_msg_time;
    }
}
