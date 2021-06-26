package com.succorfish.installer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentUnsynced extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    RadioRealButtonGroup mSegmentedGroup;
    RadioRealButton mRadioButtonInstall;
    RadioRealButton mRadioButtonInspection;
    RadioRealButton mRadioButtonUninstall;
    FragmentTransaction fragmentTransaction;
    int selectedTab = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_unsynced, container, false);
        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_dashboard_menu_unsynced));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mSegmentedGroup = (RadioRealButtonGroup) mViewRoot
                .findViewById(R.id.fragment_unsynced_segmentedgroup_tab);
        mRadioButtonInstall = (RadioRealButton) mViewRoot
                .findViewById(R.id.fragment_unsynced_segmented_btn_install);
        mRadioButtonInspection = (RadioRealButton) mViewRoot
                .findViewById(R.id.fragment_unsynced_segmented_btn_inspection);
        mRadioButtonUninstall = (RadioRealButton) mViewRoot
                .findViewById(R.id.fragment_unsynced_segmented_btn_uninstall);
        if (selectedTab == 0) {
            mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            replacesFragment(new FragmentUnsyncedInstall(), false, null, 1);
        } else if (selectedTab == 1) {
            mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            replacesFragment(new FragmentUnsyncedUnInstall(), false, null, 1);
        } else if (selectedTab == 2) {
            mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            replacesFragment(new FragmentUnsyncedInspection(), false, null, 1);
        }

        mSegmentedGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if (position == 0) {
                    selectedTab = 0;
                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    replacesFragment(new FragmentUnsyncedInstall(), false, null, 1);
                } else if (position == 1) {
                    selectedTab = 1;
                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    replacesFragment(new FragmentUnsyncedUnInstall(), false, null, 1);
                } else if (position == 2) {
                    selectedTab = 2;

                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    replacesFragment(new FragmentUnsyncedInspection(), false, null, 1);
                } else {
                    selectedTab = 0;
                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    replacesFragment( new FragmentUnsyncedInstall(), false, null, 1);
                }
            }
        });

        return mViewRoot;
    }

    public void replacesFragment(Fragment mFragment, boolean isBackState, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState)
            fragmentTransaction.addToBackStack(null);
        if (mBundle != null)
            mFragment.setArguments(mBundle);
        fragmentTransaction.replace(R.id.fragment_unsynced_content_container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }
}
