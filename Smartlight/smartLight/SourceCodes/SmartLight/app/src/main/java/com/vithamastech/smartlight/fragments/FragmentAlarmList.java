package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evergreen.ble.advertisement.ManufactureData;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.libRG.CustomTextView;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoAlarm;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoDeviceList;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 02-04-2018.
 */

public class FragmentAlarmList extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    VoDeviceList mVoDeviceList;
    AlarmListAdapter mAlarmListAdapter;
    @BindView(R.id.fragment_alarm_list_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_alarm_list_textview_no_alarm)
    TextView mTextViewNoAlarmSet;
    @BindView(R.id.activity_alarm_button_bottom_add)
    FloatingActionButton mFloatingActionButtonAdd;

    int mIntMaxAlarmLimitCount = 1;
    private SimpleDateFormat mTimeFormatter;
    private SimpleDateFormat mCurrentTimeFormatter24Hour;

    ArrayList<VoAlarm> mArrayListAlarm;
    ArrayList<VoDeviceList> mArrayListCheckedDevice = new ArrayList<>();
    ArrayList<VoBluetoothDevices> mLeDevicesTemp = new ArrayList<>();
    int currentLoopPosition = 0;
    boolean isGroupDeleted = false;
    startDeviceScanTimer mStartDeviceScanTimer;
    String mStringAlarmLocalId = "";
    int mStringAlarmCountNo = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mTimeFormatter = new SimpleDateFormat("hh:mm a");
        mCurrentTimeFormatter24Hour = new SimpleDateFormat("HH:mm");
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            mActivity.sendCurrentTimeDevice();
        } else {
            mActivity.connectDeviceWithProgress();
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_alarm_list, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.activity_main_menu_alarm);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
//        mActivity.mImageViewConnectionStatus.setVisibility(View.VISIBLE);
        mActivity.showBackButton(false);

        mActivity.isAddDeviceScan = true;

        mStartDeviceScanTimer = new startDeviceScanTimer(8000, 1000);
        isGroupDeleted = false;

        getDBAlarmList();
        mAlarmListAdapter = new AlarmListAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAlarmListAdapter);

        /*Device Connection scan callback*/
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.ALARM_REMOVE_RSP)) {
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

    @OnClick(R.id.activity_alarm_button_bottom_add)
    public void onFloatingAddButtonClick(View mView) {
        if (isAdded()) {
            String queryMaxAlarm = "select count(*) from " + mActivity.mDbHelper.mTableAlarm + " where " + mActivity.mDbHelper.mFieldAlarmIsActive + "= '1'" + " AND " + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
            String maxAlarmCount = mActivity.mDbHelper.getQueryResult(queryMaxAlarm);
            try {
                int mAlarmLimit = Integer.parseInt(maxAlarmCount);
                if (mAlarmLimit < 6) {
                    FragmentAddAlarm mFragmentAddAlarm = new FragmentAddAlarm();
                    mActivity.replacesFragment(mFragmentAddAlarm, true, null, 1);
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.frg_alarm_max_routine), 1, true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /*get Database alarm list*/
    private void getDBAlarmList() {
        DataHolder mDataHolder;
        mArrayListAlarm = new ArrayList<>();
        isGroupDeleted = false;
        try {
            //String url = "select * from " + mActivity.mDbHelper.mTableAlarm + " where " + mActivity.mDbHelper.mFieldAlarmIsActive + "= '1'" + " AND " + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
            String url = "select * from " + mActivity.mDbHelper.mTableAlarm + " join " + mActivity.mDbHelper.mTableAlarmDeviceList + " on " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= " + mActivity.mDbHelper.mFieldAlarmLocalID + " AND " + mActivity.mDbHelper.mFieldADUserId + "= " + mActivity.mDbHelper.mFieldAlarmUserId + " AND " + mActivity.mDbHelper.mFieldADDeviceStatus + "= '1'" + " where " + mActivity.mDbHelper.mFieldAlarmIsActive + "= '1'" + " AND " + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " group by " + mActivity.mDbHelper.mFieldAlarmLocalID;
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                mIntMaxAlarmLimitCount = mDataHolder.get_Listholder().size();
                VoAlarm mVoAlarm;
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoAlarm = new VoAlarm();
                    mVoAlarm.setAlarm_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmLocalID));
                    mVoAlarm.setAlarm_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmServerID));
                    mVoAlarm.setAlarm_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmName));
                    mVoAlarm.setAlarm_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmTime));
                    mVoAlarm.setAlarm_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmStatus));
                    mVoAlarm.setAlarm_days(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmDays));
                    mVoAlarm.setAlarm_color(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmColor));
                    mVoAlarm.setAlarm_count_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmCountNo));
                    mVoAlarm.setAlarm_light_on(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmLightOn));
                    mVoAlarm.setAlarm_wake_up_sleep(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmWakeUpSleep));
                    mVoAlarm.setAlarm_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmTimeStamp));
                    mVoAlarm.setAlarm_is_active(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmIsActive));
                    mVoAlarm.setCreated_at(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmCreatedAt));
                    mVoAlarm.setUpdated_at(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmUpdatedAt));
                    mVoAlarm.setAlarm_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAlarmIsSync));
                    if (mVoAlarm.getAlarm_status() != null && mVoAlarm.getAlarm_status().equalsIgnoreCase("1")) {
                        mVoAlarm.setIsChecked(true);
                    } else {
                        mVoAlarm.setIsChecked(false);
                    }
                    mArrayListAlarm.add(mVoAlarm);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        mAlarmListAdapter = new AlarmListAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAlarmListAdapter);
        mAlarmListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
    }

    private void checkAdapterIsEmpty() {
        if (mAlarmListAdapter.getItemCount() == 0) {
            mTextViewNoAlarmSet.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
        } else {
            mTextViewNoAlarmSet.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    /*Delete alarm list from device*/
    private void deleteAlarm() {
        DataHolder mDataHolder;
        mArrayListCheckedDevice = new ArrayList<>();
        currentLoopPosition = 0;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableAlarmDeviceList + " inner join " + mActivity.mDbHelper.mTableAlarm + " on " + mActivity.mDbHelper.mFieldAlarmLocalID + "=" + mActivity.mDbHelper.mFieldADAlarmLocalID + " AND " + mActivity.mDbHelper.mFieldADDeviceStatus + "= 1" + " inner join " + mActivity.mDbHelper.mTableDevice + " on " + mActivity.mDbHelper.mFieldDeviceLocalId + "=" + mActivity.mDbHelper.mFieldADDeviceLocalID + " where " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= '" + mStringAlarmLocalId + "'";
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
            }
        } catch (Exception e) {
            mActivity.hideProgress();
            e.printStackTrace();
        }
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            if (mArrayListCheckedDevice.size() > 0) {
                mActivity.showProgress("Deleting Routine...", true);
                deleteDeviceAlarmRequest();
            } else {
                mActivity.hideProgress();
                showGroupRetryAlert();
            }
        } else {
            mActivity.hideProgress();
            mActivity.connectDeviceWithProgress();
        }

    }

    /* Send ble device delete request to device*/
    private void deleteDeviceAlarmRequest() {
        if (currentLoopPosition >= mArrayListCheckedDevice.size()) {
            mActivity.hideProgress();
            return;
        }
        if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
            try {
                isGroupDeleted = true;
                mActivity.deleteAlarmForDay(BLEUtility.intToByte(100), Short.parseShort(mArrayListCheckedDevice.get(currentLoopPosition).getDevice_Comm_id()), BLEUtility.intToByte(mStringAlarmCountNo), false);

                Timer innerTimer = new Timer();
                innerTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if (currentLoopPosition == mArrayListCheckedDevice.size() - 1) {
                            if (mStartDeviceScanTimer != null)
                                mStartDeviceScanTimer.start();

                        } else {
                            currentLoopPosition++;
                            deleteDeviceAlarmRequest();
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

    private void showGroupRetryAlert() {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_routine_not_deleted), 1, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void showGroupDeleteAlert() {
        mActivity.isAddDeviceScan = false;
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_routine_delete_success), 0, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    /*Timer for scan acknowledgement response*/
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

    /*Check Device is delete from bulb or not*/
    private void getDeviceListData() {
        boolean isAnyDeviceDeletedInGroup = false;
        String mStringDeviceHexId;
        String mStringDeviceHexData;
        for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
            for (int i = 0; i < mLeDevicesTemp.size(); i++) {
                if (isGroupDeleted) {
                    if (!mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup()) {
                        mStringDeviceHexId = mArrayListCheckedDevice.get(j).getDevice_Comm_hexId();
                        if (mStringDeviceHexId != null && !mStringDeviceHexId.equalsIgnoreCase("")) {
                            mStringDeviceHexData = mLeDevicesTemp.get(i).getDeviceHexData();
                            if (mStringDeviceHexData.toLowerCase().contains(mStringDeviceHexId.toLowerCase())) {
                                if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ALARM_REMOVE_RSP)) {
                                    isAnyDeviceDeletedInGroup = true;
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
        if (isAnyDeviceDeletedInGroup) {
            isGroupDeleted = false;
            for (int j = 0; j < mArrayListCheckedDevice.size(); j++) {
                if (mArrayListCheckedDevice.get(j).getIsDeviceSyncWithGroup()) {
                    mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableAlarmDeviceList + " where " + mActivity.mDbHelper.mFieldADDeviceLocalID + "= '" + mArrayListCheckedDevice.get(j).getDevicLocalId() + "'" + " AND " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= '" + mStringAlarmLocalId + "'");
                }
            }
            String queryAlarmDelete = "select count(*) from " + mActivity.mDbHelper.mTableAlarmDeviceList + " where " + mActivity.mDbHelper.mFieldADAlarmLocalID + "= '" + mStringAlarmLocalId + "'" + " AND " + mActivity.mDbHelper.mFieldADUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
            String AlarmDeleteCount = mActivity.mDbHelper.getQueryResult(queryAlarmDelete);
            if (Integer.parseInt(AlarmDeleteCount) == 0) {
                ContentValues mContentValuesAD = new ContentValues();
                mContentValuesAD.put(mActivity.mDbHelper.mFieldAlarmIsActive, "0");
                mContentValuesAD.put(mActivity.mDbHelper.mFieldAlarmIsSync, "0");
                String[] mArrayAD = new String[]{mStringAlarmLocalId};
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAlarm, mContentValuesAD, mActivity.mDbHelper.mFieldAlarmLocalID + " =?", mArrayAD);
            }

            getDBAlarmList();
            mArrayListCheckedDevice = new ArrayList<>();
            showGroupDeleteAlert();
        } else {
            mActivity.isAddDeviceScan = true;
            mActivity.RescanDevice(false);
            showGroupRetryAlert();
        }
    }

    String mStringSelectedDay = "";
    StringBuilder sb;
    String[] AlarmSplited;


    /*Alarm List adapter*/
    public class AlarmListAdapter extends RecyclerView.Adapter<AlarmListAdapter.ViewHolder> {

        @Override
        public AlarmListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_alarm_list_item, parent, false);
            return new AlarmListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final AlarmListAdapter.ViewHolder holder, final int position) {
            if (mArrayListAlarm.get(position).getAlarm_days() != null && !mArrayListAlarm.get(position).getAlarm_days().equalsIgnoreCase("") && !mArrayListAlarm.get(position).getAlarm_days().equalsIgnoreCase("null")) {
                mStringSelectedDay = "";
                if (mArrayListAlarm.get(position).getAlarm_days().contains("0")) {
                    mStringSelectedDay = mStringSelectedDay + "Sun, ";
                }
                if (mArrayListAlarm.get(position).getAlarm_days().contains("1")) {
                    mStringSelectedDay = mStringSelectedDay + "Mon, ";
                }
                if (mArrayListAlarm.get(position).getAlarm_days().contains("2")) {
                    mStringSelectedDay = mStringSelectedDay + "Tue, ";
                }
                if (mArrayListAlarm.get(position).getAlarm_days().contains("3")) {
                    mStringSelectedDay = mStringSelectedDay + "Wed, ";
                }
                if (mArrayListAlarm.get(position).getAlarm_days().contains("4")) {
                    mStringSelectedDay = mStringSelectedDay + "Thu, ";
                }
                if (mArrayListAlarm.get(position).getAlarm_days().contains("5")) {
                    mStringSelectedDay = mStringSelectedDay + "Fri, ";
                }
                if (mArrayListAlarm.get(position).getAlarm_days().contains("6")) {
                    mStringSelectedDay = mStringSelectedDay + "Sat, ";
                }
                sb = new StringBuilder(mStringSelectedDay);
                sb.deleteCharAt(sb.length() - 1);
                sb.deleteCharAt(sb.length() - 1);
                mStringSelectedDay = sb.toString();
                holder.mTextViewAlarmDays.setText(mStringSelectedDay);
            } else {
                holder.mTextViewAlarmDays.setText("");
            }
            if (mArrayListAlarm.get(position).getAlarm_time() != null && !mArrayListAlarm.get(position).getAlarm_time().equalsIgnoreCase("")) {
                if (mArrayListAlarm.get(position).getAlarm_time().contains(" ")) {
                    AlarmSplited = mArrayListAlarm.get(position).getAlarm_time().split("\\s");
                    holder.mTextViewTime.setText(AlarmSplited[0]);
                    holder.mTextViewAmPm.setText(AlarmSplited[1]);
                } else {
                    holder.mTextViewTime.setText("");
                    holder.mTextViewAmPm.setText("");
                }
            } else {
                holder.mTextViewTime.setText("");
                holder.mTextViewAmPm.setText("");
            }
            if (mArrayListAlarm.get(position).getIsChecked()) {
                holder.mSwitchAlarm.setChecked(true);
            } else {
                holder.mSwitchAlarm.setChecked(false);
            }
            if (mArrayListAlarm.get(position).getAlarm_light_on() != null && !mArrayListAlarm.get(position).getAlarm_light_on().equalsIgnoreCase("") && !mArrayListAlarm.get(position).getAlarm_light_on().equals("null")) {
                if (mArrayListAlarm.get(position).getAlarm_light_on().equals("1")) {
                    holder.mTextViewAlarmStatus.setText("ON");
                    if (mArrayListAlarm.get(position).getAlarm_color() != null && !mArrayListAlarm.get(position).getAlarm_color().equals("") && !mArrayListAlarm.get(position).getAlarm_color().equals("null")) {
                        try {
                            int mAlarmColor = Integer.parseInt(mArrayListAlarm.get(position).getAlarm_color());
                            if (mAlarmColor != 0) {
                                holder.mTextViewSelectedColor.setVisibility(View.VISIBLE);
                                holder.mTextViewSelectedColor.setBackgroundColor(Integer.parseInt(mArrayListAlarm.get(position).getAlarm_color()));
                            } else {
                                holder.mTextViewSelectedColor.setVisibility(View.GONE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            holder.mTextViewSelectedColor.setVisibility(View.GONE);
                        }
                    }
                } else {
                    holder.mTextViewAlarmStatus.setText("OFF");
                    holder.mTextViewSelectedColor.setVisibility(View.GONE);
                }
            } else {
                holder.mTextViewAlarmStatus.setText("OFF");
                holder.mTextViewSelectedColor.setVisibility(View.GONE);
            }

            holder.mImageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListAlarm.get(position) != null) {
                        if (position < mArrayListAlarm.size()) {
                            if (mArrayListAlarm.get(position).getAlarm_time() != null && !mArrayListAlarm.get(position).getAlarm_time().equalsIgnoreCase("")) {
                                mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.frg_alarm_delete_alert), getResources().getString(R.string.frg_routine_delete_confirmation), "Yes", "No", true, 2, new onAlertDialogCallBack() {
                                    @Override
                                    public void PositiveMethod(DialogInterface dialog, int id) {
                                        mActivity.isAddDeviceScan = true;
                                        mActivity.mLeDevicesTemp = new ArrayList<>();
                                        mLeDevicesTemp = new ArrayList<>();
                                        mActivity.RescanDevice(false);
                                        if (mArrayListAlarm.get(position) != null) {
                                            if (position < mArrayListAlarm.size()) {
                                                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
//                                                    mActivity.deleteAlarmForDay(BLEUtility.intToByte(100), Short.parseShort(mArrayListAlarm.get(position).getDevice_Comm_id()), BLEUtility.intToByte(position), true);
                                                    try {
                                                        mStringAlarmLocalId = mArrayListAlarm.get(position).getAlarm_local_id();
                                                        mStringAlarmCountNo = Integer.parseInt(mArrayListAlarm.get(position).getAlarm_count_no());
                                                        if (mActivity.getIsDeviceSupportedAdvertisment()) {
                                                            if (!mActivity.isPingRequestSent) {
                                                                mActivity.sendPingRequestToDevice();
                                                            } else {
                                                                deleteAlarm();
                                                            }
                                                        } else {
                                                            deleteAlarm();
                                                        }
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
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

                            }
                        }
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListAlarm != null) {
                        if (position < mArrayListAlarm.size()) {
                            FragmentEditAlarm mFragmentEditAlarm = new FragmentEditAlarm();
                            Bundle mBundle = new Bundle();
                            mBundle.putSerializable("intent_vo_alarm", mArrayListAlarm.get(position));
                            mActivity.replacesFragment(mFragmentEditAlarm, true, mBundle, 0);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListAlarm.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_alarm_list_item_textview_time)
            TextView mTextViewTime;
            @BindView(R.id.raw_alarm_list_item_textview_time_ampm)
            TextView mTextViewAmPm;
            @BindView(R.id.raw_alarm_list_item_textview_name)
            TextView mTextViewAlarmDays;
            @BindView(R.id.raw_alarm_list_item_textview_on_off_status)
            TextView mTextViewAlarmStatus;
            @BindView(R.id.raw_alarm_list_item_switch_alarm)
            SwitchCompat mSwitchAlarm;
            @BindView(R.id.raw_alarm_list_item_imageview_delete)
            ImageView mImageViewDelete;
            @BindView(R.id.raw_alarm_list_item_imageview_selected_color)
            CustomTextView mTextViewSelectedColor;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.showBackButton(false);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
//        mActivity.mImageViewConnectionStatus.setVisibility(View.GONE);
        mActivity.isAddDeviceScan = false;
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.cancel();
    }


    /**
     * test workibg   vunsfdhfhfhgfhh 
     */
}
