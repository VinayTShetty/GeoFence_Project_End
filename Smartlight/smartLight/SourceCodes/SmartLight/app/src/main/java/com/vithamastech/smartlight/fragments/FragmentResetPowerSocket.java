package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.evergreen.ble.advertisement.AdvertisementFlag;
import com.evergreen.ble.advertisement.AdvertisementRecord;
import com.evergreen.ble.advertisement.ManufactureData;

import com.google.gson.Gson;
import com.vithamas.blecommmodule.ScannerModule.BLEScanner;
import com.vithamas.blecommmodule.ScannerModule.interfaces.BLEScannerCallback;
import com.vithamastech.smartlight.Adapter.FoundPowerSocketAdapter;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.PowerSocketUtils.Constants;
import com.vithamastech.smartlight.PowerSocketUtils.EncryptionUtilsPowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketAdvData;
import com.vithamastech.smartlight.PowerSocketUtils.PowerSocketConstants;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.interfaces.OnBluetoothStateChangeListener;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.vithamastech.smartlight.services.PowerSocketBLEEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketBLEService;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.vithamastech.smartlight.db.DBHelper.mTableAlarmPowerSocket;
import static com.vithamastech.smartlight.db.DBHelper.mfield_ble_address;

// Todo handle situation when user doesn't turn on the device after reset operation is complete
public class FragmentResetPowerSocket extends Fragment {

