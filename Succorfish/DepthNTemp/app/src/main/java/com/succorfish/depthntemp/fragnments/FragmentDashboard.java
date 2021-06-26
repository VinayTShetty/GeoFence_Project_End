package com.succorfish.depthntemp.fragnments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.R;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 16-02-2018.
 */

public class FragmentDashboard extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_dashboard, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mToolbar.setVisibility(View.GONE);
        mActivity.mTextViewTitle.setText(R.string.app_name);
//        mActivity.mImageViewBack.setVisibility(View.GONE);
//        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
//        mActivity.mTextViewAdd.setVisibility(View.GONE);
        return mViewRoot;
    }

    @OnClick(R.id.frg_dashboard_rl_view_data)
    public void onViewDataClick(View mView) {
        if (isAdded()) {
            mActivity.deviceOnePosition = 0;
            mActivity.deviceTwoPosition = 0;
            mActivity.diveOnePosition = 0;
            mActivity.diveTwoPosition = 0;
            mActivity.replacesFragment(new FragmentViewData(), true, null, 1);
        }
    }

    @OnClick(R.id.frg_dashboard_rl_configure_sensor)
    public void onConfigureSensorClick(View mView) {
        if (isAdded()) {
            mActivity.replacesFragment(new FragmentConfigureSensors(), true, null, 1);
        }
    }

    @OnClick(R.id.frg_dashboard_rl_app_setting)
    public void onAppSettingClick(View mView) {
        if (isAdded()) {
            mActivity.replacesFragment(new FragmentSetting(), true, null, 1);
        }
    }

    boolean isDive = true;

    @OnClick(R.id.frg_dashboard_rl_about)
    public void onAboutClick(View mView) {
        if (isAdded()) {
            mActivity.replacesFragment(new FragmentHelp(), true, null, 1);
//            if (isDive) {
//                final TableDive mTableDive = new TableDive();
//                mTableDive.setBleAddress("AABBCCDDEE");
//                mTableDive.setDiveNo(1);
//                mTableDive.setGpsLatitude("25.32");
//                mTableDive.setGpsLongitude("54.21");
//                mTableDive.setUtcTime("123456789");
//                mTableDive.setCreatedAt("12313");
//                mTableDive.setUpdatedAt("45455");
//                new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        System.out.println("DriveID-" + mActivity.mAppRoomDatabase.diveDao().insert(mTableDive));
//                        System.out.println("PressTempSize-" + mActivity.mAppRoomDatabase.tempPressDao().getAllPressTempData().size());
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void agentsCount) {
//                    }
//                }.execute();
//                isDive = false;
//            } else {
//                final TablePressureTemperature mTablePressureTemperature = new TablePressureTemperature();
//                mTablePressureTemperature.setDiveIdFk(1);
//                mTablePressureTemperature.setPressure("12332");
//                mTablePressureTemperature.setTemperature("54564");
//                mTablePressureTemperature.setUtcTime("45645");
//                mTablePressureTemperature.setCreatedAt("13212");
//                mTablePressureTemperature.setUpdatedAt("4545252");
//                new AsyncTask<Void, Void, Void>() {
//                    @Override
//                    protected Void doInBackground(Void... params) {
//                        mActivity.mAppRoomDatabase.tempPressDao().insert(mTablePressureTemperature);
//                        System.out.println("DiveSize-" + mActivity.mAppRoomDatabase.diveDao().getAllDive().size());
//                        return null;
//                    }
//
//                    @Override
//                    protected void onPostExecute(Void agentsCount) {
//                    }
//                }.execute();
//                isDive = true;
//            }
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setVisibility(View.VISIBLE);
    }
}
