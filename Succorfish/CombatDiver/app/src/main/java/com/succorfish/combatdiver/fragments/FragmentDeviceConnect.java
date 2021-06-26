package com.succorfish.combatdiver.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.Vo.VoBluetoothDevices;
import com.succorfish.combatdiver.interfaces.onDeviceConnectionStatusChange;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 17-01-2018.
 */

public class FragmentDeviceConnect extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_connect_device_recyclerview_device)
    RecyclerView mRecyclerViewDevice;
    @BindView(R.id.frg_connect_device_textview_nodevice)
    TextView mTextViewNoDeviceFound;
    @BindView(R.id.frg_connect_device_title)
    TextView mTextViewTitle;
    @BindView(R.id.frg_connect_device_swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    ArrayList<VoBluetoothDevices> mArrayListDevice = new ArrayList<>();
    DeviceAdapter mDeviceAdapter;
    private boolean isFromDeviceSetup = false;
    public boolean isConnectionRequestSend = false;
    startDeviceConnectTimer mStartDeviceConnectTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            isFromDeviceSetup = getArguments().getBoolean("intent_is_from_device_setup", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_connect_device, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_connct_device_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mActivity.isFromDeviceConnection = true;

        mActivity.mDeviceListArrayMain = new ArrayList<>();
        mActivity.RescanDevice();
        mArrayListDevice = new ArrayList<>();
        mStartDeviceConnectTimer = new startDeviceConnectTimer(7000, 1000);
        getDeviceListData();
        mSwipeRefreshLayout.setRefreshing(true);
        if (isFromDeviceSetup) {
            mTextViewTitle.setText("Connect device for setup");
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mActivity.mDeviceListArrayMain = new ArrayList<>();
                mActivity.RescanDevice();
                mArrayListDevice = new ArrayList<>();
                mSwipeRefreshLayout.setRefreshing(false);
                getDeviceListData();
            }
        });
        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });

        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {
                if (isAdded()) {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mArrayListDevice = new ArrayList<>();
                    getDeviceListData();
                    System.out.println("BridgeCOnnection AddDevice");
                }
            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {

            }

            @Override
            public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                System.out.println("BridgeCOnnection COnnect");
                if (mArrayListDevice.size() > 0) {
                    for (int j = 0; j < mArrayListDevice.size(); j++) {
                        if (mArrayListDevice.get(j).getDeviceAddress().equalsIgnoreCase(connectedDevice.getAddress())) {
                            mArrayListDevice.get(j).setIsConnected(true);
                            if (mDeviceAdapter != null) {
                                System.out.println("BridgeCOnnection COnnect Notify");
                                mDeviceAdapter.notifyDataSetChanged();
                                break;
                            }
                        }
                    }
                } else {
                    System.out.println("BridgeCOnnection COnnect Refresh List");
                    mArrayListDevice = new ArrayList<>();
                    getDeviceListData();
                }
                mActivity.onBackPressed();
            }

            @Override
            public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                System.out.println("BridgeCOnnection DisCOnnect");
                if (mArrayListDevice.size() > 0) {
                    for (int j = 0; j < mArrayListDevice.size(); j++) {
                        if (mArrayListDevice.get(j).getDeviceAddress().equalsIgnoreCase(connectedDevice.getAddress())) {
                            mArrayListDevice.get(j).setIsConnected(false);
                            if (mDeviceAdapter != null) {
                                mDeviceAdapter.notifyDataSetChanged();
                                System.out.println("BridgeCOnnection DisCOnnect Notify");
                                break;
                            }
                        }
                    }
                } else {
                    System.out.println("BridgeCOnnection DisCOnnect Refresh List");
                    mArrayListDevice = new ArrayList<>();
                    getDeviceListData();
                }
            }

            @Override
            public void onError() {
                mArrayListDevice = new ArrayList<>();
                getDeviceListData();
                System.out.println("BridgeCOnnection ERROR");
            }
        });
        return mViewRoot;
    }

    private void getDeviceListData() {
        System.out.println("getting non connectable list-");
        if (mActivity.mDeviceListArrayMain != null) {
            System.out.println("mActivity.mLeDevices-" + mActivity.mDeviceListArrayMain.size());
            for (int i = 0; i < mActivity.mDeviceListArrayMain.size(); i++) {
                mArrayListDevice.add(mActivity.mDeviceListArrayMain.get(i));
            }
        }
        if (mDeviceAdapter == null) {
            mDeviceAdapter = new DeviceAdapter();
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
            mRecyclerViewDevice.setLayoutManager(mLayoutManager);
            mRecyclerViewDevice.setAdapter(mDeviceAdapter);
            mDeviceAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    checkAdapterIsEmpty();
                }
            });

        } else {
            mDeviceAdapter.notifyDataSetChanged();
        }
        checkAdapterIsEmpty();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    private void checkAdapterIsEmpty() {
        if (mDeviceAdapter != null) {
            if (mDeviceAdapter.getItemCount() > 0) {
                mTextViewNoDeviceFound.setVisibility(View.GONE);
            } else {
                mTextViewNoDeviceFound.setVisibility(View.VISIBLE);
            }
        } else {
            mTextViewNoDeviceFound.setVisibility(View.VISIBLE);
        }
    }

    public class DeviceAdapter extends RecyclerView.Adapter<DeviceAdapter.ViewHolder> {

        @Override
        public DeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_connect_device_list_item, parent, false);
            return new DeviceAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DeviceAdapter.ViewHolder mViewHolder, final int position) {

            if (mArrayListDevice.get(position).getIsConnected()) {
                mViewHolder.mTextViewConnectDisConnect.setTextColor(getResources().getColor(R.color.colorRed));
                mViewHolder.mTextViewConnectDisConnect.setText("Disconnect");
            } else {
                mViewHolder.mTextViewConnectDisConnect.setText("Connect");
                mViewHolder.mTextViewConnectDisConnect.setTextColor(getResources().getColor(R.color.colorWhite));
            }
            if (mArrayListDevice.get(position).getDeviceName() != null && !mArrayListDevice.get(position).getDeviceName().equalsIgnoreCase("")) {
                mViewHolder.mTextViewDeviceName.setText("Device "+mArrayListDevice.get(position).getDeviceName());
            } else {
                mViewHolder.mTextViewDeviceName.setText("");
            }
            if (mArrayListDevice.get(position).getDeviceAddress() != null && !mArrayListDevice.get(position).getDeviceAddress().equalsIgnoreCase("")) {
                mViewHolder.mTextViewDeviceAddress.setText("Device ID: "+mArrayListDevice.get(position).getDeviceAddress().replace(":", ""));
            } else {
                mViewHolder.mTextViewDeviceAddress.setText("");
            }
//            mViewHolder.mTextViewName.setText(mDrawerItemArrayList.get(position).getmStringName());
//            mViewHolder.mImageViewIcon.setColorFilter(ContextCompat.getColor(MainActivity.this, R.color.colorWhite));
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDevice != null) {
                        if (position < mArrayListDevice.size()) {
                            try {
//                                for (VoBluetoothDevices device : mActivity.mLeDevices) {
//                                    if (device.getIsConnected()) {
//                                        if (!device.getDeviceAddress().equalsIgnoreCase(mActivity.mLeDevices.get(position).getDeviceAddress())) {
//                                            mActivity.disconnectDevices(device.getBluetoothDevice());
//                                        }
//                                        break;
//                                    }
//                                }
                                System.out.println("isConnected-" + mArrayListDevice.get(position).getIsConnected());
                                System.out.println("--connection Request Address-" + mArrayListDevice.get(position).getBluetoothDevice().getAddress());
                                if (!isConnectionRequestSend) {
                                    isConnectionRequestSend = true;
                                    if (mArrayListDevice.get(position).getIsConnected()) {
                                        mActivity.isManualDisconnect = true;
                                        mActivity.showProgress("Disconnecting..", true);
                                        mActivity.disconnectDevices(mArrayListDevice.get(position).getBluetoothDevice());
                                    } else {
                                        mActivity.showProgress("Connecting..", true);
                                        mActivity.ConnectDevices(mArrayListDevice.get(position).getBluetoothDevice(), false);
                                    }
                                    if (mStartDeviceConnectTimer != null)
                                        mStartDeviceConnectTimer.start();
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
            return mArrayListDevice.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_connect_device_list_item_textview_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_connect_device_list_item_textview_device_address)
            TextView mTextViewDeviceAddress;
            @BindView(R.id.raw_connect_device_list_item_textview_connect)
            TextView mTextViewConnectDisConnect;
            @BindView(R.id.raw_connect_device_list_item_linearlayout_main)
            RelativeLayout mLinearLayoutMain;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.isFromDeviceConnection = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.isFromDeviceConnection = false;
        mActivity.hideProgress();
        mActivity.stopScan();
        if (mStartDeviceConnectTimer != null) {
            mStartDeviceConnectTimer.cancel();
        }
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
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

}
