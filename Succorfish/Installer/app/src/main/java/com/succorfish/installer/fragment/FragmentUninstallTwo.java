package com.succorfish.installer.fragment;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoInstallationPhoto;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoLastInstallation;
import com.succorfish.installer.Vo.VoResponseError;
import com.succorfish.installer.Vo.VoUnInstall;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.URLCLASS;
import com.succorfish.installer.interfaces.onAlertDialogCallBack;
import com.succorfish.installer.interfaces.onBackPressWithAction;
import com.succorfish.installer.interfaces.onNewInstallationBackNext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentUninstallTwo extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;
    Button backBT;
    Button nextBT;
    RelativeLayout mRelativeLayoutSignature;
    RelativeLayout mRelativeLayoutWarrantyReturn;
    RelativeLayout mRelativeLayoutTestDevice;

    TextView mTextViewDeviceStatus;
    TextView mTextViewDeviceReportedDate;

    EditText mEditTextOwnerName;
    EditText mEditTextOwnerAddress;
    EditText mEditTextOwnerCity;
    EditText mEditTextOwnerState;
    EditText mEditTextOwnerZipcode;
    EditText mEditTextOwnerEmail;
    EditText mEditTextOwnerPhoneNo;

    String mStringOwnerName = "";
    String mStringOwnerAddress = "";
    String mStringOwnerCity = "";
    String mStringOwnerState = "";
    String mStringOwnerZipcode = "";
    String mStringOwnerEmail = "";
    String mStringOwnerPhoneNo = "";
    String mStringOwnerSignatureImagePath = "";
    String mStringInstallerSignatureImagePath = "";

    private OnStepTwoListener mListener;
    private File mFileOwnerSignaturePath;
    private File mFileInstallerSignaturePath;
    private Calendar newCalendar;
    private boolean isTestDeviceByUser = false;
    VoUnInstall mVoUnInstall;
    boolean isAlradyFillData = false;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    String mStringIMEINo = "";
    String mStringVesselName = "";
    String mStringVesselRegNo = "";
    String mStringWarrantyStatus = "";
    private SimpleDateFormat mDateFormatDb;

    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private File mFileSDCard;
    private File mFileAppDirectory;

    public FragmentUninstallTwo() {
        // Required empty public constructor
    }

    public static FragmentUninstallTwo newInstance() {
        return new FragmentUninstallTwo();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        newCalendar = Calendar.getInstance();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_uninstall_two, container, false);
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        backBT = mViewRoot.findViewById(R.id.backBT);
        nextBT = mViewRoot.findViewById(R.id.nextBT);
        mRelativeLayoutSignature = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_uninstall_two_relativelayout_signature);
        mRelativeLayoutWarrantyReturn = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_uninstall_two_relativelayout_inspe_warranty_return);
        mRelativeLayoutTestDevice = (RelativeLayout) mViewRoot.findViewById(R.id.frg_uninstall_two_rl_test_device);
        mEditTextOwnerName = (EditText) mViewRoot.findViewById(R.id.fragment_uninstall_two_editext_owner_name);
        mEditTextOwnerAddress = (EditText) mViewRoot.findViewById(R.id.fragment_uninstall_two_editext_address_one);
        mEditTextOwnerCity = (EditText) mViewRoot.findViewById(R.id.fragment_uninstall_two_editext_owner_city);
        mEditTextOwnerState = (EditText) mViewRoot.findViewById(R.id.fragment_uninstall_two_editext_owner_state);
        mEditTextOwnerZipcode = (EditText) mViewRoot.findViewById(R.id.fragment_uninstall_two_editext_owner_zipcode);
        mEditTextOwnerEmail = (EditText) mViewRoot.findViewById(R.id.fragment_uninstall_two_editext_owner_email);
        mEditTextOwnerPhoneNo = (EditText) mViewRoot.findViewById(R.id.fragment_uninstall_two_editext_owner_phoneno);
        mTextViewDeviceStatus = (TextView) mViewRoot.findViewById(R.id.frg_uninstall_two_tv_test_device_status);
        mTextViewDeviceReportedDate = (TextView) mViewRoot.findViewById(R.id.frg_uninstall_two_tv_test_device_reported_date);
        System.out.println("TwomActivity.mIntUnInstallationId-" + mActivity.mIntUnInstallationId);
