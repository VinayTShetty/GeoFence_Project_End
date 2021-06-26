package com.succorfish.depthntemp.fragnments;

import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.db.TablePressureDepthCutOff;
import com.succorfish.depthntemp.helper.BLEUtility;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.interfaces.onAlertDialogCallBack;
import com.succorfish.depthntemp.interfaces.onDeviceConnectionStatusChange;
import com.succorfish.depthntemp.interfaces.onDeviceSettingChange;
import com.succorfish.depthntemp.views.MaterialNumberPicker;
import com.succorfish.depthntemp.vo.VoBluetoothDevices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 16-02-2018.
 */

public class FragmentDeviceSetting extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_device_setting_spinner_transmission)
    Spinner mSpinnerTransmission;
    @BindView(R.id.frg_device_setting_spinner_report_unit)
    Spinner mSpinnerReportUnit;
    //    @BindView(R.id.frg_device_setting_spinner_set_gps_interval)
//    Spinner mSpinnerGpsInterval;
    @BindView(R.id.frg_device_setting_spinner_set_gps_timeout)
    Spinner mSpinnerGpsTimeout;

    @BindView(R.id.frg_device_setting_tv_gps_interval)
    TextView mTextViewGpsInterval;
    @BindView(R.id.frg_device_setting_tv_time)
    TextView mTextViewTime;
    @BindView(R.id.frg_device_setting_tv_selected_device)
    TextView mTextViewSelectedDevice;
    @BindView(R.id.frg_device_setting_tv_set_current_time)
    TextView mTextViewDeviceUTCTime;
    @BindView(R.id.frg_device_setting_tv_battery)
    TextView mTextViewDeviceBattery;
    @BindView(R.id.frg_device_setting_tv_memory_level)
    TextView mTextViewDeviceMemory;
    @BindView(R.id.frg_device_setting_tv_firmware_version)
    TextView mTextViewDeviceFwVersion;
    @BindView(R.id.frg_device_setting_iv_battery)
    ImageView mImageViewBattery;
    @BindView(R.id.frg_device_setting_rg_frq_int_time)
    RadioGroup mRadioGroupFrqTime;

    Calendar newCalendar;
    SimpleDateFormat mSimpleDateFormatDate;
    ArrayList<String> mTransmissionList = new ArrayList<>();
    ArrayList<String> mListGpsInterval = new ArrayList<>();
    ArrayList<String> mListGpsTimeout = new ArrayList<>();
    List<TablePressureDepthCutOff> mListDepthCutOff = new ArrayList<>();
    TransmissionListAdapter mTransmissionListAdapter;
    ReportUnitListAdapter mReportUnitListAdapter;
    GpsIntervalAdapter mGpsIntervalAdapter;
    GpsTimeoutListAdapter mGpsTimeoutListAdapter;
    private SimpleDateFormat mSimpleDateFormat;
    startGetDeviceSettingTimer mStartGetDeviceSettingTimer;
    int second;
    int mIntGpsIntervalMinute = 0;
    int stationaryInterval = 5;// 5 Sec Default
    int stationaryIntervalType = 3; // 1-Hour,2-Min,3-Sec
    boolean isDepthValueChange = false;
    boolean isBleTransmissionValueChange = false;
    boolean isGpsIntervalValueChange = false;
    boolean isGpsTimeoutValueChange = false;
    Dialog myDialog;
    RadioGroup mRadioGroupGpsInterval;
    int mIntGpsIntervalPosition = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_device_settings, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_map_marker_white_18dp);
        mSimpleDateFormatDate = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        mSimpleDateFormat = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm:ss", Locale.getDefault());
        mStartGetDeviceSettingTimer = new startGetDeviceSettingTimer(18000, 2000);
        newCalendar = Calendar.getInstance();

//        System.out.println("JD-ON_REFRESH_SETTING");
        /*Init Transmission list*/
        mTransmissionList = new ArrayList<>();
        mTransmissionList.add("Always");
        mTransmissionList.add("After dive 10 Min");
        /*Init gps time out list*/
        for (int i = 0; i <= 60; i++) {
            mListGpsTimeout.add(i + "");
        }
        /*Init Report List Adapter*/
        mReportUnitListAdapter = new ReportUnitListAdapter();
        mSpinnerReportUnit.setAdapter(mReportUnitListAdapter);
        mSpinnerReportUnit.setSelection(0, false);

        /*Init Transmission list Adapter*/
        mTransmissionListAdapter = new TransmissionListAdapter();
        mSpinnerTransmission.setAdapter(mTransmissionListAdapter);
        mSpinnerTransmission.setSelection(0, false);

        /*Init Gps list Adapter*/
        mGpsTimeoutListAdapter = new GpsTimeoutListAdapter();
        mSpinnerGpsTimeout.setAdapter(mGpsTimeoutListAdapter);
        mSpinnerGpsTimeout.setSelection(0, false);

        /*get depth cut off list from local db*/
        new GetDepthCutOffList().execute();

