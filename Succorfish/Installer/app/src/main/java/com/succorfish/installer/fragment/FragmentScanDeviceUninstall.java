package com.succorfish.installer.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.succorfish.installer.BuildConfig;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoAddInstallation;
import com.succorfish.installer.Vo.VoGetDeviceInfo;
import com.succorfish.installer.Vo.VoInstallationResponse;
import com.succorfish.installer.Vo.VoScanDeviceData;
import com.succorfish.installer.interfaces.onBackPressWithAction;
import com.succorfish.installer.interfaces.onBackWithInstallationData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;
import me.dm7.barcodescanner.zxing.ZXingScannerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 23-02-2018.
 */

public class FragmentScanDeviceUninstall extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_scan_device_relativelayout_main)
    RelativeLayout mRelativeLayoutMain;
    //    @BindView(R.id.fragment_scan_device_scannerview)
//    ZBarScannerView mZBarScannerView;
    @BindView(R.id.fragment_scan_device_zxingscannerview)
    ZXingScannerView mZXingScannerView;
    @BindView(R.id.fragment_scan_device_textview_scan_result)
    TextView mTextViewScanResult;
    @BindView(R.id.fragment_scan_device_relativelayout_bottomLayout)
    RelativeLayout mRelativeLayoutScanResult;

    //    List<BarcodeFormat> nBarcodeFormats;
    List<com.google.zxing.BarcodeFormat> nBarcodeFormatsZxing;
    private boolean mBooleanIsScanStart = true;
    onBackWithInstallationData mOnBackWithInstallationData;
    public static int CAMERA_PERMISSION_REQUEST = 139;
    public final String[] CAMERA_PERMS = {Manifest.permission.CAMERA};
    private long mLastClickTime = 0;
    private boolean isFromUninstall = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            isFromUninstall = getArguments().getBoolean("intent_is_from_uninstall");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_scan_device, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_scan_device));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        mActivity.mTextViewDone.setVisibility(View.VISIBLE);
        mActivity.mTextViewDone.setText(getResources().getString(R.string.str_stop));

//        nBarcodeFormats = new ArrayList();
//        nBarcodeFormats.addAll(BarcodeFormat.ALL_FORMATS);
//        mZBarScannerView.setFormats(nBarcodeFormats);
        /*Barcode supported format*/
        nBarcodeFormatsZxing = new ArrayList<>();
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.AZTEC);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.CODABAR);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.CODE_39);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.CODE_93);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.CODE_128);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.DATA_MATRIX);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.EAN_8);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.EAN_13);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.ITF);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.MAXICODE);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.PDF_417);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.QR_CODE);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.RSS_14);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.RSS_EXPANDED);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.UPC_A);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.UPC_E);
        nBarcodeFormatsZxing.add(com.google.zxing.BarcodeFormat.UPC_EAN_EXTENSION);

        mZXingScannerView.setFormats(nBarcodeFormatsZxing);

        PackageManager packageManager = mActivity.getPackageManager();
        if (!packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {

            } else {
                mLastClickTime = SystemClock.elapsedRealtime();
                mActivity.mTextViewDone.setText(getResources().getString(R.string.str_start));
                mBooleanIsScanStart = false;
//                mZBarScannerView.stopCamera();
                mZXingScannerView.stopCamera();
                enterImeiDialog(false);
            }
        }
//        mZBarScannerView.setResultHandler(new ZBarScannerView.ResultHandler() {
//            @Override
//            public void handleResult(Result result) {
//                onScanResult(result);
//            }
//        });
        mZXingScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {

            @Override
            public void handleResult(com.google.zxing.Result result) {
                onScanZxingResult(result);
            }
        });

        mRelativeLayoutScanResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                    return;
                }
                mLastClickTime = SystemClock.elapsedRealtime();
                mActivity.mTextViewDone.setText(getResources().getString(R.string.str_start));
                mBooleanIsScanStart = false;