//        nextBT.setEnabled(false);
        isTestDeviceByUser = false;
        System.out.println("nextBT-DISABLE");
        if (!mActivity.mUtility.haveInternet() || mActivity.mIsTestDeviceCheck) {
            System.out.println("nextBT-ENABLE");
//            nextBT.setEnabled(true);
            isTestDeviceByUser = true;
        }

        if (mActivity.mStringTestDeviceReportedDate != null && !mActivity.mStringTestDeviceReportedDate.equalsIgnoreCase("")) {
            mTextViewDeviceReportedDate.setText(mActivity.mStringTestDeviceReportedDate);
        } else {
            mTextViewDeviceReportedDate.setText("NA");
        }
        if (mActivity.mTestDeviceStatus == 2) {
            mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_ready_to_go));
            mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen));
        } else if (mActivity.mTestDeviceStatus == 3) {
            mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_no_reports));
            mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorRed));
        } else {
            mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_waiting));
            mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
        }
        mActivity.setNewInstallationBackNext(new onNewInstallationBackNext() {
            @Override
            public void onInstallFirstBack(Fragment fragment) {

            }

            @Override
            public void onInstallFirstNext(Fragment fragment) {
                System.out.println("TWO FirstNExt");
                mActivity.mViewPager.setCurrentItem(1, true);
                getDBUnInstallationList(true);
//                if (mActivity.mIntUnInstallationId != 0 && !isAlradyFillData) {
//                    System.out.println("From Edit");
//                    isAlradyFillData = true;
//                    getDBUnInstallationList(true);
//                } else {
//                    getDBUnInstallationList(false);
//                }
//                nextBT.setEnabled(false);
                isTestDeviceByUser = false;
                System.out.println("nextBT-DISABLE");
                if (!mActivity.mUtility.haveInternet() || mActivity.mIsTestDeviceCheck) {
                    System.out.println("nextBT-ENABLE");
//                    nextBT.setEnabled(true);
                    isTestDeviceByUser = true;
                }
                if (mActivity.mStringTestDeviceReportedDate != null && !mActivity.mStringTestDeviceReportedDate.equalsIgnoreCase("")) {
                    mTextViewDeviceReportedDate.setText(mActivity.mStringTestDeviceReportedDate);
                } else {
                    mTextViewDeviceReportedDate.setText("NA");
                }
                if (mActivity.mTestDeviceStatus == 2) {
                    mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_ready_to_go));
                    mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen));
                } else if (mActivity.mTestDeviceStatus == 3) {
                    mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_no_reports));
                    mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorRed));
                } else {
                    mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_waiting));
                    mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorWhite));
                }

            }

            @Override
            public void onInstallSecondBack(Fragment fragment) {
                System.out.println("TWO SecondBAck");
                mActivity.mViewPager.setCurrentItem(0, true);
            }

            @Override
            public void onInstallSecondNext(Fragment fragment) {
                System.out.println("SecondFinish");
            }

            @Override
            public void onInstallThirdBack(Fragment fragment) {

            }

            @Override
            public void onInstallThirdComplete(Fragment fragment) {

            }
        });
        return mViewRoot;
    }

    /*get Installation list from local db*/
    private void getDBUnInstallationList(boolean isFromEdit) {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableUnInstall + " where " + mActivity.mDbHelper.mFieldUnInstallIsSync + "= 0" + " AND " + mActivity.mDbHelper.mFieldUnInstallLocalId + "= '" + mActivity.mIntUnInstallationId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoUnInstall = new VoUnInstall();
                    mVoUnInstall.setUninst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalId));
                    mVoUnInstall.setUninst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerId));
                    mVoUnInstall.setUninst_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallUserId));
                    mVoUnInstall.setUninst_device_type_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceIMEINo));
                    mVoUnInstall.setUninst_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceServerId));
                    mVoUnInstall.setUninst_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceLocalId));
                    mVoUnInstall.setUninst_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceName));
                    mVoUnInstall.setUninst_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceWarrantStatus));
                    mVoUnInstall.setUninst_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceTypeName));
                    mVoUnInstall.setUninst_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselLocalId));
                    mVoUnInstall.setUninst_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselServerId));
                    mVoUnInstall.setUninst_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselName));
                    mVoUnInstall.setUninst_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallVesselRegNo));
                    mVoUnInstall.setUninst_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerName));
                    mVoUnInstall.setUninst_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerAddress));
                    mVoUnInstall.setUninst_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerCity));
                    mVoUnInstall.setUninst_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerState));
                    mVoUnInstall.setUninst_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode));
                    mVoUnInstall.setUninst_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerEmail));
                    mVoUnInstall.setUninst_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo));
                    mVoUnInstall.setUninst_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl));
                    mVoUnInstall.setUninst_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerSignUrl));
                    mVoUnInstall.setUninst_local_uninstaller_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl));
                    mVoUnInstall.setUninst_server_uninstaller_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerUninstallerSignUrl));
                    mVoUnInstall.setUninst_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallServerSignUrl));
                    mVoUnInstall.setUninst_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallCreatedDate));
                    mVoUnInstall.setUninst_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallUpdatedDate));
                    mVoUnInstall.setUninst_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSync));
                    mVoUnInstall.setUninst_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallStatus));
                    mVoUnInstall.setUninst_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDateTime));
                    mVoUnInstall.setUninst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp));
                    mVoUnInstall.setUninst_owner_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSyncOwnerSign));
                    mVoUnInstall.setUninst_installer_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSyncInstallerSign));
                    if (isFromEdit) {
                        mStringOwnerName = mVoUnInstall.getUninst_owner_name();
                        mStringOwnerAddress = mVoUnInstall.getUninst_owner_address();
                        mStringOwnerCity = mVoUnInstall.getUninst_owner_city();
                        mStringOwnerState = mVoUnInstall.getUninst_owner_state();
                        mStringOwnerZipcode = mVoUnInstall.getUninst_owner_zipcode();
                        mStringOwnerEmail = mVoUnInstall.getUninst_owner_email();
                        mStringOwnerPhoneNo = mVoUnInstall.getUninst_owner_mobile_no();
                        mStringOwnerSignatureImagePath = mVoUnInstall.getUninst_local_sign_url();
                        mStringInstallerSignatureImagePath = mVoUnInstall.getUninst_local_uninstaller_sign_url();
                        mEditTextOwnerName.setText(mStringOwnerName);
                        mEditTextOwnerAddress.setText(mStringOwnerAddress);
                        mEditTextOwnerEmail.setText(mStringOwnerEmail);
                        mEditTextOwnerPhoneNo.setText(mStringOwnerPhoneNo);
                        mEditTextOwnerCity.setText(mStringOwnerCity);
                        mEditTextOwnerState.setText(mStringOwnerState);
                        mEditTextOwnerZipcode.setText(mStringOwnerZipcode);
                    }
                    mStringIMEINo = mVoUnInstall.getUninst_device_type_imei_no();
                    mStringVesselName = mVoUnInstall.getUninst_vessel_name();
                    mStringVesselRegNo = mVoUnInstall.getUninst_vessel_regi_no();
                    mStringWarrantyStatus = mVoUnInstall.getUninst_device_warranty_status();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Handle on back press*/
    public void onBackPressCall(MainActivity mActivity) {
        mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
        mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
        mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
        mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();

        mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
        mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
        mStringOwnerZipcode = mEditTextOwnerZipcode.getText().toString().trim();

        Calendar calBack = Calendar.getInstance();
        ContentValues mContentValuesBack = new ContentValues();
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerName, mStringOwnerName);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerAddress, mStringOwnerAddress);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerCity, mStringOwnerCity);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerState, mStringOwnerState);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode, mStringOwnerZipcode);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerEmail, mStringOwnerEmail);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo, mStringOwnerPhoneNo);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl, mStringOwnerSignatureImagePath);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl, mStringInstallerSignatureImagePath);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, calBack.getTimeInMillis() + "");
        String isExistInDBBack = CheckRecordExistInUnInstallDB(mActivity.mIntUnInstallationId + "");
        if (isExistInDBBack.equalsIgnoreCase("-1")) {
            mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallDateTime, calBack.getTimeInMillis() + "");
            mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, calBack.getTimeInMillis() + "");
            int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValuesBack);
            if (isInsertInstall != -1) {
                System.out.println("Device UnInstall Added In Local Db");
            } else {
                System.out.println("Device UnInstall Adding In Local DB");
            }
        } else {
            String[] mArray = new String[]{isExistInDBBack};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValuesBack, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
            System.out.println("Device UnInstall updated In Local Db");
        }
        System.out.println("Device updated In Local Db");
        System.out.println("2-BACK-JD");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.backBT:
                /*handle back click*/
                mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
                mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
                mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
                mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();

                mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
                mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
                mStringOwnerZipcode = mEditTextOwnerZipcode.getText().toString().trim();

                Calendar calBack = Calendar.getInstance();
                ContentValues mContentValuesBack = new ContentValues();
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerName, mStringOwnerName);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerAddress, mStringOwnerAddress);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerCity, mStringOwnerCity);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerState, mStringOwnerState);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode, mStringOwnerZipcode);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerEmail, mStringOwnerEmail);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo, mStringOwnerPhoneNo);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl, mStringOwnerSignatureImagePath);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl, mStringInstallerSignatureImagePath);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, calBack.getTimeInMillis() + "");
                String isExistInDBBack = CheckRecordExistInUnInstallDB(mActivity.mIntUnInstallationId + "");
                if (isExistInDBBack.equalsIgnoreCase("-1")) {
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallDateTime, calBack.getTimeInMillis() + "");
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, calBack.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValuesBack);
                    if (isInsertInstall != -1) {
                        System.out.println("Device UnInstall Added In Local Db");
                    } else {
                        System.out.println("Device UnInstall Adding In Local DB");
                    }
                } else {
                    String[] mArray = new String[]{isExistInDBBack};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValuesBack, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
                    System.out.println("Device UnInstall updated In Local Db");
                }
                if (mListener != null)
                    mListener.onBackTwoPressed(this);
                break;
            case R.id.fragment_uninstall_two_relativelayout_inspe_warranty_return:
                if (isAdded()) {
                    try {
                        if (mStringWarrantyStatus == null || mStringWarrantyStatus.equalsIgnoreCase("") || mStringWarrantyStatus.equalsIgnoreCase("null")) {
                            mStringWarrantyStatus = "NA";
                        }
                        Calendar cal = Calendar.getInstance();
                        Date currentLocalTime = cal.getTime();
                        String msgData = "Device " + mStringIMEINo + " from " + mStringVesselName + " & " + mStringVesselRegNo + " will be returned for warranty inspection. Its warranty status " + mStringWarrantyStatus + " and was inspected by " + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName() + " on " + mDateFormatDb.format(currentLocalTime);
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setData(Uri.parse("mailto:"));
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"warranties@succorfish.com"});
                        sendIntent.putExtra(Intent.EXTRA_SUBJECT, mStringIMEINo);
                        sendIntent.putExtra(Intent.EXTRA_TEXT, msgData);
                        if (sendIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                            mActivity.startActivity(sendIntent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.fragment_uninstall_two_relativelayout_signature:
                FragmentSignature mFragmentSignature = new FragmentSignature();
                Bundle mBundle = new Bundle();
                mBundle.putString("intent_owner_signature", mStringOwnerSignatureImagePath);
                mBundle.putString("intent_installer_signature", mStringInstallerSignatureImagePath);
                mBundle.putInt("intent_is_from", 2);
                mFragmentSignature.setOnScanResultSet(new onBackPressWithAction() {
                    @Override
                    public void onBackWithAction(String scanResult) {

                    }

                    @Override
                    public void onBackWithAction(String value1, String value2) {
                        mStringOwnerSignatureImagePath = value1;
                        mStringInstallerSignatureImagePath = value2;
                        System.out.println("mStringOwnerSignatureImagePath-" + mStringOwnerSignatureImagePath);
                        System.out.println("mStringInstallerSignatureImagePath-" + mStringInstallerSignatureImagePath);
                        mVoUnInstall.setUninst_local_sign_url(mStringOwnerSignatureImagePath);
                        mVoUnInstall.setUninst_local_uninstaller_sign_url(mStringInstallerSignatureImagePath);
                    }

                    @Override
                    public void onBackWithAction(String imei, String deviceType, String warrantyStatus) {

                    }
                });
                mActivity.replacesFragment(mFragmentSignature, true, mBundle, 1);
                break;
            case R.id.frg_uninstall_two_rl_test_device:
                if (mActivity.mUtility.haveInternet()) {
                    getTestInstallInformation(true);
                }
                break;
            case R.id.nextBT:
                if (Build.VERSION.SDK_INT >= 23) {
                    callMarshMallowPermission();
                }
                createAllFolderDir();

                mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
                mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
                mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
                mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();

                mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
                mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
                mStringOwnerZipcode = mEditTextOwnerZipcode.getText().toString().trim();

                if (mStringOwnerName.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_name));
                    return;
                }
                if (mStringOwnerAddress.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_address));
                    return;
                }
