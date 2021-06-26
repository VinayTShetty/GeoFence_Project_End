// Todo -> Add/Update Sync backlog devices using AddAPI
package com.vithamastech.smartlight.fragments;

import android.animation.Animator;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evergreen.ble.advertisement.AdvertisementRecord;
import com.evergreen.ble.advertisement.ManufactureData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.vithamastech.smartlight.LoginActivity;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.OnBluetoothStateChangeListener;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.vithamastech.smartlight.interfaces.onSyncComplete;
import com.vithamastech.smartlight.services.PowerSocketBLEService;
import com.vithamastech.smartlight.services.PowerSocketBLEEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketMQTTEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketMQTTService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.vithamastech.smartlight.MainActivity.mActivityPowerSocketSelected;
import static com.vithamastech.smartlight.MainActivity.mBluetoothManager;
import static com.vithamastech.smartlight.db.DBHelper.mTableAlarmPowerSocket;
import static com.vithamastech.smartlight.db.DBHelper.mfield_ble_address;

public class FragmentDevices extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_device_button_add_device)
    Button mButtonAddDevice;
    @BindView(R.id.fragment_device_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_device_relativelayout_no_device)
    RelativeLayout mRelativeLayoutNoDeviceFound;
    @BindView(R.id.fragment_device_swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.activity_register_rl_main)
    CoordinatorLayout coordinatorLayout;
    ArrayList<VoDeviceList> mArrayListDevice = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    private boolean isCalling = true;
    private int mExpandedPosition = -1;
    private int previousExpandedPosition = -1;
    private int intDeleteDevicePosition;
    boolean isDeviceDelete = false;
    String mStringDeviceCommHexID = "";
    String mStringDeviceBleAddress = "";
    startDeviceScanTimer mStartDeviceScanTimer;
    startDeviceStateCheckTimer mStartDeviceStateCheckTimer;
    private boolean isCurrentFragment = false;
    boolean isAnyDeviceDeleted = false;
    boolean isCheckDeviceState = true;
    boolean isFistTimeCheck = true;
    Dialog myDialogSetting;
    String mStringDeviceHexData;
    String mStringDeviceHexForState;
    String mStringDeviceTypeForState;
    String mStringDeviceOnOffForState;
    boolean mABooleanColorChange = false;
    int mIntDeviceRGBColorForState;
    int mIntSeekSelectedValue = 100;
    int mIntSeekChangeValue = 100;
    float[] DeviceHsvForState;
    float mDeviceBrightnessForState;
    String[] mArrayBrightness;
    String[] mStateCheckParam;
    ContentValues mContentValuesState;
    MyTimerDeviceStateTask myTimerDeviceStateTask;
    Timer mTimer;
    /**
     * new Design animation
     */

    @BindView(R.id.mainfab_container)
    LinearLayout mainFabContainer_linearLayout;
    @BindView(R.id.powerSocket_linear_layout)
    LinearLayout linearLayout_powerSocket;
    @BindView(R.id.smartlight_linear_layout)
    LinearLayout linearLayout_SmartLight;
    @BindView(R.id.smartlight_fabButton)
    FloatingActionButton smartLightFab_Button;
    @BindView(R.id.powerSocket_fabButton)
    FloatingActionButton powerSocketFab_Button;
    @BindView(R.id.main_fabButton)
    FloatingActionButton mainContainerFab_Button;
    @BindView(R.id.fabGroup)
    View fabGroupView;

    boolean isFabOpen = false;

    PowerSocketBLEService powerSocketBLEService;
    PowerSocketMQTTService powerSocketMQTTService;

    boolean isPowerSocketDeleting;
    private ScheduledExecutorService service;

    private PowerSocket powerSocketToDelete;
    private int transactionId = 3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_devices, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        powerSocketBLEService = PowerSocketBLEService.getInstance(mActivity.getApplicationContext());
        powerSocketMQTTService = PowerSocketMQTTService.getInstance(mActivity.getApplicationContext());

        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlueText,
                R.color.colorBlack);

        isDeviceDelete = false;
        isCalling = true;
        isCurrentFragment = true;
        mActivity.mLeDevicesTemp = new ArrayList<>();
        mActivity.isAddDeviceScan = true;
        mActivity.isFromBridgeConnection = true;
        mStartDeviceScanTimer = new startDeviceScanTimer(29 * 60 * 1000, 1000);
        mStartDeviceStateCheckTimer = new startDeviceStateCheckTimer(29 * 60 * 1000, 1000);
        /*Start device state and brightness check*/
        startDeviceStateCheck();
        /*Get Device list from database*/
        getDBDeviceList(true);

        /*Refresh Database list*/
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isCalling) {
                    isDeviceDelete = false;
                    isCalling = true;
                    getDBDeviceList(false);
                    System.out.println("JD---");
                    if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
                        if (mActivity.isFromLogin) {
                            mActivity.new syncDeviceDataAsyncTask(true).execute("");
                        } else {
                            mActivity.new syncDeviceDataAsyncTask(false).execute("");
                        }
                    }
                }
            }
        });

        /*Add device click handler*/
        mActivity.mImageViewAddDevice.setOnClickListener(view -> {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                if (mActivity.getIsDeviceSupportedAdvertisment()) {
                    if (!mActivity.isPingRequestSent) {
                        mActivity.sendPingRequestToDevice();
                    } else {
                        FragmentAddDevice mFragmentAddDevice = new FragmentAddDevice();
                        mActivity.replacesFragment(mFragmentAddDevice, true, null, 0);
                    }
                } else {
                    FragmentAddDevice mFragmentAddDevice = new FragmentAddDevice();
                    mActivity.replacesFragment(mFragmentAddDevice, true, null, 0);
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        });

        /*on Background device sync complete refresh list*/
        mActivity.setOnSyncCompleteListner(new onSyncComplete() {
            @Override
            public void onDeviceSyncComplete() {
                if (isAdded()) {
                    if (isCurrentFragment) {
                        if (!isCalling) {
                            isCalling = true;
                            getDBDeviceList(false);
                        }
                    }
                }
            }

            @Override
            public void onGroupSyncComplete() {

            }

        });

        /*Scroll Hide/show add button*/
  /*      mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mFloatingActionButtonAdd.getVisibility() == View.VISIBLE) {
                    mFloatingActionButtonAdd.hide();
                } else if (dy < 0 && mFloatingActionButtonAdd.getVisibility() != View.VISIBLE) {
                    mFloatingActionButtonAdd.show();
                }
            }
        });*/
        return mViewRoot;
    }

    onDeviceConnectionStatusChange connectionStatusChangeCallback = new onDeviceConnectionStatusChange() {
        @Override
        public void addScanDevices() {

        }

        @Override
        public void addScanDevices(final VoBluetoothDevices mVoBluetoothDevices) {
            if (isVisible()) {
                console.log("weicwicbuiwebcuwebiwbec", mVoBluetoothDevices.getDeviceHexData());
                if (isDeviceDelete) {
                    mStringDeviceHexData = mVoBluetoothDevices.getDeviceHexData().toLowerCase();
                    if (mStringDeviceHexData != null && !mStringDeviceHexData.equalsIgnoreCase("")) {
                        if (mStringDeviceCommHexID != null && !mStringDeviceCommHexID.equalsIgnoreCase("")) {
                            if (mStringDeviceHexData.substring(20, 24).toLowerCase().equals(mStringDeviceCommHexID.toLowerCase())
                                    && mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ASSOC_REMOVE_RSP)) {
                                isAnyDeviceDeleted = true;
                                console.log("sdcusiudciusdbc", "Deleted Smartlight Device");
                                if (mStartDeviceScanTimer != null) {
                                    mStartDeviceScanTimer.cancel();
                                    mStartDeviceScanTimer.onFinish();
                                }
                            }
                        }
                    }
                } else {
                    try {
                        if (isAdded() && mActivity != null) {
                            mActivity.runOnUiThread(() -> {
                                if (isCheckDeviceState) {
                                    mStringDeviceHexData = mVoBluetoothDevices.getDeviceHexData().toLowerCase();
                                    if (mStringDeviceHexData != null && !mStringDeviceHexData.equalsIgnoreCase("")) {
                                        if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.LIGHT_SYNC_STATE)) {
                                            mStringDeviceHexForState = mStringDeviceHexData.substring(20, 24).toUpperCase();
                                            mStringDeviceTypeForState = mStringDeviceHexData.substring(36, 40).toUpperCase();
                                            mStringDeviceOnOffForState = mStringDeviceHexData.substring(40, 42);

                                            mContentValuesState = new ContentValues();
                                            for (int i = 0; i < mArrayListDevice.size(); i++) {
                                                if (mArrayListDevice.get(i).getDevice_Comm_hexId().toLowerCase().equals(mStringDeviceHexForState.toLowerCase())
                                                        && mArrayListDevice.get(i).getDevice_Type().equals(mStringDeviceTypeForState)) {

                                                    if (mStringDeviceOnOffForState.equals("01")) {
                                                        mContentValuesState.put(DBHelper.mFieldSwitchStatus, "ON");
                                                        mArrayListDevice.get(i).setDevice_SwitchStatus("ON");
                                                        mArrayListDevice.get(i).setIsChecked(true);

                                                    } else {
                                                        mContentValuesState.put(DBHelper.mFieldSwitchStatus, "OFF");
                                                        mArrayListDevice.get(i).setDevice_SwitchStatus("OFF");
                                                        mArrayListDevice.get(i).setIsChecked(false);
                                                    }

                                                    if (mStringDeviceTypeForState.equalsIgnoreCase("0100") || mStringDeviceTypeForState.equalsIgnoreCase("0200") || mStringDeviceTypeForState.equalsIgnoreCase("0600") || mStringDeviceTypeForState.equalsIgnoreCase("0700")) {
                                                        mIntDeviceRGBColorForState = Color.parseColor("#" + mStringDeviceHexData.substring(42, 48));

                                                        DeviceHsvForState = new float[3];
                                                        Color.colorToHSV(mIntDeviceRGBColorForState, DeviceHsvForState);
                                                        mIntDeviceRGBColorForState = Color.HSVToColor(DeviceHsvForState);
                                                        mDeviceBrightnessForState = DeviceHsvForState[2] * 100.0f;

                                                        if (mArrayListDevice.get(i).getDevice_Type().equalsIgnoreCase("0100") || mArrayListDevice.get(i).getDevice_Type().equalsIgnoreCase("0200") || mArrayListDevice.get(i).getDevice_Type().equalsIgnoreCase("0600") || mArrayListDevice.get(i).getDevice_Type().equalsIgnoreCase("0700")) {
                                                            mArrayListDevice.get(i).setDevice_rgb_color(mIntDeviceRGBColorForState);
                                                            mArrayListDevice.get(i).setDevice_brightness((int) mDeviceBrightnessForState);
                                                        }

                                                        mContentValuesState.put(DBHelper.mFieldDeviceBrightness, (int) mDeviceBrightnessForState);
                                                        mContentValuesState.put(DBHelper.mFieldDeviceColor, mIntDeviceRGBColorForState);
                                                    }
                                                    mStateCheckParam = new String[]{mArrayListDevice.get(i).getDevicLocalId()};
                                                    mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValuesState,
                                                            DBHelper.mFieldDeviceLocalId + " = ?", mStateCheckParam);
                                                    if (mDeviceListAdapter != null) {
                                                        mDeviceListAdapter.notifyDataSetChanged();
                                                    }
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                }

                                // Check for devices from Scanning
                                // The Self Id of devices in Database will not be 0
                                for (VoDeviceList device : mArrayListDevice) {
                                    if (device.getDevice_BleAddress().equalsIgnoreCase(mVoBluetoothDevices.getDeviceAddress().replace(":", ""))) {
                                        // Check for Power Socket
                                        String advHexString = mVoBluetoothDevices.getDeviceHexData();
                                        if (advHexString != null && !advHexString.isEmpty()) {
                                            byte[] advBytes = ByteConverter.getByteArrayFromHexString(advHexString);
                                            // Todo - Check for General Discoverable bit from Adv Flag
                                            AdvertisementRecord advRecord = AdvertisementRecord.parse(advBytes);
                                            if (advRecord != null) {
                                                ManufactureData manufactureData = advRecord.getManufactureData();
                                                if (manufactureData != null) {
                                                    int manufacturerID = ByteConverter.convertByteArrayToInt(manufactureData.id);
                                                    if (manufacturerID == 0x3200) {   // Power Socket
                                                        byte[] data = manufactureData.data;
                                                        if (data != null) {
                                                            byte[] selfDeviceIdBytes = ByteConverter.copyOfRange(data, 3, 5);
                                                            int selfDeviceId = ByteConverter.convertByteArrayToInt(selfDeviceIdBytes);
                                                            if (selfDeviceId == 0x00) {
                                                                stopExecutorService();
                                                                powerSocketToDelete = new PowerSocket(device);
                                                                {
                                                                    String spannableText = "The power socket " + device.getDevice_name() + " may have been deleted from another phone. " +
                                                                            "Please add the selected power socket again.";
                                                                    SpannableString spannableString = new SpannableString(spannableText);
                                                                    spannableString.setSpan(new StyleSpan(Typeface.BOLD), 17, 17 + device.getDevice_name().length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);

                                                                    mActivity.mUtility.errorDialogWithCallBack(spannableString, 1, false, new onAlertDialogCallBack() {
                                                                        @Override
                                                                        public void PositiveMethod(DialogInterface dialog, int id) {
                                                                            if (powerSocketMQTTService.isConnected()) {
                                                                                powerSocketMQTTService.unsubscribe(powerSocketToDelete);
                                                                            }

                                                                            if (powerSocketBLEService.isDeviceConnected(powerSocketToDelete.bleAddress)) {
                                                                                powerSocketBLEService.disconnect(powerSocketToDelete.bleAddress);
                                                                            }

                                                                            removePowerSocketDataAndUpdate();
                                                                        }

                                                                        @Override
                                                                        public void NegativeMethod(DialogInterface dialog, int id) {

                                                                        }
                                                                    });
                                                                }
                                                            } else {
                                                                // Todo
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
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
    };

    /*Start device state and brightness check timer*/
    private void startDeviceStateCheck() {
        myTimerDeviceStateTask = new MyTimerDeviceStateTask();
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(myTimerDeviceStateTask, 0, 70000);
    }

    /*device state timer*/
    class MyTimerDeviceStateTask extends TimerTask {
        public void run() {
            if (!isDeviceDelete) {
                if (isFistTimeCheck) {
                    isCheckDeviceState = true;
                    if (mStartDeviceStateCheckTimer != null) {
                        mStartDeviceStateCheckTimer.cancel();
                    }
                    if (mStartDeviceStateCheckTimer != null)
                        mStartDeviceStateCheckTimer.start();
                    isFistTimeCheck = false;
                } else {
                    if (isCheckDeviceState) {
                        isCheckDeviceState = false;
                    } else {
                        isCheckDeviceState = true;
                        if (mStartDeviceStateCheckTimer != null) {
                            mStartDeviceStateCheckTimer.cancel();
                        }
                        if (mStartDeviceStateCheckTimer != null)
                            mStartDeviceStateCheckTimer.start();
                    }
                }
            }
        }
    }

    @OnClick(R.id.fragment_device_button_add_device)
    public void onAddButtonClick(View mView) {
        if (isAdded()) {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                if (mActivity.getIsDeviceSupportedAdvertisment()) {
                    if (!mActivity.isPingRequestSent) {
                        mActivity.sendPingRequestToDevice();
                    } else {
                        FragmentAddDevice mFragmentAddDevice = new FragmentAddDevice();
                        mActivity.replacesFragment(mFragmentAddDevice, true, null, 0);
                    }
                } else {
                    FragmentAddDevice mFragmentAddDevice = new FragmentAddDevice();
                    mActivity.replacesFragment(mFragmentAddDevice, true, null, 0);
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        }
    }

    private void showFabMenu() {
        isFabOpen = true;
        linearLayout_powerSocket.setVisibility(View.VISIBLE);
        linearLayout_SmartLight.setVisibility(View.VISIBLE);
        fabGroupView.setVisibility(View.VISIBLE);
        mainContainerFab_Button.animate().rotationBy(180);
        linearLayout_powerSocket.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        linearLayout_SmartLight.animate().translationY(-getResources().getDimension(R.dimen.standard_100));
    }

    private void closeFabMenu() {
        isFabOpen = false;
        fabGroupView.setVisibility(View.GONE);
        mainContainerFab_Button.animate().rotation(0);
        linearLayout_powerSocket.animate().translationY(0);
        linearLayout_SmartLight.animate().translationY(0);

        linearLayout_SmartLight.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (!isFabOpen) {
                    linearLayout_powerSocket.setVisibility(View.GONE);
                    linearLayout_SmartLight.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }

    @OnClick(R.id.fabGroup)
    public void onViewCLicked() {
        if (isFabOpen) {
            closeFabMenu();
        }
    }

    @OnClick(R.id.main_fabButton)
    public void mainFabButtonClick() {
        if (!isFabOpen) {
            showFabMenu();
        } else {
            closeFabMenu();
        }
    }

    @OnClick(R.id.smartlight_fabButton)
    public void onSmartLightFabButtonClick(View mView) {
        if (isAdded()) {
            isFabOpen = false;
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                if (mActivity.getIsDeviceSupportedAdvertisment()) {
                    if (!mActivity.isPingRequestSent) {
                        mActivity.sendPingRequestToDevice();
                    } else {
                        FragmentAddDevice mFragmentAddDevice = new FragmentAddDevice();
                        mActivity.replacesFragment(mFragmentAddDevice, true, null, 0);
                    }
                } else {
                    FragmentAddDevice mFragmentAddDevice = new FragmentAddDevice();
                    mActivity.replacesFragment(mFragmentAddDevice, true, null, 0);
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        }
    }

    @OnClick(R.id.powerSocket_fabButton)
    public void onPowerSocketFabButtonClicked() {
        if (isAdded()) {
            isFabOpen = false;
            FragmentAddPowerSocket mFragmentAddDevice = new FragmentAddPowerSocket();
            mActivity.replacesFragment(mFragmentAddDevice, true, null, 0);
        }
    }

    /*Fetch device from database*/
    private void getDBDeviceList(boolean isGetDataFromServer) {
        mExpandedPosition = -1;
        previousExpandedPosition = -1;
        DataHolder mDataHolderLight;
        mArrayListDevice = new ArrayList<>();
        ArrayList<String> mStringsArrayName = new ArrayList<>();
        try {
            String url = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceIsActive + "= '1'" + " AND " +
                    DBHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " ORDER BY " +
                    DBHelper.mFieldDeviceIsFavourite + " desc";
            mDataHolderLight = mActivity.mDbHelper.readData(url);

            if (mDataHolderLight != null) {
                VoDeviceList mVoDeviceList;
                for (int i = 0; i < mDataHolderLight.get_Listholder().size(); i++) {
                    mVoDeviceList = new VoDeviceList();
                    mStringsArrayName.add(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceLocalId));
                    mVoDeviceList.setDevicLocalId(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceLocalId));
                    mVoDeviceList.setDeviceServerid(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceServerId));
                    mVoDeviceList.setUser_id(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceUserId));
                    mVoDeviceList.setDevice_Comm_id(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommID));
                    mVoDeviceList.setDevice_Comm_hexId(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommHexId));
                    mVoDeviceList.setDevice_name(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceName));
                    mVoDeviceList.setDevice_realName(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceRealName));
                    mVoDeviceList.setDevice_BleAddress(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceBleAddress).toUpperCase());
                    mVoDeviceList.setDevice_Type(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceType));
                    mVoDeviceList.setDevice_type_name(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceTypeName));
                    mVoDeviceList.setDevice_ConnStatus(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldConnectStatus));
                    mVoDeviceList.setDevice_brightness(Integer.parseInt(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceBrightness)));
                    mVoDeviceList.setDevice_rgb_color(Integer.parseInt(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceColor)));
                    mVoDeviceList.setDevice_SwitchStatus(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldSwitchStatus));
                    mVoDeviceList.setDevice_is_favourite(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsFavourite));
                    mVoDeviceList.setDevice_last_state_remember(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceLastState));
                    mVoDeviceList.setDevice_timestamp(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceTimeStamp));
                    mVoDeviceList.setDevice_is_active(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsActive));
                    mVoDeviceList.setDevice_created_at(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceCreatedAt));
                    mVoDeviceList.setDevice_updated_at(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceUpdatedAt));
                    mVoDeviceList.setDevice_is_sync(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsSync));
                    mVoDeviceList.setIsWifiConfigured(Integer.parseInt(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsWifiConfigured)));
                    mVoDeviceList.setSocketState(Integer.parseInt(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceSocketState)));

                    if (mVoDeviceList.getDevice_SwitchStatus() != null &&
                            mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                        mVoDeviceList.setIsChecked(true);
                    } else {
                        mVoDeviceList.setIsChecked(false);
                    }

                    console.log("xsabxuasbxuibasuix", new Gson().toJson(mVoDeviceList));

                    mArrayListDevice.add(mVoDeviceList);
                }

