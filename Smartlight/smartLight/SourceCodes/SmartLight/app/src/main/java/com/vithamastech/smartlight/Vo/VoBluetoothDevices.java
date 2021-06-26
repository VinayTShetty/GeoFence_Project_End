package com.vithamastech.smartlight.Vo;

import android.bluetooth.BluetoothDevice;

import java.io.Serializable;

/**
 * Created by Jaydeep on 26-12-2017.
 */
/*Bluetooth Device Getter Setter*/
public class VoBluetoothDevices implements Serializable {

    String deviceName = "";
    String deviceIEEE = "";
    String deviceAddress = "";
    String deviceHexData = "";
    String deviceScanOpcode = "";
    String deviceType = "";
//    public int deviceRSSI;
    BluetoothDevice bluetoothDevice;
    boolean isFromNotification = false;
    boolean isConnected = false;
    boolean isConnectable = false;

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getDeviceIEEE() {
        return deviceIEEE;
    }

    public void setDeviceIEEE(String deviceIEEE) {
        this.deviceIEEE = deviceIEEE;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

//    public int getDeviceRSSI() {
//        return deviceRSSI;
//    }
//
//    public void setDeviceRSSI(int deviceRSSI) {
//        this.deviceRSSI = deviceRSSI;
//    }

    public BluetoothDevice getBluetoothDevice() {
        return bluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public boolean getIsConnected() {
        return isConnected;
    }

    public void setIsConnected(boolean connected) {
        isConnected = connected;
    }

    public boolean getIsConnectable() {
        return isConnectable;
    }

    public void setIsConnectable(boolean connectable) {
        isConnectable = connectable;
    }

    public String getDeviceHexData() {
        return deviceHexData;
    }

    public void setDeviceHexData(String deviceHexData) {
        this.deviceHexData = deviceHexData;
    }

    public String getDeviceScanOpcode() {
        return deviceScanOpcode;
    }

    public void setDeviceScanOpcode(String deviceScanOpCode) {
        this.deviceScanOpcode = deviceScanOpCode;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public boolean getIsFromNotification() {
        return isFromNotification;
    }

    public void setFromNotification(boolean fromNotification) {
        isFromNotification = fromNotification;
    }
}
