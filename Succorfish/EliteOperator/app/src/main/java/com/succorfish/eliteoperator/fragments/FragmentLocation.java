package com.succorfish.eliteoperator.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

import com.succorfish.eliteoperator.MainActivity;

import butterknife.Unbinder;

public class FragmentLocation extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

}
