package com.vithamastech.smartlight.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.AppCompatEditText;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.vithamastech.smartlight.AppIntroActivity;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.doorbell.DoorbellApi;
import com.vithamastech.smartlight.doorbell.RestCallback;
import com.vithamastech.smartlight.doorbell.RestErrorCallback;
import com.vithamastech.smartlight.helper.URLCLASS;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 23-12-2017.
 */

public class FragmentHelp extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;


    private DoorbellApi mApi;
    private JSONObject mProperties;

    private static final String PROPERTY_MODEL = "Model";
    private static final String PROPERTY_ANDROID_VERSION = "Android Version";
    private static final String PROPERTY_WI_FI_ENABLED = "WiFi enabled";
    private static final String PROPERTY_MOBILE_DATA_ENABLED = "Mobile Data enabled";
    private static final String PROPERTY_GPS_ENABLED = "GPS enabled";
    private static final String PROPERTY_SCREEN_RESOLUTION = "Screen Resolution";
    private static final String PROPERTY_ACTIVITY = "Activity";
    private static final String PROPERTY_APP_VERSION_NAME = "App Version Name";
    private static final String PROPERTY_APP_VERSION_CODE = "App Version Code";
    private static final String PROPERTY_APP_LANGUAGE = "App Language";
    private static final String PROPERTY_DEVICE_LANGUAGE = "Device Language";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_help, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_settings_help);
        mActivity.mImageViewBack.setVisibility(View.GONE);
