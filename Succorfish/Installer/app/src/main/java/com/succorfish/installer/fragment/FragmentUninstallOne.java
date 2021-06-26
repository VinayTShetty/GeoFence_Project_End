package com.succorfish.installer.fragment;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoDeviceType;
import com.succorfish.installer.Vo.VoGetDeviceInfo;
import com.succorfish.installer.Vo.VoInstallation;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoServerInstallation;
import com.succorfish.installer.Vo.VoUnInstall;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.helper.DateUtils;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.interfaces.onBackPressWithAction;
import com.succorfish.installer.interfaces.onBackWithInstallationData;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 22-02-2018.
 */

public class FragmentUninstallOne extends Fragment implements View.OnClickListener {

    View mViewRoot;
    MainActivity mActivity;
    Button mButtonNext;
    RelativeLayout mRelativeLayoutScanDevice;
    RelativeLayout mRelativeLayoutDeviceType;
    RelativeLayout mRelativeLayoutBottomSheet;
    LinearLayout mLinearLayoutVesselAsset;
    TextView mTextViewAssetName;
    TextView mTextViewRegNo;
    TextView mTextViewScanImeiNo;
    TextView mTextViewDeviceType;
    TextView mTvWarrantyStatus;
    private OnStepOneListener mListener;
    String mStringDeviceScanImei = "";
    String mStringDeviceType = "";
    String mStringAssetName = "";
    String mStringVesselID = "";
    String mStringRegNo = "";
    String mStringWarrantyStatus = "";
    String mStringDeviceId = "";
    private boolean IsReadyToUs = false;
    ArrayList<VoDeviceType> mArrayListDeviceType = new ArrayList<>();
    DeviceTypeAdapter mDeviceTypeAdapter;
    BottomSheetBehavior mBottomSheetBehavior;
    private BottomSheetDialog mBottomSheetDialog;
    private Calendar newCalendar;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    VoUnInstall mVoUnInstall;
    VoGetDeviceInfo mVoGetDeviceInfo;

    public FragmentUninstallOne() {
        // Required empty public constructor
    }

