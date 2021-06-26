package com.succorfish.installer.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class VoInspectionPhoto implements Serializable {

    String insp_photo_local_id = "";
    String insp_photo_server_id = "";
    String insp_photo_local_url = "";
    String insp_photo_server_url = "";
    String insp_photo_type = "";
    String insp_local_id = "";
    String insp_server_id = "";
    String insp_photo_user_id = "";
    String insp_photo_created_date = "";
    String insp_photo_update_date = "";
    String insp_photo_is_sync = "";

    boolean has_image = false;
    boolean modify_image = false;
    boolean isSignature=false;
    boolean isOwnerSignature=false;
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

    public String getInsp_photo_local_id() {
        return insp_photo_local_id;
    }

    public void setInsp_photo_local_id(String insp_photo_local_id) {
        this.insp_photo_local_id = insp_photo_local_id;
    }

    public String getInsp_photo_server_id() {
        return insp_photo_server_id;
    }

    public void setInsp_photo_server_id(String insp_photo_server_id) {
        this.insp_photo_server_id = insp_photo_server_id;
    }

    public String getInsp_photo_local_url() {
        return insp_photo_local_url;
    }

    public void setInsp_photo_local_url(String insp_photo_local_url) {
        this.insp_photo_local_url = insp_photo_local_url;
    }

    public String getInsp_photo_server_url() {
        return insp_photo_server_url;
    }

    public void setInsp_photo_server_url(String insp_photo_server_url) {
        this.insp_photo_server_url = insp_photo_server_url;
    }

    public String getInsp_photo_type() {
        return insp_photo_type;
    }

    public void setInsp_photo_type(String insp_photo_type) {
        this.insp_photo_type = insp_photo_type;
    }

    public String getInsp_local_id() {
        return insp_local_id;
    }

    public void setInsp_local_id(String insp_local_id) {
        this.insp_local_id = insp_local_id;
    }

    public String getInsp_server_id() {
        return insp_server_id;
    }

    public void setInsp_server_id(String insp_server_id) {
        this.insp_server_id = insp_server_id;
    }

    public String getInsp_photo_user_id() {
        return insp_photo_user_id;
    }

    public void setInsp_photo_user_id(String insp_photo_user_id) {
        this.insp_photo_user_id = insp_photo_user_id;
    }

    public String getInsp_photo_created_date() {
        return insp_photo_created_date;
    }

    public void setInsp_photo_created_date(String insp_photo_created_date) {
        this.insp_photo_created_date = insp_photo_created_date;
    }

    public String getInsp_photo_update_date() {
        return insp_photo_update_date;
    }

    public void setInsp_photo_update_date(String insp_photo_update_date) {
        this.insp_photo_update_date = insp_photo_update_date;
    }

    public String getInsp_photo_is_sync() {
        return insp_photo_is_sync;
    }

    public void setInsp_photo_is_sync(String insp_photo_is_sync) {
        this.insp_photo_is_sync = insp_photo_is_sync;
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
