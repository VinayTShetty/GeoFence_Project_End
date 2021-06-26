package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.evergreen.ble.advertisement.AdvertisementRecord;
import com.evergreen.ble.advertisement.ManufactureData;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.PowerSocketUtils.ByteConverter;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;

import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentSettingBridgeConnection extends Fragment {

    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    DeviceListAdapter mDeviceListAdapter;
    ArrayList<VoBluetoothDevices> mArrayDeviceBridgeList = new ArrayList<>();
    boolean isConnectionRequestSend = false;
    boolean isDisconnectConnectRequest = false;
    startDeviceConnectTimer mStartDeviceConnectTimer;

    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recyclerViewFoundDevices)
    RecyclerView recyclerView;
    @BindView(R.id.linearLayoutSearchingDevices)
    LinearLayout linearLayoutSearchingDevices;
    @BindView(R.id.linearLayoutNodeviceFound)
    LinearLayout linearLayoutNoDeviceFound;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_setting_bridge_connection, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.isAddDeviceScan = false;
        isConnectionRequestSend = false;
        mActivity.mTextViewTitle.setText(R.string.frg_settings_bridge_connection);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_refresh_icon_white);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mActivity.isFromBridgeConnection = true;
        mStartDeviceConnectTimer = new startDeviceConnectTimer(13000, 1000);
        mArrayDeviceBridgeList = new ArrayList<>();
        getDeviceListData();

        mActivity.mImageViewBack.setOnClickListener(view -> mActivity.onBackPressed());

        mActivity.mImageViewAddDevice.setOnClickListener(v -> {
            beginDeviceScan();
        });

        swipeRefreshLayout = mViewRoot.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            //
            beginDeviceScan();
            swipeRefreshLayout.setRefreshing(true);
        });

        /*Device connections, scan call back*/
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {
            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (isAdded()) {
                    String advHexString = mVoBluetoothDevices.getDeviceHexData();
                    if (advHexString != null && !advHexString.isEmpty()) {
                        byte[] advBytes = ByteConverter.getByteArrayFromHexString(advHexString);
                        // Todo - Check for General Discoverable bit from Adv Flag
                        AdvertisementRecord advRecord = AdvertisementRecord.parse(advBytes);
                        if (advRecord != null) {
                            ManufactureData manufactureData = advRecord.getManufactureData();
                            if (manufactureData != null) {
                                int manufacturerID = ByteConverter.convertByteArrayToInt(manufactureData.id);
                                if (manufacturerID == 0x0A00) {
                                    boolean containsInScanDevice = false;

                                    // Todo check when device is reset
                                    if (mVoBluetoothDevices.getIsFromNotification()) {
                                        return;
                                    }

                                    for (VoBluetoothDevices device : mArrayDeviceBridgeList) {
                                        if (mVoBluetoothDevices.getDeviceAddress().equals(device.getDeviceAddress())) {
                                            containsInScanDevice = true;
                                            break;
                                        }
                                    }

                                    if (!containsInScanDevice) {
                                        swipeRefreshLayout.setRefreshing(false);
                                        mArrayDeviceBridgeList.add(mVoBluetoothDevices);
                                        mActivity.runOnUiThread(() -> {
                                            if (mDeviceListAdapter != null) {
                                                mDeviceListAdapter.notifyDataSetChanged();
                                            }
                                        });
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
                if (isAdded()) {
                    if (mArrayDeviceBridgeList.size() > 0) {
                        for (int j = 0; j < mArrayDeviceBridgeList.size(); j++) {
                            if (mArrayDeviceBridgeList.get(j).getDeviceAddress().equalsIgnoreCase(connectedDevice.getAddress())) {
                                mArrayDeviceBridgeList.get(j).setIsConnected(true);

                                mActivity.runOnUiThread(() -> {
                                    if (mDeviceListAdapter != null) {
                                        mDeviceListAdapter.notifyDataSetChanged();
                                    }

                                });
                                if (isConnectionRequestSend) {
                                    if (mStartDeviceConnectTimer != null) {
                                        mStartDeviceConnectTimer.cancel();
                                        mStartDeviceConnectTimer.onFinish();
                                    }
                                    showDeviceConnectedAlert();
                                }
                                break;
                            }
                        }
                    } else {
//                    getDeviceListData();
                    }
                }
            }

            @Override
            public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                    if (mArrayDeviceBridgeList.size() > 0) {
                        for (int j = 0; j < mArrayDeviceBridgeList.size(); j++) {
                            if (mArrayDeviceBridgeList.get(j).getDeviceAddress().equalsIgnoreCase(connectedDevice.getAddress())) {
                                mArrayDeviceBridgeList.get(j).setIsConnected(false);
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mDeviceListAdapter != null) {
                                            mDeviceListAdapter.notifyDataSetChanged();

                                        }
                                    }
                                });
                                if (isConnectionRequestSend) {
                                    if (!isDisconnectConnectRequest) {
                                        if (mStartDeviceConnectTimer != null) {
                                            mStartDeviceConnectTimer.cancel();
                                            mStartDeviceConnectTimer.onFinish();
                                        }
                                    } else {
                                        isDisconnectConnectRequest = false;
                                    }
                                }
                                break;
                            }
                        }
                    } else {
//                    getDeviceListData();
                    }
                }
            }

            @Override
            public void onError() {
            }
        });
        return mViewRoot;
    }

    private void beginDeviceScan() {
        if (!isDetached()) {
            mArrayDeviceBridgeList = new ArrayList<>();

            swipeRefreshLayout.setRefreshing(true);

            // Get Connected devices
            List<BluetoothDevice> connectedDevicesList = mActivity.mBluetoothLeService.getConnectedDevicesList();
            for (BluetoothDevice connectedDevice : connectedDevicesList) {
                if (connectedDevice.getName().equals("Vithamas Strip")) {
                    VoBluetoothDevices bluetoothDevice = new VoBluetoothDevices();
                    bluetoothDevice.setBluetoothDevice(connectedDevice);
                    bluetoothDevice.setDeviceName(connectedDevice.getName());
                    bluetoothDevice.setDeviceAddress(connectedDevice.getAddress());
                    bluetoothDevice.setIsConnected(true);
                    mArrayDeviceBridgeList.add(bluetoothDevice);
                    mDeviceListAdapter.notifyDataSetChanged();
                }
            }

            checkAdapterIsEmpty();

            mActivity.isFromBridgeConnection = true;
        }
    }

    /*Init Device list adapter*/
    private void getDeviceListData() {
        if (mDeviceListAdapter == null) {
            mDeviceListAdapter = new DeviceListAdapter();
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mDeviceListAdapter);
            mDeviceListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    checkAdapterIsEmpty();
                }
            });
        } else {
            mDeviceListAdapter.notifyDataSetChanged();
        }
        checkAdapterIsEmpty();
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

    @Override
    public void onStop() {
        super.onStop();
        mActivity.isFromBridgeConnection = false;
        recyclerView.setAdapter(null);
        mArrayDeviceBridgeList = null;
        mDeviceListAdapter = null;
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        if (mStartDeviceConnectTimer != null) {
            mStartDeviceConnectTimer.cancel();
        }
        unbinder.unbind();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }

    /*Check adapter is empty or not*/
    private void checkAdapterIsEmpty() {
        if (isAdded()) {
            if (mDeviceListAdapter != null) {
                if (mDeviceListAdapter.getItemCount() > 0) {
                    swipeRefreshLayout.setRefreshing(false);
                    showDeviceList();
                } else {
                    showSearchingDevicesLayout();
                }
            } else {
                showSearchingDevicesLayout();
            }
        }
    }

    /*Device list adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @NonNull
        @Override
        public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_bridge_connection_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DeviceListAdapter.ViewHolder holder, final int position) {
//            if (mArrayDeviceBridgeList.get(position).getDeviceHexData() != null && !mArrayDeviceBridgeList.get(position).getDeviceHexData().equalsIgnoreCase("") && !mArrayDeviceBridgeList.get(position).getDeviceHexData().equalsIgnoreCase("null")) {
//                mStrTempHex = mArrayDeviceBridgeList.get(position).getDeviceHexData().substring(30, 32);
//                System.out.println("mStrTempHex-" + mStrTempHex);
//                holder.mTextViewTemp.setText("Temp-" + BLEUtility.hexToDecimal(mStrTempHex) + "");
//            } else {
//                holder.mTextViewTemp.setText("");
//            }
            if (mArrayDeviceBridgeList.get(position).getDeviceName() != null && !mArrayDeviceBridgeList.get(position).getDeviceName().equalsIgnoreCase("")) {
                holder.mTextViewDeviceName.setText(mArrayDeviceBridgeList.get(position).getDeviceName());
            } else {
                holder.mTextViewDeviceName.setText("");
            }
            if (mArrayDeviceBridgeList.get(position).getIsConnected()) {
                holder.mTextViewConnect.setText(R.string.frg_setting_brg_conn_btn_disconnect);
            } else {
                holder.mTextViewConnect.setText(R.string.frg_setting_brg_conn_btn_connect);
            }
            if (mArrayDeviceBridgeList.get(position).getDeviceAddress() != null && !mArrayDeviceBridgeList.get(position).getDeviceAddress().equalsIgnoreCase("")) {
                holder.mTextViewDeviceId.setText(mArrayDeviceBridgeList.get(position).getDeviceAddress().replace(":", ""));
            } else {
                holder.mTextViewDeviceId.setText("");
            }
            holder.itemView.setOnClickListener(v -> {
                if (mArrayDeviceBridgeList != null) {
                    if (position < mArrayDeviceBridgeList.size()) {
                        try {
                            mActivity.isHardResetRequest = false;
                            mActivity.isIdentificationRequest = false;
                            if (!isConnectionRequestSend) {
                                isConnectionRequestSend = true;
                                if (mArrayDeviceBridgeList.get(position).getIsConnected()) {
                                    mActivity.showProgress("Disconnecting..", true);
                                    isDisconnectConnectRequest = false;
                                    mActivity.disconnectDevices(mArrayDeviceBridgeList.get(position).getBluetoothDevice(), false);
                                } else {
                                    if (mActivity.isDevicesConnected) {
                                        isDisconnectConnectRequest = true;
                                        mActivity.showProgress("Connecting..", true);
                                        if (mActivity.mBluetoothDevice != null) {
                                            mActivity.disconnectDevices(mActivity.mBluetoothDevice, false);
                                        }
                                        Timer innerTimer = new Timer();
                                        innerTimer.schedule(new TimerTask() {
                                            @Override
                                            public void run() {
                                                mActivity.ConnectDevices(mArrayDeviceBridgeList.get(position).getBluetoothDevice(), false);
                                            }
                                        }, 1000);
                                    } else {
                                        mActivity.showProgress("Connecting..", true);
                                        mActivity.ConnectDevices(mArrayDeviceBridgeList.get(position).getBluetoothDevice(), false);
                                    }
                                }
                                if (mStartDeviceConnectTimer != null) {
                                    mStartDeviceConnectTimer.start();
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayDeviceBridgeList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_bridge_connection_list_item_textview_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_bridge_connection_list_item_textview_connect)
            TextView mTextViewConnect;
            @BindView(R.id.raw_bridge_connection_list_item_textview_deviceid)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_bridge_connection_list_item_textview_temp)
            TextView mTextViewTemp;
            @BindView(R.id.raw_bridge_connection_list_item_imageview_device)
            ImageView mImageViewDevice;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Alert Dialog Device connection success */
    private void showDeviceConnectedAlert() {
        isConnectionRequestSend = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_setting_brg_conn_connected_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private class startDeviceConnectTimer extends CountDownTimer {

        public startDeviceConnectTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            isConnectionRequestSend = false;
            mActivity.hideProgress();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter.isEnabled()) {
            mActivity.isFromBridgeConnection = true;
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
    }
}