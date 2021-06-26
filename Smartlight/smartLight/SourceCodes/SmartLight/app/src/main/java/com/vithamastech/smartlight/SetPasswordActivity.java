package com.vithamastech.smartlight;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.RelativeLayout;

import com.vithamastech.smartlight.Vo.VoLoginData;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.helper.PreferenceHelper;
import com.vithamastech.smartlight.helper.URLCLASS;
import com.vithamastech.smartlight.helper.Utility;
import com.vithamastech.smartlight.interfaces.API;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;

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
 * Created by Jaydeep on 20-02-2018.
 */

public class SetPasswordActivity extends AppCompatActivity {

    private Retrofit mRetrofit;
    private API mApiService;
    private PreferenceHelper mPreferenceHelper;
    Utility mUtility;
    DBHelper mDbHelper;

    @BindView(R.id.activity_set_password_edittext_password)
    AppCompatEditText mEditTextPassword;
    @BindView(R.id.activity_set_password_edittext_confirm_password)
    AppCompatEditText mEditTextConfirmPassword;
    @BindView(R.id.activity_set_password_rl_main)
    RelativeLayout mRelativeLayoutMain;

    private String mStringConfirmPw = "";
    private String mStringPassword = "";
    String mStringUserMobileNo = "";
    VoLoginData mLoginData;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_set_password);
        ButterKnife.bind(SetPasswordActivity.this);
        mDbHelper = DBHelper.getDBHelperInstance(SetPasswordActivity.this);
        mUtility = new Utility(SetPasswordActivity.this);
        mPreferenceHelper = new PreferenceHelper(SetPasswordActivity.this);
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.MAIN_URL)
                .client(mUtility.getSimpleClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);
        mEditTextPassword.setTransformationMethod(new PasswordTransformationMethod());
        mEditTextConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
        if (getIntent() != null) {
            mStringUserMobileNo = getIntent().getStringExtra("intent_mobile_no");
            mLoginData = (VoLoginData) getIntent().getSerializableExtra("intent_users_data");
        }
    }

    @OnClick(R.id.activity_set_pw_imageview_back)
    public void onBackClick(View mView) {
        onBackPressed();
    }

    /*Change user password*/
    @OnClick(R.id.activity_set_password_button_save)
    public void onSaveClick(View mView) {
        if (!isFinishing()) {
            mUtility.hideKeyboard(SetPasswordActivity.this);

            mStringPassword = mEditTextPassword.getText().toString().trim();
            mStringConfirmPw = mEditTextConfirmPassword.getText().toString().trim();

            if (mStringPassword.equalsIgnoreCase("")) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_forgot_enter_new_password), getResources().getString(R.string.str_ok));
                return;
            }
            if (mStringConfirmPw.equalsIgnoreCase("")) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_forgot_enter_confirm_password), getResources().getString(R.string.str_ok));
                return;
            }
            if (mStringPassword.length() < 6 || mStringPassword.length() > 15) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_sign_up_enter_valid_password), getResources().getString(R.string.str_ok));
                return;
            }
            if (!mStringPassword.equalsIgnoreCase(mStringConfirmPw)) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_settings_password_not_match), getResources().getString(R.string.str_ok));
                return;
            }
            if (!mUtility.haveInternet()) {
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_no_internet_connection), getResources().getString(R.string.str_ok));
            } else {
                setUserPassword();
            }
        }
    }

    @Override
    public void onBackPressed() {
        mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.str_exit), getResources().getString(R.string.str_password_exit_confirm), "Yes", "No", true, 3, new onAlertDialogCallBack() {
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

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mUtility.hideKeyboard(SetPasswordActivity.this);
        Snackbar.make(mView, mStringMessage, 10000)
                .setAction(mActionMessage, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                    }
                })
                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
                .show();
    }

    /* Change user password API*/
    private void setUserPassword() {
        mUtility.hideKeyboard(SetPasswordActivity.this);
        mUtility.ShowProgress("Please Wait..");
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mLoginData.getData().getUser_id());
        params.put("new_password", mStringPassword);
        params.put("old_password", "");
        params.put("isChangePass", "0");
        Call<VoLogout> mLogin = mApiService.userChangePasswordAPI(params);
        mLogin.enqueue(new Callback<VoLogout>() {
            @Override
            public void onResponse(Call<VoLogout> call, Response<VoLogout> response) {
                mUtility.HideProgress();
                VoLogout mLoginData = response.body();
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    mUtility.errorDialogWithCallBack(mLoginData.getMessage(), 0, false, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            Intent mIntent = new Intent(SetPasswordActivity.this, LoginActivity.class);
                            mIntent.putExtra("is_from_add_account", false);
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(mIntent);
                            finish();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                } else {
                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
                        showMessageRedAlert(mRelativeLayoutMain, mLoginData.getMessage(), getResources().getString(R.string.str_ok));
                }
            }

            @Override
            public void onFailure(Call<VoLogout> call, Throwable t) {
                mUtility.HideProgress();
                showMessageRedAlert(mRelativeLayoutMain, getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUtility.hideKeyboard(SetPasswordActivity.this);
    }
}
