package com.vithamastech.smartlight.Vo;

import java.io.Serializable;
import java.util.ArrayList;

/* Favourite color data getter Setter*/
public class VoFavColor  implements Serializable {

    private String colorSection;
    private ArrayList<VoPattern> colorLists;
    public String getColorSection() {
        return colorSection;
    }

    public void setColorSection(String colorSection) {
        this.colorSection = colorSection;
    }

    public ArrayList<VoPattern> getColorLists() {
        return colorLists;
    }

    public void setColorLists(ArrayList<VoPattern> colorLists) {
        this.colorLists = colorLists;
    }

    public void addColorLists(ArrayList<VoPattern> colorLists) {
        this.colorLists.addAll(colorLists);
    }
}