//                mZBarScannerView.stopCamera();
                mZXingScannerView.stopCamera();
                enterImeiDialog(true);
            }
        });
        mActivity.mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isAdded()) {
                    if (mBooleanIsScanStart) {
                        mActivity.mTextViewDone.setText(getResources().getString(R.string.str_start));
                        mBooleanIsScanStart = false;
//                        mZBarScannerView.stopCamera();
                        mZXingScannerView.stopCamera();
                    } else {
                        mBooleanIsScanStart = true;
//                        mZBarScannerView.startCamera();
//                        mZBarScannerView.setResultHandler(new ZBarScannerView.ResultHandler() {
//                            @Override
//                            public void handleResult(Result result) {
//                                onScanResult(result);
//                            }
//                        });
                        mZXingScannerView.startCamera();
                        mZXingScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {

                            @Override
                            public void handleResult(com.google.zxing.Result result) {
                                onScanZxingResult(result);
                            }
                        });
                        mActivity.mTextViewDone.setText(getResources().getString(R.string.str_stop));
                    }
                }
            }
        });
        return mViewRoot;
    }

    public void setOnScanResultSet(onBackWithInstallationData mScanResultSet) {
        mOnBackWithInstallationData = mScanResultSet;
    }

    /*Offline Imei input Dialog*/
    public void enterImeiDialog(final boolean isEnterManually) {
        final Dialog myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_imei_input);
        myDialog.setCancelable(false);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        Button mButtonOk = (Button) myDialog
                .findViewById(R.id.popup_imei_buttton_ok);
        Button mButtonCancel = (Button) myDialog
                .findViewById(R.id.popup_imei_buttton_cancal);
        final AppCompatEditText mEditTextImei = (AppCompatEditText) myDialog.findViewById(R.id.popup_imei_edittext_imei_no);

        mButtonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (isAdded()) {
                    String mStringImei = mEditTextImei.getText().toString().trim();
                    mActivity.mUtility.hideKeyboard(mActivity);
                    InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(mEditTextImei.getWindowToken(), 0);
                    if (mStringImei.equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_scan_device_enter_imei_no));
                        return;
                    }
//                    if (TextUtils.isDigitsOnly(mStringImei)) {
//                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_scan_device_enter_sixteen_digit_no));
//                        return;
//                    }
                    if (mStringImei.length() != 15) {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_scan_device_enter_sixteen_digit_no));
                        return;
                    }
                    if (mActivity.mUtility.haveInternet()) {
                        myDialog.dismiss();
                        getDeviceInformationFromImei(mStringImei);
                    } else {
                        myDialog.dismiss();
                        if (mOnBackWithInstallationData != null) {
                            mOnBackWithInstallationData.onBackWithInstallData(mStringImei);
                        }
                        mActivity.onBackPressed();
                    }
                }
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.mUtility.hideKeyboard(mActivity);
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditTextImei.getWindowToken(), 0);
                myDialog.dismiss();
                if (isAdded()) {
                    if (!isEnterManually) {
                        mActivity.onBackPressed();
                    } else {
                        mBooleanIsScanStart = true;
//                        mZBarScannerView.startCamera();
//                        mZBarScannerView.setResultHandler(new ZBarScannerView.ResultHandler() {
//                            @Override
//                            public void handleResult(Result result) {
//                                onScanResult(result);
//                            }
//                        });
                        mZXingScannerView.startCamera();
                        mZXingScannerView.setResultHandler(new ZXingScannerView.ResultHandler() {

                            @Override
                            public void handleResult(com.google.zxing.Result result) {
                                onScanZxingResult(result);
                            }
                        });
                        mActivity.mTextViewDone.setText(getResources().getString(R.string.str_stop));
                    }
                }
            }
        });
        myDialog.show();

        Window window = myDialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    /*Handle Scan result*/
    private void onScanZxingResult(com.google.zxing.Result result) {
        if (result != null) {
            System.out.println("Content--" + result.getText());
            System.out.println("getBarcodeFormat().getName()--" + result.getBarcodeFormat().toString());
            mActivity.mTextViewDone.setText(getResources().getString(R.string.str_start));
            mBooleanIsScanStart = false;
            mZXingScannerView.stopCamera();
            if (!result.getText().equalsIgnoreCase("")) {
                if (result.getText().length() != 15) {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_scan_device_sixteen_digit_no));
                    return;
                }
                if (mActivity.mUtility.haveInternet()) {
                    getDeviceInformationFromImei(result.getText());
                } else {
                    if (mOnBackWithInstallationData != null) {
                        mOnBackWithInstallationData.onBackWithInstallData(result.getText());
                    }
                    mActivity.onBackPressed();
                }
            }
        }
    }
