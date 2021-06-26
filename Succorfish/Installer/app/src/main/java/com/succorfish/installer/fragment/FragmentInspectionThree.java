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
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.succorfish.installer.Vo.VoAddInspection;
import com.succorfish.installer.Vo.VoAddPhotoInspection;
import com.succorfish.installer.Vo.VoInspection;
import com.succorfish.installer.Vo.VoInspectionPhoto;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentInspectionThree extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;
    Button backBT;
    Button nextBT;
    private OnStepThreeListener mListener;
    RelativeLayout mRelativeLayoutBottomSheet;
    RelativeLayout mRelativeLayoutAddPhoto;
    RelativeLayout mRelativeLayoutActionTaken;
    RelativeLayout mRelativeLayoutWarrantyReturn;
    RelativeLayout mRelativeLayoutSignature;
    RelativeLayout mRelativeLayoutTestDevice;
    TextView mTextViewDeviceStatus;
    TextView mTextViewDeviceReportedDate;
    TextView mTextViewAddPhoto;
    TextView mTextViewActionTaken;
    TextView mTextViewPhotoCount;
    EditText mEditTextResult;
    String mStringResult = "";
    String mStringActionTaken = "";
    String mStringIMEINo = "";
    String mStringVesselName = "";
    String mStringVesselRegNo = "";
    String mStringWarrantyStatus = "";
    String mStringOwnerSignatureImagePath = "";
    String mStringInstallerSignatureImagePath = "";
    ActionTakenAdapter mPowerAdapter;
    ArrayList<String> mArrayListActionTaken = new ArrayList<>();
    BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    public ArrayList<VoInspectionPhoto> mArrayListPhotoList = new ArrayList<>();
    //    private boolean isAllPhotoImageExis = true;
    int imageDisplayCount = 0;
    VoInspection mVoInspection;


    private File mFileOwnerSignaturePath;
    private File mFileInstallerSignaturePath;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    private SimpleDateFormat mDateFormatDb;
    private boolean isTestDeviceByUser = false;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;
    private File mFileSDCard;
    private File mFileAppDirectory;


    public FragmentInspectionThree() {
        // Required empty public constructor
    }

    public static FragmentInspectionThree newInstance() {
        return new FragmentInspectionThree();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        if (mActivity.mIntInspectionId != 0) {
            System.out.println("From Edit");
            getDBInspectionList(true);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_inspection_three, container, false);
        mEditTextResult = (EditText) mViewRoot.findViewById(R.id.fragment_inspection_three_editext_result);
        mTextViewActionTaken = (TextView) mViewRoot.findViewById(R.id.fragment_inspection_three_textview_action_taken);
        mTextViewAddPhoto = (TextView) mViewRoot.findViewById(R.id.fragment_inspection_three_textview_insp_photo);
        mTextViewPhotoCount = (TextView) mViewRoot.findViewById(R.id.fragment_inspection_three_textview_insp_photo);
        mTextViewDeviceStatus = (TextView) mViewRoot.findViewById(R.id.frg_inspection_three_tv_test_device_status);
        mTextViewDeviceReportedDate = (TextView) mViewRoot.findViewById(R.id.frg_inspection_three_tv_test_device_reported_date);
        mRelativeLayoutAddPhoto = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_inspection_three_relativelayout_inspe_photo);
        mRelativeLayoutActionTaken = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_inspection_three_relativelayout_inspe_action_taken);
        mRelativeLayoutWarrantyReturn = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_inspection_three_relativelayout_inspe_warranty_return);
        mRelativeLayoutBottomSheet = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_inspection_three_relativelayout_bottomsheet);
        mRelativeLayoutSignature = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_inspection_three_relativelayout_signature);
        mRelativeLayoutTestDevice = (RelativeLayout) mViewRoot.findViewById(R.id.frg_inspection_three_rl_test_device);
        mBottomSheetBehavior = BottomSheetBehavior.from(mRelativeLayoutBottomSheet);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        backBT = mViewRoot.findViewById(R.id.backBT);
        nextBT = mViewRoot.findViewById(R.id.nextBT);
