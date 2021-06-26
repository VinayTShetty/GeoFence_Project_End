package com.succorfish.geofence;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.EditText;

import com.google.gson.Gson;
import com.kaopiz.kprogresshud.KProgressHUD;
import com.succorfish.geofence.customObjects.LoginData;
import com.succorfish.geofence.dialog.DialogProvider;
import com.succorfish.geofence.helper.PreferenceHelper;
import com.succorfish.geofence.interfaces.API;
import com.succorfish.geofence.interfaces.onAlertDialogCallBack;
import com.succorfish.geofence.utility.URL_helper;
import com.succorfish.geofence.utility.Utility;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.succorfish.geofence.utility.RetrofitHelperClass.getClientWithAutho;
import static com.succorfish.geofence.utility.RetrofitHelperClass.getSimpleClient;
import static com.succorfish.geofence.utility.RetrofitHelperClass.haveInternet;
import static com.succorfish.geofence.utility.URL_helper.SERVER_URL_SUCCORFISH;

public class LoginActivity extends AppCompatActivity {
    private Retrofit mRetrofit_isntance;
    private API mApiService;
    private String mStringUsername = "";
    private String mStringPassword = "";
    private Unbinder unbinder;
    DialogProvider dialogProvider_helper;
    @BindView(R.id.activity_login_edittext_username)
    EditText user_name_EditText;
    @BindView(R.id.activity_login_edittext_password)
    EditText password_EditText;
    @BindView(R.id.activity_login_checkbox_remember_pw)
    CheckBox rememberMeCheckbox;
    PreferenceHelper preferenceHelper;
    Utility utility;
    private KProgressHUD hud;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        unbinder=ButterKnife.bind(LoginActivity.this);
        intializeDialog();
        intializePreferenceHelper();
        intializeUtility();
        intializeView();

    }
    

    private void intializeView() {
        hud = KProgressHUD.create(this);
    }


    private void intializePreferenceHelper() {
        preferenceHelper=PreferenceHelper.getPreferenceInstance(getApplicationContext());
    }

    private void intializeDialog(){
        dialogProvider_helper=new DialogProvider(LoginActivity.this);
    }

    private void intializeUtility(){
        utility=new Utility();
    }
    private void intializeRetrofitInstance(){
        mRetrofit_isntance = new Retrofit.Builder()
                .baseUrl(SERVER_URL_SUCCORFISH)
                .client(getClientWithAutho(mStringUsername,mStringPassword))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
    private void intializeAPI_serviceInstance(){
        mApiService = mRetrofit_isntance.create(API.class);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    @OnClick(R.id.activity_login_buttton_login)
    public void loginClick(){
          mStringUsername =user_name_EditText.getText().toString();
          mStringPassword =password_EditText.getText().toString();
          if(mStringUsername.equalsIgnoreCase("")){
              dialogProvider_helper.errorDialog("Enter User Name");
              return;
          }else if(mStringPassword.equalsIgnoreCase("")){
              dialogProvider_helper.errorDialog("Enter Password");
              return;
          }else if((mStringUsername.length()>0)&&(mStringPassword.length()>0)){
              if(haveInternet(LoginActivity.this)){
                  intializeRetrofitInstance();
                  intializeAPI_serviceInstance();
                loginAPI();
              }else {
                  dialogProvider_helper.errorDialog("No Internet Connection");
              }
          }
    }




    private void loginAPI() {
        utility.hideKeyboard(LoginActivity.this);
        showProgressDialog();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                Call<LoginData> loginData= mApiService.userLoginAPI();
                loginData.enqueue(new Callback<LoginData>() {
                    @Override
                    public void onResponse(Call<LoginData> call, Response<LoginData> response) {
                        if (response.code() == 200 || response.isSuccessful()) {
                            LoginData logindata=response.body();
                            Gson gson = new Gson();
                            String json = gson.toJson(logindata);
                            // System.out.println("response mLoginData---------" + json);
                            if(rememberMeCheckbox.isChecked()){
                                preferenceHelper.set_Remember_me_Checked(true);
                                preferenceHelper.set_PREF_remember_me_userName(mStringUsername);
                                preferenceHelper.set_PREF_remember_me_password(mStringPassword);
                            }else if(!(rememberMeCheckbox.isChecked())){
                                preferenceHelper.set_Remember_me_Checked(false);
                                preferenceHelper.set_userName(mStringUsername);
                                preferenceHelper.set_password(mStringPassword);
                            }
                            cancelProgressDialog();
                            show_DialogFor_Login();

                        }else {
                            LoginData logindata=response.body();
                            Gson gson = new Gson();
                            String json = gson.toJson(logindata);
                            System.out.println("response mLoginData---------" + json);
                            System.out.println("response mLoginData---------" + response.code());
                            cancelProgressDialog();
                            dialogProvider_helper.errorDialog("Invalid Credentials");
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginData> call, Throwable t) {
                        cancelProgressDialog();
                        dialogProvider_helper.errorDialog("Server Issue");
                    }
                });
            }
        });

    }

    private void showProgressDialog() {
        hud
                .setStyle(KProgressHUD.Style.SPIN_INDETERMINATE)
                .setLabel("Please Wait")
                .setCancellable(false);
        hud.show();
    }

    private void cancelProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (hud != null) {
                    if (hud.isShowing()) {
                        hud.dismiss();
                    }
                }
            }
        });
    }

    private void show_DialogFor_Login(){
        dialogProvider_helper.errorDialogWithCallBack("",getResources().getString(R.string.Login_Sucessfull), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                Intent mIntent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(mIntent);
                finish();
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
    }
}