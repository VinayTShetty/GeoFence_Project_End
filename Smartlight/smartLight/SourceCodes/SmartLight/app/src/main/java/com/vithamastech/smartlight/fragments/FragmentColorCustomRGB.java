package com.vithamastech.smartlight.fragments;

import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 30-12-2017.
 */

public class FragmentColorCustomRGB extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    int mIntRandomNo = 0;
    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;
    @BindView(R.id.fragment_color_custom_imageview_lamp)
    ImageView mImageViewLamp;
    String mStringLocalId = "";
    String mStringServerId = "";

    IndicatorSeekBar mSeekBarRed;
    IndicatorSeekBar mSeekBarGreen;
    IndicatorSeekBar mSeekBarBlue;
    int mIntRed = 255, mIntGreen = 255, mIntBlue = 255;
    boolean mABooleanColorChange = false;
    int mRGBColor;
    String[] mArrayLocalID;

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
        mViewRoot = inflater.inflate(R.layout.fragment_color_custom, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mSeekBarRed = (IndicatorSeekBar) mViewRoot.findViewById(R.id.fragment_color_custom_seekbar_red);
        mSeekBarGreen = (IndicatorSeekBar) mViewRoot.findViewById(R.id.fragment_color_custom_seekbar_green);
        mSeekBarBlue = (IndicatorSeekBar) mViewRoot.findViewById(R.id.fragment_color_custom_seekbar_blue);
        mActivity.mSwitchCompatOnOff.setOnCheckedChangeListener(powerChange);
        mArrayLocalID = new String[]{mStringLocalId};
        mABooleanColorChange = false;
        mSeekBarRed.setProgress(255);
        mSeekBarGreen.setProgress(255);
        mSeekBarBlue.setProgress(255);
        mRGBColor = Color.rgb(mIntRed, mIntGreen, mIntBlue);
        Drawable mDrawable = mActivity.getResources().getDrawable(R.drawable.unnamed_color);
        mDrawable.setColorFilter(new
                PorterDuffColorFilter(mRGBColor, PorterDuff.Mode.SRC_ATOP));
        mImageViewLamp.setImageDrawable(mDrawable);
//        Red Seek bar change value listener
        mSeekBarRed.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                mIntRed = progress;
                mActivity.mSwitchCompatOnOff.setChecked(true);
                setBulbColor();

            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String tickBelowText, boolean fromUserTouch) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
                mIntRed = progress;
                mActivity.mSwitchCompatOnOff.setChecked(true);
                setBulbColorDirectly();
            }
        });
        //        Green Seek bar change value listener
        mSeekBarGreen.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                mIntGreen = progress;
                mActivity.mSwitchCompatOnOff.setChecked(true);
                setBulbColor();
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String tickBelowText, boolean fromUserTouch) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
                mIntGreen = progress;
                mActivity.mSwitchCompatOnOff.setChecked(true);
                setBulbColorDirectly();
            }
        });
        //        Blue Seek bar change value listener
        mSeekBarBlue.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                mIntBlue = progress;
                mActivity.mSwitchCompatOnOff.setChecked(true);
                setBulbColor();
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String tickBelowText, boolean fromUserTouch) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
                mIntBlue = progress;
                mActivity.mSwitchCompatOnOff.setChecked(true);
                setBulbColorDirectly();
            }
        });
        return mViewRoot;
    }

    int mColorRedBulb;
    int mColorGreenBulb;
    int mColorBlueBulb;
    int mRGBColorBulb;

    /*Change Image Bulb color and send ble command*/
    private void setBulbColor() {
        mRGBColor = Color.rgb(mIntRed, mIntGreen, mIntBlue);
        mColorRedBulb = mIntRed;
        mColorGreenBulb = mIntGreen;
        mColorBlueBulb = mIntBlue;
        mRGBColorBulb = Color.rgb(mColorRedBulb, mColorGreenBulb, mColorBlueBulb);
        Drawable mDrawable = mActivity.getResources().getDrawable(R.drawable.unnamed_color);
        mDrawable.setColorFilter(new
                PorterDuffColorFilter(mRGBColorBulb, PorterDuff.Mode.SRC_ATOP));
        mImageViewLamp.setImageDrawable(mDrawable);
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            if (!mABooleanColorChange) {
                mABooleanColorChange = true;
                if (mIsFromAllGroup) {
                    mActivity.setLightColorRGB(BLEUtility.intToByte(100), BLEUtility.intToByte(mIntRed), BLEUtility.intToByte(mIntGreen), BLEUtility.intToByte(mIntBlue), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true, false);
                } else {
                    mActivity.setLightColorRGB(BLEUtility.intToByte(100), BLEUtility.intToByte(mIntRed), BLEUtility.intToByte(mIntGreen), BLEUtility.intToByte(mIntBlue), BLEUtility.intToByte(0), Short.parseShort(mIntRandomNo + ""), false, false);
                }
                if (!mIsFromGroup) {
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mRGBColor);
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                }
                Timer innerTimer = new Timer();
                innerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mABooleanColorChange = false;
                    }
                }, 420);
            }
        } else {
            mActivity.connectDeviceWithProgress();
        }
    }

    /*Change Image Bulb color and send ble command without delay*/
    private void setBulbColorDirectly() {
        mRGBColor = Color.rgb(mIntRed, mIntGreen, mIntBlue);
        mColorRedBulb = mIntRed;
        mColorGreenBulb = mIntGreen;
        mColorBlueBulb = mIntBlue;
        mRGBColorBulb = Color.rgb(mColorRedBulb, mColorGreenBulb, mColorBlueBulb);

        Drawable mDrawable = mActivity.getResources().getDrawable(R.drawable.unnamed_color);
        mDrawable.setColorFilter(new
                PorterDuffColorFilter(mRGBColorBulb, PorterDuff.Mode.SRC_ATOP));
        mImageViewLamp.setImageDrawable(mDrawable);
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            if (mIsFromAllGroup) {
                mActivity.setLightColorRGB(BLEUtility.intToByte(100), BLEUtility.intToByte(mIntRed), BLEUtility.intToByte(mIntGreen), BLEUtility.intToByte(mIntBlue), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true, false);
            } else {
                mActivity.setLightColorRGB(BLEUtility.intToByte(100), BLEUtility.intToByte(mIntRed), BLEUtility.intToByte(mIntGreen), BLEUtility.intToByte(mIntBlue), BLEUtility.intToByte(0), Short.parseShort(mIntRandomNo + ""), false, false);
            }
            if (!mIsFromGroup) {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mRGBColor);
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
            }
        } else {
            mActivity.connectDeviceWithProgress();
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * Called when power button is pressed on/off.
     */
    private CompoundButton.OnCheckedChangeListener powerChange = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, "ON");
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String mSwitchStatus = "ON";
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, "ON");
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            } else {
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, "OFF");
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String mSwitchStatus = "OFF";
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, "OFF");
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            }
        }
    };
}