//                if (mStringOwnerCity.equalsIgnoreCase("")) {
//                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_city));
//                    return;
//                }
//                if (mStringOwnerState.equalsIgnoreCase("")) {
//                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_state));
//                    return;
//                }
                if (mStringOwnerZipcode.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_zipcode));
                    return;
                }

                if (mStringOwnerEmail.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_email));
                    return;
                }

                if (!mActivity.mUtility.isValidEmail(mStringOwnerEmail)) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_valid_email_address));
                    return;
                }
                if (mStringOwnerPhoneNo.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_enter_owner_phonono));
                    return;
                }
                if (mStringOwnerEmail != null && !mStringOwnerEmail.equalsIgnoreCase("")) {
                    if (!mActivity.mUtility.isValidEmail(mStringOwnerEmail)) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_valid_email_address));
                        return;
                    }
                }
                if (mStringOwnerSignatureImagePath == null || mStringOwnerSignatureImagePath.equalsIgnoreCase("") || mStringOwnerSignatureImagePath.equalsIgnoreCase("null")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_draw_sign));
                    return;
                }
                if (mStringInstallerSignatureImagePath == null || mStringInstallerSignatureImagePath.equalsIgnoreCase("") || mStringOwnerSignatureImagePath.equalsIgnoreCase("null")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_ask_installer_sign));
                    return;
                }
                if (mStringOwnerSignatureImagePath != null && !mStringOwnerSignatureImagePath.equalsIgnoreCase("")) {
                    mFileOwnerSignaturePath = new File(mStringOwnerSignatureImagePath);
                    if (mFileOwnerSignaturePath != null && mFileOwnerSignaturePath.exists()) {
                    } else {
                        mStringOwnerSignatureImagePath = "";
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_sign_owner_missing_image));
                        return;
                    }
                }
                if (mStringInstallerSignatureImagePath != null && !mStringInstallerSignatureImagePath.equalsIgnoreCase("")) {
                    mFileInstallerSignaturePath = new File(mStringInstallerSignatureImagePath);
                    if (mFileInstallerSignaturePath != null && mFileInstallerSignaturePath.exists()) {
                    } else {
                        mStringInstallerSignatureImagePath = "";
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_sign_uninstaller_missing_image));
                        return;
                    }
                }
                if (!mActivity.mUtility.haveInternet() || mActivity.mIsTestDeviceCheck) {
                    System.out.println("nextBT-ENABLE");
//            nextBT.setEnabled(true);
                    isTestDeviceByUser = true;
                }

                if (!isTestDeviceByUser) {
                    mActivity.mUtility.errorDialogWithCallBack("Please tap on Test Device first to check the status of Device.", new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {

                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                    return;
                }
//                if (mActivity.mTestDeviceStatus != 2) {
//                    mActivity.mUtility.errorDialogWithCallBack("You can not submit this report as no reports in past 2 hours", new onAlertDialogCallBack() {
//                        @Override
//                        public void PositiveMethod(DialogInterface dialog, int id) {
//
//                        }
//
//                        @Override
//                        public void NegativeMethod(DialogInterface dialog, int id) {
//
//                        }
//                    });
//                    return;
//                }
                saveQuestionFile();
                if (!mActivity.mUtility.haveInternet()) {
                    Calendar cal = Calendar.getInstance();
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerName, mStringOwnerName);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerAddress, mStringOwnerAddress);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerCity, mStringOwnerCity);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerState, mStringOwnerState);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode, mStringOwnerZipcode);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerEmail, mStringOwnerEmail);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo, mStringOwnerPhoneNo);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl, mStringOwnerSignatureImagePath);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl, mStringInstallerSignatureImagePath);
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, cal.getTimeInMillis() + "");
                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallStatus, "1");
                    String isExistInDB = CheckRecordExistInUnInstallDB(mActivity.mIntUnInstallationId + "");
                    if (isExistInDB.equalsIgnoreCase("-1")) {
                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDateTime, cal.getTimeInMillis() + "");
                        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, cal.getTimeInMillis() + "");
                        int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues);
                        if (isInsertInstall != -1) {
                            System.out.println("Device UnInstall Added In Local Db");
                        } else {
                            System.out.println("Device UnInstall Adding In Local DB");
                        }
                    } else {
                        String[] mArray = new String[]{isExistInDB};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
                        System.out.println("Device UnInstall updated In Local Db");
                    }
                    showSuccessMessage();
                } else {
                    try {
                        if (mVoUnInstall.getUninst_server_id() != null && !mVoUnInstall.getUninst_server_id().equalsIgnoreCase("") && !mVoUnInstall.getUninst_server_id().equalsIgnoreCase("null")) {
                            new InstallationPhotoUploadAsyncTask().execute("");
                        } else {
                            if (mVoUnInstall.getUninst_device_server_id() != null && !mVoUnInstall.getUninst_device_server_id().equalsIgnoreCase("") && !mVoUnInstall.getUninst_device_server_id().equalsIgnoreCase("null")) {
                                AddUnInstallationAPI();
                            } else {
                                mActivity.mUtility.errorDialog("Please rescan device. device details is not valid");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /*Save Un install question and ans into file*/
    private void saveQuestionFile() {
        String fileName = mFileAppDirectory.getAbsolutePath() + "/" + "health-and-safety.txt";
        File mFileQuestion = new File(fileName);
        try {
            if (mFileQuestion.exists()) {
                mFileQuestion.delete();
            }
            mFileQuestion.createNewFile();
            FileOutputStream fOut = new FileOutputStream(mFileQuestion);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
            String questionNo = "";
            myOutWriter.append("===============================================================\r\n");
            myOutWriter.append("            Uninstall Health and Safety Questionnaire          \r\n");
            myOutWriter.append("===============================================================\r\n");
            for (int i = 0; i < mActivity.mListQuestion.size(); i++) {
                questionNo = "\r\nQ" + mActivity.mListQuestion.get(i).getQuestionNo() + ". " + mActivity.mListQuestion.get(i).getQuestionName() + "\r\n";
                myOutWriter.append(questionNo);
                myOutWriter.append("Answer : " + mActivity.mListQuestion.get(i).getAnsText() + "\r\n");
                if (mActivity.mListQuestion.get(i).getQuestionType() == 1) {
                    if (mActivity.mListQuestion.get(i).getChooseAns() == 2) {
                        myOutWriter.append("Reason : " + mActivity.mListQuestion.get(i).getAnsComment() + "\r\n");
                    }
                } else if (mActivity.mListQuestion.get(i).getQuestionType() == 2) {
                    myOutWriter.append("Reason : " + mActivity.mListQuestion.get(i).getAnsComment() + "\r\n");
                }
            }
            myOutWriter.close();
            fOut.flush();
            fOut.close();
            System.out.println("Question file generated.");
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    /*Get Test information api*/
    private void getTestInstallInformation(boolean isShowAlert) {
        if (mVoUnInstall != null) {
            mActivity.mUtility.hideKeyboard(mActivity);
            mActivity.mUtility.ShowProgress();
            System.out.println("Device_Server_ID" + mVoUnInstall.getUninst_device_server_id());
            Call<String> mVoLastInstallationCall = mActivity.mApiService.getTestDetailsAPI(mVoUnInstall.getUninst_device_server_id());
            System.out.println("URL-" + mVoLastInstallationCall.request().url().toString());
            mVoLastInstallationCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
//                    nextBT.setEnabled(true);
                    isTestDeviceByUser = true;
                    mActivity.mIsTestDeviceCheck = true;
                    if (isAdded()) {
                        mActivity.mUtility.HideProgress();
                        Gson gson = new Gson();
                        if (response.code() == 200 || response.isSuccessful()) {
                            System.out.println("response Test---------" + response.body());
                            if (response.body() != null && !response.body().equalsIgnoreCase("") && !response.body().equalsIgnoreCase("null")) {
                                VoLastInstallation mVoLastInstallation = gson.fromJson(response.body(), VoLastInstallation.class);
                                if (mVoLastInstallation != null) {
                                    if (mVoLastInstallation.getGeneratedDate() != null && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("") && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("null")) {
                                        try {
                                            System.out.println("generatedDate - " + mVoLastInstallation.getGeneratedDate());
                                            Calendar calCurrentDate = Calendar.getInstance(Locale.getDefault());
                                            Calendar cal = Calendar.getInstance(Locale.getDefault());
                                            cal.setTimeInMillis(Long.parseLong(mVoLastInstallation.getGeneratedDate()));
                                            long different = calCurrentDate.getTimeInMillis() - cal.getTimeInMillis();
                                            mActivity.mStringTestDeviceReportedDate = mSimpleDateFormatDateDisplay.format(cal.getTime());
                                            mTextViewDeviceReportedDate.setText(mActivity.mStringTestDeviceReportedDate);
                                            if (different <= 7200000) {
                                                mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_ready_to_go));
                                                mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen));
                                                mActivity.mTestDeviceStatus = 2;
                                            } else {
                                                mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_no_reports));
                                                mTextViewDeviceStatus.setTextColor(ContextCompat.getColor(mActivity, R.color.colorRed));
                                                mActivity.mTestDeviceStatus = 3;
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                                        }
                                    }
                                } else {
                                    mActivity.mUtility.errorDialog("Device has never submitted any positional data to the system");
                                }
                            } else {
                                mActivity.mUtility.errorDialog("Device has never submitted any positional data to the system");
                            }
                        } else {
                            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
//                    nextBT.setEnabled(true);
                    isTestDeviceByUser = true;
                    mActivity.mIsTestDeviceCheck = true;
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
                }
            });
        }
    }

    /*Call Api to add un install record to server*/
    public void AddUnInstallationAPI() {
        if (mVoUnInstall != null) {
            mActivity.mUtility.hideKeyboard(mActivity);
            mActivity.mUtility.ShowProgress();

            JsonObject jsonObject = new JsonObject();
            JsonObject jsonObjectContact = new JsonObject();
            jsonObjectContact.addProperty("name", TextUtils.isEmpty(mStringOwnerName) ? "NA" : mStringOwnerName);
            jsonObjectContact.addProperty("address", TextUtils.isEmpty(mStringOwnerAddress) ? "NA" : mStringOwnerAddress);
            jsonObjectContact.addProperty("city", TextUtils.isEmpty(mStringOwnerCity) ? "NA" : mStringOwnerCity);
            jsonObjectContact.addProperty("state", TextUtils.isEmpty(mStringOwnerState) ? "NA" : mStringOwnerState);
            jsonObjectContact.addProperty("zipCode", TextUtils.isEmpty(mStringOwnerZipcode) ? "NA" : mStringOwnerZipcode);
            jsonObjectContact.addProperty("email", TextUtils.isEmpty(mStringOwnerEmail) ? "NA" : mStringOwnerEmail);
            jsonObjectContact.addProperty("telephone", TextUtils.isEmpty(mStringOwnerPhoneNo) ? "NA" : mStringOwnerPhoneNo);
            jsonObjectContact.addProperty("occupation", "NA");
            jsonObject.add("contactInfo", jsonObjectContact);
            System.out.println("mHashMap-" + jsonObject.toString());
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());

            Call<String> mAddUnInstallationCall = mActivity.mApiService.saveUnInstallationData(mVoUnInstall.getUninst_device_server_id(), body);

            System.out.println("URL-" + mAddUnInstallationCall.request().url().toString());
            mAddUnInstallationCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (isAdded()) {
                        mActivity.mUtility.HideProgress();
                        Gson gson = new Gson();
                        System.out.println("response " + response.code());
                        if (response.code() == 200 || response.isSuccessful()) {
                            System.out.println("response mAddUnInstData---------" + response.body());
                            VoInstallationResponse mVoAddUnInstallation = gson.fromJson(response.body(), VoInstallationResponse.class);
                            String json = gson.toJson(mVoAddUnInstallation);
                            Calendar cal = Calendar.getInstance();

                            ContentValues mContentValues = new ContentValues();
                            mVoUnInstall.setUninst_server_id(mVoAddUnInstallation.getId());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallServerId, mVoAddUnInstallation.getId());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselServerId, mVoAddUnInstallation.getRealAssetId());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerName, mStringOwnerName);
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerAddress, mStringOwnerAddress);
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerCity, mStringOwnerCity);
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerState, mStringOwnerState);
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode, mStringOwnerZipcode);
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerEmail, mStringOwnerEmail);
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo, mStringOwnerPhoneNo);
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalSignUrl, mStringOwnerSignatureImagePath);
//                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallServerSignUrl, mVoAddUnInstallation.getData().getSignaturepath());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallLocalUninstallerSignUrl, mStringInstallerSignatureImagePath);
//                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallServerUninstallerSignUrl, mVoAddUnInstallation.getData().getUn_sign_image());
//                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallPdfUrl, mVoAddUnInstallation.getData().getPdfpath());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, mVoAddUnInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp, mVoAddUnInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDateTime, mVoAddUnInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallStatus, "1");
                            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallIsSync, "0");
