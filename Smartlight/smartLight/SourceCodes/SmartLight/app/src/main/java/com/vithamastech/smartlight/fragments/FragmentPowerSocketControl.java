package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.vithamastech.smartlight.Adapter.SwitchControlAdapter;
import com.vithamastech.smartlight.DialogFragementHelper.BottomSheetMenu;
import com.vithamastech.smartlight.DialogFragementHelper.BottomSheetSocketCustomization;
import com.vithamastech.smartlight.IconSelector;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketCustomObjects.PowerSocket;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;

import com.vithamastech.smartlight.PowerSocketUtils.Socket;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.interfaces.OnBluetoothStateChangeListener;
import com.vithamastech.smartlight.services.PowerSocketBLEService;
import com.vithamastech.smartlight.services.PowerSocketBLEEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketMQTTEventCallbacks;
import com.vithamastech.smartlight.services.PowerSocketMQTTService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.vithamastech.smartlight.MainActivity.mActivityPowerSocketSelected;
import static com.vithamastech.smartlight.MainActivity.mBluetoothManager;

public class FragmentPowerSocketControl extends Fragment {

    private final String TAG = this.getClass().getSimpleName();
    private RecyclerView recyclerView;
    private SwitchControlAdapter adapter = null;

    private PowerSocketBLEService powerSocketBLEService;
    private PowerSocketMQTTService powerSocketMQTTService;

    private PowerSocket powerSocket;

    private ScheduledExecutorService service;
    private ImageView imageViewBluetoothStatus, imageViewWifiStatus;

    private MainActivity activity;

    private RadioButton radioButtonGroupSwitchOn, radioButtonGroupSwitchOff;
    private LinearLayout rootLayout;
    private boolean isMqttSocketControlInProgress;
    private ProgressBar progressBar;
    private boolean previousMasterSwitchOffState;
    private boolean previousMasterSwitchOnState;

    private BluetoothAdapter bluetoothAdapter;
    private int transactionId = 3;

