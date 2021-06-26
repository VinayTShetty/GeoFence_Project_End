package com.succorfish.combatdiver.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.compass.Compass;
import com.succorfish.combatdiver.compass.MyAzimuthSensorListener;
import com.succorfish.combatdiver.compass.MyLocationListener;
import com.succorfish.combatdiver.MainActivity;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Jaydeep on 19-01-2018.
 */

public class FragmentCompass extends Fragment implements SensorListener {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    //    private Compass compass;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener mySensorEventListener;
    private Compass compass;
    private LocationManager locManager;
    private LocationListener locListener;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private ImageView compassImage;
    boolean accelerometer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.activity_compass, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_setting_txt_compass));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        compassImage = (ImageView) mViewRoot.findViewById(R.id.main_image_dial);
        setupAzimuth();
//        compass = new Compass(mActivity);
//        Timer mTimer = new Timer();
//        mTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (isAdded()) {
//                    compass.arrowView = (ImageView) mViewRoot.findViewById(R.id.main_image_hands);
//                }
//            }
//        }, 400);
        return mViewRoot;
    }

    private void setupAzimuth() {
        //initialize compass object
        compass = new Compass(compassImage);
        //define sensor manager and set required parameters
        sensorManager = (SensorManager) mActivity.getSystemService(SENSOR_SERVICE);
        accelerometer= sensorManager.registerListener(this,SensorManager.SENSOR_ACCELEROMETER);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (accelerometer) {
            if (sensor != null) {
                //register listener
                //initialize listener and callback with obtained data
                mySensorEventListener = new MyAzimuthSensorListener(new MyAzimuthSensorListener.OnAzimuthListener() {
                    @Override
                    public void onAzimuth(final float azimuthFrom, final float azimuthTo) {
                        compass.rotate(azimuthFrom, azimuthTo);
                    }
                });
                sensorManager.registerListener(mySensorEventListener, sensor,
                        SensorManager.SENSOR_DELAY_UI);

            } else {
                Toast.makeText(mActivity, "ORIENTATION Sensor not found",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupLocation() {

        //define location manager together with providers
        locManager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
        gps_enabled = locManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER);
        network_enabled = locManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gps_enabled && !network_enabled) {
            Toast.makeText(mActivity, "Location is not enabled",
                    Toast.LENGTH_LONG).show();
        }

        //initialize listener and callback with obtained data
        locListener = new MyLocationListener(new MyLocationListener.OnLocationListener() {
            @Override
            public void onLocation(Location location) {
                /*Toast.makeText(getApplicationContext(), "Provider: "+location.getProvider()+" Accuracy: "+location.getAccuracy(),
                        Toast.LENGTH_LONG).show();*/
            }
        });

        if (gps_enabled) {
            if (ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mActivity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
                    0, locListener);
        }
        if (network_enabled) {
            locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                    0, 0, locListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            if (locManager != null) {
                if (locListener != null) {
                    locManager.removeUpdates(locListener);
                    locListener = null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            setupLocation();
            setupAzimuth();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {

    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {

    }
}
