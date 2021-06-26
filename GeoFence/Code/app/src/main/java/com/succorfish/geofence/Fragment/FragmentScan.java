package com.succorfish.geofence.Fragment;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.kaopiz.kprogresshud.KProgressHUD;
import com.succorfish.geofence.BaseFragment.BaseFragment;
import com.succorfish.geofence.LoginActivity;
import com.succorfish.geofence.MainActivity;
import com.succorfish.geofence.R;
import com.succorfish.geofence.RoomDataBaseEntity.DeviceTable;
import com.succorfish.geofence.adapter.FragmentScanAdapter;
import com.succorfish.geofence.customObjects.CustBluetootDevices;
import com.succorfish.geofence.customObjects.DeviceTableDetails;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.interfaceActivityToFragment.ConnectionStatus;
import com.succorfish.geofence.interfaceActivityToFragment.GeoFenceDialogAlertShow;
import com.succorfish.geofence.interfaceActivityToFragment.OpenDialogToCheckDeviceName;
import com.succorfish.geofence.interfaceActivityToFragment.PassConnectionStatusToFragment;
import com.succorfish.geofence.interfaceActivityToFragment.PassScanDeviceToActivity_interface;
import com.succorfish.geofence.interfaceFragmentToActivity.DeviceConnectDisconnect;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;
import com.succorfish.geofence.interfaces.onDeviceNameAlert;
import com.succorfish.geofence.utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static com.succorfish.geofence.MainActivity.CONNECTED_BLE_ADDRESS;
import static com.succorfish.geofence.MainActivity.roomDBHelperInstance;
import static com.succorfish.geofence.utility.Utility.ble_on_off;

public class FragmentScan extends BaseFragment {
    private final int LocationPermissionRequestCode = 100;
    private Unbinder unbinder;
    View fragmenScanView;
    DialogProvider dialogProvider;
    @BindView(R.id.Tap_on_connect_button)
    TextView connect_inst_Txtview;
    @BindView(R.id.fragmentScan_recycleView)
    RecyclerView fragmentScanRecycleView;
    private FragmentScanAdapter fragmentScanAdapter;
    private KProgressHUD hud;
    MainActivity mainActivity;
    /**
     * This is used for scan Devices.
     */
    private ArrayList<CustBluetootDevices> customBluetoothDeviceList = new ArrayList<CustBluetootDevices>();
    /**
     * This is used for Getting DeviceTable Name;
     */
    ArrayList<DeviceTableDetails> deviceTableslistDetails = new ArrayList<DeviceTableDetails>();
    Fragment_ConnectionTimeOutTimer fragmentScanConnectionTimeOutTimer;
    DeviceConnectDisconnect deviceConnectDisconnect;
    private static int number_of_Records_Avaliable_device_name_table;



    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = (MainActivity) getActivity();
        interfaceIntialization();

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmenScanView = inflater.inflate(R.layout.fragment_scan, container, false);
        unbinder = ButterKnife.bind(this, fragmenScanView);
        bottomLayoutVisibility(true);
        loadDeviceTableData();
        intializeView();
        checkPermissionGiven();
        checkBluetoothIsOn();
        setUpRecycleView();
        interfaceImplementation_CallBack();
        ItemClickonDevice();
        intializeDialog();
        geoFenceAlertImplementation();
        getListOfConnectedDevices();