    public static FragmentUninstallOne newInstance() {
        return new FragmentUninstallOne();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        newCalendar = Calendar.getInstance();
        mActivity.mStringDeviceAccountId = "";
        if (mActivity.mIntUnInstallationId != 0) {
            System.out.println("From Edit");
            getDBUnInstallationList();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_uninstall_one, container, false);
        mButtonNext = (Button) mViewRoot.findViewById(R.id.nextBT);
        mRelativeLayoutScanDevice = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_uninstall_one_cardview_scan_device);
        mRelativeLayoutDeviceType = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_uninstall_cardview_device_type);
        mRelativeLayoutBottomSheet = (RelativeLayout) mViewRoot.findViewById(R.id.fragment_uninstall_one_relativelayout_bottomsheet);
        mLinearLayoutVesselAsset = (LinearLayout) mViewRoot.findViewById(R.id.fragment_uninstall_one_linearlayout_vessel_asset);

        mTextViewScanImeiNo = (TextView) mViewRoot.findViewById(R.id.fragment_uninstall_one_textview_scan_device);
        mTextViewDeviceType = (TextView) mViewRoot.findViewById(R.id.fragment_uninstall_one_textview_device_type);
        mTextViewAssetName = (TextView) mViewRoot.findViewById(R.id.fragment_uninstall_one_textview_vessel_asset_name);
        mTextViewRegNo = (TextView) mViewRoot.findViewById(R.id.fragment_uninstall_one_textview_regi_no);
        mTvWarrantyStatus = (TextView) mViewRoot.findViewById(R.id.fragment_uninstall_tv_warranty_status);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        System.out.println("mStringDeviceScanImei-" + mStringDeviceScanImei);
        System.out.println("mStringDeviceType-" + mStringDeviceType);
        System.out.println("mStringAssetName-" + mStringAssetName);
        System.out.println("mStringRegNo-" + mStringRegNo);
        System.out.println("mStringWarrantyStatus-" + mStringWarrantyStatus);
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
        mTextViewDeviceType.setText(mStringDeviceType);
        mTextViewAssetName.setText(mStringAssetName);
        mTextViewRegNo.setText(mStringRegNo);
        if (mStringWarrantyStatus != null && !mStringWarrantyStatus.equalsIgnoreCase("") && !mStringWarrantyStatus.equalsIgnoreCase("null")) {
            try {
                Calendar mCalendar = Calendar.getInstance();
                mCalendar.setTimeInMillis(Long.parseLong(mStringWarrantyStatus));
                mTvWarrantyStatus.setText(mSimpleDateFormatDateDisplay.format(mCalendar.getTime()));
                Calendar mCalendarCurrent = Calendar.getInstance();
                if (DateUtils.isAfterDay(mCalendarCurrent, mCalendar)) {
                    System.out.println("Passed Date");
                    mTvWarrantyStatus.setTextColor(getResources().getColor(R.color.colorRed));
                } else {
                    mTvWarrantyStatus.setTextColor(getResources().getColor(R.color.colorWhite));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return mViewRoot;
    }

    /*get uninstall details from local db*/
    private void getDBUnInstallationList() {
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
                    mVoUnInstall.setUninst_device_account_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDeviceAccountId));
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
                    mVoUnInstall.setUninst_pdf_url(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallPdfUrl));
                    mVoUnInstall.setUninst_created_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallCreatedDate));
                    mVoUnInstall.setUninst_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallUpdatedDate));
                    mVoUnInstall.setUninst_is_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSync));
                    mVoUnInstall.setUninst_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallStatus));
                    mVoUnInstall.setUninst_date_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallDateTime));
                    mVoUnInstall.setUninst_date_timestamp(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallSDateTimeStamp));
                    mVoUnInstall.setUninst_owner_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSyncOwnerSign));
                    mVoUnInstall.setUninst_installer_sign_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUnInstallIsSyncInstallerSign));

                    mStringDeviceScanImei = mVoUnInstall.getUninst_device_type_imei_no();
                    mStringDeviceType = mVoUnInstall.getUninst_device_type_name();
                    mStringAssetName = mVoUnInstall.getUninst_vessel_name();
                    mStringRegNo = mVoUnInstall.getUninst_vessel_regi_no();
                    mStringWarrantyStatus = mVoUnInstall.getUninst_device_warranty_status();
                    mStringDeviceId = mVoUnInstall.getUninst_device_server_id();
                    mActivity.mStringDeviceAccountId = mVoUnInstall.getUninst_device_account_id();
                    System.out.println("mStringDeviceScanImei-" + mStringDeviceScanImei);
                    System.out.println("mStringDeviceType-" + mStringDeviceType);
                    if (mStringRegNo == null || mStringRegNo.equalsIgnoreCase("") || mStringRegNo.equalsIgnoreCase("null")) {
                        mStringRegNo = "NA";
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.nextBT:
                System.out.println("mStringWarrantyStatus-" + mStringWarrantyStatus);
                if (mStringDeviceScanImei == null || mStringDeviceScanImei.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_one_get_device_id));
                    return;
                }
                if (mStringDeviceType == null || mStringDeviceType.equalsIgnoreCase("")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_one_select_device_msg));
                    return;
                }
                if (mStringAssetName == null || mStringAssetName.equalsIgnoreCase("") || mStringAssetName.equalsIgnoreCase("null")) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_frg_two_select_vessel_asset_select_name));
                    return;
                }
                if (mStringRegNo == null || mStringRegNo.equalsIgnoreCase("") || mStringRegNo.equalsIgnoreCase("null")) {
                    mStringRegNo = "NA";
                }
                if (mActivity.mUtility.haveInternet()) {
                    if (!IsReadyToUs) {
                        if (mVoUnInstall != null) {
                            if (mVoUnInstall.getUninst_server_id() != null && !mVoUnInstall.getUninst_server_id().equalsIgnoreCase("") && !mVoUnInstall.getUninst_server_id().equalsIgnoreCase("null")) {
                                saveInstallData();
                            } else {
                                getDeviceInformationFromImei(mStringDeviceScanImei);
                            }
                        } else {
                            getDeviceInformationFromImei(mStringDeviceScanImei);
                        }
                    } else {
                        saveInstallData();
                    }
                } else {
                    saveInstallData();
                }
                break;
            case R.id.fragment_uninstall_one_cardview_scan_device:
                if (isAdded()) {
                    FragmentScanDeviceUninstall mFragmentScanDeviceUninstall = new FragmentScanDeviceUninstall();
                    Bundle mBundle = new Bundle();
                    mBundle.putBoolean("intent_is_from_uninstall", true);
                    mFragmentScanDeviceUninstall.setOnScanResultSet(new onBackWithInstallationData() {
                        @Override
                        public void onBackWithInstallData(VoServerInstallation mVoServerInstallation) {

                        }

                        @Override
                        public void onBackWithInstallData(VoGetDeviceInfo mDeviceInfo) {
                            mVoGetDeviceInfo = mDeviceInfo;
                            mStringDeviceScanImei = mVoGetDeviceInfo.getImei();
                            mStringDeviceType = mVoGetDeviceInfo.getType();
                            mStringVesselID = mVoGetDeviceInfo.getAssetId();
                            mStringAssetName = mVoGetDeviceInfo.getAssetName();
                            mStringRegNo = mVoGetDeviceInfo.getPortNo();
                            mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                            mStringDeviceId = mVoGetDeviceInfo.getId();
                            mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();
                            if (mStringRegNo == null || mStringRegNo.equalsIgnoreCase("") || mStringRegNo.equalsIgnoreCase("null")) {
                                mStringRegNo = "NA";
                            }
                        }

                        @Override
                        public void onBackWithInstallData(String mStringimei) {
                            mStringDeviceScanImei = mStringimei;
                        }
                    });
                    mActivity.replacesFragment(mFragmentScanDeviceUninstall, true, mBundle, 1);
                }
                break;

            case R.id.fragment_uninstall_cardview_device_type:
                if (isAdded()) {
                    showBottomSheetDialog();
                }
                break;
            case R.id.fragment_uninstall_one_linearlayout_vessel_asset:
                FragmentVesselAsset mFragmentVesselAsset = new FragmentVesselAsset();
                Bundle mBundle = new Bundle();
                mBundle.putString("mIntent_accountId", mActivity.mStringDeviceAccountId);
                mBundle.putBoolean("mIntent_isFromInstall", false);
                mFragmentVesselAsset.setOnScanResultSet(new onBackPressWithAction() {
                    @Override
                    public void onBackWithAction(String scanResult) {

                    }

                    @Override
                    public void onBackWithAction(String value1, String value2) {

                    }

                    @Override
                    public void onBackWithAction(String vesselID, String vesselName, String vesselPort) {
                        mStringVesselID = vesselID;
                        mStringAssetName = vesselName;
                        mStringRegNo = vesselPort;
                        if (mStringRegNo == null || mStringRegNo.equalsIgnoreCase("") || mStringRegNo.equalsIgnoreCase("null")) {
                            mStringRegNo = "NA";
                        }
                        mTextViewAssetName.setText(mStringAssetName);
                        mTextViewRegNo.setText(mStringRegNo);
                    }
                });
                mActivity.replacesFragment(mFragmentVesselAsset, true, mBundle, 1);
                break;
        }
    }

    /*get Device information from imei*/
    private void getDeviceInformationFromImei(final String imeiNo) {
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
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("UNINSTALLED") || mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("DELIVERED") || mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("RETURNED")) {
                                mStringDeviceScanImei = mVoGetDeviceInfo.getImei();
                                mStringDeviceType = mVoGetDeviceInfo.getType();
                                mStringVesselID = mVoGetDeviceInfo.getAssetId();
                                mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                                mStringDeviceId = mVoGetDeviceInfo.getId();
                                mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();

                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(getResources().getString(R.string.str_test_device));
                                builder.setCancelable(false);
                                builder.setMessage(String.format(getResources().getString(R.string.str_test_device_alrady_uninstall), imeiNo + ""));
                                builder.setPositiveButton(getResources().getString(R.string.str_test_device_view_last_record), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
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
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("INSTALLED")) {
                                IsReadyToUs = true;
                                mStringDeviceScanImei = mVoGetDeviceInfo.getImei();
                                mStringDeviceType = mVoGetDeviceInfo.getType();
                                mStringVesselID = mVoGetDeviceInfo.getAssetId();
                                mStringWarrantyStatus = mVoGetDeviceInfo.getWarrantyExpires();
                                mStringDeviceId = mVoGetDeviceInfo.getId();
                                mActivity.mStringDeviceAccountId = mVoGetDeviceInfo.getAccountId();
                                saveInstallData();
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("IN_STOCK")) {
                                mActivity.mUtility.errorDialog("You can't use this device at this time.");
                            } else {
                                mActivity.mUtility.errorDialog("You can't use this device at this time.");
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

    /*Save install data*/
    private void saveInstallData() {
        Calendar cal = Calendar.getInstance();
        ContentValues mContentValues = new ContentValues();
        System.out.println("mStringDeviceId-" + mStringDeviceId);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUserId, PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId());
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceIMEINo, mStringDeviceScanImei);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceServerId, mStringDeviceId);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceLocalId, "1");
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceAccountId, mActivity.mStringDeviceAccountId);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceName, "Device 1");
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceTypeName, mStringDeviceType);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselLocalId, "");
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselServerId, mStringVesselID);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselName, mStringAssetName);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallVesselRegNo, mStringRegNo);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDeviceWarrantStatus, mStringWarrantyStatus);
        mContentValues.put(mActivity.mDbHelper.mFieldUnInstallUpdatedDate, cal.getTimeInMillis() + "");
        if (mVoGetDeviceInfo != null) {
            if (mVoGetDeviceInfo.getContactInfo() != null) {
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerName, mVoGetDeviceInfo.getContactInfo().getName());
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerAddress, mVoGetDeviceInfo.getContactInfo().getAddress());
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerCity, mVoGetDeviceInfo.getContactInfo().getCity());
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerState, mVoGetDeviceInfo.getContactInfo().getState());
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerZipcode, mVoGetDeviceInfo.getContactInfo().getZipCode());
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerEmail, mVoGetDeviceInfo.getContactInfo().getEmail());
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallOwnerMobileNo, mVoGetDeviceInfo.getContactInfo().getTelephone());
            }
        }
        if (mActivity.mIntUnInstallationId == 0) {
            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDateTime, cal.getTimeInMillis() + "");
            mContentValues.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, cal.getTimeInMillis() + "");
            mActivity.mIntUnInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues);
        } else {
            String isExistInDB = CheckRecordExistInUnInstallDB(mActivity.mIntUnInstallationId + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallDateTime, cal.getTimeInMillis() + "");
                mContentValues.put(mActivity.mDbHelper.mFieldUnInstallCreatedDate, cal.getTimeInMillis() + "");
                mActivity.mIntUnInstallationId = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues);
            } else {
                String[] mArray = new String[]{isExistInDB};
                mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableUnInstall, mContentValues, mActivity.mDbHelper.mFieldUnInstallLocalId + "=?", mArray);
                System.out.println("Device UnInstall updated In Local Db");
            }
        }
        mActivity.getUnSyncedCount();
        System.out.println("mActivity.mIntUnInstallationId-" + mActivity.mIntUnInstallationId);
        if (mListener != null)
            mListener.onNextPressed(this);
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
        mButtonNext.setOnClickListener(this);
        mRelativeLayoutScanDevice.setOnClickListener(this);
        mRelativeLayoutDeviceType.setOnClickListener(this);
        mLinearLayoutVesselAsset.setOnClickListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        mButtonNext.setOnClickListener(null);
        mRelativeLayoutScanDevice.setOnClickListener(null);
        mRelativeLayoutDeviceType.setOnClickListener(null);
        mLinearLayoutVesselAsset.setOnClickListener(null);
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
        void onNextPressed(Fragment fragment);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        mButtonNext = null;
        mRelativeLayoutScanDevice = null;
        mRelativeLayoutDeviceType = null;
        mLinearLayoutVesselAsset = null;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mUtility.hideKeyboard(mActivity);
    }

    /*get Device type list adapter*/
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


}
