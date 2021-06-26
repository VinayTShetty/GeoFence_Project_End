package com.vithamastech.smartlight;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import com.google.android.material.snackbar.Snackbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.content.res.ResourcesCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.PasswordTransformationMethod;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;
import com.hbb20.CountryCodePicker;
import com.vithamastech.smartlight.Vo.VoLoginData;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.PreferenceHelper;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.helper.Utility;
import com.vithamastech.smartlight.interfaces.API;

import java.security.NoSuchAlgorithmException;
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


public class RegisterActivity extends AppCompatActivity {

    private Retrofit mRetrofit;
    private API mApiService;
    private PreferenceHelper mPreferenceHelper;
    Utility mUtility;
    DBHelper mDbHelper;
    @BindView(R.id.activity_register_edittext_username)
    AppCompatEditText mEditTextUsername;
    @BindView(R.id.activity_register_et_account_name)
    AppCompatEditText mEditTextAccountName;
    @BindView(R.id.activity_register_edittext_password)
    AppCompatEditText mEditTextPassword;
    @BindView(R.id.activity_register_edittext_email)
    AppCompatEditText mEditTextEmail;
    @BindView(R.id.activity_register_edittext_mobile)
    AppCompatEditText mEditTextMobileNO;
    @BindView(R.id.activity_register_button_register)
    Button mButtonRegister;
    @BindView(R.id.activity_register_checkbox_terms_condition)
    CheckBox mCheckBoxTermNCondition;
    @BindView(R.id.activity_register_textview_terms_n_condition)
    TextView mTextViewTermNCondition;
    @BindView(R.id.activity_register_country_picker_countrycode)
    CountryCodePicker countryCodePicker;
    @BindView(R.id.activity_register_rl_main)
    ConstraintLayout mRelativeLayoutMain;
    @BindView(R.id.activity_register_textview_already_account)
    TextView textViewSignIn;

    String mStringCountryCode = "+91";
    String mStringUsername = "";
    String mStringAccountName = "";
    String mStringEmail = "";
    String mStringMobileNO = "";
    String mStringPassword = "";
    String mStringDevicesUIDFCMToken = "test";
    String mStringGuestUserId = "0000";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_register);
        ButterKnife.bind(RegisterActivity.this);
        mCheckBoxTermNCondition.setTypeface(ResourcesCompat.getFont(this, R.font.century_gothic));
        mDbHelper = DBHelper.getDBHelperInstance(RegisterActivity.this);
        mUtility = new Utility(RegisterActivity.this);
        mPreferenceHelper = new PreferenceHelper(RegisterActivity.this);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.MAIN_URL)
                .client(mUtility.getSimpleClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);
        mStringDevicesUIDFCMToken = FirebaseInstanceId.getInstance().getToken();
        mPreferenceHelper.setDeviceToken(mStringDevicesUIDFCMToken);
        mEditTextPassword.setTransformationMethod(new PasswordTransformationMethod());
        LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver,
                new IntentFilter("FcmTokenReceiver"));
//        countryCodePicker.registerCarrierNumberEditText(mEditTextMobileNO);
        countryCodePicker.setDefaultCountryUsingNameCode("IN");