//        mCheckBelowWater.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    mLinearLayoutBelowWater.setVisibility(View.VISIBLE);
//                } else {
//                    mLinearLayoutBelowWater.setVisibility(View.GONE);
//                }
//            }
//        });
//        mCheckPressure.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    mRelativeLayoutUnit.setVisibility(View.VISIBLE);
//                } else {
//                    mRelativeLayoutUnit.setVisibility(View.GONE);
//                }
//            }
//        });
//        mCheckBoxTime.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    mRelativeLayoutFrqTime.setVisibility(View.VISIBLE);
//                } else {
//                    mRelativeLayoutFrqTime.setVisibility(View.GONE);
//                }
//            }
//        });
        if (mActivity.isDevicesConnected) {
            mTextViewSelectedDevice.setText(mActivity.mStringConnectedDevicesAddress.replace(":", ""));
            mActivity.showProgress("Fetching Device Settings..", false);
            if (mStartGetDeviceSettingTimer != null)
                mStartGetDeviceSettingTimer.start();

        } else {
//            mActivity.mUtility.errorDialogWithCallBack("Device is disconnected. Please connect and try again.", 1, false, new onAlertDialogCallBack() {
//                @Override
//                public void PositiveMethod(DialogInterface dialog, int id) {
//                    if (mActivity.mSmartTabLayout != null) {
//                        mActivity.mSmartTabLayout.getTabAt(0).select();
//                    }
//                }
//
//                @Override
//                public void NegativeMethod(DialogInterface dialog, int id) {
//
//                }
//            });
        }
        mRadioGroupFrqTime.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mActivity.mUtility.hideKeyboard(mActivity);
//                if (checkedId == R.id.frg_device_setting_rb_hour) {
//                    if (stationaryIntervalType == 1) {
//                        mTextViewTime.setText("Interval : " + stationaryInterval + " Hour");
//                    } else {
//                        mTextViewTime.setText("Interval : " + "1 Hour");
//                    }
//
//                } else if (checkedId == R.id.frg_device_setting_rb_min) {
//                    if (stationaryIntervalType == 2) {
//                        mTextViewTime.setText("Interval : " + stationaryInterval + " Minute");
//                    } else {
//                        mTextViewTime.setText("Interval : " + "5 Minute");
//                    }
//                } else {
//                    if (stationaryIntervalType == 3) {
//                        mTextViewTime.setText("Interval : " + stationaryInterval + " Seconds");
//                    } else {
//                        mTextViewTime.setText("Interval : " + "1 Seconds");
//                    }
//                }
            }
        });
        mSpinnerReportUnit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isDepthValueChange = true;
                return false;
            }
        });
        mSpinnerReportUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                // Depth in m
                if (isDepthValueChange) {
                    System.out.println("JD-SETTING--OnReportItem Event FIRE");
                    if (mActivity.isDevicesConnected) {
                        try {
                            short mByteCommand = (short) 0x0005;
                            mActivity.setCommandData(mByteCommand);
                            Timer outerTimer = new Timer();
                            outerTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mActivity.setReadingDepth((int) mListDepthCutOff.get(position).getDepthInMillBar());
                                    Timer innerTimer = new Timer();
                                    innerTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            isDepthValueChange = false;
                                            getDepthCutOffCommand();
                                        }
                                    }, 500);
                                }
                            }, 500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {
                        mActivity.showDisconnectedDeviceAlert();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerTransmission.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isBleTransmissionValueChange = true;
                return false;
            }
        });
        mSpinnerTransmission.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                // Depth in m
                if (isBleTransmissionValueChange) {
                    System.out.println("JD-SETTING--Transmission Event FIRE");
                    if (mActivity.isDevicesConnected) {
                        try {
                            short mByteCommand = (short) 0x000E;
                            mActivity.setCommandData(mByteCommand);

                            Timer outerTimer = new Timer();
                            outerTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    if (position == 0) {
                                        short mCommandTransmission = (short) 0xFF;
                                        mActivity.setBleTransmissionData(mCommandTransmission);
                                    } else {
                                        short mCommandTransmission = (short) 0x0A;
                                        mActivity.setBleTransmissionData(mCommandTransmission);
                                        mActivity.mLeDevices.clear();
                                    }
                                    Timer innerTimer = new Timer();
                                    innerTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            isBleTransmissionValueChange = false;
                                            getBluetoothTransmissionCommand();
                                        }
                                    }, 500);
                                }
                            }, 500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mActivity.showDisconnectedDeviceAlert();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
//        mSpinnerGpsInterval.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                isGpsIntervalValueChanage = true;
//                return false;
//            }
//        });
        mSpinnerGpsTimeout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                isGpsTimeoutValueChange = true;
                return false;
            }
        });
