package com.vithamastech.smartlight.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class FragmentReset extends Fragment {

    public static final String TAG = FragmentPowerSocketSetting.class.getSimpleName();
    MainActivity activity;
    private Unbinder unbinder;
    @BindView(R.id.linearLayoutSmartLightReset)
    LinearLayout linearLayoutSmartLightReset;
    @BindView(R.id.linearLayoutPowerSocketReset)
    LinearLayout linearLayoutPowerSocketReset;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_reset, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        unbinder = ButterKnife.bind(this, view);

        activity.mTextViewTitle.setText(R.string.header_Fragment_PowerSocket_setting);
        activity.mImageViewBack.setVisibility(View.GONE);
        activity.mImageViewAddDevice.setVisibility(View.VISIBLE);
        activity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        activity.showBackButton(true);
        activity.mImageViewAddDevice.setVisibility(View.INVISIBLE);
        activity.mTextViewAdd.setVisibility(View.VISIBLE);
        activity.mTextViewAdd.setVisibility(View.GONE);
    }

    @OnClick(R.id.linearLayoutSmartLightReset)
    public void onSmartLightReset(View view) {
        FragmentResetSmartLight fragmentResetSmartLight = new FragmentResetSmartLight();
        activity.replacesFragment(fragmentResetSmartLight, true, null, 0);
    }

    @OnClick(R.id.linearLayoutPowerSocketReset)
    public void onPowerSocketReset(View view) {
        FragmentResetPowerSocket fragmentResetPowerSocket = new FragmentResetPowerSocket();
        activity.replacesFragment(fragmentResetPowerSocket, true, null, 0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (unbinder != null) {
            unbinder.unbind();
        }
    }
}