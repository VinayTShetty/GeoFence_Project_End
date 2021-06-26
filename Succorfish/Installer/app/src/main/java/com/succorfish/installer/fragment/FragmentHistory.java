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

import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentHistory extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
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
        mViewRoot = inflater.inflate(R.layout.fragment_history, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.activity_main_menu_history));
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.VISIBLE);
        mSegmentedGroup = (RadioRealButtonGroup) mViewRoot
                .findViewById(R.id.fragment_history_segmentedgroup_tab);
        mRadioButtonInstall = (RadioRealButton) mViewRoot
                .findViewById(R.id.fragment_history_segmented_btn_install);
        mRadioButtonInspection = (RadioRealButton) mViewRoot
                .findViewById(R.id.fragment_history_segmented_btn_inspection);
        mRadioButtonUninstall = (RadioRealButton) mViewRoot
                .findViewById(R.id.fragment_history_segmented_btn_uninstall);

        /*History open tab based on selection - install history, uninstall history and inspection history.*/
        if (selectedTab == 0) {
            mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            FragmentHistoryInstall mFragmentHistoryInstall = new FragmentHistoryInstall();
            replacesFragment(mFragmentHistoryInstall, false, null, 1);
        } else if (selectedTab == 1) {
            mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            FragmentHistoryUnInstall mFragmentHistoryUnInstall = new FragmentHistoryUnInstall();
            replacesFragment(mFragmentHistoryUnInstall, false, null, 1);
        } else if (selectedTab == 2) {
            mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
            FragmentHistoryInspection mFragmentHistoryInspection = new FragmentHistoryInspection();
            replacesFragment(mFragmentHistoryInspection, false, null, 1);
        }
        mSegmentedGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                if (position == 0) {
                    selectedTab = 0;
                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    FragmentHistoryInstall mFragmentHistoryInstall = new FragmentHistoryInstall();
                    replacesFragment(mFragmentHistoryInstall, false, null, 1);
                } else if (position == 1) {
                    selectedTab = 1;
                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    FragmentHistoryUnInstall mFragmentHistoryUnInstall = new FragmentHistoryUnInstall();
                    replacesFragment(mFragmentHistoryUnInstall, false, null, 1);
                } else if (position == 2) {
                    selectedTab = 2;
                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    FragmentHistoryInspection mFragmentHistoryInspection = new FragmentHistoryInspection();
                    replacesFragment(mFragmentHistoryInspection, false, null, 1);
                } else {
                    selectedTab = 0;
                    mRadioButtonInstall.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
                    mRadioButtonInspection.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    mRadioButtonUninstall.setBackgroundColor(getResources().getColor(R.color.radio_button_deselected_color));
                    FragmentHistoryInstall mFragmentHistoryInstall = new FragmentHistoryInstall();
                    replacesFragment(mFragmentHistoryInstall, false, null, 1);
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
        fragmentTransaction.replace(R.id.fragment_history_content_container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
