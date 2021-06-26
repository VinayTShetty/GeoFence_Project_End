package com.succorfish.combatdiver.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 07-02-2018.
 */

public class VoMessageHistory implements Serializable {

    String history_id = "";
    String history_from_id = "";
    String history_from_name = "";
    String history_to_id = "";
    String history_to_name = "";
    String history_message_id = "";
    String history_message_name = "";
    String history_time = "";
    String history_status = "";
    String history_updated_date = "";

    public String getHistory_id() {
        return history_id;
    }

    public void setHistory_id(String history_id) {
        this.history_id = history_id;
    }

    public String getHistory_from_id() {
        return history_from_id;
    }

    public void setHistory_from_id(String history_from_id) {
        this.history_from_id = history_from_id;
    }

    public String getHistory_from_name() {
        return history_from_name;
    }

    public void setHistory_from_name(String history_from_name) {
        this.history_from_name = history_from_name;
    }

    public String getHistory_to_id() {
        return history_to_id;
    }

    public void setHistory_to_id(String history_to_id) {
        this.history_to_id = history_to_id;
    }

    public String getHistory_to_name() {
        return history_to_name;
    }

    public void setHistory_to_name(String history_to_name) {
        this.history_to_name = history_to_name;
    }

    public String getHistory_message_id() {
        return history_message_id;
    }

    public void setHistory_message_id(String history_message_id) {
        this.history_message_id = history_message_id;
    }

    public String getHistory_message_name() {
        return history_message_name;
    }

    public void setHistory_message_name(String history_message_name) {
        this.history_message_name = history_message_name;
    }

    public String getHistory_time() {
        return history_time;
    }

    public void setHistory_time(String history_time) {
        this.history_time = history_time;
    }

    public String getHistory_status() {
        return history_status;
    }

    public void setHistory_status(String history_status) {
        this.history_status = history_status;
    }

    public String getHistory_updated_date() {
        return history_updated_date;
    }

    public void setHistory_updated_date(String history_updated_date) {
        this.history_updated_date = history_updated_date;
    }
}