//        nextBT.setEnabled(false);
        isTestDeviceByUser = false;

        System.out.println("nextBT-DISABLE");
        if (!mActivity.mUtility.haveInternet() || mActivity.mIsTestDeviceCheck) {
            System.out.println("nextBT-ENABLE");
//            nextBT.setEnabled(true);
            isTestDeviceByUser = true;
        }

        mEditTextResult.setText(mStringResult);
        mTextViewActionTaken.setText(mStringActionTaken);
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
        /*get Inspection photo list*/
        getDBInspectionPhotoList();
        mArrayListActionTaken = new ArrayList<>();
        // ALSO CHANGE IN ADD INSPECTION API.
        mArrayListActionTaken.add("None");
        mArrayListActionTaken.add("Repair");
        mArrayListActionTaken.add("Replace out of warranty");
        mArrayListActionTaken.add("Replace under warranty");
        if (mArrayListPhotoList != null) {
            int imageCount = 0;
            for (int i = 0; i < mArrayListPhotoList.size(); i++) {
                if (mArrayListPhotoList.get(i).getIsHasImage()) {
                    imageCount = imageCount + 1;
                }
            }
            mTextViewPhotoCount.setText(String.valueOf(imageCount));
        }

        return mViewRoot;
    }

    /*get inspection data from local database*/
    private void getDBInspectionList(boolean isFromEdit) {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionIsSync + "= 0" + " AND " + mActivity.mDbHelper.mFieldInspectionLocalId + "= '" + mActivity.mIntInspectionId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoInspection = new VoInspection();
                    mVoInspection.setInsp_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalId));
                    mVoInspection.setInsp_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerId));
                    mVoInspection.setInsp_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionUserId));
                    mVoInspection.setInsp_device_imei_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceIMEINo));
                    mVoInspection.setInsp_device_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceServerId));
                    mVoInspection.setInsp_device_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceLocalId));
                    mVoInspection.setInsp_device_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDevicName));
                    mVoInspection.setInsp_device_warranty_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceWarranty_status));
                    mVoInspection.setInsp_device_type_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDeviceTypeName));
                    mVoInspection.setInsp_vessel_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselLocalId));
                    mVoInspection.setInsp_vessel_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselServerId));
                    mVoInspection.setInsp_vessel_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselName));
                    mVoInspection.setInsp_vessel_regi_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionVesselRegNo));
                    mVoInspection.setInsp_owner_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerName));
                    mVoInspection.setInsp_owner_address(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerAddress));
                    mVoInspection.setInsp_owner_city(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerCity));
                    mVoInspection.setInsp_owner_state(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerState));
                    mVoInspection.setInsp_owner_zipcode(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerZipcode));
                    mVoInspection.setInsp_owner_email(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerEmail));
                    mVoInspection.setInsp_owner_mobile_no(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionOwnerMobileNo));
                    mVoInspection.setInsp_result(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionResult));
                    mVoInspection.setInsp_action_taken(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionActionTaken));
                    mVoInspection.setInsp_warranty_return(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionWarrentyReturn));
                    mVoInspection.setInsp_local_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalSignUrl));
                    mVoInspection.setInsp_server_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerSignUrl));
                    mVoInspection.setInsp_local_inspector_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl));
                    mVoInspection.setInsp_server_inspector_sign_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionServerInspectorSignUrl));
                    mVoInspection.setInsp_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionCreatedDate));
                    mVoInspection.setInsp_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionUpdatedDate));
                    mVoInspection.setInsp_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionIsSync));
                    mVoInspection.setInsp_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionStatus));
                    mVoInspection.setInsp_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionPdfUrl));
                    mVoInspection.setInsp_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDateTime));
                    mVoInspection.setInsp_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionDateTimeStamp));
                    mVoInspection.setInsp_owner_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionIsSyncOwnerSign));
                    mVoInspection.setInsp_installer_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspectionIsSyncInstallerSign));

                    if (isFromEdit) {
                        mStringResult = mVoInspection.getInsp_result();
                        mStringActionTaken = mVoInspection.getInsp_action_taken();
                        mStringInstallerSignatureImagePath = mVoInspection.getInsp_local_inspector_sign_url();
                        mStringOwnerSignatureImagePath = mVoInspection.getInsp_local_sign_url();
                    }
                    mStringIMEINo = mVoInspection.getInsp_device_imei_no();
                    mStringVesselName = mVoInspection.getInsp_vessel_name();
                    mStringVesselRegNo = mVoInspection.getInsp_vessel_regi_no();
                    mStringWarrantyStatus = mVoInspection.getInsp_device_warranty_status();

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*Get Inspection photo from list*/
    private void getDBInspectionPhotoList() {
        DataHolder mDataHolder;
//        isAllPhotoImageExis = true;
        imageDisplayCount = 0;
        mArrayListPhotoList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableInspectionPhoto + " where " + mActivity.mDbHelper.mFieldInspcLocalId + "= '" + mActivity.mIntInspectionId + "'";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Photo List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoInspectionPhoto mVoInspectionPhoto = new VoInspectionPhoto();
                    mVoInspectionPhoto.setInsp_photo_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoLocalID));
                    mVoInspectionPhoto.setInsp_photo_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoServerID));
                    mVoInspectionPhoto.setInsp_photo_local_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoLocalURL));
                    mVoInspectionPhoto.setInsp_photo_server_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoServerURL));
                    mVoInspectionPhoto.setInsp_photo_type(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoType));
                    mVoInspectionPhoto.setInsp_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcLocalId));
                    mVoInspectionPhoto.setInsp_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcServerId));
                    mVoInspectionPhoto.setInsp_photo_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoUserId));
                    mVoInspectionPhoto.setInsp_photo_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoCreatedDate));
                    mVoInspectionPhoto.setInsp_photo_update_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoUpdateDate));
                    mVoInspectionPhoto.setInsp_photo_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoIsSync));
                    if (mVoInspectionPhoto.getInsp_photo_local_url() != null && !mVoInspectionPhoto.getInsp_photo_local_url().equalsIgnoreCase("")) {
                        mVoInspectionPhoto.setIsHasImage(true);
                        File mFileImagePath = new File(mVoInspectionPhoto.getInsp_photo_local_url());
                        if (mFileImagePath != null && mFileImagePath.exists()) {
                            imageDisplayCount = imageDisplayCount + 1;
                        }
                    } else {
                        mVoInspectionPhoto.setIsHasImage(false);
                    }
                    System.out.println("THREE-URL-" + mVoInspectionPhoto.getInsp_photo_local_url());
                    mArrayListPhotoList.add(mVoInspectionPhoto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mTextViewPhotoCount.setText(String.valueOf(imageDisplayCount));
    }

    /*Handle on back press*/
    public void onBackPressCall(MainActivity mActivity) {
        mStringResult = mEditTextResult.getText().toString().trim();
        Calendar calBack = Calendar.getInstance();
        ContentValues mContentValuesBack = new ContentValues();
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionResult, mStringResult);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionActionTaken, mStringActionTaken);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionLocalSignUrl, mStringOwnerSignatureImagePath);
        mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl, mStringInstallerSignatureImagePath);
        String isExistInDBBack = CheckRecordExistInInspectionDB(mActivity.mIntInspectionId + "");
        if (isExistInDBBack.equalsIgnoreCase("-1")) {
            mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, calBack.getTimeInMillis() + "");
            int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack);
            if (isInsertInstall != -1) {
                System.out.println("Device Inspection Added In Local Db");
            } else {
                System.out.println("Device Inspection Adding In Local DB");
            }
            mActivity.mIntInspectionId = isInsertInstall;
        } else {
            String[] mArray = new String[]{isExistInDBBack};
            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
            System.out.println("Device Inspection updated In Local Db");
        }
        System.out.println("Device updated In Local Db");
        System.out.println("3-BACK-JD");
    }


    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.fragment_inspection_three_relativelayout_inspe_photo:
                FragmentInspectionPhoto mFragmentInspectionPhoto = new FragmentInspectionPhoto();
                mActivity.replacesFragment(mFragmentInspectionPhoto, true, null, 1);
                break;
            case R.id.fragment_inspection_three_relativelayout_inspe_action_taken:
                if (isAdded()) {
                    showBottomSheetDialog();
                }
                break;
            case R.id.fragment_inspection_three_relativelayout_signature:
                FragmentSignature mFramentSignature = new FragmentSignature();
                Bundle mBundle = new Bundle();
                mBundle.putString("intent_owner_signature", mStringOwnerSignatureImagePath);
                mBundle.putString("intent_installer_signature", mStringInstallerSignatureImagePath);
                mBundle.putInt("intent_is_from", 3);
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
                        mVoInspection.setInsp_local_sign_url(mStringOwnerSignatureImagePath);
                        mVoInspection.setInsp_local_inspector_sign_url(mStringInstallerSignatureImagePath);
                    }

                    @Override
                    public void onBackWithAction(String imei, String deviceType, String warrantyStatus) {

                    }
                });
                mActivity.replacesFragment(mFramentSignature, true, mBundle, 1);
                break;
            case R.id.fragment_inspection_three_relativelayout_inspe_warranty_return:
                if (isAdded()) {
                    try {
                        Calendar cal = Calendar.getInstance();
                        Date currentLocalTime = cal.getTime();
                        String msgData = "Device " + mStringIMEINo + " from " + mStringVesselName + " & " + mStringVesselRegNo + " will be returned for warranty inspection. Its warranty status " + mStringWarrantyStatus + " and was inspected by " + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName() + " on " + mDateFormatDb.format(currentLocalTime);
                        Intent sendIntent = new Intent(Intent.ACTION_SEND);
                        sendIntent.setData(Uri.parse("mailto:"));
                        sendIntent.setType("text/plain");
                        sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tech@succorfish.com"});
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
            case R.id.frg_inspection_three_rl_test_device:
                if (mActivity.mUtility.haveInternet()) {
                    getTestInstallInformation(true);
                }
                break;
            case R.id.backBT:
                mStringResult = mEditTextResult.getText().toString().trim();
                Calendar calBack = Calendar.getInstance();
                ContentValues mContentValuesBack = new ContentValues();
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionResult, mStringResult);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionActionTaken, mStringActionTaken);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionLocalSignUrl, mStringOwnerSignatureImagePath);
                mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl, mStringInstallerSignatureImagePath);
                String isExistInDBBack = CheckRecordExistInInspectionDB(mActivity.mIntInspectionId + "");
                if (isExistInDBBack.equalsIgnoreCase("-1")) {
                    mContentValuesBack.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, calBack.getTimeInMillis() + "");
                    int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack);
                    if (isInsertInstall != -1) {
                        System.out.println("Device Inspection Added In Local Db");
                    } else {
                        System.out.println("Device Inspection Adding In Local DB");
                    }
                    mActivity.mIntInspectionId = isInsertInstall;
                } else {
                    String[] mArray = new String[]{isExistInDBBack};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValuesBack, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
                    System.out.println("Device Inspection updated In Local Db");
                }

                if (mListener != null)
                    mListener.onBackThreePressed(this);
                break;

            case R.id.nextBT:
                if (Build.VERSION.SDK_INT >= 23) {
                    callMarshMallowPermission();
                }
                createAllFolderDir();
                mStringResult = mEditTextResult.getText().toString().trim();
                if (mStringResult.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_inspection_three_enter_insp_result));
                    return;
                }
                if (mArrayListPhotoList != null && mArrayListPhotoList.size() > 0) {
                    int imageCount = 0;
                    for (int i = 0; i < mArrayListPhotoList.size(); i++) {
                        if (mArrayListPhotoList.get(i).getInsp_photo_local_url() != null && !mArrayListPhotoList.get(i).getInsp_photo_local_url().equalsIgnoreCase("") && !mArrayListPhotoList.get(i).getInsp_photo_local_url().equalsIgnoreCase("null")) {
                            File mFileImagePath = new File(mArrayListPhotoList.get(i).getInsp_photo_local_url());
                            if (mFileImagePath != null && mFileImagePath.exists()) {
                                imageCount = imageCount + 1;
                            }
                        }
                    }
                    if (imageCount != 4) {
                        mActivity.mUtility.errorDialog("Please add 4 photos of inspection.");
                        return;
                    }
                } else {
                    mActivity.mUtility.errorDialog("Please add 4 photos of inspection.");
                    return;
                }

