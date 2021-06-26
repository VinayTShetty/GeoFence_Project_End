package com.succorfish.depthntemp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.succorfish.depthntemp.ble.BluetoothLeService;
import com.succorfish.depthntemp.ble.SampleGattAttributes;
import com.succorfish.depthntemp.db.AppRoomDatabase;
import com.succorfish.depthntemp.db.TableBleDevice;
import com.succorfish.depthntemp.db.TableDive;
import com.succorfish.depthntemp.db.TablePressureTemperature;
import com.succorfish.depthntemp.fragnments.FragmentDashboard;
import com.succorfish.depthntemp.helper.BLEUtility;
import com.succorfish.depthntemp.helper.CustomDialog;
import com.succorfish.depthntemp.helper.Encryption;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.helper.URLCLASS;
import com.succorfish.depthntemp.helper.Utility;
import com.succorfish.depthntemp.interfaces.onAlertDialogCallBack;
import com.succorfish.depthntemp.interfaces.onDeviceConnectionStatusChange;
import com.succorfish.depthntemp.interfaces.onDeviceSettingChange;
import com.succorfish.depthntemp.vo.VoBluetoothDevices;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    private LocationManager mLocationManager;
    String TAG = MainActivity.class.getSimpleName();
    public Utility mUtility;
    //    public PreferenceHelper mPreferenceHelper;
    @BindView(R.id.activity_main_relativelayout_main)
    public RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.activity_main_coordinateLayout)
    public CoordinatorLayout mViewMainContainer;
    @BindView(R.id.activity_main_toolbar)
    public Toolbar mToolbar;
    public ImageView mImageViewBack;
    public ImageView mImageViewAddDevice;
    public TextView mTextViewTitle;
    public TextView mTextViewAdd;
    FragmentTransaction fragmentTransaction;
    private boolean exit = false;

    // BLE
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeService mBluetoothLeService;
    public ArrayList<VoBluetoothDevices> mLeDevices = new ArrayList<>();
    public BluetoothDevice mBluetoothDevice;
    private ScanCallback scanCallback;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothAdapter.LeScanCallback leScanCallback;
    public static final int BLUETOOTH_ENABLE_REQT = 11;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private boolean isReadyToUnbind = false;
    public boolean isDevicesConnected = false;

    private boolean mIsScaning = false;
    private boolean isBLEFailToSCan = false;
    public onDeviceConnectionStatusChange mOnDeviceConnectionStatusChange;
    public onDeviceSettingChange mOnDeviceSettingChange;

    public String mStringConnectedDevicesAddress = "";
    public boolean isNeverAskPermissionCheck = false;

    public AppRoomDatabase mAppRoomDatabase;

    public String mStringDeviceName = "";
    public String mStringDeviceTime = "";
    public String mStringDeviceBattery = "";
    public String mStringDeviceMemory = "";
    public String mStringDeviceFirmwareVersion = "";
    public String mStringDeviceStationaryInterval = "";
    public String mStringDeviceReadingDepthCutoff = "";
    public String mStringDeviceBleTransmission = "";
    public String mStringGpsInterval = "";
    public String mStringGpsTimeout = "";
    public int mIntGetSettingType = 0;
    public boolean mDeviceSettingFetch = false;
    short mByteAckCommand = (short) 0xFF0D;
    public TabLayout mSmartTabLayout;
    public boolean mIsDeviceSyncStart = false;
    public int diveOnePosition = 0;
    public int diveTwoPosition = 0;
    public int deviceOnePosition = 0;
    public int deviceTwoPosition = 0;
    public VoBluetoothDevices mVoBluetoothDevicesConnected;
    private Encryption mEncryption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);
//        mPreferenceHelper = new PreferenceHelper(MainActivity.this);
        mUtility = new Utility(MainActivity.this);
        initToolbar();
        mLeDevices = new ArrayList<>();
        mAppRoomDatabase = AppRoomDatabase.getDatabaseInstance(MainActivity.this);
        try {
            mEncryption = new Encryption(BLEUtility.hexStringToBytes(BuildConfig.APP_SECREAT_KEY));
        } catch (Exception e) {
            e.printStackTrace();
        }

        removeAllFragmentFromBack();
        FragmentDashboard mFragmentHome = new FragmentDashboard();
        replacesFragment(mFragmentHome, false, null, 1);
        setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {

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

        setOnDevicesSettingChange(new onDeviceSettingChange() {
            @Override
            public void onDeviceFwChange(String fwChange) {

            }

            @Override
            public void onDeviceMemoryChange(String memoryChange) {

            }

            @Override
            public void onDeviceTimeChange(String timeChange) {

            }

            @Override
            public void onDeviceBatteryChange(String batteryChange) {

            }

            @Override
            public void onDeviceStationaryIntervalChange(String batteryChange) {

            }

            @Override
            public void onDeviceDepthCutOffChange(String batteryChange) {

            }

            @Override
            public void onDeviceBleTransmissionChange(String batteryChange) {

            }

            @Override
            public void onDeviceGpsIntervalChange(String batteryChange) {

            }

            @Override
            public void onDeviceGpsTimeoutChange(String batteryChange) {

            }
        });
    }

    public void setOnDevicesStatusChange(onDeviceConnectionStatusChange stateChange) {
        this.mOnDeviceConnectionStatusChange = stateChange;
    }

    public void setOnDevicesSettingChange(onDeviceSettingChange settingChange) {
        this.mOnDeviceSettingChange = settingChange;
    }

    /*Bluetooth state receiver*/
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        System.out.println("Bluetooth OFF");
                        isDevicesConnected = false;
                        stopScan(true);
                        if (mBluetoothLeService != null) {
                            mBluetoothLeService.disconnect();
                            mBluetoothLeService.close();
                            mBluetoothLeService.stopSelf();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        System.out.println("Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        System.out.println("Bluetooth on");
//                        BluetoothAdapter.getDefaultAdapter().enable();
//                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
//                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//                        if (mAutoStopDeviceScanTimer != null) {
//                            mAutoStopDeviceScanTimer.cancel();
//                        }
//                        mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer(autoStopScanTime, 1000);
//                        mAutoStopDeviceScanTimer.start();
                        stopScan(true);
                        mLeDevices = new ArrayList<>();

                        if (Build.VERSION.SDK_INT >= 23) {
                            // Marshmallow+ Permission APIs
                            callMarshmallowPermission();
                        }

                        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//                            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//                                BluetoothAdapter.getDefaultAdapter().enable();
//                            }
                            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                            mBluetoothAdapter = mBluetoothManager.getAdapter();
                            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() == false) {
                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableIntent, BLUETOOTH_ENABLE_REQT);
                            } else {
                                if (!isLocationEnabled(MainActivity.this)) {
                                    if (Build.VERSION.SDK_INT >= 23) {
                                        System.out.println("STATE_ON-GPS");
                                        mUtility.errorDialogWithYesNoCallBack("Turn on GPS Location.", "Since Android 6.0 the system requires access to device's location in order to scan bluetooth devices.", "Ok", "Cancel", false, 3, new onAlertDialogCallBack() {
                                            @Override
                                            public void PositiveMethod(DialogInterface dialog, int id) {
                                                try {
                                                    Intent mIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                                    startActivity(mIntent);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }

                                            @Override
                                            public void NegativeMethod(DialogInterface dialog, int id) {

                                            }
                                        });
                                    }
                                }
                                Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                            }
                            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                            registerReceiver(mBluetoothStateReceiver, filter);
                            startDeviceScan();
                            isReadyToUnbind = true;
                        }

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        System.out.println("Turning Bluetooth on...");
                        break;
                }
            }
        }
    };

    /*Check location enable or not*/
    public boolean isLocationEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            return mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            String locationProviders;
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /*Ble service connection*/
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                System.out.println("Bluetooth Service not initialize. Please open app again.");
            } else {

                mLeDevices = new ArrayList<>();
//                stopScan();
                RescanDevice(false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // mBluetoothLeService = null;

            System.out.println("-------Services Disconnected...");
        }
    };
    public int mIntPackageLength = 0;
    public int mDecodeIndex = 0;
    String tempLatitude = "", latitudeFull = "", tempLongitude = "", longitudeFull = "", stat_time = "", mov_time = "", utc_time = "", full_utc_time = "";
    public int mNextDecodeType = 0;
    public String mStoredPressure = "";

    public int mIntPacketReceivedLength = 0;
    public String mStrBleAddress = "";
    //    public int mIntScanCount = 0;
    String mStringReceivedMsg = "";
    String mStringReceivedMsgProcess = "";
    String mStringReceivedMsgFull = "";
    String mStringReceivedMsgFullTemp = "";
    public long mLongDiveId = 1;
    int index = 0;
    int intPacketNo = 0;
    int intPriviousPacketNo = 0;
    int mIntNotificationType = 0;
    double tempCal = 0;
    double pressCal = 0;
    String pressure = "", temp = "";
    boolean data_present = false;
    boolean isDataAlreadyAvailable = false;
    TablePressureTemperature mTablePressureTemperature;
    /*ble gatt receiver*/
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String mStringDevicesAddress = intent.getStringExtra("mStringDevicesAddress");
            final String mStringDevicesName = intent.getStringExtra("mStringDevicesName");
            try {
                System.out.println(TAG + "-----BroadcastReceiver action " + action);
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    System.out.println(TAG + "-----Connected Device " + action);
                    System.out.println(TAG + "-----BroadcastReceiver mStringDevicesAddress " + mStringDevicesAddress);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//						stopScan();
                            isDevicesConnected = true;
                            stopScan(true);
                            if (mBluetoothDevice != null) {

                                for (int i = 0; i < mLeDevices.size(); i++) {
                                    if (mStringDevicesAddress != null && mStringDevicesAddress.equalsIgnoreCase(mLeDevices.get(i).getDeviceAddress())) {
                                        System.out.println(TAG + "-----Connected Device " + mStringDevicesAddress);
                                        mStringConnectedDevicesAddress = mStringDevicesAddress;
                                        System.out.println(TAG + "-----BroadcastReceiver mStringConnectedDevicesAddress " + mStringConnectedDevicesAddress);
                                        mLeDevices.get(i).setIsConnected(true);
                                        if (mOnDeviceConnectionStatusChange != null) {
                                            mOnDeviceConnectionStatusChange.onConnect(null, mBluetoothDevice.getName(), mBluetoothDevice);
                                        }
//                                    if (mStartDeviceScanTimer != null) {
//                                        mStartDeviceScanTimer.cancel();
//                                    }

                                        break;
                                    }
                                }
                            }
                        }
                    });


