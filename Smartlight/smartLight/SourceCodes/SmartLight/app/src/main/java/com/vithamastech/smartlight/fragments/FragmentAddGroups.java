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
import com.vithamastech.smartlight.db.DBHelper;
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

public class FragmentAddGroups extends Fragment {
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
    int mIntRandomGroupId;
    String mStringGroupHexId = "";
    String mStringGroupName = "";
    startDeviceScanTimer mStartDeviceScanTimer;
    private ArrayList<VoBluetoothDevices> mLeDevicesTemp = new ArrayList<>();
    int currentLoopPosition = 0;
    SimpleDateFormat mDateFormatDb;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_add_groups, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_add_group_header);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.VISIBLE);
        mActivity.mTextViewAdd.setText("Save");
        mActivity.showBackButton(true);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
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
                if (mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_ADD_RSP)) {
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
                    mLeDevicesTemp = new ArrayList<>();
                    mActivity.mLeDevicesTemp = new ArrayList<>();
                    mActivity.RescanDevice(false);
                    mArrayListCheckedDevice = new ArrayList<>();
                    currentLoopPosition = 0;
                    mActivity.mUtility.hideKeyboard(mActivity);
                    mStringGroupName = mAppCompatEditTextGroupName.getText().toString().trim();
                    if (mStringGroupName.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_add_group_enter_room_name_alert), 3, true);
                        return;
                    }

                    for (int i = 0; i < mArrayListAddDevice.size(); i++) {
                        if (mArrayListAddDevice.get(i).getIsGroupChecked()) {
                            mArrayListCheckedDevice.add(mArrayListAddDevice.get(i));
                        }
                    }
//                    Handler handler1 = new Handler();
                    if (mArrayListCheckedDevice.size() > 0) {
                        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {

                            mIntRandomGroupId = mActivity.generateRandomNo();
                            mStringGroupHexId = String.format("%04X", (0xFFFF & mIntRandomGroupId));
                            mActivity.showProgress("Saving Rooms...", true);
                            addingDeviceGroupRequest();

                        } else {
                            mActivity.connectDeviceWithProgress();
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
            if (mArrayListCheckedDevice.get(currentLoopPosition).getIsGroupChecked()) {
                isGroupAdded = true;
                try {
//                        final int finalI = currentLoopPosition;
                    mActivity.addDeviceToGroup(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), Short.parseShort(mIntRandomGroupId + ""));
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
                    }, 1000);

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
//            }
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
    // Edited by Muataz Medini. Show only smart bulb and smart strip.
    private void getDBDeviceList() {
        DataHolder mDataHolder;
        mArrayListAddDevice = new ArrayList<>();
        try {
            String url = "select * from " + DBHelper.mTableDevice + " where " + DBHelper.mFieldDeviceIsActive + "= '1'" +
                    " AND " + DBHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" +
                    " AND " + DBHelper.mFieldDeviceType + " = " + "'" + "0100" + "'" + " OR " +
                    DBHelper.mFieldDeviceType + " = " + "'" + "0600" + "'";

            mDataHolder = mActivity.mDbHelper.read(url);

            if (mDataHolder != null) {
                VoDeviceList mVoDeviceList;
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoDeviceList = new VoDeviceList();
                    mVoDeviceList.setDevicLocalId(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceLocalId));
                    mVoDeviceList.setDeviceServerid(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceServerId));
                    mVoDeviceList.setUser_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceUserId));
                    mVoDeviceList.setDevice_Comm_id(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommID));
                    mVoDeviceList.setDevice_Comm_hexId(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceCommHexId));
                    mVoDeviceList.setDevice_name(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceName));
                    mVoDeviceList.setDevice_realName(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceRealName));
                    mVoDeviceList.setDevice_BleAddress(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceBleAddress).toUpperCase());
                    mVoDeviceList.setDevice_Type(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceType));
                    mVoDeviceList.setDevice_type_name(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceTypeName));
                    mVoDeviceList.setDevice_ConnStatus(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldConnectStatus));
                    mVoDeviceList.setDevice_SwitchStatus(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldSwitchStatus));
                    mVoDeviceList.setDevice_last_state_remember(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceLastState));
                    mVoDeviceList.setDevice_is_favourite(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsFavourite));
                    mVoDeviceList.setDevice_timestamp(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceTimeStamp));
                    mVoDeviceList.setDevice_is_active(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsActive));
                    mVoDeviceList.setDevice_created_at(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceCreatedAt));
                    mVoDeviceList.setDevice_updated_at(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceUpdatedAt));
                    mVoDeviceList.setDevice_is_sync(mDataHolder.get_Listholder().get(i).get(DBHelper.mFieldDeviceIsSync));
                    if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
                        mVoDeviceList.setIsChecked(true);
                    } else {
                        mVoDeviceList.setIsChecked(false);
                    }

                    mArrayListAddDevice.add(mVoDeviceList);
                }
