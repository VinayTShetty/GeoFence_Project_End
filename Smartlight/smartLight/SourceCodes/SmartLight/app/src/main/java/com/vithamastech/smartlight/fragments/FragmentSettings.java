package com.vithamastech.smartlight.fragments;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.evergreen.ble.advertisement.ManufactureData;
import com.vithamastech.smartlight.LoginActivity;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.Vo.VoPattern;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentSettings extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    Dialog myDialog;

    RecyclerView mRecyclerViewImage;
    ArrayList<VoPattern> mArrayListPattern = new ArrayList<>();
    ImageListAdapter mImageListAdapter;
    Dialog myDialogSetting;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        return mViewRoot;
    }


    @OnClick(R.id.fragment_settings_ll_bridge)
    public void onBridgeConnectionClick(View mView) {
        FragmentSettingBridgeConnection mFragmentSettingBridgeConnection = new FragmentSettingBridgeConnection();
        mActivity.replacesFragment(mFragmentSettingBridgeConnection, true, null, 0);
    }

    @OnClick(R.id.fragment_settings_ll_master_setting)
    public void onMasterSettingClick(View mView) {
        if (isAdded()) {
            try {
                if (Integer.parseInt(mActivity.getUserActiveCount()) > 0) {
                    mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.frg_setting_master_delete_confirmation_title), getResources().getString(R.string.frg_setting_all_device_delete_confirmation), "Yes", "No", true, 2, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                    if (mActivity.mUtility.haveInternet()) {
                                        checkAuthenticationAPI(true);
                                    } else {
                                        mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_device_not_reset_without_internet), 1, true);
                                    }
                                } else {
                                    resetAllDeviceRequest();
                                }
                            } else {
                                mActivity.connectDeviceWithProgress();
                            }
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_no_device_added_in_account), 3, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_no_device_added_in_account), 3, true);
            }
        }
    }

    @OnClick(R.id.fragment_settings_ll_light_state)
    public void onLastStateClick(View mView) {
        if (isAdded()) {
            showDeviceSettingDialog();
        }
    }

    @OnClick(R.id.fragment_settings_ll_change_background)
    public void onChangePasswordClick(View mView) {
        changeImageDialog();
    }

    /*Call Authentication API*/
    private void checkAuthenticationAPI(final boolean isShowProgress) {
        mActivity.mUtility.hideKeyboard(mActivity);
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress("Please Wait..");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
        params.put("password", mActivity.mPreferenceHelper.getUserPassword());
        Call<VoLogout> mLogin = mActivity.mApiService.authenticateUserCheck(params);
        mLogin.enqueue(new Callback<VoLogout>() {
            @Override
            public void onResponse(Call<VoLogout> call, Response<VoLogout> response) {
                if (isShowProgress) {
                    mActivity.mUtility.HideProgress();
                }
                VoLogout mLoginData = response.body();
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    resetAllDeviceRequest();
                } else {
                    mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_session_expired), 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            mActivity.mPreferenceHelper.ResetPrefData();
                            Intent mIntent = new Intent(mActivity, LoginActivity.class);
                            mIntent.putExtra("is_from_add_account", false);
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mIntent);
                            mActivity.finish();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<VoLogout> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again), 1, true);
            }
        });
    }

    // Todo
    /*Remove all device from user account*/
    private void resetAllDeviceRequest() {
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("DELETE BY SKIP");
                    mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(0 + ""), true, true);
                    mActivity.showProgress("Delete Devices..", true);
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            System.out.println("DELETE BY USER");
                            if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                if (!mActivity.isPingRequestSent) {
                                    mActivity.sendPingRequestToDevice();
                                } else {
                                    mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(0 + ""), true, false);
                                    Timer innerTimer = new Timer();
                                    innerTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(0 + ""), true, false);
                                        }
                                    }, 440);
                                }
                            } else {
                                mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(0 + ""), true, false);
                                Timer innerTimer = new Timer();
                                innerTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(0 + ""), true, false);
                                    }
                                }, 440);
                            }
                            if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
                                resetAllDeviceAPI();
                            } else {
                                deleteAllDevices();
                            }
                        }
                    }, 1000);
                }
            });
        } else {
            mActivity.connectDeviceWithProgress();
        }
    }

    // Todo
    /*Clear all device from the database*/
    private void deleteAllDevices() {
        mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'");
        mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableGroup + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'");
        mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableAlarm + " where " + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'");
        mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableGroupDeviceList + " where " + mActivity.mDbHelper.mFieldGDListUserID + "= '" + mActivity.mPreferenceHelper.getUserId() + "'");
        mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableAlarmDeviceList + " where " + mActivity.mDbHelper.mFieldADUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'");

        String isExistInDB;
        ContentValues mContentValues;
        for (int i = 0; i < 6; i++) {
            isExistInDB = CheckRecordExistInAlarmDB(i + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues = new ContentValues();
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmUserId, mActivity.mPreferenceHelper.getUserId());
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmName, "Alarm " + (i + 1));
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmTime, "");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmStatus, "1");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmDays, "6543210");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmLightOn, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmWakeUpSleep, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmColor, 0);
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmCountNo, i);
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmIsActive, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmIsSync, "0");
                mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableAlarm, mContentValues);
            }
        }
        mActivity.hideProgress();
        showDeviceDeleteAlert();
    }

    /*Show Device setting dialog*/
    private void showDeviceSettingDialog() {
        myDialogSetting = new Dialog(mActivity);
        myDialogSetting.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialogSetting.setContentView(R.layout.popup_device_setting);
        myDialogSetting.setCancelable(true);
        myDialogSetting.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparentWhite)));
        Button mButtonSend = (Button) myDialogSetting
                .findViewById(R.id.popup_device_setting_btn_save);
        Button mButtonCancel = (Button) myDialogSetting
                .findViewById(R.id.popup_device_setting_btn_cancel);
        final RadioGroup mRadioGroupState = (RadioGroup) myDialogSetting.findViewById(R.id.popup_device_setting_rg_power_state);
        System.out.println("getDevice_last_state_remember" + mActivity.mPreferenceHelper.getLightLastState());
        if (mActivity.mPreferenceHelper.getLightLastState() == 0) {
            ((RadioButton) mRadioGroupState.getChildAt(0)).setChecked(true);
        } else if (mActivity.mPreferenceHelper.getLightLastState() == 1) {
            ((RadioButton) mRadioGroupState.getChildAt(1)).setChecked(true);
        } else if (mActivity.mPreferenceHelper.getLightLastState() == 2) {
            ((RadioButton) mRadioGroupState.getChildAt(2)).setChecked(true);
        } else if (mActivity.mPreferenceHelper.getLightLastState() == 3) {
            ((RadioButton) mRadioGroupState.getChildAt(3)).setChecked(true);
        } else {
            ((RadioButton) mRadioGroupState.getChildAt(0)).setChecked(true);
        }
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                mActivity.mUtility.hideKeyboard(mActivity);
                myDialogSetting.dismiss();
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    try {
                        int selectedState;
                        if (mRadioGroupState.getCheckedRadioButtonId() == R.id.popup_device_setting_rb_cool_white) {
                            selectedState = 0;
                        } else if (mRadioGroupState.getCheckedRadioButtonId() == R.id.popup_device_setting_rb_last_set_color) {
                            selectedState = 1;
                        } else if (mRadioGroupState.getCheckedRadioButtonId() == R.id.popup_device_setting_rb_warm_white) {
                            selectedState = 2;
                        } else if (mRadioGroupState.getCheckedRadioButtonId() == R.id.popup_device_setting_rb_mood_lighting) {
                            selectedState = 3;
                        } else {
                            selectedState = 0;
                        }
                        mActivity.mPreferenceHelper.setLightLastState(selectedState);
                        mActivity.setLightLastState(BLEUtility.intToByte(100), BLEUtility.intToByte(selectedState), Short.parseShort(0 + ""), true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }

            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.mUtility.hideKeyboard(mActivity);
                myDialogSetting.dismiss();
            }
        });
        myDialogSetting.show();
        Window window = myDialogSetting.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    /*CHange Image Dialog*/
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
        for (int i = 1; i <= 4; i++) {
            mVoPattern = new VoPattern();
            mVoPattern.setPattern_name("Pattern-" + i);
            mVoPattern.setPattern_value(i + "");
            if (i == 1) {
                mVoPattern.setPattern_image(R.drawable.ic_screen_bg);
            } else if (i == 2) {
                mVoPattern.setPattern_image(R.drawable.ic_screen_bg);
            } else if (i == 3) {
                mVoPattern.setPattern_image(R.drawable.ic_screen_bg);
            } else if (i == 4) {
                mVoPattern.setPattern_image(R.drawable.ic_screen_bg);
            } else {
                mVoPattern.setPattern_image(R.drawable.ic_screen_bg);
            }
            mArrayListPattern.add(mVoPattern);
        }

        mImageListAdapter = new ImageListAdapter();
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
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

    /*Image list adapter*/
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
                    .placeholder(R.drawable.ic_screen_bg)
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
                            int background = Imageposition + 1;
                            mActivity.mPreferenceHelper.setAppBackground(background + "");
                            if (mActivity.mPreferenceHelper.getAppBackground().equals("1")) {
                                background = R.drawable.ic_screen_bg;
                            } else if (mActivity.mPreferenceHelper.getAppBackground().equals("2")) {
                                background = R.drawable.ic_screen_bg;
                            } else if (mActivity.mPreferenceHelper.getAppBackground().equals("3")) {
                                background = R.drawable.ic_screen_bg;
                            } else if (mActivity.mPreferenceHelper.getAppBackground().equals("4")) {
                                background = R.drawable.ic_screen_bg;
                            } else {
                                background = R.drawable.ic_screen_bg;
                            }
                            Glide.with(mActivity)
                                    .load(background)
                                    .centerCrop()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .skipMemoryCache(false)
                                    .crossFade()
                                    .placeholder(R.drawable.ic_screen_bg)
                                    .into(mActivity.mAppCompatImageViewContainer);

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

    /*Check record exist in alarm table or not*/
    public String CheckRecordExistInAlarmDB(String alarmCount) {
        DataHolder mDataHolder;
        String url = "select * from " + mActivity.mDbHelper.mTableAlarm + " where " + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " and " + mActivity.mDbHelper.mFieldAlarmCountNo + "= '" + alarmCount + "'";
        System.out.println(" URL : " + url);
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" AlarmList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldAlarmLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Call Remove all device, group from account*/
    private void resetAllDeviceAPI() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
        params.put("device_token", mActivity.mPreferenceHelper.getDeviceToken());
        Call<VoLogout> mLogin = mActivity.mApiService.resetAllDeviceAPI(params);
        mLogin.enqueue(new Callback<VoLogout>() {
            @Override
            public void onResponse(Call<VoLogout> call, Response<VoLogout> response) {
                VoLogout mAddDeviceAPI = response.body();
                if (mAddDeviceAPI != null && mAddDeviceAPI.getResponse().equalsIgnoreCase("true")) {
                    deleteAllDevices();
                }
            }

            @Override
            public void onFailure(Call<VoLogout> call, Throwable t) {
            }
        });
    }

    /*Delete all device success alert dialog*/
    private void showDeviceDeleteAlert() {
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_all_device_deleted_success), 0, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myDialogSetting != null)
            myDialogSetting.dismiss();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