//                if (imageCount > 0) {
//                    if (!isAllPhotoImageExis) {
//                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_inspection_three_add_missing_image));
//                        return;
//                    }
//                } else {
//                    mActivity.mUtility.errorDialog("Please add at least one photo");
//                    return;
//                }
                if (mStringActionTaken == null || mStringActionTaken.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_inspection_three_select_action_taken));
                    return;
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
                saveQuestionFile();
                getDBInspectionList(false);
                if (!mActivity.mUtility.haveInternet()) {
                    Calendar cal = Calendar.getInstance();
                    ContentValues mContentValues = new ContentValues();
                    mContentValues.put(mActivity.mDbHelper.mFieldInspectionResult, mStringResult);
                    mContentValues.put(mActivity.mDbHelper.mFieldInspectionActionTaken, mStringActionTaken);
                    mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalSignUrl, mStringOwnerSignatureImagePath);
                    mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl, mStringInstallerSignatureImagePath);
                    mContentValues.put(mActivity.mDbHelper.mFieldInspectionStatus, "1");
                    String isExistInDB = CheckRecordExistInInspectionDB(mActivity.mIntInspectionId + "");
                    if (isExistInDB.equalsIgnoreCase("-1")) {
                        mContentValues.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, cal.getTimeInMillis() + "");
                        int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValues);
                        if (isInsertInstall != -1) {
                            System.out.println("Device Inspection Added In Local Db");
                        } else {
                            System.out.println("Device Inspection Adding In Local DB");
                        }
                        mActivity.mIntInspectionId = isInsertInstall;
                    } else {
                        String[] mArray = new String[]{isExistInDB};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValues, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
                        System.out.println("Device Inspection updated In Local Db");
                    }
                    showSuccessMessage();
                } else {
                    try {
                        if (mVoInspection.getInsp_server_id() != null && !mVoInspection.getInsp_server_id().equalsIgnoreCase("") && !mVoInspection.getInsp_server_id().equalsIgnoreCase("null")) {
                            new InspectionPhotoUploadAsyncTask().execute("");
                        } else {
                            if (mVoInspection.getInsp_device_server_id() != null && !mVoInspection.getInsp_device_server_id().equalsIgnoreCase("") && !mVoInspection.getInsp_device_server_id().equalsIgnoreCase("null")) {
                                AddInspectionAPI();
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

    /*Save question into file*/
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
            myOutWriter.append("            Inspection Health and Safety Questionnaire          \r\n");
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

    /*get test installation information*/
    private void getTestInstallInformation(boolean isShowAlert) {
        if (mVoInspection != null) {
            mActivity.mUtility.hideKeyboard(mActivity);
            mActivity.mUtility.ShowProgress();
            System.out.println("Device_Server_ID" + mVoInspection.getInsp_device_server_id());
            Call<String> mVoLastInstallationCall = mActivity.mApiService.getTestDetailsAPI(mVoInspection.getInsp_device_server_id());
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
                    mActivity.mIsTestDeviceCheck = true;
                    isTestDeviceByUser = true;
//                    nextBT.setEnabled(true);
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
                }
            });
        }
    }

    /*Call API to add inspection*/
    public void AddInspectionAPI() {
        if (mVoInspection != null) {
            mActivity.mUtility.hideKeyboard(mActivity);
            mActivity.mUtility.ShowProgress();
//            Gson gson = new Gson();
//            String json = gson.toJson(mVoInspection);

            JsonObject jsonObject = new JsonObject();
            JsonObject jsonObjectContact = new JsonObject();
            jsonObjectContact.addProperty("name", TextUtils.isEmpty(mVoInspection.getInsp_owner_name()) ? "NA" : mVoInspection.getInsp_owner_name());
            jsonObjectContact.addProperty("address", TextUtils.isEmpty(mVoInspection.getInsp_owner_address()) ? "NA" : mVoInspection.getInsp_owner_address());
            jsonObjectContact.addProperty("city", TextUtils.isEmpty(mVoInspection.getInsp_owner_city()) ? "NA" : mVoInspection.getInsp_owner_city());
            jsonObjectContact.addProperty("state", TextUtils.isEmpty(mVoInspection.getInsp_owner_state()) ? "NA" : mVoInspection.getInsp_owner_state());
            jsonObjectContact.addProperty("zipCode", TextUtils.isEmpty(mVoInspection.getInsp_owner_zipcode()) ? "NA" : mVoInspection.getInsp_owner_zipcode());
            jsonObjectContact.addProperty("email", TextUtils.isEmpty(mVoInspection.getInsp_owner_email()) ? "NA" : mVoInspection.getInsp_owner_email());
            jsonObjectContact.addProperty("telephone", TextUtils.isEmpty(mVoInspection.getInsp_owner_mobile_no()) ? "NA" : mVoInspection.getInsp_owner_mobile_no());
            jsonObjectContact.addProperty("occupation", "NA");
            jsonObject.add("contactInfo", jsonObjectContact);
            jsonObject.addProperty("notes", mStringResult);
            String mStringAction = "";
            if (mStringActionTaken.equalsIgnoreCase("None")) {
                mStringAction = "INSPECTION";
            } else if (mStringActionTaken.equalsIgnoreCase("Repair")) {
                mStringAction = "REPAIR";
            } else if (mStringActionTaken.equalsIgnoreCase("Replace out of warranty")) {
                mStringAction = "REPLACE_OUT_OF_WARRANTY";
            } else if (mStringActionTaken.equalsIgnoreCase("Replace under warranty")) {
                mStringAction = "REPLACE_UNDER_WARRANTY";
            }
            jsonObject.addProperty("operation", mStringAction);

            System.out.println("mHashMap-" + jsonObject.toString());
            RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonObject.toString());
            System.out.println("mHashMapbody-" + body.toString());
            Call<String> mAddInstallationmLogin = mActivity.mApiService.saveInspectionData(mVoInspection.getInsp_device_server_id(), body);

            System.out.println("URL-" + mAddInstallationmLogin.request().url().toString());
            mAddInstallationmLogin.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (isAdded()) {
                        mActivity.mUtility.HideProgress();
                        Gson gson = new Gson();
                        if (response.code() == 200 || response.isSuccessful()) {
                            System.out.println("response mAddInspData---------" + response.body());
                            VoInstallationResponse mVoAddInstallation = gson.fromJson(response.body(), VoInstallationResponse.class);
                            String json = gson.toJson(mVoAddInstallation);
                            System.out.println("response mAddInspData---------" + json);
                            Calendar cal = Calendar.getInstance();

                            ContentValues mContentValues = new ContentValues();
                            mVoInspection.setInsp_server_id(mVoAddInstallation.getId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionServerId, mVoAddInstallation.getId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionVesselServerId, mVoAddInstallation.getRealAssetId());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionResult, mVoAddInstallation.getNotes());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionActionTaken, mVoAddInstallation.getOperation());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionUpdatedDate, mVoAddInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionDateTimeStamp, mVoAddInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionDateTime, mVoAddInstallation.getDate());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalSignUrl, mStringOwnerSignatureImagePath);