//                            //Vessel
//                            ContentValues mContentValuesVessel = new ContentValues();
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselServerId, mVoAddUnInstallation.getData().getVessel_id());
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselName, mVoUnInstall.getUninst_vessel_name());
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselPortNo, mVoUnInstall.getUninst_vessel_regi_no());
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselIsSync, "1");
//                            String isExistInDB = CheckRecordExistInVesselDB(mVoAddUnInstallation.getData().getVessel_id());
//                            if (isExistInDB.equalsIgnoreCase("-1")) {
//                                mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselCreatedDate, mActivity.mDateFormatDb.format(currentLocalTime));
//                                mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselUpdatedDate, mActivity.mDateFormatDb.format(currentLocalTime));
//                                int isInsertSocket = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableVesselAsset, mContentValuesVessel);
//                                if (isInsertSocket != -1) {
//                                    System.out.println("Vessel Added In Local Db");
//                                    mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselLocalId, isInsertSocket);
//                                } else {
//                                    System.out.println("Vessel Failed Adding In Local DB");
//                                }
//                            } else {
//                                String[] mArray = new String[]{isExistInDB};
//                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableVesselAsset, mContentValuesVessel, mActivity.mDbHelper.mFieldVesselServerId + "=?", mArray);
//                                System.out.println("Vessel updated In Local Db");
//                            }

