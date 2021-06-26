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
import com.succorfish.installer.Vo.VoInstallation;
import com.succorfish.installer.Vo.VoInstallationPhoto;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoLastInstallation;
import com.succorfish.installer.Vo.VoResponseError;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.URLCLASS;
import com.succorfish.installer.interfaces.onAlertDialogCallBack;
import com.succorfish.installer.interfaces.onBackPressWithAction;

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

public class FragmentNewInstallationThree extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;
    Button backBT;
    Button nextBT;
    RelativeLayout mRelativeLayoutSignature;
    RelativeLayout mRelativeLayoutTestDevice;
    //    CountryCodePicker countryCodePicker;
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
    String mStringOccuption = "";
    String mStringOwnerEmail = "";
    String mStringOwnerPhoneNo = "";
    String mStringOwnerSignatureImagePath = "";
    String mStringInstallerSignatureImagePath = "";
    private OnStepThreeListener mListener;

    private File mFileOwnerSignaturePath;
    private File mFileInstallerSignaturePath;
    VoInstallation mVoInstallation;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    private SimpleDateFormat mDateFormatDb;
    private boolean isTestDeviceByUser = false;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private File mFileSDCard;
    private File mFileAppDirectory;

    public FragmentNewInstallationThree() {
        // Required empty public constructor
    }

    public static FragmentNewInstallationThree newInstance() {
        return new FragmentNewInstallationThree();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        System.out.println("mIntInstallationId-" + mActivity.mIntInstallationId);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_new_installation_three, container, false);

        backBT = mViewRoot.findViewById(R.id.backBT);
        nextBT = mViewRoot.findViewById(R.id.nextBT);
        mRelativeLayoutSignature = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_three_relativelayout_signature);
        mRelativeLayoutTestDevice = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_three_rl_test_device);
        mEditTextOwnerName = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_three_editext_owner_name);
        mEditTextOwnerAddress = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_three_editext_address_one);
        mEditTextOwnerCity = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_three_editext_owner_city);
        mEditTextOwnerState = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_three_editext_owner_state);
        mEditTextOwnerZipcode = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_three_editext_owner_zipcode);
        mEditTextOwnerEmail = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_three_editext_owner_email);
        mEditTextOwnerPhoneNo = (EditText) mViewRoot.findViewById(R.id.fragment_new_ins_three_editext_owner_phoneno);
        mTextViewDeviceStatus = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_three_tv_test_device_status);
        mTextViewDeviceReportedDate = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_three_tv_test_device_reported_date);
//        countryCodePicker = (CountryCodePicker) mViewRoot.findViewById(R.id.fragment_new_ins_three_country_picker);
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        System.out.println("THIRD");
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
        /*get Installation record using id*/
        getDBInstallationList(true);
