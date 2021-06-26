package com.succorfish.installer.fragment;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.installer.LoginActivity;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.helper.PreferenceHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import me.leolin.shortcutbadger.ShortcutBadger;

/**
 * Created by Jaydeep on 21-02-2018.
 */

public class FragmentSettings extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_settings_relativelayout_main)
    RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.fragment_setting_selected_date_format)
    TextView mTextViewSelectedDate;
    @BindView(R.id.fragment_setting_contact_us)
    TextView mTextViewContactNO;
    String mStringSelectedFormat = "yyyy-MM-dd";
    String[] mListDateFormat = {"yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "dd MMMM yyyy", "MM/dd/yyyy", "yyyy/MM/dd"};
    int mIntSelectedItemPosition = 0;
    int mIntTempSelectedItemPosition = 0;
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
        mViewRoot = inflater.inflate(R.layout.fragment_settings, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_settings));
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.VISIBLE);

        for (int i = 0; i < mListDateFormat.length; i++) {
            if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat().equalsIgnoreCase(mListDateFormat[i])) {
                mIntSelectedItemPosition = i;
                mIntTempSelectedItemPosition = i;
                mStringSelectedFormat = mListDateFormat[i];
            }
        }
        mTextViewSelectedDate.setText(mStringSelectedFormat);
        return mViewRoot;
    }

    @OnClick(R.id.fragment_setting_linearlayout_update_profile)
    public void onUpdateProfileClick(View mView) {
        if (isAdded()) {
            FragmentUpdateProfile mFragmentUpdateProfile = new FragmentUpdateProfile();
            mActivity.replacesFragment(mFragmentUpdateProfile, true, null, 1);
        }
    }

    @OnClick(R.id.fragment_setting_linearlayout_change_password)
    public void onChangePasswordClick(View mView) {
        if (isAdded()) {
            FragmentChangePassword mFragmentChangePassword = new FragmentChangePassword();
            mActivity.replacesFragment(mFragmentChangePassword, true, null, 1);
        }
    }

    @OnClick(R.id.fragment_setting_linearlayout_select_dateformat)
    public void onSelectDateFormatClick(View mView) {
        if (isAdded()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
            builder.setTitle("Choose Date Format");
            System.out.println("Selected DateFormat-" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat());
            mIntTempSelectedItemPosition = mIntSelectedItemPosition;
            builder.setSingleChoiceItems(mListDateFormat, mIntSelectedItemPosition, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // user checked an item
                    mIntTempSelectedItemPosition = which;
                    System.out.println("mIntTempSelectedItemPosition-Pos-" + mIntTempSelectedItemPosition);
                }
            });
            builder.setPositiveButton(getResources().getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // user clicked OK
                    mStringSelectedFormat = mListDateFormat[mIntTempSelectedItemPosition];
                    mIntSelectedItemPosition = mIntTempSelectedItemPosition;
                    System.out.println("Selected-" + mStringSelectedFormat);
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setSelectedDateFormat(mStringSelectedFormat);
                    mTextViewSelectedDate.setText(mStringSelectedFormat);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.str_cancel), null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @OnClick(R.id.fragment_setting_linearlayout_logout)
    public void onLogoutClick(View mView) {
        if (isAdded()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.str_logout));
            builder.setCancelable(false);
            builder.setMessage(getResources().getString(R.string.str_logout_confirmation));
            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).ResetPrefData();
//                    String mStringQuery = "delete from " + mActivity.mDbHelper.mTableInstall;
//                    mActivity.mDbHelper.exeQuery(mStringQuery);
//                    String mStringQueryInsPhoto = "delete from " + mActivity.mDbHelper.mTableInstallerPhoto;
//                    mActivity.mDbHelper.exeQuery(mStringQueryInsPhoto);
//                    String mStringQueryUnIns = "delete from " + mActivity.mDbHelper.mTableUnInstall;
//                    mActivity.mDbHelper.exeQuery(mStringQueryUnIns);
//                    String mStringQueryInsp = "delete from " + mActivity.mDbHelper.mTableInspection;
//                    mActivity.mDbHelper.exeQuery(mStringQueryInsp);
//                    String mStringQueryInspPhoto = "delete from " + mActivity.mDbHelper.mTableInspectionPhoto;
//                    mActivity.mDbHelper.exeQuery(mStringQueryInspPhoto);
                    ShortcutBadger.removeCount(mActivity);
                    Intent mIntent = new Intent(mActivity, LoginActivity.class);
                    startActivity(mIntent);
                    mActivity.finish();
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }
    @OnClick(R.id.fragment_setting_linearlayout_contact_us)
    public void onContactUsClick(View mView) {
        if (isAdded()) {
            String mStringUserPhone = mTextViewContactNO.getText().toString().trim();
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
