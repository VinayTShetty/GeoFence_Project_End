package com.vithamastech.smartlight.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.vithamastech.smartlight.BaseFragment.BaseFragment;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocketAlaramUIDays;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket_AlaramDays;
import com.vithamastech.smartlight.PowerSocketUtils.Socket;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.CustomToggleButton;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.services.PowerSocketBLEEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketBLEService;
import com.vithamastech.smartlight.services.PowerSocketMQTTEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketMQTTService;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.vithamastech.smartlight.PowerSocketUtils.BLECommandUtils.FAILURE;
import static com.vithamastech.smartlight.PowerSocketUtils.BLECommandUtils.SUCESS;
import static com.vithamastech.smartlight.db.DBHelper.mTableAlarmPowerSocket;
import static com.vithamastech.smartlight.db.DBHelper.mfield_OffTimestamp;
import static com.vithamastech.smartlight.db.DBHelper.mfield_Off_original;
import static com.vithamastech.smartlight.db.DBHelper.mfield_OnTimestamp;
import static com.vithamastech.smartlight.db.DBHelper.mfield_On_original;
import static com.vithamastech.smartlight.db.DBHelper.mfield_alarm_id;
import static com.vithamastech.smartlight.db.DBHelper.mfield_alarm_state;
import static com.vithamastech.smartlight.db.DBHelper.mfield_ble_address;
import static com.vithamastech.smartlight.db.DBHelper.mfield_day_selected;
import static com.vithamastech.smartlight.db.DBHelper.mfield_day_value;
import static com.vithamastech.smartlight.db.DBHelper.mfield_socket_id;

public class FragmentPowerSocketAlaramSchedule extends BaseFragment {
    public static final String TAG = FragmentPowerSocketAlaramSchedule.class.getSimpleName();
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    ArrayList<PowerSocket_AlaramDays> alaramButton;
    /**
     * Top alaram
     */
    @BindView(R.id.sunday_fabButton_alaram_1)
    CustomToggleButton fabbutton_sunday_1;
    @BindView(R.id.monday_fabButton_alaram_1)
    CustomToggleButton fabbutton_monday_1;
    @BindView(R.id.tuesday_fabButton_alaram_1)
    CustomToggleButton fabbutton_tuesday_1;
    @BindView(R.id.wednesday_fabButton_alaram_1)
    CustomToggleButton fabbutton_wednesday_1;
    @BindView(R.id.thursday_fabButton_alaram_1)
    CustomToggleButton fabbutton_thursday_1;
    @BindView(R.id.friday_fabButton_alaram_1)
    CustomToggleButton fabbutton_friday_1;
    @BindView(R.id.saturday_fabButton_alaram_1)
    CustomToggleButton fabbutton_satrday_1;
    /**
     * Bottom Alaram
     */
    @BindView(R.id.sunday_fabButton_alaram_2)
    CustomToggleButton fabbutton_sunday_2;
    @BindView(R.id.monday_fabButton_alaram_2)
    CustomToggleButton fabbutton_monday_2;
    @BindView(R.id.tuesday_fabButton_alaram_2)
    CustomToggleButton fabbutton_tuesday_2;
    @BindView(R.id.wednesday_fabButton_alaram_2)
    CustomToggleButton fabbutton_wednesday_2;
    @BindView(R.id.thursday_fabButton_alaram_2)
    CustomToggleButton fabbutton_thursday_2;
    @BindView(R.id.friday_fabButton_alaram_2)
    CustomToggleButton fabbutton_friday_2;
    @BindView(R.id.saturday_fabButton_alaram_2)
    CustomToggleButton fabbutton_satrday_2;
    /**
     * TimeSet textView for Alaram Box.
     */
    @BindView(R.id.ontime_Alaram_1_textView)
    TextView onTime_Alaram_1_text;
    @BindView(R.id.offtime_Alaram_1_textView)
    TextView offTime_Alaram_1_text;
    @BindView(R.id.ontime_Alaram_2_textView)
    TextView onTime_Alaram_2_text;
    @BindView(R.id.offtime_Alaram_2_textView)
    TextView offTime_Alaram_2_text;
    /**
     * Repeat alarm Layout
     */
    @BindView(R.id.alaram_layout_2)
    LinearLayout linearlayout_alramId_2;
    @BindView(R.id.repeat_alaram_2_checkBox)
    CheckBox checkbox_repeat_alaram_2;

    @BindView(R.id.alaram_layout_1)
    LinearLayout linearlayout_alramId_1;
    @BindView(R.id.repeat_alaram_1_checkBox)
    CheckBox checkbox_repeat_alaram_1;

    int onTimeStamp_Alaram_1;
    int offTimeStamp_Alaram_1;
    int onTimeStamp_Alaram_2;
    int offTimeStamp_Alaram_2;

    int selected_ScoketdId;// Remove later as there is bug when sending socket number.
    PowerSocket powerSocket;
    Socket socket;

    private PowerSocketBLEService powerSocketBLEService;
    int alaramId_1 = -1;
    int alaramId_2 = -1;

    private int selectedYear, selectedMonth, selectedDay, selectedHour, selectedMinute;
    private int present_mYear, present_mMonth, present_mDay, present_mHour, present_mMinute;

    String powerSocketBleAddress;
    /**
     * MQTT part.
     */
    private PowerSocketMQTTService powerSocketMQTTService;

    private String previousStateAlaram_1;
    private String previousStateAlaram_2;