//        mSpinnerGpsInterval.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
//                // Depth in m
//                if (isGpsIntervalValueChanage) {
//                    System.out.println("JD-SETTING--GPS Interval Event FIRE");
//                    if (mActivity.isDevicesConnected) {
//                        try {
//                            short mByteCommand = (short) 0x0004;
//                            mActivity.setCommandData(mByteCommand);
//
//                            Timer outerTimer = new Timer();
//                            outerTimer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    mActivity.setGpsIntervalData((Integer.parseInt(mListGpsInterval.get(position))));
//                                    Timer innerTimer = new Timer();
//                                    innerTimer.schedule(new TimerTask() {
//                                        @Override
//                                        public void run() {
//                                            isGpsIntervalValueChanage = false;
//                                            getGpsIntervalCommand();
//                                        }
//                                    }, 500);
//                                }
//                            }, 500);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    } else {
//                        mActivity.showDisconnectedDeviceAlert();
//                    }
//                }
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//
//            }
//        });
        mSpinnerGpsTimeout.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, final int position, long id) {
                // Depth in m
                if (isGpsTimeoutValueChange) {
                    System.out.println("JD-SETTING--GPS Interval Event FIRE");
                    if (mActivity.isDevicesConnected) {
                        try {
                            short mByteCommand = (short) 0x0007;
                            mActivity.setCommandData(mByteCommand);

                            Timer outerTimer = new Timer();
                            outerTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mActivity.setGpsTimeoutData((Integer.parseInt(mListGpsTimeout.get(position))));
                                    Timer innerTimer = new Timer();
                                    innerTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            isGpsTimeoutValueChange = false;
                                            getGpsTimeoutCommand();
                                        }
                                    }, 500);
                                }
                            }, 500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mActivity.showDisconnectedDeviceAlert();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mActivity.setOnDevicesSettingChange(new onDeviceSettingChange() {
            @Override
            public void onDeviceFwChange(String fwChange) {
                if (isAdded()) {
                    onFirmwareValueChange();
                }
            }

            @Override
            public void onDeviceMemoryChange(String memoryChange) {
                if (isAdded()) {
                    onMemoryValueChange();
                }
            }

            @Override
            public void onDeviceTimeChange(String timeChange) {
                if (isAdded()) {
                    onTimeValueChange();
                }
            }

            @Override
            public void onDeviceBatteryChange(String batteryChange) {
                if (isAdded()) {
                    onBatteryValueChange();
                }
            }

            @Override
            public void onDeviceStationaryIntervalChange(String batteryChange) {
                if (isAdded()) {
                    onStationaryIntervalValueChange();
                }
            }

            @Override
            public void onDeviceDepthCutOffChange(String batteryChange) {
                if (isAdded()) {
                    onDepthCutOffValueChange();
                }
            }

            @Override
            public void onDeviceBleTransmissionChange(String batteryChange) {
                if (isAdded()) {
                    onBleTransmissionChange();
                }
            }

            @Override
            public void onDeviceGpsIntervalChange(String batteryChange) {
                if (isAdded()) {
                    onGpsIntervalValueChange();
                }
            }

            @Override
            public void onDeviceGpsTimeoutChange(String batteryChange) {
                if (isAdded()) {
                    onGpsTimeoutValueChange();
                }
            }
        });
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (isAdded()) {

                }
            }

            @Override
            public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {

                }
            }

            @Override
            public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                    System.out.println("BridgeConnection DisConnect");
                    if (mActivity.mSmartTabLayout != null) {
                        mActivity.mSmartTabLayout.getTabAt(0).select();
                    }
                }
            }

            @Override
            public void onError() {

            }
        });
        mActivity.mImageViewAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mActivity.isDevicesConnected) {
                    short mByteGetCommand = (short) 0xFF03;
                    mActivity.setCommandData(mByteGetCommand);
                } else {
                    mActivity.showDisconnectedDeviceAlert();
                }
            }
        });

//        setDeviceSettingData();
        return mViewRoot;
    }

    private void setDeviceSettingData() {
        if (isAdded()) {
            try {
                onFirmwareValueChange();
                onMemoryValueChange();
                onTimeValueChange();
                onBatteryValueChange();
                onStationaryIntervalValueChange();
//                onDepthCutOffValueChange();
//                onBleTransmissionChange();
//                mActivity.mDeviceSettingFetch=true;
                onGpsIntervalValueChange();
                onGpsTimeoutValueChange();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.frg_device_setting_tv_change_time)
    public void onSelectDateFormatClick(View mView) {
        if (isAdded()) {
            int selected = 5;
            if (mRadioGroupFrqTime.getCheckedRadioButtonId() == R.id.frg_device_setting_rb_hour) {
                if (stationaryIntervalType == 1) {
                    selected = stationaryInterval;
                } else {
                    selected = 1;
                }

            } else if (mRadioGroupFrqTime.getCheckedRadioButtonId() == R.id.frg_device_setting_rb_min) {
                if (stationaryIntervalType == 2) {
//                    mTextViewTime.setText("Interval : " + stationaryInterval + " Minute");
                    selected = stationaryInterval;
                } else {
//                    mTextViewTime.setText("Interval : " + "5 Minute");
                    selected = 5;
                }
            } else {
                if (stationaryIntervalType == 3) {
//                    mTextViewTime.setText("Interval : " + stationaryInterval + " Seconds");
                    selected = stationaryInterval;
                } else {
//                    mTextViewTime.setText("Interval : " + "1 Seconds");
                    selected = 1;
                }
            }
            showStartTimeDialog(selected);
        }
    }

    @OnClick(R.id.frg_device_setting_tv_gps_interval)
    public void onGpsIntervalClick(View mView) {
        if (isAdded()) {
            showGpsIntervalDialog(mIntGpsIntervalMinute);
        }
    }

    @OnClick(R.id.frg_device_setting_rl_set_time)
    public void onUTCTimeSetClick(View mView) {
        if (isAdded()) {
            if (mActivity.isDevicesConnected) {
                mActivity.setCurrentUTCTime();
                mActivity.mUtility.errorDialog("Device time set successfully.", 0);
                Timer innerTimer = new Timer();
                innerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        getUtcTimeCommand();
                    }
                }, 500);

            } else {
                mActivity.showDisconnectedDeviceAlert();
            }

        }
    }

