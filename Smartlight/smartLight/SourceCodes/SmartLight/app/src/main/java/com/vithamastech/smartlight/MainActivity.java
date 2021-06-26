package com.vithamastech.smartlight;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.evergreen.ble.advertisement.ManufactureData;
import com.google.android.material.appbar.AppBarLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.navigation.NavigationView;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.Toolbar;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vithamas.blecommmodule.ScannerModule.BLEScanner;
import com.vithamas.blecommmodule.ScannerModule.interfaces.BLEScannerCallback;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.PowerSocketUtils.EncryptionUtilsPowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketAdvData;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketConstants;
import com.vithamastech.smartlight.Vo.VoAddDeviceData;
import com.vithamastech.smartlight.Vo.VoAddGroupData;
import com.vithamastech.smartlight.Vo.VoAddGroupDataDelete;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.Vo.VoDeviceTypeList;
import com.vithamastech.smartlight.Vo.VoLocalGroupData;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.Vo.VoServerDeviceList;
import com.vithamastech.smartlight.Vo.VoServerGroupList;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.fragments.FragmentAboutUs;
import com.vithamastech.smartlight.fragments.FragmentAccount;
import com.vithamastech.smartlight.fragments.FragmentAlarmList;
import com.vithamastech.smartlight.fragments.FragmentContactUs;
import com.vithamastech.smartlight.fragments.FragmentReset;
import com.vithamastech.smartlight.fragments.FragmentHelp;
import com.vithamastech.smartlight.fragments.FragmentHome;
import com.vithamastech.smartlight.fragments.FragmentSettingBridgeConnection;
import com.vithamastech.smartlight.fragments.FragmentYouTube;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.CustomDialog;
import com.vithamastech.smartlight.helper.Encryption;
import com.vithamastech.smartlight.helper.PreferenceHelper;
import com.vithamastech.smartlight.helper.SampleGattAttributes;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.helper.Utility;
import com.vithamastech.smartlight.interfaces.API;
import com.vithamastech.smartlight.interfaces.OnBluetoothStateChangeListener;
import com.vithamastech.smartlight.interfaces.OnHardResetListener;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.vithamastech.smartlight.interfaces.onSyncComplete;
import com.vithamastech.smartlight.services.AdvertiserService;
import com.vithamastech.smartlight.services.BluetoothLeService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import static android.content.Intent.ACTION_VIEW;
import static com.vithamastech.smartlight.db.DBHelper.mTableAlarmPowerSocket;
import static com.vithamastech.smartlight.db.DBHelper.mfield_OffTimestamp;
import static com.vithamastech.smartlight.db.DBHelper.mfield_OnTimestamp;
import static com.vithamastech.smartlight.db.DBHelper.mfield_day_value;

public class MainActivity extends AppCompatActivity {

    String TAG = MainActivity.class.getSimpleName();
    public Utility mUtility;

    public PreferenceHelper mPreferenceHelper;
    public Retrofit mRetrofit;
    public API mApiService;

    @BindView(R.id.activity_main_relativelayout_main)
    public RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.activity_main_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.activity_main_navigation_view)
    public NavigationView mNavigationView;
    @BindView(R.id.activity_main_drawer_layout)
    public DrawerLayout mDrawerLayout;
    @BindView(R.id.activity_main_imageview_container)
    public AppCompatImageView mAppCompatImageViewContainer;
    @BindView(R.id.activity_main_appbar_header)
    public AppBarLayout appBarLayout;
    ActionBarDrawerToggle mActionBarDrawerToggle;

    public ImageView mImageViewBack;
    public ImageView mImageViewAddDevice;
    //    public ImageView mImageViewConnectionStatus;
    public TextView mTextViewDrawerAccountName;
    public TextView mTextViewDrawerMobileNo;
    public TextView mTextViewTitle;
    public TextView mTextViewSubTitle;
    public TextView mTextViewAdd;
    public SwitchCompat mSwitchCompatOnOff;

    FragmentTransaction fragmentTransaction;
    // BLE
    public static BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeService mBluetoothLeService;

    public ArrayList<VoBluetoothDevices> mLeDevices = new ArrayList<>();
    public ArrayList<VoBluetoothDevices> mLeDevicesTemp = new ArrayList<>();

    public BluetoothDevice mBluetoothDevice;

    public String mStringConnectedDevicesAddress = "";
    public onDeviceConnectionStatusChange mOnDeviceConnectionStatusChange;
    public OnBluetoothStateChangeListener onBluetoothStateChangeListener;
    public OnHardResetListener mOnHardResetListener;
    public onSyncComplete mOnSyncComplete;

    public DBHelper mDbHelper;

    public startPingRequestTimer mStartPingRequestTimer;
    public sendMultiAdvertisementTimer mSendMultiAdvertisementTimer;

    private boolean exit = false;
    public boolean isAddDeviceScan = true;
    public boolean isDevicesConnected = false;
    public boolean isPingRequestSent = true;
    public boolean isPingRequestConnection = false;
    public boolean isFromBridgeConnection = false;
    public boolean isFromPowerSocketConnection = false;
    public boolean isConnectionRequestSend = false;
    public AdvertiserService mAdvertiserService;

    public static final int BLUETOOTH_ENABLE_REQUEST = 11;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    public int sequenceNo = 1;
    int selfDeviceId = 9000;
    int selfDeviceIdAssociate = 0000;
    int allGroupDeviceId = 0x0000;
    int mNotificationPacketLength = 0;
    int mIntNotificationType = 0;
    int mIntRssiValue = 0;

    public int mCurrentColorWheel = Color.rgb(255, 255, 255);
    public int mCurrentColorWhiteLight = Color.rgb(255, 255, 255);

    public SimpleDateFormat mDateFormatDb;
    public boolean isFromLogin = false;
    private boolean isReadyToUnbind = false;
    boolean isNeverAskPermissionCheck = false;
    boolean isRequiredToDisconnect = false;
    public boolean isHardResetRequest = false;
    public boolean isIdentificationRequest = false;
    public boolean isShowResetAlert = false;
    private boolean mToolBarNavigationListenerIsRegistered = false;
    private boolean isLocationEnable = false;

    // Notification
    String mStringNotificationMsg;
    byte[] originalNotificationRawData;
    String mStrNotificationHexPacketData = "";
    String calCrcHexNotification = "";
    String mStringNotificationHexData = "";
    String mStrNotificationDeviceType = "";
    VoBluetoothDevices mVoBluetoothDevicesNotification;

    // Scan Variables
    String mStringBridgeScanHexData = "";
    String mStringEncScanHexData = "";
    String mStrEncyHexPacketData = "";
    String mStrDeyHexPacketData = "";
    String calCrcHex = "";
    String mStrDeviceType = "";
    String mStringHexData = "";
    byte[] originalRawData;
    VoBluetoothDevices mVoBluetoothDevicesScan;
    VoBluetoothDevices mVoBluetoothBridgeDevices;

    ResetConnectionStatusTimerTask mResetConnectionStatusTimerTask;
    Timer mTimerResetConnectionStatus;
    BLEScanner bleScanner;
    public boolean isInitialized = true;         // One time initialized to request Socket alarm states when this fragment is created.
    /**
     * Universal object used for Control from
     * FragmentDevices--->FragmentPowerSocketHome
     */
    public static PowerSocket mActivityPowerSocketSelected;
    public ReleasePowerSocketBLEServiceCallback releasePowerSocketBLEServiceCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_main);
        isFromLogin = getIntent().getBooleanExtra("isFromLogin", false);
        ButterKnife.bind(MainActivity.this);
        bleScanner = BLEScanner.getInstance(this);
        bleScanner.allowOperationsOnMainThread(false);
        mDbHelper = DBHelper.getDBHelperInstance(MainActivity.this);
        mPreferenceHelper = new PreferenceHelper(MainActivity.this);
        mUtility = new Utility(MainActivity.this);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.MAIN_URL)
                .client(mUtility.getSimpleClient())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        sequenceNo = mPreferenceHelper.getDeviceSequenceNo();
        mApiService = mRetrofit.create(API.class);
        mPreferenceHelper.setIsDeviceSync(false);
        mPreferenceHelper.setIsGroupSync(false);
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        try {
            mDbHelper.createDataBase();
            mDbHelper.openDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        //  Create Custom Action bar
        initToolbar();
        // Initialize drawer layout
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open, R.string.drawer_close) {

            @Override
            public void onDrawerClosed(View v) {
                super.onDrawerClosed(v);
            }

            @Override
            public void onDrawerOpened(View v) {
                super.onDrawerOpened(v);
            }
        };
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();
        showBackButton(false);

        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(final MenuItem menuItem) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        selectDrawerItem(menuItem);
                    }
                }, 200);
                return true;
            }
        });
        setScrollingBehavior(true);
        View headerView = mNavigationView.getHeaderView(0);
        mTextViewDrawerAccountName = headerView.findViewById(R.id.nav_header_main_tv_account_name);
        mTextViewDrawerMobileNo = headerView.findViewById(R.id.nav_header_main_tv_mobile_no);

        if (!mPreferenceHelper.getIsSkipUser()) {
            mTextViewDrawerAccountName.setText(mPreferenceHelper.getAccountName());
            mTextViewDrawerMobileNo.setText(mPreferenceHelper.getUserContactNo());
        } else {
            mTextViewDrawerAccountName.setText("Guest User");
            mTextViewDrawerMobileNo.setText("");
        }
        mLeDevices = new ArrayList<>();
        mLeDevicesTemp = new ArrayList<>();
        mStartPingRequestTimer = new startPingRequestTimer(5000, 1000);

        removeAllFragmentFromBack();
        replacesFragment(new FragmentHome(), false, null, 1);

        LocalBroadcastManager.getInstance(this).registerReceiver(AppArchReceiver,
                new IntentFilter("ApplicationArch"));

        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {

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
        setOnHardReset(new OnHardResetListener() {
            @Override
            public void onHardResetRequestSuccess() {

            }
        });
        setOnSyncCompleteListner(new onSyncComplete() {
            @Override
            public void onDeviceSyncComplete() {

            }

            @Override
            public void onGroupSyncComplete() {

            }

        });
        mResetConnectionStatusTimerTask = new ResetConnectionStatusTimerTask();
        mTimerResetConnectionStatus = new Timer();
        mTimerResetConnectionStatus.scheduleAtFixedRate(mResetConnectionStatusTimerTask, 0, 10000);
        TextView mTextView = new TextView(MainActivity.this);

        /**
         * PowerSocket Alaram Delete if already time has passed
         */
        deletePoweSocketAlaram_If_TimeElapsed("" + System.currentTimeMillis() / 1000);

    }

    public void deletePoweSocketAlaram_If_TimeElapsed(String timStamp) {
        String querey = "DELETE FROM " + mTableAlarmPowerSocket + " WHERE " + mfield_day_value + " = 0 AND " + mfield_OnTimestamp + " < " + timStamp + " AND " + mfield_OffTimestamp + " < " + timStamp;
        mDbHelper.exeQuery(querey);
    }

    // Reset Connection Status timer
    class ResetConnectionStatusTimerTask extends TimerTask {
        public void run() {
            mIntRssiValue = 0;
            if (!isDevicesConnected) {
                runOnUiThread(() -> {
//                        mImageViewConnectionStatus.setImageResource(R.drawable.ic_bluetooth_gray);
                });
            }
        }
    }

    // Calculate Light Brightness
    public int getLightBrightness(int valueBrightness) {
        // (100-Min/100)*value+Min
        return (int) ((0.8 * valueBrightness) + 20);
    }

    // Calculate Light Brightness from scan
    public int getScanLightBrightness(int valueBrightness) {
        // (value-min)/(100-Min/100)
        return (int) ((valueBrightness - 20) / 0.8);
    }

    /**
     * Returns Intent addressed to the {@code AdvertiserService} class.
     */
    private static Intent getServiceIntent(Context c) {
        return new Intent(c, AdvertiserService.class);
    }

    // Bluetooth state change receiver broadcast receiver.
    private final BroadcastReceiver mBluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                console.log("dkjcxaskxkjasjkxasjkxv", "State = " + state);
                console.log("akjbkjabsjkabskjxbajks", mBluetoothAdapter.getState());

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        console.log("asxoiansxoiansix", "here");
                        // on bluetooth off disconnect device , stop scan and close
                        isDevicesConnected = false;
                        mBluetoothLeService.disconnect();
                        mBluetoothLeService.stopSelf();

                        if (onBluetoothStateChangeListener != null) {
                            onBluetoothStateChangeListener.onBluetoothOff();
                        }
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        stopScan();
                        if (onBluetoothStateChangeListener != null) {
                            onBluetoothStateChangeListener.onBluetoothTurningOff();
                        }
                        break;
                    case BluetoothAdapter.STATE_ON:
                        // on bluetooth on start ble scanning and initialize