//                    Timer innerTimer = new Timer();
//                    innerTimer.schedule(new TimerTask() {
//                        @Override
//                        public void run() {
//                            runOnUiThread(new Runnable() {
//                                public void run() {
//                                    openDeviceNameDialog();
//                                }
//                            });
//                        }
//                    }, 500);
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    System.out.println(TAG + "-----Disconnected Device " + action);
                    hideProgress();
                    if (mIsDeviceSyncStart) {
                        mIsDeviceSyncStart = false;
                        mUtility.errorDialogWithCallBack("Device sync is interrupted. Please try again device sync.", 0, true, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                            }
                        });
                        if (!isDataAlreadyAvailable) {
                            new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected void onPreExecute() {
                                    super.onPreExecute();
                                    System.out.println("JD-Dive Deleted CHECK");
                                }

                                @Override
                                protected Void doInBackground(Void... params) {
                                    System.out.println("JD-Start temp*depth data Deleted");
                                    mAppRoomDatabase.tempPressDao().deleteTempPressDiveByDiveId((int) mLongDiveId);
                                    System.out.println("JD-temp*depth data Deleted");
                                    System.out.println("JD-Start Dive Deleted");
                                    mAppRoomDatabase.diveDao().deleteDiveById((int) mLongDiveId);
                                    System.out.println("JD-Dive Deleted");
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void agentsCount) {

                                }
                            }.execute();
                        }
                    }
                    runOnUiThread(new Runnable() {
                        public void run() {
                            if (mBluetoothDevice != null) {
                                for (int i = 0; i < mLeDevices.size(); i++) {
                                    if (mStringDevicesAddress != null && !mStringDevicesAddress.equalsIgnoreCase("")) {
                                        if (mStringDevicesAddress.equalsIgnoreCase(mLeDevices.get(i).getDeviceAddress())) {
                                            mStringConnectedDevicesAddress = "";
                                            mLeDevices.get(i).setIsConnected(false);
                                            break;
                                        }
                                    } else {
                                        mStringConnectedDevicesAddress = "";
                                    }
                                }
//                                boolean isConnectedAnyDevice = false;
//                                for (VoBluetoothDevices device : mDeviceListArrayMain) {
//                                    if (device.getIsConnected()) {
//                                        isConnectedAnyDevice = true;
//                                        break;
//                                    }
//                                }
                                isDevicesConnected = false;
                                mDeviceSettingFetch = false;
                                stopScan(true);
//                                if (!isFromDeviceConnection) {
//                                    mDeviceListArrayMain = new ArrayList<>();
//                                    RescanDevice();
//                                }
                                if (mOnDeviceConnectionStatusChange != null) {
                                    System.out.println(TAG + "-----Disconnected Device");
                                    if (mBluetoothDevice != null) {
                                        mOnDeviceConnectionStatusChange.onDisconnect(null, mBluetoothDevice.getName(), mBluetoothDevice);
                                        mBluetoothDevice = null;
                                    }
                                }
//                            Toast.makeText(MainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    runOnUiThread(new Runnable() {
                        public void run() {
//                            System.out.println(TAG + "-----ACTION_GATT_SERVICES_DISCOVERED");
                            displayGattServices(mBluetoothLeService.getSupportedGattServices(), mStringDevicesAddress, mStringDevicesName);
                        }
                    });
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                    System.out.println(TAG + "-----ACTION_DATA_AVAILABLE");
                    if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA) != null) {
                        mStringReceivedMsg = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                        mIntNotificationType = intent.getIntExtra(BluetoothLeService.BLE_NOTIFICATION_TYPE, 0);
                        if (mStringReceivedMsg != null && !mStringReceivedMsg.equals("") && !mStringReceivedMsg.equals("null")) {
//                            System.out.println("JD-mStringReceivedMsg-" + mStringReceivedMsg);
                            mStringReceivedMsg = mStringReceivedMsg.replaceAll("\\s+", "");
                            System.out.println("JD-mStringReceivedMsgFinal-" + mStringReceivedMsg);
                            mStringReceivedMsgFull = mStringReceivedMsg;
                            try {
                                if (mStringReceivedMsg.length() == 20) {
                                    if (mStringReceivedMsg.startsWith("FF03")) {

                                        int latitude = (int) Long.parseLong(mStringReceivedMsg.substring(4, 12), 16);
                                        int longitude = (int) Long.parseLong(mStringReceivedMsg.substring(12, 20), 16);
                                        final String mStrLat = getLocationCalculation(latitude);
                                        final String mStrLong = getLocationCalculation(longitude);
                                        System.out.println("mStrLatitudePost=" + mStrLat);
                                        System.out.println("mStrLongitudePost=" + mStrLong);
                                        if (mStrLat.equalsIgnoreCase("0") && mStrLong.equalsIgnoreCase("0")) {
                                            mUtility.errorDialog("Gps positional data not found", 0);
                                        } else {
                                            mUtility.errorDialogWithYesNoCallBack("Device Location", "Do you want to view requested device location?", "View", "Cancel", true, 0, new onAlertDialogCallBack() {
                                                @Override
                                                public void PositiveMethod(DialogInterface dialog, int id) {
                                                    Intent mIntent = new Intent(MainActivity.this, MapsActivity.class);
                                                    mIntent.putExtra("mIntent_latitude_1", mStrLat);
                                                    mIntent.putExtra("mIntent_longitude_1", mStrLong);
                                                    mIntent.putExtra("mIntent_latitude_2", "0");
                                                    mIntent.putExtra("mIntent_longitude_2", "0");
                                                    mIntent.putExtra("mIntent_location_1_title", mStringDeviceName + "_" + mStrBleAddress);
                                                    mIntent.putExtra("mIntent_location_2_title", "");
                                                    mIntent.putExtra("mIntent_is_single_location", true);
                                                    startActivity(mIntent);

                                                }

                                                @Override
                                                public void NegativeMethod(DialogInterface dialog, int id) {
                                                }
                                            });
                                        }
                                        return;
                                    }
                                }
                                if (mIntNotificationType == 1) {
//                                    /* Skip the first packet or retrieve the bd address and no of packets if required*/
                                    if (mStringReceivedMsg.startsWith("FFFFFFFD")) {
                                        mIntPacketReceivedLength = BLEUtility.hexToDecimal(mStringReceivedMsg.substring(8, 12));    //packet length
                                        mStrBleAddress = mStringReceivedMsg.substring(12, 24);    //BLE Address
                                        intPacketNo = 0;
                                        intPriviousPacketNo = 0;
                                        isDataAlreadyAvailable = false;
                                        mNextDecodeType = 0;
                                        mDecodeIndex = 0;
//                                        full_utc_time = "";
//                                        utc_time = "";
//                                        latitudeFull = "";
//                                        tempLatitude = "";
//                                        longitudeFull = "";
//                                        tempLongitude = "";
//                                        stat_time = "";
//                                        mov_time = "";

                                        if (mIntPacketReceivedLength == 0) {
                                            mUtility.errorDialogWithCallBack("Device data not available.", 0, false, new onAlertDialogCallBack() {
                                                @Override
                                                public void PositiveMethod(DialogInterface dialog, int id) {
                                                }

                                                @Override
                                                public void NegativeMethod(DialogInterface dialog, int id) {
                                                }
                                            });
                                        }
                                        setCommandDataAckSignal(mByteAckCommand, (short) intPacketNo);
                                        return;
                                    }

                                    if (!mStringReceivedMsg.startsWith("FFFFFFFD")) {
                                        if (mStringReceivedMsg.length() > 4) {
                                            intPacketNo = BLEUtility.hexToDecimal(mStringReceivedMsg.substring(0, 4));
//                                            System.out.println("JD-mStringReceivedMsgFinal-1-" + mStringReceivedMsg);
                                        }
                                    }
                                    if (intPacketNo == 1) {
                                        mIsDeviceSyncStart = true;
                                        showProgress("Please wait until fetch device data", true);
                                    }
                                    if (intPacketNo >= mIntPacketReceivedLength) {
                                        mIsDeviceSyncStart = false;
                                        hideProgress();
                                        mAppRoomDatabase.destroyDatabaseInstance();
                                        mAppRoomDatabase = AppRoomDatabase.getDatabaseInstance(MainActivity.this);
                                        if (intPacketNo == mIntPacketReceivedLength) {
                                            if (!isFinishing()) {
                                                mUtility.errorDialogWithCallBack("Device data fetched successfully.", 0, false, new onAlertDialogCallBack() {
                                                    @Override
                                                    public void PositiveMethod(DialogInterface dialog, int id) {
                                                    }

                                                    @Override
                                                    public void NegativeMethod(DialogInterface dialog, int id) {
                                                    }
                                                });
                                            }
                                        }

                                    }
                                    if (intPriviousPacketNo == intPacketNo) {
                                        return;
                                    }
                                    try {
                                        mUtility.updateProgressCount((intPacketNo * 100) / mIntPacketReceivedLength);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    intPriviousPacketNo = intPacketNo;
                                    mStringReceivedMsg = mStringReceivedMsg.substring(4, mStringReceivedMsg.length());
                                    mIntPackageLength = mStringReceivedMsg.length();
                                    mStringReceivedMsgProcess = mStringReceivedMsg;
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            index = 0;
                                            pressure = "";
                                            temp = "";
                                            // System.out.println("JD-mStrBleAddress-" + mStrBleAddress);
//                                            System.out.println("JD-mIntPacketReceivedLength-" + mIntPacketReceivedLength);
                                            System.out.println("JD-intPacketNo-" + intPacketNo);
//                                            System.out.println("JD-full_utc_time-" + full_utc_time);
                                            // System.out.println("JD-latitudeFull-" + latitudeFull);
                                            // System.out.println("JD-longitudeFull-" + longitudeFull);
//                                            System.out.println("JD-stat_interval-" + stat_time);
                                            // System.out.println("JD-moving_interval-" + mov_time);
                                            while (mIntPackageLength > 0) {

                                                /* Skip the first packet or retrieve the bd address and no of packets if required*/
                                                data_present = false;
                                                index = decodeHeader(mStringReceivedMsgProcess, index);

                                                if (mIntPackageLength >= 4 && mNextDecodeType == 0) {
                                                    pressure = mStringReceivedMsgProcess.substring(index, (index + 4));
                                                    index += 4;
                                                    mIntPackageLength -= 4;
                                                    mNextDecodeType = 1;
                                                    mStoredPressure = pressure;
                                                }
                                                index = decodeHeader(mStringReceivedMsgProcess, index);
                                                if (mIntPackageLength >= 4 && mNextDecodeType == 1) {
                                                    temp = mStringReceivedMsgProcess.substring(index, (index + 4));
                                                    index += 4;
                                                    mIntPackageLength -= 4;
                                                    data_present = true;
                                                    mNextDecodeType = 0;
                                                    if (pressure == "")
                                                        pressure = mStoredPressure;
                                                }
                                                if (isDataAlreadyAvailable) {
                                                    System.out.println("JD-DATA SKIPP");
                                                } else {
                                                    if (data_present == true) {
//                                                    System.out.println("JD-pressure-" + pressure);
//                                                    System.out.println("JD-temp-" + temp);

                                                        mTablePressureTemperature = new TablePressureTemperature();
                                                        mTablePressureTemperature.setDiveIdFk((int) mLongDiveId);
                                                        mTablePressureTemperature.setPressure(BLEUtility.hexToDecimal(pressure) + "");
                                                        mTablePressureTemperature.setTemperature("0");
                                                        mTablePressureTemperature.setPackets(mStringReceivedMsgFull);
                                                        mTablePressureTemperature.setPressure_depth("0");
                                                        mTablePressureTemperature.setTemperature_far("0");
                                                        try {
                                                            tempCal = BLEUtility.hexToSignedDecimal(temp);
                                                            tempCal = tempCal / 100;
                                                            mTablePressureTemperature.setTemperature(tempCal + "");

                                                            pressCal = (double) BLEUtility.hexToDecimal(pressure);
                                                            pressCal = (pressCal - 1013) / 100;
                                                            mTablePressureTemperature.setPressure_depth(pressCal + "");

                                                            tempCal = ((tempCal * 1.8) + 32);
                                                            mTablePressureTemperature.setTemperature_far(tempCal + "");
                                                            full_utc_time = (Long.parseLong(full_utc_time) + (BLEUtility.hexToDecimal(stat_time) * 1000)) + "";
                                                            mTablePressureTemperature.setUtcTime(Long.parseLong(full_utc_time));
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                        mTablePressureTemperature.setCreatedAt((System.currentTimeMillis()));
                                                        mTablePressureTemperature.setUpdatedAt((System.currentTimeMillis()));
                                                        mAppRoomDatabase.tempPressDao().insert(mTablePressureTemperature);
                                                    }
                                                }
                                            }
                                            setCommandDataAckSignal(mByteAckCommand, (short) intPacketNo);

                                        }
                                    });
                                } else if (mIntNotificationType == 2) {
                                    if (mIntGetSettingType == 0) {
                                        try {
                                            int auth_key = BLEUtility.hexToDecimal(mStringReceivedMsg);
                                            System.out.println(TAG + "JDD-mStringReceivedMsgAuthKey-" + auth_key);
                                            final int auth_key_calculate = ((((auth_key * 23) + 3896) * 27) - (42 * auth_key + 3129));

                                            short mByteCommand = (short) 0x0008;
                                            setCommandData(mByteCommand);
                                            Timer outerTimer = new Timer();
                                            outerTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    setAuthResponseAck(auth_key_calculate);
                                                    System.out.println(TAG + "JDD-mStringReceivedMsgGenKey-" + auth_key_calculate);
                                                    Timer innerTimer = new Timer();
                                                    innerTimer.schedule(new TimerTask() {
                                                        @Override
                                                        public void run() {
                                                            setCurrentUTCTime();
                                                            Timer innerTimerName = new Timer();
                                                            innerTimerName.schedule(new TimerTask() {
                                                                @Override
                                                                public void run() {
                                                                    System.out.println(TAG + "JDD-StartSync-" + auth_key_calculate);
                                                                    isServiceDiscovered = true;
                                                                    if (mStringConnectedDevicesAddress != null && !mStringConnectedDevicesAddress.equalsIgnoreCase("")) {
                                                                        new DeviceNameList(mStringConnectedDevicesAddress).execute();
                                                                    }
                                                                }
                                                            }, 500);
                                                        }
                                                    }, 500);
                                                }
                                            }, 500);

                                        } catch (Exception e) {
                                            hideProgress();
                                            e.printStackTrace();
                                        }
                                    } else if (mIntGetSettingType == 1) {
                                        mStringDeviceMemory = BLEUtility.hexToDecimal(mStringReceivedMsg) + "";
//                                        System.out.println("mStringDeviceMemory-" + mStringDeviceMemory);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceMemoryChange(mStringDeviceMemory);
                                        }
                                    } else if (mIntGetSettingType == 2) {
                                        mStringDeviceBattery = BLEUtility.hexToDecimal(mStringReceivedMsg) + "";
//                                        System.out.println("mStringDeviceBattery-" + mStringDeviceBattery);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceBatteryChange(mStringDeviceBattery);
                                        }
                                    } else if (mIntGetSettingType == 3) {
                                        // Firmware Version in Hex
                                        mStringDeviceFirmwareVersion = mStringReceivedMsg + "";
//                                        System.out.println("mStringDeviceFirmwareVersion-" + mStringDeviceFirmwareVersion);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceFwChange(mStringDeviceFirmwareVersion);
                                        }
                                    } else if (mIntGetSettingType == 4) {
                                        long currentDateTime = new BigInteger(mStringReceivedMsg, 16).longValue();
                                        //creating Date from millisecond
                                        Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                                        aGMTCalendar.setTimeInMillis(currentDateTime * 1000);
//                                    Date currentDate = new Date(currentDateTime * 1000);
                                        mStringDeviceTime = aGMTCalendar.getTimeInMillis() + "";
//                                        System.out.println("JD-UTC-TIME-" + aGMTCalendar.getTimeInMillis());
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceTimeChange(mStringDeviceTime);
                                        }
                                    } else if (mIntGetSettingType == 5) {
                                        mStringDeviceStationaryInterval = BLEUtility.hexToDecimal(mStringReceivedMsg) + "";
//                                        System.out.println("mStringDeviceStationaryInterval-" + mStringDeviceStationaryInterval);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceStationaryIntervalChange(mStringDeviceStationaryInterval);
                                        }
                                    } else if (mIntGetSettingType == 6) {
                                        mStringDeviceReadingDepthCutoff = BLEUtility.hexToDecimal(mStringReceivedMsg) + "";
                                        System.out.println("JD-SETTING--DepthCutoff" + mStringDeviceReadingDepthCutoff);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceDepthCutOffChange(mStringDeviceReadingDepthCutoff);
                                        }
                                    } else if (mIntGetSettingType == 7) {
                                        // Ble Transmission in Hex
                                        mStringDeviceBleTransmission = mStringReceivedMsg + "";
                                        System.out.println("JD-SETTING--BleTransmission-" + mStringDeviceBleTransmission);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceBleTransmissionChange(mStringDeviceBleTransmission);
                                        }
                                    } else if (mIntGetSettingType == 8) {
                                        mStringGpsInterval = BLEUtility.hexToDecimal(mStringReceivedMsg) + "";
                                        System.out.println("JD-SETTING--GpsInterval-" + mStringGpsInterval);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceGpsIntervalChange(mStringGpsInterval);
                                        }
                                    } else if (mIntGetSettingType == 9) {
                                        mStringGpsTimeout = BLEUtility.hexToDecimal(mStringReceivedMsg) + "";
                                        System.out.println("JD-SETTING--GpsTimeout-" + mStringGpsTimeout);
                                        if (mOnDeviceSettingChange != null) {
                                            mOnDeviceSettingChange.onDeviceGpsTimeoutChange(mStringGpsTimeout);
                                        }
                                    }

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                hideProgress();
                            }
                        }
                    }
                } else if (action.equalsIgnoreCase(BluetoothLeService.RSSI_DATA)) {
//                    System.out.println(TAG + "--rssi data " + mBluetoothLeService.updateRSSI);
                } else if (action.equalsIgnoreCase(BluetoothLeService.ERORR)) {
                    System.out.println(TAG + "-----ERROR Device " + action);
//                    stopScan();
                    mLeDevices = new ArrayList<>();
                    RescanDevice(true);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    TableDive mTableDive;

    /*Decode Header data*/
    private int decodeHeader(String strArray, int index) {
        // Dive Identifier
        if (mDecodeIndex == 0 && strArray.startsWith("FFFF", index) && mIntPackageLength >= 4) {
            mIntPackageLength -= 4;
            mDecodeIndex = 1;
            index += 4;
        }
        if (mDecodeIndex == 1 && strArray.startsWith("FFFE", index) && mIntPackageLength >= 4) {
            mIntPackageLength -= 4;
            mDecodeIndex = 2;
            index += 4;
            mStringReceivedMsgFullTemp = mStringReceivedMsgFull;
        }
//        // Mission ID
//        if (mDecodeIndex == 2 && mIntPackageLength >= 4) {
//            tempMissionId = strArray.substring(index, index + 4);
//            missionId = tempMissionId;
//            mIntPackageLength -= 4;
//            mDecodeIndex = 3;
//            index += 4;
//        }
//        if (mDecodeIndex == 3 && mIntPackageLength >= 4) {
//            tempMissionId = strArray.substring(index, index + 4);
//            missionId += tempMissionId;
//            mIntPackageLength -= 4;
//            mDecodeIndex = 4;
//            index += 4;
//        }
        /* Retrieve the UTC time here 4 bytes */
        if (mDecodeIndex == 2 && mIntPackageLength >= 4) {
            utc_time = strArray.substring(index, index + 4);
            full_utc_time = utc_time;
            mIntPackageLength -= 4;
            mDecodeIndex = 3;
            index += 4;
        }
        if (mDecodeIndex == 3 && mIntPackageLength >= 4) {
            utc_time = strArray.substring(index, index + 4);
            full_utc_time += utc_time;
            long currentDateTime = new BigInteger(full_utc_time, 16).longValue();
            //creating Date from millisecond
            Date currentDate = new Date(currentDateTime * 1000);
            full_utc_time = currentDate.getTime() + "";
            mIntPackageLength -= 4;
            mDecodeIndex = 4;
            index += 4;
        }

        /* Retrieve 8 bytes of GPS information here */
        // 4 bytes Latitude
        if (mDecodeIndex == 4 && mIntPackageLength >= 4) {
            tempLatitude = strArray.substring(index, index + 4);
            latitudeFull = tempLatitude;
            mIntPackageLength -= 4;
            mDecodeIndex = 5;
            index += 4;
        }
        if (mDecodeIndex == 5 && mIntPackageLength >= 4) {
            tempLatitude = strArray.substring(index, index + 4);
            latitudeFull += tempLatitude;
            mIntPackageLength -= 4;
            mDecodeIndex = 6;
            index += 4;
        }
        // 4 bytes Longitude
        if (mDecodeIndex == 6 && mIntPackageLength >= 4) {
            tempLongitude = strArray.substring(index, index + 4);
            longitudeFull = tempLongitude;
            mIntPackageLength -= 4;
            mDecodeIndex = 7;
            index += 4;
        }
        if (mDecodeIndex == 7 && mIntPackageLength >= 4) {
            tempLongitude = strArray.substring(index, index + 4);
            longitudeFull += tempLongitude;
            mIntPackageLength -= 4;
            mDecodeIndex = 8;
            index += 4;

        }

        /* Retrieve stationary measurement interval */
        if (mDecodeIndex == 8 && mIntPackageLength >= 4) {
            stat_time = strArray.substring(index, index + 4);
            mIntPackageLength -= 4;
            mDecodeIndex = 9;
            index += 4;
        }
        /* Retrieve Moving measurement interval */
        if (mDecodeIndex == 9 && mIntPackageLength >= 4) {
            mov_time = strArray.substring(index, index + 4);
            mIntPackageLength -= 4;
            mDecodeIndex = 0;
            index += 4;

            TableBleDevice mTableBleDevice = mAppRoomDatabase.bleDeviceDao().getDeviceName(mStringConnectedDevicesAddress);
            if (mTableBleDevice == null) {
                mStringDeviceName = "DepthNTemp1";
            } else {
                mStringDeviceName = mTableBleDevice.getDeviceName();
            }
            mTableDive = new TableDive();
            mTableDive.setDeviceName(mStringDeviceName);
            mTableDive.setBleAddress(mStrBleAddress);
            mTableDive.setDiveNo(1);
            int latitude = (int) Long.parseLong(latitudeFull, 16);
            int longitude = (int) Long.parseLong(longitudeFull, 16);
//            long latitude = new BigInteger(latitudeFull, 16).longValue();
//            long longitude = new BigInteger(longitudeFull, 16).longValue();
            latitudeFull = latitude + "";
            longitudeFull = longitude + "";
//            if (latitudeFull.length() > 2) {
//                latitudeFull = latitudeFull.substring(0, 2) + "." + latitudeFull.substring(2, latitudeFull.length());
//            }
//            if (longitudeFull.length() > 2) {
//                longitudeFull = longitudeFull.substring(0, 2) + "." + longitudeFull.substring(2, longitudeFull.length());
//            }
            mTableDive.setGpsLatitude(latitudeFull);
            mTableDive.setGpsLongitude(longitudeFull);
            mTableDive.setUtcTime(Long.parseLong(full_utc_time));
            mTableDive.setStationaryInterval(BLEUtility.hexToDecimal(stat_time));
            mTableDive.setMovingInterval(BLEUtility.hexToDecimal(mov_time));
            mTableDive.setPackets(mStringReceivedMsgFullTemp);
            mTableDive.setCreatedAt(System.currentTimeMillis());
            mTableDive.setUpdatedAt(System.currentTimeMillis());
            System.out.println("JD-mStrBleAddress-" + mStrBleAddress);
            System.out.println("JD-full_utc_time-" + full_utc_time);
            TableDive mDepthCutOffCheck = mAppRoomDatabase.diveDao().checkDiveRecordIsExistOrNot(mStrBleAddress, full_utc_time);
            if (mDepthCutOffCheck == null) {
                mLongDiveId = mAppRoomDatabase.diveDao().insert(mTableDive);
                isDataAlreadyAvailable = false;
            } else {
                mLongDiveId = mDepthCutOffCheck.getDiveId();
//                mAppRoomDatabase.diveDao().deleteDiveById(mDepthCutOffCheck.getDiveId());
//                mAppRoomDatabase.tempPressDao().deleteTempPressDiveByDiveId(mDepthCutOffCheck.getDiveId());
//                mLongDiveId = mAppRoomDatabase.diveDao().insert(mTableDive);
                isDataAlreadyAvailable = true;
            }
        }
        return index;
    }

    /*Calculate GPS location*/
    private String getLocationCalculation(int latitudeLongitude) {
        String latLong = "0";
        try {
            int mIntLocationPrefix = latitudeLongitude / 1000000;
            int mIntLocationPostfix = latitudeLongitude % 1000000;
            System.out.println("mIntLocationPrefix=" + mIntLocationPrefix);
            System.out.println("mIntLocationPostfix=" + mIntLocationPostfix);
            double mLongPostfix = Double.parseDouble(mIntLocationPostfix + "") / 600000;
            System.out.println("mLongPostfix=" + mLongPostfix);
            return new DecimalFormat("##.######").format((mIntLocationPrefix + mLongPostfix));
        } catch (Exception e) {
            e.printStackTrace();
            return latLong;
        }
    }

    /*Device name list adapter*/
    public class DeviceNameList extends AsyncTask<String, Integer, TableBleDevice> {
        String bleAddress;
        boolean isRecordFound = false;

        public DeviceNameList(String deviceBleAddress) {
            bleAddress = deviceBleAddress;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected TableBleDevice doInBackground(String... params) {
            System.out.println("JD-BLE Address-" + bleAddress);
            TableBleDevice mBleDevice = mAppRoomDatabase.bleDeviceDao().checkDeviceIsExistOrNot(bleAddress);
            if (mBleDevice != null) {
                System.out.println("JD-BLE Address-" + mBleDevice.getBleAddress());
                isRecordFound = true;
            } else {
                isRecordFound = false;
                System.out.println("JD-DATA-NULL");
                mBleDevice = new TableBleDevice();
            }
            return mBleDevice;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(TableBleDevice mBleDevice) {
            super.onPostExecute(mBleDevice);
            hideProgress();
            if (isRecordFound) {
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getIsAutoSync()) {
                    if (isDevicesConnected) {
                        short mByteGetCommand = (short) 0xFF0C;
                        setCommandData(mByteGetCommand);
                    } else {
                        showDisconnectedDeviceAlert();
                    }
                }
            } else {
                openDeviceNameDialog(bleAddress);
            }
        }
    }

    boolean isServiceDiscovered = true;

    private void displayGattServices(List<BluetoothGattService> gattServices, final String mStringAddress, String mStringDevicesName) {
        if (gattServices == null)
            return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                if (gattCharacteristic != null) {
                    final int charaProp = gattCharacteristic.getProperties();
//                    System.out.println(TAG + "--UUID " + gattCharacteristic.getUuid().toString());
                    if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                            (SampleGattAttributes.UUID_DNT_GET_DATA.equals(gattCharacteristic.getUuid()))) {
                        mBluetoothLeService.setGetDataChar(gattCharacteristic);
//                        mBluetoothLeService.readCharacteristic(gattCharacteristic);
                        mBluetoothLeService.setCharacteristicNotifications(gattCharacteristic, true);
                    } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                            (SampleGattAttributes.UUID_DNT_SET_SETTING_COMMAND_DATA.equals(gattCharacteristic.getUuid()))) {
                        if (isServiceDiscovered) {
                            isServiceDiscovered = false;
                            final BluetoothGattCharacteristic gattCharacteristicTemp = gattCharacteristic;
                            Timer outerTimer = new Timer();
                            outerTimer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    mBluetoothLeService.setSettingSetCommandChar(gattCharacteristicTemp);
                                    mBluetoothLeService.readCharacteristic(gattCharacteristicTemp);
                                    mBluetoothLeService.setCharacteristicNotifications(gattCharacteristicTemp, true);
                                    Timer innerTimerUTC = new Timer();
                                    innerTimerUTC.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            mIntGetSettingType = 0;
                                            short mByteGetAuthCommand = (short) 0xFF08;
                                            setCommandData(mByteGetAuthCommand);
//                                            setCurrentUTCTime();
//                                            Timer innerTimer = new Timer();
//                                            innerTimer.schedule(new TimerTask() {
//                                                @Override
//                                                public void run() {
//                                                    isServiceDis34covered = true;
//                                                    if (mStringConnectedDevicesAddress != null && !mStringConnectedDevicesAddress.equalsIgnoreCase("")) {
//                                                        new DeviceNameList(mStringConnectedDevicesAddress).execute();
//                                                    }
//                                                }
//                                            }, 500);
                                        }
                                    }, 500);
                                }
                            }, 500);
                        }
                    } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                            (SampleGattAttributes.UUID_DNT_GET_SETTING_DATA.equals(gattCharacteristic.getUuid()))) {
                        mBluetoothLeService.setSettingGetDataChar(gattCharacteristic);
                    }
                }
            }
        }
    }

    /*Connect Device*/
    public void ConnectDevices(final BluetoothDevice mConnectBluetoothDevice, final boolean isAutoConnect) {
        System.out.println(TAG + "--Requesting-");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothLeService != null) {
                    System.out.println(TAG + "--Connecting-");
//                    stopScan(false);
                    mBluetoothDevice = mConnectBluetoothDevice;
                    mBluetoothLeService.connect(mConnectBluetoothDevice);

                }
            }
        });

    }

    /*Disconnect device*/
    public void disconnectDevices(final BluetoothDevice mBluetoothDevice, final boolean isStopScanComplete) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                stopScan(isStopScanComplete);
                mBluetoothLeService.removeDevices(mBluetoothDevice);
            }
        });
    }

    /*Rescan device*/
    public void RescanDevice(boolean isNeedToStopScan) {
        if (isNeedToStopScan) {
//            if (mAutoStopDeviceScanTimer != null) {
//                mAutoStopDeviceScanTimer.cancel();
//            }
//            mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer(autoStopScanTime, 1000);
//            mAutoStopDeviceScanTimer.start();
//            stopScan();
        }
//        mLeDevices = new ArrayList<>();
//                        mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer(autoStopScanTime, 1000);
//                        mAutoStopDeviceScanTimer.start();
//        startDeviceScan();
//
//        Timer innerTimer = new Timer();
//        innerTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mLeDevicesTemp = new ArrayList<>();
//                        mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer(autoStopScanTime, 1000);
//                        mAutoStopDeviceScanTimer.start();
//                        startDeviceScan();
//                    }
//                });
//            }
//        }, 200);
//        Handler handler = new Handler();
//        Runnable r = new Runnable() {
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//
//                    }
//                });
//            }
//        };
//        handler.postDelayed(r, 200);
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
//            }
//        };
//        thread.start();
    }

    /*Show Pregress*/
    public void showProgress(final String mStringProgressTitle, boolean isShowCount) {
        if (!isFinishing()) {
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (!isFinishing()) {
//                        mUtility.ShowProgress(mStringProgressTitle);
//                    }
//                }
//            });
            mUtility.ShowProgress(mStringProgressTitle, isShowCount);
        }
//        mRelativeLayoutProgress.setVisibility(View.GONE);
//        if (isShowTitle) {
//            mTextViewProgressTitle.setVisibility(View.VISIBLE);
//            mTextViewProgressTitle.setText(mStringProgressTitle);
//        } else {
//            mTextViewProgressTitle.setVisibility(View.GONE);
//        }
    }

    /*Hide Progress*/
    public void hideProgress() {
        mUtility.HideProgress();
//        mRelativeLayoutProgress.setVisibility(View.GONE);
    }

    public static boolean getIsSDKAbove21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public boolean getIsDeviceSupportedAdvertisment() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public boolean getScanning() {
        return mIsScaning;
    }

    /*Start Device Scan*/
    @SuppressLint("InlinedApi")
    public void startDeviceScan() {
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//
//                    }
//                });
//            }
//        };
//        thread.start();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {

                System.out.println(TAG + "startDeviceScan status " + getScanning());
                if (getIsSDKAbove21()) {
                    //            if (getScanning()) {
                    //                System.out.println(TAG + "-----already scaning isSDK > 17 ");
                    //                return;
                    //            }
                    if (mBluetoothAdapter == null) {
                        System.out.println(TAG + "-----mBtAdapter null isSDK > 17 ");
                        return;
                    }
                    mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    mBluetoothAdapter = mBluetoothManager.getAdapter();
                    bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    scanCallback = new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, final ScanResult result) {
                            if (result.getScanRecord() != null) {
                                final BluetoothDevice newDeivce = result.getDevice();
                                if ((newDeivce == null)) {
                                    return;
                                }
                                onScanResultGet(newDeivce, result.getScanRecord().getBytes());
                            }
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            super.onScanFailed(errorCode);
                            System.out.println("--------------FAILE SCANNN-------" + errorCode);
                            if (isBLEFailToSCan) {
                                BluetoothAdapter.getDefaultAdapter().disable();
                            }
                            if (isBLEFailToSCan) {
                                Timer innerTimer = new Timer();
                                innerTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        BluetoothAdapter.getDefaultAdapter().enable();
                                        isBLEFailToSCan = true;
                                    }
                                }, 1000);
                            }
                        }
                    };
                    ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
                    ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
                    settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
                    List<ScanFilter> filters = new ArrayList<ScanFilter>();
                    filters.add(filterBuilder.build());
                    if (bluetoothLeScanner != null) {
                        bluetoothLeScanner.startScan(filters, settingsBuilder.build(), scanCallback);
                        mIsScaning = true;
                    }
                } else {
                    //            if (getScanning()) {
                    //                System.out.println(TAG + "-----already scaning isSDK < 17 ");
                    //                return;
                    //            }
                    if (mBluetoothAdapter == null) {
                        System.out.println(TAG + "-----already scaning isSDK < 17 ");
                        return;
                    }
                    leScanCallback = new BluetoothAdapter.LeScanCallback() {
                        public void onLeScan(final BluetoothDevice newDeivce, final int newRssi, final byte[] newScanRecord) {
                            try {
                                if ((newDeivce == null)) {
                                    return;
                                }
                                onScanResultGet(newDeivce, newScanRecord);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    };
                    mBluetoothAdapter.startLeScan(leScanCallback);
                    mIsScaning = true;
                }
            }
        });

    }

    VoBluetoothDevices mVoBluetoothDevices;
    String mStringEncScanHexData = "";
    String mStringDecHexData = "";

    /*Handle scan device data result*/
    private void onScanResultGet(BluetoothDevice newDevice, byte[] newScanRecord) {
//                                System.out.println(TAG + "----scan ScanRecord " + result.getScanRecord().toString());
        if (newScanRecord != null) {
            try {
                if (mEncryption != null) {
                    mStringEncScanHexData = BLEUtility.toHexString(newScanRecord, true);
//            System.out.println("JD-mStringHexData--" + mStringHexData);
//            System.out.println("JD-DEVICE NAME--" + newDevice.getName());
                    if (mStringEncScanHexData != null && !mStringEncScanHexData.equalsIgnoreCase("")) {
//                if (mStringHexData.startsWith("020104")) {
                        if (newDevice != null) {
                            if (newDevice.getName() != null) {
                                if (newDevice.getName().contains("Succorfish D&T")) {
                                    if (mStringEncScanHexData.contains("FF5900")) {
                                        mStringEncScanHexData = mStringEncScanHexData.substring((mStringEncScanHexData.indexOf("FF5900") + 6), ((mStringEncScanHexData.indexOf("FF5900") + 6) + 32));
                                        mStringDecHexData = BLEUtility.toHexString(getAesDecByteArray(BLEUtility.hexStringToBytes(mStringEncScanHexData)), true);
//                                        System.out.println("mStringDecHexData=" + mStringDecHexData);
                                        boolean containsInScanDevice = false;
                                        for (VoBluetoothDevices device : mLeDevices) {
                                            if (newDevice.getAddress().equals(device.getDeviceIEEE())) {
                                                containsInScanDevice = true;
                                                break;
                                            }
                                        }
                                        if (!containsInScanDevice) {
                                            mVoBluetoothDevices = new VoBluetoothDevices();
                                            mVoBluetoothDevices.setBluetoothDevice(newDevice);
                                            mVoBluetoothDevices.setIsConnected(false);
                                            mVoBluetoothDevices.setDeviceName(newDevice.getName());
                                            mVoBluetoothDevices.setDeviceAddress(newDevice.getAddress());
                                            mVoBluetoothDevices.setDeviceIEEE(newDevice.getAddress());
                                            mVoBluetoothDevices.setDeviceHexData(mStringDecHexData);
                                            if (newDevice.getAddress().equalsIgnoreCase(mStringConnectedDevicesAddress)) {
                                                System.out.println("CONNECTEDD");
                                                if (isDevicesConnected) {
                                                    mVoBluetoothDevices.setIsConnected(true);
                                                }
                                            } else {
                                                mVoBluetoothDevices.setIsConnected(false);
                                            }
                                            mLeDevices.add(mVoBluetoothDevices);
                                            if (mOnDeviceConnectionStatusChange != null) {
                                                mOnDeviceConnectionStatusChange.addScanDevices();
                                                mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothDevices);
                                            }
                                        }
                                    }
                                }
                            }
                        }
//                }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(TAG + "--byte[] data-null");
        }
//        System.out.println("DeviceListSize-" + mLeDevices.size() + "--DeviceListTempSize-" + mLeDevicesTemp.size());
    }

    /*Stop Scan*/
    public void stopScan(boolean isFullScanStop) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                System.out.println(TAG + "-------Scan stopped.....");
                if (getIsSDKAbove21()) {
                    mIsScaning = false;
                    if (bluetoothLeScanner != null && mBluetoothAdapter.isEnabled() &&
                            mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                        System.out.println(TAG + "-------Scan STOPPPPPPP.....");
                        if (scanCallback != null) {
                            bluetoothLeScanner.stopScan(scanCallback);
                        }
                    }
                } else {
                    if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() &&
                            mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                        mIsScaning = false;
                        if (scanCallback != null) {
                            mBluetoothAdapter.stopLeScan(leScanCallback);
                        }
                    }
                }
            }
        });
        if (!isFullScanStop) {
//        if (isFromBridgeConnection) {
            Timer mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    startDeviceScan();
                }
            }, 5000);
