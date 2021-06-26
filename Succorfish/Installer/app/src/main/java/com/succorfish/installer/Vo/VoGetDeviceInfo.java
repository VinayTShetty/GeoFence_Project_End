package com.succorfish.installer.Vo;

import java.io.Serializable;

public class VoGetDeviceInfo implements Serializable {

    String id = "";
    String imei = "";
    String imea = "";
    String telephone = "";
    String imeiIridium = "";
    String iridiumDisabled = "";
    String firmwareVersion = "";
    String hardwareRevision = "";
    String status = "";
    String type = "";
    String accountName = "";
    String accountId = "";
    String assetId = "";
    String systemAssetId = "";
    String assetName = "";
    String portNo = "";
    String warrantyLength = "";
    String warrantyExpires = "";
    String consistent = "";

    VoContactInfo ContactInfo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getImea() {
        return imea;
    }

    public void setImea(String imea) {
        this.imea = imea;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getImeiIridium() {
        return imeiIridium;
    }

    public void setImeiIridium(String imeiIridium) {
        this.imeiIridium = imeiIridium;
    }

    public String getIridiumDisabled() {
        return iridiumDisabled;
    }

    public void setIridiumDisabled(String iridiumDisabled) {
        this.iridiumDisabled = iridiumDisabled;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public String getHardwareRevision() {
        return hardwareRevision;
    }

    public void setHardwareRevision(String hardwareRevision) {
        this.hardwareRevision = hardwareRevision;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getAssetId() {
        return assetId;
    }

    public void setAssetId(String assetId) {
        this.assetId = assetId;
    }

    public String getSystemAssetId() {
        return systemAssetId;
    }

    public void setSystemAssetId(String systemAssetId) {
        this.systemAssetId = systemAssetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getWarrantyLength() {
        return warrantyLength;
    }

    public void setWarrantyLength(String warrantyLength) {
        this.warrantyLength = warrantyLength;
    }

    public String getWarrantyExpires() {
        return warrantyExpires;
    }

    public void setWarrantyExpires(String warrantyExpires) {
        this.warrantyExpires = warrantyExpires;
    }

    public String getConsistent() {
        return consistent;
    }

    public void setConsistent(String consistent) {
        this.consistent = consistent;
    }

    public String getPortNo() {
        return portNo;
    }

    public void setPortNo(String portNo) {
        this.portNo = portNo;
    }

    public VoContactInfo getContactInfo() {
        return ContactInfo;
    }

    public void setContactInfo(VoContactInfo contactInfo) {
        ContactInfo = contactInfo;
    }
}
