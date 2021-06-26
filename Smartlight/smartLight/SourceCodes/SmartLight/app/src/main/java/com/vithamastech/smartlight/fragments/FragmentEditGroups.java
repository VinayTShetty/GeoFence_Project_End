package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.evergreen.ble.advertisement.ManufactureData;
import com.google.gson.Gson;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoAddGroupData;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.Vo.VoLocalGroupData;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 22-12-2017.
 */

public class FragmentEditGroups extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_add_group_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_add_group_textview_nodevice)
    TextView mTextViewNoDeviceFound;
    @BindView(R.id.fragment_add_group_edittext_group_name)
    AppCompatEditText mAppCompatEditTextGroupName;

    @BindView(R.id.fragment_add_group_swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    ArrayList<VoDeviceList> mArrayListAddDevice = new ArrayList<>();
    ArrayList<VoDeviceList> mArrayListCheckedDevice = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    boolean isGroupAdded = false;
    boolean isAnythingChange = false;
    int mIntRandomGroupId;
    String mStringGroupHexId = "";
    startDeviceScanTimer mStartDeviceScanTimer;
    ArrayList<VoBluetoothDevices> mLeDevicesTemp = new ArrayList<>();
    int currentLoopPosition = 0;
    SimpleDateFormat mDateFormatDb;
    String mStringGroupLocalId = "";
    String mStringGroupServerId = "";
    String mStringGroupName = "";
    VoLocalGroupData mVoLocalGroupData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        if (getArguments() != null) {
            mStringGroupLocalId = getArguments().getString("group_local_id");
            mStringGroupServerId = getArguments().getString("group_server_id");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_add_groups, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_edit_group_header);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.VISIBLE);
        mActivity.mTextViewAdd.setText("Save");
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlueText,
                R.color.colorBlack);
        mActivity.isAddDeviceScan = true;
        isGroupAdded = false;
        // Get device list from database
        getDBDeviceList();
        mStartDeviceScanTimer = new startDeviceScanTimer(5000, 1000);
        mActivity.RescanDevice(false);
        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });
//        Device connection status,scan call back
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {
            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_REMOVE_RSP) || mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_ADD_RSP)) {
                    boolean containsInScanDevice = false;
                    for (VoBluetoothDevices device : mLeDevicesTemp) {
                        if (mVoBluetoothDevices.getDeviceHexData().equals(device.getDeviceHexData())) {
                            containsInScanDevice = true;
                            break;
                        }
                    }
                    if (!containsInScanDevice) {
                        mLeDevicesTemp.add(mVoBluetoothDevices);
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
        /*Refresh data on refresh*/
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getDBDeviceList();
            }
        });
        /*Add device data*/
        mActivity.mTextViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    mActivity.isAddDeviceScan = true;
                    mActivity.mLeDevicesTemp = new ArrayList<>();
                    mLeDevicesTemp = new ArrayList<>();
                    mActivity.RescanDevice(false);
                    mArrayListCheckedDevice = new ArrayList<>();
                    currentLoopPosition = 0;
                    mActivity.mUtility.hideKeyboard(mActivity);
                    mStringGroupName = mAppCompatEditTextGroupName.getText().toString().trim();
                    if (mStringGroupName.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_add_group_enter_room_name_alert), 3, true);
                        return;
                    }
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(mActivity.mDbHelper.mFieldGroupIsSync, "0");
                    mContentValues.put(mActivity.mDbHelper.mFieldGroupName, mStringGroupName);
                    String[] mArray = new String[]{mStringGroupLocalId};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValues, mActivity.mDbHelper.mFieldGroupLocalID + "=?", mArray);
                    if (!isAnythingChange) {
                        mActivity.new syncGroupDataAsyncTask(false).execute("");
                        showGroupAddAlert();
                        return;
                    }
                    boolean isAnyDeviceAdd = false;
                    for (int i = 0; i < mArrayListAddDevice.size(); i++) {
                        if (mArrayListAddDevice.get(i).getIsDeviceAlradyInGroup()) {
                            mArrayListCheckedDevice.add(mArrayListAddDevice.get(i));
                        } else {
                            if (mArrayListAddDevice.get(i).getIsGroupChecked()) {
                                mArrayListCheckedDevice.add(mArrayListAddDevice.get(i));
                            }
                        }
                        if (mArrayListAddDevice.get(i).getIsGroupChecked()) {
                            isAnyDeviceAdd = true;
                        }
                    }