//        }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    /*Show device disconnect device*/
    public void showDisconnectedDeviceAlert() {
        mUtility.errorDialogWithCallBack("Device is disconnected. Please connect and try again.", 1, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    /*Send ble data to Bluetooth le service class*/
    public void sendMessageOverBleOrAdvertise(byte[] mByteArrayFullPackets, String serviceType) {
        if (isDevicesConnected) {
            mBluetoothLeService.transmitMessageToDevice(mByteArrayFullPackets, serviceType);
        } else {
//            showDisconnectedDeviceAlert();
        }
    }

    /*Encryption*/
    public byte[] getAesEncByteArray(byte[] bytePacket) throws Exception {
        return mEncryption.encrypt(bytePacket);
    }

    /*Decryption*/
    public byte[] getAesDecByteArray(byte[] bytePacket) throws Exception {
        return mEncryption.decrypt(bytePacket);
    }

    /**
     * Writes the DNT Command data on the target device
     *
     * @param commandValue write the command value.
     */
    public void setCommandDataAckSignal(short commandValue, int value) {
        byte s_value[] = new byte[4];
        s_value[1] = (byte) (commandValue & 0xFF);
        s_value[0] = (byte) ((commandValue >> 8) & 0xFF);
        s_value[3] = (byte) (value & 0xFF);
        s_value[2] = (byte) ((value >> 8) & 0xFF);
        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_COMMAND);
        s_value = null;
    }

    /**
     * Writes the DNT Command data on the target device
     *
     * @param commandValue write the command value.
     */
    public void setCommandData(short commandValue) {
        byte s_value[] = new byte[2];
        s_value[1] = (byte) (commandValue & 0xFF);
        s_value[0] = (byte) ((commandValue >> 8) & 0xFF);
        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_COMMAND);
        s_value = null;
    }

    /**
     * Writes the Millibar reading depth cutoff to be used on the target device
     *
     * @param value write the Millibar value.
     */
    public void setCommandBleTransmissionData(short commandValue, int value) {
        System.out.println("value-" + value);
        byte s_value[] = new byte[3];
        s_value[1] = (byte) (commandValue & 0xFF);
        s_value[0] = (byte) ((commandValue >> 8) & 0xFF);
        s_value[2] = (byte) (value & 0xFF);
//        s_value[2] = (byte) ((value >> 8) & 0xFF);

        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_COMMAND);
        s_value = null;
    }

    /**
     * Writes the Stationary Pressure interval to be used on the target device
     *
     * @param value write the Stationary Measurement Interval value.
     */
    public void setAuthResponseAck(int value) {
        byte s_value[] = new byte[4];
        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);
        s_value[2] = (byte) ((value >> 16) & 0xFF);
        s_value[3] = (byte) ((value >> 24) & 0xFF);
        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_DATA);
        s_value = null;
    }

    /**
     * Writes the Stationary Pressure interval to be used on the target device
     *
     * @param value write the Stationary Measurement Interval value.
     */
    public void setStatMeasInterval(int value) {
        byte s_value[] = new byte[2];
        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);

        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_DATA);
        s_value = null;
    }


    /**
     * Writes the Millibar reading depth cutoff to be used on the target device
     *
     * @param value write the Millibar value.
     */
    public void setBleTransmissionData(int value) {
        System.out.println("value-" + value);
        byte s_value[] = new byte[2];
        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);

        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_DATA);
        s_value = null;
    }

    public void setReadingDepth(int value) {
        System.out.println("JD-DEPTH-value-Reading Depth" + value);
        byte s_value[] = new byte[2];
        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);

        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_DATA);
        s_value = null;
    }

    public void setGpsIntervalData(int value) {
        System.out.println("JD-GpsInterval-" + value);
        byte s_value[] = new byte[2];
        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);
