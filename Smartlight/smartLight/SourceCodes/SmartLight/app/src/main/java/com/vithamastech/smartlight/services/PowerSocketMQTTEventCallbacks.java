package com.vithamastech.smartlight.services;

public abstract class PowerSocketMQTTEventCallbacks {
    public abstract void onMQTTConnected();
    public abstract void onMQTTDisconnected();
    public abstract void onMQTTConnectionFailed();
    public abstract void onMQTTException();
    public abstract void onMQTTTimeout(String deviceAddress);
    public void onTopicSubscribed(String deviceAddress){}
    public void onOTASubscribed(String deviceAddress){}
    public void onCurrentTimeSet(String deviceAddress){}
    public void onCheckSocketDiagnostics(String deviceAddress, int transactionId, byte[] data) {}
    public void onSingleSocketStateChange(String deviceAddress, int transactionId, byte[] data) {}
    public void onSingleSocketStateChangeFailed(String deviceAddress, int transactionId){}
    public void onAllSocketsStateChange(String deviceAddress, int transactionId, byte[] data) {}
    public void onAllSocketsStateChangeFailed(String deviceAddress, int transactionId){}
    public void onDeviceRemoved(String deviceAddress) {}
    public void onDeviceRemovalFailed(String deviceAddress) {}
    public void onOTAUpdateSuccessful(String deviceAddress, String version){}
    public void onOTAUpdateFailed(String deviceAddress){}

    public void onAlaramSetMqtt(String deviceAddress,int transactionId, byte alaramId, byte socketId, byte sucess_failure){}
    public void onAlaramDeleteMqtt(String deviceAddress,int transactionId, byte alaram_id, byte sucess_failure){}
    public void onAlaramUpdatedFromDeviceMQTT(boolean result){}
}