//    @OnClick(R.id.frg_device_setting_rl_firmware_version)
//    public void onFirmwareVersionClick(View mView) {
//        if (isAdded()) {
//            if (mActivity.isDevicesConnected) {
//
//                mActivity.mUtility.errorDialogWithYesNoCallBack("Start/Stop Dummy Data?", "Are you sure you want to add dummy device data?", "START", "STOP", true, 2, new onAlertDialogCallBack() {
//                    @Override
//                    public void PositiveMethod(DialogInterface dialog, int id) {
//                        if (mActivity.isDevicesConnected) {
//                            short mByteGetCommand = (short) 0xFFF1;
//                            mActivity.setCommandData(mByteGetCommand);
//                        } else {
//                            mActivity.showDisconnectedDeviceAlert();
//                        }
//                    }
//
//                    @Override
//                    public void NegativeMethod(DialogInterface dialog, int id) {
//                        if (mActivity.isDevicesConnected) {
//                            short mByteGetCommand = (short) 0xFFF2;
//                            mActivity.setCommandData(mByteGetCommand);
//                        } else {
//                            mActivity.showDisconnectedDeviceAlert();
//                        }
//                    }
//                });
//            } else {
//                mActivity.showDisconnectedDeviceAlert();
//            }
//        }
//    }


    @OnClick(R.id.frg_device_setting_rl_erase)
    public void onEraseDataClick(View mView) {
        if (isAdded()) {
            if (mActivity.isDevicesConnected) {
                mActivity.mUtility.errorDialogWithYesNoCallBack("Erase Device Data?", "Are you sure you want to erase device data?", "YES", "NO", true, 2, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        if (mActivity.isDevicesConnected) {
                            short mByteCommand = (short) 0x0003;
                            mActivity.setCommandData(mByteCommand);
                            mActivity.mUtility.errorDialog("Device data erase successfully.", 0);
                            Timer innerTimer = new Timer();
                            innerTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    getDeviceMemory();
                                }
                            }, 500);
                        } else {
                            mActivity.showDisconnectedDeviceAlert();
                        }
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            } else {
                mActivity.showDisconnectedDeviceAlert();
            }

        }
    }

    /*Update Ui on Batter value change*/
    private void onBatteryValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringDeviceBattery != null && !mActivity.mStringDeviceBattery.equalsIgnoreCase("")) {
                    mTextViewDeviceBattery.setText(mActivity.mStringDeviceBattery + "%");
                    if (Integer.parseInt(mActivity.mStringDeviceBattery) <= 10) {
                        mImageViewBattery.setImageResource(R.drawable.battery_empty);
                    } else if (Integer.parseInt(mActivity.mStringDeviceBattery) > 10 && Integer.parseInt(mActivity.mStringDeviceBattery) <= 40) {
                        mImageViewBattery.setImageResource(R.drawable.battery_quater);
                    } else if (Integer.parseInt(mActivity.mStringDeviceBattery) > 40 && Integer.parseInt(mActivity.mStringDeviceBattery) <= 60) {
                        mImageViewBattery.setImageResource(R.drawable.battery_half);
                    } else if (Integer.parseInt(mActivity.mStringDeviceBattery) > 60 && Integer.parseInt(mActivity.mStringDeviceBattery) <= 90) {
                        mImageViewBattery.setImageResource(R.drawable.battery_third);
                    } else {
                        mImageViewBattery.setImageResource(R.drawable.battery_full);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Firmware value change*/
    private void onFirmwareValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringDeviceFirmwareVersion != null && !mActivity.mStringDeviceFirmwareVersion.equalsIgnoreCase("null") && !mActivity.mStringDeviceFirmwareVersion.equalsIgnoreCase("")) {
                    if (mActivity.mStringDeviceFirmwareVersion.length() > 2) {
                        mTextViewDeviceFwVersion.setText("v" + BLEUtility.hexToDecimal(mActivity.mStringDeviceFirmwareVersion.substring(0, 2)) + "." + BLEUtility.hexToDecimal(mActivity.mStringDeviceFirmwareVersion.substring(2, mActivity.mStringDeviceFirmwareVersion.length())));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Memory value change*/
    private void onMemoryValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringDeviceMemory != null && !mActivity.mStringDeviceMemory.equalsIgnoreCase("")) {
                    mTextViewDeviceMemory.setText(mActivity.mStringDeviceMemory + "%");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Ble transmission value change*/
    private void onBleTransmissionChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringDeviceBleTransmission != null && !mActivity.mStringDeviceBleTransmission.equalsIgnoreCase("")) {
//                    System.out.println("Ble Transmission-" + mActivity.mStringDeviceBleTransmission);
                    if (mActivity.mStringDeviceBleTransmission.equalsIgnoreCase("0A")) {
                        mSpinnerTransmission.setSelection(1, false);
                    } else {
                        mSpinnerTransmission.setSelection(0, false);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Time value change*/
    private void onTimeValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringDeviceTime != null && !mActivity.mStringDeviceTime.equalsIgnoreCase("")) {
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(Long.parseLong(mActivity.mStringDeviceTime));
                    mTextViewDeviceUTCTime.setText(mSimpleDateFormat.format(mCalendar.getTime()));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Stationary interval value change*/
    private void onStationaryIntervalValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringDeviceStationaryInterval != null && !mActivity.mStringDeviceStationaryInterval.equalsIgnoreCase("")) {
                    if (Integer.parseInt(mActivity.mStringDeviceStationaryInterval) >= 3600) {
                        ((RadioButton) mRadioGroupFrqTime.getChildAt(0)).setChecked(true);
                        stationaryInterval = (Integer.parseInt(mActivity.mStringDeviceStationaryInterval) / 3600);
                        mTextViewTime.setText("Interval : " + stationaryInterval + " Hour");
                        stationaryIntervalType = 1;
                    } else if (Integer.parseInt(mActivity.mStringDeviceStationaryInterval) >= 300 && Integer.parseInt(mActivity.mStringDeviceStationaryInterval) < 3600) {
                        ((RadioButton) mRadioGroupFrqTime.getChildAt(1)).setChecked(true);
                        stationaryInterval = (Integer.parseInt(mActivity.mStringDeviceStationaryInterval) / 60);
                        mTextViewTime.setText("Interval : " + stationaryInterval + " Minute");
                        stationaryIntervalType = 2;
                    } else {
                        ((RadioButton) mRadioGroupFrqTime.getChildAt(2)).setChecked(true);
                        stationaryInterval = (Integer.parseInt(mActivity.mStringDeviceStationaryInterval));
                        mTextViewTime.setText("Interval : " + stationaryInterval + " Seconds");
                        stationaryIntervalType = 3;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Depth cut off value change*/
    private void onDepthCutOffValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringDeviceReadingDepthCutoff != null && !mActivity.mStringDeviceReadingDepthCutoff.equalsIgnoreCase("")) {
                    System.out.println("JD-SETTING--DepthCutOFF-" + mActivity.mStringDeviceReadingDepthCutoff);
                    new MyTaskDepthCutOff().execute();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Gps interval value change*/
    private void onGpsIntervalValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringGpsInterval != null && !mActivity.mStringGpsInterval.equalsIgnoreCase("")) {
                    System.out.println("JD-SETTING--GpsInterval-" + mActivity.mStringGpsInterval);
                    mIntGpsIntervalMinute = Integer.parseInt(mActivity.mStringGpsInterval);
                    if (mIntGpsIntervalMinute < 60) {
                        mTextViewGpsInterval.setText(mIntGpsIntervalMinute + " Min");
                    } else {
                        mTextViewGpsInterval.setText((mIntGpsIntervalMinute / 60) + " Hour");
                    }
//                    mSpinnerGpsInterval.setSelection(Integer.parseInt(mActivity.mStringGpsInterval), false);
                } else {
                    mIntGpsIntervalMinute = 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Update Ui on Gps Timeout value change*/
    private void onGpsTimeoutValueChange() {
        try {
            if (isAdded()) {
                if (mActivity.mStringGpsTimeout != null && !mActivity.mStringGpsTimeout.equalsIgnoreCase("")) {
                    System.out.println("JD-SETTING--GpsTimeout-" + mActivity.mStringGpsTimeout);
                    mSpinnerGpsTimeout.setSelection(Integer.parseInt(mActivity.mStringGpsTimeout), false);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Get Depth cut off list from local db*/
    private class GetDepthCutOffList extends AsyncTask<String, Integer, List<TablePressureDepthCutOff>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<TablePressureDepthCutOff> doInBackground(String... params) {
            return mActivity.mAppRoomDatabase.depthCutOffDao().getAllDepthCutOffList();
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<TablePressureDepthCutOff> mCutOffList) {
            super.onPostExecute(mCutOffList);
            mListDepthCutOff = mCutOffList;
            mReportUnitListAdapter = new ReportUnitListAdapter();
            mSpinnerReportUnit.setAdapter(mReportUnitListAdapter);
            mSpinnerReportUnit.setSelection(0, false);
        }
    }

    private class MyTaskDepthCutOff extends AsyncTask<String, Integer, TablePressureDepthCutOff> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected TablePressureDepthCutOff doInBackground(String... params) {
            return mActivity.mAppRoomDatabase.depthCutOffDao().getRecordBasedOnMilibar(Integer.parseInt(mActivity.mStringDeviceReadingDepthCutoff));
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(TablePressureDepthCutOff mTablePressureDepthCutOff) {
            super.onPostExecute(mTablePressureDepthCutOff);
            if (mTablePressureDepthCutOff != null) {
                System.out.println("JD-SETTING--NOT NUll");
                mSpinnerReportUnit.setSelection(mTablePressureDepthCutOff.getCutOffId() - 1, false);
            } else {
                System.out.println("JD-SETTING--NUll");
                mSpinnerReportUnit.setSelection(0, false);
            }
        }
    }

    /*Time Dialog*/
    private void showStartTimeDialog(int selectedNo) {
        final NumberPicker picker;
        MaterialNumberPicker.Builder numberPickerBuilder = new MaterialNumberPicker.Builder(mActivity);
        int minValue;
        int maxValue;
        if (mRadioGroupFrqTime.getCheckedRadioButtonId() == R.id.frg_device_setting_rb_hour) {
            minValue = 1;
            maxValue = 4;
        } else if (mRadioGroupFrqTime.getCheckedRadioButtonId() == R.id.frg_device_setting_rb_min) {
            minValue = 5;
            maxValue = 59;
        } else {
            minValue = 1;
            maxValue = 299;
        }
        numberPickerBuilder
                .minValue(minValue)
                .maxValue(maxValue)
                .defaultValue(selectedNo)
                .separatorColor(ContextCompat.getColor(mActivity, R.color.colorAccent))
                .textColor(ContextCompat.getColor(mActivity, R.color.colorPrimary))
                .textSize(25);
//                .formatter(new NumberPicker.Formatter() {
//                    @Override
//                    public String format(int value) {
////                        mTextViewTime.setText(value);
//                        return value + "";
//
//                    }
//                });
        picker = numberPickerBuilder.build();
        new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle)
                .setTitle("Select Interval")
                .setView(picker)
                .setNegativeButton(getString(android.R.string.cancel), null)
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mActivity.isDevicesConnected) {
                            short mByteCommand = (short) 0x0001;
                            mActivity.setCommandData(mByteCommand);
                            System.out.println("selectedValue-" + picker.getValue());
                            if (mRadioGroupFrqTime.getCheckedRadioButtonId() == R.id.frg_device_setting_rb_hour) {
                                mTextViewTime.setText("Interval : " + picker.getValue() + " Hour");
                                second = (picker.getValue() * 3600);
                            } else if (mRadioGroupFrqTime.getCheckedRadioButtonId() == R.id.frg_device_setting_rb_min) {
                                mTextViewTime.setText("Interval : " + picker.getValue() + " Minute");
                                second = (picker.getValue() * 60);
                            } else {
                                mTextViewTime.setText("Interval : " + picker.getValue() + " Second");
                                second = picker.getValue();
                            }
                            Timer outerTimer = new Timer();
                            outerTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    System.out.println("second-" + second);
                                    mActivity.setStatMeasInterval(second);
                                    Timer innerTimer = new Timer();
                                    innerTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            getStationaryIntervalCommand();
                                        }
                                    }, 500);
                                }
                            }, 500);
                        } else {
                            mActivity.showDisconnectedDeviceAlert();
                        }

                    }
                })
                .show();
