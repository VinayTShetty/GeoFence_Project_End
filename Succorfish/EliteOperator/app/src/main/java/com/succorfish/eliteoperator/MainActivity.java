package com.succorfish.eliteoperator;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
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
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.SpinKitView;
import com.github.ybq.android.spinkit.SpriteFactory;
import com.github.ybq.android.spinkit.Style;
import com.github.ybq.android.spinkit.sprite.Sprite;
import com.libRG.CustomTextView;
import com.succorfish.eliteoperator.Vo.VoAddressBook;
import com.succorfish.eliteoperator.Vo.VoBluetoothDevices;
import com.succorfish.eliteoperator.Vo.VoDrawerListItem;
import com.succorfish.eliteoperator.db.DBHelper;
import com.succorfish.eliteoperator.db.DataHolder;
import com.succorfish.eliteoperator.fragments.FragmentDashboard;
import com.succorfish.eliteoperator.fragments.FragmentDeviceConnect;
import com.succorfish.eliteoperator.fragments.FragmentDriverIoc;
import com.succorfish.eliteoperator.fragments.FragmentMessage;
import com.succorfish.eliteoperator.fragments.FragmentSetting;
import com.succorfish.eliteoperator.helper.AESCrypt;
import com.succorfish.eliteoperator.helper.BLEUtility;
import com.succorfish.eliteoperator.helper.CustomDialog;
import com.succorfish.eliteoperator.helper.PreferenceHelper;
import com.succorfish.eliteoperator.helper.SampleGattAttributes;
import com.succorfish.eliteoperator.helper.Utility;
import com.succorfish.eliteoperator.interfaces.onAlertDialogCallBack;
import com.succorfish.eliteoperator.interfaces.onDeviceConnectionStatusChange;
import com.succorfish.eliteoperator.interfaces.onDeviceMessegeSent;
import com.succorfish.eliteoperator.interfaces.onResponseGetRefresh;
import com.succorfish.eliteoperator.services.BluetoothLeService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    String TAG = MainActivity.class.getSimpleName();

    public TextView mTextViewTitle;
    public CustomTextView mTextViewActiveCount;

    @BindView(R.id.nav_header_main_imageView_logo)
    public ImageView mImageViewLogo;
    @BindView(R.id.activity_main_imageview_heat_one)
    public ImageView mImageViewHeatOne;
    @BindView(R.id.activity_main_imageview_heat_two)
    public ImageView mImageViewHeatTwo;
    @BindView(R.id.activity_main_imageview_heat_three)
    public ImageView mImageViewHeatThree;
    @BindView(R.id.activity_main_imageview_heat_four)
    public ImageView mImageViewHeatFour;
    @BindView(R.id.activity_main_imageview_delete)
    public ImageView mImageViewHeatDelete;

    public ImageView mImageViewBack;
    public ImageView mImageViewPerson;
    public ImageView mImageViewAdd;
    public ImageView mImageViewDrawer;

    @BindView(R.id.activity_main_listview_menu)
    RecyclerView mRecyclerViewDrawerList;

    @BindView(R.id.activity_main_appBarLayout)
    public AppBarLayout appBarLayout;
    @BindView(R.id.activity_main_drawer_layout)
    public DrawerLayout mDrawerLayout;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.activity_main_toolbars)
    Toolbar mToolbar;
    private ActionBarDrawerToggle mActionBarDrawerToggle;

    FragmentTransaction fragmentTransaction;
    ArrayList<VoDrawerListItem> mDrawerItemArrayList = new ArrayList<>();
    DrawerItemCustomAdapter mDrawerItemCustomAdapter;
    boolean exit = false;
    public DBHelper mDbHelper;
    public Utility mUtility;
    public PreferenceHelper mPreferenceHelper;
    public static final int BLUETOOTH_ENABLE_REQT = 11;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    //    BLE
    public static BluetoothManager mBluetoothManager;
    public BluetoothAdapter mBluetoothAdapter;
    public BluetoothLeService mBluetoothLeService;
    public ArrayList<VoBluetoothDevices> mDeviceListArrayMain = new ArrayList<>();
    public BluetoothDevice mBluetoothDevice;
    private ScanCallback mBLEScanCallback;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    public final static UUID UUID_ELITE_START_STOP_SYNC_CHAR =
            UUID.fromString(SampleGattAttributes.ELITE_START_STOP_SYNC_CHAR);
    public final static UUID UUID_ELITE_SYNC_CONTACT_CHAR =
            UUID.fromString(SampleGattAttributes.ELITE_SYNC_CONTACT_CHAR);
    public final static UUID UUID_ELITE_SYNC_SURFACE_MSG_CHAR =
            UUID.fromString(SampleGattAttributes.ELITE_SYNC_SURFACE_MSG_CHAR);
    public final static UUID UUID_ELITE_SYNC_DIVER_MSG_CHAR =
            UUID.fromString(SampleGattAttributes.ELITE_SYNC_DIVER_MSG_CHAR);
    public final static UUID UUID_ELITE_SEND_MSG_CHAR =
            UUID.fromString(SampleGattAttributes.ELITE_SEND_MSG_CHAR);

    public String mStringConnectedDevicesAddress = "";

    public onDeviceConnectionStatusChange mOnDeviceConnectionStatusChange;
    public onDeviceMessegeSent mOnDeviceMessegeSent;
    public onResponseGetRefresh mOnResponseGetRefresh;

    public boolean isDeviceScanning = false;
    public boolean isDevicesConnected = false;
    public boolean isConnectionRequestSend = false;
    public boolean timerStartScan = false;
    public boolean isFromDeviceConnection = true;
    public boolean isManualDisconnect = false;
    public startDeviceScanTimer mStartDeviceScanTimer;
    public autoStopDeviceScanTimer mAutoStopDeviceScanTimer;

    private SimpleDateFormat mDateFormatDb;
    LocationManager mLocationManager;
    private boolean isReadyToUnbind = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);
        mDbHelper = new DBHelper(MainActivity.this);
        mUtility = new Utility(MainActivity.this);
        mPreferenceHelper = new PreferenceHelper(MainActivity.this);
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        initToolbar();
        setNavigationDrawer();
        setScrollingBehavior(true);
        mStartDeviceScanTimer = new startDeviceScanTimer(5000, 1000);
        mAutoStopDeviceScanTimer = new autoStopDeviceScanTimer((30000 * 1), 1000);
        try {
            mDbHelper.createDataBase();
            mDbHelper.openDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mUtility.errorDialog(getResources().getString(R.string.ble_not_supported), 1);
        }
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
        setOnDeviceMessageSent(new onDeviceMessegeSent() {
            @Override
            public void onMessegeSentRefreshUI() {

            }
        });
        setResponseGetRefresh(new onResponseGetRefresh() {
            @Override
            public void onRefreshData() {

            }
        });
        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    public void setOnDevicesStatusChange(onDeviceConnectionStatusChange stateChange) {
        this.mOnDeviceConnectionStatusChange = stateChange;
    }

    public void setOnDeviceMessageSent(onDeviceMessegeSent messageSent) {
        this.mOnDeviceMessegeSent = messageSent;
    }

    public void setResponseGetRefresh(onResponseGetRefresh onRefresh) {
        this.mOnResponseGetRefresh = onRefresh;
    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        mToolbar.setNavigationIcon(R.drawable.ic_drawer_icon);
        View actionBar = getLayoutInflater().inflate(R.layout.custome_actionbar, null);
        mImageViewBack = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_back);
        mImageViewAdd = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_add);
        mImageViewPerson = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_person);
        mImageViewDrawer = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_drawer);
        mTextViewTitle = (TextView) actionBar.findViewById(R.id.custom_actionbar_imageview_title);
        mTextViewActiveCount = (CustomTextView) actionBar.findViewById(R.id.custom_actionbar_textview_active_count);
        mToolbar.addView(actionBar);
    }


    @OnClick(R.id.activity_main_imageview_delete)
    public void onDeleteClick(View mView) {
        mDrawerLayout.closeDrawers();
        if (isDevicesConnected) {
            mUtility.errorDialogWithYesNoCallBack("Delete History", "Are you sure you want to delete all history?", "YES", "NO", 1, new onAlertDialogCallBack() {
                @Override
                public void PositiveMethod(DialogInterface dialog, int id) {
                    dialog.cancel();
                    String mStringQuery = "delete from " + mDbHelper.mTableHistory;
                    mDbHelper.exeQuery(mStringQuery);
                    String mStringQueryLastMessage = "update " + mDbHelper.mTableAddressBook + " set " + mDbHelper.mFieldAddressBookLastMessageId + "= ''," + mDbHelper.mFieldAddressBookLastMessageName + "= ''," + mDbHelper.mFieldAddressBookLastMessageTime + "= ''" + " where " + mDbHelper.mFieldAddressBookLastMessageId + "!= ''" + " OR " + mDbHelper.mFieldAddressBookLastMessageId + "!= 'null'";
                    mDbHelper.exeQuery(mStringQueryLastMessage);
                    if (mOnDeviceMessegeSent != null) {
                        mOnDeviceMessegeSent.onMessegeSentRefreshUI();
                    }
                    mUtility.errorDialogWithCallBack("All history message deleted successfully", 0, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {

                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                }

                @Override
                public void NegativeMethod(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        } else {
            showDisconnectedDeviceAlert(false);
        }
    }

    @OnClick(R.id.activity_main_imageview_heat_one)
    public void onHeat1Click(View mView) {
        mDrawerLayout.closeDrawers();
        getHeatMessage(mPreferenceHelper.getDeviceHeat1Msg());
    }

    @OnClick(R.id.activity_main_imageview_heat_two)
    public void onHeat2Click(View mView) {
        mDrawerLayout.closeDrawers();
        getHeatMessage(mPreferenceHelper.getDeviceHeat2Msg());
    }

    @OnClick(R.id.activity_main_imageview_heat_three)
    public void onHeat3Click(View mView) {
        mDrawerLayout.closeDrawers();
        getHeatMessage(mPreferenceHelper.getDeviceHeat3Msg());
    }

    @OnClick(R.id.activity_main_imageview_heat_four)
    public void onHeat4Click(View mView) {
        mDrawerLayout.closeDrawers();
        getHeatMessage(mPreferenceHelper.getDeviceHeat4Msg());
    }

    public void showDisconnectedDeviceAlert(boolean isCancalable) {
        mUtility.errorDialogWithCallBack("Device is disconnected. Please connect and try again.", 1, isCancalable, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                FragmentDeviceConnect mFragmentDeviceConnect = new FragmentDeviceConnect();
                Bundle mBundle = new Bundle();
                mBundle.putBoolean("intent_is_from_device_setup", false);
                replacesFragment(mFragmentDeviceConnect, true, mBundle, 1);
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void getHeatMessage(String mStringPosition) {
        if (isDevicesConnected) {
            DataHolder mDataHolder;
            try {
                String url = "select * from " + mDbHelper.mTableCannedDiverMessage + " where " + mDbHelper.mFieldDiverMessageId + "= '" + mStringPosition + "'";
                System.out.println("Local url " + url);
                mDataHolder = mDbHelper.read(url);
                if (mDataHolder != null) {
                    System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                    if (isDevicesConnected) {
                        for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                            sendMessageToDevice(BLEUtility.intToByte(255), BLEUtility.intToByte(Integer.parseInt(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldDiverMessageMSGId))), true);
                            saveAllHeatMessages(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldDiverMessageMSGId), mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldDiverMessageMessage));
                            break;
                        }
                    } else {
                        showDisconnectedDeviceAlert(false);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showDisconnectedDeviceAlert(false);
        }
    }

    private void saveAllHeatMessages(String mStringSelectedMessageId, String mStringSelectedMessageName) {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mDbHelper.mTableAddressBook;
            System.out.println("Local url " + url);
            mDataHolder = mDbHelper.read(url);
            if (mDataHolder != null && mDataHolder.get_Listholder().size() > 0) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                Calendar cal = Calendar.getInstance();
                Date currentLocalTime = cal.getTime();
                String msgTIme = mDateFormatDb.format(currentLocalTime);
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoAddressBook mVoAddressBook = new VoAddressBook();
                    mVoAddressBook.setId(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookId));
                    mVoAddressBook.setUser_id(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUserId));
                    mVoAddressBook.setUser_name(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUserName));
                    mVoAddressBook.setUser_photo(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUserPhoto));
                    mVoAddressBook.setUser_type(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUserType));
                    mVoAddressBook.setDevice_id(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookDeviceId));
                    mVoAddressBook.setLast_msg_id(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookLastMessageId));
                    mVoAddressBook.setLast_msg_name(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookLastMessageName));
                    mVoAddressBook.setLast_msg_time(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookLastMessageTime));
                    mVoAddressBook.setCreated_date(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookCreatedDate));
                    mVoAddressBook.setUpdated_date(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUpdatedDate));
                    mVoAddressBook.setIs_sync(mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookIsSync));

                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(mDbHelper.mFieldHistoryFromId, "0");
                    mContentValues.put(mDbHelper.mFieldHistoryFromName, "Me");
                    mContentValues.put(mDbHelper.mFieldHistoryToId, mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUserId));
                    mContentValues.put(mDbHelper.mFieldHistoryToName, mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUserName));
                    mContentValues.put(mDbHelper.mFieldHistoryMessageId, mStringSelectedMessageId);
                    mContentValues.put(mDbHelper.mFieldHistoryMessageName, mStringSelectedMessageName);
                    mContentValues.put(mDbHelper.mFieldHistoryTime, msgTIme);
                    mContentValues.put(mDbHelper.mFieldHistoryUpdatedDate, msgTIme);
                    mContentValues.put(mDbHelper.mFieldHistoryStatus, "0");
                    int isInsert = mDbHelper.insertRecord(mDbHelper.mTableHistory, mContentValues);
                    System.out.println("isInsert-" + isInsert);
                    if (isInsert != -1) {
                        System.out.println("Added In Local Db");

                        ContentValues mContentValuesUser = new ContentValues();
                        mContentValuesUser.put(mDbHelper.mFieldAddressBookLastMessageId, mStringSelectedMessageId);
                        mContentValuesUser.put(mDbHelper.mFieldAddressBookLastMessageName, mStringSelectedMessageName);
                        mContentValuesUser.put(mDbHelper.mFieldAddressBookLastMessageTime, msgTIme);
                        String[] mArray = new String[]{mDataHolder.get_Listholder().get(i).get(mDbHelper.mFieldAddressBookUserId)};
                        mDbHelper.updateRecord(mDbHelper.mTableAddressBook, mContentValuesUser, mDbHelper.mFieldAddressBookUserId + "=?", mArray);
                        System.out.println("Updated In Local Db");

                        if (i == mDataHolder.get_Listholder().size() - 1) {
                            if (mOnDeviceMessegeSent != null) {
                                mOnDeviceMessegeSent.onMessegeSentRefreshUI();
                            }
                            mUtility.errorDialogWithCallBack("Message sent successfully", 0, false, new onAlertDialogCallBack() {
                                @Override
                                public void PositiveMethod(DialogInterface dialog, int id) {

                                }

                                @Override
                                public void NegativeMethod(DialogInterface dialog, int id) {

                                }
                            });
                        }
                    } else {
                        System.out.println("Failed Adding In Local DB");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
                            stopScan();
//                            Timer innerTimer = new Timer();
//                            innerTimer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//
//                                }
//                            }, 3000);
                            if (mBluetoothDevice != null) {
                                for (int i = 0; i < mDeviceListArrayMain.size(); i++) {
                                    if (mStringDevicesAddress != null && mStringDevicesAddress.equalsIgnoreCase(mDeviceListArrayMain.get(i).getDeviceAddress())) {
                                        System.out.println(TAG + "-----Connected Device " + mStringDevicesAddress);
                                        mStringConnectedDevicesAddress = mStringDevicesAddress;
                                        System.out.println(TAG + "-----BroadcastReceiver mStringConnectedDevicesAddress " + mStringConnectedDevicesAddress);
                                        mDeviceListArrayMain.get(i).setIsConnected(true);
//                                    Toast.makeText(MainActivity.this, "Device connected", Toast.LENGTH_SHORT).show();
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
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    System.out.println(TAG + "-----Disconnected Device " + action);
                    runOnUiThread(new Runnable() {
                        public void run() {
                            System.out.println(TAG + "-----Disconnected Device step-1");
                            if (mBluetoothDevice != null) {
                                System.out.println(TAG + "-----Disconnected Device step-2");
                                for (int i = 0; i < mDeviceListArrayMain.size(); i++) {
                                    if (mStringDevicesAddress != null && !mStringDevicesAddress.equalsIgnoreCase("")) {
                                        if (mStringDevicesAddress.equalsIgnoreCase(mDeviceListArrayMain.get(i).getDeviceAddress())) {
                                            System.out.println(TAG + "-----Disconnected Device step-3");
                                            mStringConnectedDevicesAddress = "";
                                            mDeviceListArrayMain.get(i).setIsConnected(false);
                                            break;
                                        }
                                    } else {
                                        mStringConnectedDevicesAddress = "";
                                    }
                                }
                                System.out.println(TAG + "-----Disconnected Device step-4");
//                                boolean isConnectedAnyDevice = false;
//                                for (VoBluetoothDevices device : mDeviceListArrayMain) {
//                                    if (device.getIsConnected()) {
//                                        isConnectedAnyDevice = true;
//                                        break;
//                                    }
//                                }
                                isDevicesConnected = false;
                                stopScan();
//                                if (!isFromDeviceConnection) {
//                                    mDeviceListArrayMain = new ArrayList<>();
//                                    RescanDevice();
//                                }
                                if (isManualDisconnect) {
                                    System.out.println("MANUAL DISCONNECT");
                                    isManualDisconnect = false;
                                } else {
//                                    System.out.println("AUTO CONNECT AFTER DISCONNECT");
//                                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(mStringDevicesAddress);
//                                    ConnectDevices(device, false);
                                }
                                if (mOnDeviceConnectionStatusChange != null) {
                                    System.out.println(TAG + "-----Disconnected Device step-5");
                                    mOnDeviceConnectionStatusChange.onDisconnect(null, mBluetoothDevice.getName(), mBluetoothDevice);
                                }
//                            Toast.makeText(MainActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                            }

                        }
                    });
                    System.out.println(TAG + "-----Disconnected Device step-6");
                } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            runOnUiThread(new Runnable() {
                                public void run() {
                                    System.out.println(TAG + "-----ACTION_GATT_SERVICES_DISCOVERED");
                                    displayGattServices(mBluetoothLeService.getSupportedGattServices(), mStringDevicesAddress, mStringDevicesName);
                                }
                            });
                        }
                    });
                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    System.out.println(TAG + "-----ACTION_DATA_AVAILABLE");
                    String mStringReceivedMsg = "";
                    if (intent.getStringExtra(BluetoothLeService.EXTRA_DATA) != null) {
                        mStringReceivedMsg = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                        if (mStringReceivedMsg != null && !mStringReceivedMsg.equals("") && !mStringReceivedMsg.equals("null")) {
                            System.out.println("mStringReceivedMsg-" + mStringReceivedMsg);
                            mStringReceivedMsg = mStringReceivedMsg.replaceAll("\\s+", "");
                            System.out.println("mStringReceivedMsg-" + mStringReceivedMsg);
                            if (mStringReceivedMsg.toLowerCase().startsWith("0a")) {
                                if (mStringReceivedMsg != null && mStringReceivedMsg.length() >= 24) {
                                    String strLatPrefix = mStringReceivedMsg.substring(4, 6);
                                    String strLatPostfix = mStringReceivedMsg.substring(6, 14);
                                    String strLonPrefix = mStringReceivedMsg.substring(14, 16);
                                    String strLonPostfix = mStringReceivedMsg.substring(16, 24);

                                    System.out.println("strLatPrefix-" + strLatPrefix);
                                    System.out.println("strLatPostfix-" + strLatPostfix);
                                    System.out.println("strLonPrefix-" + strLonPrefix);
                                    System.out.println("strLonPostfix-" + strLonPostfix);

                                    String mStrLatitudePost = "" + BLEUtility.hexToDecimal(strLatPostfix);
                                    String mStrLongitudePost = "" + BLEUtility.hexToDecimal(strLonPostfix);

                                    mStrLatitudePost = mStrLatitudePost.substring(0, 2) + "." + mStrLatitudePost.substring(2, mStrLatitudePost.length());
                                    mStrLongitudePost = mStrLongitudePost.substring(0, 2) + "." + mStrLongitudePost.substring(2, mStrLongitudePost.length());

                                    final double mLongLatitude = (BLEUtility.hexToDecimal(strLatPrefix) + (Double.parseDouble(mStrLatitudePost) / 60));
                                    final double mLongLongitude = (BLEUtility.hexToDecimal(strLonPrefix) + (Double.parseDouble(mStrLongitudePost) / 60));


                                    System.out.println("mLongLatitude-" + mLongLatitude);
                                    System.out.println("mLongLongitude-" + mLongLongitude);
                                    try {
                                        String encryptedLatitude = AESCrypt.encrypt("ATAK_SC4", mLongLatitude + "");
                                        String encryptedLongitude = AESCrypt.encrypt("ATAK_SC4", mLongLongitude + "");
                                        System.out.println("mLongLatitude-" + encryptedLatitude);
                                        System.out.println("mLongLongitude-" + encryptedLongitude);
                                        final Intent intentBroadcast = new Intent();
                                        intentBroadcast.setAction("com.succorfish.eliteoperator");
                                        intentBroadcast.putExtra("succorfish_latitude", encryptedLatitude);
                                        intentBroadcast.putExtra("succorfish_longitude", encryptedLongitude);
//        intent.setComponent(new ComponentName("com.succorfish.locationreciver","com.succorfish.locationreciver.MyBroadcastReceiver"));
                                        sendBroadcast(intentBroadcast);
                                    } catch (GeneralSecurityException e) {
                                        //handle error
                                        e.printStackTrace();
                                    }

                                    mUtility.errorDialogWithYesNoCallBack("GPS Location", "GPS Location Received successfully.", "View on Map", "OK", 0, new onAlertDialogCallBack() {
                                        @Override
                                        public void PositiveMethod(DialogInterface dialog, int id) {
                                            try {
                                                String uriBegin = "geo:" + mLongLatitude + "," + mLongLongitude;
                                                String query = mLongLatitude + "," + mLongLongitude + "('Succorfish')";
                                                String encodedQuery = Uri.encode(query);
                                                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                                                Uri uri = Uri.parse(uriString);
                                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                                                startActivity(intent);
                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            }
                                        }

                                        @Override
                                        public void NegativeMethod(DialogInterface dialog, int id) {

                                        }
                                    });
                                }
                            } else if (mStringReceivedMsg.toLowerCase().startsWith("07")) {
                                if (mStringReceivedMsg != null && mStringReceivedMsg.length() >= 6) {
                                    String strContactPrefix = mStringReceivedMsg.substring(4, 6);
                                    System.out.println("strContactPrefix-" + strContactPrefix);
                                    int contactId = BLEUtility.hexToDecimal(strContactPrefix);
                                    System.out.println("contactId-" + contactId);
                                    String queryContactName = "select " + mDbHelper.mFieldAddressBookUserName + " from " + mDbHelper.mTableAddressBook + " where " + mDbHelper.mFieldAddressBookId + "= '" + contactId + "'";
                                    String mStrContactName = mDbHelper.getQueryResult(queryContactName);
                                    System.out.println("mStrContactName--" + mStrContactName);
                                    if (mStrContactName != null && !mStrContactName.equals("")) {
                                        mUtility.errorDialogWithCallBack(contactId + ". " + mStrContactName, 0, false, new onAlertDialogCallBack() {
                                            @Override
                                            public void PositiveMethod(DialogInterface dialog, int id) {

                                            }

                                            @Override
                                            public void NegativeMethod(DialogInterface dialog, int id) {

                                            }
                                        });
                                    }
                                }
                            } else if (mStringReceivedMsg.toLowerCase().startsWith("01")) {
                                mUtility.errorDialogWithCallBack("Time set successfully", 0, false, new onAlertDialogCallBack() {
                                    @Override
                                    public void PositiveMethod(DialogInterface dialog, int id) {

                                    }

                                    @Override
                                    public void NegativeMethod(DialogInterface dialog, int id) {

                                    }
                                });
                            } else if (mStringReceivedMsg.toLowerCase().startsWith("02")) {
                                if (mStringReceivedMsg != null && mStringReceivedMsg.length() >= 6) {
                                    String strContactPrefix = mStringReceivedMsg.substring(4, 6);
                                    System.out.println("strContactPrefix-" + strContactPrefix);
                                    int contactId = BLEUtility.hexToDecimal(strContactPrefix);
                                    System.out.println("contactId-" + contactId);
                                    String queryContactName = "select " + mDbHelper.mFieldAddressBookUserName + " from " + mDbHelper.mTableAddressBook + " where " + mDbHelper.mFieldAddressBookId + "= '" + contactId + "'";
                                    String mStrContactName = mDbHelper.getQueryResult(queryContactName);
                                    System.out.println("mStrContactName--" + mStrContactName);
                                    if (mStrContactName != null && !mStrContactName.equals("")) {
                                        mUtility.errorDialogWithCallBack("Modem sync with " + contactId + ". " + mStrContactName, 0, false, new onAlertDialogCallBack() {
                                            @Override
                                            public void PositiveMethod(DialogInterface dialog, int id) {

                                            }

                                            @Override
                                            public void NegativeMethod(DialogInterface dialog, int id) {

                                            }
                                        });
                                    }
                                }
                            } else if (mStringReceivedMsg.toLowerCase().startsWith("11")) {
                                if (mStringReceivedMsg != null && mStringReceivedMsg.length() >= 28) {
                                    String strGsmInterval = mStringReceivedMsg.substring(4, 8);
                                    String strGpsInterval = mStringReceivedMsg.substring(8, 12);
                                    String strIridiumInterval = mStringReceivedMsg.substring(12, 16);
                                    String strGsmTimeout = mStringReceivedMsg.substring(16, 20);
                                    String strGpsTimeout = mStringReceivedMsg.substring(20, 24);
                                    String strIridiumTimeout = mStringReceivedMsg.substring(24, 28);
                                    strGsmInterval = strGsmInterval.substring(2, strGsmInterval.length()) + "" + strGsmInterval.substring(0, 2);
                                    strGpsInterval = strGpsInterval.substring(2, strGpsInterval.length()) + "" + strGpsInterval.substring(0, 2);
                                    strIridiumInterval = strIridiumInterval.substring(2, strIridiumInterval.length()) + "" + strIridiumInterval.substring(0, 2);
                                    strGsmTimeout = strGsmTimeout.substring(2, strGsmTimeout.length()) + "" + strGsmTimeout.substring(0, 2);
                                    strGpsTimeout = strGpsTimeout.substring(2, strGpsTimeout.length()) + "" + strGpsTimeout.substring(0, 2);
                                    strIridiumTimeout = strIridiumTimeout.substring(2, strIridiumTimeout.length()) + "" + strIridiumTimeout.substring(0, 2);

                                    mPreferenceHelper.setGSMInterval(BLEUtility.hexToDecimal(strGsmInterval) + "");
                                    mPreferenceHelper.setGPSInterval(BLEUtility.hexToDecimal(strGpsInterval) + "");
                                    mPreferenceHelper.setIridiumInterval(BLEUtility.hexToDecimal(strIridiumInterval) + "");
                                    mPreferenceHelper.setGSMTimeOut(BLEUtility.hexToDecimal(strGsmTimeout) + "");
                                    mPreferenceHelper.setGPSTimeOut(BLEUtility.hexToDecimal(strGpsTimeout) + "");
                                    mPreferenceHelper.setIridiumTimeOut(BLEUtility.hexToDecimal(strIridiumTimeout) + "");

                                    if (mOnResponseGetRefresh != null) {
                                        mOnResponseGetRefresh.onRefreshData();
                                    }
                                    mUtility.errorDialogWithCallBack("Device Settings " + "\nGSM Interval : " + mPreferenceHelper.getGSMInterval() + "\nGPS Interval : " + mPreferenceHelper.getGPSInterval() + "\nIridium Interval : " + mPreferenceHelper.getIridiumInterval() + "\nGSM Timeout : " + mPreferenceHelper.getGSMTimeOut() + "\nGPS Timeout : " + mPreferenceHelper.getGPSTimeOut() + "\nIridium Timeout : " + mPreferenceHelper.getIridiumTimeOut(), 0, false, new onAlertDialogCallBack() {
                                        @Override
                                        public void PositiveMethod(DialogInterface dialog, int id) {

                                        }

                                        @Override
                                        public void NegativeMethod(DialogInterface dialog, int id) {

                                        }
                                    });
                                }
                            } else {
                                if (mStringReceivedMsg != null && mStringReceivedMsg.length() >= 8) {
                                    String strContactIdHex = mStringReceivedMsg.substring(4, 6);
                                    String strDiverMessageIdHex = mStringReceivedMsg.substring(6, 8);
                                    int contactId = BLEUtility.hexToDecimal(strContactIdHex);
                                    int diverMessageId = BLEUtility.hexToDecimal(strDiverMessageIdHex);
                                    System.out.println("contactId-" + contactId);
                                    System.out.println("diverMessageId-" + diverMessageId);
                                    String queryContactName = "select " + mDbHelper.mFieldAddressBookUserName + " from " + mDbHelper.mTableAddressBook + " where " + mDbHelper.mFieldAddressBookId + "= '" + contactId + "'";
                                    String mStrContactName = mDbHelper.getQueryResult(queryContactName);
                                    System.out.println("mStrContactName--" + mStrContactName);
                                    String queryMsgName = "select " + mDbHelper.mFieldDiverMessageMessage + " from " + mDbHelper.mTableCannedDiverMessage + " where " + mDbHelper.mFieldDiverMessageMSGId + "= '" + diverMessageId + "'";
                                    String mStrMsgName = mDbHelper.getQueryResult(queryMsgName);
                                    System.out.println("mStrMsgName--" + mStrMsgName);
                                    if (mStrContactName != null && !mStrContactName.equals("") && mStrMsgName != null && !mStrMsgName.equals("")) {
                                        mUtility.errorDialogWithCallBack(mStrContactName + " Sent Message - " + mStrMsgName, 0, false, new onAlertDialogCallBack() {
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
                        }
                    }
                } else if (action.equalsIgnoreCase(BluetoothLeService.RSSI_DATA)) {
                    System.out.println(TAG + "--rssi data " + mBluetoothLeService.updateRssi);
                } else if (action.equalsIgnoreCase(BluetoothLeService.ERORR)) {
                    System.out.println(TAG + "-----ERORR Device " + action);
                    mDeviceListArrayMain = new ArrayList<>();
                    RescanDevice();
                    if (mOnDeviceConnectionStatusChange != null) {
                        mOnDeviceConnectionStatusChange.onError();
                    }

//                for (VoBluetoothDevices device : mDeviceListArrayMain) {
//                    if (device.getDeviceAddress().equalsIgnoreCase(mStringDevicesAddress)) {
//                        ConnectDevices(device.getBluetoothDevice());
//                    }
//                }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices, final String mStringAddress, String mStringDevicesName) {
        if (gattServices == null)
            return;
        // Loops through available GATT Services.
//        final BluetoothGattService genericAttributeService = mBluetoothLeService.getGattService(mStringAddress, SampleGattAttributes.SERVICE_ELITE_SERVICE);
//        if (genericAttributeService !=null){
//
//        }else {
//            System.out.println("SERVICE NOT AVAILABLE");
//        }
        for (BluetoothGattService gattService : gattServices) {
            System.out.println("service");
            System.out.println("getUuid-" + gattService.getUuid().toString());
            System.out.println("SERVICE_ELITE_SERVICE-" + SampleGattAttributes.SERVICE_ELITE_SERVICE);
            if (SampleGattAttributes.SERVICE_ELITE_SERVICE.toLowerCase().equalsIgnoreCase(gattService
                    .getUuid().toString().toLowerCase())) {
                System.out.println("serviceName-" + gattService
                        .getUuid().toString());
                List<BluetoothGattCharacteristic> gattCharacteristics =
                        gattService.getCharacteristics();
                // Loops through available Characteristics.
                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                    if (gattCharacteristic != null) {
                        final int charaProp = gattCharacteristic.getProperties();
//                    System.out.println(TAG + "--UUID " + gattCharacteristic.getUuid().toString());
                        System.out.println("CharName-" + gattCharacteristic.getUuid());
                        if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                                (UUID_ELITE_START_STOP_SYNC_CHAR.toString().toLowerCase().equals(gattCharacteristic.getUuid().toString().toLowerCase()))) {
                            mBluetoothLeService.setStartStopCharacteristic(gattCharacteristic);
                            mBluetoothLeService.setSendMessageCharacteristic(gattCharacteristic);
                            mBluetoothLeService.setCharacteristicNotifications(gattCharacteristic, true);

                        } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                                (UUID_ELITE_SYNC_CONTACT_CHAR.toString().toLowerCase().equals(gattCharacteristic.getUuid().toString().toLowerCase()))) {
                            mBluetoothLeService.setSyncContactCharacteristic(gattCharacteristic);
                        } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                                (UUID_ELITE_SYNC_SURFACE_MSG_CHAR.toString().toLowerCase().equals(gattCharacteristic.getUuid().toString().toLowerCase()))) {
                            mBluetoothLeService.setSyncSurfaceMsgCharacteristic(gattCharacteristic);
                        } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                                (UUID_ELITE_SYNC_DIVER_MSG_CHAR.toString().toLowerCase().equals(gattCharacteristic.getUuid().toString().toLowerCase()))) {
                            mBluetoothLeService.setSyncDiverMsgCharacteristic(gattCharacteristic);
                        } else if (((charaProp | BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) > 0) &&
                                (UUID_ELITE_SEND_MSG_CHAR.toString().toLowerCase().equals(gattCharacteristic.getUuid().toString().toLowerCase()))) {
                        } else {
                            mBluetoothLeService.setStartStopCharacteristic(gattCharacteristic);
                        }
                    }
                }
            }
        }
//        final BluetoothGattService genericAttributeService = mBluetoothLeService.getGattService(mStringAddress, kGenericAttributeService);
//        if (genericAttributeService != null) {
//            Log.d(TAG, "kGenericAttributeService found. Check if kServiceChangedCharacteristic exists");
//            final UUID characteristicUuid = UUID.fromString(kServiceChangedCharacteristic);
//            final BluetoothGattCharacteristic dataCharacteristic = genericAttributeService.getCharacteristic(characteristicUuid);
//            if (dataCharacteristic != null) {
//                System.out.println(TAG + "--UUID " + dataCharacteristic.getUuid().toString());
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        mBluetoothLeService.setCharacteristicNotification(genericAttributeService, kServiceChangedCharacteristic, true, mStringAddress);
//                    }
//                });
//                Log.d(TAG, "kServiceChangedCharacteristic exists. Enable indication");
//            } else {
//                Log.d(TAG, "Skip enable indications for kServiceChangedCharacteristic. Characteristic not found");
//            }
//        } else {
//            Log.d(TAG, "Skip enable indications for kServiceChangedCharacteristic. kGenericAttributeService not found");
//        }

    }

    public void ConnectDevices(final BluetoothDevice mBluetoothDeviceConnect, final boolean isBackgroundConnect) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mBluetoothLeService != null) {
                    System.out.println(TAG + "--Connecting-");
                    stopScan();
                    mBluetoothDevice = mBluetoothDeviceConnect;
                    mBluetoothLeService.connect(mBluetoothDeviceConnect);
                }
            }
        });

    }

    public void disconnectDevices(final BluetoothDevice mBluetoothDevice) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stopScan();
                mBluetoothLeService.removeDevices(mBluetoothDevice);
            }
        });
    }

    public void RescanDevice() {

//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        stopScan();
//                        if (mAutoStopDeviceScanTimer != null)
//                            mAutoStopDeviceScanTimer.cancel();
//                        if (mAutoStopDeviceScanTimer != null)
//                            mAutoStopDeviceScanTimer.start();
//                        startDeviceScan();
//                    }
//                });
//            }
//        };
//        thread.start();

//        stopScan();
        mDeviceListArrayMain = new ArrayList<>();
        if (mAutoStopDeviceScanTimer != null)
            mAutoStopDeviceScanTimer.cancel();
        if (mAutoStopDeviceScanTimer != null)
            mAutoStopDeviceScanTimer.start();
        startDeviceScan();

    }

    @SuppressLint("InlinedApi")
    public void startDeviceScan() {
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
            mBluetoothManager = (BluetoothManager) this.getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            mBLEScanCallback = new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, final ScanResult result) {
//                    runOnUiThread(new Runnable() {
//                        @SuppressLint("DefaultLocale")
//                        @Override
//                        public void run() {
//                            if (result.getScanRecord() != null) {
//                                final BluetoothDevice newDeivce = result.getDevice();
//                                if ((newDeivce == null)) {
//                                    return;
//                                }
//                                onScanResultGet(newDeivce, result.getScanRecord().getBytes());
//                            }
//                        }
//                    });
                    final BluetoothDevice newDeivce = result.getDevice();
                    if ((newDeivce == null)) {
                        return;
                    }
                    onScanResultGet(newDeivce, result.getScanRecord().getBytes());
                }

                @Override
                public void onScanFailed(int errorCode) {
                    super.onScanFailed(errorCode);
                    System.out.println("--------------FAILE SCANNN-------" + errorCode);
                }
            };
            ScanFilter.Builder filterBuilder = new ScanFilter.Builder();
            ScanSettings.Builder settingsBuilder = new ScanSettings.Builder();
            settingsBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY);
            List<ScanFilter> filters = new ArrayList<ScanFilter>();
            filters.add(filterBuilder.build());
            if (mBluetoothLeScanner != null) {
                mBluetoothLeScanner.startScan(filters, settingsBuilder.build(), mBLEScanCallback);
                isDeviceScanning = true;
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
            mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                public void onLeScan(final BluetoothDevice newDeivce, final int newRssi, final byte[] newScanRecord) {
                    try {
//                        runOnUiThread(new Runnable() {
//                            @SuppressLint("DefaultLocale")
//                            @Override
//                            public void run() {
//                                if ((newDeivce == null)) {
//                                    return;
//                                }
//                                onScanResultGet(newDeivce, newScanRecord);
//                            }
//                        });
                        if ((newDeivce == null)) {
                            return;
                        }
                        onScanResultGet(newDeivce, newScanRecord);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mBluetoothAdapter.startLeScan(mLeScanCallback);
            isDeviceScanning = true;
        }
    }

    private void onScanResultGet(BluetoothDevice newDeivce, byte[] newScanRecord) {

//                                System.out.println(TAG + "----scan ScanRecord " + result.getScanRecord().toString());
        if (isFromDeviceConnection) {
            if (newDeivce.getName() != null && !newDeivce.getName().equalsIgnoreCase("") && !newDeivce.getName().equalsIgnoreCase(null)) {
                if (newDeivce.getName().toLowerCase().contains("succorfish sc4")) {//SC4
                    String mStringHexData = BLEUtility.toHexString(newScanRecord, true);
                    if (mStringHexData != null && !mStringHexData.equalsIgnoreCase("")) {
                        if (mStringHexData.contains("5900")) {
                            System.out.println(TAG + "----scan device name " + newDeivce.getName());
                            System.out.println(TAG + "----scan device HEX " + mStringHexData);
                            boolean contains = false;
                            for (VoBluetoothDevices device : mDeviceListArrayMain) {
                                if (newDeivce.getAddress().equals(device.getDeviceAddress())) {
                                    contains = true;
                                    break;
                                }
                            }
                            VoBluetoothDevices mVoBluetoothDevices = new VoBluetoothDevices();
                            mVoBluetoothDevices.setBluetoothDevice(newDeivce);
                            mVoBluetoothDevices.setDeviceRSSI(0);
                            mVoBluetoothDevices.setDeviceName(newDeivce.getName());
                            mVoBluetoothDevices.setDeviceAddress(newDeivce.getAddress());
                            mVoBluetoothDevices.setIsConnected(false);
                            // >= 8.0
                            // mVoBluetoothDevices.setIsConnectable(result.isConnectable());
                            if (newDeivce.getAddress().equalsIgnoreCase(mStringConnectedDevicesAddress)) {
                                System.out.println("CONNECTEDD");
                                mVoBluetoothDevices.setIsConnected(true);
                            } else {
                                mVoBluetoothDevices.setIsConnected(false);
                            }
                            mVoBluetoothDevices.setDeviceHexData(mStringHexData);
                            if (!contains) {
                                mDeviceListArrayMain.add(mVoBluetoothDevices);
                                if (mOnDeviceConnectionStatusChange != null) {
                                    mOnDeviceConnectionStatusChange.addScanDevices();
                                    mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothDevices);
                                }
                            }
                        }
                    }

                }
            }


//            if (!isDevicesConnected) {
//                // Connect Any one Device
//                boolean isConnectedAnyDevice = false;
//                for (VoBluetoothDevices device : mDeviceListArrayMain) {
//                    if (device.getIsConnected()) {
//                        isConnectedAnyDevice = true;
//                        break;
//                    }
//                }
//                if (!isConnectedAnyDevice) {
//                    if (mDeviceListArrayMain != null && mDeviceListArrayMain.size() > 0) {
//                        if (!isConnectionRequestSend) {
//                            System.out.println("First Device Auto Connect");
//                            System.out.println(TAG + "--connection Request Address-" + mDeviceListArrayMain.get(0).getBluetoothDevice().getAddress());
//                            isConnectionRequestSend = true;
//                            ConnectDevices(mDeviceListArrayMain.get(0).getBluetoothDevice(), true);
//                            Timer innerTimer = new Timer();
//                            innerTimer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    isConnectionRequestSend = false;
//                                }
//                            }, 3000);
//                        }
//                    }
//                }
//            }
        } else {
//            if (!isDevicesConnected) {
//                if (newDeivce.getName() != null && !newDeivce.getName().equalsIgnoreCase("") && !newDeivce.getName().equalsIgnoreCase(null)) {
//                    if (newDeivce.getName().toLowerCase().contains("succorfish sc4")) {//SC4
//                        String mStringHexData = BLEUtility.toHexString(newScanRecord, true);
//                        if (mStringHexData != null && !mStringHexData.equalsIgnoreCase("")) {
//                            if (mStringHexData.contains("5900")) {
//                                System.out.println(TAG + "----scan device name " + newDeivce.getName());
//                                System.out.println(TAG + "----scan device HEX " + mStringHexData);
//                                boolean contains = false;
//                                for (VoBluetoothDevices device : mDeviceListArrayMain) {
//                                    if (newDeivce.getAddress().equals(device.getDeviceAddress())) {
//                                        contains = true;
//                                        break;
//                                    }
//                                }
//                                VoBluetoothDevices mVoBluetoothDevices = new VoBluetoothDevices();
//                                mVoBluetoothDevices.setBluetoothDevice(newDeivce);
//                                mVoBluetoothDevices.setDeviceRSSI(0);
//                                mVoBluetoothDevices.setDeviceName(newDeivce.getName());
//                                mVoBluetoothDevices.setDeviceAddress(newDeivce.getAddress());
//                                mVoBluetoothDevices.setIsConnected(false);
//                                // >= 8.0
//                                // mVoBluetoothDevices.setIsConnectable(result.isConnectable());
//                                if (newDeivce.getAddress().equalsIgnoreCase(mStringConnectedDevicesAddress)) {
//                                    System.out.println("CONNECTEDD");
//                                    mVoBluetoothDevices.setIsConnected(true);
//                                } else {
//                                    mVoBluetoothDevices.setIsConnected(false);
//                                }
//                                mVoBluetoothDevices.setDeviceHexData(mStringHexData);
//                                if (!contains) {
//                                    mDeviceListArrayMain.add(mVoBluetoothDevices);
//                                    if (mOnDeviceConnectionStatusChange != null) {
//                                        mOnDeviceConnectionStatusChange.addScanDevices();
//                                        mOnDeviceConnectionStatusChange.addScanDevices(mVoBluetoothDevices);
//                                    }
//                                }
//                                if (!isDevicesConnected) {
//                                    // Connect Any one Device
//                                    boolean isConnectedAnyDevice = false;
//                                    for (VoBluetoothDevices device : mDeviceListArrayMain) {
//                                        if (device.getIsConnected()) {
//                                            isConnectedAnyDevice = true;
//                                            break;
//                                        }
//                                    }
//                                    if (!isConnectedAnyDevice) {
//                                        if (mDeviceListArrayMain != null && mDeviceListArrayMain.size() > 0) {
//                                            if (!isConnectionRequestSend) {
//                                                System.out.println("First Device Auto Connect");
//                                                System.out.println(TAG + "--connection Request Address-" + mDeviceListArrayMain.get(0).getBluetoothDevice().getAddress());
//                                                isConnectionRequestSend = true;
//                                                ConnectDevices(mDeviceListArrayMain.get(0).getBluetoothDevice(), true);
//                                                Timer innerTimer = new Timer();
//                                                innerTimer.schedule(new TimerTask() {
//                                                    @Override
//                                                    public void run() {
//                                                        isConnectionRequestSend = false;
//                                                    }
//                                                }, 3000);
//                                            }
//                                        }
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//
//            }
        }
//        System.out.println("DeviceListSize-" + mDeviceListArrayMain.size() + "--DeviceListTempSize-" + mDeviceListArrayTemp.size());
    }

    public void stopScan() {
        System.out.println(TAG + "-------Scan stopped.....");
        if (getIsSDKAbove21()) {
//            if (!getScanning()) {
//                return;
//            }
            isDeviceScanning = false;
            if (mBluetoothLeScanner != null && mBluetoothAdapter.isEnabled() &&
                    mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
                System.out.println(TAG + "-------Scan STOPPPPPPP.....");
                if (mBluetoothLeScanner != null && mBLEScanCallback != null) {
                    mBluetoothLeScanner.stopScan(mBLEScanCallback);
                }
            }
        } else {
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() &&
                    mBluetoothAdapter.getState() == BluetoothAdapter.STATE_ON) {
//                if (!getScanning()) {
//                    return;
//                }
                isDeviceScanning = false;
                if (mBluetoothAdapter != null && mLeScanCallback != null) {
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                }
            }
        }
    }

    private class autoStopDeviceScanTimer extends CountDownTimer {

        public autoStopDeviceScanTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            System.out.println("Add Device Scan Auto Scan Stop---");
            stopScan();
        }
    }

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
            timerStartScan = false;
            if (!isDevicesConnected) {
//                if (!isFinishing()) {
//                    mUtility.errorDialog("Something went wrong. please scan again.",1);
//                }
            }
        }
    }

    public void connectDeviceWithProgress() {
        if (timerStartScan) {
            return;
        }
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() == false) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, BLUETOOTH_ENABLE_REQT);
            return;
        }
        timerStartScan = true;
        mDeviceListArrayMain = new ArrayList<>();
        stopScan();
        RescanDevice();
        showProgress("Connecting...", true);
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.start();
    }

    public void showProgress(String mStringProgressTitle, boolean isShowTitle) {
        mUtility.ShowProgress(mStringProgressTitle);
    }

    public void hideProgress() {
        mUtility.HideProgress();
    }

    static String sparseByteArrayToString(SparseArray<byte[]> array) {
        if (array == null) {
            return "null";
        }
        if (array.size() == 0) {
            return "{}";
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append('{');
        for (int i = 0; i < array.size(); ++i) {
            buffer.append(array.keyAt(i)).append("=").append(Arrays.toString(array.valueAt(i)));
        }
        buffer.append('}');
        return buffer.toString();
    }

    public static boolean getIsSDKAbove21() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public boolean getScanning() {
        return isDeviceScanning;
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            callMarshMallowParmession();
        }
        mLocationManager = (LocationManager) MainActivity.this.getSystemService(Context.LOCATION_SERVICE);
        if (!mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            if (Build.VERSION.SDK_INT >= 23) {
                mUtility.errorDialogWithYesNoCallBack("Turn on GPS Location.", "Since Android 6.0 Marshmallow the system requires access to device's location in order to scan devices.", "Allow", "Cancel", 1, new onAlertDialogCallBack() {
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

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mBluetoothAdapter = mBluetoothManager.getAdapter();
            registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
            if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled() == false) {
                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableIntent, BLUETOOTH_ENABLE_REQT);
            } else {
                Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            }

            IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mbluetoothStateReceiver, filter);
            isReadyToUnbind = true;
        }
    }

    public void startDeviceSync(int syncType) {
        System.out.println("Device Sync start");
        byte start_sync_value[] = new byte[3];
        short startSync = 0x06;
        short startFix = 0x01;
        start_sync_value[0] = (byte) (startSync & 0xFF);
        start_sync_value[1] = (byte) (startFix & 0xFF);
        start_sync_value[2] = BLEUtility.intToByte(syncType);
        sendMessageOverBle(start_sync_value, "Type_StartStop");
    }

    public void stopDeviceSync() {
        System.out.println("Device Sync stop");
        byte stop_sync_value[] = new byte[3];
        short startSync = 0x09;
        short stopFix = 0x01;
        short stop = 0x01;
        stop_sync_value[0] = (byte) (startSync & 0xFF);
        stop_sync_value[1] = (byte) (stopFix & 0xFF);
        stop_sync_value[2] = (byte) (stop & 0xFF);
        sendMessageOverBle(stop_sync_value, "Type_StartStop");
    }

    public void syncAddressBookContact(int strId, byte[] byteContactData) {
        byte sync_contact_value[] = new byte[1 + byteContactData.length];
        byte msg_index = 0, index;
//        if (byteContactData.length > 9 || byteContactData.length == 0) {
//            return;
//        }
        /* Initialize the array with zeros */
        for (index = 0; index < sync_contact_value.length; index++) {
            sync_contact_value[index] = 0;
        }
        //Add ID
        sync_contact_value[msg_index] = BLEUtility.intToByte(strId);
        msg_index++;
        // Add contactData
        for (index = 0; index < byteContactData.length; index++) {
            System.out.println("dest_id[" + index + "]--" + byteContactData[index]);
            sync_contact_value[msg_index++] = byteContactData[index];
        }
        sendMessageOverBle(sync_contact_value, "Type_SyncContact");
    }

    public void syncCannedSurfaceMessage(int strId, byte[] byteMessageData, boolean isSurfaceMessage) {
        byte sync_canned_value[] = new byte[1 + byteMessageData.length];
        byte msg_index = 0, index;
//        if (byteMessageData.length > 9 || byteMessageData.length == 0) {
//            return;
//        }
        /* Initialize the array with zeros */
        for (index = 0; index < sync_canned_value.length; index++) {
            sync_canned_value[index] = 0;
        }
        //Add ID
        sync_canned_value[msg_index] = BLEUtility.intToByte(strId);
        msg_index++;
        // Add MessageData
        for (index = 0; index < byteMessageData.length; index++) {
            System.out.println("dest_id[" + index + "]--" + byteMessageData[index]);
            sync_canned_value[msg_index++] = byteMessageData[index];
        }
        if (isSurfaceMessage) {
            sendMessageOverBle(sync_canned_value, "Type_SyncSurfaceMsg");
        } else {
            sendMessageOverBle(sync_canned_value, "Type_SyncDiverMsg");
        }
    }

    public void setDeviceTime(byte[] byteTimeStamp) {
        byte time_value[] = new byte[6];
        byte msg_index = 0, index;
        short timePrefix = 0x01;
        short timePrefix1 = 0x04;
//        if (byteMessageData.length > 9 || byteMessageData.length == 0) {
//            return;
//        }
        /* Initialize the array with zeros */
        for (index = 0; index < time_value.length; index++) {
            time_value[index] = 0;
        }
//        time_value[msg_index] = (byte) (timePrefix & 0xFF);
//        time_value[msg_index++] = (byte) (timePrefix1 & 0xFF);

        time_value[msg_index] = (byte) (timePrefix & 0xFF);
        msg_index++;
        time_value[msg_index] = (byte) (timePrefix1 & 0xFF);
        // Add TimeData
        for (index = 0; index < byteTimeStamp.length; index++) {
            System.out.println("dest_id[" + index + "]--" + byteTimeStamp[index]);
            msg_index++;
            time_value[msg_index] = byteTimeStamp[index];
        }
        sendMessageOverBle(time_value, "Type_SetTime");
    }

    public void setSyncModem(byte byteModemId) {
        byte modem_value[] = new byte[3];
        short timePrefix = 0x02;
        short timePrefix1 = 0x01;
//        if (byteMessageData.length > 9 || byteMessageData.length == 0) {
//            return;
//        }
        modem_value[0] = (byte) (timePrefix & 0xFF);
        modem_value[1] = (byte) (timePrefix1 & 0xFF);
        modem_value[2] = byteModemId;
        sendMessageOverBle(modem_value, "Type_SetTime");
    }

    public void setDeviceSetting(boolean isFirstTimeRequest, byte[] byteDeviceSettingData) {
        byte device_value[] = new byte[14];
        byte msg_index = 0, index;
        short devicePrefixOpcode = 0x11;
        short devicePrefixLength = 0x00;
        short devicePrefixDataLength = 0x0C;
        for (index = 0; index < device_value.length; index++) {
            device_value[index] = 0;
        }

        device_value[msg_index] = (byte) (devicePrefixOpcode & 0xFF);
        msg_index++;
        if (isFirstTimeRequest) {
            device_value[msg_index] = (byte) (devicePrefixLength & 0xFF);
        } else {
            device_value[msg_index] = (byte) (devicePrefixDataLength & 0xFF);
            for (index = 0; index < byteDeviceSettingData.length; index++) {
                System.out.println("Device_Setting[" + index + "]--" + byteDeviceSettingData[index]);
                msg_index++;
                device_value[msg_index] = byteDeviceSettingData[index];
            }
        }
        sendMessageOverBle(device_value, "Type_DeviceSetting");
    }


    public void setRequestModem(byte byteModemId) {
        byte modem_value[] = new byte[3];
        short timePrefix = 0x07;
        short timePrefix1 = 0x01;
//        if (byteMessageData.length > 9 || byteMessageData.length == 0) {
//            return;
//        }
        modem_value[0] = (byte) (timePrefix & 0xFF);
        modem_value[1] = (byte) (timePrefix1 & 0xFF);
        modem_value[2] = byteModemId;
        sendMessageOverBle(modem_value, "Type_SetTime");
    }

    public void getCurrentLocation() {
        byte location_value[] = new byte[3];
        short startSync = 0x0A;
        short stopFix = 0x01;
        short stop = 0x01;
        location_value[0] = (byte) (startSync & 0xFF);
        location_value[1] = (byte) (stopFix & 0xFF);
        location_value[2] = (byte) (stop & 0xFF);
        sendMessageOverBle(location_value, "Type_Location");
    }

    public void sendMessageToDevice(byte byteUserId, byte byteMessageId, boolean isAllDevice) {
        byte message_value[] = new byte[4];
        short msgPrefix = 0x05;
        short msgPrefix1 = 0x02;
//        if (byteMessageData.length > 9 || byteMessageData.length == 0) {
//            return;
//        }
        message_value[0] = (byte) (msgPrefix & 0xFF);
        message_value[1] = (byte) (msgPrefix1 & 0xFF);
        message_value[2] = byteUserId;
        message_value[3] = byteMessageId;
        sendMessageOverBle(message_value, "Type_SendMessage");
    }

    public void sendMessageOverBle(byte[] mByteArrayFullPackets, String serviceType) {
        if (isDevicesConnected && mBluetoothLeService != null) {
            System.out.println("--SEND-PACKET--" + BLEUtility.toHexString(mByteArrayFullPackets, true));
            mBluetoothLeService.sendDataToDevice(mByteArrayFullPackets, serviceType);
        } else {
            showDisconnectedDeviceAlert(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        importDB();
        if (mAutoStopDeviceScanTimer != null)
            mAutoStopDeviceScanTimer.cancel();
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            if (isReadyToUnbind) {
                if (mServiceConnection != null) {
                    unbindService(mServiceConnection);
                }
                if (mGattUpdateReceiver != null) {
                    unregisterReceiver(mGattUpdateReceiver);
                }
                if (mbluetoothStateReceiver != null) {
                    unregisterReceiver(mbluetoothStateReceiver);
                }
            }
        }

    }

    public void importDB() {

        String dir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File sd = new File(dir);
        File data = Environment.getDataDirectory();
        FileChannel source = null;
        FileChannel destination = null;
        String backupDBPath = "/data/com.succorfish.eliteoperator/databases/EliteOperators.sqlite";
        String currentDBPath = "EliteOperatorsBackup.sqlite";
        File currentDB = new File(data, backupDBPath);
        File backupDB = new File(sd, currentDBPath);
        System.out.println(currentDB.getAbsoluteFile());
        System.out.println(backupDB.getAbsoluteFile());
        try {
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
//            Toast.makeText(this, "Please wait", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private final BroadcastReceiver mbluetoothStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        System.out.println("Bluetooth OFF");
                        mBluetoothLeService.disconnect();
                        mBluetoothLeService.close();
                        mBluetoothLeService.stopSelf();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        System.out.println("Turning Bluetooth off...");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        System.out.println("Bluetooth on");
                        Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                        mDeviceListArrayMain = new ArrayList<>();
                        startDeviceScan();
                        if (mAutoStopDeviceScanTimer != null)
                            mAutoStopDeviceScanTimer.cancel();
                        if (mAutoStopDeviceScanTimer != null)
                            mAutoStopDeviceScanTimer.start();
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        System.out.println("Turning Bluetooth on...");
                        break;
                }
            }
        }
    };
    public final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                CustomDialog.Snakbar(MainActivity.this, "Bluetooth Service not initialize. Please open app again.");
            } else {
                mDeviceListArrayMain = new ArrayList<>();
                RescanDevice();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            // mBluetoothLeService = null;
            System.out.println(TAG + "-------Services Disconnected...");
        }
    };


    @TargetApi(Build.VERSION_CODES.M)
    private void callMarshMallowParmession() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write Storage");
        if (permissionsList.size() > 0) {
//            if (permissionsNeeded.size() > 0) {
            // Need Rationale
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

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
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

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied
                    mDeviceListArrayMain = new ArrayList<>();
                    if (mAutoStopDeviceScanTimer != null)
                        mAutoStopDeviceScanTimer.cancel();
                    if (mAutoStopDeviceScanTimer != null)
                        mAutoStopDeviceScanTimer.start();
                    startDeviceScan();
                } else if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        System.out.println("fragmentCount-" + count);
        if (mDrawerLayout != null) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(Gravity.LEFT);
            }
        }
        if (count > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_relativelayout_fragment_container);
            if (mFragment instanceof FragmentDashboard) {
                if (exit) {
                    finish();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.str_press_back_again_to_exit),
                            Toast.LENGTH_LONG).show();
                    exit = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, 3000);
                }
            } else {
                for (int i = 0; i < mDrawerItemArrayList.size(); i++) {
                    mDrawerItemArrayList.get(i).setSelected(false);
                }
                mDrawerItemArrayList.get(0).setSelected(true);
                mDrawerItemCustomAdapter.notifyDataSetChanged();

                FragmentDashboard mFragmentDashboard = new FragmentDashboard();
                replacesFragment(mFragmentDashboard, false, null, 0);
            }
        }
    }

    public void replacesFragment(Fragment mFragment, boolean isBackState, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState)
            fragmentTransaction.addToBackStack(null);
        if (mBundle != null)
            mFragment.setArguments(mBundle);

        fragmentTransaction.replace(R.id.activity_main_relativelayout_fragment_container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void addFragment(Fragment mFragment, boolean isBackState, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState)
            fragmentTransaction.addToBackStack(null);
        if (mBundle != null)
            mFragment.setArguments(mBundle);
        fragmentTransaction.add(R.id.activity_main_relativelayout_fragment_container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    public void removeAllFragmentFromBack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void removeNumberOfFragmnet(int num) {
        for (int i = 0; i < num; ++i) {
            getSupportFragmentManager().popBackStack();
        }
    }

    private void setNavigationDrawer() {
        setData();
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.str_drawer_open, R.string.str_drawer_close);
        mActionBarDrawerToggle.setDrawerIndicatorEnabled(false);

        mDrawerItemCustomAdapter = new DrawerItemCustomAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewDrawerList.setLayoutManager(mLayoutManager);
        mRecyclerViewDrawerList.setAdapter(mDrawerItemCustomAdapter);

        FragmentDashboard mFragmentDashboard = new FragmentDashboard();
        replacesFragment(mFragmentDashboard, false, null, 0);
        mImageViewDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                } else {
                    mDrawerLayout.openDrawer(Gravity.LEFT);
                }

            }
        });
        mDrawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
            }

            @Override
            public void onDrawerClosed(View drawerView) {

            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    public void setData() {

        mDrawerItemArrayList = new ArrayList<>();

        VoDrawerListItem mDrawerItem = new VoDrawerListItem();
        mDrawerItem.setmStringName("Dashboard");
        mDrawerItem.setSelectedImage(R.drawable.ic_drawer_dashboard_icon);
        mDrawerItem.setUnSelectedImage(R.drawable.ic_drawer_dashboard_icon);
        mDrawerItem.setSelected(true);
        mDrawerItemArrayList.add(mDrawerItem);

        mDrawerItem = new VoDrawerListItem();
        mDrawerItem.setmStringName("Message");
        mDrawerItem.setSelectedImage(R.drawable.ic_drawer_message_icon);
        mDrawerItem.setUnSelectedImage(R.drawable.ic_drawer_message_icon);
        mDrawerItem.setSelected(false);
        mDrawerItemArrayList.add(mDrawerItem);

        mDrawerItem = new VoDrawerListItem();
        mDrawerItem.setmStringName("DiverIoc8");
        mDrawerItem.setSelectedImage(R.drawable.ic_drawer_driverioc_icon);
        mDrawerItem.setUnSelectedImage(R.drawable.ic_drawer_driverioc_icon);
        mDrawerItem.setSelected(false);
        mDrawerItemArrayList.add(mDrawerItem);

        mDrawerItem = new VoDrawerListItem();
        mDrawerItem.setmStringName("Setting");
        mDrawerItem.setSelectedImage(R.drawable.ic_drawer_settings_icon);
        mDrawerItem.setUnSelectedImage(R.drawable.ic_drawer_settings_icon);
        mDrawerItem.setSelected(false);
        mDrawerItemArrayList.add(mDrawerItem);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        System.out.println(TAG + "-----requestCode " + requestCode);
        if (requestCode == BLUETOOTH_ENABLE_REQT) {
            mDeviceListArrayMain = new ArrayList<>();
            startDeviceScan();
            if (mAutoStopDeviceScanTimer != null)
                mAutoStopDeviceScanTimer.cancel();
            if (mAutoStopDeviceScanTimer != null)
                mAutoStopDeviceScanTimer.start();
        } else {
            System.out.println("--ActivityResult RESULTTT--");
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_relativelayout_fragment_container);
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void setScrollingBehavior(boolean isMapIndex) {
        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
        CoordinatorLayout.LayoutParams appBarLayoutParams = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
        if (isMapIndex) {
            params.setScrollFlags(0);
//            appBarLayoutParams.setBehavior(null);
            appBarLayout.setLayoutParams(appBarLayoutParams);
        } else {
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
            appBarLayout.setLayoutParams(appBarLayoutParams);
        }
    }


    public class DrawerItemCustomAdapter extends RecyclerView.Adapter<DrawerItemCustomAdapter.ViewHolder> {

        @Override
        public DrawerItemCustomAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_drawer_list_item, parent, false);
            return new DrawerItemCustomAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DrawerItemCustomAdapter.ViewHolder mViewHolder, final int position) {

            if (mDrawerItemArrayList.get(position).isSelected()) {
                mViewHolder.mImageViewIcon.setImageResource(mDrawerItemArrayList.get(position).getSelectedImage());
                mViewHolder.mRelativeLayout.setBackgroundResource(R.drawable.menu_selector);
            } else {
                mViewHolder.mImageViewIcon.setImageResource(mDrawerItemArrayList.get(position).getUnSelectedImage());
                mViewHolder.mRelativeLayout.setBackgroundColor(getResources().getColor(R.color.colorAppTheam));
            }
            mViewHolder.mTextViewName.setText(mDrawerItemArrayList.get(position).getmStringName());
            mViewHolder.mImageViewIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorWhite));
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.closeDrawers();
                    for (int i = 0; i < mDrawerItemArrayList.size(); i++) {
                        mDrawerItemArrayList.get(i).setSelected(false);
                    }
                    mDrawerItemArrayList.get(position).setSelected(true);
                    mDrawerItemCustomAdapter.notifyDataSetChanged();
                    if (position == 0) {
                        mTextViewTitle.setText("Dashboard");
                        removeAllFragmentFromBack();
                        FragmentDashboard mFragmentDashboard = new FragmentDashboard();
                        replacesFragment(mFragmentDashboard, false, null, 0);
                    } else if (position == 1) {
                        mTextViewTitle.setText(getResources().getString(R.string.frg_message_txt_composed_msg));
                        removeAllFragmentFromBack();
                        FragmentMessage mFragmentMessage = new FragmentMessage();
                        replacesFragment(mFragmentMessage, false, null, 0);
                    } else if (position == 2) {
                        mTextViewTitle.setText(getResources().getString(R.string.frg_driver_ioc_txt_title));
                        removeAllFragmentFromBack();
                        FragmentDriverIoc mFragmentDriverIoc = new FragmentDriverIoc();
                        replacesFragment(mFragmentDriverIoc, false, null, 0);
                    } else if (position == 3) {
                        mTextViewTitle.setText(getResources().getString(R.string.frg_setting_txt_title));
                        removeAllFragmentFromBack();
                        FragmentSetting mFragmentSetting = new FragmentSetting();
                        replacesFragment(mFragmentSetting, false, null, 0);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDrawerItemArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_drawer_list_item_textview_menu_name)
            TextView mTextViewName;
            @BindView(R.id.raw_drawer_list_item_imageview_menu_icon)
            ImageView mImageViewIcon;
            @BindView(R.id.raw_drawer_list_relative_main)
            RelativeLayout mRelativeLayout;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