//                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
//                    @Override
//                    public int compare(TreatmentList s1, TreatmentList s2) {
//                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
//                    }
//                });
            }
        } catch (Exception e) {
            e.printStackTrace();
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
        isCalling = false;
        mSwipeRefreshLayout.setRefreshing(false);

        // Fetch Data from Server (For Login user)
        if (isGetDataFromServer && mActivity.mUtility.haveInternet()) {
            if (!mActivity.mPreferenceHelper.getIsDeviceSync() && !mActivity.mPreferenceHelper.getIsSkipUser()) {
                if (mActivity.isFromLogin) {
                    console.log("asxubauisbxuiabsuix", "From Login");
                    mActivity.new DeviceTypeListAsyncTask(true).execute("");
                } else {
                    console.log("asxubauisbxuiabsuix", "From Sync");
                    mActivity.new syncDeviceDataAsyncTask(false).execute("");
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        unbinder.unbind();
        isCurrentFragment = false;
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.cancel();
        if (mStartDeviceStateCheckTimer != null)
            mStartDeviceStateCheckTimer.cancel();
        if (mTimer != null) {
            mTimer.cancel();
        }
        if (myDialogSetting != null)
            myDialogSetting.dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();
        isCurrentFragment = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isFistTimeCheck = true;
        isCurrentFragment = true;
        isCheckDeviceState = true;
    }

    /*Check device list adapter*/
    private void checkAdapterIsEmpty() {
        try {
            if (mDeviceListAdapter != null) {
                if (mDeviceListAdapter.getItemCount() == 0) {
                    mRelativeLayoutNoDeviceFound.setVisibility(View.VISIBLE);
                    mRecyclerView.setVisibility(View.GONE);
                } else {
                    mRelativeLayoutNoDeviceFound.setVisibility(View.GONE);
                    mRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    GradientDrawable mGradientDrawable;

    /*Device list adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @Override
        public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_device_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public int getItemCount() {
            return (mArrayListDevice == null) ? 0 : mArrayListDevice.size();
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int relativePosition) {
            final ViewHolder itemViewHolder = (ViewHolder) holder;

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
                int imagePath;
                if (mStrDeviceType.equalsIgnoreCase("0100")) {
                    imagePath = R.drawable.ic_default_pic;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.VISIBLE);
                } else if (mStrDeviceType.equalsIgnoreCase("0200")) {
                    imagePath = R.drawable.ic_default_pic;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.VISIBLE);
                } else if (mStrDeviceType.equalsIgnoreCase("0300")) {
                    imagePath = R.drawable.ic_default_switch_icon;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.GONE);
                } else if (mStrDeviceType.equalsIgnoreCase("0400")) {
                    imagePath = R.drawable.ic_default_powerstrip_icon;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.GONE);
                } else if (mStrDeviceType.equalsIgnoreCase("0500")) {
                    imagePath = R.drawable.ic_default_fan_icon;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.VISIBLE);
                } else if (mStrDeviceType.equalsIgnoreCase("0600")) {
                    imagePath = R.drawable.ic_default_striplight_icon;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.VISIBLE);
                } else if (mStrDeviceType.equalsIgnoreCase("0700")) {
                    imagePath = R.drawable.ic_default_lamp_icon;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.VISIBLE);
                } else if (mStrDeviceType.equalsIgnoreCase("0800")) {
                    imagePath = R.drawable.ic_default_socket_icon;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.GONE);
                } else {
                    imagePath = R.drawable.ic_default_pic;
                    itemViewHolder.layoutBrightnessControl.setVisibility(View.VISIBLE);
                }
                Glide.with(mActivity)
                        .load(imagePath)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .crossFade()
                        .placeholder(R.drawable.ic_default_pic)
                        .into(itemViewHolder.mImageViewDevice);
                itemViewHolder.mImageViewDevice.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
            }

            if (mArrayListDevice.get(relativePosition).getIsChecked()) {
                itemViewHolder.mSwitchDevice.setChecked(true);
                if (mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("0100") || mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("0200") || mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("0600") || mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("0700")) {
                    itemViewHolder.mSeekBarBrightness.setVisibility(View.VISIBLE);
                    itemViewHolder.mSeekBarBrightness.setProgress(mActivity.getScanLightBrightness(mArrayListDevice.get(relativePosition).getDevice_brightness()));
                }
            } else {
                itemViewHolder.mSwitchDevice.setChecked(false);
                itemViewHolder.mSeekBarBrightness.setVisibility(View.GONE);
            }

            if (mArrayListDevice.get(relativePosition).getDevice_is_favourite() != null && mArrayListDevice.get(relativePosition).getDevice_is_favourite().equalsIgnoreCase("1")) {
                itemViewHolder.mImageViewFavourite.setImageResource(R.drawable.ic_favorite);
            } else {
                itemViewHolder.mImageViewFavourite.setImageResource(R.drawable.ic_unfavorite);
            }
            final boolean isExpanded = mExpandedPosition == relativePosition;
            itemViewHolder.mLinearLayoutExpanded.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            if (isExpanded) {
                previousExpandedPosition = relativePosition;
            }

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (relativePosition % 2 == 0) {
                        mGradientDrawable = new GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT,
                                new int[]{Color.parseColor("#cc2b5e"), Color.parseColor("#753a88")});
                    } else {
                        mGradientDrawable = new GradientDrawable(
                                GradientDrawable.Orientation.LEFT_RIGHT,
                                new int[]{Color.parseColor("#753a88"), Color.parseColor("#cc2b5e")});
                    }
                    mGradientDrawable.setCornerRadius(getResources().getDimension(R.dimen._5sdp));
                    itemViewHolder.mRelativeLayoutMain.setBackground(mGradientDrawable);
                }
            });

            //Todo
            itemViewHolder.mSwitchDevice.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                if (itemViewHolder.mSwitchDevice.isPressed()) {
                    if (mArrayListDevice != null) {
                        if (relativePosition < mArrayListDevice.size()) {
                            VoDeviceList selectedVoDevice = mArrayListDevice.get(relativePosition);
                            String deviceTypeStr = selectedVoDevice.getDevice_Type();
                            if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
                                int deviceType = Integer.parseInt(deviceTypeStr, 16);

                                switch (deviceType) {
                                    case 0x100:              // Bulb
                                    case 0x200:              // Bulb
                                    case 0x600:              // Strip Light
                                    case 0x700:              // Lamp
                                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                            if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                                if (!mActivity.isPingRequestSent) {
                                                    mActivity.sendPingRequestToDevice();
                                                } else {
                                                    onOffLight(relativePosition, isChecked, itemViewHolder);
                                                }
                                            } else {
                                                onOffLight(relativePosition, isChecked, itemViewHolder);
                                            }
                                        } else {
                                            mActivity.connectDeviceWithProgress();
                                        }
                                        break;

                                    case 0x400:              // Power Socket
                                    case 0x800:              // Socket
                                        //  Use BleCentralManager to control sockets
                                        changeSocketState(relativePosition, isChecked);
                                        break;

                                    case 0x300:              // Switch
                                        break;
                                    case 0x500:              // Fan
                                        break;
                                }
                            }
                        }
                    }
                }
            });

            itemViewHolder.mImageViewSetting.setOnClickListener(v -> {
                if (mArrayListDevice != null) {
                    if (relativePosition < mArrayListDevice.size()) {
                        VoDeviceList selectedVoDevice = mArrayListDevice.get(relativePosition);
                        String deviceTypeStr = selectedVoDevice.getDevice_Type();
                        if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
                            int deviceType = Integer.parseInt(deviceTypeStr, 16);
                            switch (deviceType) {
                                case 0x100:              // Bulb
                                case 0x200:              // Bulb
                                case 0x600:              // Strip Light
                                case 0x700:              // Lamp
                                default:
                                    if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                        showDeviceSettingDialog(relativePosition);
                                    } else {
                                        mActivity.connectDeviceWithProgress();
                                    }
                                    break;

                                case 0x400:              // Power Socket
                                case 0x800:              // Socket
//                                        Bundle mBundle = new Bundle();
//                                        mBundle.putSerializable("SelectedPowerSocket", selectedPowerSocket);
                                    mActivityPowerSocketSelected = new PowerSocket(selectedVoDevice);
                                    ;
                                    FragmentPowerSocketWifiSettings fragmentPowerSocketWifiSettings = new FragmentPowerSocketWifiSettings();
                                    mActivity.replacesFragment(fragmentPowerSocketWifiSettings, true, null, 0);
                                    break;

                                case 0x300:              // Switch
                                    break;
                                case 0x500:              // Fan
                                    break;
                            }
                        }
                    }
                }
            });

            itemViewHolder.itemView.setOnLongClickListener(v -> {
                System.out.println("ON LONG CLICK");
                if (mArrayListDevice != null) {
                    if (relativePosition < mArrayListDevice.size()) {
                        VoDeviceList selectedVoDevice = mArrayListDevice.get(relativePosition);
                        String deviceTypeStr = selectedVoDevice.getDevice_Type();
                        if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
                            int deviceType = Integer.parseInt(deviceTypeStr, 16);
                            switch (deviceType) {
                                case 0x100:              // Bulb
                                case 0x200:              // Bulb
                                case 0x600:              // Strip Light
                                case 0x700:
                                    if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                        if (mArrayListDevice.get(relativePosition).getDevice_Comm_hexId() != null && !mArrayListDevice.get(relativePosition).getDevice_Comm_hexId().equals("")) {
                                            String mStringRandomDeviceIdHexTemp = mArrayListDevice.get(relativePosition).getDevice_Comm_hexId();
                                            if (mStringRandomDeviceIdHexTemp.length() == 3) {
                                                mStringRandomDeviceIdHexTemp = "0" + mStringRandomDeviceIdHexTemp;
                                            }
                                            mStringRandomDeviceIdHexTemp = mStringRandomDeviceIdHexTemp.substring(2) + mStringRandomDeviceIdHexTemp.substring(0, 2);
                                            int mIntRandomDeviceId = BLEUtility.hexToDecimal(mStringRandomDeviceIdHexTemp);
                                            mActivity.setCheckDevice(BLEUtility.intToByte(100), Short.parseShort(mIntRandomDeviceId + ""), BLEUtility.hexStringToBytes(mArrayListDevice.get(relativePosition).getDevice_BleAddress()), true);
                                        }
                                    } else {
                                        mActivity.connectDeviceWithProgress();
                                    }
                                    break;
                            }
                        }
                    }
                }
                return true;
            });

            itemViewHolder.itemView.setOnClickListener(view -> {
                if (mArrayListDevice != null) {
                    if (relativePosition < mArrayListDevice.size()) {
                        VoDeviceList selectedVoDevice = mArrayListDevice.get(relativePosition);
                        String deviceTypeStr = selectedVoDevice.getDevice_Type();
                        if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
                            int deviceType = Integer.parseInt(deviceTypeStr, 16);
                            switch (deviceType) {
                                case 0x100:              // Bulb
                                case 0x200:              // Bulb
                                case 0x600:              // Strip Light
                                case 0x700:              // Lamp
                                default:
//                                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                    if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                        if (!mActivity.isPingRequestSent) {
                                            mActivity.sendPingRequestToDevice();
                                        } else {
                                            openFragment(relativePosition);
                                        }
                                    } else {
                                        openFragment(relativePosition);
                                    }
//                                        } else {
//                                            mActivity.connectDeviceWithProgress();
//                                        }
                                    break;

                                case 0x400:              // Power Socket
                                case 0x800:              // Socket
                                   /*  Bundle mBundle = new Bundle();
                                    mBundle.putSerializable("SelectedPowerSocket", selectedPowerSocket);
                                    mActivity.replacesFragment(fragmentPowerSocketControl, true, mBundle, 0);*/

                                    /**
                                     * Assigning PowerSocket object to the mainActivity variable.
                                     * not using bundle.
                                     */
                                    mActivityPowerSocketSelected = new PowerSocket(selectedVoDevice);
                                    console.log("scbiusbcuisbcui", new Gson().toJson(mActivityPowerSocketSelected));
                                    FragmentPowerSocketHome fragmentPowerSocketHome = new FragmentPowerSocketHome();
                                    mActivity.replacesFragment(fragmentPowerSocketHome, true, null, 0);
                                    break;
                                case 0x300:              // Switch
                                    break;
                                case 0x500:              // Fan
                                    break;
                            }
                        }
                    }
                }
            });

            itemViewHolder.mImageViewMore.setOnClickListener(v -> {
                if (mArrayListDevice != null) {
                    if (relativePosition < mArrayListDevice.size()) {
                        mExpandedPosition = isExpanded ? -1 : relativePosition;
                        notifyItemChanged(previousExpandedPosition);
                        notifyItemChanged(relativePosition);
                    }
                }
            });

            itemViewHolder.mImageViewFavourite.setOnClickListener(view -> {
                if (mArrayListDevice != null) {
                    if (relativePosition < mArrayListDevice.size()) {
                        ContentValues mContentValues = new ContentValues();
                        if (mArrayListDevice.get(relativePosition).getDevice_is_favourite() != null &&
                                mArrayListDevice.get(relativePosition).getDevice_is_favourite().equalsIgnoreCase("1")) {
                            mContentValues.put(DBHelper.mFieldDeviceIsFavourite, "0");
                            mArrayListDevice.get(relativePosition).setDevice_is_favourite("0");
                        } else {
                            mContentValues.put(DBHelper.mFieldDeviceIsFavourite, "1");
                            mArrayListDevice.get(relativePosition).setDevice_is_favourite("1");
                        }

                        mArrayListDevice.get(relativePosition).setDevice_is_sync("0");
                        mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");
                        String[] mArray = new String[]{mArrayListDevice.get(relativePosition).getDevicLocalId()};
                        mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues, DBHelper.mFieldDeviceLocalId
                                + "=?", mArray);

                        if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                            if (mActivity.mUtility.haveInternet()) {
                                mActivity.updateDeviceAPI(mArrayListDevice.get(relativePosition));
                            }
                        }
                        isCalling = true;
                        getDBDeviceList(false);
                    }
                }
            });

            itemViewHolder.mImageViewEdit.setOnClickListener((View.OnClickListener) view -> {
                if (mArrayListDevice != null) {
                    if (relativePosition < mArrayListDevice.size()) {

                        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
                        View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
                        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                        alertDialogBuilderUserInput.setView(mView);
                        final EditText mEditTextDeviceName = (EditText) mView.findViewById(R.id.user_input_dialog_edittext_device_name);
                        if (mArrayListDevice.get(relativePosition).getDevice_name() != null && !mArrayListDevice.get(relativePosition).getDevice_name().equalsIgnoreCase("")) {
                            mEditTextDeviceName.setText(mArrayListDevice.get(relativePosition).getDevice_name());
                        }

                        alertDialogBuilderUserInput
                                .setCancelable(true)
                                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialogBox, int id) {
                                        // ToDo get user input here
                                        if (mArrayListDevice != null) {
                                            if (relativePosition < mArrayListDevice.size()) {
                                                String mStringDeviceName = mEditTextDeviceName.getText().toString().trim();
                                                if (mStringDeviceName.equalsIgnoreCase("")) {
                                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_enter_device_name), 3, false);
                                                    return;
                                                }
                                                ContentValues mContentValues = new ContentValues();
                                                mContentValues.put(DBHelper.mFieldDeviceName, mStringDeviceName);
                                                mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");
                                                String[] mArray = new String[]{mArrayListDevice.get(relativePosition).getDevicLocalId()};
                                                mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                                                        DBHelper.mFieldDeviceLocalId + "=?", mArray);
                                                itemViewHolder.mTextViewDeviceName.setText(mStringDeviceName);
                                                mArrayListDevice.get(relativePosition).setDevice_name(mStringDeviceName);
                                                mArrayListDevice.get(relativePosition).setDevice_is_sync("0");
                                                mActivity.mUtility.hideKeyboard(mActivity);
                                                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                im.hideSoftInputFromWindow(mEditTextDeviceName.getWindowToken(), 0);
                                                if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                                    if (mActivity.mUtility.haveInternet()) {
                                                        mActivity.updateDeviceAPI(mArrayListDevice.get(relativePosition));
                                                    }
                                                }
                                            }
                                        }
                                    }
                                })
                                .setNegativeButton("Cancel",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialogBox, int id) {
                                                mActivity.mUtility.hideKeyboard(mActivity);
                                                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                im.hideSoftInputFromWindow(mEditTextDeviceName.getWindowToken(), 0);
                                                dialogBox.cancel();
                                            }
                                        });

                        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
                        alertDialogAndroid.show();
                    }
                }
            });

            itemViewHolder.mImageViewDelete.setOnClickListener(view -> {
                mActivity.isAddDeviceScan = true;
                mActivity.mLeDevicesTemp = new ArrayList<>();
                mActivity.RescanDevice(false);
                if (mArrayListDevice != null) {
                    if (relativePosition < mArrayListDevice.size()) {
                        VoDeviceList selectedVoDevice = mArrayListDevice.get(relativePosition);
                        String deviceTypeStr = selectedVoDevice.getDevice_Type();
                        if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
                            int deviceType = Integer.parseInt(deviceTypeStr, 16);
                            switch (deviceType) {
                                case 0x100:              // Bulb
                                case 0x200:              // Bulb
                                case 0x600:              // Strip Light
                                case 0x700:              // Lamp
                                    if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                        mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.frg_device_delete_device), getResources().getString(R.string.frg_device_delete_confirmation), "Yes", "No", true, 2, new onAlertDialogCallBack() {
                                            @Override
                                            public void PositiveMethod(DialogInterface dialog, int id) {
                                                if (mArrayListDevice != null) {
                                                    if (relativePosition < mArrayListDevice.size()) {
                                                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                                            mActivity.runOnUiThread(new Runnable() {
                                                                @Override
                                                                public void run() {
                                                                    if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                                                        if (mActivity.mBluetoothAdapter != null && BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                                                                            if (!mActivity.isPingRequestSent) {
                                                                                mActivity.sendPingRequestToDevice();
                                                                            } else {
                                                                                if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                                                                    if (mActivity.mUtility.haveInternet()) {
                                                                                        checkAuthenticationAPI(true, relativePosition);
                                                                                    } else {
                                                                                        mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_device_not_delete_without_internet), 1, true);
                                                                                    }
                                                                                } else {
                                                                                    deleteSmartLightDeviceRequest(relativePosition);
                                                                                }
                                                                            }
                                                                        } else {
                                                                            if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                                                                if (mActivity.mUtility.haveInternet()) {
                                                                                    checkAuthenticationAPI(true, relativePosition);
                                                                                } else {
                                                                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_device_not_delete_without_internet), 1, true);
                                                                                }
                                                                            } else {
                                                                                deleteSmartLightDeviceRequest(relativePosition);
                                                                            }
                                                                        }
                                                                    } else {
                                                                        if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                                                            if (mActivity.mUtility.haveInternet()) {
                                                                                checkAuthenticationAPI(true, relativePosition);
                                                                            } else {
                                                                                mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_device_not_delete_without_internet), 1, true);
                                                                            }
                                                                        } else {
                                                                            deleteSmartLightDeviceRequest(relativePosition);
                                                                        }
                                                                    }
                                                                }
                                                            });
                                                        } else {
                                                            mActivity.connectDeviceWithProgress();
                                                        }
                                                    }
                                                }
                                            }

                                            @Override
                                            public void NegativeMethod(DialogInterface dialog, int id) {

                                            }
                                        });
                                    } else {
                                        mActivity.connectDeviceWithProgress();
                                    }
                                    break;
                                case 0x400:              // Power Socket
                                case 0x800:              // Socket
                                    // Use BleCentralManager to delete Power socket device
                                    String dialogTitle = getResources().getString(R.string.frg_device_delete_device);
                                    String dialogMessage = getResources().getString(R.string.frg_device_delete_confirmation);
                                    mActivity.mUtility.errorDialogWithYesNoCallBack(dialogTitle, dialogMessage, "Yes", "No",
                                            true, 2, new onAlertDialogCallBack() {
                                                @Override
                                                public void PositiveMethod(DialogInterface dialog, int id) {
                                                    if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                                        if (mActivity.mUtility.haveInternet()) {
                                                            checkAuthenticationAPI(true, relativePosition);
                                                        } else {
                                                            mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_device_not_delete_without_internet), 1, true);
                                                        }
                                                    } else {
                                                        mActivity.showProgress("Deleting power socket. Please wait...", true);
                                                        deletePowerSocket(selectedVoDevice);
                                                    }
                                                }

                                                @Override
                                                public void NegativeMethod(DialogInterface dialog, int id) {

                                                }
                                            });
                                    break;
                                case 0x300:              // Switch
                                    break;
                                case 0x500:              // Fan
                                    break;
                            }
                        }
                    }
                }
            });

            itemViewHolder.mSeekBarBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
                        try {
                            if (mArrayListDevice != null) {
                                if (relativePosition < mArrayListDevice.size()) {
                                    mIntSeekSelectedValue = progress;
                                    mIntSeekChangeValue = mActivity.getLightBrightness(mIntSeekSelectedValue);
                                    if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                        if (!mABooleanColorChange) {
                                            mABooleanColorChange = true;
                                            mActivity.setLightColor(BLEUtility.intToByte(100), mArrayListDevice.get(relativePosition).getDevice_rgb_color(), mIntSeekChangeValue, Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false);
                                            ContentValues mContentValues = new ContentValues();
                                            mContentValues.put(DBHelper.mFieldDeviceBrightness, mIntSeekChangeValue);
                                            mArrayBrightness = new String[]{mArrayListDevice.get(relativePosition).getDevicLocalId()};
                                            mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                                                    DBHelper.mFieldDeviceLocalId + "=?", mArrayBrightness);
                                            Timer innerTimer = new Timer();
                                            innerTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    mABooleanColorChange = false;
                                                }
                                            }, 400);
                                        }

                                    } else {
                                        mActivity.connectDeviceWithProgress();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    try {
                        mIntSeekSelectedValue = seekBar.getProgress();
                        mIntSeekChangeValue = getLightBrightness(mIntSeekSelectedValue);
                        System.out.println("mIntSeekSelectedValue=" + mIntSeekSelectedValue);
                        System.out.println("mIntSeekChangeValue=" + mIntSeekChangeValue);
                        mABooleanColorChange = false;
                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                            mActivity.setLightColor(BLEUtility.intToByte(100), mArrayListDevice.get(relativePosition).getDevice_rgb_color(), mIntSeekChangeValue, Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false);
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(DBHelper.mFieldDeviceBrightness, mIntSeekChangeValue);
                            mArrayBrightness = new String[]{mArrayListDevice.get(relativePosition).getDevicLocalId()};
                            mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                                    DBHelper.mFieldDeviceLocalId + "=?", mArrayBrightness);
                        } else {
                            mActivity.connectDeviceWithProgress();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        // ItemViewHolder Class for Items in each Section
        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_device_list_item_imageview_device)
            ImageView mImageViewDevice;
            @BindView(R.id.raw_device_list_item_imageview_favourite)
            ImageView mImageViewFavourite;
            @BindView(R.id.raw_device_list_item_imageview_edit)
            ImageView mImageViewEdit;
            @BindView(R.id.raw_device_list_item_imageview_delete)
            ImageView mImageViewDelete;
            @BindView(R.id.raw_device_list_item_imageview_more)
            ImageView mImageViewMore;
            @BindView(R.id.raw_device_list_item_iv_setting)
            ImageView mImageViewSetting;
            @BindView(R.id.raw_device_list_item_textview_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_device_list_item_textview_device_id)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_device_list_item_switch_device)
            SwitchCompat mSwitchDevice;
            @BindView(R.id.raw_device_list_item_textview_last_state)
            TextView mTextViewLastState;
            @BindView(R.id.raw_device_list_item_seek_bar_brightness)
            AppCompatSeekBar mSeekBarBrightness;
            @BindView(R.id.raw_device_list_item_linearlayout_expanded)
            LinearLayout mLinearLayoutExpanded;
            @BindView(R.id.raw_device_list_item_relativelayout_main)
            RelativeLayout mRelativeLayoutMain;
            @BindView(R.id.layoutBrightnessControl)
            RelativeLayout layoutBrightnessControl;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*get Light brightness*/
    public int getLightBrightness(int valueBrightness) {
        // (100-Min/100)*value+Min
        return (int) ((0.8 * valueBrightness) + 20);
//        return (int) ((0.8 * valueBrightness));    // Remove offset (20)
    }

    /*get scan Light brightness*/
    public int getScanLightBrightness(int valueBrightness) {
        // (value-min)/(100-Min/100)
        return (int) ((valueBrightness - 20) / 0.8);
    }

    /*Send delete device request to ble device*/
    private void deleteSmartLightDeviceRequest(final int relativePosition) {
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            if (mActivity.getIsDeviceSupportedAdvertisment()) {
                if (mActivity.mBluetoothAdapter != null && BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                    if (!mActivity.isPingRequestSent) {
                        mActivity.sendPingRequestToDevice();
                    } else {
                        isDeviceDelete = true;
                        intDeleteDevicePosition = relativePosition;
                        mStringDeviceCommHexID = mArrayListDevice.get(relativePosition).getDevice_Comm_hexId();
                        mStringDeviceBleAddress = mArrayListDevice.get(relativePosition).getDevice_BleAddress();
                        mActivity.showProgress("Deleting Device..", true);
                        mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false, false);
                        Timer mTimer = new Timer();
                        mTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false, false);
//                                        checkScanDeviceList();
                                if (mStartDeviceScanTimer != null)
                                    mStartDeviceScanTimer.start();
                            }
                        }, 350);

                    }
                } else {
                    isDeviceDelete = true;
                    intDeleteDevicePosition = relativePosition;
                    mStringDeviceCommHexID = mArrayListDevice.get(relativePosition).getDevice_Comm_hexId();
                    mStringDeviceBleAddress = mArrayListDevice.get(relativePosition).getDevice_BleAddress();
                    mActivity.showProgress("Deleting Device..", true);
                    mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false, false);

                    Timer mTimer = new Timer();
                    mTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false, false);
