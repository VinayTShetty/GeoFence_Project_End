package com.succorfish.depthntemp.fragnments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MapsActivity;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.helper.BLEUtility;
import com.succorfish.depthntemp.interfaces.onAlertDialogCallBack;
import com.succorfish.depthntemp.interfaces.onDeviceConnectionStatusChange;
import com.succorfish.depthntemp.vo.VoBluetoothDevices;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentDeviceList extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_device_list_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_device_list_textview_nodevice)
    TextView mTextViewNoDeviceFound;
    @BindView(R.id.fragment_device_list_relativelayout_nodevice)
    RelativeLayout mRelativeLayoutNoDevice;
    ProgressBar mProgressBar;

    ArrayList<VoBluetoothDevices> mArrayListAddDevice = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    boolean isConnectionRequestSend = false;
    boolean isDisconnectConnectRequest = false;
    startDeviceConnectTimer mStartDeviceConnectTimer;
    scanTimeout mScanTimeout;
    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_device_list, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mProgressBar = (ProgressBar) mViewRoot.findViewById(R.id.fragment_device_list_progress);
        mSwipeRefreshLayout = (SwipeRefreshLayout) mViewRoot.findViewById(R.id.fragment_add_device_swipeRefreshLayout);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_sync);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.dive_one,
                R.color.colorWhite, R.color.dive_two);

        System.out.println("JD-ON_REFRESH_DEVICE");
        isConnectionRequestSend = false;
        mActivity.RescanDevice(true);
        if (!mActivity.getScanning()) {
            mActivity.startDeviceScan();
        }
        mStartDeviceConnectTimer = new startDeviceConnectTimer(13000, 1000);
        mScanTimeout = new scanTimeout(40000, 1000);
        if (mScanTimeout != null) {
            mScanTimeout.start();
        }
        mArrayListAddDevice = new ArrayList<>();
        mArrayListAddDevice.addAll(mActivity.mLeDevices);
        mDeviceListAdapter = null;
        getDeviceListData();
        if (mActivity.isNeverAskPermissionCheck) {
            mActivity.mUtility.errorDialogWithYesNoCallBack("Permissions Required", "You have forcefully denied GPS Location permissions. Please allow them from setting", "Settings", "Cancel", false, 3, new onAlertDialogCallBack() {
                @Override
                public void PositiveMethod(DialogInterface dialog, int id) {
                    try {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", mActivity.getPackageName(), null));
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void NegativeMethod(DialogInterface dialog, int id) {

                }
            });
        } else {
            if (!mActivity.isLocationEnabled(mActivity)) {
                mActivity.mUtility.errorDialogWithYesNoCallBack("Turn on GPS Location.", "Since Android 6.0 the system requires access to device's location in order to scan bluetooth devices.", "Ok", "Cancel", false, 3, new onAlertDialogCallBack() {
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
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mSwipeRefreshLayout.setRefreshing(false);
                mActivity.mLeDevices.clear();
                if (mActivity.isDevicesConnected) {
                    if (mActivity.mVoBluetoothDevicesConnected != null) {
                        mActivity.mVoBluetoothDevicesConnected.setIsConnectable(true);
                        mActivity.mLeDevices.add(mActivity.mVoBluetoothDevicesConnected);
                    }
                }
                mArrayListAddDevice = new ArrayList<>();
                mArrayListAddDevice.addAll(mActivity.mLeDevices);
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mDeviceListAdapter != null) {
                            mDeviceListAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });

//        mActivity.mTextViewAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                System.out.println("JD SCAN" + mActivity.getScanning());
//                if (mActivity.getScanning()) {
//                    mActivity.mTextViewAdd.setText("SCAN");
//                    mActivity.stopScan(true);
//                } else {
//                    mArrayListAddDevice = new ArrayList<>();
//                    mActivity.mLeDevices = new ArrayList<>();
//                    if (mDeviceListAdapter != null) {
//                        mDeviceListAdapter.notifyDataSetChanged();
//                    }
//                    mActivity.mTextViewAdd.setText("STOP");
//                    mActivity.startDeviceScan();
//                }
//            }
//        });

//        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mActivity.onBackPressed();
//            }
//        });
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (isAdded()) {
                    boolean containsInScanDevice = false;
                    for (VoBluetoothDevices device : mArrayListAddDevice) {
                        if (mVoBluetoothDevices.getDeviceAddress().equals(device.getDeviceAddress())) {
                            containsInScanDevice = true;
                            break;
                        }
                    }
                    if (!containsInScanDevice) {
                        mArrayListAddDevice.add(mVoBluetoothDevices);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mDeviceListAdapter != null) {
                                    mDeviceListAdapter.notifyDataSetChanged();
                                }
                            }
                        });

                    }
                }
            }

            @Override
            public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                    System.out.println("BridgeConnection Connect");
                    if (mArrayListAddDevice.size() > 0) {
                        for (int j = 0; j < mArrayListAddDevice.size(); j++) {
                            if (mArrayListAddDevice.get(j).getDeviceAddress().equalsIgnoreCase(connectedDevice.getAddress())) {
                                mArrayListAddDevice.get(j).setIsConnected(true);
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mDeviceListAdapter != null) {
                                            System.out.println("BridgeConnection Connect Notify");
                                            mDeviceListAdapter.notifyDataSetChanged();
                                        }

                                    }
                                });
                                if (isConnectionRequestSend) {
                                    if (mStartDeviceConnectTimer != null) {
                                        mStartDeviceConnectTimer.cancel();
//                                        mStartDeviceConnectTimer.onFinish();
                                    }
                                    isConnectionRequestSend = false;
//                                    showDeviceConnectedAlert();
                                }

                                break;
                            }
                        }
                    } else {
                        System.out.println("BridgeConnection Connect Refresh List");
//                    getDeviceListData();
                    }
                }
            }

            @Override
            public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                    System.out.println("BridgeConnection DisConnect");
                    if (mArrayListAddDevice.size() > 0) {
                        for (int j = 0; j < mArrayListAddDevice.size(); j++) {
                            if (mArrayListAddDevice.get(j).getDeviceAddress().equalsIgnoreCase(connectedDevice.getAddress())) {
                                mArrayListAddDevice.get(j).setIsConnected(false);
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mDeviceListAdapter != null) {
                                            mDeviceListAdapter.notifyDataSetChanged();
                                            System.out.println("BridgeConnection DisConnect Notify");

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
                        System.out.println("BridgeConnection DisConnect Refresh List");
//                    getDeviceListData();
                    }
                }
            }

            @Override
            public void onError() {

            }
        });

        mActivity.mImageViewAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                try {
////                    int mIntLatitude = (int) Long.parseLong("03474703", 16);
////                    int mIntLongitude = (int) Long.parseLong("FFECBE26", 16);
////                    System.out.println("mIntLatitude=" + BLEUtility.intToByte(-180000000));
////                    System.out.println("mIntLatitude=" + BLEUtility.intToByte(-90000000));
//                    int mIntLatitude = (int) Long.parseLong("03474703", 16);
//                    int mIntLongitude = (int) Long.parseLong("FFECBE26", 16);
//
//                    System.out.println("mIntLatitude=" + mIntLatitude);
//                    System.out.println("mIntLongitude=" + mIntLongitude);
//                    String mStrLat = getLocationCalculation(mIntLatitude);
//                    String mStrLong = getLocationCalculation(mIntLongitude);
//                    System.out.println("mStrLatitudePost=" + mStrLat);
//                    System.out.println("mStrLongitudePost=" + mStrLong);
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                if (mActivity.isDevicesConnected) {
                    mActivity.mUtility.errorDialogWithYesNoCallBack("Sync Device", getResources().getString(R.string.str_start_sync_msg), "YES", "NO", true, 0, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            if (mActivity.isDevicesConnected) {
                                short mByteGetCommand = (short) 0xFF0C;
                                mActivity.setCommandData(mByteGetCommand);
//                                mActivity.new DeviceNameList(mActivity.mStringConnectedDevicesAddress).execute();
                            } else {
                                mActivity.showDisconnectedDeviceAlert();
                            }
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                } else {
                    mActivity.showDisconnectedDeviceAlert();
                }
            }
        });
        return mViewRoot;
    }

    /*Calculate GPS location*/
    private String getLocationCalculation(int latitudeLongitude) {
        String latLong = "0";
        try {
            int mIntLocationPrefix = latitudeLongitude / 1000000;
            int mIntLocationPostfix = latitudeLongitude % 1000000;
            System.out.println("mIntLocationPrefix=" + mIntLocationPrefix);
            System.out.println("mIntLocationPostfix=" + mIntLocationPostfix);
            double mLongPostfix = Double.parseDouble(mIntLocationPostfix + "") / 600000;
            System.out.println("mLongPostfix=" + mLongPostfix);
            return new DecimalFormat("##.######").format((mIntLocationPrefix + mLongPostfix));
        } catch (Exception e) {
            e.printStackTrace();
            return latLong;
        }
    }

    /*Init device list adapter*/
    private void getDeviceListData() {
        if (mDeviceListAdapter == null) {
            mDeviceListAdapter = new DeviceListAdapter();
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
            mRecyclerView.setLayoutManager(mLayoutManager);
            mRecyclerView.setAdapter(mDeviceListAdapter);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mStartDeviceConnectTimer != null) {
            mStartDeviceConnectTimer.cancel();
        }
        if (mScanTimeout != null) {
            mScanTimeout.cancel();
        }
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    private void checkAdapterIsEmpty() {
        if (isAdded()) {
            if (mDeviceListAdapter != null) {
                if (mDeviceListAdapter.getItemCount() > 0) {
                    mRelativeLayoutNoDevice.setVisibility(View.GONE);
                } else {
                    mRelativeLayoutNoDevice.setVisibility(View.VISIBLE);
                }
            } else {
                mRelativeLayoutNoDevice.setVisibility(View.VISIBLE);
            }
        }
    }

    /*Device adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_device_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if (mArrayListAddDevice.get(position).getDeviceIEEE() != null && !mArrayListAddDevice.get(position).getDeviceIEEE().equalsIgnoreCase("")) {
                holder.mTextViewDeviceId.setText(mArrayListAddDevice.get(position).getDeviceIEEE().replace(":", ""));
            } else {
                holder.mTextViewDeviceId.setText("");
            }
            if (mArrayListAddDevice.get(position).getDeviceName() != null && !mArrayListAddDevice.get(position).getDeviceName().equalsIgnoreCase("")) {
                holder.mTextViewDeviceName.setText(mArrayListAddDevice.get(position).getDeviceName());
            } else {
                holder.mTextViewDeviceName.setText("-NA-");
            }
            if (mArrayListAddDevice.get(position).getDeviceHexData() != null && mArrayListAddDevice.get(position).getDeviceHexData().length() >= 32) {
                try {
                    int latitude = (int) Long.parseLong(mArrayListAddDevice.get(position).getDeviceHexData().substring(16, 24), 16);
                    int longitude = (int) Long.parseLong(mArrayListAddDevice.get(position).getDeviceHexData().substring(24, 32), 16);
                    String mStrLat = getLocationCalculation(latitude);
                    String mStrLong = getLocationCalculation(longitude);
                    if (mStrLat.equalsIgnoreCase("0") || mStrLong.equalsIgnoreCase("0")) {
                        holder.mTextViewLocation.setTextColor(getResources().getColor(R.color.colorGrayText));
                        holder.mTextViewLocation.setEnabled(false);
                    } else {
                        holder.mTextViewLocation.setEnabled(true);
                        holder.mTextViewLocation.setTextColor(getResources().getColor(R.color.colorWhite));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.mTextViewLocation.setTextColor(getResources().getColor(R.color.colorGrayText));
                    holder.mTextViewLocation.setEnabled(false);
                }
            } else {
                holder.mTextViewLocation.setEnabled(false);
                holder.mTextViewLocation.setTextColor(getResources().getColor(R.color.colorGrayText));
            }
            if (mArrayListAddDevice.get(position).getIsConnected()) {
                holder.mTextViewStatus.setText(R.string.str_disconnect);
            } else {
                holder.mTextViewStatus.setText(R.string.str_connect);
            }
            holder.mTextViewLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListAddDevice != null) {
                        if (position < mArrayListAddDevice.size()) {
//                            mArrayListAddDevice.get(position).setDeviceHexData("0201061209537563636F72666973682044265420323115FF5900C2341288CEFD646400040000000000000000000000000000000000000000000000000000");
                            if (mArrayListAddDevice.get(position).getDeviceHexData() != null && mArrayListAddDevice.get(position).getDeviceHexData().length() >= 32) {
                                int latitude = (int) Long.parseLong(mArrayListAddDevice.get(position).getDeviceHexData().substring(16, 24), 16);
                                int longitude = (int) Long.parseLong(mArrayListAddDevice.get(position).getDeviceHexData().substring(24, 32), 16);
                                String mStrLat = getLocationCalculation(latitude);
                                String mStrLong = getLocationCalculation(longitude);
                                System.out.println("mStrLatitudePost=" + mStrLat);
                                System.out.println("mStrLongitudePost=" + mStrLong);
                                if (mStrLat.equalsIgnoreCase("0") && mStrLong.equalsIgnoreCase("0")) {
                                    mActivity.mUtility.errorDialog("Gps positional data not found", 0);
                                } else {
                                    Intent mIntent = new Intent(mActivity, MapsActivity.class);
                                    mIntent.putExtra("mIntent_latitude_1", mStrLat);
                                    mIntent.putExtra("mIntent_longitude_1", mStrLong);
                                    mIntent.putExtra("mIntent_latitude_2", "0");
                                    mIntent.putExtra("mIntent_longitude_2", "0");
                                    mIntent.putExtra("mIntent_location_1_title", mArrayListAddDevice.get(position).getDeviceName() + "_" + mArrayListAddDevice.get(position).getDeviceIEEE().replace(":", ""));
                                    mIntent.putExtra("mIntent_location_2_title", "");
                                    mIntent.putExtra("mIntent_is_single_location", true);
                                    startActivity(mIntent);
                                }
                            }
                        }
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListAddDevice != null) {
                        if (position < mArrayListAddDevice.size()) {
                            mActivity.mVoBluetoothDevicesConnected = mArrayListAddDevice.get(position);
                            try {
                                System.out.println("isConnected-" + mArrayListAddDevice.get(position).getIsConnected());
                                System.out.println("--connection Request Address-" + mArrayListAddDevice.get(position).getBluetoothDevice().getAddress());
                                if (!isConnectionRequestSend) {
                                    isConnectionRequestSend = true;
                                    if (mArrayListAddDevice.get(position).getIsConnected()) {
                                        mActivity.showProgress("Disconnecting..", false);
                                        isDisconnectConnectRequest = false;
                                        mActivity.disconnectDevices(mArrayListAddDevice.get(position).getBluetoothDevice(), false);
                                    } else {
                                        if (mActivity.isDevicesConnected) {
                                            isDisconnectConnectRequest = true;
                                            mActivity.showProgress("Connecting..", false);
                                            if (mActivity.mBluetoothDevice != null) {
                                                mActivity.disconnectDevices(mActivity.mBluetoothDevice, false);
                                            }
                                            Timer innerTimer = new Timer();
                                            innerTimer.schedule(new TimerTask() {
                                                @Override
                                                public void run() {
                                                    mActivity.ConnectDevices(mArrayListAddDevice.get(position).getBluetoothDevice(), false);
                                                }
                                            }, 1000);
                                            //                                            mStartDeviceConnectTimer = new startDeviceConnectTimer(9000, 1000,mArrayDeviceBridgeList.get(position).getBluetoothDevice(),false);
//                                            mStartDeviceConnectTimer.start();
                                        } else {
                                            mActivity.showProgress("Connecting..", false);
                                            mActivity.ConnectDevices(mArrayListAddDevice.get(position).getBluetoothDevice(), false);
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
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListAddDevice.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_add_device_item_tv_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_add_device_item_tv_device_id)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_add_device_item_tv_status)
            TextView mTextViewStatus;
            @BindView(R.id.raw_add_device_item_tv_location)
            TextView mTextViewLocation;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Show Device Connected alert*/
    private void showDeviceConnectedAlert() {
        isConnectionRequestSend = false;
        mActivity.mUtility.errorDialogWithCallBack("Device connected successfully.", 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    /*Device Connection Timer*/
    private class startDeviceConnectTimer extends CountDownTimer {
        BluetoothDevice bluetoothDevice;
        boolean isDisconnectTimer = false;

        public startDeviceConnectTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
//            if (!isDisconnectTimer) {
//                mActivity.ConnectDevices(bluetoothDevice, false);
//            }
        }

        @Override
        public void onFinish() {
            isConnectionRequestSend = false;
            mActivity.hideProgress();
        }
    }

    /*Scan Timeout*/
    private class scanTimeout extends CountDownTimer {

        public scanTimeout(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
