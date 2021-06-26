package com.succorfish.eliteoperator.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;

import com.succorfish.eliteoperator.MainActivity;
import com.succorfish.eliteoperator.R;
import com.succorfish.eliteoperator.interfaces.onDeviceMessegeSent;
import com.succorfish.eliteoperator.interfaces.onResponseGetRefresh;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 06-02-2018.
 */

public class FragmentDeviceSettings extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_device_setting_edittext_gsm_interval)
    AppCompatEditText mEditTextGsmInterval;
    @BindView(R.id.frg_device_setting_edittext_gps_interval)
    AppCompatEditText mEditTextGpsInterval;
    @BindView(R.id.frg_device_setting_edittext_iridium_interval)
    AppCompatEditText mEditTextIridiumInterval;
    @BindView(R.id.frg_device_setting_edittext_gsm_timeout)
    AppCompatEditText mEditTextGsmTimeout;
    @BindView(R.id.frg_device_setting_edittext_gps_timeout)
    AppCompatEditText mEditTextGpsTimeout;
    @BindView(R.id.frg_device_setting_edittext_iridium_timeout)
    AppCompatEditText mEditTextIridiumTimeout;
    @BindView(R.id.frg_device_setting_btn_send)
    AppCompatButton mAppCompatButtonSend;

    String mStringGPSInterval = "";
    String mStringGSMInterval = "";
    String mStringIridiumInterval = "";
    String mStringGPSTimeout = "";
    String mStringGSMTimeout = "";
    String mStringIridiumTimout = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_device_setting, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_setting_txt_device_setting));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        getDeviceData();
        if (mActivity.isDevicesConnected) {
            mActivity.setDeviceSetting(true, new byte[12]);
        } else {
            mActivity.showDisconnectedDeviceAlert(true);
        }

        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });

        mActivity.setResponseGetRefresh(new onResponseGetRefresh() {
            @Override
            public void onRefreshData() {
                if (isAdded()){
                    getDeviceData();
                }
            }
        });

        return mViewRoot;
    }
    private void getDeviceData(){
        mEditTextGsmInterval.setText(mActivity.mPreferenceHelper.getGSMInterval());
        mEditTextGpsInterval.setText(mActivity.mPreferenceHelper.getGPSInterval());
        mEditTextIridiumInterval.setText(mActivity.mPreferenceHelper.getIridiumInterval());
        mEditTextGsmTimeout.setText(mActivity.mPreferenceHelper.getGSMTimeOut());
        mEditTextGpsTimeout.setText(mActivity.mPreferenceHelper.getGPSTimeOut());
        mEditTextIridiumTimeout.setText(mActivity.mPreferenceHelper.getIridiumTimeOut());
    }
    @OnClick(R.id.frg_device_setting_btn_send)
    public void onClickSend(View mView) {
        if (mActivity.isDevicesConnected) {
            mActivity.mUtility.hideKeyboard(mActivity);
            mStringGSMInterval = mEditTextGsmInterval.getText().toString().trim();
            mStringGPSInterval = mEditTextGpsInterval.getText().toString().trim();
            mStringIridiumInterval = mEditTextIridiumInterval.getText().toString().trim();
            mStringGSMTimeout = mEditTextGsmTimeout.getText().toString().trim();
            mStringGPSTimeout = mEditTextGpsTimeout.getText().toString().trim();
            mStringIridiumTimout = mEditTextIridiumTimeout.getText().toString().trim();
            if (mStringGSMInterval.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog("Enter GSM Interval", 1);
                return;
            }
            if (mStringGPSInterval.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog("Enter GPS Interval", 1);
                return;
            }
            if (mStringIridiumInterval.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog("Enter Iridium Interval", 1);
                return;
            }
            if (mStringGSMTimeout.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog("Enter GSM Timeout", 1);
                return;
            }
            if (mStringGPSTimeout.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog("Enter GPS Timeout", 1);
                return;
            }
            if (mStringIridiumTimout.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog("Enter Iridium Timeout", 1);
                return;
            }
            if (Integer.parseInt(mStringGSMInterval) < 0 || Integer.parseInt(mStringGSMInterval) > 65535) {
                mActivity.mUtility.errorDialog("Enter GSM Interval value between 0 to 65535 range", 1);
                return;
            }
            if (Integer.parseInt(mStringGPSInterval) < 0 || Integer.parseInt(mStringGPSInterval) > 65535) {
                mActivity.mUtility.errorDialog("Enter GPS Interval value between 0 to 65535 range", 1);
                return;
            }
            if (Integer.parseInt(mStringIridiumInterval) < 0 || Integer.parseInt(mStringIridiumInterval) > 65535) {
                mActivity.mUtility.errorDialog("Enter Iridium Interval value between 0 to 65535 range", 1);
                return;
            }
            if (Integer.parseInt(mStringGSMTimeout) < 0 || Integer.parseInt(mStringGSMTimeout) > 65535) {
                mActivity.mUtility.errorDialog("Enter GSM Timeout value between 0 to 65535 range", 1);
                return;
            }
            if (Integer.parseInt(mStringGPSTimeout) < 0 || Integer.parseInt(mStringGPSTimeout) > 65535) {
                mActivity.mUtility.errorDialog("Enter GPS Timeout value between 0 to 65535 range", 1);
                return;
            }
            if (Integer.parseInt(mStringIridiumTimout) < 0 || Integer.parseInt(mStringIridiumTimout) > 65535) {
                mActivity.mUtility.errorDialog("Enter Iridium Timeout value between 0 to 65535 range", 1);
                return;
            }
            short mShortGsmInterval = (short) Integer.parseInt(mStringGSMInterval);
            short mShortGpsInterval = (short) Integer.parseInt(mStringGPSInterval);
            short mShortIridiumInterval = (short) Integer.parseInt(mStringIridiumInterval);
            short mShortGsmTimeout = (short) Integer.parseInt(mStringGSMTimeout);
            short mShortGpsTimeout = (short) Integer.parseInt(mStringGPSTimeout);
            short mShortIridiumTimeout = (short) Integer.parseInt(mStringIridiumTimout);

            byte setting_value[] = new byte[12];
            setting_value[0] = (byte) ((mShortGsmInterval) & 0x00FF);
            setting_value[1] = (byte) ((mShortGsmInterval >> 8) & 0x00FF);
            setting_value[2] = (byte) ((mShortGpsInterval) & 0x00FF);
            setting_value[3] = (byte) ((mShortGpsInterval >> 8) & 0x00FF);
            setting_value[4] = (byte) ((mShortIridiumInterval) & 0x00FF);
            setting_value[5] = (byte) ((mShortIridiumInterval >> 8) & 0x00FF);
            setting_value[6] = (byte) ((mShortGsmTimeout) & 0x00FF);
            setting_value[7] = (byte) ((mShortGsmTimeout >> 8) & 0x00FF);
            setting_value[8] = (byte) ((mShortGpsTimeout) & 0x00FF);
            setting_value[9] = (byte) ((mShortGpsTimeout >> 8) & 0x00FF);
            setting_value[10] = (byte) ((mShortIridiumTimeout) & 0x00FF);
            setting_value[11] = (byte) ((mShortIridiumTimeout >> 8) & 0x00FF);
            mActivity.setDeviceSetting(false, setting_value);
        } else {
            mActivity.showDisconnectedDeviceAlert(false);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
    }
}
