package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
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
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentResetSmartLight extends Fragment {

    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    DeviceListAdapter mDeviceListAdapter;
    ArrayList<VoBluetoothDevices> mArrayDeviceBridgeList = new ArrayList<>();
    boolean isConnectionRequestSend = false;
    boolean isDisconnectConnectRequest = false;
    boolean isHardRequestSent = false;
    startDeviceConnectTimer mStartDeviceConnectTimer;
    hardResetCheckTimer mHardResetCheckTimer;
    String mStringResetBleAddress = "";

    @BindView(R.id.recyclerViewFoundDevices)
    RecyclerView mRecyclerView;
    SwipeRefreshLayout swipeRefreshLayout;
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
        mViewRoot = inflater.inflate(R.layout.fragment_reset_smartlight, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.isAddDeviceScan = false;
        isConnectionRequestSend = false;
        mActivity.mTextViewTitle.setText(R.string.frg_setting_hard_reset);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_refresh_icon_white);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mActivity.isFromBridgeConnection = true;
        mActivity.mLeDevices = new ArrayList<>();
        mActivity.RescanDevice(false);
        mStartDeviceConnectTimer = new startDeviceConnectTimer(13000, 1000);
        mHardResetCheckTimer = new hardResetCheckTimer(15000, 1000);
        mArrayDeviceBridgeList = new ArrayList<>();
        getDeviceListData();

        /*Hard rest ack status response*/
        mActivity.setOnHardReset(() -> {
            if (mStartDeviceConnectTimer != null) {
                isHardRequestSent = true;
                mActivity.isAddDeviceScan = true;
                mActivity.isFromBridgeConnection = true;
                mActivity.mLeDevices = new ArrayList<>();
                mActivity.mLeDevicesTemp = new ArrayList<>();
                mActivity.RescanDevice(false);
                mActivity.showProgress(getResources().getString(R.string.str_turn_off_on), true);
                mStartDeviceConnectTimer.start();
            }
        });

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
                                    if (mVoBluetoothDevices.getIsFromNotification()) {
                                        return;
                                    }

                                    if (!isHardRequestSent) {
                                        boolean containsInScanDevice = false;
                                        for (VoBluetoothDevices device : mArrayDeviceBridgeList) {
                                            if (mVoBluetoothDevices.getDeviceAddress().equals(device.getDeviceAddress())) {
                                                containsInScanDevice = true;
                                                break;
                                            }
                                        }
                                        if (!containsInScanDevice) {
                                            mArrayDeviceBridgeList.add(mVoBluetoothDevices);
                                            mActivity.runOnUiThread(() -> {
                                                if (mDeviceListAdapter != null) {
                                                    mDeviceListAdapter.notifyDataSetChanged();
                                                }
                                            });
                                        }
                                    } else {
                                        if (mVoBluetoothDevices.getDeviceHexData().substring(32, 36).equals(URLCLASS.ASSOC_NON_CONNECTABLE_RSP)
                                                && mVoBluetoothDevices.getDeviceHexData().substring(36, 48).toLowerCase().equals(mStringResetBleAddress.toLowerCase())) {
                                            if (mHardResetCheckTimer != null) {
                                                mHardResetCheckTimer.cancel();
                                                mHardResetCheckTimer.onFinish();
                                            }
                                            showResetSuccessAlert();
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
                                mActivity.runOnUiThread(() -> {
                                    if (mDeviceListAdapter != null) {
                                        mDeviceListAdapter.notifyDataSetChanged();

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
                mActivity.ConnectDevices(mActivity.mBluetoothDevice, false);
            }

            @Override
            public void onError() {
            }
        });
        return mViewRoot;
    }

    /*Init device scan list adapter*/
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
    public void onResume() {
        super.onResume();
        mActivity.isFromBridgeConnection = true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.isFromBridgeConnection = false;
        mRecyclerView.setAdapter(null);
        mArrayDeviceBridgeList = null;
        mDeviceListAdapter = null;
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        if (mStartDeviceConnectTimer != null) {
            mStartDeviceConnectTimer.cancel();
        }
        if (mHardResetCheckTimer != null) {
            mHardResetCheckTimer.cancel();
        }

        unbinder.unbind();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }

    /*Check Adapter is empty or not*/
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

    private void showDeviceList() {
        if (!isDetached()) {
            mRecyclerView.setVisibility(View.VISIBLE);
            linearLayoutNoDeviceFound.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.GONE);
        }
    }

    private void showNoDevicesLayout() {
        if (!isDetached()) {
            mRecyclerView.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.GONE);
            linearLayoutNoDeviceFound.setVisibility(View.VISIBLE);
        }
    }

    private void showSearchingDevicesLayout() {
        if (!isDetached()) {
            mRecyclerView.setVisibility(View.GONE);
            linearLayoutSearchingDevices.setVisibility(View.VISIBLE);
            linearLayoutNoDeviceFound.setVisibility(View.GONE);
        }
    }

    /*Device list adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @NonNull
        @Override
        public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_reset_smartlight_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(DeviceListAdapter.ViewHolder holder, final int position) {
            if (mArrayDeviceBridgeList.get(position).getDeviceName() != null && !mArrayDeviceBridgeList.get(position).getDeviceName().equalsIgnoreCase("")) {
                holder.mTextViewDeviceName.setText(mArrayDeviceBridgeList.get(position).getDeviceName());
            } else {
                holder.mTextViewDeviceName.setText("");
            }
//            if (mArrayDeviceBridgeList.get(position).getIsConnected()) {
//                holder.mTextViewConnect.setText(R.string.frg_setting_brg_conn_btn_disconnect);
//            } else {
//                holder.mTextViewConnect.setText(R.string.frg_setting_brg_conn_btn_connect);
//            }
            if (mArrayDeviceBridgeList.get(position).getDeviceAddress() != null && !mArrayDeviceBridgeList.get(position).getDeviceAddress().equalsIgnoreCase("")) {
                holder.mTextViewDeviceId.setText(mArrayDeviceBridgeList.get(position).getDeviceAddress().replace(":", ""));
            } else {
                holder.mTextViewDeviceId.setText("");
            }

            holder.itemView.setOnClickListener(v -> {
                if (mArrayDeviceBridgeList != null) {
                    if (position < mArrayDeviceBridgeList.size()) {
                        mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.str_factory_reset_title), getResources().getString(R.string.str_factory_reset_confirmation), "Yes", "No", true, 2, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                try {
                                    if (!isConnectionRequestSend) {
                                        mStringResetBleAddress = mArrayDeviceBridgeList.get(position).getDeviceAddress().replace(":", "");
                                        if (mArrayDeviceBridgeList.get(position).getIsConnected()) {
                                            isDisconnectConnectRequest = true;
                                            mActivity.isIdentificationRequest = true;
                                            mActivity.isHardResetRequest = false;
                                            mActivity.showProgress("Resetting..", true);

                                            if (mActivity.mBluetoothDevice != null) {
                                                mActivity.disconnectDevices(mActivity.mBluetoothDevice, false);
                                            }
                                        } else {
                                            isConnectionRequestSend = true;
                                            mActivity.isIdentificationRequest = true;
                                            mActivity.isHardResetRequest = false;
                                            if (mActivity.isDevicesConnected) {
                                                isDisconnectConnectRequest = true;
                                                mActivity.showProgress("Resetting..", true);
                                                if (mActivity.mBluetoothDevice != null) {
                                                    mActivity.disconnectDevices(mActivity.mBluetoothDevice, false);
                                                }
                                            } else {
                                                mActivity.showProgress("Resetting..", true);
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

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {

                            }
                        });
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayDeviceBridgeList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_hard_reset_list_item_tv_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_hard_reset_list_item_tv_connect)
            TextView mTextViewConnect;
            @BindView(R.id.raw_hard_reset_list_item_tv_device_id)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_hard_reset_list_item_iv_device)
            ImageView mImageViewDevice;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*After device reset successfully remove those device from the device, group and alarm*/
    private void showResetSuccessAlert() {
        isHardRequestSent = false;
        DataHolder mDataHolderLight;
        try {
            String url = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceIsActive + "= '1'" +
                    " AND " + DBHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" +
                    " AND " + DBHelper.mFieldDeviceBleAddress + "= '" + mStringResetBleAddress.toUpperCase() + "'" + " Limit 1";
            mDataHolderLight = mActivity.mDbHelper.read(url);
            if (mDataHolderLight != null) {
                for (int i = 0; i < mDataHolderLight.get_Listholder().size(); i++) {

                    VoDeviceList mVoDeviceList = new VoDeviceList();
                    mVoDeviceList.setDevicLocalId(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceLocalId));
                    mVoDeviceList.setDeviceServerid(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceServerId));
                    mVoDeviceList.setUser_id(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceUserId));
                    mVoDeviceList.setDevice_Comm_id(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommID));
                    mVoDeviceList.setDevice_Comm_hexId(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommHexId));
                    mVoDeviceList.setDevice_name(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceName));
                    mVoDeviceList.setDevice_realName(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceRealName));
                    mVoDeviceList.setDevice_BleAddress(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceBleAddress).toUpperCase());
                    mVoDeviceList.setDevice_Type(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceType));
                    mVoDeviceList.setDevice_type_name(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceTypeName));
                    mVoDeviceList.setDevice_ConnStatus(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldConnectStatus));
                    mVoDeviceList.setDevice_SwitchStatus(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldSwitchStatus));
                    mVoDeviceList.setDevice_is_favourite(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsFavourite));
                    mVoDeviceList.setDevice_last_state_remember(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceLastState));
                    mVoDeviceList.setDevice_timestamp(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceTimeStamp));
                    mVoDeviceList.setDevice_is_active(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsActive));
                    mVoDeviceList.setDevice_created_at(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceCreatedAt));
                    mVoDeviceList.setDevice_updated_at(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceUpdatedAt));
                    mVoDeviceList.setDevice_is_sync(mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsSync));
                    if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                        mVoDeviceList.setIsChecked(true);
                    } else {
                        mVoDeviceList.setIsChecked(false);
                    }

                    String mStrDeviceLocalId = mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceLocalId);
                    String mStrDeviceServerId = mDataHolderLight.get_Listholder().get(i).get(DBHelper.mFieldDeviceServerId);
                    ContentValues mContentValuesGD = new ContentValues();
                    mContentValuesGD.put(DBHelper.mFieldGDListStatus, "0");
                    mActivity.mDbHelper.updateRecord(DBHelper.mTableGroupDeviceList, mContentValuesGD,
                            DBHelper.mFieldGDListLocalDeviceID + "=?", new String[]{mStrDeviceLocalId});
                    DataHolder mDataHolderDltGroup;
                    try {
                        String deleteGroupDevice = "select * from " + DBHelper.mTableGroup + " INNER JOIN "
                                + DBHelper.mTableGroupDeviceList + " on " + DBHelper.mFieldGDListLocalGroupID + " =" +
                                DBHelper.mFieldGroupLocalID + " AND " + DBHelper.mFieldGDListUserID + "= "
                                + DBHelper.mTableGroup + "." + DBHelper.mFieldGroupUserId + " AND "
                                + DBHelper.mFieldGroupIsActive + "= 1" + " INNER JOIN " + DBHelper.mTableDevice
                                + " on " + mActivity.mDbHelper.mFieldDeviceLocalId + " =" + mActivity.mDbHelper.mFieldGDListLocalDeviceID + " AND " + mActivity.mDbHelper.mTableDevice + "." + mActivity.mDbHelper.mFieldDeviceUserId + "= " + mActivity.mDbHelper.mFieldGDListUserID + " where " + mActivity.mDbHelper.mFieldDeviceLocalId + "= '" + mStrDeviceLocalId + "'" + " AND " + mActivity.mDbHelper.mTableDevice + "." + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mDataHolderDltGroup = mActivity.mDbHelper.read(deleteGroupDevice);
                        if (mDataHolderDltGroup != null) {
                            String mStringGroupServerId;
                            int intGroupDeviceCount;
                            int intGroupInactiveDeviceCount;
                            for (int j = 0; j < mDataHolderDltGroup.get_Listholder().size(); j++) {
                                mStringGroupServerId = mDataHolderDltGroup.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldGroupServerID);
                                ContentValues mContentValuesGroup = new ContentValues();
                                intGroupDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mActivity.mDbHelper.mTableGroupDeviceList + " where " + mActivity.mDbHelper.mFieldGDListUserID + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldGDListLocalGroupID + "= '" + mDataHolderDltGroup.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldGroupLocalID) + "'" + ") as count");
                                intGroupInactiveDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mActivity.mDbHelper.mTableGroupDeviceList + " where " + mActivity.mDbHelper.mFieldGDListStatus + "= 0" + " AND " + mActivity.mDbHelper.mFieldGDListUserID + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldGDListLocalGroupID + "= '" + mDataHolderDltGroup.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldGroupLocalID) + "'" + ") as count");

                                if (intGroupDeviceCount == intGroupInactiveDeviceCount) {
                                    mContentValuesGroup.put(mActivity.mDbHelper.mFieldGroupIsActive, "0");
                                    if (mStringGroupServerId != null && !mStringGroupServerId.equalsIgnoreCase("") && !mStringGroupServerId.equalsIgnoreCase("null")) {
                                        mContentValuesGroup.put(mActivity.mDbHelper.mFieldGroupIsSync, "0");
                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValuesGroup, mActivity.mDbHelper.mFieldGroupLocalID + "=?", new String[]{mDataHolderDltGroup.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldGroupLocalID)});
                                    } else {
                                        mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableGroup + " where " + mActivity.mDbHelper.mFieldGroupLocalID + "= '" + mDataHolderDltGroup.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldGroupLocalID) + "'");
                                    }
                                } else {
                                    mContentValuesGroup.put(mActivity.mDbHelper.mFieldGroupIsSync, "0");
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValuesGroup, mActivity.mDbHelper.mFieldGroupLocalID + "=?", new String[]{mDataHolderDltGroup.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldGroupLocalID)});
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    DataHolder mDataHolderAlarm;
                    try {
//                    String deleteAlarmDevice = "select * from " + mActivity.mDbHelper.mTableAlarm + " INNER JOIN " + mActivity.mDbHelper.mTableAlarmDeviceList + " on " + mActivity.mDbHelper.mFieldADAlarmLocalID + " =" + mActivity.mDbHelper.mFieldAlarmLocalID + " AND " + mActivity.mDbHelper.mFieldADUserId + "= " + mActivity.mDbHelper.mTableAlarm + "." + mActivity.mDbHelper.mFieldAlarmUserId + " AND " + mActivity.mDbHelper.mFieldAlarmIsActive + "= 1" + " INNER JOIN " + mActivity.mDbHelper.mTableDevice + " on " + mActivity.mDbHelper.mFieldDeviceLocalId + " =" + mActivity.mDbHelper.mFieldADDeviceLocalID + " AND " + mActivity.mDbHelper.mTableAlarm + "." + mActivity.mDbHelper.mFieldAlarmUserId + "= " + mActivity.mDbHelper.mFieldADUserId + " where " + mActivity.mDbHelper.mFieldDeviceLocalId + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'" + " AND " + mActivity.mDbHelper.mTableAlarm + "." + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        String deleteAlarmDevice = "select * from " + mActivity.mDbHelper.mTableAlarm + " INNER JOIN " + mActivity.mDbHelper.mTableAlarmDeviceList + " on " + mActivity.mDbHelper.mFieldADAlarmLocalID + " =" + mActivity.mDbHelper.mFieldAlarmLocalID + " AND " + mActivity.mDbHelper.mFieldADUserId + "= " + mActivity.mDbHelper.mTableAlarm + "." + mActivity.mDbHelper.mFieldAlarmUserId + " AND " + mActivity.mDbHelper.mFieldAlarmIsActive + "= 1" + " INNER JOIN " + mActivity.mDbHelper.mTableDevice + " on " + mActivity.mDbHelper.mFieldDeviceLocalId + " =" + mActivity.mDbHelper.mFieldADDeviceLocalID + " AND " + mActivity.mDbHelper.mTableDevice + "." + mActivity.mDbHelper.mFieldDeviceUserId + "= " + mActivity.mDbHelper.mFieldADUserId + " where " + mActivity.mDbHelper.mFieldDeviceLocalId + "= '" + mStrDeviceLocalId + "'" + " AND " + mActivity.mDbHelper.mTableDevice + "." + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mDataHolderAlarm = mActivity.mDbHelper.read(deleteAlarmDevice);
                        if (mDataHolderAlarm != null) {
                            int intAlarmDeviceCount;
                            int intAlarmInactiveDeviceCount;
                            for (int j = 0; j < mDataHolderAlarm.get_Listholder().size(); j++) {
                                ContentValues mContentValuesAlarm = new ContentValues();
                                intAlarmDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mActivity.mDbHelper.mTableAlarmDeviceList + " where " + mActivity.mDbHelper.mFieldADUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= '" + mDataHolderAlarm.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldAlarmLocalID) + "'" + ") as count");
                                intAlarmInactiveDeviceCount = mActivity.mDbHelper.getCountRecordByQuery("SELECT (SELECT count(*) from " + mActivity.mDbHelper.mTableAlarmDeviceList + " where " + mActivity.mDbHelper.mFieldADDeviceStatus + "= 1" + " AND " + mActivity.mDbHelper.mFieldADDeviceLocalID + "= '" + mStrDeviceLocalId + "'" + " AND " + mActivity.mDbHelper.mFieldADUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= '" + mDataHolderAlarm.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldAlarmLocalID) + "'" + ") as count");
                                if (intAlarmDeviceCount == intAlarmInactiveDeviceCount) {
                                    mContentValuesAlarm.put(mActivity.mDbHelper.mFieldAlarmIsActive, "0");
                                }
                                mContentValuesAlarm.put(mActivity.mDbHelper.mFieldAlarmIsSync, "0");
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAlarm, mContentValuesAlarm, mActivity.mDbHelper.mFieldAlarmLocalID + "=?", new String[]{mDataHolderAlarm.get_Listholder().get(j).get(mActivity.mDbHelper.mFieldAlarmLocalID)});
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    String mStringQueryAlarm = "delete from " + mActivity.mDbHelper.mTableAlarmDeviceList + " where " + mActivity.mDbHelper.mFieldADDeviceLocalID + "= '" + mStrDeviceLocalId + "'";
                    mActivity.mDbHelper.exeQuery(mStringQueryAlarm);
//                    String mStringQueryGroup = "delete from " + mActivity.mDbHelper.mTableGroupDeviceList + " where " + mActivity.mDbHelper.mFieldGDListLocalDeviceID + "= '" + mArrayListDevice.get(intDeleteDevicePosition).getDevicLocalId() + "'";
//                    mActivity.mDbHelper.exeQuery(mStringQueryGroup);

                    if (mStrDeviceServerId != null && !mStrDeviceServerId.equalsIgnoreCase("") && !mStrDeviceServerId.equalsIgnoreCase("null")) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(mActivity.mDbHelper.mFieldDeviceIsActive, "0");
                        mContentValues.put(mActivity.mDbHelper.mFieldDeviceIsSync, "0");
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", new String[]{mStrDeviceLocalId});
                        mVoDeviceList.setDevice_is_active("0");
                        mVoDeviceList.setDevice_is_sync("0");
                        if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                            if (mActivity.mUtility.haveInternet()) {
                                mActivity.updateDeviceAPI(mVoDeviceList);
                            }
                        }
                    } else {
                        String mStringQuery = "delete from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceLocalId + "= '" + mStrDeviceLocalId + "'";
                        mActivity.mDbHelper.exeQuery(mStringQuery);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_factory_reset_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void showDeviceConnectedAlert() {
        isConnectionRequestSend = false;
//        if (mActivity.mBluetoothDevice != null) {
//            mActivity.disconnectDevices(mActivity.mBluetoothDevice, false);
//        }
//        mActivity.mUtility.errorDialogWithCallBack("Device hard reset successfully.", 0, false, new onAlertDialogCallBack() {
//            @Override
//            public void PositiveMethod(DialogInterface dialog, int id) {
//                mActivity.onBackPressed();
//            }
//
//            @Override
//            public void NegativeMethod(DialogInterface dialog, int id) {
//
//            }
//        });
    }

    private void beginDeviceScan() {
        if (!isDetached()) {
            mArrayDeviceBridgeList = new ArrayList<>();

//            swipeRefreshLayout.setRefreshing(true);

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
//            mActivity.hideProgress();
        }
    }

    private class hardResetCheckTimer extends CountDownTimer {
        public hardResetCheckTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {
        }

        @Override
        public void onFinish() {
            mActivity.isFromBridgeConnection = true;
            mActivity.isAddDeviceScan = false;
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