//        s_value[1] = (byte) (commandValue & 0xFF);
//        s_value[0] = (byte) ((commandValue >> 8) & 0xFF);
//        s_value[3] = (byte) (value & 0xFF);
//        s_value[2] = (byte) ((value >> 8) & 0xFF);
        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_DATA);
        s_value = null;
    }

    public void setGpsTimeoutData(int value) {
        System.out.println("JD-GpsInterval-" + value);
        byte s_value[] = new byte[2];
        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);

        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_DATA);
        s_value = null;
    }

    /**
     * Writes the Export mode to be used on the target device
     *
     * @param byteTimeStamp write the Moving Measurement Interval value.
     */
    public void setUTCTime(byte[] byteTimeStamp) {
        byte s_value[] = new byte[4];
        byte msg_index = 0, index;
//        s_value[0] = (byte) (byteTimeStamp & 0xFF);
//        s_value[1] = (byte) ((byteTimeStamp >> 8) & 0xFF);
//        s_value[2] = (byte) ((byteTimeStamp >> 16) & 0xFF);
//        s_value[3] = (byte) ((byteTimeStamp >> 24) & 0xFF);

        for (index = 0; index < byteTimeStamp.length; index++) {
            s_value[msg_index] = byteTimeStamp[index];
            msg_index++;
        }
        sendMessageOverBleOrAdvertise(s_value, URLCLASS.TYPE_DATA);
        s_value = null;
    }

    /*send current time*/
    public void setCurrentUTCTime() {
        short mByteCommand = (short) 0x0006;
        setCommandData(mByteCommand);
        Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        try {
            if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTimeUFC() == 0) {
                aGMTCalendar.add(Calendar.HOUR, -1);
            } else if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTimeUFC() == 1) {
                aGMTCalendar.add(Calendar.HOUR, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long currentTimeMillis = aGMTCalendar.getTimeInMillis() / 1000L;
        String hexTime = Long.toHexString(currentTimeMillis);
//                 Reverse the hex string.
        int mInt = (int) Long.parseLong(hexTime, 16);
        int mIntReverse = ((mInt >> 24) & 0xff) |       // byte 3 to byte 0
                ((mInt << 8) & 0xff0000) |    // byte 1 to byte 2
                ((mInt >> 8) & 0xff00) |      // byte 2 to byte 1
                ((mInt << 24) & 0xff000000);   // byte 0 to byte 3
        final String mStrReverseHex = Integer.toHexString(mIntReverse);
        System.out.println("UTC-currentTimeMillis-" + currentTimeMillis);
        Timer innerTimer = new Timer();
        innerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                setUTCTime(BLEUtility.hexStringToBytes(mStrReverseHex));
            }
        }, 200);

    }

    /**
     * Writes the DNT Start stop meas data on the target device
     *
     * @param value write the Frequency value.
     */
    public void startStopMeasData(byte value, String serviceType) {
        byte s_value[] = new byte[1];
        s_value[0] = value;
        sendMessageOverBleOrAdvertise(s_value, serviceType);
        s_value = null;
    }

    /**
     * Writes the DNT Start stop meas data on the target device
     *
     * @param value write the Frequency value.
     */
    public void setPressureMeasData(byte value, String serviceType) {
        byte s_value[] = new byte[1];
        s_value[0] = value;
        sendMessageOverBleOrAdvertise(s_value, serviceType);
        s_value = null;
    }


    /**
     * Writes the Moving Pressure interval to be used on the target device
     *
     * @param value write the Moving Measurement Interval value.
     */
    public void setMovingMeasInterval(int value, String serviceType) {
        byte s_value[] = new byte[4];

        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);
        s_value[2] = (byte) ((value >> 16) & 0xFF);
        s_value[3] = (byte) ((value >> 24) & 0xFF);

        sendMessageOverBleOrAdvertise(s_value, serviceType);
        s_value = null;
    }

    /**
     * Writes the Export mode to be used on the target device
     *
     * @param value write the Moving Measurement Interval value.
     */
    public void setExportMode(short value, String serviceType) {
        byte s_value[] = new byte[2];

        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);

        sendMessageOverBleOrAdvertise(s_value, serviceType);
        s_value = null;
    }


    /**
     * Writes the GPS Tracking interval to be used on the target device
     *
     * @param value write the GPS Tracking Interval value.
     */
    public void setGPSTrackingInterval(int value, String serviceType) {
        byte s_value[] = new byte[4];

        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);
        s_value[2] = (byte) ((value >> 16) & 0xFF);
        s_value[3] = (byte) ((value >> 24) & 0xFF);
        sendMessageOverBleOrAdvertise(s_value, serviceType);
        s_value = null;

    }

    /**
     * Writes the GPS BLE Cutoff to be used on the target device
     *
     * @param value write the Frequency value.
     */
    public void setGPSBLECutoffValue(short value, String serviceType) {
        byte s_value[] = new byte[2];

        s_value[0] = (byte) (value & 0xFF);
        s_value[1] = (byte) ((value >> 8) & 0xFF);
        sendMessageOverBleOrAdvertise(s_value, serviceType);
        s_value = null;
    }

    /*open device name dialog*/
    public void openDeviceNameDialog(final String mBleAddress) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(this);
        final View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
        android.support.v7.app.AlertDialog.Builder alertDialogBuilderUserInput = new android.support.v7.app.AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
        alertDialogBuilderUserInput.setView(mView);
        final EditText mEditTextMsg = (EditText) mView.findViewById(R.id.user_input_dialog_et_name);
