package com.vithamastech.smartlight;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.vithamastech.smartlight.Views.PinEntryEditText;
import com.vithamastech.smartlight.Vo.VoLoginData;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.PreferenceHelper;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.helper.Utility;
import com.vithamastech.smartlight.interfaces.API;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Jaydeep on 19-02-2018.
 */

public class VerifyRegisterAccount extends AppCompatActivity {

    private Retrofit mRetrofit;
    private API mApiService;
    private PreferenceHelper mPreferenceHelper;
    Utility mUtility;
    DBHelper mDbHelper;

    @BindView(R.id.activity_verify_account_txt_pin_entry)
    PinEntryEditText mPinEntryEditText;
    @BindView(R.id.activity_verify_account_linearlayout_resend_otp)
    LinearLayout mLinearLayoutResendOtp;
    @BindView(R.id.activity_verify_register_textview_mobileno)
    TextView mTextViewMobileNo;
    @BindView(R.id.activity_verify_account_button_verify)
    Button mButtonVerify;
    @BindView(R.id.activity_verify_account_rl_main)
    RelativeLayout mRelativeLayoutMain;

    String mStringUsername = "";
    String mStringAccountName = "";
    String mStringEmail = "";
    String mStringMobileNO = "";
    String mStringPassword = "";
    String mStringDeviceType = "1";
    String mStringDevicesUIDFCMToken = "test";
    String mStringCountryCode = "+91";
    boolean isFromSignUp = false;
    private FirebaseAuth mAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String verificationId = "";
    private PhoneAuthProvider.ForceResendingToken mResendToken;
    //    private SmsVerifyCatcher smsVerifyCatcher;
    VoLoginData mLoginData;

    private static final int OTP_TIMEOUT_IN_SEC = 2 * 60;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_verify_register_account);
        ButterKnife.bind(VerifyRegisterAccount.this);
        mDbHelper = DBHelper.getDBHelperInstance(VerifyRegisterAccount.this);
        mUtility = new Utility(VerifyRegisterAccount.this);
        mPreferenceHelper = new PreferenceHelper(VerifyRegisterAccount.this);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.MAIN_URL)
                .client(mUtility.getSimpleClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);
        mAuth = FirebaseAuth.getInstance();
        if (getIntent() != null) {
            isFromSignUp = getIntent().getBooleanExtra("intent_is_from_signup", false);
            mStringMobileNO = getIntent().getStringExtra("intent_mobileno");
            mStringCountryCode = getIntent().getStringExtra("intent_country_code");
            if (isFromSignUp) {
                mStringUsername = getIntent().getStringExtra("intent_username");
                mStringAccountName = getIntent().getStringExtra("intent_account_name");
                mStringEmail = getIntent().getStringExtra("intent_email");
                mStringPassword = getIntent().getStringExtra("intent_password");
                mStringDevicesUIDFCMToken = getIntent().getStringExtra("intent_fcm_token");
            } else {
                mLoginData = (VoLoginData) getIntent().getSerializableExtra("intent_user_data");
            }
        }

        if (BuildConfig.DEBUG) {
            System.out.println("mStringCountryCode-" + mStringCountryCode);
            System.out.println("mStringUsername-" + mStringUsername);
            System.out.println("mStringEmail-" + mStringEmail);
            System.out.println("mStringMobileNO-" + mStringMobileNO);
            System.out.println("mStringPassword-" + mStringPassword);
            System.out.println("mStringDevicesUIDFCMToken-" + mStringDevicesUIDFCMToken);
        }

        mTextViewMobileNo.setText(mStringCountryCode + " " + mStringMobileNO);

        // Initialize phone auth callbacks
        // [START phone_auth_callbacks]
        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            /*
             This callback will be invoked in two situations:
             1 - Instant verification. In some cases the phone number can be instantly
                 verified without needing to send or enter a verification code.
             2 - Auto-retrieval. On some devices Google Play services can automatically
                 detect the incoming verification SMS and perform verification without
                 user action.
             */
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                console.log("VerifyRegisterAccount_FirebaseAuth", "OnVerificationCompleted");
                console.log("VerifyRegisterAccount_FirebaseAuth", phoneAuthCredential.getSmsCode());
                mPinEntryEditText.setText(phoneAuthCredential.getSmsCode());
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            /*
               This callback is invoked in an invalid request for verification is made,
               for instance if the the phone number format is not valid.
             */
            @Override
            public void onVerificationFailed(FirebaseException e) {
                console.log("VerifyRegisterAccount_FirebaseAuth", "OnVerificationFailed = " + e.getMessage());
                if (e instanceof FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                    console.log("VerifyRegisterAccount_FirebaseAuth", "Invalid credential: " + e.getLocalizedMessage());
                } else if (e instanceof FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                    console.log("VerifyRegisterAccount_FirebaseAuth", "SMS Quota Exceeded");
                }

                //sendPhoneNumberVerificationCode(mStringCountryCode + mStringMobileNO);
            }

            /*
               The SMS verification code has been sent to the provided phone number, we
               now need to ask the user to enter the code and then construct a credential
               by combining the code with a verification ID.
            */
            @Override
            public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(s, forceResendingToken);
                console.log("VerifyRegisterAccount_FirebaseAuth", "SMS OTP Code sent from server");
                verificationId = s;
                // Save verification ID and resending token so we can use them later
                mResendToken = forceResendingToken;
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
                console.log("VerifyRegisterAccount_FirebaseAuth", "OTP Timeout");
            }
        };
        // [END phone_auth_callbacks]


        // sent otp to mobile no
        sendPhoneNumberVerificationCode(mStringCountryCode + mStringMobileNO);
        if (mPinEntryEditText != null) {
            mPinEntryEditText.setOnPinEnteredListener(new PinEntryEditText.OnPinEnteredListener() {
                @Override
                public void onPinEntered(CharSequence str) {
                    if (!str.toString().equalsIgnoreCase("") && str.toString().length() == 6) {
                        mUtility.hideKeyboard(VerifyRegisterAccount.this);
                    }
                }
            });
        }
