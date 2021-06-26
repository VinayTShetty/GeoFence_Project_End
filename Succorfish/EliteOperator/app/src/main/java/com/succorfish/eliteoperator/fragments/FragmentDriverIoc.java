package com.succorfish.eliteoperator.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.succorfish.eliteoperator.MainActivity;
import com.succorfish.eliteoperator.R;
import com.succorfish.eliteoperator.compass.Compass;
import com.succorfish.eliteoperator.compass.MyAzimuthSensorListener;
import com.succorfish.eliteoperator.compass.MyLocationListener;

import java.util.ArrayList;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.geeksters.radar.Radar;
import co.geeksters.radar.RadarPoint;
import co.geeksters.radar.RadarScanView;
import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.SENSOR_SERVICE;

/**
 * Created by Jaydeep on 18-01-2018.
 */

public class FragmentDriverIoc extends Fragment implements  SensorListener {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    Radar radar;
    RadarScanView radarScanView;
    @BindView(R.id.frg_driverioc_reltivelayout_user_driver)
    LinearLayout mRelativeLayoutUser;
    @BindView(R.id.frg_driverioc_imageview_profile)
    ImageView mCircleImageViewProfile;
    @BindView(R.id.frg_driverioc_imageview_message)
    ImageView mImageViewSendMessage;
    @BindView(R.id.frg_driverioc_textview_drivername)
    TextView mTextViewDriverName;
    @BindView(R.id.frg_driverioc_textview_distance)
    TextView mTextViewDriverDistance;
    @BindView(R.id.frg_driverioc_textview_position)
    TextView mTextViewDriverPosition;
    @BindView(R.id.frg_driverioc_textview_depth)
    TextView mTextViewDriverDepth;
    private Handler handler;
    private Runnable runnableCode;
    private Handler handlerAnimation;
    private Runnable runnableAnimation;
    final int mIntContinuesScanTime = 20000;
    private Random rand;
    //    private Compass compass;
    private ImageView compassImage;
    private SensorManager sensorManager;
    private Sensor sensor;
    private SensorEventListener mySensorEventListener;
    private Compass compass;
    private LocationManager locManager;
    private LocationListener locListener;
    private boolean gps_enabled = false;
    private boolean network_enabled = false;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    boolean accelerometer;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_driverioc, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_driver_ioc_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);

        radar = (Radar) mViewRoot.findViewById(R.id.radar);
        radarScanView = (RadarScanView) mViewRoot.findViewById(R.id.radar_scan_view);
        compassImage = (ImageView) mViewRoot.findViewById(R.id.frg_driverioc_imageview_compass);
        RelativeLayout layout = (RelativeLayout)mViewRoot.findViewById(R.id.frg_driverioc_relativelayout_radar);
        setupAzimuth();
        handler = new Handler();
        handlerAnimation = new Handler();
        rand = new Random();

        radar.setReferencePoint(new RadarPoint("Boat", 10.00000f, 22.0000f, 0, getResources().getColor(R.color.colorGreen)));
        radarScanView.setReferencePoint(new RadarPoint("Boat", 10.00000f, 22.0000f, 0, getResources().getColor(R.color.colorGreen)));

        ArrayList<RadarPoint> points = new ArrayList<RadarPoint>();
        points.add(new RadarPoint("1", 10.00080f, 22.0030f, 55, getResources().getColor(R.color.colorDepth_one)));
        points.add(new RadarPoint("2", 10.00180f, 22.0060f, 200, getResources().getColor(R.color.colorDepth_two)));
        points.add(new RadarPoint("3", 10.00350f, 22.0115f, 300, getResources().getColor(R.color.colorDepth_three)));
        points.add(new RadarPoint("4", 10.00120f, 22.0170f, 140, getResources().getColor(R.color.colorDepth_four)));
        points.add(new RadarPoint("5", 10.00380f, 22.0210f, 350, getResources().getColor(R.color.colorDepth_five)));
        radar.setPoints(points);

