package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.evergreen.ble.advertisement.AdvertisementRecord;
import com.evergreen.ble.advertisement.ManufactureData;
import com.vithamastech.smartlight.BaseFragment.BaseFragment;
import com.vithamastech.smartlight.DialogFragementHelper.DeviceNameEditAlertDialog;
import com.vithamastech.smartlight.DialogFragementHelper.EditTextDialog;
import com.vithamastech.smartlight.DialogFragementHelper.WifiConfigAlertDialog;
import com.vithamastech.smartlight.LoginActivity;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.PowerSocketUtils.Constants;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketAdvData;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketConstants;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.OnBluetoothStateChangeListener;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.vithamastech.smartlight.services.PowerSocketBLEEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketBLEService;
import com.vithamastech.smartlight.services.PowerSocketMQTTEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketMQTTService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.vithamastech.smartlight.MainActivity.mBluetoothManager;
import static com.vithamastech.smartlight.db.DBHelper.mTableAlarmPowerSocket;
import static com.vithamastech.smartlight.db.DBHelper.mfield_ble_address;

// Todo handle situation when user doesn't turn on the device after reset operation is complete
// Todo merge remove device from database functions
// Todo code cleanup
public class FragmentPowerSocketSetting extends BaseFragment {
    public static final String TAG = FragmentPowerSocketSetting.class.getSimpleName();
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    PowerSocketBLEService powerSocketBLEService;
    PowerSocketMQTTService powerSocketMQTTService;
    BluetoothAdapter bluetoothAdapter;
    PowerSocket selectedPowerSocket;
    boolean isPowerSocketDeleting;
    private Handler handler;
    private Runnable timeoutRunnable;
    private boolean isFirstStepResetComplete;
    private boolean isPowerSocketResetInProgress;
    private boolean isFirmwareVersionRequestInProgress;
    private boolean isFactorySocketTestInProgress;
    private String selectedFirmwareVersion;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        selectedPowerSocket = MainActivity.mActivityPowerSocketSelected;
        handler = new Handler();
        timeoutRunnable = this::beginDeviceScan;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_socket_setting, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        powerSocketBLEService = PowerSocketBLEService.getInstance(mActivity.getApplicationContext());
        powerSocketMQTTService = PowerSocketMQTTService.getInstance(mActivity.getApplicationContext());

        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mActivity.mImageViewAddDevice.setVisibility(View.INVISIBLE);
        mActivity.mTextViewAdd.setVisibility(View.VISIBLE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        return mViewRoot;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @OnClick(R.id.wifiSetting_layout)
    public void wifiClickLayout() {
        FragmentPowerSocketWifiSettings fragmentPowerSocketWifiSettings = new FragmentPowerSocketWifiSettings();
        mActivity.replacesFragment(fragmentPowerSocketWifiSettings, true, null, 0);
    }

    // Todo
    @OnClick(R.id.delete_device)
    public void deleteDeviceLayout() {
        mActivity.mUtility.errorDialogWithYesNoCallBack("Delete Device",
                "Are yo sure you want to delete this device?", "Yes", "No",
                false, 3, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        // Check if it is Login User or a Skip User
                        if (!mActivity.mPreferenceHelper.getIsSkipUser()) { // Login User
                            if (mActivity.mUtility.haveInternet()) {
                                checkAuthenticationAPI(true);
                            } else {
                                isPowerSocketDeleting = false;
                                mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_device_not_delete_without_internet), 1, true);
                            }
                        } else {        // Skip User
                            deletePowerSocket(selectedPowerSocket);
                        }
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
    }

    @OnClick(R.id.reset_device)
    public void resetDevice() {
        mActivity.mUtility.errorDialogWithYesNoCallBack("Reset Device",
                "Are yo sure you want to reset this device?", "Yes", "No",
                false, 3, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        if (bluetoothAdapter.isEnabled()) {
                            isPowerSocketResetInProgress = true;
                            mActivity.showProgress("Device is being reset. Please wait...", false);
                            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                                beginResetDevice();
                            } else {
                                powerSocketBLEService.connect(selectedPowerSocket);
                            }
                        } else {
                            mActivity.mUtility.errorDialog("Bluetooth is off. Please switch on Bluetooth and try again.", 2, true);
                        }
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
    }

