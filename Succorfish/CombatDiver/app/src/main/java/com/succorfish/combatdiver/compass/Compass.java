package com.succorfish.combatdiver.compass;

import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

/**
 * Created by kris on 20.05.15.
 */
public class Compass {
    private ImageView compass;
    private float azimuthFrom;
    private float azimuthTo;
    private RotateAnimation anim;

    public Compass(ImageView compass) {
        this.compass = compass;
    }

    public void rotate(float azimuthFrom, float azimuthTo) {

        anim = new RotateAnimation(360-azimuthFrom, 360-azimuthTo, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        anim.setInterpolator(new DecelerateInterpolator(100));
        anim.setRepeatCount(0);
        anim.setDuration(1000);
        compass.startAnimation(anim);
    }

    public float getAzimuthTo() {
        return azimuthTo;
    }

    public void setAzimuthTo(float azimuthTo) {
        this.azimuthTo = azimuthTo;
    }

    public float getAzimuthFrom() {
        return azimuthFrom;
    }

    public void setAzimuthFrom(float azimuthFrom) {
        this.azimuthFrom = azimuthFrom;
    }

}
