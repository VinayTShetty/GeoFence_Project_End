package com.succorfish.depthntemp.fragnments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MapsActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.helper.PreferenceHelper;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 16-02-2018.
 */

public class FragmentViewDataCompare extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_compare_data_smart_tab)
    TabLayout mSmartTabLayout;
    @BindView(R.id.frg_compare_data_ll_dive)
    LinearLayout mLinearLayoutMainDive;
    @BindView(R.id.frg_compare_data_ll_date_time)
    LinearLayout mLinearLayoutMainTime;
    @BindView(R.id.frg_compare_data_ll_dive_two)
    LinearLayout mLinearLayoutDiveTwo;
    @BindView(R.id.frg_compare_data_ll_date_time_two)
    RelativeLayout mRelativeLayoutTimeTwo;

    @BindView(R.id.frg_compare_data_tv_lbl_ble_dive_one)
    TextView mTextViewLblDiveOne;
    @BindView(R.id.frg_compare_data_tv_lbl_ble_device_one)
    TextView mTextViewLblBleDeviceOne;

    @BindView(R.id.frg_compare_data_tv_ble_device_one)
    TextView mTextViewBleDeviceOne;
    @BindView(R.id.frg_compare_data_tv_ble_dive_one)
    TextView mTextViewDiveOne;
    @BindView(R.id.frg_compare_data_tv_ble_device_two)
    TextView mTextViewBleDeviceTwo;
    @BindView(R.id.frg_compare_data_tv_ble_dive_two)
    TextView mTextViewDiveTwo;

    @BindView(R.id.frg_compare_data_tv_lbl_date_time_one)
    TextView mTextViewLblTimeOne;
    @BindView(R.id.frg_compare_data_tv_date_time_one)
    TextView mTextViewDateTimeOne;
    @BindView(R.id.frg_compare_data_tv_date_time_two)
    TextView mTextViewDateTimeTwo;


    private FragmentCompareRawData mFragmentCompareRawData;
    private FragmentDeviceMissionDetail mFragmentDeviceMissionDetail;
    private FragmentCompareHeatMap mFragmentCompareHeatMap;
    boolean isSingleDataSelect = true;
    boolean isDiveDataSelect = true;
    String mStrDeviceOneBleAdd = "";
    String mStrDeviceTwoBleAdd = "";
    String mStrDeviceOneName = "";
    String mStrDeviceTwoName = "";
    String mStrDiveOneId = "";
    String mStrDiveTwoId = "";
    String mStrDiveOneName = "";
    String mStrDiveTwoName = "";
    String mStrStartDateTime = "";
    String mStrEndDateTime = "";
    String mStrDiveOneLatitude = "";
    String mStrDiveTwoLatitude = "";
    String mStrDiveOneLongitude = "";
    String mStrDiveTwoLongitude = "";

    private SimpleDateFormat mSimpleDateFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();

        if (getArguments() != null) {
            isSingleDataSelect = getArguments().getBoolean("mIntent_Is_Single_Data");
            isDiveDataSelect = getArguments().getBoolean("mIntent_Is_Dive_Data");
            mStrDeviceOneBleAdd = getArguments().getString("mIntent_device_one_ble");
            mStrDeviceTwoBleAdd = getArguments().getString("mIntent_device_two_ble");
            mStrDeviceOneName = getArguments().getString("mIntent_device_one_name");
            mStrDeviceTwoName = getArguments().getString("mIntent_device_two_name");
            mStrDiveOneId = getArguments().getString("mIntent_dive_one_id");
            mStrDiveTwoId = getArguments().getString("mIntent_dive_two_id");
            mStrDiveOneName = getArguments().getString("mIntent_dive_one_name");
            mStrDiveTwoName = getArguments().getString("mIntent_dive_two_name");
            mStrStartDateTime = getArguments().getString("mIntent_start_date_time");
            mStrEndDateTime = getArguments().getString("mIntent_end_date_time");
            mStrDiveOneLatitude = getArguments().getString("mIntent_dive_one_latitude");
            mStrDiveOneLongitude = getArguments().getString("mIntent_dive_one_longitude");
            mStrDiveTwoLatitude = getArguments().getString("mIntent_dive_two_latitude");
            mStrDiveTwoLongitude = getArguments().getString("mIntent_dive_two_longitude");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_compare_view_data, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mToolbar.setVisibility(View.VISIBLE);
        mActivity.mTextViewTitle.setText(R.string.str_compare_data_title);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_map_marker_white_18dp);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mSimpleDateFormat = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm:ss", Locale.getDefault());

        if (isDiveDataSelect) {
            mLinearLayoutMainDive.setVisibility(View.VISIBLE);
            mLinearLayoutMainTime.setVisibility(View.GONE);
            mTextViewBleDeviceOne.setText(mStrDeviceOneName);
            mTextViewDiveOne.setText(mStrDiveOneName);
            if (isSingleDataSelect) {
                mLinearLayoutDiveTwo.setVisibility(View.GONE);
                mTextViewLblDiveOne.setText(getResources().getText(R.string.str_dives));
                mTextViewLblBleDeviceOne.setText(getResources().getText(R.string.str_device));
            } else {
                mLinearLayoutDiveTwo.setVisibility(View.VISIBLE);
                mTextViewLblDiveOne.setText(getResources().getText(R.string.str_dive_one));
                mTextViewLblBleDeviceOne.setText(getResources().getText(R.string.str_device_one));
                mTextViewBleDeviceTwo.setText(mStrDeviceTwoName);
                mTextViewDiveTwo.setText(mStrDiveTwoName);
            }
        } else {
            mLinearLayoutMainDive.setVisibility(View.GONE);
            mLinearLayoutMainTime.setVisibility(View.VISIBLE);
            try {
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(Long.parseLong(mStrStartDateTime));
                mTextViewDateTimeOne.setText(mSimpleDateFormat.format(mCalendar.getTime()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (isSingleDataSelect) {
                mTextViewLblTimeOne.setText(getResources().getText(R.string.str_date));
                mRelativeLayoutTimeTwo.setVisibility(View.GONE);
            } else {
                mRelativeLayoutTimeTwo.setVisibility(View.VISIBLE);
                mTextViewLblTimeOne.setText(getResources().getText(R.string.str_date_one));
                try {
                    Calendar mCalendar = Calendar.getInstance();
                    mCalendar.setTimeInMillis(Long.parseLong(mStrEndDateTime));
                    mTextViewDateTimeTwo.setText(mSimpleDateFormat.format(mCalendar.getTime()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }


        mSmartTabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
        setupTabLayout();
        mActivity.mImageViewAddDevice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (mStrDiveOneLatitude.contains(".")) {
                        mStrDiveOneLatitude = mStrDiveOneLatitude.replace(".", "");
                    }
                    if (mStrDiveOneLongitude.contains(".")) {
                        mStrDiveOneLongitude = mStrDiveOneLongitude.replace(".", "");
                    }
                    if (mStrDiveTwoLatitude.contains(".")) {
                        mStrDiveTwoLatitude = mStrDiveTwoLatitude.replace(".", "");
                    }
                    if (mStrDiveTwoLongitude.contains(".")) {
                        mStrDiveTwoLongitude = mStrDiveTwoLongitude.replace(".", "");
                    }

                    String mStrLat = getLocationCalculation(Integer.parseInt(mStrDiveOneLatitude));
                    String mStrLong = getLocationCalculation(Integer.parseInt(mStrDiveOneLongitude));
                    String mStrLat1 = getLocationCalculation(Integer.parseInt(mStrDiveTwoLatitude));
                    String mStrLong1 = getLocationCalculation(Integer.parseInt(mStrDiveTwoLongitude));

                    if (mStrLat.equalsIgnoreCase("0") && mStrLong.equalsIgnoreCase("0")) {
                        mActivity.mUtility.errorDialog("Gps positional data not found", 0);
                        return;
                    }
                    if (!isSingleDataSelect) {
                        if (mStrLat1.equalsIgnoreCase("0") && mStrLong1.equalsIgnoreCase("0")) {
                            mActivity.mUtility.errorDialog("Gps positional data not found", 0);
                            return;
                        }
                    }

                    Intent mIntent = new Intent(mActivity, MapsActivity.class);
                    mIntent.putExtra("mIntent_latitude_1", mStrLat);
                    mIntent.putExtra("mIntent_longitude_1", mStrLong);
                    mIntent.putExtra("mIntent_latitude_2", mStrLat1);
                    mIntent.putExtra("mIntent_longitude_2", mStrLong1);
                    mIntent.putExtra("mIntent_location_1_title", mStrDiveOneName);
                    mIntent.putExtra("mIntent_location_2_title", mStrDiveTwoName);
                    mIntent.putExtra("mIntent_is_single_location", isSingleDataSelect);
                    startActivity(mIntent);
//                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:12.3431691, 76.6165882?q=12.339607,76.6123883(JD TEST)?q=12.3431691,76.6165882(JD TEST 111)"));
//                startActivity(intent);
//                String mStringUri="http://maps.google.com/maps?q=12.3431691, 76.6165882(labelLocation)?q=12.3431691, 76.6165882(labelLocation)&iwloc=A&hl=es";
////                String mStringUri=String.format(Locale.ENGLISH, "http://maps.google.com/maps?daddr=12.3431691,76.6165882 (%s)","Where the party is at");
//                Uri gmmIntentUri = Uri.parse(mStringUri);
//                Intent intent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                intent.setPackage("com.google.android.apps.maps");
//                try {
//                    startActivity(intent);
//                } catch (ActivityNotFoundException ex) {
//                    try {
//                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                        startActivity(unrestrictedIntent);
//                    } catch (ActivityNotFoundException innerEx) {
//                        Toast.makeText(mActivity, "Please install a maps application", Toast.LENGTH_LONG).show();
//                    }
//                }
                } catch (Exception e) {
                    mActivity.mUtility.errorDialog("Gps positional data not found", 0);
                    e.printStackTrace();
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

    private void setupTabLayout() {
//        mFragmentCompareRawData = new FragmentCompareRawData();
//        mFragmentDeviceMissionDetail = new FragmentDeviceMissionDetail();
        mSmartTabLayout.addTab(mSmartTabLayout.newTab().setText(getResources().getString(R.string.str_raw_data)), true);
        mSmartTabLayout.addTab(mSmartTabLayout.newTab().setText(getResources().getString(R.string.str_line)));
        mSmartTabLayout.addTab(mSmartTabLayout.newTab().setText(getResources().getString(R.string.str_heat_map)));
    }

    private void setCurrentTabFragment(int tabPosition) {
        switch (tabPosition) {
            case 0:
                mFragmentCompareRawData = new FragmentCompareRawData();
                Bundle mBundle = new Bundle();
                mBundle.putBoolean("mBundle_Is_Single_Data", isSingleDataSelect);
                mBundle.putBoolean("mBundle_Is_Dive_Data", isDiveDataSelect);
                mBundle.putString("mBundle_device_one_ble", mStrDeviceOneBleAdd);
                mBundle.putString("mBundle_device_two_ble", mStrDeviceTwoBleAdd);
                mBundle.putString("mBundle_dive_one_id", mStrDiveOneId);
                mBundle.putString("mBundle_dive_two_id", mStrDiveTwoId);
                mBundle.putString("mBundle_start_date_time", mStrStartDateTime);
                mBundle.putString("mBundle_end_date_time", mStrEndDateTime);
                mFragmentCompareRawData.setArguments(mBundle);
                replaceFragment(mFragmentCompareRawData);
                break;
            case 1:
                mFragmentDeviceMissionDetail = new FragmentDeviceMissionDetail();
                Bundle mBundle1 = new Bundle();
                mBundle1.putBoolean("mBundle_Is_Single_Data", isSingleDataSelect);
                mBundle1.putBoolean("mBundle_Is_Dive_Data", isDiveDataSelect);
                mBundle1.putString("mBundle_device_one_ble", mStrDeviceOneBleAdd);
                mBundle1.putString("mBundle_device_two_ble", mStrDeviceTwoBleAdd);
                mBundle1.putString("mBundle_dive_one_id", mStrDiveOneId);
                mBundle1.putString("mBundle_dive_two_id", mStrDiveTwoId);
                mBundle1.putString("mBundle_start_date_time", mStrStartDateTime);
                mBundle1.putString("mBundle_end_date_time", mStrEndDateTime);
                mFragmentDeviceMissionDetail.setArguments(mBundle1);
                replaceFragment(mFragmentDeviceMissionDetail);
                break;
            case 2:
                mFragmentCompareHeatMap = new FragmentCompareHeatMap();
                Bundle mBundle2 = new Bundle();
                mBundle2.putBoolean("mBundle_Is_Single_Data", isSingleDataSelect);
                mBundle2.putBoolean("mBundle_Is_Dive_Data", isDiveDataSelect);
                mBundle2.putString("mBundle_device_one_ble", mStrDeviceOneBleAdd);
                mBundle2.putString("mBundle_device_two_ble", mStrDeviceTwoBleAdd);
                mBundle2.putString("mBundle_dive_one_id", mStrDiveOneId);
                mBundle2.putString("mBundle_dive_two_id", mStrDiveTwoId);
                mBundle2.putString("mBundle_start_date_time", mStrStartDateTime);
                mBundle2.putString("mBundle_end_date_time", mStrEndDateTime);
                mFragmentCompareHeatMap.setArguments(mBundle2);
                replaceFragment(mFragmentCompareHeatMap);
                break;
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getChildFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.frg_compare_data_fl_container, fragment);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.commit();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setImageResource(R.drawable.ic_sync);

    }


}
