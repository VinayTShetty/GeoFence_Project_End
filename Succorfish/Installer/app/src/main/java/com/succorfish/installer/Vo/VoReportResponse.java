package com.succorfish.installer.Vo;

import java.io.Serializable;

public class VoReportResponse implements Serializable {
    String id="";
    String mimeType="";
    String filename="";
    String oid="";
    String length="";
    String contentBytes="";

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getOid() {
        return oid;
    }

    public void setOid(String oid) {
        this.oid = oid;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getContentBytes() {
        return contentBytes;
    }

    public void setContentBytes(String contentBytes) {
        this.contentBytes = contentBytes;
    }
}
