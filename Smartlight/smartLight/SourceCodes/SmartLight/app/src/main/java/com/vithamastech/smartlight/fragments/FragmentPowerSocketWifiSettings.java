package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.vithamastech.smartlight.DialogFragementHelper.WifiConfigAlertDialog;
import com.vithamastech.smartlight.DialogFragementHelper.WifiDialogFragment;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;
import com.vithamastech.smartlight.PowerSocketUtils.DateUtils;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoAddDeviceData;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.interfaces.OnBluetoothStateChangeListener;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.services.PowerSocketBLEService;
import com.vithamastech.smartlight.services.PowerSocketBLEEventCallbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.vithamastech.smartlight.MainActivity.mActivityPowerSocketSelected;

public class FragmentPowerSocketWifiSettings extends Fragment {

    private Button buttonRemoveWifi, buttonConfigWifi;
    private MainActivity activity;
    private PowerSocket powerSocket;

    private int actionSelected = 0;            // Selects between StartWifiScan and DeleteWifiDevice. This will be used when Authorization is successful after connecting to the device

    private ImageView imageViewWifiConfigStatus;
    private TextView textViewWifiConfigStatus;
    private boolean isPasswordEntered;
    private boolean isWifiConfigInProgress;
    private boolean isWifiConfigRemovalInProgress;

    private PowerSocketBLEService powerSocketBLEService;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<WifiDevice> foundWifiNetworkList;

    public FragmentPowerSocketWifiSettings() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        powerSocket = mActivityPowerSocketSelected;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_socket_wifi_setting, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        activity.mTextViewTitle.setText(R.string.frg_settings_header);
        activity.mImageViewBack.setVisibility(View.GONE);
        activity.mImageViewAddDevice.setVisibility(View.GONE);
        activity.mImageViewAddDevice.setImageResource(R.drawable.ic_refresh_icon_white);
        activity.showBackButton(true);
        activity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        powerSocketBLEService = PowerSocketBLEService.getInstance(activity.getApplicationContext());

        buttonRemoveWifi = view.findViewById(R.id.buttonRemoveWifi);
        buttonConfigWifi = view.findViewById(R.id.buttonConfigWifi);
        imageViewWifiConfigStatus = view.findViewById(R.id.imageViewWifiConfigStatus);
        textViewWifiConfigStatus = view.findViewById(R.id.textViewWifiConfigStatus);

        buttonRemoveWifi.setOnClickListener(v -> {
            isWifiConfigRemovalInProgress = true;
            // Remove Wifi
            actionSelected = 1;
            deleteWifiConfig();
        });