//        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
//            @Override
//            public void onFragmentBackPress(Fragment mFragment) {
//                if (mFragment instanceof FragmentNewInstallation) {
//                    System.out.println("BackKKThree");
//                    backBT.performClick();
//                    int currentPage = mActivity.mViewPagerInstallation.getCurrentItem();
//                    if (currentPage == 2) {
//                        mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
//                        mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
//                        mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
//                        mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();
//                        mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
//                        mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
//                        mStringOwnerZipcode = mEditTextOwnerZipcode.getText().toString().trim();
//
//                        Calendar calBack = Calendar.getInstance();
//                        ContentValues mContentValuesBack = new ContentValues();
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerName, mStringOwnerName);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerAddress, mStringOwnerAddress);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerCity, mStringOwnerCity);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerState, mStringOwnerState);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerZipcode, mStringOwnerZipcode);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerEmail, mStringOwnerEmail);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerMobileNo, mStringOwnerPhoneNo);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocalSignUrl, mStringOwnerSignatureImagePath);
//                        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl, mStringInstallerSignatureImagePath);
//                        String isExistInDBBack = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
//                        if (isExistInDBBack.equalsIgnoreCase("-1")) {
//                            mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCreatedDate, calBack.getTime() + "");
//                            int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack);
//                            if (isInsertInstall != -1) {
//                                System.out.println("Device Install Added In Local Db");
//                            } else {
//                                System.out.println("Device Install Adding In Local DB");
//                            }
//                        } else {
//                            mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, calBack.getTime() + "");
//                            String[] mArray = new String[]{isExistInDBBack};
//                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
//                            System.out.println("Device updated In Local Db");
//                        }
//                    }
//                    mActivity.mViewPagerInstallation.setCurrentItem(1, true);
//                }
//            }
//        });

        return mViewRoot;
    }

    /*get installation record by id*/
    private void getDBInstallationList(boolean isFromEdit) {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallIsSync + "= 0" + " AND " + mActivity.mDbHelper.mFieldInstallLocalId + "= '" + mActivity.mIntInstallationId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoInstallation = new VoInstallation();
                    mVoInstallation.setInst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalId));
                    mVoInstallation.setInst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerId));
                    mVoInstallation.setInst_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallUserId));
                    mVoInstallation.setInst_device_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceIMEINo));
                    mVoInstallation.setInst_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceServerId));
                    mVoInstallation.setInst_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceLocalId));
                    mVoInstallation.setInst_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDevicName));
                    mVoInstallation.setInst_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceWarranty_status));
                    mVoInstallation.setInst_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceTypeName));
                    mVoInstallation.setInst_help_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallHelpNo));
                    mVoInstallation.setInst_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDateTime));
                    mVoInstallation.setInst_latitude(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLatitude));
                    mVoInstallation.setInst_longitude(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLongitude));
                    mVoInstallation.setInst_country_code(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCountryCode));
                    mVoInstallation.setInst_country_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCountryName));
                    mVoInstallation.setInst_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselLocalId));
                    mVoInstallation.setInst_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselServerId));
                    mVoInstallation.setInst_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselName));
                    mVoInstallation.setInst_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallVesselRegNo));
                    mVoInstallation.setInst_power(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallPower));
                    mVoInstallation.setInst_location(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocation));
                    mVoInstallation.setInst_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerName));
                    mVoInstallation.setInst_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerAddress));
                    mVoInstallation.setInst_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerCity));
                    mVoInstallation.setInst_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerState));
                    mVoInstallation.setInst_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerZipcode));
                    mVoInstallation.setInst_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerEmail));
                    mVoInstallation.setInst_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallOwnerMobileNo));
                    mVoInstallation.setInst_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalSignUrl));
                    mVoInstallation.setInst_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerSignUrl));
                    mVoInstallation.setInst_local_installer_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl));
                    mVoInstallation.setInst_server_installer_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallServerInstallerSignUrl));
                    mVoInstallation.setInst_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallPdfUrl));
                    mVoInstallation.setInst_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallCreatedDate));
                    mVoInstallation.setInst_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallUpdatedDate));
                    mVoInstallation.setInst_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallIsSync));
                    mVoInstallation.setInst_owner_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallIsSyncOwnerSign));
                    mVoInstallation.setInst_installer_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallIsSyncInstallerSign));
                    mVoInstallation.setIs_install(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallStatus));
                    mVoInstallation.setInst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDateTimeStamp));
                    if (isFromEdit) {
                        mStringOwnerName = mVoInstallation.getInst_owner_name();
                        mStringOwnerAddress = mVoInstallation.getInst_owner_address();
                        mStringOwnerCity = mVoInstallation.getInst_owner_city();
                        mStringOwnerState = mVoInstallation.getInst_owner_state();
                        mStringOwnerZipcode = mVoInstallation.getInst_owner_zipcode();
                        mStringOwnerEmail = mVoInstallation.getInst_owner_email();
                        mStringOwnerPhoneNo = mVoInstallation.getInst_owner_mobile_no();
                        mStringOwnerSignatureImagePath = mVoInstallation.getInst_local_sign_url();
                        mStringInstallerSignatureImagePath = mVoInstallation.getInst_local_installer_sign_url();
                        mEditTextOwnerName.setText(mStringOwnerName);
                        mEditTextOwnerAddress.setText(mStringOwnerAddress);
                        mEditTextOwnerEmail.setText(mStringOwnerEmail);
                        mEditTextOwnerPhoneNo.setText(mStringOwnerPhoneNo);
                        mEditTextOwnerCity.setText(mStringOwnerCity);
                        mEditTextOwnerState.setText(mStringOwnerState);
                        mEditTextOwnerZipcode.setText(mStringOwnerZipcode);
                    }
                    Gson gson = new Gson();
                    String json = gson.toJson(mVoInstallation);
                    System.out.println("GETT mAddInstData---------" + json);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Handle Back press event*/
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
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerName, mStringOwnerName);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerAddress, mStringOwnerAddress);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerCity, mStringOwnerCity);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerState, mStringOwnerState);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerZipcode, mStringOwnerZipcode);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerEmail, mStringOwnerEmail);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerMobileNo, mStringOwnerPhoneNo);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocalSignUrl, mStringOwnerSignatureImagePath);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl, mStringInstallerSignatureImagePath);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, calBack.getTimeInMillis() + "");
        String[] mArray = new String[]{mActivity.mIntInstallationId + ""};
        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
        System.out.println("Device updated In Local Db");
        System.out.println("3-BACK-JD");
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.backBT:
                /*on Back click*/
                mStringOwnerName = mEditTextOwnerName.getText().toString().trim();
                mStringOwnerAddress = mEditTextOwnerAddress.getText().toString().trim();
                mStringOwnerEmail = mEditTextOwnerEmail.getText().toString().trim();
                mStringOwnerPhoneNo = mEditTextOwnerPhoneNo.getText().toString().trim();
                mStringOwnerCity = mEditTextOwnerCity.getText().toString().trim();
                mStringOwnerState = mEditTextOwnerState.getText().toString().trim();
                mStringOwnerZipcode = mEditTextOwnerZipcode.getText().toString().trim();

                Calendar calBack = Calendar.getInstance();
                ContentValues mContentValuesBack = new ContentValues();
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerName, mStringOwnerName);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerAddress, mStringOwnerAddress);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerCity, mStringOwnerCity);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerState, mStringOwnerState);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerZipcode, mStringOwnerZipcode);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerEmail, mStringOwnerEmail);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallOwnerMobileNo, mStringOwnerPhoneNo);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocalSignUrl, mStringOwnerSignatureImagePath);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl, mStringInstallerSignatureImagePath);
                String isExistInDBBack = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
                if (isExistInDBBack.equalsIgnoreCase("-1")) {
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallCreatedDate, calBack.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack);
                    if (isInsertInstall != -1) {
                        System.out.println("Device Install Added In Local Db");
                    } else {
                        System.out.println("Device Install Adding In Local DB");
                    }
                } else {
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, calBack.getTimeInMillis() + "");
                    String[] mArray = new String[]{isExistInDBBack};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesBack, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                    System.out.println("Device updated In Local Db");
                }
                if (mListener != null)
                    mListener.onBackThreePressed(this);
                break;
            case R.id.fragment_new_ins_three_relativelayout_signature:
                FragmentSignature mFramentSignature = new FragmentSignature();
                Bundle mBundle = new Bundle();
                mBundle.putString("intent_owner_signature", mStringOwnerSignatureImagePath);
                mBundle.putString("intent_installer_signature", mStringInstallerSignatureImagePath);
                mBundle.putInt("intent_is_from", 1);
                mFramentSignature.setOnScanResultSet(new onBackPressWithAction() {
                    @Override
                    public void onBackWithAction(String scanResult) {

                    }

                    @Override
                    public void onBackWithAction(String value1, String value2) {
                        mStringOwnerSignatureImagePath = value1;
                        mStringInstallerSignatureImagePath = value2;
                        System.out.println("mStringOwnerSignatureImagePath-" + mStringOwnerSignatureImagePath);
                        System.out.println("mStringInstallerSignatureImagePath-" + mStringInstallerSignatureImagePath);
                        mVoInstallation.setInst_local_sign_url(mStringOwnerSignatureImagePath);
                        mVoInstallation.setInst_local_installer_sign_url(mStringInstallerSignatureImagePath);
                    }

                    @Override
                    public void onBackWithAction(String imei, String deviceType, String warrantyStatus) {

                    }
                });
                mActivity.replacesFragment(mFramentSignature, true, mBundle, 1);
                break;
            case R.id.fragment_new_ins_three_rl_test_device:
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

