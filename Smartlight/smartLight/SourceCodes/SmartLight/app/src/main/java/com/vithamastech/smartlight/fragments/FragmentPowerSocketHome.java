package com.vithamastech.smartlight.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.vithamastech.smartlight.BaseFragment.BaseFragment;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.console;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.ceryle.radiorealbutton.RadioRealButton;
import co.ceryle.radiorealbutton.RadioRealButtonGroup;

import static com.vithamastech.smartlight.MainActivity.mActivityPowerSocketSelected;

public class FragmentPowerSocketHome extends BaseFragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_socket_control_home)
    RadioRealButtonGroup mSegmentedGroup;
    @BindView(R.id.fragment_socket_control)
    RadioRealButton radioButtonSocketControl;
    @BindView(R.id.fragment_socket_settings)
    RadioRealButton radioButtonSocketSettings;
    FragmentTransaction fragmentTransaction;
    private boolean isPowerSocketControlOpen = true;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_socket_home, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(mActivityPowerSocketSelected.bleName);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.mTextViewSubTitle.setVisibility(View.GONE);
        mActivity.showBackButton(true);

        if(isPowerSocketControlOpen){
            openFragment(0);
        }else {
            openFragment(1);
        }
        mSegmentedGroup.setOnClickedButtonListener((button, position) -> openFragment(position));
        return mViewRoot;
    }

    private void openFragment(int position) {
        if(position==0){
            radioButtonSocketControl.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            radioButtonSocketControl.setTextColor(getResources().getColor(R.color.colorWhite));
            /**
             * Setting Tab UI
             */
            radioButtonSocketSettings.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            radioButtonSocketSettings.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            isPowerSocketControlOpen = true;
            Fragment mFragment = getChildFragmentManager().findFragmentById(R.id.fragment_socket_home_container);
            if(mFragment instanceof FragmentPowerSocketControl){
                return;
            }
            replacesFragment(new FragmentPowerSocketControl());
        }else if(position==1){
            isPowerSocketControlOpen = false;
            radioButtonSocketSettings.setBackgroundColor(getResources().getColor(R.color.radio_button_selected_color));
            radioButtonSocketSettings.setTextColor(getResources().getColor(R.color.colorWhite));
            /**
             * Setting Tab UI
             */
            radioButtonSocketControl.setBackgroundColor(getResources().getColor(R.color.colorWhite));
            radioButtonSocketControl.setTextColor(getResources().getColor(R.color.radio_button_selected_color));
            Fragment mFragment = getChildFragmentManager().findFragmentById(R.id.fragment_socket_home_container);
            if(mFragment instanceof FragmentPowerSocketSetting){
                return;
            }
            replacesFragment(new FragmentPowerSocketSetting());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewSubTitle.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mTextViewSubTitle.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }


    private void replacesFragment(Fragment mFragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStackImmediate();
        }

        fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_socket_home_container, mFragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
}