package com.succorfish.depthntemp.fragnments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.db.TablePressureTemperature;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.interfaces.onDeviceConnectionStatusChange;
import com.succorfish.depthntemp.vo.VoBluetoothDevices;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentCompareRawData extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.frg_compare_raw_data_list_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.frg_compare_raw_data_rl_no_data)
    RelativeLayout mRelativeLayoutNoData;
    List<TablePressureTemperature> mArrayListPresTemp = new ArrayList<>();
    DeviceListAdapter mDeviceListAdapter;

    boolean isSingleDataSelect = true;
    boolean isDiveDataSelect = true;
    String mStrDeviceOneBleAdd = "";
    String mStrDeviceTwoBleAdd = "";
    String mStrDiveOneId = "0";
    String mStrDiveTwoId = "0";
    String mStrStartDateTime = "";
    String mStrEndDateTime = "";
    private SimpleDateFormat mSimpleDateFormat;
    Calendar mCalendarLocalStartTime;
    Calendar mCalendarLocalStartTimeTo;
    Calendar mCalendarLocalEndTimeFrom;
    Calendar mCalendarLocalEndTimeTo;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mSimpleDateFormat = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm:ss", Locale.getDefault());
        if (getArguments() != null) {
            isSingleDataSelect = getArguments().getBoolean("mBundle_Is_Single_Data");
            isDiveDataSelect = getArguments().getBoolean("mBundle_Is_Dive_Data");
            mStrDeviceOneBleAdd = getArguments().getString("mBundle_device_one_ble");
            mStrDeviceTwoBleAdd = getArguments().getString("mBundle_device_two_ble");
            mStrDiveOneId = getArguments().getString("mBundle_dive_one_id", "0");
            mStrDiveTwoId = getArguments().getString("mBundle_dive_two_id", "0");
            mStrStartDateTime = getArguments().getString("mBundle_start_date_time");
            mStrEndDateTime = getArguments().getString("mBundle_end_date_time");

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_compare_raw_data, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mCalendarLocalStartTime = Calendar.getInstance();
        mCalendarLocalStartTimeTo = Calendar.getInstance();
        mCalendarLocalEndTimeFrom = Calendar.getInstance();
        mCalendarLocalEndTimeTo = Calendar.getInstance();
        mArrayListPresTemp = new ArrayList<>();
//        getDeviceListData();

        new GetDbPressTempDataList().execute();
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {
                if (isAdded()) {
                }
            }

            @Override
            public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                }
            }

            @Override
            public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                }
            }

            @Override
            public void onError() {

            }
        });
        return mViewRoot;
    }

    /*get db pressure temperature data*/
    private class GetDbPressTempDataList extends AsyncTask<String, Integer, List<TablePressureTemperature>> {

        @Override
        protected void onPreExecute() {
            mActivity.showProgress("Please wait..", false);
            super.onPreExecute();
        }

        @Override
        protected List<TablePressureTemperature> doInBackground(String... params) {
//            List<TablePressureTemperature> mTablePressureTemperatures = new ArrayList<>();
            if (isSingleDataSelect) {
                if (isDiveDataSelect) {
                    mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDive(Integer.parseInt(mStrDiveOneId));
                } else {
                    mCalendarLocalStartTime.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.set(Calendar.HOUR_OF_DAY, 23);
                    mCalendarLocalStartTimeTo.set(Calendar.MINUTE, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.SECOND, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.MILLISECOND, 999);
                    System.out.println("mCalendarLocalStartTime-" + mCalendarLocalStartTime.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTimeTo-" + mCalendarLocalStartTimeTo.getTimeInMillis());
//                    mCalendarUtcStartTime.setTimeInMillis(mCalendarLocalStartTime.getTimeInMillis());
//                    mCalendarUtcStartTimeTo.setTimeInMillis(mCalendarLocalStartTimeTo.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTime-" + localToUTC(mCalendarLocalStartTime.getTimeInMillis()));
                    System.out.println("mCalendarLocalStartTimeTo-" + localToUTC(mCalendarLocalStartTimeTo.getTimeInMillis()));
                    try {
                        mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDate(mCalendarLocalStartTime.getTimeInMillis(), mCalendarLocalStartTimeTo.getTimeInMillis());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                if (isDiveDataSelect) {
                    mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataByMultiDive(Integer.parseInt(mStrDiveOneId), Integer.parseInt(mStrDiveTwoId));
                } else {
                    mCalendarLocalStartTime.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                    mCalendarLocalStartTimeTo.set(Calendar.HOUR_OF_DAY, 23);
                    mCalendarLocalStartTimeTo.set(Calendar.MINUTE, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.SECOND, 59);
                    mCalendarLocalStartTimeTo.set(Calendar.MILLISECOND, 999);

                    mCalendarLocalEndTimeFrom.setTimeInMillis(Long.parseLong(mStrEndDateTime));
                    mCalendarLocalEndTimeTo.setTimeInMillis(Long.parseLong(mStrEndDateTime));
                    mCalendarLocalEndTimeTo.set(Calendar.HOUR_OF_DAY, 23);
                    mCalendarLocalEndTimeTo.set(Calendar.MINUTE, 59);
                    mCalendarLocalEndTimeTo.set(Calendar.SECOND, 59);
                    mCalendarLocalEndTimeTo.set(Calendar.MILLISECOND, 999);

                    System.out.println("mCalendarLocalStartTime-" + mCalendarLocalStartTime.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTimeTo-" + mCalendarLocalStartTimeTo.getTimeInMillis());
//                    mCalendarUtcStartTime.setTimeInMillis(mCalendarLocalStartTime.getTimeInMillis());
//                    mCalendarUtcStartTimeTo.setTimeInMillis(mCalendarLocalStartTimeTo.getTimeInMillis());
                    System.out.println("mCalendarLocalStartTime-" + localToUTC(mCalendarLocalStartTime.getTimeInMillis()));
                    System.out.println("mCalendarLocalStartTimeTo-" + localToUTC(mCalendarLocalStartTimeTo.getTimeInMillis()));

                    mArrayListPresTemp = mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDate(mCalendarLocalStartTime.getTimeInMillis(), mCalendarLocalStartTimeTo.getTimeInMillis());
                    mArrayListPresTemp.addAll(mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempDataBySingleDate(mCalendarLocalEndTimeFrom.getTimeInMillis(), mCalendarLocalEndTimeTo.getTimeInMillis()));
                }
            }

            if (mArrayListPresTemp == null) {
                mArrayListPresTemp = new ArrayList<>();
            }
            return mArrayListPresTemp;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<TablePressureTemperature> mPressureTemperatureList) {
            super.onPostExecute(mPressureTemperatureList);
            mActivity.hideProgress();
            System.out.println("SIZE-" + mArrayListPresTemp.size());
            mDeviceListAdapter = new DeviceListAdapter();
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity);
            mLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
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
        }
    }

    public static long localToUTC(long time) {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = new Date(time);
            dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            String strDate = dateFormat.format(date);
//            System.out.println("Local Millis * " + date.getTime() + "  ---UTC time  " + strDate);//correct
            SimpleDateFormat dateFormatLocal = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date utcDate = dateFormatLocal.parse(strDate);
//            System.out.println("UTC Millis * " + utcDate.getTime() + " ------  " + dateFormatLocal.format(utcDate));
            return utcDate.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return time;
    }

//    private void getDeviceListData() {
//        mArrayListDriveData = new ArrayList<>();
//        try {
//            String url = "select * from " + mActivity.mDbHelper.mTableDevice + " where " + mActivity.mDbHelper.mFieldDeviceIsActive + "= '1'" + " AND " + mActivity.mDbHelper.mFieldDeviceUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " ORDER BY " + mActivity.mDbHelper.mFieldDeviceIsFavourite + " desc";
//            System.out.println("Local url " + url);
//            mDataHolderLight = mActivity.mDbHelper.read(url);
//            if (mDataHolderLight != null) {
//                System.out.println("Local Device List " + url + " : " + mDataHolderLight.get_Listholder().size());
//                VoDeviceList mVoDeviceList;
//                for (int i = 0; i < mDataHolderLight.get_Listholder().size(); i++) {
//                    mVoDeviceList = new VoDeviceList();
//                    mVoDeviceList.setDevicLocalId(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLocalId));
//                    mVoDeviceList.setDeviceServerid(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceServerId));
//                    mVoDeviceList.setUser_id(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUserId));
//                    mVoDeviceList.setDevice_Comm_id(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommID));
//                    mVoDeviceList.setDevice_Comm_hexId(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCommHexId));
//                    mVoDeviceList.setDevice_name(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceName));
//                    mVoDeviceList.setDevice_realName(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceRealName));
//                    mVoDeviceList.setDevice_BleAddress(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceBleAddress).toUpperCase());
//                    mVoDeviceList.setDevice_Type(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceType));
//                    mVoDeviceList.setDevice_type_name(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTypeName));
//                    mVoDeviceList.setDevice_ConnStatus(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldConnectStatus));
//                    mVoDeviceList.setDevice_SwitchStatus(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSwitchStatus));
//                    mVoDeviceList.setDevice_is_favourite(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsFavourite));
//                    mVoDeviceList.setDevice_last_state_remember(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceLastState));
//                    mVoDeviceList.setDevice_timestamp(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceTimeStamp));
//                    mVoDeviceList.setDevice_is_active(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsActive));
//                    mVoDeviceList.setDevice_created_at(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceCreatedAt));
//                    mVoDeviceList.setDevice_updated_at(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceUpdatedAt));
//                    mVoDeviceList.setDevice_is_sync(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDeviceIsSync));
//                    if (mVoDeviceList.getDevice_SwitchStatus() != null && mVoDeviceList.getDevice_SwitchStatus().equalsIgnoreCase("ON")) {
//                        mVoDeviceList.setIsChecked(true);
//                    } else {
//                        mVoDeviceList.setIsChecked(false);
//                    }
//                    mArrayListDevice.add(mVoDeviceList);
//                }
////                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
////                    @Override
////                    public int compare(TreatmentList s1, TreatmentList s2) {
////                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
////                    }
////                });
//            }
//
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        mDeviceListAdapter = new DeviceListAdapter();
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setAdapter(mDeviceListAdapter);
//        mDeviceListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                checkAdapterIsEmpty();
//            }
//        });
//        checkAdapterIsEmpty();
//        isCalling = false;
//        mSwipeRefreshLayout.setRefreshing(false);
//
//        if (mDeviceListAdapter == null) {
//            mDeviceListAdapter = new DeviceListAdapter();
//            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
//            mRecyclerView.setLayoutManager(mLayoutManager);
//            mRecyclerView.setAdapter(mDeviceListAdapter);
//            mDeviceListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//                @Override
//                public void onChanged() {
//                    super.onChanged();
//                    checkAdapterIsEmpty();
//                }
//            });
//        } else {
//            mDeviceListAdapter.notifyDataSetChanged();
//        }
//        checkAdapterIsEmpty();
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        mArrayListPresTemp.clear();
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
                    mRelativeLayoutNoData.setVisibility(View.GONE);
                } else {
                    mRelativeLayoutNoData.setVisibility(View.VISIBLE);
                }
            } else {
                mRelativeLayoutNoData.setVisibility(View.VISIBLE);
            }
        }
    }

    Calendar mCalendarTime;
    double tempCal = 0;
    //    double pressCal = 0;

    /*List adapter*/
    public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.ViewHolder> {

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_data_caompare_list_item, parent, false);
            return new DeviceListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            if (mArrayListPresTemp.get(position).getDiveIdFk() + "" != null && !(mArrayListPresTemp.get(position).getDiveIdFk() + "").equalsIgnoreCase("")) {
                holder.mTextViewDrive.setText("Dive " + mArrayListPresTemp.get(position).getDiveIdFk() + "");
            } else {
                holder.mTextViewDrive.setText("-NA-");
            }
            if (mArrayListPresTemp.get(position).getUtcTime() + "" != null && !(mArrayListPresTemp.get(position).getUtcTime() + "").equalsIgnoreCase("")) {
                try {
                    mCalendarTime = Calendar.getInstance();
                    mCalendarTime.setTimeInMillis(mArrayListPresTemp.get(position).getUtcTime());
                    holder.mTextViewTime.setText(mSimpleDateFormat.format(mCalendarTime.getTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                    holder.mTextViewTime.setText("-NA-");
                }
            } else {
                holder.mTextViewTime.setText("-NA-");
            }
            if (mArrayListPresTemp.get(position).getPressure_depth() != null && !mArrayListPresTemp.get(position).getPressure_depth().equalsIgnoreCase("")) {
//                holder.mTextViewDepth.setText(BLEUtility.hexToDecimal(mArrayListPresTemp.get(position).getPressure()) + "");
//                pressCal = Double.parseDouble(mArrayListPresTemp.get(position).getPressure());
//                pressCal = (pressCal - 1013) / 100;
                holder.mTextViewDepth.setText(mArrayListPresTemp.get(position).getPressure_depth() + "");
            } else {
                holder.mTextViewDepth.setText("-NA-");
            }
            try {
                if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
                    if (mArrayListPresTemp.get(position).getTemperature() != null && !mArrayListPresTemp.get(position).getTemperature().equalsIgnoreCase("")) {
                        tempCal = Double.parseDouble(mArrayListPresTemp.get(position).getTemperature());
                        holder.mTextViewTemp.setText(String.format("%.2f °C", tempCal));
                    } else {
                        holder.mTextViewTemp.setText("-NA-");
                    }
                } else {
                    if (mArrayListPresTemp.get(position).getTemperature_far() != null && !mArrayListPresTemp.get(position).getTemperature_far().equalsIgnoreCase("")) {
                        tempCal = Double.parseDouble(mArrayListPresTemp.get(position).getTemperature_far());
                        holder.mTextViewTemp.setText(String.format("%.2f °F", tempCal));
                    } else {
                        holder.mTextViewTemp.setText("-NA-");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                holder.mTextViewTime.setText("-NA-");
            }
//                holder.mTextViewDepth.setText(mArrayListPresTemp.get(position).getPressure());

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    try {
//                        if (mArrayListPresTemp != null) {
//                            if (position < mArrayListPresTemp.size()) {
//                                openDeviceNameDialog(mArrayListPresTemp.get(position));
//                            }
//                        }
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListPresTemp == null ? 0 : mArrayListPresTemp.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_compare_raw_data_item_tv_drive)
            TextView mTextViewDrive;
            @BindView(R.id.raw_compare_raw_data_item_tv_time)
            TextView mTextViewTime;
            @BindView(R.id.raw_compare_raw_data_item_tv_depth)
            TextView mTextViewDepth;
            @BindView(R.id.raw_compare_raw_data_item_tv_temp)
            TextView mTextViewTemp;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

//    public void openDeviceNameDialog(final TablePressureTemperature mTablePressureTemperature) {
//        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
//        final View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
//        android.support.v7.app.AlertDialog.Builder alertDialogBuilderUserInput = new android.support.v7.app.AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//        alertDialogBuilderUserInput.setView(mView);
//        final EditText mEditTextMsg = (EditText) mView.findViewById(R.id.user_input_dialog_et_name);
//        final EditText mEditTextNo = (EditText) mView.findViewById(R.id.user_input_dialog_et_no);
//        mEditTextNo.setVisibility(View.VISIBLE);
////        TextView mTextViewTitle = (TextView) mView.findViewById(R.id.user_input_dialog_tv_title);
//        alertDialogBuilderUserInput
//                .setCancelable(false)
//                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialogBox, int id) {
//                        // ToDo get user input here
//
//                    }
//                })
//                .setNegativeButton("Cancel",
//                        new DialogInterface.OnClickListener() {
//                            public void onClick(DialogInterface dialogBox, int id) {
//                                mActivity.mUtility.hideKeyboard(mActivity);
//                                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                                im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
//                                dialogBox.cancel();
//                            }
//                        });
//        final android.support.v7.app.AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
//        alertDialogAndroid.show();
//        alertDialogAndroid.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mActivity.mUtility.hideKeyboard(mActivity);
//                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
//                alertDialogAndroid.dismiss();
//            }
//        });
//        alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                final String mStringName = mEditTextMsg.getText().toString().trim();
//                final String mStringNo = mEditTextNo.getText().toString().trim();
//                if (mStringName != null && !mStringName.equalsIgnoreCase("")) {
//                    InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                    im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
//                    alertDialogAndroid.dismiss();
//                    if (mStringNo.equalsIgnoreCase("1")) {
//                        mTablePressureTemperature.setPressure(mStringName);
//                    } else if (mStringNo.equalsIgnoreCase("2")) {
//                        mTablePressureTemperature.setPressure_depth(mStringName);
//                    } else if (mStringNo.equalsIgnoreCase("3")) {
//                        mTablePressureTemperature.setTemperature(mStringName);
//                    } else if (mStringNo.equalsIgnoreCase("4")) {
//                        mTablePressureTemperature.setTemperature_far(mStringName);
//                    } else if (mStringNo.equalsIgnoreCase("5")) {
//                    }
//                    new AsyncTask<Void, Void, Void>() {
//                        @Override
//                        protected Void doInBackground(Void... params) {
//                            if (mStringNo.equalsIgnoreCase("5")) {
//                                mActivity.mAppRoomDatabase.diveDao().deleteDiveById(Integer.parseInt(mStringName));
//                            } else {
//                                mActivity.mAppRoomDatabase.tempPressDao().update(mTablePressureTemperature);
//                            }
//                            return null;
//                        }
//
//                        @Override
//                        protected void onPostExecute(Void agentsCount) {
//                        }
//                    }.execute();
//
//                } else {
//                    mActivity.mUtility.hideKeyboard(mActivity);
//                }
//            }
//        });
//
//    }
}
