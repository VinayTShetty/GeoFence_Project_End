package com.vithamastech.smartlight.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.evergreen.ble.advertisement.ManufactureData;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.ColorPicker;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoPattern;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.ColorUtil;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 25-12-2017.
 */

public class FragmentColorWheel extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    IndicatorSeekBar mIndicatorSeekBar;
    @BindView(R.id.baseColorPicker)
    ColorPicker colorPicker;
    short mIntRandomNo = 0;
    int mIntSeekSelectedValue = 100;
    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;

    boolean mABooleanColorChange = false;
    boolean mABooleanIsReady = false;
    boolean mIsColorChange = false;
    String mStringLocalId = "";
    String mStringServerId = "";
    RecyclerView mRecyclerViewImage;
    ArrayList<VoPattern> mArrayListPattern = new ArrayList<>();
    ImageListAdapter mImageListAdapter;
    Dialog myDialog;
    int mSelectedImage = 1;
    String[] mArrayLocalID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mIntRandomNo = Short.parseShort(getArguments().getInt("intent_device_id", 0) + "");
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
        mViewRoot = inflater.inflate(R.layout.fragment_color_wheel, container, false);
        colorPicker = new ColorPicker(mActivity);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mIndicatorSeekBar = (IndicatorSeekBar) mViewRoot.findViewById(R.id.fragment_device_set_color_indicator_seekbar);
        mActivity.mSwitchCompatOnOff.setOnCheckedChangeListener(powerChange);
        colorPicker.setColorPicker(R.drawable.color_picker_circle);
        mSelectedImage = Integer.parseInt(mActivity.mPreferenceHelper.getWheelBackground());
        colorPicker.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        mArrayLocalID = new String[]{mStringLocalId};
        if (mSelectedImage == 1) {
            colorPicker.setGradientView(R.drawable.ic_wheel_two, true);
            colorPicker.setBackgroundColor(getResources().getColor(R.color.colorAppTheam));
        } else if (mSelectedImage == 2) {
            colorPicker.setGradientView(R.drawable.ic_wheel_five, true);
        }

        mIntSeekSelectedValue = 100;
        if (!mIsFromGroup) {
            String queryDeviceBrightness = "select " + mActivity.mDbHelper.mFieldDeviceBrightness + " from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceLocalId + "= '" + mStringLocalId + "'";
            mIntSeekSelectedValue = mActivity.mDbHelper.getQueryIntResult(queryDeviceBrightness);
        }
        System.out.println("mIntSeekChangeValueInit=" + mIntSeekSelectedValue);
        mABooleanColorChange = false;
        mABooleanIsReady = false;
        mIsColorChange = false;