//                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionServerSignUrl, mVoAddInstallation.getData().getSignaturepath());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionLocalInspectorSignUrl, mStringInstallerSignatureImagePath);
//                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionServerInspectorSignUrl, mVoAddInstallation.getData().getIns_sign_image());
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionStatus, "1");
                            mContentValues.put(mActivity.mDbHelper.mFieldInspectionIsSync, "0");
//                            try {
//                                Date mDatelocalDate = mSimpleDateFormatDate.parse(mVoInspection.getInsp_date_time());
//                                System.out.println("TimeStamp-" + mDatelocalDate.getTime());
//                                mContentValues.put(mActivity.mDbHelper.mFieldInspectionDateTimeStamp, mDatelocalDate.getTime());
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
                            String isInstallExistInDB = CheckRecordExistInInstallDB(mActivity.mIntInspectionId + "");
                            if (isInstallExistInDB.equalsIgnoreCase("-1")) {
                                mContentValues.put(mActivity.mDbHelper.mFieldInspectionCreatedDate, cal.getTimeInMillis() + "");
                                int isInsertInstall = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspection, mContentValues);
                                if (isInsertInstall != -1) {
                                    System.out.println("Device Inspection Added In Local Db");
                                } else {
                                    System.out.println("Device Inspection Adding In Local DB");
                                }
                            } else {
                                String[] mArray = new String[]{isInstallExistInDB};
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValues, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
                                System.out.println("Device Inspection In Local Db");
                            }
                            mActivity.getUnSyncedCount();
                            new InspectionPhotoUploadAsyncTask().execute("");

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
                                        System.out.println("JSONArray-2");
                                        if (mErrorResponse.size() > 0) {
                                            System.out.println("JSONArray-3");
                                            if (mErrorResponse.get(0).getErrorCode().contains("device.cannot.inspect")) {
                                                mActivity.mUtility.errorDialog("This device can not be inspected at this time.");
                                            } else if (mErrorResponse.get(0).getErrorCode().contains("Pattern.deviceInspectDto.contactInfo.telephone")) {
                                                mActivity.mUtility.errorDialog("Please enter valid mobile no.");
                                            } else if (mErrorResponse.get(0).getErrorCode().contains("NotBlank.deviceInspectDto.contactInfo.zipCode")) {
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

    /*Upload Image*/
    class InspectionPhotoUploadAsyncTask extends AsyncTask<String, Void, String> {
        ArrayList<VoInspectionPhoto> mArrayListPhoto = new ArrayList<>();
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
            if (mVoInspection.getInsp_owner_sign_sync().equalsIgnoreCase("0")) {
                VoInspectionPhoto mVoInspectionPhoto = new VoInspectionPhoto();
                mVoInspectionPhoto.setInsp_photo_local_url(mStringOwnerSignatureImagePath);
                mVoInspectionPhoto.setIsSignature(true);
                mVoInspectionPhoto.setQuestionFile(false);
                mVoInspectionPhoto.setIsOwnerSignature(true);
                mArrayListPhoto.add(mVoInspectionPhoto);
            }
            if (mVoInspection.getInsp_installer_sign_sync().equalsIgnoreCase("0")) {
                VoInspectionPhoto mVoInspectionPhoto = new VoInspectionPhoto();
                mVoInspectionPhoto.setInsp_photo_local_url(mStringInstallerSignatureImagePath);
                mVoInspectionPhoto.setIsSignature(true);
                mVoInspectionPhoto.setQuestionFile(false);
                mVoInspectionPhoto.setIsOwnerSignature(false);
                mArrayListPhoto.add(mVoInspectionPhoto);
            }
            DataHolder mDataHolder;
            try {
                String url = "select * from " + mActivity.mDbHelper.mTableInspectionPhoto + " where " + mActivity.mDbHelper.mFieldInspcLocalId + "= '" + mActivity.mIntInspectionId + "'" + " AND " + mActivity.mDbHelper.mFieldInspcPhotoIsSync + "= 0";
                System.out.println("Local url " + url);
                mDataHolder = mActivity.mDbHelper.read(url);
                if (mDataHolder != null) {
                    System.out.println("Local Photo List " + url + " : " + mDataHolder.get_Listholder().size());
                    for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                        final VoInspectionPhoto mVoInspectionPhoto = new VoInspectionPhoto();
                        mVoInspectionPhoto.setInsp_photo_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoLocalID));
                        mVoInspectionPhoto.setInsp_photo_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoServerID));
                        mVoInspectionPhoto.setInsp_photo_local_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoLocalURL));
                        mVoInspectionPhoto.setInsp_photo_server_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoServerURL));
                        mVoInspectionPhoto.setInsp_photo_type(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoType));
                        mVoInspectionPhoto.setInsp_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcLocalId));
                        mVoInspectionPhoto.setInsp_server_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcServerId));
                        mVoInspectionPhoto.setInsp_photo_user_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoUserId));
                        mVoInspectionPhoto.setInsp_photo_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoCreatedDate));
                        mVoInspectionPhoto.setInsp_photo_update_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoUpdateDate));
                        mVoInspectionPhoto.setInsp_photo_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInspcPhotoIsSync));
                        if (mVoInspectionPhoto.getInsp_photo_local_url() != null && !mVoInspectionPhoto.getInsp_photo_local_url().equalsIgnoreCase("")) {
                            mVoInspectionPhoto.setIsHasImage(true);
                        } else {
                            mVoInspectionPhoto.setIsHasImage(false);
                        }
                        mArrayListPhoto.add(mVoInspectionPhoto);