//                if (countryCodePicker != null && !countryCodePicker.isValidFullNumber()) {
//                    mActivity.mUtility.errorDialog("Enter valid phoneno");
//                    System.out.println("Not Valid");
//                    return;
//                } else {
//                    System.out.println("Valid");
//                }

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
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_three_sign_inspector_missing_image));
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
//                if (mActivity.mUtility.haveInternet()) {
//                    if (!mActivity.mIsTestDeviceCheck) {
//                        mActivity.mUtility.errorDialogWithCallBack("You can not submit this report as no reports in past 2 hours", new onAlertDialogCallBack() {
//                            @Override
//                            public void PositiveMethod(DialogInterface dialog, int id) {
//
//                            }
//
//                            @Override
//                            public void NegativeMethod(DialogInterface dialog, int id) {
//
//                            }
//                        });
//                        return;
//                    }
//                }
                /*Save Question ans in file*/
                saveQuestionFile();
                getDBInstallationList(false);
                if (!mActivity.mUtility.haveInternet()) {
                    Calendar cal = Calendar.getInstance();
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerName, mStringOwnerName);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerAddress, mStringOwnerAddress);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerCity, mStringOwnerCity);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerState, mStringOwnerState);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerZipcode, mStringOwnerZipcode);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerEmail, mStringOwnerEmail);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerMobileNo, mStringOwnerPhoneNo);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalSignUrl, mStringOwnerSignatureImagePath);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl, mStringInstallerSignatureImagePath);
                    mContentValues.put(mActivity.mDbHelper.mFieldInstallStatus, "1");
                    String isExistInDB = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
                    if (isExistInDB.equalsIgnoreCase("-1")) {
                        mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, cal.getTimeInMillis() + "");
                        mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
                    } else {
                        mContentValues.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, cal.getTimeInMillis() + "");
                        String[] mArray = new String[]{isExistInDB};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                        System.out.println("Device updated In Local Db");
                    }
                    showSuccessMessage();
                } else {
                    try {
                        if (mVoInstallation.getInst_server_id() != null && !mVoInstallation.getInst_server_id().equalsIgnoreCase("") && !mVoInstallation.getInst_server_id().equalsIgnoreCase("null")) {
                            new InstallationPhotoUploadAsyncTask().execute("");
                        } else {
                            if (mVoInstallation.getInst_device_server_id() != null && !mVoInstallation.getInst_device_server_id().equalsIgnoreCase("") && !mVoInstallation.getInst_device_server_id().equalsIgnoreCase("null")) {
                                AddInstallationAPI();
                            } else {
                                mActivity.mUtility.errorDialog("Please rescan device. Device details is not valid");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /*Save Installation Question and Ans in file*/
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
            myOutWriter.append("            Installer Health and Safety Questionnaire          \r\n");
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

    /*Get Installation information*/
    private void getTestInstallInformation(boolean isShowAlert) {
        if (mVoInstallation != null) {
            mActivity.mUtility.hideKeyboard(mActivity);
            mActivity.mUtility.ShowProgress();
            System.out.println("Device_Server_ID" + mVoInstallation.getInst_device_server_id());
            Call<String> mVoLastInstallationCall = mActivity.mApiService.getTestDetailsAPI(mVoInstallation.getInst_device_server_id());
            System.out.println("URL-" + mVoLastInstallationCall.request().url().toString());
            mVoLastInstallationCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    mActivity.mIsTestDeviceCheck = true;
                    isTestDeviceByUser = true;
//                    nextBT.setEnabled(true);
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
                                                mTextViewDeviceReportedDate.setTextColor(ContextCompat.getColor(mActivity, R.color.colorGreen));
                                                mActivity.mTestDeviceStatus = 2;
                                            } else {
                                                mTextViewDeviceStatus.setText(getResources().getString(R.string.str_test_device_status_no_reports));
                                                mTextViewDeviceReportedDate.setTextColor(ContextCompat.getColor(mActivity, R.color.colorRed));
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
                    mActivity.mIsTestDeviceCheck = true;
//                    nextBT.setEnabled(true);
                    isTestDeviceByUser = true;
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
                }
            });
        }
    }

    /*Call API to Add installation data*/
    public void AddInstallationAPI() {
        if (mVoInstallation != null) {
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
            jsonObject.addProperty("assetId", mVoInstallation.getInst_vessel_server_id());
            jsonObject.addProperty("installationPlace", mVoInstallation.getInst_location());
            String mStringPower = "";
            if (mVoInstallation.getInst_power().equalsIgnoreCase("Constant 6-36V")) {
                mStringPower = "CONSTANT";
            } else if (mVoInstallation.getInst_power().equalsIgnoreCase("Periodic 6-36V")) {
                mStringPower = "PERIODIC";
            } else if (mVoInstallation.getInst_power().equalsIgnoreCase("Constant regulated 6-36V")) {
                mStringPower = "CONSTANT_REGULATED";
            } else if (mVoInstallation.getInst_power().equalsIgnoreCase("Periodic regulated 6-36V")) {
                mStringPower = "PERIODIC_REGULATED";
            } else if (mVoInstallation.getInst_power().equalsIgnoreCase("Solar")) {
                mStringPower = "SOLAR";
            } else if (mVoInstallation.getInst_power().equalsIgnoreCase("Internal rechargeable battery")) {
                mStringPower = "BATTERY";
            }
            jsonObject.addProperty("powerSource", mStringPower);
            System.out.println("mHashMap-" + jsonObject.toString());
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            System.out.println("mHashMapBody-" + body.toString());
            Call<String> mAddInstallationLogin = mActivity.mApiService.saveInstallationData(mVoInstallation.getInst_device_server_id(), body);

            System.out.println("URL-" + mAddInstallationLogin.request().url().toString());
            mAddInstallationLogin.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, final Response<String> response) {
                    if (isAdded()) {
                        mActivity.mUtility.HideProgress();
                        Gson gson = new Gson();
                        if (response.code() == 200 || response.isSuccessful()) {
                            System.out.println("response mAddInstData---------" + response.body());
                            VoInstallationResponse mVoAddInstallation = gson.fromJson(response.body(), VoInstallationResponse.class);
                            String json = gson.toJson(mVoAddInstallation);
                            System.out.println("response mAddInstData---------" + json);
                            Calendar cal = Calendar.getInstance();
//                            ContentValues mContentValuesVessel = new ContentValues();
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselServerId, mVoAddInstallation.getRealAssetId());
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselName, mVoAddInstallation.getRealAssetName());
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselPortNo, mVoAddInstallation.getRealAssetRegNo());
//                            mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselIsSync, "1");
//                            String isExistInDB = CheckRecordExistInVesselDB(mVoAddInstallation.getData().getVessel_id());
//                            if (isExistInDB.equalsIgnoreCase("-1")) {
//                                mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselCreatedDate, mActivity.mDateFormatDb.format(currentLocalTime));
//                                mContentValuesVessel.put(mActivity.mDbHelper.mFieldVesselUpdatedDate, mActivity.mDateFormatDb.format(currentLocalTime));
//                                int isInsertSocket = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableVesselAsset, mContentValuesVessel);
//                                if (isInsertSocket != -1) {
//                                    System.out.println("Vessel Added In Local Db");
//                                } else {
//                                    System.out.println("Vessel Failed Adding In Local DB");
//                                }
//                            } else {
//                                String[] mArray = new String[]{isExistInDB};
//                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableVesselAsset, mContentValuesVessel, mActivity.mDbHelper.mFieldVesselServerId + "=?", mArray);
//                                System.out.println("Vessel updated In Local Db");
//                            }

                            ContentValues mContentValues = new ContentValues();
                            mVoInstallation.setInst_server_id(mVoAddInstallation.getId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallServerId, mVoAddInstallation.getId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallVesselServerId, mVoAddInstallation.getRealAssetId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerName, mStringOwnerName);
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerAddress, mStringOwnerAddress);
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerCity, mStringOwnerCity);
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerState, mStringOwnerState);
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerZipcode, mStringOwnerZipcode);
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerEmail, mStringOwnerEmail);
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallOwnerMobileNo, mStringOwnerPhoneNo);

                            mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalSignUrl, mStringOwnerSignatureImagePath);