        return fragmenScanView;
    }



    private void bottomLayoutVisibility(boolean hide_true_unhide_false) {
        mainActivity.hideBottomLayout(hide_true_unhide_false);
    }

    private void interfaceIntialization() {
        deviceConnectDisconnect = (DeviceConnectDisconnect) getActivity();
    }

  /*  private void addNotification() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                int count = roomDBHelperInstance.get_GeoFenceAlert_info_dao().getCountNumberOfRecordsAvaliable("0");
                mainActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (count <= 0) {
                            mtextViewCountNotification.setVisibility(View.INVISIBLE);
                            mtextViewCountNotification.setBackgroundColor(Color.parseColor("#000000"));
                        } else if (count > 0) {
                            mtextViewCountNotification.setVisibility(View.VISIBLE);
                            mtextViewCountNotification.setBackgroundResource(R.drawable.lable);
                            mtextViewCountNotification.setText("" + count);
                        }
                    }
                });
            }
        });
    }*/

    private void intializeView() {
        hud = KProgressHUD.create(getActivity());
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        clearScanDevices();
        clearDatabaseSavedDevices();
        System.gc();
    }

    @Override
    public String toString() {
        return FragmentScan.class.getSimpleName();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LocationPermissionRequestCode:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mainActivity.start_stop_scan();
                } else {
                    askPermission();
                }
        }
    }

    private void checkPermissionGiven() {
        if (isAdded()) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mainActivity.start_stop_scan();
            } else {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LocationPermissionRequestCode);
            }
        }
    }

    private void askPermission() {
        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            giveLocation_NotGivenToast();
        } else {
            location_allow_setting();
        }
    }

    private void checkBluetoothIsOn() {
        Utility utility = new Utility();
        if (!ble_on_off()) {
            utility.showTaost(getActivity(), "Turn on Bluetooth", getResources().getDrawable(R.drawable.ic_bluetoth_not_enabled));
        }
    }

    private void giveLocation_NotGivenToast() {
        Utility utility = new Utility();
        utility.showTaost(getActivity(), "Location Denied", getResources().getDrawable(R.drawable.ic_location_not_enabled));
    }

    private void location_allow_setting() {
        Utility utility = new Utility();
        utility.showPermissionDialog(getActivity(), "Permission Denied For Scanning");
    }

    private void setUpRecycleView() {
        fragmentScanAdapter = new FragmentScanAdapter(customBluetoothDeviceList);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        fragmentScanRecycleView.setLayoutManager(mLayoutManager);
        fragmentScanRecycleView.setAdapter(fragmentScanAdapter);
    }

    private void interfaceImplementation_CallBack() {
        mainActivity.setupPassScanDeviceToActivity_interface(new PassScanDeviceToActivity_interface() {
            @Override
            public void sendCustomBleDevice(CustBluetootDevices custBluetootDevices) {
                System.out.println("FC devices= " + custBluetootDevices.getBleAddress());
                if (!customBluetoothDeviceList.contains(custBluetootDevices)) {
                    customBluetoothDeviceList.add(custBluetootDevices);
                    String deviceName = getdevialiasNamefromDB(custBluetootDevices.getBleAddress());
                    custBluetootDevices.setDeviceName(deviceName);
                    connect_inst_Txtview.setVisibility(View.VISIBLE);
                    fragmentScanAdapter.notifyDataSetChanged();
                }
            }
        });

        mainActivity.setUpConnectionStatus(new ConnectionStatus() {
            @Override
            public void connectedDevicePostion(BluetoothDevice bluetoothDevice, boolean status) {
                if (status) {
                    fragmentScanAdapter.notifyDataSetChanged();
                } else {
                    fragmentScanAdapter.notifyDataSetChanged();
                }
            }
        });

        mainActivity.setupPassConnectionStatusToFragment(new PassConnectionStatusToFragment() {
            @Override
            public void connectDisconnect(String bleAddress, boolean connected_disconnected) {
                if (connected_disconnected) {
                    cancelTimerFragmentScanTimer();
                    CustBluetootDevices custBluetootDevices = new CustBluetootDevices();
                    custBluetootDevices.setBleAddress(bleAddress);
                    if (customBluetoothDeviceList.contains(custBluetootDevices)) {
                        int postion = customBluetoothDeviceList.indexOf(custBluetootDevices);
                        CustBluetootDevices custBluetootDevices1 = customBluetoothDeviceList.get(postion);
                        custBluetootDevices1.setConnected(true);
                        fragmentScanAdapter.notifyItemChanged(postion);
                        cancelProgressDialog();
                    }
                } else {
                    CustBluetootDevices custBluetootDevices = new CustBluetootDevices();
                    custBluetootDevices.setBleAddress(bleAddress);
                    if (customBluetoothDeviceList.contains(custBluetootDevices)) {
                        int postion = customBluetoothDeviceList.indexOf(custBluetootDevices);
                        CustBluetootDevices custBluetootDevices1 = customBluetoothDeviceList.get(postion);
                        custBluetootDevices1.setConnected(false);
                        fragmentScanAdapter.notifyItemChanged(postion);
                    }
                }
            }
        });


        mainActivity.setUpOpenDialogToCheckDeviceName(new OpenDialogToCheckDeviceName() {
            @Override
            public void showDialogNameNotAvaliable(String bleAddressForDeviceAfterConfermation, String deviceToken, String imeiNumberFromFirmware) {
                dialogProvider.enterNameDialog(new onDeviceNameAlert() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id, String deviceName) {
                        String devicealiasName_givenFromuser = deviceName;
                        if (devicealiasName_givenFromuser.equalsIgnoreCase("")) {
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    number_of_Records_Avaliable_device_name_table = 0;
                                    number_of_Records_Avaliable_device_name_table = roomDBHelperInstance.get_TableDevice_dao().getNumberOfRecordsCount();
                                    if (number_of_Records_Avaliable_device_name_table == 0) {
                                        String devicealiasName = getResources().getString(R.string.device_name_alias_save);
                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {
                                                roomDBHelperInstance.get_TableDevice_dao().insert_tableDevices(new DeviceTable(
                                                        bleAddressForDeviceAfterConfermation.replace(":", "").toLowerCase(), devicealiasName + " " + "1",
                                                        imeiNumberFromFirmware,
                                                        deviceToken,
                                                        "NA"
                                                ));
                                                mainActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        int postionToChange = getPositionOfItemFromBleAddress(bleAddressForDeviceAfterConfermation);
                                                        if (postionToChange != -1) {
                                                            customBluetoothDeviceList.get(postionToChange).setDeviceName(devicealiasName + " " + "1");
                                                            fragmentScanAdapter.notifyItemChanged(postionToChange);
                                                            dialog.dismiss();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    } else if (number_of_Records_Avaliable_device_name_table > 0) {
                                        number_of_Records_Avaliable_device_name_table++;
                                        String devicealiasName = getResources().getString(R.string.device_name_alias_save);
                                        AsyncTask.execute(new Runnable() {
                                            @Override
                                            public void run() {

                                                roomDBHelperInstance.get_TableDevice_dao().insert_tableDevices(new DeviceTable(
                                                        bleAddressForDeviceAfterConfermation.replace(":", "").toLowerCase(),
                                                        devicealiasName + " " + number_of_Records_Avaliable_device_name_table,
                                                        imeiNumberFromFirmware,
                                                        deviceToken,
                                                        "NA"
                                                ));

                                                mainActivity.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        int postionToChange = getPositionOfItemFromBleAddress(bleAddressForDeviceAfterConfermation);
                                                        if (postionToChange != -1) {
                                                            customBluetoothDeviceList.get(postionToChange).setDeviceName(devicealiasName + " " + number_of_Records_Avaliable_device_name_table);
                                                            fragmentScanAdapter.notifyItemChanged(postionToChange);
                                                            dialog.dismiss();
                                                        }
                                                    }
                                                });
                                            }
                                        });
                                    }

                                }
                            });
                        } else {
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {

                                    roomDBHelperInstance.get_TableDevice_dao().insert_tableDevices(new DeviceTable(
                                            bleAddressForDeviceAfterConfermation.replace(":", "").toLowerCase(),
                                            devicealiasName_givenFromuser,
                                            imeiNumberFromFirmware,
                                            deviceToken,
                                            "NA"
                                    ));
                                    mainActivity.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            int postionToChange = getPositionOfItemFromBleAddress(bleAddressForDeviceAfterConfermation);
                                            if (postionToChange != -1) {
                                                customBluetoothDeviceList.get(postionToChange).setDeviceName(devicealiasName_givenFromuser);
                                                fragmentScanAdapter.notifyItemChanged(postionToChange);
                                                dialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {
                        AsyncTask.execute(new Runnable() {
                            @Override
                            public void run() {
                                number_of_Records_Avaliable_device_name_table = 0;
                                number_of_Records_Avaliable_device_name_table = roomDBHelperInstance.get_TableDevice_dao().getNumberOfRecordsCount();
                                if (number_of_Records_Avaliable_device_name_table == 0) {
                                    String devicealiasName = getResources().getString(R.string.device_name_alias_save);
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            roomDBHelperInstance.get_TableDevice_dao().insert_tableDevices(new DeviceTable(
                                                    bleAddressForDeviceAfterConfermation.replace(":", "").toLowerCase(),
                                                    devicealiasName + " " + "1",
                                                    imeiNumberFromFirmware,
                                                    deviceToken,
                                                    "NA"
                                            ));
                                            mainActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    int postionToChange = getPositionOfItemFromBleAddress(bleAddressForDeviceAfterConfermation);
                                                    if (postionToChange != -1) {
                                                        customBluetoothDeviceList.get(postionToChange).setDeviceName(devicealiasName + " " + "1");

                                                        fragmentScanAdapter.notifyItemChanged(postionToChange);
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                } else if (number_of_Records_Avaliable_device_name_table > 0) {
                                    number_of_Records_Avaliable_device_name_table++;
                                    String devicealiasName = getResources().getString(R.string.device_name_alias_save);
                                    AsyncTask.execute(new Runnable() {
                                        @Override
                                        public void run() {
                                            roomDBHelperInstance.get_TableDevice_dao().insert_tableDevices(new DeviceTable(
                                                    bleAddressForDeviceAfterConfermation.replace(":", "").toLowerCase(),
                                                    devicealiasName + " " + number_of_Records_Avaliable_device_name_table,
                                                    imeiNumberFromFirmware,
                                                    deviceToken,
                                                    "NA"
                                            ));
                                            mainActivity.runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    int postionToChange = getPositionOfItemFromBleAddress(bleAddressForDeviceAfterConfermation);
                                                    if (postionToChange != -1) {
                                                        customBluetoothDeviceList.get(postionToChange).setDeviceName(devicealiasName + " " + number_of_Records_Avaliable_device_name_table);
                                                        fragmentScanAdapter.notifyItemChanged(postionToChange);
                                                        dialog.dismiss();
                                                    }
                                                }
                                            });
                                        }
                                    });
                                }

                            }
                        });
                    }
                });
            }
        });

    }

    private void ItemClickonDevice() {
        fragmentScanAdapter.setOnItemClickListner(new FragmentScanAdapter.ScanOnItemClickInterface() {
            @Override
            public void ConnectionStatusClick(CustBluetootDevices customBluetoothObject, int ItemSlected) {
                CONNECTED_BLE_ADDRESS = customBluetoothObject.getBleAddress();
                if (deviceConnectDisconnect != null) {
                    if (customBluetoothObject.isConnected()) {
                        deviceConnectDisconnect.makeDevieConnecteDisconnect(customBluetoothObject, false);
                    } else if (!customBluetoothObject.isConnected()) {
                        if (ble_on_off()) {
                            fragmentScanConnectionTimeOutTimer = new Fragment_ConnectionTimeOutTimer(30000, 1000);
                            fragmentScanConnectionTimeOutTimer.start();
                            showProgressDialog(customBluetoothObject.getBleAddress(), "Connectiong ");
                            deviceConnectDisconnect.makeDevieConnecteDisconnect(customBluetoothObject, true);
                        } else {
                            dialogProvider.errorDialog("Turn on Bluetooth");
                        }
                    }
                }
            }

            @Override
            public void messagingLayoutClick(CustBluetootDevices customBluetoothObject, int ItemSlected) {
                CONNECTED_BLE_ADDRESS = customBluetoothObject.getBleAddress();
                mainActivity.replaceFragment(new FragmentChatting(), null, null, false);
            }

            @Override
            public void geoFenceLayoutClick(CustBluetootDevices customBluetoothObject, int ItemSlected) {
                CONNECTED_BLE_ADDRESS = customBluetoothObject.getBleAddress();
                mainActivity.replaceFragment(new FragmentHistory(), null, null, false);
            }

            @Override
            public void liveTracking(CustBluetootDevices customBluetooth, int postion) {
                CONNECTED_BLE_ADDRESS = customBluetooth.getBleAddress();
                mainActivity.replaceFragment(new FragmentLiveTracking(), null, null, false);
            }

            @Override
            public void overFlow_menu_Setting(CustBluetootDevices customBluetooth, int postion) {
                CONNECTED_BLE_ADDRESS = customBluetooth.getBleAddress();
                mainActivity.replaceFragment(new FragmentSetting(), null, null, false);
            }

            @Override
            public void overFlow_menu_SOS(CustBluetootDevices customBluetooth, int postion) {
                CONNECTED_BLE_ADDRESS = customBluetooth.getBleAddress();
                mainActivity.replaceFragment(new FragmentSOS(), null, false);
            }
        });

    }

    @OnClick(R.id.reload)
    public void reloadImageClick() {
        checkBluetoothIsOn();
        clearScanConnectedDevices();
        mainActivity.start_stop_scan();
    }

   /* @OnClick(R.id.notificaiton_count)
    public void notificationIconClick() {
        mainActivity.replaceFragment(new FragmentHistory(), null, null, false);
    }*/

    @OnClick(R.id.logout_imagebutotn)
    public void historyImageClick_insideButton() {
        logoutUser();
    }

    private void geoFenceAlertImplementation() {
        Fragment fragment = getFragmentManager().findFragmentById(R.id.mainActivity_container);
        mainActivity.setUpGeoFenceAlertDialogInterface(new GeoFenceDialogAlertShow() {
            @Override
            public void showDialogInterface(String ruleVioation, String bleAddress, String message_one, String messageTwo, String timeStamp) {
                if (isAdded() && isVisible() && fragment instanceof FragmentScan) {
                    //  checkNotificationCount();
                    dialogProvider.showGeofenceAlertDialog(ruleVioation, bleAddress, message_one, messageTwo, 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            /**
                             * Open map Fragment.
                             */
                            mainActivity.replaceFragment(new FragmentMap(), timeStamp, new FragmentMap().toString(), false);
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                } else {
                }
            }
        });
    }

    private void intializeDialog() {
        dialogProvider = new DialogProvider(getActivity());
    }


    /**
     * Already implemented this logc so Remove it
     */

    /*private void addConnectedDevice() {
        List<BleDevice> bleDeviceList = BleManager.getInstance().getAllConnectedDevice();
        for (BleDevice bleDevice : bleDeviceList) {
            String macAddress = bleDevice.getMac();
            RxBleClient rxBleClient = RxBleClient.create(getActivity());
            RxBleDevice device = rxBleClient.getBleDevice(macAddress);
            String deviceName = getdevialiasNamefromDB(device.getMacAddress());
            CustomBluetooth customBluetooth = new CustomBluetooth(device, deviceName, device.getMacAddress());
            customBluetoothDeviceList.add(customBluetooth);
            connect_inst_Txtview.setVisibility(View.VISIBLE);
            fragmentScanAdapter.notifyDataSetChanged();
        }
    }*/
    private String getdevialiasNamefromDB(String bleAddress) {
        String deviceName = getResources().getString(R.string.device_name_alias);
        for (DeviceTableDetails deviceTableDetails : deviceTableslistDetails) {
            if (deviceTableDetails.getBleAddress().replace(":", "").toLowerCase().equalsIgnoreCase(bleAddress.replace(":", "").toString())) {
                deviceName = deviceTableDetails.getDeviceName();
            }
        }
        return deviceName;
    }

    private void clearScanConnectedDevices() {
        customBluetoothDeviceList.clear();
        fragmentScanAdapter.notifyDataSetChanged();
        getListOfConnectedDevices();
//        addConnectedDevice();   // Remove
    }

    private void clearScanDevices() {
        customBluetoothDeviceList.clear();
    }

    private void clearDatabaseSavedDevices() {
        deviceTableslistDetails.clear();
    }

  /*  private void checkNotificationCount() {
        if (mtextViewCountNotification.getText().toString().equalsIgnoreCase("")) {
            mtextViewCountNotification.setVisibility(View.VISIBLE);
            mtextViewCountNotification.setBackgroundResource(R.drawable.lable);
            mtextViewCountNotification.setText("" + 1);
        } else {
            int countAlreadyInTextView = Integer.parseInt(mtextViewCountNotification.getText().toString());
            if (countAlreadyInTextView > 0) {
                countAlreadyInTextView++;
                mtextViewCountNotification.setVisibility(View.VISIBLE);
                mtextViewCountNotification.setBackgroundResource(R.drawable.lable);
                mtextViewCountNotification.setText("" + countAlreadyInTextView);
            }
        }
    }*/

    private void showProgressDialog(String bleAddress, String detailedLabel) {
        hud.setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Connecting")
                .setDetailsLabel(detailedLabel + " " + bleAddress)
                .setCancellable(false)
                .show();
    }

    private void cancelProgressDialog() {
        if (hud != null) {
            if (hud.isShowing()) {
                hud.dismiss();
            }
        }
    }

    private void loadDeviceTableData() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if (deviceTableslistDetails != null) {
                    deviceTableslistDetails.clear();
                }
                List<DeviceTable> deviceTableList = roomDBHelperInstance.get_TableDevice_dao().getAll_tableDevices();
                for (DeviceTable deviceTable : deviceTableList) {
                    DeviceTableDetails deviceTableDetails = new DeviceTableDetails();
                    deviceTableDetails.setBleAddress(deviceTable.getBLE_Address());
                    deviceTableDetails.setDeviceName(deviceTable.getName());
                    deviceTableslistDetails.add(deviceTableDetails);
                }
            }
        });
    }


    private void logoutUser() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle);
        builder.setTitle("Logout");
        builder.setCancelable(false);
        builder.setMessage("Are you sure ?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (ble_on_off()) {
                    Map<String, BluetoothGatt> instanceOFBluetoothGhatt = mainActivity.mBluetoothLeService.getConnectedBluetoothGhatt();
                    for (Map.Entry<String, BluetoothGatt> entry : instanceOFBluetoothGhatt.entrySet()) {
                        if (entry.getValue() != null) {
                            BluetoothGatt ghatt = entry.getValue();
                            ghatt.disconnect();
                            ghatt.close();
                            ghatt = null;
                        }
                    }
                }
                dialog.dismiss();
                mainActivity.preferenceHelper.resetPreferenceData();
                Intent mIntent = new Intent(getActivity(), LoginActivity.class);
                startActivity(mIntent);
                mainActivity.finish();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Remove and Implement later commented.
     */
  /*  private void disconnectAllDevices() {
        if (getBluetoothAdapter() != null) {
            BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();
            if (bluetoothAdapter.isEnabled()) {
              BleManager.getInstance().disconnectAllDevice();
              if(main_Activity_connectedDevicesAliasList!=null){
                  main_Activity_connectedDevicesAliasList.clear();
              }
            }
        }
    }*/


    /**
     * Google BLE library.
     */
    private void getListOfConnectedDevices() {
        if (ble_on_off()) {
            if (mainActivity.mBluetoothLeService != null) {
                List<BluetoothDevice> connectedDevicesList = mainActivity.mBluetoothLeService.getListOfConnectedDevices();
                if ((connectedDevicesList != null) && (connectedDevicesList.size() > 0)) {
                    for (BluetoothDevice bluetoothDevice : connectedDevicesList) {
                        if ((bluetoothDevice != null) && (bluetoothDevice.getName() != null) && (bluetoothDevice.getName().startsWith("Succorfish SC2"))) {
                            CustBluetootDevices custBluetootDevices = new CustBluetootDevices();
                            custBluetootDevices.setBleAddress(bluetoothDevice.getAddress());
                            custBluetootDevices.setDataObtained("");
                            custBluetootDevices.setConnected(true);
                            if (bluetoothDevice.getName() != null) {
                                custBluetootDevices.setDeviceName(bluetoothDevice.getName());
                            } else {
                                custBluetootDevices.setDeviceName("NA");
                            }
                            customBluetoothDeviceList.add(custBluetootDevices);
                            fragmentScanAdapter.notifyDataSetChanged();
                        }

                    }
                }
            }
        } else {
            customBluetoothDeviceList.clear();
            fragmentScanAdapter.notifyDataSetChanged();
            dialogProvider.errorDialog("Turn on Bluetooth");
        }
    }

    public class Fragment_ConnectionTimeOutTimer extends CountDownTimer {
        /**
         * @param millisInFuture    The number of millis in the future from the call
         *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
         *                          is called.
         * @param countDownInterval The interval along the way to receive
         *                          {@link #onTick(long)} callbacks.
         */
        public Fragment_ConnectionTimeOutTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            cancelProgressDialog();
        }
    }

    private void cancelTimerFragmentScanTimer() {
        if (fragmentScanConnectionTimeOutTimer != null) {
            fragmentScanConnectionTimeOutTimer.cancel();
            fragmentScanConnectionTimeOutTimer = null;
        }
    }

    /**
     * get the postion of the Device from the Arraylist.
     */
    private int getPositionOfItemFromBleAddress(String bleAddress_For_Position) {
        int result = -1;
        CustBluetootDevices custBluetootDevices = new CustBluetootDevices();
        custBluetootDevices.setBleAddress(bleAddress_For_Position);
        if (customBluetoothDeviceList.contains(custBluetootDevices)) {
            result = customBluetoothDeviceList.indexOf(custBluetootDevices);
        } else {
            result = -1;
        }
        return result;
    }

}
