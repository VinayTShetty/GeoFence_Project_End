package com.succorfish.depthntemp.fragnments;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.vo.VoHeatMap;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentHelp extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_help_textview_email)
    TextView mTextViewEmail;
    @BindView(R.id.fragment_help_textview_call_no)
    TextView mTextViewCallNo;
    private static final int REQUEST_CALL = 1;
    Intent callIntent;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_help, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mToolbar.setVisibility(View.VISIBLE);
        mActivity.mTextViewTitle.setText(R.string.str_menu_about);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);

        return mViewRoot;
    }

    @OnClick(R.id.fragment_help_linearlayout_email)
    public void onEmailClick(View mView) {
        if (isAdded()) {
            try {
                Intent sendIntent = new Intent(Intent.ACTION_SEND);
                sendIntent.setData(Uri.parse("mailto:"));
                sendIntent.setType("text/plain");
                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mTextViewEmail.getText().toString().trim()});
                if (sendIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                    startActivity(sendIntent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.fragment_help_linearlayout_call)
    public void onCallClick(View mView) {
        if (isAdded()) {
            String mStringUserPhone = mTextViewCallNo.getText().toString().trim();
            System.out.println("mStringUserPhone-" + mStringUserPhone);
            if (mStringUserPhone != null && !mStringUserPhone.equals("")) {
                mStringUserPhone = mStringUserPhone.replace("-", "");
                mStringUserPhone = mStringUserPhone.replace(" ", "");
                callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setData(Uri.parse("tel:" + mStringUserPhone));
                if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                } else {
                    startActivity(callIntent);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startActivity(callIntent);
                }
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
