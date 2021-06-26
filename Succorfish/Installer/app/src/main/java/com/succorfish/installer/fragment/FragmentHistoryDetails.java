package com.succorfish.installer.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.gson.Gson;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInspection;
import com.succorfish.installer.Vo.VoInstallation;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoReportResponse;
import com.succorfish.installer.Vo.VoUnInstall;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.URLCLASS;
import com.succorfish.installer.interfaces.onBackPressWithAction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentHistoryDetails extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_history_details_textview_date)
    TextView mTextViewDate;
    @BindView(R.id.fragment_history_details_textview_imei_no)
    TextView mTextViewImeiCode;
    @BindView(R.id.fragment_history_details_textview_vessel_name)
    TextView mTextViewVesselName;
    @BindView(R.id.fragment_history_details_textview_reg_no)
    TextView mTextViewRegNo;
    @BindView(R.id.fragment_history_details_textview_device_type)
    TextView mTextViewDeviceType;
    @BindView(R.id.fragment_history_detaile_buttton_view_pdf)
    Button mButtonViewPdf;
    @BindView(R.id.fragment_history_detaile_buttton_view_install_record)
    Button mButtonViewInstallRecord;

    int mIntHistoryType = 0;
    VoInstallation mVoInstallation;
    VoUnInstall mVoUnInstall;
    VoInspection mVoInspection;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    Calendar mCalendar;
    VoInstallationResponse mVoInstallationResponse;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private File mFileSDCard;
    private File mFileAppDirectory;
    private File mFileInstallerDirectory;
    private File mFileUninstallDirectory;
    private File mFileInspectionDirectory;
    private File mFileReportDirectory;
    private String mStrDirectoryFolderName = URLCLASS.DIRECTORY_FOLDER_NAME;
    private String mStrDirectoryInstallationFolderName = URLCLASS.DIRECTORY_INSTALLATION_FOLDER_NAME;
    private String mStrDirectoryUninstallFolderName = URLCLASS.DIRECTORY_UNINSTALL_FOLDER_NAME;
    private String mStrDirectoryInspectionFolderName = URLCLASS.DIRECTORY_INSPECTION_FOLDER_NAME;
    private String mStrDirectoryReportFolderName = URLCLASS.DIRECTORY_REPORT;
    private long mLastClickTime = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mCalendar = Calendar.getInstance();
        if (getArguments() != null) {
            mIntHistoryType = getArguments().getInt("intent_history_type");
            if (mIntHistoryType == 0) {
                mVoInstallation = (VoInstallation) getArguments().getSerializable("intent_install_data");
            } else if (mIntHistoryType == 1) {
                mVoUnInstall = (VoUnInstall) getArguments().getSerializable("intent_uninstall_data");
            } else if (mIntHistoryType == 2) {
                mVoInspection = (VoInspection) getArguments().getSerializable("intent_inspection_data");
            } else {
                mVoInstallation = (VoInstallation) getArguments().getSerializable("intent_install_data");
            }
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        System.out.println("mIntHistoryType-" + mIntHistoryType);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_history_details, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_history_details));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());
        mVoInstallationResponse = new VoInstallationResponse();
        if (mIntHistoryType == 0) {
            if (mVoInstallation != null) {
                if (mVoInstallation.getInst_date_time() != null && !mVoInstallation.getInst_date_time().equalsIgnoreCase("") && !mVoInstallation.getInst_date_time().equalsIgnoreCase("null")) {
                    try {
                        mCalendar.setTimeInMillis(Long.parseLong(mVoInstallation.getInst_date_time()));
                        mTextViewDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                    } catch (Exception e) {
                        mTextViewDate.setText("-NA-");
                        e.printStackTrace();
                    }
                } else {
                    mTextViewDate.setText("-NA-");
                }
                if (mVoInstallation.getInst_device_imei_no() != null && !mVoInstallation.getInst_device_imei_no().equalsIgnoreCase("")) {
                    mTextViewImeiCode.setText(mVoInstallation.getInst_device_imei_no());
                } else {
                    mTextViewImeiCode.setText("NA");
                }
                if (mVoInstallation.getInst_vessel_name() != null && !mVoInstallation.getInst_vessel_name().equalsIgnoreCase("")) {
                    mTextViewVesselName.setText(mVoInstallation.getInst_vessel_name());
                } else {
                    mTextViewVesselName.setText("NA");
                }
                if (mVoInstallation.getInst_vessel_regi_no() != null && !mVoInstallation.getInst_vessel_regi_no().equalsIgnoreCase("")) {
                    mTextViewRegNo.setText(mVoInstallation.getInst_vessel_regi_no());
                } else {
                    mTextViewRegNo.setText("NA");
                }
                if (mVoInstallation.getInst_device_type_name() != null && !mVoInstallation.getInst_device_type_name().equalsIgnoreCase("")) {
                    mTextViewDeviceType.setText(mVoInstallation.getInst_device_type_name());
                } else {
                    mTextViewDeviceType.setText("NA");
                }
                if (mVoInstallation.getInst_pdf_url() != null && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("") && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("null")) {
                    mButtonViewPdf.setEnabled(true);
                } else {
                    mButtonViewPdf.setEnabled(false);
                }

            }
        } else if (mIntHistoryType == 1) {
            if (mVoUnInstall != null) {
                if (mVoUnInstall.getUninst_date_time() != null && !mVoUnInstall.getUninst_date_time().equalsIgnoreCase("") && !mVoUnInstall.getUninst_date_time().equalsIgnoreCase("null")) {
                    try {
                        mCalendar.setTimeInMillis(Long.parseLong(mVoUnInstall.getUninst_date_time()));
                        mTextViewDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                    } catch (Exception e) {
                        mTextViewDate.setText("-NA-");
                        e.printStackTrace();
                    }
                } else {
                    mTextViewDate.setText("-NA-");
                }
                if (mVoUnInstall.getUninst_device_type_imei_no() != null && !mVoUnInstall.getUninst_device_type_imei_no().equalsIgnoreCase("")) {
                    mTextViewImeiCode.setText(mVoUnInstall.getUninst_device_type_imei_no());
                } else {
                    mTextViewImeiCode.setText("NA");
                }
                if (mVoUnInstall.getUninst_vessel_name() != null && !mVoUnInstall.getUninst_vessel_name().equalsIgnoreCase("")) {
                    mTextViewVesselName.setText(mVoUnInstall.getUninst_vessel_name());
                } else {
                    mTextViewVesselName.setText("NA");
                }
                if (mVoUnInstall.getUninst_vessel_regi_no() != null && !mVoUnInstall.getUninst_vessel_regi_no().equalsIgnoreCase("")) {
                    mTextViewRegNo.setText(mVoUnInstall.getUninst_vessel_regi_no());
                } else {
                    mTextViewRegNo.setText("NA");
                }
                if (mVoUnInstall.getUninst_device_type_name() != null && !mVoUnInstall.getUninst_device_type_name().equalsIgnoreCase("")) {
                    mTextViewDeviceType.setText(mVoUnInstall.getUninst_device_type_name());
                } else {
                    mTextViewDeviceType.setText("NA");
                }
                System.out.println("getUninst_pdf_url " + mVoUnInstall.getUninst_pdf_url());
                if (mVoUnInstall.getUninst_pdf_url() != null && !mVoUnInstall.getUninst_pdf_url().equalsIgnoreCase("") && !mVoUnInstall.getUninst_pdf_url().equalsIgnoreCase("null")) {
                    mButtonViewPdf.setEnabled(true);
                } else {
                    mButtonViewPdf.setEnabled(false);
                }
            }
        } else if (mIntHistoryType == 2) {
            if (mVoInspection != null) {
                if (mVoInspection.getInsp_date_time() != null && !mVoInspection.getInsp_date_time().equalsIgnoreCase("") && !mVoInspection.getInsp_date_time().equalsIgnoreCase("null")) {
                    try {
                        mCalendar.setTimeInMillis(Long.parseLong(mVoInspection.getInsp_date_time()));
                        mTextViewDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                    } catch (Exception e) {
                        mTextViewDate.setText("-NA-");
                        e.printStackTrace();
                    }
                } else {
                    mTextViewDate.setText("-NA-");
                }
                if (mVoInspection.getInsp_device_imei_no() != null && !mVoInspection.getInsp_device_imei_no().equalsIgnoreCase("")) {
                    mTextViewImeiCode.setText(mVoInspection.getInsp_device_imei_no());
                } else {
                    mTextViewImeiCode.setText("NA");
                }
                if (mVoInspection.getInsp_vessel_name() != null && !mVoInspection.getInsp_vessel_name().equalsIgnoreCase("")) {
                    mTextViewVesselName.setText(mVoInspection.getInsp_vessel_name());
                } else {
                    mTextViewVesselName.setText("NA");
                }
                if (mVoInspection.getInsp_vessel_regi_no() != null && !mVoInspection.getInsp_vessel_regi_no().equalsIgnoreCase("")) {
                    mTextViewRegNo.setText(mVoInspection.getInsp_vessel_regi_no());
                } else {
                    mTextViewRegNo.setText("NA");
                }
                if (mVoInspection.getInsp_device_type_name() != null && !mVoInspection.getInsp_device_type_name().equalsIgnoreCase("")) {
                    mTextViewDeviceType.setText(mVoInspection.getInsp_device_type_name());
                } else {
                    mTextViewDeviceType.setText("NA");
                }
                if (mVoInspection.getInsp_pdf_url() != null && !mVoInspection.getInsp_pdf_url().equalsIgnoreCase("") && !mVoInspection.getInsp_pdf_url().equalsIgnoreCase("null")) {
                    mButtonViewPdf.setEnabled(true);
                } else {
                    mButtonViewPdf.setEnabled(false);
                }
            }
        } else {
            if (mVoInstallation != null) {
                if (mVoInstallation.getInst_date_time() != null && !mVoInstallation.getInst_date_time().equalsIgnoreCase("") && !mVoInstallation.getInst_date_time().equalsIgnoreCase("null")) {
                    try {
                        mCalendar.setTimeInMillis(Long.parseLong(mVoInstallation.getInst_date_time()));
                        mTextViewDate.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                    } catch (Exception e) {
                        mTextViewDate.setText("-NA-");
                        e.printStackTrace();
                    }
                } else {
                    mTextViewDate.setText("-NA-");
                }
                if (mVoInstallation.getInst_device_imei_no() != null && !mVoInstallation.getInst_device_imei_no().equalsIgnoreCase("")) {
                    mTextViewImeiCode.setText(mVoInstallation.getInst_device_imei_no());
                } else {
                    mTextViewImeiCode.setText("NA");
                }
                if (mVoInstallation.getInst_vessel_name() != null && !mVoInstallation.getInst_vessel_name().equalsIgnoreCase("")) {
                    mTextViewVesselName.setText(mVoInstallation.getInst_vessel_name());
                } else {
                    mTextViewVesselName.setText("NA");
                }
                if (mVoInstallation.getInst_vessel_regi_no() != null && !mVoInstallation.getInst_vessel_regi_no().equalsIgnoreCase("")) {
                    mTextViewRegNo.setText(mVoInstallation.getInst_vessel_regi_no());
                } else {
                    mTextViewRegNo.setText("NA");
                }
                if (mVoInstallation.getInst_device_type_name() != null && !mVoInstallation.getInst_device_type_name().equalsIgnoreCase("")) {
                    mTextViewDeviceType.setText(mVoInstallation.getInst_device_type_name());
                } else {
                    mTextViewDeviceType.setText("NA");
                }
                if (mVoInstallation.getInst_pdf_url() != null && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("") && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("null")) {
                    mButtonViewPdf.setEnabled(true);
                } else {
                    mButtonViewPdf.setEnabled(false);
                }
            }
        }
        return mViewRoot;
    }

    /*Display pdf*/
    private void showPdf(String pdfName, String mStringPDF) {
        if (mStringPDF != null && !mStringPDF.equalsIgnoreCase("") && !mStringPDF.equalsIgnoreCase("null")) {
            byte[] bytes = Base64.decode(mStringPDF, Base64.DEFAULT);
            String fileName = mFileReportDirectory.getAbsolutePath();
            String outFileName = fileName + "/" + pdfName + ".pdf";
            System.out.println("outFileName-" + outFileName);
            FileOutputStream mFileOutputStream = null;
            try {
                File mFileLocalPDF = new File(outFileName);
                if (!mFileLocalPDF.exists()) {
                    mFileOutputStream = new FileOutputStream(outFileName);
                    mFileOutputStream.write(bytes);
                    mFileOutputStream.flush();
                }
                System.out.println("mFileLocalPDF-" + mFileLocalPDF.getAbsolutePath());
                FragmentViewPDF mFragmentViewPDF = new FragmentViewPDF();
                Bundle mBundle = new Bundle();
                mBundle.putString("intent_pdf_url", mStringPDF);
                mBundle.putString("intent_local_pdf_url", mFileLocalPDF.getAbsolutePath());
                mBundle.putInt("intent_pdf_type", mIntHistoryType);
                if (mIntHistoryType == 0) {
                    mBundle.putString("intent_imei_no", mVoInstallation.getInst_device_imei_no());
                    mBundle.putString("intent_vessel_name", mVoInstallation.getInst_vessel_name());
                    mBundle.putString("intent_date", mVoInstallation.getInst_date_time());
                } else if (mIntHistoryType == 1) {
                    mBundle.putString("intent_imei_no", mVoUnInstall.getUninst_device_type_imei_no());
                    mBundle.putString("intent_vessel_name", mVoUnInstall.getUninst_vessel_name());
                    mBundle.putString("intent_date", mVoUnInstall.getUninst_date_time());
                } else if (mIntHistoryType == 2) {
                    mBundle.putString("intent_imei_no", mVoInspection.getInsp_device_imei_no());
                    mBundle.putString("intent_vessel_name", mVoInspection.getInsp_vessel_name());
                    mBundle.putString("intent_date", mVoInspection.getInsp_date_time());
                } else {
                    mBundle.putString("intent_imei_no", mVoInstallation.getInst_device_imei_no());
                    mBundle.putString("intent_vessel_name", mVoInstallation.getInst_vessel_name());
                    mBundle.putString("intent_date", mVoInstallation.getInst_date_time());
                }
                mActivity.replacesFragment(mFragmentViewPDF, true, mBundle, 1);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (mFileOutputStream != null) {
                    try {
                        mFileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    /*Check test device*/
    private void testDevice(String mStringImeiNo, boolean hasIntentData, String deviceId, VoInstallationResponse mVoInstallationResponse) {

        FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
        Bundle mBundle = new Bundle();
        mBundle.putString("mIntent_Imei_no", mStringImeiNo);
        mBundle.putBoolean("mIntent_has_data", hasIntentData);
        mBundle.putBoolean("mIntent_is_from_test_device", false);
//        mBundle.putBoolean("mIntent_is_show_lat_long", false);
        mBundle.putSerializable("mIntent_last_install_record", mVoInstallationResponse);
        if (hasIntentData) {
            mBundle.putString("mIntent_device_id", deviceId);
        }

        mFragmentTestDevice.setOnTestResultSet(new onBackPressWithAction() {
            @Override
            public void onBackWithAction(String scanResult) {
            }

            @Override
            public void onBackWithAction(String value1, String value2) {

            }

            @Override
            public void onBackWithAction(String imei, String deviceType, String warrantyStatus) {

            }
        });
        mActivity.replacesFragment(mFragmentTestDevice, true, mBundle, 1);
    }

    @OnClick(R.id.fragment_history_detaile_buttton_view_pdf)
    public void onViewPdfClick(View mView) {
        if (isAdded()) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                return;
            }
            mLastClickTime = SystemClock.elapsedRealtime();
            if (Build.VERSION.SDK_INT >= 23) {
                checkMarshMallowPermission();
            }
            if (Build.VERSION.SDK_INT >= 23) {
                if (hasPermissions(mActivity, new String[]{
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE})) {
                    createAllFolderDir();
                    getPdfDetails();
                }
            } else {
                createAllFolderDir();
                getPdfDetails();
            }


        }
    }

    /*Get Pdf details*/
    private void getPdfDetails() {
        if (mIntHistoryType == 0) {
            mFileReportDirectory = new File(mFileInstallerDirectory.getAbsolutePath() + mStrDirectoryReportFolderName);
            if (!mFileReportDirectory.exists()) {
                mFileReportDirectory.mkdirs();
            }

            if (mVoInstallation != null) {
                if (mActivity.mUtility.haveInternet()) {
                    if (mVoInstallation.getInst_pdf_url() != null && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("") && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("null")) {
                        getInstallationDetails(mVoInstallation.getInst_pdf_url(), mVoInstallation.getInst_server_id());
                    }
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                }
            }
        } else if (mIntHistoryType == 1) {
            mFileReportDirectory = new File(mFileUninstallDirectory.getAbsolutePath() + mStrDirectoryReportFolderName);
            if (!mFileReportDirectory.exists()) {
                mFileReportDirectory.mkdirs();
            }
            if (mVoUnInstall != null) {
                if (mActivity.mUtility.haveInternet()) {
                    if (mVoUnInstall.getUninst_pdf_url() != null && !mVoUnInstall.getUninst_pdf_url().equalsIgnoreCase("") && !mVoUnInstall.getUninst_pdf_url().equalsIgnoreCase("null")) {
                        getInstallationDetails(mVoUnInstall.getUninst_pdf_url(), mVoUnInstall.getUninst_server_id());
                    }
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                }
            }
        } else if (mIntHistoryType == 2) {
            mFileReportDirectory = new File(mFileInspectionDirectory.getAbsolutePath() + mStrDirectoryReportFolderName);
            if (!mFileReportDirectory.exists()) {
                mFileReportDirectory.mkdirs();
            }
            if (mVoInspection != null) {
                if (mActivity.mUtility.haveInternet()) {
                    if (mVoInspection.getInsp_pdf_url() != null && !mVoInspection.getInsp_pdf_url().equalsIgnoreCase("") && !mVoInspection.getInsp_pdf_url().equalsIgnoreCase("null")) {
                        getInstallationDetails(mVoInspection.getInsp_pdf_url(), mVoInspection.getInsp_server_id());
                    }
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                }
            }
        } else {
            mFileReportDirectory = new File(mFileInstallerDirectory.getAbsolutePath() + mStrDirectoryReportFolderName);
            if (!mFileReportDirectory.exists()) {
                mFileReportDirectory.mkdirs();
            }
            if (mVoInstallation != null) {
                if (mActivity.mUtility.haveInternet()) {
                    if (mVoInstallation.getInst_pdf_url() != null && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("") && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("null")) {
                        getInstallationDetails(mVoInstallation.getInst_pdf_url(), mVoInstallation.getInst_server_id());
                    }
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                }
            }
        }
    }

    @OnClick(R.id.fragment_history_detaile_buttton_view_install_record)
    public void onViewLastRecordViewClick(View mView) {
        if (isAdded()) {
            if (mIntHistoryType == 0) {
                if (mVoInstallation != null) {
                    mVoInstallationResponse.setDate(mVoInstallation.getInst_date_time());
                    mVoInstallationResponse.setDeviceImei(mVoInstallation.getInst_device_imei_no());
                    mVoInstallationResponse.setDeviceType(mVoInstallation.getInst_device_type_name());
                    mVoInstallationResponse.setCreatedByUsername(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName());
                    mVoInstallationResponse.setRealAssetName(mVoInstallation.getInst_vessel_name());
                    mVoInstallationResponse.setRealAssetRegNo(mVoInstallation.getInst_vessel_regi_no());
                    testDevice(mVoInstallation.getInst_device_imei_no(), true, mVoInstallation.getInst_device_server_id(), mVoInstallationResponse);
                }
            } else if (mIntHistoryType == 1) {
                if (mVoUnInstall != null) {
                    mVoInstallationResponse.setDate(mVoUnInstall.getUninst_date_time());
                    mVoInstallationResponse.setDeviceImei(mVoUnInstall.getUninst_device_type_imei_no());
                    mVoInstallationResponse.setDeviceType(mVoUnInstall.getUninst_device_type_name());
                    mVoInstallationResponse.setCreatedByUsername(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName());
                    mVoInstallationResponse.setRealAssetName(mVoUnInstall.getUninst_vessel_name());
                    mVoInstallationResponse.setRealAssetRegNo(mVoUnInstall.getUninst_vessel_regi_no());
                    testDevice(mVoUnInstall.getUninst_device_type_imei_no(), true, mVoUnInstall.getUninst_device_server_id(), mVoInstallationResponse);

                }
            } else if (mIntHistoryType == 2) {
                if (mVoInspection != null) {
                    mVoInstallationResponse.setDate(mVoInspection.getInsp_date_time());
                    mVoInstallationResponse.setDeviceImei(mVoInspection.getInsp_device_imei_no());
                    mVoInstallationResponse.setDeviceType(mVoInspection.getInsp_device_type_name());
                    mVoInstallationResponse.setCreatedByUsername(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName());
                    mVoInstallationResponse.setRealAssetName(mVoInspection.getInsp_vessel_name());
                    mVoInstallationResponse.setRealAssetRegNo(mVoInspection.getInsp_vessel_regi_no());
                    testDevice(mVoInspection.getInsp_device_imei_no(), true, mVoInspection.getInsp_device_server_id(), mVoInstallationResponse);
                }
            } else {
                if (mVoInstallation != null) {
                    mVoInstallationResponse.setDate(mVoInstallation.getInst_date_time());
                    mVoInstallationResponse.setDeviceImei(mVoInstallation.getInst_device_imei_no());
                    mVoInstallationResponse.setDeviceType(mVoInstallation.getInst_device_type_name());
                    mVoInstallationResponse.setCreatedByUsername(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName());
                    mVoInstallationResponse.setRealAssetName(mVoInstallation.getInst_vessel_name());
                    mVoInstallationResponse.setRealAssetRegNo(mVoInstallation.getInst_vessel_regi_no());
                    testDevice(mVoInstallation.getInst_device_imei_no(), true, mVoInstallation.getInst_device_server_id(), mVoInstallationResponse);
                }
            }
        }
    }

    /*get installation record history*/
    public void getInstallationDetails(String mStringResID, String mStringInstallationID) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mUtility.ShowProgress();
        final String[] splitedId = mStringResID.split("/");
        Call<VoReportResponse> mLogin = mActivity.mApiService.getReportData(splitedId[0], mStringInstallationID);
        System.out.println("URL-" + mLogin.request().url().toString());
        mLogin.enqueue(new Callback<VoReportResponse>() {
            @Override
            public void onResponse(Call<VoReportResponse> call, Response<VoReportResponse> response) {
                if (isAdded()) {
                    mActivity.mUtility.HideProgress();
                    if (response.code() == 200 || response.isSuccessful()) {
                        VoReportResponse mVoAddInstallation = response.body();
                        Gson gson = new Gson();
                        String json = gson.toJson(mVoAddInstallation);
                        System.out.println("response mAddInstData---------" + mVoAddInstallation.getFilename());
                        showPdf(splitedId[0], mVoAddInstallation.getContentBytes());
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                    }
                }
            }

            @Override
            public void onFailure(Call<VoReportResponse> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
            }
        });
    }

//    public void getInstallationDetails(String installationId) {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        params.put("installation_id", installationId);
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoAddInstallation> mLogin = mActivity.mApiService.getInstallationDetails(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoAddInstallation>() {
//            @Override
//            public void onResponse(Call<VoAddInstallation> call, Response<VoAddInstallation> response) {
//                if (isAdded()) {
//                    mActivity.mUtility.HideProgress();
//                    VoAddInstallation mVoAddInstallation = response.body();
//                    Gson gson = new Gson();
//                    String json = gson.toJson(mVoAddInstallation);
//                    System.out.println("response mAddInstData---------" + json);
//                    if (mVoAddInstallation != null && mVoAddInstallation.getResponse().equalsIgnoreCase("true")) {
//                        if (mVoAddInstallation.getData() != null) {
//                            ContentValues mContentValues = new ContentValues();
//                            mVoInstallation.setInst_pdf_url(mVoAddInstallation.getData().getPdfpath());
//                            mContentValues.put(mActivity.mDbHelper.mFieldInstallPdfUrl, mVoAddInstallation.getData().getPdfpath());
//                            String[] mArray = new String[]{mVoInstallation.getInst_local_id()};
//                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
//                            System.out.println("Device updated In Local Db");
//                            if (mVoInstallation.getInst_pdf_url() != null && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("") && !mVoInstallation.getInst_pdf_url().equalsIgnoreCase("null")) {
//                                showPdf(mVoInstallation.getInst_pdf_url());
//                            } else {
//                                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
//                            }
//                        }
//                    } else {
//                        if (mVoAddInstallation != null && mVoAddInstallation.getMessage() != null && !mVoAddInstallation.getMessage().equalsIgnoreCase(""))
//                            mActivity.mUtility.errorDialog(mVoAddInstallation.getMessage());
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoAddInstallation> call, Throwable t) {
//                mActivity.mUtility.HideProgress();
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//            }
//        });
//    }
//
//    public void getUnInstallationDetails(String uninstallationId) {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        params.put("uninstallation_id", uninstallationId);
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoAddUnInstallation> mLogin = mActivity.mApiService.getUnInstallationDetails(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoAddUnInstallation>() {
//            @Override
//            public void onResponse(Call<VoAddUnInstallation> call, Response<VoAddUnInstallation> response) {
//                mActivity.mUtility.HideProgress();
//                VoAddUnInstallation mVoAddInstallation = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mVoAddInstallation);
//                System.out.println("response mAddInstData---------" + json);
//                if (mVoAddInstallation != null && mVoAddInstallation.getResponse().equalsIgnoreCase("true")) {
//                    if (mVoAddInstallation.getData() != null) {
//                        ContentValues mContentValues = new ContentValues();
//                        mVoUnInstall.setUninst_pdf_url(mVoAddInstallation.getData().getPdfpath());
//                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallPdfUrl, mVoAddInstallation.getData().getPdfpath());
//                        String[] mArray = new String[]{mVoUnInstall.getUninst_local_id()};
//                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
//                        System.out.println("Device updated In Local Db");
//                        if (mVoUnInstall.getUninst_pdf_url() != null && !mVoUnInstall.getUninst_pdf_url().equalsIgnoreCase("") && !mVoUnInstall.getUninst_pdf_url().equalsIgnoreCase("null")) {
//                            showPdf(mVoUnInstall.getUninst_pdf_url());
//                        } else {
//                            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
//                        }
//                    }
//                } else {
//                    if (mVoAddInstallation != null && mVoAddInstallation.getMessage() != null && !mVoAddInstallation.getMessage().equalsIgnoreCase(""))
//                        mActivity.mUtility.errorDialog(mVoAddInstallation.getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoAddUnInstallation> call, Throwable t) {
//                mActivity.mUtility.HideProgress();
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//            }
//        });
//    }
//
//    public void getInspectionDetails(String inspectionId) {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        params.put("inspection_id", inspectionId);
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoAddInspection> mLogin = mActivity.mApiService.getInspectionDetails(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoAddInspection>() {
//            @Override
//            public void onResponse(Call<VoAddInspection> call, Response<VoAddInspection> response) {
//                mActivity.mUtility.HideProgress();
//                VoAddInspection mVoAddInstallation = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mVoAddInstallation);
//                System.out.println("response mAddInspData---------" + json);
//                if (mVoAddInstallation != null && mVoAddInstallation.getResponse().equalsIgnoreCase("true")) {
//                    if (mVoAddInstallation.getData() != null) {
//                        ContentValues mContentValues = new ContentValues();
//                        mVoInspection.setInsp_pdf_url(mVoAddInstallation.getData().getPdfpath());
//                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionPdfUrl, mVoAddInstallation.getData().getPdfpath());
//                        String[] mArray = new String[]{mVoInspection.getInsp_local_id()};
//                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValues, mActivity.mDbHelper.mFieldInspcLocalId + "=?", mArray);
//                        System.out.println("Device updated In Local Db");
//                        if (mVoInspection.getInsp_pdf_url() != null && !mVoInspection.getInsp_pdf_url().equalsIgnoreCase("") && !mVoInspection.getInsp_pdf_url().equalsIgnoreCase("null")) {
//                            showPdf(mVoInspection.getInsp_pdf_url());
//                        } else {
//                            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
//                        }
//                    }
//                } else {
//                    if (mVoAddInstallation != null && mVoAddInstallation.getMessage() != null && !mVoAddInstallation.getMessage().equalsIgnoreCase(""))
//                        mActivity.mUtility.errorDialog(mVoAddInstallation.getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoAddInspection> call, Throwable t) {
//                mActivity.mUtility.HideProgress();
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//            }
//        });
//    }

    //    public void getLastInstallationDetails(final String imeiNo) {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        params.put("imei", imeiNo);
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoAddInstallation> mLogin = mActivity.mApiService.getLastInstallDetails(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoAddInstallation>() {
//            @Override
//            public void onResponse(Call<VoAddInstallation> call, Response<VoAddInstallation> response) {
//                mActivity.mUtility.HideProgress();
//                VoAddInstallation mVoAddInstallation = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mVoAddInstallation);
//                System.out.println("response mAddInstData---------" + json);
//                if (mVoAddInstallation != null && mVoAddInstallation.getResponse().equalsIgnoreCase("true")) {
//                    if (mVoAddInstallation.getData() != null) {
//                        testDevice(imeiNo, true, mVoAddInstallation);
//                    }
//                } else {
//                    if (mVoAddInstallation != null && mVoAddInstallation.getMessage() != null && !mVoAddInstallation.getMessage().equalsIgnoreCase(""))
//                        mActivity.mUtility.errorDialog(mVoAddInstallation.getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoAddInstallation> call, Throwable t) {
//                mActivity.mUtility.HideProgress();
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//            }
//        });
//    }

    /*Create all folder*/
    public void createAllFolderDir() {
        mFileSDCard = Environment.getExternalStorageDirectory();
        mFileAppDirectory = new File(mFileSDCard.getAbsolutePath() + mStrDirectoryFolderName);
        if (!mFileAppDirectory.exists()) {
            mFileAppDirectory.mkdirs();
        }
        mFileInstallerDirectory = new File(mFileAppDirectory.getAbsolutePath() + mStrDirectoryInstallationFolderName);
        if (!mFileInstallerDirectory.exists()) {
            mFileInstallerDirectory.mkdirs();
        }
        mFileUninstallDirectory = new File(mFileAppDirectory.getAbsolutePath() + mStrDirectoryUninstallFolderName);
        if (!mFileUninstallDirectory.exists()) {
            mFileUninstallDirectory.mkdirs();
        }
        mFileInspectionDirectory = new File(mFileAppDirectory.getAbsolutePath() + mStrDirectoryInspectionFolderName);
        if (!mFileInspectionDirectory.exists()) {
            mFileInspectionDirectory.mkdirs();
        }

    }

    /*Check required permission*/
    @TargetApi(Build.VERSION_CODES.M)
    private void checkMarshMallowPermission() {
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
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
