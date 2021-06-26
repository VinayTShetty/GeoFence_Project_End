package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.List;

/*Local Group Data Getter Setter*/
public class VoLocalGroupData implements Serializable {

    String group_local_id = "";
    String group_server_id = "";
    String user_id = "";
    String group_comm_id = "";
    String group_comm_hex_id = "";
    String group_name = "";
    String group_switch_status = "";
    String group_is_favourite = "";
    String group_timestamp = "";
    String group_is_active = "";
    String group_created_at = "";
    String group_updated_at = "";
    String group_is_sync = "";
    boolean isGroupChecked = false;

    List<VoDeviceList> mVoDeviceLists;

    public String getGroup_local_id() {
        return group_local_id;
    }

    public void setGroup_local_id(String group_local_id) {
        this.group_local_id = group_local_id;
    }

    public String getGroup_server_id() {
        return group_server_id;
    }

    public void setGroup_server_id(String group_server_id) {
        this.group_server_id = group_server_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getGroup_comm_id() {
        return group_comm_id;
    }

    public void setGroup_comm_id(String group_comm_id) {
        this.group_comm_id = group_comm_id;
    }

    public String getGroup_comm_hex_id() {
        return group_comm_hex_id;
    }

    public void setGroup_comm_hex_id(String group_comm_hex_id) {
        this.group_comm_hex_id = group_comm_hex_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_switch_status() {
        return group_switch_status;
    }

    public void setGroup_switch_status(String group_switch_status) {
        this.group_switch_status = group_switch_status;
    }

    public String getGroup_is_favourite() {
        return group_is_favourite;
    }

    public void setGroup_is_favourite(String group_is_favourite) {
        this.group_is_favourite = group_is_favourite;
    }

    public String getGroup_timestamp() {
        return group_timestamp;
    }

    public void setGroup_timestamp(String group_timestamp) {
        this.group_timestamp = group_timestamp;
    }

    public String getGroup_is_active() {
        return group_is_active;
    }

    public void setGroup_is_active(String group_is_active) {
        this.group_is_active = group_is_active;
    }

    public String getGroup_created_at() {
        return group_created_at;
    }

    public void setGroup_created_at(String group_created_at) {
        this.group_created_at = group_created_at;
    }

    public String getGroup_updated_at() {
        return group_updated_at;
    }

    public void setGroup_updated_at(String group_updated_at) {
        this.group_updated_at = group_updated_at;
    }

    public String getGroup_is_sync() {
        return group_is_sync;
    }

    public void setGroup_is_sync(String group_is_sync) {
        this.group_is_sync = group_is_sync;
    }

    public boolean getIsGroupChecked() {
        return isGroupChecked;
    }

    public void setIsGroupChecked(boolean groupChecked) {
        isGroupChecked = groupChecked;
    }

    public List<VoDeviceList> getmVoDeviceLists() {
        return mVoDeviceLists;
    }

    public void setmVoDeviceLists(List<VoDeviceList> mVoDeviceLists) {
        this.mVoDeviceLists = mVoDeviceLists;
    }
}