//                        mImageViewConnectionStatus.setImageResource(R.drawable.ic_bluetooth_gray);
                        stopScan();
                        mLeDevices = new ArrayList<>();
                        mLeDevicesTemp = new ArrayList<>();
                        if (Build.VERSION.SDK_INT >= 23) {
                            callMarshmallowPermission();
                        }
                        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                            mBluetoothAdapter = mBluetoothManager.getAdapter();
                            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                            if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                                startActivityForResult(enableIntent, BLUETOOTH_ENABLE_REQUEST);
                            } else {
                                if (!isLocationEnabled(MainActivity.this)) {
                                    if (Build.VERSION.SDK_INT >= 23) {
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
                                        return;
                                    }
                                }
                                Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                    if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                                        Intent gattAdvServiceIntent = new Intent(MainActivity.this, AdvertiserService.class);
                                        bindService(gattAdvServiceIntent, mAdvertiserServiceConnection, BIND_AUTO_CREATE);
                                    }
                                }
                            }
                            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                            registerReceiver(mBluetoothStateReceiver, filter);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                                    IntentFilter filterAdvertiser = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                                    registerReceiver(advertisingFailureReceiver, filterAdvertiser);
                                    sendCurrentTimeDevice();
                                }
                            }
                            startDeviceScan();
                            isReadyToUnbind = true;
                        }

                        if (onBluetoothStateChangeListener != null) {
                            onBluetoothStateChangeListener.onBluetoothOn();
                        }

                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        if (onBluetoothStateChangeListener != null) {
                            onBluetoothStateChangeListener.onBluetoothTurningOn();
                        }
                        break;
                }
            }
        }
    };

    /*Connect ble service*/
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                CustomDialog.Snakbar(MainActivity.this, "Bluetooth Service not initialize. Please open app again.");
            } else {
                mLeDevices = new ArrayList<>();
                mLeDevicesTemp = new ArrayList<>();
                RescanDevice(false);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // mBluetoothLeService = null;
        }
    };

    /*Connect Advertiser Service*/
    public final ServiceConnection mAdvertiserServiceConnection = new ServiceConnection() {

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mAdvertiserService = ((AdvertiserService.LocalBinder) service).getService();
            if (!mAdvertiserService.initialize()) {
                CustomDialog.Snakbar(MainActivity.this, "Bluetooth Advertiser Service not initialize. Please open app again.");
            } else {
                System.out.println(TAG + "Advertisement Service Started");
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // mBluetoothLeService = null;
            System.out.println(TAG + "-------Advertisement Services Disconnected...");
        }
    };
    /*Advertiser Receiver Service*/
    private BroadcastReceiver advertisingFailureReceiver = new BroadcastReceiver() {
        /**
         * Receives Advertising error codes from {@code AdvertiserService} and displays error messages
         * to the user. Sets the advertising toggle to 'false.'
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            int errorCode = intent.getIntExtra(AdvertiserService.ADVERTISING_FAILED_EXTRA_CODE, -1);
            String errorMessage = " Advertisement Message";
            switch (errorCode) {
                case AdvertiseCallback.ADVERTISE_FAILED_ALREADY_STARTED:
                    errorMessage += " ADVERTISE_FAILED_ALREADY_STARTED";
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_DATA_TOO_LARGE:
                    errorMessage += " ADVERTISE_FAILED_DATA_TOO_LARGE";
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_FEATURE_UNSUPPORTED:
                    errorMessage += " ADVERTISE_FAILED_FEATURE_UNSUPPORTED";
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_INTERNAL_ERROR:
                    errorMessage += " ADVERTISE_FAILED_INTERNAL_ERROR";
                    break;
                case AdvertiseCallback.ADVERTISE_FAILED_TOO_MANY_ADVERTISERS:
                    errorMessage += " ADVERTISE_FAILED_TOO_MANY_ADVERTISERS";
                    break;
                case AdvertiserService.ADVERTISING_TIMED_OUT:
                    errorMessage = " ADVERTISING_TIMED_OUT";
                    break;
                default:
//                    mBluetoothLeService.close();
//                    mBluetoothLeService.stopSelf();
                    errorMessage += " Unknown";
            }
            System.out.println(TAG + " Advertisement BroadCast-" + errorMessage);
        }
    };
    /*Ble Connect, disconnect, discover,data and error broadcast receiver */
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            final String mStringDevicesAddress = intent.getStringExtra("mStringDevicesAddress");
            final String mStringDevicesName = intent.getStringExtra("mStringDevicesName");
            try {
//                System.out.println(TAG + "-----BroadcastReceiver action " + action);
                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    System.out.println(TAG + "-----BroadcastReceiver mStringDevicesAddress " + mStringDevicesAddress);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBluetoothDevice != null) {
                                isDevicesConnected = true;
                                for (int i = 0; i < mLeDevices.size(); i++) {
                                    if (mStringDevicesAddress != null && mStringDevicesAddress.equalsIgnoreCase(mLeDevices.get(i).getDeviceAddress())) {
                                        mStringConnectedDevicesAddress = mStringDevicesAddress;
                                        mLeDevices.get(i).setIsConnected(true);
                                        if (mOnDeviceConnectionStatusChange != null) {
                                            mOnDeviceConnectionStatusChange.onConnect(null, mBluetoothDevice.getName(), mBluetoothDevice);
                                        }
                                        break;
                                    }
                                }
                                if (mOnDeviceConnectionStatusChange != null) {
                                    mOnDeviceConnectionStatusChange.onConnect(null, mBluetoothDevice.getName(), mBluetoothDevice);
                                }
                            }
                        }
                    });
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    console.log("scsscsdc_main_connection", "here!!!!");
                    System.out.println(TAG + "-----Disconnected Device " + action);
                    runOnUiThread(() -> {
                        isDevicesConnected = false;
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
                            boolean isConnectedAnyDevice = false;
                            for (VoBluetoothDevices device : mLeDevices) {
                                if (device.getIsConnected()) {
                                    isConnectedAnyDevice = true;
                                    break;
                                }
                            }
                            if (!isConnectedAnyDevice) {
                                isDevicesConnected = false;
                                if (!isFinishing()) {
                                    Timer innerTimer = new Timer();
                                    innerTimer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            isConnectionRequestSend = false;
                                        }
                                    }, 60000);
                                }
                            }
                        }
                        if (mOnDeviceConnectionStatusChange != null) {
                            if (mBluetoothDevice != null) {
                                mOnDeviceConnectionStatusChange.onDisconnect(null, mBluetoothDevice.getName(), mBluetoothDevice);
                                mBluetoothDevice = null;
                            }
                        }
                    });
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    runOnUiThread(() -> {
                        System.out.println(TAG + "-----ACTION_GATT_SERVICES_DISCOVERED");
                        displayGattServices(mBluetoothLeService.getSupportedGattServices(), mStringDevicesAddress, mStringDevicesName);
                    });
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA) != null) {
                        mStringNotificationMsg = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                        mIntNotificationType = intent.getIntExtra(BluetoothLeService.BLE_NOTIFICATION_TYPE, 0);

                        if (mStringNotificationMsg != null && !mStringNotificationMsg.equals("") && !mStringNotificationMsg.equals("null")) {
                            mStringNotificationMsg = mStringNotificationMsg.replaceAll("\\s+", "");
//                            System.out.println(TAG + "-Notification-mStringReceivedMsg-" + mStringNotificationMsg);
                            console.log("scsscsdc_HardResetFinal", "Notification Msg Here");
                            if (isHardResetRequest) {
                                // To Hard Reset
                                console.log("scsscsdc_HardResetFinal", "Here");
                                try {
                                    int reset_key = BLEUtility.hexToDecimal(mStringNotificationMsg);
                                    reset_key = ((((reset_key * 9) + 17) * 23) - (9 * reset_key + 55));
                                    System.out.println(TAG + "-Notification-ResetMsgGenKey-" + reset_key);
                                    short reset_value_res = (short) reset_key;
                                    byte reset_value[] = new byte[2];
                                    reset_value[0] = (byte) (reset_value_res & 0x00FF);
                                    reset_value[1] = (byte) ((reset_value_res >> 8) & 0x00FF);
                                    mBluetoothLeService.sendDeviceHardResetMsg(reset_value);
                                    isHardResetRequest = false;
                                    if (mOnHardResetListener != null) {
                                        mOnHardResetListener.onHardResetRequestSuccess();
                                    }
                                } catch (Exception e) {
                                    isHardResetRequest = false;
                                    e.printStackTrace();
                                }
                            } else {
                                try {
                                    // Notification Scanning Parse Data
                                    if (mStringNotificationMsg.length() >= 22) {
                                        console.log("scsscsdc_Other_Message", "Here");
                                        mNotificationPacketLength = (mStringNotificationMsg.length() / 2);
                                        if (mStringNotificationMsg.substring(6, 10).equals("0000")) {
                                            originalNotificationRawData = decryptRawByteData(BLEUtility.hexStringToBytes(mStringNotificationMsg), true);
                                        } else {
                                            originalNotificationRawData = decryptRawByteData(BLEUtility.hexStringToBytes(mStringNotificationMsg), false);
                                        }
                                        mStrNotificationHexPacketData = BLEUtility.toHexString(originalNotificationRawData, true);
                                        calCrcHexNotification = (String.format("%04X", (0xFFFF & checkCrcIsValidPacketData(originalNotificationRawData))).toUpperCase());
                                        if (calCrcHexNotification != null && calCrcHexNotification.length() >= 2) {
                                            calCrcHexNotification = calCrcHexNotification.substring(2) + calCrcHexNotification.substring(0, 2);
                                        }
                                        if (mStrNotificationHexPacketData.substring(14, 18).equals(calCrcHexNotification)) {
                                            mStringNotificationHexData = "020104" + String.format("%02d", mNotificationPacketLength) + "FF0A00" + mStrNotificationHexPacketData;
                                            if (mStringNotificationHexData.length() < 52) {
                                                int length = 52 - mStringNotificationHexData.length();
                                                for (int i = 0; i < length; i++) {
                                                    mStringNotificationHexData = mStringNotificationHexData + "0";
                                                }
                                            }
//                                            System.out.println("mStringNotificationHexData="+mStringNotificationHexData);
                                            mStrNotificationDeviceType = mStringNotificationHexData.substring(48, 52);
                                            mVoBluetoothDevicesNotification = new VoBluetoothDevices();
//                                            mVoBluetoothDevicesScan.setBluetoothDevice(newDevices);
//                                            mVoBluetoothDevicesScan.setDeviceAddress(newDevices.getAddress());
                                            mVoBluetoothDevicesNotification.setIsConnected(false);
                                            mVoBluetoothDevicesNotification.setFromNotification(true);
                                            mVoBluetoothDevicesNotification.setDeviceIEEE(mStringNotificationHexData.substring(36, 48));
                                            mVoBluetoothDevicesNotification.setDeviceHexData(mStringNotificationHexData);
                                            mVoBluetoothDevicesNotification.setDeviceScanOpcode(mStringNotificationHexData.substring(32, 36));
                                            mVoBluetoothDevicesNotification.setDeviceType(mStrNotificationDeviceType);
                                            if (mStrNotificationDeviceType.equalsIgnoreCase("0100")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Light");
                                            } else if (mStrNotificationDeviceType.equalsIgnoreCase("0200")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas White Light");
                                            } else if (mStrNotificationDeviceType.equalsIgnoreCase("0300")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Switch");
                                            } else if (mStrNotificationDeviceType.equalsIgnoreCase("0400")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Socket");
                                            } else if (mStrNotificationDeviceType.equalsIgnoreCase("0500")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Fan");
                                            } else if (mStrNotificationDeviceType.equalsIgnoreCase("0600")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Strip Light");
                                            } else if (mStrNotificationDeviceType.equalsIgnoreCase("0700")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Night Lamp");
                                            } else if (mStrNotificationDeviceType.equalsIgnoreCase("0800")) {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Power Strip");
                                            } else {
                                                mVoBluetoothDevicesNotification.setDeviceName("Vithamas Light");
                                            }
                                            boolean containsInScanDevice = false;
                                            for (VoBluetoothDevices device : mLeDevicesTemp) {
                                                if (mStringNotificationHexData.equals(device.getDeviceHexData())) {
                                                    containsInScanDevice = true;
                                                    break;
                                                }
                                            }
                                            if (!containsInScanDevice) {
                                                mLeDevicesTemp.add(mVoBluetoothDevicesNotification);
                                                if (mOnDeviceConnectionStatusChange != null) {
                                                    mOnDeviceConnectionStatusChange.addScanDevices();
                                                    mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothDevicesNotification);
                                                }
                                            } else {
                                                for (int j = 0; j < mLeDevicesTemp.size(); j++) {
                                                    if (j < mLeDevicesTemp.size()) {
                                                        if (mStringNotificationHexData.equals(mLeDevicesTemp.get(j).getDeviceHexData())) {
                                                            mLeDevicesTemp.set(j, mVoBluetoothDevicesNotification);
                                                            if (mOnDeviceConnectionStatusChange != null) {
                                                                mOnDeviceConnectionStatusChange.addScanDevices();
                                                                mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothDevicesNotification);
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        console.log("scsscsdc_HardResetInitial", "Here");
                                        console.log("scsscsdc_NotificationMessage", mStringNotificationMsg);
                                        int auth_key = BLEUtility.hexToDecimal(mStringNotificationMsg);
                                        auth_key = ((((auth_key * 7) + 19) * 12) - (4 * auth_key + 13));
                                        System.out.println(TAG + "-Notification-mStringReceivedMsgGenKey-" + auth_key);
                                        short auth_value_res = (short) auth_key;
                                        byte auth_value[] = new byte[2];
                                        auth_value[0] = (byte) (auth_value_res & 0x00FF);
                                        auth_value[1] = (byte) ((auth_value_res >> 8) & 0x00FF);
                                        mBluetoothLeService.sendDeviceAuthMsg(auth_value);

                                        if (isShowResetAlert) {
                                            isShowResetAlert = false;
                                            isIdentificationRequest = false;
                                            hideProgress();
                                            mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.frg_setting_hard_reset_alert), getResources().getString(R.string.frg_setting_hard_reset_confirmation), "YES", "NO", false, 2, new onAlertDialogCallBack() {
                                                @Override
                                                public void PositiveMethod(DialogInterface dialog, int id) {
                                                    try {
                                                        isHardResetRequest = true;
                                                        mBluetoothLeService.readCharacteristic(mBluetoothLeService.getDeviceHardResetCharacteristic());
                                                    } catch (Exception e) {
                                                        isHardResetRequest = false;
                                                        e.printStackTrace();
                                                    }
                                                }

                                                @Override
                                                public void NegativeMethod(DialogInterface dialog, int id) {

                                                }
                                            });
                                        }
                                        if (isIdentificationRequest) {
                                            console.log("scsscsdc_IdentificationRequest", "here");
                                            Timer innerTimer = new Timer();
                                            innerTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    try {
                                                        isIdentificationRequest = false;
                                                        isShowResetAlert = true;
                                                        mBluetoothLeService.readCharacteristic(mBluetoothLeService.getDeviceAuthCharacteristic());
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, 300);
                                        }
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                } else if (action.equalsIgnoreCase(BluetoothLeService.RSSI_DATA)) {
                    System.out.println(TAG + "--RSSI_DATA " + mBluetoothLeService.updateRSSI);
                } else if (action.equalsIgnoreCase(BluetoothLeService.ERORR)) {
                    System.out.println(TAG + "-----ERROR Device " + action);
                    mLeDevices = new ArrayList<>();
                    mLeDevicesTemp = new ArrayList<>();
                    RescanDevice(true);
                    if (mOnDeviceConnectionStatusChange != null) {
                        mOnDeviceConnectionStatusChange.onError();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    // discover gatt service after device connected.
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
                    if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                            (SampleGattAttributes.UUID_SMART_MESH_AUTH_CHAR.equals(gattCharacteristic.getUuid()))) {
                        final BluetoothGattCharacteristic gattCharacteristicTemp = gattCharacteristic;
                        Timer innerTimer = new Timer();
                        innerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                try {
                                    System.out.println(TAG + "--UUID " + gattCharacteristicTemp.getUuid().toString());
                                    mBluetoothLeService.setDeviceAuthCharacteristic(gattCharacteristicTemp);
                                    mBluetoothLeService.readCharacteristic(gattCharacteristicTemp);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }, 300);
//                        mBluetoothLeService.setCharacteristicNotifications(gattCharacteristic, true);
                    } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                            (SampleGattAttributes.UUID_SMART_MESH_HARD_RESET_CHAR.equals(gattCharacteristic.getUuid()))) {
                        mBluetoothLeService.setDeviceHardResetCharacteristic(gattCharacteristic);
                        System.out.println(TAG + "--UUID " + gattCharacteristic.getUuid().toString());
                    } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                            (SampleGattAttributes.UUID_SMART_MESH_CONT_CHAR.equals(gattCharacteristic.getUuid()))) {
                        System.out.println(TAG + "--UUID " + gattCharacteristic.getUuid().toString());
                        mBluetoothLeService.setContPartCharacteristic(gattCharacteristic);
//                        mBluetoothLeService.readCharacteristic(gattCharacteristic);
//                        mBluetoothLeService.setCharacteristicNotifications(gattCharacteristic, true);
                        Timer innerTimerUTC = new Timer();
                        innerTimerUTC.schedule(new TimerTask() {
                            @Override
                            public void run() {
//                                Send Current Time To Device.
                                sendCurrentTimeDevice();
                            }
                        }, 300);
                    }
                }
            }
        }
    }

    /*Connect Ble Device */
    public void ConnectDevices(final BluetoothDevice mConnectBluetoothDevice, final boolean isAutoConnect) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothLeService != null) {
//                    stopScan(false);
                    mBluetoothDevice = mConnectBluetoothDevice;
                    mBluetoothLeService.connect(mConnectBluetoothDevice, isAutoConnect);
                }
            }
        });

    }

    /*DisConnect Ble Device */
    public void disconnectDevices(final BluetoothDevice mBluetoothDevice, final boolean isStopScanComplete) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                stopScan(isStopScanComplete);
                mBluetoothLeService.removeDevices(mBluetoothDevice);
            }
        });
    }

    public void RescanDevice(boolean isNeedToStopScan) {
        mLeDevicesTemp = new ArrayList<>();
    }

    /*Start Device Scan*/
    @SuppressLint("InlinedApi")
    public void startDeviceScan() {
        if (!bleScanner.isScanning()) {
            bleScanner.startDeviceScan();
        }
    }

    /*send ping request to device*/
    public void sendPingRequestToDevice() {
        if (isPingRequestConnection) {
            return;
        }
        RescanDevice(false);
        sendAdvertisePingRequest(BLEUtility.intToByte(100), Short.parseShort(0 + ""), false);
        isPingRequestConnection = true;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgress("Connecting...", true);
                if (mStartPingRequestTimer != null)
                    mStartPingRequestTimer.start();
            }
        });

    }

    /*Ping Request Timer*/
    public class startPingRequestTimer extends CountDownTimer {

        public startPingRequestTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            hideProgress();
            isPingRequestConnection = false;
            if (!isPingRequestSent) {
                if (!isFinishing()) {
//                    mUtility.errorDialog("Something went wrong. please scan again.", 1);
                }
            }
        }
    }

    /*Show Progress and start scan*/
    public void connectDeviceWithProgress() {
        if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BLUETOOTH_ENABLE_REQUEST);
            return;
        }
        mLeDevices = new ArrayList<>();