//                    Handler handler1 = new Handler();
                    if (isAnyDeviceAdd) {
                        if (mArrayListCheckedDevice.size() > 0) {
                            int skippCount = 0;
                            for (int i = 0; i < mArrayListCheckedDevice.size(); i++) {
                                if (mArrayListCheckedDevice.get(i).getIsDeviceAlradyInGroup() && mArrayListCheckedDevice.get(i).getIsGroupChecked()) {
                                    skippCount = skippCount + 1;
                                }
                            }
                            if (skippCount == mArrayListCheckedDevice.size()) {
                                mActivity.new syncGroupDataAsyncTask(false).execute("");
                                showGroupAddAlert();
                                return;
                            }
                            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                mActivity.showProgress("Updating Rooms...", true);
                                addingDeviceGroupRequest();
                            } else {
                                mActivity.connectDeviceWithProgress();
                            }
                        } else {
                            mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_add_group_select_device), 3, true);
                        }
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_add_group_select_device), 3, true);
                    }

                }
            }
        });
        return mViewRoot;
    }
    /*Add device request to group*/

    private void addingDeviceGroupRequest() {
        if (currentLoopPosition >= mArrayListCheckedDevice.size()) {
            mActivity.hideProgress();
            return;
        }
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
//            for (int i = 0; i < mArrayListCheckedDevice.size(); i++) {
            if (mArrayListCheckedDevice.get(currentLoopPosition).getIsDeviceAlradyInGroup()) {
                if (!mArrayListCheckedDevice.get(currentLoopPosition).getIsGroupChecked()) {
                    try {
                        isGroupAdded = true;
                        mActivity.deleteAllGroupDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), Short.parseShort(mIntRandomGroupId + ""), false);
                        Timer innerTimer = new Timer();
                        innerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (currentLoopPosition == mArrayListCheckedDevice.size() - 1) {
                                    if (mStartDeviceScanTimer != null)
                                        mStartDeviceScanTimer.start();
                                } else {
                                    currentLoopPosition++;
                                    addingDeviceGroupRequest();
                                }
                            }
                        }, 900);
                    } catch (Exception e) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.hideProgress();
                            }
                        });
                        e.printStackTrace();
                    }
                } else {
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (currentLoopPosition == mArrayListCheckedDevice.size() - 1) {
                                if (mStartDeviceScanTimer != null)
                                    mStartDeviceScanTimer.start();
                            } else {
                                currentLoopPosition++;
                                addingDeviceGroupRequest();
                            }
                        }
                    }, 50);
                }
            } else {
                if (mArrayListCheckedDevice.get(currentLoopPosition).getIsGroupChecked()) {
                    try {
                        isGroupAdded = true;
//                        final int finalI = currentLoopPosition;
                        mActivity.addDeviceToGroup(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), Short.parseShort(mIntRandomGroupId + ""));
                        Timer innerTimer = new Timer();
                        innerTimer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                if (currentLoopPosition == mArrayListCheckedDevice.size() - 1) {
                                    System.out.println("mStringDevice-FINISH-" + currentLoopPosition);
                                    if (mStartDeviceScanTimer != null)
                                        mStartDeviceScanTimer.start();
                                } else {
                                    currentLoopPosition++;
                                    addingDeviceGroupRequest();
                                }
                            }
                        }, 900);
                    } catch (Exception e) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mActivity.hideProgress();
                            }
                        });
                        e.printStackTrace();
                    }
                }
            }
        } else {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mActivity.hideProgress();
                }
            });
            mActivity.connectDeviceWithProgress();
        }
    }
    /*Fetch device data from local database*/
    private void getDBDeviceList() {
        DataHolder mDataHolderGroup;
        DataHolder mDataHolder;
        DataHolder mDataHolderLocalDevice;
        mArrayListAddDevice = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableGroup + " where " + mActivity.mDbHelper.mFieldGroupLocalID + "= '" + mStringGroupLocalId + "'";
            mDataHolderGroup = mActivity.mDbHelper.read(url);
            if (mDataHolderGroup != null) {
                if (mDataHolderGroup.get_Listholder() != null && mDataHolderGroup.get_Listholder().size() > 0) {
                    for (int k = 0; k < mDataHolderGroup.get_Listholder().size(); k++) {
                        try {
                            mVoLocalGroupData = new VoLocalGroupData();
                            mVoLocalGroupData.setGroup_local_id(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupLocalID));
                            mVoLocalGroupData.setGroup_server_id(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupServerID));
                            mVoLocalGroupData.setUser_id(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupUserId));
                            mVoLocalGroupData.setGroup_comm_id(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupCommId));
                            mVoLocalGroupData.setGroup_comm_hex_id(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupCommHexId));
                            mVoLocalGroupData.setGroup_name(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupName));
                            mVoLocalGroupData.setGroup_switch_status(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus));
                            mVoLocalGroupData.setGroup_is_favourite(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupIsFavourite));
                            mVoLocalGroupData.setGroup_timestamp(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupTimeStamp));
                            mVoLocalGroupData.setGroup_is_active(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupIsActive));
                            mVoLocalGroupData.setGroup_created_at(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupCreatedAt));
                            mVoLocalGroupData.setGroup_updated_at(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupUpdatedAt));
                            mVoLocalGroupData.setGroup_is_sync(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupIsSync));
                            if (mVoLocalGroupData.getGroup_switch_status() != null && mVoLocalGroupData.getGroup_switch_status().equalsIgnoreCase("ON")) {
                                mVoLocalGroupData.setIsGroupChecked(true);
                            } else {
                                mVoLocalGroupData.setIsGroupChecked(false);
                            }
                            mStringGroupName = mVoLocalGroupData.getGroup_name();
                            mAppCompatEditTextGroupName.setText(mStringGroupName);
                            mIntRandomGroupId = Integer.parseInt(mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupCommId));
                            mStringGroupHexId = mDataHolderGroup.get_Listholder().get(k).get(mActivity.mDbHelper.mFieldGroupCommHexId);

                            String urlDevice = "select * from " + mActivity.mDbHelper.mTableDevice + " inner join " + mActivity.mDbHelper.mTableGroupDeviceList + " on " + mActivity.mDbHelper.mFieldGDListLocalDeviceID + "= " + mActivity.mDbHelper.mFieldDeviceLocalId + " AND " + mActivity.mDbHelper.mFieldGDListUserID + "= " + mActivity.mDbHelper.mFieldDeviceUserId + " where " + mActivity.mDbHelper.mFieldGDListLocalGroupID + "= '" + mStringGroupLocalId + "'" + " AND " + mActivity.mDbHelper.mFieldGDListStatus + "= '1'" + " group by " + mActivity.mDbHelper.mFieldDeviceLocalId;
                            mDataHolder = mActivity.mDbHelper.read(urlDevice);
                            if (mDataHolder != null) {
                                VoDeviceList mVoDeviceList;
                                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                                    mVoDeviceList = new VoDeviceList();
                                    mVoDeviceList.setDevicLocalId(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLocalId));
                                    mVoDeviceList.setDeviceServerid(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceServerId));
                                    mVoDeviceList.setUser_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUserId));
                                    mVoDeviceList.setDevice_Comm_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommID));
                                    mVoDeviceList.setDevice_Comm_hexId(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommHexId));
                                    mVoDeviceList.setDevice_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceName));
                                    mVoDeviceList.setDevice_realName(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceRealName));
                                    mVoDeviceList.setDevice_BleAddress(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceBleAddress).toUpperCase());
                                    mVoDeviceList.setDevice_Type(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceType));
                                    mVoDeviceList.setDevice_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTypeName));
                                    mVoDeviceList.setDevice_ConnStatus(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldConnectStatus));
                                    mVoDeviceList.setDevice_SwitchStatus(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSwitchStatus));
                                    mVoDeviceList.setDevice_is_favourite(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsFavourite));
                                    mVoDeviceList.setDevice_last_state_remember(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLastState));
                                    mVoDeviceList.setDevice_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTimeStamp));
                                    mVoDeviceList.setDevice_is_active(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsActive));
                                    mVoDeviceList.setDevice_created_at(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCreatedAt));
                                    mVoDeviceList.setDevice_updated_at(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUpdatedAt));
                                    mVoDeviceList.setDevice_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsSync));
                                    if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                                        mVoDeviceList.setIsChecked(true);
                                    } else {
                                        mVoDeviceList.setIsChecked(false);
                                    }
                                    mVoDeviceList.setDeviceAlradyInGroup(true);
                                    mVoDeviceList.setIsGroupChecked(true);
                                    mArrayListAddDevice.add(mVoDeviceList);
                                }
                            }

                            String urlLocalDevice = "select * from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceIsActive + "= '1'" + " AND " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                            mDataHolderLocalDevice = mActivity.mDbHelper.read(urlLocalDevice);

                            if (mDataHolderLocalDevice != null) {
                                VoDeviceList mVoDeviceList;
                                for (int i = 0; i < mDataHolderLocalDevice.get_Listholder().size(); i++) {
                                    mVoDeviceList = new VoDeviceList();
                                    mVoDeviceList.setDevicLocalId(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLocalId));
                                    mVoDeviceList.setDeviceServerid(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceServerId));
                                    mVoDeviceList.setUser_id(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUserId));
                                    mVoDeviceList.setDevice_Comm_id(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommID));
                                    mVoDeviceList.setDevice_Comm_hexId(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommHexId));
                                    mVoDeviceList.setDevice_name(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceName));
                                    mVoDeviceList.setDevice_realName(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceRealName));
                                    mVoDeviceList.setDevice_BleAddress(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceBleAddress).toUpperCase());
                                    mVoDeviceList.setDevice_Type(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceType));
                                    mVoDeviceList.setDevice_type_name(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTypeName));
                                    mVoDeviceList.setDevice_ConnStatus(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldConnectStatus));
                                    mVoDeviceList.setDevice_SwitchStatus(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSwitchStatus));
                                    mVoDeviceList.setDevice_is_favourite(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsFavourite));
                                    mVoDeviceList.setDevice_last_state_remember(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLastState));
                                    mVoDeviceList.setDevice_timestamp(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTimeStamp));
                                    mVoDeviceList.setDevice_is_active(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsActive));
                                    mVoDeviceList.setDevice_created_at(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCreatedAt));
                                    mVoDeviceList.setDevice_updated_at(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUpdatedAt));
                                    mVoDeviceList.setDevice_is_sync(mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsSync));
                                    if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                                        mVoDeviceList.setIsChecked(true);
                                    } else {
                                        mVoDeviceList.setIsChecked(false);
                                    }
                                    mVoDeviceList.setIsGroupChecked(false);
                                    mVoDeviceList.setDeviceAlradyInGroup(false);
                                    boolean contains = false;
                                    for (VoDeviceList device : mArrayListAddDevice) {
                                        if (mDataHolderLocalDevice.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLocalId).equals(device.getDevicLocalId())) {
                                            contains = true;
                                            break;
                                        }
                                    }
                                    if (!contains) {
                                        mArrayListAddDevice.add(mVoDeviceList);
                                    }
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

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

        checkAdapterIsEmpty();
        mSwipeRefreshLayout.setRefreshing(false);
    }
    /*Check device acknowledgement response */
    private void getDeviceListData() {
        boolean isAnyDeviceAddedInGroup = false;
        int syncCount = 0;
        String mStringDeviceHexId;
        String mStringDeviceHexData;
        if (mStringGroupHexId != null && mStringGroupHexId.length() >= 2) {
            for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
                for (int i = 0; i < mLeDevicesTemp.size(); i++) {
                    if (isGroupAdded) {
                        if (!mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup()) {
                            mStringDeviceHexId = mArrayListCheckedDevice.get(j).getDevice_Comm_hexId();
                            if (mStringDeviceHexId != null && !mStringDeviceHexId.equalsIgnoreCase("")) {
                                mStringDeviceHexData = mLeDevicesTemp.get(i).getDeviceHexData();
                                if (mArrayListCheckedDevice.get(j).getIsDeviceAlradyInGroup()) {
                                    if (!mArrayListCheckedDevice.get(j).getIsGroupChecked()) {
                                        if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_REMOVE_RSP) && mStringDeviceHexData.toLowerCase().contains(mStringDeviceHexId.toLowerCase())) {
                                            isAnyDeviceAddedInGroup = true;
                                            mArrayListCheckedDevice.get(j).setDeviceSyncWithGroup(true);
                                            syncCount = syncCount + 1;
                                            break;
                                        }
                                    } else {
                                        mArrayListCheckedDevice.get(j).setDeviceSyncWithGroup(true);
                                        break;
                                    }
                                } else {
                                    if (mArrayListCheckedDevice.get(j).getIsGroupChecked()) {
                                        if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_ADD_RSP) && mStringDeviceHexData.toLowerCase().contains(mStringDeviceHexId.toLowerCase())) {
                                            isAnyDeviceAddedInGroup = true;
                                            mArrayListCheckedDevice.get(j).setDeviceSyncWithGroup(true);
                                            syncCount = syncCount + 1;
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        mActivity.hideProgress();
        if (isAnyDeviceAddedInGroup) {
            Calendar cal = Calendar.getInstance();
            Date currentLocalTime = cal.getTime();
            String mStringSelectedDeviceId = "";
            ContentValues mContentValuesGD;
            //Group
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(mActivity.mDbHelper.mFieldGroupName, mStringGroupName);
            mContentValues.put(mActivity.mDbHelper.mFieldGroupTimeStamp, cal.getTimeInMillis());
            mContentValues.put(mActivity.mDbHelper.mFieldGroupUpdatedAt, mDateFormatDb.format(currentLocalTime));
            mContentValues.put(mActivity.mDbHelper.mFieldGroupIsSync, "0");

            String[] mArray = new String[]{mStringGroupLocalId};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValues, mActivity.mDbHelper.mFieldGroupLocalID + "=?", mArray);
            String mStringQuery = "delete from " + mActivity.mDbHelper.mTableGroupDeviceList + " where " + mActivity.mDbHelper.mFieldGDListLocalGroupID + "= '" + mStringGroupLocalId + "'";
            mActivity.mDbHelper.exeQuery(mStringQuery);

            // GroupDevice
            for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
                if (mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup() && mArrayListCheckedDevice.get(j).getIsGroupChecked()) {

                    if (mArrayListCheckedDevice.get(j).getDeviceServerId() != null && !mArrayListCheckedDevice.get(j).getDeviceServerId().equalsIgnoreCase("") && !mArrayListCheckedDevice.get(j).getDeviceServerId().equalsIgnoreCase("null")) {
                        mStringSelectedDeviceId = mStringSelectedDeviceId + mArrayListCheckedDevice.get(j).getDeviceServerId() + ", ";
                    }
                    mContentValuesGD = new ContentValues();
                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListUserID, mActivity.mPreferenceHelper.getUserId());
                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListLocalDeviceID, mArrayListCheckedDevice.get(j).getDevicLocalId());
                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListServerDeviceID, mArrayListCheckedDevice.get(j).getDeviceServerId());
                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListLocalGroupID, mStringGroupLocalId);
                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListServerGroupID, mStringGroupServerId);
                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListStatus, "1");
                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListCreatedDate, mDateFormatDb.format(currentLocalTime));
                    mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableGroupDeviceList, mContentValuesGD);

                }
            }
            isGroupAdded = false;
            mLeDevicesTemp = new ArrayList<>();
            if (mActivity.mPreferenceHelper.getIsSkipUser()) {
                showGroupAddAlert();
            } else {
                if (!mActivity.mUtility.haveInternet()) {
                    showGroupAddAlert();
                } else {
                    if (mStringSelectedDeviceId != null && !mStringSelectedDeviceId.equalsIgnoreCase("") && !mStringSelectedDeviceId.equalsIgnoreCase(null)) {
                        if (mVoLocalGroupData != null) {
                            StringBuilder sb = new StringBuilder(mStringSelectedDeviceId);
                            sb.deleteCharAt(sb.length() - 1);
                            sb.deleteCharAt(sb.length() - 1);
                            mStringSelectedDeviceId = sb.toString();
                            updateGroupAPI(mStringSelectedDeviceId);
                        }
                    } else {
                        showGroupAddAlert();
                    }
                }
            }
        } else {
            showGroupRetryAlert();
        }
    }
    /*Call api and update group data to server*/
    public void updateGroupAPI(String deviceId) {
        mActivity.mUtility.ShowProgress("Please Wait..");
        mActivity.mUtility.hideKeyboard(mActivity);

        Map<String, String> params = new HashMap<String, String>();
        params.put("device_token", mActivity.mPreferenceHelper.getDeviceToken());
        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
        params.put("group_name", mStringGroupName);
        params.put("local_group_id", mIntRandomGroupId + "");
        params.put("local_group_hex_id", mStringGroupHexId.toLowerCase());
        params.put("devices", deviceId);
        params.put("status", "1");// 1-Active,2-DeActive
        params.put("is_favourite", "2");// 1-YES,2-NO
        params.put("is_update", "1");// 0-Insert,1-Update

        if (mVoLocalGroupData.getGroup_is_favourite() != null && !mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("") && !mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("null")) {
            if (mVoLocalGroupData.getGroup_is_favourite().equalsIgnoreCase("1")) {
                params.put("is_favourite", "1");
            }
        }

        Call<VoAddGroupData> mLogin = mActivity.mApiService.addGroupAPI(params);
        mLogin.enqueue(new Callback<VoAddGroupData>() {
            @Override
            public void onResponse(Call<VoAddGroupData> call, Response<VoAddGroupData> response) {
                mActivity.mUtility.HideProgress();
                if (isAdded()) {
                    VoAddGroupData mVoAddGroupData = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mVoAddGroupData);
                    if (mVoAddGroupData != null && mVoAddGroupData.getResponse().equalsIgnoreCase("true")) {
                        if (mVoAddGroupData.getData() != null && mVoAddGroupData.getData().size() > 0) {
                            ContentValues mContentValues;
                            String[] mArray;
                            ContentValues mContentValuesGD;
                            String[] mArrayGroupLocalId;
                            for (int i = 0; i < mVoAddGroupData.getData().size(); i++) {
                                mContentValues = new ContentValues();
                                mContentValues.put(mActivity.mDbHelper.mFieldGroupServerID, mVoAddGroupData.getData().get(i).getDevice_group_id());
                                mContentValues.put(mActivity.mDbHelper.mFieldGroupIsSync, "1");
                                mArray = new String[]{mStringGroupLocalId};
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValues, mActivity.mDbHelper.mFieldGroupLocalID + "=?", mArray);
                                if (mVoAddGroupData.getData().get(i).getDevices() != null && mVoAddGroupData.getData().get(i).getDevices().size() > 0) {
                                    mContentValuesGD = new ContentValues();
                                    mContentValuesGD.put(mActivity.mDbHelper.mFieldGDListServerGroupID, mVoAddGroupData.getData().get(i).getDevice_group_id());
                                    mArrayGroupLocalId = new String[]{mStringGroupLocalId};
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroupDeviceList, mContentValuesGD, mActivity.mDbHelper.mFieldGDListLocalGroupID + "=?", mArrayGroupLocalId);
                                }
                            }
                        }
                    }
                    showGroupAddAlert();
                }
            }

            @Override
            public void onFailure(Call<VoAddGroupData> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                if (isAdded()) {
                    showGroupAddAlert();
                }
            }
        });
    }

    private void showGroupRetryAlert() {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_no_device_update_in_routine), 1, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void showGroupAddAlert() {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_routine_update_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }
    /*Device scan timer for some time*/
    private class startDeviceScanTimer extends CountDownTimer {

        public startDeviceScanTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            getDeviceListData();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.isAddDeviceScan = false;
        mActivity.mTextViewAdd.setText("Add");
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.cancel();
    }

    private void checkAdapterIsEmpty() {
        if (mDeviceListAdapter.getItemCount() == 0) {
            mTextViewNoDeviceFound.setVisibility(View.VISIBLE);
        } else {
            mTextViewNoDeviceFound.setVisibility(View.GONE);
        }
    }
    /*Device list data adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @Override
        public DeviceListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_alarm_device_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DeviceListAdapter.ViewHolder holder, final int position) {

            if (mArrayListAddDevice.get(position).getDevice_name() != null && !mArrayListAddDevice.get(position).getDevice_name().equalsIgnoreCase("")) {
                holder.mTextViewDeviceName.setText(mArrayListAddDevice.get(position).getDevice_name());
            } else {
                holder.mTextViewDeviceName.setText("");
            }
            if (mArrayListAddDevice.get(position).getDevice_BleAddress() != null && !mArrayListAddDevice.get(position).getDevice_BleAddress().equalsIgnoreCase("")) {
                holder.mTextViewDeviceId.setText(mArrayListAddDevice.get(position).getDevice_BleAddress().replace(":", ""));
            } else {
                holder.mTextViewDeviceId.setText("");
            }
            if (mArrayListAddDevice.get(position).getDevice_Type() != null && !mArrayListAddDevice.get(position).getDevice_Type().equalsIgnoreCase("")) {
                String mStrDeviceType = mArrayListAddDevice.get(position).getDevice_Type();
                if (mStrDeviceType != null && !mStrDeviceType.equalsIgnoreCase("")) {
                    if (mStrDeviceType.equalsIgnoreCase("0100")) {
                        holder.mImageViewDevice.setImageResource(R.drawable.ic_default_pic);
                    } else if (mStrDeviceType.equalsIgnoreCase("0200")) {
                        holder.mImageViewDevice.setImageResource(R.drawable.ic_default_pic);
                    } else if (mStrDeviceType.equalsIgnoreCase("0300")) {
                        holder.mImageViewDevice.setImageResource(R.drawable.ic_default_switch_icon);
                    } else if (mStrDeviceType.equalsIgnoreCase("0400")) {
                        holder.mImageViewDevice.setImageResource(R.drawable.ic_default_socket_icon);
                    } else if (mStrDeviceType.equalsIgnoreCase("0500")) {
                        holder.mImageViewDevice.setImageResource(R.drawable.ic_default_pic);
                    } else {
                        holder.mImageViewDevice.setImageResource(R.drawable.ic_default_pic);
                    }
                    holder.mImageViewDevice.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
                }
            }
            if (mArrayListAddDevice.get(position).getIsGroupChecked()) {
                holder.mAppCompatCheckBox.setChecked(true);
            } else {
                holder.mAppCompatCheckBox.setChecked(false);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListAddDevice != null) {
                        if (position < mArrayListAddDevice.size()) {
                            mActivity.mUtility.hideKeyboard(mActivity);
                            isAnythingChange = true;
                            System.out.println("getIsGroupChecked()-" + mArrayListAddDevice.get(position).getIsGroupChecked());
                            if (mArrayListAddDevice.get(position).getIsGroupChecked()) {
                                mArrayListAddDevice.get(position).setIsGroupChecked(false);
                                holder.mAppCompatCheckBox.setChecked(false);
                            } else {
                                mArrayListAddDevice.get(position).setIsGroupChecked(true);
                                holder.mAppCompatCheckBox.setChecked(true);
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
            @BindView(R.id.raw_alarm_device_list_item_textview_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_alarm_device_list_item_textview_device_id)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_alarm_device_list_item_imageview_device)
            ImageView mImageViewDevice;
            @BindView(R.id.raw_alarm_device_list_item_checkbox)
            AppCompatCheckBox mAppCompatCheckBox;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
