package com.succorfish.depthntemp.interfaces;

/**
 * Created by Jaydeep on 26-12-2017.
 * device setting callback
 */

public interface onDeviceSettingChange {

    public void onDeviceFwChange(String fwChange);

    public void onDeviceMemoryChange(String memoryChange);

    public void onDeviceTimeChange(String timeChange);

    public void onDeviceBatteryChange(String batteryChange);

    public void onDeviceStationaryIntervalChange(String stationaryIntervalChange);

    public void onDeviceDepthCutOffChange(String cutOffChange);

    public void onDeviceBleTransmissionChange(String batteryChange);

    public void onDeviceGpsIntervalChange(String batteryChange);

    public void onDeviceGpsTimeoutChange(String batteryChange);
}