    public FragmentPowerSocketControl() {
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
        powerSocket = mActivityPowerSocketSelected;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_socket_control, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        activity.mImageViewBack.setVisibility(View.GONE);
        activity.mImageViewAddDevice.setVisibility(View.GONE);
        activity.mImageViewAddDevice.setImageResource(R.drawable.ic_refresh_icon_white);
        activity.showBackButton(true);
        activity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        powerSocketBLEService = PowerSocketBLEService.getInstance(activity.getApplicationContext());
        powerSocketMQTTService = PowerSocketMQTTService.getInstance(activity.getApplicationContext());

        recyclerView = view.findViewById(R.id.recyclerView);
        imageViewWifiStatus = view.findViewById(R.id.imageViewWifiStatus);
        imageViewBluetoothStatus = view.findViewById(R.id.imageViewBluetoothStatus);
        radioButtonGroupSwitchOn = view.findViewById(R.id.radioButtonOn);
        radioButtonGroupSwitchOff = view.findViewById(R.id.radioButtonOff);
        progressBar = view.findViewById(R.id.progressBarWaitMqtt);
        rootLayout = view.findViewById(R.id.rootLayout);

        recyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(activity);
        recyclerView.setLayoutManager(layoutManager);

        List<Socket> socketListOld = getSocketList();
        console.log("xskasbxkjabsx_old", new Gson().toJson(socketListOld));

        List<Socket> socketList = loadSocketConfigDetails();
        console.log("xskasbxkjabsx_new", new Gson().toJson(socketList));

        adapter = new SwitchControlAdapter(socketList);
        recyclerView.setAdapter(adapter);

        adapter.setOnSocketAlarmClickListener((socket, itemPosition, state) -> {
            BottomSheetMenu bottomSheetMenu = BottomSheetMenu.newInstance();
            bottomSheetMenu.show(getFragmentManager(), BottomSheetMenu.TAG);
            bottomSheetMenu.setOnMenuItemClickListener(new BottomSheetMenu.OnMenuItemClickListener() {
                @Override
                public void onAlarmMenuClicked() {
                    FragmentPowerSocketAlaramSchedule mFragmentAddDevice = new FragmentPowerSocketAlaramSchedule();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(getResources().getString(R.string.power_socket_object_Fragment_PowerSocket_AlaramSchedule), powerSocket);
                    bundle.putSerializable(getResources().getString(R.string.socket_object_Fragment_PowerSocket_AlaramSchedule), socket);
                    activity.replacesFragment(mFragmentAddDevice, true, bundle, 0);
                }

                @Override
                public void onSocketCustomizationClicked() {
                    BottomSheetSocketCustomization bottomSheetSocketCustomization = BottomSheetSocketCustomization.newInstance();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("SelectedSocket", socket);
                    bottomSheetSocketCustomization.setArguments(bundle);
                    bottomSheetSocketCustomization.show(getFragmentManager(), BottomSheetSocketCustomization.TAG);
                    bottomSheetSocketCustomization.setOnSocketCustomizationSelectedListener(new BottomSheetSocketCustomization.OnSocketCustomizationSelectedListener() {
                        @Override
                        public void onSocketCustomized(IconSelector iconSelector, String newSocketName) {
                            socket.socketName = newSocketName;
                            socket.imageType = iconSelector.source;
                            adapter.update();

                            updateSocketConfig(socket);
                        }
                    });
                }
            });
        });

        adapter.setOnSocketStateChangeListener((socket, itemPosition) -> {
            controlSingleSwitch(socket);

            if (socket.socketState == 0x01) {
                socket.socketState = 0x00;
            } else if (socket.socketState == 0x00) {
                socket.socketState = 0x01;
            }

            if (isSocketCommunicationAvailable()) {
                socket.shouldWaitForOutput = true;              // Call by reference

                List<Socket> socketListFromDisplay = adapter.getSocketsList();
                for (Socket socketFromDisplay : socketListFromDisplay) {
                    socketFromDisplay.isEnabled = false;
                }

                radioButtonGroupSwitchOn.setEnabled(false);
                radioButtonGroupSwitchOff.setEnabled(false);
            }

            adapter.update();
        });

        radioButtonGroupSwitchOn.setOnClickListener((v) -> {
            if (previousMasterSwitchOnState != radioButtonGroupSwitchOn.isChecked()) {
                byte state = 0x01;
                controlAllSwitches((byte) state);

                // UI Update
                radioButtonGroupSwitchOff.setChecked(true);
                radioButtonGroupSwitchOn.setChecked(false);

                if (isSocketCommunicationAvailable()) {
                    List<Socket> socketListFromDisplay = adapter.getSocketsList();
                    for (Socket socketFromDisplay : socketListFromDisplay) {
                        socketFromDisplay.isEnabled = false;
                    }
                    adapter.update();

                    radioButtonGroupSwitchOn.setEnabled(false);
                    radioButtonGroupSwitchOff.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                }

                previousMasterSwitchOnState = radioButtonGroupSwitchOn.isChecked();
            }
        });

        radioButtonGroupSwitchOff.setOnClickListener((v) -> {
            if (previousMasterSwitchOffState != radioButtonGroupSwitchOff.isChecked()) {
                byte state = 0x00;
                controlAllSwitches((byte) state);

                //UI Update
                radioButtonGroupSwitchOff.setChecked(false);
                radioButtonGroupSwitchOn.setChecked(true);

                if (isSocketCommunicationAvailable()) {
                    List<Socket> socketListFromDisplay = adapter.getSocketsList();
                    for (Socket socketFromDisplay : socketListFromDisplay) {
                        socketFromDisplay.isEnabled = false;
                    }
                    adapter.update();
                    radioButtonGroupSwitchOn.setEnabled(false);
                    radioButtonGroupSwitchOff.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);
                }

                previousMasterSwitchOffState = radioButtonGroupSwitchOff.isChecked();
            }
        });

