package com.vithamastech.smartlight.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 25-12-2017.
 * Patter Getter Setter
 */

public class VoPattern implements Serializable {

    String pattern_name = "";
    String pattern_value = "";
    int pattern_image;
    boolean isChecked = false;
    // for favourite color
    boolean isFavColor = false;
    boolean isVisibleAddOption = false;
    String fav_id = "";

    public String getPattern_name() {
        return pattern_name;
    }

    public void setPattern_name(String pattern_name) {
        this.pattern_name = pattern_name;
    }

    public String getPattern_value() {
        return pattern_value;
    }

    public void setPattern_value(String pattern_value) {
        this.pattern_value = pattern_value;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    public void setIsChecked(boolean checked) {
        isChecked = checked;
    }

    public int getPattern_image() {
        return pattern_image;
    }

    public void setPattern_image(int patern_image) {
        this.pattern_image = patern_image;
    }

    public boolean getIsFavColor() {
        return isFavColor;
    }

    public void setIsFavColor(boolean favColor) {
        isFavColor = favColor;
    }

    public String getFav_id() {
        return fav_id;
    }

    public void setFav_id(String fav_id) {
        this.fav_id = fav_id;
    }

    public boolean getIsVisibleAddOption() {
        return isVisibleAddOption;
    }

    public void setIsVisibleAddOption(boolean visibleAddOption) {
        isVisibleAddOption = visibleAddOption;
    }
}
