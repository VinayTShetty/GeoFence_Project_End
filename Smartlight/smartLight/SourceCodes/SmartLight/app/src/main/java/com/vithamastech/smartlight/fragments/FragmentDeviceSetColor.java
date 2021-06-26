package com.vithamastech.smartlight.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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
import com.vithamastech.smartlight.console;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

import static android.app.Activity.RESULT_OK;

/**
 * Created by Jaydeep on 25-12-2017.
 */

public class FragmentDeviceSetColor extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    FragmentTransaction fragmentTransaction;
    @BindView(R.id.fragment_device_set_color_segmentedgroup_tab)
    RadioRealButtonGroup mSegmentedGroup;
    @BindView(R.id.fragment_device_set_color_segmented_btn_color)
    RadioRealButton mRadioButtonColor;
    @BindView(R.id.fragment_device_set_color_segmented_btn_patterns)
    RadioRealButton mRadioButtonPattern;
    @BindView(R.id.fragment_device_set_color_segmented_btn_music)
    RadioRealButton mRadioButtonMusic;
    @BindView(R.id.fragment_device_set_color_segmented_btn_smartlight_settings)
    RadioRealButton mRadioButtonSmartLightSettings;

    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;
    boolean mIsDeviceSwitchOn = false;
    String mStringDeviceName = "";
    String mStringDeviceCommId = "";
    String mStringLocalId = "";
    String mStringServerId = "";
    String mStringBLEAddress = "";
    int mIntRandomNo = 0;

    int selectedPosition = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        console.log("kjsbkjsbksjbskjbkjs", "here");
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        console.log("kjsbkjsbksjbskjbkjs", "here");

        mViewRoot = inflater.inflate(R.layout.fragment_device_set_color, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity = (MainActivity) getActivity();
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mSwitchCompatOnOff.setVisibility(View.VISIBLE);
        mActivity.showBackButton(true);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.isAddDeviceScan = false;
        mActivity.mCurrentColorWheel = Color.rgb(255, 255, 255);
        mActivity.mCurrentColorWhiteLight = Color.rgb(255, 255, 255);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        if (getArguments() != null) {
            mIsFromGroup = getArguments().getBoolean("intent_is_from_group", false);
            mIsFromAllGroup = getArguments().getBoolean("intent_is_from_all_group", false);
            if (!mIsFromAllGroup) {
                mIsDeviceSwitchOn = getArguments().getBoolean("intent_is_turn_on", false);
                mStringDeviceName = getArguments().getString("intent_group_name");
                mStringDeviceCommId = getArguments().getString("intent_comm_id");
                mStringLocalId = getArguments().getString("intent_local_id");
                mStringServerId = getArguments().getString("intent_server_id");
                mStringBLEAddress = getArguments().getString("intent_ble_address");
                if (!mIsFromGroup) {
                    mActivity.mCurrentColorWheel = getArguments().getInt("intent_device_color", -1);
                }
            }
        }
        mActivity.mSwitchCompatOnOff.setOnCheckedChangeListener(null);
        if (!mIsFromAllGroup && !mIsFromGroup) {

        }
        if (mIsFromGroup) {
            if (mIsFromAllGroup) {
                if (mActivity.mPreferenceHelper.getIsAllDeviceOn()) {
                    mActivity.mSwitchCompatOnOff.setChecked(true);
                } else {
                    mActivity.mSwitchCompatOnOff.setChecked(false);
                }
                mActivity.mTextViewTitle.setText("All Group");
            } else {
                if (mStringDeviceName != null && !mStringDeviceName.equalsIgnoreCase("")) {
                    mActivity.mTextViewTitle.setText(mStringDeviceName);
                }
                if (mIsDeviceSwitchOn) {
                    mActivity.mSwitchCompatOnOff.setChecked(true);
                } else {
                    mActivity.mSwitchCompatOnOff.setChecked(false);
                }
            }
        } else {
            if (mStringDeviceName != null && !mStringDeviceName.equalsIgnoreCase("")) {
                mActivity.mTextViewTitle.setText(mStringDeviceName);
            }
            if (mIsDeviceSwitchOn) {
                mActivity.mSwitchCompatOnOff.setChecked(true);
            } else {
                mActivity.mSwitchCompatOnOff.setChecked(false);
            }
        }

        selectedPosition = 3;
        selectFragment(selectedPosition);

        /*Tab change click listener*/
        mSegmentedGroup.setOnClickedButtonListener(new RadioRealButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(RadioRealButton button, int position) {
                selectedPosition = position;
                selectFragment(selectedPosition);
            }
        });

        return mViewRoot;
    }

    private void selectFragment(int selectedPosition) {
        if (selectedPosition == 0) {
            console.log("sxaxasasxasxasx", selectedPosition);
            mRadioButtonColor.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonColor.setTextColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonPattern.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonPattern.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonMusic.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonMusic.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonSmartLightSettings.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonSmartLightSettings.setTextColor(getResources().getColor(R.color.radio_button_selected_color));

            FragmentColor mFragmentColor = new FragmentColor();
            Bundle mBundle = new Bundle();
            if (mIsFromGroup) {
                if (mIsFromAllGroup) {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                } else {
                    mBundle.putInt("intent_device_id", Integer.parseInt(mStringDeviceCommId));
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
            } else {
                mBundle.putInt("intent_device_id", Integer.parseInt(mStringDeviceCommId));
                mBundle.putString("intent_server_id", mStringServerId);
                mBundle.putString("intent_local_id", mStringLocalId);
            }
            mBundle.putBoolean("intent_from_group", mIsFromGroup);
            mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
            replacesFragment(mFragmentColor, false, mBundle, 1);
        } else if (selectedPosition == 1) {
            console.log("sxaxasasxasxasx", selectedPosition);
            mRadioButtonColor.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonColor.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonPattern.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonPattern.setTextColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonMusic.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonMusic.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonSmartLightSettings.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonSmartLightSettings.setTextColor(getResources().getColor(R.color.radio_button_selected_color));

            FragmentColorPattern mFragmentColorPattern = new FragmentColorPattern();
            Bundle mBundle = new Bundle();
            if (mIsFromGroup) {
                if (mIsFromAllGroup) {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                } else {
                    mBundle.putInt("intent_device_id", Integer.parseInt(mStringDeviceCommId));
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
            } else {
                mBundle.putInt("intent_device_id", Integer.parseInt(mStringDeviceCommId));
                mBundle.putString("intent_server_id", mStringServerId);
                mBundle.putString("intent_local_id", mStringLocalId);
            }
            mBundle.putBoolean("intent_from_group", mIsFromGroup);
            mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
            replacesFragment(mFragmentColorPattern, false, mBundle, 1);

        } else if (selectedPosition == 2) {
            console.log("sxaxasasxasxasx", selectedPosition);
            mRadioButtonColor.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonColor.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonPattern.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonPattern.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonMusic.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonMusic.setTextColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonSmartLightSettings.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonSmartLightSettings.setTextColor(getResources().getColor(R.color.radio_button_selected_color));

            FragmentColorMusic mFragmentMusic = new FragmentColorMusic();
            Bundle mBundle = new Bundle();
            if (mIsFromGroup) {
                if (mIsFromAllGroup) {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                } else {
                    mBundle.putInt("intent_device_id", Integer.parseInt(mStringDeviceCommId));
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
            } else {
                mBundle.putInt("intent_device_id", Integer.parseInt(mStringDeviceCommId));
                mBundle.putString("intent_server_id", mStringServerId);
                mBundle.putString("intent_local_id", mStringLocalId);
            }
            mBundle.putBoolean("intent_from_group", mIsFromGroup);
            mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
            replacesFragment(mFragmentMusic, false, mBundle, 1);
        } else if (selectedPosition == 3) {
            console.log("sxaxasasxasxasx", selectedPosition);
            mRadioButtonColor.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonColor.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonPattern.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonPattern.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonMusic.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonMusic.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonSmartLightSettings.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            mRadioButtonSmartLightSettings.setTextColor(getResources().getColor(R.color.colorWhite));

//            replacesFragment(new FragmentSettings(), true, null, 1);

            Fragment mFragment = getChildFragmentManager().findFragmentById(R.id.fragment_device_set_color_content_container);
            if(mFragment instanceof FragmentSettings){
                return;
            }
            replacesFragment(new FragmentSettings());
        }
    }

    private void replacesFragment(Fragment mFragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStackImmediate();
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_device_set_color_content_container, mFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK && data != null) {
                Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragment_device_set_color_content_container);
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Replace fragment*/
    public void replacesFragment(Fragment mFragment, boolean isBackState, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState)
            fragmentTransaction.addToBackStack(null);
        if (mBundle != null)
            mFragment.setArguments(mBundle);
//        System.gc();
        fragmentTransaction.replace(R.id.fragment_device_set_color_content_container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.RescanDevice(false);
        unbinder.unbind();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mSwitchCompatOnOff.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }
}
