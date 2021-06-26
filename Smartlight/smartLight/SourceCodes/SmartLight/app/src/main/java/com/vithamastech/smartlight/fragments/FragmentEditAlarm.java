package com.vithamastech.smartlight.fragments;

import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.evergreen.ble.advertisement.ManufactureData;
import com.libRG.CustomTextView;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.CustomToggleButton;
import com.vithamastech.smartlight.Vo.VoAlarm;
import com.vithamastech.smartlight.Vo.VoAlarmDays;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onBackPressWithAction;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentEditAlarm extends Fragment {
    String TAG = FragmentEditAlarm.class.getSimpleName();
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    @BindView(R.id.frg_add_alarm_textview_time)
    TextView mTextViewAlarmTime;
    @BindView(R.id.frg_add_alarm_textview_time_ampm)
    TextView mTextViewAlarmTimeAMPM;
    @BindView(R.id.frg_add_alarm_textview_no_device)
    TextView mTextViewNoDeviceFound;
    @BindView(R.id.frg_add_alarm_tv_wakeup_sleep_title)
    TextView mTextViewWakeUpSleep;
    @BindView(R.id.frg_add_alarm_imageview_selected_color)
    CustomTextView mTextViewSelectedColor;

    @BindView(R.id.frg_add_alarm_recyclerview_device)
    RecyclerView mRecyclerView;
    @BindView(R.id.frg_add_alarm_recyclerview_days)
    RecyclerView mRecyclerViewDays;
    @BindView(R.id.frg_add_alarm_radiogroup_light_on_off)
    RadioGroup mRadioGroupLightOnOff;
    @BindView(R.id.frg_add_alarm_radiobutton_on)
    RadioButton mRadioButtonLightOn;
    @BindView(R.id.frg_add_alarm_radiobutton_off)
    RadioButton mRadioButtonLightOff;
    @BindView(R.id.fragment_add_linearlayout_alarm_color)
    RelativeLayout mLinearLayoutAlarmColor;
    @BindView(R.id.frg_add_alarm_cb_wakeup_sleep)
    AppCompatCheckBox mAppCompatCheckBoxWakeUpSleep;
    Calendar mCalendar;
    TimePickerDialog mTimePickerDialog;
    int mCalendarHour, mCalendarMinute;

    public SimpleDateFormat mDateFormatDb;
    private SimpleDateFormat mTimeFormatter;

    ArrayList<VoDeviceList> mArrayListDevice = new ArrayList<>();
    ArrayList<VoDeviceList> mArrayListCheckedDevice = new ArrayList<>();

    ArrayList<VoAlarmDays> mArrayListDays = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    DaysListAdapter mDaysListAdapter;
    boolean isAlarmAdded = false;
    int mAlarmColor = Color.rgb(255, 255, 255);
    int currentLoopPosition = 0;
    startDeviceScanTimer mStartDeviceScanTimer;
    ArrayList<VoBluetoothDevices> mLeDevicesTemp = new ArrayList<>();
    String mStringSelectedDays = "";
    String mStringDaysMask = "0";
    int mIntMaxAlarmCount = 0;

    VoAlarm mVoAlarmLocal;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        mTimeFormatter = new SimpleDateFormat("hh:mm a");
        if (getArguments() != null) {
            mVoAlarmLocal = (VoAlarm) getArguments().getSerializable("intent_vo_alarm");
        }
        mCalendar = Calendar.getInstance();
        mCalendarHour = mCalendar.get(Calendar.HOUR_OF_DAY);
        mCalendarMinute = mCalendar.get(Calendar.MINUTE);
        if (mVoAlarmLocal != null) {
            if (mVoAlarmLocal.getAlarm_time() != null && !mVoAlarmLocal.getAlarm_time().equals("") && !mVoAlarmLocal.getAlarm_time().equals("null")) {
                try {
                    Date date = mTimeFormatter.parse(mVoAlarmLocal.getAlarm_time());
                    mCalendar.setTime(date);
                    mCalendarHour = mCalendar.get(Calendar.HOUR_OF_DAY);
                    mCalendarMinute = mCalendar.get(Calendar.MINUTE);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (mVoAlarmLocal.getAlarm_color() != null && !mVoAlarmLocal.getAlarm_color().equals("") && !mVoAlarmLocal.getAlarm_color().equals("null")) {
                try {
                    mAlarmColor = Integer.parseInt(mVoAlarmLocal.getAlarm_color());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (mVoAlarmLocal.getAlarm_count_no() != null && !mVoAlarmLocal.getAlarm_count_no().equals("") && !mVoAlarmLocal.getAlarm_count_no().equals("null")) {
                try {
                    mIntMaxAlarmCount = Integer.parseInt(mVoAlarmLocal.getAlarm_count_no());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_add_alarm, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_alarm_edit_title);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.VISIBLE);
        mActivity.mTextViewAdd.setText("Save");
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mStartDeviceScanTimer = new startDeviceScanTimer(8000, 1000);
        mActivity.isAddDeviceScan = true;
        isAlarmAdded = false;


        displayCurrentTime();
        displayDays();
        getDBDeviceList();

        if (mAlarmColor == 0) {
            mRadioButtonLightOff.setChecked(true);
            mRadioButtonLightOn.setChecked(false);
            mAlarmColor = Color.rgb(255, 255, 255);
            mLinearLayoutAlarmColor.setVisibility(View.GONE);

        } else {
            mRadioButtonLightOff.setChecked(false);
            mRadioButtonLightOn.setChecked(true);
            mLinearLayoutAlarmColor.setVisibility(View.VISIBLE);
        }

        mTextViewSelectedColor.setBackgroundColor(mAlarmColor);
        mRadioGroupLightOnOff.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.frg_add_alarm_radiobutton_on) {
                    mLinearLayoutAlarmColor.setVisibility(View.VISIBLE);
                    mTextViewWakeUpSleep.setText(getResources().getText(R.string.frg_alarm_wake_up_title));
                } else {
                    mTextViewWakeUpSleep.setText(getResources().getText(R.string.frg_alarm_sleep_off_title));
                    mLinearLayoutAlarmColor.setVisibility(View.GONE);
                }
            }
        });
        if (mVoAlarmLocal != null) {
            if (mVoAlarmLocal.getAlarm_wake_up_sleep() != null && !mVoAlarmLocal.getAlarm_wake_up_sleep().equals("") && mVoAlarmLocal.getAlarm_wake_up_sleep().equals("1")) {
                mAppCompatCheckBoxWakeUpSleep.setChecked(true);
            } else {
                mAppCompatCheckBoxWakeUpSleep.setChecked(false);
            }
            if (mRadioGroupLightOnOff.getCheckedRadioButtonId() == R.id.frg_add_alarm_radiobutton_on) {
                mTextViewWakeUpSleep.setText(getResources().getText(R.string.frg_alarm_wake_up_title));
            } else {
                mTextViewWakeUpSleep.setText(getResources().getText(R.string.frg_alarm_sleep_off_title));
            }
        }
        /*Device connections, scan call back*/
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {
            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.ALARM_ADD_RSP) || mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.ALARM_REMOVE_RSP)) {
                    boolean containsInScanDevice = false;
                    for (VoBluetoothDevices device : mLeDevicesTemp) {
                        if (mVoBluetoothDevices.getDeviceHexData().equals(device.getDeviceHexData())) {
                            containsInScanDevice = true;
                            break;
                        }
                    }
                    if (!containsInScanDevice) {
                        mLeDevicesTemp.add(mVoBluetoothDevices);
                    }
                }
            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices, ManufactureData manufactureData) {

            }

            @Override
            public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
            }

            @Override
            public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {

            }

            @Override
            public void onError() {
            }
        });
        /*Edit alarm click handler*/
        mActivity.mTextViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    mActivity.isAddDeviceScan = true;
                    mActivity.mLeDevicesTemp = new ArrayList<>();
                    mLeDevicesTemp = new ArrayList<>();
                    mActivity.RescanDevice(false);
                    mArrayListCheckedDevice = new ArrayList<>();
                    currentLoopPosition = 0;
                    for (int i = 0; i < mArrayListDevice.size(); i++) {
                        if (mArrayListDevice.get(i).getIsDeviceAlradyInGroup()) {
                            mArrayListCheckedDevice.add(mArrayListDevice.get(i));
                        } else {
                            if (mArrayListDevice.get(i).getIsChecked()) {
                                mArrayListCheckedDevice.add(mArrayListDevice.get(i));
                            }
                        }
                    }
                    mStringSelectedDays = "";
                    mStringDaysMask = "0";
                    for (int j = mArrayListDays.size() - 1; j >= 0; j--) {
                        if (mArrayListDays.get(j).getIsIs_day_checked()) {
                            mStringDaysMask = mStringDaysMask + "1";
                            mStringSelectedDays = mStringSelectedDays + j + "";
                        } else {
                            mStringDaysMask = mStringDaysMask + "0";
                        }
                    }
                    if (mStringSelectedDays != null && !mStringSelectedDays.equalsIgnoreCase("") && !mStringSelectedDays.equalsIgnoreCase("null")) {
                        if (mArrayListCheckedDevice.size() > 0) {
                            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                mActivity.showProgress("Routine Setting...", true);
                                addingAlarmDeviceRequest();
                            } else {
                                mActivity.connectDeviceWithProgress();
                            }
                        } else {
                            mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_alarm_add_at_least_one_device), 3, true);
                        }
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_alarm_select_routine_day), 3, true);
                    }
                }
            }
        });
        return mViewRoot;
    }

    /*Display Selected Days*/
    private void displayDays() {
        if (mDaysListAdapter == null) {
            VoAlarmDays mVoAlarmDays;
            mArrayListDays = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                mVoAlarmDays = new VoAlarmDays();
                mVoAlarmDays.setIs_day_checked(false);
                if (j == 0) {
                    mVoAlarmDays.setDays_name("S");
                    if (mVoAlarmLocal.getAlarm_days() != null && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("") && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("null")) {
                        if (mVoAlarmLocal.getAlarm_days().contains("0")) {
                            mVoAlarmDays.setIs_day_checked(true);
                        }
                    }
                } else if (j == 1) {
                    mVoAlarmDays.setDays_name("M");
                    if (mVoAlarmLocal.getAlarm_days() != null && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("") && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("null")) {
                        if (mVoAlarmLocal.getAlarm_days().contains("1")) {
                            mVoAlarmDays.setIs_day_checked(true);
                        }
                    }
                } else if (j == 2) {
                    mVoAlarmDays.setDays_name("T");
                    if (mVoAlarmLocal.getAlarm_days() != null && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("") && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("null")) {
                        if (mVoAlarmLocal.getAlarm_days().contains("2")) {
                            mVoAlarmDays.setIs_day_checked(true);
                        }
                    }
                } else if (j == 3) {
                    mVoAlarmDays.setDays_name("W");
                    if (mVoAlarmLocal.getAlarm_days() != null && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("") && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("null")) {
                        if (mVoAlarmLocal.getAlarm_days().contains("3")) {
                            mVoAlarmDays.setIs_day_checked(true);
                        }
                    }
                } else if (j == 4) {
                    mVoAlarmDays.setDays_name("T");
                    if (mVoAlarmLocal.getAlarm_days() != null && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("") && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("null")) {
                        if (mVoAlarmLocal.getAlarm_days().contains("4")) {
                            mVoAlarmDays.setIs_day_checked(true);
                        }
                    }
                } else if (j == 5) {
                    mVoAlarmDays.setDays_name("F");
                    if (mVoAlarmLocal.getAlarm_days() != null && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("") && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("null")) {
                        if (mVoAlarmLocal.getAlarm_days().contains("5")) {
                            mVoAlarmDays.setIs_day_checked(true);
                        }
                    }
                } else if (j == 6) {
                    mVoAlarmDays.setDays_name("S");
                    if (mVoAlarmLocal.getAlarm_days() != null && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("") && !mVoAlarmLocal.getAlarm_days().equalsIgnoreCase("null")) {
                        if (mVoAlarmLocal.getAlarm_days().contains("6")) {
                            mVoAlarmDays.setIs_day_checked(true);
                        }
                    }
                }
                mArrayListDays.add(mVoAlarmDays);
            }
        }
        mDaysListAdapter = new DaysListAdapter();
        mRecyclerViewDays.setLayoutManager(new GridLayoutManager(mActivity, 7));
        mRecyclerViewDays.setAdapter(mDaysListAdapter);
    }

    /*Display Current Time*/
    private void displayCurrentTime() {

        int selectedHour = mCalendarHour;
        int selectedMinute = mCalendarMinute;

        String mStrFormat;
        if (selectedHour == 0) {
            selectedHour += 12;
            mStrFormat = "AM";
        } else if (selectedHour == 12) {
            mStrFormat = "PM";
        } else if (selectedHour > 12) {
            selectedHour -= 12;
            mStrFormat = "PM";
        } else {
            mStrFormat = "AM";
        }
        String min;
        String hour;
        if (selectedHour < 10)
            hour = "0" + selectedHour;
        else
            hour = String.valueOf(selectedHour);

        if (selectedMinute < 10)
            min = "0" + selectedMinute;
        else
            min = String.valueOf(selectedMinute);
        mTextViewAlarmTime.setText(new StringBuilder().append(hour).append(':')
                .append(min).toString());
        mTextViewAlarmTimeAMPM.setText(mStrFormat);

    }

    @OnClick(R.id.frg_add_alarm_textview_time)
    public void onAddAlarmClick(View mView) {
        if (isAdded()) {
            int dialogTheme = 0;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                dialogTheme = R.style.DialogTheme;
            }
            mTimePickerDialog = new TimePickerDialog(mActivity, dialogTheme,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker timePicker,
                                              int selectedHour, int selectedMinute) {
                            mCalendarHour = selectedHour;
                            mCalendarMinute = selectedMinute;
                            String mStrFormat;
                            if (selectedHour == 0) {
                                selectedHour += 12;
                                mStrFormat = "AM";
                            } else if (selectedHour == 12) {
                                mStrFormat = "PM";
                            } else if (selectedHour > 12) {
                                selectedHour -= 12;
                                mStrFormat = "PM";
                            } else {
                                mStrFormat = "AM";
                            }
                            String min;
                            String hour;
                            if (selectedHour < 10)
                                hour = "0" + selectedHour;
                            else
                                hour = String.valueOf(selectedHour);

                            if (selectedMinute < 10)
                                min = "0" + selectedMinute;
                            else
                                min = String.valueOf(selectedMinute);
                            mTextViewAlarmTime.setText(new StringBuilder().append(hour).append(':')
                                    .append(min).toString());
                            mTextViewAlarmTimeAMPM.setText(mStrFormat);

                        }
                    }, mCalendarHour, mCalendarMinute, false);// Yes 24 hour time
            mTimePickerDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    mActivity.mUtility.hideKeyboard(mActivity);
                }
            });
            mTimePickerDialog.setTitle(getResources().getString(R.string.frg_add_routine_time));
            mTimePickerDialog.show();

        }
    }

    @OnClick(R.id.fragment_add_ll_alarm_wake_up_sleep)
    public void onWakeUpSleepClick(View mView) {
        if (mAppCompatCheckBoxWakeUpSleep.isChecked()) {
            mAppCompatCheckBoxWakeUpSleep.setChecked(false);
        } else {
            mAppCompatCheckBoxWakeUpSleep.setChecked(true);
        }
    }

    @OnClick(R.id.fragment_add_linearlayout_alarm_color)
    public void onAlarmColorClick(View mView) {
        if (isAdded()) {
            FragmentColorAlarm mFragmentColorAlarm = new FragmentColorAlarm();
            Bundle mBundle = new Bundle();
            mBundle.putInt("mIntent_selected_color", mAlarmColor);
            mFragmentColorAlarm.setOnColorResultSet(new onBackPressWithAction() {
                @Override
                public void onBackWithAction(int color) {
                    mAlarmColor = color;

                }

                @Override
                public void onBackWithAction(String color, String brightness) {

                }

                @Override
                public void onBackWithAction(String color, String brightness, String deviceID) {

                }
            });
            mActivity.replacesFragment(mFragmentColorAlarm, true, mBundle, 1);
        }
    }
    /*Send ble command for add alarm to device*/
    private void addingAlarmDeviceRequest() {
        if (currentLoopPosition >= mArrayListCheckedDevice.size()) {
            mActivity.hideProgress();
            return;
        }
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            if (mArrayListCheckedDevice.get(currentLoopPosition).getIsDeviceAlradyInGroup() && !mArrayListCheckedDevice.get(currentLoopPosition).getIsChecked()) {
                try {
                    System.out.println(TAG + "currentLoopPosition=" + currentLoopPosition);
                    isAlarmAdded = true;
                    mActivity.deleteAlarmForDay(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), BLEUtility.intToByte(mIntMaxAlarmCount), false);
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (currentLoopPosition == mArrayListCheckedDevice.size() - 1) {
                                if (mStartDeviceScanTimer != null)
                                    mStartDeviceScanTimer.start();
                            } else {
                                currentLoopPosition++;
                                addingAlarmDeviceRequest();
                            }
                        }
                    }, 1000);
                } catch (Exception e) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.hideProgress();
                        }
                    });
                    e.printStackTrace();
                }
            } else {
                if (mArrayListCheckedDevice.get(currentLoopPosition).getIsChecked()) {
                    try {
                        System.out.println(TAG + "currentLoopPosition=" + currentLoopPosition);
                        isAlarmAdded = true;
                        int selectedId = mRadioGroupLightOnOff.getCheckedRadioButtonId();
                        if (selectedId == R.id.frg_add_alarm_radiobutton_on) {
                            int isWakeUpOn = 0;
                            if (mAppCompatCheckBoxWakeUpSleep.isChecked()) {
                                isWakeUpOn = 1;
                            }
                            mActivity.setAlarmForDay(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), BLEUtility.intToByte(mIntMaxAlarmCount), BLEUtility.intToByte(Integer.parseInt(mStringDaysMask, 2)), BLEUtility.intToByte(mCalendarHour), BLEUtility.intToByte(mCalendarMinute), BLEUtility.intToByte(Color.red(mAlarmColor)), BLEUtility.intToByte(Color.green(mAlarmColor)), BLEUtility.intToByte(Color.blue(mAlarmColor)), BLEUtility.intToByte(isWakeUpOn), false);
                        } else {
                            int isTurnOff = 0;
                            if (mAppCompatCheckBoxWakeUpSleep.isChecked()) {
                                isTurnOff = 1;
                            }
                            mActivity.setAlarmForDay(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), BLEUtility.intToByte(mIntMaxAlarmCount), BLEUtility.intToByte(Integer.parseInt(mStringDaysMask, 2)), BLEUtility.intToByte(mCalendarHour), BLEUtility.intToByte(mCalendarMinute), BLEUtility.intToByte(0), BLEUtility.intToByte(0), BLEUtility.intToByte(0), BLEUtility.intToByte(isTurnOff), false);
                        }

                        Timer innerTimer = new Timer();
                        innerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (currentLoopPosition == mArrayListCheckedDevice.size() - 1) {
                                    if (mStartDeviceScanTimer != null)
                                        mStartDeviceScanTimer.start();
                                } else {
                                    currentLoopPosition++;
                                    addingAlarmDeviceRequest();
                                }
                            }
                        }, 1000);

                    } catch (Exception e) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.hideProgress();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            }
        } else {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActivity.hideProgress();
                }
            });
            mActivity.connectDeviceWithProgress();
        }
    }
    /*Send ble command for add alarm to device*/
    private class startDeviceScanTimer extends CountDownTimer {

        public startDeviceScanTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            getDeviceListData();
        }
    }
    /* Check acknowledgement data from ble device and save*/
    private void getDeviceListData() {
        boolean isAnyDeviceAddedInAlarm = false;
        boolean isAlarmLimitCross = false;
        String mStringDeviceHexId;
        String mStringDeviceHexData;
        for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
            mStringDeviceHexId = mArrayListCheckedDevice.get(j).getDevice_Comm_hexId();
            for (int i = 0; i < mLeDevicesTemp.size(); i++) {
                if (isAlarmAdded) {
                    if (!mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup()) {
                        if (mStringDeviceHexId != null && !mStringDeviceHexId.equalsIgnoreCase("") && mStringDeviceHexId.length() >= 2) {
                            mStringDeviceHexData = mLeDevicesTemp.get(i).getDeviceHexData();
                            System.out.println(TAG + "-mStringDeviceHexId=" + mStringDeviceHexId);
                            System.out.println(TAG + "-mStringDeviceHexData=" + mStringDeviceHexData);
                            if (mArrayListCheckedDevice.get(j).getIsDeviceAlradyInGroup() && !mArrayListCheckedDevice.get(j).getIsChecked()) {
                                if (mStringDeviceHexData.toLowerCase().contains(mStringDeviceHexId.toLowerCase())) {
                                    if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ALARM_REMOVE_RSP)) {
                                        isAnyDeviceAddedInAlarm = true;
                                        mArrayListCheckedDevice.get(j).setDeviceSyncWithGroup(true);
                                        break;
                                    }
                                }
                            } else {
                                if (mArrayListCheckedDevice.get(j).getIsChecked()) {
                                    if (mStringDeviceHexData.toLowerCase().contains(mStringDeviceHexId.toLowerCase())) {
                                        if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ALARM_ADD_RSP) && mStringDeviceHexData.substring(36, 38).equals("00")) {
                                            isAnyDeviceAddedInAlarm = true;
                                            mArrayListCheckedDevice.get(j).setDeviceSyncWithGroup(true);
                                            break;
                                        } else if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ALARM_ADD_RSP) && mStringDeviceHexData.substring(36, 38).equals("01")) {
                                            isAlarmLimitCross = true;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        mActivity.hideProgress();
        System.out.println(TAG + "-isAnyDeviceAddedInAlarm=" + isAnyDeviceAddedInAlarm);
        if (isAnyDeviceAddedInAlarm) {

            Calendar cal = Calendar.getInstance();
            Date currentLocalTime = cal.getTime();

            ContentValues mContentValues = new ContentValues();
            mContentValues.put(mActivity.mDbHelper.mFieldAlarmTime, mTextViewAlarmTime.getText().toString().trim() + " " + mTextViewAlarmTimeAMPM.getText().toString().trim());
            mContentValues.put(mActivity.mDbHelper.mFieldAlarmDays, mStringSelectedDays);

            int selectedId = mRadioGroupLightOnOff.getCheckedRadioButtonId();
            if (selectedId == R.id.frg_add_alarm_radiobutton_on) {
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmLightOn, "1");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmColor, mAlarmColor);
            } else {
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmLightOn, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmColor, 0);
            }
            mContentValues.put(mActivity.mDbHelper.mFieldAlarmWakeUpSleep, mAppCompatCheckBoxWakeUpSleep.isChecked() ? "1" : "0");
            mContentValues.put(mActivity.mDbHelper.mFieldAlarmUpdatedAt, mDateFormatDb.format(currentLocalTime));
            mContentValues.put(mActivity.mDbHelper.mFieldAlarmIsSync, "0");

            String[] mArray = new String[]{mVoAlarmLocal.getAlarm_local_id()};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAlarm, mContentValues, mActivity.mDbHelper.mFieldAlarmLocalID + "=?", mArray);
            String mStringQuery = "delete from " + mActivity.mDbHelper.mTableAlarmDeviceList + " where " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= '" + mVoAlarmLocal.getAlarm_local_id() + "'";
            mActivity.mDbHelper.exeQuery(mStringQuery);

            // GroupDevice
            ContentValues mContentValuesAD;
            for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
                if (mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup() && mArrayListCheckedDevice.get(j).getIsChecked()) {
                    mContentValuesAD = new ContentValues();
                    mContentValuesAD.put(mActivity.mDbHelper.mFieldADUserId, mActivity.mPreferenceHelper.getUserId());
                    mContentValuesAD.put(mActivity.mDbHelper.mFieldADAlarmLocalID, mVoAlarmLocal.getAlarm_local_id());
                    mContentValuesAD.put(mActivity.mDbHelper.mFieldADAlarmServerID, "");
                    mContentValuesAD.put(mActivity.mDbHelper.mFieldADDeviceLocalID, mArrayListCheckedDevice.get(j).getDevicLocalId());
                    mContentValuesAD.put(mActivity.mDbHelper.mFieldADDeviceServerID, mArrayListCheckedDevice.get(j).getDeviceServerId());
                    mContentValuesAD.put(mActivity.mDbHelper.mFieldADDeviceStatus, mArrayListCheckedDevice.get(j).getDevice_is_active());
                    mContentValuesAD.put(mActivity.mDbHelper.mFieldADCreatedDate, mDateFormatDb.format(currentLocalTime));
                    mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableAlarmDeviceList, mContentValuesAD);
                }
            }
            isAlarmAdded = false;
            mLeDevicesTemp = new ArrayList<>();
            showAlarmAddAlert();
        } else {
            if (isAlarmLimitCross) {
                showAlarmRetryAlert(getResources().getString(R.string.frg_routine_limit_cross));
            } else {
                showAlarmRetryAlert(getResources().getString(R.string.frg_no_device_added_in_routine));
            }
        }
    }

    private void showAlarmRetryAlert(String msg) {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(msg, 1, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void showAlarmAddAlert() {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_routine_updated_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }
    /*get All device from database and display*/
    private void getDBDeviceList() {
        if (mDeviceListAdapter == null) {
            DataHolder mDataHolderLight;
            DataHolder mDataHolderLocalDevice;
            mArrayListDevice = new ArrayList<>();
            try {
                String urlDevice = "select * from " + mActivity.mDbHelper.mTableDevice + " inner join " + mActivity.mDbHelper.mTableAlarmDeviceList + " on " + mActivity.mDbHelper.mFieldADDeviceLocalID + "= " + mActivity.mDbHelper.mFieldDeviceLocalId + " where " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= '" + mVoAlarmLocal.getAlarm_local_id() + "'" + " group by " + mActivity.mDbHelper.mFieldDeviceLocalId;
                mDataHolderLight = mActivity.mDbHelper.read(urlDevice);
                if (mDataHolderLight != null) {
                    VoDeviceList mVoDeviceList;
                    for (int i = 0; i < mDataHolderLight.get_Listholder().size(); i++) {
                        mVoDeviceList = new VoDeviceList();
                        mVoDeviceList.setDevicLocalId(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLocalId));
                        mVoDeviceList.setDeviceServerid(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceServerId));
                        mVoDeviceList.setUser_id(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUserId));
                        mVoDeviceList.setDevice_Comm_id(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommID));
                        mVoDeviceList.setDevice_Comm_hexId(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommHexId));
                        mVoDeviceList.setDevice_name(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceName));
                        mVoDeviceList.setDevice_realName(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceRealName));
                        mVoDeviceList.setDevice_BleAddress(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceBleAddress).toUpperCase());
                        mVoDeviceList.setDevice_Type(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceType));
                        mVoDeviceList.setDevice_type_name(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTypeName));
                        mVoDeviceList.setDevice_ConnStatus(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldConnectStatus));
                        mVoDeviceList.setDevice_SwitchStatus(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSwitchStatus));
                        mVoDeviceList.setDevice_is_favourite(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsFavourite));
                        mVoDeviceList.setDevice_last_state_remember(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLastState));
                        mVoDeviceList.setDevice_timestamp(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTimeStamp));
                        mVoDeviceList.setDevice_is_active(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsActive));
                        mVoDeviceList.setDevice_created_at(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCreatedAt));
                        mVoDeviceList.setDevice_updated_at(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUpdatedAt));
                        mVoDeviceList.setDevice_is_sync(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsSync));
                        mVoDeviceList.setIsChecked(true);

                        mVoDeviceList.setDeviceAlradyInGroup(true);
                        mVoDeviceList.setIsGroupChecked(true);
                        mArrayListDevice.add(mVoDeviceList);
                    }
                }

                String urlLocalDevice = "select * from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceIsActive + "= '1'" + " AND " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                mDataHolderLocalDevice = mActivity.mDbHelper.read(urlLocalDevice);

                if (mDataHolderLocalDevice != null) {
                    VoDeviceList mVoDeviceList;
                    for (int i = 0; i < mDataHolderLocalDevice.get_Listholder().size(); i++) {
                        mVoDeviceList = new VoDeviceList();
                        mVoDeviceList.setDevicLocalId(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLocalId));
                        mVoDeviceList.setDeviceServerid(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceServerId));
                        mVoDeviceList.setUser_id(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUserId));
                        mVoDeviceList.setDevice_Comm_id(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommID));
                        mVoDeviceList.setDevice_Comm_hexId(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommHexId));
                        mVoDeviceList.setDevice_name(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceName));
                        mVoDeviceList.setDevice_realName(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceRealName));
                        mVoDeviceList.setDevice_BleAddress(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceBleAddress).toUpperCase());
                        mVoDeviceList.setDevice_Type(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceType));
                        mVoDeviceList.setDevice_type_name(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTypeName));
                        mVoDeviceList.setDevice_ConnStatus(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldConnectStatus));
                        mVoDeviceList.setDevice_SwitchStatus(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSwitchStatus));
                        mVoDeviceList.setDevice_is_favourite(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsFavourite));
                        mVoDeviceList.setDevice_last_state_remember(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLastState));
                        mVoDeviceList.setDevice_timestamp(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTimeStamp));
                        mVoDeviceList.setDevice_is_active(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsActive));
                        mVoDeviceList.setDevice_created_at(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCreatedAt));
                        mVoDeviceList.setDevice_updated_at(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUpdatedAt));
                        mVoDeviceList.setDevice_is_sync(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsSync));

                        mVoDeviceList.setIsChecked(false);
                        mVoDeviceList.setIsGroupChecked(false);
                        mVoDeviceList.setDeviceAlradyInGroup(false);
                        boolean contains = false;
                        for (VoDeviceList device : mArrayListDevice) {
                            if (mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLocalId).equals(device.getDevicLocalId())) {
                                contains = true;
                                break;
                            }
                        }
                        if (!contains) {
                            mArrayListDevice.add(mVoDeviceList);
                        }
                    }
                }


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mDeviceListAdapter = new DeviceListAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mDeviceListAdapter);
        mDeviceListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();

    }
    /*Check device adapter is empty or not*/
    private void checkAdapterIsEmpty() {
        if (mDeviceListAdapter != null) {
            if (mDeviceListAdapter.getItemCount() == 0) {
                mTextViewNoDeviceFound.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mTextViewNoDeviceFound.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }
    /* Device List Adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @Override
        public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_alarm_device_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public int getItemCount() {
            return mArrayListDevice.size();
        }

        @Override
        public void onBindViewHolder(final DeviceListAdapter.ViewHolder itemViewHolder, final int relativePosition) {
            if (mArrayListDevice.get(relativePosition).getDevice_name() != null && !mArrayListDevice.get(relativePosition).getDevice_name().equalsIgnoreCase("")) {
                itemViewHolder.mTextViewDeviceName.setText(mArrayListDevice.get(relativePosition).getDevice_name());
            } else {
                itemViewHolder.mTextViewDeviceName.setText("");
            }
            if (mArrayListDevice.get(relativePosition).getDevice_BleAddress() != null && !mArrayListDevice.get(relativePosition).getDevice_BleAddress().equalsIgnoreCase("")) {
                itemViewHolder.mTextViewDeviceId.setText(mArrayListDevice.get(relativePosition).getDevice_BleAddress());
            } else {
                itemViewHolder.mTextViewDeviceId.setText("");
            }
            if (mArrayListDevice.get(relativePosition).getDevice_Type() != null && !mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("")) {
                String mStrDeviceType = mArrayListDevice.get(relativePosition).getDevice_Type();
                if (mStrDeviceType.equalsIgnoreCase("0100")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_pic);
                } else if (mStrDeviceType.equalsIgnoreCase("0200")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_pic);
                } else if (mStrDeviceType.equalsIgnoreCase("0300")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_switch_icon);
                } else if (mStrDeviceType.equalsIgnoreCase("0400")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_socket_icon);
                } else if (mStrDeviceType.equalsIgnoreCase("0500")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_fan_icon);
                } else if (mStrDeviceType.equalsIgnoreCase("0600")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_striplight_icon);
                } else if (mStrDeviceType.equalsIgnoreCase("0700")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_lamp_icon);
                } else if (mStrDeviceType.equalsIgnoreCase("0800")) {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_powerstrip_icon);
                } else {
                    itemViewHolder.mImageViewDevice.setImageResource(R.drawable.ic_default_pic);
                }
                itemViewHolder.mImageViewDevice.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
            }
            if (mArrayListDevice.get(relativePosition).getIsChecked()) {
                itemViewHolder.mAppCompatCheckBox.setChecked(true);
            } else {
                itemViewHolder.mAppCompatCheckBox.setChecked(false);
            }
            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDevice != null) {
                        if (relativePosition < mArrayListDevice.size()) {
                            if (mArrayListDevice.get(relativePosition).getIsChecked()) {
                                mArrayListDevice.get(relativePosition).setIsChecked(false);
                                itemViewHolder.mAppCompatCheckBox.setChecked(false);
                            } else {
                                mArrayListDevice.get(relativePosition).setIsChecked(true);
                                itemViewHolder.mAppCompatCheckBox.setChecked(true);
                            }
                        }
                    }
                }
            });
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_alarm_device_list_item_imageview_device)
            ImageView mImageViewDevice;
            @BindView(R.id.raw_alarm_device_list_item_textview_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_alarm_device_list_item_textview_device_id)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_alarm_device_list_item_checkbox)
            AppCompatCheckBox mAppCompatCheckBox;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
    /* Day list adapter*/
    public class DaysListAdapter extends RecyclerView.Adapter<DaysListAdapter.ViewHolderToggle> {

        @Override
        public DaysListAdapter.ViewHolderToggle onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_days_list_item, parent, false);
            return new DaysListAdapter.ViewHolderToggle(itemView);
        }

        @Override
        public int getItemCount() {
            return mArrayListDays.size();
        }

        @Override
        public void onBindViewHolder(final DaysListAdapter.ViewHolderToggle itemViewHolder, final int position) {

            itemViewHolder.mToggleButton.setTextOff(mArrayListDays.get(position).getDays_name());
            itemViewHolder.mToggleButton.setTextOn(mArrayListDays.get(position).getDays_name());
            if (mArrayListDays.get(position).getIsIs_day_checked()) {
                itemViewHolder.mToggleButton.setChecked(true);
            } else {
                itemViewHolder.mToggleButton.setChecked(false);
            }
            itemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDays != null) {
                        if (position < mArrayListDays.size()) {
                            if (mArrayListDays.get(position).getIsIs_day_checked()) {
                                mArrayListDays.get(position).setIs_day_checked(false);
                                itemViewHolder.mToggleButton.setChecked(false);
                            } else {
                                mArrayListDays.get(position).setIs_day_checked(true);
                                itemViewHolder.mToggleButton.setChecked(true);
                            }
                        }
                    }
                }
            });
        }

        public class ViewHolderToggle extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_days_list_item_toggle_days)
            CustomToggleButton mToggleButton;

            public ViewHolderToggle(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.isAddDeviceScan = false;
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

}
