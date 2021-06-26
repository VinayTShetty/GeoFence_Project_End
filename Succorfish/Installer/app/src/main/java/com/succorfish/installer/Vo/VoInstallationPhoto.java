package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class VoInstallationPhoto implements Serializable {

    String inst_photo_local_id = "";
    String inst_photo_server_id = "";
    String inst_photo_local_url = "";
    String inst_photo_server_url = "";
    String inst_photo_type = "";
    String inst_local_id = "";
    String inst_server_id = "";
    String inst_photo_user_id = "";
    String inst_photo_created_date = "";
    String inst_photo_update_date = "";
    String is_sync = "";
    boolean has_image = false;
    boolean modify_image = false;
    boolean isSignature = false;
    boolean isOwnerSignature = false;
    boolean isQuestionFile = false;

    public boolean getIsHasImage() {
        return has_image;
    }

    public void setIsHasImage(boolean has_image) {
        this.has_image = has_image;
    }

    public boolean getIsModifyImage() {
        return modify_image;
    }

    public void setIsModifyImage(boolean modifyimage) {
        this.modify_image = modifyimage;
    }

    public String getInst_photo_local_id() {
        return inst_photo_local_id;
    }

    public void setInst_photo_local_id(String inst_photo_local_id) {
        this.inst_photo_local_id = inst_photo_local_id;
    }

    public String getInst_photo_server_id() {
        return inst_photo_server_id;
    }

    public void setInst_photo_server_id(String inst_photo_server_id) {
        this.inst_photo_server_id = inst_photo_server_id;
    }

    public String getInst_photo_local_url() {
        return inst_photo_local_url;
    }

    public void setInst_photo_local_url(String inst_photo_local_url) {
        this.inst_photo_local_url = inst_photo_local_url;
    }

    public String getInst_photo_server_url() {
        return inst_photo_server_url;
    }

    public void setInst_photo_server_url(String inst_photo_server_url) {
        this.inst_photo_server_url = inst_photo_server_url;
    }

    public String getInst_photo_type() {
        return inst_photo_type;
    }

    public void setInst_photo_type(String inst_photo_type) {
        this.inst_photo_type = inst_photo_type;
    }

    public String getInst_local_id() {
        return inst_local_id;
    }

    public void setInst_local_id(String inst_local_id) {
        this.inst_local_id = inst_local_id;
    }

    public String getInst_server_id() {
        return inst_server_id;
    }

    public void setInst_server_id(String inst_server_id) {
        this.inst_server_id = inst_server_id;
    }

    public String getInst_photo_user_id() {
        return inst_photo_user_id;
    }

    public void setInst_photo_user_id(String inst_photo_user_id) {
        this.inst_photo_user_id = inst_photo_user_id;
    }

    public String getInst_photo_created_date() {
        return inst_photo_created_date;
    }

    public void setInst_photo_created_date(String inst_photo_created_date) {
        this.inst_photo_created_date = inst_photo_created_date;
    }

    public String getInst_photo_update_date() {
        return inst_photo_update_date;
    }

    public void setInst_photo_update_date(String inst_photo_update_date) {
        this.inst_photo_update_date = inst_photo_update_date;
    }

    public String getIs_sync() {
        return is_sync;
    }

    public void setIs_sync(String is_sync) {
        this.is_sync = is_sync;
    }

    public boolean getIsSignature() {
        return isSignature;
    }

    public void setIsSignature(boolean signature) {
        isSignature = signature;
    }

    public boolean getIsOwnerSignature() {
        return isOwnerSignature;
    }

    public void setIsOwnerSignature(boolean ownerSignature) {
        isOwnerSignature = ownerSignature;
    }

    public boolean getIsQuestionFile() {
        return isQuestionFile;
    }

    public void setQuestionFile(boolean questionFile) {
        isQuestionFile = questionFile;
    }
}