//        TextView mTextViewTitle = (TextView) mView.findViewById(R.id.user_input_dialog_tv_title);
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here

                    }
                });
//                .setNegativeButton("Cancel",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialogBox, int id) {
//                                mUtility.hideKeyboard(MainActivity.this);
//                                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                                im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
//                                dialogBox.cancel();
//                            }
//                        });
        final android.support.v7.app.AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
        alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String mStringName = mEditTextMsg.getText().toString().trim();
                if (mStringName != null && !mStringName.equalsIgnoreCase("")) {
                    InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
                    alertDialogAndroid.dismiss();

                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            TableBleDevice mTableBleDevice = new TableBleDevice();
                            mTableBleDevice.setBleAddress(mBleAddress);
                            mTableBleDevice.setDeviceName(mStringName);
                            mTableBleDevice.setCreatedAt(System.currentTimeMillis() + "");
                            mTableBleDevice.setUpdatedAt(System.currentTimeMillis() + "");
                            mAppRoomDatabase.bleDeviceDao().insert(mTableBleDevice);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void agentsCount) {
                            if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getIsAutoSync()) {
                                if (isDevicesConnected) {
                                    short mByteGetCommand = (short) 0xFF0C;
                                    setCommandData(mByteGetCommand);
                                } else {
                                    showDisconnectedDeviceAlert();
                                }
                            }
                        }
                    }.execute();

                } else {
                    mUtility.hideKeyboard(MainActivity.this);
                    showMessageRedAlert(mView, "Please enter device name", getResources().getString(R.string.str_ok));
                }
            }
        });

    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mUtility.hideKeyboard(MainActivity.this);
        Snackbar mSnackBar = Snackbar.make(mView, mStringMessage, 5000);
        mSnackBar.setAction(mActionMessage, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mSnackBar.setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
        mSnackBar.getView().setBackgroundColor(getResources().getColor(R.color.colorInActiveMenu));
        mSnackBar.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.gc();
        System.out.println("JD-onResume");
        if (!isNeverAskPermissionCheck) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    System.out.println("JD-PERMISSION GRANTED");
                } else {
                    callMarshmallowPermission();
                    return;
                }
                // Marshmallow+ Permission APIs

