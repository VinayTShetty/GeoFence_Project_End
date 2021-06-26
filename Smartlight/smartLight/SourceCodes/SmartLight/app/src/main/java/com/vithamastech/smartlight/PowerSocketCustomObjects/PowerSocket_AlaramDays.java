package com.vithamastech.smartlight.PowerSocketCustomObjects;

import java.io.Serializable;

public class PowerSocket_AlaramDays implements Serializable {
    private boolean isChecked;
    private int buttonValue;
    private int buttonId;
    private int buttonIndex;

    public PowerSocket_AlaramDays(boolean isChecked, int buttonValue, int buttonId, int buttonIndex) {
        this.isChecked = isChecked;
        this.buttonValue = buttonValue;
        this.buttonId = buttonId;
        this.buttonIndex=buttonIndex;
    }

    public PowerSocket_AlaramDays() {
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public int getButtonValue() {
        return buttonValue;
    }

    public void setButtonValue(int buttonValue) {
        this.buttonValue = buttonValue;
    }

    public int getButtonId() {
        return buttonId;
    }

    public void setButtonId(int buttonId) {
        this.buttonId = buttonId;
    }

    public int getButtonIndex() {
        return buttonIndex;
    }

    public void setButtonIndex(int buttonIndex) {
        this.buttonIndex = buttonIndex;
    }

}
