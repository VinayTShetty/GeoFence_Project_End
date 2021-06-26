package com.succorfish.installer.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.google.gson.Gson;
import com.succorfish.installer.BuildConfig;
import com.succorfish.installer.MainActivity;
import com.succorfish.installer.R;
import com.succorfish.installer.Vo.VoChangePassword;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 21-02-2018.
 */

public class FragmentChangePassword extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;

    @BindView(R.id.fragment_change_password_edittext_current_pw)
    AppCompatEditText mEditTextCurrentPassword;
    @BindView(R.id.fragment_change_password_edittext_new_password)
    AppCompatEditText mEditTextNewPassword;
    @BindView(R.id.fragment_change_password_edittext_confirm_password)
    AppCompatEditText mEditTextConfirmPassword;

    private String mStringCurrentPassword = "";
    private String mStringNewPassword = "";
    private String mStringConfirmPassword = "";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_change_password, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.str_change_password));
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mRelativeLayoutBottomMenu.setVisibility(View.GONE);
        return mViewRoot;
    }

    @OnClick(R.id.fragment_change_password_button_save)
    public void onSaveClick(View mView) {
        if (isAdded()) {
            mActivity.mUtility.hideKeyboard(mActivity);

            mStringCurrentPassword = mEditTextCurrentPassword.getText().toString().trim();
            mStringNewPassword = mEditTextNewPassword.getText().toString().trim();
            mStringConfirmPassword = mEditTextConfirmPassword.getText().toString().trim();
            if (mStringCurrentPassword.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_change_pw_enter_current_pw));
                return;
            }
            if (mStringNewPassword.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_change_pw_enter_new_pw));
                return;
            }
            if (mStringConfirmPassword.equalsIgnoreCase("")) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_change_pw_enter_conf_pw));
                return;
            }
            if (!mStringNewPassword.equalsIgnoreCase(mStringConfirmPassword)) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_change_pw_pw_not_match));
                return;
            }
            if (!mActivity.mUtility.haveInternet()) {
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection));
            } else {
//                setUserPassword();
            }
        }
    }

//    public void setUserPassword() {
//        mActivity.mUtility.hideKeyboard(mActivity);
//        mActivity.mUtility.ShowProgress();
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
//        params.put("current_pass", mStringCurrentPassword);
//        params.put("new_pass", mStringNewPassword);
//        System.out.println("params-" + params.toString());
//        System.out.println("paramsHeader-" + mActivity.mPreferenceHelper.getAccessToken());
//        Call<VoChangePassword> mLogin = mActivity.mApiService.changeUserPassword(params, mActivity.mPreferenceHelper.getAccessToken());
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<VoChangePassword>() {
//            @Override
//            public void onResponse(Call<VoChangePassword> call, Response<VoChangePassword> response) {
//                mActivity.mUtility.HideProgress();
//                VoChangePassword mVoChangePassword = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mVoChangePassword);
//                System.out.println("response setUserPassword---------" + json);
//                if (mVoChangePassword != null && mVoChangePassword.getResponse().equalsIgnoreCase("true")) {
//                    if (mVoChangePassword != null && mVoChangePassword.getMessage() != null && !mVoChangePassword.getMessage().equalsIgnoreCase(""))
//                        mActivity.mUtility.errorDialog(mVoChangePassword.getMessage());
//                } else {
//                    if (mVoChangePassword != null && mVoChangePassword.getMessage() != null && !mVoChangePassword.getMessage().equalsIgnoreCase(""))
//                        mActivity.mUtility.errorDialog(mVoChangePassword.getMessage());
//                }
//            }
//
//            @Override
//            public void onFailure(Call<VoChangePassword> call, Throwable t) {
//                mActivity.mUtility.HideProgress();
//                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again));
//
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
