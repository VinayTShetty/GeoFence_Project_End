package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.evergreen.ble.advertisement.AdvertisementRecord;
import com.evergreen.ble.advertisement.ManufactureData;
import com.google.gson.Gson;

import com.vithamastech.smartlight.Adapter.FoundPowerSocketAdapter;
import com.vithamastech.smartlight.BaseFragment.BaseFragment;
import com.vithamastech.smartlight.DialogFragementHelper.DeviceNameEditAlertDialog;
import com.vithamastech.smartlight.DialogFragementHelper.WifiConfigAlertDialog;
import com.vithamastech.smartlight.DialogFragementHelper.WifiDialogFragment;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketCustomObjects.WifiDevice;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.PowerSocketUtils.DateUtils;
import com.vithamastech.smartlight.PowerSocketUtils.EncryptionUtilsPowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketAdvData;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketConstants;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoAddDeviceData;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.vithamastech.smartlight.services.PowerSocketBLEService;
import com.vithamastech.smartlight.services.PowerSocketBLEEventCallbacks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Todo complete isAddDeviceInProgress validation
// Todo complete isWifiConfigInProgress validation
public class FragmentAddPowerSocket extends BaseFragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    private FoundPowerSocketAdapter foundMonitorAdapter;

    private PowerSocketBLEService powerSocketBLEService;
    private PowerSocket selectedPowerSocket;

    private boolean isDeviceAlreadyRegistered;   // This flag is activated when the device is already associated. We then don't need to check for Wifi connection state.
    private boolean isPasswordEntered;
    private boolean isDeviceAddingInProgress;    // This flag is used to display alert dialog when Bluetooth communication fails during adding device
    private boolean isWifiConfigInProgress;

    List<PowerSocket> addedDeviceList;
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerViewFoundDevices)
    RecyclerView recyclerView;
    @BindView(R.id.linearLayoutSearchingDevices)
    LinearLayout linearLayoutSearchingDevices;
    @BindView(R.id.linearLayoutNodeviceFound)
    LinearLayout linearLayoutNoDeviceFound;
    private ArrayList<WifiDevice> foundWifiNetworkList;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        isDeviceAlreadyRegistered = false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_add_device_powersocket, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(R.string.frg_add_power_socket_header);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_refresh_icon_white);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);

        powerSocketBLEService = PowerSocketBLEService.getInstance(mActivity.getApplicationContext());

        recyclerView.setHasFixedSize(false);
        RecyclerView.LayoutManager layoutManagerFoundMonitor = new LinearLayoutManager(mActivity);
        recyclerView.setLayoutManager(layoutManagerFoundMonitor);
        recyclerView.setItemAnimator(null);
        foundMonitorAdapter = new FoundPowerSocketAdapter();
        recyclerView.setAdapter(foundMonitorAdapter);

        foundMonitorAdapter.setOnBLEDeviceClickedCallback((powerSocket, itemPosition) -> {
            isDeviceAddingInProgress = true;
            selectedPowerSocket = powerSocket;
            powerSocketBLEService.connect(selectedPowerSocket);
            mActivity.showProgress("Connecting Please Wait", true);
        });

        mActivity.mImageViewAddDevice.setOnClickListener(v -> {
            beginDeviceScan();
            swipeRefreshLayout.setRefreshing(true);
        });

        swipeRefreshLayout = mViewRoot.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::beginDeviceScan);

        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                String advHexString = mVoBluetoothDevices.getDeviceHexData();
                if (advHexString != null && !advHexString.isEmpty()) {
                    byte[] advBytes = ByteConverter.getByteArrayFromHexString(advHexString);
                    // Todo - Check for General Discoverable bit from Adv Flag
                    AdvertisementRecord advRecord = AdvertisementRecord.parse(advBytes);
                    if (advRecord != null) {
                        ManufactureData manufactureData = advRecord.getManufactureData();
                        if (manufactureData != null) {
                            int manufacturerID = ByteConverter.convertByteArrayToInt(manufactureData.id);
                            if (manufacturerID == 0x3200) {
                                PowerSocketAdvData powerSocketAdvData = getGloSmartAdvData(manufactureData);
                                if (powerSocketAdvData != null) {
                                    int opcode = powerSocketAdvData.getOpcode();
                                    // Show only power socket devices
                                    if (powerSocketAdvData.getDeviceType() == 0x400) {
                                        // Show only the devices that are associated/ not associated (ie: 1700 or 3000).
                                        if (opcode == PowerSocketConstants.ASSOCIATED || opcode == PowerSocketConstants.NOT_ASSOCIATED) {
                                            PowerSocket foundPowerSocket = new PowerSocket();
                                            // If the socket is associated, then set this flag to true. Else, false
                                            foundPowerSocket.setBleAddress(mVoBluetoothDevices.getDeviceAddress());
                                            foundPowerSocket.setBleName("Vithamas Socket");
                                            foundPowerSocket.isAssociated = opcode == PowerSocketConstants.ASSOCIATED;
                                            foundPowerSocket.setDeviceType(powerSocketAdvData.getDeviceType());

                                            if (!addedDeviceList.contains(foundPowerSocket)) {
                                                foundMonitorAdapter.addDevice(foundPowerSocket);
                                                swipeRefreshLayout.setRefreshing(false);
                                                showDeviceList();
                                            } else {
                                                foundPowerSocket = null;
                                            }
                                        }
                                    }
                                }
                            }
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
        });

        return mViewRoot;
    }

    private PowerSocketAdvData getGloSmartAdvData(ManufactureData manufactureData) {
        PowerSocketAdvData powerSocketAdvData = null;
        if (manufactureData != null) {
            byte[] data = manufactureData.data;
            if (data != null) {
                // Check if a device is associated
                byte[] selfDeviceIdBytes = ByteConverter.copyOfRange(data, 3, 5);
                int selfDeviceId = ByteConverter.convertByteArrayToInt(selfDeviceIdBytes);
                byte[] encryptionKey = getEncryptionKey(selfDeviceId);
                byte[] originalData = decryptRawByteDataPowerSocket(data, encryptionKey);
                powerSocketAdvData = PowerSocketAdvData.parse(originalData);
                if (powerSocketAdvData != null) {
                    displayDataInLog(powerSocketAdvData, data, originalData);
                }
            } else {
                //
            }
        }
        return powerSocketAdvData;
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
            String encryptionKeyStr = mActivity.mPreferenceHelper.getSecretKey();
            if (encryptionKeyStr != null && !encryptionKeyStr.isEmpty()) {
                encryptionKey = ByteConverter.getByteArrayFromHexString(encryptionKeyStr);
            }
        } else {
            encryptionKey = PowerSocketConstants.defaultKey;
        }
        return encryptionKey;
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

    @Override
    public void onStart() {
        super.onStart();
        addedDeviceList = getDevicesFromDb();
        console.log("asxuabsuixbasuix", new Gson().toJson(addedDeviceList));
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            powerSocketBLEService.setOnPowerSocketEventCallbacks(powerSocketBLEEventCallbacks);
            beginDeviceScan();
        } else {
            mActivity.mUtility.errorDialogWithCallBack("Please enable Bluetooth to enable searching for Power Socket",
                    1, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            mActivity.onBackPressed();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
        }
        console.log("asxjbakjsx_111", "OnStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        console.log("asxjbakjsx_111", "OnStop");
        mActivity.isFromBridgeConnection = true;
        mActivity.mLeDevices = new ArrayList<>();
        mActivity.RescanDevice(false);
        powerSocketBLEService.setOnPowerSocketEventCallbacks(null);
        mActivity.setOnDevicesStatusChange(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        console.log("asxjbakjsx_111", "OnDestroyView");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        console.log("asxjbakjsx_111", "OnDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        console.log("asxjbakjsx_111", "OnDetach");
    }

    @Override
    public void onPause() {
        super.onPause();
        console.log("asxjbakjsx_111", "OnPause");
    }

    @Override
    public void onResume() {
        super.onResume();
        console.log("asxjbakjsx_111", "OnResume");
    }

    PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks = new PowerSocketBLEEventCallbacks() {
        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            if (isVisible()) {
                mActivity.hideProgress();
                if (isDeviceAddingInProgress) {
                    isDeviceAddingInProgress = false;
                    mActivity.mUtility.errorDialog("Device is offline. Please check the device is On and try again", 1, true);
                }
            }
        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {
            if (isVisible()) {
                mActivity.hideProgress();
                if (isDeviceAddingInProgress) {
                    isDeviceAddingInProgress = false;
                    mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.BLE_CONNECTION_FAILED), 1, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            beginDeviceScan();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                }
            }
        }

        @Override
        public void onBluetoothRestart() {
            super.onBluetoothRestart();
            if (isVisible()) {
                mActivity.hideProgress();
                mActivity.mUtility.errorDialogWithCallBack("Device connection failed. Please turn off and turn off Bluetooth and try again.\n\n" +
                        "If the same problems persists, restart the phone and try again.", 1, false, new onAlertDialogCallBack() {
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
        public void onAuthenticationSuccess(String deviceAddress) {
            super.onAuthenticationSuccess(deviceAddress);
            // Check Device Registration Status
            powerSocketBLEService.checkDeviceRegistration(selectedPowerSocket);
        }

        @Override
        public void onAuthenticationFailure(String deviceAddress) {
            super.onAuthenticationFailure(deviceAddress);
            if (isVisible()) {
                if (isDeviceAddingInProgress) {
                    isDeviceAddingInProgress = false;

                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialogWithCallBack("Authentication failed.", 1, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            powerSocketBLEService.disconnect(deviceAddress);
                            beginDeviceScan();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                }
            }
        }

        @Override
        public void onDeviceAlreadyRegistered(String deviceAddress) {
            super.onDeviceAlreadyRegistered(deviceAddress);
            mActivity.hideProgress();
            isDeviceAlreadyRegistered = true;
            showPowerSocketNameEditDialog();
        }

        @Override
        public void onDeviceNotRegistered(String deviceAddress) {
            super.onDeviceNotRegistered(deviceAddress);
            powerSocketBLEService.addPowerSocket(selectedPowerSocket);
        }

        @Override
        public void onDeviceAddedSuccessfully(PowerSocket powerSocket) {
            super.onDeviceAddedSuccessfully(powerSocket);
            mActivity.hideProgress();
            selectedPowerSocket = powerSocket;
            showPowerSocketNameEditDialog();
        }

        @Override
        public void onDeviceAddingFailed(String deviceAddress) {
            super.onDeviceAddingFailed(deviceAddress);
            if (isVisible()) {
                if (isDeviceAddingInProgress) {
                    isDeviceAddingInProgress = false;
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialogWithCallBack("Cannot add device. Please try again.", 1, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            powerSocketBLEService.disconnect(deviceAddress);
                            beginDeviceScan();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                }
            }
        }

        @Override
        public void onWifiConfigured(String deviceAddress) {
            super.onWifiConfigured(deviceAddress);
            mActivity.hideProgress();
            selectedPowerSocket.isWifiConfigured = true;
            console.log("asuxbauisbxuiasbx", new Gson().toJson(selectedPowerSocket));
            saveDeviceData(selectedPowerSocket);

            //No need to display alert dialog, if power socket was already connected to Wi-Fi network
            if (!isDeviceAlreadyRegistered) {
                mActivity.mUtility.errorDialogWithCallBack("Wifi configuration successful",
                        0, false, dialogCallBack);
            } else {
                // Reset this flag, so the flag can be used when adding another power socket.
                isDeviceAlreadyRegistered = false;
//                navigateBack();
            }
        }

        @Override
        public void onWifiNotConfigured(String deviceAddress) {
            super.onWifiNotConfigured(deviceAddress);
            mActivity.hideProgress();
            if (isPasswordEntered) {
                isPasswordEntered = false;
                mActivity.mUtility.errorDialogWithCallBack("Wi-Fi not configured. Please try again", 1, true, dialogCallBack);
            } else {
                mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.Wifi_Config_Dialog_Titile),
                        getResources().getString(R.string.Wifi_Config_Dialog), "Yes", "No",
                        false, 3, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                mActivity.showProgress("Searching for available Wi-Fi", false);
                                powerSocketBLEService.scanWifiDevices(selectedPowerSocket);
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                // Show Alert Dialog that the device is only controlled by Bluetooth.
                                showBluetoothControlOnlyErrorDialog();
                            }
                        });
            }
        }

        @Override
        public void onWifiConfigFailed(String deviceAddress) {
            super.onWifiConfigFailed(deviceAddress);
            if (isDeviceAddingInProgress) {
                isDeviceAddingInProgress = false;
                mActivity.hideProgress();
                mActivity.mUtility.errorDialogWithCallBack("Wifi configuration failed. Please try again",
                        1, false, dialogCallBack);
            }
        }

        @Override
        public void onWifiMqttNotSubscribed(String deviceAddress) {
            super.onWifiMqttNotSubscribed(deviceAddress);
            mActivity.hideProgress();
            mActivity.mUtility.errorDialogWithCallBack("Connected to MQTT, but not configured",
                    1, false, dialogCallBack);
        }

        @Override
        public void onWifiDeviceListFound(String deviceAddress, ArrayList<WifiDevice> wifiDeviceList) {
            super.onWifiDeviceListFound(deviceAddress, wifiDeviceList);
            if (isVisible()) {
                mActivity.hideProgress();
                foundWifiNetworkList = wifiDeviceList;
                Bundle wifiBundleList = new Bundle();
                wifiBundleList.putSerializable(getResources().getString(R.string.Wifi_list), foundWifiNetworkList);
                mActivity.hideProgress();
                showWifiDialogFragment(wifiBundleList);
            }
        }

        @Override
        public void onWifiDevicesNotFound(String deviceAddress) {
            super.onWifiDevicesNotFound(deviceAddress);
            if (isVisible()) {
                mActivity.hideProgress();
                mActivity.mUtility.errorDialogWithYesNoCallBack("Wi-Fi network not found",
                        " Do you want to search for Wi-Fi networks again?", "Yes", "Cancel", false,
                        3, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                mActivity.showProgress("Searching for available Wi-Fi", false);
                                powerSocketBLEService.scanWifiDevices(selectedPowerSocket);
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                isDeviceAddingInProgress = false;
                                // Show Alert Dialog that the device is only controlled by Bluetooth.
                                showBluetoothControlOnlyErrorDialog();
                            }
                        }
                );
            }
        }
    };

    onAlertDialogCallBack deviceAddedAlertDialogCallback = new onAlertDialogCallBack() {
        @Override
        public void PositiveMethod(DialogInterface dialog, int id) {
            powerSocketBLEService.checkDeviceWifiConfigStatus(selectedPowerSocket);
        }

        @Override
        public void NegativeMethod(DialogInterface dialog, int id) {

        }
    };

    private void showBluetoothControlOnlyErrorDialog() {
        mActivity.mUtility.errorDialogWithCallBack("Device can only be controlled over Bluetooth.\n " +
                        "You can configure Wi-Fi by selecting on the Vithamas Socket settings when necessary.",
                3, false, dialogCallBack);
    }

    private void navigateBack() {
        mActivity.onBackPressed();
    }

    onAlertDialogCallBack dialogCallBack = new onAlertDialogCallBack() {
        @Override
        public void PositiveMethod(DialogInterface dialog, int id) {
            navigateBack();
        }

        @Override
        public void NegativeMethod(DialogInterface dialog, int id) {

        }
    };

    private void displayDataInLog(PowerSocketAdvData powerSocketAdvData, byte[] manufacturerData, byte[] originalData) {
        console.log("asxasxas1111", "Raw Manufacturer Data = " + ByteConverter.getHexStringFromByteArray(manufacturerData, true));
        console.log("asxasxas1111", "Original Data = " + ByteConverter.getHexStringFromByteArray(originalData, true));
        console.log("asxasxas1111", "TTL = " + powerSocketAdvData.getTtl());
        console.log("asxasxas1111", "Sequence Number = " + powerSocketAdvData.getSequenceNumber());
        console.log("asxasxas1111", "Self Device ID = " + powerSocketAdvData.getSelfDeviceId());
        console.log("asxasxas1111", "Device ID = " + powerSocketAdvData.getDeviceId());
        console.log("asxasxas1111", "CRC = " + powerSocketAdvData.getCrc());
        console.log("asxasxas1111", "Opcode = " + powerSocketAdvData.getOpcode());
        console.log("asxasxas1111", "Device Addr = " + powerSocketAdvData.getDeviceAddress());
        console.log("asxasxas1111", "Device Type = " + powerSocketAdvData.getDeviceType());
    }

    private void showWifiDialogFragment(Bundle bundle) {
        WifiDialogFragment wifiDialogFragment = new WifiDialogFragment();
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mActivity.getSupportFragmentManager().findFragmentByTag(getResources().getString(R.string.Wifi_Dialog_Fragment_DialogTag_1));
        if (prev != null) {
            ft.remove(prev);
        }
        wifiDialogFragment.setArguments(bundle);
        wifiDialogFragment.show(mActivity.getSupportFragmentManager(), getResources().getString(R.string.Wifi_Dialog_Fragment_DialogTag_1));
        wifiDialogFragment.setUpDialogListenerWifi(new WifiDialogFragment.DialogFragmentWifiListener() {
            @Override
            public void onWifiDeviceClicked(WifiDevice wifiDevice, int position) {
                showWifiConfigDialog(wifiDevice);
            }

            @Override
            public void onCancelled() {
                // Show Alert Dialog that the device is only controlled by Bluetooth.
                showBluetoothControlOnlyErrorDialog();
            }
        });
    }

    public void showWifiConfigDialog(WifiDevice wifiDevice) {
        WifiConfigAlertDialog dialogFragment = new WifiConfigAlertDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(WifiConfigAlertDialog.WifiDeviceLabel, wifiDevice);
        dialogFragment.setArguments(bundle);

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mActivity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        dialogFragment.show(mActivity.getSupportFragmentManager(), "dialog");
        dialogFragment.setDialogListener(new WifiConfigAlertDialog.DialogListener() {
            @Override
            public void onFinishEditDialog(WifiDevice wifiDevice, String wifiPassword, boolean dialogCancelFlag) {
                mActivity.showProgress("Configuring Wi-Fi...", false);
                isPasswordEntered = true;

                // Set WifiSSID Index
                powerSocketBLEService.configureWifi(selectedPowerSocket, wifiDevice, wifiPassword);
            }

            @Override
            public void onCancelled() {
                Bundle wifiBundleList = new Bundle();
                wifiBundleList.putSerializable(getResources().getString(R.string.Wifi_list), foundWifiNetworkList);
                mActivity.hideProgress();
                showWifiDialogFragment(wifiBundleList);
            }
        });
    }

    private void showPowerSocketNameEditDialog() {
        DeviceNameEditAlertDialog dialogFragment = new DeviceNameEditAlertDialog();
        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mActivity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(mActivity.getSupportFragmentManager(), "dialog");

        dialogFragment.setDialogListener((powerSocketName, dialogCancelFlag) -> {
            if (!powerSocketName.isEmpty()) {
                String nameAlreadyExists = checkDeviceNameExistInDeviceDB(powerSocketName);
                if (nameAlreadyExists.equalsIgnoreCase("-1")) {
                    selectedPowerSocket.bleName = powerSocketName;
                    saveDeviceData(selectedPowerSocket);
                } else {
                    showPowerSocketNameEditDialog();
                    Toast.makeText(getContext(), "This name already exists for one of the devices. Please set different name", Toast.LENGTH_LONG).show();
                }
            } else {
                showPowerSocketNameEditDialog();
                Toast.makeText(getContext(), "Please type a valid name for the device", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveDeviceData(PowerSocket powerSocket) {
        // Check if a record exists in DB
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(DBHelper.mFieldDeviceServerId, "");
        mContentValues.put(DBHelper.mFieldDeviceUserId, mActivity.mPreferenceHelper.getUserId());
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
        String deviceTypeName = mActivity.mDbHelper.getQueryResult(queryDeviceType);

        mContentValues.put(DBHelper.mFieldDeviceTypeName, deviceTypeName);
        mContentValues.put(DBHelper.mFieldConnectStatus, "YES");
        mContentValues.put(DBHelper.mFieldSwitchStatus, "ON");
        mContentValues.put(DBHelper.mFieldDeviceIsFavourite, "0");
        mContentValues.put(DBHelper.mFieldDeviceLastState, "0");
        mContentValues.put(DBHelper.mFieldDeviceTimeStamp, System.currentTimeMillis());
        mContentValues.put(DBHelper.mFieldDeviceIsActive, "1");
        mContentValues.put(DBHelper.mFieldDeviceCreatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));
        mContentValues.put(DBHelper.mFieldDeviceUpdatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));
        mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");

        boolean isWifiConfigured = powerSocket.isWifiConfigured;
        int isWifiConfigState = 0;
        if (isWifiConfigured) {
            isWifiConfigState = 1;
        }
        mContentValues.put(DBHelper.mFieldDeviceIsWifiConfigured, isWifiConfigState);

        // Check if a record exists in Database
        String localDeviceId = checkRecordExistInDeviceDB(formattedBLEAddress);
        if (localDeviceId.equalsIgnoreCase("-1")) {  // Insert new record
            // For Login User
            if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
                addDeviceAPI(mContentValues, false);
            } else { // For Skip User
                // Store device data in Local SQLite Database
                mActivity.mDbHelper.insertRecord(DBHelper.mTableDevice, mContentValues);
                saveSocketData();
                showDeviceAddAlert();
            }
        } else { // Update existing record
            // For Login User
            if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
                mContentValues.put(DBHelper.mFieldDeviceLocalId, localDeviceId);
                addDeviceAPI(mContentValues, true);
            } else { // For Skip User
                // Update existing device data in Local SQLite Database
                mContentValues.put(DBHelper.mFieldDeviceUpdatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));
                mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues,
                        DBHelper.mFieldDeviceLocalId + "=?", new String[]{localDeviceId});
            }
        }

        //else { // Update existing record
//            String queryDeviceName = "select " + DBHelper.mFieldDeviceName + " from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceLocalId + "= '" + localDeviceId + "'";
//            String deviceName = mActivity.mDbHelper.getQueryResult(queryDeviceName);
//            mContentValues.put(DBHelper.mFieldDeviceLocalId, localDeviceId);
//            mContentValues.put(DBHelper.mFieldDeviceName, deviceName);
//            mContentValues.put(DBHelper.mFieldDeviceUpdatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));
//
//            // For Login User
//            if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
//                // API is required to add/update a device for Login user
//                addDeviceAPI(mContentValues);
//            } else { // For Skip User
//                // This is for Skip User. Add/update device locally in Android phone database.
//                // This flag is set when user types a new device name and clicks on Save button or Cancels by clicking on Cancel button.
//                // The device name will be updated in local Android phone database.
//                if (isAddingDeviceInProgress) {
//                    // Show Device successfully added alert dialog after user types new device name.
//                    // Show device added successfully Alert dialog
//                    showDeviceAddAlert();
//                } else {
//                    if (!isDeviceAdded) {
//                        // Show Entry field dialog when the device is added.
////                        showPowerSocketNameEditDialog();
//                    }
//                }
//            }
//        }
    }

    private void saveSocketData() {
        String formattedBLEAddress = selectedPowerSocket.bleAddress.toUpperCase();
        formattedBLEAddress = formattedBLEAddress.replace(":", "");

        String localDeviceId = checkRecordExistInDeviceDB(formattedBLEAddress);

        console.log("kbakj_DeviceId", localDeviceId);
        console.log("kbakj_DeviceType", selectedPowerSocket.deviceType);

        int deviceType = selectedPowerSocket.deviceType;
        if (deviceType == 0x400) {
            for (int i = 0; i < 6; i++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(DBHelper.mFieldTableSocketDeviceDtlSocketId, i);
                contentValues.put(DBHelper.mFieldTableSocketDeviceDtlSocketName, "Socket - " + (i+1));
                contentValues.put(DBHelper.mFieldTableSocketDeviceDtlImageType, 0);
                contentValues.put(DBHelper.mFieldTableSocketDeviceDtlDeviceId, Integer.parseInt(localDeviceId));
                mActivity.mDbHelper.insertRecord(DBHelper.mTableSocketDeviceDtl, contentValues);
                console.log("skjbskjbskjs","inserted");
            }
        }
    }

    private void showDeviceAddAlert() {
        // Show an alert dialog that device is added successfully
        mActivity.mUtility.errorDialogWithCallBack("Device added successfully.",
                0, false, deviceAddedAlertDialogCallback);
    }

    public String checkRecordExistInDeviceDB(String bleAddress) {
        if (bleAddress != null && !bleAddress.equalsIgnoreCase("") && !bleAddress.equalsIgnoreCase("null")) {
            bleAddress = bleAddress.toUpperCase();
        }

        DataHolder mDataHolder;
        String url = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceBleAddress + "= '" + bleAddress + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Check record exist or not in device table*/
    public String checkDeviceNameExistInDeviceDB(String deviceName) {
        deviceName = deviceName.replace("'", "''");
        deviceName = deviceName.replace("\"", "\\\"");
        DataHolder mDataHolder;
        String url = "select " + mActivity.mDbHelper.mFieldDeviceName + " from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceName + "= '" + deviceName + "'" + " AND " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldDeviceIsActive + "= 1";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldDeviceName);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    // Todo handle network request failure
    /*Call api and save device data to server*/
    public void addDeviceAPI(final ContentValues mContentValuesParam, boolean isUpdate) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mUtility.ShowProgress("Adding device. Please Wait..");
        Map<String, String> params = new HashMap<String, String>();
        String mStringDeviceHex = mContentValuesParam.get(DBHelper.mFieldDeviceCommHexId).toString();
        params.put("device_token", mActivity.mPreferenceHelper.getDeviceToken());
        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
        params.put("device_id", mContentValuesParam.get(DBHelper.mFieldDeviceCommID).toString());
        params.put("hex_device_id", mStringDeviceHex.toLowerCase());
        params.put("device_name", mContentValuesParam.get(DBHelper.mFieldDeviceName).toString());
        String queryDeviceTypeId = "select " + DBHelper.mFieldDeviceTypeServerID + " from " + DBHelper.mTableDeviceType + " where " +
                DBHelper.mFieldDeviceTypeType + "= '" + mContentValuesParam.get(DBHelper.mFieldDeviceTypeType).toString() + "'";
        String deviceTypeServerID = mActivity.mDbHelper.getQueryResult(queryDeviceTypeId);
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
        console.log("abxuuibxwu", String.valueOf(mContentValuesParam.get(DBHelper.mFieldDeviceIsWifiConfigured)));
        params.put("wifi_configured", String.valueOf(mContentValuesParam.get(DBHelper.mFieldDeviceIsWifiConfigured)));

        console.log("cksdjbcjksdjkc_isUpdate", isUpdate);

        Call<VoAddDeviceData> mLogin = mActivity.mApiService.addDeviceAPI(params);
        mLogin.enqueue(new Callback<VoAddDeviceData>() {
            @Override
            public void onResponse(Call<VoAddDeviceData> call, Response<VoAddDeviceData> response) {
                mActivity.mUtility.HideProgress();
                console.log("sdciusdbcuisbc1", "here");
                if (!isRemoving()) {
                    console.log("sdciusdbcuisbc2", "here");
                    VoAddDeviceData mAddDeviceAPI = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mAddDeviceAPI);
                    console.log("skbsubsusiusbusi", json);
                    if (mAddDeviceAPI != null && mAddDeviceAPI.getResponse().equalsIgnoreCase("true")) {
                        if (mAddDeviceAPI.getData() != null) {
                            console.log("cksdjbcjksdjkc_isUpdate", isUpdate);
                            if (isUpdate) {
                                console.log("cksdjbcjksdjkc_localId", mContentValuesParam.get(DBHelper.mFieldDeviceLocalId).toString());
                                console.log("cksdjbcjksdjkc_wifiConfigured", mContentValuesParam.get(DBHelper.mFieldDeviceIsWifiConfigured));

                                mContentValuesParam.put(DBHelper.mFieldDeviceUpdatedAt, DateUtils.getFormattedToday("yyyy-MM-dd HH:mm:ss"));
                                mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValuesParam,
                                        DBHelper.mFieldDeviceLocalId + "=?",
                                        new String[]{mContentValuesParam.get(DBHelper.mFieldDeviceLocalId).toString()});
                                navigateBack();
                            } else {
                                mContentValuesParam.put(DBHelper.mFieldDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                                mContentValuesParam.put(DBHelper.mFieldDeviceIsSync, "1");
                                mActivity.mDbHelper.insertRecord(DBHelper.mTableDevice, mContentValuesParam);

                                mActivity.runOnUiThread(() -> {
                                    showDeviceAddAlert();
                                });
                            }
//                            // This flag is set when user types a new device name and clicks on Save button or Cancels by clicking on Cancel button.
//                            // The device name will be updated in local Android phone database.
//                            if (isAddingDeviceInProgress) {
//                                // Show Device successfully added alert dialog after user types new device name.
//                                // Show device added successfully Alert dialog
//                                showDeviceAddAlert();
//                            } else {
//                                if (!isDeviceAdded) {
//                                    // Show Entry field dialog when the device is added.
////                                    showPowerSocketNameEditDialog();
//                                }
//                            }
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
                mActivity.mUtility.HideProgress();
                if (isAdded()) {
//                checkAdapterIsEmpty();
                    showDeviceAddAlert();
                }
                t.printStackTrace();
            }
        });
    }

    private void showDeviceList() {
        if (!isDetached()) {
            recyclerView.setVisibility(View.VISIBLE);
            linearLayoutNoDeviceFound.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.GONE);
        }
    }

    private void showNoDevicesLayout() {
        if (!isDetached()) {
            recyclerView.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.GONE);
            linearLayoutNoDeviceFound.setVisibility(View.VISIBLE);
        }
    }

    private void showSearchingDevicesLayout() {
        if (!isDetached()) {
            recyclerView.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.VISIBLE);
            linearLayoutNoDeviceFound.setVisibility(View.GONE);
        }
    }

    private void beginDeviceScan() {
        if (!isDetached()) {
            foundMonitorAdapter.clear();
            showSearchingDevicesLayout();
            swipeRefreshLayout.setRefreshing(true);
            mActivity.isFromBridgeConnection = true;
        }
    }

    private List<PowerSocket> getDevicesFromDb() {
        List<PowerSocket> retVal = new ArrayList<>();
        try {
            DataHolder dataHolder;
//            String query = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceIsActive + "= '1'" + " AND " +
//                    DBHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " +
//                    DBHelper.mFieldDeviceType + "= '" + "0400" + "'" + " ORDER BY " +
//                    DBHelper.mFieldDeviceIsFavourite + " desc";

            String query = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceIsActive + "= '1'" + " AND " +
                    DBHelper.mFieldDeviceType + "= '" + "0400" + "'" + " ORDER BY " +
                    DBHelper.mFieldDeviceIsFavourite + " desc";

            console.log("akcbsuausbcukacb", "Query = " + query);

            dataHolder = mActivity.mDbHelper.readData(query);
            if (dataHolder != null) {
                VoDeviceList device;
                for (int i = 0; i < dataHolder.get_Listholder().size(); i++) {
                    device = new VoDeviceList();
                    device.setDevicLocalId(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceLocalId));
                    device.setDeviceServerid(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceServerId));
                    device.setUser_id(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceUserId));
                    device.setDevice_Comm_id(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommID));
                    device.setDevice_Comm_hexId(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommHexId));
                    device.setDevice_name(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceName));
                    device.setDevice_realName(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceRealName));
                    device.setDevice_BleAddress(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceBleAddress).toUpperCase());
                    device.setDevice_Type(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceType));
                    device.setDevice_type_name(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceTypeName));
                    device.setDevice_ConnStatus(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldConnectStatus));
                    device.setDevice_brightness(Integer.parseInt(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceBrightness)));
                    device.setDevice_rgb_color(Integer.parseInt(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceColor)));
                    device.setDevice_SwitchStatus(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldSwitchStatus));
                    device.setDevice_is_favourite(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsFavourite));
                    device.setDevice_last_state_remember(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceLastState));
                    device.setDevice_timestamp(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceTimeStamp));
                    device.setDevice_is_active(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsActive));
                    device.setDevice_created_at(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceCreatedAt));
                    device.setDevice_updated_at(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceUpdatedAt));
                    device.setDevice_is_sync(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsSync));
                    device.setIsWifiConfigured(Integer.parseInt(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsWifiConfigured)));
                    device.setSocketState(Integer.parseInt(dataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceSocketState)));

                    if (device.getDevice_SwitchStatus() != null &&
                            device.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                        device.setIsChecked(true);
                    } else {
                        device.setIsChecked(false);
                    }

                    PowerSocket powerSocket = new PowerSocket(device);
                    retVal.add(powerSocket);
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
        return retVal;
    }
}