//        compass = new Compass(mActivity);
//        Timer mTimer = new Timer();
//        mTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if (isAdded()) {
//                    compass.arrowView = (ImageView) mViewRoot.findViewById(R.id.frg_driverioc_imageview_compass);
//                }
//            }
//        }, 400);

        runnableCode = new Runnable() {
            @Override
            public void run() {
                System.out.println("CALLLL");
                if (isAdded()) {
                    ArrayList<RadarPoint> points = new ArrayList<RadarPoint>();
                    int angle = rand.nextInt(360) + 1;
                    points.add(new RadarPoint("1", 10.00080f, 22.0030f, angle, getResources().getColor(R.color.colorDepth_one)));
                    angle = rand.nextInt(360) + 1;
                    points.add(new RadarPoint("2", 10.00180f, 22.0060f, angle, getResources().getColor(R.color.colorDepth_two)));
                    angle = rand.nextInt(360) + 1;
                    points.add(new RadarPoint("3", 10.00350f, 22.0115f, angle, getResources().getColor(R.color.colorDepth_three)));
                    angle = rand.nextInt(360) + 1;
                    points.add(new RadarPoint("4", 10.00120f, 22.0170f, angle, getResources().getColor(R.color.colorDepth_four)));
                    angle = rand.nextInt(360) + 1;
                    points.add(new RadarPoint("5", 10.00380f, 22.0210f, angle, getResources().getColor(R.color.colorDepth_five)));

                    radar.setPoints(points);
                    radar.refresh();
                    handler.postDelayed(runnableCode, mIntContinuesScanTime);
                }
            }
        };
        handler.postDelayed(runnableCode, mIntContinuesScanTime);

        radar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String pinIdentifier = radar.getTouchedPin(event);
                if (pinIdentifier != null) {
//                    Toast.makeText(mActivity, pinIdentifier, Toast.LENGTH_SHORT).show();
                    mRelativeLayoutUser.setVisibility(View.VISIBLE);
                    slideDown(mRelativeLayoutUser);
                    if (handlerAnimation != null) {
                        System.out.println("HandlerAnimCancel");
                        if (runnableAnimation != null) {
                            handlerAnimation.removeCallbacks(runnableAnimation);
                        }
                    }
                    runnableAnimation = new Runnable() {
                        @Override
                        public void run() {
                            System.out.println("CALLLL ANIMATION");
                            slideUp(mRelativeLayoutUser);
                        }
                    };
                    handlerAnimation.postDelayed(runnableAnimation, 5000);
                }
                return true;
            }
        });
        mImageViewSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                FragmentSentMessage mFragmentMessage = new FragmentSentMessage();
//                mActivity.replacesFragment(mFragmentMessage, true, null, 1);
            }
        });
        return mViewRoot;
    }

    // slide the view from below itself to the current position
    public void slideUp(View view) {
        view.setVisibility(View.INVISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                0,  // fromYDelta
                (0 - view.getHeight()));                // toYDelta
        animate.setDuration(1000);
        animate.setFillAfter(true);
        view.startAnimation(animate);
    }

    // slide the view from its current position to below itself
    public void slideDown(View view) {
        view.setVisibility(View.VISIBLE);
        TranslateAnimation animate = new TranslateAnimation(
                0,                 // fromXDelta
                0,                 // toXDelta
                (0 - view.getHeight()),                 // fromYDelta
                0); // toYDelta
        animate.setDuration(1000);
        animate.setFillAfter(true);
        view.startAnimation(animate);
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
                    public void onAzimuth(float azimuthFrom, float azimuthTo) {
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
        sensorManager.unregisterListener((SensorListener) this);
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (handler != null) {
            System.out.println("HandlerCancel");
            handler.removeCallbacks(runnableCode);
        }
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
        if (handlerAnimation != null) {
            System.out.println("HandlerAnimCancel");
            handlerAnimation.removeCallbacks(runnableAnimation);
        }
    }

    @Override
    public void onSensorChanged(int sensor, float[] values) {

    }

    @Override
    public void onAccuracyChanged(int sensor, int accuracy) {

    }
}
