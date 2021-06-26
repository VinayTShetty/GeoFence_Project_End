package com.vithamastech.smartlight.fragments;

import android.Manifest;
import android.annotation.TargetApi;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.SoundMeter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import com.vithamastech.smartlight.Views.vumeter.VuMeterView;

/**
 * Created by Jaydeep on 13-02-2018.
 */

public class FragmentColorMusic extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    int mIntRandomNo = 0;
    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;
    String mStringLocalId = "";
    String mStringServerId = "";
    @BindView(R.id.volumeLevel)
    TextView mTextViewVolume;
    @BindView(R.id.fragment_music_button_play_stop)
    Button mButtonPlayStop;
    @BindView(R.id.fragment_music_circleimageview_music)
    ImageView mImageViewAnimation;
    @BindView(R.id.vumeter)
    VuMeterView mVuMeterView;
    private Handler mHandlerMusic;
    private Runnable mRunnableMusic;
    private SoundMeter mSensor;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    boolean mBooleanIsMusicStart = false;
    int sendCount = 0;
    ArrayList<Integer> mArrayListAmplitudeAvg = new ArrayList<>();
    int mIntPreviousAvg = 0;
    int averageAmplitude = 0;
    double sumOfAmplitude;
    double volumeLevel;
    AudioManager mAudioManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mIntRandomNo = getArguments().getInt("intent_device_id", 0);
            mIsFromGroup = getArguments().getBoolean("intent_from_group", false);
            mIsFromAllGroup = getArguments().getBoolean("intent_from_all_group", false);
            if (!mIsFromAllGroup) {
                mStringLocalId = getArguments().getString("intent_local_id");
                mStringServerId = getArguments().getString("intent_server_id");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_music, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mSensor = new SoundMeter();
        mActivity.mSwitchCompatOnOff.setOnCheckedChangeListener(powerChange);
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);

        return mViewRoot;
    }

    /* Launch default music player */
    @OnClick(R.id.fragment_music_iv_launch_music)
    public void onLaunchMusicClick(View mView) {
        try {
            Intent intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_music_no_player), 3, true);
        }
    }

    /*Start Music Play*/
    @OnClick(R.id.fragment_music_button_play_stop)
    public void onPlayStopClick(View mView) {

        if (Build.VERSION.SDK_INT >= 23) {
            checkMarshmallowPermission();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (hasPermissions(mActivity, new String[]{
                    Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE})) {
                getSoundAmplitude();
            }
        } else {
            getSoundAmplitude();
        }
    }

    /*Get Amplitude sound from near voice*/
    private void getSoundAmplitude() {
        try {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                if (!mBooleanIsMusicStart) {
                    mSensor.start(mActivity);
                    mVuMeterView.resume(true);
                    if (isAdded()) {
                        mVuMeterView.setColor(Color.parseColor("#f12711"), Color.parseColor("#f5af19"), Color.parseColor("#ffffff"));
                    }
                    mBooleanIsMusicStart = true;
                    mButtonPlayStop.setText(getResources().getString(R.string.str_stop_music_mode));
                    sendCount = 0;
                    mArrayListAmplitudeAvg = new ArrayList<>();
                    if (!mActivity.mSwitchCompatOnOff.isChecked()) {
                        mActivity.mSwitchCompatOnOff.setChecked(true);
                    }
                    mHandlerMusic = new Handler();
                    mRunnableMusic = new Runnable() {
                        public void run() {
                            volumeLevel = mSensor.getAmplitudes();
                            if (mAudioManager.isMusicActive()) {
                                System.out.println("JD-MUSIC IN MY DEVICE");
                                if (volumeLevel < 2600) {
                                    volumeLevel = 10 * (volumeLevel / 36000);
                                } else {
                                    volumeLevel = 10 * (volumeLevel / 32000);
                                }
                            } else {
                                if (volumeLevel < 2600) {
                                    volumeLevel = 10 * (volumeLevel / 40000);
                                } else {
                                    volumeLevel = 10 * (volumeLevel / 32000);
                                }
                            }

                            System.out.println("volume = " + (int) Math.round(volumeLevel));
                            mArrayListAmplitudeAvg.add((int) Math.round(volumeLevel));
                            sendCount++;
                            if (sendCount == 5) {
                                sumOfAmplitude = 0;
                                for (int i = 0; i < mArrayListAmplitudeAvg.size(); i++) {
                                    sumOfAmplitude += mArrayListAmplitudeAvg.get(i);
                                }
                                sendCount = 0;
                                mArrayListAmplitudeAvg = new ArrayList<>();

                                if (sumOfAmplitude <= 5) {
                                    averageAmplitude = (int) sumOfAmplitude / 5;
                                } else {
                                    averageAmplitude = (int) Math.round(sumOfAmplitude / 5);
                                }

                                if (averageAmplitude > 10) {
                                    averageAmplitude = 10;
                                }
                                if (averageAmplitude <= 0) {
                                    averageAmplitude = 0;
                                }

                                if (mIntPreviousAvg != averageAmplitude) {
                                    mIntPreviousAvg = averageAmplitude;
                                    sendMusicLight(averageAmplitude);
                                    if (isAdded()) {
                                        mVuMeterView.setSpeed(averageAmplitude * 10);
                                        mVuMeterView.invalidate();
                                        mTextViewVolume.setText(String.valueOf(averageAmplitude));
                                    }
                                }
                            }
                            mHandlerMusic.postDelayed(this, 1000);
                        }
                    };
                    mHandlerMusic.postDelayed(mRunnableMusic, 1000);
                } else {
                    mBooleanIsMusicStart = false;
                    mSensor.stop();
                    mVuMeterView.stop(true);
                    mButtonPlayStop.setText(getResources().getString(R.string.str_start_music_mode));
                    mTextViewVolume.setText("0.0");
                    sendMusicLight(0);
                    if (mHandlerMusic != null) {
                        mHandlerMusic.removeCallbacks(mRunnableMusic);
                        mTextViewVolume.setText("0.0");
                    }
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /*Stop Music mode and stop music animation*/
    private void stopMusicModeWithoutBle() {
        mBooleanIsMusicStart = false;
        mSensor.stop();
        mVuMeterView.stop(true);
        mButtonPlayStop.setText(getResources().getString(R.string.str_start_music_mode));
        mTextViewVolume.setText("0.0");
        if (mHandlerMusic != null) {
            System.out.println("HandlerCancel");
            mHandlerMusic.removeCallbacks(mRunnableMusic);
            mTextViewVolume.setText("0.0");
        }
    }

    /*Send Command to ble light*/
    private void sendMusicLight(int value) {
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            if (mIsFromAllGroup) {
                mActivity.setLightMusic(BLEUtility.intToByte(100), BLEUtility.intToByte(value), Short.parseShort(0 + ""), true);
            } else {
                mActivity.setLightMusic(BLEUtility.intToByte(100), BLEUtility.intToByte(value), Short.parseShort(mIntRandomNo + ""), false);
            }
        } else {
            stopMusicModeWithoutBle();
            mActivity.connectDeviceWithProgress();
        }
    }

    @Override
    public void onPause() {
//        if (mBooleanIsMusicStart) {
//            stopMusicModeWithoutBle();
//            sendMusicLight(0);
//        }
//
        super.onPause();
//        mVuMeterView.pause();
//
//        if (mBooleanIsMusicStart) {
//            mSensor.stop();
//        }
//        if (mHandlerMusic != null) {
//            mHandlerMusic.removeCallbacks(mRunnableMusic);
//        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mActivity.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mVuMeterView.pause();

        if (mBooleanIsMusicStart) {
            stopMusicModeWithoutBle();
            sendMusicLight(0);
            mSensor.stop();
        }
        if (mHandlerMusic != null) {
            mHandlerMusic.removeCallbacks(mRunnableMusic);
        }

        unbinder.unbind();
    }

    /**
     * Called when power button is pressed to on/off light.
     */
    private CompoundButton.OnCheckedChangeListener powerChange = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //    mController.setLightPower(isChecked ? PowerState.ON : PowerState.OFF);
            if (isChecked) {
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    String mSwitchStatus = "ON";
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                            String[] mArray = new String[]{mStringLocalId};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArray);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            } else {
                stopMusicModeWithoutBle();
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    String mSwitchStatus = "OFF";
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                            String[] mArray = new String[]{mStringLocalId};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArray);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            }
        }
    };

    /*Check Audio record permission*/
    @TargetApi(Build.VERSION_CODES.M)
    private void checkMarshmallowPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.RECORD_AUDIO))
            permissionsNeeded.add("Record Audio");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    /* Request permission result*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    getSoundAmplitude();
                } else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                    builder.setTitle("Permissions Required");
                    builder.setCancelable(true);
                    builder.setMessage("You have forcefully denied record audio permissions. Please open settings, go to permissions and allow them.");
                    builder.setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", mActivity.getPackageName(), null));
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied
                }
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied#858585
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (mActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        boolean hasAllPermissions = true;
        for (String permission : permissions) {
            //you can return false instead of assigning, but by assigning you can log all permission values
            if (!hasPermission(context, permission)) {
                hasAllPermissions = false;
            }
        }
        return hasAllPermissions;
    }

    private static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    double getDecibleValue(double d) {
        return (d < 0.0d || d > 25.0d) ? (d < 25.0d || d > 50.0d) ? (d < 50.0d || d > 80.0d) ? (d < 80.0d || d > 140.0d) ? (d < 140.0d || d > 250.0d) ? (d < 250.0d || d > 320.0d) ? (d < 320.0d || d > 450.0d) ? (d < 450.0d || d > 520.0d) ? (d < 520.0d || d > 600.0d) ? (d < 600.0d || d > 670.0d) ? (d < 670.0d || d > 800.0d) ? (d < 800.0d || d > 870.0d) ? (d < 870.0d || d > 950.0d) ? (d < 950.0d || d > 1020.0d) ? 1.0f : 0.0f : 0.9f : 0.0f : 0.8f : 0.7f : 0.6f : 0.0f : 0.5f : 0.4f : 0.3f : 0.0f : 0.2f : 0.0f : 0.1f;
    }

    double getLowDecibleValue(double d) {
        return (d < 0.0d || d > 250.0d) ? (d < 200.0d || d > 300.0d) ? (d < 300.0d || d > 700.0d) ? (d < 700.0d || d > 1000.0d) ? (d < 1000.0d || d > 1100.0d) ? (d < 1100.0d || d > 1400.0d) ? (d < 1400.0d || d > 1500.0d) ? (d < 1500.0d || d > 1800.0d) ? (d < 1800.0d || d > 1900.0d) ? (d < 1900.0d || d > 2200.0d) ? (d < 2200.0d || d > 2300.0d) ? (d < 2300.0d || d > 2600.0d) ? (d < 2600.0d || d > 2700.0d) ? (d < 2700.0d || d > 2900.0d) ? (d < 2900.0d || d > 3000.0d) ? 1.0f : 0.9f : 0.8f : 0.0f : 0.7f : 0.0f : 0.6f : 0.0f : 0.5f : 0.0f : 0.4f : 0.0f : 0.3f : 0.2f : 0.1f : 0.0f;
    }
}