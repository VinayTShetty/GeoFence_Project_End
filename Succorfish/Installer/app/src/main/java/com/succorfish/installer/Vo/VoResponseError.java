package com.succorfish.installer.Vo;

import java.io.Serializable;

public class VoResponseError implements Serializable {

    String invalidValue = "";
    String errorCode = "";
    String errorArgs = "";
    String status = "";

    public String getInvalidValue() {
        return invalidValue;
    }

    public void setInvalidValue(String invalidValue) {
        this.invalidValue = invalidValue;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorArgs() {
        return errorArgs;
    }

    public void setErrorArgs(String errorArgs) {
        this.errorArgs = errorArgs;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