//        mActivity.mImageViewConnectionStatus.setVisibility(View.VISIBLE);
        mActivity.showBackButton(false);
        return mViewRoot;
    }

    @OnClick(R.id.fragment_help_ll_follow_us)
    public void onFollowUsClick(View mViewS) {
//        FragmentWebView mFragmentWebview = new FragmentWebView();
//        Bundle mBundle = new Bundle();
//        mBundle.putString("intent_title", "Follow us");
//        mBundle.putString("intent_url", "https://in.linkedin.com/company/vithamas-technologies-pvt-ltd");
//        mActivity.replacesFragment(mFragmentWebview, true, mBundle, 0);

    }

    @OnClick(R.id.fragment_help_ll_user_manual)
    public void onUserManualClick(View mView) {
        Intent mIntent = new Intent(mActivity, AppIntroActivity.class);
        mIntent.putExtra("isFirstTime", false);
        startActivity(mIntent);
    }

    @OnClick(R.id.fragment_help_ll_youtube)
    public void onYoutubeClick(View mView) {
        FragmentWebView mFragmentWebview = new FragmentWebView();
        Bundle mBundle = new Bundle();
        mBundle.putString("intent_title", getResources().getString(R.string.app_name));
        mBundle.putString("intent_url", getResources().getString(R.string.frg_contact_us_youtube));
        mActivity.replacesFragment(mFragmentWebview, true, mBundle, 0);
    }

    @OnClick(R.id.fragment_help_ll_feedback)
    public void onFeedbackClick(View mView) {
        if (isAdded()) {
            showFeedBackDialog();
        }
    }

    @OnClick(R.id.fragment_help_ll_about_us)
    public void onAboutUsClick(View mView) {
        FragmentWebView mFragmentWebview = new FragmentWebView();
        Bundle mBundle = new Bundle();
        mBundle.putString("intent_title", getResources().getString(R.string.frg_settings_about_us));
        mBundle.putString("intent_url", getResources().getString(R.string.frg_contact_about_us));
        mActivity.replacesFragment(mFragmentWebview, true, mBundle, 0);
    }

    /*Show Feedback dialog*/
    private void showFeedBackDialog() {
        final Dialog myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_feedback);
        myDialog.setCancelable(false);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparentWhite)));
        Button mButtonSend = (Button) myDialog.findViewById(R.id.popup_feedback_button_send);
        Button mButtonCancel = (Button) myDialog.findViewById(R.id.popup_feedback_button_cancel);
        final View mView = (View) myDialog.findViewById(R.id.popup_feedback_main_layout);
        final AppCompatEditText mEditTextMsg = (AppCompatEditText) myDialog.findViewById(R.id.popup_feedback_edittext_msg);
        final AppCompatEditText mEditTextEmail = (AppCompatEditText) myDialog.findViewById(R.id.popup_feedback_edittext_email);
        mEditTextEmail.setText(mActivity.mPreferenceHelper.getUser_email());
        mApi = new DoorbellApi(mActivity);
        mApi.setAppId(URLCLASS.DOORBELL_APP_ID);
        mApi.setApiKey(URLCLASS.DOORBELL_API_KEY);
        // Set app related properties
        mProperties = new JSONObject();
        buildProperties();

        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String mStringMsg = mEditTextMsg.getText().toString().trim();
                String mStringEmail = mEditTextEmail.getText().toString().trim();
                mActivity.mUtility.hideKeyboard(mActivity);
                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
                InputMethodManager imm1 = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(mEditTextEmail.getWindowToken(), 0);

                if (mStringMsg.equalsIgnoreCase("")) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_settings_feedback_enter_feedback), getResources().getString(R.string.str_ok));
                    return;
                }
                if (mStringEmail.equalsIgnoreCase("")) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_sign_up_enter_email_address), getResources().getString(R.string.str_ok));
                    return;
                }
                if (!mActivity.mUtility.isValidEmail(mStringEmail)) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_sign_up_enter_valid_email_address), getResources().getString(R.string.str_ok));
                    return;
                }
                if (mActivity.mUtility.haveInternet()) {
                    myDialog.dismiss();
                    mApi.setLoadingMessage(mActivity.getString(R.string.doorbell_sending));
                    mApi.sendFeedback(mStringMsg, mStringEmail, mProperties, mActivity.mPreferenceHelper.getUserContactNo());
                    mApi.setCallback(new RestCallback() {
                        @Override
                        public void success(Object obj) {
                            mActivity.mUtility.errorDialog(obj.toString(), 0, true);
                        }
                    });
                    mApi.setErrorCallback(new RestErrorCallback() {
                        @Override
                        public void error(String message) {
                            mActivity.mUtility.errorDialog(message, 1, true);
                        }
                    });
                } else {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
                }
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.mUtility.hideKeyboard(mActivity);
                InputMethodManager imm = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
                InputMethodManager imm1 = (InputMethodManager) mActivity.getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm1.hideSoftInputFromWindow(mEditTextEmail.getWindowToken(), 0);
                myDialog.dismiss();
            }
        });
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    /*Build Feedback properties*/
    private void buildProperties() {
        // Set phone related properties
        try {
            mProperties.put("Brand", mActivity.mPreferenceHelper.getUserId()); // mobile phone carrier
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            mProperties.put(PROPERTY_MODEL, Build.MODEL);
            mProperties.put(PROPERTY_ANDROID_VERSION, Build.VERSION.RELEASE);
            try {
                SupplicantState supState;
                WifiManager wifiManager = (WifiManager) mActivity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                supState = wifiInfo.getSupplicantState();

                mProperties.put(PROPERTY_WI_FI_ENABLED, supState);
            } catch (Exception e) {
                e.printStackTrace();
            }
            boolean mobileDataEnabled = false; // Assume disabled
            ConnectivityManager cm = (ConnectivityManager) mActivity.getSystemService(Context.CONNECTIVITY_SERVICE);
            try {
                Class cmClass = Class.forName(cm.getClass().getName());
                Method method = cmClass.getDeclaredMethod("getMobileDataEnabled");
                method.setAccessible(true); // Make the method callable
                // get the setting for "mobile data"
                mobileDataEnabled = (Boolean) method.invoke(cm);
            } catch (Exception e) {
                // Some problem accessible private API
                // TODO do whatever error handling you want here
                e.printStackTrace();
            }
            mProperties.put(PROPERTY_MOBILE_DATA_ENABLED, mobileDataEnabled);

            try {
                final LocationManager manager = (LocationManager) mActivity.getSystemService(Context.LOCATION_SERVICE);
                boolean gpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

                mProperties.put(PROPERTY_GPS_ENABLED, gpsEnabled);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                DisplayMetrics metrics = new DisplayMetrics();

                this.mActivity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                String resolution = Integer.toString(metrics.widthPixels) + "x" + Integer.toString(metrics.heightPixels);
                mProperties.put(PROPERTY_SCREEN_RESOLUTION, resolution);
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                String activityName = this.mActivity.getClass().getSimpleName();
                mProperties.put(PROPERTY_ACTIVITY, activityName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            mProperties.put(PROPERTY_APP_LANGUAGE, this.mActivity.getResources().getConfiguration().locale.getDisplayName());
            mProperties.put(PROPERTY_DEVICE_LANGUAGE, Locale.getDefault().getDisplayName());

            PackageManager manager = mActivity.getPackageManager();
            try {
                PackageInfo info = manager.getPackageInfo(mActivity.getPackageName(), 0);
                mProperties.put(PROPERTY_APP_VERSION_NAME, info.versionName);
                mProperties.put(PROPERTY_APP_VERSION_CODE, info.versionCode);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mActivity.mUtility.hideKeyboard(mActivity);
        Snackbar mSnackBar = Snackbar.make(mView, mStringMessage, 5000);
        mSnackBar.setAction(mActionMessage, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mSnackBar.setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
        mSnackBar.getView().setBackgroundColor(getResources().getColor(R.color.colorInActiveMenu));
        mSnackBar.show();
    }

    @Override
    public void onResume() {
        mActivity.showBackButton(false);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
//        mActivity.mImageViewConnectionStatus.setVisibility(View.GONE);
    }
}
