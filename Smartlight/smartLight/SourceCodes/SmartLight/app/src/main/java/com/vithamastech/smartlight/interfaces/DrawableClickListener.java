package com.vithamastech.smartlight.interfaces;

/**
 * Created by muataz medini on 7/2/2016.
 */
public interface DrawableClickListener {

    public static enum DrawablePosition { TOP, BOTTOM, LEFT, RIGHT };
    public void onClick(DrawablePosition target);
}
