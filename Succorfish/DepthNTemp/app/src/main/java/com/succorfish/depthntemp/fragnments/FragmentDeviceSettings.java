package com.succorfish.depthntemp.fragnments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.vo.VoBluetoothDevices;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

//public class FragmentDeviceSettings extends Fragment {
//    MainActivity mActivity;
//    View mViewRoot;
//    private Unbinder unbinder;
//    IndicatorSeekBar mSeekBarStationaryHours;
//    IndicatorSeekBar mSeekBarStationaryMinute;
//    IndicatorSeekBar mSeekBarStationarySeconds;
//    IndicatorSeekBar mSeekBarGpsHours;
//    IndicatorSeekBar mSeekBarGpsMinute;
//    IndicatorSeekBar mSeekBarGpsSeconds;
//    IndicatorSeekBar mSeekBarGpsBleCutOff;
//    @BindView(R.id.frg_device_setting_tv_stationary_interval_hour)
//    TextView mTextViewStationalryHour;
//    @BindView(R.id.frg_device_setting_tv_stationary_interval_minute)
//    TextView mTextViewStationalryMinute;
//    @BindView(R.id.frg_device_setting_tv_stationary_interval_seconds)
//    TextView mTextViewStationalrySeconds;
//    @BindView(R.id.frg_device_setting_tv_gps_interval_hour)
//    TextView mTextViewGpsHour;
//    @BindView(R.id.frg_device_setting_tv_gps_interval_minute)
//    TextView mTextViewGpsMinute;
//    @BindView(R.id.frg_device_setting_tv_gps_interval_seconds)
//    TextView mTextViewGpsSeconds;
//    @BindView(R.id.frg_device_setting_tv_gps_ble_milli_bars)
//    TextView mTextViewGpsBleCutOff;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mActivity = (MainActivity) getActivity();
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        mViewRoot = inflater.inflate(R.layout.fragment_device_setting, container, false);
//        unbinder = ButterKnife.bind(this, mViewRoot);
//        mActivity.mTextViewTitle.setText(R.string.str_settings);
//        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
//        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
//        mSeekBarStationaryHours = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_device_setting_sb_stationary_interval_hour);
//        mSeekBarStationaryMinute = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_device_setting_sb_stationary_interval_minute);
//        mSeekBarStationarySeconds = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_device_setting_sb_stationary_interval_seconds);
//        mSeekBarGpsHours = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_device_setting_sb_gps_interval_hour);
//        mSeekBarGpsMinute = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_device_setting_sb_gps_interval_minute);
//        mSeekBarGpsSeconds = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_device_setting_sb_gps_interval_seconds);
//        mSeekBarGpsBleCutOff = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_device_setting_sb_gps_ble_cut_off);
//
//        mActivity.mImageViewAddDevice.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                FragmentDeviceMissionList fragmentDeviceMissionList = new FragmentDeviceMissionList();
//                mActivity.replacesFragment(fragmentDeviceMissionList, true, null, 0);
//            }
//        });
//        mSeekBarStationaryHours.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
//                mTextViewStationalryHour.setText(progress + " Hours");
//            }
//
//            @Override
//            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
//
//            }
//        });
//        mSeekBarStationaryMinute.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
//                mTextViewStationalryMinute.setText(progress + " Minutes");
//            }
//
//            @Override
//            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
//
//            }
//        });
//        mSeekBarStationarySeconds.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
//                mTextViewStationalrySeconds.setText(progress + " Seconds");
//            }
//
//            @Override
//            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
//
//            }
//        });
//        mSeekBarGpsHours.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
//                mTextViewGpsHour.setText(progress + " Hours");
//            }
//
//            @Override
//            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
//
//            }
//        });
//        mSeekBarGpsMinute.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
//                mTextViewGpsMinute.setText(progress + " Minutes");
//            }
//
//            @Override
//            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
//
//            }
//        });
//        mSeekBarGpsSeconds.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
//                mTextViewGpsSeconds.setText(progress + " Seconds");
//            }
//
//            @Override
//            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
//
//            }
//        });
//        mSeekBarGpsBleCutOff.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
//                mTextViewGpsBleCutOff.setText(progress + " Milli Bars");
//            }
//
//            @Override
//            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String textBelowTick, boolean fromUserTouch) {
//
//            }
//
//            @Override
//            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
//
//            }
//        });
//        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mActivity.onBackPressed();
//            }
//        });
//        return mViewRoot;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
////        mArrayListAddDevice = null;
//        unbinder.unbind();
//        mActivity.mImageViewBack.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//
//}