//        countryCodePicker.setNumberAutoFormattingEnabled(true);

        try{
            String text = getResources().getString(R.string.str_sign_up_already_account);
            SpannableString spannableString = new SpannableString(text);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            spannableString.setSpan(boldSpan,26,32, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            textViewSignIn.setText(spannableString);
        }catch (Exception e){
            e.printStackTrace();
        }

        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                mStringCountryCode = countryCodePicker.getSelectedCountryCodeWithPlus();
            }
        });
        mRelativeLayoutMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUtility.hideKeyboard(RegisterActivity.this);
            }
        });
    }
    /* Receive fcm token when generated*/
    BroadcastReceiver tokenReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String token = intent.getStringExtra("fcm_token");
            if (token != null) {
                mStringDevicesUIDFCMToken = token;
            }
        }
    };

    @OnClick(R.id.activity_register_textview_already_account)
    public void onSignInClick(View mView) {
        Intent mIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        mIntent.putExtra("is_from_add_account", false);
        startActivity(mIntent);
        finish();
    }

    @OnClick(R.id.activity_register_textview_terms_n_condition)
    public void onTermsAndConditionClick(View mView) {
        startActivity(new Intent(RegisterActivity.this, TermsAndConditionActivity.class));
    }

    /* Skip User(Guest) login */
    @OnClick(R.id.activity_register_textview_skip)
    public void onSkipClick(View mView) {
        mPreferenceHelper.setUserId(mStringGuestUserId);
        mPreferenceHelper.setUserFirstName("Guest User");
        mPreferenceHelper.setIsSkipUser(true);
        try {
            String hash256 = BLEUtility.hash256Encryption(URLCLASS.APP_SKIP_PW);
            String mHashSecretKey = (hash256.length() >= 32) ? hash256.substring(hash256.length() - 32, hash256.length()) : hash256;
            mPreferenceHelper.setSecretKey(mHashSecretKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        // Add Default 6 alarm entry
        String isExistInDB;
        ContentValues mContentValues;
        for (int i = 0; i < 6; i++) {
            isExistInDB = CheckRecordExistInAlarmDB(i + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues = new ContentValues();
                mContentValues.put(mDbHelper.mFieldAlarmUserId, mPreferenceHelper.getUserId());
                mContentValues.put(mDbHelper.mFieldAlarmName, "Alarm " + (i + 1));
                mContentValues.put(mDbHelper.mFieldAlarmTime, "");
                mContentValues.put(mDbHelper.mFieldAlarmStatus, "1");
                mContentValues.put(mDbHelper.mFieldAlarmDays, "6543210");
                mContentValues.put(mDbHelper.mFieldAlarmLightOn, "0");
                mContentValues.put(mDbHelper.mFieldAlarmWakeUpSleep, "0");
                mContentValues.put(mDbHelper.mFieldAlarmColor, 0);
                mContentValues.put(mDbHelper.mFieldAlarmCountNo, i);
                mContentValues.put(mDbHelper.mFieldAlarmIsActive, "0");
                mContentValues.put(mDbHelper.mFieldAlarmIsSync, "0");
                mDbHelper.insertRecord(mDbHelper.mTableAlarm, mContentValues);
            }
        }

        Intent mIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mIntent.putExtra("isFromLogin", true);
        startActivity(mIntent);
        finish();
    }

    /* On Register Button Click*/
    @OnClick(R.id.activity_register_button_register)
    public void onRegisterClick(View mView) {
        if (!isFinishing()) {
            mUtility.hideKeyboard(RegisterActivity.this);
            mStringUsername = mEditTextUsername.getText().toString().trim();
            mStringAccountName = mEditTextAccountName.getText().toString().trim();
            mStringEmail = mEditTextEmail.getText().toString().trim();
            mStringMobileNO = mEditTextMobileNO.getText().toString().trim();
            mStringPassword = mEditTextPassword.getText().toString().trim();
            mStringDevicesUIDFCMToken = FirebaseInstanceId.getInstance().getToken();
            mPreferenceHelper.setDeviceToken(mStringDevicesUIDFCMToken);
            if (mStringUsername.equalsIgnoreCase("")) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_name), getResources().getString(R.string.str_ok));
                return;
            }
            if (mStringAccountName.equalsIgnoreCase("")) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_account_name), getResources().getString(R.string.str_ok));
                return;
            }
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
            if (!mStringEmail.equalsIgnoreCase("")) {
                if (!mUtility.isValidEmail(mStringEmail)) {
                    showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_valid_email_address), getResources().getString(R.string.str_ok));
                    return;
                }
            }
            if (mStringPassword.equalsIgnoreCase("")) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_password), getResources().getString(R.string.str_ok));
                return;
            }
            if (mStringPassword.length() < 6 || mStringPassword.length() > 15) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_valid_password), getResources().getString(R.string.str_ok));
                return;
            }
            if (!mCheckBoxTermNCondition.isChecked()) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_agree_terms_condition), getResources().getString(R.string.str_ok));
                return;
            }
            if (mStringDevicesUIDFCMToken == null || mStringDevicesUIDFCMToken.equalsIgnoreCase("null") || mStringDevicesUIDFCMToken.equalsIgnoreCase("")) {
                mStringDevicesUIDFCMToken = FirebaseInstanceId.getInstance().getToken();
            }
            if (mStringDevicesUIDFCMToken == null || mStringDevicesUIDFCMToken.equalsIgnoreCase("null") || mStringDevicesUIDFCMToken.equalsIgnoreCase("")) {
                mStringDevicesUIDFCMToken = System.currentTimeMillis() + "";
            }

            console.log("asxasxasxasx_token",mStringDevicesUIDFCMToken);

            if (!mUtility.haveInternet()) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
            } else {
                checkUserRegister();
            }
        }
    }
    /* Call user Register API */
    private void checkUserRegister() {
        console.log("aschschjcvshj", mStringCountryCode + mStringMobileNO);
        console.log("asxasxasxasx_token","Register here!!!!!");
        mUtility.hideKeyboard(RegisterActivity.this);
        mUtility.ShowProgress("Please Wait..");
        Map<String, String> params = new HashMap<String, String>();
        params.put("mobile_number", mStringMobileNO);
        Call<VoLoginData> mLogin = mApiService.checkUserAlreadyRegistered(params);
        mLogin.enqueue(new Callback<VoLoginData>() {
            @Override
            public void onResponse(Call<VoLoginData> call, Response<VoLoginData> response) {
                mUtility.HideProgress();
                VoLoginData mLoginData = response.body();
                // If not register then send detail with otp.
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("false")) {
                    Intent mIntent = new Intent(RegisterActivity.this, VerifyRegisterAccount.class);
                    mIntent.putExtra("intent_username", mStringUsername);
                    mIntent.putExtra("intent_account_name", mStringAccountName);
                    mIntent.putExtra("intent_email", mStringEmail);
                    mIntent.putExtra("intent_mobileno", mStringMobileNO);
                    mIntent.putExtra("intent_password", mStringPassword);
                    mIntent.putExtra("intent_fcm_token", mStringDevicesUIDFCMToken);
                    mIntent.putExtra("intent_country_code", mStringCountryCode);
                    mIntent.putExtra("intent_is_from_signup", true);
                    startActivity(mIntent);
                } else if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_already_register, mStringMobileNO), getResources().getString(R.string.str_ok));
                } else {
                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
                        showMessageRedAlert(mRelativeLayoutMain, mLoginData.getMessage(), getResources().getString(R.string.str_ok));
                }
            }

            @Override
            public void onFailure(Call<VoLoginData> call, Throwable t) {
                mUtility.HideProgress();
                console.log("asxasxasx",new Gson().toJson(call.request().body()));
                t.printStackTrace();
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
            }
        });
    }

    /* Check record is exist in alarm table or not*/
    private String CheckRecordExistInAlarmDB(String alarmCount) {
        DataHolder mDataHolder;
        String url = "select * from " + mDbHelper.mTableAlarm + " where " + mDbHelper.mFieldAlarmUserId + "= '" + mPreferenceHelper.getUserId() + "'" + " and " + mDbHelper.mFieldAlarmCountNo + "= '" + alarmCount + "'";
        System.out.println(" URL : " + url);
        mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" AlarmList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldAlarmLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent mIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        mIntent.putExtra("is_from_add_account", false);
        startActivity(mIntent);
        finish();
    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mUtility.hideKeyboard(RegisterActivity.this);
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