package com.succorfish.combatdiver.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 17-01-2018.
 */

public class VoDeviceUser implements Serializable {

    String deviceUserName = "";
    String deviceUserId = "";
    String devicePresser = "";
    String deviceUserImage = "";

    public String getDeviceUserName() {
        return deviceUserName;
    }

    public void setDeviceUserName(String deviceUserName) {
        this.deviceUserName = deviceUserName;
    }

    public String getDeviceUserId() {
        return deviceUserId;
    }

    public void setDeviceUserId(String deviceUserId) {
        this.deviceUserId = deviceUserId;
    }

    public String getDevicePresser() {
        return devicePresser;
    }

    public void setDevicePresser(String devicePresser) {
        this.devicePresser = devicePresser;
    }

    public String getDeviceUserImage() {
        return deviceUserImage;
    }

    public void setDeviceUserImage(String deviceUserImage) {
        this.deviceUserImage = deviceUserImage;
    }
}
