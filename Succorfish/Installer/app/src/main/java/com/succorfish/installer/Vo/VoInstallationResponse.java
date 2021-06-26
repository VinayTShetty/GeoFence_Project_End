package com.succorfish.installer.Vo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Map;

public class VoInstallationResponse implements Serializable {

    String deviceId = "";
    String id = "";
    String date = "";
    String created = "";
    String completed = "";
    String createdById = "";
    String createdByUsername = "";
    String status = "";
    String operation = "";
    String notes = "";
    String deviceImei = "";
    String deviceType = "";
    String realAssetId = "";
    String realAssetName = "";
    String realAssetRegNo = "";
    VoContactInfo contactInfo;
    String powerSource = "";
    String installationPlace = "";
    String tcAccepted = "";
    String qcBy = "";
    String courier = "";
    String trackingId = "";
    String qcSticker = "";
    String rnd = "";
    String glandCheck = "";
    String studsNPads = "";
//    VoAttachedFile attachedFiles;
    @SerializedName("attachedFiles")
    @Expose
    Map<String, String> attachedFilesList;
//    String objectHistory = "";

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getCompleted() {
        return completed;
    }

    public void setCompleted(String completed) {
        this.completed = completed;
    }

    public String getCreatedById() {
        return createdById;
    }

    public void setCreatedById(String createdById) {
        this.createdById = createdById;
    }

    public String getCreatedByUsername() {
        return createdByUsername;
    }

    public void setCreatedByUsername(String createdByUsername) {
        this.createdByUsername = createdByUsername;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getDeviceImei() {
        return deviceImei;
    }

    public void setDeviceImei(String deviceImei) {
        this.deviceImei = deviceImei;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getRealAssetId() {
        return realAssetId;
    }

    public void setRealAssetId(String realAssetId) {
        this.realAssetId = realAssetId;
    }

    public String getRealAssetName() {
        return realAssetName;
    }

    public void setRealAssetName(String realAssetName) {
        this.realAssetName = realAssetName;
    }

    public String getRealAssetRegNo() {
        return realAssetRegNo;
    }

    public void setRealAssetRegNo(String realAssetRegNo) {
        this.realAssetRegNo = realAssetRegNo;
    }

    public VoContactInfo getContactInfo() {
        return contactInfo;
    }

    public void setContactInfo(VoContactInfo contactInfo) {
        this.contactInfo = contactInfo;
    }

    public String getPowerSource() {
        return powerSource;
    }

    public void setPowerSource(String powerSource) {
        this.powerSource = powerSource;
    }

    public String getInstallationPlace() {
        return installationPlace;
    }

    public void setInstallationPlace(String installationPlace) {
        this.installationPlace = installationPlace;
    }

    public String getTcAccepted() {
        return tcAccepted;
    }

    public void setTcAccepted(String tcAccepted) {
        this.tcAccepted = tcAccepted;
    }

    public String getQcBy() {
        return qcBy;
    }

    public void setQcBy(String qcBy) {
        this.qcBy = qcBy;
    }

    public String getCourier() {
        return courier;
    }

    public void setCourier(String courier) {
        this.courier = courier;
    }

    public String getTrackingId() {
        return trackingId;
    }

    public void setTrackingId(String trackingId) {
        this.trackingId = trackingId;
    }

    public String getQcSticker() {
        return qcSticker;
    }

    public void setQcSticker(String qcSticker) {
        this.qcSticker = qcSticker;
    }

    public String getRnd() {
        return rnd;
    }

    public void setRnd(String rnd) {
        this.rnd = rnd;
    }

    public String getGlandCheck() {
        return glandCheck;
    }

    public void setGlandCheck(String glandCheck) {
        this.glandCheck = glandCheck;
    }

    public String getStudsNPads() {
        return studsNPads;
    }

    public void setStudsNPads(String studsNPads) {
        this.studsNPads = studsNPads;
    }

//    public VoAttachedFile getAttachedFiles() {
//        return attachedFiles;
//    }
//
//    public void setAttachedFiles(VoAttachedFile attachedFiles) {
//        this.attachedFiles = attachedFiles;
//    }

//    public String getObjectHistory() {
//        return objectHistory;
//    }
//
//    public void setObjectHistory(String objectHistory) {
//        this.objectHistory = objectHistory;
//    }

    public Map<String, String> getAttachedFilesList() {
        return attachedFilesList;
    }

    public void setAttachedFilesList(Map<String, String> attachedFilesList) {
        this.attachedFilesList = attachedFilesList;
    }
}
