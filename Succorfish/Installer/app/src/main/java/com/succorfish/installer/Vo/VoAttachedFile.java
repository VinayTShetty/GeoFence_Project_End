package com.succorfish.installer.Vo;

import java.io.Serializable;
import java.util.Map;

public class VoAttachedFile implements Serializable {

    Map<String, String> fileList;

    public Map<String, String> getFileList() {
        return fileList;
    }

    public void setFileList(Map<String, String> fileList) {
        this.fileList = fileList;
    }
}
