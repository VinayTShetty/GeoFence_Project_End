package com.succorfish.installer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoQuestion;
import com.succorfish.installer.helper.Utility;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentHealthNsafety extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    private boolean isFromUnSync=false;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            isFromUnSync = getArguments().getBoolean("mIntent_is_from_un_sync");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_health_and_safety, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_frg_health_n_safety_title));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        return mViewRoot;
    }

    @OnClick(R.id.fragment_health_safety_tv_go)
    public void onGoClick(View mView) {
        if (isAdded()) {
            mActivity.replacesFragment(new FragmentHealthSafetyBase(), true, null, 1);
//            if (!isFromUnSync) {
//
//            }else {
//                mActivity.replacesFragment(new FragmentHealthSafetyBase(), true, null, 1);
//            }

        }
    }

    @OnClick(R.id.fragment_health_safety_tv_review)
    public void onRiskAssistantClick(View mView) {
        if (isAdded()) {
            FragmentViewInstallGuide mFragmentViewInstallGuide = new FragmentViewInstallGuide();
            Bundle mBundle = new Bundle();
            mBundle.putString("intent_title", getResources().getString(R.string.str_health_safety_info));
            mBundle.putString("intent_file_url", "Succorfish_H&S_Risk_Assessment.pdf");
            mBundle.putBoolean("intent_is_landscape", true);
            mActivity.replacesFragment(mFragmentViewInstallGuide, true, mBundle, 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }
}