//    private void onScanResult(Result result) {
//        if (result != null) {
//            System.out.println("Content--" + result.getContents());
//            System.out.println("getBarcodeFormat().getName()--" + result.getBarcodeFormat().getName());
//            mActivity.mTextViewDone.setText(getResources().getString(R.string.str_start));
//            mBooleanIsScanStart = false;
//            mZBarScannerView.stopCamera();
//            if (!result.getContents().equalsIgnoreCase("")) {
//                if (result.getContents().length() != 15) {
//                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_scan_device_sixteen_digit_no));
//                    return;
//                }
//                if (mActivity.mUtility.haveInternet()) {
//                    getDeviceInformationFromImei(result.getContents());
//                } else {
//                    if (mOnBackWithInstallationData != null) {
//                        mOnBackWithInstallationData.onBackWithInstallData(result.getContents());
//                    }
//                    mActivity.onBackPressed();
//                }
//            }
//        }
//    }

    //    public void checkImeiDevice(final String imeiNo) {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        params.put("imei", imeiNo);
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoAddInstallation> mLogin = mActivity.mApiService.testDeviceInstallation(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoAddInstallation>() {
//            @Override
//            public void onResponse(Call<VoAddInstallation> call, Response<VoAddInstallation> response) {
//                mActivity.mUtility.HideProgress();
//                if (isAdded()) {
//                    final VoAddInstallation mVoAddInstallation = response.body();
//                    Gson gson = new Gson();
//                    String json = gson.toJson(mVoAddInstallation);
//                    System.out.println("response mScanDeviceData---------" + json);
//                    if (mVoAddInstallation != null && mVoAddInstallation.getResponse().equalsIgnoreCase("true")) {
//                        if (mVoAddInstallation.getData() != null) {
//                            if (mVoAddInstallation.getData().getStatus() != null && mVoAddInstallation.getData().getStatus() != null && !mVoAddInstallation.getData().getStatus().equalsIgnoreCase("")) {
//                                if (mVoAddInstallation.getData().getStatus().equalsIgnoreCase("1")) {
////                                    if (isFromUninstall) {
//                                        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//                                        builder.setTitle(getResources().getString(R.string.str_test_device));
//                                        builder.setCancelable(false);
//                                        if (isFromUninstall) {
//                                            builder.setMessage(String.format(getResources().getString(R.string.str_test_device_ready_to_uninstall), imeiNo + ""));
//                                        } else {
//                                            builder.setMessage(String.format(getResources().getString(R.string.str_test_device_ready_to_inspect), imeiNo + ""));
//                                        }
//                                        builder.setPositiveButton(getResources().getString(R.string.str_test_device_view_last_record), new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
////                                            dialog.dismiss();
//                                                if (mVoAddInstallation.getData() != null) {
//                                                    FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
//                                                    Bundle mBundle = new Bundle();
//                                                    mBundle.putString("mIntent_Imei_no", imeiNo);
//                                                    mBundle.putSerializable("mIntent_installation", mVoAddInstallation);
//                                                    mBundle.putBoolean("mIntent_has_data", true);
//                                                    mBundle.putBoolean("mIntent_is_from_test_device", true);
//                                                    mBundle.putBoolean("mIntent_is_show_lat_long", false);
//                                                    mFragmentTestDevice.setOnTestResultSet(new onBackPressWithAction() {
//                                                        @Override
//                                                        public void onBackWithAction(String scanResult) {
//                                                            if (mOnBackWithInstallationData != null) {
//                                                                mOnBackWithInstallationData.onBackWithInstallData(mVoAddInstallation.getData());
//                                                            }
//                                                            mActivity.onBackPressed();
//                                                        }
//
//                                                        @Override
//                                                        public void onBackWithAction(String value1, String value2) {
//
//                                                        }
//
//                                                        @Override
//                                                        public void onBackWithAction(String imei, String deviceType, String warrantyStatus) {
//
//                                                        }
//                                                    });
//                                                    mActivity.replacesFragment(mFragmentTestDevice, true, mBundle, 1);
//                                                }
//                                            }
//                                        });
//                                        builder.setNegativeButton(getResources().getString(R.string.str_ok), new DialogInterface.OnClickListener() {
//                                            @Override
//                                            public void onClick(DialogInterface dialog, int which) {
//                                                dialog.dismiss();
//                                                if (mOnBackWithInstallationData != null) {
//                                                    mOnBackWithInstallationData.onBackWithInstallData(mVoAddInstallation.getData());
//                                                }
//                                                mActivity.onBackPressed();
//                                            }
//                                        });
//                                        builder.show();
////                                    }else {
////                                        getDeviceInformationFromImei(imeiNo, mVoAddInstallation);
////                                    }
//                                } else if (mVoAddInstallation.getData().getStatus().equalsIgnoreCase("2")) {
//                                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
//                                    builder.setTitle(getResources().getString(R.string.str_test_device));
//                                    builder.setCancelable(false);
//                                    if (isFromUninstall) {
//                                        builder.setMessage(String.format(getResources().getString(R.string.str_test_device_alrady_uninstall), imeiNo + ""));
//                                    } else {
//                                        builder.setMessage(String.format(getResources().getString(R.string.str_test_device_alrady_inspect), imeiNo + ""));
//                                    }
//                                    builder.setPositiveButton(getResources().getString(R.string.str_test_device_view_last_record), new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
////                                        dialog.dismiss();
//                                            if (mVoAddInstallation.getData() != null) {
//                                                FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
//                                                Bundle mBundle = new Bundle();
//                                                mBundle.putString("mIntent_Imei_no", imeiNo);
//                                                mBundle.putSerializable("mIntent_installation", mVoAddInstallation);
//                                                mBundle.putBoolean("mIntent_has_data", true);
//                                                mBundle.putBoolean("mIntent_is_from_test_device", true);
//                                                mBundle.putBoolean("mIntent_is_show_lat_long", false);
//                                                mFragmentTestDevice.setOnTestResultSet(new onBackPressWithAction() {
//                                                    @Override
//                                                    public void onBackWithAction(String scanResult) {
//                                                        mActivity.onBackPressedDirect();
//                                                    }
//
//                                                    @Override
//                                                    public void onBackWithAction(String value1, String value2) {
//
//                                                    }
//
//                                                    @Override
//                                                    public void onBackWithAction(String imei, String deviceType, String warrantyStatus) {
//
//                                                    }
//                                                });
//                                                mActivity.replacesFragment(mFragmentTestDevice, true, mBundle, 1);
//                                            }
//                                        }
//                                    });
//                                    builder.setNegativeButton(getResources().getString(R.string.str_go_back), new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//                                            mActivity.onBackPressedDirect();
//                                        }
//                                    });
//                                    builder.show();
//                                } else {
//                                    if (mVoAddInstallation != null && mVoAddInstallation.getMessage() != null && !mVoAddInstallation.getMessage().equalsIgnoreCase(""))
//                                        mActivity.mUtility.errorDialog(mVoAddInstallation.getMessage());
//                                }
//                            } else {
//                                if (mVoAddInstallation != null && mVoAddInstallation.getMessage() != null && !mVoAddInstallation.getMessage().equalsIgnoreCase(""))
//                                    mActivity.mUtility.errorDialog(mVoAddInstallation.getMessage());
//                            }
//                        } else {
//                            if (mVoAddInstallation != null && mVoAddInstallation.getMessage() != null && !mVoAddInstallation.getMessage().equalsIgnoreCase(""))
//                                mActivity.mUtility.errorDialog(mVoAddInstallation.getMessage());
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
//
//    }

    /*Get Device Information from Imei*/
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                                builder.setTitle(getResources().getString(R.string.str_test_device));
                                builder.setCancelable(false);
                                if (isFromUninstall) {
                                    builder.setMessage(String.format(getResources().getString(R.string.str_test_device_alrady_uninstall), imeiNo + ""));
                                } else {
                                    builder.setMessage(String.format(getResources().getString(R.string.str_test_device_alrady_inspect), imeiNo + ""));
                                }
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
                                                mActivity.onBackPressedDirect();
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
                                builder.setNegativeButton(getResources().getString(R.string.str_go_back), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        mActivity.onBackPressedDirect();
                                    }
                                });
                                builder.show();
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("INSTALLED")) {
                                if (mActivity.mUtility.haveInternet()) {
                                    getDeviceLatestInstall(mVoGetDeviceInfo);
                                } else {
                                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                                    builder.setTitle(getResources().getString(R.string.str_test_device));
                                    builder.setCancelable(false);
                                    if (isFromUninstall) {
                                        builder.setMessage(String.format(getResources().getString(R.string.str_test_device_ready_to_uninstall), imeiNo + ""));
                                    } else {
                                        builder.setMessage(String.format(getResources().getString(R.string.str_test_device_ready_to_inspect), imeiNo + ""));
                                    }
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
                                                    if (mOnBackWithInstallationData != null) {
                                                        mOnBackWithInstallationData.onBackWithInstallData(mVoGetDeviceInfo);
                                                    }
                                                    mActivity.onBackPressed();
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
                                            if (mOnBackWithInstallationData != null) {
                                                mOnBackWithInstallationData.onBackWithInstallData(mVoGetDeviceInfo);
                                            }
                                            mActivity.onBackPressed();
                                        }
                                    });
                                    builder.show();
                                }
                            } else if (mVoGetDeviceInfo.getStatus().toString().toUpperCase().equals("IN_STOCK")) {
                                mActivity.mUtility.errorDialog("You can't use this device at this time.");
                            } else {
                                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
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

    private void getDeviceLatestInstall(final VoGetDeviceInfo mVoGetDeviceInfo) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mUtility.ShowProgress();
        Call<VoInstallationResponse> mVoLastInstallationCall = mActivity.mApiService.getDeviceLatestInstallAPI(mVoGetDeviceInfo.getId());
        System.out.println("URL-" + mVoLastInstallationCall.request().url().toString());
        mVoLastInstallationCall.enqueue(new Callback<VoInstallationResponse>() {
            @Override
            public void onResponse(Call<VoInstallationResponse> call, Response<VoInstallationResponse> response) {
                mActivity.mUtility.HideProgress();
                if (isAdded()) {
                    System.out.println("response mLastInstall---------" + response.body());
                    final VoInstallationResponse mLastInstall = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mLastInstall);
                    System.out.println("response mLastInstall---------" + json);
                    if (response.code() == 200 || response.isSuccessful()) {
                        if (mLastInstall != null) {
                            mVoGetDeviceInfo.setType(mLastInstall.getDeviceType());
                            mVoGetDeviceInfo.setAssetId(mLastInstall.getRealAssetId());
                            mVoGetDeviceInfo.setAssetName(mLastInstall.getRealAssetName());
                            mVoGetDeviceInfo.setPortNo(mLastInstall.getRealAssetRegNo());
                            if (mLastInstall.getContactInfo() != null) {
                                mVoGetDeviceInfo.setContactInfo(mLastInstall.getContactInfo());
                            }
                            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
                            builder.setTitle(getResources().getString(R.string.str_test_device));
                            builder.setCancelable(false);
                            if (isFromUninstall) {
                                builder.setMessage(String.format(getResources().getString(R.string.str_test_device_ready_to_uninstall), mVoGetDeviceInfo.getImei() + ""));
                            } else {
                                builder.setMessage(String.format(getResources().getString(R.string.str_test_device_ready_to_inspect), mVoGetDeviceInfo.getImei() + ""));
                            }
                            builder.setPositiveButton(getResources().getString(R.string.str_test_device_view_last_record), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                        dialog.dismiss();
                                    FragmentTestDevice mFragmentTestDevice = new FragmentTestDevice();
                                    Bundle mBundle = new Bundle();
                                    mBundle.putString("mIntent_Imei_no", mVoGetDeviceInfo.getImei());
                                    mBundle.putString("mIntent_device_id", mVoGetDeviceInfo.getId());
                                    mBundle.putBoolean("mIntent_has_data", true);
                                    mBundle.putBoolean("mIntent_is_from_test_device", false);
                                    mBundle.putSerializable("mIntent_last_install_record", new VoInstallationResponse());
                                    mFragmentTestDevice.setOnTestResultSet(new onBackPressWithAction() {
                                        @Override
                                        public void onBackWithAction(String scanResult) {
                                            if (mOnBackWithInstallationData != null) {
                                                mOnBackWithInstallationData.onBackWithInstallData(mVoGetDeviceInfo);
                                            }
                                            mActivity.onBackPressed();
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
                                    if (mOnBackWithInstallationData != null) {
                                        mOnBackWithInstallationData.onBackWithInstallData(mVoGetDeviceInfo);
                                    }
                                    mActivity.onBackPressed();
                                }
                            });
                            builder.show();
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

    @Override
    public void onResume() {
        super.onResume();
        if (!requestForPermission()) {
            requestForPermission();
        }
//        mZBarScannerView.startCamera();
        mZXingScannerView.startCamera();
    }

    public boolean requestForPermission() {
        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessCamera()) {
                isPermissionOn = false;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(CAMERA_PERMS, CAMERA_PERMISSION_REQUEST);
                }
            }
        }
        return isPermissionOn;
    }

    public boolean canAccessCamera() {
        return (hasPermission(Manifest.permission.CAMERA));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(mActivity, perm));
    }

    @Override
    public void onPause() {
        super.onPause();
//        mZBarScannerView.stopCamera();           // Stop camera on pause
        mZXingScannerView.stopCamera();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mTextViewDone.setVisibility(View.GONE);
    }
}
