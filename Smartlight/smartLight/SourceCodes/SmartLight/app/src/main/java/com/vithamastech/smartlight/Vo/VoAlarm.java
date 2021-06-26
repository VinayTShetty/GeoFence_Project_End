package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 14-02-2018.
 */
/* Alarm Data Getter Setter*/
public class VoAlarm implements Serializable {
    String alarm_local_id = "";
    String alarm_server_id = "";
    String alarm_name = "";
    String alarm_time = "";
    String alarm_status = "";
    String alarm_days = "";
    String alarm_color = "";
    String alarm_light_on = "";
    String alarm_wake_up_sleep = "";
    String alarm_count_no = "";
    String alarm_timestamp = "";
    String alarm_is_active = "";
    String created_at = "";
    String updated_at = "";
    String alarm_is_sync = "";

    boolean isChecked = false;


    public String getAlarm_local_id() {
        return alarm_local_id;
    }

    public void setAlarm_local_id(String alarm_local_id) {
        this.alarm_local_id = alarm_local_id;
    }

    public String getAlarm_name() {
        return alarm_name;
    }

    public void setAlarm_name(String alarm_name) {
        this.alarm_name = alarm_name;
    }

    public String getAlarm_time() {
        return alarm_time;
    }

    public void setAlarm_time(String alarm_time) {
        this.alarm_time = alarm_time;
    }

    public String getAlarm_status() {
        return alarm_status;
    }

    public void setAlarm_status(String alarm_status) {
        this.alarm_status = alarm_status;
    }

    public String getAlarm_server_id() {
        return alarm_server_id;
    }

    public void setAlarm_server_id(String alarm_server_id) {
        this.alarm_server_id = alarm_server_id;
    }

    public String getAlarm_timestamp() {
        return alarm_timestamp;
    }

    public void setAlarm_timestamp(String alarm_timestamp) {
        this.alarm_timestamp = alarm_timestamp;
    }

    public String getAlarm_is_active() {
        return alarm_is_active;
    }

    public void setAlarm_is_active(String alarm_is_active) {
        this.alarm_is_active = alarm_is_active;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getAlarm_is_sync() {
        return alarm_is_sync;
    }

    public void setAlarm_is_sync(String alarm_is_sync) {
        this.alarm_is_sync = alarm_is_sync;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean checked) {
        isChecked = checked;
    }

    public String getAlarm_days() {
        return alarm_days;
    }

    public void setAlarm_days(String alarm_days) {
        this.alarm_days = alarm_days;
    }

    public String getAlarm_color() {
        return alarm_color;
    }

    public void setAlarm_color(String alarm_color) {
        this.alarm_color = alarm_color;
    }

    public String getAlarm_light_on() {
        return alarm_light_on;
    }

    public void setAlarm_light_on(String alarm_light_on) {
        this.alarm_light_on = alarm_light_on;
    }

    public String getAlarm_count_no() {
        return alarm_count_no;
    }

    public void setAlarm_count_no(String alarm_count_no) {
        this.alarm_count_no = alarm_count_no;
    }

    public String getAlarm_wake_up_sleep() {
        return alarm_wake_up_sleep;
    }

    public void setAlarm_wake_up_sleep(String alarm_wake_up_sleep) {
        this.alarm_wake_up_sleep = alarm_wake_up_sleep;
    }
}