//            boolean showRationale = shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION);
//            if (!showRationale) {
//
//            }
            }
        }
//        if (Build.VERSION.SDK_INT >= 23) {
//            LocationManager mLocationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
//                        !mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

//        }

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
//                BluetoothAdapter.getDefaultAdapter().enable();
//            }
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() == false) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, BLUETOOTH_ENABLE_REQT);
            } else {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (!isLocationEnabled(MainActivity.this)) {
                        System.out.println("OnResume-GPS");
                        mUtility.errorDialogWithYesNoCallBack("Turn on GPS Location.", "Since Android 6.0 the system requires access to device's location in order to scan bluetooth devices.", "Ok", "Cancel", false, 3, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                try {
                                    Intent mIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    startActivity(mIntent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {

                            }
                        });
                    }
                }
                Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }

//        if (!requestForPermission()) {
//            requestForPermission();
//        }

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBluetoothStateReceiver, filter);
            startDeviceScan();
            isReadyToUnbind = true;
        } else {
            Toast.makeText(this, "Mobile is not supported BLE features", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan(true);
//        if (mAutoStopDeviceScanTimer != null) {
//            mAutoStopDeviceScanTimer.cancel();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (isReadyToUnbind) {
                if (mServiceConnection != null) {
                    unbindService(mServiceConnection);
                }
                if (mGattUpdateReceiver != null) {
                    unregisterReceiver(mGattUpdateReceiver);
                }
                if (mBluetoothStateReceiver != null) {
                    unregisterReceiver(mBluetoothStateReceiver);
                }
                stopScan(true);
            }
            if (isDevicesConnected) {
                if (mBluetoothDevice != null) {
                    disconnectDevices(mBluetoothDevice, false);
                }
            }
        }
    }

