package com.succorfish.installer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.hbb20.CountryCodePicker;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.MyApplication;
import com.succorfish.installer.R;
import com.succorfish.installer.helper.PreferenceHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 21-02-2018.
 */

public class FragmentUpdateProfile extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.fragment_update_profile_relativelayout_main)
    RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.fragment_update_profile_edittext_name)
    AppCompatEditText mEditTextName;
    @BindView(R.id.fragment_update_profile_edittext_business_name)
    AppCompatEditText mEditTextBusinessName;
    @BindView(R.id.fragment_update_profile_edittext_email)
    AppCompatEditText mEditTextEmail;
    @BindView(R.id.fragment_update_profile_edittext_mobile)
    AppCompatEditText mEditTextMobileNO;
    @BindView(R.id.fragment_update_profile_edittext_address)
    AppCompatEditText mEditTextAddress;
    @BindView(R.id.fragment_update_profile_button_save)
    Button mButtonSave;
    @BindView(R.id.fragment_update_profile_country_picker_countrycode)
    CountryCodePicker countryCodePicker;

    String mStringName = "";
    String mStringBusinessName = "";
    String mStringEmail = "";
    String mStringMobileNO = "";
    String mStringAddress = "";
    String mStringCountryNameCode = "GB";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_update_profile, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_update_profile));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);

        countryCodePicker.setDefaultCountryUsingNameCode("GB");
        countryCodePicker.setNumberAutoFormattingEnabled(true);
        countryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                mStringCountryNameCode = countryCodePicker.getSelectedCountryNameCode();
                System.out.println(" SelectedCountry-" + countryCodePicker.getSelectedCountryName());
                System.out.println(" SelectedCountryCode-" + countryCodePicker.getSelectedCountryNameCode());
                System.out.println(" SelectedCountryCodePlus-" + countryCodePicker.getSelectedCountryCodeWithPlus());
            }
        });
        displayUserData();
        return mViewRoot;
    }

    @OnClick(R.id.fragment_update_profile_button_save)
    public void onSaveClick(View mView) {
        if (isAdded()) {
            mActivity.mUtility.hideKeyboard(mActivity);
            mStringName = mEditTextName.getText().toString().trim();
            mStringBusinessName = mEditTextBusinessName.getText().toString().trim();
            mStringEmail = mEditTextEmail.getText().toString().trim();
            mStringMobileNO = mEditTextMobileNO.getText().toString().trim();
            mStringAddress = mEditTextAddress.getText().toString().trim();
            if (mStringName.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_name));
                return;
            }
            if (mStringBusinessName.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_business_name));
                return;
            }
            if (mStringAddress.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_address));
                return;
            }
            if (mStringEmail.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_email_address));
                return;
            }
            if (!mActivity.mUtility.isValidEmail(mStringEmail)) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_valid_email_address));
                return;
            }
            if (mStringMobileNO.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_sign_up_enter_mobile_no));
                return;
            }

            if (!mActivity.mUtility.haveInternet()) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
            } else {
//                updateUserProfile();
            }
        }
    }

//    public void getUserProfileData() {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoLoginData> mLogin = mActivity.mApiService.getUserProfile(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoLoginData>() {
//            @Override
//            public void onResponse(Call<VoLoginData> call, Response<VoLoginData> response) {
//                mActivity.mUtility.HideProgress();
//                VoLoginData mLoginData = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mLoginData);
//                System.out.println("response mLoginData---------" + json);
//                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
//                    if (mLoginData.getData() != null) {
//                        mActivity.mPreferenceHelper.setUserName(mLoginData.getData().getName());
//                        mActivity.mPreferenceHelper.setUserEmail(mLoginData.getData().getEmail());
//                        mActivity.mPreferenceHelper.setUserMobileNo(mLoginData.getData().getMobile_no());
//                        mActivity.mPreferenceHelper.setUserPassword(mLoginData.getData().getPassword());
//                        mActivity.mPreferenceHelper.setUserBusinessName(mLoginData.getData().getBusiness_name());
//                        mActivity.mPreferenceHelper.setUserContry(mLoginData.getData().getPhonecode());
//                        mActivity.mPreferenceHelper.setUserAddress(mLoginData.getData().getAddress());
//                        displayUserData();
//                    }
//                } else {
//                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
//                        mActivity.mUtility.errorDialog(mLoginData.getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoLoginData> call, Throwable t) {
//                mActivity.mUtility.HideProgress();
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//            }
//        });
//    }

    private void displayUserData() {
        mEditTextName.setText(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserName());
        mEditTextBusinessName.setText("");
        mEditTextAddress.setText("");
        mEditTextEmail.setText(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUser_email());
        mEditTextMobileNO.setText("");
//        mStringCountryNameCode = mActivity.mPreferenceHelper.getPostalCode();
//        countryCodePicker.setCountryForNameCode(mStringCountryNameCode);
        countryCodePicker.setNumberAutoFormattingEnabled(true);
    }

//    public void updateUserProfile() {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("name", mStringName);
//        params.put("business_name", mStringBusinessName);
//        params.put("address", mStringAddress);
//        params.put("mobile_no", mStringMobileNO);
//        params.put("phonecode", mStringCountryNameCode);
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoLoginData> mLogin = mActivity.mApiService.updateUserProfile(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoLoginData>() {
//            @Override
//            public void onResponse(Call<VoLoginData> call, Response<VoLoginData> response) {
//                mActivity.mUtility.HideProgress();
//                VoLoginData mLoginData = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mLoginData);
//                System.out.println("response mLoginData---------" + json);
//                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
//                    if (mLoginData.getData() != null) {
//                        mActivity.mPreferenceHelper.setUserName(mLoginData.getData().getName());
//                        mActivity.mPreferenceHelper.setUserEmail(mLoginData.getData().getEmail());
//                        mActivity.mPreferenceHelper.setUserMobileNo(mLoginData.getData().getMobile_no());
//                        mActivity.mPreferenceHelper.setUserPassword(mLoginData.getData().getPassword());
//                        mActivity.mPreferenceHelper.setUserBusinessName(mLoginData.getData().getBusiness_name());
//                        mActivity.mPreferenceHelper.setUserContry(mLoginData.getData().getPhonecode());
//                        mActivity.mPreferenceHelper.setUserAddress(mLoginData.getData().getAddress());
//                        displayUserData();
//                        if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
//                            mActivity.mUtility.errorDialog(mLoginData.getMessage());
//                    }
//                } else {
//                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase(""))
//                        mActivity.mUtility.errorDialog(mLoginData.getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoLoginData> call, Throwable t) {
//                mActivity.mUtility.HideProgress();
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//            }
//        });
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mImageViewBack.setVisibility(View.GONE);
    }
}
