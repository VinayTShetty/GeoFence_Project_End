package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.Nullable;

import com.evergreen.ble.advertisement.ManufactureData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vithamastech.smartlight.LoginActivity;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.Vo.VoLocalGroupData;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.vithamastech.smartlight.interfaces.onSyncComplete;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 22-12-2017.
 */

public class FragmentGroups extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    @BindView(R.id.fragment_group_button_add_group)
    Button mButtonAddDevice;
    @BindView(R.id.fragment_group_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_group_relativelayout_no_group)
    RelativeLayout mRelativeLayoutNoDeviceFound;
    @BindView(R.id.fragment_group_swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.fragment_group_floating_button_add_group)
    FloatingActionButton mFloatingActionButtonAdd;

    @BindView(R.id.fragment_group_switch_all_group)
    SwitchCompat mSwitchCompatAllGroup;
    @BindView(R.id.fragment_group_relativelayout_all_group)
    RelativeLayout mRelativeLayoutAllGroup;
    @BindView(R.id.fragment_group_textview_all_device)
    TextView mTextViewAllDeviceHeader;
    @BindView(R.id.fragment_group_textview_groups)
    TextView mTextViewGroupHeader;


    ArrayList<VoLocalGroupData> mArrayListGroupList = new ArrayList<>();
    ArrayList<VoDeviceList> mArrayListCheckedDevice = new ArrayList<>();
    GroupListAdapter mGroupListAdapter;

    private boolean isCalling = true;
    boolean isGroupDeleted = false;
    boolean mABooleanIsReady = false;
    startDeviceScanTimer mStartDeviceScanTimer;
    ArrayList<VoBluetoothDevices> mLeDevicesTemp = new ArrayList<>();
    private int mExpandedPosition = -1;
    private int previousExpandedPosition = -1;

    int currentLoopPosition = 0;

    private boolean isCurrentFragment = false;
    private String mStringGroupLocalId = "";
    private String mStringGroupServerId = "";
    private String mStringGroupComId = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_groups, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.isAddDeviceScan = true;
        mSwipeRefreshLayout.setColorSchemeResources(
                R.color.colorBlueText,
                R.color.colorBlack);
        isGroupDeleted = false;
        isCurrentFragment = true;
        mABooleanIsReady = false;
        mActivity.mLeDevicesTemp = new ArrayList<>();
        mStartDeviceScanTimer = new startDeviceScanTimer(8000, 1000);
        if (mActivity.mPreferenceHelper.getIsAllDeviceOn()) {
            mSwitchCompatAllGroup.setChecked(true);
        } else {
            mSwitchCompatAllGroup.setChecked(false);
        }
        GradientDrawable mGradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                new int[]{Color.parseColor("#8A2387"), Color.parseColor("#E94057"), Color.parseColor("#F27121")});
        mGradientDrawable.setCornerRadius(getResources().getDimension(R.dimen._5sdp));
        mRelativeLayoutAllGroup.setBackground(mGradientDrawable);
        mSwitchCompatAllGroup.setOnCheckedChangeListener(powerChange);

        Timer innerTimer = new Timer();
        innerTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                mABooleanIsReady = true;
            }
        }, 200);
        isCalling = true;
        /*Get all group device list from local database*/
        getDBGroupList(true);

        /*Refresh group data from database*/
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (!isCalling) {
                    isCalling = true;
                    getDBGroupList(false);
                    if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
                        if (mActivity.isFromLogin) {
                            mActivity.new syncGroupDataAsyncTask(true).execute("");
                        } else {
                            mActivity.new syncGroupDataAsyncTask(false).execute("");
                        }
                    }
                }
            }
        });
        /*Device connections, scan call back*/
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {
            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_REMOVE_RSP)) {
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
        /*On Sync group complete listener*/
        mActivity.setOnSyncCompleteListner(new onSyncComplete() {
            @Override
            public void onDeviceSyncComplete() {

            }

            @Override
            public void onGroupSyncComplete() {
                if (isAdded()) {
                    if (isCurrentFragment) {
                        if (!isCalling) {
                            isCalling = true;
                            getDBGroupList(false);
                        }
                    }
                }
            }

        });
        /*Scroll Hide/show add button*/
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && mFloatingActionButtonAdd.getVisibility() == View.VISIBLE) {
                    mFloatingActionButtonAdd.hide();
                } else if (dy < 0 && mFloatingActionButtonAdd.getVisibility() != View.VISIBLE) {
                    mFloatingActionButtonAdd.show();
                }
            }
        });
        return mViewRoot;
    }

    /*Control all device data*/
    @OnClick(R.id.fragment_group_relativelayout_all_group)
    public void onAllGroupClick(View mView) {
        if (isAdded()) {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                if (mActivity.getIsDeviceSupportedAdvertisment()) {
                    if (!mActivity.isPingRequestSent) {
                        mActivity.sendPingRequestToDevice();
                    } else {
                        FragmentDeviceSetColor mFragmentDeviceSetColor = new FragmentDeviceSetColor();
                        Bundle mBundle = new Bundle();
                        mBundle.putBoolean("intent_is_from_all_group", true);
                        mBundle.putBoolean("intent_is_from_group", true);
                        mActivity.replacesFragment(mFragmentDeviceSetColor, true, mBundle, 0);
                    }
                } else {
                    FragmentDeviceSetColor mFragmentDeviceSetColor = new FragmentDeviceSetColor();
                    Bundle mBundle = new Bundle();
                    mBundle.putBoolean("intent_is_from_all_group", true);
                    mBundle.putBoolean("intent_is_from_group", true);
                    mActivity.replacesFragment(mFragmentDeviceSetColor, true, mBundle, 0);
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        }
    }

    /*Add Group Click listener*/
    @OnClick(R.id.fragment_group_floating_button_add_group)
    public void onFloatingAddButtonClick(View mView) {
        if (isAdded()) {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                String urlString = "SELECT (SELECT count(*) from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceIsActive + "= 1" + " AND " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + ") as count";
                int tableDeviceCount = mActivity.mDbHelper.getCountRecordByQuery(urlString);
                if (tableDeviceCount > 0) {
                    if (mActivity.getIsDeviceSupportedAdvertisment()) {
                        if (!mActivity.isPingRequestSent) {
                            mActivity.sendPingRequestToDevice();
                        } else {
                            FragmentAddGroups mFragmentAddGroups = new FragmentAddGroups();
                            mActivity.replacesFragment(mFragmentAddGroups, true, null, 0);
                        }
                    } else {
                        FragmentAddGroups mFragmentAddGroups = new FragmentAddGroups();
                        mActivity.replacesFragment(mFragmentAddGroups, true, null, 0);
                    }
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_alarm_add_at_least_one_device), 3, true);
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        }
    }

    /*Add Group Click listener*/
    @OnClick(R.id.fragment_group_button_add_group)
    public void onAddButtonClick(View mView) {
        if (isAdded()) {
            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                String urlString = "SELECT (SELECT count(*) from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceIsActive + "= 1" + " AND " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + ") as count";
                int tableDeviceCount = mActivity.mDbHelper.getCountRecordByQuery(urlString);
                if (tableDeviceCount > 0) {
                    if (mActivity.getIsDeviceSupportedAdvertisment()) {
                        if (!mActivity.isPingRequestSent) {
                            mActivity.sendPingRequestToDevice();
                        } else {
                            FragmentAddGroups mFragmentAddGroups = new FragmentAddGroups();
                            mActivity.replacesFragment(mFragmentAddGroups, true, null, 0);
                        }
                    } else {
                        FragmentAddGroups mFragmentAddGroups = new FragmentAddGroups();
                        mActivity.replacesFragment(mFragmentAddGroups, true, null, 0);
                    }
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_alarm_add_at_least_one_device), 3, true);
                }
            } else {
                mActivity.connectDeviceWithProgress();
            }
        }
    }

    /*Fetch group list from database*/
    private void getDBGroupList(boolean isGetDataFromServer) {
        mRelativeLayoutAllGroup.setVisibility(View.VISIBLE);
        DataHolder mDataHolder;

        mArrayListGroupList = new ArrayList<>();
        mExpandedPosition = -1;
        previousExpandedPosition = -1;
        isGroupDeleted = false;
        try {
            String url = "select * from " + DBHelper.mTableGroup + " join " + DBHelper.mTableGroupDeviceList + " on " +
                    DBHelper.mFieldGDListLocalGroupID + "= " + DBHelper.mFieldGroupLocalID + " AND " + DBHelper.mFieldGDListUserID +
                    "= " + DBHelper.mFieldGroupUserId + " AND " + DBHelper.mFieldGDListStatus + "= '1'" + " where "
                    + DBHelper.mFieldGroupIsActive + "= '1'" + " AND " + DBHelper.mFieldGroupUserId + "= '"
                    + mActivity.mPreferenceHelper.getUserId() + "'" + " group by " + DBHelper.mFieldGroupLocalID +
                    " ORDER BY " + DBHelper.mFieldGroupIsFavourite + " desc";

            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                VoLocalGroupData mVoLocalGroupData;
                ArrayList<VoDeviceList> mArrayListUnSyncGroupDeviceList;
                VoDeviceList mVoDeviceList;
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoLocalGroupData = new VoLocalGroupData();
                    mVoLocalGroupData.setGroup_local_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupLocalID));
                    mVoLocalGroupData.setGroup_server_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupServerID));
                    mVoLocalGroupData.setUser_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupUserId));
                    mVoLocalGroupData.setGroup_comm_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupCommId));
                    mVoLocalGroupData.setGroup_comm_hex_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupCommHexId));
                    mVoLocalGroupData.setGroup_name(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupName));
                    mVoLocalGroupData.setGroup_switch_status(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupDeviceSwitchStatus));
                    mVoLocalGroupData.setGroup_is_favourite(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupIsFavourite));
                    mVoLocalGroupData.setGroup_timestamp(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupTimeStamp));
                    mVoLocalGroupData.setGroup_is_active(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupIsActive));
                    mVoLocalGroupData.setGroup_created_at(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupCreatedAt));
                    mVoLocalGroupData.setGroup_updated_at(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupUpdatedAt));
                    mVoLocalGroupData.setGroup_is_sync(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupIsSync));
                    if (mVoLocalGroupData.getGroup_switch_status() != null && mVoLocalGroupData.getGroup_switch_status().equalsIgnoreCase("ON")) {
                        mVoLocalGroupData.setIsGroupChecked(true);
                    } else {
                        mVoLocalGroupData.setIsGroupChecked(false);
                    }
                    mArrayListUnSyncGroupDeviceList = new ArrayList<>();
                    DataHolder mDataHolderDevice;
                    try {
                        String urlDevice = "select * from " + DBHelper.mTableDevice + " inner join " +
                                DBHelper.mTableGroupDeviceList + " on " + DBHelper.mFieldGDListLocalDeviceID + "= " +
                                DBHelper.mFieldDeviceLocalId + " AND " + DBHelper.mFieldDeviceIsActive + "= 1" + " AND "
                                + DBHelper.mFieldGDListStatus + "= 1" + " where " + DBHelper.mFieldGDListLocalGroupID + "= '"
                                + mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldGroupLocalID) + "'";

                        mDataHolderDevice = mActivity.mDbHelper.read(urlDevice);
                        if (mDataHolderDevice != null) {
                            for (int j = 0; j < mDataHolderDevice.get_Listholder().size(); j++) {
                                mVoDeviceList = new VoDeviceList();
                                mVoDeviceList.setDevicLocalId(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceLocalId));
                                mVoDeviceList.setDeviceServerid(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceServerId));
                                mVoDeviceList.setUser_id(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceUserId));
                                mVoDeviceList.setDevice_Comm_id(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceCommID));
                                mVoDeviceList.setDevice_Comm_hexId(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceCommHexId));
                                mVoDeviceList.setDevice_name(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceName));
                                mVoDeviceList.setDevice_realName(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceRealName));
                                mVoDeviceList.setDevice_BleAddress(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceBleAddress).toUpperCase());
                                mVoDeviceList.setDevice_Type(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceType));
                                mVoDeviceList.setDevice_type_name(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceTypeName));
                                mVoDeviceList.setDevice_ConnStatus(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldConnectStatus));
                                mVoDeviceList.setDevice_SwitchStatus(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldSwitchStatus));
                                mVoDeviceList.setDevice_is_favourite(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceIsFavourite));
                                mVoDeviceList.setDevice_last_state_remember(mDataHolder.get_Listholder().get(j).get(DBHelper.mFieldDeviceLastState));
                                mVoDeviceList.setDevice_timestamp(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceTimeStamp));
                                mVoDeviceList.setDevice_is_active(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceIsActive));
                                mVoDeviceList.setDevice_created_at(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceCreatedAt));
                                mVoDeviceList.setDevice_updated_at(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceUpdatedAt));
                                mVoDeviceList.setDevice_is_sync(mDataHolderDevice.get_Listholder().get(j).get(DBHelper.mFieldDeviceIsSync));
                                if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                                    mVoDeviceList.setIsChecked(true);
                                } else {
                                    mVoDeviceList.setIsChecked(false);
                                }
                                mArrayListUnSyncGroupDeviceList.add(mVoDeviceList);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mVoLocalGroupData.setmVoDeviceLists(mArrayListUnSyncGroupDeviceList);
                    mArrayListGroupList.add(mVoLocalGroupData);
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
        mGroupListAdapter = new GroupListAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mGroupListAdapter);
        mGroupListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
        isCalling = false;
        mSwipeRefreshLayout.setRefreshing(false);


        if (isGetDataFromServer && mActivity.mUtility.haveInternet()) {
            if (!mActivity.mPreferenceHelper.getIsGroupSync() && !mActivity.mPreferenceHelper.getIsSkipUser()) {
                if (mActivity.isFromLogin) {
                    mActivity.new syncGroupDataAsyncTask(true).execute("");
                } else {
                    mActivity.new syncGroupDataAsyncTask(false).execute("");
                }
            }
        }
    }

    /*Device ble scan acknowledgement response and check status*/
    private void getDeviceListData() {
        boolean isAnyDeviceDeletedInGroup = false;
        int syncCount = 0;
        String mStringGroupHexSwap;
        String mStringDeviceHexData;
        ContentValues mContentValuesAD;
        for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
            mStringGroupHexSwap = mArrayListCheckedDevice.get(j).getDevice_Comm_hexId();
            for (int i = 0; i < mLeDevicesTemp.size(); i++) {
                if (isGroupDeleted) {
                    if (!mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup()) {
                        if (mStringGroupHexSwap != null && !mStringGroupHexSwap.equalsIgnoreCase("")) {
                            mStringDeviceHexData = mLeDevicesTemp.get(i).getDeviceHexData();
                            if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_REMOVE_RSP) && mStringDeviceHexData.toLowerCase().contains(mStringGroupHexSwap.toLowerCase())) {
                                isAnyDeviceDeletedInGroup = true;
                                syncCount = syncCount + 1;
                                mArrayListCheckedDevice.get(j).setDeviceSyncWithGroup(true);
                                if (mArrayListCheckedDevice.get(j).getDeviceServerId() != null && !mArrayListCheckedDevice.get(j).getDeviceServerId().equalsIgnoreCase("") && !mArrayListCheckedDevice.get(j).getDeviceServerId().equalsIgnoreCase("null")) {
                                    mContentValuesAD = new ContentValues();
                                    mContentValuesAD.put(mActivity.mDbHelper.mFieldGDListStatus, "0");
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroupDeviceList, mContentValuesAD, mActivity.mDbHelper.mFieldGDListLocalGroupID + " =? AND " + mActivity.mDbHelper.mFieldGDListLocalDeviceID + " =?", new String[]{mStringGroupLocalId, mArrayListCheckedDevice.get(j).getDevicLocalId()});
                                } else {
                                    mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableGroupDeviceList + " where " + mActivity.mDbHelper.mFieldGDListLocalDeviceID + "= '" + mArrayListCheckedDevice.get(j).getDevicLocalId() + "'" + " AND " + mActivity.mDbHelper.mFieldGDListLocalGroupID + "= '" + mStringGroupLocalId + "'");
                                }
                                break;
                            }
                        }
                    }
                }
            }
        }
        mActivity.hideProgress();
        if (isAnyDeviceDeletedInGroup) {
            isGroupDeleted = false;
            if (mArrayListCheckedDevice.size() == syncCount) {
                if (mStringGroupServerId != null && !mStringGroupServerId.equalsIgnoreCase("") && !mStringGroupServerId.equalsIgnoreCase("null")) {
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(mActivity.mDbHelper.mFieldGroupIsActive, "0");
                    mContentValues.put(mActivity.mDbHelper.mFieldGroupIsSync, "0");
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValues, mActivity.mDbHelper.mFieldGroupLocalID + "=?", new String[]{mStringGroupLocalId});
                } else {
                    String mStringQuery = "delete from " + mActivity.mDbHelper.mTableGroup + " where " + mActivity.mDbHelper.mFieldGroupLocalID + "= '" + mStringGroupLocalId + "'";
                    mActivity.mDbHelper.exeQuery(mStringQuery);
                }

                showGroupDeleteAlert(getResources().getString(R.string.frg_group_delete_group));
            } else {
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(mActivity.mDbHelper.mFieldGroupIsSync, "0");
                String[] mArray = new String[]{mStringGroupLocalId};
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValues, mActivity.mDbHelper.mFieldGroupLocalID + "=?", mArray);
                showGroupDeleteAlert(getResources().getString(R.string.frg_group_cant_delete_group));
            }
            isCalling = true;
            getDBGroupList(false);
            mArrayListCheckedDevice = new ArrayList<>();
            if (mActivity.mUtility.haveInternet()) {
                if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                    mActivity.new syncGroupDataAsyncTask(false).execute("");
                }
            }
        } else {
            showGroupRetryAlert();
        }
    }

    /*fetch delete group device list */
    private void deleteGroup() {
        DataHolder mDataHolder;
        mArrayListCheckedDevice = new ArrayList<>();
        currentLoopPosition = 0;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableGroupDeviceList + " inner join " + mActivity.mDbHelper.mTableGroup + " on " + mActivity.mDbHelper.mFieldGroupLocalID + "=" + mActivity.mDbHelper.mFieldGDListLocalGroupID + " AND " + mActivity.mDbHelper.mFieldGDListStatus + "= 1" + " inner join " + mActivity.mDbHelper.mTableDevice + " on " + mActivity.mDbHelper.mFieldDeviceLocalId + "=" + mActivity.mDbHelper.mFieldGDListLocalDeviceID + " where " + mActivity.mDbHelper.mFieldGDListLocalGroupID + "= '" + mStringGroupLocalId + "'";
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                VoDeviceList mVoDeviceList;
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoDeviceList = new VoDeviceList();
                    mVoDeviceList.setGroup_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldGroupLocalID));
                    mVoDeviceList.setGroup_comm_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldGroupCommId));
                    mVoDeviceList.setGroup_comm_hex_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldGroupCommHexId));
                    mVoDeviceList.setGroup_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldGroupName));
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
                        mVoDeviceList.setIsGroupChecked(true);
                    } else {
                        mVoDeviceList.setIsGroupChecked(false);
                    }
                    mArrayListCheckedDevice.add(mVoDeviceList);
                }
            } else {
                mActivity.hideProgress();
                showGroupRetryAlert();
            }
        } catch (Exception e) {
            mActivity.hideProgress();
            e.printStackTrace();
        }
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            if (mArrayListCheckedDevice.size() > 0) {
                mActivity.showProgress("Deleting Rooms...", true);
                deleteDeviceGroupRequest();
            } else {
                mActivity.hideProgress();
                showGroupRetryAlert();
            }
        } else {
            mActivity.hideProgress();
            mActivity.connectDeviceWithProgress();
        }

    }

    private void showGroupRetryAlert() {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_group_not_delete_group), 1, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void showGroupDeleteAlert(String msg) {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(msg, 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    /*send group device request*/
    private void deleteDeviceGroupRequest() {
        if (currentLoopPosition >= mArrayListCheckedDevice.size()) {
            mActivity.hideProgress();
            return;
        }
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            try {
                isGroupDeleted = true;
                mActivity.deleteAllGroupDevice(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), Short.parseShort(mStringGroupComId), false);

                Timer innerTimer = new Timer();
                innerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (currentLoopPosition == mArrayListCheckedDevice.size() - 1) {
                            System.out.println("FINISH" + currentLoopPosition);
                            if (mStartDeviceScanTimer != null)
                                mStartDeviceScanTimer.start();
                        } else {
                            currentLoopPosition++;
                            deleteDeviceGroupRequest();
                        }
                    }
                }, 1000);

            } catch (Exception e) {
                e.printStackTrace();
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.hideProgress();
                        showGroupRetryAlert();
                    }
                });
            }
        } else {
            mActivity.hideProgress();
            mActivity.connectDeviceWithProgress();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        unbinder.unbind();
        mActivity.isAddDeviceScan = false;
        isCurrentFragment = false;
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.cancel();
    }

    @Override
    public void onPause() {
        super.onPause();
        isCurrentFragment = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isCurrentFragment = true;
    }

    /*Device ble scan acknowledgement response check timer*/
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

    private void checkAdapterIsEmpty() {
        if (mGroupListAdapter != null) {
            if (mGroupListAdapter.getItemCount() == 0) {
                mRelativeLayoutNoDeviceFound.setVisibility(View.VISIBLE);
                mRecyclerView.setVisibility(View.GONE);
            } else {
                mRelativeLayoutNoDeviceFound.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    GradientDrawable mGradientDrawable;

    /*Group List Adapter*/
    public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ViewHolder> {

        @Override
        public GroupListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_group_list_item, parent, false);
            return new GroupListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final GroupListAdapter.ViewHolder holder, final int position) {

            if (mArrayListGroupList.get(position).getGroup_name() != null && !mArrayListGroupList.get(position).getGroup_name().equalsIgnoreCase("")) {
                holder.mTextViewGroupName.setText(mArrayListGroupList.get(position).getGroup_name());
            } else {
                holder.mTextViewGroupName.setText("");
            }

            if (mArrayListGroupList.get(position).getIsGroupChecked()) {
                holder.mSwitchGroup.setChecked(true);
            } else {
                holder.mSwitchGroup.setChecked(false);
            }
            if (mArrayListGroupList.get(position).getGroup_is_favourite() != null && mArrayListGroupList.get(position).getGroup_is_favourite().equalsIgnoreCase("1")) {
                holder.mImageViewFavourite.setImageResource(R.drawable.ic_favorite);
            } else {
                holder.mImageViewFavourite.setImageResource(R.drawable.ic_unfavorite);
            }
            Glide.with(mActivity)
                    .load(R.drawable.ic_default_group_icon)
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .crossFade()
                    .placeholder(R.drawable.ic_default_group_icon)
                    .into(holder.mImageViewGroup);
            holder.mImageViewGroup.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
            final boolean isExpanded = mExpandedPosition == position;
            holder.mLinearLayoutExpanded.setVisibility(isExpanded ? View.VISIBLE : View.GONE);
            if (isExpanded) {
                previousExpandedPosition = position;
            }
            if (position % 2 == 0) {
                mGradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{Color.parseColor("#8A2387"), Color.parseColor("#4286f4")});
            } else {
                mGradientDrawable = new GradientDrawable(
                        GradientDrawable.Orientation.LEFT_RIGHT,
                        new int[]{Color.parseColor("#4286f4"), Color.parseColor("#8A2387")});
            }
            mGradientDrawable.setCornerRadius(getResources().getDimension(R.dimen._5sdp));
            holder.mRelativeLayoutMain.setBackground(mGradientDrawable);
            holder.mImageViewMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListGroupList != null) {
                        if (position < mArrayListGroupList.size()) {
                            mExpandedPosition = isExpanded ? -1 : position;
                            notifyItemChanged(previousExpandedPosition);
                            notifyItemChanged(position);
                        }
                    }
                }
            });
            holder.mSwitchGroup.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isCHecked) {
                    if (holder.mSwitchGroup.isClickable()) {
                        if (mArrayListGroupList.get(position) != null) {
                            if (position < mArrayListGroupList.size()) {
                                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                    if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                        if (!mActivity.isPingRequestSent) {
                                            mActivity.sendPingRequestToDevice();
                                        } else {
                                            onOffGroupLight(position, isCHecked);
                                        }
                                    } else {
                                        onOffGroupLight(position, isCHecked);
                                    }
                                } else {
                                    mActivity.connectDeviceWithProgress();
                                }
                            }
                        }
                    }
                }
            });
            holder.mImageViewFavourite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mArrayListGroupList.get(position) != null) {
                        if (position < mArrayListGroupList.size()) {
                            ContentValues mContentValues = new ContentValues();
                            if (mArrayListGroupList.get(position).getGroup_is_favourite() != null && mArrayListGroupList.get(position).getGroup_is_favourite().equalsIgnoreCase("1")) {
                                mContentValues.put(mActivity.mDbHelper.mFieldGroupIsFavourite, "0");
                                mArrayListGroupList.get(position).setGroup_is_favourite("0");
                            } else {
                                mContentValues.put(mActivity.mDbHelper.mFieldGroupIsFavourite, "1");
                                mArrayListGroupList.get(position).setGroup_is_favourite("1");
                            }
                            mContentValues.put(mActivity.mDbHelper.mFieldGroupIsSync, "0");
                            mArrayListGroupList.get(position).setGroup_is_sync("0");
                            String[] mArray = new String[]{mArrayListGroupList.get(position).getGroup_local_id()};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableGroup, mContentValues, mActivity.mDbHelper.mFieldGroupLocalID + "=?", mArray);
                            if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                if (mActivity.mUtility.haveInternet()) {
                                    mActivity.updateGroupAPI(mArrayListGroupList.get(position));
                                }
                            }
                            isCalling = true;
                            getDBGroupList(false);
                        }
                    }
                }
            });
            holder.mImageViewEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                        if (mActivity.mUtility.haveInternet()) {
                            checkAuthenticationAPI(true, position, true);
                        } else {
                            mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_group_not_delete_without_internet), 1, true);
                        }
                    } else {
                        if (mArrayListGroupList.get(position) != null) {
                            if (position < mArrayListGroupList.size()) {
                                FragmentEditGroups mFragmentEditGroups = new FragmentEditGroups();
                                Bundle mBundle = new Bundle();
                                mBundle.putString("group_local_id", mArrayListGroupList.get(position).getGroup_local_id());
                                mBundle.putString("group_server_id", mArrayListGroupList.get(position).getGroup_server_id());
                                mActivity.replacesFragment(mFragmentEditGroups, true, mBundle, 0);
                            }
                        }
                    }
                }
            });
            holder.mImageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mActivity.isAddDeviceScan = true;
                    mActivity.mLeDevicesTemp = new ArrayList<>();
                    mLeDevicesTemp = new ArrayList<>();
                    mActivity.RescanDevice(false);
                    if (mArrayListGroupList.get(position) != null) {
                        if (position < mArrayListGroupList.size()) {
                            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.frg_group_delete_group_alert), getResources().getString(R.string.frg_group_delete_confirmation), "Yes", "No", true, 2, new onAlertDialogCallBack() {
                                    @Override
                                    public void PositiveMethod(DialogInterface dialog, int id) {
                                        if (mArrayListGroupList.get(position) != null) {
                                            if (position < mArrayListGroupList.size()) {
                                                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                                    mStringGroupLocalId = mArrayListGroupList.get(position).getGroup_local_id();
                                                    mStringGroupServerId = mArrayListGroupList.get(position).getGroup_server_id();
                                                    mStringGroupComId = mArrayListGroupList.get(position).getGroup_comm_id();
                                                    if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                                        if (!mActivity.isPingRequestSent) {
                                                            mActivity.sendPingRequestToDevice();
                                                        } else {
                                                            if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                                                if (mActivity.mUtility.haveInternet()) {
                                                                    checkAuthenticationAPI(true, position, false);
                                                                } else {
                                                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_group_not_delete_without_internet), 1, true);
                                                                }
                                                            } else {
                                                                deleteGroupRequest(position);
                                                            }
                                                        }
                                                    } else {
                                                        if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
                                                            if (mActivity.mUtility.haveInternet()) {
                                                                checkAuthenticationAPI(true, position, false);
                                                            } else {
                                                                mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_group_not_delete_without_internet), 1, true);
                                                            }
                                                        } else {
                                                            deleteGroupRequest(position);
                                                        }
                                                    }
                                                } else {
                                                    mActivity.connectDeviceWithProgress();
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void NegativeMethod(DialogInterface dialog, int id) {

                                    }
                                });
                            } else {
                                mActivity.connectDeviceWithProgress();
                            }
                        }
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListGroupList.get(position) != null) {
                        if (position < mArrayListGroupList.size()) {
                            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                    if (!mActivity.isPingRequestSent) {
                                        mActivity.sendPingRequestToDevice();
                                    } else {
                                        FragmentDeviceSetColor mFragmentDeviceSetColor = new FragmentDeviceSetColor();
                                        Bundle mBundle = new Bundle();
                                        mBundle.putBoolean("intent_is_from_all_group", false);
                                        mBundle.putBoolean("intent_is_from_group", true);
                                        mBundle.putBoolean("intent_is_turn_on", mArrayListGroupList.get(position).getIsGroupChecked());
                                        mBundle.putString("intent_group_name", mArrayListGroupList.get(position).getGroup_name());
                                        mBundle.putString("intent_comm_id", mArrayListGroupList.get(position).getGroup_comm_id());
                                        mBundle.putString("intent_local_id", mArrayListGroupList.get(position).getGroup_local_id());
                                        mBundle.putString("intent_server_id", mArrayListGroupList.get(position).getGroup_server_id());
                                        mBundle.putString("intent_ble_address", "");
                                        mActivity.replacesFragment(mFragmentDeviceSetColor, true, mBundle, 0);
                                    }
                                } else {
                                    FragmentDeviceSetColor mFragmentDeviceSetColor = new FragmentDeviceSetColor();
                                    Bundle mBundle = new Bundle();
                                    mBundle.putBoolean("intent_is_from_all_group", false);
                                    mBundle.putBoolean("intent_is_from_group", true);
                                    mBundle.putBoolean("intent_is_turn_on", mArrayListGroupList.get(position).getIsGroupChecked());
                                    mBundle.putString("intent_group_name", mArrayListGroupList.get(position).getGroup_name());
                                    mBundle.putString("intent_comm_id", mArrayListGroupList.get(position).getGroup_comm_id());
                                    mBundle.putString("intent_local_id", mArrayListGroupList.get(position).getGroup_local_id());
                                    mBundle.putString("intent_server_id", mArrayListGroupList.get(position).getGroup_server_id());
                                    mBundle.putString("intent_ble_address", "");
                                    mActivity.replacesFragment(mFragmentDeviceSetColor, true, mBundle, 0);
                                }
                            } else {
                                mActivity.connectDeviceWithProgress();
                            }
                        }
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return mArrayListGroupList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_group_list_item_textview_group_name)
            TextView mTextViewGroupName;
            @BindView(R.id.raw_group_list_item_imageview_more)
            ImageView mImageViewMore;
            @BindView(R.id.raw_group_list_item_imageview_favourite)
            ImageView mImageViewFavourite;
            @BindView(R.id.raw_group_list_item_imageview_edit)
            ImageView mImageViewEdit;
            @BindView(R.id.raw_group_list_item_imageview_delete)
            ImageView mImageViewDelete;
            @BindView(R.id.raw_group_list_item_imageview_group)
            ImageView mImageViewGroup;
            @BindView(R.id.raw_group_list_item_imageview_device_list)
            ImageView mImageViewDeviceList;
            @BindView(R.id.raw_group_list_item_switch_group)
            SwitchCompat mSwitchGroup;
            @BindView(R.id.raw_group_list_item_linearlayout_expanded)
            LinearLayout mLinearLayoutExpanded;
            @BindView(R.id.raw_group_list_item_relativelayout_main)
            RelativeLayout mRelativeLayoutMain;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Init delete group request */
    private void deleteGroupRequest(int position) {
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            mStringGroupLocalId = mArrayListGroupList.get(position).getGroup_local_id();
            mStringGroupServerId = mArrayListGroupList.get(position).getGroup_server_id();
            mStringGroupComId = mArrayListGroupList.get(position).getGroup_comm_id();
            if (mActivity.getIsDeviceSupportedAdvertisment()) {
                if (!mActivity.isPingRequestSent) {
                    mActivity.sendPingRequestToDevice();
                } else {
                    deleteGroup();
                }
            } else {
                deleteGroup();
            }
        } else {
            mActivity.connectDeviceWithProgress();
        }
    }

    /*Call Authentication api */
    private void checkAuthenticationAPI(final boolean isShowProgress, final int relativePosition, final boolean isEditGroup) {
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
                    if (isEditGroup) {
                        if (mArrayListGroupList.get(relativePosition) != null) {
                            if (relativePosition < mArrayListGroupList.size()) {
                                FragmentEditGroups mFragmentEditGroups = new FragmentEditGroups();
                                Bundle mBundle = new Bundle();
                                mBundle.putString("group_local_id", mArrayListGroupList.get(relativePosition).getGroup_local_id());
                                mBundle.putString("group_server_id", mArrayListGroupList.get(relativePosition).getGroup_server_id());
                                mActivity.replacesFragment(mFragmentEditGroups, true, mBundle, 0);
                            }
                        }
                    } else {
                        deleteGroupRequest(relativePosition);
                    }

                } else {
                    mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_session_expired), 3, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
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
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again), 1, true);
            }
        });
    }

    /*On/off Group Light state*/
    private void onOffGroupLight(int position, boolean isChecked) {
        String mSwitchStatus;
        if (isChecked) {
            mSwitchStatus = "ON";
            mArrayListGroupList.get(position).setIsGroupChecked(true);
            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(Integer.parseInt(mArrayListGroupList.get(position).getGroup_comm_id()) + ""), false);
        } else {
            mSwitchStatus = "OFF";
            mArrayListGroupList.get(position).setIsGroupChecked(false);
            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(Integer.parseInt(mArrayListGroupList.get(position).getGroup_comm_id()) + ""), false);
        }
        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mArrayListGroupList.get(position).getGroup_comm_id() + "'";
        mActivity.mDbHelper.exeQuery(url);
    }

    /**
     * Called when power button is pressed.
     */
    protected CompoundButton.OnCheckedChangeListener powerChange = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mABooleanIsReady) {
                //    mController.setLightPower(isChecked ? PowerState.ON : PowerState.OFF);
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    if (mActivity.getIsDeviceSupportedAdvertisment()) {
                        if (!mActivity.isPingRequestSent) {
                            mActivity.sendPingRequestToDevice();
                        } else {
                            String mSwitchStatus = "ON";
                            if (isChecked) {
                                mSwitchStatus = "ON";
                                mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                                mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(0 + ""), true);
                            } else {
                                mSwitchStatus = "OFF";
                                mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                                mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true);
                            }
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                            String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                            mActivity.mDbHelper.exeQuery(url);
                            isCalling = true;
                            getDBGroupList(false);
                        }
                    } else {
                        String mSwitchStatus = "ON";
                        if (isChecked) {
                            mSwitchStatus = "ON";
                            mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(0 + ""), true);
                        } else {
                            mSwitchStatus = "OFF";
                            mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                            mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true);
                        }
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            }
        }
    };
}