//    private void checkRequiredPermission() {
//        if (Build.VERSION.SDK_INT >= 23) {
//            // Marshmallow+ Permission APIs
//            callMarshmallowPermission();
//        }
//        mLocationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
//        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
//            if (Build.VERSION.SDK_INT >= 23) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
//                builder.setTitle("Turn on GPS Location.");
//                builder.setMessage("Since Android 6.0 Marshmallow the system requires access to device's location in order to scan devices.");
//                builder.setCancelable(false);
//                builder.setPositiveButton("Allow", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
//                        try {
//                            Intent mIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                            startActivity(mIntent);
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                        }
//                    }
//                });
//                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int id) {
//                        dialog.dismiss();
//                    }
//                });
//                AlertDialog alert = builder.create();
//                alert.show();
//
//
//                return;
//            }
//        }
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(TAG + "-----requestCode " + requestCode);
        if (requestCode == BLUETOOTH_ENABLE_REQT) {
//            if (mAutoStopDeviceScanTimer != null) {
//                mAutoStopDeviceScanTimer.cancel();
//            }
//            mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer(autoStopScanTime, 1000);
//            mAutoStopDeviceScanTimer.start();
            stopScan(true);
            mLeDevices = new ArrayList<>();
            startDeviceScan();

        } else {
            try {
                if (resultCode != RESULT_CANCELED && data != null) {
                    System.out.println("--ActivityResult RESULTTT--");
//                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
//                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*Check required permission*/
    @TargetApi(Build.VERSION_CODES.M)
    private void callMarshmallowPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");
        if (permissionsList.size() > 0) {
//            if (permissionsNeeded.size() > 0) {
//                // Need Rationale
//                String message = "App need access to " + permissionsNeeded.get(0);
//                for (int i = 1; i < permissionsNeeded.size(); i++)
//                    message = message + ", " + permissionsNeeded.get(i);
//                showMessageOKCancel(message,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
//                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
//                            }
//                        });
//                return;
//            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                if (permissions.length == 0) {
                    return;
                }
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied
                    System.out.println("GrantedACCESS_FINE_LOCATION");
//                    if (mAutoStopDeviceScanTimer != null) {
//                        mAutoStopDeviceScanTimer.cancel();
//                    }
//                    mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer(autoStopScanTime, 1000);
//                    mAutoStopDeviceScanTimer.start();
                    mLeDevices = new ArrayList<>();
                    startDeviceScan();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                        //denied
                        System.out.println("JD-DENIED");
                        isNeverAskPermissionCheck = false;
                    } else {
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            isNeverAskPermissionCheck = false;
                            //allowed
                            System.out.println("JD-Granted ACCESS_FINE_LOCATION");
                            //                    if (mAutoStopDeviceScanTimer != null) {
//                        mAutoStopDeviceScanTimer.cancel();
//                    }
//                    mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer(autoStopScanTime, 1000);
//                    mAutoStopDeviceScanTimer.start();
                            mLeDevices = new ArrayList<>();
                            startDeviceScan();
                        } else {
                            //set to never ask again
                            System.out.println("JD-NEVER ASK permission");
                            isNeverAskPermissionCheck = true;
                        }
                    }
                }


            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    /*Init action bar*/
    private void initToolbar() {

        setSupportActionBar(mToolbar);
        View logo = getLayoutInflater().inflate(R.layout.custom_actionbar, null);
        mImageViewBack = (ImageView) logo.findViewById(R.id.custom_action_img_back);
        mImageViewAddDevice = (ImageView) logo.findViewById(R.id.custom_actionbar_iv_add);
        mTextViewTitle = (TextView) logo.findViewById(R.id.custom_action_txt_title);
        mTextViewAdd = (TextView) logo.findViewById(R.id.custom_action_txt_add);
        mTextViewAdd.setVisibility(View.GONE);
        mTextViewTitle.setText(R.string.app_name);
        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mToolbar.addView(logo);
    }

    public void removeAllFragmentFromBack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void removeNumberOfFragmnet(int num) {
        for (int i = 0; i < num; ++i) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /*replace fragment*/
    public void replacesFragment(Fragment mFragment, boolean isBackState, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        if (isBackState)
            fragmentTransaction.addToBackStack(null);

        if (mBundle != null)
            mFragment.setArguments(mBundle);
        fragmentTransaction.replace(R.id.activity_main_main_content_container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void replacesFragment(Fragment mOldFragment, Fragment mFragment, boolean isBackState, Bundle mBundle) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState)
            fragmentTransaction.addToBackStack(null);
        if (mBundle != null)
            mFragment.setArguments(mBundle);
        // fragmentTransaction.replace(R.id.activity_main_fragment_container,
        // mFragment);
        // fragmentTransaction.commitAllowingStateLoss();
        fragmentTransaction.hide(mOldFragment);
        fragmentTransaction.add(R.id.activity_main_main_content_container,
                mFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void addFragment(Fragment mFragment, boolean isBackState,
                            Bundle mBundle) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        if (isBackState)
            fragmentTransaction.addToBackStack(null);

        if (mBundle != null)
            mFragment.setArguments(mBundle);

        fragmentTransaction.add(R.id.activity_main_main_content_container,
                mFragment);
        fragmentTransaction.commitAllowingStateLoss();

    }

    /*Handle back press*/
    @Override
    public void onBackPressed() {
        mUtility.hideKeyboard(MainActivity.this);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
        if (count > 0) {
            if (mFragment instanceof FragmentDashboard) {
                if (mFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                    mFragment.getChildFragmentManager().popBackStack();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            if (mFragment instanceof FragmentDashboard) {
                if (exit) {
                    CustomDialog.ExitDialog(MainActivity.this, getString(R.string.str_exit_confirmation));

                } else {
                    exit = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, 2000);
                }

            } else {
                FragmentDashboard mFragmentHome = new FragmentDashboard();
                replacesFragment(mFragmentHome, false, null, 0);
            }
        }
    }

}
