package com.succorfish.installer.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.succorfish.installer.BuildConfig;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoAddInstallation;
import com.succorfish.installer.Vo.VoDeviceType;
import com.succorfish.installer.Vo.VoGetDeviceInfo;
import com.succorfish.installer.Vo.VoInstallation;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoLastInstallation;
import com.succorfish.installer.Vo.VoServerInstallation;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.interfaces.onBackPressWithAction;
import com.succorfish.installer.interfaces.onBackWithInstallationData;
import com.succorfish.installer.interfaces.onFragmentBackPress;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentNewInstallationOne extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;

    Button mButtonNext;
    Button backBT;
    RelativeLayout mRelativeLayoutScanDevice;
    RelativeLayout mRelativeLayoutDeviceType;
    RelativeLayout mRelativeLayoutTestDevice;
    RelativeLayout mRelativeLayoutBottomSheet;
    TextView mTextViewScanImeiNo;
    TextView mTextViewDeviceType;
    private OnStepOneListener mListener;
    String mStringDeviceScanImei = "";
    String mStringWarrantyStatus = "";
    String mStringDeviceType = "";
    String mStringDeviceId = "";
    String mStringDeviceName = "";
    ArrayList<VoDeviceType> mArrayListDeviceType = new ArrayList<>();
    DeviceTypeAdapter mDeviceTypeAdapter;
    BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private boolean IsReadyToUs = false;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    VoInstallation mVoInstallation;

    public FragmentNewInstallationOne() {
        // Required empty public constructor
    }

    public static FragmentNewInstallationOne newInstance() {
        return new FragmentNewInstallationOne();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mActivity.mStringDeviceAccountId = "";
        if (mActivity.mIntInstallationId != 0) {
            System.out.println("From Edit");
            /*get Installation record by id*/
            getDBInstallationList();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_new_installation_one, container, false);
        mButtonNext = (Button) mViewRoot.findViewById(R.id.nextBT);
        backBT = mViewRoot.findViewById(R.id.backBT);
        mRelativeLayoutScanDevice = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_one_cardview_scan_device);
        mRelativeLayoutTestDevice = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_one_cardview_test_device);
        mRelativeLayoutDeviceType = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_one_cardview_device_type);
        mRelativeLayoutBottomSheet = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_new_ins_one_relativelayout_bottomsheet);

        mTextViewScanImeiNo = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_one_textview_scan_device);
        mTextViewDeviceType = (TextView) mViewRoot.findViewById(R.id.fragment_new_ins_one_textview_device_type);

        System.out.println("mStringDeviceScanImei-" + mStringDeviceScanImei);
        System.out.println("mStringWarrantyStatus-" + mStringWarrantyStatus);
        System.out.println("mStringDeviceType-" + mStringDeviceType);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        mArrayListDeviceType = new ArrayList<>();
        for (int i = 1; i <= 2; i++) {
            VoDeviceType mVoDeviceType = new VoDeviceType();
            if (i == 1) {
                mVoDeviceType.setDevice_type_name("SC2");
            } else {
                mVoDeviceType.setDevice_type_name("SC3");
            }
            mArrayListDeviceType.add(mVoDeviceType);
        }
        mTextViewScanImeiNo.setText(mStringDeviceScanImei);
        if (mStringDeviceScanImei != null && !mStringDeviceScanImei.equalsIgnoreCase("") && mStringDeviceScanImei.equalsIgnoreCase("357152070984180")) {
            mTextViewDeviceType.setText("SC3");
            mStringDeviceType = "SC3";
        } else {
            mTextViewDeviceType.setText(mStringDeviceType);
        }
