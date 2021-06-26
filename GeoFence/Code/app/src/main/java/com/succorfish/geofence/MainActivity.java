package com.succorfish.geofence;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.Manifest;
import android.app.KeyguardManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.succorfish.geofence.Fragment.FragmentBandConfiguration;
import com.succorfish.geofence.Fragment.FragmentChatting;
import com.succorfish.geofence.Fragment.FragmentDeviceConfiguration;
import com.succorfish.geofence.Fragment.FragmentHistory;
import com.succorfish.geofence.Fragment.FragmentIndustrySpecificConfig;
import com.succorfish.geofence.Fragment.FragmentLiveTracking;
import com.succorfish.geofence.Fragment.FragmentMap;
import com.succorfish.geofence.Fragment.FragmentRemoteTrackingList;
import com.succorfish.geofence.Fragment.FragmentRemoteTrackingMap;
import com.succorfish.geofence.Fragment.FragmentScan;
import com.succorfish.geofence.Fragment.FragmentServerConfiguration;
import com.succorfish.geofence.Fragment.FragmentSetting;
import com.succorfish.geofence.Fragment.FragmentSimConfiguration;
import com.succorfish.geofence.Fragment.FragmentUARTConfiguration;
import com.succorfish.geofence.Fragment.FragmentWifiConfiguration;
import com.succorfish.geofence.MyServices.BluetoothLeService;
import com.succorfish.geofence.MyServices.DfuService;
import com.succorfish.geofence.RoomDataBaseEntity.ChatInfo;
import com.succorfish.geofence.RoomDataBaseEntity.DeviceTable;
import com.succorfish.geofence.RoomDataBaseEntity.Geofence;
import com.succorfish.geofence.RoomDataBaseEntity.GeofenceAlert;
import com.succorfish.geofence.RoomDataBaseEntity.PolygonEnt;
import com.succorfish.geofence.RoomDataBaseEntity.Rules;
import com.succorfish.geofence.RoomDataBaseHelper.RoomDBHelper;
import com.succorfish.geofence.customA2_object.GeoFenceObjectData;
import com.succorfish.geofence.customA2_object.LatLong;
import com.succorfish.geofence.customA2_object.RuleId_Value_ActionBitMask;
import com.succorfish.geofence.customObjects.ChattingObject;
import com.succorfish.geofence.customObjects.CustBluetootDevices;
import com.succorfish.geofence.customObjects.DeviceTableDetails;
import com.succorfish.geofence.customObjects.HistroyList;
import com.succorfish.geofence.customObjects.IncommingMessagePacket;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.helper.PreferenceHelper;
import com.succorfish.geofence.interfaceActivityToFragment.ChatDeliveryACK;
import com.succorfish.geofence.interfaceActivityToFragment.ConnectionStatus;
import com.succorfish.geofence.interfaceActivityToFragment.DFUFileSelectedValid_Invalid;
import com.succorfish.geofence.interfaceActivityToFragment.GeoFenceDialogAlertShow;
import com.succorfish.geofence.interfaceActivityToFragment.LiveRequestDataPassToFragment;
import com.succorfish.geofence.interfaceActivityToFragment.OpenDialogToCheckDeviceName;
import com.succorfish.geofence.interfaceActivityToFragment.PassChatObjectToFragment;
import com.succorfish.geofence.interfaceActivityToFragment.PassConnectionStatusToFragment;
import com.succorfish.geofence.interfaceActivityToFragment.PassScanDeviceToActivity_interface;
import com.succorfish.geofence.interfaceFragmentToActivity.DeviceConfigurationPackets;
import com.succorfish.geofence.interfaceFragmentToActivity.DeviceConnectDisconnect;
import com.succorfish.geofence.interfaceFragmentToActivity.IndustrySpeificConfigurationPackets;
import com.succorfish.geofence.interfaceFragmentToActivity.MessageChatPacket;
import com.succorfish.geofence.interfaceFragmentToActivity.PassBuzzerVolumeToDevice;
import com.succorfish.geofence.interfaceFragmentToActivity.ResetDeviceInterface;
import com.succorfish.geofence.interfaceFragmentToActivity.ServerConfigurationDataPass;
import com.succorfish.geofence.interfaceFragmentToActivity.SimConfigurationPackets;
import com.succorfish.geofence.interfaceFragmentToActivity.WifiConfigurationPackets;
import com.succorfish.geofence.interfaces.API;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;
import com.succorfish.geofence.utility.URL_helper;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import static com.succorfish.geofence.RoomDataBaseHelper.RoomDBHelper.getRoomDBInstance;
import static com.succorfish.geofence.blecalculation.Blecalculation.WriteValue01;
import static com.succorfish.geofence.blecalculation.Blecalculation.askForGeoFenceId_timeStamp;
import static com.succorfish.geofence.blecalculation.Blecalculation.calculateAlgorithmValue;
import static com.succorfish.geofence.blecalculation.Blecalculation.checkType;
import static com.succorfish.geofence.blecalculation.Blecalculation.convert4bytes;
import static com.succorfish.geofence.blecalculation.Blecalculation.convertHexToLong;
import static com.succorfish.geofence.blecalculation.Blecalculation.geoFenceId;
import static com.succorfish.geofence.blecalculation.Blecalculation.getConnectionMainCode;
import static com.succorfish.geofence.blecalculation.Blecalculation.getFloatingPointValueFromHex;
import static com.succorfish.geofence.blecalculation.Blecalculation.hexToint;
import static com.succorfish.geofence.blecalculation.Blecalculation.sendAckReadyForNextPacket;
import static com.succorfish.geofence.blecalculation.Blecalculation.send_Geo_fenceID_fetched_finished_Acknoledgement;
import static com.succorfish.geofence.blecalculation.Blecalculation.set_firmwareTimeStamp;
import static com.succorfish.geofence.blecalculation.ByteConversion.byteConverstionHelper_hexStringToByteArray;
import static com.succorfish.geofence.blecalculation.ByteConversion.bytesToHex;
import static com.succorfish.geofence.blecalculation.ByteConversion.convert7bytesToLong;
import static com.succorfish.geofence.blecalculation.ByteConversion.convertHexStringToString;
import static com.succorfish.geofence.blecalculation.DeviceTokenPacket.deviceTokenpacketArray;
import static com.succorfish.geofence.blecalculation.IMEIpacket.askIMEI_number;
import static com.succorfish.geofence.blecalculation.LiveLocationPacketManufacturer.Start_Stop_LIVE_LOCATION;
import static com.succorfish.geofence.blecalculation.MessageCalculation.incommingMessageACK;
import static com.succorfish.geofence.encryption.Encryption.decryptData;
import static com.succorfish.geofence.encryption.Encryption.encryptData;
import static com.succorfish.geofence.utility.RetrofitHelperClass.getClientWithAutho;
import static com.succorfish.geofence.utility.RetrofitHelperClass.haveInternet;
import static com.succorfish.geofence.utility.Utility.ble_on_off;
import static com.succorfish.geofence.utility.Utility.getCurrenTimeStamp;
import static com.succorfish.geofence.utility.Utility.getDateTime;
import static com.succorfish.geofence.utility.Utility.getDateWithtime;
import static com.succorfish.geofence.utility.Utility.getHexArrayList;
import static com.succorfish.geofence.utility.Utility.getID_From_ArrayList;
import static com.succorfish.geofence.utility.Utility.getTimeStampMilliSecondd;
import static com.succorfish.geofence.utility.Utility.get_TimeStamp_ArrayList;
import static com.succorfish.geofence.utility.Utility.removePreviousZero;
import static com.succorfish.geofence.utility.Utility.splitString;
public class MainActivity extends AppCompatActivity implements
        PassBuzzerVolumeToDevice,
        MessageChatPacket,
        DeviceConfigurationPackets,
        ServerConfigurationDataPass,
        IndustrySpeificConfigurationPackets,
        WifiConfigurationPackets,
        SimConfigurationPackets,
        ResetDeviceInterface,
        DeviceConnectDisconnect{
    private final static String TAG = MainActivity.class.getSimpleName();
    public static final int START_ACTIVITY_REQUEST_CODE=101;
    private Unbinder unbinder;
    PassScanDeviceToActivity_interface  passScanDeviceToActivity_interface;
    /**
     * interface from Activity to Fragment
     */
    GeoFenceDialogAlertShow geoFenceDialogAlertShow;
    ConnectionStatus connectionStatus;
    OpenDialogToCheckDeviceName openDialogToCheckDeviceName;
    ChatDeliveryACK chatDeliveryACK;
    LiveRequestDataPassToFragment liveRequestDataPassToFragment;
    PassChatObjectToFragment passChatObjectToFragment;
    PassConnectionStatusToFragment passConnectionStatusToFragment;
    DFUFileSelectedValid_Invalid dfuFileSelectedValid_invalid;

    /**
     * interface from Activity to Fragment
     */
    @BindView(R.id.bottom_navigation_layout)
    public RelativeLayout bottomRelativelayout;
    @BindView(R.id.home_bottom_navigation)
    BottomNavigationView bottomNavigationView;
    FragmentTransaction fragmentTransction;
    /**
     * Exit window.
     */
    private boolean exit = false;
    private Handler scanHandler;
    // Stops scanning after 10 seconds.
    ArrayList<String> from_firmware_ID_TimeStamp;
    ArrayList<String> from_DataBase_ID_TimeStamp;
    ArrayList<String> from_firmware_ID_TimeStamp_A6_Packet = new ArrayList<String>();
    ArrayList<String> from_firmware_ID_TimeStamp_A8_Packet = new ArrayList<String>();
    public PreferenceHelper preferenceHelper;
    private boolean application_Visible_ToUser = false;
    private KProgressHUD hud;
    DialogProvider dialogProvider;
    String deviceToken_fromFirmware = "";
    private static String imeiNumberFomFirmware="";
    public static RoomDBHelper roomDBHelperInstance;
   private static ArrayList<String> UNIVERSAL_ARRAY_PACEKT_LIST=new ArrayList<String>();
    /**
     * Used to Send the Connected BLE Address to different fragment.
     */
    private static String incommingMessageForConcatenation="";
    public static String CONNECTED_BLE_ADDRESS = "";
    /**
     * Retrofit Implementation
     */
    public Retrofit mRetrofit_instance;
    public API mApiService;
    /**
     *  Incomming message packet.
     */
  private   IncommingMessagePacket incommingMessagePacket;

    /**
     *
     *Enabilng Auto ReConnect Mode.
     */
    List<DeviceTable> deviceTableList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        unbinder = ButterKnife.bind(MainActivity.this);
        UNIVERSAL_ARRAY_PACEKT_LIST=new ArrayList<String>();
        intializeView();
        bindBleServiceToMainActivity();
        intializeRoomDataBaseInstance();
        removeallFragments();
        createNotificationChannel_codeTutor();
        interfaceImpleMainActivity();
        intializeScanHandler();
        intializePreferenceInstance();
        intializeRetrofitInstance();
        intializeDialog();
        onClickBottomNavigationView();
       replaceFragment(new FragmentScan(), null, null, false);
      //  replaceFragment(new FragmentSetting(), null, null, false);
        getAllRegisteredDevices();
    }

    private void intializeRoomDataBaseInstance() {
        roomDBHelperInstance = getRoomDBInstance(this);
    }
    @Override
    protected void onStart() {
        super.onStart();
    }
    @Override
    protected void onResume() {
        super.onResume();
        application_Visible_ToUser = false;
        registerReceiver(bluetootServiceRecieverData, makeGattUpdateIntentFilter());
        DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);
    }
    @Override
    protected void onPause() {
        super.onPause();
        application_Visible_ToUser = true;
        unregisterReceiver(bluetootServiceRecieverData);
        DfuServiceListenerHelper.unregisterProgressListener(this, dfuProgressListener);
    }
    @Override
    protected void onStop() {
        super.onStop();
        application_Visible_ToUser = true;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
        unbindService(serviceConnection);
        mBluetoothLeService = null;
    }

    private void onClickBottomNavigationView(){
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainActivity_container);
                switch (menuItem.getItemId()) {
                    case R.id.home:
                        if (fragment.toString().equalsIgnoreCase(new FragmentRemoteTrackingList().toString())) {
                            replaceFragment(new FragmentScan(),null,false);
                        }
                        break;
                    case R.id.remote_tracking:
                         fragment = getSupportFragmentManager().findFragmentById(R.id.mainActivity_container);
                        if (fragment.toString().equalsIgnoreCase(new FragmentScan().toString())) {
                            replaceFragment(new FragmentRemoteTrackingList(),null,false);
                        }

                        break;
                }
                return true;
            }
        });
    }
    private Uri fileuri;
    private String filepath;
    private String fileExtensionType;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        filepath="";
        fileuri=null;
        fileExtensionType="";
        if(requestCode==START_ACTIVITY_REQUEST_CODE&&resultCode==RESULT_OK){
            fileuri=data.getData();
            filepath=fileuri.getPath();
            if(true){       //     if(filepath.contains(".")){
              //  fileExtensionType=filepath.substring(filepath.lastIndexOf("."));
                if(true){   //        if((filepath!=null)&&(filepath.length()>0)&&(fileuri!=null)&&(filepath!=null)&&(fileExtensionType.equalsIgnoreCase(".zip"))){
                    /**
                     * Selected proper zip file formatt..
                     */
                    if(dfuFileSelectedValid_invalid!=null){
                        dfuFileSelectedValid_invalid.SelecetedFileForDFU(true,fileuri,filepath,fileExtensionType);
                    }
                }
                else{
                    dialogProvider.errorDialog("File Formatt Not Supported");
                }
            }else {
                dialogProvider.errorDialog("File Formatt Not Supported");
            }

        }
    }


    public void startDFUUpdate(Uri fileStreamURI,String fileStreamPath,String bleAddress){
        String deviceNameFOR_DFU="";
          BluetoothDevice bluetoothDevice=   mBluetoothLeService.getBluetoothDevice_From_BleAddress(bleAddress);
          if(bluetoothDevice!=null){
              deviceNameFOR_DFU=bluetoothDevice.getName();
          }
        final DfuServiceInitiator starter = new DfuServiceInitiator(bleAddress)
                .setForeground(false)
                .setDeviceName(deviceNameFOR_DFU);
        starter.setPrepareDataObjectDelay(300L);

        if (0 == DfuService.TYPE_AUTO)
            starter.setZip(fileStreamURI, fileStreamPath);
        else {
            starter.setBinOrHex(0, fileStreamURI, fileStreamPath).setInitFile(fileStreamURI, fileStreamPath);
        }
        starter.start(this, DfuService.class);
    }
    private final DfuProgressListener dfuProgressListener=new DfuProgressListener() {
        @Override
        public void onDeviceConnecting(@NonNull String s) {
        }

        @Override
        public void onDeviceConnected(@NonNull String s) {

        }

        @Override
        public void onDfuProcessStarting(@NonNull String s) {
            showDFUProgressDialog_change_label("OTA Update","Processing OTA update");

        }

        @Override
        public void onDfuProcessStarted(@NonNull String s) {
            showDFUProgressDialog_change_label(null,"OTA Update Started");
        }

        @Override
        public void onEnablingDfuMode(@NonNull String s) {
            showDFUProgressDialog_change_label(null,"Enabling DFU");
        }

        @Override
        public void onProgressChanged(@NonNull String s, int i, float v, float v1, int i1, int i2) {
            showDFUProgressDialog_change_label(null,"Progressing DFU");
        }

        @Override
        public void onFirmwareValidating(@NonNull String s) {
            showDFUProgressDialog_change_label(null,"Validating DFU");
        }

        @Override
        public void onDeviceDisconnecting(String s) {
            showDFUProgressDialog_change_label(null,"Device DisConnecting");
        }

        @Override
        public void onDeviceDisconnected(@NonNull String s) {
            showDFUProgressDialog_change_label(null,"Device Disconnected");
        }

        @Override
        public void onDfuCompleted(@NonNull String s) {
            showDFUProgressDialog_change_label(null,"DFU Completed.");
            cancelProgressDialog();
            dialogProvider.errorDialog("DFU COMPLETED");
        }

        @Override
        public void onDfuAborted(@NonNull String s) {
            cancelProgressDialog();
            dialogProvider.errorDialog("DFU Aborted");
        }

        @Override
        public void onError(@NonNull String s, int i, int i1, String s1) {
            cancelProgressDialog();
            dialogProvider.errorDialog("DFU ERROR");
        }
    };

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainActivity_container);
        if (fragment.toString().equalsIgnoreCase(new FragmentScan().toString())) {
            /**
             * make logic here for the Exit window.
             */
            if (exit) {
                showExitDialog(getString(R.string.str_exit_confirmation));
            } else {
                exit = true;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        exit = false;
                    }
                }, 2000);
            }
        } else if (fragment.toString().equalsIgnoreCase(new FragmentChatting().toString())) {
            replaceFragment(new FragmentScan(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentSetting().toString())) {
            replaceFragment(new FragmentScan(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentHistory().toString())) {
            replaceFragment(new FragmentScan(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentMap().toString())) {
            replaceFragment(new FragmentHistory(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentDeviceConfiguration().toString())) {
            replaceFragment(new FragmentSetting(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentSimConfiguration().toString())) {
            replaceFragment(new FragmentSetting(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentServerConfiguration().toString())) {
            replaceFragment(new FragmentSetting(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentIndustrySpecificConfig().toString())) {
            replaceFragment(new FragmentSetting(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentWifiConfiguration().toString())) {
            replaceFragment(new FragmentSetting(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentUARTConfiguration().toString())) {
            replaceFragment(new FragmentSimConfiguration(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentBandConfiguration().toString())) {
            replaceFragment(new FragmentSimConfiguration(), null, null, false);
        } else if (fragment.toString().equalsIgnoreCase(new FragmentLiveTracking().toString())) {
            replaceFragment(new FragmentScan(), null, null, false);
        }else if(fragment.toString().equalsIgnoreCase(new FragmentRemoteTrackingList().toString())){
            bottomNavigationView.setSelectedItemId(R.id.home);
            replaceFragment(new FragmentScan(),null,false);
        }else if(fragment.toString().equalsIgnoreCase(new FragmentRemoteTrackingMap().toString())){
            bottomNavigationView.setSelectedItemId(R.id.remote_tracking);
            replaceFragment(new FragmentRemoteTrackingList(),null,false);
        }
    }

    public void hideBottomLayout(boolean result){
        if(result){
            bottomRelativelayout.setVisibility(View.VISIBLE);
        }else {
            bottomRelativelayout.setVisibility(View.GONE);
        }
    }

    private void showExitDialog(String dialogMessage) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Exit");
            builder.setCancelable(false);
            builder.setMessage(dialogMessage);
            builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(ble_on_off()){
                   Map<String , BluetoothGatt> instanceOFBluetoothGhatt= mBluetoothLeService.getConnectedBluetoothGhatt();
                        for (Map.Entry<String,BluetoothGatt> entry:instanceOFBluetoothGhatt.entrySet() ) {
                                if(entry.getValue()!=null){
                                    BluetoothGatt ghatt=entry.getValue();
                                    ghatt.disconnect();
                                    ghatt.close();
                                    ghatt=null;
                                }
                        }
                    }
                    dialog.dismiss();
                    finish();
                }
            });
            builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();


    }

    private void intializeView() {
        hud = KProgressHUD.create(this);
    }

    private void showProgressDialog(String labelToShow) {
        hud
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel(labelToShow)
                .setCancellable(false);
        hud.show();
    }

    private void cancelProgressDialog() {
        if (hud != null) {
            if (hud.isShowing()) {
                hud.dismiss();
            }
        }
    }

    private void intializeDialog() {
        dialogProvider = new DialogProvider(this);
    }


    private void intializeScanHandler() {
        scanHandler = new Handler();
    }

    private void intializeRetrofitInstance() {
        String userName = "";
        String password = "";
        if (preferenceHelper.get_Remember_me_Checked()) {
            userName = preferenceHelper.get_PREF_remember_me_userName();
            password = preferenceHelper.get_PREF_remember_password();
        } else if (!preferenceHelper.get_Remember_me_Checked()) {
            userName = preferenceHelper.get_userName();
            password = preferenceHelper.get_password();
        }

        if (!userName.equalsIgnoreCase("") && !password.equalsIgnoreCase("")) {
            mRetrofit_instance = new Retrofit.Builder()
                    .baseUrl(URL_helper.SERVER_URL_SUCCORFISH)
                    .client(getClientWithAutho(userName, password))
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            intialize_API_instance();
        }
    }

    private void intialize_API_instance() {
        mApiService = mRetrofit_instance.create(API.class);
    }

    private void getDeviceTokenAPI(String imeiNumber, String bleAddress) {
        if (haveInternet(this)) {
            Call<String> deviceToken = mApiService.getDeviceToken(imeiNumber);
            deviceToken.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    deviceToken_fromFirmware = "";
                    if (response.code() == 200 || response.isSuccessful()) {
                        deviceToken_fromFirmware = response.body().toString();
                        deviceToken_fromFirmware = deviceToken_fromFirmware.substring(1, deviceToken_fromFirmware.length() - 1);
                        processDeviceTokenAndSend(deviceToken_fromFirmware, bleAddress);
                    } else if (response.code() == 400) {
                        deviceToken_fromFirmware = "";
                        dialogProvider.errorDialog("Server Error");
                        mBluetoothLeService.disconnect(bleAddress);
                    } else {
                        dialogProvider.errorDialog("Server Error");
                        mBluetoothLeService.disconnect(bleAddress);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    deviceToken_fromFirmware = "";
                    dialogProvider.errorDialog("Server Error");
                    mBluetoothLeService.disconnect(bleAddress);
                }
            });
        } else {
            dialogProvider.errorDialog("No Internet\nCannot fetch Device Token");
        }
    }

    ArrayList<byte[]> deviceToken__byteArray;
    private void processDeviceTokenAndSend(String deviceToken_fromFirmware, String bleAddress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                showProgressDialog("Please wait");
            }
        });
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
               deviceToken__byteArray = new ArrayList<byte[]>();
                List<String> listOfString_startPacket = splitString(deviceToken_fromFirmware, 13);
                int indexPosition = listOfString_startPacket.size();
                for (String individualString : listOfString_startPacket) {
                    deviceToken__byteArray.add(deviceTokenpacketArray(indexPosition, individualString));
                    indexPosition--;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(ble_on_off()){
                            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(deviceToken__byteArray);
                                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                                        sendSinglePacketDataToBle(bleAddress,bytesDataToWrite,"WRITING DEVICE TOKEN");
                            }else{

                            }
                        }

                    }
                });
            }
        });

    }

    private void intializePreferenceInstance() {
        preferenceHelper = new PreferenceHelper(MainActivity.this);
    }

    private void removeallFragments() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void replaceFragment(Fragment fragment, String data_TobePassed, String String_Tag, boolean addtoBackStack) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransction = fragmentManager.beginTransaction();
        Bundle sendBundle = new Bundle();
        fragmentTransction.replace(R.id.mainActivity_container, fragment, fragment.toString());
        if (addtoBackStack) {
            fragmentTransction.addToBackStack(fragment.toString());
        }
        if ((data_TobePassed != null) && (String_Tag.length() > 0) && (!String_Tag.equalsIgnoreCase(""))) {
            sendBundle.putString(String_Tag, data_TobePassed);
            fragment.setArguments(sendBundle);
        }
        fragmentTransction.commit();
    }


    public void replaceFragment(Fragment fragment,Bundle bundle,boolean addtoBackStack){
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentTransction = fragmentManager.beginTransaction();
        if (addtoBackStack) {
            fragmentTransction.addToBackStack(fragment.toString());
        }
        fragment.setArguments(bundle);
        fragmentTransction.replace(R.id.mainActivity_container, fragment, fragment.toString());
        fragmentTransction.commit();
    }

    public void setUpGeoFenceAlertDialogInterface(GeoFenceDialogAlertShow loc_geoFenceAlertDialogInterface) {
        geoFenceDialogAlertShow = loc_geoFenceAlertDialogInterface;
    }

    public void setUpConnectionStatus(ConnectionStatus loc_connectionStatus) {
        connectionStatus = loc_connectionStatus;
    }

    public void setUpOpenDialogToCheckDeviceName(OpenDialogToCheckDeviceName loc_openDialogToCheckDeviceName) {
        openDialogToCheckDeviceName = loc_openDialogToCheckDeviceName;
    }

    public void setUpchatDeliveryACK(ChatDeliveryACK loc_chatDeliveryACK) {
        chatDeliveryACK = loc_chatDeliveryACK;
    }
    public void setUpLiveRequest(LiveRequestDataPassToFragment liveRequestDataPassToFragment_loc){
        liveRequestDataPassToFragment=liveRequestDataPassToFragment_loc;
    }
    public void setUpPassChatObjectToFragment(PassChatObjectToFragment passChatObjectToFragment_loc){
        passChatObjectToFragment=passChatObjectToFragment_loc;
    }
    public void setupPassConnectionStatusToFragment(PassConnectionStatusToFragment locpassConnectionStatusToFragment) {
        this.passConnectionStatusToFragment = locpassConnectionStatusToFragment;
    }

    public void setUpDFUFileSelectedValid_Invalid(DFUFileSelectedValid_Invalid loc_dfuFileSelectedValid_invalid){
        dfuFileSelectedValid_invalid=loc_dfuFileSelectedValid_invalid;
    }


    public void interfaceImpleMainActivity() {
        setUpGeoFenceAlertDialogInterface(new GeoFenceDialogAlertShow() {
            @Override
            public void showDialogInterface(String ruleVioation, String bleAddress, String message_one, String messageTwo, String time_stamp) {

            }
        });
        setUpConnectionStatus(new ConnectionStatus() {
            @Override
            public void connectedDevicePostion(BluetoothDevice bluetoothDevice, boolean status) {

            }
        });

        setUpOpenDialogToCheckDeviceName(new OpenDialogToCheckDeviceName() {
            @Override
            public void showDialogNameNotAvaliable(String bleAddressForDeviceAfterConfermation, String deviToken,String imeiNumberFromFirmware) {

            }
        });
        setUpchatDeliveryACK(new ChatDeliveryACK() {
            @Override
            public void chatDeliveryStatus(String bleAddress, String sequenceNumber, String messageStatus) {

            }
        });
        setUpLiveRequest(new LiveRequestDataPassToFragment() {
            @Override
            public void liveRequestDataFromFirmware(Double latitudeValue, Double longValue, String bleAddress) {

            }
        });

        setUpPassChatObjectToFragment(new PassChatObjectToFragment() {
            @Override
            public void ChatObjetShare(ChattingObject chattingObject) {

            }
        });

        setupPassScanDeviceToActivity_interface(new PassScanDeviceToActivity_interface() {
            @Override
            public void sendCustomBleDevice(CustBluetootDevices custBluetootDevices) {

            }
        });
        setupPassConnectionStatusToFragment(new PassConnectionStatusToFragment() {
            @Override
            public void connectDisconnect(String bleAddress, boolean connected_disconnected) {

            }
        });

        setUpDFUFileSelectedValid_Invalid(new DFUFileSelectedValid_Invalid() {
            @Override
            public void SelecetedFileForDFU(boolean selectedFileType_true_false,Uri selectedFileUri,String filepath,String fileExtensiongType) {

            }
        });

    }

    GeoFenceObjectData geoFenceObjectData;
    private List<LatLong> latLong;
    List<RuleId_Value_ActionBitMask> ruleId_value_actionBitMasks;
    /**
     * Used for Checking the log issues issues with setWriteType.
     */

    private void insertChatInfoToTable(String messageRecieved,String sequenceNumber,String bleAddress,String GSM_IRIDIUM) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                String timeStampDateBase =getTimeStampMilliSecondd();
           //     String timeStampDateBase = DateUtilsMyHelper.getCurrentDate(DateUtilsMyHelper.dateFormatStandard);
                String date_time = getDateWithtime();
                ChatInfo chatInfo = new ChatInfo();
                chatInfo.setFrom_name(getString(R.string.fragment_chat_server_name));
                chatInfo.setTo_name(getString(R.string.fragment_chat_owner_name));
                chatInfo.setMsg_txt(messageRecieved);
                chatInfo.setTime(date_time);
                chatInfo.setStatus(getResources().getString(R.string.fragment_chat_message_mesaage_recieved_from_ble));
                chatInfo.setSequence("" + sequenceNumber);
                chatInfo.setIdentifier(bleAddress);
                chatInfo.setTimeStamp(timeStampDateBase);
                if (GSM_IRIDIUM.equalsIgnoreCase(getResources().getString(R.string.GSM))) {
                    chatInfo.setIsGSM("1");
                } else if (GSM_IRIDIUM.equalsIgnoreCase(getResources().getString(R.string.IRIDIUM))) {
                    chatInfo.setIsGSM("0");
                }
                roomDBHelperInstance.get_Chat_info_dao().insert_ChatInfo(chatInfo);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainActivity_container);
                        if (fragment.toString().equalsIgnoreCase(new FragmentChatting().toString())) {
                            ChattingObject chattingObject = new ChattingObject();
                            chattingObject.setMode(getResources().getString(R.string.fragment_chat_message_mesaage_incomming_message));// Incomming 1 ,// out going 0
                            chattingObject.setMessage(messageRecieved);
                            chattingObject.setDate(date_time);
                            chattingObject.setTimeStamp(timeStampDateBase);
                            chattingObject.setTime_chat(date_time.substring(11,16));
                            chattingObject.setBleAddress(bleAddress);
                            chattingObject.setSequenceNumber(sequenceNumber);
                            if(passChatObjectToFragment!=null){
                                passChatObjectToFragment.ChatObjetShare(chattingObject);
                            }
                        }

                    }
                });
            }
        });

    }


    private void checkTokenAvaliableForImeiNumber(String bleaddress, String imeiNumber) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                boolean isImeiNumberAValiable = roomDBHelperInstance.get_TableDevice_dao().isImeiNumberExists(imeiNumber);
                if (isImeiNumberAValiable) {
                    String deviceToken = roomDBHelperInstance.get_TableDevice_dao().getDeviceToken(imeiNumber);
                    if ((!deviceToken.equalsIgnoreCase("")) && (!deviceToken.equalsIgnoreCase("NA")) && (deviceToken != null) && (deviceToken.length() > 1)) {
                        deviceToken_fromFirmware = deviceToken;
                        processDeviceTokenAndSend(deviceToken_fromFirmware, bleaddress);
                    }
                } else {
                    /**
                     * call the API and Get the device Token and Send it to the Firmware..
                     */
                    getDeviceTokenAPI(imeiNumber, bleaddress);
                }
            }
        });
    }

    private void insertIntoGeoFenceTable(GeoFenceObjectData geoFenceObjectData) {
        if(geoFenceObjectData!=null){
            Geofence geofence=new Geofence();
            geofence.setName("NA");
            geofence.setGeofence_ID(""+geoFenceObjectData.getGeoId());
            geofence.setType(geoFenceObjectData.getGeoFenceType());
            if (geoFenceObjectData.getGeoFenceType().toString().equalsIgnoreCase("Circular")) {
                geofence.setLat(""+geoFenceObjectData.getLatLong().get(0).getLatitude());
                geofence.setLongValue(""+geoFenceObjectData.getLatLong().get(0).getLongitude());
            }else if(geoFenceObjectData.getGeoFenceType().toString().equalsIgnoreCase("Polygon")){
                geofence.setLat("NA");
                geofence.setLongValue("NA");
            }
            geofence.setRadiusOrvertices(""+geoFenceObjectData.getRadius_vertices());
            geofence.setNumber_of_rules(""+geoFenceObjectData.getNumberOfRules());
            geofence.setIs_active("NA");
            geofence.setGsm_reporting(""+geoFenceObjectData.getIridium_reportingTime());
            geofence.setIridium_reporting(""+geoFenceObjectData.getGsm_ReportingTime());
            geofence.setFirmware_timestamp(""+geoFenceObjectData.getFirmwareTimeStamp());
            roomDBHelperInstance.get_GeoFence_info_dao().insert_GeoFence(geofence);
        }else {

        }
    }

    private void loadGeoFenceId_TimeStamp(String  bleAddressResult) {
        if (from_DataBase_ID_TimeStamp != null) {
            from_DataBase_ID_TimeStamp.clear();
        }
        List<Geofence> geofenceList = roomDBHelperInstance.get_GeoFence_info_dao().getAll_GeoFence();
        for (Geofence geofence : geofenceList) {
            from_DataBase_ID_TimeStamp.add(geofence.getGeofence_ID() + ":" + geofence.getFirmware_timestamp());
        }
        deviceToken_fromFirmware = "";
        System.gc();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                sendSinglePacketDataToBle(bleAddressResult,askForGeoFenceId_timeStamp(),"Asking GeoFence ID TimeStamp after connection");
            }
        });

    }

    @Override
    public void timeIntervalGiven(String bleAddress, String timeInterval) {
        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                sendSinglePacketDataToBle(bleAddress,set_firmwareTimeStamp(Integer.parseInt(timeInterval)),"FIRMWARE TIMES TAMP");
            }
        }
       // System.out.println("Time_Interval = " + timeInterval);

    }

    private void playAlertMusic() {
        final MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.fence_alert);
        mediaPlayer.start();
    }

    private void process_TimeStamp_id_One_After_Other_A6_packet(String bleAddress) {
        if (from_firmware_ID_TimeStamp.size() > 0) {
            if (check_timeStamp_id_Avaliable_In_dataBase_Arraylist(from_firmware_ID_TimeStamp.get(0))) {
                removeId_time_Stamp_from_firmware_arraylist( null);
                process_TimeStamp_id_One_After_Other_A6_packet(bleAddress);
            } else {
                /**
                 * Request ID_TimeStamp from firmware.
                 */
                String firmwareDetailsArray = from_firmware_ID_TimeStamp.get(0);
                String geofenceId = getID_From_ArrayList(firmwareDetailsArray);
                String geofenceTimeStamp = get_TimeStamp_ArrayList(firmwareDetailsArray);
                sendSinglePacketDataToBle(bleAddress, geoFenceId(Integer.parseInt(geofenceId)),"Asking For GeoFenceID from Time Stamp=  ID= " + geofenceId);
            }
        } else {
            /**
             * Send Ack that no geoFence id there..so ready to recieve geoFence alert.
             */
            cancelProgressDialog();
            sendSinglePacketDataToBle(bleAddress, send_Geo_fenceID_fetched_finished_Acknoledgement((byte) 0xa4),"ACK to Firmware i.e Ready to Recieve alerts A4FF");

        }
    }

    private void removeId_time_Stamp_from_firmware_arraylist(String index_items) {
        if (index_items == null) {
            from_firmware_ID_TimeStamp.remove(0);
        } else if (index_items.length() > 2) {
            from_firmware_ID_TimeStamp.remove(index_items);
        }
    }

    private boolean check_timeStamp_id_Avaliable_In_dataBase_Arraylist(String id_timeStamp_from_firmware) {
        boolean result = false;
        if (from_DataBase_ID_TimeStamp.contains(id_timeStamp_from_firmware)) {
            result = true;
        } else {
            result = false;
        }
        return result;
    }

    private void process_TimeStamp_id_One_After_Other_A8_packet(String bleAddress) {
        if (from_firmware_ID_TimeStamp_A8_Packet.size() > 0) {
            String firmwareDetailsArray = from_firmware_ID_TimeStamp_A8_Packet.get(0);
            String geofenceId = getID_From_ArrayList(firmwareDetailsArray);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    sendSinglePacketDataToBle(bleAddress,geoFenceId(Integer.parseInt(geofenceId)),"Asking For GeoFenceID from A8 PACKET " + geofenceId);
                }
            });

        }
    }

    private void remove_id_time_stamp_from_A8_packet_SendAck_if_packet_process_Finished(String id_stamp_from_firmware, String bleAddresResult) {
        if (from_firmware_ID_TimeStamp_A8_Packet.size() > 0) {
            if (from_firmware_ID_TimeStamp_A8_Packet.contains(id_stamp_from_firmware)) {
                from_firmware_ID_TimeStamp_A8_Packet.remove(id_stamp_from_firmware);
                if (from_firmware_ID_TimeStamp_A8_Packet.isEmpty()) {
                    sendSinglePacketDataToBle(bleAddresResult,send_Geo_fenceID_fetched_finished_Acknoledgement((byte) 0xa8),"ACK to Firmware i.e Ready to Recieve alerts A8FF");
                } else {
                    process_TimeStamp_id_One_After_Other_A8_packet(bleAddresResult);
                }
            }
        } else {

        }
    }

    private void createNotificationChannel_codeTutor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(getString(R.string.GEO_FENCE_ID), getString(R.string.GEO_FENCE_ALERTS), NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(getString(R.string.GEO_FENCE_DESCRIPTION));
            notificationChannel.setShowBadge(false);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    private boolean checkPhoneScreenLocked() {
        KeyguardManager myKM = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        if (myKM.isKeyguardLocked()) {
            return true;
        } else {
            return false;
        }
    }

    public void createNotification(String bleDeviceAliasName, String headerName, String message_one, String message_two) {
        String endResult = bleDeviceAliasName + "\n" + headerName + "\n" + message_one + "\n" + message_two;
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.GEO_FENCE_ID))
                .setSmallIcon(R.drawable.geo_fence_notification_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.notification_icon))
                .setContentTitle("GeoFence Alert")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setChannelId(getString(R.string.GEO_FENCE_ID))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(endResult));
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify((int) ((new Date().getTime() / 1000L) % Integer.MAX_VALUE), builder.build());
    }

    @Override
    public void messagePacketArray(String bleAddress, ArrayList<byte[]> messageArraylist) {
        /**
         * call back from the interface after typing the message.
         */

        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(messageArraylist);
                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                sendSinglePacketDataToBle(bleAddress,bytesDataToWrite,"MESSAGE_PACKET_ARRAY");
            }else {
                dialogProvider.errorDialog("Device Disconnected");
            }
        }else {
            dialogProvider.errorDialog("BlueTooth is Off");
        }





    }

    private void changeMessageStatusInDb(String bleAddress, String sequenceNumber, String messageStatus) {
        if (chatDeliveryACK != null) {
            chatDeliveryACK.chatDeliveryStatus(bleAddress, sequenceNumber, messageStatus);
        }
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                roomDBHelperInstance.get_Chat_info_dao().updateMessageStatusInDb(bleAddress,sequenceNumber,messageStatus);
            }
        });
    }

    @Override
    public void deviceConfigurationDetails(String bleAddress, ArrayList<byte[]> configurationList) {
        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(configurationList);
                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                sendSinglePacketDataToBle(bleAddress,bytesDataToWrite,"DEVICE CONFIGURATION INTIAL PACKET");
                showProgressDialog("Saving Setting");
            }else {
                dialogProvider.errorDialog("Device Disconnected");
            }
        }else {
            dialogProvider.errorDialog("BlueTooth is Off");
        }
    }
    @Override
    public void ServerConfigurationPacketArray(String bleAddress, ArrayList<byte[]> entitemessageList) {
        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(entitemessageList);
                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                sendSinglePacketDataToBle(bleAddress,bytesDataToWrite,"SERVER DEVICE CONFIGURATION");
                showProgressDialog("Saving Setting");
            }else {
                dialogProvider.errorDialog("Device Disconnected");
            }
        }else {
            dialogProvider.errorDialog("BlueTooth is Off");
        }

    }

    @Override
    public void industrySpcificConfigurationDetails(String bleAddress, ArrayList<byte[]> entitemessageList) {
        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(entitemessageList);
                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                sendSinglePacketDataToBle(bleAddress,bytesDataToWrite,"INDUSTRY SPECIFIC CONFIGURATION");
                showProgressDialog("Saving Setting");
            }else {
                dialogProvider.errorDialog("Device Disconnected");
            }
        }else {
            dialogProvider.errorDialog("BlueTooth is Off");
        }
    }

    @Override
    public void wifiConfigurationDetails(String bleAddress, ArrayList<byte[]> configArrayList) {
        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(configArrayList);
                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                sendSinglePacketDataToBle(bleAddress,bytesDataToWrite,"WIFI CONFIGURATION DETAILS ");
                showProgressDialog("Saving Setting");
            }else {
                dialogProvider.errorDialog("Device Disconnected");
            }
        }else {
            dialogProvider.errorDialog("BlueTooth is Off");
        }
    }


    @Override
    public void SimConfigurationDetails(String bleAddress, ArrayList<byte[]> simconfigurationList) {
        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddress)){
                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(simconfigurationList);
                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                sendSinglePacketDataToBle(bleAddress,bytesDataToWrite,"SIM CONFIGURATION DATA PARSING");
                showProgressDialog("Saving Setting");
            }else {
                dialogProvider.errorDialog("Device Disconnected");
            }
        }
    }

    @Override
    public void resetDevicePacketSend(String bleaddress, ArrayList<byte[]> resetFirmware) {
        if(ble_on_off()){
            if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleaddress)){
                UNIVERSAL_ARRAY_PACEKT_LIST = new ArrayList<String>();
                UNIVERSAL_ARRAY_PACEKT_LIST = getHexArrayList(resetFirmware);
                byte[] bytesDataToWrite = byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0));
                sendSinglePacketDataToBle(bleaddress,bytesDataToWrite,"Reset Device");
                showProgressDialog("Saving Setting");
            }else {
                dialogProvider.errorDialog("Device Disconnected");
            }
        }
    }
    /**
     * Google BLE libraray implementation.
     */
    public static BluetoothLeService mBluetoothLeService;
    private BluetoothLeScanner bluetoothLeScanner = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
    private Handler handler = new Handler();
    private static final long SCAN_PERIOD = 30000;
    public static String SCAN_TAG = "";
    private void bindBleServiceToMainActivity() {
        Intent intent = new Intent(this, BluetoothLeService.class);
        bindService(intent, serviceConnection, BIND_AUTO_CREATE);
    }
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothLeService = null;
        }
    };
    /**
     * BroadCast Reciever Data Trigger.
     */
    private IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(getResources().getString(R.string.BLUETOOTHLE_SERVICE_CONNECTION_STATUS));
        intentFilter.addAction(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_WRITTEN_FOR_CONFERMATION));
        intentFilter.addAction(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_OBTAINED));
        intentFilter.addAction(getResources().getString(R.string.BLUETOOTHLE_SERVICE_NOTIFICATION_ENABLE));
        return intentFilter;
    }
    private final BroadcastReceiver bluetootServiceRecieverData = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if ((action != null) && (action.equalsIgnoreCase(getResources().getString(R.string.BLUETOOTHLE_SERVICE_CONNECTION_STATUS)))) {
                /**
                 * Connection/Disconnection of the Device.
                 */
                String bleAddress = intent.getStringExtra((getResources().getString(R.string.BLUETOOTHLE_SERVICE_CONNECTION_STATUS_BLE_ADDRESS)));
                boolean connectionStatus = intent.getBooleanExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_CONNECTION_STATUS_CONNECTED_DISCONNECTED), false);
                passConnectionSucesstoFragmentScanForUIChange(bleAddress, connectionStatus);
            }
            else if ((action != null) && (action.equalsIgnoreCase(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_WRITTEN_FOR_CONFERMATION)))) {
                /**
                 * Data Written to the firmware getting loop back after write confermation.
                 */
                String bleAddress = intent.getStringExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_WRITTEN_FOR_CONFERMATION_BLE_ADDRESS));
                byte[] dataWritten = intent.getByteArrayExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_WRITTEN_FOR_CONFERMATION_BLE_DATA_WRITTEN));
                int dataWrittenType = intent.getIntExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_WRITTEN_FOR_CONFERMATION_BLE_DATA_WRITTEN_TYPE), -1);
                int status = intent.getIntExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_WRITTEN_FOR_CONFERMATION_STATUS), -1);
                if(bytesToHex(dataWritten).length()==32){
                    try {
                        byte [] decryptedHexValue=   decryptData(dataWritten);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    }
                }else {

                }

                if(UNIVERSAL_ARRAY_PACEKT_LIST.size()>0){
                    sendNextDataToFirmmWareAfterConfermation(dataWritten,bleAddress,null);
                }
              /*
                System.out.println("what data written to the Firmware= "+convertHexToBigIntegert(bytesToHex(dataWritten)));
                System.out.println("what data written to the Firmware bleAddres = "+bleAddress);
                System.out.println("what data written to the Firmware type = "+dataWrittenType);*/

            }else if ((action != null) && (action.equalsIgnoreCase(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_OBTAINED)))) {
                /**
                 * Data Obtained from the firmware.
                 */
                String bleAddressFromNotificationChanged = intent.getStringExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_OBTAINED_BLE_ADDRESS));
                byte[] obtainedFromFirmware = intent.getByteArrayExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_DATA_OBTAINED_DATA_RECIEVED));
             //   System.out.println("DATA_FIRMWARE_OBTAINED= "+""+bytesToHex(obtainedFromFirmware));
                /**
                 * Pasting Old NotifyDataChanrcterstic changed here...
                 */
                String blehexObtainedFrom_Firmware = "";
                blehexObtainedFrom_Firmware = bytesToHex(obtainedFromFirmware).toLowerCase();
                /**
                 * Differentiate here for connection maintainence code and encryption data mainpulation.
                 *
                 */


                if ((blehexObtainedFrom_Firmware.length() == 6) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("0101"))) {

                    int m_auth_key = hexToint(blehexObtainedFrom_Firmware.substring(4, 6));
                    int calculatedMagicNumber = calculateAlgorithmValue(m_auth_key);
                    byte[] connectionArray = getConnectionMainCode(calculatedMagicNumber);
                    Log.d(TAG, "onReceive: Writing Connection Maintainence Code.");
                    writeConnectionMainTaincenceCodeToFirmware(bleAddressFromNotificationChanged,connectionArray);



                    final Handler handler = new Handler(Looper.getMainLooper());
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //Do something after 100ms
                          //  Log.d(TAG, "onReceive: Asking IMEI Number yui");
                            System.gc();
                        //    sendSinglePacketDataToBle(bleAddressFromNotificationChanged,askIMEI_number(),"ASKING_IMEI_NUMBER ");
                        }
                    }, 800);


                }else if((blehexObtainedFrom_Firmware.length()==6)&&(blehexObtainedFrom_Firmware.substring(0,4).equalsIgnoreCase("0201"))){

                    int auth_sucess = hexToint(blehexObtainedFrom_Firmware.substring(4, 6));
                    if(auth_sucess==1){
                        /**
                         * Ask imei number and Procedd further
                         */
                        Log.d(TAG, "onReceive: Asking IMEI Number 456");
                        System.gc();

                       sendSinglePacketDataToBle(bleAddressFromNotificationChanged,askIMEI_number(),"ASKING_IMEI_NUMBER ");
                    }else if (auth_sucess==0){
                        /**
                         * Disconnect the device..
                         */
                        dialogProvider.errorDialog("INVALID AUTHENICATION");
                        mBluetoothLeService.disconnect(bleAddressFromNotificationChanged);
                        Log.d(TAG, "onReceive: Authenication Failure");

                    }else {
                        /**
                         * Disconnect the device..
                         */
                        dialogProvider.errorDialog("Authenication Failure\nTry later");
                        mBluetoothLeService.disconnect(bleAddressFromNotificationChanged);

                    }

                }
                else {
                    /**
                     * Go for the Encryption Code Manipulation here.
                     * 1)Take the encrypted byte array.
                     * 2)Decrypt the byte array.
                     * 3)Convert the Decrypt array to HexString.
                     */
                    try {
                        byte[] decrypted_byteArray_FromFirmware = decryptData(obtainedFromFirmware);
                        String hex_converted_decrypted_byte_array = bytesToHex(decrypted_byteArray_FromFirmware);

                        blehexObtainedFrom_Firmware = hex_converted_decrypted_byte_array.toLowerCase();
                    } catch (IllegalBlockSizeException e) {
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        e.printStackTrace();
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        e.printStackTrace();
                    }
                    /**
                     * GeoFence Data Manipulation Start.
                     */
                    if ((blehexObtainedFrom_Firmware.toString().startsWith("a6"))) {
                        /**
                         * add all the GeoFence Id to the Map.
                         */
                        if ((blehexObtainedFrom_Firmware.toString().startsWith("a6")) && (blehexObtainedFrom_Firmware.length() == 32) && (!blehexObtainedFrom_Firmware.substring(2, 4).equalsIgnoreCase("00"))) {
                            /**
                             *  3 Packets Validation
                             */
                            /**
                             * Key:- ID ,              Value= TimeStamp
                             */
                            String id_timeStamp_1 = hexToint(blehexObtainedFrom_Firmware.substring(4, 8)) + ":" + convertHexToLong(blehexObtainedFrom_Firmware.substring(8, 16));
                            String id_timeStamp_2 = hexToint(blehexObtainedFrom_Firmware.substring(16, 20)) + ":" + convertHexToLong(blehexObtainedFrom_Firmware.substring(20, 28));

                            if (!id_timeStamp_1.equalsIgnoreCase("0:0")) {
                                from_firmware_ID_TimeStamp.add(id_timeStamp_1);
                            }

                            if (!id_timeStamp_2.equalsIgnoreCase("0:0")) {
                                from_firmware_ID_TimeStamp.add(id_timeStamp_2);
                            }


                        }
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.equalsIgnoreCase("A4000000000000000000000000000000"))) {

                        /**
                         * Check if the Firmware dosent contains any ID_TIME stamp send ACK to the firmware..
                         */
                        showProgressDialog("Syncing");
                        process_TimeStamp_id_One_After_Other_A6_packet(bleAddressFromNotificationChanged);

                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.equalsIgnoreCase("a2010000000000000000000000000000"))) {
                        /**
                         * Error Packet of the GeoFence.
                         */
                        cancelProgressDialog();

                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("a201"))) {

                        String locaVariableForBlock = blehexObtainedFrom_Firmware;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {

                                /**
                                 * First Packet Started.
                                 */
                                System.gc();
                                geoFenceObjectData = new GeoFenceObjectData();
                                latLong = geoFenceObjectData.getLatLongInstance();
                                ruleId_value_actionBitMasks = geoFenceObjectData.getInstanceOfruleId_value_actionBitMasks();
                                /**
                                 * 1st packet Parameters
                                 * GeoFence ID
                                 *Geo Fence Size
                                 *Geo Fence Type
                                 *Geo Fence Radius_Vertices.
                                 */
                                geoFenceObjectData.setGeoId(hexToint(locaVariableForBlock.substring(6, 10)));
                                geoFenceObjectData.setGeosize(hexToint(locaVariableForBlock.substring(10, 14)));
                                geoFenceObjectData.setGeoFenceType(checkType(locaVariableForBlock.substring(14, 16)));
                                geoFenceObjectData.setRadius_vertices(Double.parseDouble("" + convert4bytes(locaVariableForBlock.substring(16, 24))));
                                geoFenceObjectData.setFirmwareTimeStamp("" + convertHexToLong(locaVariableForBlock.substring(24, 32)));
                                AsyncTask.execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        /**
                                         * Removed the GeoFence ID details in the Table GeoFence,Rules_info_Table.
                                         */
                                        roomDBHelperInstance.get_GeoFence_info_dao().deleteRecordFromGeoFenceId(""+geoFenceObjectData.getGeoId());
                                        roomDBHelperInstance.get_RulesTable_dao().deleteRecordFromGeoFenceId(""+geoFenceObjectData.getGeoId());

                                    }
                                });
                            }
                        });

                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("a202"))) {

                        String locaVariableForBlock = blehexObtainedFrom_Firmware;

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 * 2 nd Packet Parameter Packet format
                                 * Latitude
                                 * Longitude
                                 */

                                String lat_from_firmware_1 = locaVariableForBlock.substring(6, 14);
                                String long_from_firmware_1 = locaVariableForBlock.substring(14, 22);

                                if (!(lat_from_firmware_1.equalsIgnoreCase("00000000")) && !(long_from_firmware_1.equalsIgnoreCase("00000000"))) {
                                    latLong.add(new LatLong(
                                            Double.parseDouble(getFloatingPointValueFromHex(lat_from_firmware_1)),
                                            Double.parseDouble(getFloatingPointValueFromHex(long_from_firmware_1))
                                    ));
                                }


                            }
                        });
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("a203"))) {

                        String locaVariableForBlock = blehexObtainedFrom_Firmware;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 *  3rd  Packet Parameter
                                 *  Number of Rules
                                 */
                                geoFenceObjectData.setGsm_ReportingTime("" + convert4bytes(locaVariableForBlock.substring(6, 14)));
                                geoFenceObjectData.setIridium_reportingTime("" + convert4bytes(locaVariableForBlock.substring(14, 22)));
                                geoFenceObjectData.setNumberOfRules(hexToint(locaVariableForBlock.substring(22, 24)));


                            }
                        });

                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("a204"))) {

                        String locaVariableForBlock = blehexObtainedFrom_Firmware;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 * 4 th  Packet Parameter
                                 * RuleId
                                 * Value
                                 * ActionBitMask
                                 */
                                ruleId_value_actionBitMasks.add(new RuleId_Value_ActionBitMask(
                                        hexToint(locaVariableForBlock.substring(6, 8)),
                                        "" + convert4bytes(locaVariableForBlock.substring(8, 16)),
                                        "" + convert4bytes(locaVariableForBlock.substring(16, 24))
                                ));
                            }
                        });

                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 8).equalsIgnoreCase("a2050101"))) {

                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (geoFenceObjectData.getGeoFenceType().equalsIgnoreCase("Circular")) {
                                    insertIntoGeoFenceTable(geoFenceObjectData);
                                    for (int i = 0; i < ruleId_value_actionBitMasks.size(); i++) {
                                        roomDBHelperInstance.get_RulesTable_dao().insert_RulesTable(new Rules("NA", "" + geoFenceObjectData.getGeoId(), "" + ruleId_value_actionBitMasks.get(i).getRuleId(), ruleId_value_actionBitMasks.get(i).getValue()));
                                    }
                                } else if (geoFenceObjectData.getGeoFenceType().equalsIgnoreCase("Polygon")) {
                                    insertIntoGeoFenceTable(geoFenceObjectData);
                                    for (int i = 0; i < latLong.size(); i++) {
                                        roomDBHelperInstance.get_Polygon_info_dao().insert_Polygon(new PolygonEnt("" + geoFenceObjectData.getGeoId(), "" + latLong.get(i).getLatitude(), "" + latLong.get(i).getLongitude(), "" + geoFenceObjectData.getFirmwareTimeStamp()));
                                    }
                                    for (int i = 0; i < ruleId_value_actionBitMasks.size(); i++) {
                                        roomDBHelperInstance.get_RulesTable_dao().insert_RulesTable(new Rules("NA", "" + geoFenceObjectData.getGeoId(), "" + ruleId_value_actionBitMasks.get(i).getRuleId(), "" + ruleId_value_actionBitMasks.get(i).getValue()));
                                    }
                                }

                            }
                        });
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                System.out.println("**************1st Packet *********** ");
                                System.out.println("Details_Tag Id = " + geoFenceObjectData.getGeoId());
                                System.out.println("Details_Tag Type = " + geoFenceObjectData.getGeoFenceType());
                                System.out.println("Details_Tag Geo Size = " + geoFenceObjectData.getGeosize());
                                System.out.println("Details_Tag Geo Radius_Vertices = " + geoFenceObjectData.getRadius_vertices());
                                System.out.println("-------------------------------------------------------------- ");
                                System.out.println("**************2 nd Packet*********** ");
                                System.out.println("Details_Tag number of LatLong Obtaine= " + geoFenceObjectData.getLatLong().size());
                                for (int i = 0; i < geoFenceObjectData.getLatLong().size(); i++) {
                                    System.out.println("Details_Tag  latitude= " + geoFenceObjectData.getLatLong().get(i).getLatitude() + " Longitude " + geoFenceObjectData.getLatLong().get(i).getLongitude());
                                }
                                System.out.println("-------------------------------------------------------------- ");
                                System.out.println("Details_Tag Geo Number of Rules = " + geoFenceObjectData.getNumberOfRules());
                                System.out.println("--------------------------------------------------------------");
                                System.out.println("************** 4th Packet*********** ");
                                for (int i = 0; i < ruleId_value_actionBitMasks.size(); i++) {
                                    System.out.println("Details_Tag RuleId_Value_NumberOfAction Rule ID = " + ruleId_value_actionBitMasks.get(i).getRuleId() + " Value = " + ruleId_value_actionBitMasks.get(i).getValue() + " Number of BitMask = " + ruleId_value_actionBitMasks.get(i).getActionBitMask());
                                }
                                System.out.println("--------------------------------------------------------------");
                                /**
                                 * Used to remove the TimeStamp and ID from A6 packets.
                                 */
                                removeId_time_Stamp_from_firmware_arraylist(geoFenceObjectData.getGeoId() + ":" + geoFenceObjectData.getFirmwareTimeStamp());
                                System.out.println("Removed ID and TimeStamp from the arrayList ID= " + geoFenceObjectData.getGeoId() + " TimeStamp= " + geoFenceObjectData.getFirmwareTimeStamp());
                                process_TimeStamp_id_One_After_Other_A6_packet(bleAddressFromNotificationChanged);
                                /**
                                 * Used to remove the TimeStamp and ID from A8 packets.
                                 */
                                remove_id_time_stamp_from_A8_packet_SendAck_if_packet_process_Finished(geoFenceObjectData.getGeoId() + ":" + geoFenceObjectData.getFirmwareTimeStamp(), bleAddressFromNotificationChanged);
                            }
                        });
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 2).equalsIgnoreCase("a6"))) {
                        String id_1 = blehexObtainedFrom_Firmware.substring(4, 8);
                        String timeStamp_1 = blehexObtainedFrom_Firmware.substring(8, 16);
                        String id_2 = blehexObtainedFrom_Firmware.substring(16, 20);
                        String timeStamp_2 = blehexObtainedFrom_Firmware.substring(20, 28);
                        if ((!id_1.equalsIgnoreCase("0000")) && (!timeStamp_1.equalsIgnoreCase("00000000"))) {
                            from_firmware_ID_TimeStamp_A6_Packet.add(hexToint(id_1) + ":" + convertHexToLong(timeStamp_1));
                        }
                        if ((!id_2.equalsIgnoreCase("0000")) && (!timeStamp_2.equalsIgnoreCase("00000000"))) {
                            from_firmware_ID_TimeStamp_A6_Packet.add(hexToint(id_2) + ":" + convertHexToLong(timeStamp_2));
                        }
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 2).equalsIgnoreCase("a5"))) {
                        String locaVariableForBlock = blehexObtainedFrom_Firmware;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                /**
                                 * Hex Values
                                 */
                                String geoFenceId_hex = locaVariableForBlock.substring(2, 6);
                                String breachLatitude_hex = locaVariableForBlock.substring(6, 14);
                                String breachLongitude_hex = locaVariableForBlock.substring(14, 22);
                                String rule_Id_hex = locaVariableForBlock.substring(22, 24);
                                String rule_Value_hex = locaVariableForBlock.substring(24, 32);
                                /**
                                 * Hex Converted Values
                                 */
                                String geoFence_id = removePreviousZero("" + hexToint(geoFenceId_hex));
                                String breach_latitude = getFloatingPointValueFromHex(breachLatitude_hex);
                                String breach_longitude = getFloatingPointValueFromHex(breachLongitude_hex);
                                String rule_id = "" + hexToint(rule_Id_hex);
                                String rule_value = removePreviousZero("" + hexToint(rule_Value_hex));
                                String bleAddressWithColon = bleAddressFromNotificationChanged;
                                String bleAddress = bleAddressFromNotificationChanged.replace(":", "").toLowerCase();
                                boolean isGeoFenceId_Avaliable = roomDBHelperInstance.get_GeoFence_info_dao().isGeoFenceId_Avaliable(geoFence_id);
                                if (isGeoFenceId_Avaliable) {
                                    String geofenceType =roomDBHelperInstance.get_GeoFence_info_dao().getGeoFenceTypeFromGeoFenceId(geoFence_id);
                                    String originalValue = "NA";
                                    String bleDeviceNameSaved = roomDBHelperInstance.get_TableDevice_dao().getDeviceNameSavedFromBleAddress(bleAddress);
                                    originalValue =  removePreviousZero(roomDBHelperInstance.get_RulesTable_dao().getRuleValueFromGeoFenceId_ruleId(geoFence_id,rule_id));
                                    if (originalValue.equalsIgnoreCase("")) {
                                        originalValue = "NA";
                                    }
                                    String timeStamp = getCurrenTimeStamp();
                                    GeofenceAlert geofenceAlert = new GeofenceAlert();
                                    geofenceAlert.setGeofence_ID(geoFence_id);
                                    geofenceAlert.setGeo_name("NA");
                                    geofenceAlert.setGeo_Type(geofenceType);

                                    /**
                                     * BreachType Check
                                     */

                                    if (rule_id.equalsIgnoreCase("07")) {
                                        if (rule_Value_hex.equalsIgnoreCase("00000000")) {
                                            geofenceAlert.setBreach_Type("OUT");

                                        } else if (rule_Value_hex.equalsIgnoreCase("00000001")) {
                                        }
                                        geofenceAlert.setBreach_Type("IN");
                                    }
                                    geofenceAlert.setBreach_Lat(breach_latitude);
                                    geofenceAlert.setBreach_Long(breach_longitude);
                                    geofenceAlert.setDate_Time(getDateTime());
                                    geofenceAlert.setTimeStamp(timeStamp);
                                    geofenceAlert.setBleAddress("SC2 Device : " + bleAddressWithColon);
                                    geofenceAlert.setIs_Read("0");
                                    geofenceAlert.setBreachRule_ID(rule_id);
                                    geofenceAlert.setAlias_name_alert(bleDeviceNameSaved);

                                    /**
                                     * Rule Name
                                     */
                                    if (rule_Id_hex.equalsIgnoreCase("03")) {
                                        geofenceAlert.setRule_Name("Breach Minimum Dwell Time");

                                    } else if (rule_Id_hex.equalsIgnoreCase("04")) {

                                        geofenceAlert.setRule_Name("Breach Maximum Dwell Time");

                                    } else if (rule_Id_hex.equalsIgnoreCase("05")) {
                                        geofenceAlert.setRule_Name("Breach Minimum speed limit");

                                    } else if (rule_Id_hex.equalsIgnoreCase("06")) {
                                        geofenceAlert.setRule_Name("Breach Maximum speed limit");

                                    } else if (rule_Id_hex.equalsIgnoreCase("07")) {
                                        geofenceAlert.setRule_Name("Boundary Cross Violation");

                                    } else if (rule_Id_hex.equalsIgnoreCase("01")) {
                                        geofenceAlert.setRule_Name("Start Date Geo Fence Enabled/Disabled");

                                    } else if (rule_Id_hex.equalsIgnoreCase("02")) {
                                        geofenceAlert.setRule_Name("End Date Geo Fence Enabled/Disabled");

                                    }
                                    geofenceAlert.setBreachRuleValue(rule_value);
                                    if (rule_Id_hex.equalsIgnoreCase("01")) {
                                        /**
                                         * Not used by the firmware
                                         */
                                        geofenceAlert.setMessage_one("GeoFence Should be Disabled/Enabled");
                                        geofenceAlert.setMessage_two("Date: ");
                                    } else if (rule_Id_hex.equalsIgnoreCase("02")) {
                                        /**
                                         * Not used by the Firmware
                                         */
                                        geofenceAlert.setMessage_one("GeoFence Should be Disabled/Enabled");
                                        geofenceAlert.setMessage_two("Date: ");
                                    } else if (rule_Id_hex.equalsIgnoreCase("03")) {

                                        geofenceAlert.setMessage_one("Minimum Dwell Time Permitted :  " + originalValue + " Min");
                                        String messageTwo = rule_value;
                                        if (messageTwo.equalsIgnoreCase("")) {
                                            messageTwo = "0";
                                        }
                                        geofenceAlert.setMessage_two("Current Time :" + messageTwo + " Min");
                                    } else if (rule_Id_hex.equalsIgnoreCase("04")) {
                                        geofenceAlert.setMessage_one("Maximum Dwell Time Permitted :  " + originalValue + " Min");
                                        String messageTwo = rule_value;
                                        if (messageTwo.equalsIgnoreCase("")) {
                                            messageTwo = "0";
                                        }
                                        geofenceAlert.setMessage_two("Current Time :" + messageTwo + " Min");
                                    } else if (rule_Id_hex.equalsIgnoreCase("05")) {
                                        geofenceAlert.setMessage_one("Minimum Speed Limit  :  " + originalValue + " km/hr");
                                        String messageTwo = rule_value;
                                        if (messageTwo.equalsIgnoreCase("")) {
                                            messageTwo = "0";
                                        }
                                        geofenceAlert.setMessage_two("Current Speed :" + messageTwo + " km/hr");
                                    } else if (rule_Id_hex.equalsIgnoreCase("06")) {
                                        geofenceAlert.setMessage_one("Maximum Speed Limit  :  " + originalValue + " km/hr");
                                        String messageTwo = rule_value;
                                        if (messageTwo.equalsIgnoreCase("")) {
                                            messageTwo = "0";
                                        }
                                        geofenceAlert.setMessage_two("Current Speed :" + messageTwo + " km/hr");

                                    } else if (rule_Id_hex.equalsIgnoreCase("07")) {

                                        if (rule_Value_hex.equalsIgnoreCase("00000001")) {
                                            geofenceAlert.setMessage_one("came in GeoFence" + "ID= " + geoFence_id);
                                            geofenceAlert.setMessage_two("");
                                        } else if (rule_Value_hex.equalsIgnoreCase("00000000")) {
                                            geofenceAlert.setMessage_one("Went out of Geo Fence" + "ID= " + geoFence_id);
                                            geofenceAlert.setMessage_two("");
                                        }
                                    }
                                    geofenceAlert.setOriginalRuleValue(originalValue);
                                    String geoFence_timestamp = roomDBHelperInstance.get_GeoFence_info_dao().getFirmwareTimeStampFromGeoFenceId(geoFence_id);
                                    String geoFence_lat = roomDBHelperInstance.get_GeoFence_info_dao().getLatitudeFromGeoFenceId(geoFence_id);
                                    String geoFence_long = roomDBHelperInstance.get_GeoFence_info_dao().getLongitudeFromGeoFenceId(geoFence_id);
                                    String geoFence_long_vertices_radius = roomDBHelperInstance.get_GeoFence_info_dao().getRadiusVerticesFromGeoFenceId(geoFence_id);
                                    geofenceAlert.setGeoFence_timestamp(geoFence_timestamp);
                                    geofenceAlert.setGeoFence_lat(geoFence_lat);
                                    geofenceAlert.setGeoFence_long(geoFence_long);
                                    geofenceAlert.setGeoFence_radius_vertices(geoFence_long_vertices_radius);
                                    roomDBHelperInstance.get_GeoFenceAlert_info_dao().insert_GeoFence_Alert(geofenceAlert);

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (geoFenceDialogAlertShow != null) {
                                                HistroyList histroyList = new HistroyList();


                                                histroyList.setGeoFenceId(geofenceAlert.getId());
                                                histroyList.setGeoFenceType(geofenceAlert.getGeo_Type());
                                                histroyList.setBreachlatitude(Double.parseDouble(geofenceAlert.getBreach_Lat()));
                                                histroyList.setBreachLongitude(Double.parseDouble(geofenceAlert.getBreach_Long()));
                                                histroyList.setBrachMessage(geofenceAlert.getRule_Name());
                                                histroyList.setMessage_one(geofenceAlert.getMessage_one());
                                                histroyList.setMessage_two(geofenceAlert.getMessage_two());
                                                histroyList.setTimeStamp(timeStamp);
                                                histroyList.setGeoFenceTimStamp(geoFence_timestamp);

                                                /**
                                                 * Increment the alertNotification for Evey alert
                                                 */
                                                String header_ruleViolation = geofenceAlert.getRule_Name();

                                                String ble_deviceName = bleDeviceNameSaved;
                                                String frist_message = geofenceAlert.getMessage_one();
                                                String second_message = geofenceAlert.getMessage_two();
                                                geoFenceDialogAlertShow.showDialogInterface(header_ruleViolation, ble_deviceName, frist_message, second_message, timeStamp);
                                                if (application_Visible_ToUser) {
                                                    /**
                                                     * show notification to the user  when the application is in backGround
                                                     */
                                                    createNotification(ble_deviceName, header_ruleViolation, frist_message, second_message);
                                                } else if (checkPhoneScreenLocked() && (!application_Visible_ToUser)) {
                                                    /**
                                                     * Show notification to the user when the application is in forground and Device is locked.
                                                     * This Screen lock method will work only when the user has selcted any one of the security patters for locking the screen other than none.
                                                     */
                                                    createNotification(ble_deviceName, header_ruleViolation, frist_message, second_message);
                                                }
                                                try {
                                                    Thread.sleep(10);
                                                    /**
                                                     * Remove here
                                                     */
                                                    sendSinglePacketDataToBle(bleAddressFromNotificationChanged,sendAckReadyForNextPacket(),"A5 ALERTS RECIEVED,SENDING ACK READY FOR NEXT ALERT");
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                }
                                                playAlertMusic();
                                                System.gc();
                                            }
                                        }
                                    });
                                }
                            }
                        });

                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("a801"))) {
                        from_firmware_ID_TimeStamp_A8_Packet = new ArrayList<String>();
                        from_firmware_ID_TimeStamp_A8_Packet.clear();
                        System.gc();
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("a802"))) {
                        String locaVariableForBlock = blehexObtainedFrom_Firmware;
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {

                                System.out.println("A8_CHECK "+locaVariableForBlock);
                                String id_1 = locaVariableForBlock.substring(6, 10);
                                String timeStamp_1 = locaVariableForBlock.substring(10, 18);

                             /*   String id_2 = locaVariableForBlock.substring(16, 20);
                                String timeStamp_2 = locaVariableForBlock.substring(20, 28);*/

                                System.out.println("A8_CHECK  ID_1 "+id_1+" TimeStamp= "+timeStamp_1);
                          //      System.out.println("A8_CHECK  ID_1 "+id_2+" TimeStamp= "+timeStamp_2);

                                if ((!id_1.equalsIgnoreCase("0000")) && (!timeStamp_1.equalsIgnoreCase("00000000"))) {
                                    from_firmware_ID_TimeStamp_A8_Packet.add(hexToint(id_1) + ":" + convertHexToLong(timeStamp_1));
                                }
                                /**
                                 * Commented as A8 packets is only sending one GeoFece ID and TimeStamp.
                                 */
                               /* if ((!id_2.equalsIgnoreCase("0000")) && (!timeStamp_2.equalsIgnoreCase("00000000"))) {
                                    from_firmware_ID_TimeStamp_A8_Packet.add(hexToint(id_2) + ":" + convertHexToLong(timeStamp_2));
                                }*/
                            }
                        });
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 4).equalsIgnoreCase("a803"))) {

                        process_TimeStamp_id_One_After_Other_A8_packet(bleAddressFromNotificationChanged);
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && (blehexObtainedFrom_Firmware.substring(0, 2).equalsIgnoreCase("b1"))) {
                        /**
                         * ACK packet for the message sent.
                         */
                        String locaVariableForBlock = blehexObtainedFrom_Firmware;
                        String sequenceNumber=""+convert4bytes(locaVariableForBlock.substring(4,12));
                        String messageACK=locaVariableForBlock.substring(12,14);
                        String messageStatusProcessed = "";

                        if (messageACK.equalsIgnoreCase("00")) {
                            messageStatusProcessed = getString(R.string.fragment_chat_message_mesaage_invalid_channel_id);
                            changeMessageStatusInDb(bleAddressFromNotificationChanged.replace(":", "").toLowerCase(), sequenceNumber, messageStatusProcessed);
                        } else if (messageACK.equalsIgnoreCase("01")) {
                            messageStatusProcessed = getString(R.string.fragment_chat_message_mesaage_full_message_recieved_by_device);
                            changeMessageStatusInDb(bleAddressFromNotificationChanged.replace(":", "").toLowerCase(), sequenceNumber, messageStatusProcessed);
                        } else if (messageACK.equalsIgnoreCase("02")) {
                            messageStatusProcessed = getString(R.string.fragment_chat_message_mesaage_message_sent_gsm);
                            changeMessageStatusInDb(bleAddressFromNotificationChanged.replace(":", "").toLowerCase(), sequenceNumber, messageStatusProcessed);
                        } else if (messageACK.equalsIgnoreCase("03")) {
                            messageStatusProcessed = getString(R.string.fragment_chat_message_mesaage_failed_message_gsm);
                            changeMessageStatusInDb(bleAddressFromNotificationChanged.replace(":", "").toLowerCase(), sequenceNumber, messageStatusProcessed);
                        } else if (messageACK.equalsIgnoreCase("04")) {
                            messageStatusProcessed = getString(R.string.fragment_chat_message_mesaage_send_to_iridium);
                            changeMessageStatusInDb(bleAddressFromNotificationChanged.replace(":", "").toLowerCase(), sequenceNumber, messageStatusProcessed);
                        } else if (messageACK.equalsIgnoreCase("05")) {
                            messageStatusProcessed = getString(R.string.fragment_chat_message_mesaage_server_sending_failed);
                            changeMessageStatusInDb(bleAddressFromNotificationChanged.replace(":", "").toLowerCase(), sequenceNumber, messageStatusProcessed);
                        } else {
                            messageStatusProcessed = getString(R.string.fragment_chat_message_mesaage_failed_app);
                            changeMessageStatusInDb(bleAddressFromNotificationChanged.replace(":", "").toLowerCase(), sequenceNumber, messageStatusProcessed);
                        }
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && ((blehexObtainedFrom_Firmware.substring(0, 2).equalsIgnoreCase("e1")))) {
                        String localVariableForBlock = blehexObtainedFrom_Firmware;
                        localVariableForBlock = localVariableForBlock.substring(16, 18) + localVariableForBlock.substring(14, 16) + localVariableForBlock.substring(12, 14) + localVariableForBlock.substring(10, 12) + localVariableForBlock.substring(8, 10) + localVariableForBlock.substring(6, 8) + localVariableForBlock.substring(4, 6);
                        /**
                         * After getting the IMEI.
                         * Ask for the token from firmware(If token avaliable in DB)
                         * API(Not avaliable in DB,Get from API)
                         */
                        imeiNumberFomFirmware="";
                        imeiNumberFomFirmware=convert7bytesToLong(localVariableForBlock);
                        checkTokenAvaliableForImeiNumber(bleAddressFromNotificationChanged,imeiNumberFomFirmware);
                        Log.d(TAG, "onReceive: ");
                    }
                    else if ((blehexObtainedFrom_Firmware.length() == 32) && ((blehexObtainedFrom_Firmware.substring(0, 2).equalsIgnoreCase("e2")))) {
                        String localVariableForBlock = blehexObtainedFrom_Firmware;
                        if (localVariableForBlock.equalsIgnoreCase("e2010100000000000000000000000000")) {
                            /**
                             * Ask for geoFence ID and Add Devices for Re-Connection.
                             * As its valid Token..
                             */
                            cancelProgressDialog();
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {

                                    boolean isRecordAvaliableForBleAddress = roomDBHelperInstance.get_TableDevice_dao().isRecordAvaliableForBleAddress(bleAddressFromNotificationChanged.toLowerCase().replace(":", ""));
                                    if (isRecordAvaliableForBleAddress) {
                                        loadGeoFenceId_TimeStamp(bleAddressFromNotificationChanged);
                                    } else {
                                        if ((openDialogToCheckDeviceName != null)) {
                                            openDialogToCheckDeviceName.showDialogNameNotAvaliable(bleAddressFromNotificationChanged, deviceToken_fromFirmware,imeiNumberFomFirmware);
                                            imeiNumberFomFirmware="";
                                        }
                                        loadGeoFenceId_TimeStamp(bleAddressFromNotificationChanged);
                                    }
                                }
                            });

                        } else if (localVariableForBlock.equalsIgnoreCase("e2010000000000000000000000000000")) {
                            /**
                             * Disconnect the device Give pop up.
                             * Token is Invalid.
                             */

                            deviceToken_fromFirmware = "";
                            cancelProgressDialog();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialogProvider.errorDialog("Invalid Token");
                                }
                            });
                            mBluetoothLeService.disconnect(bleAddressFromNotificationChanged);

                        } else if (localVariableForBlock.equalsIgnoreCase("e2010200000000000000000000000000")) {
                            /**
                             * Device Token not found.So Disconnect
                             */
                            deviceToken_fromFirmware = "";
                            cancelProgressDialog();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dialogProvider.errorDialog("Try After Some Time");
                                }
                            });
                            mBluetoothLeService.disconnect(bleAddressFromNotificationChanged);
                        }

                    }
                    else if((blehexObtainedFrom_Firmware.length() == 32) && ((blehexObtainedFrom_Firmware.substring(0, 2).equalsIgnoreCase("e3")))){
                        /**
                         * live tracking data obtained from the firmware.
                         */
                        String localVariableForBlock = blehexObtainedFrom_Firmware;
                        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainActivity_container);
                        if (fragment.toString().equalsIgnoreCase(new FragmentLiveTracking().toString())) {
                            if(liveRequestDataPassToFragment!=null){
                                liveRequestDataPassToFragment.liveRequestDataFromFirmware(Double.parseDouble(getFloatingPointValueFromHex(localVariableForBlock.substring(4,12))), Double.parseDouble(getFloatingPointValueFromHex(localVariableForBlock.substring(12,20))), bleAddressFromNotificationChanged);

                            }
                        }else {
                            if(ble_on_off()){
                                if(mBluetoothLeService.checkDeviceIsAlreadyConnected(bleAddressFromNotificationChanged)){
                                    byte [] stopReuestLcoation=Start_Stop_LIVE_LOCATION(false);
                                    sendSinglePacketDataToBle(bleAddressFromNotificationChanged,stopReuestLcoation,"STOP LIVE LOCAITON ");
                                }
                            }
                        }
                    }
                    else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c1010100000000000000000000000000"))){
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Setting Saved", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                onBackPressed();
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }
                    else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c3010100000000000000000000000000"))){
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Sim Details\nsaved", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                              dialog.dismiss();
                           //     onBackPressed();
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }   else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c4010100000000000000000000000000"))){
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Server Configuration\nSaved", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                onBackPressed();
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }
                    else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c5010100000000000000000000000000"))){
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Device Reset Sucessfull", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }  else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c2010100000000000000000000000000"))){
                        /**
                         * Industry Specifc Configuration Sucess.
                         */
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Industry Specific Configuration Saved", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                onBackPressed();

                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    } else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c2010000000000000000000000000000"))){
                        /**
                         * Industry Specifc Configuration failure.
                         */
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Something Went Wrong\nPlease try again.", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                onBackPressed();

                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c6010100000000000000000000000000"))){
                        /**
                         * Wifi Configuration Sucess.
                         */
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Wifi Configuration Saved", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                onBackPressed();

                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.equalsIgnoreCase("c6010000000000000000000000000000"))){
                        /**
                         * Wifi Configuration Sucess.
                         */
                        cancelProgressDialog();
                        dialogProvider.errorDialogWithCallBack("SC2 Companion App","Something Went Wrong\nPlease try again.", 0, false, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                onBackPressed();

                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    }else if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.substring(0,2).equalsIgnoreCase("b2"))){
                        /**
                         * Process the incomming message s here.
                         */
                        if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.substring(0,2).equalsIgnoreCase("b2"))&&(blehexObtainedFrom_Firmware.substring(4,6).equalsIgnoreCase("01"))){
                            /**
                             * message Start packet with upcode 01
                             */
                            String localVariableForBlock= blehexObtainedFrom_Firmware;
                            /**
                             * Message Starting packet.
                             */
                            incommingMessagePacket=new IncommingMessagePacket();
                            incommingMessagePacket.setTotalLengthOfTextmessage(""+Integer.parseInt(localVariableForBlock.substring(8,10),16));
                            String  timeStampInHexOppsite=localVariableForBlock.substring(16,18)+""+localVariableForBlock.substring(14,16)+""+localVariableForBlock.substring(12,14)+""+localVariableForBlock.substring(10,12);
                            incommingMessagePacket.setTimeStamp(""+Integer.parseInt(timeStampInHexOppsite,16));
                            incommingMessagePacket.setSequenceNumber(""+Integer.parseInt(localVariableForBlock.substring(18,26),16));
                        }
                        if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.substring(0,2).equalsIgnoreCase("b2"))&&(blehexObtainedFrom_Firmware.substring(4,6).equalsIgnoreCase("02"))){
                            /**
                             * message Start packet with upcode 02
                             */
                            String localVariableForBlock= blehexObtainedFrom_Firmware;
                            /**
                             * Message Starting packet.
                             */
                            incommingMessagePacket.setMessagePacketDataLength(""+Integer.parseInt(localVariableForBlock.substring(2,4),16));
                            int messageToExtractFrom=Integer.parseInt(incommingMessagePacket.getMessagePacketDataLength());
                            messageToExtractFrom=messageToExtractFrom-2;
                            messageToExtractFrom=messageToExtractFrom*2;
                            String hexString=localVariableForBlock.substring(8,messageToExtractFrom+8);
                            String getlast2charcters=hexString.length() > 2 ? hexString.substring(hexString.length() - 2) : hexString;
                            if(getlast2charcters.equalsIgnoreCase("00")){
                                hexString=hexString.substring(0,hexString.length()-2);
                                incommingMessageForConcatenation=incommingMessageForConcatenation+convertHexStringToString(hexString);
                            }else {
                                incommingMessageForConcatenation=incommingMessageForConcatenation+convertHexStringToString(hexString);
                            }
                        }
                        if((blehexObtainedFrom_Firmware.length()==32)&&(blehexObtainedFrom_Firmware.substring(0,2).equalsIgnoreCase("b2"))&&(blehexObtainedFrom_Firmware.substring(4,6).equalsIgnoreCase("03"))){
                            /**
                             * message Start packet with upcode 03 ending packet incomming message.
                             */
                            incommingMessagePacket.setMessageData(incommingMessageForConcatenation);
                            incommingMessageForConcatenation="";
                            String localVariableForBlock= blehexObtainedFrom_Firmware;
                            if(localVariableForBlock.substring(6,8).equalsIgnoreCase("01")){
                                incommingMessagePacket.setEndpacketChannelID(getResources().getString(R.string.GSM));
                            }else if(localVariableForBlock.substring(6,8).equalsIgnoreCase("02")){
                                incommingMessagePacket.setEndpacketChannelID(getResources().getString(R.string.IRIDIUM));
                            }

                            byte channelId=0;
                            if(incommingMessagePacket.getEndpacketChannelID().equalsIgnoreCase(getResources().getString(R.string.IRIDIUM))){
                                channelId=2;
                            }else   if(incommingMessagePacket.getEndpacketChannelID().equalsIgnoreCase(getResources().getString(R.string.GSM))){
                                channelId=1;
                            }
                            if(Integer.parseInt(incommingMessagePacket.getTotalLengthOfTextmessage())==Integer.parseInt(""+incommingMessagePacket.getMessageData().length())){
                                /**
                                 * Recieveed incomming message sucess
                                 */
                                sendSinglePacketDataToBle(bleAddressFromNotificationChanged,incommingMessageACK(incommingMessagePacket.getSequenceNumber(),channelId, (byte) 1),"INCOMMING MESSAGE SUCESS");
                                /**
                                 * insert chat to table and send UI updte for recycleView.
                                 */
                                insertChatInfoToTable(incommingMessagePacket.getMessageData(),incommingMessagePacket.getSequenceNumber(),bleAddressFromNotificationChanged.toLowerCase().replace(":",""), incommingMessagePacket.getEndpacketChannelID());

                            }else {
                                /**
                                 * incomming message recieved failure..
                                 */
                                sendSinglePacketDataToBle(bleAddressFromNotificationChanged,incommingMessageACK(incommingMessagePacket.getSequenceNumber(),channelId, (byte) 0),"INCOMMING MESSAGE FAILURE");
                            }

                        }

                    }
                }







            }else  if ((action != null) && (action.equalsIgnoreCase(getResources().getString(R.string.BLUETOOTHLE_SERVICE_NOTIFICATION_ENABLE)))) {
                /**
                 * Send Data to BLE Device.
                 * 1)Ask for the Code.(i.e write 01 to BleDevice.)
                 */
                boolean notificationEnabled=intent.getBooleanExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_NOTIFICATION_ENABLE_DATA),false);
                if(notificationEnabled){
                    String bleAddress = intent.getStringExtra(getResources().getString(R.string.BLUETOOTHLE_SERVICE_NOTIFICATION_ENABLE_BLE_AADRESS));
                    mBluetoothLeService.sendDataToBleDevice(bleAddress,WriteValue01());
                    Log.d(TAG, "onReceive: Asing Magic Numebr");
                }
            }
        }

        private void writeConnectionMainTaincenceCodeToFirmware(String bleAddress, byte[] connectionArray) {
            from_DataBase_ID_TimeStamp = new ArrayList<String>();
            from_firmware_ID_TimeStamp = new ArrayList<String>();
            from_firmware_ID_TimeStamp_A8_Packet = new ArrayList<String>();
            System.gc();
            sendDataToBleDeviceWithoutEncryption(bleAddress,connectionArray,"Write Connection MainTainence Code");
        }

        private void passConnectionSucesstoFragmentScanForUIChange(String connectedDeviceAddress, boolean connect_disconnect) {
            if (passConnectionStatusToFragment != null) {
                passConnectionStatusToFragment.connectDisconnect(connectedDeviceAddress, connect_disconnect);
            }
        }
    };
    private ScanCallback leScanCallback =
            new ScanCallback() {
                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    super.onScanResult(callbackType, result);
                    if (passScanDeviceToActivity_interface != null) {
                        if (result != null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainActivity_container);
                                    if (fragment != null) {
                                        if (fragment.toString().equalsIgnoreCase(new FragmentScan().toString())) {
                                            if ((result.getDevice().getName() != null) && (result.getDevice().getName().length() > 0)&&(result.getDevice().getName().startsWith(getString(R.string.device_name_filter)))) {
                                                passScanDeviceToActivity_interface.sendCustomBleDevice(new CustBluetootDevices(result.getDevice().getAddress(), result.getDevice().getName(), result.getDevice(), false));
                                            }
                                        }
                                    }

                                }
                            });
                        }
                    }
                }
            };

    private void startScan() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                SCAN_TAG = getResources().getString(R.string.SCAN_STARTED);
                bluetoothLeScanner.startScan(leScanCallback);
            }
        });

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, SCAN_PERIOD);
    }

    private void stopScan() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (SCAN_TAG.equalsIgnoreCase(getResources().getString(R.string.SCAN_STARTED))) {
                    SCAN_TAG = getResources().getString(R.string.SCAN_STOPED);
                    bluetoothLeScanner.stopScan(leScanCallback);
                }

            }
        });
    }

    public void start_stop_scan() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if(ble_on_off()){
                if (SCAN_TAG.equalsIgnoreCase(getResources().getString(R.string.SCAN_STOPED)) || (SCAN_TAG.equalsIgnoreCase(""))) {
                    startScan();
                }else if (SCAN_TAG.equalsIgnoreCase(getResources().getString(R.string.SCAN_STARTED))) {
                    /**
                     * Scan already started.
                     */
                }
            }
        }

    }

    public void setupPassScanDeviceToActivity_interface(PassScanDeviceToActivity_interface loc_passScanDeviceToActivity_interface) {
        this.passScanDeviceToActivity_interface = loc_passScanDeviceToActivity_interface;
    }

    @Override
    public void makeDevieConnecteDisconnect(CustBluetootDevices custBluetootDevices, boolean connect_disconnect) {
        if (connect_disconnect) {
            boolean connectissue = mBluetoothLeService.connect(custBluetootDevices.getBleAddress());
            if (SCAN_TAG.equalsIgnoreCase(getResources().getString(R.string.SCAN_STARTED))) {
                stopScan();
            }
        } else {
            mBluetoothLeService.disconnect(custBluetootDevices.getBleAddress());
            if (SCAN_TAG.equalsIgnoreCase(getResources().getString(R.string.SCAN_STARTED))) {
                stopScan();
            }
        }
    }

    /**
     * Write Data to BLE device After Confermation.
     */

    private static void sendNextDataToFirmmWareAfterConfermation(byte [] obtainedFromOnCharcterticWrite,String bleAddressToWrite,String reasonToWrite){
        try {
            byte[] decrypted_byteArray_FromFirmware =decryptData(obtainedFromOnCharcterticWrite);
            String hex_converted_decrypted_byte_array = bytesToHex(decrypted_byteArray_FromFirmware);
            if (!UNIVERSAL_ARRAY_PACEKT_LIST.isEmpty() && (UNIVERSAL_ARRAY_PACEKT_LIST.contains(hex_converted_decrypted_byte_array))) {
                UNIVERSAL_ARRAY_PACEKT_LIST.remove(hex_converted_decrypted_byte_array);
                if (UNIVERSAL_ARRAY_PACEKT_LIST.size() > 0) {

                    byte[] bytesDataToWrite = encryptData(byteConverstionHelper_hexStringToByteArray(UNIVERSAL_ARRAY_PACEKT_LIST.get(0)));
                    mBluetoothLeService.sendDataToBleDevice(bleAddressToWrite,bytesDataToWrite);
                }
            }

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public static void  sendSinglePacketDataToBle(String bleAddress,byte[] dataNeedToSend,String reasonToWriteData){
        try {

            byte [] encryptedData=encryptData(dataNeedToSend);
            mBluetoothLeService.sendDataToBleDevice(bleAddress,encryptedData);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
    }

    public static void sendDataToBleDeviceWithoutEncryption(String bleAddress,byte[] dataNeedToSend,String reasonToWriteData){
        mBluetoothLeService.sendDataToBleDevice(bleAddress,dataNeedToSend);
    }

    private void showDFUProgressDialog_change_label(String label,String detailedlabel){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(hud!=null&&hud.isShowing()){
                    hud.setDetailsLabel(detailedlabel);
                }else {
                    hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                            .setLabel(label)
                            .setDetailsLabel(detailedlabel)
                            .setCancellable(false);
                    hud.show();
                }
            }
        });
    }


    /**
     * Used to Fetch all registered device list.
     */
    private void getAllRegisteredDevices() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                deviceTableList = roomDBHelperInstance.get_TableDevice_dao().getAll_tableDevices();
            }
        });
    }
}


