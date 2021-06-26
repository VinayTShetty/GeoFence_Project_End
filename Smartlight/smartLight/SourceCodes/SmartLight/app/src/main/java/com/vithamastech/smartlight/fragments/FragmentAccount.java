package com.vithamastech.smartlight.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.AppCompatEditText;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vithamastech.smartlight.LoginActivity;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;

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
 * Created by Jaydeep on 23-12-2017.
 */

public class FragmentAccount extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;

    @BindView(R.id.fragment_account_ll_change_password)
    LinearLayout mLinearLayoutChangePassword;
    @BindView(R.id.fragment_account_ll_manage_account)
    LinearLayout mLinearLayoutManageAccount;
    @BindView(R.id.fragment_account_ll_switch_account)
    LinearLayout mLinearLayoutSwitchAccount;
    @BindView(R.id.fragment_account_tv_logout)
    TextView mTextViewLogout;
    @BindView(R.id.fragment_account_tv_username)
    TextView mTextViewUsername;
    @BindView(R.id.imageViewLogOutArrow)
    ImageView imageViewLogoutArrow;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_account, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_settings_manage_account);
        mActivity.mImageViewBack.setVisibility(View.GONE);
//        mActivity.mImageViewConnectionStatus.setVisibility(View.VISIBLE);

        mActivity.showBackButton(false);
        if (mActivity.mPreferenceHelper.getIsSkipUser()) {
            mLinearLayoutChangePassword.setVisibility(View.GONE);
            mLinearLayoutManageAccount.setVisibility(View.GONE);
            mLinearLayoutSwitchAccount.setVisibility(View.GONE);
            mTextViewLogout.setText(getResources().getString(R.string.frg_account_sign_in));
            mTextViewUsername.setText(getResources().getString(R.string.frg_account_welcome_guest, "Guest"));
            imageViewLogoutArrow.setVisibility(View.VISIBLE);
        } else {
            mLinearLayoutManageAccount.setVisibility(View.VISIBLE);
            mLinearLayoutChangePassword.setVisibility(View.VISIBLE);
            mLinearLayoutSwitchAccount.setVisibility(View.GONE);
            imageViewLogoutArrow.setVisibility(View.GONE);
            mTextViewUsername.setText(getResources().getString(R.string.frg_account_welcome_guest, mActivity.mPreferenceHelper.getUserFirstName()));
            String userCount = mActivity.mDbHelper.getQueryResult("select count(*) from " + mActivity.mDbHelper.mTableUserAccount + " where " + mActivity.mDbHelper.mFieldUserServerID + " != '" + mActivity.mPreferenceHelper.getUserId() + "'");
            int mUserCount = Integer.parseInt(userCount);
            if (mUserCount > 0) {
                mLinearLayoutSwitchAccount.setVisibility(View.VISIBLE);
            }
        }
        return mViewRoot;
    }


    @OnClick(R.id.fragment_account_ll_change_password)
    public void onRetrievesPasswordClick(View mView) {
        if (!mActivity.mPreferenceHelper.getIsSkipUser()) {
            changePasswordDialog();
        }
    }

    @OnClick(R.id.fragment_account_ll_manage_account)
    public void onManageAccountClick(View mView) {
        if (isAdded()) {
            FragmentManageAccount mFragmentManageAccount = new FragmentManageAccount();
            mActivity.replacesFragment(mFragmentManageAccount, true, null, 0);
        }
    }

    @OnClick(R.id.fragment_account_ll_switch_account)
    public void onSwitchAccountClick(View mView) {
        if (isAdded()) {
            FragmentSwitchAccount mFragmentSwitchAccount = new FragmentSwitchAccount();
            mActivity.replacesFragment(mFragmentSwitchAccount, true, null, 0);
        }
    }

    @OnClick(R.id.fragment_account_ll_logout)
    public void onLogoutClick(View mView) {
        if (mActivity.mPreferenceHelper.getIsSkipUser()) {
            openLoginScreen(true);
        } else {
            if (isAdded()) {
                try {
                    if (Integer.parseInt(mActivity.getUserActiveCount()) > 0) {
                        mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.str_logout), getResources().getString(R.string.str_logout_confirmation), getResources().getString(R.string.str_yes), getResources().getString(R.string.str_no), true, 3, new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                                openLoginScreenWithLogout();
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                    } else {
                        openLoginScreenWithLogout();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    openLoginScreenWithLogout();
                }
            }
        }
    }

    private void openLoginScreenWithLogout() {
        mActivity.mPreferenceHelper.ResetPrefData();
        mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_logout_success), 0, false, new onAlertDialogCallBack() {
            @Override
            public void PositiveMethod(DialogInterface dialog, int id) {
                openLoginScreen(false);
            }

            @Override
            public void NegativeMethod(DialogInterface dialog, int id) {

            }
        });
    }

    private void openLoginScreen(boolean isSkipUser) {
        mActivity.hideProgress();
        Intent mIntent = new Intent(mActivity, LoginActivity.class);
        mIntent.putExtra("is_from_add_account", false);
        startActivity(mIntent);
        if (!isSkipUser) {
            mActivity.finish();
        }
    }

    /*Call Change password Alert dialog*/
    public void changePasswordDialog() {
        final Dialog myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_change_password);
        myDialog.setCancelable(true);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorSemiTransparentWhite)));
        final View mView = (View) myDialog.findViewById(R.id.popup_change_password_main_layout);
        Button mButtonSend = (Button) myDialog
                .findViewById(R.id.popup_change_password_button_send);
        Button mButtonCancel = (Button) myDialog
                .findViewById(R.id.popup_change_password_button_cancel);
        final AppCompatEditText mEditTextOldPw = (AppCompatEditText) myDialog.findViewById(R.id.popup_change_password_edittext_old_password);
        final AppCompatEditText mEditTextNewPw = (AppCompatEditText) myDialog.findViewById(R.id.popup_change_password_edittext_new_password);
        final AppCompatEditText mEditTextConfirmPw = (AppCompatEditText) myDialog.findViewById(R.id.popup_change_password_edittext_confirm_password);
        mEditTextOldPw.setTransformationMethod(new PasswordTransformationMethod());
        mEditTextNewPw.setTransformationMethod(new PasswordTransformationMethod());
        mEditTextConfirmPw.setTransformationMethod(new PasswordTransformationMethod());
        mEditTextConfirmPw.setOnEditorActionListener(new AppCompatEditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager oldPW = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    oldPW.hideSoftInputFromWindow(mEditTextConfirmPw.getWindowToken(), 0);
                    mActivity.mUtility.hideKeyboard(mActivity);
                    return true;
                }
                return false;
            }
        });
        mButtonSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                String mStringOld = mEditTextOldPw.getText().toString().trim();
                String mStringNew = mEditTextNewPw.getText().toString().trim();
                String mStringConfirmPw = mEditTextConfirmPw.getText().toString().trim();

                mActivity.mUtility.hideKeyboard(mActivity);
                InputMethodManager oldPW = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                oldPW.hideSoftInputFromWindow(mEditTextOldPw.getWindowToken(), 0);
                InputMethodManager newPW = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                newPW.hideSoftInputFromWindow(mEditTextNewPw.getWindowToken(), 0);
                InputMethodManager confPW = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                confPW.hideSoftInputFromWindow(mEditTextConfirmPw.getWindowToken(), 0);
                if (mStringOld.equalsIgnoreCase("")) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_settings_enter_old_password), getResources().getString(R.string.str_ok));
                    return;
                }
                if (mStringNew.equalsIgnoreCase("")) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_settings_enter_new_password), getResources().getString(R.string.str_ok));
                    return;
                }
                if (mStringConfirmPw.equalsIgnoreCase("")) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_settings_enter_confirm_password), getResources().getString(R.string.str_ok));
                    return;
                }
                if (mStringNew.length() < 6 || mStringNew.length() > 15) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_sign_up_enter_valid_password), getResources().getString(R.string.str_ok));
                    return;
                }
                if (!mStringNew.equalsIgnoreCase(mStringConfirmPw)) {
                    showMessageRedAlert(mView, getResources().getString(R.string.str_settings_password_not_match), getResources().getString(R.string.str_ok));
                    return;
                }
                if (mActivity.mUtility.haveInternet()) {
                    myDialog.dismiss();
                    changeUserPassword(mStringNew, mStringOld);
                } else {
                    mActivity.mUtility.errorDialog(getResources().getString(R.string.str_no_internet_connection), 3, true);
                }
            }
        });
        mButtonCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mActivity.mUtility.hideKeyboard(mActivity);
                InputMethodManager oldPW = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                oldPW.hideSoftInputFromWindow(mEditTextOldPw.getWindowToken(), 0);
                InputMethodManager newPW = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                newPW.hideSoftInputFromWindow(mEditTextNewPw.getWindowToken(), 0);
                InputMethodManager confPW = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                confPW.hideSoftInputFromWindow(mEditTextConfirmPw.getWindowToken(), 0);
                myDialog.dismiss();
            }
        });
        myDialog.show();
        Window window = myDialog.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
    }

    /*Call Change password api*/
    public void changeUserPassword(String newPassword, String oldPassword) {
        mActivity.mUtility.hideKeyboard(mActivity);
        mActivity.mUtility.ShowProgress("Please Wait..");
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mActivity.mPreferenceHelper.getUserId());
        params.put("new_password", newPassword);
        params.put("old_password", oldPassword);
        params.put("isChangePass", "1");
        Call<VoLogout> mLogin = mActivity.mApiService.userChangePasswordAPI(params);
        mLogin.enqueue(new Callback<VoLogout>() {
            @Override
            public void onResponse(Call<VoLogout> call, Response<VoLogout> response) {
                mActivity.mUtility.HideProgress();
                VoLogout mLoginData = response.body();
                Gson gson = new Gson();
                String json = gson.toJson(mLoginData);
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog(mLoginData.getMessage(), 0, true);
                    } else {
                        mActivity.mUtility.errorDialog(getResources().getString(R.string.str_password_link), 0, true);
                    }
                } else {
                    if (mLoginData != null && mLoginData.getMessage() != null && !mLoginData.getMessage().equalsIgnoreCase("")) {
                        mActivity.mUtility.errorDialog(mLoginData.getMessage(), 3, true);
                    }
                }
            }

            @Override
            public void onFailure(Call<VoLogout> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                mActivity.mUtility.errorDialog(getResources().getString(R.string.str_server_error_try_again), 1, true);
            }
        });
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