//        mActivity.setOnBackFrgPress(new onFragmentBackPress() {
//            @Override
//            public void onFragmentBackPress(Fragment mFragment) {
//
//                if (mFragment instanceof FragmentNewInstallation) {
//                    System.out.println("BackKKOne");
//                    backBT.performClick();
//                    int currentPage = mActivity.mViewPagerInstallation.getCurrentItem();
//                    if (currentPage == 0) {
//                        if (mActivity.mIntInstallationId != 0) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//                            builder.setCancelable(false);
//                            builder.setMessage(getResources().getString(R.string.str_back_confirmation));
//                            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    mActivity.onBackPressedDirect();
//                                }
//                            });
//                            builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            builder.show();
//                        } else {
//                            mActivity.onBackPressedDirect();
//                        }
//                    } else {
//                        if (mActivity.mIntInstallationId != 0) {
//                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//                            builder.setCancelable(false);
//                            builder.setMessage(getResources().getString(R.string.str_back_confirmation));
//                            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                    mActivity.onBackPressedDirect();
//                                }
//                            });
//                            builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    dialog.dismiss();
//                                }
//                            });
//                            builder.show();
//                        } else {
//                            mActivity.onBackPressedDirect();
//                        }
//                    }
//                }
//            }
//        });
        return mViewRoot;
    }

    /*get Installation record by id*/
    private void getDBInstallationList() {
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
                    mVoInstallation.setInst_device_account_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDeviceAccountId));
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
                    mVoInstallation.setIs_install(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallStatus));
                    mVoInstallation.setInst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldInstallDateTimeStamp));

                    mStringDeviceScanImei = mVoInstallation.getInst_device_imei_no();
                    mStringDeviceType = mVoInstallation.getInst_device_type_name();
                    mStringWarrantyStatus = mVoInstallation.getInst_device_warranty_status();
                    mStringDeviceId = mVoInstallation.getInst_device_server_id();
                    mActivity.mStringDeviceAccountId = mVoInstallation.getInst_device_account_id();
                    System.out.println("mStringDeviceScanImei-" + mStringDeviceScanImei);
                    System.out.println("mStringDeviceType-" + mStringDeviceType);
                    System.out.println("mStringWarrantyStatus-" + mStringWarrantyStatus);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.backBT:
                /*back button click*/
                if (mListener != null)
                    mListener.onBackOnePressed(this);
                break;
            case R.id.nextBT:
                /*Next button click*/
                if (mStringDeviceScanImei == null || mStringDeviceScanImei.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_one_get_device_id));
                    return;
                }
                if (mStringDeviceType == null || mStringDeviceType.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_one_select_device_msg));
                    return;
                }
                if (mActivity.mUtility.haveInternet()) {
                    if (!IsReadyToUs) {
                        if (mVoInstallation != null) {
                            if (mVoInstallation.getInst_server_id() != null && !mVoInstallation.getInst_server_id().equalsIgnoreCase("") && !mVoInstallation.getInst_server_id().equalsIgnoreCase("null")) {
                                saveInstallData();
                            } else {
                                getDeviceInformationFromImei(mStringDeviceScanImei, false);
                            }
                        } else {
                            getDeviceInformationFromImei(mStringDeviceScanImei, false);
                        }
                    } else {
                        saveInstallData();
                    }
                } else {
                    saveInstallData();
                }
                break;
            case R.id.fragment_new_ins_one_cardview_scan_device:
                if (isAdded()) {
                    FragmentScanDevice mFragmentScanDevice = new FragmentScanDevice();
                    mFragmentScanDevice.setOnScanResultSet(new onBackWithInstallationData() {
                        @Override
                        public void onBackWithInstallData(VoServerInstallation mVoServerInstallation) {

                        }

                        @Override
                        public void onBackWithInstallData(VoGetDeviceInfo mVoGetDeviceInfo) {
                            mStringDeviceScanImei = mVoGetDeviceInfo.getImei();
                            mStringDeviceType = mVoGetDeviceInfo.getType();
                            mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                            mStringDeviceId = mVoGetDeviceInfo.getId();
                            mStringDeviceName = mVoGetDeviceInfo.getAssetName();
                            mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();
                        }

                        @Override
                        public void onBackWithInstallData(String mStringimei) {
                            IsReadyToUs = false;
                            mStringDeviceScanImei = mStringimei;
                        }
                    });
                    mActivity.replacesFragment(mFragmentScanDevice, true, null, 1);
                }
                break;
            case R.id.fragment_new_ins_one_cardview_test_device:
                if (isAdded()) {
                    if (mStringDeviceScanImei.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_one_get_device_id));
                        return;
                    }
                    if (mActivity.mUtility.haveInternet()) {
                        if (mStringDeviceId != null && !mStringDeviceId.equalsIgnoreCase("") && !mStringDeviceId.equalsIgnoreCase("null")) {
                            FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                            Bundle mBundle = new Bundle();
                            mBundle.putString("mIntent_Imei_no", mStringDeviceScanImei);
                            mBundle.putString("mIntent_device_id", mStringDeviceId);
                            mBundle.putBoolean("mIntent_has_data", true);
                            mBundle.putBoolean("mIntent_is_from_test_device", true);
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
                        } else {
                            getDeviceInformationFromImei(mStringDeviceScanImei, true);
                        }
                    } else {
                        FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                        Bundle mBundle = new Bundle();
                        mBundle.putString("mIntent_Imei_no", mStringDeviceScanImei);
                        mBundle.putBoolean("mIntent_has_data", false);
                        mBundle.putBoolean("mIntent_is_from_test_device", true);
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
                }
                break;
            case R.id.fragment_new_ins_one_cardview_device_type:
                if (isAdded()) {
                    showBottomSheetDialog();
                }
                break;
        }
    }

    /*Save Installation data*/
    private void saveInstallData() {
        System.out.println("mStringDeviceScanImei-" + mStringDeviceScanImei);
        Calendar cal = Calendar.getInstance();
        ContentValues mContentValues = new ContentValues();
        mContentValues.put(mActivity.mDbHelper.mFieldInstallUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceIMEINo, mStringDeviceScanImei);
        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceServerId, mStringDeviceId);
        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceLocalId, "1");
        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceAccountId, mActivity.mStringDeviceAccountId);
        mContentValues.put(mActivity.mDbHelper.mFieldInstallDevicName, "");
        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceWarranty_status, mStringWarrantyStatus);
        mContentValues.put(mActivity.mDbHelper.mFieldInstallDeviceTypeName, mStringDeviceType);
        mContentValues.put(mActivity.mDbHelper.mFieldInstallUpdatedDate, cal.getTimeInMillis() + "");
        if (mActivity.mIntInstallationId == 0) {
            mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, cal.getTimeInMillis() + "");
            mActivity.mIntInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
        } else {
            String isExistInDB = CheckRecordExistInInstallDB(mActivity.mIntInstallationId + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues.put(mActivity.mDbHelper.mFieldInstallCreatedDate, cal.getTimeInMillis() + "");
                mActivity.mIntInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableInstall, mContentValues);
            } else {
                String[] mArray = new String[]{isExistInDB};
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableInstall, mContentValues, mActivity.mDbHelper.mFieldInstallLocalId + "=?", mArray);
                System.out.println("Device updated In Local Db");
            }
        }
        mActivity.getUnSyncedCount();
        if (mListener != null)
            mListener.onNextPressed(this);
    }

    /*getDevice information by Imei no*/
    private void getDeviceInformationFromImei(final String imeiNo, final boolean isFromTestDevice) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mUtility.ShowProgress();
        Call<VoGetDeviceInfo> mVoLastInstallationCall = mActivity.mApiService.getDeviceInfoFromImei(imeiNo);
        System.out.println("URL-" + mVoLastInstallationCall.request().url().toString());
        mVoLastInstallationCall.enqueue(new Callback<VoGetDeviceInfo>() {
            @Override
            public void onResponse(Call<VoGetDeviceInfo> call, Response<VoGetDeviceInfo> response) {
                mActivity.mUtility.HideProgress();
                final VoGetDeviceInfo mVoGetDeviceInfo = response.body();
                Gson gson = new Gson();
                String json = gson.toJson(mVoGetDeviceInfo);
                System.out.println("response mVoGetDeviceInfo---------" + json);
                if (response.code() == 200 || response.isSuccessful()) {
                    if (mVoGetDeviceInfo != null) {
                        if (mVoGetDeviceInfo.getStatus() != null && !mVoGetDeviceInfo.getStatus().equals("") && !mVoGetDeviceInfo.equals("null")) {
                            if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("FORBIDDEN") || mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("UNAUTHORIZED")) {
                                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("UNINSTALLED") || mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("DELIVERED")) {
                                mStringDeviceId = mVoGetDeviceInfo.getId();
                                mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();
                                mStringDeviceType = mVoGetDeviceInfo.getType();
                                mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                                mStringDeviceName = mVoGetDeviceInfo.getAssetName();
                                IsReadyToUs = true;
                                if (isFromTestDevice) {
                                    FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                                    Bundle mBundle = new Bundle();
                                    mBundle.putString("mIntent_Imei_no", imeiNo);
                                    mBundle.putString("mIntent_device_id", mVoGetDeviceInfo.getId());
                                    mBundle.putBoolean("mIntent_has_data", true);
                                    mBundle.putBoolean("mIntent_is_from_test_device", true);
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
                                } else {
                                    saveInstallData();
                                }
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("INSTALLED")) {
                                mStringDeviceId = mVoGetDeviceInfo.getId();
                                mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();
                                mStringDeviceType = mVoGetDeviceInfo.getType();
                                mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                                mStringDeviceName = mVoGetDeviceInfo.getAssetName();
                                if (isFromTestDevice) {
                                    FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                                    Bundle mBundle = new Bundle();
                                    mBundle.putString("mIntent_Imei_no", imeiNo);
                                    mBundle.putString("mIntent_device_id", mVoGetDeviceInfo.getId());
                                    mBundle.putBoolean("mIntent_has_data", true);
                                    mBundle.putBoolean("mIntent_is_from_test_device", true);
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
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle(getResources().getString(R.string.str_test_device));
                                    builder.setCancelable(false);
                                    builder.setMessage(String.format(getResources().getString(R.string.str_test_device_alrady_install), imeiNo + ""));
                                    builder.setPositiveButton(getResources().getString(R.string.str_test_device_view_last_record), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                                            Bundle mBundle = new Bundle();
                                            mBundle.putString("mIntent_Imei_no", imeiNo);
                                            mBundle.putString("mIntent_device_id", mVoGetDeviceInfo.getId());
                                            mBundle.putBoolean("mIntent_has_data", true);
                                            mBundle.putBoolean("mIntent_is_from_test_device", false);
                                            mBundle.putSerializable("mIntent_last_install_record", new VoInstallationResponse());
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
                                    });
                                    builder.setNegativeButton(getResources().getString(R.string.str_ok), new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    builder.show();
                                }
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("IN_STOCK") || mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("RETURNED")) {
                                mStringDeviceId = mVoGetDeviceInfo.getId();
                                mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();
                                mStringDeviceType = mVoGetDeviceInfo.getType();
                                mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                                mStringDeviceName = mVoGetDeviceInfo.getAssetName();
                                if (isFromTestDevice) {
                                    FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                                    Bundle mBundle = new Bundle();
                                    mBundle.putString("mIntent_Imei_no", imeiNo);
                                    mBundle.putString("mIntent_device_id", mVoGetDeviceInfo.getId());
                                    mBundle.putBoolean("mIntent_has_data", true);
                                    mBundle.putBoolean("mIntent_is_from_test_device", true);
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
                                } else {
                                    mActivity.mUtility.errorDialog("You can't use this device at this time.");
                                }
                            } else {
                                mStringDeviceId = mVoGetDeviceInfo.getId();
                                mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();
                                mStringDeviceType = mVoGetDeviceInfo.getType();
                                mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                                mStringDeviceName = mVoGetDeviceInfo.getAssetName();
                                if (isFromTestDevice) {
                                    FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                                    Bundle mBundle = new Bundle();
                                    mBundle.putString("mIntent_Imei_no", imeiNo);
                                    mBundle.putString("mIntent_device_id", mVoGetDeviceInfo.getId());
                                    mBundle.putBoolean("mIntent_has_data", true);
                                    mBundle.putBoolean("mIntent_is_from_test_device", true);
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
                                } else {
                                    mActivity.mUtility.errorDialog("You can't use this device at this time.");
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

            @Override
            public void onFailure(Call<VoGetDeviceInfo> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
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

    /*Show bottom dialog*/
    private void showBottomSheetDialog() {

        mBottomSheetBehavior = BottomSheetBehavior.from(mRelativeLayoutBottomSheet);
        if (mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        mBottomSheetDialog = new BottomSheetDialog(mActivity);
        View view = getLayoutInflater().inflate(R.layout.dialog_bottomsheet, null);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.dialog_bottomsheet_recyclerView);
        TextView mTextViewTitle = (TextView) view.findViewById(R.id.dialog_bottomsheet_textview_title);
        TextView mTextViewDone = (TextView) view.findViewById(R.id.dialog_bottomsheet_textview_done);
        mTextViewTitle.setText(R.string.str_frg_one_select_device_type);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(mActivity));
        mDeviceTypeAdapter = new DeviceTypeAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mDeviceTypeAdapter);
        mBottomSheetDialog.setContentView(view);
        mBottomSheetDialog.show();
        mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mBottomSheetDialog.cancel();
            }
        });
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
        mButtonNext.setOnClickListener(this);
        mRelativeLayoutScanDevice.setOnClickListener(this);
        mRelativeLayoutTestDevice.setOnClickListener(this);
        mRelativeLayoutDeviceType.setOnClickListener(this);
    }

    /*Device list adapter*/
    public class DeviceTypeAdapter extends RecyclerView.Adapter<DeviceTypeAdapter.ViewHolder> {

        @Override
        public DeviceTypeAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_spinner_item, parent, false);
            return new DeviceTypeAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DeviceTypeAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mTextViewName.setText(mArrayListDeviceType.get(position).getDevice_type_name());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDeviceType != null) {
                        if (position < mArrayListDeviceType.size()) {
                            mBottomSheetDialog.cancel();
                            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                            mStringDeviceType = mArrayListDeviceType.get(position).getDevice_type_name();
                            mTextViewDeviceType.setText(mStringDeviceType);
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListDeviceType.size();
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

    @Override
    public void onPause() {
        super.onPause();
        mButtonNext.setOnClickListener(null);
        backBT.setOnClickListener(null);
        mRelativeLayoutScanDevice.setOnClickListener(null);
        mRelativeLayoutTestDevice.setOnClickListener(null);
        mRelativeLayoutDeviceType.setOnClickListener(null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (context instanceof OnStepOneListener) {
                mListener = (OnStepOneListener) context;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface OnStepOneListener {
        //void onFragmentInteraction(Uri uri);
        void onBackOnePressed(Fragment fragment);

        void onNextPressed(Fragment fragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mButtonNext = null;
        backBT = null;
        mRelativeLayoutScanDevice = null;
        mRelativeLayoutTestDevice = null;
        mRelativeLayoutDeviceType = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mUtility.hideKeyboard(mActivity);
    }

}