//        mTimePickerStartTimeDialog = new TimePickerDialog(mActivity, R.style.DialogTheme,
//                new TimePickerDialog.OnTimeSetListener() {
//                    @Override
//                    public void onTimeSet(TimePicker timePicker,
//                                          int selectedHour, int selectedMinute) {
////                        mStartDate.setHours(selectedHour);
////                        mStartDate.setMinutes(selectedMinute);
//                        newCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
//                        newCalendar.set(Calendar.MINUTE, selectedMinute);
//                        newCalendar.set(Calendar.SECOND, 00);
//                        mTextViewTime.setText(mSimpleDateFormatDate.format(newCalendar.getTime()));
//                    }
//                }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);// Yes 24 hour time
//        mTimePickerStartTimeDialog.setTitle("Select Time");
//        mTimePickerStartTimeDialog.show();
    }

    /*Show Gps Interval Dialog*/
    public void showGpsIntervalDialog(int gpsIntervalMin) {
        myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_gps_interval_dialog);
        myDialog.setCancelable(true);
        ColorDrawable back = new ColorDrawable(mActivity.getResources().getColor(R.color.colorWhite));
        InsetDrawable inset = new InsetDrawable(back, 0);
        myDialog.getWindow().setBackgroundDrawable(inset);
        Button mButtonCancel = (Button) myDialog
                .findViewById(R.id.popup_device_setting_btn_cancel);
        Button mButtonSave = (Button) myDialog
                .findViewById(R.id.popup_device_setting_btn_save);
        RecyclerView mRecyclerViewImage = (RecyclerView) myDialog.findViewById(R.id.popup_gps_interval_dialog_rv_gps_interval);
        mRadioGroupGpsInterval = (RadioGroup) myDialog.findViewById(R.id.popup_gps_interval_dialog_rg_gps_interval);
        mListGpsInterval.clear();

        if (gpsIntervalMin < 60) {
            mIntGpsIntervalPosition = gpsIntervalMin;
            ((RadioButton) mRadioGroupGpsInterval.getChildAt(0)).setChecked(true);
            for (int i = 0; i < 60; i++) {
                mListGpsInterval.add(i + "");
            }
        } else {
            mIntGpsIntervalPosition = (gpsIntervalMin / 60);
            if (mIntGpsIntervalPosition > 60) {
                mIntGpsIntervalPosition = 60;
            }
            mIntGpsIntervalPosition = mIntGpsIntervalPosition - 1;
            ((RadioButton) mRadioGroupGpsInterval.getChildAt(1)).setChecked(true);
            for (int i = 1; i <= 60; i++) {
                mListGpsInterval.add(i + "");
            }
        }
        mGpsIntervalAdapter = new GpsIntervalAdapter();
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewImage.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false));
        mRecyclerViewImage.setHasFixedSize(true);
        mRecyclerViewImage.setAdapter(mGpsIntervalAdapter);

        mGpsIntervalAdapter.setSelectedPosition(mIntGpsIntervalPosition);
        mRadioGroupGpsInterval.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.popup_gps_interval_dialog_rb_min) {
                    mListGpsInterval.clear();
                    for (int i = 0; i < 60; i++) {
                        mListGpsInterval.add(i + "");
                    }
                    if (mGpsIntervalAdapter != null) {
                        mGpsIntervalAdapter.setSelectedPosition(0);
                        mGpsIntervalAdapter.notifyDataSetChanged();
                    }

                } else {
                    mListGpsInterval.clear();
                    for (int i = 1; i <= 60; i++) {
                        mListGpsInterval.add(i + "");
                    }
                    if (mGpsIntervalAdapter != null) {
                        mGpsIntervalAdapter.setSelectedPosition(0);
                        mGpsIntervalAdapter.notifyDataSetChanged();
                    }
                }
            }
        });


        mButtonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                myDialog.dismiss();
            }
        });
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isAdded()) {
                    if (mActivity.isDevicesConnected) {
                        try {
                            short mByteCommand = (short) 0x0004;
                            mActivity.setCommandData(mByteCommand);
                            Timer outerTimer = new Timer();
                            outerTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mIntGpsIntervalPosition = mGpsIntervalAdapter.getSelectedPosition();
                                    if (mRadioGroupGpsInterval.getCheckedRadioButtonId() == R.id.popup_gps_interval_dialog_rb_min) {
                                        mIntGpsIntervalMinute = (Integer.parseInt(mListGpsInterval.get(mGpsIntervalAdapter.getSelectedPosition())));
                                        mActivity.setGpsIntervalData((Integer.parseInt(mListGpsInterval.get(mGpsIntervalAdapter.getSelectedPosition()))));
                                        mTextViewGpsInterval.setText(mListGpsInterval.get(mGpsIntervalAdapter.getSelectedPosition()) + " Min");
                                    } else {
                                        mIntGpsIntervalMinute = (Integer.parseInt(mListGpsInterval.get(mGpsIntervalAdapter.getSelectedPosition())) * 60);
                                        mActivity.setGpsIntervalData(mIntGpsIntervalMinute);
                                        mTextViewGpsInterval.setText(mListGpsInterval.get(mGpsIntervalAdapter.getSelectedPosition()) + " Hour");
                                    }
                                    Timer innerTimer = new Timer();
                                    innerTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            isGpsIntervalValueChange = false;
                                            getGpsIntervalCommand();
                                            myDialog.dismiss();
                                        }
                                    }, 500);
                                }
                            }, 500);
                        } catch (Exception e) {
                            e.printStackTrace();
                            myDialog.dismiss();
                        }
                    } else {
                        myDialog.dismiss();
                        mActivity.showDisconnectedDeviceAlert();
                    }
                } else {
                    myDialog.dismiss();
                }
            }
        });