    private static int local_TransactionId = -1;
    private static boolean alaramSet_1 = false;
    private static boolean alaramSet_2 = false;
    private static boolean alaramDelete_1 = false;
    private static boolean alaramDelete_2 = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        getBundleData();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_alaram_schedule, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        /**
         * ToolBar design parameters.
         */
        mActivity.mTextViewTitle.setText(R.string.header_Fragment_PowerSocket_AlaramSchedule);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mActivity.mImageViewAddDevice.setVisibility(View.INVISIBLE);
        alaramButton = new ArrayList<PowerSocket_AlaramDays>();
        get_DetailedDataFromBundle();
        intializeCustomUIButtonToogleButtons();
        setUpBle();
        setUpMqtt();
        defaultAlaramFlagToFalse();
        allAllAlaramButtons();
        set_AlaramIdValues();
        prepareUiIfValuesAlreadyStored();
        return mViewRoot;
    }

    private void defaultAlaramFlagToFalse() {
        previousStateAlaram_1 = getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule);
        previousStateAlaram_2 = getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule);
    }

    private void intializeCustomUIButtonToogleButtons() {
        /**
         * Top alaram Default Values
         */
        fabbutton_sunday_1.setTextOn("S");
        fabbutton_monday_1.setTextOn("M");
        fabbutton_tuesday_1.setTextOn("T");
        fabbutton_wednesday_1.setTextOn("W");
        fabbutton_thursday_1.setTextOn("T");
        fabbutton_friday_1.setTextOn("F");
        fabbutton_satrday_1.setTextOn("S");
        /**
         Bottom Aalram Values.
         */
        fabbutton_sunday_2.setTextOn("S");
        fabbutton_monday_2.setTextOn("M");
        fabbutton_tuesday_2.setTextOn("T");
        fabbutton_wednesday_2.setTextOn("W");
        fabbutton_thursday_2.setTextOn("T");
        fabbutton_friday_2.setTextOn("F");
        fabbutton_satrday_2.setTextOn("S");
    }

    private void setUpMqtt() {
        powerSocketMQTTService = PowerSocketMQTTService.getInstance(mActivity.getApplicationContext());
        powerSocketMQTTService.setOnPowerSocketMQTTEventCallbacks(powerSocketMQTTEventCallbacks);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        powerSocketBLEService.setOnPowerSocketEventCallbacks(powerSocketBLEEventCallbacks);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private void sendAlarmaVia_BLE_ALARAM_1() {
        int numbeOfdaysCheckedValue_Alaram_1 = getNumberOfDaysSelected_Alaram_1();
        String binaryDaysSelected = binaryDaysAlaram_1();
        local_TransactionId = -1;
        local_TransactionId = generateRandomNumber();
        powerSocketBLEService.setAlarmForSocket(onTimeStamp_Alaram_1, offTimeStamp_Alaram_1, alaramId_1, numbeOfdaysCheckedValue_Alaram_1, powerSocket, selected_ScoketdId, local_TransactionId);
        String onOriginalTime_1 = "NA";
        String oFFOriginalTime_1 = "NA";
        onOriginalTime_1 = onTime_Alaram_1_text.getText().toString();
        oFFOriginalTime_1 = offTime_Alaram_1_text.getText().toString();
        update_Insert_AlaramDataBase(alaramId_1);//, selected_ScoketdId, numbeOfdaysCheckedValue_Alaram_1, "" + onTimeStamp_Alaram_1, "" + offTimeStamp_Alaram_1, onOriginalTime_1, oFFOriginalTime_1, powerSocketBleAddress.replace(":", "").toUpperCase(), binaryDaysSelected);
    }

    private void sendAlarmaVia_MQTT_ALARAM_1() {
        int numbeOfdaysCheckedValue_Alaram_1 = getNumberOfDaysSelected_Alaram_1();
        String binaryDaysSelected = binaryDaysAlaram_1();
        local_TransactionId = -1;
        local_TransactionId = generateRandomNumber();
        powerSocketMQTTService.setAlaram(onTimeStamp_Alaram_1, offTimeStamp_Alaram_1, alaramId_1, numbeOfdaysCheckedValue_Alaram_1, powerSocket, selected_ScoketdId, local_TransactionId);
        String onOriginalTime_1 = "NA";
        String oFFOriginalTime_1 = "NA";
        onOriginalTime_1 = onTime_Alaram_1_text.getText().toString();
        oFFOriginalTime_1 = offTime_Alaram_1_text.getText().toString();
        update_Insert_AlaramDataBase(alaramId_1);//, selected_ScoketdId, numbeOfdaysCheckedValue_Alaram_1, "" + onTimeStamp_Alaram_1, "" + offTimeStamp_Alaram_1, onOriginalTime_1, oFFOriginalTime_1, powerSocketBleAddress.replace(":", "").toUpperCase(), binaryDaysSelected);
    }

    private void sendAlarmaVia_BLE_ALARAM_2() {
        int numbeOfdaysCheckedValue_Alaram_2 = getNumberOfDaysSelected_Alaram_2();
        String binaryDaysSelected = binaryDaysAlaram_2();
        String onOriginalTime_2 = "NA";
        String oFFOriginalTime_2 = "NA";
        onOriginalTime_2 = onTime_Alaram_2_text.getText().toString();
        oFFOriginalTime_2 = offTime_Alaram_2_text.getText().toString();
        local_TransactionId = -1;
        local_TransactionId = generateRandomNumber();
        powerSocketBLEService.setAlarmForSocket(onTimeStamp_Alaram_2, offTimeStamp_Alaram_2, alaramId_2, numbeOfdaysCheckedValue_Alaram_2, powerSocket, selected_ScoketdId, local_TransactionId);
        update_Insert_AlaramDataBase(alaramId_2);//, selected_ScoketdId, numbeOfdaysCheckedValue_Alaram_2, "" + onTimeStamp_Alaram_2, "" + offTimeStamp_Alaram_2, onOriginalTime_2, oFFOriginalTime_2, powerSocketBleAddress.replace(":", "").toUpperCase(), binaryDaysSelected);
    }

    private void sendAlarmaVia_MQTT_ALARAM_2() {
        int numbeOfdaysCheckedValue_Alaram_2 = getNumberOfDaysSelected_Alaram_2();
        String binaryDaysSelected = binaryDaysAlaram_2();
        local_TransactionId = -1;
        local_TransactionId = generateRandomNumber();
        powerSocketMQTTService.setAlaram(onTimeStamp_Alaram_2, offTimeStamp_Alaram_2, alaramId_2, numbeOfdaysCheckedValue_Alaram_2, powerSocket, selected_ScoketdId, local_TransactionId);
        String onOriginalTime_2 = "NA";
        String oFFOriginalTime_2 = "NA";
        onOriginalTime_2 = onTime_Alaram_2_text.getText().toString();
        oFFOriginalTime_2 = offTime_Alaram_2_text.getText().toString();
        update_Insert_AlaramDataBase(alaramId_2);//, selected_ScoketdId, numbeOfdaysCheckedValue_Alaram_2, "" + onTimeStamp_Alaram_2, "" + offTimeStamp_Alaram_2, onOriginalTime_2, oFFOriginalTime_2, powerSocketBleAddress.replace(":", "").toUpperCase(), binaryDaysSelected);
    }

    @OnClick(R.id.saveAlaram_1)
    public void setSaveAlaram_1() {
        String onTimeAlaram_1 = onTime_Alaram_1_text.getText().toString();
        String offTimeAlaram_1 = offTime_Alaram_1_text.getText().toString();
        int numbeOfdaysCheckedValue_Alaram_1 = getNumberOfDaysSelected_Alaram_1();
        if (((onTime_Alaram_1_text.getText().toString().equalsIgnoreCase("NA"))) && (((offTime_Alaram_1_text.getText().toString().equalsIgnoreCase("NA"))))) {
            mActivity.mUtility.errorDialog("Please Set Alaram Time", 1, false);
            return;
        } else if (onTimeAlaram_1.equalsIgnoreCase(offTimeAlaram_1)) {
            mActivity.mUtility.errorDialog("Please Set Differen Time\n ON & OFF ", 3, false);
            return;
        } else if ((checkbox_repeat_alaram_1.isChecked()) && (numbeOfdaysCheckedValue_Alaram_1 <= 0)) {
            mActivity.mUtility.errorDialog("Please Select atleast one Day", 3, false);
            return;
        } else {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.isEnabled() && powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                /**
                 * Ble Part.
                 */
                alaramSet_1 = true;
                sendAlarmaVia_BLE_ALARAM_1();

            } else if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
                /**
                 * Mqtt Part.
                 */
                if (powerSocket.isWifiConfigured) {
                    mActivity.showProgress("Plese wait", true);
                    alaramSet_1 = true;
                    sendAlarmaVia_MQTT_ALARAM_1();
                } else {
                    mActivity.mUtility.errorDialog("Wifi Not Configured to Socket", 1, false);
                }

            } else {

                mActivity.mUtility.errorDialog("Wifi\nBluetooth Both Not Avaliable", 1, false);
            }
        }
    }

    @OnClick(R.id.saveAlaram_2)
    public void setSaveAlaram_2() {
        String onTimeAlaram_2 = onTime_Alaram_2_text.getText().toString();
        String offTimeAlaram_2 = offTime_Alaram_2_text.getText().toString();
        int numbeOfdaysCheckedValue_Alaram_2 = getNumberOfDaysSelected_Alaram_2();
        if (((onTime_Alaram_2_text.getText().toString().equalsIgnoreCase("NA"))) && (((offTime_Alaram_2_text.getText().toString().equalsIgnoreCase("NA"))))) {
            mActivity.mUtility.errorDialog("Please Set Alaram Time", 1, false);
            return;
        } else if (onTimeAlaram_2.equalsIgnoreCase(offTimeAlaram_2)) {
            mActivity.mUtility.errorDialog("Please Set Different Time\n ON & OFF ", 1, false);
            return;
        } else if ((checkbox_repeat_alaram_2.isChecked()) && (numbeOfdaysCheckedValue_Alaram_2 <= 0)) {
            mActivity.mUtility.errorDialog("Please Select atleast one Day", 3, false);
            return;
        } else {

            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter.isEnabled() && powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                /**
                 * Ble Part.
                 */
                alaramSet_2 = true;
                sendAlarmaVia_BLE_ALARAM_2();
            } else if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
                /**
                 * Mqtt Part.
                 */
                if (powerSocket.isWifiConfigured) {
                    mActivity.showProgress("Plese wait", true);
                    alaramSet_2 = true;
                    sendAlarmaVia_MQTT_ALARAM_2();
                } else {
                    mActivity.mUtility.errorDialog("Wifi Not Configured to Socket", 1, false);
                }
            } else {
                mActivity.mUtility.errorDialog("Wifi\nBluetooth Both Not Avaliable", 1, false);
            }
        }
    }

    private void getBundleData() {
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            powerSocket = (PowerSocket) bundle.getSerializable(getResources().getString(R.string.power_socket_object_Fragment_PowerSocket_AlaramSchedule));
            socket = (Socket) bundle.getSerializable(getResources().getString(R.string.socket_object_Fragment_PowerSocket_AlaramSchedule));
        }
    }

    private void get_DetailedDataFromBundle() {
        selected_ScoketdId = socket.socketId;
        powerSocketBleAddress = powerSocket.bleAddress;
    }

    @OnClick({R.id.sunday_fabButton_alaram_1, R.id.sunday_fabButton_alaram_2,
            R.id.monday_fabButton_alaram_1, R.id.monday_fabButton_alaram_2,
            R.id.tuesday_fabButton_alaram_1, R.id.tuesday_fabButton_alaram_2,
            R.id.wednesday_fabButton_alaram_1, R.id.wednesday_fabButton_alaram_2,
            R.id.thursday_fabButton_alaram_1, R.id.thursday_fabButton_alaram_2,
            R.id.friday_fabButton_alaram_1, R.id.friday_fabButton_alaram_2,
            R.id.saturday_fabButton_alaram_1, R.id.saturday_fabButton_alaram_2})
    public void alaramDaysClicked(View view) {
        switch (view.getId()) {
            case R.id.sunday_fabButton_alaram_1: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.sunday_fabButton_alaram_1);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_sunday_1.setChecked(false);
                    fabbutton_sunday_1.setTextOff("S");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_sunday_1.setChecked(true);
                    fabbutton_sunday_1.setTextOn("S");
                }
            }
            break;
            case R.id.monday_fabButton_alaram_1: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.monday_fabButton_alaram_1);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_monday_1.setChecked(false);
                    fabbutton_monday_1.setTextOff("M");

                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_monday_1.setChecked(true);
                    fabbutton_monday_1.setTextOn("M");
                }
            }
            break;
            case R.id.tuesday_fabButton_alaram_1: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.tuesday_fabButton_alaram_1);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_tuesday_1.setChecked(false);
                    fabbutton_tuesday_1.setTextOff("T");

                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_tuesday_1.setChecked(true);
                    fabbutton_tuesday_1.setTextOn("T");
                }
            }
            break;
            case R.id.wednesday_fabButton_alaram_1: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.wednesday_fabButton_alaram_1);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_wednesday_1.setChecked(false);
                    fabbutton_wednesday_1.setTextOff("W");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_wednesday_1.setChecked(true);
                    fabbutton_wednesday_1.setTextOn("W");
                }
            }
            break;
            case R.id.thursday_fabButton_alaram_1: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.thursday_fabButton_alaram_1);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_thursday_1.setChecked(false);
                    fabbutton_thursday_1.setTextOff("T");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_thursday_1.setTextOn("T");
                }
            }
            break;
            case R.id.friday_fabButton_alaram_1: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.friday_fabButton_alaram_1);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_friday_1.setChecked(false);
                    fabbutton_friday_1.setTextOff("F");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_friday_1.setChecked(true);
                    fabbutton_friday_1.setTextOn("F");
                }
            }
            break;
            case R.id.saturday_fabButton_alaram_1: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.saturday_fabButton_alaram_1);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_satrday_1.setChecked(false);
                    fabbutton_satrday_1.setTextOff("S");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_satrday_1.setChecked(true);
                    fabbutton_satrday_1.setTextOn("S");
                }
            }
            break;
            case R.id.sunday_fabButton_alaram_2: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.sunday_fabButton_alaram_2);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_sunday_2.setChecked(false);
                    fabbutton_sunday_2.setTextOff("S");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_sunday_2.setChecked(true);
                    fabbutton_sunday_2.setTextOn("S");
                }
            }
            break;
            case R.id.monday_fabButton_alaram_2: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.monday_fabButton_alaram_2);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_monday_2.setChecked(false);
                    fabbutton_monday_2.setTextOff("M");

                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_monday_2.setChecked(true);
                    fabbutton_monday_2.setTextOn("M");
                }
            }
            break;
            case R.id.tuesday_fabButton_alaram_2: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.tuesday_fabButton_alaram_2);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_tuesday_2.setChecked(false);
                    fabbutton_tuesday_2.setTextOff("T");

                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_tuesday_2.setChecked(true);
                    fabbutton_tuesday_2.setTextOn("T");
                }
            }
            break;
            case R.id.wednesday_fabButton_alaram_2: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.wednesday_fabButton_alaram_2);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_wednesday_2.setChecked(false);
                    fabbutton_wednesday_2.setTextOff("W");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_wednesday_2.setChecked(true);
                    fabbutton_wednesday_2.setTextOn("W");
                }
            }
            break;
            case R.id.thursday_fabButton_alaram_2: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.thursday_fabButton_alaram_2);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_thursday_2.setChecked(false);
                    fabbutton_thursday_2.setTextOff("T");
                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_thursday_2.setChecked(true);
                    fabbutton_thursday_2.setTextOn("T");
                }
            }
            break;
            case R.id.friday_fabButton_alaram_2: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.friday_fabButton_alaram_2);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_friday_2.setChecked(false);
                    fabbutton_friday_2.setTextOff("F");

                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_friday_2.setChecked(true);
                    fabbutton_friday_2.setTextOn("F");
                }
            }
            break;
            case R.id.saturday_fabButton_alaram_2: {
                PowerSocket_AlaramDays powerSocket_alaramDays = getButtonDataFromId(R.id.saturday_fabButton_alaram_2);
                if (powerSocket_alaramDays.isChecked()) {
                    powerSocket_alaramDays.setChecked(false);
                    fabbutton_satrday_2.setChecked(false);
                    fabbutton_satrday_2.setTextOff("S");

                } else {
                    powerSocket_alaramDays.setChecked(true);
                    fabbutton_satrday_2.setChecked(true);
                    fabbutton_satrday_2.setTextOn("S");
                }
            }
            break;
        }
    }

    private void setUpBle() {
        powerSocketBLEService = PowerSocketBLEService.getInstance(mActivity.getApplicationContext());
    }

    private void allAllAlaramButtons() {
        /**
         * Top alaram
         */
        alaramButton.add(new PowerSocket_AlaramDays(false, 1, R.id.sunday_fabButton_alaram_1, 0));
        alaramButton.add(new PowerSocket_AlaramDays(false, 2, R.id.monday_fabButton_alaram_1, 1));
        alaramButton.add(new PowerSocket_AlaramDays(false, 4, R.id.tuesday_fabButton_alaram_1, 2));
        alaramButton.add(new PowerSocket_AlaramDays(false, 8, R.id.wednesday_fabButton_alaram_1, 3));
        alaramButton.add(new PowerSocket_AlaramDays(false, 16, R.id.thursday_fabButton_alaram_1, 4));
        alaramButton.add(new PowerSocket_AlaramDays(false, 32, R.id.friday_fabButton_alaram_1, 5));
        alaramButton.add(new PowerSocket_AlaramDays(false, 64, R.id.saturday_fabButton_alaram_1, 6));
        /**
         * Bottom alaram
         */
        alaramButton.add(new PowerSocket_AlaramDays(false, 1, R.id.sunday_fabButton_alaram_2, 7));
        alaramButton.add(new PowerSocket_AlaramDays(false, 2, R.id.monday_fabButton_alaram_2, 8));
        alaramButton.add(new PowerSocket_AlaramDays(false, 4, R.id.tuesday_fabButton_alaram_2, 9));
        alaramButton.add(new PowerSocket_AlaramDays(false, 8, R.id.wednesday_fabButton_alaram_2, 10));
        alaramButton.add(new PowerSocket_AlaramDays(false, 16, R.id.thursday_fabButton_alaram_2, 11));
        alaramButton.add(new PowerSocket_AlaramDays(false, 32, R.id.friday_fabButton_alaram_2, 12));
        alaramButton.add(new PowerSocket_AlaramDays(false, 64, R.id.saturday_fabButton_alaram_2, 13));
    }

    private void daysUnchecked_makeAlaram_1() {
        for (int i = 0; i < 7; i++) {
            alaramButton.get(i).setChecked(false);
            switch (i) {
                case 0:
                    fabbutton_sunday_1.setChecked(false);
                    fabbutton_sunday_1.setTextOff("S");
                    break;
                case 1:
                    fabbutton_monday_1.setChecked(false);
                    fabbutton_monday_1.setTextOff("M");
                    break;
                case 2:
                    fabbutton_tuesday_1.setChecked(false);
                    fabbutton_tuesday_1.setTextOff("T");
                    break;
                case 3:
                    fabbutton_wednesday_1.setChecked(false);
                    fabbutton_wednesday_1.setTextOff("W");
                    break;
                case 4:
                    fabbutton_thursday_1.setChecked(false);
                    fabbutton_thursday_1.setTextOff("T");
                    break;
                case 5:
                    fabbutton_friday_1.setChecked(false);
                    fabbutton_friday_1.setTextOff("F");
                    break;
                case 6:
                    fabbutton_satrday_1.setChecked(false);
                    fabbutton_satrday_1.setTextOff("S");
                    break;
            }
        }
    }

    private void daysUnchecked_makeAlaram_2() {
        for (int i = 7; i < 14; i++) {
            alaramButton.get(i).setChecked(false);
            switch (i) {
                case 7:
                    fabbutton_sunday_2.setChecked(false);
                    fabbutton_sunday_2.setTextOff("S");
                    break;
                case 8:
                    fabbutton_monday_2.setChecked(false);
                    fabbutton_monday_2.setTextOff("M");
                    break;
                case 9:
                    fabbutton_tuesday_2.setChecked(false);
                    fabbutton_tuesday_2.setTextOff("T");
                    break;
                case 10:
                    fabbutton_wednesday_2.setChecked(false);
                    fabbutton_wednesday_2.setTextOff("W");
                    break;
                case 11:
                    fabbutton_thursday_2.setChecked(false);
                    fabbutton_thursday_2.setTextOff("T");
                    break;
                case 12:
                    fabbutton_friday_2.setChecked(false);
                    fabbutton_friday_2.setTextOff("F");
                    break;
                case 13:
                    fabbutton_satrday_2.setChecked(false);
                    fabbutton_satrday_2.setTextOff("S");
                    break;
            }
        }
    }


    private String binaryDaysAlaram_1() {
        String inBinary = "";
        for (int i = 0; i < 7; i++) {
            if (alaramButton.get(i).isChecked()) {
                inBinary = inBinary + "1";
            } else {
                inBinary = inBinary + "0";
            }
        }
        return inBinary;
    }

    private String binaryDaysAlaram_2() {
        String inBinary = "";
        for (int i = 7; i < 14; i++) {
            if (alaramButton.get(i).isChecked()) {
                inBinary = inBinary + "1";
            } else {
                inBinary = inBinary + "0";
            }
        }
        return inBinary;
    }

    private PowerSocket_AlaramDays getButtonDataFromId(int button_UI_Id) {
        int objectId = -1;
        for (int i = 0; i < alaramButton.size(); i++) {
            if (alaramButton.get(i).getButtonId() == button_UI_Id) {
                objectId = alaramButton.get(i).getButtonIndex();
            }
        }
        return alaramButton.get(objectId);
    }

    private int getNumberOfDaysSelected_Alaram_1() {
        /**
         * For Top alaram the index starts from 0 - 6.
         * so the iteration from 0 to 6
         */
        int daysValueSelected = 0;
        for (int i = 0; i < 7; i++) {
            if (alaramButton.get(i).isChecked()) {
                daysValueSelected = daysValueSelected + alaramButton.get(i).getButtonValue();
            }
        }
        return daysValueSelected;
    }

    private int getNumberOfDaysSelected_Alaram_2() {
        /**
         * For Top alaram the index starts from 7 - 13.
         * so the iteration from 7 to 13
         */
        int daysValueSelected = 0;
        for (int i = 7; i < 14; i++) {
            if (alaramButton.get(i).isChecked()) {
                daysValueSelected = daysValueSelected + alaramButton.get(i).getButtonValue();
            }
        }
        return daysValueSelected;
    }

    @OnClick({R.id.ontime_Alaram_1_textView, R.id.offtime_Alaram_1_textView,
            R.id.ontime_Alaram_2_textView, R.id.offtime_Alaram_2_textView,})
    public void onclickAlaramSetTextView(View view) {
        switch (view.getId()) {
            case R.id.ontime_Alaram_1_textView: {
                getCurrentDate_Time_From_Mobile();
                if (checkbox_repeat_alaram_1.isChecked()) {
                    show_TimePickerDialog(getResources().getString(R.string.alaram_ID_1_ON_Fragment_PowerSocket_AlaramSchedule), true);
                } else {
                    showDatePickerDialog(getResources().getString(R.string.alaram_ID_1_ON_Fragment_PowerSocket_AlaramSchedule));
                }
            }
            break;
            case R.id.offtime_Alaram_1_textView: {
                getCurrentDate_Time_From_Mobile();
                if (checkbox_repeat_alaram_1.isChecked()) {
                    show_TimePickerDialog(getResources().getString(R.string.alaram_ID_1_OFF_Fragment_PowerSocket_AlaramSchedule), true);
                } else {
                    showDatePickerDialog(getResources().getString(R.string.alaram_ID_1_OFF_Fragment_PowerSocket_AlaramSchedule));
                }
            }
            break;
            case R.id.ontime_Alaram_2_textView: {
                getCurrentDate_Time_From_Mobile();
                if (checkbox_repeat_alaram_2.isChecked()) {
                    show_TimePickerDialog(getResources().getString(R.string.alaram_ID_2_ON_Fragment_PowerSocket_AlaramSchedule), true);
                } else {
                    showDatePickerDialog(getResources().getString(R.string.alaram_ID_2_ON_Fragment_PowerSocket_AlaramSchedule));

                }
            }
            break;
            case R.id.offtime_Alaram_2_textView: {
                getCurrentDate_Time_From_Mobile();
                if (checkbox_repeat_alaram_2.isChecked()) {
                    show_TimePickerDialog(getResources().getString(R.string.alaram_ID_2_OFF_Fragment_PowerSocket_AlaramSchedule), true);
                } else {
                    showDatePickerDialog(getResources().getString(R.string.alaram_ID_2_OFF_Fragment_PowerSocket_AlaramSchedule));
                }
            }
            break;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void convert_SelectedDate_Time_TimeStamp(int year_input, int month_input, int date_input, int hour_input, int mins_input, String tagClicked_ForSelection, boolean repeatedDays_true) {
        if (repeatedDays_true) {
            Calendar calendarLocal = new GregorianCalendar(TimeZone.getDefault());
            calendarLocal.set(Calendar.HOUR_OF_DAY, hour_input);
            calendarLocal.set(Calendar.MINUTE, mins_input);
            calendarLocal.set(Calendar.SECOND, 0);
            int timeStamp = (int) (calendarLocal.getTimeInMillis() / 1000);
            if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_1_ON_Fragment_PowerSocket_AlaramSchedule))) {
                onTimeStamp_Alaram_1 = timeStamp;
                textViewSetDate_Time(onTime_Alaram_1_text, calendarLocal, true);
            } else if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_1_OFF_Fragment_PowerSocket_AlaramSchedule))) {
                offTimeStamp_Alaram_1 = timeStamp;
                textViewSetDate_Time(offTime_Alaram_1_text, calendarLocal, true);
            } else if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_2_ON_Fragment_PowerSocket_AlaramSchedule))) {
                onTimeStamp_Alaram_2 = timeStamp;
                textViewSetDate_Time(onTime_Alaram_2_text, calendarLocal, true);
            } else if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_2_OFF_Fragment_PowerSocket_AlaramSchedule))) {
                offTimeStamp_Alaram_2 = timeStamp;
                textViewSetDate_Time(offTime_Alaram_2_text, calendarLocal, true);
            }
        } else {
            selectedYear = 0;
            selectedMonth = 0;
            selectedDay = 0;
            selectedHour = 0;
            selectedMinute = 0;
            Long timeStampAftterSelection;
            Calendar calendarLocal = new GregorianCalendar(TimeZone.getDefault());
            calendarLocal.set(year_input, month_input, date_input);
            calendarLocal.set(Calendar.HOUR_OF_DAY, hour_input);
            calendarLocal.set(Calendar.MINUTE, mins_input);
            calendarLocal.set(Calendar.SECOND, 0);
            timeStampAftterSelection = calendarLocal.getTimeInMillis();
            timeStampAftterSelection = timeStampAftterSelection / 1000;
            String ts = timeStampAftterSelection.toString();
            if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_1_ON_Fragment_PowerSocket_AlaramSchedule))) {
                onTimeStamp_Alaram_1 = Integer.parseInt(ts);
                textViewSetDate_Time(onTime_Alaram_1_text, calendarLocal, false);
            } else if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_1_OFF_Fragment_PowerSocket_AlaramSchedule))) {
                offTimeStamp_Alaram_1 = Integer.parseInt(ts);
                textViewSetDate_Time(offTime_Alaram_1_text, calendarLocal, false);
            } else if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_2_ON_Fragment_PowerSocket_AlaramSchedule))) {
                onTimeStamp_Alaram_2 = Integer.parseInt(ts);
                textViewSetDate_Time(onTime_Alaram_2_text, calendarLocal, false);
            } else if (tagClicked_ForSelection.equalsIgnoreCase(getResources().getString(R.string.alaram_ID_2_OFF_Fragment_PowerSocket_AlaramSchedule))) {
                offTimeStamp_Alaram_2 = Integer.parseInt(ts);
                textViewSetDate_Time(offTime_Alaram_2_text, calendarLocal, false);
            }
        }
    }

    private void showDatePickerDialog(String tagClicked_ForSelection) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), R.style.Date_Time_Theme, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                selectedYear = year;
                selectedMonth = month;
                selectedDay = dayOfMonth;
                show_TimePickerDialog(tagClicked_ForSelection, false);
            }
        }, present_mYear, present_mMonth, present_mDay);
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.show();
    }

    private void show_TimePickerDialog(String tagClicked_ForSelection, boolean repeatedDays_true) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), R.style.Date_Time_Theme, new TimePickerDialog.OnTimeSetListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                /**
                 * new logic implementation for checking the past time.
                 */
                Calendar datetime = Calendar.getInstance();
                Calendar c = Calendar.getInstance();
                datetime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                datetime.set(Calendar.MINUTE, minutes);
                if(repeatedDays_true){
                    selectedHour = hourOfDay;
                    selectedMinute = minutes;
                    convert_SelectedDate_Time_TimeStamp(present_mYear, present_mMonth, present_mDay, hourOfDay, minutes, tagClicked_ForSelection, true);
                }else {
                    Log.d(TAG, "onTimeSet: "+datetime.getTimeInMillis()+"    "+c.getTimeInMillis());
                    if (datetime.getTimeInMillis() > c.getTimeInMillis()+60000) {
//                    if (datetime.getTimeInMillis() > c.getTimeInMillis()) {
                        /**
                         * true means taking only future time.
                         */
                        selectedHour = hourOfDay;
                        selectedMinute = minutes;
                        convert_SelectedDate_Time_TimeStamp(selectedYear, selectedMonth, selectedDay, hourOfDay, minutes, tagClicked_ForSelection, false);

                    }else {
                        mActivity.mUtility.errorDialog("Please select Future time", 3, false);
                        return;
                    }
                }

                /*  if (datetime.getTimeInMillis() >= c.getTimeInMillis()) {
                 *//**
                 * true means taking only future time.
                 *//*
                    selectedHour = hourOfDay;
                    selectedMinute = minutes;
                    if (repeatedDays_true) {
                        convert_SelectedDate_Time_TimeStamp(present_mYear, present_mMonth, present_mDay, hourOfDay, minutes, tagClicked_ForSelection, true);
                    } else {
                        convert_SelectedDate_Time_TimeStamp(selectedYear, selectedMonth, selectedDay, hourOfDay, minutes, tagClicked_ForSelection, false);
                    }
                } else {
                    *//**
                 * false means show pop up that Time is past time Please select the future time...
                 *//*
                    mActivity.mUtility.errorDialog("Time already Passed", 3, false);
                    return;
                }*/


            }
        }, present_mHour, present_mMinute, false);
        timePickerDialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void textViewSetDate_Time(TextView date_Time, Calendar calendar, boolean repeatedDays_true) {
        String time = "";
        String date = "";
        /**
         * 24 hours Formatt
         */
/*        calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        String test = sdf.format(calendar.getTime());
        date_Time.setText(test);
        Log.e("TEST", test);*/
        /**
         * 12 hours formatt
         */
       /* calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
        String test = sdf.format(calendar.getTime());
        date_Time.setText(test);
        Log.e("TEST", test);*/

        /**
         * 12 hours formatt discarding mins.
         */
        // calendar = Calendar.getInstance();

        //     SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        //     String timeValue = sdf.format(calendar.getTime());
        //     date_Time.setText(timeValue);

        if (repeatedDays_true) {
            SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm a");
            time = time_sdf.format(calendar.getTime());
            date_Time.setText(time);
        } else {
            /**
             * Time fetching
             */
            SimpleDateFormat time_sdf = new SimpleDateFormat("hh:mm a");
            time = time_sdf.format(calendar.getTime());
            /**
             * Date Fetching.
             */
            SimpleDateFormat date_sdf = new SimpleDateFormat("dd/MM/yyyy");
            date = date_sdf.format(calendar.getTime());
            date_Time.setText(time + "\n" + date);
        }
    }

    private void getCurrentDate_Time_From_Mobile() {
        Calendar c = Calendar.getInstance();
        present_mYear = c.get(Calendar.YEAR);
        present_mMonth = c.get(Calendar.MONTH);
        present_mDay = c.get(Calendar.DAY_OF_MONTH);
        /**
         * Picking up time from android.
         */
        present_mHour = c.get(Calendar.HOUR_OF_DAY);// 24 HOURS formatt--->HOUR_OF_DAY
        present_mMinute = c.get(Calendar.MINUTE);
    }

    private void update_Insert_AlaramDataBase(int alaramId_1_2, int socketId, int number_of_days_Selected, String ontimeStamp, String offtimeStamp, String onOriginal, String offOriginal, String bleAddress, String daysSelectedInBinary) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (alaramId_1_2 % 2 == 0) {
                    if (checkbox_repeat_alaram_2.isChecked()) {
                        previousStateAlaram_2 = getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule);
                    } else {
                        previousStateAlaram_2 = getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule);
                    }
                } else {
                    if (checkbox_repeat_alaram_1.isChecked()) {
                        previousStateAlaram_1 = getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule);
                    } else {
                        previousStateAlaram_1 = getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule);
                    }
                }
                ContentValues contentValues = new ContentValues();
                contentValues.put(mfield_alarm_id, alaramId_1_2);
                contentValues.put(mfield_socket_id, socketId);
                contentValues.put(mfield_day_value, number_of_days_Selected);
                contentValues.put(mfield_OnTimestamp, ontimeStamp);
                contentValues.put(mfield_OffTimestamp, offtimeStamp);
                contentValues.put(mfield_On_original, onOriginal);
                contentValues.put(mfield_Off_original, offOriginal);
                contentValues.put(mfield_alarm_state, "NA");
                contentValues.put(mfield_ble_address, bleAddress);
                contentValues.put(mfield_day_selected, daysSelectedInBinary);
                boolean recordExists = mActivity.mDbHelper.checkRecordAvaliableInTable(mTableAlarmPowerSocket, mfield_socket_id, mfield_alarm_id, mfield_ble_address, "" + socketId, "" + alaramId_1_2, bleAddress);
                if (recordExists) {
                    /**
                     * Insert Record
                     */
                    mActivity.mDbHelper.insertRecord(mTableAlarmPowerSocket, contentValues);
                } else {
                    /**
                     * Update the Record for mulitple where Condition.
                     */
                    mActivity.mDbHelper.updateRecord(mTableAlarmPowerSocket, contentValues, "alarm_id=? and socket_id=? and ble_address=?", new String[]{"" + alaramId_1_2, "" + socketId, bleAddress});
                }
            }
        });
    }

    private void update_Insert_AlaramDataBase(int alaramId_1_2) {
        if (alaramId_1_2 % 2 == 0) {
            if (checkbox_repeat_alaram_2.isChecked()) {
                previousStateAlaram_2 = getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule);
            } else {
                previousStateAlaram_2 = getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule);
            }
        } else {
            if (checkbox_repeat_alaram_1.isChecked()) {
                previousStateAlaram_1 = getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule);
            } else {
                previousStateAlaram_1 = getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule);
            }
        }
    }

    private void set_AlaramIdValues() {
        if (selected_ScoketdId == 0) {
            alaramId_1 = 1;
            alaramId_2 = 2;
        } else if (selected_ScoketdId == 1) {
            alaramId_1 = 3;
            alaramId_2 = 4;
        } else if (selected_ScoketdId == 2) {
            alaramId_1 = 5;
            alaramId_2 = 6;
        } else if (selected_ScoketdId == 3) {
            alaramId_1 = 7;
            alaramId_2 = 8;
        } else if (selected_ScoketdId == 4) {
            alaramId_1 = 9;
            alaramId_2 = 10;
        } else if (selected_ScoketdId == 5) {
            alaramId_1 = 11;
            alaramId_2 = 12;
        }
    }

    @OnClick(R.id.repeat_alaram_2_checkBox)
    public void repeatAlaram_Checkbox_2_click() {
        if (previousStateAlaram_2.equalsIgnoreCase(getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule))) {
            if (checkbox_repeat_alaram_2.isChecked()) {
                linearlayout_alramId_2.setVisibility(View.VISIBLE);
            } else {
                linearlayout_alramId_2.setVisibility(View.GONE);
            }
        } else if ((checkbox_repeat_alaram_2.isChecked()) && (previousStateAlaram_2.equalsIgnoreCase(getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule)))) {
            prepareUiIfValuesAlreadyStored();
        } else if ((!checkbox_repeat_alaram_2.isChecked()) && (previousStateAlaram_2.equalsIgnoreCase(getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule)))) {
            prepareUiIfValuesAlreadyStored();
        } else if ((!checkbox_repeat_alaram_2.isChecked()) && (previousStateAlaram_2.equalsIgnoreCase(getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule)))) {
            linearlayout_alramId_2.setVisibility(View.GONE);
            onTime_Alaram_2_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
            offTime_Alaram_2_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
        } else if ((checkbox_repeat_alaram_2.isChecked()) && (previousStateAlaram_2.equalsIgnoreCase(getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule)))) {
            linearlayout_alramId_2.setVisibility(View.VISIBLE);
            onTime_Alaram_2_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
            offTime_Alaram_2_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
        }
    }

    @OnClick(R.id.repeat_alaram_1_checkBox)
    public void repeatAlaram_Checkbox_1_click() {
        if (previousStateAlaram_1.equalsIgnoreCase(getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule))) {
            if (checkbox_repeat_alaram_1.isChecked()) {
                linearlayout_alramId_1.setVisibility(View.VISIBLE);
            } else {
                linearlayout_alramId_1.setVisibility(View.GONE);
            }
        } else if ((checkbox_repeat_alaram_1.isChecked()) && (previousStateAlaram_1.equalsIgnoreCase(getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule)))) {
            prepareUiIfValuesAlreadyStored();
        } else if ((!checkbox_repeat_alaram_1.isChecked()) && (previousStateAlaram_1.equalsIgnoreCase(getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule)))) {
            prepareUiIfValuesAlreadyStored();
        } else if ((!checkbox_repeat_alaram_1.isChecked()) && (previousStateAlaram_1.equalsIgnoreCase(getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule)))) {
            linearlayout_alramId_1.setVisibility(View.GONE);
            onTime_Alaram_1_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
            offTime_Alaram_1_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
        } else if ((checkbox_repeat_alaram_1.isChecked()) && (previousStateAlaram_1.equalsIgnoreCase(getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule)))) {
            linearlayout_alramId_1.setVisibility(View.VISIBLE);
            onTime_Alaram_1_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
            offTime_Alaram_1_text.setText(getResources().getString(R.string.not_appplicable_Fragment_PowerSocket_AlaramSchedule));
        }
    }

    @OnClick(R.id.deleteAlaram_1)
    public void delAlaram_1() {
        String onTimeAlaram_1_textViewValue = onTime_Alaram_1_text.getText().toString();
        String offTimeAlaram_1_textViewValue = offTime_Alaram_1_text.getText().toString();
        if (onTimeAlaram_1_textViewValue.equalsIgnoreCase("NA") && offTimeAlaram_1_textViewValue.equalsIgnoreCase("NA")) {
            mActivity.mUtility.errorDialog("Pelase Set the Alaram Frist", 3, false);
            return;
        }
        local_TransactionId = -1;
        local_TransactionId = generateRandomNumber();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isEnabled() && powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
            alaramDelete_1 = true;
            powerSocketBLEService.deleteAlaram(alaramId_1, powerSocket, local_TransactionId);
        } else if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
            alaramDelete_1 = true;
            powerSocketMQTTService.deleteAlarma_mqtt(powerSocket, alaramId_1, local_TransactionId);
        } else {
            mActivity.mUtility.errorDialog("Please Use Wifi\nBluetooth to Delete", 3, false);
            return;
        }
        mActivity.showProgress("Please wait", false);
 /*       String bleAddress = powerSocket.bleAddress.replace(":", "");
        String quereyDelete_Alaram_1 = "DELETE FROM " + mTablePowerSocket + " WHERE " + mfield_socket_id + "= '" + selected_ScoketdId + "'" + " AND " + mfield_alarm_id + "= '" + alaramId_1 + "'" + " AND " + mfield_ble_address + " ='" + bleAddress + "'";
        mActivity.mDbHelper.exeQuery(quereyDelete_Alaram_1);*/
        /**
         * UI chnages.
         */
        previousStateAlaram_1 = getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule);
        daysUnchecked_makeAlaram_1();
        UiChangesDeleteAlaram_1();
        onTimeStamp_Alaram_1 = 0;
        offTimeStamp_Alaram_1 = 0;
        mActivity.hideProgress();
    }

    private void UiChangesDeleteAlaram_1() {
        checkbox_repeat_alaram_1.setChecked(false);
        linearlayout_alramId_1.setVisibility(View.GONE);
        onTime_Alaram_1_text.setText("NA");
        offTime_Alaram_1_text.setText("NA");
    }

    @OnClick(R.id.deleteAlaram_2)
    public void delAlarm_2() {
        String onTimeAlaram_2_textViewValue = onTime_Alaram_2_text.getText().toString();
        String offTimeAlaram_2_textViewValue = offTime_Alaram_2_text.getText().toString();
        if (onTimeAlaram_2_textViewValue.equalsIgnoreCase("NA") && offTimeAlaram_2_textViewValue.equalsIgnoreCase("NA")) {
            mActivity.mUtility.errorDialog("Pelase Set the Alaram Frist", 3, false);
            return;
        }
        /**
         * Delete alaram from BLE.
         */
        local_TransactionId = -1;
        local_TransactionId = generateRandomNumber();
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter.isEnabled() && powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
            alaramDelete_2 = true;
            powerSocketBLEService.deleteAlaram(alaramId_2, powerSocket, local_TransactionId);
        } else if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
            alaramDelete_2 = true;
            powerSocketMQTTService.deleteAlarma_mqtt(powerSocket, alaramId_2, local_TransactionId);
        } else {
            mActivity.mUtility.errorDialog("Please Use Wifi\nBluetooth to Delete", 3, false);
            return;
        }
        mActivity.showProgress("Please wait", false);
     /*   String bleAddress = powerSocket.bleAddress.replace(":", "");
        String quereyDelete_Alaram_1 = "DELETE FROM " + mTablePowerSocket + " WHERE " + mfield_socket_id + "= '" + selected_ScoketdId + "'" + " AND " + mfield_alarm_id + "= '" + alaramId_2 + "'" + " AND " + mfield_ble_address + " ='" + bleAddress + "'";
        mActivity.mDbHelper.exeQuery(quereyDelete_Alaram_1);*/
        previousStateAlaram_2 = getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule);
        daysUnchecked_makeAlaram_2();
        onTimeStamp_Alaram_2 = 0;
        offTimeStamp_Alaram_2 = 0;
        UiChangesDeleteAlaram_2();
        mActivity.hideProgress();
    }

    private void UiChangesDeleteAlaram_2() {
        checkbox_repeat_alaram_2.setChecked(false);
        linearlayout_alramId_2.setVisibility(View.GONE);
        onTime_Alaram_2_text.setText("NA");
        offTime_Alaram_2_text.setText("NA");
    }

    PowerSocketAlaramUIDays powerSocketAlaramUIDays_1;
    PowerSocketAlaramUIDays powerSocketAlaramUIDays_2;

    private void prepareUiIfValuesAlreadyStored() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String bleAddress = powerSocket.bleAddress.replace(":", "");
                String alaram_querey_1 = "SELECT * from " + mTableAlarmPowerSocket + " WHERE " + mfield_socket_id + "= '" + selected_ScoketdId + "'" + "AND " + mfield_alarm_id + "= '" + alaramId_1 + "'" + " AND " + mfield_ble_address + "= '" + bleAddress + "'";
                String alaram_querey_2 = "SELECT * from " + mTableAlarmPowerSocket + " WHERE " + mfield_socket_id + "= '" + selected_ScoketdId + "'" + "AND " + mfield_alarm_id + "= '" + alaramId_2 + "'" + " AND " + mfield_ble_address + "= '" + bleAddress + "'";
                DataHolder mDataHolderLight_alarma_1 = mActivity.mDbHelper.readData(alaram_querey_1);
                DataHolder mDataHolderLight_alarma_2 = mActivity.mDbHelper.readData(alaram_querey_2);

                powerSocketAlaramUIDays_1 = null;
                powerSocketAlaramUIDays_2 = null;

                for (int i = 0; i < mDataHolderLight_alarma_1.get_Listholder().size(); i++) {
                    powerSocketAlaramUIDays_1 = new PowerSocketAlaramUIDays();
                    powerSocketAlaramUIDays_1.setDaysSelected(mDataHolderLight_alarma_1.get_Listholder().get(i).get(DBHelper.mfield_day_selected));
                    powerSocketAlaramUIDays_1.setDayValue(Integer.parseInt(mDataHolderLight_alarma_1.get_Listholder().get(i).get(DBHelper.mfield_day_value)));
                    powerSocketAlaramUIDays_1.setOnOriginalTime(mDataHolderLight_alarma_1.get_Listholder().get(i).get(DBHelper.mfield_On_original));
                    powerSocketAlaramUIDays_1.setOffOriginalTime(mDataHolderLight_alarma_1.get_Listholder().get(i).get(DBHelper.mfield_Off_original));
                    powerSocketAlaramUIDays_1.setOnTimeStamp(Integer.parseInt(mDataHolderLight_alarma_1.get_Listholder().get(i).get(DBHelper.mfield_OnTimestamp)));
                    powerSocketAlaramUIDays_1.setOffTimeStamp(Integer.parseInt(mDataHolderLight_alarma_1.get_Listholder().get(i).get(DBHelper.mfield_OffTimestamp)));
                }
                for (int i = 0; i < mDataHolderLight_alarma_2.get_Listholder().size(); i++) {
                    powerSocketAlaramUIDays_2 = new PowerSocketAlaramUIDays();
                    powerSocketAlaramUIDays_2.setDaysSelected(mDataHolderLight_alarma_2.get_Listholder().get(i).get(DBHelper.mfield_day_selected));
                    powerSocketAlaramUIDays_2.setDayValue(Integer.parseInt(mDataHolderLight_alarma_2.get_Listholder().get(i).get(DBHelper.mfield_day_value)));
                    powerSocketAlaramUIDays_2.setOnOriginalTime(mDataHolderLight_alarma_2.get_Listholder().get(i).get(DBHelper.mfield_On_original));
                    powerSocketAlaramUIDays_2.setOffOriginalTime(mDataHolderLight_alarma_2.get_Listholder().get(i).get(DBHelper.mfield_Off_original));
                    powerSocketAlaramUIDays_2.setOnTimeStamp(Integer.parseInt(mDataHolderLight_alarma_2.get_Listholder().get(i).get(DBHelper.mfield_OnTimestamp)));
                    powerSocketAlaramUIDays_2.setOffTimeStamp(Integer.parseInt(mDataHolderLight_alarma_2.get_Listholder().get(i).get(DBHelper.mfield_OffTimestamp)));
                }
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        /**
                         * UI manipulation if alaram has been already set.
                         */

                        if (powerSocketAlaramUIDays_1 != null) {
                            if (powerSocketAlaramUIDays_1.getDayValue() > 0) {
                                checkbox_repeat_alaram_1.setChecked(true);
                                previousStateAlaram_1 = getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule);
                                linearlayout_alramId_1.setVisibility(View.VISIBLE);
                                onTime_Alaram_1_text.setText(powerSocketAlaramUIDays_1.getOnOriginalTime());
                                offTime_Alaram_1_text.setText(powerSocketAlaramUIDays_1.getOffOriginalTime());
                                onTimeStamp_Alaram_1 = powerSocketAlaramUIDays_1.getOnTimeStamp();
                                offTimeStamp_Alaram_1 = powerSocketAlaramUIDays_1.getOffTimeStamp();
                                String daysSelectedInBin_Alaram_1 = powerSocketAlaramUIDays_1.getDaysSelected();
                                char[] arrayValuesForDaySelected_alaram_1 = daysSelectedInBin_Alaram_1.toCharArray();
                                for (int i = 0; i < arrayValuesForDaySelected_alaram_1.length; i++) {
                                    char individaulData = arrayValuesForDaySelected_alaram_1[i];
                                    switch (i) {
                                        case 0:
                                            if (individaulData == '1') {
                                                alaramButton.get(0).setChecked(true);
                                                fabbutton_sunday_1.setChecked(true);
                                                fabbutton_sunday_1.setTextOn("S");


                                            } else if (individaulData == '0') {
                                                alaramButton.get(0).setChecked(false);
                                                fabbutton_sunday_1.setChecked(false);
                                                fabbutton_sunday_1.setTextOff("S");

                                            }
                                            break;
                                        case 1:
                                            if (individaulData == '1') {
                                                alaramButton.get(1).setChecked(true);


                                                fabbutton_monday_1.setChecked(true);
                                                fabbutton_monday_1.setTextOn("M");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(1).setChecked(false);


                                                fabbutton_monday_1.setChecked(false);
                                                fabbutton_monday_1.setTextOff("M");

                                            }
                                            break;
                                        case 2:
                                            if (individaulData == '1') {
                                                alaramButton.get(2).setChecked(true);


                                                fabbutton_tuesday_1.setChecked(true);
                                                fabbutton_tuesday_1.setTextOn("T");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(2).setChecked(false);


                                                fabbutton_tuesday_1.setChecked(false);
                                                fabbutton_tuesday_1.setTextOff("T");


                                            }
                                            break;
                                        case 3:
                                            if (individaulData == '1') {
                                                alaramButton.get(3).setChecked(true);


                                                fabbutton_wednesday_1.setChecked(true);
                                                fabbutton_wednesday_1.setTextOn("W");


                                            } else if (individaulData == '0') {
                                                alaramButton.get(3).setChecked(false);


                                                fabbutton_wednesday_1.setChecked(false);
                                                fabbutton_wednesday_1.setTextOff("W");

                                            }
                                            break;
                                        case 4:
                                            if (individaulData == '1') {
                                                alaramButton.get(4).setChecked(true);


                                                fabbutton_thursday_1.setChecked(true);
                                                fabbutton_thursday_1.setTextOn("T");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(4).setChecked(false);


                                                fabbutton_thursday_1.setChecked(false);
                                                fabbutton_thursday_1.setTextOff("T");
                                            }
                                            break;
                                        case 5:
                                            if (individaulData == '1') {
                                                alaramButton.get(5).setChecked(true);


                                                fabbutton_friday_1.setChecked(true);
                                                fabbutton_friday_1.setTextOn("F");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(5).setChecked(false);


                                                fabbutton_friday_1.setChecked(false);
                                                fabbutton_friday_1.setTextOff("F");
                                            }
                                            break;
                                        case 6:
                                            if (individaulData == '1') {
                                                alaramButton.get(6).setChecked(true);


                                                fabbutton_satrday_1.setChecked(true);
                                                fabbutton_satrday_1.setTextOn("S");


                                            } else if (individaulData == '0') {
                                                alaramButton.get(6).setChecked(false);


                                                fabbutton_satrday_1.setChecked(false);
                                                fabbutton_satrday_1.setTextOff("S");


                                            }
                                            break;
                                    }
                                }

                            } else if ((powerSocketAlaramUIDays_1 != null) && (powerSocketAlaramUIDays_1.getOffOriginalTime().toString().length() > 0) && (powerSocketAlaramUIDays_1.getOnOriginalTime().toString().length() > 0)) {
                                checkbox_repeat_alaram_1.setChecked(false);
                                previousStateAlaram_1 = getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule);
                                linearlayout_alramId_1.setVisibility(View.GONE);
                                onTime_Alaram_1_text.setText(powerSocketAlaramUIDays_1.getOnOriginalTime());
                                offTime_Alaram_1_text.setText(powerSocketAlaramUIDays_1.getOffOriginalTime());
                                onTimeStamp_Alaram_1 = powerSocketAlaramUIDays_1.getOnTimeStamp();
                                offTimeStamp_Alaram_1 = powerSocketAlaramUIDays_1.getOffTimeStamp();
                            }
                        } else if (powerSocketAlaramUIDays_1 == null) {
                            previousStateAlaram_1 = getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule);
                            daysUnchecked_makeAlaram_1();
                            UiChangesDeleteAlaram_1();
                            onTimeStamp_Alaram_1 = 0;
                            offTimeStamp_Alaram_1 = 0;
                        }
                        if (powerSocketAlaramUIDays_2 != null) {
                            if (powerSocketAlaramUIDays_2.getDayValue() > 0) {
                                checkbox_repeat_alaram_2.setChecked(true);
                                previousStateAlaram_2 = getResources().getString(R.string.previous_state_checked_Fragment_PowerSocket_AlaramSchedule);
                                linearlayout_alramId_2.setVisibility(View.VISIBLE);
                                onTime_Alaram_2_text.setText(powerSocketAlaramUIDays_2.getOnOriginalTime());
                                offTime_Alaram_2_text.setText(powerSocketAlaramUIDays_2.getOffOriginalTime());
                                onTimeStamp_Alaram_2 = powerSocketAlaramUIDays_2.getOnTimeStamp();
                                offTimeStamp_Alaram_2 = powerSocketAlaramUIDays_2.getOffTimeStamp();
                                /**
                                 * Bottom Alaram
                                 */
                                String daysSelectedInBin_Alaram_2 = powerSocketAlaramUIDays_2.getDaysSelected();
                                char[] arrayValuesForDaySelected_alaram_2 = daysSelectedInBin_Alaram_2.toCharArray();

                                for (int i = 0; i < arrayValuesForDaySelected_alaram_2.length; i++) {
                                    char individaulData = arrayValuesForDaySelected_alaram_2[i];
                                    switch (i) {
                                        case 0:
                                            if (individaulData == '1') {
                                                alaramButton.get(7).setChecked(true);

                                                fabbutton_sunday_2.setChecked(true);
                                                fabbutton_sunday_2.setTextOn("S");


                                            } else if (individaulData == '0') {
                                                alaramButton.get(7).setChecked(false);
                                                fabbutton_sunday_2.setChecked(false);
                                                fabbutton_sunday_2.setTextOff("S");

                                            }
                                            break;
                                        case 1:
                                            if (individaulData == '1') {
                                                alaramButton.get(8).setChecked(true);
                                                fabbutton_monday_2.setChecked(true);
                                                fabbutton_monday_2.setTextOn("M");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(8).setChecked(false);
                                                fabbutton_monday_2.setChecked(false);
                                                fabbutton_monday_2.setTextOff("M");
                                            }
                                            break;
                                        case 2:
                                            if (individaulData == '1') {
                                                alaramButton.get(9).setChecked(true);
                                                fabbutton_tuesday_2.setChecked(true);
                                                fabbutton_tuesday_2.setTextOn("T");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(9).setChecked(false);
                                                fabbutton_tuesday_2.setChecked(false);
                                                fabbutton_tuesday_2.setTextOff("T");
                                            }
                                            break;
                                        case 3:
                                            if (individaulData == '1') {
                                                alaramButton.get(10).setChecked(true);
                                                fabbutton_wednesday_2.setChecked(true);
                                                fabbutton_wednesday_2.setTextOn("W");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(10).setChecked(false);
                                                fabbutton_wednesday_2.setChecked(false);
                                                fabbutton_wednesday_2.setTextOff("W");
                                            }
                                            break;
                                        case 4:
                                            if (individaulData == '1') {
                                                alaramButton.get(11).setChecked(true);
                                                fabbutton_thursday_2.setChecked(true);
                                                fabbutton_thursday_2.setTextOn("T");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(11).setChecked(false);
                                                fabbutton_thursday_2.setChecked(false);
                                                fabbutton_thursday_2.setTextOff("T");
                                            }
                                            break;
                                        case 5:
                                            if (individaulData == '1') {
                                                alaramButton.get(12).setChecked(true);
                                                fabbutton_friday_2.setChecked(true);
                                                fabbutton_friday_2.setTextOn("F");


                                            } else if (individaulData == '0') {
                                                alaramButton.get(12).setChecked(false);
                                                fabbutton_friday_2.setChecked(false);
                                                fabbutton_friday_2.setTextOff("F");
                                            }
                                            break;
                                        case 6:
                                            if (individaulData == '1') {
                                                alaramButton.get(13).setChecked(true);
                                                fabbutton_satrday_2.setChecked(true);
                                                fabbutton_satrday_2.setTextOn("S");

                                            } else if (individaulData == '0') {
                                                alaramButton.get(13).setChecked(false);
                                                fabbutton_satrday_2.setChecked(false);
                                                fabbutton_satrday_2.setTextOff("S");
                                            }
                                            break;
                                    }
                                }
                            } else if ((powerSocketAlaramUIDays_2 != null) && (powerSocketAlaramUIDays_2.getOffOriginalTime().toString().length() > 0) && (powerSocketAlaramUIDays_2.getOnOriginalTime().toString().length() > 0)) {
                                checkbox_repeat_alaram_2.setChecked(false);
                                previousStateAlaram_2 = getResources().getString(R.string.previous_state_unchecked_Fragment_PowerSocket_AlaramSchedule);
                                linearlayout_alramId_2.setVisibility(View.GONE);
                                onTime_Alaram_2_text.setText(powerSocketAlaramUIDays_2.getOnOriginalTime());
                                offTime_Alaram_2_text.setText(powerSocketAlaramUIDays_2.getOffOriginalTime());
                                onTimeStamp_Alaram_2 = powerSocketAlaramUIDays_2.getOnTimeStamp();
                                offTimeStamp_Alaram_2 = powerSocketAlaramUIDays_2.getOffTimeStamp();
                            }
                        } else if (powerSocketAlaramUIDays_2 == null) {
                            previousStateAlaram_2 = getResources().getString(R.string.previous_state_unknown_Fragment_PowerSocket_AlaramSchedule);
                            daysUnchecked_makeAlaram_2();
                            UiChangesDeleteAlaram_2();
                            onTimeStamp_Alaram_2 = 0;
                            offTimeStamp_Alaram_2 = 0;
                        }
                    }
                });
            }
        });
    }

    PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks = new PowerSocketBLEEventCallbacks() {
        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {

        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {

        }

        @Override
        public void onAuthenticationSuccess(String deviceAddress) {
            super.onAuthenticationSuccess(deviceAddress);
            //
        }

        @Override
        public void onAuthenticationFailure(String deviceAddress) {
            super.onAuthenticationFailure(deviceAddress);
            // Todo Display message -> Authentication Failure
        }

        @Override
        public void onAlaramSetBLE(String deviceAddress, int transactionId, byte alaramId, byte socketId, byte sucess_failure) {
            super.onAlaramSetBLE(deviceAddress, transactionId, alaramId, socketId, sucess_failure);
            if ((sucess_failure == SUCESS) && (transactionId == local_TransactionId)) {
                if (alaramId % 2 != 0) {
                    /**
                     * Top alaram
                     */
                    if (alaramSet_1) {
                        alaramSet_1 = false;
                        mActivity.mUtility.errorDialog("Alaram Set Sucessfull Alaram Id= " +
                                alaramId, 0, true);
                        prepareUiIfValuesAlreadyStored();
                    }
                } else if (alaramId % 2 == 0) {
                    /**
                     * Bottom Alaram
                     */
                    if (alaramSet_2) {
                        alaramSet_2 = false;
                        mActivity.mUtility.errorDialog("Alaram Set Sucessfull Alaram Id= " + alaramId,
                                0, true);
                        prepareUiIfValuesAlreadyStored();
                    }
                }
            } else if (sucess_failure == FAILURE) {
                mActivity.mUtility.errorDialog("Something Went Wrong Please Try again.", 3, true);
            } else if ((sucess_failure == SUCESS) && (transactionId != local_TransactionId)) {
                prepareUiIfValuesAlreadyStored();
            }
        }

        @Override
        public void onAlaramDeleteBLE(String deviceAddress, int transactionId, byte alaram_id, byte sucess_failure) {
            super.onAlaramDeleteBLE(deviceAddress, transactionId, alaram_id, sucess_failure);
            String bleAddress = powerSocket.bleAddress.replace(":", "");
            if ((sucess_failure == SUCESS) && (transactionId == local_TransactionId)) {
                if (alaram_id % 2 != 0) {
                    if (alaramDelete_1) {
                        alaramDelete_1 = false;
                        mActivity.mUtility.errorDialog("Alaram Delete Sucessfull for Socekt ID " + alaram_id,
                                1, true);
                        prepareUiIfValuesAlreadyStored();
                    }

                } else if (alaram_id % 2 == 0) {
                    if (alaramDelete_2) {
                        alaramDelete_2 = false;
                        mActivity.mUtility.errorDialog("Alaram Delete Sucessfull for Socekt ID " + alaram_id,
                                1, true);
                        prepareUiIfValuesAlreadyStored();
                    }
                }
            } else if (sucess_failure == FAILURE) {
                mActivity.mUtility.errorDialog("Something Went Wrong Please Try again.", 3, true);
            } else if ((sucess_failure == SUCESS) && (transactionId != local_TransactionId)) {
                prepareUiIfValuesAlreadyStored();
            }

        }

        @Override
        public void onAlaramUpdatedFromDeviceBLE(boolean result) {
            super.onAlaramUpdatedFromDeviceBLE(result);
            if (result) {
                Fragment currentFragment = getFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
                if (currentFragment instanceof FragmentPowerSocketAlaramSchedule) {
                    if (isVisible()) {
                        prepareUiIfValuesAlreadyStored();
                    }
                }
            }
        }
    };

    PowerSocketMQTTEventCallbacks powerSocketMQTTEventCallbacks = new PowerSocketMQTTEventCallbacks() {
        @Override
        public void onAlaramDeleteMqtt(String deviceAddress, int transactionId, byte alaram_id, byte sucess_failure) {
            super.onAlaramDeleteMqtt(deviceAddress, transactionId, alaram_id, sucess_failure);
            if ((sucess_failure == SUCESS) && (transactionId == local_TransactionId)) {
                if (alaram_id % 2 != 0) {
                    if (alaramDelete_1) {
                        alaramDelete_1 = false;
                        mActivity.mUtility.errorDialog("Alaram Delete Sucessfull for Socekt ID " + alaram_id, 1, true);
                        prepareUiIfValuesAlreadyStored();
                    }
                } else if (alaram_id % 2 == 0) {
                    if (alaramDelete_2) {
                        alaramDelete_2 = false;
                        mActivity.mUtility.errorDialog("Alaram Delete Sucessfull for Socekt ID " + alaram_id, 1, true);
                        prepareUiIfValuesAlreadyStored();
                    }
                }

            } else if (sucess_failure == FAILURE) {
                mActivity.mUtility.errorDialog("Something Went Wrong Please Try again.", 3, true);
            } else if ((sucess_failure == SUCESS) && (transactionId != local_TransactionId)) {
                prepareUiIfValuesAlreadyStored();
            }
        }

        @Override
        public void onMQTTConnected() {

        }

        @Override
        public void onMQTTDisconnected() {

        }

        @Override
        public void onMQTTConnectionFailed() {

        }

        @Override
        public void onMQTTException() {

        }

        @Override
        public void onMQTTTimeout(String deviceAddress) {

        }

        @Override
        public void onAlaramSetMqtt(String deviceAddress, int transactionId, byte alaramId, byte socketId, byte sucess_failure) {
            super.onAlaramSetMqtt(deviceAddress, transactionId, alaramId, socketId, sucess_failure);
            mActivity.hideProgress();
            if ((sucess_failure == SUCESS) && (transactionId == local_TransactionId)) {
                if (alaramId % 2 != 0) {
                    /**
                     * Top alaram
                     */
                    if (alaramSet_1) {
                        alaramSet_1 = false;
                        mActivity.mUtility.errorDialog("Alaram Set Sucessfull Alaram Id= " + alaramId, 0, true);
                        prepareUiIfValuesAlreadyStored();
                    }

                } else if (alaramId % 2 == 0) {
                    /**
                     * Bottom Alaram
                     */
                    if (alaramSet_2) {
                        alaramSet_2 = false;
                        mActivity.mUtility.errorDialog("Alaram Set Sucessfull Alaram Id= " + alaramId, 0, true);
                        prepareUiIfValuesAlreadyStored();
                    }
                }

            } else if (sucess_failure == FAILURE) {
                mActivity.mUtility.errorDialog("Something Went Wrong Please Try again.", 3, true);
            } else if ((sucess_failure == SUCESS) && (transactionId != local_TransactionId)) {
                prepareUiIfValuesAlreadyStored();
            }
        }

        @Override
        public void onAlaramUpdatedFromDeviceMQTT(boolean result) {
            super.onAlaramUpdatedFromDeviceMQTT(result);
            Fragment currentFragment = getFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
            if (currentFragment instanceof FragmentPowerSocketAlaramSchedule) {
                if (isVisible()) {
                    prepareUiIfValuesAlreadyStored();
                }
            }
        }
    };

    /**
     * Use this method to call for alaram Detials
     */
//    private void askAlaramDetials() {
//        /**
//         * BLE asking for alaram Details.
//         */
//        powerSocketBLEService.askAlaramDetailsFrom_socket(powerSocket);
//        /**
//         * MQTT asking for alaram Detials.
//         */
//        powerSocketMQTTService.askAlaramDetailsFrom_socket_Mqtt(powerSocket);
//    }
    private static int generateRandomNumber() {
        int min = 1;
        int max = 65535;
        int random = (int) ((int) min + (max - min) * Math.random());
        return random;
    }
}