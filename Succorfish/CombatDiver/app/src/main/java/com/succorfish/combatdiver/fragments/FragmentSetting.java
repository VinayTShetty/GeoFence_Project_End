package com.succorfish.combatdiver.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.libRG.CustomTextView;
import com.succorfish.combatdiver.helper.Constant;
import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 18-01-2018.
 */

public class FragmentSetting extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    private String mStrDirectoryFolderName = Constant.DIRECTORY_FOLDER_NAME;
    private File mFileSDCard;
    private File mFileAppDirectory;
    private boolean isSystemUdpate = false;
    @BindView(R.id.frg_setting_seekBrightness)
    SeekBar mSeekBarBatteryLevel;
    @BindView(R.id.frg_setting_circle_textview_battery_count)
    CustomTextView mCustomTextViewBatteryLevel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_setting, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_setting_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mSeekBarBatteryLevel.setOnSeekBarChangeListener(batteryLevelChange);
        return mViewRoot;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    @OnClick(R.id.frg_setting_relativelayout_setup_device)
    public void onClickSetupDevice(View mView) {
        if (mActivity.isDevicesConnected) {
            FragmentSurfaceSetup mFragmentSurfaceSetup = new FragmentSurfaceSetup();
            Bundle mBundle = new Bundle();
            mBundle.putBoolean("intent_is_from_setting", true);
            mActivity.replacesFragment(mFragmentSurfaceSetup, true, mBundle, 1);
        } else {
            mActivity.showDisconnectedDeviceAlert();
        }
    }

    @OnClick(R.id.frg_setting_relativelayout_surface_msg)
    public void onClickSurfaceMessage(View mView) {
        FragmentSurfaceMessage mFragmentSurfaceMessage = new FragmentSurfaceMessage();
        mActivity.replacesFragment(mFragmentSurfaceMessage, true, null, 1);
    }

    @OnClick(R.id.frg_setting_relativelayout_diver_msg)
    public void onClickDiverMessage(View mView) {
        FragmentDiverMessage mFragmentDiverMessage = new FragmentDiverMessage();
        mActivity.replacesFragment(mFragmentDiverMessage, true, null, 1);
    }

    @OnClick(R.id.frg_setting_relativelayout_address_book)
    public void onClickAddressBook(View mView) {
        FragmentAddressBook mFragmentAddressBook = new FragmentAddressBook();
        mActivity.replacesFragment(mFragmentAddressBook, true, null, 1);
    }

    @OnClick(R.id.frg_setting_relativelayout_history)
    public void onClickHistory(View mView) {
        FragmentMessageHistory mFragmentMessageHistory = new FragmentMessageHistory();
        mActivity.replacesFragment(mFragmentMessageHistory, true, null, 1);
    }

    @OnClick(R.id.frg_setting_relativelayout_user_manual)
    public void onClickUserManual(View mView) {
        if (Build.VERSION.SDK_INT >= 23) {
            checkMarshMallowPermission();
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (hasPermissions(mActivity, new String[]{
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE})) {
                downloadManual();
            }
        } else {
            downloadManual();
        }
    }
    @OnClick(R.id.frg_setting_relativelayout_heat_msg)
    public void onClickHeatMessage(View mView) {
        FragmentSettingPrefrence mFragmentSettingPrefrence = new FragmentSettingPrefrence();
        mActivity.replacesFragment(mFragmentSettingPrefrence, true, null, 1);
    }
    @TargetApi(Build.VERSION_CODES.M)
    private void checkMarshMallowPermission() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (permissionsList.size() > 0) {
//            if (permissionsNeeded.size() > 0) {
//                // Need Rationale
//                String message = "App need access to " + permissionsNeeded.get(0);
//                for (int i = 1; i < permissionsNeeded.size(); i++) {
//                    message = message + ", " + permissionsNeeded.get(i);
//                }
//                showMessageOKCancel(message,
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
//                                        REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
//                            }
//                        });
//                return;
//            }
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

    }

    public void createAllFolderDir() {
        mFileSDCard = Environment.getExternalStorageDirectory();
        mFileAppDirectory = new File(mFileSDCard.getAbsolutePath() + mStrDirectoryFolderName);
        if (!mFileAppDirectory.exists()) {
            mFileAppDirectory.mkdirs();
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        boolean hasAllPermissions = true;
        for (String permission : permissions) {
            //you can return false instead of assigning, but by assigning you can log all permission values
            if (!hasPermission(context, permission)) {
                hasAllPermissions = false;
            }
        }
        return hasAllPermissions;
    }

    public static boolean hasPermission(Context context, String permission) {
        int res = context.checkCallingOrSelfPermission(permission);
        return res == PackageManager.PERMISSION_GRANTED;
    }

    private void downloadManual() {
        createAllFolderDir();
        try {
            copyFromAsset();
            File mFile = new File(mFileAppDirectory.getAbsolutePath() + "/UserManual.pdf");
            if (mFile.exists()) {
                Intent intentUrl = new Intent(Intent.ACTION_VIEW);
                intentUrl.setDataAndType(Uri.fromFile(mFile), "application/pdf");
                intentUrl.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intentUrl.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intentUrl);
            }
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(mActivity, "No PDF Viewer Installed", Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void copyFromAsset() throws IOException {
        String fileName = mFileAppDirectory.getAbsolutePath();
        // Open your local db as the input stream
        InputStream myInput = mActivity.getAssets().open("UserManual.pdf");
        // Path to the just created empty db
        String outFileName = fileName + "/UserManual.pdf";
        File mFileOut = new File(outFileName);
        if (!mFileOut.exists()) {
            // Open the empty db as the output stream
            OutputStream myOutput = new FileOutputStream(outFileName);
            // transfer bytes from the inputfile to the outputfile
            byte[] buffer = new byte[2048];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            // Close the streams
            myOutput.flush();
            myOutput.close();
            myInput.close();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
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
                    // Permission Denied
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
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
    /**
     * Called when the Battery slider changes position.
     */
    protected SeekBar.OnSeekBarChangeListener batteryLevelChange = new SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // No behaviour.
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // No behaviour.
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, final int progress, boolean fromUser) {
            mCustomTextViewBatteryLevel.setText(progress + "");
        }
    };

}
