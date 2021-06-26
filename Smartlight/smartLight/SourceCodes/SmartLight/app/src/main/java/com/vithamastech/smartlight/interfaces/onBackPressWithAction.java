package com.vithamastech.smartlight.interfaces;

/**
 * Created by Jaydeep on 13-03-2018.
 */
public interface onBackPressWithAction {
    public void onBackWithAction(int color);

    public void onBackWithAction(String color, String brightness);

    public void onBackWithAction(String color, String brightness, String deviceID);

}
