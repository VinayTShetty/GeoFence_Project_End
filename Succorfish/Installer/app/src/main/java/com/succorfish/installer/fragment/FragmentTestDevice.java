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
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoGetDeviceInfo;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoLastInstallation;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.interfaces.onAlertDialogCallBack;
import com.succorfish.installer.interfaces.onBackPressWithAction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 23-02-2018.
 */

public class FragmentTestDevice extends Fragment {

    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    //    VoAddInstallation mVoAddInstallation;
    @BindView(R.id.fragment_test_device_relativelayout_main)
    RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.fragment_test_device_relativelayout_call)
    RelativeLayout mRelativeLayoutCall;
    @BindView(R.id.fragment_test_device_relativelayout_details)
    RelativeLayout mRelativeLayoutDetail;
    @BindView(R.id.fragment_test_device_linearlayout_latlong)
    LinearLayout mLinearLayoutLatLong;
    @BindView(R.id.fragment_test_device_linearlayout_installer_details)
    LinearLayout mLinearLayoutInstallerDetails;
    @BindView(R.id.fragment_test_device_button_call)
    Button mButtonCall;
    @BindView(R.id.fragment_test_device_textview_contact_no)
    TextView mTextViewContactNo;

    @BindView(R.id.fragment_test_device_textview_lbl_installed_date)
    TextView mTextViewLblInstalledDate;
    @BindView(R.id.fragment_test_device_textview_installed_date)
    TextView mTextViewInstalledDate;
    @BindView(R.id.fragment_test_device_textview_imei_code)
    TextView mTextViewIMEICode;
    @BindView(R.id.fragment_test_device_textview_device_type)
    TextView mTextViewDeviceType;
    @BindView(R.id.fragment_test_device_textview_installer_name)
    TextView mTextViewInstallerName;
    @BindView(R.id.fragment_test_device_textview_vessel_name)
    TextView mTextViewVesselName;
    @BindView(R.id.fragment_test_device_textview_reg_no)
    TextView mTextViewRegNo;
    @BindView(R.id.fragment_test_device_textview_last_install_location)
    TextView mTextViewInstallLocation;
    @BindView(R.id.fragment_test_device_textview_latitude)
    TextView mTextViewLatitude;
    @BindView(R.id.fragment_test_device_textview_longitude)
    TextView mTextViewLongitude;
    @BindView(R.id.fragment_test_device_relativelayout_install_location)
    RelativeLayout mRelativeLayoutLocation;

    String mStringLatitude = "";
    String mStringLongitude = "";
    String mStringImeiNo = "";
    String mStringDeviceId = "";

    private static final int REQUEST_CALL = 1;
    Intent callIntent;
    onBackPressWithAction mOnBackPressWithAction;
    boolean isHasData = false;
    boolean isFromTestDevice = false;
    private SimpleDateFormat mSimpleDateFormatDateDisplay;
    VoLastInstallation mVoLastInstallation;
    VoInstallationResponse mVoInstallationResponse;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mStringImeiNo = getArguments().getString("mIntent_Imei_no");
            isHasData = getArguments().getBoolean("mIntent_has_data", false);
            isFromTestDevice = getArguments().getBoolean("mIntent_is_from_test_device", false);
            if (!isFromTestDevice) {
                mVoInstallationResponse = (VoInstallationResponse) getArguments().getSerializable("mIntent_last_install_record");
            }
            if (isHasData) {
                mStringDeviceId = getArguments().getString("mIntent_device_id");
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_test_device, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mSimpleDateFormatDateDisplay = new SimpleDateFormat(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getSelectedDateFormat() + " HH:mm", Locale.getDefault());

        if (isFromTestDevice) {
            mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_test_device));
        } else {
            mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_history_view_install_record));
        }
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mTextViewDone.setVisibility(View.GONE);
        Linkify.addLinks(mTextViewContactNo, Linkify.PHONE_NUMBERS);