//                        if (mVoInspectionPhoto.getInsp_photo_local_url() != null && !mVoInspectionPhoto.getInsp_photo_local_url().equalsIgnoreCase("")) {
//                            File mFileImagePath = new File(mVoInspectionPhoto.getInsp_photo_local_url());
//                            if (mFileImagePath != null && mFileImagePath.exists()) {
//                                String response = AddInspectionPhotoAPI(mVoInspectionPhoto);
//                                if (response.equalsIgnoreCase("fail")) {
//                                    result = "fail";
//                                    break;
//                                }
//                            }
//                        }
                    }


                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            VoInspectionPhoto mVoInspectionPhoto = new VoInspectionPhoto();
            mVoInspectionPhoto.setInsp_photo_local_url(mFileAppDirectory.getAbsolutePath() + "/" + "health-and-safety.txt");
            mVoInspectionPhoto.setIsSignature(false);
            mVoInspectionPhoto.setQuestionFile(true);
            mVoInspectionPhoto.setIsOwnerSignature(false);
            mArrayListPhoto.add(mVoInspectionPhoto);

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
            if (mArrayListPhoto.get(currentLoopPosition).getInsp_photo_local_url() != null && !mArrayListPhoto.get(currentLoopPosition).getInsp_photo_local_url().equalsIgnoreCase("")) {
                File mFileImagePath = new File(mArrayListPhoto.get(currentLoopPosition).getInsp_photo_local_url());
                if (mFileImagePath != null && mFileImagePath.exists()) {
                    AddInspectionPhotoAPI(mArrayListPhoto.get(currentLoopPosition));
                } else {
                    mActivity.mUtility.HideProgress();
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_inspection_three_add_missing_image));
                }
            } else {
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_inspection_three_add_missing_image));
            }
            return "";
        }

        /*Upload Image API*/
        private String AddInspectionPhotoAPI(final VoInspectionPhoto mVoInspectionPhoto) {
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("multipart/form-data"), new File(mVoInspectionPhoto.getInsp_photo_local_url()));
            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("file", new File(mVoInspectionPhoto.getInsp_photo_local_url()).getAbsolutePath(), requestFile);
            Call<Void> mAddInspectionPhotoCall = mActivity.mApiService.saveInstallationPhotoData(mVoInspection.getInsp_server_id(), body);

            System.out.println("URL-" + mAddInspectionPhotoCall.request().url().toString());
            mAddInspectionPhotoCall.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (isAdded()) {
                        if (response.code() == 200 || response.isSuccessful()) {
                            Void mVoAddPhotoInstallation = response.body();
                            Gson gson = new Gson();
                            String json = gson.toJson(mVoAddPhotoInstallation);
                            System.out.println("response mAddInspPhotoData---------" + json);

                            if (mVoInspectionPhoto.getIsSignature()) {
                                ContentValues mContentValuesInst = new ContentValues();
                                if (mVoInspectionPhoto.getIsOwnerSignature()) {
                                    mVoInspection.setInsp_owner_sign_sync("1");
                                    mContentValuesInst.put(mActivity.mDbHelper.mFieldInspectionIsSyncOwnerSign, "1");
                                } else {
                                    mVoInspection.setInsp_installer_sign_sync("1");
                                    mContentValuesInst.put(mActivity.mDbHelper.mFieldInspectionIsSyncInstallerSign, "1");
                                }
                                String[] mArray = new String[]{mActivity.mIntInspectionId + ""};
                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValuesInst, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
                                System.out.println("Signature updated In Local Db");
                            } else {
                                if (mVoInspectionPhoto.getIsQuestionFile()) {

                                } else {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoServerID, mVoInspection.getInsp_server_id());
                                    mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
                                    mContentValues.put(mActivity.mDbHelper.mFieldInspcPhotoIsSync, "1");
                                    String isExistInDB = CheckRecordExistInInspectionPhotoDB(mVoInspectionPhoto.getInsp_local_id(), mVoInspectionPhoto.getInsp_photo_type());
                                    if (isExistInDB.equalsIgnoreCase("-1")) {
//                                mContentValues.put(mActivity.mDbHelper.mFieldInstPhotoCreatedDate, mVoAddPhotoInstallation.getData().getCreated_at());
                                        mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInspectionPhoto, mContentValues);
                                        System.out.println("Inspection Photo Added In Local Db");
                                    } else {
                                        String[] mArray = new String[]{isExistInDB};
                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspectionPhoto, mContentValues, mActivity.mDbHelper.mFieldInspcPhotoLocalID + "=?", mArray);
                                        System.out.println("Inspection Photo updated In Local Db");
                                    }
                                }
                            }

                            System.out.println("currentLoopPosition END-" + currentLoopPosition);
                            System.out.println("mArrayListPhoto END-" + (mArrayListPhoto.size() - 1) + "");
                            if (currentLoopPosition == mArrayListPhoto.size() - 1) {
                                System.out.println("FINISH" + currentLoopPosition);
                                if (mActivity.mUtility.haveInternet()) {
                                    completeInspectionAPI(false);
                                } else {
                                    mActivity.mUtility.HideProgress();
                                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
                                }
//                                mActivity.mUtility.HideProgress();
//                                ContentValues mContentValuesInst = new ContentValues();
//                                mContentValuesInst.put(mActivity.mDbHelper.mFieldInspectionStatus, "1");
//                                mContentValuesInst.put(mActivity.mDbHelper.mFieldInspectionIsSync, "1");
//                                String[] mArray = new String[]{mActivity.mIntInspectionId + ""};
//                                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValuesInst, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
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
                    completeInspectionAPI(false);
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

    /*Complete Inspection API*/
    private void completeInspectionAPI(boolean isShowProgress) {
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress();
        }
        Call<VoInstallationResponse> mInstallationComplete = mActivity.mApiService.completeInstallationHistory(mVoInspection.getInsp_server_id());

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
                            mContentValuesInst.put(mActivity.mDbHelper.mFieldInspectionStatus, "1");
                            mContentValuesInst.put(mActivity.mDbHelper.mFieldInspectionIsSync, "1");
                            String[] mArray = new String[]{mActivity.mIntInspectionId + ""};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInspection, mContentValuesInst, mActivity.mDbHelper.mFieldInspectionLocalId + "=?", mArray);
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

    /*Show Error message dialog*/
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
                            sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Report : Inspection " + mDateFormatDb.format(currentLocalTime));
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

    /*check record exist or not*/
    public String CheckRecordExistInInspectionPhotoDB(String localInspectionId, String imageType) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInspectionPhoto + " where " + mActivity.mDbHelper.mFieldInspcLocalId + "= '" + localInspectionId + "'" + " AND " + mActivity.mDbHelper.mFieldInspcPhotoType + "= '" + imageType + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install Photo List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInspcPhotoLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*check record exist or not*/
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

    /*check record exist or not*/
    public String CheckRecordExistInInspectionDB(String localInspId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mActivity.mDbHelper.mTableInspection + " where " + mActivity.mDbHelper.mFieldInspectionLocalId + "= '" + localInspId + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" Install List : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldInspectionLocalId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Display success message*/
    private void showSuccessMessage() {
        if (mListener != null) {
            mListener.onNextThreePressed(this);
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
        String msg = getResources().getString(R.string.str_inspection_three_insp_successfully);
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

    /*Action taken list adapter*/
    public class ActionTakenAdapter extends RecyclerView.Adapter<ActionTakenAdapter.ViewHolder> {

        @Override
        public ActionTakenAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_spinner_item, parent, false);
            return new ActionTakenAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ActionTakenAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mTextViewName.setText(mArrayListActionTaken.get(position));
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListActionTaken != null) {
                        if (position < mArrayListActionTaken.size()) {
                            mBottomSheetDialog.cancel();
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                            mStringActionTaken = mArrayListActionTaken.get(position);
                            mTextViewActionTaken.setText(mStringActionTaken);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListActionTaken.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_spinner_item_textview_item)
            TextView mTextViewName;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*check record exist or not*/
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

    /*Display Bottom dialog*/
    private void showBottomSheetDialog() {
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        mBottomSheetDialog = new BottomSheetDialog(mActivity);
        View view = getLayoutInflater().inflate(R.layout.dialog_bottomsheet, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dialog_bottomsheet_recyclerView);
        TextView mTextViewTitle = (TextView) view.findViewById(R.id.dialog_bottomsheet_textview_title);
        mTextViewTitle.setText(R.string.str_inspection_three_select_action_taken);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mPowerAdapter = new ActionTakenAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mPowerAdapter);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.show();
        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                mBottomSheetDialog = null;

            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        backBT.setOnClickListener(this);
        nextBT.setOnClickListener(this);
        mRelativeLayoutTestDevice.setOnClickListener(this);
        mRelativeLayoutAddPhoto.setOnClickListener(this);
        mRelativeLayoutActionTaken.setOnClickListener(this);
        mRelativeLayoutWarrantyReturn.setOnClickListener(this);
        mRelativeLayoutSignature.setOnClickListener(this);
        mBottomSheetBehavior = BottomSheetBehavior.from(mRelativeLayoutBottomSheet);
    }

    @Override
    public void onPause() {
        super.onPause();
        backBT.setOnClickListener(null);
        nextBT.setOnClickListener(null);
        mRelativeLayoutTestDevice.setOnClickListener(null);
        mRelativeLayoutAddPhoto.setOnClickListener(null);
        mRelativeLayoutActionTaken.setOnClickListener(null);
        mRelativeLayoutWarrantyReturn.setOnClickListener(null);
        mRelativeLayoutSignature.setOnClickListener(null);
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
        mRelativeLayoutTestDevice = null;
        mRelativeLayoutAddPhoto = null;
        mRelativeLayoutActionTaken = null;
        mRelativeLayoutWarrantyReturn = null;
        mRelativeLayoutSignature = null;
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