//                            mContentValues.put(mActivity.mDbHelper.mFieldInstallServerSignUrl, mVoAddInstallation.getData().getSignaturepath());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallLocalInstallerSignUrl, mStringInstallerSignatureImagePath);
//                            mContentValues.put(mActivity.mDbHelper.mFieldInstallServerInstallerSignUrl, mVoAddInstallation.getData().getIns_sign_image());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, mVoAddInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallDateTimeStamp, mVoAddInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallDateTime, mVoAddInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallStatus, "1");
                            mContentValues.put(mActivity.mDbHelper.mFieldInstallIsSync, "0");
//                            try {
//                                Date mDatelocalDate = mSimpleDateFormatDate.parse(mVoInstallation.getInst_date_time());
//                                System.out.println("TimeStamp-" + mDatelocalDate.getTime());
//                                mContentValues.put(mActivity.mDbHelper.mFieldInstallDateTimeStamp, mDatelocalDate.getTime());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            String isInstallExistInDB = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
                            if (isInstallExistInDB.equalsIgnoreCase("-1")) {
                                mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, cal.getTimeInMillis() + "");
                                int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
                                if (isInsertInstall != -1) {
                                    System.out.println("Device Install Added In Local Db");
                                } else {
                                    System.out.println("Device Install Adding In Local DB");
                                }
                            } else {
                                String[] mArray = new String[]{isInstallExistInDB};
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                                System.out.println("Device updated In Local Db");
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
                                            boolean isAnyConditionMatch = false;
                                            for (int i = 0; i < mErrorResponse.size(); i++) {
                                                if (mErrorResponse.get(i).getErrorCode().contains("device.cannot.install")) {
                                                    mActivity.mUtility.errorDialog("This device can not be installed at this time.");
                                                    isAnyConditionMatch = true;
                                                    break;
                                                } else if (mErrorResponse.get(i).getErrorCode().contains("device.state.inconsistent")) {
                                                    isAnyConditionMatch = true;
                                                    mActivity.mUtility.errorDialog("Device " + mVoInstallation.getInst_device_imei_no() + " is not correctly registered in the system. Please, contact administrator right away");
                                                    break;
                                                } else if (mErrorResponse.get(i).getErrorCode().contains("Pattern.deviceInstallDto.contactInfo.telephone")) {
                                                    isAnyConditionMatch = true;
                                                    mActivity.mUtility.errorDialog("Please enter valid mobile no.");
                                                    break;
                                                } else if (mErrorResponse.get(i).getErrorCode().contains("NotBlank.deviceInstallDto.contactInfo.zipCode")) {
                                                    isAnyConditionMatch = true;
                                                    mActivity.mUtility.errorDialog("Please enter valid zip code.");
                                                    break;
                                                }
                                            }
                                            if (!isAnyConditionMatch) {
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

    /*Upload image into installation*/
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

            if (mVoInstallation.getInst_owner_sign_sync().equalsIgnoreCase("0")) {
                VoInstallationPhoto mVoInstallationPhoto = new VoInstallationPhoto();
                mVoInstallationPhoto.setInst_photo_local_url(mStringOwnerSignatureImagePath);
                mVoInstallationPhoto.setIsSignature(true);
                mVoInstallationPhoto.setQuestionFile(false);
                mVoInstallationPhoto.setIsOwnerSignature(true);
                mArrayListPhoto.add(mVoInstallationPhoto);
            }
            if (mVoInstallation.getInst_installer_sign_sync().equalsIgnoreCase("0")) {
                VoInstallationPhoto mVoInstallationPhoto = new VoInstallationPhoto();
                mVoInstallationPhoto.setInst_photo_local_url(mStringInstallerSignatureImagePath);
                mVoInstallationPhoto.setIsSignature(true);
                mVoInstallationPhoto.setQuestionFile(false);
                mVoInstallationPhoto.setIsOwnerSignature(false);
                mArrayListPhoto.add(mVoInstallationPhoto);
            }

            DataHolder mDataHolder;
            try {
                String url = "select * from " + mActivity.mDbHelper.mTableInstallerPhoto + " where " + mActivity.mDbHelper.mFieldInstLocalId + "= '" + mActivity.mIntInstallationId + "'" + " AND " + mActivity.mDbHelper.mFieldInstPhotoIsSync + "= 0";
                System.out.println("Local url " + url);
                mDataHolder = mActivity.mDbHelper.read(url);
                if (mDataHolder != null) {
                    System.out.println("Local Photo List " + url + " : " + mDataHolder.get_Listholder().size());
                    VoInstallationPhoto mVoInstallationPhoto;
                    for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                        mVoInstallationPhoto = new VoInstallationPhoto();
                        mVoInstallationPhoto.setInst_photo_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoLocalID));
                        mVoInstallationPhoto.setInst_photo_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoServerID));
                        mVoInstallationPhoto.setInst_photo_local_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoLocalURL));
                        mVoInstallationPhoto.setInst_photo_server_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoServerURL));
                        mVoInstallationPhoto.setInst_photo_type(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoType));
                        mVoInstallationPhoto.setInst_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstLocalId));
                        mVoInstallationPhoto.setInst_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstServerId));
                        mVoInstallationPhoto.setInst_photo_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoUserId));
                        mVoInstallationPhoto.setInst_photo_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoCreatedDate));
                        mVoInstallationPhoto.setInst_photo_update_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoUpdateDate));
                        mVoInstallationPhoto.setIs_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstPhotoIsSync));
                        mVoInstallationPhoto.setIsSignature(false);
                        mVoInstallationPhoto.setQuestionFile(false);
                        if (mVoInstallationPhoto.getInst_photo_local_url() != null && !mVoInstallationPhoto.getInst_photo_local_url().equalsIgnoreCase("")) {
                            mVoInstallationPhoto.setIsHasImage(true);
                        } else {
                            mVoInstallationPhoto.setIsHasImage(false);
                        }
                        mArrayListPhoto.add(mVoInstallationPhoto);
