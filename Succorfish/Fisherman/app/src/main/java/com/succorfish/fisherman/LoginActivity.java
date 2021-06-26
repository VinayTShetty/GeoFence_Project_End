package com.succorfish.fisherman;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.succorfish.fisherman.Vo.VoLoginData;
import com.succorfish.fisherman.helper.PreferenceHelper;
import com.succorfish.fisherman.helper.URLCLASS;
import com.succorfish.fisherman.helper.Utility;
import com.succorfish.fisherman.interfaces.API;

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

    @BindView(R.id.activity_login_textview_dont_account)
    TextView mTextViewDontAccount;
    @BindView(R.id.activity_login_rl_main)
    RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.activity_login_textview_forgot_password)
    TextView mTextViewForgotPassword;
    @BindView(R.id.activity_login_edittext_username)
    AppCompatEditText mEditTextUserName;
    @BindView(R.id.activity_login_edittext_password)
    AppCompatEditText mEditTextPassword;
    @BindView(R.id.activity_login_checkbox_remember_pw)
    CheckBox mCheckBoxRememberPw;
    @BindView(R.id.activity_login_buttton_login)
    Button mButtonLogin;
    private String mStringUsername = "";
    private String mStringPassword = "";
    private static final int REQUEST_CALL = 1;
    Intent callIntent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(LoginActivity.this);
        mUtility = new Utility(LoginActivity.this);
        mPreferenceHelper = new PreferenceHelper(LoginActivity.this);
    /*    mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.SUCCORFISH_URL)
                .client(mUtility.getClientWithAutho(mStringUsername,mStringPassword))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);*/

        if (mPreferenceHelper.getIsRememberMe()) {
            mCheckBoxRememberPw.setChecked(true);
            mEditTextUserName.setText(mPreferenceHelper.getRememberMeUsername());
            mEditTextPassword.setText(mPreferenceHelper.getRememberMePassword());
        } else {
            mEditTextUserName.setText(mPreferenceHelper.getRememberMeUsername());
            mCheckBoxRememberPw.setChecked(false);
        }
    }

    @OnClick(R.id.activity_login_textview_forgot_password)
    public void onForgotPasswordClick(View mView) {
        mUtility.hideKeyboard(LoginActivity.this);
        forgotPasswordDialog();
    }

    @OnClick(R.id.activity_login_buttton_login)
    public void onLoginClick(View mView) {
        if (!isFinishing()) {
            mUtility.hideKeyboard(LoginActivity.this);
            mStringUsername = mEditTextUserName.getText().toString().trim();
            mStringPassword = mEditTextPassword.getText().toString().trim();

            if (mStringUsername.equalsIgnoreCase("")) {
                mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enname));
                return;
            }
            if (mStringPassword.equalsIgnoreCase("")) {
                mUtility.errorDialog(getResources().getString(R.string.str_login_enter_password));
                return;
            }
            mPreferenceHelper.setUName(mStringUsername);
            mPreferenceHelper.setUPassword(mStringPassword);

           // mApiService = mRetrofit.create(API.class);

            if (!mUtility.haveInternet()) {
                mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
            } else {
                intializeRetrofitInstance(mStringUsername,mStringPassword);
                intializeAPIinstance();
                userLogin();
            }
        }
    }

    public void forgotPasswordDialog() {
        final Dialog myDialog = new Dialog(LoginActivity.this);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_forgot_password);
        myDialog.setCancelable(false);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparent)));

        Button mButtonCall = (Button) myDialog
                .findViewById(R.id.popup_forgot_pw_button_call);
        Button mButtonEmail = (Button) myDialog
                .findViewById(R.id.popup_forgot_pw_button_email);
        final TextView mTextViewCallNo = (TextView) myDialog
                .findViewById(R.id.popup_forgot_pw_textview_call_no);
        final TextView mTextViewEmail = (TextView) myDialog
                .findViewById(R.id.popup_forgot_pw_textview_email);
        ImageView mImageView = (ImageView) myDialog
                .findViewById(R.id.popup_forgot_password_imageview_close);