//        showProgress("Connecting...", true);
        RescanDevice(false);
        isConnectionRequestSend = false;
        startDeviceScan();
    }

    /*Show Progress*/
    public void showProgress(final String mStringProgressTitle, boolean isShowTitle) {
        if (!isFinishing()) {
            mUtility.ShowProgress(mStringProgressTitle);
        }
    }

    /*Hide Progress*/
    public void hideProgress() {
        mUtility.HideProgress();
    }

    /*Start Device Scan Timer If not connect show alert and redirect to bridge connection.*/
    public class startDeviceScanTimer extends CountDownTimer {

        public startDeviceScanTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            hideProgress();
            if (!isFinishing()) {
                if (!isDevicesConnected || getIsSDKAbove21()) {
                    if (getIsSDKAbove21()) {
                        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                            if (!isAdvertisingSupported()) {
                                if (!isFinishing() && !isDevicesConnected) {
                                    mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_no_any_device_connect), 1, true, new onAlertDialogCallBack() {
                                        @Override
                                        public void PositiveMethod(DialogInterface dialog, int id) {
                                            Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
                                            if (!(mFragment instanceof FragmentSettingBridgeConnection)) {
                                                if (!isDevicesConnected) {
                                                    replacesFragment(new FragmentSettingBridgeConnection(), true, null, 0);
                                                }
                                            }
                                        }

                                        @Override
                                        public void NegativeMethod(DialogInterface dialog, int id) {

                                        }
                                    });
                                }
                            }
                        }
                    } else {
                        if (!isFinishing() && !isDevicesConnected) {
                            mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_no_any_device_connect), 1, true, new onAlertDialogCallBack() {
                                @Override
                                public void PositiveMethod(DialogInterface dialog, int id) {
                                    Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
                                    if (!(mFragment instanceof FragmentSettingBridgeConnection)) {
                                        if (!isDevicesConnected) {
                                            replacesFragment(new FragmentSettingBridgeConnection(), true, null, 0);
                                        }
                                    }
                                }

                                @Override
                                public void NegativeMethod(DialogInterface dialog, int id) {

                                }
                            });
                        }
                    }
                }
            }
        }
    }

    /*Stop device scan */
    public void stopScan() {
        console.log("asxavsxyjavsxyuasyuxv", "here");
        if (bleScanner != null) {
            try {
                bleScanner.stopScan();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //    }
    /*Check location is enable or not*/
    public boolean isLocationEnabled(Context context) {
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            isLocationEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            isLocationEnable = locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isLocationEnable = !TextUtils.isEmpty(locationProviders);
        }
        return isLocationEnable;
    }

    /*Gps On/Off broadcast receiver*/
    private BroadcastReceiver mGpsSwitchStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                // Make an action or refresh an already managed state.
                if (!isLocationEnabled(context)) {
                    if (Build.VERSION.SDK_INT >= 23) {
                        System.out.println("JD-ON Receiver GPS ALERT");
                        if (!isFinishing()) {
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
                }
            }
        }
    };

    // App Archetechture Broadcast Receiver
    BroadcastReceiver AppArchReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String state = intent.getStringExtra("App_State");
            if (state != null) {
                if (isRequiredToDisconnect) {
                    if (state.equals("ON_STOP")) {
                        System.out.println("APP STOP");
                    } else if (state.equals("ON_START")) {
                        System.out.println("APP START");
                        isConnectionRequestSend = false;
//                        isDevicesConnected = false;
                        mStringConnectedDevicesAddress = "";
                        mLeDevices = new ArrayList<>();
                        mLeDevicesTemp = new ArrayList<>();
                    } else if (state.equals("ON_DESTROY")) {
                        System.out.println("APP DESTROYED");
                        try {
                            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                                if (isDevicesConnected) {
                                    if (mBluetoothDevice != null) {
                                        System.out.println("DISCONNECT ON DESTROY");
                                        disconnectDevices(mBluetoothDevice, true);
                                    }
                                }
                                stopScan();
                                Timer innerTimer = new Timer();
                                innerTimer.schedule(new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (isReadyToUnbind) {
                                            try {
                                                if (mServiceConnection != null) {
                                                    unbindService(mServiceConnection);
                                                }

                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                                                        if (mAdvertiserServiceConnection != null) {
                                                            unbindService(mAdvertiserServiceConnection);
                                                        }
                                                    }
                                                }
                                                if (mGattUpdateReceiver != null) {
                                                    unregisterReceiver(mGattUpdateReceiver);
                                                }
                                                if (mBluetoothStateReceiver != null) {
                                                    unregisterReceiver(mBluetoothStateReceiver);
                                                }
                                                if (mGpsSwitchStateReceiver != null) {
                                                    unregisterReceiver(mGpsSwitchStateReceiver);
                                                }
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                                    if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                                                        if (advertisingFailureReceiver != null) {
                                                            unregisterReceiver(advertisingFailureReceiver);
                                                        }
                                                    }
                                                }
                                            } catch (Exception e) {
                                                // already registered
                                                e.printStackTrace();
                                            }

                                            isReadyToUnbind = false;
                                        }
                                    }
                                }, 1000);

                            }

                            if (mStartPingRequestTimer != null)
                                mStartPingRequestTimer.cancel();
                            if (mSendMultiAdvertisementTimer != null)
                                mSendMultiAdvertisementTimer.cancel();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    };

    BLEScannerCallback bleScannerCallback = new BLEScannerCallback() {
        @Override
        public void onStartScan() {

        }

        @Override
        public void onStopScan() {

        }

        @Override
        public void onScanFailed(int i, String s) {

        }

        @Override
        public void onScanResult(BluetoothDevice bluetoothDevice, byte[] encRawData) {
//            String logData = bluetoothDevice.getAddress() + " --> " + ByteConverter.getHexStringFromByteArray(encRawData, false);
//            console.log("sdcsucusgdcuigsdc", logData);
            if (mBluetoothAdapter.isEnabled()) {
                mStringBridgeScanHexData = BLEUtility.toHexString(encRawData, true);

                // This flag is set when FragmentResetSmartLight/ FragmentSettingBridgeConnection is visible.
                // User wants to connect to the device. --> Connectible Advertisement
                if (isFromBridgeConnection) {
                    // Scan Bridge(Device) Connection and pass scan result in Bridge(Device) Connection Screen
                    mStringBridgeScanHexData = BLEUtility.toHexString(encRawData, true);
                    if (mStringBridgeScanHexData != null && !mStringBridgeScanHexData.equalsIgnoreCase("")) {
                /*
                // Check for LE General Discoverable Mode bit, LE Limited Discoverable mode and BR/EDR not supported bit
                /*
                 1. 06 -> 00000110 = BR/EDR not supported, LE General Discoverable mode -> Connectible advertisement
                 2. 03 -> 00000011 = LE General Discoverable mode, LE Limited Discoverable mode -> Connectible advertisement
                 */
                        if (mStringBridgeScanHexData.startsWith("020106") || mStringBridgeScanHexData.startsWith("020103")) {
                            // Check for Manufacturer Data Id = 0x000A (Reverse of 0x0A00)
                            if (mStringBridgeScanHexData.contains("FF0A00") || mStringBridgeScanHexData.contains("FF3200")) {
                                if (mStringBridgeScanHexData.length() >= 32) {

                                    mVoBluetoothBridgeDevices = new VoBluetoothDevices();
                                    mVoBluetoothBridgeDevices.setBluetoothDevice(bluetoothDevice);
                                    mVoBluetoothBridgeDevices.setDeviceName("Vithamas");
                                    mVoBluetoothBridgeDevices.setDeviceAddress(bluetoothDevice.getAddress());
                                    mVoBluetoothBridgeDevices.setDeviceIEEE(mStringBridgeScanHexData.substring(18, 30));
                                    mVoBluetoothBridgeDevices.setIsConnected(false);
                                    if (bluetoothDevice.getAddress().equalsIgnoreCase(mStringConnectedDevicesAddress)) {
                                        if (isDevicesConnected) {
                                            mVoBluetoothBridgeDevices.setIsConnected(true);
                                        }
                                    } else {
                                        mVoBluetoothBridgeDevices.setIsConnected(false);
                                    }
                                    if (mBluetoothDevice != null) {
                                        if (bluetoothDevice.getAddress().equalsIgnoreCase(mBluetoothDevice.getAddress()) && isDevicesConnected) {
                                            mVoBluetoothBridgeDevices.setIsConnected(true);
                                            isDevicesConnected = true;

                                        }
                                    }
                                    mVoBluetoothBridgeDevices.setDeviceHexData(mStringBridgeScanHexData);

//                                console.log("skxoisxn_DeviceAddres", mVoBluetoothBridgeDevices.getDeviceAddress());
//                                console.log("skxoisxn_IEEE", mVoBluetoothBridgeDevices.getDeviceIEEE());
//                                console.log("skxoisxn_HexData", mVoBluetoothBridgeDevices.getDeviceHexData());

                                    if (mOnDeviceConnectionStatusChange != null) {
                                        mOnDeviceConnectionStatusChange.addScanDevices();
                                        mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothBridgeDevices);
//                                    Log.d(TAG, "MainActivity_onScanResultGet: " + mVoBluetoothBridgeDevices.getDeviceAddress());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Apart from bridge connection scan and check is device not connected then try to connect in background.
                    if (!isDevicesConnected) {
//                if (getIsSDKAbove21()) {
//                    if (encRawData != null) {
//                        mStringBridgeScanHexData = BLEUtility.toHexString(encRawData, true);
//                        if (mStringBridgeScanHexData != null && !mStringBridgeScanHexData.equalsIgnoreCase("")) {
//                            if (mStringBridgeScanHexData.startsWith("020106")) {
//                                if (mStringBridgeScanHexData.contains("FF0A00")) {
//                                    if (mStringBridgeScanHexData.length() >= 32) {
//                                        String mStrBleAdd = mStringBridgeScanHexData.substring(18, 30);
//                                        boolean contains = false;
//                                        for (int i = 0; i < mLeDevices.size(); i++) {
//                                            if (mStrBleAdd.equals(mLeDevices.get(i).getDeviceAddress())) {
//                                                contains = true;
//                                                break;
//                                            }
//                                        }
//                                        if (!contains) {
//                                            mVoBluetoothBridgeDevices = new VoBluetoothDevices();
//                                            mVoBluetoothBridgeDevices.setBluetoothDevice(newDevices);
//                                            mVoBluetoothBridgeDevices.setDeviceName("Vithamas");
//                                            mVoBluetoothBridgeDevices.setDeviceAddress(newDevices.getAddress());
//                                            mVoBluetoothBridgeDevices.setDeviceIEEE(mStrBleAdd);
//                                            mVoBluetoothBridgeDevices.setIsConnected(false);
//                                            if (newDevices.getAddress().equalsIgnoreCase(mStringConnectedDevicesAddress)) {
//                                                if (isDevicesConnected) {
//                                                    mVoBluetoothBridgeDevices.setIsConnected(true);
//                                                }
//                                            } else {
//                                                mVoBluetoothBridgeDevices.setIsConnected(false);
//                                            }
//                                            if (mBluetoothDevice != null) {
//                                                if (newDevices.getAddress().equalsIgnoreCase(mBluetoothDevice.getAddress()) && isDevicesConnected) {
//                                                    mVoBluetoothBridgeDevices.setIsConnected(true);
//                                                    isDevicesConnected = true;
//                                                }
//                                            }
//                                            mVoBluetoothBridgeDevices.setDeviceHexData(mStringBridgeScanHexData);
//                                            mLeDevices.add(mVoBluetoothBridgeDevices);
//                                        }
//                                        // Connect Any one Device
//                                        boolean isConnectedAnyDevice = false;
//                                        for (int i = 0; i < mLeDevices.size(); i++) {
//                                            if (mLeDevices.get(i).getIsConnected()) {
//                                                isConnectedAnyDevice = true;
//                                                break;
//                                            }
//                                        }
//                                        if (!isConnectedAnyDevice) {
//                                            if (mLeDevices != null && mLeDevices.size() > 0) {
//                                                if (!isConnectionRequestSend) {
//                                                    System.out.println(TAG + "Scan First Device Auto Connect");
//                                                    System.out.println(TAG + "Scan Connection Request Address-" + mLeDevices.get(0).getBluetoothDevice().getAddress());
//                                                    isConnectionRequestSend = true;
//                                                    isHardResetRequest = false;
//                                                    isShowResetAlert = false;
//                                                    isIdentificationRequest = false;
//                                                    ConnectDevices(mLeDevices.get(0).getBluetoothDevice(), true);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//                else {
//                    if (encRawData != null) {
//                        mStringBridgeScanHexData = BLEUtility.toHexString(encRawData, true);
//                        if (mStringBridgeScanHexData != null && !mStringBridgeScanHexData.equalsIgnoreCase("")) {
//                            if (mStringBridgeScanHexData.startsWith("020106")) {
//                                if (mStringBridgeScanHexData.contains("FF0A00")) {
//                                    if (mStringBridgeScanHexData.length() >= 32) {
//                                        String mStrBleAdd = mStringBridgeScanHexData.substring(18, 30);
//                                        boolean contains = false;
//                                        for (VoBluetoothDevices device : mLeDevices) {
//                                            if (mStrBleAdd.equals(device.getDeviceAddress())) {
//                                                contains = true;
//                                                break;
//                                            }
//                                        }
//                                        if (!contains) {
//                                            mVoBluetoothBridgeDevices = new VoBluetoothDevices();
//                                            mVoBluetoothBridgeDevices.setBluetoothDevice(newDevices);
//                                            mVoBluetoothBridgeDevices.setDeviceName("Vithamas");
//                                            mVoBluetoothBridgeDevices.setDeviceAddress(newDevices.getAddress());
//                                            mVoBluetoothBridgeDevices.setDeviceIEEE(mStringBridgeScanHexData.substring(18, 30));
//                                            mVoBluetoothBridgeDevices.setIsConnected(false);
//                                            if (newDevices.getAddress().equalsIgnoreCase(mStringConnectedDevicesAddress)) {
//                                                if (isDevicesConnected) {
//                                                    mVoBluetoothBridgeDevices.setIsConnected(true);
//                                                }
//                                            } else {
//                                                mVoBluetoothBridgeDevices.setIsConnected(false);
//                                            }
//                                            if (mBluetoothDevice != null) {
//                                                if (newDevices.getAddress().equalsIgnoreCase(mBluetoothDevice.getAddress()) && isDevicesConnected) {
//                                                    mVoBluetoothBridgeDevices.setIsConnected(true);
//                                                    isDevicesConnected = true;
//                                                }
//                                            }
//                                            mVoBluetoothBridgeDevices.setDeviceHexData(mStringBridgeScanHexData);
//                                            mLeDevices.add(mVoBluetoothBridgeDevices);
//                                        }
//                                        // Connect Any one Device
//                                        boolean isConnectedAnyDevice = false;
//                                        for (int i = 0; i < mLeDevices.size(); i++) {
//                                            if (mLeDevices.get(i).getIsConnected()) {
//                                                isConnectedAnyDevice = true;
//                                                break;
//                                            }
//                                        }
//                                        if (!isConnectedAnyDevice) {
//                                            if (mLeDevices != null && mLeDevices.size() > 0) {
//                                                if (!isConnectionRequestSend) {
//                                                    System.out.println(TAG + "Scan First Device Auto Connect");
//                                                    System.out.println(TAG + "Scan Connection Request Address-" + mLeDevices.get(0).getBluetoothDevice().getAddress());
//                                                    isConnectionRequestSend = true;
//                                                    isHardResetRequest = false;
//                                                    isShowResetAlert = false;
//                                                    isIdentificationRequest = false;
//                                                    ConnectDevices(mLeDevices.get(0).getBluetoothDevice(), true);
//                                                }
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
                    }
                }

                // This flag is set when FragmentAddDevice is visible.
                // When user wants to add device --> Non Connectible Advertisement
                // Without Bridge(Device) Connection scan and pass scan result in all other Screen
                if (isAddDeviceScan) {
                    if (encRawData != null) {
                        mStringEncScanHexData = BLEUtility.toHexString(encRawData, true);
//                System.out.println("mStringEncScanHexData="+mStringEncScanHexData);
                        if (mStringEncScanHexData != null && !mStringEncScanHexData.equalsIgnoreCase("")) {
                            if (mStringEncScanHexData.startsWith("020104")) {              // Non connectable Advertisement
                                if (mStringEncScanHexData.contains("FF0A00")) {
//                                if (mStringEncHexData.length() >= 52) {
                                    if (mPreferenceHelper.getSecretKey() != null && !mPreferenceHelper.getSecretKey().equalsIgnoreCase("")) {
                                        mStrEncyHexPacketData = mStringEncScanHexData.substring(14, 14 + (((BLEUtility.hexToDecimal(mStringEncScanHexData.substring(6, 8))) * 2) - 6));
                                        // Decrypt Scan Device Data
                                        if (mStringEncScanHexData.substring(20, 24).equals("0000")) { // Self Device ID
                                            originalRawData = decryptRawByteData(BLEUtility.hexStringToBytes(mStrEncyHexPacketData), true);
                                        } else {
                                            originalRawData = decryptRawByteData(BLEUtility.hexStringToBytes(mStrEncyHexPacketData), false);
                                        }

                                        mStrDeyHexPacketData = BLEUtility.toHexString(originalRawData, true);
                                        calCrcHex = (String.format("%04X", (0xFFFF & checkCrcIsValidPacketData(originalRawData))).toUpperCase());
                                        if (calCrcHex != null && calCrcHex.length() >= 2) {
                                            calCrcHex = calCrcHex.substring(2) + calCrcHex.substring(0, 2);
                                        }
                                        if (mStrDeyHexPacketData.substring(14, 18).equals(calCrcHex)) {
                                            mStringHexData = mStringEncScanHexData.substring(0, 14);
//                                    System.out.println("mStringHexData="+mStringHexData);
                                            mStringHexData = mStringHexData + mStrDeyHexPacketData;
//                                    System.out.println("mStringHexData="+mStrDeyHexPacketData);
                                            if (mStringHexData.length() < 52) {
                                                int length = 52 - mStringHexData.length();
                                                for (int i = 0; i < length; i++) {
                                                    mStringHexData = mStringHexData + "0";
                                                }
                                            }
                                            mStrDeviceType = mStringHexData.substring(48, 52);
//                                    System.out.println("mStringHexData=" + mStringHexData);
                                            mVoBluetoothDevicesScan = new VoBluetoothDevices();
                                            // used in connection
                                            mVoBluetoothDevicesScan.setBluetoothDevice(bluetoothDevice);
                                            mVoBluetoothDevicesScan.setDeviceAddress(bluetoothDevice.getAddress());
                                            mVoBluetoothDevicesScan.setIsConnected(false);
                                            mVoBluetoothDevicesScan.setDeviceIEEE(mStringHexData.substring(36, 48));
                                            mVoBluetoothDevicesScan.setDeviceHexData(mStringHexData);
                                            mVoBluetoothDevicesScan.setDeviceScanOpcode(mStringHexData.substring(32, 36));
                                            mVoBluetoothDevicesScan.setDeviceType(mStrDeviceType);
                                            if (mStrDeviceType.equalsIgnoreCase("0100")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Light");
                                            } else if (mStrDeviceType.equalsIgnoreCase("0200")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas White Light");
                                            } else if (mStrDeviceType.equalsIgnoreCase("0300")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Switch");
                                            } else if (mStrDeviceType.equalsIgnoreCase("0400")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Socket");
                                            } else if (mStrDeviceType.equalsIgnoreCase("0500")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Fan");
                                            } else if (mStrDeviceType.equalsIgnoreCase("0600")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Strip Light");
                                            } else if (mStrDeviceType.equalsIgnoreCase("0700")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Night Lamp");
                                            } else if (mStrDeviceType.equalsIgnoreCase("0800")) {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Power Strip");
                                            } else {
                                                mVoBluetoothDevicesScan.setDeviceName("Vithamas Light");
                                            }
                                            boolean containsInScanDevice = false;
                                            for (VoBluetoothDevices device : mLeDevicesTemp) {
                                                if (mStringHexData.equals(device.getDeviceHexData())) {
                                                    containsInScanDevice = true;
                                                    break;
                                                }
                                            }
                                            if (!containsInScanDevice) {
                                                mLeDevicesTemp.add(mVoBluetoothDevicesScan);
                                                if (mOnDeviceConnectionStatusChange != null) {
                                                    mOnDeviceConnectionStatusChange.addScanDevices();
                                                    mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothDevicesScan);
                                                }
                                            } else {
                                                for (int j = 0; j < mLeDevicesTemp.size(); j++) {
                                                    if (j < mLeDevicesTemp.size()) {
                                                        if (mStringHexData.equals(mLeDevicesTemp.get(j).getDeviceHexData())) {
                                                            mLeDevicesTemp.set(j, mVoBluetoothDevicesScan);
                                                            if (mOnDeviceConnectionStatusChange != null) {
                                                                mOnDeviceConnectionStatusChange.addScanDevices();
                                                                mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothDevicesScan);
                                                            }
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
//                                }
                                }
                            }
                        }
                    }
                } else if (isPingRequestConnection) {
                    if (encRawData != null) {
                        String mStringHexData = BLEUtility.toHexString(encRawData, true);
                        if (mStringHexData != null && !mStringHexData.equalsIgnoreCase("")) {
                            if (mStringHexData.substring(32, 36).equals(URLCLASS.PING_RSP)) {
                                isPingRequestSent = true;
                                if (mStartPingRequestTimer != null)
                                    mStartPingRequestTimer.cancel();
                            }
                        }
                    }
                }
            }
        }

        @Override
        public void onScanTimeout() {

        }
    };

    private PowerSocketAdvData getGloSmartAdvData(byte[] encRawData) {
        PowerSocketAdvData powerSocketAdvData = null;
        if (encRawData != null) {
            // Check if a device is associated
            byte[] selfDeviceIdBytes = ByteConverter.copyOfRange(encRawData, 3, 5);
            int selfDeviceId = ByteConverter.convertByteArrayToInt(selfDeviceIdBytes);
            byte[] encryptionKey = getEncryptionKey(selfDeviceId);
            byte[] originalData = decryptRawByteDataPowerSocket(encRawData, encryptionKey);
            powerSocketAdvData = PowerSocketAdvData.parse(originalData);
            if (powerSocketAdvData != null) {
//                    displayDataInLog(powerSocketAdvData, data, originalData);
            }
        } else {
            //
        }

        return powerSocketAdvData;
    }

    public static byte[] decryptRawByteDataPowerSocket(byte[] msgRawData, byte[] encryptionKey) {
        byte[] msgRawDecryptedData = new byte[20];
        byte index;
        byte[] msgSeqCrcRawData = new byte[16];
        for (index = 0; index < msgRawData.length; index++) {
            msgRawDecryptedData[index] = msgRawData[index];
        }
        msgSeqCrcRawData[1] = msgRawData[1];
        msgSeqCrcRawData[2] = msgRawData[2];
        msgSeqCrcRawData[3] = msgRawData[3];
        msgSeqCrcRawData[4] = msgRawData[4];

        try {
            byte[] decryptedSeqCrc = EncryptionUtilsPowerSocket.encryptData(msgSeqCrcRawData, encryptionKey);
            for (int i = 0; i < 15; i++) {
                msgRawDecryptedData[i + 5] = (byte) (msgRawDecryptedData[i + 5] ^ decryptedSeqCrc[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return msgRawDecryptedData;
    }

    private byte[] getEncryptionKey(int selfDeviceId) {
        byte[] encryptionKey = null;
        if (selfDeviceId == 0x0000) {
            console.log("scsdcsdc", "Encrypt/Decrypt with default key");
            encryptionKey = PowerSocketConstants.defaultKey;
        } else if (selfDeviceId == 0x0001) {
            console.log("scsdcsdc", "Encrypt/Decrypt with main key");
            // Main Key refers to
            // 1. Skip User key = ~vith => For guest users
            // 2. Login User key = mobile_number + ~vith => For Login users
            String encryptionKeyStr = mPreferenceHelper.getSecretKey();
            if (encryptionKeyStr != null && !encryptionKeyStr.isEmpty()) {
                encryptionKey = ByteConverter.getByteArrayFromHexString(encryptionKeyStr);
            }
        } else {
            encryptionKey = PowerSocketConstants.defaultKey;
        }
        return encryptionKey;
    }

    @Override
    protected void onStart() {
        super.onStart();
        isRequiredToDisconnect = true;
        System.out.println("APP RESUME");
        MyApplication.activityResumed();
        System.gc();
        isLocationEnabled(MainActivity.this);
        registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        if (!isNeverAskPermissionCheck) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                } else {
                    callMarshmallowPermission();
                    return;
                }
            }
        }
        try {
            // start ble scanning and initialize
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = mBluetoothManager.getAdapter();
                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                if (mBluetoothAdapter != null && !mBluetoothAdapter.isEnabled()) {
                    Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, BLUETOOTH_ENABLE_REQUEST);
                } else {
                    registerReceiver(mGpsSwitchStateReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
                    System.out.println("GPS-" + isLocationEnable);
                    if (Build.VERSION.SDK_INT >= 23) {
                        if (!isLocationEnable) {
                            System.out.println("JD-ON RESUME GPS ALERT");
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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                            Intent gattAdvServiceIntent = new Intent(MainActivity.this, AdvertiserService.class);
                            bindService(gattAdvServiceIntent, mAdvertiserServiceConnection, BIND_AUTO_CREATE);
                        }
                    }
                }

                IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                registerReceiver(mBluetoothStateReceiver, filter);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                        IntentFilter filterAdvertiser = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
                        registerReceiver(advertisingFailureReceiver, filterAdvertiser);
                        sendCurrentTimeDevice();
                    }
                }

                bleScanner.setOnBLEScanListener(bleScannerCallback);
                startDeviceScan();
                isReadyToUnbind = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mUtility.haveInternet()) {
            if (!mPreferenceHelper.getIsSkipUser()) {
                checkAuthenticationAPI(false);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideProgress();
        mDrawerLayout.closeDrawer(GravityCompat.START);
        System.out.println("APP PAUSE");
        MyApplication.activityPaused();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("APP DESTROY");
        isRequiredToDisconnect = false;
        // on app destroy stop scan unregister and unbind all service
        try {
            if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                console.log("sKbxKJXBjs", "here!!!");
                stopScan();
                if (isReadyToUnbind) {
                    if (mServiceConnection != null) {
                        unbindService(mServiceConnection);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                            if (mAdvertiserServiceConnection != null) {
                                unbindService(mAdvertiserServiceConnection);
                            }
                        }
                    }
                    if (mGattUpdateReceiver != null) {
                        unregisterReceiver(mGattUpdateReceiver);
                    }
                    if (mBluetoothStateReceiver != null) {
                        unregisterReceiver(mBluetoothStateReceiver);
                    }
                    if (mGpsSwitchStateReceiver != null) {
                        unregisterReceiver(mGpsSwitchStateReceiver);
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                            if (advertisingFailureReceiver != null) {
                                unregisterReceiver(advertisingFailureReceiver);
                            }
                        }
                    }

                    isReadyToUnbind = false;
                }
            }
            if (mTimerResetConnectionStatus != null)
                mTimerResetConnectionStatus.cancel();
            if (mStartPingRequestTimer != null)
                mStartPingRequestTimer.cancel();
            if (mSendMultiAdvertisementTimer != null)
                mSendMultiAdvertisementTimer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (releasePowerSocketBLEServiceCallback != null) {
            releasePowerSocketBLEServiceCallback.onRelease();
        }
    }

    /*Check device version*/
    public static boolean getIsSDKAbove21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public boolean getIsDeviceSupportedAdvertisment() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    /*Initialize Toolbar*/
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        mToolbar.setNavigationIcon(R.drawable.ic_menu);
        View logo = getLayoutInflater().inflate(R.layout.custom_actionbar, null);
        mImageViewBack = logo.findViewById(R.id.custom_action_img_back);
        mImageViewAddDevice = logo.findViewById(R.id.custom_actionbar_imageview_add);
        mTextViewTitle = logo.findViewById(R.id.custom_action_txt_title);
        mTextViewSubTitle = logo.findViewById(R.id.custom_action_txt_sub_title);
        mTextViewAdd = logo.findViewById(R.id.custom_action_txt_add);
        mSwitchCompatOnOff = logo.findViewById(R.id.custom_action_switch_on_off);
        mTextViewAdd.setVisibility(View.GONE);
        mTextViewTitle.setText(R.string.str_dashboard_title);
        mImageViewBack.setOnClickListener(v -> onBackPressed());

        mToolbar.addView(logo);
    }

    /*Connection gatt Filter*/
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == BLUETOOTH_ENABLE_REQUEST) {
            stopScan();
            mLeDevices = new ArrayList<>();
            mLeDevicesTemp = new ArrayList<>();
            startDeviceScan();

        } else if (requestCode == 103) {
            if (resultCode == RESULT_CANCELED && data != null) {
                if (Build.VERSION.SDK_INT >= 23) {
                    callMarshmallowPermission();
                }
            }
        } else {
            try {
                if (data != null) {
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
                    fragment.onActivityResult(requestCode, resultCode, data);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /* Send Message Multiple time Timer*/
    private class sendMultiAdvertisementTimer extends CountDownTimer {
        byte[] mByteArrayFullPackets;

        public sendMultiAdvertisementTimer(long millisInFuture, long countDownInterval, byte[] mByteFullPackets) {
            super(millisInFuture, countDownInterval);
            mByteArrayFullPackets = mByteFullPackets;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAdvertiserService.TransmitMessageOverMesh(mByteArrayFullPackets);
            }
        }

        @Override
        public void onFinish() {

        }
    }

    /*Send Message over ble and Advertisement*/
    public void sendMessageOverBleOrAdvertise(byte[] mByteArrayFullPackets) {
        if (isDevicesConnected || getIsSDKAbove21()) {
            if (getIsSDKAbove21()) {
                if (isDevicesConnected) {
                    System.out.println(TAG + " CONNECTED");
                    mBluetoothLeService.TransmitMessageOverMesh(mByteArrayFullPackets);
                    if (mBluetoothAdapter != null) {
                        if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                            mAdvertiserService.TransmitMessageOverMesh(mByteArrayFullPackets);
                        }
                    }
                } else {
                    System.out.println(TAG + " NOT CONNECTED");
                    if (BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported()) {
                        mAdvertiserService.TransmitMessageOverMesh(mByteArrayFullPackets);
                    } else {
                        connectDeviceWithProgress();
                    }
                }
            } else {
                mBluetoothLeService.TransmitMessageOverMesh(mByteArrayFullPackets);
            }
        } else {
            connectDeviceWithProgress();
        }
    }

    /*Send Current Time To device*/
    public void sendCurrentTimeDevice() {
        if (isDevicesConnected || getIsSDKAbove21()) {
            Timer mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    System.out.println(TAG + "-----TimeSet-----");
                    Calendar mCalendar = Calendar.getInstance();

                    String currentTime = new SimpleDateFormat("HH:mm").format(new Date());
                    System.out.println("CurrentTime-" + currentTime);
                    String[] timeSplit = currentTime.split(":");
                    setAlarmCurrentTime(BLEUtility.intToByte(100), Short.parseShort(0 + ""), BLEUtility.intToByte(mCalendar.get(Calendar.DAY_OF_WEEK)), BLEUtility.intToByte(Integer.parseInt(timeSplit[0])), BLEUtility.intToByte(Integer.parseInt(timeSplit[1])), true);
                }
            }, 5000);
        }
    }

    /*Encrypt Skip user data*/
    public byte[] getEncByteArrayWithSkipUser(byte[] bytePacket) throws Exception {
        String hash256 = BLEUtility.hash256Encryption(URLCLASS.APP_SKIP_PW);
        String mHashSecretKey = (hash256.length() >= 32) ? hash256.substring(hash256.length() - 32, hash256.length()) : hash256;
        Encryption mEncryption = new Encryption(BLEUtility.hexStringToBytes(mHashSecretKey));
        return mEncryption.encrypt(bytePacket);
    }

    /*Encrypt message packet data*/
    public byte[] getEncByteArray(byte[] bytePacket) throws Exception {
        Encryption mEncryption = new Encryption(BLEUtility.hexStringToBytes(mPreferenceHelper.getSecretKey()));
        return mEncryption.encrypt(bytePacket);
    }

    /*Encrypt Association message packet data*/
    public byte[] getAssEncByteArray(byte[] bytePacket) throws Exception {
        Encryption mEncryption = new Encryption(BLEUtility.hexStringToBytes(BuildConfig.VDKApiKey));
        return mEncryption.encrypt(bytePacket);
    }

    /*Check CRS message packet data*/
    public int checkCrcIsValidPacketData(byte decRawData[]) {
        byte msgSeqCrcEncryptMask[] = new byte[decRawData.length - 1];
        byte index;
        for (index = 0; index < msgSeqCrcEncryptMask.length; index++) {
            msgSeqCrcEncryptMask[index] = decRawData[index + 1];
        }
        msgSeqCrcEncryptMask[6] = (byte) ((allGroupDeviceId) & 0x00FF);
        msgSeqCrcEncryptMask[7] = (byte) ((allGroupDeviceId >> 8) & 0x00FF);
        return (BLEUtility.crc16ByteCalculation(msgSeqCrcEncryptMask, msgSeqCrcEncryptMask.length) & 0xFFFF);
    }

    /*Decrypt ble message packet data*/
    public byte[] decryptRawByteData(byte msgRawData[], boolean isAssociationRequest) {
        byte msgRawDecryptedData[] = new byte[20];
        byte index;
        byte msgSeqCrcRawData[] = new byte[16];
        /* Initialize the array with zeros */
        for (index = 0; index < 20; index++) {
            msgRawDecryptedData[index] = 0;
            if (index < 16) {
                msgSeqCrcRawData[index] = 0;
            }
        }
        for (index = 0; index < msgRawData.length; index++) {
            msgRawDecryptedData[index] = msgRawData[index];
        }
        msgSeqCrcRawData[1] = msgRawData[1];
        msgSeqCrcRawData[2] = msgRawData[2];
        msgSeqCrcRawData[3] = msgRawData[3];
        msgSeqCrcRawData[4] = msgRawData[4];

        try {
            byte decryptedSeqCrc[];
            if (isAssociationRequest) {
                decryptedSeqCrc = getAssEncByteArray(msgSeqCrcRawData);
            } else {
                decryptedSeqCrc = getEncByteArray(msgSeqCrcRawData);
            }
            for (int i = 0; i < 15; i++) {
                msgRawDecryptedData[i + 5] = (byte) (msgRawDecryptedData[i + 5] ^ decryptedSeqCrc[i]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        msgRawDecryptedData = Arrays.copyOfRange(msgRawDecryptedData, 0, msgRawData.length);
        return msgRawDecryptedData;
    }


    /* Message Format
     * [0]   [1-2]     [3-4]       [5-6]    [7-8]  [9-10]   [11-19]
     * TTL  SEQ_NO SELF_DEV_ID  DEV_DEST_ID  CRC   OPCODE  MESH MSG
     * */
    /*Transmit All ble Message With encryption*/
    public void TransmitMessageOverMesh(byte ttl, short device_dest_id, byte msgPacket[], boolean isAllGroupDevice, boolean isAssociationRequest, boolean isUseSkipUserKey) {
        try {
            // Sequence and crc packet data
            byte msgEncryptMask[] = new byte[16];
            byte msgCrcMask[];
            // Raw packet data
            byte msgRawData[] = new byte[20];
//            short msgPacketData[] = new short[20];
            byte msgRawDataIndex = 0, index;
            int crcVal = 0;
            if (!isAssociationRequest) {
                if (msgPacket.length > 16 || msgPacket.length == 0) {
                    return;
                }
            }
            if (isAssociationRequest) {
                if (isUseSkipUserKey) {
                    msgCrcMask = new byte[8 + 2];
                } else {
                    msgCrcMask = new byte[8 + 10];
                }
            } else {
                msgCrcMask = new byte[8 + msgPacket.length];
            }
            /* Initialize the array with zeros */
            for (index = 0; index < msgRawData.length; index++) {
                msgRawData[index] = 0;
//                msgPacketData[index] = 0;
                if (index < 16) {
                    msgEncryptMask[index] = 0;
                }
            }
            /* Add ttl value */
            msgRawData[msgRawDataIndex++] = ttl;

            /* Add Seq No */
            msgRawData[msgRawDataIndex++] = (byte) ((sequenceNo) & 0x00FF);
            msgRawData[msgRawDataIndex++] = (byte) ((sequenceNo >> 8) & 0x00FF);
            msgEncryptMask[1] = msgRawData[1];
            msgEncryptMask[2] = msgRawData[2];
            msgCrcMask[0] = msgRawData[1];
            msgCrcMask[1] = msgRawData[2];
            sequenceNo++;
            if (sequenceNo == 65535) {
                sequenceNo = 1;
            }
            selfDeviceId = Integer.parseInt(mPreferenceHelper.getSelfDeviceId());
            /* Add self device id onto msg */
            if (isAssociationRequest) {
                msgRawData[msgRawDataIndex++] = (byte) ((selfDeviceIdAssociate) & 0x00FF);
                msgRawData[msgRawDataIndex++] = (byte) ((selfDeviceIdAssociate >> 8) & 0x00FF);
            } else {
                msgRawData[msgRawDataIndex++] = (byte) ((selfDeviceId) & 0x00FF);
                msgRawData[msgRawDataIndex++] = (byte) ((selfDeviceId >> 8) & 0x00FF);
            }
            msgEncryptMask[3] = msgRawData[3];
            msgEncryptMask[4] = msgRawData[4];
            msgCrcMask[2] = msgRawData[3];
            msgCrcMask[3] = msgRawData[4];
            /* Add dest device id onto msg */
            if (isAllGroupDevice) {
                msgRawData[msgRawDataIndex++] = (byte) ((allGroupDeviceId) & 0x00FF);
                msgRawData[msgRawDataIndex++] = (byte) ((allGroupDeviceId >> 8) & 0x00FF);
            } else {
                msgRawData[msgRawDataIndex++] = (byte) ((device_dest_id) & 0x00FF);
                msgRawData[msgRawDataIndex++] = (byte) ((device_dest_id >> 8) & 0x00FF);
            }
            msgCrcMask[4] = msgRawData[5];
            msgCrcMask[5] = msgRawData[6];

            msgCrcMask[6] = (byte) ((selfDeviceIdAssociate) & 0x00FF);
            msgCrcMask[7] = (byte) ((selfDeviceIdAssociate >> 8) & 0x00FF);

            /* copy the received opcode and parameter value onto the msg */
            int bleAssCount = 9;
            for (index = 0; index < msgPacket.length; index++) {
                if (isAssociationRequest) {
                    if (isUseSkipUserKey) {
                        if (index >= 3 && index < 9) {
                            msgEncryptMask[bleAssCount++] = msgPacket[index];
                        } else {
                            msgRawData[index + 9] = msgPacket[index];
                            msgCrcMask[index + 8] = msgPacket[index];
                        }
                    } else {
                        if (index >= 10 && index < 16) {
                            msgEncryptMask[bleAssCount++] = msgPacket[index];
                        } else {
                            msgRawData[index + 9] = msgPacket[index];
                            msgCrcMask[index + 8] = msgPacket[index];
                        }
                    }
                } else {
                    msgRawData[index + 9] = msgPacket[index];
                    msgCrcMask[index + 8] = msgPacket[index];
                }
            }
            crcVal = BLEUtility.crc16ByteCalculation(msgCrcMask, msgCrcMask.length);
//            System.out.println("crc_val_HEX=" + BLEUtility.intToByte(crcVal & 0xFFFF));
//            System.out.println("crc_val=" + crcVal);
            msgRawData[msgRawDataIndex++] = (byte) ((crcVal) & 0x00FF);
            msgRawData[msgRawDataIndex++] = (byte) ((crcVal >> 8) & 0x00FF);

            byte encryptedSeqCrc[];
            if (isAssociationRequest) {
                encryptedSeqCrc = getAssEncByteArray(msgEncryptMask);
            } else {
                if (isUseSkipUserKey) {
                    encryptedSeqCrc = getEncByteArrayWithSkipUser(msgEncryptMask);
                } else {
                    encryptedSeqCrc = getEncByteArray(msgEncryptMask);
                }
            }
            for (int i = 0; i < 15; i++) {
//                if (i != 3) {
                msgRawData[i + 5] = (byte) (msgRawData[i + 5] ^ encryptedSeqCrc[i]);
//                }
            }
//            System.out.println("mStringHexEncPacketData=" + BLEUtility.toHexString(msgRawData, true));
            byte[] sliceMsgRawData;
            if (isAssociationRequest) {
                if (isUseSkipUserKey) {
                    sliceMsgRawData = Arrays.copyOfRange(msgRawData, 0, 9 + 2);
                } else {
                    sliceMsgRawData = Arrays.copyOfRange(msgRawData, 0, 9 + 10);
                }

            } else {
                sliceMsgRawData = Arrays.copyOfRange(msgRawData, 0, 9 + msgPacket.length);
            }
//            System.out.println("mStringHexEncPacketDataSlice=" + BLEUtility.toHexString(sliceMsgRawData, true));
            /* write the message onto the characteristics based on its size */
            if (mPreferenceHelper != null) {
                mPreferenceHelper.setDeviceSequenceNo(sequenceNo);
            }
            sendMessageOverBleOrAdvertise(sliceMsgRawData);
        } catch (Exception e) {
            e.printStackTrace();
            hideProgress();
        }
    }

    /*Send ble message association request key part 1*/
    public void setAssRequestKeyPart(byte dev_ttl, short dest_id, byte[] ble_add, boolean isPart1) {
        byte msg_index = 0, index;
        byte add_value[] = new byte[16];
        short opcode = URLCLASS.ASSOC_REQ_PART_1;
        if (!isPart1) {
            opcode = URLCLASS.ASSOC_REQ_PART_2;
        }
        // Add Opcode
        add_value[0] = (byte) (opcode & 0x00FF);
        msg_index++;
        add_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        msg_index++;

        String pwPartHex = "";
        String encryptedSecretKey = mPreferenceHelper.getSecretKey();
        if (isPart1) {
            pwPartHex = encryptedSecretKey.substring(0, 16);
        } else {
            pwPartHex = encryptedSecretKey.substring(16, 32);
        }
        byte[] keyByteData = BLEUtility.hexStringToBytes(pwPartHex);
        // Add key part
        for (index = 0; index < keyByteData.length; index++) {
            add_value[msg_index++] = keyByteData[index];
        }
        // Add Device Address
        for (index = 0; index < ble_add.length; index++) {
            add_value[msg_index++] = ble_add[index];
        }
//        add_value[10] = ble_add[1];
//        add_value[11] = ble_add[0];
//        add_value[12] = ble_add[3];
//        add_value[13] = ble_add[2];
//        add_value[14] = ble_add[5];
//        add_value[15] = ble_add[4];
        TransmitMessageOverMesh(dev_ttl, dest_id, add_value, false, true, false);
    }

    /*Send ble message association request*/
    public void setCheckDevice(byte dev_ttl, short dest_id, byte[] ble_add, boolean isPart1) {
        byte msg_index = 0, index;
        byte check_device_value[] = new byte[16];
        short opcode = URLCLASS.DEVICE_CHECK_REQ;
        // Add Opcode
        check_device_value[0] = (byte) (opcode & 0x00FF);
        msg_index++;
        check_device_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        msg_index++;
        String pwPartHex;
        String encryptedSecretKey = mPreferenceHelper.getSecretKey();
        if (isPart1) {
            pwPartHex = encryptedSecretKey.substring(0, 16);
        } else {
            pwPartHex = encryptedSecretKey.substring(16, 32);
        }
        byte[] keyByteData = BLEUtility.hexStringToBytes(pwPartHex);
        // Add key part
        for (index = 0; index < keyByteData.length; index++) {
            check_device_value[msg_index++] = keyByteData[index];
        }
        // Add Device Address
        for (index = 0; index < ble_add.length; index++) {
            check_device_value[msg_index++] = ble_add[index];
        }
        TransmitMessageOverMesh(dev_ttl, dest_id, check_device_value, false, true, false);
    }

    /* Add Device TO Group Message Format
     * [1]   [2-3]   [4-5]   [6-7]    [8-9]  [10-11]   [12]    [13-14]
     * TTL   SEQ NO  DEV ID  DEST ID  CRC    OPCODE  Static byte    UID(RandomDigit)
     * */
    /*Add ble device to group*/
    public void addDeviceToGroup(byte dev_ttl, short dest_id, short groupId) {
//        byte msg_index = 0, index;
        byte add_value[] = new byte[5];
        short opcode = URLCLASS.GROUP_ADD_REQ;
        short temp = 0x01;
        // Add Opcode
        add_value[0] = (byte) (opcode & 0x00FF);
//        msg_index++;
        add_value[1] = (byte) ((opcode >> 8) & 0x00FF);
//        msg_index++;
        // Static Ox01
        add_value[2] = (byte) (temp & 0xFF);
//        msg_index++;

        // Add Group Address
//        for (index = 0; index < groupId.length; index++) {
//            add_value[msg_index++] = groupId[index];
//        }
//        add_value[3] = groupId[0];
//        add_value[4] = groupId[1];
        add_value[3] = (byte) ((groupId) & 0x00FF);
        add_value[4] = (byte) ((groupId >> 8) & 0x00FF);

        TransmitMessageOverMesh(dev_ttl, dest_id, add_value, false, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param value write the light value.
     */
    public void setWhiteLightLevel(byte dev_ttl, short value, short dest_id, boolean isAllGroupDevice) {
        byte light_value[] = new byte[3];
        short opcode = URLCLASS.COLOR_WHITE_LIGHT_LEVEL;

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);

        light_value[2] = (byte) ((value >> 8) & 0x00FF);
//        light_value[2] = value;

        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isAllGroupDevice, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param colorRGB represents rgb values and brightness gives level.
     */
    /*send light color with brightness*/
    public void setLightColorWithBrightness(byte dev_ttl, int colorRGB, short dest_id, boolean isAllGroupDevice) {
        short opcode = URLCLASS.COLOR_CHANGE_COLOR;
        byte light_value[] = new byte[5];
//        float[] hsv = new float[3];
//        int color_to_send;
//        Color.colorToHSV(color, hsv);
//        hsv[2] = ((float) brightness + 1) / 100.0f;
//        color_to_send = Color.HSVToColor(hsv);


        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);

        light_value[2] = (byte) (Color.red(colorRGB) & 0xFF);
        light_value[3] = (byte) (Color.green(colorRGB) & 0xFF);
        light_value[4] = (byte) (Color.blue(colorRGB) & 0xFF);

        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isAllGroupDevice, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param color represents rgb values and brightness gives level.
     */
    /*send light color*/
    public void setLightColor(byte dev_ttl, int color, int brightness, short dest_id, boolean isAllGroupDevice) {

        short opcode = URLCLASS.COLOR_CHANGE_COLOR;
        byte light_value[] = new byte[5];
        float[] hsv = new float[3];
        float[] hsvReverse = new float[3];
        int color_to_send;
        Color.colorToHSV(color, hsv);
        hsv[2] = ((float) brightness + 1) / 100.0f;
        color_to_send = Color.HSVToColor(hsv);

        Color.colorToHSV(color_to_send, hsvReverse);
        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);

        light_value[2] = (byte) (Color.red(color_to_send) & 0xFF);
        light_value[3] = (byte) (Color.green(color_to_send) & 0xFF);
        light_value[4] = (byte) (Color.blue(color_to_send) & 0xFF);
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isAllGroupDevice, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param color represents rgb values.
     */
    /*send white light color*/
    public void setWhiteLightColor(byte dev_ttl, int color, int brightness, short dest_id, boolean isAllGroupDevice) {

        short opcode = URLCLASS.COLOR_WHITE_LIGHT;

        byte light_value[] = new byte[5];
        float[] hsv = new float[3];
        int color_to_send;
        Color.colorToHSV(color, hsv);
        hsv[2] = ((float) brightness + 1) / 100.0f;
        color_to_send = Color.HSVToColor(hsv);

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);

        light_value[2] = (byte) (Color.red(color_to_send) & 0xFF);
        light_value[3] = (byte) (Color.green(color_to_send) & 0xFF);
        light_value[4] = (byte) (Color.blue(color_to_send) & 0xFF);
//        TransmitMessageOverMesh1(dev_ttl, dest_id, light_value, (byte) 5, isAllGroupDevice);
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isAllGroupDevice, false, false);
    }

    /*send color RGB*/
    public void setLightColorRGB(byte dev_ttl, byte red, byte green, byte blue, byte white, short dest_id, boolean isAllGroupDevice, boolean use45Code) {
        short opcode;
        if (use45Code) {
            opcode = URLCLASS.COLOR_WHITE_LIGHT_45;
        } else {
            opcode = URLCLASS.COLOR_CHANGE_COLOR;
        }
        byte light_value[] = new byte[6];
        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        light_value[2] = red;
        light_value[3] = green;
        light_value[4] = blue;
        light_value[5] = white;
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isAllGroupDevice, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param OnOff represents device on(1) or off(0).
     */
    /*send data to turn on/off light*/
    public void setLightOnOff(byte dev_ttl, byte OnOff, short dest_id, boolean isFromGroup) {
        short opcode = URLCLASS.COLOR_ON_OFF_LIGHT;
        byte light_value[] = new byte[3];

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        light_value[2] = OnOff;
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isFromGroup, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param dest_id .
     */
    /*send ping request command*/
    public void sendAdvertisePingRequest(byte dev_ttl, short dest_id, boolean isFromGroup) {
        short opcode = URLCLASS.PING_REQ;
        byte light_value[] = new byte[2];

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isFromGroup, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param patternNo represents device color pattern.
     */
    /*Change pattern color command*/
    public void changePatternColor(byte dev_ttl, byte patternNo, short dest_id, boolean isFromGroup) {
        short opcode = URLCLASS.COLOR_CHANGE_PATTERN;
        byte light_value[] = new byte[3];

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        light_value[2] = patternNo;
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isFromGroup, false, false);
    }

    /*Change pattern color with color command*/
    public void changePatternColorWithColor(byte dev_ttl, byte patternNo, byte red, byte green, byte blue, short dest_id, boolean isFromGroup) {
        short opcode = URLCLASS.COLOR_CHANGE_PATTERN;
        byte light_value[] = new byte[6];

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);

        light_value[2] = patternNo;
        light_value[3] = red;
        light_value[4] = green;
        light_value[5] = blue;
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isFromGroup, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param isAllGroupDevice represents deassociate all device if true other wise particular device.
     */
    /*Delete device command*/
    public void resetAllDevice(byte dev_ttl, short dest_id, boolean isAllGroupDevice, boolean isUseSkipUserKey) {
        short opcode = URLCLASS.ASSOC_REMOVE_REQ;
        byte light_value[] = new byte[2];

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isAllGroupDevice, false, isUseSkipUserKey);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param isAllGroupDevice represents deassociate all device if true other wise particular device.
     */
    /*Delete All group device command*/
    public void deleteAllGroupDevice(byte dev_ttl, short dest_id, short group_id, boolean isAllGroupDevice) {
//        short opcode = 0x0037;
        short opcode = URLCLASS.GROUP_REMOVE_REQ;
        short NoOfGroups = 0x01;
        byte light_value[] = new byte[5];
        System.out.println("getDeviceBOTH ID-" + dest_id + "-" + group_id);

        light_value[0] = (byte) (opcode & 0x00FF);
        light_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        light_value[2] = (byte) (NoOfGroups & 0xFF);
        light_value[3] = (byte) ((group_id) & 0x00FF);
        light_value[4] = (byte) ((group_id >> 8) & 0x00FF);
        TransmitMessageOverMesh(dev_ttl, dest_id, light_value, isAllGroupDevice, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param day represents current time all device if true other wise particular device.
     */
    /*Set Alarm current Time*/
    public void setAlarmCurrentTime(byte dev_ttl, short dest_id, byte day, byte hour, byte minute, boolean isAllGroupDevice) {
        short opcode = URLCLASS.ALARM_SET_CURRENT_TIME_REQ;
        byte time_value[] = new byte[5];

        time_value[0] = (byte) (opcode & 0x00FF);
        time_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        time_value[2] = day;
        time_value[3] = hour;
        time_value[4] = minute;
        TransmitMessageOverMesh(dev_ttl, dest_id, time_value, isAllGroupDevice, false, false);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param alarmID represents current time all device if true other wise particular device.
     */
    /*Set Alarm For day*/
    public void setAlarmForDay(byte dev_ttl, short dest_id, byte alarmID, byte alarmMask, byte alarmHour, byte alarmMinute, byte colorRed, byte colorGreen, byte colorBlue, byte wakeUpSleep, boolean isAllGroupDevice) {
        short opcode = URLCLASS.ALARM_ADD_REQ;
        byte alarm_value[] = new byte[10];

        alarm_value[0] = (byte) (opcode & 0x00FF);
        alarm_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        alarm_value[2] = alarmID;
        alarm_value[3] = alarmMask;
        alarm_value[4] = alarmHour;
        alarm_value[5] = alarmMinute;
        alarm_value[6] = colorRed;
        alarm_value[7] = colorGreen;
        alarm_value[8] = colorBlue;
        alarm_value[9] = wakeUpSleep;
        TransmitMessageOverMesh(dev_ttl, dest_id, alarm_value, isAllGroupDevice, false, false);
    }

    /*Delete Alarm For day*/
    public void deleteAlarmForDay(byte dev_ttl, short dest_id, byte alarmID, boolean isAllGroupDevice) {
        short opcode = URLCLASS.ALARM_REMOVE_REQ;
        byte alarm_value[] = new byte[3];

        alarm_value[0] = (byte) (opcode & 0x00FF);
        alarm_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        alarm_value[2] = alarmID;
        TransmitMessageOverMesh(dev_ttl, dest_id, alarm_value, isAllGroupDevice, false, false);
    }

    /*Set Music light*/
    public void setLightMusic(byte dev_ttl, byte loudness_value, short dest_id, boolean isAllGroupDevice) {
        short opcode = URLCLASS.COLOR_MUSIC_LIGHT;
        byte music_value[] = new byte[3];
        music_value[0] = (byte) (opcode & 0x00FF);
        music_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        music_value[2] = loudness_value;

        TransmitMessageOverMesh(dev_ttl, dest_id, music_value, isAllGroupDevice, false, false);
    }

    /*Set Music light state*/
    public void setLightLastState(byte dev_ttl, byte state, short dest_id, boolean isAllGroupDevice) {
        short opcode = URLCLASS.COLOR_LIGHT_STATE;
        byte state_value[] = new byte[3];
        state_value[0] = (byte) (opcode & 0x00FF);
        state_value[1] = (byte) ((opcode >> 8) & 0x00FF);
        state_value[2] = state;

        TransmitMessageOverMesh(dev_ttl, dest_id, state_value, isAllGroupDevice, false, false);
    }

    /*check local db and fetch unsync device data and send to server and get the data and store in local database.*/
    public class syncDeviceDataAsyncTask extends AsyncTask<String, Void, String> {
        String result = "success";
        ArrayList<VoDeviceList> mArrayListUnSyncDevice = new ArrayList<>();
        int currentLoopPosition = 0;
        boolean showProgress = false;

        public syncDeviceDataAsyncTask(boolean isShowProgress) {
            this.showProgress = isShowProgress;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentLoopPosition = 0;
            mArrayListUnSyncDevice = new ArrayList<>();
        }

        @Override
        protected String doInBackground(String... params) {
            DataHolder mDataHolderDevice;
            try {
                // fetch un sync data from local database
                String url = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceIsSync + "= '0'" +
                        " AND " + DBHelper.mFieldDeviceUserId + "= '" + mPreferenceHelper.getUserId() + "'";
                mDataHolderDevice = mDbHelper.read(url);
                if (mDataHolderDevice != null) {
                    VoDeviceList mVoDeviceList;
                    for (int i = 0; i < mDataHolderDevice.get_Listholder().size(); i++) {
                        mVoDeviceList = new VoDeviceList();
                        mVoDeviceList.setDevicLocalId(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceLocalId));
                        mVoDeviceList.setDeviceServerid(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceServerId));
                        mVoDeviceList.setUser_id(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceUserId));
                        mVoDeviceList.setDevice_Comm_id(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommID));
                        mVoDeviceList.setDevice_Comm_hexId(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommHexId));
                        mVoDeviceList.setDevice_name(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceName));
                        mVoDeviceList.setDevice_realName(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceRealName));
                        mVoDeviceList.setDevice_BleAddress(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceBleAddress));
                        mVoDeviceList.setDevice_Type(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceType));
                        mVoDeviceList.setDevice_type_name(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceTypeName));
                        mVoDeviceList.setDevice_ConnStatus(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldConnectStatus));
                        mVoDeviceList.setDevice_SwitchStatus(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldSwitchStatus));
                        mVoDeviceList.setDevice_is_favourite(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsFavourite));
                        mVoDeviceList.setDevice_last_state_remember(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceLastState));
                        mVoDeviceList.setDevice_timestamp(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceTimeStamp));
                        mVoDeviceList.setDevice_is_active(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsActive));
                        mVoDeviceList.setDevice_created_at(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceCreatedAt));
                        mVoDeviceList.setDevice_updated_at(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceUpdatedAt));
                        mVoDeviceList.setDevice_is_sync(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsSync));
                        mVoDeviceList.setIsWifiConfigured(Integer.parseInt(mDataHolderDevice.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsWifiConfigured)));

                        if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                            mVoDeviceList.setIsChecked(true);
                        } else {
                            mVoDeviceList.setIsChecked(false);
                        }
                        mArrayListUnSyncDevice.add(mVoDeviceList);
                    }
                }

            } catch (Exception e) {
                result = "fail";
                e.printStackTrace();
            }
            if (mArrayListUnSyncDevice.size() > 0) {
                // Send Un sync Data to server one by one.
                String response = sendDeviceDataToServer();
                System.out.println("forwardLoop-response-" + response);
                if (response.equalsIgnoreCase("fail")) {
                    result = "fail";
                    return result;
                }
            } else {
                if (mUtility.haveInternet()) {
                    if (!mPreferenceHelper.getIsSkipUser()) {
                        if (isFromLogin) {
                            getDeviceListAPI(true);
                        } else {
                            getDeviceListAPI(false);
                        }
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mArrayListUnSyncDevice.size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                    }
                });
            }
        }

        private String sendDeviceDataToServer() {
            String loopResult = "success";
            if (currentLoopPosition >= mArrayListUnSyncDevice.size()) {
                loopResult = "finish";
                return loopResult;
            }
            addDeviceAPI(mArrayListUnSyncDevice.get(currentLoopPosition));
            return loopResult;
        }

        // Send unSync Device data to server
        public void addDeviceAPI(final VoDeviceList mVoDeviceList) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("device_token", mPreferenceHelper.getDeviceToken());
            params.put("user_id", mPreferenceHelper.getUserId());
            params.put("device_id", mVoDeviceList.getDevice_Comm_id());
            params.put("hex_device_id", mVoDeviceList.getDevice_Comm_hexId().toLowerCase());
            params.put("device_name", mVoDeviceList.getDevice_name());
            String queryDeviceTypeId = "select " + DBHelper.mFieldDeviceTypeServerID + " from " + DBHelper.mTableDeviceType +
                    " where " + DBHelper.mFieldDeviceTypeType + "= '" + mVoDeviceList.getDevice_Type() + "'";
            String deviceTypeServerID = mDbHelper.getQueryResult(queryDeviceTypeId);
            System.out.println("deviceTypeServerID=" + deviceTypeServerID);
            params.put("device_type", deviceTypeServerID);
            params.put("ble_address", mVoDeviceList.getDevice_BleAddress().toUpperCase());
            // is_favourite 1-YES,2-NO
            // status 1-Active,2-deActive
            // is_update  0-Insert, 1-Update
            params.put("is_favourite", "2");
            params.put("status", "1");
            params.put("remember_last_color", "0");

            if (mVoDeviceList.getIsWifiConfigured()) {
                params.put("wifi_configured", "1");
            } else {
                params.put("wifi_configured", "0");
            }

            if (mVoDeviceList.getDevice_is_favourite() != null && !mVoDeviceList.getDevice_is_favourite().equalsIgnoreCase("") && !mVoDeviceList.getDevice_is_favourite().equalsIgnoreCase("null")) {
                if (mVoDeviceList.getDevice_is_favourite().equalsIgnoreCase("1")) {
                    params.put("is_favourite", "1");
                }
            }
            if (mVoDeviceList.getDevice_last_state_remember() != null && !mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("") && !mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("null")) {
                if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("0")) {
                    params.put("remember_last_color", "0");
                } else if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("1")) {
                    params.put("remember_last_color", "1");
                } else if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("2")) {
                    params.put("remember_last_color", "2");
                } else if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("3")) {
                    params.put("remember_last_color", "3");
                }
            }
            if (mVoDeviceList.getDeviceServerId() != null && !mVoDeviceList.getDeviceServerId().equalsIgnoreCase("") && !mVoDeviceList.getDeviceServerId().equalsIgnoreCase("null")) {
                params.put("is_update", "1");// 1-Update device
                if (mVoDeviceList.getDevice_is_active() != null && !mVoDeviceList.getDevice_is_active().equalsIgnoreCase("") && !mVoDeviceList.getDevice_is_active().equalsIgnoreCase("null")) {
                    if (mVoDeviceList.getDevice_is_active().equalsIgnoreCase("0")) {
                        params.put("status", "2"); // 2- deleted device
                    }
                }
            } else {
                params.put("is_update", "0"); // 0- Add device
                params.put("status", "1"); //  1-not deleted device
            }

            Call<VoAddDeviceData> mLogin = mApiService.addDeviceAPI(params);
            if (BuildConfig.DEBUG) {
                System.out.println("params-" + params.toString());
                System.out.println("URL-" + mLogin.request().url().toString());
            }
            mLogin.enqueue(new Callback<VoAddDeviceData>() {
                @Override
                public void onResponse(Call<VoAddDeviceData> call, Response<VoAddDeviceData> response) {
                    VoAddDeviceData mAddDeviceAPI = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mAddDeviceAPI);
                    if (mAddDeviceAPI != null && mAddDeviceAPI.getResponse().equalsIgnoreCase("true")) {
                        if (mAddDeviceAPI.getData() != null) {
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(DBHelper.mFieldDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                            mContentValues.put(DBHelper.mFieldDeviceIsSync, "1");
                            String[] mArray = new String[]{mVoDeviceList.getDevicLocalId()};
                            mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                                    DBHelper.mFieldDeviceLocalId + "=?", mArray);

                            ContentValues mContentValuesSocket = new ContentValues();
                            mContentValuesSocket.put(DBHelper.mFieldSocketDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                            String[] mArraySocket = new String[]{mVoDeviceList.getDevicLocalId()};
                            mDbHelper.updateRecord(DBHelper.mTablePowerStripSocket, mContentValuesSocket,
                                    DBHelper.mFieldSocketDeviceLocalId + "=?", mArraySocket);
                            if (currentLoopPosition == mArrayListUnSyncDevice.size() - 1) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        hideProgress();
                                    }
                                });

                                if (mUtility.haveInternet()) {
                                    if (!mPreferenceHelper.getIsSkipUser()) {
                                        mArrayListUnSyncDevice = new ArrayList<>();
                                        if (isFromLogin) {
                                            getDeviceListAPI(true);
                                        } else {
                                            getDeviceListAPI(false);
                                        }
                                    }
                                }
                            } else {
                                currentLoopPosition++;
                                sendDeviceDataToServer();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<VoAddDeviceData> call, Throwable t) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });
                    t.printStackTrace();
                    if (!mPreferenceHelper.getIsSkipUser()) {
                        mArrayListUnSyncDevice = new ArrayList<>();
                        if (isFromLogin) {
                            getDeviceListAPI(true);
                        } else {
                            getDeviceListAPI(false);
                        }
                    }
                }
            });
        }
    }

    /*Get Device List Data from Server and store in local db*/
    public void getDeviceListAPI(final boolean isShowProgress) {
        if (isShowProgress) {
            mUtility.hideKeyboard(MainActivity.this);
//            mUtility.ShowProgress("Please Wait..");
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mPreferenceHelper.getUserId());

        Call<VoServerDeviceList> mVoServerDeviceListCall = mApiService.getAllDeviceListAPI(params);
        if (BuildConfig.DEBUG) {
            System.out.println("params-" + params.toString());
            System.out.println("URL-" + mVoServerDeviceListCall.request().url().toString());
        }
        mVoServerDeviceListCall.enqueue(new Callback<VoServerDeviceList>() {
            @Override
            public void onResponse(Call<VoServerDeviceList> call, Response<VoServerDeviceList> response) {
                if (!isFinishing()) {
                    hideProgress();
                    Gson mGson = new Gson();
                    mPreferenceHelper.setIsDeviceSync(true);

                    VoServerDeviceList mVoServerDeviceList = response.body();
                    System.out.println("onResponse=" + mGson.toJson(mVoServerDeviceList));
                    console.log("asbjxakjsbx", mGson.toJson(mVoServerDeviceList));
                    new InsertDeviceDataInDbAsyncTask(mVoServerDeviceList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");

//                    if (mOnSyncComplete != null) {
//                        mOnSyncComplete.onDeviceSyncComplete();
//                    }
//                    if (mUtility.haveInternet()) {
//                        if (!isFromLogin) {
//                            new DeviceTypeListAsyncTask(false).execute("");
//                        }
//                    }
//                    isFromLogin = false;
                }
            }

            @Override
            public void onFailure(Call<VoServerDeviceList> call, Throwable t) {
                hideProgress();
                if (mUtility.haveInternet()) {
                    if (!isFromLogin) {
                        new DeviceTypeListAsyncTask(false).execute("");
                    }
                }
                isFromLogin = false;
            }
        });
    }

    /*send un Sync group data with server*/
    public class syncGroupDataAsyncTask extends AsyncTask<String, Void, String> {
        String result = "success";
        ArrayList<VoLocalGroupData> mArrayListUnSyncGroup = new ArrayList<>();
        int currentLoopPosition = 0;
        boolean showProgress = false;

        public syncGroupDataAsyncTask(boolean isShowProgress) {
            this.showProgress = isShowProgress;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentLoopPosition = 0;
            mArrayListUnSyncGroup = new ArrayList<>();

        }

        @Override
        protected String doInBackground(String... params) {
            DataHolder mDataHolder;
            try {
                String url = "select * from " + DBHelper.mTableGroup + " where " + DBHelper.mFieldGroupIsSync + "= '0'" + " AND " +
                        DBHelper.mFieldGroupUserId + "= '" + mPreferenceHelper.getUserId() + "'";
                mDataHolder = mDbHelper.read(url);
                if (mDataHolder != null) {
                    VoLocalGroupData mVoGroupList;
                    DataHolder mDataHolderDevice;
                    ArrayList<VoDeviceList> mArrayListUnSyncGroupDeviceList;
                    for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                        mVoGroupList = new VoLocalGroupData();
                        mVoGroupList.setGroup_local_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupLocalID));
                        mVoGroupList.setGroup_server_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupServerID));
                        mVoGroupList.setUser_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupUserId));
                        mVoGroupList.setGroup_comm_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupCommId));
                        mVoGroupList.setGroup_comm_hex_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupCommHexId));
                        mVoGroupList.setGroup_name(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupName));
                        mVoGroupList.setGroup_switch_status(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupDeviceSwitchStatus));
                        mVoGroupList.setGroup_is_favourite(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupIsFavourite));
                        mVoGroupList.setGroup_timestamp(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupTimeStamp));
                        mVoGroupList.setGroup_is_active(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupIsActive));
                        mVoGroupList.setGroup_created_at(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupCreatedAt));
                        mVoGroupList.setGroup_updated_at(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupUpdatedAt));
                        mVoGroupList.setGroup_is_sync(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupIsSync));
                        if (mVoGroupList.getGroup_switch_status() != null && mVoGroupList.getGroup_switch_status().equalsIgnoreCase("ON")) {
                            mVoGroupList.setIsGroupChecked(true);
                        } else {
                            mVoGroupList.setIsGroupChecked(false);
                        }
                        mArrayListUnSyncGroupDeviceList = new ArrayList<>();

                        try {
                            String urlDevice;
                            if (mVoGroupList.getGroup_is_active() != null && !mVoGroupList.getGroup_is_active().equalsIgnoreCase("") && mVoGroupList.getGroup_is_active().equalsIgnoreCase("0")) {
                                urlDevice = "select * from " + DBHelper.mTableDevice + " inner join " + DBHelper.mTableGroupDeviceList +
                                        " on " + DBHelper.mFieldGDListLocalDeviceID + "= "
                                        + DBHelper.mFieldDeviceLocalId + " AND " + DBHelper.mFieldGDListStatus + "= 0" +
                                        " where " + DBHelper.mFieldGDListLocalGroupID + "= '" +
                                        mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupLocalID) + "'";
                            } else {
                                urlDevice = "select * from " + DBHelper.mTableDevice + " inner join " +
                                        DBHelper.mTableGroupDeviceList + " on " + DBHelper.mFieldGDListLocalDeviceID + "= " +
                                        DBHelper.mFieldDeviceLocalId + " AND " + DBHelper.mFieldGDListStatus + "= 1" + " where " +
                                        DBHelper.mFieldGDListLocalGroupID + "= '" +
                                        mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupLocalID) + "'";
                            }

                            mDataHolderDevice = mDbHelper.read(urlDevice);
                            if (mDataHolderDevice != null) {
                                VoDeviceList mVoDeviceList;
                                for (int j = 0; j < mDataHolderDevice.get_Listholder().size(); j++) {
                                    mVoDeviceList = new VoDeviceList();
                                    mVoDeviceList.setDevicLocalId(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceLocalId));
                                    mVoDeviceList.setDeviceServerid(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceServerId));
                                    mVoDeviceList.setUser_id(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceUserId));
                                    mVoDeviceList.setDevice_Comm_id(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceCommID));
                                    mVoDeviceList.setDevice_Comm_hexId(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceCommHexId));
                                    mVoDeviceList.setDevice_name(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceName));
                                    mVoDeviceList.setDevice_realName(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceRealName));
                                    mVoDeviceList.setDevice_BleAddress(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceBleAddress));
                                    mVoDeviceList.setDevice_Type(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceType));
                                    mVoDeviceList.setDevice_type_name(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceTypeName));
                                    mVoDeviceList.setDevice_ConnStatus(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldConnectStatus));
                                    mVoDeviceList.setDevice_SwitchStatus(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldSwitchStatus));
                                    mVoDeviceList.setDevice_is_favourite(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceIsFavourite));
                                    mVoDeviceList.setDevice_last_state_remember(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceLastState));
                                    mVoDeviceList.setDevice_timestamp(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceTimeStamp));
                                    mVoDeviceList.setDevice_is_active(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceIsActive));
                                    mVoDeviceList.setDevice_created_at(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceCreatedAt));
                                    mVoDeviceList.setDevice_updated_at(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceUpdatedAt));
                                    mVoDeviceList.setDevice_is_sync(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceIsSync));
                                    if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                                        mVoDeviceList.setIsChecked(true);
                                    } else {
                                        mVoDeviceList.setIsChecked(false);
                                    }
                                    mArrayListUnSyncGroupDeviceList.add(mVoDeviceList);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        mVoGroupList.setmVoDeviceLists(mArrayListUnSyncGroupDeviceList);
                        mArrayListUnSyncGroup.add(mVoGroupList);
                    }
                }

            } catch (Exception e) {
                result = "fail";
                e.printStackTrace();
            }
            if (mArrayListUnSyncGroup.size() > 0) {
                sendGroupDataToServer();
            } else {
                if (mUtility.haveInternet()) {
                    if (!mPreferenceHelper.getIsSkipUser()) {
                        if (isFromLogin) {
                            getGroupListAPI(true);
                        } else {
                            getGroupListAPI(false);
                        }
                    }
                }
            }
            System.out.println("result-" + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mArrayListUnSyncGroup.size() == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                    }
                });
            }
        }

        // Send data to server
        private String sendGroupDataToServer() {
            String loopResult = "success";
            if (currentLoopPosition >= mArrayListUnSyncGroup.size()) {
                loopResult = "finish";
                return loopResult;
            }
            addGroupAPI(mArrayListUnSyncGroup.get(currentLoopPosition));
            return loopResult;
        }

        // Add group data to server
        public void addGroupAPI(final VoLocalGroupData mVoLocalGroupData) {
            String mStringSelectedDeviceId = "";
            int j;
            if (mVoLocalGroupData.getmVoDeviceLists() != null && mVoLocalGroupData.getmVoDeviceLists().size() > 0) {
                for (j = 0; j < mVoLocalGroupData.getmVoDeviceLists().size(); j++) {
                    if (mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId() != null && !mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId().equalsIgnoreCase("") && !mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId().equalsIgnoreCase("null")) {
                        mStringSelectedDeviceId = mStringSelectedDeviceId + mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId() + ", ";
                    }
                }
            }
            if (mStringSelectedDeviceId != null && !mStringSelectedDeviceId.equalsIgnoreCase("") && !mStringSelectedDeviceId.equalsIgnoreCase(null)) {
                StringBuilder sb = new StringBuilder(mStringSelectedDeviceId);
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
                mStringSelectedDeviceId = sb.toString();
                Map<String, String> params = new HashMap<String, String>();
                params.put("device_token", mPreferenceHelper.getDeviceToken());
                params.put("user_id", mPreferenceHelper.getUserId());
                params.put("group_name", mVoLocalGroupData.getGroup_name());
                params.put("local_group_id", mVoLocalGroupData.getGroup_comm_id());
                params.put("local_group_hex_id", mVoLocalGroupData.getGroup_comm_hex_id().toLowerCase());
                params.put("devices", mStringSelectedDeviceId);

                // is_favourite 1-YES,2-NO
                // status 1-Active,2-deActive
                // is_update  0-Insert, 1-Update
                params.put("is_favourite", "2");
                params.put("status", "1");
                if (mVoLocalGroupData.getGroup_is_favourite() != null && !mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("") && !mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("null")) {
                    if (mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("1")) {
                        params.put("is_favourite", "1");
                    }
                }
                if (mVoLocalGroupData.getGroup_server_id() != null && !mVoLocalGroupData.getGroup_server_id().equalsIgnoreCase("") && !mVoLocalGroupData.getGroup_server_id().equalsIgnoreCase("null")) {
                    params.put("is_update", "1");// 1-Update
                    if (mVoLocalGroupData.getGroup_is_active() != null && !mVoLocalGroupData.getGroup_is_active().equalsIgnoreCase("") && !mVoLocalGroupData.getGroup_is_active().equalsIgnoreCase("null")) {
                        if (mVoLocalGroupData.getGroup_is_active().equalsIgnoreCase("0")) {
                            params.put("status", "2");
                        }
                    }
                } else {
                    params.put("is_update", "0");
                    params.put("status", "1");
                }
                Call<String> mLogin = mApiService.addGroupAPIWithStringResponse(params);
                if (BuildConfig.DEBUG) {
                    System.out.println("params-" + params.toString());
                    System.out.println("URL-" + mLogin.request().url().toString());
                }
                mLogin.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgress();
                            }
                        });
                        if (response.code() == 200 || response.isSuccessful()) {
                            Gson gson = new Gson();
                            try {
                                VoAddGroupData mVoAddGroupData = gson.fromJson(response.body(), VoAddGroupData.class);
                                if (mVoAddGroupData != null && mVoAddGroupData.getResponse().equalsIgnoreCase("true")) {
                                    if (mVoAddGroupData.getData() != null && mVoAddGroupData.getData().size() > 0) {
                                        ContentValues mContentValues;
                                        String[] mArray;
                                        for (int i = 0; i < mVoAddGroupData.getData().size(); i++) {
                                            mContentValues = new ContentValues();
                                            mContentValues.put(mDbHelper.mFieldGroupServerID, mVoAddGroupData.getData().get(i).getDevice_group_id());
                                            mContentValues.put(mDbHelper.mFieldGroupIsSync, "1");
                                            mArray = new String[]{mVoLocalGroupData.getGroup_local_id()};
                                            mDbHelper.updateRecord(mDbHelper.mTableGroup, mContentValues, mDbHelper.mFieldGroupLocalID + "=?", mArray);
                                            if (mVoAddGroupData.getData().get(i).getDevices() != null && mVoAddGroupData.getData().get(i).getDevices().size() > 0) {
                                                ContentValues mContentValuesGD = new ContentValues();
                                                mContentValuesGD.put(mDbHelper.mFieldGDListServerGroupID, mVoAddGroupData.getData().get(i).getDevice_group_id());
                                                String[] mArrayGroupLocalId = new String[]{mVoLocalGroupData.getGroup_local_id()};
                                                mDbHelper.updateRecord(mDbHelper.mTableGroupDeviceList, mContentValuesGD, mDbHelper.mFieldGDListLocalGroupID + "=?", mArrayGroupLocalId);
                                            }
                                        }
                                        if (currentLoopPosition == mArrayListUnSyncGroup.size() - 1) {
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    hideProgress();
                                                }
                                            });
                                            if (mUtility.haveInternet()) {
                                                if (mPreferenceHelper.getIsSkipUser()) {
                                                    mArrayListUnSyncGroup = new ArrayList<>();
                                                    if (isFromLogin) {
                                                        getGroupListAPI(true);
                                                    } else {
                                                        getGroupListAPI(false);
                                                    }
                                                }
                                            }
                                        } else {
                                            currentLoopPosition++;
                                            sendGroupDataToServer();
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                VoAddGroupDataDelete mVoAddGroupData = gson.fromJson(response.body(), VoAddGroupDataDelete.class);
                                if (mVoAddGroupData != null && mVoAddGroupData.getResponse().equalsIgnoreCase("true")) {
                                    if (mVoAddGroupData.getMessage().equalsIgnoreCase("Group already deleted")) {
                                        if (mVoAddGroupData.getData() != null) {
                                            ContentValues mContentValues = new ContentValues();
                                            mContentValues.put(mDbHelper.mFieldGroupServerID, mVoAddGroupData.getData().getDevice_group_id());
                                            mContentValues.put(mDbHelper.mFieldGroupIsSync, "1");
                                            String[] mArray = new String[]{mVoLocalGroupData.getGroup_local_id()};
                                            mDbHelper.updateRecord(mDbHelper.mTableGroup, mContentValues, mDbHelper.mFieldGroupLocalID + "=?", mArray);
                                            if (currentLoopPosition == mArrayListUnSyncGroup.size() - 1) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        hideProgress();
                                                    }
                                                });
                                                if (mUtility.haveInternet()) {
                                                    if (!mPreferenceHelper.getIsSkipUser()) {
                                                        mArrayListUnSyncGroup = new ArrayList<>();
                                                        if (isFromLogin) {
                                                            getGroupListAPI(true);
                                                        } else {
                                                            getGroupListAPI(false);
                                                        }
                                                    }
                                                }
                                            } else {
                                                currentLoopPosition++;
                                                sendGroupDataToServer();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                hideProgress();
                            }
                        });
                        if (isFromLogin) {
                            getGroupListAPI(true);
                        } else {
                            getGroupListAPI(false);
                        }
                    }
                });
            } else {
                if (currentLoopPosition == mArrayListUnSyncGroup.size() - 1) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });
                    if (mUtility.haveInternet()) {
                        if (!mPreferenceHelper.getIsSkipUser()) {
                            mArrayListUnSyncGroup = new ArrayList<>();
                            if (isFromLogin) {
                                getGroupListAPI(true);
                            } else {
                                getGroupListAPI(false);
                            }
                        }
                    }
                } else {
                    currentLoopPosition++;
                    sendGroupDataToServer();
                }
            }
        }


    }

    /* Fetch Group Data List from server*/
    public void getGroupListAPI(final boolean isShowProgress) {
        if (isShowProgress) {
            mUtility.hideKeyboard(MainActivity.this);
//            mUtility.ShowProgress("Please Wait..");
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mPreferenceHelper.getUserId());
        Call<VoServerGroupList> mVoServerGroupListListCall = mApiService.getAllGroupListAPI(params);
        if (BuildConfig.DEBUG) {
            System.out.println("params-" + params.toString());
            System.out.println("URL-" + mVoServerGroupListListCall.request().url().toString());
        }
        mVoServerGroupListListCall.enqueue(new Callback<VoServerGroupList>() {
            @Override
            public void onResponse(Call<VoServerGroupList> call, Response<VoServerGroupList> response) {
                if (!isFinishing()) {
                    hideProgress();
                    VoServerGroupList mVoServerGroupList = response.body();
//                    System.out.println("response mGroupListData...... " + new Gson().toJson(mVoServerGroupList));
                    mPreferenceHelper.setIsGroupSync(true);

                    String mStringQueryGroup = "delete from " + mDbHelper.mTableGroup + " where " + mDbHelper.mFieldGroupUserId + "= '" + mPreferenceHelper.getUserId() + "'" + " AND " + mDbHelper.mFieldGroupIsSync + "= '1'";
                    mDbHelper.exeQuery(mStringQueryGroup);
                    new InsertGroupDataInDbAsyncTask(mVoServerGroupList).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                }
            }

            @Override
            public void onFailure(Call<VoServerGroupList> call, Throwable t) {
                hideProgress();
            }
        });
    }

    /* Update device date to server*/
    public void updateDeviceAPI(final VoDeviceList mVoDeviceList) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("device_token", mPreferenceHelper.getDeviceToken());
        params.put("user_id", mPreferenceHelper.getUserId());
        params.put("device_id", mVoDeviceList.getDevice_Comm_id());
        params.put("hex_device_id", mVoDeviceList.getDevice_Comm_hexId().toLowerCase());
        params.put("device_name", mVoDeviceList.getDevice_name());
        String queryDeviceTypeId = "select " + mDbHelper.mFieldDeviceTypeServerID + " from " + mDbHelper.mTableDeviceType + " where " + mDbHelper.mFieldDeviceTypeType + "= '" + mVoDeviceList.getDevice_Type() + "'";
        String deviceTypeServerID = mDbHelper.getQueryResult(queryDeviceTypeId);
        System.out.println("deviceTypeServerID=" + deviceTypeServerID);
        params.put("device_type", deviceTypeServerID);
        params.put("ble_address", mVoDeviceList.getDevice_BleAddress().toUpperCase());
        // is_favourite 1-YES,2-NO
        // status 1-Active,2-deActive
        // is_update  0-Insert, 1-Update
        params.put("is_favourite", "2");
        params.put("status", "1");
        params.put("remember_last_color", "0");

        if (mVoDeviceList.getDevice_is_favourite() != null && !mVoDeviceList.getDevice_is_favourite().equalsIgnoreCase("") && !mVoDeviceList.getDevice_is_favourite().equalsIgnoreCase("null")) {
            if (mVoDeviceList.getDevice_is_favourite().equalsIgnoreCase("1")) {
                params.put("is_favourite", "1");
            }
        }
        if (mVoDeviceList.getDevice_last_state_remember() != null && !mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("") && !mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("null")) {
            if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("0")) {
                params.put("remember_last_color", "0");
            } else if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("1")) {
                params.put("remember_last_color", "1");
            } else if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("2")) {
                params.put("remember_last_color", "2");
            } else if (mVoDeviceList.getDevice_last_state_remember().equalsIgnoreCase("3")) {
                params.put("remember_last_color", "3");
            }
        }
        if (mVoDeviceList.getDeviceServerId() != null && !mVoDeviceList.getDeviceServerId().equalsIgnoreCase("") && !mVoDeviceList.getDeviceServerId().equalsIgnoreCase("null")) {
            params.put("is_update", "1");// 1-Update
            if (mVoDeviceList.getDevice_is_active() != null && !mVoDeviceList.getDevice_is_active().equalsIgnoreCase("") && !mVoDeviceList.getDevice_is_active().equalsIgnoreCase("null")) {
                if (mVoDeviceList.getDevice_is_active().equalsIgnoreCase("0")) {
                    params.put("status", "2");
                }
            }
        } else {
            params.put("is_update", "0");
            params.put("status", "1");
        }
        Call<VoAddDeviceData> mLogin = mApiService.addDeviceAPI(params);
        if (BuildConfig.DEBUG) {
            System.out.println("params-" + params.toString());
            System.out.println("URL-" + mLogin.request().url().toString());
        }
        mLogin.enqueue(new Callback<VoAddDeviceData>() {
            @Override
            public void onResponse(Call<VoAddDeviceData> call, Response<VoAddDeviceData> response) {
                VoAddDeviceData mAddDeviceAPI = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mAddDeviceAPI);
                if (mAddDeviceAPI != null && mAddDeviceAPI.getResponse().equalsIgnoreCase("true")) {
                    if (mAddDeviceAPI.getData() != null) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(mDbHelper.mFieldDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                        mContentValues.put(mDbHelper.mFieldDeviceIsSync, "1");
                        String[] mArray = new String[]{mVoDeviceList.getDevicLocalId()};
                        mDbHelper.updateRecord(mDbHelper.mTableDevice, mContentValues, mDbHelper.mFieldDeviceLocalId + "=?", mArray);
                    }
                }
            }

            @Override
            public void onFailure(Call<VoAddDeviceData> call, Throwable t) {

            }
        });
    }

    /* Update group data to server*/
    public void updateGroupAPI(final VoLocalGroupData mVoLocalGroupData) {
        String mStringSelectedDeviceId = "";
        if (mVoLocalGroupData.getmVoDeviceLists() != null && mVoLocalGroupData.getmVoDeviceLists().size() > 0) {
            for (int j = 0; j < mVoLocalGroupData.getmVoDeviceLists().size(); j++) {
                if (mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId() != null && !mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId().equalsIgnoreCase("") && !mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId().equalsIgnoreCase("null")) {
                    mStringSelectedDeviceId = mStringSelectedDeviceId + mVoLocalGroupData.getmVoDeviceLists().get(j).getDeviceServerId() + ", ";
                }
            }
        }
        if (mStringSelectedDeviceId != null && !mStringSelectedDeviceId.equalsIgnoreCase("") && !mStringSelectedDeviceId.equalsIgnoreCase(null)) {
            StringBuilder sb = new StringBuilder(mStringSelectedDeviceId);
            sb.deleteCharAt(sb.length() - 1);
            sb.deleteCharAt(sb.length() - 1);
            mStringSelectedDeviceId = sb.toString();
            Map<String, String> params = new HashMap<String, String>();
            params.put("device_token", mPreferenceHelper.getDeviceToken());
            params.put("user_id", mPreferenceHelper.getUserId());
            params.put("group_name", mVoLocalGroupData.getGroup_name());
            params.put("local_group_id", mVoLocalGroupData.getGroup_comm_id());
            params.put("local_group_hex_id", mVoLocalGroupData.getGroup_comm_hex_id().toLowerCase());
            params.put("devices", mStringSelectedDeviceId);
            // is_favourite 1-YES,2-NO
            // status 1-Active,2-deActive
            // is_update  0-Insert, 1-Update
            params.put("is_favourite", "2");
            params.put("status", "1");
            if (mVoLocalGroupData.getGroup_is_favourite() != null && !mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("") && !mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("null")) {
                if (mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("1")) {
                    params.put("is_favourite", "1");
                }
            }
            if (mVoLocalGroupData.getGroup_server_id() != null && !mVoLocalGroupData.getGroup_server_id().equalsIgnoreCase("") && !mVoLocalGroupData.getGroup_server_id().equalsIgnoreCase("null")) {
                params.put("is_update", "1");// 1-Update
                if (mVoLocalGroupData.getGroup_is_active() != null && !mVoLocalGroupData.getGroup_is_active().equalsIgnoreCase("") && !mVoLocalGroupData.getGroup_is_active().equalsIgnoreCase("null")) {
                    if (mVoLocalGroupData.getGroup_is_active().equalsIgnoreCase("0")) {
                        params.put("status", "2");
                    }
                }
            } else {
                params.put("is_update", "0");
                params.put("status", "1");
            }
            Call<VoAddGroupData> mLogin = mApiService.addGroupAPI(params);
            if (BuildConfig.DEBUG) {
                System.out.println("params-" + params.toString());
                System.out.println("URL-" + mLogin.request().url().toString());
            }
            mLogin.enqueue(new Callback<VoAddGroupData>() {
                @Override
                public void onResponse(Call<VoAddGroupData> call, Response<VoAddGroupData> response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });
                    VoAddGroupData mVoAddGroupData = response.body();
                    if (mVoAddGroupData != null && mVoAddGroupData.getResponse().equalsIgnoreCase("true")) {
                        if (mVoAddGroupData.getData() != null && mVoAddGroupData.getData().size() > 0) {
                            ContentValues mContentValues;
                            String[] mArray;
                            ContentValues mContentValuesGD;
                            String[] mArrayGroupLocalId;
                            for (int i = 0; i < mVoAddGroupData.getData().size(); i++) {
                                System.out.println();
                                mContentValues = new ContentValues();
                                mContentValues.put(mDbHelper.mFieldGroupServerID, mVoAddGroupData.getData().get(i).getDevice_group_id());
                                mContentValues.put(mDbHelper.mFieldGroupIsSync, "1");
                                mArray = new String[]{mVoLocalGroupData.getGroup_local_id()};
                                mDbHelper.updateRecord(mDbHelper.mTableGroup, mContentValues, mDbHelper.mFieldGroupLocalID + "=?", mArray);
                                if (mVoAddGroupData.getData().get(i).getDevices() != null && mVoAddGroupData.getData().get(i).getDevices().size() > 0) {
                                    mContentValuesGD = new ContentValues();
                                    mContentValuesGD.put(mDbHelper.mFieldGDListServerGroupID, mVoAddGroupData.getData().get(i).getDevice_group_id());
                                    mArrayGroupLocalId = new String[]{mVoLocalGroupData.getGroup_local_id()};
                                    mDbHelper.updateRecord(mDbHelper.mTableGroupDeviceList, mContentValuesGD, mDbHelper.mFieldGDListLocalGroupID + "=?", mArrayGroupLocalId);
                                }
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<VoAddGroupData> call, Throwable t) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });
                }
            });
        }
    }

    public class DeviceTypeListAsyncTask extends AsyncTask<String, Void, String> {
        boolean showProgress;

        public DeviceTypeListAsyncTask(boolean isShowProgress) {
            this.showProgress = isShowProgress;
        }


        @Override
        protected String doInBackground(String... urls) {
            getDeviceTypeListAPI(showProgress);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {

        }
    }

    /* Insert Group data in group table*/
    public class InsertGroupDataInDbAsyncTask extends AsyncTask<String, Void, String> {
        VoServerGroupList mVoServerGroupList;

        public InsertGroupDataInDbAsyncTask(VoServerGroupList mGroupList) {
            this.mVoServerGroupList = mGroupList;
        }


        @Override
        protected String doInBackground(String... urls) {
            if (mVoServerGroupList != null) {
                if (mVoServerGroupList.getResponse() != null && mVoServerGroupList.getResponse().equalsIgnoreCase("true")) {
                    if (mVoServerGroupList.getGroup() != null && mVoServerGroupList.getGroup().size() > 0) {
                        ContentValues mContentValues;
                        Calendar cal = Calendar.getInstance();
                        Date date;
                        String groupLocalEditId;
                        int groupLocalInsertId;
                        ContentValues mContentValuesGD;
                        for (int i = 0; i < mVoServerGroupList.getGroup().size(); i++) {
                            mContentValues = new ContentValues();
                            mContentValues.put(mDbHelper.mFieldGroupServerID, mVoServerGroupList.getGroup().get(i).getDevice_group_id());
                            mContentValues.put(mDbHelper.mFieldGroupUserId, mPreferenceHelper.getUserId());
                            mContentValues.put(mDbHelper.mFieldGroupCommId, mVoServerGroupList.getGroup().get(i).getLocal_group_id());
                            mContentValues.put(mDbHelper.mFieldGroupCommHexId, mVoServerGroupList.getGroup().get(i).getLocal_group_hex_id());
                            mContentValues.put(mDbHelper.mFieldGroupName, mVoServerGroupList.getGroup().get(i).getGroup_name());

                            mContentValues.put(mDbHelper.mFieldGroupIsFavourite, "0");
                            if (mVoServerGroupList.getGroup().get(i).getIs_favourite() != null && mVoServerGroupList.getGroup().get(i).getIs_favourite().toLowerCase().equalsIgnoreCase("1")) {
                                mContentValues.put(mDbHelper.mFieldGroupIsFavourite, "1");
                            }

                            mContentValues.put(mDbHelper.mFieldGroupTimeStamp, cal.getTimeInMillis());
                            if (mVoServerGroupList.getGroup().get(i).getCreated_date() != null && !mVoServerGroupList.getGroup().get(i).getCreated_date().equalsIgnoreCase("") && !mVoServerGroupList.getGroup().get(i).getCreated_date().equalsIgnoreCase("null")) {
                                try {
                                    date = mDateFormatDb.parse(mVoServerGroupList.getGroup().get(i).getCreated_date());
                                    mContentValues.put(mDbHelper.mFieldGroupTimeStamp, date.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            mContentValues.put(mDbHelper.mFieldGroupIsActive, "1");
                            if (mVoServerGroupList.getGroup().get(i).getStatus() != null && !mVoServerGroupList.getGroup().get(i).getStatus().equalsIgnoreCase("") && !mVoServerGroupList.getGroup().get(i).getStatus().equalsIgnoreCase("null")) {
                                if (mVoServerGroupList.getGroup().get(i).getStatus().equalsIgnoreCase("2")) {
                                    mContentValues.put(mDbHelper.mFieldGroupIsActive, "0");
                                }
                            }
                            mContentValues.put(mDbHelper.mFieldGroupCreatedAt, mVoServerGroupList.getGroup().get(i).getCreated_date());
                            mContentValues.put(mDbHelper.mFieldGroupUpdatedAt, mVoServerGroupList.getGroup().get(i).getCreated_date());
                            mContentValues.put(mDbHelper.mFieldGroupIsSync, "1");
                            groupLocalEditId = CheckRecordExistInGroupDB(mVoServerGroupList.getGroup().get(i).getDevice_group_id());
                            if (groupLocalEditId.equalsIgnoreCase("-1")) {
                                mContentValues.put(mDbHelper.mFieldGroupDeviceSwitchStatus, "OFF");
                                groupLocalInsertId = mDbHelper.insertRecord(mDbHelper.mTableGroup, mContentValues);
                                if (groupLocalInsertId != -1) {
                                    mDbHelper.exeQuery("delete from " + mDbHelper.mTableGroupDeviceList + " where " + mDbHelper.mFieldGDListServerGroupID + "= '" + mVoServerGroupList.getGroup().get(i).getDevice_group_id() + "'");
                                    if (mVoServerGroupList.getGroup().get(i).getDevice_details() != null && mVoServerGroupList.getGroup().get(i).getDevice_details().size() > 0) {
                                        String bleAddress;
                                        String deviceLocalId;
                                        String isExistInGD;
                                        String[] mArray;
                                        for (int j = 0; j < mVoServerGroupList.getGroup().get(i).getDevice_details().size(); j++) {
                                            mContentValuesGD = new ContentValues();
                                            bleAddress = mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getBle_address();
                                            if (bleAddress != null && !bleAddress.equalsIgnoreCase("") && !bleAddress.equalsIgnoreCase("null")) {
                                                bleAddress = bleAddress.toUpperCase();
                                            }
                                            deviceLocalId = mDbHelper.getQueryResult("select " + mDbHelper.mFieldDeviceLocalId + " from " + mDbHelper.mTableDevice + " where " + mDbHelper.mFieldDeviceBleAddress + "= '" + bleAddress + "'");
                                            mContentValuesGD.put(mDbHelper.mFieldGDListUserID, mPreferenceHelper.getUserId());
                                            mContentValuesGD.put(mDbHelper.mFieldGDListLocalDeviceID, deviceLocalId);
                                            mContentValuesGD.put(mDbHelper.mFieldGDListServerDeviceID, mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getServer_device_id());
                                            mContentValuesGD.put(mDbHelper.mFieldGDListLocalGroupID, groupLocalInsertId);
                                            mContentValuesGD.put(mDbHelper.mFieldGDListServerGroupID, mVoServerGroupList.getGroup().get(i).getDevice_group_id());
                                            mContentValuesGD.put(mDbHelper.mFieldGDListStatus, "1");
                                            if (mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus() != null && !mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus().equalsIgnoreCase("") && !mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus().equalsIgnoreCase("null")) {
                                                if (mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus().equalsIgnoreCase("2")) {
                                                    mContentValuesGD.put(mDbHelper.mFieldGDListStatus, "0");
                                                }
                                            }
                                            mContentValuesGD.put(mDbHelper.mFieldGDListCreatedDate, mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getCreated_date());
                                            isExistInGD = CheckRecordExistInGD(deviceLocalId, String.valueOf(groupLocalInsertId));
                                            if (isExistInGD.equalsIgnoreCase("-1")) {
                                                mDbHelper.insertRecord(mDbHelper.mTableGroupDeviceList, mContentValuesGD);
                                            } else {
                                                mArray = new String[]{isExistInGD};
                                                mDbHelper.updateRecord(mDbHelper.mTableGroupDeviceList, mContentValuesGD, mDbHelper.mFieldGDListLocalID + "=?", mArray);
//                                                        System.out.println("GD updated In Local Db");
                                            }
                                        }
                                    }
                                }
                            } else {
                                String[] mArray = new String[]{groupLocalEditId};
                                mDbHelper.updateRecord(mDbHelper.mTableGroup, mContentValues, mDbHelper.mFieldGroupLocalID + "=?", mArray);
                                String mStringQuery = "delete from " + mDbHelper.mTableGroupDeviceList + " where " + mDbHelper.mFieldGDListServerGroupID + "= '" + mVoServerGroupList.getGroup().get(i).getDevice_group_id() + "'";
                                mDbHelper.exeQuery(mStringQuery);
                                if (mVoServerGroupList.getGroup().get(i).getDevice_details() != null && mVoServerGroupList.getGroup().get(i).getDevice_details().size() > 0) {
                                    ContentValues mContentValuesGDU;
                                    String bleAddress;
                                    String deviceLocalId;
                                    String isExistInGD;
                                    String[] mArrayGD;
                                    for (int j = 0; j < mVoServerGroupList.getGroup().get(i).getDevice_details().size(); j++) {
                                        mContentValuesGDU = new ContentValues();
                                        bleAddress = mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getBle_address();
                                        if (bleAddress != null && !bleAddress.equalsIgnoreCase("") && !bleAddress.equalsIgnoreCase("null")) {
                                            bleAddress = bleAddress.toUpperCase();
                                        }
                                        deviceLocalId = mDbHelper.getQueryResult("select " + mDbHelper.mFieldDeviceLocalId + " from " + mDbHelper.mTableDevice + " where " + mDbHelper.mFieldDeviceBleAddress + "= '" + bleAddress + "'");
                                        mContentValuesGDU.put(mDbHelper.mFieldGDListUserID, mPreferenceHelper.getUserId());
                                        mContentValuesGDU.put(mDbHelper.mFieldGDListLocalDeviceID, deviceLocalId);
                                        mContentValuesGDU.put(mDbHelper.mFieldGDListServerDeviceID, mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getServer_device_id());
                                        mContentValuesGDU.put(mDbHelper.mFieldGDListLocalGroupID, groupLocalEditId);
                                        mContentValuesGDU.put(mDbHelper.mFieldGDListServerGroupID, mVoServerGroupList.getGroup().get(i).getDevice_group_id());
                                        mContentValuesGDU.put(mDbHelper.mFieldGDListStatus, "1");
                                        if (mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus() != null && !mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus().equalsIgnoreCase("") && !mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus().equalsIgnoreCase("null")) {
                                            if (mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getStatus().equalsIgnoreCase("2")) {
                                                mContentValuesGDU.put(mDbHelper.mFieldGDListStatus, "0");
                                            }
                                        }
                                        mContentValuesGDU.put(mDbHelper.mFieldGDListCreatedDate, mVoServerGroupList.getGroup().get(i).getDevice_details().get(j).getCreated_date());
                                        isExistInGD = CheckRecordExistInGD(deviceLocalId, groupLocalEditId);
                                        if (isExistInGD.equalsIgnoreCase("-1")) {
                                            mDbHelper.insertRecord(mDbHelper.mTableGroupDeviceList, mContentValuesGDU);
                                        } else {
                                            mArrayGD = new String[]{isExistInGD};
                                            mDbHelper.updateRecord(mDbHelper.mTableGroupDeviceList, mContentValuesGDU, mDbHelper.mFieldGDListLocalID + "=?", mArrayGD);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (mOnSyncComplete != null) {
                mOnSyncComplete.onGroupSyncComplete();
            }
        }
    }

    /* Insert Device data in device table*/
    public class InsertDeviceDataInDbAsyncTask extends AsyncTask<String, Void, String> {
        VoServerDeviceList mVoServerDeviceList;

        public InsertDeviceDataInDbAsyncTask(VoServerDeviceList deviceList) {
            mVoServerDeviceList = deviceList;
        }

        @Override
        protected String doInBackground(String... urls) {

            if (mVoServerDeviceList != null) {
                if (mVoServerDeviceList.getResponse() != null && mVoServerDeviceList.getResponse().equalsIgnoreCase("true")) {
                    if (mVoServerDeviceList.getData() != null && mVoServerDeviceList.getData().size() > 0) {

                        console.log("sdcklsdncklsndc", "Data = " + new Gson().toJson(mVoServerDeviceList.getData()));

                        ContentValues mContentValues;
                        String mStringbleAddress;
                        String isExistInDB;
                        for (int i = 0; i < mVoServerDeviceList.getData().size(); i++) {
                            mContentValues = new ContentValues();
                            mContentValues.put(DBHelper.mFieldDeviceServerId, mVoServerDeviceList.getData().get(i).getServer_device_id());
                            mContentValues.put(DBHelper.mFieldDeviceUserId, mPreferenceHelper.getUserId());
                            mContentValues.put(DBHelper.mFieldDeviceCommID, mVoServerDeviceList.getData().get(i).getDevice_id());
                            mContentValues.put(DBHelper.mFieldDeviceCommHexId, mVoServerDeviceList.getData().get(i).getHex_device_id());
                            mContentValues.put(DBHelper.mFieldDeviceRealName, "Vithamas Light");
                            mContentValues.put(DBHelper.mFieldDeviceName, mVoServerDeviceList.getData().get(i).getDevice_name());
                            mStringbleAddress = mVoServerDeviceList.getData().get(i).getBle_address();
                            if (mStringbleAddress != null && !mStringbleAddress.equalsIgnoreCase("") && !mStringbleAddress.equalsIgnoreCase("null")) {
                                mStringbleAddress = mStringbleAddress.toUpperCase();
                            }
                            mContentValues.put(DBHelper.mFieldDeviceBleAddress, mStringbleAddress);
                            mContentValues.put(DBHelper.mFieldDeviceType, mVoServerDeviceList.getData().get(i).getDevice_type_value());
                            mContentValues.put(DBHelper.mFieldDeviceTypeName, mVoServerDeviceList.getData().get(i).getDevice_type_name());
                            mContentValues.put(DBHelper.mFieldConnectStatus, "YES");

                            mContentValues.put(DBHelper.mFieldDeviceIsFavourite, "0");
                            if (mVoServerDeviceList.getData().get(i).getIs_favourite() != null && mVoServerDeviceList.getData().get(i).getIs_favourite().toLowerCase().equalsIgnoreCase("1")) {
                                mContentValues.put(DBHelper.mFieldDeviceIsFavourite, "1");
                            }
                            mContentValues.put(DBHelper.mFieldDeviceLastState, "0");
                            if (mVoServerDeviceList.getData().get(i).getRemember_last_color() != null && mVoServerDeviceList.getData().get(i).getRemember_last_color().toLowerCase().equalsIgnoreCase("0")) {
                                mContentValues.put(DBHelper.mFieldDeviceLastState, "0");
                            } else if (mVoServerDeviceList.getData().get(i).getRemember_last_color() != null && mVoServerDeviceList.getData().get(i).getRemember_last_color().toLowerCase().equalsIgnoreCase("1")) {
                                mContentValues.put(DBHelper.mFieldDeviceLastState, "1");
                            } else if (mVoServerDeviceList.getData().get(i).getRemember_last_color() != null && mVoServerDeviceList.getData().get(i).getRemember_last_color().toLowerCase().equalsIgnoreCase("2")) {
                                mContentValues.put(DBHelper.mFieldDeviceLastState, "2");
                            } else if (mVoServerDeviceList.getData().get(i).getRemember_last_color() != null && mVoServerDeviceList.getData().get(i).getRemember_last_color().toLowerCase().equalsIgnoreCase("3")) {
                                mContentValues.put(DBHelper.mFieldDeviceLastState, "3");
                            } else {
                                mContentValues.put(DBHelper.mFieldDeviceLastState, "0");
                            }

                            mContentValues.put(DBHelper.mFieldDeviceIsActive, "1");
                            if (mVoServerDeviceList.getData().get(i).getStatus() != null && !mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("") && !mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("null")) {
                                if (mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("2") ||
                                        mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("0")) {
                                    mContentValues.put(DBHelper.mFieldDeviceIsActive, "0");
                                }
                            }
                            Calendar cal = Calendar.getInstance();
                            mContentValues.put(DBHelper.mFieldDeviceTimeStamp, cal.getTimeInMillis());
                            if (mVoServerDeviceList.getData().get(i).getCreated_date() != null && !mVoServerDeviceList.getData().get(i).getCreated_date().equalsIgnoreCase("") && !mVoServerDeviceList.getData().get(i).getCreated_date().equalsIgnoreCase("null")) {
                                try {
                                    Date date = (Date) mDateFormatDb.parse(mVoServerDeviceList.getData().get(i).getCreated_date());
                                    mContentValues.put(DBHelper.mFieldDeviceTimeStamp, date.getTime());
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            mContentValues.put(DBHelper.mFieldDeviceCreatedAt, mVoServerDeviceList.getData().get(i).getCreated_date());
                            mContentValues.put(DBHelper.mFieldDeviceUpdatedAt, mVoServerDeviceList.getData().get(i).getUpdated_date());
                            mContentValues.put(DBHelper.mFieldDeviceIsSync, "1");
                            mContentValues.put(DBHelper.mFieldDeviceIsWifiConfigured, mVoServerDeviceList.getData().get(i).getWifi_configured());

                            console.log("dckjbdkjcbskjdbck", mVoServerDeviceList.getData().get(i).getWifi_configured());

                            isExistInDB = CheckRecordExistInDeviceDB(mVoServerDeviceList.getData().get(i).getBle_address());

                            if (isExistInDB.equalsIgnoreCase("-1")) {
                                mContentValues.put(DBHelper.mFieldSwitchStatus, "OFF");
                                mDbHelper.insertRecord(DBHelper.mTableDevice, mContentValues);
                            } else {
                                mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                                        DBHelper.mFieldDeviceLocalId + "=?", new String[]{isExistInDB});
//                                        System.out.println("Device updated In Local Db");
                            }
                            if (mVoServerDeviceList.getData().get(i).getStatus() != null && !mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("") && !mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("null")) {
                                if (mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("2") || mVoServerDeviceList.getData().get(i).getStatus().equalsIgnoreCase("0")) {
                                    ContentValues mContentValuesGD = new ContentValues();
                                    mContentValuesGD.put(DBHelper.mFieldGDListStatus, "0");
                                    mDbHelper.updateRecord(DBHelper.mTableGroupDeviceList, mContentValuesGD,
                                            DBHelper.mFieldGDListLocalDeviceID + "=?", new String[]{isExistInDB});
                                    DataHolder mDataHolderDltGroup;
                                    try {
                                        String deleteGroupDevice = "select * from " + DBHelper.mTableGroup + " INNER JOIN " + DBHelper.mTableGroupDeviceList +
                                                " on " + DBHelper.mFieldGDListLocalGroupID + " =" + DBHelper.mFieldGroupLocalID + " AND " + DBHelper.mFieldGDListUserID +
                                                "= " + DBHelper.mTableGroup + "." + mDbHelper.mFieldGroupUserId + " AND " + mDbHelper.mFieldGroupIsActive + "= 1" +
                                                " INNER JOIN " + mDbHelper.mTableDevice + " on " + mDbHelper.mFieldDeviceLocalId + " =" + mDbHelper.mFieldGDListLocalDeviceID +
                                                " AND " + mDbHelper.mTableDevice + "." + mDbHelper.mFieldDeviceUserId + "= " + mDbHelper.mFieldGDListUserID +
                                                " where " + mDbHelper.mFieldDeviceLocalId + "= '" + isExistInDB + "'" + " AND " + mDbHelper.mTableDevice + "." +
                                                mDbHelper.mFieldDeviceUserId + "= '" + mPreferenceHelper.getUserId() + "'";
//                                                System.out.println("DeviceList-deleteGroupDevice url " + deleteGroupDevice);
                                        mDataHolderDltGroup = mDbHelper.read(deleteGroupDevice);
                                        if (mDataHolderDltGroup != null) {
                                            String mStringGroupServerId;
                                            int intGroupDeviceCount;
                                            int intGroupInactiveDeviceCount;
                                            for (int j = 0; j < mDataHolderDltGroup.get_Listholder().size(); j++) {
                                                mStringGroupServerId = mDataHolderDltGroup.get_Listholder().get(j).get(mDbHelper.mFieldGroupServerID);
//                                                        System.out.println("GroupLocalID-"+mDataHolderDltGroup.get_Listholder().get(j).get(mDbHelper.mFieldGroupLocalID));
                                                ContentValues mContentValuesGroup = new ContentValues();
                                                intGroupDeviceCount = mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mDbHelper.mTableGroupDeviceList + " where " + mDbHelper.mFieldGDListUserID + "= '" + mPreferenceHelper.getUserId() + "'" + " AND " + mDbHelper.mFieldGDListLocalGroupID + "= '" + mDataHolderDltGroup.get_Listholder().get(j).get(mDbHelper.mFieldGroupLocalID) + "'" + ") as count");
                                                intGroupInactiveDeviceCount = mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mDbHelper.mTableGroupDeviceList + " where " + mDbHelper.mFieldGDListStatus + "= 0" + " AND " + mDbHelper.mFieldGDListUserID + "= '" + mPreferenceHelper.getUserId() + "'" + " AND " + mDbHelper.mFieldGDListLocalGroupID + "= '" + mDataHolderDltGroup.get_Listholder().get(j).get(mDbHelper.mFieldGroupLocalID) + "'" + ") as count");
                                                if (intGroupDeviceCount == intGroupInactiveDeviceCount) {
                                                    if (mStringGroupServerId != null && !mStringGroupServerId.equalsIgnoreCase("") && !mStringGroupServerId.equalsIgnoreCase("null")) {
                                                        mContentValuesGroup.put(mDbHelper.mFieldGroupIsActive, "0");
                                                        mContentValuesGroup.put(mDbHelper.mFieldGroupIsSync, "0");
                                                        mDbHelper.updateRecord(mDbHelper.mTableGroup, mContentValuesGroup, mDbHelper.mFieldGroupLocalID + "=?", new String[]{mDataHolderDltGroup.get_Listholder().get(j).get(mDbHelper.mFieldGroupLocalID)});
                                                    } else {
                                                        mDbHelper.exeQuery("delete from " + mDbHelper.mTableGroup + " where " + mDbHelper.mFieldGroupLocalID + "= '" + mDataHolderDltGroup.get_Listholder().get(j).get(mDbHelper.mFieldGroupLocalID) + "'");
                                                    }
                                                } else {
                                                    mContentValuesGroup.put(mDbHelper.mFieldGroupIsSync, "0");
                                                    mDbHelper.updateRecord(mDbHelper.mTableGroup, mContentValuesGroup, mDbHelper.mFieldGroupLocalID + "=?", new String[]{mDataHolderDltGroup.get_Listholder().get(j).get(mDbHelper.mFieldGroupLocalID)});
                                                }
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                    DataHolder mDataHolderAlarm;
                                    try {
//                                                String deleteAlarmDevice = "select * from " + mDbHelper.mTableAlarm + " INNER JOIN " + mDbHelper.mTableAlarmDeviceList + " on " + mDbHelper.mFieldADAlarmLocalID + " =" + mDbHelper.mFieldAlarmLocalID + " AND " + mDbHelper.mFieldADUserId + "= " + mDbHelper.mTableAlarm + "." + mDbHelper.mFieldAlarmUserId + " AND " + mDbHelper.mFieldAlarmIsActive + "= 1" + " INNER JOIN " + mDbHelper.mTableDevice + " on " + mDbHelper.mFieldDeviceLocalId + " =" + mDbHelper.mFieldADDeviceLocalID + " AND " + mDbHelper.mTableAlarm + "." + mDbHelper.mFieldAlarmUserId + "= " + mDbHelper.mFieldADUserId + " where " + mDbHelper.mFieldDeviceLocalId + "= '" + isExistInDB + "'" + " AND " + mDbHelper.mTableAlarm + "." + mDbHelper.mFieldAlarmUserId + "= '" + mPreferenceHelper.getUserId() + "'";
                                        String deleteAlarmDevice = "select * from " + mDbHelper.mTableAlarm + " INNER JOIN " + mDbHelper.mTableAlarmDeviceList + " on " + mDbHelper.mFieldADAlarmLocalID + " =" + mDbHelper.mFieldAlarmLocalID + " AND " + mDbHelper.mFieldADUserId + "= " + mDbHelper.mTableAlarm + "." + mDbHelper.mFieldAlarmUserId + " AND " + mDbHelper.mFieldAlarmIsActive + "= 1" + " INNER JOIN " + mDbHelper.mTableDevice + " on " + mDbHelper.mFieldDeviceLocalId + " =" + mDbHelper.mFieldADDeviceLocalID + " AND " + mDbHelper.mTableDevice + "." + mDbHelper.mFieldDeviceUserId + "= " + mDbHelper.mFieldADUserId + " where " + mDbHelper.mFieldDeviceLocalId + "= '" + isExistInDB + "'" + " AND " + mDbHelper.mTableDevice + "." + mDbHelper.mFieldDeviceUserId + "= '" + mPreferenceHelper.getUserId() + "'";

                                        mDataHolderAlarm = mDbHelper.read(deleteAlarmDevice);
                                        if (mDataHolderAlarm != null) {
                                            int intAlarmDeviceCount;
                                            int intAlarmInactiveDeviceCount;
                                            for (int j = 0; j < mDataHolderAlarm.get_Listholder().size(); j++) {
                                                ContentValues mContentValuesAlarm = new ContentValues();
                                                intAlarmDeviceCount = mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mDbHelper.mTableAlarmDeviceList + " where " + mDbHelper.mFieldADUserId + "= '" + mPreferenceHelper.getUserId() + "'" + " AND " + mDbHelper.mFieldADAlarmLocalID + "= '" + mDataHolderAlarm.get_Listholder().get(j).get(mDbHelper.mFieldAlarmLocalID) + "'" + ") as count");
                                                intAlarmInactiveDeviceCount = mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mDbHelper.mTableAlarmDeviceList + " where " + mDbHelper.mFieldADDeviceStatus + "= 1" + " AND " + mDbHelper.mFieldADDeviceLocalID + "= '" + isExistInDB + "'" + " AND " + mDbHelper.mFieldADUserId + "= '" + mPreferenceHelper.getUserId() + "'" + " AND " + mDbHelper.mFieldADAlarmLocalID + "= '" + mDataHolderAlarm.get_Listholder().get(j).get(mDbHelper.mFieldAlarmLocalID) + "'" + ") as count");
                                                if (intAlarmDeviceCount == intAlarmInactiveDeviceCount) {
                                                    mContentValuesAlarm.put(mDbHelper.mFieldAlarmIsActive, "0");
                                                }
                                                mContentValuesAlarm.put(mDbHelper.mFieldAlarmIsSync, "0");
                                                mDbHelper.updateRecord(mDbHelper.mTableAlarm, mContentValuesAlarm, mDbHelper.mFieldAlarmLocalID + "=?", new String[]{mDataHolderAlarm.get_Listholder().get(j).get(mDbHelper.mFieldAlarmLocalID)});
                                            }
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    mDbHelper.exeQuery("delete from " + mDbHelper.mTableAlarmDeviceList + " where " + mDbHelper.mFieldADDeviceLocalID + "= '" + isExistInDB + "'");
//                                            mDbHelper.exeQuery("delete from " + mDbHelper.mTableGroupDeviceList + " where " + mDbHelper.mFieldGDListLocalDeviceID + "= '" + isExistInDB + "'");
                                }
                            }
                            try {
                                Thread.sleep(50);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (mOnSyncComplete != null) {
                mOnSyncComplete.onDeviceSyncComplete();
            }
            if (mUtility.haveInternet()) {
                if (!isFromLogin) {
                    new DeviceTypeListAsyncTask(false).execute("");
                }
            }
            isFromLogin = false;
        }
    }

    /* Insert Device type in device type table*/
    public void getDeviceTypeListAPI(final boolean isShowProgress) {
        if (isShowProgress) {
            mUtility.hideKeyboard(MainActivity.this);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUtility.ShowProgress("Please Wait..");
                }
            });
        }
        final Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mPreferenceHelper.getUserId());
        Call<VoDeviceTypeList> mVoServerDeviceListCall = mApiService.getDeviceTypeListAPI(params);
        if (BuildConfig.DEBUG) {
            System.out.println("params-" + params.toString());
            System.out.println("URL-" + mVoServerDeviceListCall.request().url().toString());
        }
        mVoServerDeviceListCall.enqueue(new Callback<VoDeviceTypeList>() {
            @Override
            public void onResponse(Call<VoDeviceTypeList> call, Response<VoDeviceTypeList> response) {
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });
                    VoDeviceTypeList mVoDeviceTypeList = response.body();
                    Gson mGson = new Gson();
                    System.out.println("onResponse" + mGson.toJson(mVoDeviceTypeList));
                    if (mVoDeviceTypeList != null) {
                        if (mVoDeviceTypeList.getResponse() != null && mVoDeviceTypeList.getResponse().equalsIgnoreCase("true")) {
                            if (mVoDeviceTypeList.getData() != null && mVoDeviceTypeList.getData().size() > 0) {
                                ContentValues mContentValues;
                                String isExistInDB;
                                String[] mArray;
                                for (int i = 0; i < mVoDeviceTypeList.getData().size(); i++) {
                                    mContentValues = new ContentValues();
                                    mContentValues.put(mDbHelper.mFieldDeviceTypeServerID, mVoDeviceTypeList.getData().get(i).getId());
                                    mContentValues.put(mDbHelper.mFieldDeviceTypeTypeName, mVoDeviceTypeList.getData().get(i).getDevice_type_name());
                                    mContentValues.put(mDbHelper.mFieldDeviceTypeType, mVoDeviceTypeList.getData().get(i).getDevice_type_value());
                                    mContentValues.put(mDbHelper.mFieldDeviceTypeIsActive, mVoDeviceTypeList.getData().get(i).getStatus());
                                    mContentValues.put(mDbHelper.mFieldDeviceTypeCreatedAt, mVoDeviceTypeList.getData().get(i).getCreated_date());
                                    mContentValues.put(mDbHelper.mFieldDeviceTypeUpdatedAt, mVoDeviceTypeList.getData().get(i).getCreated_date());
                                    isExistInDB = CheckRecordExistInDeviceTypeDB(mVoDeviceTypeList.getData().get(i).getId());
                                    if (isExistInDB.equalsIgnoreCase("-1")) {
                                        mDbHelper.insertRecord(mDbHelper.mTableDeviceType, mContentValues);
                                    } else {
                                        mArray = new String[]{isExistInDB};
                                        mDbHelper.updateRecord(mDbHelper.mTableDeviceType, mContentValues, mDbHelper.mFieldDeviceTypeLocalID + "=?", mArray);
                                    }
                                }
                            }
                        }
                    }
                    if (isFromLogin && !mPreferenceHelper.getIsSkipUser()) {
                        System.out.println("FirstTimeSync");
                        new syncDeviceDataAsyncTask(true).execute("");
                    }
                }
            }

            @Override
            public void onFailure(Call<VoDeviceTypeList> call, Throwable t) {
                hideProgress();
                if (isFromLogin && !mPreferenceHelper.getIsSkipUser()) {
                    System.out.println("FirstTimeSync");
                    new syncDeviceDataAsyncTask(true).execute("");
                }
            }
        });
    }

    /* Check authentication api*/
    private void checkAuthenticationAPI(final boolean isShowProgress) {
        mUtility.hideKeyboard(this);
        if (isShowProgress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mUtility.ShowProgress("Please Wait..");
                }
            });
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mPreferenceHelper.getUserId());
        params.put("password", mPreferenceHelper.getUserPassword());
        Call<VoLogout> mLogin = mApiService.authenticateUserCheck(params);

        mLogin.enqueue(new Callback<VoLogout>() {
            @Override
            public void onResponse(Call<VoLogout> call, Response<VoLogout> response) {
                if (isShowProgress) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            hideProgress();
                        }
                    });
                }
                VoLogout mLoginData = response.body();
                Gson gson = new Gson();
                String json = gson.toJson(mLoginData);
//                System.out.println("response Authentication---------" + json);
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {

                } else {
                    mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_session_expired), 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            mPreferenceHelper.ResetPrefData();
                            Intent mIntent = new Intent(MainActivity.this, LoginActivity.class);
                            mIntent.putExtra("is_from_add_account", false);
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(mIntent);
                            finish();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<VoLogout> call, Throwable t) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                    }
                });
//                mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again), 1);
            }
        });
    }

    /* Check record exist in Group device table*/
    private String CheckRecordExistInGD(String localDeviceId, String groupLocalId) {
        String url = "select * from " + mDbHelper.mTableGroupDeviceList + " where " + mDbHelper.mFieldGDListLocalDeviceID + "= '" + localDeviceId + "'" + " and " + mDbHelper.mFieldGDListLocalGroupID + "= '" + groupLocalId + "'";
        DataHolder mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldGDListLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /* Check record exist in Group device table*/
    private String CheckRecordExistInGroupDB(String serverGroupeId) {
        String url = "select * from " + mDbHelper.mTableGroup + " where " + mDbHelper.mFieldGroupServerID + "= '" + serverGroupeId + "'" + " and " + mDbHelper.mFieldGroupUserId + "= '" + mPreferenceHelper.getUserId() + "'";
        DataHolder mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldGroupLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /* Check record exist in device table*/
    private String CheckRecordExistInDeviceDB(String bleAddress) {
        if (bleAddress != null && !bleAddress.equalsIgnoreCase("") && !bleAddress.equalsIgnoreCase("null")) {
            bleAddress = bleAddress.toUpperCase();
        }
        String url = "select * from " + mDbHelper.mTableDevice + " where " + mDbHelper.mFieldDeviceBleAddress + "= '" + bleAddress + "'" + " and " + mDbHelper.mFieldDeviceUserId + "= '" + mPreferenceHelper.getUserId() + "'";
//        System.out.println(" URL : " + url);
        DataHolder mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldDeviceLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /* Check record exist in device type*/
    public String CheckRecordExistInDeviceTypeDB(String deviceTypeServerId) {
        String url = "select * from " + mDbHelper.mTableDeviceType + " where " + mDbHelper.mFieldDeviceTypeServerID + "= '" + deviceTypeServerId + "'";
        DataHolder mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldDeviceTypeLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    public String getUserActiveCount() {
        String urlString = "SELECT (SELECT count(*) from " + mDbHelper.mTableDevice + " where " + mDbHelper.mTableDevice + "." + mDbHelper.mFieldDeviceIsActive + "= 1" + " AND " + mDbHelper.mTableDevice + "." + mDbHelper.mFieldDeviceUserId + "= '" + mPreferenceHelper.getUserId() + "'" + ")+(SELECT count(*) from " + mDbHelper.mTableAlarmDeviceList + " where " + mDbHelper.mTableAlarmDeviceList + "." + mDbHelper.mFieldADDeviceStatus + "= 1" + " AND " + mDbHelper.mTableAlarmDeviceList + "." + mDbHelper.mFieldADUserId + "= '" + mPreferenceHelper.getUserId() + "'" + ")+(SELECT count(*) from " + mDbHelper.mTableGroupDeviceList + " where " + mDbHelper.mTableGroupDeviceList + "." + mDbHelper.mFieldGDListStatus + "= 1" + " AND " + mDbHelper.mTableGroupDeviceList + "." + mDbHelper.mFieldGDListUserID + "= '" + mPreferenceHelper.getUserId() + "'" + ") as count";
        return mDbHelper.getCountRecordByQuery(urlString) + "";
    }

    public void setOnDevicesStatusChange(onDeviceConnectionStatusChange stateChange) {
        this.mOnDeviceConnectionStatusChange = stateChange;
    }

    public void setOnBluetoothStateChangeListener(OnBluetoothStateChangeListener bluetoothStateChangeListener) {
        this.onBluetoothStateChangeListener = bluetoothStateChangeListener;
    }

    public void setOnSyncCompleteListner(onSyncComplete syncComplete) {
        this.mOnSyncComplete = syncComplete;
    }

    public void setOnHardReset(OnHardResetListener resetListener) {
        this.mOnHardResetListener = resetListener;
    }

    /* Generate random no*/
    public int generateRandomNo() {
        Random random = new Random();
        String generatedPassword = String.format("%04d", random.nextInt(9999));
        int generatedNo = Integer.parseInt(generatedPassword.toString());
        if (generatedNo < 999) {
            return generateRandomNo();
        }
        return generatedNo;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mActionBarDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        mActionBarDrawerToggle.syncState();
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mActionBarDrawerToggle.onConfigurationChanged(newConfig);
    }

    public void setScrollingBehavior(boolean isMapIndex) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();

        if (isMapIndex) {
            params.setScrollFlags(0);
            appBarLayout.setLayoutParams(appBarLayoutParams);
        } else {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
            appBarLayout.setLayoutParams(appBarLayoutParams);
        }
    }

    /* Drawer menu item click handle*/
    private void selectDrawerItem(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.menu_dashboard:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentHome(), false, null, 1);
                break;
            case R.id.menu_alarm:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentAlarmList(), false, null, 1);
                break;
//            case R.id.menu_device_setting:
//                removeAllFragmentFromBack();
//                replacesFragment(new FragmentSettings(), false, null, 1);
//                break;
            case R.id.menu_factory_reset:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentReset(), false, null, 1);
                break;
            case R.id.menu_manage_account:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentAccount(), false, null, 1);
                break;
            case R.id.menu_help:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentHelp(), false, null, 1);
                break;
            case R.id.menu_buy:
                Intent intent = new Intent(ACTION_VIEW);
                intent.setData(Uri.parse("https://www.amazon.in/dp/B07QGVQ1HH"));
                startActivity(intent);
                break;
            case R.id.menu_demo_videos:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentYouTube(), false, null, 1);
                break;
            case R.id.menu_about_us:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentAboutUs(), false, null, 1);
                break;
            case R.id.menu_contact_us:
                removeAllFragmentFromBack();
                replacesFragment(new FragmentContactUs(), false, null, 1);
                break;
            default:
                break;
        }
        menuItem.setChecked(true);
        setTitle(menuItem.getTitle());
        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    /*Display hader back button or not*/
    public void showBackButton(boolean enable) {

        // To keep states of ActionBar and ActionBarDrawerToggle synchronized,
        // when you enable on one, you disable on the other.
        // And as you may notice, the order for this operation is disable first, then enable - VERY VERY IMPORTANT.
        if (enable) {
            // Remove hamburger
            mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);
            // Show back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // when DrawerToggle is disabled i.e. setDrawerIndicatorEnabled(false), navigation icon
            // clicks are disabled i.e. the UP button will not work.
            // We need to add a listener, as in below, so DrawerToggle will forward
            // click events to this listener.
            if (!mToolBarNavigationListenerIsRegistered) {
                mActionBarDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Doesn't have to be onBackPressed
                        onBackPressed();
                    }
                });

                mToolBarNavigationListenerIsRegistered = true;
            }

        } else {
            // Remove back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            // Show hamburger
            mActionBarDrawerToggle.setDrawerIndicatorEnabled(true);
            // Remove the/any drawer toggle listener
            mActionBarDrawerToggle.setToolbarNavigationClickListener(null);
            mToolBarNavigationListenerIsRegistered = false;
        }
    }

    /*Check required permission*/
    @TargetApi(Build.VERSION_CODES.M)
    private void callMarshmallowPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();
        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsNeeded.add("Show Location");
        }
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
            ActivityCompat.requestPermissions(this, permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    /*On Permission result*/
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                // EasyPermissions handles the request result.
                Map<String, Integer> perms = new HashMap<String, Integer>();
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }

                boolean allPermissionsGranted = true;
                if (grantResults.length > 0) {
                    for (int grantResult : grantResults) {
                        if (grantResult != PackageManager.PERMISSION_GRANTED) {
                            allPermissionsGranted = false;
                            break;
                        }
                    }
                }

                if (!allPermissionsGranted) {
                    boolean somePermissionsForeverDenied = false;
                    for (String permission : permissions) {
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            //denied
//                            Log.e("denied", permission);
                        } else {
                            if (ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
                                //allowed
//                                Log.e("allowed", permission);
                            } else {
                                //set to never ask again
//                                Log.e("set to never ask again", permission);
                                somePermissionsForeverDenied = true;
                            }
                        }
                    }

                    if (somePermissionsForeverDenied) {
                        if (!isNeverAskPermissionCheck) {
                            isNeverAskPermissionCheck = true;
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
                            alertDialogBuilder.setTitle("Permissions Required")
                                    .setMessage("You have forcefully denied some of the required permissions. Please open settings, go to permissions and allow them.")
                                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                    Uri.fromParts("package", getPackageName(), null));
//                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                            startActivityForResult(intent, 103);
                                            isNeverAskPermissionCheck = false;
                                        }
                                    })
                                    .setNegativeButton("", (dialog, which) -> isNeverAskPermissionCheck = false)
                                    .setCancelable(true)
                                    .create()
                                    .show();
                        }
                    }
                } else {
                    console.log("obuidbuidbduibd", "Permission Granted");
                    bleScanner.setOnBLEScanListener(bleScannerCallback);
                    startDeviceScan();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
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


    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
    }

    /*Remove all back state fragment*/
    public void removeAllFragmentFromBack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }


    public void removeNumberOfFragmnet(int num) {
        for (int i = 0; i < num; ++i) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /*Replace fragment*/
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

    /*Add fragment*/
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

    /*Handle on back press*/
    @Override
    public void onBackPressed() {
        mUtility.hideKeyboard(MainActivity.this);
        int count = getSupportFragmentManager().getBackStackEntryCount();
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        }
        if (count > 0) {
            if (mFragment instanceof FragmentHome) {
                if (mFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                    mFragment.getChildFragmentManager().popBackStack();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            if (mFragment instanceof FragmentHome) {
                if (exit) {
                    CustomDialog.ExitDialog(MainActivity.this, getString(R.string.alert_exit));

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
                mNavigationView.getMenu().getItem(0).setChecked(true);
                replacesFragment(new FragmentHome(), false, null, 0);
            }
        }
    }

    /**
     * Starts BLE Advertising by starting {@code AdvertiserService}.
     */
    private void startAdvertising() {
        Context c = this;
        c.startService(getServiceIntent(c));
    }

    /**
     * Stops BLE Advertising by stopping {@code AdvertiserService}.
     */
    public void stopAdvertising() {
        Context c = this;
        c.stopService(getServiceIntent(c));
    }

    public boolean isAdvertisingSupported() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.getBluetoothLeAdvertiser() != null;
    }

    public interface ReleasePowerSocketBLEServiceCallback {
        public void onRelease();
    }

    @Override
    protected void onPause() {
        super.onPause();
        console.log("asxjbakjsx_111", "onPause");
    }
}