//                            try {
//                                Date mDatelocalDate = mSimpleDateFormatDate.parse(mVoUnInstall.getUninst_date_time());
//                                System.out.println("TimeStamp-" + mDatelocalDate.getTime());
//                                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp, mDatelocalDate.getTime());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            String isUnInstallExistInDB = CheckRecordExistInUnInstallDB(mActivity.mIntUnInstallationId + "");
                            if (isUnInstallExistInDB.equalsIgnoreCase("-1")) {
                                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDateTime, cal.getTimeInMillis() + "");
                                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, cal.getTimeInMillis() + "");
                                int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues);
                                if (isInsertInstall != -1) {
                                    System.out.println("Device UnInstall Added In Local Db");
                                } else {
                                    System.out.println("Device UnInstall Adding In Local DB");
                                }
                            } else {
                                String[] mArray = new String[]{isUnInstallExistInDB};
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
                                System.out.println("Device UnInstall updated In Local Db");
                            }
                            mActivity.getUnSyncedCount();
                            new InstallationPhotoUploadAsyncTask().execute("");
                        } else {
                            try {
                                final String errorResponse = (response.errorBody().string());
                                System.out.println("response Error---------" + errorResponse);
                                Object json = new JSONTokener(errorResponse).nextValue();
                                if (json instanceof JSONObject) {
                                    System.out.println("JSON_OBJECT");
                                    showErrorMessage(errorResponse);
                                } else if (json instanceof JSONArray) {
                                    System.out.println("JSONArray");
                                    TypeToken<List<VoResponseError>> token = new TypeToken<List<VoResponseError>>() {
                                    };
                                    List<VoResponseError> mErrorResponse = gson.fromJson(errorResponse, token.getType());
                                    if (mErrorResponse != null) {
                                        if (mErrorResponse.size() > 0) {
                                            if (mErrorResponse.get(0).getErrorCode().contains("device.cannot.uninstall")) {
                                                mActivity.mUtility.errorDialog("This device can not be uninstalled at this time.");
                                            } else if (mErrorResponse.get(0).getErrorCode().contains("Pattern.deviceUninstallDto.contactInfo.telephone")) {
                                                System.out.println("JSONArray");
                                                mActivity.mUtility.errorDialog("Please enter valid mobile no.");
                                            } else if (mErrorResponse.get(0).getErrorCode().contains("NotBlank.deviceUninstallDto.contactInfo.zipCode")) {
                                                mActivity.mUtility.errorDialog("Please enter valid zip code.");
                                            } else {
                                                showErrorMessage(errorResponse);
                                            }
                                        } else {
                                            showErrorMessage(errorResponse);
                                        }
                                    } else {
                                        showErrorMessage(errorResponse);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                                try {
                                    showErrorMessage(response.errorBody().string());
                                } catch (Exception es) {
                                    es.printStackTrace();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                try {
                                    showErrorMessage(response.errorBody().string());
                                } catch (Exception es) {
                                    es.printStackTrace();
                                }

                            }

                        }
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    if (isAdded()) {
                        mActivity.mUtility.HideProgress();
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
                    }
                }
            });
        }
    }

    /*Upload image*/
    class InstallationPhotoUploadAsyncTask extends AsyncTask<String, Void, String> {
        ArrayList<VoInstallationPhoto> mArrayListPhoto = new ArrayList<>();
        int currentLoopPosition = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            currentLoopPosition = 0;
            mArrayListPhoto = new ArrayList<>();
            mActivity.mUtility.hideKeyboard(mActivity);
            mActivity.mUtility.ShowProgress();
        }

        @Override
        protected String doInBackground(String... params) {
            System.out.println("getUninst_owner_sign_sync" + mVoUnInstall.getUninst_owner_sign_sync());
            System.out.println("getUninst_installer_sign_sync" + mVoUnInstall.getUninst_installer_sign_sync());
            System.out.println("getUninst_local_sign_url" + mVoUnInstall.getUninst_local_sign_url());
            System.out.println("getUninst_local_uninstaller_sign_url" + mVoUnInstall.getUninst_local_uninstaller_sign_url());
            if (mVoUnInstall.getUninst_owner_sign_sync().equalsIgnoreCase("0")) {
                VoInstallationPhoto mVoInstallationPhoto = new VoInstallationPhoto();
                mVoInstallationPhoto.setInst_photo_local_url(mVoUnInstall.getUninst_local_sign_url());
                mVoInstallationPhoto.setIsSignature(true);
                mVoInstallationPhoto.setQuestionFile(false);
                mVoInstallationPhoto.setIsOwnerSignature(true);
                mArrayListPhoto.add(mVoInstallationPhoto);
            }
            if (mVoUnInstall.getUninst_installer_sign_sync().equalsIgnoreCase("0")) {
                VoInstallationPhoto mVoInstallationPhoto = new VoInstallationPhoto();
                mVoInstallationPhoto.setInst_photo_local_url(mVoUnInstall.getUninst_local_uninstaller_sign_url());
                mVoInstallationPhoto.setIsSignature(true);
                mVoInstallationPhoto.setQuestionFile(false);
                mVoInstallationPhoto.setIsOwnerSignature(false);
                mArrayListPhoto.add(mVoInstallationPhoto);
            }
            VoInstallationPhoto mVoInstallationPhoto = new VoInstallationPhoto();
            mVoInstallationPhoto.setInst_photo_local_url(mFileAppDirectory.getAbsolutePath() + "/" + "health-and-safety.txt");
            mVoInstallationPhoto.setIsSignature(false);
            mVoInstallationPhoto.setIsOwnerSignature(false);
            mVoInstallationPhoto.setQuestionFile(true);
            mArrayListPhoto.add(mVoInstallationPhoto);
            if (mArrayListPhoto.size() > 0) {
                forwardLoop();
            }
            return "";
        }

        private String forwardLoop() {
            System.out.println("SIZE-" + mArrayListPhoto.size());
            if (currentLoopPosition >= mArrayListPhoto.size()) {
                return "";
            }
            System.out.println("CALL-" + currentLoopPosition);
            if (mArrayListPhoto.get(currentLoopPosition).getInst_photo_local_url() != null && !mArrayListPhoto.get(currentLoopPosition).getInst_photo_local_url().equalsIgnoreCase("")) {
                System.out.println("mFileImagePath-" + mArrayListPhoto.get(currentLoopPosition).getInst_photo_local_url());
                File mFileImagePath = new File(mArrayListPhoto.get(currentLoopPosition).getInst_photo_local_url());
                System.out.println("mFileImagePath-" + mFileImagePath.getAbsolutePath());
                if (mFileImagePath != null && mFileImagePath.exists()) {
                    AddInstallationPhotoAPI(mArrayListPhoto.get(currentLoopPosition));
                } else {
                    mActivity.mUtility.HideProgress();
                    System.out.println("NOTFOUND-");
                    mActivity.mUtility.errorDialog("There is some signature is missing.");
                }
            } else {
                System.out.println("EMPTY-");
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog("There is some signature is missing.");
            }
            return "";
        }

        /*Call API to upload uninstall photo*/
        private String AddInstallationPhotoAPI(final VoInstallationPhoto mVoInstallationPhoto) {
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), new File(mVoInstallationPhoto.getInst_photo_local_url()));
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("file", new File(mVoInstallationPhoto.getInst_photo_local_url()).getAbsolutePath(), requestFile);
            System.out.println("getUninst_server_id" + mVoUnInstall.getUninst_server_id());
            Call<Void> mAddInstallationLogin = mActivity.mApiService.saveInstallationPhotoData(mVoUnInstall.getUninst_server_id(), body);

            System.out.println("URL-" + mAddInstallationLogin.request().url().toString());
            mAddInstallationLogin.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (isAdded()) {
                        if (response.code() == 200 || response.isSuccessful()) {
                            Void mVoAddPhotoInstallation = response.body();
                            Gson gson = new Gson();
                            String json = gson.toJson(mVoAddPhotoInstallation);
                            System.out.println("response mAddInstPhotoData---------" + json);
                            if (mVoInstallationPhoto.getIsSignature()) {
                                ContentValues mContentValuesInst = new ContentValues();
                                if (mVoInstallationPhoto.getIsOwnerSignature()) {
                                    mVoUnInstall.setUninst_owner_sign_sync("1");
                                    mContentValuesInst.put(mActivity.mDbHelper.mFieldUnInstallIsSyncOwnerSign, "1");
                                } else {
                                    mVoUnInstall.setUninst_installer_sign_sync("1");
                                    mContentValuesInst.put(mActivity.mDbHelper.mFieldUnInstallIsSyncInstallerSign, "1");
                                }
                                String[] mArray = new String[]{mActivity.mIntUnInstallationId + ""};
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValuesInst, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
                                System.out.println("Signature updated In Local Db");
                            }

                            System.out.println("currentLoopPosition END-" + currentLoopPosition);
                            System.out.println("mArrayListPhoto END-" + (mArrayListPhoto.size() - 1) + "");
                            if (currentLoopPosition == mArrayListPhoto.size() - 1) {
                                System.out.println("FINISH" + currentLoopPosition);
                                if (mActivity.mUtility.haveInternet()) {
                                    completeUninstallAPI(false);
                                } else {
                                    mActivity.mUtility.HideProgress();
                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                                }

//                                mActivity.mUtility.HideProgress();
//                                ContentValues mContentValuesInst = new ContentValues();
//                                mContentValuesInst.put(mActivity.mDbHelper.mFieldUnInstallStatus, "1");
//                                mContentValuesInst.put(mActivity.mDbHelper.mFieldUnInstallIsSync, "1");
//                                String[] mArray = new String[]{mActivity.mIntUnInstallationId + ""};
//                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValuesInst, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
//                                System.out.println("Installer updated In Local Db");
//                                mActivity.getUnSyncedCount();
//                                showSuccessMessage();
                            } else {
                                currentLoopPosition++;
                                forwardLoop();
                            }
                        } else {
                            mActivity.mUtility.HideProgress();
                            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                        }
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
                }
            });
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (mArrayListPhoto.size() == 0) {
                System.out.println("Finish");
                if (mActivity.mUtility.haveInternet()) {
                    completeUninstallAPI(false);
                } else {
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                }
            }
