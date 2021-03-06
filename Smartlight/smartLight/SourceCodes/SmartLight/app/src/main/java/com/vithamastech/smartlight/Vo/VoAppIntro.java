package com.vithamastech.smartlight.Vo;

import android.graphics.drawable.Drawable;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.vithamastech.smartlight.R;

import java.io.Serializable;

/*App Intro Screen Getter Setter*/
public class VoAppIntro implements Serializable {

    public String title;
    public String description;
    public Drawable imageResource;
    @StringRes
    public int titleResourceId;
    @StringRes
    public int descriptionResourceId;
    @DrawableRes
    public int imageResourceId;
    @ColorRes
    public int titleColor;
    @ColorRes
    public int descriptionColor;
    @ColorRes
    public int backgroundColor;
    public float titleTextSize;
    public float descriptionTextSize;
    public boolean multilineDescriptionCentered;

    public VoAppIntro(String title, String description) {
        this.title = title;
        this.description = description;
        this.backgroundColor = R.color.colorSemiTransparent;
    }

    public VoAppIntro(String title, String description, int imageResourceId) {
        this.title = title;
        this.description = description;
        this.imageResourceId = imageResourceId;
        this.backgroundColor = R.color.colorSemiTransparent;
    }

    public VoAppIntro(String title, String description, Drawable imageResource) {
        this.title = title;
        this.description = description;
        this.imageResource = imageResource;
        this.backgroundColor = R.color.colorSemiTransparent;
    }

    public VoAppIntro(int title, int description) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.backgroundColor = R.color.colorSemiTransparent;
    }

    public VoAppIntro(int title, int description, int imageResourceId) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.imageResourceId = imageResourceId;
        this.backgroundColor = R.color.colorSemiTransparent;
    }

    public VoAppIntro(int title, int description, Drawable imageResource) {
        this.titleResourceId = title;
        this.descriptionResourceId = description;
        this.imageResource = imageResource;
        this.backgroundColor = R.color.colorSemiTransparent;
    }

    public String getTitle() {
        return title;
    }

    public int getTitleResourceId() {
        return titleResourceId;
    }

    public String getDescription() {
        return description;
    }

    public int getDescriptionResourceId() {
        return descriptionResourceId;
    }

    public int getTitleColor() {
        return titleColor;
    }

    public int getDescriptionColor() {
        return descriptionColor;
    }

    public void setTitleColor(int color) {
        this.titleColor = color;
    }

    public void setDescriptionColor(int color) {
        this.descriptionColor = color;
    }

    public void setImageResourceId(int imageResourceId) {
        this.imageResourceId = imageResourceId;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public int getBackgroundColor() {
        return backgroundColor;
    }

    public float getTitleTextSize() {
        return titleTextSize;
    }

    public void setTitleTextSize(float titleTextSize) {
        this.titleTextSize = titleTextSize;
    }

    public float getDescriptionTextSize() {
        return descriptionTextSize;
    }

    public void setDescriptionTextSize(float descriptionTextSize) {
        this.descriptionTextSize = descriptionTextSize;
    }

    public boolean isMultilineDescriptionCentered() {
        return multilineDescriptionCentered;
    }

    public void setMultilineDescriptionCentered(boolean multilineDescriptionCentered) {
        this.multilineDescriptionCentered = multilineDescriptionCentered;
    }
}