    // BLE Operation
    @OnClick(R.id.hardwareVersion)
    public void getFirmwareVersion() {
        if (bluetoothAdapter.isEnabled()) {
            isFirmwareVersionRequestInProgress = true;
            mActivity.showProgress("Requesting firmware version. Please wait...", false);
            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.getCurrentFirmwareVersion(selectedPowerSocket);
            } else {
                powerSocketBLEService.connect(selectedPowerSocket);
            }
        } else {
            mActivity.mUtility.errorDialog("Bluetooth is off. Please switch on Bluetooth and try again.", 2, true);
        }
    }

    // BLE Operation
    @OnClick(R.id.factoryTest)
    public void factoryTest() {
        if (bluetoothAdapter.isEnabled()) {
            isFactorySocketTestInProgress = true;
            mActivity.showProgress("Socket test is in progress. Please wait...", false);
            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.performFactoryResetTest(selectedPowerSocket);
            } else {
                powerSocketBLEService.connect(selectedPowerSocket);
            }
        } else {
            mActivity.mUtility.errorDialog("Bluetooth is off. Please switch on Bluetooth and try again.", 2, true);
        }
    }

    // MQTT Operation
    @OnClick(R.id.otaUpdate)
    public void updateOTA() {
        // Display Edit Text Dialog
        showFirmwareVersionEntryDialog();
    }

    private void showFirmwareVersionEntryDialog() {
        EditTextDialog dialogFragment = new EditTextDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(EditTextDialog.messageKey, "Please enter firmware version to update");
        bundle.putSerializable(EditTextDialog.textHintKey, "Firmware Version");
        bundle.putSerializable(EditTextDialog.positiveButtonNameKey, "Update");
        bundle.putSerializable(EditTextDialog.negativeButtonNameKey, "Cancel");
        dialogFragment.setArguments(bundle);

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mActivity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(mActivity.getSupportFragmentManager(), "editTextDialog");

        dialogFragment.setDialogListener(new EditTextDialog.DialogListener() {
            @Override
            public void onFinishEditDialog(String firmwareVersion, boolean dialogCancelFlag) {
                if (!firmwareVersion.isEmpty() && firmwareVersion.matches("^\\d+\\.\\d+")) {
                    selectedFirmwareVersion = firmwareVersion;
                    if (selectedPowerSocket.isWifiConfigured && powerSocketMQTTService.isConnected()) {
                        mActivity.mUtility.ShowProgress("OTA Update is in progress. Please wait...");
                        powerSocketMQTTService.subscribeOTA(selectedPowerSocket);
                    } else {
                        mActivity.mUtility.errorDialog("Network unavailable. Please enable internet connection and try again",
                                1, true);
                    }
                } else {
                    Toast.makeText(getContext(), "Please enter valid firmware version", Toast.LENGTH_SHORT).show();
                    showFirmwareVersionEntryDialog();
                }
            }

            @Override
            public void onCancelEditDialog() {

            }
        });
    }

    private void beginResetDevice() {
        selectedPowerSocket.isAssociated = false;
        powerSocketBLEService.resetPowerSocket(selectedPowerSocket);
    }

    PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks = new PowerSocketBLEEventCallbacks() {
        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            if (isPowerSocketResetInProgress) {
                isPowerSocketResetInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Something went wrong. Please try again", 2, true);
                }
            } else if (isFirmwareVersionRequestInProgress) {
                isFirmwareVersionRequestInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Something went wrong. Please try again", 2, true);
                }
            } else if (isFactorySocketTestInProgress) {
                isFactorySocketTestInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Something went wrong. Please try again", 2, true);
                }
            }
        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    mActivity.hideProgress();

                    if (powerSocketMQTTService.isConnected()) {
                        powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                    }

                    checkScanDeviceList();
                }
            } else if (isPowerSocketResetInProgress) {
                isPowerSocketResetInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Something went wrong. Please try again", 2, true);
                }
            } else if (isFirmwareVersionRequestInProgress) {
                isFirmwareVersionRequestInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Something went wrong. Please try again", 2, true);
                }
            } else if (isFactorySocketTestInProgress) {
                isFactorySocketTestInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Something went wrong. Please try again", 2, true);
                }
            }
        }

        @Override
        public void onAuthenticationSuccess(String deviceAddress) {
            super.onAuthenticationSuccess(deviceAddress);
            if (isPowerSocketResetInProgress) {
                beginResetDevice();
            } else if (isFirmwareVersionRequestInProgress) {
                powerSocketBLEService.getCurrentFirmwareVersion(selectedPowerSocket);
            } else if (isFactorySocketTestInProgress) {
                powerSocketBLEService.performFactoryResetTest(selectedPowerSocket);
            }
        }

        @Override
        public void onAuthenticationFailure(String deviceAddress) {
            super.onAuthenticationFailure(deviceAddress);
            if (isPowerSocketResetInProgress) {
                isPowerSocketResetInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Auth failure", 2, true);
                }
            } else if (isFirmwareVersionRequestInProgress) {
                isFirmwareVersionRequestInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Auth failure", 2, true);
                }
            } else if (isFactorySocketTestInProgress) {
                isFactorySocketTestInProgress = false;
                if (isVisible()) {
                    mActivity.hideProgress();
                    mActivity.mUtility.errorDialog("Auth failure", 2, true);
                }
            }

            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
            }
        }

        @Override
        public void onDeviceRemoved(String deviceAddress) {
            super.onDeviceRemoved(deviceAddress);
            isPowerSocketDeleting = false;
            mActivity.hideProgress();

            if (powerSocketMQTTService.isConnected()) {
                powerSocketMQTTService.unsubscribe(selectedPowerSocket);
            }

            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
            }

            checkScanDeviceList();
        }

        @Override
        public void onDeviceRemovalFailed(String deviceAddress) {
            super.onDeviceRemovalFailed(deviceAddress);
            isPowerSocketDeleting = false;
            mActivity.hideProgress();

            if (powerSocketMQTTService.isConnected()) {
                powerSocketMQTTService.unsubscribe(selectedPowerSocket);
            }

            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
            }

            checkScanDeviceList();
        }

        @Override
        public void onDeviceResetCompleted(String deviceAddress) {
            super.onDeviceResetCompleted(deviceAddress);
            isPowerSocketResetInProgress = false;
            isFirstStepResetComplete = true;
            powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
            mActivity.hideProgress();

            // Show Dialog to user to switch off and switch on the device within 15 sec
            // Start 15sec Power off/on timeout
            mActivity.stopScan();
            handler.postDelayed(timeoutRunnable, 15000);

            handler.post(() -> mActivity.showProgress("Please turn off and turn on the device within 15 seconds", false));
        }

        @Override
        public void onDeviceResetFailed(String deviceAddress) {
            super.onDeviceResetFailed(deviceAddress);
            mActivity.hideProgress();
            mActivity.mUtility.errorDialogWithCallBack("Device Reset failed. Please try again.", 2, false, new onAlertDialogCallBack() {
                @Override
                public void PositiveMethod(DialogInterface dialog, int id) {
                    beginDeviceScan();
                }

                @Override
                public void NegativeMethod(DialogInterface dialog, int id) {

                }
            });
        }

        @Override
        public void onReceivedCurrentFirmwareVersion(String deviceAddress, String firmwareVersion) {
            super.onReceivedCurrentFirmwareVersion(deviceAddress, firmwareVersion);
            isPowerSocketResetInProgress = false;
            mActivity.mUtility.HideProgress();
            if (isVisible()) {
                mActivity.mUtility.errorDialog("Firmware version = " + firmwareVersion, 0, true);
            }
        }

        @Override
        public void onFactorySocketTestSuccessful(String deviceAddress) {
            super.onFactorySocketTestSuccessful(deviceAddress);
            mActivity.mUtility.HideProgress();
            if (isVisible()) {
                mActivity.mUtility.errorDialog("Factory socket test successful", 0, true);
            }
        }

        @Override
        public void onFactorySocketTestFailed(String deviceAddress) {
            super.onFactorySocketTestFailed(deviceAddress);
            mActivity.mUtility.HideProgress();
            if (isVisible()) {
                mActivity.mUtility.errorDialog("Factory socket test failed. Please try again", 1, true);
            }
        }
    };

    PowerSocketMQTTEventCallbacks powerSocketMQTTEventCallbacks = new PowerSocketMQTTEventCallbacks() {
        @Override
        public void onMQTTConnected() {
            if (selectedPowerSocket.isWifiConfigured) {
                powerSocketMQTTService.subscribe(selectedPowerSocket);
            }
        }

        @Override
        public void onMQTTDisconnected() {
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    mActivity.hideProgress();

                    if (powerSocketMQTTService.isConnected()) {
                        powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                    }

                    if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                        powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                    }

                    checkScanDeviceList();
                }
            }
        }

        @Override
        public void onOTASubscribed(String deviceAddress) {
            super.onOTASubscribed(deviceAddress);
            if (powerSocketMQTTService.isConnected()) {
                if (selectedFirmwareVersion != null && !selectedFirmwareVersion.isEmpty()) {
                    powerSocketMQTTService.performOTAUpdate(selectedPowerSocket, selectedFirmwareVersion);
                } else {
                    mActivity.mUtility.errorDialog("Internet not available. Please connect to internet and try again", 2, true);
                }
            }
        }

        @Override
        public void onMQTTConnectionFailed() {

        }

        @Override
        public void onMQTTException() {
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    mActivity.hideProgress();

                    if (powerSocketMQTTService.isConnected()) {
                        powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                    }

                    if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                        powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                    }

                    checkScanDeviceList();
                }
            }
        }

        @Override
        public void onMQTTTimeout(String deviceAddress) {
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                if (isVisible()) {
                    mActivity.hideProgress();

                    if (powerSocketMQTTService.isConnected()) {
                        powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                    }

                    if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                        powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                    }

                    checkScanDeviceList();
                }
            }
        }

        @Override
        public void onDeviceRemoved(String deviceAddress) {
            super.onDeviceRemoved(deviceAddress);
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                mActivity.hideProgress();
                powerSocketBLEService.cancelDeleteDeviceTimeout();

                if (powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                }

                if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                    powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                }

                checkScanDeviceList();
            } else {
                String spannableText = "The power socket " + selectedPowerSocket.getBleName() + " may have been deleted from another phone. " +
                        "Please add the selected power socket again.";
                SpannableString spannableString = new SpannableString(spannableText);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 17, 17 + selectedPowerSocket.getBleName().length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                mActivity.mUtility.errorDialogWithCallBack(spannableString, 1, false, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        if (powerSocketMQTTService.isConnected()) {
                            powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                        }

                        if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                            powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                        }

                        checkScanDeviceList();
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
            if (isPowerSocketDeleting) {
                isPowerSocketDeleting = false;
                mActivity.hideProgress();
                powerSocketBLEService.cancelDeleteDeviceTimeout();

                if (powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                }

                if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                    powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                }

                checkScanDeviceList();
            } else {
                String spannableText = "The power socket " + selectedPowerSocket.getBleName() + " may have been deleted from another phone. " +
                        "Please add the selected power socket again.";
                SpannableString spannableString = new SpannableString(spannableText);
                spannableString.setSpan(new StyleSpan(Typeface.BOLD), 17, 17 + selectedPowerSocket.getBleName().length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                mActivity.mUtility.errorDialogWithCallBack(spannableString, 1, false, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        if (powerSocketMQTTService.isConnected()) {
                            powerSocketMQTTService.unsubscribe(selectedPowerSocket);
                        }

                        if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                            powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                        }

                        checkScanDeviceList();
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }
        }
    };

    @Override
    public void onStart() {
        super.onStart();
        powerSocketBLEService.setOnPowerSocketEventCallbacks(powerSocketBLEEventCallbacks);
        mActivity.setOnDevicesStatusChange(scanAndConnectionCallback);
        mActivity.setOnBluetoothStateChangeListener(bluetoothStateChangeListener);
        //--------------------------------------------------------------------------------------
        // MQTT Broker Connection
        WifiManager wifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        // Is Wi-Fi enabled ?
        if (wifiManager.isWifiEnabled()) {
            if (!powerSocketMQTTService.isConnected()) {
                // Initiate MQTT Connection
                powerSocketMQTTService.initialize();
            } else {
                // Subscribe
                if (selectedPowerSocket.isWifiConfigured) {
                    powerSocketMQTTService.subscribe(selectedPowerSocket);
                }
            }
            powerSocketMQTTService.setOnPowerSocketMQTTEventCallbacks(powerSocketMQTTEventCallbacks);
        }
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
        mActivity.isFromBridgeConnection = true;
        powerSocketMQTTService.setOnPowerSocketMQTTEventCallbacks(null);
        powerSocketBLEService.setOnPowerSocketEventCallbacks(null);
        mActivity.setOnDevicesStatusChange(null);
        mActivity.setOnBluetoothStateChangeListener(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*ble scan acknowledgement response check*/
    private void checkScanDeviceList() {
        mActivity.hideProgress();

        // Extract VoDevice from Database based on BLE Address
        String fetchVODeviceQuery = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceBleAddress + " = '"
                + selectedPowerSocket.bleAddress.replace(":", "") + "'";

        // Extract VODevice from DataHolder Object
        DataHolder dataHolder = mActivity.mDbHelper.readData(fetchVODeviceQuery);
        VoDeviceList device = null;
        if (dataHolder != null && dataHolder.get_Listholder().size() > 0) {
            device = new VoDeviceList();

            device.setDevicLocalId(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceLocalId));
            device.setDeviceServerid(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceServerId));
            device.setUser_id(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceUserId));
            device.setDevice_Comm_id(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceCommID));
            device.setDevice_Comm_hexId(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceCommHexId));
            device.setDevice_name(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceName));
            device.setDevice_realName(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceRealName));
            device.setDevice_BleAddress(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceBleAddress).toUpperCase());
            device.setDevice_Type(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceType));
            device.setDevice_type_name(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceTypeName));
            device.setDevice_ConnStatus(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldConnectStatus));
            device.setDevice_brightness(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceBrightness)));
            device.setDevice_rgb_color(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceColor)));
            device.setDevice_SwitchStatus(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldSwitchStatus));
            device.setDevice_is_favourite(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsFavourite));
            device.setDevice_last_state_remember(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceLastState));
            device.setDevice_timestamp(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceTimeStamp));
            device.setDevice_is_active(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsActive));
            device.setDevice_created_at(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceCreatedAt));
            device.setDevice_updated_at(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceUpdatedAt));
            device.setDevice_is_sync(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsSync));
            device.setIsWifiConfigured(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsWifiConfigured)));
            device.setSocketState(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceSocketState)));

            if (device.getDevice_SwitchStatus() != null && device.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                device.setIsChecked(true);
            } else {
                device.setIsChecked(false);
            }
        }

        // Update existing record for Login user
        if (device != null) {
            // Delete Socket Alarms
            deletePowerSocketAlarmDetails(selectedPowerSocket.bleAddress.replace(":", ""));

            // Delete Socket Device Detail Table
            String deleteSocketDeviceDtlQuery = "Delete from " + DBHelper.mTableSocketDeviceDtl + " where " +
                    DBHelper.mFieldTableSocketDeviceDtlDeviceId + " = " + "'" + device.getDevicLocalId() + "'";
            console.log("kjcsjkcsjkdc", deleteSocketDeviceDtlQuery);
            mActivity.mDbHelper.exeQuery(deleteSocketDeviceDtlQuery);

            if (device.getDeviceServerId() != null && device.getDeviceServerId().equalsIgnoreCase("") &&
                    device.getDeviceServerId().equalsIgnoreCase("null")) {

                ContentValues mContentValues = new ContentValues();
                mContentValues.put(DBHelper.mFieldDeviceIsActive, "0");
                mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");
                String[] mArray = new String[]{device.getDevicLocalId()};
                mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues, DBHelper.mFieldDeviceLocalId + "=?", mArray);

                device.setDevice_is_active("0");
                device.setDevice_is_sync("0");
                if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                    if (mActivity.mUtility.haveInternet()) {
                        mActivity.updateDeviceAPI(device);
                    }
                }
            } else { // Delete existing record for Skip User
                String mStringQuery = "delete from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceBleAddress + "= '"
                        + selectedPowerSocket.bleAddress.replace(":", "") + "'";
                mActivity.mDbHelper.exeQuery(mStringQuery);
            }
        }

        showDeviceDeleteAlert();
    }

    /*Call Authentication API to check valid user*/
    private void checkAuthenticationAPI(final boolean isShowProgress) {
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
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.showProgress("Deleting power socket. Please wait...", true);
                            deletePowerSocket(selectedPowerSocket);
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
                isPowerSocketDeleting = false;
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again), 1, true);
            }
        });
    }

    private void deletePowerSocket(PowerSocket powerSocket) {
        if (powerSocket != null) {
            if (powerSocket.bleAddress != null && !powerSocket.bleAddress.isEmpty()) {
                BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
                WifiManager wifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (bluetoothAdapter.isEnabled() && powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                    isPowerSocketDeleting = true;
                    mActivity.showProgress("Deleting power socket. Please wait...", false);
                    powerSocketBLEService.removeDevice(powerSocket);
                } else if (selectedPowerSocket.isWifiConfigured && wifiManager.isWifiEnabled() && powerSocketMQTTService.isConnected()) {
                    isPowerSocketDeleting = true;
                    mActivity.showProgress("Deleting power socket. Please wait...", false);
                    powerSocketMQTTService.removeDevice(powerSocket);
                } else {
                    // Delete Power socket directly from Database
                    checkScanDeviceList();
                }
            }
        } else {
            // Todo There is no power socket to delete
        }
    }

    /*Show delete device alert dialog*/
    private void showDeviceDeleteAlert() {
        isPowerSocketDeleting = false;
        selectedPowerSocket = null;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_device_delete_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void deletePowerSocketAlarmDetails(String bleAddress) {
        AsyncTask.execute(() -> {
            String query = "DELETE FROM " + mTableAlarmPowerSocket + " WHERE " + mfield_ble_address + "= '" + bleAddress.replace(":", "").toUpperCase() + "'";
            mActivity.mDbHelper.exeQuery(query);
        });
    }

    // Todo Check Login use
    private void removeRecordsFromDb() {
        mActivity.hideProgress();

        // Extract VoDevice from Database based on BLE Address
        String fetchVODeviceQuery = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceBleAddress + " = '"
                + selectedPowerSocket.bleAddress.replace(":", "") + "'";

        // Extract VODevice from DataHolder Object
        DataHolder dataHolder = mActivity.mDbHelper.readData(fetchVODeviceQuery);
        VoDeviceList device = null;
        if (dataHolder != null && dataHolder.get_Listholder().size() > 0) {
            device = new VoDeviceList();

            device.setDevicLocalId(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceLocalId));
            device.setDeviceServerid(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceServerId));
            device.setUser_id(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceUserId));
            device.setDevice_Comm_id(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceCommID));
            device.setDevice_Comm_hexId(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceCommHexId));
            device.setDevice_name(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceName));
            device.setDevice_realName(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceRealName));
            device.setDevice_BleAddress(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceBleAddress).toUpperCase());
            device.setDevice_Type(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceType));
            device.setDevice_type_name(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceTypeName));
            device.setDevice_ConnStatus(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldConnectStatus));
            device.setDevice_brightness(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceBrightness)));
            device.setDevice_rgb_color(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceColor)));
            device.setDevice_SwitchStatus(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldSwitchStatus));
            device.setDevice_is_favourite(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsFavourite));
            device.setDevice_last_state_remember(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceLastState));
            device.setDevice_timestamp(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceTimeStamp));
            device.setDevice_is_active(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsActive));
            device.setDevice_created_at(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceCreatedAt));
            device.setDevice_updated_at(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceUpdatedAt));
            device.setDevice_is_sync(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsSync));
            device.setIsWifiConfigured(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceIsWifiConfigured)));
            device.setSocketState(Integer.parseInt(dataHolder.get_Listholder().get(0).get(DBHelper.mFieldDeviceSocketState)));

            if (device.getDevice_SwitchStatus() != null && device.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                device.setIsChecked(true);
            } else {
                device.setIsChecked(false);
            }
        }

        // Update existing record for Login user
        if (device != null) {
            // Delete Socket Alarms
            deletePowerSocketAlarmDetails(selectedPowerSocket.bleAddress.replace(":", ""));

            // Delete Socket Device Detail Table
            String deleteSocketDeviceDtlQuery = "Delete from " + DBHelper.mTableSocketDeviceDtl + " where " +
                    DBHelper.mFieldTableSocketDeviceDtlDeviceId + " = " + "'" + device.getDevicLocalId() + "'";
            console.log("kjcsjkcsjkdc", deleteSocketDeviceDtlQuery);
            mActivity.mDbHelper.exeQuery(deleteSocketDeviceDtlQuery);

            if (device.getDeviceServerId() != null && device.getDeviceServerId().equalsIgnoreCase("") &&
                    device.getDeviceServerId().equalsIgnoreCase("null")) {

                ContentValues mContentValues = new ContentValues();
                mContentValues.put(DBHelper.mFieldDeviceIsActive, "0");
                mContentValues.put(DBHelper.mFieldDeviceIsSync, "0");
                String[] mArray = new String[]{device.getDevicLocalId()};
                mActivity.mDbHelper.updateRecord(DBHelper.mTableDevice, mContentValues, DBHelper.mFieldDeviceLocalId + "=?", mArray);

                device.setDevice_is_active("0");
                device.setDevice_is_sync("0");
                if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                    if (mActivity.mUtility.haveInternet()) {
                        mActivity.updateDeviceAPI(device);
                    }
                }
            } else { // Delete existing record for Skip User
                String mStringQuery = "delete from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceBleAddress + "= '"
                        + selectedPowerSocket.bleAddress.replace(":", "") + "'";
                mActivity.mDbHelper.exeQuery(mStringQuery);
            }
        }
    }

    onDeviceConnectionStatusChange scanAndConnectionCallback = new onDeviceConnectionStatusChange() {
        @Override
        public void addScanDevices() {

        }

        @Override
        public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
            console.log("shdcjhsvdhcshcvds", mVoBluetoothDevices.getDeviceHexData());
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
                            byte[] manufacturerData = manufactureData.data;
                            // Extract Self Device ID
                            byte[] selfDeviceIdBytes = ByteConverter.copyOfRange(manufacturerData, 3, 5);
                            int selfDeviceId = ByteConverter.convertByteArrayToInt(selfDeviceIdBytes);
                            if (isFirstStepResetComplete) {
                                // Fresh Device after reset
                                mActivity.hideProgress();
                                if (selectedPowerSocket.bleAddress.equalsIgnoreCase(mVoBluetoothDevices.getDeviceAddress())) {
                                    if ((selfDeviceId == 0x00)) {
                                        // Reset is done successfully
                                        isFirstStepResetComplete = false;
                                        handler.removeCallbacks(timeoutRunnable);

                                        // Delete the device from Database
                                        // Delete Socket alarms from Database
                                        removeRecordsFromDb();

                                        mActivity.mUtility.errorDialogWithCallBack("Device reset successfully", 0, false, new onAlertDialogCallBack() {
                                            @Override
                                            public void PositiveMethod(DialogInterface dialog, int id) {
                                                mActivity.onBackPressed();
                                            }

                                            @Override
                                            public void NegativeMethod(DialogInterface dialog, int id) {

                                            }
                                        });
                                    } else {
                                        if (selfDeviceId != 0x02) {
                                            isFirstStepResetComplete = false;
                                            mActivity.mUtility.errorDialogWithCallBack("Device reset failed. Please try again", 3, false, new onAlertDialogCallBack() {
                                                @Override
                                                public void PositiveMethod(DialogInterface dialog, int id) {
                                                    // Since device is not reset, revert back to its original association state
                                                    selectedPowerSocket.isAssociated = true;
                                                    beginDeviceScan();
                                                }

                                                @Override
                                                public void NegativeMethod(DialogInterface dialog, int id) {

                                                }
                                            });
                                        }
                                    }
                                }
                            } else {
//                                if(!isPowerSocketResetInProgress){
//                                    if(selfDeviceId == 0x00){
//                                        // Delete Device
//                                        String spannableText = "The power socket " + selectedPowerSocket.getBleName() + " may have been deleted from another phone. " +
//                                                "Please add the selected power socket again.";
//                                        SpannableString spannableString = new SpannableString(spannableText);
//                                        spannableString.setSpan(new StyleSpan(Typeface.BOLD), 17, 17 + selectedPowerSocket.getBleName().length() + 1, Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
//                                        mActivity.mUtility.errorDialogWithCallBack(spannableString, 1, false, new onAlertDialogCallBack() {
//                                            @Override
//                                            public void PositiveMethod(DialogInterface dialog, int id) {
//                                                if (powerSocketMQTTService.isConnected()) {
//                                                    powerSocketMQTTService.unsubscribe(selectedPowerSocket);
//                                                }
//
//                                                if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
//                                                    powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
//                                                }
//
//                                                checkScanDeviceList();
//                                            }
//
//                                            @Override
//                                            public void NegativeMethod(DialogInterface dialog, int id) {
//
//                                            }
//                                        });
//                                    }
//                                }
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
    };

    private void beginDeviceScan() {
        if (isAdded()) {
            mActivity.isFromBridgeConnection = true;
            mActivity.startDeviceScan();
        }
    }

    OnBluetoothStateChangeListener bluetoothStateChangeListener = new OnBluetoothStateChangeListener() {
        @Override
        public void onBluetoothOff() {

        }

        @Override
        public void onBluetoothTurningOff() {
            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
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