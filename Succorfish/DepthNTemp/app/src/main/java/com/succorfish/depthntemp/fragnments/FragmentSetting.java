package com.succorfish.depthntemp.fragnments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.succorfish.depthntemp.BuildConfig;
import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.helper.URLCLASS;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 16-02-2018.
 */

public class FragmentSetting extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_setting_rg_time_utc)
    RadioGroup mRadioGroupTimeUtc;
    @BindView(R.id.frg_setting_rg_temperature_type)
    RadioGroup mRadioGroupTempType;
    @BindView(R.id.frg_setting_tv_date_format)
    TextView mTextViewDateFormat;
    @BindView(R.id.frg_app_setting_sc_auto_sync)
    SwitchCompat mSwitchCompatAutoSync;
    int mIntSelectedItemPosition = 0;
    int mIntTempSelectedItemPosition = 0;
    private File mFileSDCard;
    private File mFileAppDirectory;
    ArrayList<Uri> uris = new ArrayList<Uri>();

    String[] mListDateFormat = {"yyyy-MM-dd", "dd-MM-yyyy", "dd/MM/yyyy", "dd MMMM yyyy", "MM/dd/yyyy", "yyyy/MM/dd"};
    String mStringSelectedFormat = "yyyy-MM-dd";
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_app_setting, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mToolbar.setVisibility(View.VISIBLE);
        mActivity.mTextViewTitle.setText(R.string.str_menu_app_setting);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);

        for (int i = 0; i < mListDateFormat.length; i++) {
            if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat().equalsIgnoreCase(mListDateFormat[i])) {
                mIntSelectedItemPosition = i;
                mIntTempSelectedItemPosition = i;
                mStringSelectedFormat = mListDateFormat[i];
            }
        }
        if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getIsAutoSync()) {
            mSwitchCompatAutoSync.setChecked(true);
        } else {
            mSwitchCompatAutoSync.setChecked(false);
        }
        mTextViewDateFormat.setText(mStringSelectedFormat);
        System.out.println("getSelectedTimeUFC-" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTimeUFC());
        System.out.println("getSelectedTemperatureType-" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType());

        if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTimeUFC() == 0) {
            ((RadioButton) mRadioGroupTimeUtc.getChildAt(0)).setChecked(true);
        } else if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTimeUFC() == 1) {
            ((RadioButton) mRadioGroupTimeUtc.getChildAt(1)).setChecked(true);
        } else if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTimeUFC() == 2) {
            ((RadioButton) mRadioGroupTimeUtc.getChildAt(2)).setChecked(true);
        } else {
            ((RadioButton) mRadioGroupTimeUtc.getChildAt(2)).setChecked(true);
        }
        if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 0) {
            ((RadioButton) mRadioGroupTempType.getChildAt(0)).setChecked(true);
        } else if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedTemperatureType() == 1) {
            ((RadioButton) mRadioGroupTempType.getChildAt(1)).setChecked(true);
        } else {
            ((RadioButton) mRadioGroupTempType.getChildAt(0)).setChecked(true);
        }
        mRadioGroupTimeUtc.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mActivity.mUtility.hideKeyboard(mActivity);
                if (checkedId == R.id.frg_setting_rb_negative) {
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setSelectedTimeUFC(0);
                } else if (checkedId == R.id.frg_setting_rb_positive) {
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setSelectedTimeUFC(1);
                } else {
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setSelectedTimeUFC(2);
                }
            }
        });
        mRadioGroupTempType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                mActivity.mUtility.hideKeyboard(mActivity);
                if (checkedId == R.id.frg_setting_rb_temp_celsius) {
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setSelectedTemperatureType(0);
                } else {
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setSelectedTemperatureType(1);
                }
            }
        });
        mSwitchCompatAutoSync.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setIsAutoSync(true);
                } else {
                    PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setIsAutoSync(false);
                }
            }
        });
        return mViewRoot;
    }

    /*Date Format Selection Dialog*/
    @OnClick(R.id.frg_setting_tv_date_format)
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
                    mTextViewDateFormat.setText(mStringSelectedFormat);
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.str_cancel), null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    @OnClick(R.id.frg_app_setting_rl_heat_map_setting)
    public void onHeatMapSettingClick(View mView) {
        if (isAdded()) {
            mActivity.replacesFragment(new FragmentSettingHeatMap(), true, null, 1);
        }
    }

    private boolean checkWriteExternalPermission() {
        String permission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
        int res = getContext().checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    @OnClick(R.id.frg_app_setting_rl_export_db)
    public void onExportSettingClick(View mView) {
        if (isAdded()) {
            if (Build.VERSION.SDK_INT >= 23) {
                if (!checkWriteExternalPermission()) {
                    callMarshMallowPermission();
                    return;
                }
            }
            createAllFolderDir();
            uris = new ArrayList<>();
            mActivity.mUtility.ShowProgress("Exporting..", false);
            exportDB(0);
        }
    }

    private void createAllFolderDir() {
        mFileSDCard = Environment.getExternalStorageDirectory();
        mFileAppDirectory = new File(mFileSDCard.getAbsolutePath() + URLCLASS.DIRECTORY_FOLDER_NAME);
        if (!mFileAppDirectory.exists()) {
            mFileAppDirectory.mkdirs();
        }
    }

    private void exportDB(int type) {

        FileChannel source = null;
        FileChannel destination = null;
        String backupDBPath = "";
        String currentDBPath = "";
        if (android.os.Build.VERSION.SDK_INT >= 28) {
            if (type == 0) {
                backupDBPath = MyApplication.getAppContext().getApplicationInfo().dataDir + "/databases/DepthNTemp.db";
                currentDBPath = "DepthNTempBackup.db";
            } else if (type == 1) {
                backupDBPath = MyApplication.getAppContext().getApplicationInfo().dataDir + "/databases/DepthNTemp.db-shm";
                currentDBPath = "DepthNTempBackup.db-shm";
            } else if (type == 2) {
                backupDBPath = MyApplication.getAppContext().getApplicationInfo().dataDir + "/databases/DepthNTemp.db-wal";
                currentDBPath = "DepthNTempBackup.db-wal";
            }
        } else if (android.os.Build.VERSION.SDK_INT >= 17) {
//            backupDBPath = "/data/data/" + MyApplication.getAppContext().getPackageName() + "/databases/DepthNTemp.db";
            if (type == 0) {
                backupDBPath = MyApplication.getAppContext().getApplicationInfo().dataDir + "/databases/DepthNTemp.db";
                currentDBPath = "DepthNTempBackup.db";
            } else if (type == 1) {
                backupDBPath = MyApplication.getAppContext().getApplicationInfo().dataDir + "/databases/DepthNTemp.db-shm";
                currentDBPath = "DepthNTempBackup.db-shm";
            } else if (type == 2) {
                backupDBPath = MyApplication.getAppContext().getApplicationInfo().dataDir + "/databases/DepthNTemp.db-wal";
                currentDBPath = "DepthNTempBackup.db-wal";
            }
        } else {
            backupDBPath = "/data/data/" + MyApplication.getAppContext().getPackageName() + "/databases/DepthNTemp.db";
            currentDBPath = "DepthNTempBackup.db";
        }
        try {
            System.out.println("backupDBPath=" + backupDBPath);
            File currentDB = new File(backupDBPath);
            File backupDB = new File(mFileAppDirectory, currentDBPath);
            System.out.println(currentDB.getAbsoluteFile());
            System.out.println(backupDB.getAbsoluteFile());
            source = new FileInputStream(currentDB).getChannel();
            destination = new FileOutputStream(backupDB).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
            Uri uri = FileProvider.getUriForFile(mActivity, BuildConfig.APPLICATION_ID, backupDB);
            uris.add(uri);
            if (type == 0) {
                exportDB(1);
            }
            if (type == 1) {
                exportDB(2);
            }
            if (type == 2) {
                mActivity.mUtility.HideProgress();
                Toast.makeText(mActivity, "Database exported successfully.", Toast.LENGTH_SHORT).show();
                final Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                emailIntent.setType("text/plain");
                emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL,
                        new String[]{"kalpesh@succorfish.com"});
                emailIntent.putExtra(android.content.Intent.EXTRA_CC,
                        new String[]{"jaydip@succorfish.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "DepthNTemp Database");
                emailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mActivity.startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            }
        } catch (IOException e) {
            e.printStackTrace();
            mActivity.mUtility.HideProgress();
            Toast.makeText(mActivity, "Something went wrong.", Toast.LENGTH_SHORT).show();
        }
    }

    /*Check Required Permission*/
    @TargetApi(Build.VERSION_CODES.M)
    private void callMarshMallowPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (mActivity.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);

                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied
                }
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied#858585
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mActivity.mUtility.hideKeyboard(mActivity);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setVisibility(View.GONE);
    }
}
