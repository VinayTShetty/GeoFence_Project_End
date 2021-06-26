package com.vithamastech.smartlight.fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

/**
 * Created by Jaydeep on 22-12-2017.
 */

public class FragmentHome extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    @BindView(R.id.fragment_home_segmentedgroup_tab)
    RadioRealButtonGroup mSegmentedGroup;
    @BindView(R.id.fragment_home_segmented_btn_devices)
    RadioRealButton mRadioButtonDevices;
    @BindView(R.id.fragment_home_segmented_btn_groups)
    RadioRealButton mRadioButtonGroups;

    FragmentTransaction fragmentTransaction;
    private boolean isDeviceTabActive = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.str_dashboard_title);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
//        mActivity.mImageViewConnectionStatus.setVisibility(View.VISIBLE);
        mActivity.mTextViewSubTitle.setVisibility(View.GONE);
        mActivity.showBackButton(false);
        mActivity.mPreferenceHelper.setIsDeviceSync(false);
        if (isDeviceTabActive) {
            openFragment(0);
        } else {
            openFragment(1);
        }
        mSegmentedGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                openFragment(position);
            }
        });
        return mViewRoot;
    }

    /*Open Device or group screen*/
    private void openFragment(int position) {
        if (position == 0) {
            mRadioButtonDevices.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonGroups.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonDevices.setTextColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonGroups.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            isDeviceTabActive = true;
//                    mRadioButtonDevices.requestLayout();
            Fragment mFragment = getChildFragmentManager().findFragmentById(R.id.fragment_home_content_container);
            if (mFragment instanceof FragmentDevices) {
                return;
            }
            replacesFragment(new FragmentDevices());
        } else {
            isDeviceTabActive = false;
            mRadioButtonDevices.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonGroups.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonDevices.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonGroups.setTextColor(getResources().getColor(R.color.colorWhite));
//                    mRadioButtonGroups.requestLayout();
            Fragment mFragment = getChildFragmentManager().findFragmentById(R.id.fragment_home_content_container);
            if (mFragment instanceof FragmentGroups) {
                return;
            }
            replacesFragment(new FragmentGroups());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.showBackButton(false);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewSubTitle.setVisibility(View.GONE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mTextViewSubTitle.setVisibility(View.GONE);
//        mActivity.mImageViewConnectionStatus.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
    }


    public void replacesFragment(Fragment mFragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStackImmediate();
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_home_content_container, mFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}