//                        synchronized (this) {
//
//                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
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
                File mFileImagePath = new File(mArrayListPhoto.get(currentLoopPosition).getInst_photo_local_url());
                if (mFileImagePath != null && mFileImagePath.exists()) {
                    AddInstallationPhotoAPI(mArrayListPhoto.get(currentLoopPosition));
                } else {
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_add_missing_image));
                }
            } else {
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_add_missing_image));
            }
            return "";
        }

        /*Add Installation photo API*/
        private String AddInstallationPhotoAPI(final VoInstallationPhoto mVoInstallationPhoto) {
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), new File(mVoInstallationPhoto.getInst_photo_local_url()));
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("file", new File(mVoInstallationPhoto.getInst_photo_local_url()).getAbsolutePath(), requestFile);
            Call<Void> mAddInstallationLogin = mActivity.mApiService.saveInstallationPhotoData(mVoInstallation.getInst_server_id(), body);

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
                                    mVoInstallation.setInst_owner_sign_sync("1");
                                    mContentValuesInst.put(mActivity.mDbHelper.mFieldInstallIsSyncOwnerSign, "1");
                                } else {
                                    mVoInstallation.setInst_installer_sign_sync("1");
                                    mContentValuesInst.put(mActivity.mDbHelper.mFieldInstallIsSyncInstallerSign, "1");
                                }
                                String[] mArray = new String[]{mActivity.mIntInstallationId + ""};
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesInst, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                                System.out.println("Signature updated In Local Db");
                            } else {
                                if (mVoInstallationPhoto.getIsQuestionFile()) {

                                } else {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldInstServerId, mVoInstallation.getInst_server_id());
                                    mContentValues.put(mActivity.mDbHelper.mFieldInstPhotoUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                                    mContentValues.put(mActivity.mDbHelper.mFieldInstPhotoIsSync, "1");
                                    String isExistInDB = CheckRecordExistInInstallPhotoDB(mVoInstallationPhoto.getInst_local_id(), mVoInstallationPhoto.getInst_photo_type());
                                    if (isExistInDB.equalsIgnoreCase("-1")) {
//                                mContentValues.put(mActivity.mDbHelper.mFieldInstPhotoCreatedDate, mVoAddPhotoInstallation.getData().getCreated_at());
                                        mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstallerPhoto, mContentValues);
                                        System.out.println("Install Photo Added In Local Db");
                                    } else {
                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstallerPhoto, mContentValues, mActivity.mDbHelper.mFieldInstPhotoLocalID + "=?", new String[]{isExistInDB});
                                        System.out.println("Install Photo updated In Local Db");
                                    }
                                }
                            }

                            System.out.println("currentLoopPosition END-" + currentLoopPosition);
                            System.out.println("mArrayListPhoto END-" + (mArrayListPhoto.size() - 1) + "");
                            if (currentLoopPosition == mArrayListPhoto.size() - 1) {
                                System.out.println("FINISH" + currentLoopPosition);
                                if (mActivity.mUtility.haveInternet()) {
                                    completeInstallationAPI(false);
                                } else {
                                    mActivity.mUtility.HideProgress();
                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                                }
//                                mActivity.mUtility.HideProgress();
//                                ContentValues mContentValuesInst = new ContentValues();
//                                mContentValuesInst.put(mActivity.mDbHelper.mFieldInstallStatus, "1");
//                                mContentValuesInst.put(mActivity.mDbHelper.mFieldInstallIsSync, "1");
//                                String[] mArray = new String[]{mActivity.mIntInstallationId + ""};
//                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesInst, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
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
                if (mActivity.mUtility.haveInternet()) {
                    completeInstallationAPI(false);
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

    /*Call API to complete installation*/
    private void completeInstallationAPI(boolean isShowProgress) {
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress();
        }
        Call<VoInstallationResponse> mInstallationComplete = mActivity.mApiService.completeInstallationHistory(mVoInstallation.getInst_server_id());

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
                            mContentValuesInst.put(mActivity.mDbHelper.mFieldInstallStatus, "1");
                            mContentValuesInst.put(mActivity.mDbHelper.mFieldInstallIsSync, "1");
                            String[] mArray = new String[]{mActivity.mIntInstallationId + ""};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValuesInst, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                            System.out.println("Installer updated In Local Db-COMPLETE");
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

    /*Show error message*/
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
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Report : Installation " + mDateFormatDb.format(currentLocalTime));
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

    /*Check record exist or not*/
    public String CheckRecordExistInInstallDB(String localInstallId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallLocalId + "= '" + localInstallId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInstallLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Check record exist or not*/
    public String CheckServerRecordExistInInstallDB(String serverInstallId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInstall + " where " + mActivity.mDbHelper.mFieldInstallServerId + "= '" + serverInstallId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInstallServerId);
            } else {
                return "-1";
            }
        }
        return "-1";
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
    public String CheckRecordExistInInstallPhotoDB(String localInstallId, String imageType) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInstallerPhoto + " where " + mActivity.mDbHelper.mFieldInstLocalId + "= '" + localInstallId + "'" + " AND " + mActivity.mDbHelper.mFieldInstPhotoType + "= '" + imageType + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install Photo List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInstPhotoLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Add installation success dialog*/
    private void showSuccessMessage() {
        if (mListener != null) {
            mListener.onNextThreePressed(FragmentNewInstallationThree.this);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
        String msg = getResources().getString(R.string.str_frg_three_installation_successfully);
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

    @Override
    public void onResume() {
        super.onResume();
        backBT.setOnClickListener(this);
        nextBT.setOnClickListener(this);
        mRelativeLayoutSignature.setOnClickListener(this);
        mRelativeLayoutTestDevice.setOnClickListener(this);
//        countryCodePicker.setOnClickListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        backBT.setOnClickListener(null);
        nextBT.setOnClickListener(null);
        mRelativeLayoutSignature.setOnClickListener(null);
        mRelativeLayoutTestDevice.setOnClickListener(null);
//        countryCodePicker.setOnClickListener(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof OnStepThreeListener) {
                mListener = (OnStepThreeListener) context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnStepThreeListener {
        void onBackThreePressed(Fragment fragment);

        void onNextThreePressed(Fragment fragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        backBT = null;
        nextBT = null;
        mRelativeLayoutSignature = null;
        mRelativeLayoutTestDevice = null;
//        countryCodePicker = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    /*Check required permission*/
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