        //Delete alarm of the PowerSockets if the time has elapsed.
        activity.deletePoweSocketAlaram_If_TimeElapsed("" + System.currentTimeMillis() / 1000);
    }

    private void controlSingleSwitch(Socket socket) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        byte switchId = (byte) socket.socketId;
        byte socketState = (byte) socket.socketState;

        int mqttState = 0;                     // Connected
        int bluetoothState = 0;                // Connected
        // Check if Bluetooth is Enabled
        if (bluetoothAdapter.isEnabled()) {
            if (powerSocketBLEService.isDeviceConnected(powerSocket.getBleAddress())) {
                powerSocketBLEService.controlSingleSwitch(powerSocket, switchId, socketState, transactionId);
            } else {
                bluetoothState = 1;           // BLE device not connected
            }
        } else {
            bluetoothState = 2;               // Bluetooth Adapter is disabled
        }

        if (bluetoothState == 1 || bluetoothState == 2) {
            // Check is WiFi is enabled
            if (powerSocket.isWifiConfigured) {
                if (powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.controlSingleSwitch(powerSocket, switchId, socketState, transactionId);
                    isMqttSocketControlInProgress = true;
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

    private void controlAllSwitches(byte state) {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        int mqttState = 0;                     // Connected
        int bluetoothState = 0;                // Connected
        // Check if Bluetooth is Enabled
        if (bluetoothAdapter.isEnabled()) {
            if (powerSocketBLEService.isDeviceConnected(powerSocket.getBleAddress())) {
                powerSocketBLEService.controlAllSwitches(powerSocket, state, transactionId);
            } else {
                bluetoothState = 1;           // BLE device not connected
            }
        } else {
            bluetoothState = 2;               // Bluetooth Adapter is disabled
        }

        if (bluetoothState == 1 || bluetoothState == 2) {
            // Check is WiFi is enabled
            if (powerSocket.isWifiConfigured) {
                if (powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.controlAllSwitches(powerSocket, state, transactionId);
                    isMqttSocketControlInProgress = true;
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

    PowerSocketMQTTEventCallbacks powerSocketMQTTEventCallbacks = new PowerSocketMQTTEventCallbacks() {
        @Override
        public void onMQTTConnected() {
            console.log(TAG, "Connected to MQTT Broker");
            imageViewWifiStatus.setImageResource(R.drawable.ic_wifi_connected);
            if (powerSocket != null) {
                if (powerSocket.isWifiConfigured) {
                    powerSocketMQTTService.subscribe(powerSocket);
                }
            }
        }

        @Override
        public void onMQTTDisconnected() {
            console.log(TAG, "Disconnected from MQTT Broker");
            imageViewWifiStatus.setImageResource(R.drawable.ic_wifi_disconnected);
            if (!isNetworkAvailable()) {
                imageViewWifiStatus.setImageResource(R.drawable.mqtt_not_configured);
            }

            if (!isSocketCommunicationAvailable()) {
                // Disable UI switches
                disableSwitchUI();
            }
        }

        @Override
        public void onMQTTConnectionFailed() {
            console.log(TAG, "MQTT Connection failed");
            imageViewWifiStatus.setImageResource(R.drawable.ic_wifi_disconnected);
        }

        @Override
        public void onMQTTException() {
            console.log(TAG, "MQTT Exception");
        }

        @Override
        public void onMQTTTimeout(String deviceAddress) {
            console.log(TAG, "MQTT Timeout => " + deviceAddress);
            activity.hideProgress();
            activity.mUtility.errorDialog("MQTT Timeout. Please make sure your power socket is turned on and try again",
                    1, true);

            List<Socket> socketListFromDisplay = adapter.getSocketsList();
            for (Socket socketFromDisplay : socketListFromDisplay) {
                socketFromDisplay.shouldWaitForOutput = false;
                socketFromDisplay.isEnabled = true;
            }
            adapter.update();

            progressBar.setVisibility(View.GONE);
            radioButtonGroupSwitchOff.setEnabled(true);
            radioButtonGroupSwitchOn.setEnabled(true);

            previousMasterSwitchOffState = radioButtonGroupSwitchOff.isChecked();
            previousMasterSwitchOnState = radioButtonGroupSwitchOn.isChecked();
        }

        @Override
        public void onTopicSubscribed(String deviceAddress) {
            super.onTopicSubscribed(deviceAddress);
            // Set UTC Time
            if (!bluetoothAdapter.isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                enableSwitchUI();
                if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.setCurrentTime(powerSocket, true);
                }
            }
        }

        @Override
        public void onCurrentTimeSet(String deviceAddress) {
            super.onCurrentTimeSet(deviceAddress);
            if (!bluetoothAdapter.isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                console.log("jsnjsnjkssjknsjksjknskj", "here");
                if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
                    powerSocketMQTTService.checkSocketDiagnostics(powerSocket, transactionId);
                }
            }
        }

        @Override
        public void onCheckSocketDiagnostics(String deviceAddress, int transactionId, byte[] data) {
            super.onCheckSocketDiagnostics(deviceAddress, transactionId, data);
            console.log(TAG, "Socket Diagnostics Complete => " + deviceAddress + " " + ByteConverter.getHexStringFromByteArray(data, true));

            if (!mBluetoothManager.getAdapter().isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                updateSocketList(data);
                if (activity.isInitialized) {
                    activity.isInitialized = false;
                    if (powerSocketMQTTService != null && powerSocketMQTTService.isConnected()) {
                        powerSocketMQTTService.askAlaramDetailsFrom_socket_Mqtt(powerSocket);
                    }
                }
            } else {
                if (FragmentPowerSocketControl.this.transactionId != transactionId) {
                    updateSocketList(data);
                }
            }
        }

        @Override
        public void onSingleSocketStateChange(String deviceAddress, int transactionId, byte[] data) {
            super.onSingleSocketStateChange(deviceAddress, transactionId, data);
            console.log(TAG, "Socket State Change => " + deviceAddress + " " + ByteConverter.getHexStringFromByteArray(data, true));
            activity.hideProgress();

            if (!mBluetoothManager.getAdapter().isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                updateSocketList(data);
            } else {
                if (FragmentPowerSocketControl.this.transactionId != transactionId) {
                    updateSocketList(data);
                }
            }
        }

        @Override
        public void onAllSocketsStateChange(String deviceAddress, int transactionId, byte[] data) {
            super.onAllSocketsStateChange(deviceAddress, transactionId, data);
            console.log(TAG, "All Sockets State Change => " + deviceAddress + " " + ByteConverter.getHexStringFromByteArray(data, true));
            activity.hideProgress();

            if (!mBluetoothManager.getAdapter().isEnabled() || !powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                updateSocketList(data);
            } else {
                if (FragmentPowerSocketControl.this.transactionId != transactionId) {
                    updateSocketList(data);
                }
            }
        }
    };

    PowerSocketBLEEventCallbacks powerSocketBLEEventCallbacks = new PowerSocketBLEEventCallbacks() {
        @Override
        public void onDeviceConnected(String deviceAddress) {

        }

        @Override
        public void onDeviceDisconnected(String deviceAddress) {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                if (powerSocket.bleAddress.equalsIgnoreCase(deviceAddress)) {
                    activity.getSupportActionBar().setSubtitle("Device disconnected");
                }
                imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);

                if (!isSocketCommunicationAvailable()) {
                    disableSwitchUI();
                }
            }
        }

        @Override
        public void onDeviceConnectionFailed(String deviceAddress) {
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);
        }

        @Override
        public void onAuthenticationSuccess(String deviceAddress) {
            super.onAuthenticationSuccess(deviceAddress);
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_connected);
            enableSwitchUI();
            if (powerSocket != null) {
                activity.getSupportActionBar().setSubtitle(powerSocket.bleAddress);
                // Get the status of all sockets
                powerSocketBLEService.checkSocketDiagnostics(powerSocket, transactionId);
            }
        }

        @Override
        public void onAuthenticationFailure(String deviceAddress) {
            super.onAuthenticationFailure(deviceAddress);
            if (!isDetached()) {
                activity.mUtility.AlertDialog("Authentication failed. Please check whether the smart light app is associated to the device and try again.");
                if (powerSocketBLEService.isDeviceConnected(deviceAddress)) {
                    powerSocketBLEService.disconnect(deviceAddress);
                }
            }
        }

        @Override
        public void onCheckSocketDiagnostics(String deviceAddress, int transactionId, byte[] data) {
            super.onCheckSocketDiagnostics(deviceAddress, transactionId, data);
            updateSocketList(data);
            if (activity.isInitialized) {
                activity.isInitialized = false;
                powerSocketBLEService.askAlaramDetailsFrom_socket(mActivityPowerSocketSelected);
            }
        }

        @Override
        public void onSingleSocketStateChange(String deviceAddress, int transactionId, byte[] data) {
            super.onSingleSocketStateChange(deviceAddress, transactionId, data);
            console.log(TAG, "Single Socket State Change = " + ByteConverter.getHexStringFromByteArray(data, true));
            activity.hideProgress();
            updateSocketList(data);
        }

        @Override
        public void onAllSocketsStateChange(String deviceAddress, int transactionId, byte[] data) {
            super.onAllSocketsStateChange(deviceAddress, transactionId, data);
            console.log(TAG, "All Sockets State Change = " + ByteConverter.getHexStringFromByteArray(data, true));
            updateSocketList(data);
        }
    };

    OnBluetoothStateChangeListener bluetoothStateChangeListener = new OnBluetoothStateChangeListener() {
        @Override
        public void onBluetoothOff() {
            Toast.makeText(activity.getApplicationContext(), "Bluetooth is disabled", Toast.LENGTH_SHORT).show();
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_gray);
            activity.getSupportActionBar().setSubtitle("Bluetooth Disabled");

            if (!isSocketCommunicationAvailable()) {
                // Disable UI switches
                disableSwitchUI();
            }
        }

        @Override
        public void onBluetoothTurningOff() {
            // Since Bluetooth is switched off, some devices are not disconnected.
            if (powerSocketBLEService.isDeviceConnected(mActivityPowerSocketSelected.bleAddress)) {
                powerSocketBLEService.disconnect(mActivityPowerSocketSelected.bleAddress);
            }
        }

        @Override
        public void onBluetoothOn() {
            Toast.makeText(activity.getApplicationContext(), "Bluetooth is on", Toast.LENGTH_SHORT).show();
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);
        }

        @Override
        public void onBluetoothTurningOn() {

        }
    };

    private void enableSwitchUI() {
        // Enable UI power socket switches
        //---------------------------Common Function 1--------------------------------------------
        List<Socket> socketListFromDisplay = adapter.getSocketsList();
        for (Socket socketFromDisplay : socketListFromDisplay) {
            socketFromDisplay.shouldWaitForOutput = true;
            socketFromDisplay.isEnabled = false;
        }
        adapter.update();

        radioButtonGroupSwitchOff.setEnabled(false);
        radioButtonGroupSwitchOn.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
        //-----------------------------------------------------------------------------------------
    }

    private void disableSwitchUI() {
        // -----------------------Common Function 2 ---------------------------------------------------------------
        previousMasterSwitchOffState = radioButtonGroupSwitchOff.isChecked();
        previousMasterSwitchOnState = radioButtonGroupSwitchOn.isChecked();

        List<Socket> socketListFromDisplay = adapter.getSocketsList();
        for (Socket socketFromDisplay : socketListFromDisplay) {
            socketFromDisplay.shouldWaitForOutput = false;
        }

        adapter.update();

        progressBar.setVisibility(View.GONE);

        previousMasterSwitchOffState = radioButtonGroupSwitchOff.isChecked();
        previousMasterSwitchOnState = radioButtonGroupSwitchOn.isChecked();
        // ----------------------------------------------------------------------------------------------------------
    }

    @Override
    public void onStart() {
        super.onStart();

        activity.setOnBluetoothStateChangeListener(bluetoothStateChangeListener);

        if (powerSocket != null) {
            activity.getSupportActionBar().setTitle(powerSocket.bleName);
            activity.getSupportActionBar().setSubtitle(powerSocket.bleAddress);
        }

        // Disable UI switches
        disableSwitchUI();

        if (bluetoothAdapter.isEnabled()) {
            if (powerSocket.bleAddress != null && !powerSocket.bleAddress.isEmpty()) {
                if (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                    imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_connected);
                    // Enable UI power socket switches
                    enableSwitchUI();
                    powerSocketBLEService.checkSocketDiagnostics(powerSocket, transactionId);
                } else {
                    imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_disconnected);
                }
            } else {
                imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_gray);
            }
        } else {
            imageViewBluetoothStatus.setImageResource(R.drawable.ic_bluetooth_gray);
        }

        powerSocketBLEService.setOnPowerSocketEventCallbacks(powerSocketBLEEventCallbacks);
        //--------------------------------------------------------------------------------------
        // For scheduling of tasks
        service = Executors.newScheduledThreadPool(1);
        service.scheduleAtFixedRate(new BLEConnectionTask(powerSocketBLEService, powerSocket), 0, 10, TimeUnit.SECONDS);

        //--------------------------------------------------------------------------------------
        if (powerSocket.isWifiConfigured) {
            // Register for Network Connectivity (Internet) change callbacks
            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback);
            } else {
                NetworkRequest request = new NetworkRequest.Builder()
                        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        .build();
                connectivityManager.registerNetworkCallback(request, networkCallback);
            }
        } else {
            imageViewWifiStatus.setImageResource(R.drawable.ic_wifi_disconnected);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        powerSocketMQTTService.cancelPendingMQTTTimeouts();

        if (service != null) {
            if (!service.isShutdown()) {
                service.shutdownNow();
            }
        }

        List<String> connectingDevicesAddressList = powerSocketBLEService.getConnectingDevicesAddressList();
        for (String address : connectingDevicesAddressList) {
            powerSocketBLEService.disconnect(address);
        }

        powerSocketMQTTService.setOnPowerSocketMQTTEventCallbacks(null);

        if (powerSocket.isWifiConfigured) {
            // Unregister Network Connectivity (Internet) change callbacks
            ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
            connectivityManager.unregisterNetworkCallback(networkCallback);
        }

        powerSocketBLEService.setOnPowerSocketEventCallbacks(null);
        activity.setOnDevicesStatusChange(null);
        activity.setOnBluetoothStateChangeListener(null);
    }

    public static class BLEConnectionTask implements Runnable {
        private PowerSocketBLEService powerSocketBLEService;
        private PowerSocket powerSocket;

        private BLEConnectionTask(PowerSocketBLEService powerSocketBLEService, PowerSocket powerSocket) {
            this.powerSocketBLEService = powerSocketBLEService;
            this.powerSocket = powerSocket;
        }

        @Override
        public void run() {
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled()) {
                if (!powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                    console.log("PowerSocketControlActivity", "Ready to connect to " + powerSocket.bleAddress);
                    powerSocketBLEService.connect(powerSocket);
                } else {
                    console.log("PowerSocketControlActivity", "Already Connected");
                }
            } else {
                console.log("PowerSocketControlActivity", "Bluetooth is off");
            }
        }
    }

    private List<Socket> getSocketList() {
        List<Socket> retVal = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            retVal.add(new Socket(i, 0, "Socket" + (i + 1)));
        }
        return retVal;
    }

    private void updateSocketList(byte[] socketStateArray) {
        activity.hideProgress();

        List<Socket> socketListFromDisplay = adapter.getSocketsList();
        for (int i = 0; i < socketStateArray.length; i++) {
            Socket socketFromDisplay = socketListFromDisplay.get(i);
            socketFromDisplay.socketState = socketStateArray[i];
            socketFromDisplay.shouldWaitForOutput = false;
            socketFromDisplay.isEnabled = true;
        }
        adapter.update();

        radioButtonGroupSwitchOff.setEnabled(true);
        radioButtonGroupSwitchOn.setEnabled(true);
        progressBar.setVisibility(View.GONE);

        boolean isMasterSwitchOff = false;
        for (byte socketState : socketStateArray) {
            if (socketState == 0x00) {
                isMasterSwitchOff = true;
                break;
            }
        }
        if (isMasterSwitchOff) {
            radioButtonGroupSwitchOff.setChecked(true);
        } else {
            radioButtonGroupSwitchOn.setChecked(true);
        }

        previousMasterSwitchOffState = radioButtonGroupSwitchOff.isChecked();
        previousMasterSwitchOnState = radioButtonGroupSwitchOn.isChecked();
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

        Snackbar.make(rootLayout, errorMessage, 5000)
                .setAction("OK", v -> {

                })
                .setText(errorMessage)
                .setActionTextColor(getResources().getColor(android.R.color.white))
                .show();
    }

    ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onAvailable(@NonNull Network network) {
            // network available
            console.log(TAG, "Network Available");
            activity.runOnUiThread(() -> {
                if (powerSocket.isWifiConfigured) {
                    // MQTT Broker Connection
                    if (powerSocketMQTTService.isConnected()) {
                        imageViewWifiStatus.setImageResource(R.drawable.ic_wifi_connected);

                        if (!bluetoothAdapter.isEnabled() || !powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress)) {
                            enableSwitchUI();
                            powerSocketMQTTService.checkSocketDiagnostics(powerSocket, transactionId);
                        }
                    } else {
                        imageViewWifiStatus.setImageResource(R.drawable.ic_wifi_disconnected);
                        powerSocketMQTTService.initialize(powerSocket);
                    }
                    powerSocketMQTTService.setOnPowerSocketMQTTEventCallbacks(powerSocketMQTTEventCallbacks);
                }
            });
        }

        @Override
        public void onLost(@NonNull Network network) {
            // network unavailable
            console.log(TAG, "Network Offline");
            activity.runOnUiThread(() -> {
                        imageViewWifiStatus.setImageResource(R.drawable.mqtt_not_configured);
                        activity.hideProgress();

                        if (isMqttSocketControlInProgress) {
                            isMqttSocketControlInProgress = false;
                            activity.mUtility.errorDialog("Network unavailable.\n Please make sure either Bluetooth or Network is available for controlling socket",
                                    1, true);
                        }
                    }
            );
        }
    };

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    private boolean isSocketCommunicationAvailable() {
        return (powerSocketBLEService.isDeviceConnected(powerSocket.bleAddress) && bluetoothAdapter.isEnabled())
                || (powerSocket.isWifiConfigured && powerSocketMQTTService.isConnected());
    }

    private List<Socket> loadSocketConfigDetails() {
        ArrayList<Socket> retVal = new ArrayList<Socket>();
        DataHolder dataHolder;
        try {
            String query = "select * from " + DBHelper.mTableSocketDeviceDtl + " where " +
                    DBHelper.mFieldTableSocketDeviceDtlDeviceId + " = " + "'" + mActivityPowerSocketSelected.getId() + "'";
            dataHolder = activity.mDbHelper.readData(query);
            if (dataHolder != null) {
                ArrayList<LinkedHashMap<String, String>> listHolder = dataHolder.get_Listholder();
                for (int i = 0; i < listHolder.size(); i++) {
                    LinkedHashMap<String, String> dataContainer = listHolder.get(i);
                    Socket socket = new Socket();
                    socket.socketId = Integer.parseInt(dataContainer.get(DBHelper.mFieldTableSocketDeviceDtlSocketId));
                    socket.imageType = Integer.parseInt(dataContainer.get(DBHelper.mFieldTableSocketDeviceDtlImageType));
                    socket.socketName = dataContainer.get(DBHelper.mFieldTableSocketDeviceDtlSocketName);
                    retVal.add(socket);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return retVal;
    }

    private void updateSocketConfig(Socket socket) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.mFieldTableSocketDeviceDtlSocketName, socket.socketName);
        contentValues.put(DBHelper.mFieldTableSocketDeviceDtlImageType, socket.imageType);

        activity.mDbHelper.updateRecord(DBHelper.mTableSocketDeviceDtl, contentValues,
                DBHelper.mFieldTableSocketDeviceDtlDeviceId + "=?" + "AND " + DBHelper.mFieldTableSocketDeviceDtlSocketId + "=?",
                new String[]{String.valueOf(powerSocket.id), String.valueOf(socket.socketId)});
    }
}