//                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
//                    @Override
//                    public int compare(TreatmentList s1, TreatmentList s2) {
//                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
//                    }
//                });
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Check device acknowledgement response */
    private void getDeviceListData() {
        boolean isAnyDeviceAddedInGroup = false;
        if (mStringGroupHexId != null && mStringGroupHexId.length() >= 2) {
            if (mStringGroupHexId.length() == 3) {
                mStringGroupHexId = "0" + mStringGroupHexId;
            }
            mStringGroupHexId = mStringGroupHexId.substring(2) + mStringGroupHexId.substring(0, 2);
            String mStringDeviceHexData;
            String mStringDeviceHexId;
            for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
                for (int i = 0; i < mLeDevicesTemp.size(); i++) {
                    if (isGroupAdded) {
                        if (!mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup()) {
                            mStringDeviceHexData = mLeDevicesTemp.get(i).getDeviceHexData();
                            mStringDeviceHexId = mArrayListCheckedDevice.get(j).getDevice_Comm_hexId();
                            if (mStringDeviceHexId != null && !mStringDeviceHexId.equalsIgnoreCase("")) {
                                if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.GROUP_ADD_RSP) && mStringDeviceHexData.toLowerCase().contains(mStringDeviceHexId.toLowerCase())) {
                                    isAnyDeviceAddedInGroup = true;
                                    mArrayListCheckedDevice.get(j).setDeviceSyncWithGroup(true);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        mActivity.hideProgress();
        if (isAnyDeviceAddedInGroup) {
            boolean isShowAddMessage = false;
            Calendar cal = Calendar.getInstance();
            Date currentLocalTime = cal.getTime();
            String mStringSelectedDeviceId = "";
            String mStringGroupLocalId = "";

            //Group
            ContentValues mContentValues = new ContentValues();
            mContentValues.put(DBHelper.mFieldGroupServerID, "");
            mContentValues.put(DBHelper.mFieldGroupUserId, mActivity.mPreferenceHelper.getUserId());
            mContentValues.put(DBHelper.mFieldGroupCommId, mIntRandomGroupId + "");
            mContentValues.put(DBHelper.mFieldGroupCommHexId, mStringGroupHexId);
            mContentValues.put(DBHelper.mFieldGroupName, mStringGroupName);
            mContentValues.put(DBHelper.mFieldGroupDeviceSwitchStatus, "ON");
            mContentValues.put(DBHelper.mFieldGroupIsFavourite, "0");
            mContentValues.put(DBHelper.mFieldGroupTimeStamp, cal.getTimeInMillis());
            mContentValues.put(DBHelper.mFieldGroupIsActive, "1");
            mContentValues.put(DBHelper.mFieldGroupCreatedAt, mDateFormatDb.format(currentLocalTime));
            mContentValues.put(DBHelper.mFieldGroupUpdatedAt, mDateFormatDb.format(currentLocalTime));
            mContentValues.put(DBHelper.mFieldGroupIsSync, "0");
            int isInsert = mActivity.mDbHelper.insertRecord(DBHelper.mTableGroup, mContentValues);
            if (isInsert != -1) {
                mStringGroupLocalId = String.valueOf(isInsert);
                isGroupAdded = false;
                isShowAddMessage = true;
                // GroupDevice
                ContentValues mContentValuesGD;
                for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
                    if (mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup()) {
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceLocalId, mArrayListCheckedDevice.get(j).getDevicLocalId());
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceServerId, mArrayListCheckedDevice.get(j).getDeviceServerId());
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceCommId, mArrayListCheckedDevice.get(j).getDevice_Comm_id());
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceCommHexId, mArrayListCheckedDevice.get(j).getDevice_Comm_hexId());
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceName, mArrayListCheckedDevice.get(j).getDevice_name());
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceBleAddress, mArrayListCheckedDevice.get(j).getDevice_BleAddress());
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceType, mArrayListCheckedDevice.get(j).getDevice_Type());
//                        mContentValues.put(mActivity.mDbHelper.mFieldGroupDeviceTypeName, mArrayListCheckedDevice.get(j).getDevice_type_name());

                        if (mArrayListCheckedDevice.get(j).getDeviceServerId() != null && !mArrayListCheckedDevice.get(j).getDeviceServerId().equalsIgnoreCase("") && !mArrayListCheckedDevice.get(j).getDeviceServerId().equalsIgnoreCase("null")) {
                            mStringSelectedDeviceId = mStringSelectedDeviceId + mArrayListCheckedDevice.get(j).getDeviceServerId() + ", ";
                        }
                        mContentValuesGD = new ContentValues();
                        mContentValuesGD.put(DBHelper.mFieldGDListUserID, mActivity.mPreferenceHelper.getUserId());
                        mContentValuesGD.put(DBHelper.mFieldGDListLocalDeviceID, mArrayListCheckedDevice.get(j).getDevicLocalId());
                        mContentValuesGD.put(DBHelper.mFieldGDListServerDeviceID, mArrayListCheckedDevice.get(j).getDeviceServerId());
                        mContentValuesGD.put(DBHelper.mFieldGDListLocalGroupID, mStringGroupLocalId);
                        mContentValuesGD.put(DBHelper.mFieldGDListServerGroupID, "");
                        mContentValuesGD.put(DBHelper.mFieldGDListStatus, "1");
                        mContentValuesGD.put(DBHelper.mFieldGDListCreatedDate, mDateFormatDb.format(currentLocalTime));
                        mActivity.mDbHelper.insertRecord(DBHelper.mTableGroupDeviceList, mContentValuesGD);
                    }
                }
            } else {
                System.out.println("Failed Adding In Local DB");
            }
            mLeDevicesTemp = new ArrayList<>();
            if (isShowAddMessage) {

                if (mActivity.mPreferenceHelper.getIsSkipUser()) {
                    showGroupAddAlert();
                } else {
                    if (!mActivity.mUtility.haveInternet()) {
                        showGroupAddAlert();
                    } else {
//                        mActivity.new syncGroupDataAsyncTask(false).execute("");
                        if (mStringSelectedDeviceId != null && !mStringSelectedDeviceId.equalsIgnoreCase("") &&
                                !mStringSelectedDeviceId.equalsIgnoreCase(null)) {
                            StringBuilder sb = new StringBuilder(mStringSelectedDeviceId);
                            sb.deleteCharAt(sb.length() - 1);
                            sb.deleteCharAt(sb.length() - 1);
                            mStringSelectedDeviceId = sb.toString();
                            addGroupAPI(mStringGroupName, mStringSelectedDeviceId, mStringGroupLocalId);
                        } else {
                            showGroupAddAlert();
                        }
                    }
                }
            } else {
                showGroupRetryAlert();
            }
        } else {
            showGroupRetryAlert();
        }
    }

    /*Call api and Add group data to server*/
    public void addGroupAPI(String groupName, String deviceId, final String groupLocalId) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mUtility.ShowProgress("Please Wait..");
        Map<String, String> params = new HashMap<String, String>();
        params.put("device_token", mActivity.mPreferenceHelper.getDeviceToken());
        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
        params.put("group_name", groupName);
        params.put("local_group_id", mIntRandomGroupId + "");
        params.put("local_group_hex_id", mStringGroupHexId.toLowerCase());
        params.put("devices", deviceId);
        params.put("status", "1");// 1-Active,2-DeActive
        params.put("is_favourite", "2");// 1-YES,2-NO
        params.put("is_update", "0");// 0-Insert,1-Update

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
                                mContentValues.put(DBHelper.mFieldGroupServerID, mVoAddGroupData.getData().get(i).getDevice_group_id());
                                mContentValues.put(DBHelper.mFieldGroupIsSync, "1");
                                mArray = new String[]{groupLocalId};
                                mActivity.mDbHelper.updateRecord(DBHelper.mTableGroup, mContentValues,
                                        DBHelper.mFieldGroupLocalID + "=?", mArray);
                                if (mVoAddGroupData.getData().get(i).getDevices() != null && mVoAddGroupData.getData().get(i).getDevices().size() > 0) {
                                    mContentValuesGD = new ContentValues();
                                    mContentValuesGD.put(DBHelper.mFieldGDListServerGroupID,
                                            mVoAddGroupData.getData().get(i).getDevice_group_id());
                                    mArrayGroupLocalId = new String[]{groupLocalId};
                                    mActivity.mDbHelper.updateRecord(DBHelper.mTableGroupDeviceList, mContentValuesGD,
                                            DBHelper.mFieldGDListLocalGroupID + "=?", mArrayGroupLocalId);

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
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_add_group_no_device_added), 1, true, new onAlertDialogCallBack() {
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
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_add_group_added_success), 0, false, new onAlertDialogCallBack() {
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
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
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
                            if (mArrayListAddDevice.get(position).getIsGroupChecked()) {
                                mArrayListAddDevice.get(position).setIsGroupChecked(false);
                                holder.mAppCompatCheckBox.setChecked(false);
                            } else {
                                mArrayListAddDevice.get(position).setIsGroupChecked(true);
                                holder.mAppCompatCheckBox.setChecked(true);
                            }
//                            mDeviceListAdapter.notifyDataSetChanged();
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
