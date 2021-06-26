package com.succorfish.combatdiver.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 20-01-2018.
 */

public class VoDiverMessage implements Serializable {
    String id = "";
    String driver_message_id = "";
    String message = "";
    String is_emergency = "";
    String created_date = "";
    String updated_date = "";
    String is_sync = "";

    // For surface Message
    String surface_message_id = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDriver_message_id() {
        return driver_message_id;
    }

    public void setDriver_message_id(String driver_message_id) {
        this.driver_message_id = driver_message_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getIs_emergency() {
        return is_emergency;
    }

    public void setIs_emergency(String is_emergency) {
        this.is_emergency = is_emergency;
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

    public String getSurface_message_id() {
        return surface_message_id;
    }

    public void setSurface_message_id(String surface_message_id) {
        this.surface_message_id = surface_message_id;
    }
}
