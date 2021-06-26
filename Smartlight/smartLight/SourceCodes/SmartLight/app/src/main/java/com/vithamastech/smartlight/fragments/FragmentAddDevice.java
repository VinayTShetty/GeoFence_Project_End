package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.Nullable;

import com.evergreen.ble.advertisement.ManufactureData;
import com.google.android.material.snackbar.Snackbar;

import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.gson.Gson;
import com.vithamastech.smartlight.DialogFragementHelper.EditTextDialog;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoAddDeviceData;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.console;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentAddDevice extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_add_device_recyclerview)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_add_device_textview_nodevice)
    TextView mTextViewNoDeviceFound;
    @BindView(R.id.fragment_add_device_relativelayout_nodevice)
    RelativeLayout mRelativeLayoutNoDevice;
    @BindView(R.id.fragment_add_device_progress)
    ProgressBar mProgressBar;
    ArrayList<VoBluetoothDevices> mArrayListAddDevice = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;
    boolean isAssociateDevice = false;
    String mStringDeviceBLEAddress = "";
    String mStringDeviceFullAddress = "";
    startDeviceScanTimer mStartDeviceScanTimer;
    sendRequestTimer mSendRequestTimer;
    int mIntRandomDeviceId = 0;
    String mStringRandomDeviceIdHex = "";
    SimpleDateFormat mDateFormatDb;

    boolean isAnyDeviceAdded = false;
    int imagePath;
    String mStrListDeviceType;
    scanTimeout mScanTimeOut;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_add_device, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_add_device_header);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_refresh_icon_white);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mActivity.mLeDevicesTemp = new ArrayList<>();
        mStartDeviceScanTimer = new startDeviceScanTimer(15000, 1000);

        isAssociateDevice = false;
        mStringDeviceBLEAddress = "";
        mStringDeviceFullAddress = "";
        mArrayListAddDevice = new ArrayList<>();
        mDeviceListAdapter = null;
        getDeviceListData();
        mScanTimeOut = new scanTimeout(40000, 1000);
        if (mScanTimeOut != null) {
            mScanTimeOut.start();
        }
        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });

        /*Device connections, scan call back*/
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {
            }

            @Override
            public void addScanDevices(final VoBluetoothDevices mVoBluetoothDevices) {
                try {
                    if (isAdded() && mActivity != null) {
                        mActivity.runOnUiThread(() -> {
                            if (!isAssociateDevice) {
                                if (mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.ASSOC_NON_CONNECTABLE_RSP) ||
                                        mVoBluetoothDevices.getDeviceHexData().substring(32, 36).toLowerCase().equals(URLCLASS.ASSOC_ALREADY_ASS_RSP)) {
                                    boolean containsInScanDevice = false;
                                    for (VoBluetoothDevices device : mArrayListAddDevice) {
                                        if (mVoBluetoothDevices.getDeviceIEEE().equals(device.getDeviceIEEE())) {
                                            containsInScanDevice = true;
                                            break;
                                        }
                                    }
                                    if (!containsInScanDevice) {
                                        mArrayListAddDevice.add(mVoBluetoothDevices);
                                        Collections.sort(mArrayListAddDevice, new Comparator<VoBluetoothDevices>() {
                                            @Override
                                            public int compare(VoBluetoothDevices s1, VoBluetoothDevices s2) {
                                                return s2.getDeviceScanOpcode().compareToIgnoreCase(s1.getDeviceScanOpcode());
                                            }
                                        });
                                        if (mDeviceListAdapter != null) {
                                            mDeviceListAdapter.notifyDataSetChanged();
                                        }
                                    }
                                }
                            } else {
                                String mStringDeviceHexData = mVoBluetoothDevices.getDeviceHexData();
                                if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ASSOC_COMPLETE_RSP) ||
                                        mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ASSOC_ALREADY_ASS_RSP)) {
                                    if (mStringRandomDeviceIdHex != null && mStringRandomDeviceIdHex.length() >= 2) {
                                        if (mStringRandomDeviceIdHex.length() == 3) {
                                            mStringRandomDeviceIdHex = "0" + mStringRandomDeviceIdHex;
                                        }
                                        mStringRandomDeviceIdHex = mStringRandomDeviceIdHex.substring(2) + mStringRandomDeviceIdHex.substring(0, 2);
                                        if (mStringRandomDeviceIdHex != null && !mStringRandomDeviceIdHex.equalsIgnoreCase("")) {
                                            if (mStringDeviceHexData.toLowerCase().contains(mStringRandomDeviceIdHex.toLowerCase())) {
                                                if (mStringDeviceFullAddress.length() >= 52) {
                                                    isAnyDeviceAdded = true;
                                                    isAssociateDevice = false;
                                                    if (mStartDeviceScanTimer != null) {
                                                        mStartDeviceScanTimer.cancel();
                                                        mStartDeviceScanTimer.onFinish();
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
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

        //Refresh List
        mActivity.mImageViewAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isAdded()) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    if (mScanTimeOut != null) {
                        mScanTimeOut.start();
                    }
                    isAssociateDevice = false;
                    mActivity.startDeviceScan();
                    mActivity.RescanDevice(true);

                    mArrayListAddDevice = new ArrayList<>();
                    if (mDeviceListAdapter != null) {
                        mDeviceListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
        return mViewRoot;
    }

    /*Scan Device Data*/
    private class startDeviceScanTimer extends CountDownTimer {

        public startDeviceScanTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onTick(long millisUntilFinished) {

        }

        @Override
        public void onFinish() {
            checkScanDeviceList();
        }
    }

    /*Send Association request to device two times*/
    private class sendRequestTimer extends CountDownTimer {
        int mIntDeviceId;
        String mStringBleAdd;

        public sendRequestTimer(long millisInFuture, long countDownInterval, int deviceId, String bleAddress) {
            super(millisInFuture, countDownInterval);
            mIntDeviceId = deviceId;
            mStringBleAdd = bleAddress;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            System.out.println("millisUntilFinished=" + millisUntilFinished);
            if ((millisUntilFinished / 1000) % 2 == 0) {
                System.out.println("Part=2");
                mActivity.setAssRequestKeyPart(BLEUtility.intToByte(100), Short.parseShort(mIntDeviceId + ""), BLEUtility.hexStringToBytes(mStringBleAdd), false);
            } else {
                System.out.println("Part=1");
                mActivity.setAssRequestKeyPart(BLEUtility.intToByte(100), Short.parseShort(0 + ""), BLEUtility.hexStringToBytes(mStringBleAdd), true);
            }
        }

        @Override
        public void onFinish() {

        }
    }

    /* Initialize device adapter*/
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
            checkAdapterIsEmpty();
        } else {
            mDeviceListAdapter.notifyDataSetChanged();
        }
        checkAdapterIsEmpty();
    }

    /*Check device list adapter*/
    private void checkScanDeviceList() {

        if (isAnyDeviceAdded) {
            if (mStringDeviceFullAddress.length() >= 52) {
                saveDeviceData(false);
            } else {
                mActivity.mUtility.HideProgress();
                checkAdapterIsEmpty();
                showDeviceRetryAlert();
            }
        } else {
            mActivity.mUtility.HideProgress();
            checkAdapterIsEmpty();
            showDeviceRetryAlert();
        }
    }

    /*Save device data in local database*/
    private void saveDeviceData(boolean isAlreadyAssociate) {
        final String mStrDeviceType = mStringDeviceFullAddress.substring(48, 52);
        Calendar cal = Calendar.getInstance();
        Date currentLocalTime = cal.getTime();

        ContentValues mContentValues = new ContentValues();
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceServerId, "");
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceUserId, mActivity.mPreferenceHelper.getUserId());
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceCommID, mIntRandomDeviceId + "");
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceCommHexId, mStringRandomDeviceIdHex);
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceRealName, "Vithamas Light");
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceBleAddress, mStringDeviceBLEAddress.toUpperCase());
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceType, mStrDeviceType);
        String queryDeviceType = "select " + mActivity.mDbHelper.mFieldDeviceTypeTypeName + " from " + mActivity.mDbHelper.mTableDeviceType + " where " + mActivity.mDbHelper.mFieldDeviceTypeType + "= '" + mStrDeviceType + "'";
        String deviceTypeName = mActivity.mDbHelper.getQueryResult(queryDeviceType);
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceTypeName, deviceTypeName);
        mContentValues.put(mActivity.mDbHelper.mFieldConnectStatus, "YES");
        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, "ON");
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceIsFavourite, "0");
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceLastState, "0");
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceTimeStamp, cal.getTimeInMillis());
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceIsActive, "1");
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceCreatedAt, mDateFormatDb.format(currentLocalTime));
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceUpdatedAt, mDateFormatDb.format(currentLocalTime));
        mContentValues.put(mActivity.mDbHelper.mFieldDeviceIsSync, "0");
        String isExistInDB = CheckRecordExistInDeviceDB(mStringDeviceBLEAddress);
        mActivity.mUtility.HideProgress();
        if (isExistInDB.equalsIgnoreCase("-1")) {
            openDeviceNameDialog(mStrDeviceType, mContentValues, true);
        } else {
            if (isAlreadyAssociate) {
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", new String[]{isExistInDB});
                String queryDeviceName = "select " + mActivity.mDbHelper.mFieldDeviceName + " from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceLocalId + "= '" + isExistInDB + "'";
                String deviceName = mActivity.mDbHelper.getQueryResult(queryDeviceName);
                mContentValues.put(mActivity.mDbHelper.mFieldDeviceLocalId, isExistInDB);
                mContentValues.put(mActivity.mDbHelper.mFieldDeviceName, deviceName);
                if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
                    addDeviceAPI(mContentValues);
                } else {
                    showDeviceAddAlert();
                }
            } else {
                mContentValues.put(mActivity.mDbHelper.mFieldDeviceLocalId, isExistInDB);
                openDeviceNameDialog(mStrDeviceType, mContentValues, false);
            }
        }
    }

    /*Show device fail to add alert dialog*/
    private void showDeviceRetryAlert() {
        isAssociateDevice = false;
        mArrayListAddDevice = new ArrayList<>();
        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.notifyDataSetChanged();
        }
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_no_any_device_added), 1, true, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {

            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    /*Show Device added success dialog*/
    private void showDeviceAddAlert() {
        isAssociateDevice = false;
        mArrayListAddDevice = new ArrayList<>();
        if (mDeviceListAdapter != null) {
            mDeviceListAdapter.notifyDataSetChanged();
        }
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_device_added_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.onBackPressed();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    /*Check device record exist or not*/
    public String CheckRecordExistInDeviceDB(String bleAddress) {
        if (bleAddress != null && !bleAddress.equalsIgnoreCase("") && !bleAddress.equalsIgnoreCase("null")) {
            bleAddress = bleAddress.toUpperCase();
        }

        DataHolder mDataHolder;
        String url = "select * from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceBleAddress + "= '" + bleAddress + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldDeviceLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Call api and save device data to server*/
    public void addDeviceAPI(final ContentValues mContentValuesParam) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mUtility.ShowProgress("Please Wait..");
        mRelativeLayoutNoDevice.setVisibility(View.GONE);
        Map<String, String> params = new HashMap<String, String>();
        String mStringDeviceHex = mContentValuesParam.get(mActivity.mDbHelper.mFieldDeviceCommHexId).toString();
        params.put("device_token", mActivity.mPreferenceHelper.getDeviceToken());
        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
        params.put("device_id", mContentValuesParam.get(mActivity.mDbHelper.mFieldDeviceCommID).toString());
        params.put("hex_device_id", mStringDeviceHex.toLowerCase());
        params.put("device_name", mContentValuesParam.get(mActivity.mDbHelper.mFieldDeviceName).toString());
        String queryDeviceTypeId = "select " + mActivity.mDbHelper.mFieldDeviceTypeServerID + " from " + mActivity.mDbHelper.mTableDeviceType + " where " + mActivity.mDbHelper.mFieldDeviceTypeType + "= '" + mContentValuesParam.get(mActivity.mDbHelper.mFieldDeviceTypeType).toString() + "'";
        String deviceTypeServerID = mActivity.mDbHelper.getQueryResult(queryDeviceTypeId);
        if (deviceTypeServerID != null && !deviceTypeServerID.equalsIgnoreCase("")) {
            params.put("device_type", deviceTypeServerID);
        } else {
            params.put("device_type", "0000");
        }
        params.put("ble_address", mContentValuesParam.get(mActivity.mDbHelper.mFieldDeviceBleAddress).toString().toUpperCase());
        params.put("status", "1");// 1-Active,2-DeActive
        params.put("is_favourite", "2");// 1-YES,2-NO
        params.put("is_update", "0");// 0-Insert,1-Update
        params.put("remember_last_color", "0");
        Call<VoAddDeviceData> mLogin = mActivity.mApiService.addDeviceAPI(params);
        mLogin.enqueue(new Callback<VoAddDeviceData>() {
            @Override
            public void onResponse(Call<VoAddDeviceData> call, Response<VoAddDeviceData> response) {
                mActivity.mUtility.HideProgress();
                if (isAdded()) {
                    VoAddDeviceData mAddDeviceAPI = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mAddDeviceAPI);
                    if (mAddDeviceAPI != null && mAddDeviceAPI.getResponse().equalsIgnoreCase("true")) {
                        if (mAddDeviceAPI.getData() != null) {
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(mActivity.mDbHelper.mFieldDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                            mContentValues.put(mActivity.mDbHelper.mFieldDeviceIsSync, "1");
                            String[] mArray = new String[]{mContentValuesParam.get(mActivity.mDbHelper.mFieldDeviceLocalId).toString()};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArray);

                            ContentValues mContentValuesSocket = new ContentValues();
                            mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceServerId, mAddDeviceAPI.getData().getServer_device_id());
                            String[] mArraySocket = new String[]{mContentValuesParam.get(mActivity.mDbHelper.mFieldDeviceLocalId).toString()};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTablePowerStripSocket, mContentValuesSocket, mActivity.mDbHelper.mFieldSocketDeviceLocalId + "=?", mArraySocket);
                        }
                    } else {
                        if (mAddDeviceAPI != null && mAddDeviceAPI.getMessage() != null && !mAddDeviceAPI.getMessage().equalsIgnoreCase("")) {
//                        checkAdapterIsEmpty();
                        }
                    }
                    showDeviceAddAlert();
                }
            }

            @Override
            public void onFailure(Call<VoAddDeviceData> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                if (isAdded()) {
//                checkAdapterIsEmpty();
                    showDeviceAddAlert();
                }
                t.printStackTrace();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        if (mScanTimeOut != null) {
            mScanTimeOut.cancel();
        }
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.cancel();
        if (mSendRequestTimer != null)
            mSendRequestTimer.cancel();
        unbinder.unbind();
        mActivity.isAddDeviceScan = false;
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_add_device);
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.isAddDeviceScan = true;
        mActivity.RescanDevice(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        mActivity.isAddDeviceScan = false;
        if (mStartDeviceScanTimer != null)
            mStartDeviceScanTimer.cancel();
        if (mSendRequestTimer != null)
            mSendRequestTimer.cancel();
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

    /*Device list adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_add_device_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            holder.mAppCompatCheckBox.setVisibility(View.GONE);
            if (mArrayListAddDevice.get(position).getDeviceIEEE() != null && !mArrayListAddDevice.get(position).getDeviceIEEE().equalsIgnoreCase("")) {
                holder.mTextViewDeviceId.setText(mArrayListAddDevice.get(position).getDeviceIEEE().replace(":", ""));
            } else {
                holder.mTextViewDeviceId.setText("");
            }
            if (mArrayListAddDevice.get(position).getDeviceScanOpcode().equals(URLCLASS.ASSOC_NON_CONNECTABLE_RSP)) {
                holder.mTextViewDeviceId.setTextColor(getResources().getColor(R.color.colorGreen));
                if (mArrayListAddDevice.get(position).getDeviceName() != null && !mArrayListAddDevice.get(position).getDeviceName().equalsIgnoreCase("")) {
                    holder.mTextViewDeviceName.setText(Html.fromHtml(mArrayListAddDevice.get(position).getDeviceName() + "<font color='#FFFFFF'>" + " (New)" + "</font>"));
                }
            } else {
                holder.mTextViewDeviceId.setTextColor(getResources().getColor(R.color.colorWhiteLight));
                if (mArrayListAddDevice.get(position).getDeviceName() != null && !mArrayListAddDevice.get(position).getDeviceName().equalsIgnoreCase("")) {
                    holder.mTextViewDeviceName.setText(mArrayListAddDevice.get(position).getDeviceName());
                }
            }
            if (mArrayListAddDevice.get(position).getDeviceType() != null && !mArrayListAddDevice.get(position).getDeviceType().equalsIgnoreCase("")) {
                mStrListDeviceType = mArrayListAddDevice.get(position).getDeviceType();
                if (mStrListDeviceType.equalsIgnoreCase("0100")) {
                    imagePath = R.drawable.ic_default_pic;
                } else if (mStrListDeviceType.equalsIgnoreCase("0200")) {
                    imagePath = R.drawable.ic_default_pic;
                } else if (mStrListDeviceType.equalsIgnoreCase("0300")) {
                    imagePath = R.drawable.ic_default_switch_icon;
                } else if (mStrListDeviceType.equalsIgnoreCase("0400")) {
                    imagePath = R.drawable.ic_default_powerstrip_icon;
                } else if (mStrListDeviceType.equalsIgnoreCase("0500")) {
                    imagePath = R.drawable.ic_default_fan_icon;
                } else if (mStrListDeviceType.equalsIgnoreCase("0600")) {
                    imagePath = R.drawable.ic_default_striplight_icon;
                } else if (mStrListDeviceType.equalsIgnoreCase("0700")) {
                    imagePath = R.drawable.ic_default_lamp_icon;
                } else if (mStrListDeviceType.equalsIgnoreCase("0800")) {
                    imagePath = R.drawable.ic_default_socket_icon;
                } else {
                    imagePath = R.drawable.ic_default_pic;
                }
                Glide.with(mActivity)
                        .load(imagePath)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .skipMemoryCache(false)
                        .crossFade()
                        .placeholder(R.drawable.ic_default_pic)
                        .into(holder.mImageViewDevice);
                holder.mImageViewDevice.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorWhite));
            }
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mArrayListAddDevice != null) {
                        if (position < mArrayListAddDevice.size()) {
                            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                if (mArrayListAddDevice.get(position).getDeviceHexData() != null && !mArrayListAddDevice.get(position).getDeviceHexData().equalsIgnoreCase("")) {
                                    final String mStringDeviceHexData = mArrayListAddDevice.get(position).getDeviceHexData();
                                    if (mStringDeviceHexData.length() >= 52) {
                                        final String mStrBleAdd = mStringDeviceHexData.substring(36, 48);
                                        String mStringRandomDeviceIdHexTemp = mStringDeviceHexData.substring(20, 24);
                                        if (mStringRandomDeviceIdHexTemp.length() == 3) {
                                            mStringRandomDeviceIdHexTemp = "0" + mStringRandomDeviceIdHexTemp;
                                        }
                                        mStringRandomDeviceIdHexTemp = mStringRandomDeviceIdHexTemp.substring(2) + mStringRandomDeviceIdHexTemp.substring(0, 2);
                                        mIntRandomDeviceId = BLEUtility.hexToDecimal(mStringRandomDeviceIdHexTemp);
                                        mActivity.setCheckDevice(BLEUtility.intToByte(100), Short.parseShort(mIntRandomDeviceId + ""), BLEUtility.hexStringToBytes(mStrBleAdd), true);
                                    }
                                }

                            } else {
                                mActivity.connectDeviceWithProgress();
                            }
                        }
                    }
                    return true;
                }
            });
            holder.itemView.setOnClickListener(v -> {
                try {
                    if (mArrayListAddDevice != null) {
                        if (position < mArrayListAddDevice.size()) {
//                                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                            if (mArrayListAddDevice.get(position).getDeviceHexData() != null && !mArrayListAddDevice.get(position).getDeviceHexData().equalsIgnoreCase("")) {
                                final String mStringDeviceHexData = mArrayListAddDevice.get(position).getDeviceHexData();
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (mStringDeviceHexData.length() >= 52) {
                                            mRelativeLayoutNoDevice.setVisibility(View.GONE);
                                            if (mStringDeviceHexData.substring(32, 36).toLowerCase().equals(URLCLASS.ASSOC_ALREADY_ASS_RSP) && mStringDeviceHexData.substring(36, 48).toLowerCase().equals(mArrayListAddDevice.get(position).getDeviceIEEE().toLowerCase())) {
                                                mActivity.showProgress("Adding Device..", true);
                                                mStringDeviceBLEAddress = mArrayListAddDevice.get(position).getDeviceIEEE();
                                                mStringDeviceFullAddress = mArrayListAddDevice.get(position).getDeviceHexData();
                                                mStringRandomDeviceIdHex = mStringDeviceFullAddress.substring(20, 24);

                                                if (mStringRandomDeviceIdHex != null && mStringRandomDeviceIdHex.length() >= 2) {
                                                    String mStringRandomDeviceIdHexTemp = mStringRandomDeviceIdHex;
                                                    if (mStringRandomDeviceIdHexTemp.length() == 3) {
                                                        mStringRandomDeviceIdHexTemp = "0" + mStringRandomDeviceIdHexTemp;
                                                    }

                                                    mStringRandomDeviceIdHexTemp = mStringRandomDeviceIdHexTemp.substring(2) + mStringRandomDeviceIdHexTemp.substring(0, 2);
                                                    mIntRandomDeviceId = BLEUtility.hexToDecimal(mStringRandomDeviceIdHexTemp);

                                                    mActivity.hideProgress();
                                                    saveDeviceData(true);
                                                } else {
//                                                        if (mActivity.isDevicesConnected) {
//                                                                mActivity.mBluetoothLeService.setCharacteristicNotifications(mActivity.mBluetoothLeService.getContPartCharacteristic(), true);
                                                    mActivity.showProgress("Adding Device..", true);
                                                    isAnyDeviceAdded = false;
                                                    isAssociateDevice = true;
                                                    mActivity.isAddDeviceScan = true;
                                                    mActivity.RescanDevice(false);

                                                    final String mStrBleAdd = mStringDeviceHexData.substring(36, 48);

                                                    mIntRandomDeviceId = mActivity.generateRandomNo();
                                                    System.out.println("mIntRandomDeviceId=" + mIntRandomDeviceId);
                                                    mStringRandomDeviceIdHex = String.format("%04X", (0xFFFF & mIntRandomDeviceId));
                                                    System.out.println("mStringRandomDeviceIdHex=" + mStringRandomDeviceIdHex);
                                                    mStringDeviceBLEAddress = mStrBleAdd;
                                                    mStringDeviceFullAddress = mStringDeviceHexData;
                                                    mSendRequestTimer = new sendRequestTimer(4000, 1000, mIntRandomDeviceId, mStrBleAdd);
                                                    mSendRequestTimer.start();

                                                    if (mStartDeviceScanTimer != null)
                                                        mStartDeviceScanTimer.start();
//                                                        }
//                                                        else {
//                                                            mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_no_any_device_connect), 1, true, new onAlertDialogCallBack() {
//                                                                @Override
//                                                                public void PositiveMethod(DialogInterface dialog, int id) {
//                                                                    Fragment mFragment = mActivity.getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
//                                                                    if (!(mFragment instanceof FragmentSettingBridgeConnection)) {
//                                                                        if (!mActivity.isDevicesConnected) {
//                                                                            FragmentSettingBridgeConnection mFragmentSettingBridgeConnection = new FragmentSettingBridgeConnection();
//                                                                            mActivity.replacesFragment(mFragmentSettingBridgeConnection, true, null, 0);
//                                                                        }
//                                                                    }
//                                                                }
//
//                                                                @Override
//                                                                public void NegativeMethod(DialogInterface dialog, int id) {
//
//                                                                }
//                                                            });
//                                                        }
                                                }
                                            } else {
//                                                    if (mActivity.isDevicesConnected) {
//                                                            mActivity.mBluetoothLeService.setCharacteristicNotifications(mActivity.mBluetoothLeService.getContPartCharacteristic(), true);
                                                mActivity.showProgress("Adding Device..", true);
                                                isAnyDeviceAdded = false;
                                                isAssociateDevice = true;
                                                mActivity.isAddDeviceScan = true;
                                                mActivity.RescanDevice(false);
                                                final String mStrBleAdd = mStringDeviceHexData.substring(36, 48);

                                                mIntRandomDeviceId = mActivity.generateRandomNo();
                                                mStringRandomDeviceIdHex = String.format("%04X", (0xFFFF & mIntRandomDeviceId));
                                                System.out.println("mIntRandomDeviceId=" + mIntRandomDeviceId);
                                                System.out.println("mStringRandomDeviceIdHex=" + mStringRandomDeviceIdHex);
                                                mStringDeviceBLEAddress = mStrBleAdd;
                                                mStringDeviceFullAddress = mStringDeviceHexData;
                                                mSendRequestTimer = new sendRequestTimer(4000, 1000, mIntRandomDeviceId, mStrBleAdd);
                                                mSendRequestTimer.start();
                                                if (mStartDeviceScanTimer != null)
                                                    mStartDeviceScanTimer.start();

//                                                    }
//                                                    else {
//                                                        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_no_any_device_connect), 1, true, new onAlertDialogCallBack() {
//                                                            @Override
//                                                            public void PositiveMethod(DialogInterface dialog, int id) {
//                                                                Fragment mFragment = mActivity.getSupportFragmentManager().findFragmentById(R.id.activity_main_main_content_container);
//                                                                if (!(mFragment instanceof FragmentSettingBridgeConnection)) {
//                                                                    if (!mActivity.isDevicesConnected) {
//                                                                        FragmentSettingBridgeConnection mFragmentSettingBridgeConnection = new FragmentSettingBridgeConnection();
//                                                                        mActivity.replacesFragment(mFragmentSettingBridgeConnection, true, null, 0);
//                                                                    }
//                                                                }
//                                                            }
//
//                                                            @Override
//                                                            public void NegativeMethod(DialogInterface dialog, int id) {
//
//                                                            }
//                                                        });
//                                                    }

                                            }
                                        }
                                    }
                                });
                            }
//                                } else {
//                                    mActivity.connectDeviceWithProgress();
//                                }
                        }
                    }
                } catch (Exception e) {
                    mActivity.hideProgress();
                    e.printStackTrace();
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListAddDevice.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_add_device_item_textview_devicename)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_add_device_item_textview_deviceid)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_add_device_item_textview_add_remove)
            TextView mTextViewAddRemove;
            @BindView(R.id.raw_add_device_imageview_device)
            ImageView mImageViewDevice;
            @BindView(R.id.raw_add_device_list_item_checkbox)
            AppCompatCheckBox mAppCompatCheckBox;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Add Device name alert dialog*/
    public void openDeviceNameDialog(final String mStrDeviceType, final ContentValues mContentValuesInsert, final boolean isInsert) {
        EditTextDialog dialogFragment = new EditTextDialog();

        Bundle bundle = new Bundle();
        bundle.putSerializable(EditTextDialog.messageKey, "Please enter new device nme");
        bundle.putSerializable(EditTextDialog.textHintKey, "Vithamas Smart light");
        bundle.putSerializable(EditTextDialog.positiveButtonNameKey, "Add");
        bundle.putSerializable(EditTextDialog.negativeButtonNameKey, null);
        dialogFragment.setArguments(bundle);

        FragmentTransaction ft = mActivity.getSupportFragmentManager().beginTransaction();
        Fragment prev = mActivity.getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);
        dialogFragment.show(mActivity.getSupportFragmentManager(), "editTextDialog");

        dialogFragment.setDialogListener(new EditTextDialog.DialogListener() {
            @Override
            public void onFinishEditDialog(String newDeviceName, boolean dialogCancelFlag) {
                if (newDeviceName.isEmpty()) {
                    Toast.makeText(getContext(), "Please enter valid name", Toast.LENGTH_LONG).show();
                    openDeviceNameDialog(mStrDeviceType, mContentValuesInsert, isInsert);
                    return;
                }

                String isExistDeviceInDB = CheckDeviceNameExistInDeviceDB(newDeviceName);
                if (isExistDeviceInDB.equalsIgnoreCase("-1")) {
                    InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
//                    alertDialogAndroid.dismiss();
                    ContentValues mContentValues = mContentValuesInsert;
                    mContentValues.put(mActivity.mDbHelper.mFieldDeviceName, newDeviceName);
                    if (isInsert) {
                        int isInsert = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableDevice, mContentValues);
                        if (isInsert != -1) {
                            Calendar cal = Calendar.getInstance();
                            Date currentLocalTime = cal.getTime();
                            mContentValues.put(mActivity.mDbHelper.mFieldDeviceLocalId, isInsert);
                            if (mStrDeviceType.equalsIgnoreCase("0800")) {
                                ContentValues mContentValuesSocket;
                                for (int j = 1; j <= 4; j++) {
                                    mContentValuesSocket = new ContentValues();
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketServerD, "");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketUserId, mActivity.mPreferenceHelper.getUserId());
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketID, j + "");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketName, "Socket " + j);
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketIEEE, "");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceLocalId, isInsert);
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceServerId, "");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceCommId, mIntRandomDeviceId + "");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceCommHexId, mStringRandomDeviceIdHex);
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceName, "Vithamas Light");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceBLEAddress, mStringDeviceBLEAddress.toUpperCase());
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketDeviceType, mStrDeviceType);
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketStatus, "ON");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketTimeStamp, cal.getTimeInMillis());
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketIsActive, "1");
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketCreatedAt, mDateFormatDb.format(currentLocalTime));
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketUpdatedAt, mDateFormatDb.format(currentLocalTime));
                                    mContentValuesSocket.put(mActivity.mDbHelper.mFieldSocketIsSync, "0");
                                    mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTablePowerStripSocket, mContentValuesSocket);
                                }
                            }
                        }

                    } else {
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", new String[]{mContentValues.get(mActivity.mDbHelper.mFieldDeviceLocalId).toString()});
                    }

                    // Device Add API call
                    if (!mActivity.mPreferenceHelper.getIsSkipUser() && mActivity.mUtility.haveInternet()) {
                        addDeviceAPI(mContentValues);
                    } else {
                        showDeviceAddAlert();
                    }
                } else {
                    mActivity.mUtility.hideKeyboard(mActivity);
                    showMessageRedAlert(getView(), getResources().getString(R.string.str_device_name_already_exist), getResources().getString(R.string.str_ok));
                }
            }

            @Override
            public void onCancelEditDialog() {

            }
        });
    }

    /*Check record exist or not in device table*/
    public String CheckDeviceNameExistInDeviceDB(String deviceName) {
        deviceName = deviceName.replace("'", "''");
        deviceName = deviceName.replace("\"", "\\\"");
        DataHolder mDataHolder;
        String url = "select " + mActivity.mDbHelper.mFieldDeviceName + " from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceName + "= '" + deviceName + "'" + " AND " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " AND " + mActivity.mDbHelper.mFieldDeviceIsActive + "= 1";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldDeviceName);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mActivity.mUtility.hideKeyboard(mActivity);
        Snackbar mSnackBar = Snackbar.make(mView, mStringMessage, 5000);
        mSnackBar.setAction(mActionMessage, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mSnackBar.setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
        mSnackBar.getView().setBackgroundColor(getResources().getColor(R.color.colorInActiveMenu));
        mSnackBar.show();
    }

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