//        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
//        lp.copyFrom(myDialog.getWindow().getAttributes());
//        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
//        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        myDialog.show();
//        myDialog.getWindow().setAttributes(lp);
    }

    /*Gps Interval Adapter*/
    public class GpsIntervalAdapter extends RecyclerView.Adapter<GpsIntervalAdapter.ViewHolder> {
        private int lastSelectedPosition = 0;

        @Override
        public GpsIntervalAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_gps_interval, parent, false);
            return new GpsIntervalAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(GpsIntervalAdapter.ViewHolder holder, final int position) {
            if (getSelectedPosition() == position) {
                holder.mRadioButtonGpsInterval.setChecked(true);
            } else {
                holder.mRadioButtonGpsInterval.setChecked(false);
            }
            if (mRadioGroupGpsInterval.getCheckedRadioButtonId() == R.id.popup_gps_interval_dialog_rb_min) {
                holder.mTextViewGpsInterval.setText(mListGpsInterval.get(position) + " Minute");
            } else {
                holder.mTextViewGpsInterval.setText(mListGpsInterval.get(position) + " Hour");
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListGpsInterval != null) {
                        if (position < mListGpsInterval.size()) {
                            lastSelectedPosition = position;
                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        public int getSelectedPosition() {
            return lastSelectedPosition;
        }

        public void setSelectedPosition(int selected) {
            lastSelectedPosition = selected;
        }

        @Override
        public int getItemCount() {
            return mListGpsInterval.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_gps_interval_tv_interval)
            TextView mTextViewGpsInterval;
            @BindView(R.id.raw_gps_interval_rb_interval)
            RadioButton mRadioButtonGpsInterval;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Transmission List Adapter*/
    public class TransmissionListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mTransmissionList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTransmissionList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
            mTextViewCategoryName.setText(mTransmissionList.get(position));
            return view;
        }
    }

    /*Report Unit list adapter*/
    public class ReportUnitListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mListDepthCutOff.size();
        }

        @Override
        public Object getItem(int position) {
            return mListDepthCutOff.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
            mTextViewCategoryName.setText(mListDepthCutOff.get(position).getDepthInMeter() + " M");
            return view;
        }
    }

    //    public class GpsIntervalListAdapter extends BaseAdapter {
