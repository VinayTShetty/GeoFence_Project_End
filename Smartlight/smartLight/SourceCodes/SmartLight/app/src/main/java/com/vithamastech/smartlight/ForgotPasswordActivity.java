package com.vithamastech.smartlight;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hbb20.CountryCodePicker;
import com.vithamastech.smartlight.Vo.VoLoginData;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.helper.PreferenceHelper;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.helper.Utility;
import com.vithamastech.smartlight.interfaces.API;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ForgotPasswordActivity extends AppCompatActivity {
    private Retrofit mRetrofit;
    private API mApiService;
    private PreferenceHelper mPreferenceHelper;
    Utility mUtility;
    DBHelper mDbHelper;
    @BindView(R.id.activity_forgot_pw_edittext_mobile)
    AppCompatEditText mEditTextMobileNO;
    @BindView(R.id.activity_forgot_pw_button_submit)
    Button mButtonRegister;
    @BindView(R.id.activity_forgot_pw_picker_countrycode)
    CountryCodePicker countryCodePicker;
    @BindView(R.id.activity_forgot_password_rl_main)
    RelativeLayout mRelativeLayoutMain;
    String mStringCountryCode = "+91";
    String mStringMobileNO = "";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_forgot_password);
        ButterKnife.bind(ForgotPasswordActivity.this);
        mDbHelper = DBHelper.getDBHelperInstance(ForgotPasswordActivity.this);
        mUtility = new Utility(ForgotPasswordActivity.this);
        mPreferenceHelper = new PreferenceHelper(ForgotPasswordActivity.this);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.MAIN_URL)
                .client(mUtility.getSimpleClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);
//        countryCodePicker.registerCarrierNumberEditText(mEditTextMobileNO);
        countryCodePicker.setDefaultCountryUsingNameCode("IN");
//        countryCodePicker.setNumberAutoFormattingEnabled(true);

        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                mStringCountryCode = countryCodePicker.getSelectedCountryCodeWithPlus();
            }
        });

    }

    /* Back To previous screen*/
    @OnClick(R.id.activity_forgot_pw_imageview_back)
    public void onBackClick(View mView) {
        onBackPressed();
    }

    @OnClick(R.id.activity_forgot_pw_button_submit)
    public void onSubmitClick(View mView) {
        mStringMobileNO = mEditTextMobileNO.getText().toString().trim();

        if (mStringCountryCode.equalsIgnoreCase("")) {
            showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_select_country_code), getResources().getString(R.string.str_ok));
            return;
        }

        if (mStringMobileNO.equalsIgnoreCase("")) {
            showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_mobile_no), getResources().getString(R.string.str_ok));
            return;
        }

        if (mStringMobileNO.length() < 10 || mStringMobileNO.length() > 15) {
            showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_valid_mobile_no), getResources().getString(R.string.str_ok));
            return;
        }

        if (!mUtility.haveInternet()) {
            showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
        } else {
            checkUserRegister();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.overridePendingTransition(R.anim.enter,
                R.anim.exit);
    }

    /*Check user already register or not if register then send reset otp otherwise not*/
    private void checkUserRegister() {
        mUtility.hideKeyboard(ForgotPasswordActivity.this);
        mUtility.ShowProgress("Please Wait..");
        Map<String, String> params = new HashMap<String, String>();
        params.put("mobile_number", mStringMobileNO);
        Call<VoLoginData> mLogin = mApiService.checkUserAlreadyRegistered(params);
        mLogin.enqueue(new Callback<VoLoginData>() {
            @Override
            public void onResponse(Call<VoLoginData> call, Response<VoLoginData> response) {
                mUtility.HideProgress();
                VoLoginData mLoginData = response.body();
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    Intent mIntent = new Intent(ForgotPasswordActivity.this, VerifyRegisterAccount.class);
                    mIntent.putExtra("intent_mobileno", mStringMobileNO);
                    mIntent.putExtra("intent_country_code", mStringCountryCode);
                    mIntent.putExtra("intent_user_data", mLoginData);
                    mIntent.putExtra("intent_is_from_signup", false);
                    startActivity(mIntent);
                } else {
                    showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_forgot_password_mobile_not_register, mStringMobileNO), getResources().getString(R.string.str_ok));
                }
            }

            @Override
            public void onFailure(Call<VoLoginData> call, Throwable t) {
                mUtility.HideProgress();
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
            }
        });
    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mUtility.hideKeyboard(ForgotPasswordActivity.this);
        Snackbar.make(mView, mStringMessage, 5000)
                .setAction(mActionMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUtility.hideKeyboard(ForgotPasswordActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.activityPaused();
    }
}