//        if (isShowLatLong) {
//            mLinearLayoutLatLong.setVisibility(View.VISIBLE);
//            mLinearLayoutInstallerDetails.setVisibility(View.GONE);
//            mTextViewLblInstalledDate.setText("Generated Date");
//        } else {
//            mLinearLayoutLatLong.setVisibility(View.GONE);
//            mLinearLayoutInstallerDetails.setVisibility(View.VISIBLE);
//        }
        if (mActivity.mUtility.haveInternet()) {
            mRelativeLayoutDetail.setVisibility(View.VISIBLE);
            mRelativeLayoutCall.setVisibility(View.GONE);
            if (isHasData) {
                if (isFromTestDevice) {
                    mLinearLayoutLatLong.setVisibility(View.VISIBLE);
                    mLinearLayoutInstallerDetails.setVisibility(View.GONE);
                    getTestInstallInformation(true);
                } else {
                    mLinearLayoutLatLong.setVisibility(View.GONE);
                    mLinearLayoutInstallerDetails.setVisibility(View.VISIBLE);
                    if (mVoInstallationResponse != null) {
                        displayLastInstallData();
                    }
                    getDeviceLatestInstall(true);
                }
            }
        } else {
            if (isFromTestDevice) {
                mRelativeLayoutCall.setVisibility(View.VISIBLE);
                mRelativeLayoutDetail.setVisibility(View.GONE);
            } else {
                mRelativeLayoutDetail.setVisibility(View.VISIBLE);
                mRelativeLayoutCall.setVisibility(View.GONE);

                mLinearLayoutLatLong.setVisibility(View.GONE);
                mLinearLayoutInstallerDetails.setVisibility(View.VISIBLE);

                if (mVoInstallationResponse != null) {
                    displayLastInstallData();
                }
            }
        }

        mTextViewContactNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mStringUserPhone = mTextViewContactNo.getText().toString().trim();
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
        });
        mButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mStringUserPhone = mTextViewContactNo.getText().toString().trim();
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
        });
        mRelativeLayoutLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mStringLatitude != null && !mStringLatitude.equalsIgnoreCase("") && mStringLongitude != null && !mStringLongitude.equalsIgnoreCase("")) {
                    try {
                        String geoUri = "http://maps.google.com/maps?q=loc:" + mStringLatitude + "," + mStringLongitude + " (" + mStringImeiNo + ")";
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    mActivity.mUtility.errorDialog("No Location available.");
                }
            }
        });
        return mViewRoot;
    }

    /*Get test details information*/
    private void getTestInstallInformation(boolean showProgress) {
        mActivity.mUtility.hideKeyboard(mActivity);
        if (showProgress)
            mActivity.mUtility.ShowProgress();
        Call<String> mVoLastInstallationCall = mActivity.mApiService.getTestDetailsAPI(mStringDeviceId);
        System.out.println("URL-" + mVoLastInstallationCall.request().url().toString());
        mVoLastInstallationCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (isAdded()) {
                    mActivity.mUtility.HideProgress();
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mAddInstData---------" + response.body());
                        if (response.body() != null && !response.body().equalsIgnoreCase("") && !response.body().equalsIgnoreCase("null")) {
                            mVoLastInstallation = gson.fromJson(response.body(), VoLastInstallation.class);
                            String json = gson.toJson(mVoLastInstallation);
                            System.out.println("response mVoLastInstallationData---------" + json);
                            if (mVoLastInstallation != null) {
                                displayTestData();
                            } else {
                                mActivity.mUtility.errorDialog("Device has never submitted any positional data to the system");
                            }
                        } else {
                            mActivity.mUtility.errorDialog("Device has never submitted any positional data to the system");
                        }
                    } else {
                        displayErrorWithGoBack();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                displayErrorWithGoBack();
            }
        });
    }

    /*get device latest install information*/
    private void getDeviceLatestInstall(boolean showProgress) {
        mActivity.mUtility.hideKeyboard(mActivity);
        if (showProgress) {
            mActivity.mUtility.ShowProgress();
        }
        Call<VoInstallationResponse> mVoLastInstallationCall = mActivity.mApiService.getDeviceLatestInstallAPI(mStringDeviceId);
        System.out.println("URL-" + mVoLastInstallationCall.request().url().toString());
        mVoLastInstallationCall.enqueue(new Callback<VoInstallationResponse>() {
            @Override
            public void onResponse(Call<VoInstallationResponse> call, Response<VoInstallationResponse> response) {
                mActivity.mUtility.HideProgress();
                if (isAdded()) {

                    if (response.code() == 200 || response.isSuccessful()) {
                        final VoInstallationResponse mLastInstall = response.body();
                        Gson gson = new Gson();
                        String json = gson.toJson(mLastInstall);
                        System.out.println("response mLastInstall---------" + json);
                        if (mLastInstall != null) {
                            mVoInstallationResponse = mLastInstall;
                            if (mVoInstallationResponse != null) {
                                displayLastInstallData();
                            } else {
                                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                            }
                        } else {
                            mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
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

    /*Display Last install data*/
    private void displayLastInstallData() {
        mLinearLayoutLatLong.setVisibility(View.GONE);
        mLinearLayoutInstallerDetails.setVisibility(View.VISIBLE);
        if (mVoInstallationResponse.getDate() != null && !mVoInstallationResponse.getDate().equalsIgnoreCase("") && !mVoInstallationResponse.getDate().equalsIgnoreCase("null")) {
            try {
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(Long.parseLong(mVoInstallationResponse.getDate()));
                mTextViewInstalledDate.setText(mSimpleDateFormatDateDisplay.format(cal.getTime()));
            } catch (Exception e) {
                mTextViewInstalledDate.setText("-NA-");
                e.printStackTrace();
            }
        } else {
            mTextViewInstalledDate.setText("-NA-");
        }
        if (mStringImeiNo != null && !mStringImeiNo.equalsIgnoreCase("")) {
            mTextViewIMEICode.setText(mStringImeiNo);
        } else {
            mTextViewIMEICode.setText("NA");
        }
//        if (mVoInstallationResponse.getDeviceType() != null && !mVoInstallationResponse.getDeviceType().equalsIgnoreCase("")) {
//            mTextViewDeviceType.setText(mVoInstallationResponse.getDeviceType());
//        } else {
//            mTextViewDeviceType.setText("NA");
//        }
//        if (mVoInstallationResponse.getCreatedByUsername() != null && !mVoInstallationResponse.getCreatedByUsername().equalsIgnoreCase("")) {
//            mTextViewInstallerName.setText(mVoInstallationResponse.getCreatedByUsername());
//        } else {
//            mTextViewInstallerName.setText("NA");
//        }
        if (mVoInstallationResponse.getRealAssetName() != null && !mVoInstallationResponse.getRealAssetName().equalsIgnoreCase("")) {
            mTextViewVesselName.setText(mVoInstallationResponse.getRealAssetName());
        } else {
            mTextViewVesselName.setText("NA");
        }
        if (mVoInstallationResponse.getRealAssetRegNo() != null && !mVoInstallationResponse.getRealAssetRegNo().equalsIgnoreCase("")) {
            mTextViewRegNo.setText(mVoInstallationResponse.getRealAssetRegNo());
        } else {
            mTextViewRegNo.setText("NA");
        }
        if (mVoInstallationResponse.getInstallationPlace() != null && !mVoInstallationResponse.getInstallationPlace().equalsIgnoreCase("") && !mVoInstallationResponse.getInstallationPlace().equalsIgnoreCase("null")) {
            mTextViewInstallLocation.setText(mVoInstallationResponse.getInstallationPlace());
        } else {
            mTextViewRegNo.setText("NA");
        }
    }

    /*Display test data*/
    private void displayTestData() {
        mLinearLayoutLatLong.setVisibility(View.VISIBLE);
        mLinearLayoutInstallerDetails.setVisibility(View.GONE);

        if (mVoLastInstallation.getGeneratedDate() != null && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("") && !mVoLastInstallation.getGeneratedDate().equalsIgnoreCase("null")) {
            try {
                System.out.println("generatedDate - " + mVoLastInstallation.getGeneratedDate());
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(Long.parseLong(mVoLastInstallation.getGeneratedDate()));
                mTextViewInstalledDate.setText(mSimpleDateFormatDateDisplay.format(cal.getTime()));
            } catch (Exception e) {
                mTextViewInstalledDate.setText("-NA-");
                e.printStackTrace();
            }
        } else {
            mTextViewInstalledDate.setText("-NA-");
        }
        if (mStringImeiNo != null && !mStringImeiNo.equalsIgnoreCase("")) {
            mTextViewIMEICode.setText(mStringImeiNo);
        } else {
            mTextViewIMEICode.setText("NA");
        }
        if (mVoLastInstallation.getLat() != null && !mVoLastInstallation.getLat().equalsIgnoreCase("") && !mVoLastInstallation.getLat().equalsIgnoreCase("null")) {
            mTextViewLatitude.setText(mVoLastInstallation.getLat());
            mStringLatitude = mVoLastInstallation.getLat();
        } else {
            mTextViewLatitude.setText("NA");
        }
        if (mVoLastInstallation.getLng() != null && !mVoLastInstallation.getLng().equalsIgnoreCase("") && !mVoLastInstallation.getLng().equalsIgnoreCase("null")) {
            mTextViewLongitude.setText(mVoLastInstallation.getLng());
            mStringLongitude = mVoLastInstallation.getLng();
        } else {
            mTextViewLongitude.setText("NA");
        }
    }

    private void displayErrorWithGoBack() {
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_server_error_someting_wrong), new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                if (isFromTestDevice) {
                    mActivity.onBackPressed();
                }
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    public void setOnTestResultSet(onBackPressWithAction mScanResultSet) {
        mOnBackPressWithAction = mScanResultSet;
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
        if (mOnBackPressWithAction != null) {
            mOnBackPressWithAction.onBackWithAction("");
        }
    }
}