        buttonConfigWifi.setOnClickListener(v -> {
            isWifiConfigInProgress = true;
            // Scan available Wifi devices.
            actionSelected = 2;
            startWifiDeviceScan();
        });
    }

    private void startWifiDeviceScan() {
        if (powerSocket != null) {
            if (powerSocket.bleAddress != null && !powerSocket.bleAddress.isEmpty()) {
                if (bluetoothAdapter.isEnabled()) {
                    activity.showProgress("Searching for available Wi-Fi", false);
                    if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                        powerSocketBLEService.scanWifiDevices(powerSocket);
                    } else {
                        actionSelected = 2;
                        powerSocketBLEService.connect(powerSocket);
                    }
                } else {
                    activity.mUtility.errorDialog("Please switch on Bluetooth to remove Wi-Fi config for power socket",
                            1, true);
                }
            } else {
                // Todo Invalid BLE Address
            }
        } else {
            // Todo Power socket is null
        }
    }

    private void deleteWifiConfig() {
        if (powerSocket != null) {
            if (powerSocket.bleAddress != null && !powerSocket.bleAddress.isEmpty()) {
                if (bluetoothAdapter.isEnabled()) {
                    activity.showProgress("Removing configured Wi-Fi", false);
                    if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                        powerSocketBLEService.removeWifiConfig(powerSocket);
                    } else {
                        powerSocketBLEService.connect(powerSocket);
                    }
                } else {
                    activity.mUtility.errorDialog("Please switch on Bluetooth to remove Wi-Fi config for power socket",
                            1, true);
                }
            } else {
                // Todo Invalid device address
            }
        } else {
            // Todo Power socket is null
        }
    }

    private void showWifiListDialogFragment(Bundle bundle) {
        WifiDialogFragment wifiDialogFragment = new WifiDialogFragment();
        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.Wifi_Dialog_Fragment_DialogTag_1));
        if (prev != null) {
            ft.remove(prev);
        }
        wifiDialogFragment.setArguments(bundle);
        wifiDialogFragment.show(activity.getSupportFragmentManager(), getResources().getString(R.string.Wifi_Dialog_Fragment_DialogTag_1));
        wifiDialogFragment.setUpDialogListenerWifi(new WifiDialogFragment.DialogFragmentWifiListener() {
            @Override
            public void onWifiDeviceClicked(WifiDevice wifiDevice, int position) {
                showWifiConfigDialog(wifiDevice);
            }

            @Override
            public void onCancelled() {
                isWifiConfigInProgress = false;
                // Show Alert Dialog that the device is only controlled by Bluetooth.
                activity.mUtility.errorDialogWithCallBack("Device can only be controlled over Bluetooth.\n" +
                                "You can configure device over Wifi by going to device settings later.",
                        3, false, dialogCallBack);
            }
        });
    }

    public void showWifiConfigDialog(WifiDevice wifiDevice) {
        WifiConfigAlertDialog dialogFragment = new WifiConfigAlertDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(WifiConfigAlertDialog.WifiDeviceLabel, wifiDevice);
        dialogFragment.setArguments(bundle);

        FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
        Fragment prev = activity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(activity.getSupportFragmentManager(), "dialog");
        dialogFragment.setDialogListener(new WifiConfigAlertDialog.DialogListener() {
            @Override
            public void onFinishEditDialog(WifiDevice wifiDevice, String wifiPassword, boolean dialogCancelFlag) {
                isPasswordEntered = true;
                activity.showProgress("Configuring Wi-Fi. Please wait....", false);

                // Todo Check Bluetooth Connection before configuring Wi-Fi
                powerSocketBLEService.configureWifi(powerSocket, wifiDevice, wifiPassword);
            }

            @Override
            public void onCancelled() {
                isWifiConfigInProgress = false;
                Bundle wifiBundleList = new Bundle();
                wifiBundleList.putSerializable(getResources().getString(R.string.Wifi_list), foundWifiNetworkList);
                showWifiListDialogFragment(wifiBundleList);
            }
        });
    }

    private void navigateToDeviceListFragment() {
        activity.onBackPressed();
    }

    private void updateWifiStatus() {
        if (powerSocket != null) {
            int imagePath = 0;
            String wifiConfigStatusText = "";
            if (powerSocket.isWifiConfigured) {
                imagePath = R.drawable.tick;
                wifiConfigStatusText = "Wi-Fi Configured";
                buttonRemoveWifi.setVisibility(View.VISIBLE);
            } else {
                imagePath = R.drawable.redcross;
                wifiConfigStatusText = "Wi-Fi not Configured";
                buttonRemoveWifi.setVisibility(View.GONE);
            }

            textViewWifiConfigStatus.setText(wifiConfigStatusText);
            imageViewWifiConfigStatus.setImageDrawable(ContextCompat.getDrawable(getContext(), imagePath));
        }
    }

    onAlertDialogCallBack dialogCallBack = new onAlertDialogCallBack() {
        @Override
        public void PositiveMethod(DialogInterface dialog, int id) {
            navigateToDeviceListFragment();
        }

        @Override
        public void NegativeMethod(DialogInterface dialog, int id) {

        }
    };

    PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks = new PowerSocketBLEEventCallbacks() {
        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            // Show Device disconnected Alert Dialog only when Wi-Fi configuration is in progress
            if (isWifiConfigInProgress) {
                isWifiConfigInProgress = false;
                if (isVisible()) {
                    activity.hideProgress();
                    activity.mUtility.errorDialog("Device disconnected during Wi-Fi configuration. Please try again.", 1, true);
                }
            } else if (isWifiConfigRemovalInProgress) {
                isWifiConfigRemovalInProgress = false;
                if (isVisible()) {
                    activity.hideProgress();
                    activity.mUtility.errorDialog("Device disconnected during Wi-Fi configuration. Please try again.", 1, true);
                }
            }
        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {
            // Show Device connection failed Alert Dialog only when Wi-Fi configuration is in progress
            if (isWifiConfigInProgress) {
                isWifiConfigInProgress = false;
                if (isVisible()) {
                    activity.hideProgress();
                    activity.mUtility.errorDialog("Device connection failed during Wi-Fi configuration. Please try again.", 1, true);
                }
            } else if (isWifiConfigRemovalInProgress) {
                isWifiConfigRemovalInProgress = false;
                if (isVisible()) {
                    activity.hideProgress();
                    activity.mUtility.errorDialog("Device connection failed during Wi-Fi configuration. Please try again.", 1, true);
                }
            }
        }

//        @Override
//        public void onBluetoothRestart() {
//            super.onBluetoothRestart();
////            activity.mUtility.errorDialog("Device connection failed. Please turn off and turn off Bluetooth and try again. " +
////                    "If the same problems persists, restart the phone and try again.", 1, true);
//        }

        @Override
        public void onAuthenticationSuccess(String deviceAddress) {
            super.onAuthenticationSuccess(deviceAddress);
            if (actionSelected == 1) {
                deleteWifiConfig();
            } else if (actionSelected == 2) {
                startWifiDeviceScan();
            } else {
                activity.runOnUiThread(() -> Toast.makeText(getContext(), "Invalid action selected", Toast.LENGTH_SHORT).show());
            }
        }

        @Override
        public void onAuthenticationFailure(String deviceAddress) {
            super.onAuthenticationFailure(deviceAddress);
            isWifiConfigInProgress = false;
            isWifiConfigRemovalInProgress = false;
            activity.hideProgress();
            if (!isVisible()) {
                activity.mUtility.errorDialog("Authentication failed.", 1, true);
                if (powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                    powerSocketBLEService.disconnect(deviceAddress);
                }
            }
        }

        @Override
        public void onWifiConfigured(String deviceAddress) {
            super.onWifiConfigured(deviceAddress);
            activity.hideProgress();
            isWifiConfigInProgress = false;
            powerSocket.isWifiConfigured = true;
            saveDeviceData(powerSocket);
            updateWifiStatus();
            activity.mUtility.errorDialogWithCallBack("Wi-Fi configuration successful",
                    0, false, dialogCallBack);
        }

        @Override
        public void onWifiNotConfigured(String deviceAddress) {
            super.onWifiNotConfigured(deviceAddress);
            activity.hideProgress();
            if (isWifiConfigInProgress) {
                isWifiConfigInProgress = false;
                if (isPasswordEntered) {
                    isPasswordEntered = false;
                    activity.mUtility.errorDialog("Wi-Fi not configured. Please try again", 1, true);
                } else {
                    activity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.Wifi_Config_Dialog_Titile),
                            getResources().getString(R.string.Wifi_Config_Dialog), "Yes", "No",
                            false, 3, new onAlertDialogCallBack() {
                                @Override
                                public void PositiveMethod(DialogInterface dialog, int id) {
                                    if (powerSocket != null) {
                                        if (powerSocket.bleAddress != null && !powerSocket.bleAddress.isEmpty()) {
                                            if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                                                activity.showProgress("Searching Wi-Fi Devices", false);
                                                startWifiDeviceScan();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void NegativeMethod(DialogInterface dialog, int id) {
                                    // Show Alert Dialog that the device is only controlled by Bluetooth.
                                    activity.mUtility.errorDialogWithCallBack("Device can only be controlled over Bluetooth. " +
                                                    "You can configure device over Wi-Fi by going to device settings later.",
                                            1, false, dialogCallBack);
                                }
                            });
                }
            } else if (isWifiConfigRemovalInProgress) {
                isWifiConfigRemovalInProgress = false;
                activity.mUtility.errorDialog("Wi-Fi configuration deleted successfully.",
                        0, true);
                powerSocket.isWifiConfigured = false;
                // Update wifi-config status in Database
                saveDeviceData(powerSocket);
                updateWifiStatus();
            }
        }

        @Override
        public void onWifiConfigFailed(String deviceAddress) {
            super.onWifiConfigFailed(deviceAddress);
            isWifiConfigInProgress = false;
            activity.hideProgress();
            activity.mUtility.errorDialogWithCallBack("Wi-Fi configuration failed. Please try again",
                    1, false, dialogCallBack);
        }

        @Override
        public void onWifiMqttNotSubscribed(String deviceAddress) {
            super.onWifiMqttNotSubscribed(deviceAddress);
            isWifiConfigInProgress = false;
            activity.hideProgress();
            activity.mUtility.errorDialogWithCallBack("Connected to MQTT, but not configured",
                    1, false, dialogCallBack);
        }

        @Override
        public void onWifiDeviceListFound(String deviceAddress, ArrayList<WifiDevice> wifiDeviceList) {
            super.onWifiDeviceListFound(deviceAddress, wifiDeviceList);
            console.log("wifiDevices", new Gson().toJson(wifiDeviceList));
            activity.hideProgress();
            foundWifiNetworkList = wifiDeviceList;
            Bundle wifiBundleList = new Bundle();
            wifiBundleList.putSerializable(getResources().getString(R.string.Wifi_list), foundWifiNetworkList);
            showWifiListDialogFragment(wifiBundleList);
        }

        @Override
        public void onWifiDevicesNotFound(String deviceAddress) {
            isWifiConfigInProgress = false;
            if (isVisible()) {
                activity.mUtility.HideProgress();
                activity.mUtility.errorDialogWithCallBack("Wi-Fi devices not found. Please try again",
                        1, false, dialogCallBack);
            }
        }

        @Override
        public void onWifiConfigRemoved(String deviceAddress) {
            super.onWifiConfigRemoved(deviceAddress);
            activity.hideProgress();
            isWifiConfigRemovalInProgress = false;
            activity.mUtility.errorDialog("Wi-Fi configuration deleted successfully.",
                    0, true);
            powerSocket.isWifiConfigured = false;
            // Update wifi-config status in Database
            saveDeviceData(powerSocket);
            updateWifiStatus();
        }

        @Override
        public void onWifiConfigRemoveFailure(String deviceAddress) {
            isWifiConfigRemovalInProgress = false;
            super.onWifiConfigRemoveFailure(deviceAddress);
            activity.hideProgress();
            activity.mUtility.errorDialogWithCallBack("Failed to delete Wi-Fi configuration. Please try again",
                    0, false, dialogCallBack);
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        activity.setOnBluetoothStateChangeListener(bluetoothStateChangeListener);
        powerSocketBLEService.setOnPowerSocketEventCallbacks(powerSocketBLEEventCallbacks);
        // Check wifiStatus
        updateWifiStatus();
    }

    @Override
    public void onStop() {
        super.onStop();
        powerSocketBLEService.setOnPowerSocketEventCallbacks(null);
        activity.setOnBluetoothStateChangeListener(null);
    }

    private void saveDeviceData(PowerSocket powerSocket) {
        // Check if a record exists in DB
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(DBHelper.mFieldDeviceServerId, "");
        mContentValues.put(DBHelper.mFieldDeviceUserId, activity.mPreferenceHelper.getUserId());
        mContentValues.put(DBHelper.mFieldDeviceCommID, "NA");
        mContentValues.put(DBHelper.mFieldDeviceCommHexId, "NA");
        mContentValues.put(DBHelper.mFieldDeviceRealName, "Vithamas Socket");
        mContentValues.put(DBHelper.mFieldDeviceName, powerSocket.bleName);

        String formattedBLEAddress = powerSocket.bleAddress.toUpperCase();
        formattedBLEAddress = formattedBLEAddress.replace(":", "");
        mContentValues.put(DBHelper.mFieldDeviceBleAddress, formattedBLEAddress);

        int deviceType = powerSocket.deviceType;
        String formattedDeviceType = String.format("%04X", deviceType);
        mContentValues.put(DBHelper.mFieldDeviceType, formattedDeviceType);

        String queryDeviceType = "select " + DBHelper.mFieldDeviceTypeTypeName + " from " + DBHelper.mTableDeviceType +
                " where " + DBHelper.mFieldDeviceTypeType + "= '" + formattedDeviceType + "'";
        String deviceTypeName = activity.mDbHelper.getQueryResult(queryDeviceType);
        mContentValues.put(DBHelper.mFieldDeviceTypeName, deviceTypeName);

        boolean isWifiConfigured = powerSocket.isWifiConfigured;
        console.log("axashxkasx", "IsWifiConfigured = " + isWifiConfigured);
        int isWifiConfigState = 0;
        if (isWifiConfigured) {
            isWifiConfigState = 1;
        }
        mContentValues.put(DBHelper.mFieldDeviceIsWifiConfigured, isWifiConfigState);

        mContentValues.put(DBHelper.mFieldConnectStatus, "YES");
        mContentValues.put(DBHelper.mFieldSwitchStatus, "ON");
        mContentValues.put(DBHelper.mFieldDeviceIsFavourite, "0");
        mContentValues.put(DBHelper.mFieldDeviceLastState, "0");
        mContentValues.put(DBHelper.mFieldDeviceTimeStamp, System.currentTimeMillis());
        mContentValues.put(DBHelper.mFieldDeviceIsActive, "1");
        mContentValues.put(DBHelper.mFieldDeviceCreatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));
        mContentValues.put(DBHelper.mFieldDeviceUpdatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));
        mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");

        // Check if a record exists in Database
        String localDeviceId = CheckRecordExistInDeviceDB(formattedBLEAddress);

        if (localDeviceId.equalsIgnoreCase("-1")) {  // Insert new record
            console.log("jyasxjysafxyaf", "Insert New Record");
            activity.mDbHelper.insertRecord(DBHelper.mTableDevice, mContentValues);
        } else { // Update existing record
            console.log("jyasxjysafxyaf", "Update Record");
            console.log("aslxnalsxn", mContentValues.get(DBHelper.mFieldDeviceIsWifiConfigured));

            activity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues, DBHelper.mFieldDeviceLocalId + "=?", new String[]{localDeviceId});

            String queryDeviceName = "select " + DBHelper.mFieldDeviceName + " from " + DBHelper.mTableDevice + " where " +
                    DBHelper.mFieldDeviceLocalId + "= '" + localDeviceId + "'";

            String deviceName = activity.mDbHelper.getQueryResult(queryDeviceName);
            mContentValues.put(DBHelper.mFieldDeviceLocalId, localDeviceId);
            mContentValues.put(DBHelper.mFieldDeviceName, deviceName);
            mContentValues.put(DBHelper.mFieldDeviceUpdatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));

            if (!activity.mPreferenceHelper.getIsSkipUser() && activity.mUtility.haveInternet()) {
                addDeviceAPI(mContentValues);
            } else {
                // Todo No Internet Connection
            }
        }
    }

    public String CheckRecordExistInDeviceDB(String bleAddress) {
        if (bleAddress != null && !bleAddress.equalsIgnoreCase("") && !bleAddress.equalsIgnoreCase("null")) {
            bleAddress = bleAddress.toUpperCase();
        }

        DataHolder mDataHolder;
        String url = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceBleAddress + "= '" + bleAddress + "'";
        mDataHolder = activity.mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Call api and save device data to server*/
    public void addDeviceAPI(final ContentValues mContentValuesParam) {
        activity.mUtility.hideKeyboard(activity);
        Map<String, String> params = new HashMap<String, String>();
        String mStringDeviceHex = mContentValuesParam.get(DBHelper.mFieldDeviceCommHexId).toString();
        params.put("device_token", activity.mPreferenceHelper.getDeviceToken());
        params.put("user_id", activity.mPreferenceHelper.getUserId());
        params.put("device_id", mContentValuesParam.get(DBHelper.mFieldDeviceCommID).toString());
        params.put("hex_device_id", mStringDeviceHex.toLowerCase());
        params.put("device_name", mContentValuesParam.get(DBHelper.mFieldDeviceName).toString());
        String queryDeviceTypeId = "select " + DBHelper.mFieldDeviceTypeServerID + " from " + DBHelper.mTableDeviceType + " where " +
                DBHelper.mFieldDeviceTypeType + "= '" + mContentValuesParam.get(DBHelper.mFieldDeviceTypeType).toString() + "'";
        String deviceTypeServerID = activity.mDbHelper.getQueryResult(queryDeviceTypeId);
        if (deviceTypeServerID != null && !deviceTypeServerID.equalsIgnoreCase("")) {
            params.put("device_type", deviceTypeServerID);
        } else {
            params.put("device_type", "0000");
        }
        params.put("ble_address", mContentValuesParam.get(DBHelper.mFieldDeviceBleAddress).toString().toUpperCase());
        params.put("status", "1");// 1-Active,2-DeActive
        params.put("is_favourite", "2");// 1-YES,2-NO
        params.put("is_update", "0");// 0-Insert,1-Update
        params.put("remember_last_color", "0");
        params.put("wifi_configured", String.valueOf(mContentValuesParam.get(DBHelper.mFieldDeviceIsWifiConfigured)));

        Call<VoAddDeviceData> mLogin = activity.mApiService.addDeviceAPI(params);
        mLogin.enqueue(new Callback<VoAddDeviceData>() {
            @Override
            public void onResponse(Call<VoAddDeviceData> call, Response<VoAddDeviceData> response) {
                activity.mUtility.HideProgress();
                if (isAdded()) {
                    VoAddDeviceData mAddDeviceAPI = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mAddDeviceAPI);
                    if (mAddDeviceAPI != null && mAddDeviceAPI.getResponse().equalsIgnoreCase("true")) {
                        if (mAddDeviceAPI.getData() != null) {
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(DBHelper.mFieldDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                            mContentValues.put(DBHelper.mFieldDeviceIsSync, "1");
                            String[] mArray = new String[]{mContentValuesParam.get(DBHelper.mFieldDeviceLocalId).toString()};
                            activity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                                    DBHelper.mFieldDeviceLocalId + "=?", mArray);

                            ContentValues mContentValuesSocket = new ContentValues();
                            mContentValuesSocket.put(DBHelper.mFieldSocketDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                            String[] mArraySocket = new String[]{mContentValuesParam.get(DBHelper.mFieldDeviceLocalId).toString()};
                            activity.mDbHelper.updateRecord(DBHelper.mTablePowerStripSocket, mContentValuesSocket,
                                    DBHelper.mFieldSocketDeviceLocalId + "=?", mArraySocket);
                        }
                    } else {
                        if (mAddDeviceAPI != null && mAddDeviceAPI.getMessage() != null && !mAddDeviceAPI.getMessage().equalsIgnoreCase("")) {
//                        checkAdapterIsEmpty();
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<VoAddDeviceData> call, Throwable t) {
                activity.mUtility.HideProgress();
                if (isAdded()) {
//                checkAdapterIsEmpty();
                }
                t.printStackTrace();
            }
        });
    }

    OnBluetoothStateChangeListener bluetoothStateChangeListener = new OnBluetoothStateChangeListener() {
        @Override
        public void onBluetoothOff() {

        }

        @Override
        public void onBluetoothTurningOff() {
            if (powerSocketBLEService.isDeviceConnected(mActivityPowerSocketSelected.bleAddress)) {
                powerSocketBLEService.disconnect(mActivityPowerSocketSelected.bleAddress);
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