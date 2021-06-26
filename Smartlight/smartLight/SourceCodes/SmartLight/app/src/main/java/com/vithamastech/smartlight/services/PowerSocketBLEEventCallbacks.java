package com.vithamastech.smartlight.services;

import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;

import java.util.ArrayList;
import java.util.List;

public abstract class PowerSocketBLEEventCallbacks {
    public abstract void onDeviceConnected(String deviceAddress);
    public abstract void onDeviceDisconnected(String deviceAddress);
    public abstract void onDeviceConnectionFailed(String deviceAddress);
    public void onBluetoothRestart(){}
    public void onAuthenticationSuccess(String deviceAddress) {}
    public void onAuthenticationFailure(String deviceAddress) {}
    public void onDeviceAlreadyRegistered(String deviceAddress) {}
    public void onDeviceNotRegistered(String deviceAddress) {}
    public void onDeviceAddedSuccessfully(PowerSocket powerSocket) {}
    public void onDeviceAddingFailed(String deviceAddress) {}
    public void onWifiConfigured(String deviceAddress) {}
    public void onWifiNotConfigured(String deviceAddress) {}
    public void onWifiConfigFailed(String deviceAddress) {}
    public void onWifiMqttNotSubscribed(String deviceAddress) {}
    public void onWifiDeviceListFound(String deviceAddress, ArrayList<WifiDevice> wifiDeviceList) {}
    public void onWifiDevicesNotFound(String deviceAddress){}
    public void onWifiConfigRemoved(String deviceAddress){}
    public void onWifiConfigRemoveFailure(String deviceAddress){}
    public void onCheckSocketDiagnostics(String deviceAddress, int transactionId, byte[] data) {}
    public void onSingleSocketStateChange(String deviceAddress, int transactionId, byte[] data) {}
    public void onSingleSocketStateChangeFailed(String deviceAddress, int transactionId){}
    public void onAllSocketsStateChange(String deviceAddress, int transactionId, byte[] data) {}
    public void onAllSocketsStateChangeFailed(String deviceAddress, int transactionId){}
    public void onDeviceRemoved(String deviceAddress) {}
    public void onDeviceRemovalFailed(String deviceAddress) {}
    public void onDeviceResetCompleted(String deviceAddress){}
    public void onDeviceResetFailed(String deviceAddress){}
    public void onCurrentTimeSet(String deviceAddress){}
    public void onReceivedCurrentFirmwareVersion(String deviceAddress,String firmwareVersion){}
    public void onFactorySocketTestSuccessful(String deviceAddress){}
    public void onFactorySocketTestFailed(String deviceAddress){}

    public void onAlaramSetBLE(String deviceAddress, int transactionId, byte alaramId, byte socketId, byte sucess_failure){}
    public void onAlaramDeleteBLE(String deviceAddress, int transactionId, byte alaram_id, byte sucess_failure){}
    public void onAlaramUpdatedFromDeviceBLE(boolean result){}
}