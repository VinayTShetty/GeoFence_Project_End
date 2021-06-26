package com.succorfish.combatdiver.compass;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by kris on 20.05.15.
 */
public class MyAzimuthSensorListener implements SensorEventListener {


    private int azimuthFrom = 0; // degree
    private int azimuthTo = 0; // degree
    private OnAzimuthListener mAzimuthListener;

    public MyAzimuthSensorListener(OnAzimuthListener azimuthListener) {
        mAzimuthListener = azimuthListener;
    }

    public void setOnShakeListener(OnAzimuthListener listener) {
        mAzimuthListener = listener;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        azimuthFrom = azimuthTo;

        float[] orientation = new float[3];
        float[] rMat = new float[9];
        SensorManager.getRotationMatrixFromVector(rMat, event.values);
        azimuthTo = (int) ( Math.toDegrees( SensorManager.getOrientation( rMat, orientation )[0] ) + 360 ) % 360;

        mAzimuthListener.onAzimuth(azimuthFrom, azimuthTo);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
    public interface OnAzimuthListener {

        void onAzimuth(float azimuthFrom, float azimuthTo);
    }
}