//        final AppCompatEditText mEditTextEmail = (AppCompatEditText) myDialog.findViewById(R.id.popup_forgot_pw_edittext_email);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        mButtonCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                myDialog.dismiss();
                String mStringUserPhone = mTextViewCallNo.getText().toString().trim();
                System.out.println("mStringUserPhone-" + mStringUserPhone);
                if (mStringUserPhone != null && !mStringUserPhone.equals("")) {
                    mStringUserPhone = mStringUserPhone.replace("-", "");
                    mStringUserPhone = mStringUserPhone.replace(" ", "");
                    callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:" + mStringUserPhone));
                    if (ContextCompat.checkSelfPermission(LoginActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{Manifest.permission.CALL_PHONE}, REQUEST_CALL);
                    } else {
                        startActivity(callIntent);
                    }
                }
            }
        });
        mButtonEmail.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                myDialog.dismiss();
                try {
                    Intent sendIntent = new Intent(Intent.ACTION_SEND);
                    sendIntent.setData(Uri.parse("mailto:"));
                    sendIntent.setType("text/plain");
                    sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{mTextViewEmail.getText().toString().trim()});
                    if (sendIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(sendIntent);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
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

    public void userLogin() {
        mUtility.hideKeyboard(LoginActivity.this);
        mUtility.ShowProgress();
        mButtonLogin.setText("Logging");
        Call<VoLoginData> mLogin = mApiService.userLoginAPI();
        System.out.println("URL-" + mLogin.request().url().toString());
        mLogin.enqueue(new Callback<VoLoginData>() {
            @Override
            public void onResponse(Call<VoLoginData> call, Response<VoLoginData> response) {
                mUtility.HideProgress();
                System.out.println("Response Code " + response.code());
                if (response.code() == 200 || response.isSuccessful()) {
                    VoLoginData mLoginData = response.body();
                    Gson gson = new Gson();
                    String json = gson.toJson(mLoginData);
                    System.out.println("response mLoginData---------" + json);

                    mPreferenceHelper.setUserId(mLoginData.getId());
                    mPreferenceHelper.setAccountId(mLoginData.getAccountId());
                    mPreferenceHelper.setUserName(mLoginData.getUsername());
                    mPreferenceHelper.setUserFirstName(mLoginData.getFirstName());
                    mPreferenceHelper.setUserLastName(mLoginData.getLastName());
                    mPreferenceHelper.setUserEmail(mLoginData.getEmail());
                    mPreferenceHelper.setRoleId(mLoginData.getRoleId());
                    mPreferenceHelper.setRoleName(mLoginData.getRoleName());

                    if (mCheckBoxRememberPw.isChecked()) {
                        mPreferenceHelper.setIsRememberMe(true);
                        mPreferenceHelper.setRememberMeUsername(mStringUsername);
                        mPreferenceHelper.setRememberMepassword(mStringPassword);
                    } else {
                        mPreferenceHelper.setIsRememberMe(false);
                        mPreferenceHelper.setRememberMeUsername(mStringUsername);
                    }

                    Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(mIntent);
                    finish();
                } else {
                    mUtility.errorDialog("Invalid login credentials");
                }
            }

            @Override
            public void onFailure(Call<VoLoginData> call, Throwable t) {
                mUtility.HideProgress();
                mButtonLogin.setText("Login");
                mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));

            }
        });
    }

    private void intializeRetrofitInstance(String mStringUsername,String mStringPassword){
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.SUCCORFISH_URL)
                .client(mUtility.getClientWithAutho(mStringUsername,mStringPassword))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private void intializeAPIinstance(){
        mApiService = mRetrofit.create(API.class);
    }

//    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
//        mUtility.hideKeyboard(LoginActivity.this);
//        Snackbar.make(mView, mStringMessage, 10000)
//                .setAction(mActionMessage, new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                    }
//                })
//                .setActionTextColor(getResources().getColor(android.R.color.holo_red_light))
//                .show();
//    }
}
