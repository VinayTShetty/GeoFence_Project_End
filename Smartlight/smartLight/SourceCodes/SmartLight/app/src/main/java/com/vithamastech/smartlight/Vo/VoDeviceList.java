package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by Jaydeep on 23-12-2017.
 */
/* Device List And Group Date Getter Setter*/
public class VoDeviceList implements Serializable {

    // Device
    String local_device_id = "";
    String server_device_id = "";
    String user_id = "";
    String device_comm_id = "";
    String device_comm_hexId = "";
    String device_name = "";
    String device_realName = "";
    String device_ble_address = "";
    String device_type = "";
    String device_type_name = "";

    String device_connect_status = "";
    String device_switch_status = "";
    String device_is_favourite = "";
    String device_timestamp = "";
    String device_is_active = "";
    String device_last_state_remember = "";
    String device_created_at = "";
    String device_updated_at = "";
    String device_is_sync = "";

    boolean isChecked = false;
    // Local Use Only
    int device_rgb_color = -1;
    int device_brightness = 100;

    // Group
    boolean isGroupChecked = false;
    boolean isDeviceSyncWithGroup = false;
    boolean isDeviceAlradyInGroup = false;

    String group_local_id = "";
    String group_server_id = "";
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
    int isWifiConfigured = 0;
    int socketState = 0;

    public String getDevice_Comm_id() {
        return device_comm_id;
    }

    public void setDevice_Comm_id(String device_id) {
        this.device_comm_id = device_id;
    }

    public String getDevice_name() {
        return device_name;
    }

    public void setDevice_name(String device_name) {
        this.device_name = device_name;
    }

    public String getDevicLocalId() {
        return local_device_id;
    }

    public void setDevicLocalId(String id) {
        this.local_device_id = id;
    }

    public String getDevice_Comm_hexId() {
        return device_comm_hexId;
    }

    public void setDevice_Comm_hexId(String device_hexId) {
        this.device_comm_hexId = device_hexId;
    }

    public String getDevice_realName() {
        return device_realName;
    }

    public void setDevice_realName(String device_realName) {
        this.device_realName = device_realName;
    }

    public String getDevice_BleAddress() {
        return device_ble_address;
    }

    public void setDevice_BleAddress(String device_BleAddress) {
        this.device_ble_address = device_BleAddress;
    }

    public String getDevice_Type() {
        return device_type;
    }

    public void setDevice_Type(String device_Type) {
        this.device_type = device_Type;
    }

    public String getDevice_ConnStatus() {
        return device_connect_status;
    }

    public void setDevice_ConnStatus(String device_ConnStatus) {
        this.device_connect_status = device_ConnStatus;
    }

    public String getDevice_SwitchStatus() {
        return device_switch_status;
    }

    public void setDevice_SwitchStatus(String device_SwitchStatus) {
        this.device_switch_status = device_SwitchStatus;
    }

    public String getDeviceServerId() {
        return server_device_id;
    }

    public void setDeviceServerid(String server_device_id) {
        this.server_device_id = server_device_id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDevice_type_name() {
        return device_type_name;
    }

    public void setDevice_type_name(String device_type_name) {
        this.device_type_name = device_type_name;
    }

    public String getDevice_is_favourite() {
        return device_is_favourite;
    }

    public void setDevice_is_favourite(String device_is_favourite) {
        this.device_is_favourite = device_is_favourite;
    }

    public String getDevice_timestamp() {
        return device_timestamp;
    }

    public void setDevice_timestamp(String device_timestamp) {
        this.device_timestamp = device_timestamp;
    }

    public String getDevice_is_active() {
        return device_is_active;
    }

    public void setDevice_is_active(String device_is_active) {
        this.device_is_active = device_is_active;
    }

    public String getDevice_created_at() {
        return device_created_at;
    }

    public void setDevice_created_at(String device_created_at) {
        this.device_created_at = device_created_at;
    }

    public String getDevice_updated_at() {
        return device_updated_at;
    }

    public void setDevice_updated_at(String device_updated_at) {
        this.device_updated_at = device_updated_at;
    }

    public String getDevice_is_sync() {
        return device_is_sync;
    }

    public void setDevice_is_sync(String device_is_sync) {
        this.device_is_sync = device_is_sync;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean getIsGroupChecked() {
        return isGroupChecked;
    }

    public void setIsGroupChecked(boolean groupChecked) {
        isGroupChecked = groupChecked;
    }

    public boolean getIsDeviceSyncWithGroup() {
        return isDeviceSyncWithGroup;
    }

    public void setDeviceSyncWithGroup(boolean deviceSyncWithGroup) {
        isDeviceSyncWithGroup = deviceSyncWithGroup;
    }

    public String getGroup_local_id() {
        return group_local_id;
    }

    public void setGroup_local_id(String group_local_id) {
        this.group_local_id = group_local_id;
    }

    public String getGroup_comm_id() {
        return group_comm_id;
    }

    public void setGroup_comm_id(String group_id) {
        this.group_comm_id = group_id;
    }

    public String getGroup_comm_hex_id() {
        return group_comm_hex_id;
    }

    public void setGroup_comm_hex_id(String group_hex_id) {
        this.group_comm_hex_id = group_hex_id;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getGroup_server_id() {
        return group_server_id;
    }

    public void setGroup_server_id(String group_server_id) {
        this.group_server_id = group_server_id;
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

    public boolean getIsDeviceAlradyInGroup() {
        return isDeviceAlradyInGroup;
    }

    public void setDeviceAlradyInGroup(boolean deviceAlradyInGroup) {
        isDeviceAlradyInGroup = deviceAlradyInGroup;
    }

    public String getDevice_last_state_remember() {
        return device_last_state_remember;
    }

    public void setDevice_last_state_remember(String device_last_state_remember) {
        this.device_last_state_remember = device_last_state_remember;
    }

    public int getDevice_rgb_color() {
        return device_rgb_color;
    }

    public void setDevice_rgb_color(int device_rgb_color) {
        this.device_rgb_color = device_rgb_color;
    }

    public int getSocketState() {
        return socketState;
    }

    public void setSocketState(int socketState) {
        this.socketState = socketState;
    }

    public int getDevice_brightness() {
        return device_brightness;
    }

    public void setDevice_brightness(int device_brightness) {
        this.device_brightness = device_brightness;
    }

    public boolean getIsWifiConfigured() {
        return isWifiConfigured > 0;
    }

    public void setIsWifiConfigured(int isWifiConfigured) {
        this.isWifiConfigured = isWifiConfigured;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VoDeviceList that = (VoDeviceList) o;
        return local_device_id.equals(that.local_device_id) &&
                device_ble_address.equals(that.device_ble_address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(local_device_id, device_ble_address);
    }
}