    MainActivity mActivity;
    private Unbinder unbinder;
    private FoundPowerSocketAdapter foundMonitorAdapter;
    private PowerSocketBLEService powerSocketBLEService;
    private PowerSocket selectedPowerSocket;
    private Handler handler;
    private Runnable timeoutRunnable;
    private boolean isFirstStepResetComplete;
    private int resetState;
    private boolean isPowerSocketResetInProgress;

    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerViewFoundDevices)
    RecyclerView recyclerView;
    @BindView(R.id.linearLayoutSearchingDevices)
    LinearLayout linearLayoutSearchingDevices;
    @BindView(R.id.linearLayoutNodeviceFound)
    LinearLayout linearLayoutNoDeviceFound;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        handler = new Handler();

        timeoutRunnable = () -> {
            beginDeviceScan(true);
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset_power_socket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        resetState = Constants.SCANNING_IN_PROGRESS;

        mActivity.mTextViewTitle.setText(R.string.reset_power_socket);
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
            selectedPowerSocket = powerSocket;
//                selectedPowerSocket.isAssociated = false; // This is to use default key for Reset operation
            resetState = Constants.SCANNING_STOPPED;

            if (!powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.connect(selectedPowerSocket);
                mActivity.showProgress("Connecting Please Wait", true);
            } else {
                showResetPromptDialog();
            }
        });

        mActivity.mImageViewAddDevice.setOnClickListener(v -> {
            beginDeviceScan(false);
        });

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            beginDeviceScan(false);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mActivity.setOnBluetoothStateChangeListener(bluetoothStateChangeListener);
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            powerSocketBLEService.setOnPowerSocketEventCallbacks(powerSocketBLEEventCallbacks);
            mActivity.setOnDevicesStatusChange(scanAndConnectionCallback);
            if (resetState == Constants.SCANNING_IN_PROGRESS) {
                beginDeviceScan(false);
            }
        } else {
            mActivity.mUtility.errorDialogWithCallBack("Please enable Bluetooth to enable searching for Power Socket",
                    1, true, new onAlertDialogCallBack() {
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
    public void onStop() {
        super.onStop();
        mActivity.isFromBridgeConnection = true;
        if (resetState == Constants.SCANNING_IN_PROGRESS) {

        } else {
            resetState = Constants.SCANNING_IN_PROGRESS;
            handler.removeCallbacks(timeoutRunnable);
            isFirstStepResetComplete = false;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
        handler.removeCallbacks(timeoutRunnable);
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
                                                    beginDeviceScan(false);
                                                }

                                                @Override
                                                public void NegativeMethod(DialogInterface dialog, int id) {

                                                }
                                            });
                                        }
                                    }
                                }
                            } else {
                                if (selfDeviceId == 0x02) {
                                    PowerSocket foundPowerSocket = new PowerSocket();
                                    // If the socket is associated, then set this flag to true. Else, false
                                    foundPowerSocket.setBleAddress(mVoBluetoothDevices.getDeviceAddress());

                                    foundPowerSocket.setBleName("Vithamas Socket");

                                    foundPowerSocket.setDeviceType(0x0400);

                                    foundMonitorAdapter.addDevice(foundPowerSocket);
                                    swipeRefreshLayout.setRefreshing(false);
                                    showDeviceList();
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
    };

    PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks = new PowerSocketBLEEventCallbacks() {
        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            mActivity.hideProgress();
            if (isPowerSocketResetInProgress) {
                isPowerSocketResetInProgress = false;
                mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.BLE_CONNECTION_FAILED), 1, false, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        beginDeviceScan(false);
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }
        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {
            mActivity.hideProgress();
            if (isPowerSocketResetInProgress) {
                isPowerSocketResetInProgress = false;
                mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.BLE_CONNECTION_FAILED), 1, false, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        beginDeviceScan(false);
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }
        }

        @Override
        public void onBluetoothRestart() {
            super.onBluetoothRestart();
            mActivity.hideProgress();
            mActivity.mUtility.errorDialogWithCallBack("Device connection failed. Please turn off and turn on Bluetooth and try again.\n\n" +
                            "If the same problems persists, restart the phone and try again.", 1, false,
                    new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            mActivity.onBackPressed();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
        }

        @Override
        public void onAuthenticationSuccess(String deviceAddress) {
            super.onAuthenticationSuccess(deviceAddress);
            mActivity.hideProgress();
            // Dialog to ask user to factory reset power socket
            showResetPromptDialog();
        }

        @Override
        public void onAuthenticationFailure(String deviceAddress) {
            super.onAuthenticationFailure(deviceAddress);
            mActivity.hideProgress();
            isPowerSocketResetInProgress = false;

            if (powerSocketBLEService.isDeviceConnected(selectedPowerSocket.bleAddress)) {
                powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
            }

            if (!isDetached()) {
                mActivity.mUtility.errorDialogWithCallBack("Authentication failed.", 1, false, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        beginDeviceScan(false);
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }
        }

        @Override
        public void onDeviceResetCompleted(String deviceAddress) {
            super.onDeviceResetCompleted(deviceAddress);
            isFirstStepResetComplete = true;
            isPowerSocketResetInProgress = false;
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
            isPowerSocketResetInProgress = false;
            mActivity.mUtility.errorDialogWithCallBack("Device Reset failed. Please try again.", 2, false, new onAlertDialogCallBack() {
                @Override
                public void PositiveMethod(DialogInterface dialog, int id) {
                    beginDeviceScan(false);
                }

                @Override
                public void NegativeMethod(DialogInterface dialog, int id) {

                }
            });
        }
    };

    private void showResetPromptDialog() {
        mActivity.mUtility.errorDialogWithYesNoCallBack("Factory Reset", "Are you sure you want to reset the device?",
                "I'm sure", "Cancel", false, 1, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        // Begin Factory Reset
                        beginResetDevice();
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {
                        powerSocketBLEService.disconnect(selectedPowerSocket.bleAddress);
                        beginDeviceScan(false);
                    }
                });
    }

    private void showDeviceList() {
        if (isAdded()) {
            recyclerView.setVisibility(View.VISIBLE);
            linearLayoutNoDeviceFound.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.GONE);
        }
    }

    private void showNoDevicesLayout() {
        if (isAdded()) {
            recyclerView.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.GONE);
            linearLayoutNoDeviceFound.setVisibility(View.VISIBLE);
        }
    }

    private void showSearchingDevicesLayout() {
        if (isAdded()) {
            recyclerView.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.VISIBLE);
            linearLayoutNoDeviceFound.setVisibility(View.GONE);
        }
    }

    private List<PowerSocket> getDevicesFromDb() {
        List<PowerSocket> retVal = new ArrayList<>();
        try {
            DataHolder dataHolder;
            String url = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceIsActive + "= '1'" + " AND " +
                    DBHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " +
                    DBHelper.mFieldDeviceType + "= '" + "0400" + "'" + " ORDER BY " +
                    DBHelper.mFieldDeviceIsFavourite + " desc";

            dataHolder = mActivity.mDbHelper.readData(url);
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

    private void beginDeviceScan(boolean isBackgroundOperation) {
        if (isAdded()) {
            mActivity.isFromBridgeConnection = true;
            resetState = Constants.SCANNING_IN_PROGRESS;
            if (!isBackgroundOperation) {
                foundMonitorAdapter.clear();
                showSearchingDevicesLayout();

                List<PowerSocket> powerSocketList = getDevicesFromDb();
                if (!powerSocketList.isEmpty()) {
                    swipeRefreshLayout.setRefreshing(false);
                    foundMonitorAdapter.addDevices(powerSocketList);
                    showDeviceList();
                }
            } else {
                mActivity.startDeviceScan();
            }
        }
    }

    private void beginResetDevice() {
        isPowerSocketResetInProgress = true;
        resetState = Constants.RESET_IN_PROGRESS;
        mActivity.showProgress("Device is being reset. Please wait...", false);
        selectedPowerSocket.isAssociated = false;
        powerSocketBLEService.resetPowerSocket(selectedPowerSocket);
    }

    private void deletePowerSocketAlarmDetails(String bleAddress) {
        AsyncTask.execute(() -> {
            String query = "DELETE FROM " + mTableAlarmPowerSocket + " WHERE " + mfield_ble_address + "= '" + bleAddress.replace(":", "").toUpperCase() + "'";
            mActivity.mDbHelper.exeQuery(query);
        });
    }

    // Todo Check Login use
    private void removeRecordsFromDb() {
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
            beginDeviceScan(false);
        }

        @Override
        public void onBluetoothTurningOn() {

        }
    };
}