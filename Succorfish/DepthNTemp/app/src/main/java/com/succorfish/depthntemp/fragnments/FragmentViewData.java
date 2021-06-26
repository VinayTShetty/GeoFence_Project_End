package com.succorfish.depthntemp.fragnments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.db.TableDive;
import com.succorfish.depthntemp.helper.PreferenceHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 16-02-2018.
 */

public class FragmentViewData extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_view_data_rg_compare)
    RadioGroup mRadioGroupCompare;
    @BindView(R.id.frg_view_data_rg_time_frame)
    RadioGroup mRadioGroupTimeFrame;
    @BindView(R.id.frg_view_data_ll_dive)
    LinearLayout mLinearLayoutMainDive;
    @BindView(R.id.frg_view_data_ll_date_time)
    LinearLayout mLinearLayoutMainTime;
    @BindView(R.id.frg_view_data_ll_dive_two)
    LinearLayout mLinearLayoutDiveTwo;
    @BindView(R.id.frg_view_data_ll_date_time_two)
    LinearLayout mLinearLayoutTimeTwo;
    @BindView(R.id.frg_view_data_tv_lbl_dive_one)
    TextView mTextViewLblDiveOne;
    @BindView(R.id.frg_view_data_tv_lbl_ble_device_one)
    TextView mTextViewLblBleDeviceOne;
    @BindView(R.id.frg_view_data_tv_lbl_date_time_one)
    TextView mTextViewLblTimeOne;
    @BindView(R.id.frg_view_data_tv_date_time_two)
    TextView mTextViewDateTimeTwo;
    @BindView(R.id.frg_view_data_tv_date_time_one)
    TextView mTextViewDateTimeOne;
    @BindView(R.id.frg_view_data_spinner_dive_one)
    Spinner mSpinnerDiveOne;
    @BindView(R.id.frg_view_data_spinner_dive_two)
    Spinner mSpinnerDiveTwo;
    @BindView(R.id.frg_view_data_spinner_ble_device_one)
    Spinner mSpinnerBleDeviceOne;
    @BindView(R.id.frg_view_data_spinner_ble_device_two)
    Spinner mSpinnerBleDeviceTwo;

    List<TableDive> mDbBleDeviceList = new ArrayList<>();
    List<TableDive> mDbDiveList = new ArrayList<>();
    List<TableDive> mDbDiveListTwo = new ArrayList<>();
    DiveOneListAdapter mDiveOneListAdapter;
    DiveTwoListAdapter mDiveTwoListAdapter;
    BleDeviceOneListAdapter mBleDeviceOneListAdapter;
    BleDeviceTwoListAdapter mBleDeviceTwoListAdapter;
    SimpleDateFormat mSimpleDateFormat;
    Calendar newCalendar;
    Calendar newCalendarEnd;
    TimePickerDialog mTimePickerStartTimeDialog;
    TimePickerDialog mTimePickerEndTimeDialog;
    String mStrDeviceOneBleAdd = "";
    String mStrDeviceTwoBleAdd = "";
    String mStrDeviceOneName = "";
    String mStrDeviceTwoName = "";
    String mStrDiveOneId = "";
    String mStrDiveTwoId = "";
    String mStrDiveOneName = "";
    String mStrDiveTwoName = "";
    String mStrDiveOneLatitude = "";
    String mStrDiveTwoLatitude = "";
    String mStrDiveOneLongitude = "";
    String mStrDiveTwoLongitude = "";
    int deviceOneLocalPosition = 0;
    int deviceTwoLocalPosition = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_view_data, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        newCalendar = Calendar.getInstance();
        newCalendarEnd = Calendar.getInstance();
        mSimpleDateFormat = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm:ss", Locale.getDefault());

        mActivity.mToolbar.setVisibility(View.VISIBLE);
        mActivity.mTextViewTitle.setText(R.string.str_menu_view_date);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mDbBleDeviceList = new ArrayList<>();
        mDbDiveList = new ArrayList<>();
        mDbDiveListTwo = new ArrayList<>();
        mTextViewDateTimeOne.setText(mSimpleDateFormat.format(newCalendar.getTime()));
        mTextViewDateTimeTwo.setText(mSimpleDateFormat.format(newCalendarEnd.getTime()));
        deviceOneLocalPosition = mActivity.deviceOnePosition;
        deviceTwoLocalPosition = mActivity.deviceTwoPosition;
        /*Init Dive 1 Adapter*/
        mDiveOneListAdapter = new DiveOneListAdapter();
        mSpinnerDiveOne.setAdapter(mDiveOneListAdapter);
        mSpinnerDiveOne.setSelection(0, false);
        /*Init Dive 2 Adapter*/
        mDiveTwoListAdapter = new DiveTwoListAdapter();
        mSpinnerDiveTwo.setAdapter(mDiveTwoListAdapter);
        mSpinnerDiveTwo.setSelection(0, false);
        /*Init Device 1 Adapter*/
        mBleDeviceOneListAdapter = new BleDeviceOneListAdapter();
        mSpinnerBleDeviceOne.setAdapter(mBleDeviceOneListAdapter);
        mSpinnerBleDeviceOne.setSelection(0, false);
        /*Init Device 2 Adapter*/
        mBleDeviceTwoListAdapter = new BleDeviceTwoListAdapter();
        mSpinnerBleDeviceTwo.setAdapter(mBleDeviceTwoListAdapter);
        mSpinnerBleDeviceTwo.setSelection(0, false);
        /*Get Ble Device List from db*/
        new GetDbBleDeviceList().execute();

        mRadioGroupCompare.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mActivity.mUtility.hideKeyboard(mActivity);
                if (checkedId == R.id.frg_view_data_rb_compare) {
                    mTextViewLblDiveOne.setText(getResources().getText(R.string.str_first_dive));
                    mTextViewLblBleDeviceOne.setText(getResources().getText(R.string.str_first_device));
                    mTextViewLblTimeOne.setText(getResources().getText(R.string.str_choose_start_date_time));
                    mLinearLayoutDiveTwo.setVisibility(View.VISIBLE);
                    mLinearLayoutTimeTwo.setVisibility(View.VISIBLE);
                    if (mRadioGroupTimeFrame.getCheckedRadioButtonId() == R.id.frg_view_data_rb_time) {
                        mLinearLayoutMainTime.setVisibility(View.VISIBLE);
                        mLinearLayoutMainDive.setVisibility(View.GONE);
                    } else {
                        mLinearLayoutMainTime.setVisibility(View.GONE);
                        mLinearLayoutMainDive.setVisibility(View.VISIBLE);
                    }

                } else {
                    mLinearLayoutDiveTwo.setVisibility(View.GONE);
                    mLinearLayoutTimeTwo.setVisibility(View.GONE);
                    mTextViewLblDiveOne.setText(getResources().getText(R.string.str_prompt_select_dive));
                    mTextViewLblBleDeviceOne.setText(getResources().getText(R.string.str_prompt_select_device));
                    mTextViewLblTimeOne.setText(getResources().getText(R.string.str_choose_date_time));
                    if (mRadioGroupTimeFrame.getCheckedRadioButtonId() == R.id.frg_view_data_rb_time) {
                        mLinearLayoutMainTime.setVisibility(View.VISIBLE);
                        mLinearLayoutMainDive.setVisibility(View.GONE);
                    } else {
                        mLinearLayoutMainTime.setVisibility(View.GONE);
                        mLinearLayoutMainDive.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        mRadioGroupTimeFrame.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mActivity.mUtility.hideKeyboard(mActivity);
                if (checkedId == R.id.frg_view_data_rb_time) {
                    mLinearLayoutMainTime.setVisibility(View.VISIBLE);
                    mLinearLayoutMainDive.setVisibility(View.GONE);
                    if (mRadioGroupCompare.getCheckedRadioButtonId() == R.id.frg_view_data_rb_compare) {
                        mTextViewLblTimeOne.setText(getResources().getText(R.string.str_choose_start_date_time));
                        mLinearLayoutTimeTwo.setVisibility(View.VISIBLE);
                    } else {
                        mLinearLayoutTimeTwo.setVisibility(View.GONE);
                        mTextViewLblTimeOne.setText(getResources().getText(R.string.str_choose_date_time));
                    }
                } else {
                    mLinearLayoutMainTime.setVisibility(View.GONE);
                    mLinearLayoutMainDive.setVisibility(View.VISIBLE);
                    if (mRadioGroupCompare.getCheckedRadioButtonId() == R.id.frg_view_data_rb_compare) {
                        mLinearLayoutDiveTwo.setVisibility(View.VISIBLE);
                        mTextViewLblDiveOne.setText(getResources().getText(R.string.str_first_dive));
                        mTextViewLblBleDeviceOne.setText(getResources().getText(R.string.str_first_device));
                    } else {
                        mLinearLayoutDiveTwo.setVisibility(View.GONE);
                        mTextViewLblDiveOne.setText(getResources().getText(R.string.str_prompt_select_dive));
                        mTextViewLblBleDeviceOne.setText(getResources().getText(R.string.str_prompt_select_device));
                    }
                }
            }
        });
        mSpinnerBleDeviceOne.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStrDeviceOneBleAdd = mDbBleDeviceList.get(position).getBleAddress();
                mStrDeviceOneName = mDbBleDeviceList.get(position).getDeviceName() + "_" + mDbBleDeviceList.get(position).getBleAddress();
                deviceOneLocalPosition = position;
                getDbDiveList(true);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        mSpinnerBleDeviceTwo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStrDeviceTwoBleAdd = mDbBleDeviceList.get(position).getBleAddress();
                mStrDeviceTwoName = mDbBleDeviceList.get(position).getDeviceName() + "_" + mDbBleDeviceList.get(position).getBleAddress();
                deviceTwoLocalPosition = position;
                getDbDiveList(false);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerDiveOne.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStrDiveOneLatitude = mDbDiveList.get(position).getGpsLatitude();
                mStrDiveOneLongitude = mDbDiveList.get(position).getGpsLongitude();
                mStrDiveOneId = mDbDiveList.get(position).getDiveId() + "";
                mActivity.diveOnePosition = position;
                try {
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(mDbDiveList.get(position).getUtcTime());
                    mStrDiveOneName = "Dive " + mStrDiveOneId + "_" + mSimpleDateFormat.format(mCalendar.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    mStrDiveOneName = "Dive " + mStrDiveOneId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mSpinnerDiveTwo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mStrDiveTwoLatitude = mDbDiveListTwo.get(position).getGpsLatitude();
                mStrDiveTwoLongitude = mDbDiveListTwo.get(position).getGpsLongitude();
                mStrDiveTwoId = mDbDiveListTwo.get(position).getDiveId() + "";
                mActivity.diveTwoPosition = position;
                try {
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(mDbDiveListTwo.get(position).getUtcTime());
                    mStrDiveTwoName = "Dive " + mStrDiveTwoId + "_" + mSimpleDateFormat.format(mCalendar.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                    mStrDiveTwoName = "Dive " + mStrDiveTwoId;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return mViewRoot;
    }

    /*on submit button check required validation*/
    @OnClick(R.id.frg_view_data_button_submit)
    public void onSubmitClick(View mView) {
        if (isAdded()) {
            mActivity.mUtility.hideKeyboard(mActivity);
            boolean isSingleDataSelect = true;
            boolean isDiveDataSelect = true;
            if (mRadioGroupCompare.getCheckedRadioButtonId() == R.id.frg_view_data_rb_compare) {
                isSingleDataSelect = false;
                if (mRadioGroupTimeFrame.getCheckedRadioButtonId() == R.id.frg_view_data_rb_time) {
                    isDiveDataSelect = false;
//                    if (DateUtils.isSameDay(newCalendar, newCalendarEnd)) {
//                        mActivity.mUtility.errorDialog("Please select different date", 3);
//                        return;
//                    } else if (DateUtils.isBeforeDay(newCalendar, newCalendarEnd)) {
//                        System.out.println("BeforeDate(FutureDate)");
//                    } else if (DateUtils.isAfterDay(newCalendar, newCalendarEnd)) {
//                        System.out.println("AfterDate(PassedDate)");
//                    }
                    if (newCalendar.getTimeInMillis() == newCalendarEnd.getTimeInMillis()) {
                        mActivity.mUtility.errorDialog("Please select different date", 3);
                        return;
                    }
                } else {
                    isDiveDataSelect = true;
                    if (mStrDeviceOneBleAdd.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog("Please select device one", 3);
                        return;
                    }
                    if (mStrDiveOneId.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog("Please select dive one", 3);
                        return;
                    }
                    if (mStrDeviceTwoBleAdd.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog("Please select device two", 3);
                        return;
                    }
                    if (mStrDiveTwoId.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog("Please select dive two", 3);
                        return;
                    }
                    if (mStrDiveOneId.equalsIgnoreCase(mStrDiveTwoId)) {
                        mActivity.mUtility.errorDialog("Please select different dive", 3);
                        return;
                    }
                }
            } else {
                isSingleDataSelect = true;
                if (mRadioGroupTimeFrame.getCheckedRadioButtonId() == R.id.frg_view_data_rb_time) {
                    isDiveDataSelect = false;
                } else {
                    isDiveDataSelect = true;
                    if (mStrDeviceOneBleAdd.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog("Please select device", 3);
                        return;
                    }
                    if (mStrDiveOneId.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog("Please select dive", 3);
                        return;
                    }
                }
            }
            try {
                Bundle mBundle = new Bundle();
                mBundle.putBoolean("mIntent_Is_Single_Data", isSingleDataSelect);
                mBundle.putBoolean("mIntent_Is_Dive_Data", isDiveDataSelect);
                mBundle.putString("mIntent_device_one_ble", mStrDeviceOneBleAdd);
                mBundle.putString("mIntent_device_two_ble", mStrDeviceTwoBleAdd);
                mBundle.putString("mIntent_device_one_name", mStrDeviceOneName);
                mBundle.putString("mIntent_device_two_name", mStrDeviceTwoName);
                mBundle.putString("mIntent_dive_one_id", mStrDiveOneId);
                mBundle.putString("mIntent_dive_two_id", mStrDiveTwoId);
                mBundle.putString("mIntent_dive_one_name", mStrDiveOneName);
                mBundle.putString("mIntent_dive_two_name", mStrDiveTwoName);
                mBundle.putString("mIntent_start_date_time", newCalendar.getTimeInMillis() + "");
                mBundle.putString("mIntent_end_date_time", newCalendarEnd.getTimeInMillis() + "");
                mBundle.putString("mIntent_dive_one_latitude", mStrDiveOneLatitude);
                mBundle.putString("mIntent_dive_one_longitude", mStrDiveOneLongitude);
                mBundle.putString("mIntent_dive_two_latitude", mStrDiveTwoLatitude);
                mBundle.putString("mIntent_dive_two_longitude", mStrDiveTwoLongitude);
                mActivity.replacesFragment(new FragmentViewDataCompare(), true, mBundle, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @OnClick(R.id.frg_view_data_ll_date_time_one)
    public void onStartDateClick(View mView) {
        if (isAdded()) {
            showStartDateDialog();
        }
    }

    @OnClick(R.id.frg_view_data_ll_date_time_two)
    public void onStartTimeClick(View mView) {
        if (isAdded()) {
            showEndDateDialog();
        }
    }

    /*Get dive list from local database*/
    private void getDbDiveList(final boolean isDiveOne) {
        try {
            new AsyncTask<Void, Void, List<TableDive>>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();
                }

                @Override
                protected List<TableDive> doInBackground(Void... params) {
                    List<TableDive> mDiveList = new ArrayList<>();
                    if (isDiveOne) {
                        mDiveList = mActivity.mAppRoomDatabase.diveDao().getDiveList(mStrDeviceOneBleAdd);
                    } else {
                        mDiveList = mActivity.mAppRoomDatabase.diveDao().getDiveList(mStrDeviceTwoBleAdd);
                    }
                    if (mDiveList == null) {
                        mDiveList = new ArrayList<>();
                    }
                    return mDiveList;
                }

                @Override
                protected void onPostExecute(List<TableDive> mDiveList) {
                    super.onPostExecute(mDiveList);
                    if (isAdded()) {
                        if (isDiveOne) {
                            mDbDiveList = new ArrayList<>();
                            mDbDiveList = mDiveList;
                            mDiveOneListAdapter = new DiveOneListAdapter();
                            mSpinnerDiveOne.setAdapter(mDiveOneListAdapter);
                            System.out.println("mActivity.diveOneSize-" + mDbDiveList.size());
                            System.out.println("mActivity.diveOnePosition-" + mActivity.diveOnePosition);
                            if (deviceOneLocalPosition == mActivity.deviceOnePosition) {
                                mSpinnerDiveOne.setSelection(mActivity.diveOnePosition, false);
                            } else {
                                mSpinnerDiveOne.setSelection(0, false);
                            }
                            mActivity.deviceOnePosition = deviceOneLocalPosition;
                        } else {
                            mDbDiveListTwo = new ArrayList<>();
                            mDbDiveListTwo = mDiveList;
                            mDiveTwoListAdapter = new DiveTwoListAdapter();
                            mSpinnerDiveTwo.setAdapter(mDiveTwoListAdapter);
                            System.out.println("mActivity.diveTwoSize-" + mDbDiveListTwo.size());
                            System.out.println("mActivity.diveTwoPosition-" + mActivity.diveTwoPosition);
                            if (deviceTwoLocalPosition == mActivity.deviceTwoPosition) {
                                mSpinnerDiveTwo.setSelection(mActivity.diveTwoPosition, false);
                            } else {
                                mSpinnerDiveTwo.setSelection(0, false);
                            }
                            mActivity.deviceTwoPosition = deviceTwoLocalPosition;
                        }
                    }
                }
            }.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*get ble device list from local db*/
    private class GetDbBleDeviceList extends AsyncTask<String, Integer, List<TableDive>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected List<TableDive> doInBackground(String... params) {
            List<TableDive> mDeviceList = new ArrayList<>();
            mDeviceList = mActivity.mAppRoomDatabase.diveDao().getBleDeviceList();
            if (mDeviceList == null) {
                mDeviceList = new ArrayList<>();
            }
            return mDeviceList;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(List<TableDive> mDevicesList) {
            super.onPostExecute(mDevicesList);
            if (isAdded()) {
                mDbBleDeviceList = new ArrayList<>();
                mDbBleDeviceList = mDevicesList;
                mBleDeviceOneListAdapter = new BleDeviceOneListAdapter();
                mSpinnerBleDeviceOne.setAdapter(mBleDeviceOneListAdapter);
//            mSpinnerBleDeviceOne.setSelection(0, false);
                mSpinnerBleDeviceOne.setSelection(deviceOneLocalPosition, false);

                mBleDeviceTwoListAdapter = new BleDeviceTwoListAdapter();
                mSpinnerBleDeviceTwo.setAdapter(mBleDeviceTwoListAdapter);
//            mSpinnerBleDeviceTwo.setSelection(0, false);
                mSpinnerBleDeviceTwo.setSelection(deviceTwoLocalPosition, false);
            }
        }
    }

    /*Show Start Date Dialog*/
    private void showStartDateDialog() {
        if (mRadioGroupCompare.getCheckedRadioButtonId() == R.id.frg_view_data_rb_compare) {
            DatePickerDialog mDatePickerDialog = new DatePickerDialog(mActivity, R.style.AppCompatAlertDialogStyle, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                    Calendar newDate = Calendar.getInstance();
                    newCalendar.set(year, monthOfYear, dayOfMonth);
//                    mStartDate=newDate.getTime();
                    mTextViewDateTimeOne.setText(mSimpleDateFormat.format(newCalendar.getTime()));
                    showStartTimeDialog();
                }
            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
            mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
            mDatePickerDialog.show();
        } else {
            DatePickerDialog mDatePickerDialog = new DatePickerDialog(mActivity, R.style.AppCompatAlertDialogStyle, new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                    Calendar newDate = Calendar.getInstance();
                    newCalendar.set(year, monthOfYear, dayOfMonth);
//                    mStartDate=newDate.getTime();
                    mTextViewDateTimeOne.setText(mSimpleDateFormat.format(newCalendar.getTime()));
                    showStartTimeDialog();
                }
            }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
            mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
            mDatePickerDialog.show();
        }
    }

    /*Show Start Time Dialog*/
    private void showStartTimeDialog() {
        mTimePickerStartTimeDialog = new TimePickerDialog(mActivity, R.style.DialogTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker,
                                          int selectedHour, int selectedMinute) {
//                        mStartDate.setHours(selectedHour);
//                        mStartDate.setMinutes(selectedMinute);
                        newCalendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        newCalendar.set(Calendar.MINUTE, selectedMinute);
                        mTextViewDateTimeOne.setText(mSimpleDateFormat.format(newCalendar.getTime()));
                    }
                }, newCalendar.get(Calendar.HOUR_OF_DAY), newCalendar.get(Calendar.MINUTE), true);// Yes 24 hour time
        mTimePickerStartTimeDialog.setTitle("Select Time");
        if (mRadioGroupCompare.getCheckedRadioButtonId() == R.id.frg_view_data_rb_compare) {
            mTimePickerStartTimeDialog.setTitle("Select Start Date Time");
        }
        mTimePickerStartTimeDialog.show();
    }

    /*Show End Date Dialog*/
    private void showEndDateDialog() {
        DatePickerDialog mDatePickerDialog = new DatePickerDialog(mActivity, R.style.AppCompatAlertDialogStyle, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                Calendar newDate = Calendar.getInstance();
                newCalendarEnd.set(year, monthOfYear, dayOfMonth);
//                mEndDate=newDate.getTime();
                mTextViewDateTimeTwo.setText(mSimpleDateFormat.format(newCalendarEnd.getTime()));
                showEndTimeDialog();
            }
        }, newCalendarEnd.get(Calendar.YEAR), newCalendarEnd.get(Calendar.MONTH), newCalendarEnd.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis() - 1000);
        mDatePickerDialog.show();
    }

    private void showEndTimeDialog() {
        mTimePickerEndTimeDialog = new TimePickerDialog(mActivity, R.style.DialogTheme,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker,
                                          int selectedHour, int selectedMinute) {
//                        mEndDate.setHours(selectedHour);
//                        mEndDate.setMinutes(selectedMinute);
                        newCalendarEnd.set(Calendar.HOUR_OF_DAY, selectedHour);
                        newCalendarEnd.set(Calendar.MINUTE, selectedMinute);
                        mTextViewDateTimeTwo.setText(mSimpleDateFormat.format(newCalendarEnd.getTime()));
                    }
                }, newCalendarEnd.get(Calendar.HOUR_OF_DAY), newCalendarEnd.get(Calendar.MINUTE), true);// Yes 24 hour time
        mTimePickerEndTimeDialog.setTitle("Select End Date Time");
        mTimePickerEndTimeDialog.show();
    }

    /*Dive 1 list adapter*/
    public class DiveOneListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDbDiveList == null ? 0 : mDbDiveList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDbDiveList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
            try {
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(mDbDiveList.get(position).getUtcTime());
                mTextViewCategoryName.setText("Dive " + mDbDiveList.get(position).getDiveId() + "_" + mSimpleDateFormat.format(mCalendar.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
                mTextViewCategoryName.setText("Dive " + mDbDiveList.get(position).getDiveId());
            }
            return view;
        }
    }

    /*Dive 2 list adapter*/
    public class DiveTwoListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDbDiveListTwo == null ? 0 : mDbDiveListTwo.size();
        }

        @Override
        public Object getItem(int position) {
            return mDbDiveListTwo.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
            try {
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(mDbDiveListTwo.get(position).getUtcTime());
                mTextViewCategoryName.setText("Dive " + mDbDiveListTwo.get(position).getDiveId() + "_" + mSimpleDateFormat.format(mCalendar.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
                mTextViewCategoryName.setText("Dive " + mDbDiveListTwo.get(position).getDiveId());
            }
            return view;
        }
    }

    /*Device 1 list adapter*/
    public class BleDeviceOneListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDbBleDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDbBleDeviceList == null ? 0 : mDbBleDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
            mTextViewCategoryName.setText(mDbBleDeviceList.get(position).getDeviceName() + "_" + mDbBleDeviceList.get(position).getBleAddress());
            return view;
        }
    }

    /*Device 2 list adapter*/
    public class BleDeviceTwoListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mDbBleDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDbBleDeviceList == null ? 0 : mDbBleDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.raw_autocomplete_textview, parent, false);
            }
            TextView mTextViewCategoryName = (TextView) view.findViewById(R.id.autocomplete_tv_name);
            mTextViewCategoryName.setText(mDbBleDeviceList.get(position).getDeviceName() + "_" + mDbBleDeviceList.get(position).getBleAddress());

            return view;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setVisibility(View.GONE);
    }
}
