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
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.iid.FirebaseInstanceId;
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


/**
 * Created by Jaydeep on 16-02-2018.
 */

public class LoginActivity extends AppCompatActivity {
    private Retrofit mRetrofit;
    private API mApiService;
    private PreferenceHelper mPreferenceHelper;
    Utility mUtility;
    DBHelper mDbHelper;

    @BindView(R.id.activity_login_iv_back)
    ImageView mImageViewBack;
    @BindView(R.id.activity_login_textview_skip)
    TextView mTextViewSkip;
    @BindView(R.id.activity_login_textview_dont_account)
    TextView mTextViewDontAccount;
    @BindView(R.id.activity_login_textview_forgot_password)
    TextView mTextViewForgotPassword;
    @BindView(R.id.activity_login_edittext_mobile)
    AppCompatEditText mEditTextMobileNo;
    @BindView(R.id.activity_login_edittext_password)
    AppCompatEditText mEditTextPassword;
    @BindView(R.id.activity_login_checkbox_remember_pw)
    CheckBox mCheckBoxRememberPw;
    @BindView(R.id.activity_login_buttton_login)
    Button mButtonLogin;
    @BindView(R.id.activity_login_rl_main)
    ConstraintLayout mRelativeLayoutMain;
    private String mStringMobileNo = "";
    private String mStringPassword = "";
    private String mStringDeviceType = "1";
    private String mStringDevicesUIDFCMToken = "test";
    String mStringGuestUserId = "0000";
    boolean isFromAddAccount = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(LoginActivity.this);
        if (getIntent() != null) {
            isFromAddAccount = getIntent().getBooleanExtra("is_from_add_account", false);
        }
        mDbHelper = DBHelper.getDBHelperInstance(LoginActivity.this);
        mUtility = new Utility(LoginActivity.this);
        mPreferenceHelper = new PreferenceHelper(LoginActivity.this);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.MAIN_URL)
                .client(mUtility.getSimpleClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);
        mStringDevicesUIDFCMToken = FirebaseInstanceId.getInstance().getToken();
        mPreferenceHelper.setDeviceToken(mStringDevicesUIDFCMToken);
        mCheckBoxRememberPw.setTypeface(ResourcesCompat.getFont(this, R.font.century_gothic));
        mEditTextPassword.setTransformationMethod(new PasswordTransformationMethod());

        try{
            String text = getResources().getString(R.string.str_login_dont_account);
            SpannableString spannableString = new SpannableString(text);
            StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
            spannableString.setSpan(boldSpan,22,30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mTextViewDontAccount.setText(spannableString);
        }catch (Exception e){
            e.printStackTrace();
        }

    if (isFromAddAccount) {
            mImageViewBack.setVisibility(View.VISIBLE);
            mTextViewSkip.setVisibility(View.GONE);
            mTextViewDontAccount.setVisibility(View.GONE);
        } else {
            mImageViewBack.setVisibility(View.GONE);
            mTextViewSkip.setVisibility(View.VISIBLE);
            mTextViewDontAccount.setVisibility(View.VISIBLE);
            if (mPreferenceHelper.getIsRememberMe()) {
                mCheckBoxRememberPw.setChecked(true);
                mEditTextMobileNo.setText(mPreferenceHelper.getRememberMeUsername());
                mEditTextPassword.setText(mPreferenceHelper.getRememberMePassword());
            } else {
                mEditTextMobileNo.setText(mPreferenceHelper.getRememberMeUsername());
                mCheckBoxRememberPw.setChecked(false);
            }
        }
        mCheckBoxRememberPw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mUtility.hideKeyboard(LoginActivity.this);
            }
        });
        mEditTextPassword.setOnEditorActionListener(new AppCompatEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mUtility.hideKeyboard(LoginActivity.this);
                    return true;
                }
                return false;
            }
        });
        LocalBroadcastManager.getInstance(this).registerReceiver(tokenReceiver,
                new IntentFilter("FcmTokenReceiver"));
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

    @OnClick(R.id.activity_login_iv_back)
    public void onBackClick(View mView) {
        finish();
    }

    /* Skip User(Guest) login */
    @OnClick(R.id.activity_login_textview_skip)
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
        for (int i = 0; i < 6; i++) {
            isExistInDB = CheckRecordExistInAlarmDB(i + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                ContentValues mContentValues = new ContentValues();
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


        Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
        mIntent.putExtra("isFromLogin", true);
        startActivity(mIntent);
        finish();
    }

    @OnClick(R.id.activity_login_textview_dont_account)
    public void onRegisterClick(View mView) {
        Intent mIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(mIntent);
        finish();
    }

    @OnClick(R.id.activity_login_textview_forgot_password)
    public void onForgotPasswordClick(View mView) {
        mUtility.hideKeyboard(LoginActivity.this);
//        forgotPasswordDialog();
        Intent mIntent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(mIntent);
    }

    /* On Login Button Click*/
    @OnClick(R.id.activity_login_buttton_login)
    public void onLoginClick(View mView) {
        if (!isFinishing()) {
            mUtility.hideKeyboard(LoginActivity.this);
            mStringMobileNo = mEditTextMobileNo.getText().toString().trim();
            mStringPassword = mEditTextPassword.getText().toString().trim();
            mStringDevicesUIDFCMToken = FirebaseInstanceId.getInstance().getToken();
            mPreferenceHelper.setDeviceToken(mStringDevicesUIDFCMToken);
            if (mStringMobileNo.equalsIgnoreCase("")) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_mobile_no), getResources().getString(R.string.str_ok));
                return;
            }
            if (mStringMobileNo.length() < 10 || mStringMobileNo.length() > 15) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_valid_mobile_no), getResources().getString(R.string.str_ok));
                return;
            }
            if (mStringPassword.equalsIgnoreCase("")) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_login_enter_password), getResources().getString(R.string.str_ok));
                return;
            }
            if (!mUtility.haveInternet()) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
            } else {
                userLogin();
            }
        }
    }

    /* Call user login API */
    private void userLogin() {
        mUtility.hideKeyboard(LoginActivity.this);
        mUtility.ShowProgress("Please Wait..");
        if (mStringDevicesUIDFCMToken == null || mStringDevicesUIDFCMToken.equalsIgnoreCase("null") || mStringDevicesUIDFCMToken.equalsIgnoreCase("")) {
            mStringDevicesUIDFCMToken = FirebaseInstanceId.getInstance().getToken();
        }
        if (mStringDevicesUIDFCMToken == null || mStringDevicesUIDFCMToken.equalsIgnoreCase("null") || mStringDevicesUIDFCMToken.equalsIgnoreCase("")) {
            mStringDevicesUIDFCMToken = System.currentTimeMillis() + "";
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("mobile_number", mStringMobileNo);
        params.put("password", mStringPassword);
        params.put("device_type", mStringDeviceType);
        params.put("device_token", mStringDevicesUIDFCMToken);
        Call<VoLoginData> mLogin = mApiService.userLoginAPI(params);
        mLogin.enqueue(new Callback<VoLoginData>() {
            @Override
            public void onResponse(Call<VoLoginData> call, Response<VoLoginData> response) {
                mUtility.HideProgress();
                VoLoginData mLoginData = response.body();
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    if (mLoginData.getData() != null) {
                        // check is from add account if yes then add user to user table account.
                        if (isFromAddAccount) {
                            if (mLoginData.getData().getUser_id().equalsIgnoreCase(mPreferenceHelper.getUserId())) {
                                mUtility.errorDialog(getResources().getString(R.string.str_login_already_login_user), 1, true);
                            } else {
                                ContentValues mContentValuesUser = new ContentValues();
                                mContentValuesUser.put(mDbHelper.mFieldUserServerID, mLoginData.getData().getUser_id());
                                mContentValuesUser.put(mDbHelper.mFieldUserName, mLoginData.getData().getUserName());
                                mContentValuesUser.put(mDbHelper.mFieldUserAccountName, mLoginData.getData().getAccount_name());
                                mContentValuesUser.put(mDbHelper.mFieldUserEmail, mLoginData.getData().getEmail());
                                mContentValuesUser.put(mDbHelper.mFieldUserMobileNo, mLoginData.getData().getMobile_number());
                                mContentValuesUser.put(mDbHelper.mFieldUserPassword, mLoginData.getData().getPassword());
                                mContentValuesUser.put(mDbHelper.mFieldUserToken, mStringDevicesUIDFCMToken);
                                String isExistInUserDB = CheckRecordExistInUserAccountDB(mLoginData.getData().getUser_id());
                                if (isExistInUserDB.equalsIgnoreCase("-1")) {
                                    mDbHelper.insertRecord(mDbHelper.mTableUserAccount, mContentValuesUser);
                                } else {
                                    mDbHelper.updateRecord(mDbHelper.mTableUserAccount, mContentValuesUser, mDbHelper.mFieldUserLocalID + "=?", new String[]{isExistInUserDB});
                                }
                            }
                            finish();
                        } else {
                            // Store user details in preference
                            mPreferenceHelper.setUserFirstName(mLoginData.getData().getUserName());
                            mPreferenceHelper.setAccountName(mLoginData.getData().getAccount_name());
                            mPreferenceHelper.setUserId(mLoginData.getData().getUser_id());
                            mPreferenceHelper.setUserEmail(mLoginData.getData().getEmail());
                            mPreferenceHelper.setUserContactNo(mLoginData.getData().getMobile_number());
                            mPreferenceHelper.setUserPassword(mLoginData.getData().getPassword());
                            mPreferenceHelper.setDeviceToken(mStringDevicesUIDFCMToken);

                            // Encrypt Mobile no with key and generate secret key
                            try {
                                String hash256 = BLEUtility.hash256Encryption(mLoginData.getData().getMobile_number() + "~vith");
                                String mHashSecretKey = (hash256.length() >= 32) ? hash256.substring(hash256.length() - 32, hash256.length()) : hash256;
                                mPreferenceHelper.setSecretKey(mHashSecretKey);
                            } catch (NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            mPreferenceHelper.setIsSkipUser(false);
                            // Add default 6 alarm entry.

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

                            if (mCheckBoxRememberPw.isChecked()) {
                                mPreferenceHelper.setIsRememberMe(true);
                                mPreferenceHelper.setRememberMeUsername(mStringMobileNo);
                                mPreferenceHelper.setRememberMepassword(mStringPassword);
                            } else {
                                mPreferenceHelper.setIsRememberMe(false);
                                mPreferenceHelper.setRememberMeUsername(mStringMobileNo);
                            }
                            // Add current login user to user account
                            ContentValues mContentValuesUser = new ContentValues();
                            mContentValuesUser.put(mDbHelper.mFieldUserServerID, mLoginData.getData().getUser_id());
                            mContentValuesUser.put(mDbHelper.mFieldUserName, mLoginData.getData().getUserName());
                            mContentValuesUser.put(mDbHelper.mFieldUserAccountName, mLoginData.getData().getAccount_name());
                            mContentValuesUser.put(mDbHelper.mFieldUserEmail, mLoginData.getData().getEmail());
                            mContentValuesUser.put(mDbHelper.mFieldUserMobileNo, mLoginData.getData().getMobile_number());
                            mContentValuesUser.put(mDbHelper.mFieldUserPassword, mLoginData.getData().getPassword());
                            mContentValuesUser.put(mDbHelper.mFieldUserToken, mStringDevicesUIDFCMToken);
                            String isExistInUserDB = CheckRecordExistInUserAccountDB(mLoginData.getData().getUser_id());
                            if (isExistInUserDB.equalsIgnoreCase("-1")) {
                                mDbHelper.insertRecord(mDbHelper.mTableUserAccount, mContentValuesUser);
                            } else {
                                mDbHelper.updateRecord(mDbHelper.mTableUserAccount, mContentValuesUser, mDbHelper.mFieldUserLocalID + "=?", new String[]{isExistInUserDB});
                            }
//                        mPreferenceHelper.setFirstTime(true);
                            Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
                            mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            mIntent.putExtra("isFromNotification", false);
                            mIntent.putExtra("isFromLogin", true);
                            startActivity(mIntent);
                            finishAffinity();
                        }
                    }
                } else {
                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
                        showMessageRedAlert(mRelativeLayoutMain, mLoginData.getMessage(), getResources().getString(R.string.str_ok));
                }
            }

            @Override
            public void onFailure(Call<VoLoginData> call, Throwable t) {
                mUtility.HideProgress();
                t.printStackTrace();
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
            }
        });
    }

    /* Check user is exist in user account table or not*/
    private String CheckRecordExistInUserAccountDB(String userId) {
        DataHolder mDataHolder;
        String url = "select * from " + mDbHelper.mTableUserAccount + " where " + mDbHelper.mFieldUserServerID + "= '" + userId + "'";
        mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" UserList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldUserLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
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
//    private void forgotPasswordDialog() {
//        final Dialog myDialog = new Dialog(LoginActivity.this);
//        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        myDialog.setContentView(R.layout.popup_forgot_password);
//        myDialog.setCancelable(false);
//        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparentWhite)));
//        final View mView = (View) myDialog.findViewById(R.id.popup_forgot_password_main_layout);
//        Button mButtonSend = (Button) myDialog
//                .findViewById(R.id.popup_forgot_pw_buttton_send);
//        Button mButtonCancel = (Button) myDialog
//                .findViewById(R.id.popup_forgot_pw_buttton_cancal);
//        final AppCompatEditText mEditTextMobile = (AppCompatEditText) myDialog.findViewById(R.id.popup_forgot_pw_edittext_mobileno);
//
//        mButtonSend.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                // TODO Auto-generated method stub
//                String mStringMobile = mEditTextMobile.getText().toString().trim();
//                mUtility.hideKeyboard(LoginActivity.this);
//                InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                im.hideSoftInputFromWindow(mEditTextMobile.getWindowToken(), 0);
//
//                if (mStringMobile.equalsIgnoreCase("")) {
//                    showMessageRedAlert(mView, getResources().getString(R.string.str_sign_up_enter_mobile_no), getResources().getString(R.string.str_ok));
//                    return;
//                }
//                if (mStringMobile.length() < 10 || mStringMobile.length() > 15) {
//                    showMessageRedAlert(mView, getResources().getString(R.string.str_sign_up_enter_valid_mobile_no), getResources().getString(R.string.str_ok));
//                    return;
//                }
//
//                if (mUtility.haveInternet()) {
//                    myDialog.dismiss();
//                    forgotPassword(mStringMobile);
//                } else {
//                    mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection), 3, true);
//                }
//            }
//        });
//        mButtonCancel.setOnClickListener(new View.OnClickListener() {
//
//            @Override
//            public void onClick(View v) {
//                mUtility.hideKeyboard(LoginActivity.this);
//                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(mEditTextMobile.getWindowToken(), 0);
//                myDialog.dismiss();
//            }
//        });
//        myDialog.show();
//        Window window = myDialog.getWindow();
//        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//    }

//    private void forgotPassword(String mStringMobile) {
//        mUtility.hideKeyboard(LoginActivity.this);
//        mUtility.ShowProgress("Please Wait..");
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("mobile_number", mStringMobile);
//        System.out.println("params-" + params.toString());
//        Call<VoLogout> mLogin = mApiService.userChangePasswordAPI(params);
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoLogout>() {
//            @Override
//            public void onResponse(Call<VoLogout> call, Response<VoLogout> response) {
//                mUtility.HideProgress();
//                VoLogout mLoginData = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mLoginData);
//                System.out.println("response forgotPassword---------" + json);
//                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
//                    mUtility.errorDialog(getResources().getString(R.string.str_password_link), 0, true);
//                } else {
//                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
//                        showMessageRedAlert(mRelativeLayoutMain, mLoginData.getMessage(), getResources().getString(R.string.str_ok));
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoLogout> call, Throwable t) {
//                mUtility.HideProgress();
//                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
//            }
//        });
//    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mUtility.hideKeyboard(LoginActivity.this);
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