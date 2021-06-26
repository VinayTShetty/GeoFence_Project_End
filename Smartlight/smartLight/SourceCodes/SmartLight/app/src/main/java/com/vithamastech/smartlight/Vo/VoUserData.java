package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 27-02-2018.
 */
/* User data getter Setter*/
public class VoUserData implements Serializable {

    String user_local_id = "";
    String user_id = "";
    String username = "";
    String account_name = "";
    String email = "";
    String password = "";
    String mobile_number = "";
    String device_token = "";
    String device_type = "";
    String status = "";

    boolean isChecked = false;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobile_number() {
        return mobile_number;
    }

    public void setMobile_number(String mobile_number) {
        this.mobile_number = mobile_number;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getDevice_type() {
        return device_type;
    }

    public void setDevice_type(String device_type) {
        this.device_type = device_type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String name) {
        this.username = name;
    }

    public String getAccount_name() {
        return account_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public String getUser_local_id() {
        return user_local_id;
    }

    public void setUser_local_id(String user_local_id) {
        this.user_local_id = user_local_id;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean checked) {
        isChecked = checked;
    }
}