//            mActivity.mUtility.HideProgress();
//            if (result.equalsIgnoreCase("fail")) {
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//            } else {
//                showSuccessMessage();
//            }
        }
    }

    /*Call API to complete un install record */
    private void completeUninstallAPI(boolean isShowProgress) {
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress();
        }
        Call<VoInstallationResponse> mInstallationComplete = mActivity.mApiService.completeInstallationHistory(mVoUnInstall.getUninst_server_id());

        System.out.println("URL-" + mInstallationComplete.request().url().toString());
        mInstallationComplete.enqueue(new Callback<VoInstallationResponse>() {
            @Override
            public void onResponse(Call<VoInstallationResponse> call, Response<VoInstallationResponse> response) {
                mActivity.mUtility.HideProgress();
                if (isAdded()) {
                    final VoInstallationResponse mInstallComplete = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mInstallComplete);
                    System.out.println("response mInstallComplete---------" + json);
                    if (response.code() == 200 || response.isSuccessful()) {
                        if (mInstallComplete != null) {
                            mActivity.mUtility.HideProgress();
                            ContentValues mContentValuesInst = new ContentValues();
                            mContentValuesInst.put(mActivity.mDbHelper.mFieldUnInstallStatus, "1");
                            mContentValuesInst.put(mActivity.mDbHelper.mFieldUnInstallIsSync, "1");
                            String[] mArray = new String[]{mActivity.mIntUnInstallationId + ""};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValuesInst, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
                            System.out.println("Installer updated In Local Db");
                            mActivity.getUnSyncedCount();
                            showSuccessMessage();
                        }
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                    }
                }
            }

            @Override
            public void onFailure(Call<VoInstallationResponse> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
            }
        });
    }

    /*Show error dialog message*/
    private void showErrorMessage(final String errorResponse) {
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_server_error_someting_wrong), new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                mActivity.mUtility.errorDialogWithYesNoCallBack("Bug Report", "Do you want to report this bug to the developer team?", mActivity.getString(R.string.str_report), mActivity.getString(R.string.str_cancel), true, new onAlertDialogCallBack() {
                    @Override
                    public void PositiveMethod(DialogInterface dialog, int id) {
                        try {
                            Calendar cal = Calendar.getInstance();
                            Date currentLocalTime = cal.getTime();
                            Intent sendIntent = new Intent(Intent.ACTION_SEND);
                            sendIntent.setData(Uri.parse("mailto:"));
                            sendIntent.setType("text/plain");
                            sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tom@succorfish.com", "kalpesh@succorfish.com", "jaydip@succorfish.com"});
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Report : Uninstall " + mDateFormatDb.format(currentLocalTime));
                            sendIntent.putExtra(Intent.EXTRA_TEXT, errorResponse);
//                                                            sendIntent.putExtra(Intent.EXTRA_CC, msgData);
                            if (sendIntent.resolveActivity(mActivity.getPackageManager()) != null) {
                                mActivity.startActivity(sendIntent);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void NegativeMethod(DialogInterface dialog, int id) {

                    }
                });
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    /*Show success dialog message*/
    private void showSuccessMessage() {
        if (mListener != null) {
            mListener.onNextTwoPressed(this);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
        String msg = getResources().getString(R.string.str_uninstall_successfully);
        if (!mActivity.mUtility.haveInternet()) {
            msg = getResources().getString(R.string.str_report_submitted);
        }
        builder.setMessage(msg).setCancelable(false).setPositiveButton(mActivity.getString(R.string.str_ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
//                mActivity.onBackPressedDirect();
                mActivity.removeAllFragmentFromBack("Finish_Back");
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /*Check record exist or not*/
    public String CheckRecordExistInVesselDB(String serverVesselId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableVesselAsset + " where " + mActivity.mDbHelper.mFieldVesselServerId + "= '" + serverVesselId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" VesselList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldVesselServerId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }
    /*Check record exist or not*/
    public String CheckRecordExistInUnInstallDB(String localUnInstallId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableUnInstall + " where " + mActivity.mDbHelper.mFieldUnInstallLocalId + "= '" + localUnInstallId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldUnInstallLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    @Override
    public void onResume() {
        super.onResume();
        backBT.setOnClickListener(this);
        nextBT.setOnClickListener(this);
        mRelativeLayoutTestDevice.setOnClickListener(this);
        mRelativeLayoutSignature.setOnClickListener(this);
        mRelativeLayoutWarrantyReturn.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        backBT.setOnClickListener(null);
        nextBT.setOnClickListener(null);
        mRelativeLayoutTestDevice.setOnClickListener(null);
        mRelativeLayoutSignature.setOnClickListener(null);
        mRelativeLayoutWarrantyReturn.setOnClickListener(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof OnStepTwoListener) {
                mListener = (OnStepTwoListener) context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnStepTwoListener {
        void onBackTwoPressed(Fragment fragment);

        void onNextTwoPressed(Fragment fragment);

    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        backBT = null;
        nextBT = null;
        mRelativeLayoutTestDevice = null;
        mRelativeLayoutSignature = null;
        mRelativeLayoutWarrantyReturn = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mUtility.hideKeyboard(mActivity);
    }

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

    private void createAllFolderDir() {
        mFileSDCard = Environment.getExternalStorageDirectory();
        mFileAppDirectory = new File(mFileSDCard.getAbsolutePath() + URLCLASS.DIRECTORY_FOLDER_NAME);
        if (!mFileAppDirectory.exists()) {
            mFileAppDirectory.mkdirs();
        }
    }
}