//                                    checkScanDeviceList();
                            if (mStartDeviceScanTimer != null)
                                mStartDeviceScanTimer.start();
                        }
                    }, 350);
                }
            } else {
                isDeviceDelete = true;
                intDeleteDevicePosition = relativePosition;
                mStringDeviceCommHexID = mArrayListDevice.get(relativePosition).getDevice_Comm_hexId();
                mStringDeviceBleAddress = mArrayListDevice.get(relativePosition).getDevice_BleAddress();
                mActivity.showProgress("Deleting Device..", true);
                mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false, false);

                Timer mTimer = new Timer();
                mTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        mActivity.resetAllDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListDevice.get(relativePosition).getDevice_Comm_id()), false, false);
//                                checkScanDeviceList();
                        if (mStartDeviceScanTimer != null)
                            mStartDeviceScanTimer.start();
                    }
                }, 350);
            }
        } else {
            mActivity.connectDeviceWithProgress();
        }
    }

    // Smart light/ strip Control
    private void onOffLight(int relativePosition, boolean isChecked, DeviceListAdapter.ViewHolder itemViewHolder) {
        ContentValues mContentValues = new ContentValues();
        if (isChecked) {
            mContentValues.put(DBHelper.mFieldSwitchStatus, "ON");
            mArrayListDevice.get(relativePosition).setIsChecked(true);
            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(Integer.parseInt(mArrayListDevice.get(relativePosition).getDevice_Comm_id()) + ""), false);
            itemViewHolder.mSeekBarBrightness.setVisibility(View.VISIBLE);
        } else {
            mContentValues.put(DBHelper.mFieldSwitchStatus, "OFF");
            mArrayListDevice.get(relativePosition).setIsChecked(false);
            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(Integer.parseInt(mArrayListDevice.get(relativePosition).getDevice_Comm_id()) + ""), false);
            itemViewHolder.mSeekBarBrightness.setVisibility(View.GONE);
        }
        String[] mArray = new String[]{mArrayListDevice.get(relativePosition).getDevicLocalId()};
        mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues, DBHelper.mFieldDeviceLocalId + "=?", mArray);
        System.out.println("Device updated In Local Db");
    }

    // Todo Refine code
    // Power Socket Control
    private void changeSocketState(int relativePosition, boolean isChecked) {
        ContentValues mContentValues = new ContentValues();
        byte socketSwitchState;
        VoDeviceList voDeviceList = mArrayListDevice.get(relativePosition);
        PowerSocket selectedPowerSocket = new PowerSocket(voDeviceList);
        if (isChecked) {
            socketSwitchState = 0x01;     // Device On
            mContentValues.put(DBHelper.mFieldSwitchStatus, "ON");
            mContentValues.put(DBHelper.mFieldDeviceSocketState, 0x3F);   // 00111111 -> All 6 socket switches are turned OM
            mArrayListDevice.get(relativePosition).setIsChecked(true);
        } else {
            socketSwitchState = 0x00;     // Device Off
            mContentValues.put(DBHelper.mFieldSwitchStatus, "OFF");
            mContentValues.put(DBHelper.mFieldDeviceSocketState, 0x00);  // 00000000 -> All 6 socket switches are turned OFF
            mArrayListDevice.get(relativePosition).setIsChecked(false);
        }

        String[] mArray = new String[]{mArrayListDevice.get(relativePosition).getDevicLocalId()};
        mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues, DBHelper.mFieldDeviceLocalId + "=?", mArray);
        System.out.println("Device updated In Local Db");

        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        int mqttState = 0;                     // Connected
        int bluetoothState = 0;                // Connected
        // Check if Bluetooth is Enabled
        if (adapter.isEnabled()) {
            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.getBleAddress())) {
                powerSocketBLEService.controlAllSwitches(selectedPowerSocket, socketSwitchState, transactionId);
            } else {
                bluetoothState = 1;           // BLE device not connected
            }
        } else {
            bluetoothState = 2;               // Bluetooth Adapter is disabled
        }

        if (bluetoothState == 1 || bluetoothState == 2) {
            // Check is WiFi is enabled
            if (selectedPowerSocket.isWifiConfigured) {
                if (powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.controlAllSwitches(selectedPowerSocket, socketSwitchState, transactionId);
                } else {
                    mqttState = 1;      // MQTT disconnected
                    displayConnectionErrorMessage(mqttState, bluetoothState);
                }
            } else {
                mqttState = 3;              // Wi-Fi not configured
                displayConnectionErrorMessage(mqttState, bluetoothState);
            }
        }
    }

    private void displayConnectionErrorMessage(int mqttState, int bluetoothState) {
        String errorMessage = "";
        switch (mqttState) {
            case 1:
                if (bluetoothState == 1) {
                    errorMessage = "Please check if the added power socket is switched ON or connected to a Wi-Fi network.";
                } else if (bluetoothState == 2) {
                    errorMessage = "Please enable Bluetooth in your phone or check if the added power socket is connected to a Wi-Fi network and try again.";
                }
                break;
            case 2:
                if (bluetoothState == 1) {
                    errorMessage = "Please make sure that the power socket is turned ON or enable Wi-Fi in you phone and try again.";
                } else if (bluetoothState == 2) {
                    errorMessage = "Please enable Bluetooth/Wi-Fi in your phone to control the power socket.";
                }
                break;
            case 3:
                if (bluetoothState == 1) {
                    errorMessage = "Please make sure that the power socket is turned ON or configure power socket over Wi-Fi network.";
                } else if (bluetoothState == 2) {
                    errorMessage = "Please enable Bluetooth or configure the power socket over Wi-Fi network.";
                }
                break;
        }

        Snackbar.make(coordinatorLayout, errorMessage, 5000)
                .setAction("OK", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                })
                .setText(errorMessage)
                .setActionTextColor(getResources().getColor(android.R.color.white))
                .show();
    }

    /*Show device setting alert dialog*/
    private void showDeviceSettingDialog(final int relativePosition) {
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
        if (mArrayListDevice.get(relativePosition).getDevice_last_state_remember() != null && mArrayListDevice.get(relativePosition).getDevice_last_state_remember().equalsIgnoreCase("0")) {
            ((RadioButton) mRadioGroupState.getChildAt(0)).setChecked(true);
        } else if (mArrayListDevice.get(relativePosition).getDevice_last_state_remember() != null && mArrayListDevice.get(relativePosition).getDevice_last_state_remember().equalsIgnoreCase("1")) {
            ((RadioButton) mRadioGroupState.getChildAt(1)).setChecked(true);
        } else if (mArrayListDevice.get(relativePosition).getDevice_last_state_remember() != null && mArrayListDevice.get(relativePosition).getDevice_last_state_remember().equalsIgnoreCase("2")) {
            ((RadioButton) mRadioGroupState.getChildAt(2)).setChecked(true);
        } else if (mArrayListDevice.get(relativePosition).getDevice_last_state_remember() != null && mArrayListDevice.get(relativePosition).getDevice_last_state_remember().equalsIgnoreCase("3")) {
            ((RadioButton) mRadioGroupState.getChildAt(3)).setChecked(true);
        } else {
            ((RadioButton) mRadioGroupState.getChildAt(0)).setChecked(true);
        }
        mButtonSend.setOnClickListener(v -> {
            // TODO Auto-generated method stub
            mActivity.mUtility.hideKeyboard(mActivity);
            myDialogSetting.dismiss();
            if (mArrayListDevice != null) {
                if (relativePosition < mArrayListDevice.size()) {
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
                            mActivity.setLightLastState(BLEUtility.intToByte(100), BLEUtility.intToByte(selectedState), Short.parseShort(Integer.parseInt(mArrayListDevice.get(relativePosition).getDevice_Comm_id()) + ""), false);
                            mArrayListDevice.get(relativePosition).setDevice_last_state_remember(selectedState + "");
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(DBHelper.mFieldDeviceLastState, selectedState + "");
                            mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");
                            mArrayListDevice.get(relativePosition).setDevice_is_sync("0");
                            mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                                    DBHelper.mFieldDeviceLocalId + "=?",
                                    new String[]{mArrayListDevice.get(relativePosition).getDevicLocalId()});
                            if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                if (mActivity.mUtility.haveInternet()) {
                                    mActivity.updateDeviceAPI(mArrayListDevice.get(relativePosition));
                                }
                            }
                            isCalling = true;
                            getDBDeviceList(false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        mActivity.connectDeviceWithProgress();
                    }
                }
            }

        });
        mButtonCancel.setOnClickListener(v -> {
            mActivity.mUtility.hideKeyboard(mActivity);
            myDialogSetting.dismiss();
        });
        myDialogSetting.show();
        Window window = myDialogSetting.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    /*Open Color Fragment on Device list click*/
    private void openFragment(int relativePosition) {
//        FragmentPowerStrip mFragmentPowerStrip = new FragmentPowerStrip();
//        Bundle mBundle = new Bundle();
//        mBundle.putBoolean("intent_is_from_group", false);
//        mBundle.putBoolean("intent_is_from_all_group", false);
//        mBundle.putSerializable("intent_vodevice", mArrayListDevice.get(relativePosition));
//        mActivity.replacesFragment(mFragmentPowerStrip, true, mBundle, 0);

        if (mArrayListDevice.get(relativePosition).getDevice_Type() != null && mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("null") && mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("") && mArrayListDevice.get(relativePosition).getDevice_Type().equalsIgnoreCase("0200")) {
//            FragmentColorWhiteLight mFragmentColorWhiteLight = new FragmentColorWhiteLight();
//            Bundle mBundle = new Bundle();
//            mBundle.putBoolean("intent_is_from_group", false);
//            mBundle.putBoolean("intent_is_from_all_group", false);
//            mBundle.putBoolean("intent_is_turn_on", mArrayListDevice.get(relativePosition).getIsChecked());
//            mBundle.putString("intent_group_name", mArrayListDevice.get(relativePosition).getDevice_name());
//            mBundle.putString("intent_comm_id", mArrayListDevice.get(relativePosition).getDevice_Comm_id());
//            mBundle.putString("intent_local_id", mArrayListDevice.get(relativePosition).getDevicLocalId());
//            mBundle.putString("intent_server_id", mArrayListDevice.get(relativePosition).getDeviceServerId());
//            mActivity.replacesFragment(mFragmentColorWhiteLight, true, mBundle, 0);
        } else {
            FragmentDeviceSetColor mFragmentDeviceSetColor = new FragmentDeviceSetColor();
            Bundle mBundle = new Bundle();
            mBundle.putBoolean("intent_is_from_group", false);
            mBundle.putBoolean("intent_is_from_all_group", false);
            mBundle.putBoolean("intent_is_turn_on", mArrayListDevice.get(relativePosition).getIsChecked());
            mBundle.putInt("intent_brightness", mArrayListDevice.get(relativePosition).getDevice_brightness());
            mBundle.putString("intent_group_name", mArrayListDevice.get(relativePosition).getDevice_name());
            mBundle.putString("intent_comm_id", mArrayListDevice.get(relativePosition).getDevice_Comm_id());
//            mBundle.putString("intent_comm_id", "5666");
            mBundle.putString("intent_local_id", mArrayListDevice.get(relativePosition).getDevicLocalId());
            mBundle.putString("intent_server_id", mArrayListDevice.get(relativePosition).getDeviceServerId());
            mBundle.putString("intent_ble_address", mArrayListDevice.get(relativePosition).getDevice_BleAddress());
            mBundle.putInt("intent_device_color", mArrayListDevice.get(relativePosition).getDevice_rgb_color());
            mActivity.replacesFragment(mFragmentDeviceSetColor, true, mBundle, 0);
        }
    }


    private class startDeviceStateCheckTimer extends CountDownTimer {

        public startDeviceStateCheckTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            if (mDeviceListAdapter != null) {
                mDeviceListAdapter.notifyDataSetChanged();
            }
            isCheckDeviceState = false;
        }
    }

    /*Device ble scan acknowledgement response check timer*/
    private class startDeviceScanTimer extends CountDownTimer {

        public startDeviceScanTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);

        }

        @Override
        public void onTick(long millisUntilFinished) {
            isAnyDeviceDeleted = false;
            isDeviceDelete = true;
            isCheckDeviceState = false;
        }

        @Override
        public void onFinish() {
            removeSmartLightDataAndUpdate();
        }
    }

    /*ble scan acknowledgement response check*/
    private void removeSmartLightDataAndUpdate() {
        mActivity.hideProgress();
//        if (isAnyDeviceDeleted) {
        if (mArrayListDevice != null) {
            if (intDeleteDevicePosition < mArrayListDevice.size()) {
                ContentValues mContentValuesGD = new ContentValues();
                mContentValuesGD.put(DBHelper.mFieldGDListStatus, "0");
                mActivity.mDbHelper.updateRecord(DBHelper.mTableGroupDeviceList, mContentValuesGD, DBHelper.mFieldGDListLocalDeviceID + "=?",
                        new String[]{mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId()});
                DataHolder mDataHolderDltGroup;
                try {
                    String deleteGroupDevice = "select * from " + DBHelper.mTableGroup + " INNER JOIN " + DBHelper.mTableGroupDeviceList + " on "
                            + DBHelper.mFieldGDListLocalGroupID + " =" + DBHelper.mFieldGroupLocalID + " AND " + DBHelper.mFieldGDListUserID + "= " +
                            DBHelper.mTableGroup + "." + DBHelper.mFieldGroupUserId + " AND " + DBHelper.mFieldGroupIsActive + "= 1" + " INNER JOIN "
                            + DBHelper.mTableDevice + " on " + DBHelper.mFieldDeviceLocalId + " =" + DBHelper.mFieldGDListLocalDeviceID + " AND " +
                            DBHelper.mTableDevice + "." + DBHelper.mFieldDeviceUserId + "= " + DBHelper.mFieldGDListUserID + " where " +
                            DBHelper.mFieldDeviceLocalId + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'" +
                            " AND " + DBHelper.mTableDevice + "." + DBHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";

                    mDataHolderDltGroup = mActivity.mDbHelper.readData(deleteGroupDevice);
                    if (mDataHolderDltGroup != null) {
                        String mStringGroupServerId;
                        int intGroupDeviceCount;
                        int intGroupInactiveDeviceCount;
                        for (int j = 0; j < mDataHolderDltGroup.get_Listholder().size(); j++) {
                            mStringGroupServerId = mDataHolderDltGroup.get_Listholder().get(j).get(DBHelper.mFieldGroupServerID);
                            ContentValues mContentValuesGroup = new ContentValues();

                            intGroupDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " +
                                    DBHelper.mTableGroupDeviceList + " where " + DBHelper.mFieldGDListUserID + "= '"
                                    + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + DBHelper.mFieldGDListLocalGroupID + "= '" +
                                    mDataHolderDltGroup.get_Listholder().get(j).get(DBHelper.mFieldGroupLocalID) + "'" + ") as count");

                            intGroupInactiveDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " +
                                    DBHelper.mTableGroupDeviceList + " where " + DBHelper.mFieldGDListStatus + "= 0" + " AND " +
                                    DBHelper.mFieldGDListUserID + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " +
                                    DBHelper.mFieldGDListLocalGroupID + "= '" +
                                    mDataHolderDltGroup.get_Listholder().get(j).get(DBHelper.mFieldGroupLocalID) + "'" + ") as count");

                            if (intGroupDeviceCount == intGroupInactiveDeviceCount) {
                                mContentValuesGroup.put(DBHelper.mFieldGroupIsActive, "0");
                                if (mStringGroupServerId != null && !mStringGroupServerId.equalsIgnoreCase("")
                                        && !mStringGroupServerId.equalsIgnoreCase("null")) {
                                    mContentValuesGroup.put(DBHelper.mFieldGroupIsSync, "0");
                                    mActivity.mDbHelper.updateRecord(DBHelper.mTableGroup, mContentValuesGroup, DBHelper.mFieldGroupLocalID
                                            + "=?", new String[]{mDataHolderDltGroup.get_Listholder().get(j).get(DBHelper.mFieldGroupLocalID)});
                                } else {
                                    mActivity.mDbHelper.exeQuery("delete from " + DBHelper.mTableGroup + " where " + DBHelper.mFieldGroupLocalID
                                            + "= '" + mDataHolderDltGroup.get_Listholder().get(j).get(DBHelper.mFieldGroupLocalID) + "'");
                                }
                            } else {
                                mContentValuesGroup.put(DBHelper.mFieldGroupIsSync, "0");
                                mActivity.mDbHelper.updateRecord(DBHelper.mTableGroup, mContentValuesGroup, DBHelper.mFieldGroupLocalID + "=?",
                                        new String[]{mDataHolderDltGroup.get_Listholder().get(j).get(DBHelper.mFieldGroupLocalID)});
                            }
                        }
                    }
                } catch (Exception e) {
                    isDeviceDelete = false;
                    e.printStackTrace();
                }
                DataHolder mDataHolderAlarm;
                try {
//                    String deleteAlarmDevice = "select * from " + mActivity.mDbHelper.mTableAlarm + " INNER JOIN " + mActivity.mDbHelper.mTableAlarmDeviceList + " on " + mActivity.mDbHelper.mFieldADAlarmLocalID + " =" + mActivity.mDbHelper.mFieldAlarmLocalID + " AND " + mActivity.mDbHelper.mFieldADUserId + "= " + mActivity.mDbHelper.mTableAlarm + "." + mActivity.mDbHelper.mFieldAlarmUserId + " AND " + mActivity.mDbHelper.mFieldAlarmIsActive + "= 1" + " INNER JOIN " + mActivity.mDbHelper.mTableDevice + " on " + mActivity.mDbHelper.mFieldDeviceLocalId + " =" + mActivity.mDbHelper.mFieldADDeviceLocalID + " AND " + mActivity.mDbHelper.mTableAlarm + "." + mActivity.mDbHelper.mFieldAlarmUserId + "= " + mActivity.mDbHelper.mFieldADUserId + " where " + mActivity.mDbHelper.mFieldDeviceLocalId + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'" + " AND " + mActivity.mDbHelper.mTableAlarm + "." + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                    String deleteAlarmDevice = "select * from " + DBHelper.mTableAlarm + " INNER JOIN " + DBHelper.mTableAlarmDeviceList + " on "
                            + DBHelper.mFieldADAlarmLocalID + " =" + DBHelper.mFieldAlarmLocalID + " AND " + DBHelper.mFieldADUserId + "= "
                            + DBHelper.mTableAlarm + "." + DBHelper.mFieldAlarmUserId + " AND " + DBHelper.mFieldAlarmIsActive + "= 1" +
                            " INNER JOIN " + DBHelper.mTableDevice + " on " + DBHelper.mFieldDeviceLocalId + " =" + DBHelper.mFieldADDeviceLocalID +
                            " AND " + DBHelper.mTableDevice + "." + DBHelper.mFieldDeviceUserId + "= " + DBHelper.mFieldADUserId +
                            " where " + DBHelper.mFieldDeviceLocalId + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'" +
                            " AND " + DBHelper.mTableDevice + "." + DBHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";

                    mDataHolderAlarm = mActivity.mDbHelper.readData(deleteAlarmDevice);
                    if (mDataHolderAlarm != null) {
                        int intAlarmDeviceCount;
                        int intAlarmInactiveDeviceCount;
                        for (int j = 0; j < mDataHolderAlarm.get_Listholder().size(); j++) {
                            ContentValues mContentValuesAlarm = new ContentValues();
                            intAlarmDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " +
                                    DBHelper.mTableAlarmDeviceList + " where " + DBHelper.mFieldADUserId + "= '"
                                    + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + DBHelper.mFieldADAlarmLocalID + "= '" +
                                    mDataHolderAlarm.get_Listholder().get(j).get(DBHelper.mFieldAlarmLocalID) + "'" + ") as count");

                            intAlarmInactiveDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from "
                                    + DBHelper.mTableAlarmDeviceList + " where " + DBHelper.mFieldADDeviceStatus + "= 1" + " AND "
                                    + DBHelper.mFieldADDeviceLocalID + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'" +
                                    " AND " + DBHelper.mFieldADUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " +
                                    DBHelper.mFieldADAlarmLocalID + "= '" + mDataHolderAlarm.get_Listholder().get(j).get(DBHelper.mFieldAlarmLocalID)
                                    + "'" + ") as count");

                            if (intAlarmDeviceCount == intAlarmInactiveDeviceCount) {
                                mContentValuesAlarm.put(DBHelper.mFieldAlarmIsActive, "0");
                            }

                            mContentValuesAlarm.put(DBHelper.mFieldAlarmIsSync, "0");
                            mActivity.mDbHelper.updateRecord(DBHelper.mTableAlarm, mContentValuesAlarm, DBHelper.mFieldAlarmLocalID + "=?",
                                    new String[]{mDataHolderAlarm.get_Listholder().get(j).get(DBHelper.mFieldAlarmLocalID)});
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    isDeviceDelete = false;
                }

                String mStringQueryAlarm = "delete from " + DBHelper.mTableAlarmDeviceList + " where " +
                        DBHelper.mFieldADDeviceLocalID + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'";
                mActivity.mDbHelper.exeQuery(mStringQueryAlarm);
//                    String mStringQueryGroup = "delete from " + mActivity.mDbHelper.mTableGroupDeviceList + " where " + mActivity.mDbHelper.mFieldGDListLocalDeviceID + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'";
//                    mActivity.mDbHelper.exeQuery(mStringQueryGroup);

                if (mArrayListDevice.get(intDeleteDevicePosition).getDeviceServerId() != null &&
                        !mArrayListDevice.get(intDeleteDevicePosition).getDeviceServerId().equalsIgnoreCase("") &&
                        !mArrayListDevice.get(intDeleteDevicePosition).getDeviceServerId().equalsIgnoreCase("null")) {
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(DBHelper.mFieldDeviceIsActive, "0");
                    mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");
                    String[] mArray = new String[]{mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId()};
                    mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues, DBHelper.mFieldDeviceLocalId + "=?", mArray);
                    mArrayListDevice.get(intDeleteDevicePosition).setDevice_is_active("0");
                    mArrayListDevice.get(intDeleteDevicePosition).setDevice_is_sync("0");
                    if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                        if (mActivity.mUtility.haveInternet()) {
                            mActivity.updateDeviceAPI(mArrayListDevice.get(intDeleteDevicePosition));
                        }
                    }
                } else {
                    String mStringQuery = "delete from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceLocalId + "= '"
                            + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'";
                    mActivity.mDbHelper.exeQuery(mStringQuery);
                }

                showDeviceDeleteAlert();
            } else {
//                showDeviceDeleteRetryAlert();
            }
        } else {
//            showDeviceDeleteRetryAlert();
        }
    }

    private void removePowerSocketDataAndUpdate() {
        mActivity.hideProgress();

        // Delete Socket Alarms
//                mArrayListDevice.get(intDeleteDevicePosition).setDevice_is_active("0");
//                mArrayListDevice.get(intDeleteDevicePosition).setDevice_is_sync("0");

        console.log("asjkxaksbxjkabsxkj", powerSocketToDelete.bleAddress);
        deletePowerSocketAlarmDetails(powerSocketToDelete.getBleAddress());

        // Delete Socket Device Detail Table
        String deleteSocketDeviceDtlQuery = "Delete from " + DBHelper.mTableSocketDeviceDtl + " where " +
                DBHelper.mFieldTableSocketDeviceDtlDeviceId + " = " + "'" + powerSocketToDelete.getId() + "'";
        console.log("kjcsjkcsjkdc", deleteSocketDeviceDtlQuery);
        mActivity.mDbHelper.exeQuery(deleteSocketDeviceDtlQuery);

        String mStringQuery = "delete from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceBleAddress + "= '"
                + powerSocketToDelete.bleAddress.replace(":", "") + "'";
        mActivity.mDbHelper.exeQuery(mStringQuery);

        console.log("asjkxaksbxjkabsxkj", "Query = " + mStringQuery);

        if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
            if (mActivity.mUtility.haveInternet()) {
                mActivity.updateDeviceAPI(mArrayListDevice.get(intDeleteDevicePosition));
            }
        }
        showDeviceDeleteAlert();
    }

    /*Call Authentication API to check valid user*/
    private void checkAuthenticationAPI(final boolean isShowProgress, final int relativePosition) {
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
                    mActivity.runOnUiThread(() -> {
                        // Check device type of the selected device
                        VoDeviceList selectedVoDevice = mArrayListDevice.get(relativePosition);
                        String deviceTypeStr = selectedVoDevice.getDevice_Type();
                        if (deviceTypeStr != null && !deviceTypeStr.isEmpty()) {
                            int deviceType = Integer.parseInt(deviceTypeStr, 16);
                            switch (deviceType) {
                                case 0x100:              // Bulb
                                case 0x200:              // Bulb
                                case 0x600:              // Strip Light
                                case 0x700:              // Lamp
                                    deleteSmartLightDeviceRequest(relativePosition);
                                    break;
                                case 0x400:              // Power Socket
                                case 0x800:              // Socket
                                    mActivity.showProgress("Deleting power socket. Please wait...", true);
                                    deletePowerSocket(selectedVoDevice);
                                    break;
                                case 0x300:              // Switch
                                    break;
                                case 0x500:              // Fan
                                    break;
                            }
                        }
                    });
                } else {
                    mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_session_expired), 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            isPowerSocketDeleting = false;
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

    /*Show delete device alert dialog*/
    private void showDeviceDeleteAlert() {
        isPowerSocketDeleting = false;
        isDeviceDelete = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_device_delete_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                powerSocketToDelete = null;
                isDeviceDelete = false;
                isCalling = true;
                getDBDeviceList(false);
                onStart();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void deletePowerSocket(VoDeviceList selectedDevice) {
        stopExecutorService();

        if (selectedDevice != null) {
            if (selectedDevice.getDevice_BleAddress() != null && !selectedDevice.getDevice_BleAddress().isEmpty()) {
                powerSocketToDelete = new PowerSocket(selectedDevice);

                BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
                if (bluetoothAdapter.isEnabled() && powerSocketBLEService.isDeviceConnected(powerSocketToDelete.bleAddress)) {
                    isPowerSocketDeleting = true;
                    powerSocketBLEService.removeDevice(powerSocketToDelete);
                } else if (powerSocketToDelete.isWifiConfigured && powerSocketMQTTService.isConnected()) {
                    isPowerSocketDeleting = true;
                    powerSocketMQTTService.removeDevice(powerSocketToDelete);
                } else {
                    removePowerSocketDataAndUpdate();
                }
            }
        }
    }

    private void deletePowerSocketAlarmDetails(String bleAddress) {
        AsyncTask.execute(() -> {
            String query = "DELETE FROM " + mTableAlarmPowerSocket + " WHERE " + mfield_ble_address + "= '" +
                    bleAddress.replace(":", "").toUpperCase() + "'";
            mActivity.mDbHelper.exeQuery(query);
        });
    }

    PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks = new PowerSocketBLEEventCallbacks() {
        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            // Todo for code 19,22,133
            // This is required because the power socket disconnects from app when it is deleted.
//            if (isPowerSocketDeleting) {
//                isPowerSocketDeleting = false;
//                if (isVisible()) {
//                    mActivity.hideProgress();
//                    // Delete directly fromDatabase
//                    checkScanDeviceList();
//                }
//            }

//            // Pick a power socket from Database
//            if (mArrayListDevice.size() > 0) {
//                getPowerSocketFromDb();
//                startExecutorService();
//            }
        }

        @Override
        public void onBluetoothRestart() {
            super.onBluetoothRestart();
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                mActivity.mUtility.errorDialogWithCallBack("Something went wrong. Device connection failed. Please turn off and turn off Bluetooth and try again. " +
                        "If the same problems persists, please restart the phone and try again.", 1, true, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        mActivity.onBackPressed();
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }
        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    if (powerSocketMQTTService.isConnected()) {
                        VoDeviceList device = getDevice(deviceAddress);
                        PowerSocket powerSocket = new PowerSocket(device);
                        powerSocketMQTTService.unsubscribe(powerSocket);
                    }
                    removePowerSocketDataAndUpdate();
                }
            }
        }

        @Override
        public void onAuthenticationSuccess(String deviceAddress) {
            super.onAuthenticationSuccess(deviceAddress);
            VoDeviceList device = getDevice(deviceAddress);
            if (device != null) {
                PowerSocket powerSocket = new PowerSocket(device);
                powerSocketBLEService.checkSocketDiagnostics(powerSocket, transactionId);
            }
        }

        @Override
        public void onAuthenticationFailure(String deviceAddress) {
            super.onAuthenticationFailure(deviceAddress);
            if (isAdded()) {
                stopExecutorService();
                VoDeviceList device = getDevice(deviceAddress);
                if (device != null) {
//                    powerSocketToDelete = new PowerSocket(device);
                    Toast.makeText(getContext(), "Authentication Failed = " + deviceAddress, Toast.LENGTH_LONG).show();
                    if (powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                        powerSocketBLEService.disconnect(deviceAddress);
                    }
                }
            }
        }

        @Override
        public void onDeviceRemoved(String deviceAddress) {
            super.onDeviceRemoved(deviceAddress);

            VoDeviceList device = getDevice(deviceAddress);
            powerSocketToDelete = new PowerSocket(device);

            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                mActivity.hideProgress();

                if (powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.unsubscribe(powerSocketToDelete);
                }

                removePowerSocketDataAndUpdate();
            } else {
                String spannableText = "The power socket " + powerSocketToDelete.getBleName() + " may have been deleted from another phone. " +
                        "Please add the selected power socket again.";
                SpannableString spannableString = new SpannableString(spannableText);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 17, 17 + powerSocketToDelete.getBleName().length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                mActivity.mUtility.errorDialogWithCallBack(spannableString, 1, false, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        if (powerSocketMQTTService.isConnected()) {
                            powerSocketMQTTService.unsubscribe(powerSocketToDelete);
                        }

                        if (powerSocketBLEService.isDeviceConnected(powerSocketToDelete.bleAddress)) {
                            powerSocketBLEService.disconnect(powerSocketToDelete.bleAddress);
                        }

                        removePowerSocketDataAndUpdate();
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }
        }

        @Override
        public void onDeviceRemovalFailed(String deviceAddress) {
            super.onDeviceRemovalFailed(deviceAddress);
            isPowerSocketDeleting = false;
            mActivity.hideProgress();

            VoDeviceList device = getDevice(deviceAddress);
            PowerSocket powerSocket = new PowerSocket(device);

            if (powerSocketMQTTService.isConnected()) {
                powerSocketMQTTService.unsubscribe(powerSocket);
            }

            if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(powerSocket.bleAddress);
            }

            removePowerSocketDataAndUpdate();
        }

        @Override
        public void onCheckSocketDiagnostics(String deviceAddress, int transactionId, byte[] socketStateArray) {
            super.onCheckSocketDiagnostics(deviceAddress, transactionId, socketStateArray);
            console.log(TAG, "Socket Diagnostics = " + ByteConverter.getHexStringFromByteArray(socketStateArray, true));
            updateSocketUI(socketStateArray, deviceAddress);
        }

        @Override
        public void onSingleSocketStateChange(String deviceAddress, int transactionId, byte[] data) {
            super.onSingleSocketStateChange(deviceAddress, transactionId, data);
            console.log(TAG, "Single Socket State Change = " + ByteConverter.getHexStringFromByteArray(data, true));
            updateSocketUI(data, deviceAddress);
        }

        @Override
        public void onAllSocketsStateChange(String deviceAddress, int transactionId, byte[] data) {
            console.log(TAG, "All Sockets State Change = " + ByteConverter.getHexStringFromByteArray(data, true));
            super.onAllSocketsStateChange(deviceAddress, transactionId, data);
            updateSocketUI(data, deviceAddress);
        }

        @Override
        public void onSingleSocketStateChangeFailed(String deviceAddress, int transactionId) {
            super.onSingleSocketStateChangeFailed(deviceAddress, transactionId);
        }

        @Override
        public void onAllSocketsStateChangeFailed(String deviceAddress, int transactionId) {
            super.onAllSocketsStateChangeFailed(deviceAddress, transactionId);
        }
    };

    PowerSocketMQTTEventCallbacks powerSocketMQTTEventCallbacks = new PowerSocketMQTTEventCallbacks() {
        @Override
        public void onMQTTConnected() {
            console.log(TAG, "Connected to MQTT Broker");
            // Subscribe topics to MQTT Broker
            subscribeSockets();
        }

        @Override
        public void onMQTTDisconnected() {
            console.log(TAG, "Disconnected from MQTT Broker");
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    mActivity.hideProgress();

                    if (powerSocketToDelete != null) {
                        if (powerSocketBLEService.isDeviceConnected(powerSocketToDelete.bleAddress)) {
                            powerSocketBLEService.disconnect(powerSocketToDelete.bleAddress);
                        }
                    }

                    removePowerSocketDataAndUpdate();
                }
            }
        }

        @Override
        public void onMQTTConnectionFailed() {
            console.log(TAG, "MQTT Connection failed");
        }

        @Override
        public void onMQTTException() {
            console.log(TAG, "MQTT Exception");
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    if (powerSocketToDelete != null) {
                        if (powerSocketMQTTService.isConnected()) {
                            powerSocketMQTTService.unsubscribe(powerSocketToDelete);
                        }

                        if (powerSocketBLEService.isDeviceConnected(powerSocketToDelete.bleAddress)) {
                            powerSocketBLEService.disconnect(powerSocketToDelete.bleAddress);
                        }
                    }

                    removePowerSocketDataAndUpdate();
                }
            }
        }

        @Override
        public void onMQTTTimeout(String deviceAddress) {
            console.log(TAG, "MQTT Timeout => " + deviceAddress);
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    VoDeviceList device = getDevice(deviceAddress);
                    PowerSocket powerSocket = new PowerSocket(device);
                    mActivity.hideProgress();
                    if (powerSocketMQTTService.isConnected()) {
                        powerSocketMQTTService.unsubscribe(powerSocket);
                    }

                    if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                        powerSocketBLEService.disconnect(powerSocket.bleAddress);
                    }

                    removePowerSocketDataAndUpdate();
                }
            }
        }

        @Override
        public void onTopicSubscribed(String deviceAddress) {
            console.log(TAG, "Topic Subscribed => " + deviceAddress);
            // Set UTC Time
            if (!mBluetoothManager.getAdapter().isEnabled() ||
                    !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
                    VoDeviceList voDeviceList = getDevice(deviceAddress);
                    PowerSocket powerSocket = null;
                    if (voDeviceList != null) {
                        powerSocket = new PowerSocket(voDeviceList);
                        powerSocketMQTTService.setCurrentTime(powerSocket, true);
                    }
                }
            }
        }

        @Override
        public void onCurrentTimeSet(String deviceAddress) {
            super.onCurrentTimeSet(deviceAddress);
            console.log(TAG, "Current UTC Time set => " + deviceAddress);
            if (!mBluetoothManager.getAdapter().isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
                    VoDeviceList voDeviceList = getDevice(deviceAddress);
                    PowerSocket powerSocket = null;
                    if (voDeviceList != null) {
                        powerSocket = new PowerSocket(voDeviceList);
                        powerSocketMQTTService.checkSocketDiagnostics(powerSocket, transactionId);
                    }
                }
            }
        }

        @Override
        public void onCheckSocketDiagnostics(String deviceAddress, int transactionId, byte[] socketStateArray) {
            super.onCheckSocketDiagnostics(deviceAddress, transactionId, socketStateArray);
            console.log(TAG, "Socket Diagnostics Complete => " + deviceAddress + " " + ByteConverter.getHexStringFromByteArray(socketStateArray, true));
            if (!mBluetoothManager.getAdapter().isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                updateSocketUI(socketStateArray, deviceAddress);
            } else {
                if (FragmentDevices.this.transactionId != transactionId) {
                    updateSocketUI(socketStateArray, deviceAddress);
                }
            }
        }

        @Override
        public void onSingleSocketStateChange(String deviceAddress, int transactionId, byte[] data) {
            super.onSingleSocketStateChange(deviceAddress, transactionId, data);
            if (!mBluetoothManager.getAdapter().isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                updateSocketUI(data, deviceAddress);
            } else {
                if (FragmentDevices.this.transactionId != transactionId) {
                    updateSocketUI(data, deviceAddress);
                }
            }
        }

        @Override
        public void onAllSocketsStateChange(String deviceAddress, int transactionId, byte[] data) {
            super.onAllSocketsStateChange(deviceAddress, transactionId, data);
            if (!mBluetoothManager.getAdapter().isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                updateSocketUI(data, deviceAddress);
            } else {
                if (FragmentDevices.this.transactionId != transactionId) {
                    updateSocketUI(data, deviceAddress);
                }
            }
        }

        @Override
        public void onSingleSocketStateChangeFailed(String deviceAddress, int transactionId) {
            super.onSingleSocketStateChangeFailed(deviceAddress, transactionId);
        }

        @Override
        public void onAllSocketsStateChangeFailed(String deviceAddress, int transactionId) {
            super.onAllSocketsStateChangeFailed(deviceAddress, transactionId);
        }

        @Override
        public void onDeviceRemoved(String deviceAddress) {
            super.onDeviceRemoved(deviceAddress);
            console.log(TAG, "Device Removed MQTT => " + deviceAddress);
            isPowerSocketDeleting = false;
            mActivity.hideProgress();
            powerSocketBLEService.cancelDeleteDeviceTimeout();

            VoDeviceList device = getDevice(deviceAddress);
            PowerSocket powerSocket = new PowerSocket(device);

            if (powerSocketMQTTService.isConnected()) {
                powerSocketMQTTService.unsubscribe(powerSocket);
            }

            if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(powerSocket.bleAddress);
            }

            removePowerSocketDataAndUpdate();
        }

        @Override
        public void onDeviceRemovalFailed(String deviceAddress) {
            super.onDeviceRemovalFailed(deviceAddress);
            console.log(TAG, "Device Removal Failed => " + deviceAddress);
            isPowerSocketDeleting = false;
            mActivity.hideProgress();
            powerSocketBLEService.cancelDeleteDeviceTimeout();

            VoDeviceList device = getDevice(deviceAddress);
            PowerSocket powerSocket = new PowerSocket(device);

            if (powerSocketMQTTService.isConnected()) {
                powerSocketMQTTService.unsubscribe(powerSocket);
            }

            if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(powerSocket.bleAddress);
            }

            removePowerSocketDataAndUpdate();
        }
    };

    private void updateSocketUI(byte[] socketStateArray, String deviceAddress) {
        boolean isMasterSwitchOff = false;
        for (byte socketState : socketStateArray) {
            if (socketState == 0x00) {
                isMasterSwitchOff = true;
                break;
            }
        }

        VoDeviceList selectedVoDevice = getDevice(deviceAddress);

        if (selectedVoDevice != null) {
            if (isMasterSwitchOff) {
                selectedVoDevice.setDevice_SwitchStatus("OFF");
                selectedVoDevice.setIsChecked(false);
            } else {
                selectedVoDevice.setDevice_SwitchStatus("ON");
                selectedVoDevice.setIsChecked(true);
            }

            int index = mArrayListDevice.indexOf(selectedVoDevice);
            if (index != -1) {
                mArrayListDevice.set(index, selectedVoDevice);
                if (mDeviceListAdapter != null) {
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private VoDeviceList getDevice(String deviceAddress) {
        String queryAddress = deviceAddress.replace(":", "");
        for (VoDeviceList device : mArrayListDevice) {
            if (device.getDevice_BleAddress().equalsIgnoreCase(queryAddress)) {
                return device;
            }
        }
        return null;
    }

    // Todo Code re-writing and cleanup required
    @Override
    public void onStart() {
        super.onStart();
        // Register for Bluetooth Adapter state change callbacks
        mActivity.setOnBluetoothStateChangeListener(bluetoothStateChangeListener);
        //Register for BLE Scan Callbacks
        mActivity.setOnDevicesStatusChange(connectionStatusChangeCallback);
        // Register for PowerSocket BLE Callbacks
        powerSocketBLEService.setOnPowerSocketEventCallbacks(powerSocketBLEEventCallbacks);
        // Register for releasing PowerSocket BLE Callbacks
        mActivity.releasePowerSocketBLEServiceCallback = releasePowerSocketBLEServiceCallback;

        // BLE Connection
        // Pick a power socket from Database
        if (mArrayListDevice.size() > 0) {
            for (VoDeviceList device : mArrayListDevice) {
                if (device.getDevice_Type().equals("0400") && device.getDevice_is_active().equals("1")) {
                    // For scheduling of tasks
                    PowerSocket powerSocket = new PowerSocket(device);
                    if (powerSocket.bleAddress != null && !powerSocket.bleAddress.isEmpty()) {
                        BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
                        if (bluetoothAdapter.isEnabled() && powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                            powerSocketBLEService.checkSocketDiagnostics(powerSocket, transactionId);
                        }
                    }
                }
            }

            // Run ExecutorService
            startExecutorService();
        }

        //--------------------------------------------------------------------------------------
        // MQTT Broker Connection
        // Register for Network Connectivity (Internet) change callbacks
        ConnectivityManager connectivityManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback);
        } else {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
        }
    }

    private void subscribeSockets() {
        for (VoDeviceList device : mArrayListDevice) {
            if (device.getDevice_Type().equals("0400") && device.getDevice_is_active().equals("1")) {
                // For scheduling of tasks
                PowerSocket powerSocket = new PowerSocket(device);
                if (powerSocket.isWifiConfigured) {
                    powerSocketMQTTService.subscribe(powerSocket);
                }
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // MQTT doesn't disconnect when app is minimized

        powerSocketMQTTService.cancelPendingMQTTTimeouts();

        stopExecutorService();

        List<String> connectingDevicesAddressList = powerSocketBLEService.getConnectingDevicesAddressList();
        for (String address : connectingDevicesAddressList) {
            powerSocketBLEService.disconnect(address);
        }

        powerSocketBLEService.setOnPowerSocketEventCallbacks(null);
        mActivity.setOnDevicesStatusChange(null);
        mActivity.setOnBluetoothStateChangeListener(null);
        powerSocketMQTTService.setOnPowerSocketMQTTEventCallbacks(null);

        ConnectivityManager connectivityManager = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
        connectivityManager.unregisterNetworkCallback(networkCallback);
    }

    private void startExecutorService() {
        service = Executors.newScheduledThreadPool(1);
        for (VoDeviceList device : mArrayListDevice) {
            if (device.getDevice_Type().equals("0400") && device.getDevice_is_active().equals("1")) {
                // For scheduling of tasks
                PowerSocket powerSocket = new PowerSocket(device);
                service.scheduleAtFixedRate(new BLEConnectionTask(powerSocketBLEService, powerSocket),
                        0, 10, TimeUnit.SECONDS);
            }
        }
    }

    private void stopExecutorService() {
        if (service != null) {
            if (!service.isShutdown()) {
                service.shutdownNow();
            }
        }
    }

    public static class BLEConnectionTask implements Runnable {
        private PowerSocketBLEService powerSocketBLEService;
        private PowerSocket selectedPowerSocket;

        private BLEConnectionTask(PowerSocketBLEService powerSocketBLEService, PowerSocket selectedPowerSocket) {
            this.powerSocketBLEService = powerSocketBLEService;
            this.selectedPowerSocket = selectedPowerSocket;
        }

        @Override
        public void run() {
            // Search for Bluetooth Device
            // For scheduling of tasks
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                if (!powerSocketBLEService.isDeviceConnected(selectedPowerSocket.getBleAddress())) {
                    console.log("PowerSocketControlActivity", "Ready to connect to " + selectedPowerSocket.bleAddress);
                    powerSocketBLEService.connect(selectedPowerSocket);
                } else {
                    console.log("PowerSocketControlActivity", "Already Connected to " + selectedPowerSocket.bleAddress);
                }
            } else {
                console.log("PowerSocketControlActivity", "Bluetooth is off");
            }
        }
    }

    MainActivity.ReleasePowerSocketBLEServiceCallback releasePowerSocketBLEServiceCallback = new MainActivity.ReleasePowerSocketBLEServiceCallback() {
        @Override
        public void onRelease() {
            if (powerSocketBLEService != null) {
                powerSocketBLEService.disconnectAllDevices();
            }

            if (powerSocketMQTTService != null) {
                powerSocketMQTTService.dispose();
            }
        }
    };

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            console.log(TAG, "Network Available");
            // Is Network enabled ?
            if (!powerSocketMQTTService.isConnected()) {
                // Initiate MQTT Connection
                powerSocketMQTTService.initialize();
            } else {
                // Subscribe
                subscribeSockets();
            }
            powerSocketMQTTService.setOnPowerSocketMQTTEventCallbacks(powerSocketMQTTEventCallbacks);
        }

        @Override
        public void onLost(@NonNull Network network) {
            // network unavailable
            console.log(TAG, "Network Offline");
        }
    };

    OnBluetoothStateChangeListener bluetoothStateChangeListener = new OnBluetoothStateChangeListener() {
        @Override
        public void onBluetoothOff() {

        }

        @Override
        public void onBluetoothTurningOff() {
            for (VoDeviceList device : mArrayListDevice) {
                if (device.getDevice_Type().equals("0400") && device.getDevice_is_active().equals("1")) {
                    // For scheduling of tasks
                    PowerSocket powerSocket = new PowerSocket(device);
                    if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                        powerSocketBLEService.disconnect(powerSocket.bleAddress);
                    }
                }
            }
        }

        @Override
        public void onBluetoothOn() {

        }

        @Override
        public void onBluetoothTurningOn() {

        }
    };
}