//        int mCurrentColorWithoutAlpha = Color.rgb(Color.red(mActivity.mCurrentColorWheel), Color.green(mActivity.mCurrentColorWheel), Color.blue(mActivity.mCurrentColorWheel));
        if (mIndicatorSeekBar != null) {
            mIndicatorSeekBar.setProgress(mIntSeekSelectedValue);
//            mIndicatorSeekBar.getBuilder().setProgress(mIntSeekSelectedValue).apply();
        }
        /*Brightness value change listener*/
        mIndicatorSeekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                mIntSeekSelectedValue = progress;
                if (mABooleanIsReady) {
                    mActivity.mSwitchCompatOnOff.setChecked(true);
                    if (mIsColorChange) {
                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                            if (!mABooleanColorChange) {
                                mABooleanColorChange = true;
                                if (mIsFromAllGroup) {
                                    mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(0 + ""), true);
                                } else {
                                    mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(mIntRandomNo + ""), false);
                                }
                                if (!mIsFromGroup) {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mActivity.mCurrentColorWheel);
                                    mContentValues.put(mActivity.mDbHelper.mFieldDeviceBrightness, mIntSeekSelectedValue);
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                                }
                                updateOnOffState(true);
                                Timer innerTimer = new Timer();
                                innerTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        mABooleanColorChange = false;
                                    }
                                }, 440);
                            }
                        } else {
                            mActivity.connectDeviceWithProgress();
                        }
                    } else {

                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                            if (!mABooleanColorChange) {
                                mABooleanColorChange = true;
                                if (mIsFromAllGroup) {
                                    mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(0 + ""), true);
                                } else {
                                    mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(mIntRandomNo + ""), false);
                                }
                                if (!mIsFromGroup) {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mActivity.mCurrentColorWheel);
                                    mContentValues.put(mActivity.mDbHelper.mFieldDeviceBrightness, mIntSeekSelectedValue);
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                                }
                                updateOnOffState(true);
                                Timer innerTimer = new Timer();
                                innerTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        mABooleanColorChange = false;
                                    }
                                }, 440);
                            }

                        } else {
                            mActivity.connectDeviceWithProgress();
                        }
                    }
                }

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
                mIntSeekSelectedValue = progress;
                mABooleanColorChange = false;
                if (mABooleanIsReady) {
                    mActivity.mSwitchCompatOnOff.setChecked(true);
                    if (mIsColorChange) {
                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                            if (mIsFromAllGroup) {
                                mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(0 + ""), true);
                            } else {
                                mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(mIntRandomNo + ""), false);
                            }
                            if (!mIsFromGroup) {
                                ContentValues mContentValues = new ContentValues();
                                mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mActivity.mCurrentColorWheel);
                                mContentValues.put(mActivity.mDbHelper.mFieldDeviceBrightness, mIntSeekSelectedValue);
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                            }
                            updateOnOffState(true);
                        } else {
                            mActivity.connectDeviceWithProgress();
                        }
                    } else {

                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                            if (mIsFromAllGroup) {
                                mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(0 + ""), true);
                            } else {
                                mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort(mIntRandomNo + ""), false);
                            }
                            if (!mIsFromGroup) {
                                ContentValues mContentValues = new ContentValues();
                                mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mActivity.mCurrentColorWheel);
                                mContentValues.put(mActivity.mDbHelper.mFieldDeviceBrightness, mIntSeekSelectedValue);
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                            }
                            updateOnOffState(true);

                        } else {
                            mActivity.connectDeviceWithProgress();
                        }
                    }
                }
            }
        });
        /*get Selected color from touch*/
        colorPicker.setColorSelectedListener(new ColorPicker.ColorSelectedListener() {
            @Override
            public void onColorSelected(int pixelColor, boolean isTapUp) {
                mActivity.mSwitchCompatOnOff.setChecked(true);
                mActivity.mCurrentColorWheel = pixelColor;
                if (ColorUtil.isColorOverBlack(pixelColor)) {
                    mActivity.mCurrentColorWheel = Color.rgb(255, 255, 255);
                }

                mIsColorChange = true;
                if (mIndicatorSeekBar != null) {
                    mIndicatorSeekBar.setProgress(mIntSeekSelectedValue);
//                    mIndicatorSeekBar.getBuilder().setIndicatorColor(mActivity.mCurrentColorWheel).setProgress(mIntSeekSelectedValue).setThumbColor(mActivity.mCurrentColorWheel).setProgressTrackColor(mActivity.mCurrentColorWheel).setTickColor(mActivity.mCurrentColorWheel).apply();
                }
                if (isTapUp) {
                    if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                        if (mIsFromAllGroup) {
                            mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, Short.parseShort("0"), true);
                        } else {
                            mActivity.setLightColor(BLEUtility.intToByte(100), mActivity.mCurrentColorWheel, mIntSeekSelectedValue, mIntRandomNo, false);
                        }
                        if (!mIsFromGroup) {
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mActivity.mCurrentColorWheel);
                            mContentValues.put(mActivity.mDbHelper.mFieldDeviceBrightness, mIntSeekSelectedValue);
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                        }
                        updateOnOffState(true);
                    } else {
                        mActivity.connectDeviceWithProgress();
                    }

                }
            }
        });
        Timer innerTimer = new Timer();
        innerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mABooleanIsReady = true;
            }
        }, 200);
        return mViewRoot;
    }

    @OnClick(R.id.frg_color_wheel_choose_image)
    public void onChangeImageButtonClick(View mView) {
        if (isAdded()) {
            changeImageDialog();
        }
    }

    /*Show change image dialog*/
    public void changeImageDialog() {
        myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_choose_image);
        myDialog.setCancelable(true);
        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorTransparent));
        InsetDrawable inset = new InsetDrawable(back, 0);
        myDialog.getWindow().setBackgroundDrawable(inset);
        Button mButtonSend = (Button) myDialog
                .findViewById(R.id.popup_choose_image_button_cancel);
        mRecyclerViewImage = (RecyclerView) myDialog.findViewById(R.id.popup_choose_image_recyclerview);

        mArrayListPattern = new ArrayList<>();
        VoPattern mVoPattern;
        for (int i = 1; i <= 2; i++) {
            mVoPattern = new VoPattern();
            mVoPattern.setPattern_name("Pattern-" + i);
            mVoPattern.setPattern_value(i + "");
            if (i == 1) {
                mVoPattern.setPattern_image(R.drawable.ic_wheel_two);
            } else if (i == 2) {
                mVoPattern.setPattern_image(R.drawable.ic_wheel_five);
            } else {
                mVoPattern.setPattern_image(R.drawable.ic_wheel_two);
            }
            mArrayListPattern.add(mVoPattern);
        }

        mImageListAdapter = new ImageListAdapter();
        mRecyclerViewImage.setLayoutManager(new GridLayoutManager(mActivity, 2));
        mRecyclerViewImage.setHasFixedSize(true);
        mRecyclerViewImage.setAdapter(mImageListAdapter);
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                myDialog.dismiss();
            }
        });
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(myDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        myDialog.show();
        myDialog.getWindow().setAttributes(lp);
    }

    /*Image List adapter*/
    public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {

        @Override
        public ImageListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_image_list_item, parent, false);
            return new ImageListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ImageListAdapter.ViewHolder holder, final int position) {
            Glide.with(mActivity)
                    .load(mArrayListPattern.get(position).getPattern_image())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .crossFade()
                    .placeholder(R.drawable.ic_wheel_two)
                    .into(holder.mImageView);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListPattern != null) {
                        if (position < mArrayListPattern.size()) {
                            if (myDialog != null) {
                                myDialog.dismiss();
                            }
                            int Imageposition = position;
                            mSelectedImage = Imageposition + 1;
                            colorPicker.setColorPicker(R.drawable.color_picker_circle);
                            mActivity.mPreferenceHelper.setWheelBackground(mSelectedImage + "");
                            colorPicker.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                            if (mSelectedImage == 1) {
                                colorPicker.setGradientView(R.drawable.ic_wheel_two, true);
                                colorPicker.setBackgroundColor(getResources().getColor(R.color.colorAppTheam));
                            } else if (mSelectedImage == 2) {
                                colorPicker.setGradientView(R.drawable.ic_wheel_five, true);
                            } else {
                                colorPicker.setGradientView(R.drawable.ic_wheel_two, true);
                            }
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListPattern.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.frg_list_item_imageview_wheel)
            ImageView mImageView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /**
     * Called when power button is pressed.
     */
    private CompoundButton.OnCheckedChangeListener powerChange = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mABooleanIsReady) {
                if (isChecked) {
                    if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                        if (mIsFromAllGroup) {
                            mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(0 + ""), true);
                        } else {
                            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(mIntRandomNo + ""), false);
                        }
                        updateOnOffState(true);
                    } else {
                        mActivity.connectDeviceWithProgress();
                    }
                } else {
                    if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                        if (mIsFromAllGroup) {
                            mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true);
                        } else {
                            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(mIntRandomNo + ""), false);

                        }
                        updateOnOffState(false);
                    } else {
                        mActivity.connectDeviceWithProgress();
                    }
                }
            }
        }
    };

    /*Update on/off light state with db and send command to ble*/
    private void updateOnOffState(boolean isChecked) {
        if (isChecked) {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                ContentValues mContentValues = new ContentValues();
                String mSwitchStatus = "ON";
                if (mIsFromAllGroup) {
                    mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                    mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                    String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                    String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                    mActivity.mDbHelper.exeQuery(url);
                } else {
                    if (mIsFromGroup) {
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                    }
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        } else {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                ContentValues mContentValues = new ContentValues();
                String mSwitchStatus = "OFF";
                if (mIsFromAllGroup) {
                    mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                    mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                    String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                    String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                    mActivity.mDbHelper.exeQuery(url);
                } else {
                    if (mIsFromGroup) {
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                    }
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mIndicatorSeekBar = null;
        colorPicker = null;
        unbinder.unbind();
    }
}
