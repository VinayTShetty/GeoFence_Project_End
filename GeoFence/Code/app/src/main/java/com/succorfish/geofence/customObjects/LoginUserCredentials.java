package com.succorfish.geofence.customObjects;

import java.io.Serializable;

public class LoginUserCredentials implements Serializable {
    String id = "";
    String name = "";
    String email = "";
    String password = "";
    String mobile_no = "";
    String business_name = "";
    String phonecode = "";
    String address = "";
    String is_otp_verified = "";
    String is_password_verified = "";
    String status = "";
    String created_at = "";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getMobile_no() {
        return mobile_no;
    }

    public void setMobile_no(String mobile_no) {
        this.mobile_no = mobile_no;
    }

    public String getBusiness_name() {
        return business_name;
    }

    public void setBusiness_name(String business_name) {
        this.business_name = business_name;
    }

    public String getPhonecode() {
        return phonecode;
    }

    public void setPhonecode(String phonecode) {
        this.phonecode = phonecode;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getIs_otp_verified() {
        return is_otp_verified;
    }

    public void setIs_otp_verified(String is_otp_verified) {
        this.is_otp_verified = is_otp_verified;
    }

    public String getIs_password_verified() {
        return is_password_verified;
    }

    public void setIs_password_verified(String is_password_verified) {
        this.is_password_verified = is_password_verified;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }
}
