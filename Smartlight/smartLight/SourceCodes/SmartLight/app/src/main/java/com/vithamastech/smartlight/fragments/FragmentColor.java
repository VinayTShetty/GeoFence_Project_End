package com.vithamastech.smartlight.fragments;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

import static android.app.Activity.RESULT_OK;


public class FragmentColor extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    FragmentTransaction fragmentTransaction;
    @BindView(R.id.fragment_color_btn_wheel)
    TextView mRadioButtonWheel;
    @BindView(R.id.fragment_color_btn_pattern)
    TextView mRadioButtonPatterns;
    @BindView(R.id.fragment_color_btn_voice)
    TextView mRadioButtonColorVoice;
    @BindView(R.id.fragment_color_btn_white)
    TextView mRadioButtonColorWhite;
    @BindView(R.id.fragment_color_btn_rgb)
    TextView mRadioButtonColorRGB;

    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;
    int mIntRandomNo = 0;
    String mStringLocalId = "";
    String mStringServerId = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mIntRandomNo = getArguments().getInt("intent_device_id", 0);
            mIsFromGroup = getArguments().getBoolean("intent_from_group", false);
            mIsFromAllGroup = getArguments().getBoolean("intent_from_all_group", false);
            if (!mIsFromAllGroup) {
                mStringLocalId = getArguments().getString("intent_local_id");
                mStringServerId = getArguments().getString("intent_server_id");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_color, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        setSelectedMenu(0);
        return mViewRoot;
    }

    @OnClick(R.id.fragment_color_btn_wheel)
    public void onWheelButtonClick(View mView) {
        if (isAdded()) {
            setSelectedMenu(0);
        }
    }

    @OnClick(R.id.fragment_color_btn_pattern)
    public void onPatternButtonClick(View mView) {
        if (isAdded()) {
            setSelectedMenu(1);
        }
    }

    @OnClick(R.id.fragment_color_btn_white)
    public void onColorWhiteButtonClick(View mView) {
        if (isAdded()) {
            setSelectedMenu(2);
        }
    }

    @OnClick(R.id.fragment_color_btn_voice)
    public void onVoiceButtonClick(View mView) {
        if (isAdded()) {
            setSelectedMenu(3);
        }
    }

    @OnClick(R.id.fragment_color_btn_rgb)
    public void onRGBButtonClick(View mView) {
        if (isAdded()) {
            setSelectedMenu(4);
        }
    }

    /*Change color tab based on menu*/
    private void setSelectedMenu(int position) {

        if (position == 0) {

            FragmentColorWheel mFragmentColorWheel = new FragmentColorWheel();
            Bundle mBundle = new Bundle();
            if (mIsFromGroup) {
                if (mIsFromAllGroup) {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                } else {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
            } else {
                mBundle.putInt("intent_device_id", mIntRandomNo);
                mBundle.putString("intent_server_id", mStringServerId);
                mBundle.putString("intent_local_id", mStringLocalId);
            }
            mBundle.putBoolean("intent_from_group", mIsFromGroup);
            mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
            replacesFragment(mFragmentColorWheel, false, mBundle, 1);

            mRadioButtonWheel.setTextColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonPatterns.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorWhite.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorVoice.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorRGB.setTextColor(getResources().getColor(R.color.child_menu_unselected));


        } else if (position == 1) {
            if (getChildFragmentManager().findFragmentById(R.id.fragment_color_content_container) instanceof FragmentColorFavourite) {

            } else {
                FragmentColorFavourite mFragmentColorPattern = new FragmentColorFavourite();
                Bundle mBundle = new Bundle();
                if (mIsFromGroup) {
                    if (mIsFromAllGroup) {
                        mBundle.putInt("intent_device_id", mIntRandomNo);
                    } else {
                        mBundle.putInt("intent_device_id", mIntRandomNo);
                        mBundle.putString("intent_server_id", mStringServerId);
                        mBundle.putString("intent_local_id", mStringLocalId);
                    }
                } else {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
                mBundle.putBoolean("intent_from_group", mIsFromGroup);
                mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
                replacesFragment(mFragmentColorPattern, false, mBundle, 1);

                mRadioButtonWheel.setTextColor(getResources().getColor(R.color.child_menu_unselected));
                mRadioButtonPatterns.setTextColor(getResources().getColor(R.color.colorWhite));
                mRadioButtonColorWhite.setTextColor(getResources().getColor(R.color.child_menu_unselected));
                mRadioButtonColorVoice.setTextColor(getResources().getColor(R.color.child_menu_unselected));
                mRadioButtonColorRGB.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            }
        } else if (position == 2) {
            FragmentColorWhite mFragmentColorWhite = new FragmentColorWhite();
            Bundle mBundle = new Bundle();
            if (mIsFromGroup) {
                if (mIsFromAllGroup) {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                } else {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
            } else {
                mBundle.putInt("intent_device_id", mIntRandomNo);
                mBundle.putString("intent_server_id", mStringServerId);
                mBundle.putString("intent_local_id", mStringLocalId);
            }
            mBundle.putBoolean("intent_from_group", mIsFromGroup);
            mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
            replacesFragment(mFragmentColorWhite, false, mBundle, 1);

            mRadioButtonWheel.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonPatterns.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorWhite.setTextColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonColorVoice.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorRGB.setTextColor(getResources().getColor(R.color.child_menu_unselected));


        } else if (position == 3) {
            FragmentColorVoice mFragmentColorVoice = new FragmentColorVoice();
            Bundle mBundle = new Bundle();
            if (mIsFromGroup) {
                if (mIsFromAllGroup) {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                } else {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
            } else {
                mBundle.putInt("intent_device_id", mIntRandomNo);
                mBundle.putString("intent_server_id", mStringServerId);
                mBundle.putString("intent_local_id", mStringLocalId);
            }
            mBundle.putBoolean("intent_from_group", mIsFromGroup);
            mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
            replacesFragment(mFragmentColorVoice, false, mBundle, 1);

            mRadioButtonWheel.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonPatterns.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorWhite.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorVoice.setTextColor(getResources().getColor(R.color.colorWhite));
            mRadioButtonColorRGB.setTextColor(getResources().getColor(R.color.child_menu_unselected));


        } else if (position == 4) {
            FragmentColorCustomRGB mFragmentColorCustomRGB = new FragmentColorCustomRGB();
            Bundle mBundle = new Bundle();
            if (mIsFromGroup) {
                if (mIsFromAllGroup) {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                } else {
                    mBundle.putInt("intent_device_id", mIntRandomNo);
                    mBundle.putString("intent_server_id", mStringServerId);
                    mBundle.putString("intent_local_id", mStringLocalId);
                }
            } else {
                mBundle.putInt("intent_device_id", mIntRandomNo);
                mBundle.putString("intent_server_id", mStringServerId);
                mBundle.putString("intent_local_id", mStringLocalId);
            }
            mBundle.putBoolean("intent_from_group", mIsFromGroup);
            mBundle.putBoolean("intent_from_all_group", mIsFromAllGroup);
            replacesFragment(mFragmentColorCustomRGB, false, mBundle, 1);

            mRadioButtonWheel.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonPatterns.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorWhite.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorVoice.setTextColor(getResources().getColor(R.color.child_menu_unselected));
            mRadioButtonColorRGB.setTextColor(getResources().getColor(R.color.colorWhite));

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (resultCode == RESULT_OK && data != null) {
                Fragment fragment = getChildFragmentManager().findFragmentById(R.id.fragment_color_content_container);
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void replacesFragment(Fragment mFragment, boolean isBackState, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getChildFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState)
            fragmentTransaction.addToBackStack(null);
        if (mBundle != null)
            mFragment.setArguments(mBundle);
        System.gc();
        fragmentTransaction.replace(R.id.fragment_color_content_container, mFragment);
        fragmentTransaction.commitAllowingStateLoss();
    }
}