//
//        @Override
//        public int getCount() {
//            return mListGpsInterval.size();
//        }
//
//        @Override
//        public Object getItem(int position) {
//            return mListGpsInterval.get(position);
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            View view = convertView;
//            if (convertView == null) {
//                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
//            }
//            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
//            mTextViewCategoryName.setText(mListGpsInterval.get(position) + " Seconds");
//            return view;
//        }
//    }
    /*Gps Timeout list Adapter*/
    public class GpsTimeoutListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mListGpsTimeout.size();
        }

        @Override
        public Object getItem(int position) {
            return mListGpsTimeout.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
            mTextViewCategoryName.setText(mListGpsTimeout.get(position) + " Min");
            return view;
        }
    }

    /*Get Gps Timeout*/
    private void getGpsTimeoutCommand() {
        mActivity.mIntGetSettingType = 9;
        short mByteGetCommand = (short) 0xFF07;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Gps Interval*/
    private void getGpsIntervalCommand() {
        mActivity.mIntGetSettingType = 8;
        short mByteGetCommand = (short) 0xFF04;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Bluetooth Transmission value*/
    private void getBluetoothTransmissionCommand() {
        mActivity.mIntGetSettingType = 7;
        short mByteGetCommand = (short) 0xFF0E;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Depth Cut Off Value*/
    private void getDepthCutOffCommand() {
        mActivity.mIntGetSettingType = 6;
        short mByteGetCommand = (short) 0xFF05;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Gps Stationary Interval Value*/
    private void getStationaryIntervalCommand() {
        mActivity.mIntGetSettingType = 5;
        short mByteGetCommand = (short) 0xFF01;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Utc Time Value*/
    private void getUtcTimeCommand() {
        mActivity.mIntGetSettingType = 4;
        short mByteGetCommand = (short) 0xFF06;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Firmware version Value*/
    private void getFirmwareVersionCommand() {
        mActivity.mIntGetSettingType = 3;
        short mByteGetCommand = (short) 0xFF09;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Battery Value*/
    private void getBatteryCommand() {
        mActivity.mIntGetSettingType = 2;
        short mByteGetCommand = (short) 0xFF0A;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get Device Memory Value*/
    private void getDeviceMemory() {
        mActivity.mIntGetSettingType = 1;
        short mByteGetCommand = (short) 0xFF0B;
        mActivity.setCommandData(mByteGetCommand);
    }

    /*Get ALL Device Setting value*/
    private class startGetDeviceSettingTimer extends CountDownTimer {
        public startGetDeviceSettingTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
            System.out.println("JD-millisUntilFinished-" + millisUntilFinished);
            // Note : Increase timer time while fetch more setting
            if (millisUntilFinished <= 18000 && millisUntilFinished > 16000) {
                // Gps Timeout
                getGpsTimeoutCommand();
            }
            if (millisUntilFinished <= 16000 && millisUntilFinished > 14000) {
                // Gps Interval
                getGpsIntervalCommand();
            }
            if (millisUntilFinished <= 14000 && millisUntilFinished > 12000) {
                // Bluetooth Transmission
                getBluetoothTransmissionCommand();
            }
            if (millisUntilFinished <= 12000 && millisUntilFinished > 10000) {
                // Reading Depth cutoff
                getDepthCutOffCommand();
            } else if (millisUntilFinished <= 10000 && millisUntilFinished > 8000) {
                // Stationary Interval Time
                getStationaryIntervalCommand();
            } else if (millisUntilFinished <= 8000 && millisUntilFinished > 6000) {
                // Device UTC Time
                getUtcTimeCommand();
            } else if (millisUntilFinished <= 6000 && millisUntilFinished > 4000) {
                // Device Firmware Version
                getFirmwareVersionCommand();
            } else if (millisUntilFinished <= 4000 && millisUntilFinished > 2000) {
                // Device Battery Level
                getBatteryCommand();
            } else if (millisUntilFinished <= 2000) {
                // Device Memory
                getDeviceMemory();
            }
        }

        @Override
        public void onFinish() {
            System.out.println("FINISH");
            mActivity.hideProgress();
//            setDeviceSettingData();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (mStartGetDeviceSettingTimer != null)
            mStartGetDeviceSettingTimer.cancel();
    }
}
