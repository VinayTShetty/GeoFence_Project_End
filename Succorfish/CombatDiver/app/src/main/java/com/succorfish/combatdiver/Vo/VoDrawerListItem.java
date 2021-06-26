package com.succorfish.combatdiver.Vo;

import java.io.Serializable;

/**
 * Created by Jaydeep on 16-01-2018.
 */

public class VoDrawerListItem implements Serializable {

    String mStringName = "";
    int selectedImage = 0;
    int unSelectedImage = 0;
    boolean isSelected = false;

    public String getmStringName() {
        return mStringName;
    }

    public void setmStringName(String mStringName) {
        this.mStringName = mStringName;
    }

    public int getSelectedImage() {
        return selectedImage;
    }

    public void setSelectedImage(int selectedImage) {
        this.selectedImage = selectedImage;
    }

    public int getUnSelectedImage() {
        return unSelectedImage;
    }

    public void setUnSelectedImage(int unSelectedImage) {
        this.unSelectedImage = unSelectedImage;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