//        smsVerifyCatcher = new SmsVerifyCatcher(this, new OnSmsCatchListener<String>() {
//            @Override
//            public void onSmsCatch(String message) {
//                String code = parseCode(message);//Parse verification code
//                mPinEntryEditText.setText(code);
//                //then you can send verification code to server
//            }
//        });
//        smsVerifyCatcher.onStart();
    }

    @OnClick(R.id.activity_verify_account_imageview_back)
    public void onBackClick(View mView) {
        onBackPressed();
    }

    @Override
    public void onBackPressed() {
        mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.str_exit), getResources().getString(R.string.str_verify_account_exit_confirm), "Yes", "No", true, 3, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                finish();
                overridePendingTransition(R.anim.enter,
                        R.anim.exit);
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private String parseCode(String message) {
        Pattern p = Pattern.compile("\\b\\d{6}\\b");
        Matcher m = p.matcher(message);
        String code = "";
        while (m.find()) {
            code = m.group(0);
        }
        return code;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        smsVerifyCatcher.onStop();
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        smsVerifyCatcher.onRequestPermissionsResult(requestCode, permissions, grantResults);
//    }

    /* Sign in with PhoneAuthCredential*/
    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        mUtility.HideProgress();
                        if (task.isSuccessful()) {
                            console.log("VerifyRegisterAccount_FirebaseAuth", "Authentication Successful");
                            // Sign in success, update UI with the signed-in user's information
                            if (!mUtility.haveInternet()) {
                                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
                            } else {
                                if (isFromSignUp) {
                                    userRegister();
                                } else {
                                    Intent mIntent = new Intent(VerifyRegisterAccount.this, SetPasswordActivity.class);
                                    mIntent.putExtra("intent_mobile_no", mStringMobileNO);
                                    mIntent.putExtra("intent_country_code", mStringCountryCode);
                                    mIntent.putExtra("isFromSignUp", false);
                                    mIntent.putExtra("intent_users_data", mLoginData);
                                    startActivity(mIntent);
                                    finish();
                                }
                            }
                        } else {
                            console.log("VerifyRegisterAccount_FirebaseAuth", "Authentication Failed");
                            // Sign in failed, display a message and update the UI
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_verify_account_invalid_otp_code), getResources().getString(R.string.str_ok));
                            } else {
                                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_server_error_someting_wrong), getResources().getString(R.string.str_ok));

                            }
                        }
                    }
                });
    }

    /*Send Otp to mobile no*/
    private void sendPhoneNumberVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                            // Phone number to verify
                OTP_TIMEOUT_IN_SEC,                     // Timeout duration
                TimeUnit.SECONDS,                       // Unit of timeout
                this,                           // Activity (for callback binding)
                mCallbacks);                            // OnVerificationStateChangedCallbacks
    }

    /*Resend OTP to mobile no*/
    private void resendPhoneNumberVerificationCode(String phoneNumber,
                                                   PhoneAuthProvider.ForceResendingToken token) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,                            // Phone number to verify
                OTP_TIMEOUT_IN_SEC,                     // Timeout duration
                TimeUnit.SECONDS,                       // Unit of timeout
                this,                           // Activity (for callback binding)
                mCallbacks,                             // OnVerificationStateChangedCallbacks
                token);                                 // ForceResendingToken from callbacks
    }

    /*Confirmation alert resend otp?*/
    @OnClick(R.id.activity_verify_account_linearlayout_resend_otp)
    public void onResendOtpClick(final View mView) {
        if (!isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(VerifyRegisterAccount.this, R.style.AppCompatAlertDialogStyle);
            builder.setTitle(getResources().getString(R.string.str_verify_account_resend_otp));
            builder.setCancelable(true);
            builder.setMessage(getResources().getString(R.string.str_verify_account_confirm_resend_otp));
            builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (!mUtility.haveInternet()) {
                        showMessageRedAlert(mView, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
                    } else {
                        if (verificationId != null && !verificationId.equals("") && !verificationId.equals("null")) {
                            resendPhoneNumberVerificationCode(mStringCountryCode + mStringMobileNO, mResendToken);
                        } else {
                            showMessageRedAlert(mView, getResources().getString(R.string.str_verify_account_authentication_code_fail), getResources().getString(R.string.str_ok));
                        }
                    }
                }
            });
            builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /* Handle manual verify */
    @OnClick(R.id.activity_verify_account_button_verify)
    public void onVerifyClick(View mView) {
        if (!isFinishing()) {
            if (!mPinEntryEditText.getText().toString().equalsIgnoreCase("") && mPinEntryEditText.getText().toString().length() == 6) {
                if (!mUtility.haveInternet()) {
                    showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
                } else {
                    if (verificationId != null && !verificationId.equals("") && !verificationId.equals("null")) {
//                    userVerifyAccount(mPinEntryEditText.getText().toString().trim());
                        mUtility.ShowProgress("Verifying..");
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, mPinEntryEditText.getText().toString().trim());
                        signInWithPhoneAuthCredential(credential);
                    } else {
                        showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_verify_account_otp_code_fail), getResources().getString(R.string.str_ok));
                    }
                }
            } else {
                mUtility.hideKeyboard(VerifyRegisterAccount.this);
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_verify_account_enter_pin), getResources().getString(R.string.str_ok));
            }
        }
    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mUtility.hideKeyboard(VerifyRegisterAccount.this);
        Snackbar.make(mView, mStringMessage, 10000)
                .setAction(mActionMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    /*Call User Register api*/
    private void userRegister() {
        mUtility.hideKeyboard(VerifyRegisterAccount.this);
        mUtility.ShowProgress("Please Wait..");
        Map<String, String> params = new HashMap<String, String>();
        params.put("username", mStringUsername);
        params.put("account_name", mStringAccountName);
        params.put("mobile_number", mStringMobileNO);
        params.put("email", mStringEmail);
        params.put("password", mStringPassword);
        params.put("device_token", mStringDevicesUIDFCMToken);
        params.put("device_type", mStringDeviceType);

        Call<VoLoginData> mLogin = mApiService.userRegisterAPI(params);
        mLogin.enqueue(new Callback<VoLoginData>() {
            @Override
            public void onResponse(Call<VoLoginData> call, Response<VoLoginData> response) {
                mUtility.HideProgress();
                VoLoginData mLoginData = response.body();
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    if (mLoginData.getData() != null) {
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
                        Intent mIntent = new Intent(VerifyRegisterAccount.this, MainActivity.class);
                        mIntent.putExtra("isFromLogin", true);
                        mIntent.putExtra("isFromNotification", false);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mIntent);
                        finishAffinity();
                    }
                } else {
                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
                        showMessageRedAlert(mRelativeLayoutMain, mLoginData.getMessage(), getResources().getString(R.string.str_ok));
                }
            }

            @Override
            public void onFailure(Call<VoLoginData> call, Throwable t) {
                mUtility.HideProgress();
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
}