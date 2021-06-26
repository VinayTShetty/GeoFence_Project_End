package com.vithamastech.smartlight.fragments;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.google.gson.Gson;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoLogout;
import com.vithamastech.smartlight.Vo.VoUserData;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Jaydeep on 22-12-2017.
 */

public class FragmentSwitchAccount extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_switch_account_rv)
    RecyclerView mRecyclerViewUser;
    @BindView(R.id.fragment_switch_account_tv_no_user)
    TextView mTextViewNoDeviceFound;

    ArrayList<VoUserData> mArrayDeviceUserList = new ArrayList<>();
    UserListAdapter mUserListAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_switch_account, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_title_switch_account);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.showBackButton(true);
        mArrayDeviceUserList = new ArrayList<>();
        getUserAccountList();

        return mViewRoot;
    }

    /*Get All user from account table*/
    private void getUserAccountList() {
        DataHolder mDataHolder;
        mArrayDeviceUserList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableUserAccount;
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                VoUserData mVoUserData;
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    mVoUserData = new VoUserData();
                    mVoUserData.setUser_local_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserLocalID));
                    mVoUserData.setUser_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserServerID));
                    mVoUserData.setUserName(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserName));
                    mVoUserData.setAccount_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserAccountName));
                    mVoUserData.setEmail(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserEmail));
                    mVoUserData.setPassword(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserPassword));
                    mVoUserData.setMobile_number(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserMobileNo));
                    mVoUserData.setDevice_token(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldUserToken));
                    mVoUserData.setIsChecked(false);
                    if (mVoUserData.getUser_id().equalsIgnoreCase(mActivity.mPreferenceHelper.getUserId())) {
                        mVoUserData.setIsChecked(true);
                    }
                    mArrayDeviceUserList.add(mVoUserData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mUserListAdapter = new UserListAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewUser.setLayoutManager(mLayoutManager);
        mRecyclerViewUser.setAdapter(mUserListAdapter);
        mUserListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
    }

    /*Check adapter is empty or not*/
    private void checkAdapterIsEmpty() {
        if (isAdded()) {
            if (mUserListAdapter != null) {
                if (mUserListAdapter.getItemCount() > 0) {
                    mTextViewNoDeviceFound.setVisibility(View.GONE);
                } else {
                    mTextViewNoDeviceFound.setVisibility(View.VISIBLE);
                }
            } else {
                mTextViewNoDeviceFound.setVisibility(View.VISIBLE);
            }
        }
    }

    /*User list adapter*/
    public class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.ViewHolder> {

        @Override
        public UserListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_switch_user_list_item, parent, false);
            return new UserListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(UserListAdapter.ViewHolder holder, final int position) {
            if (mArrayDeviceUserList.get(position).getAccount_name() != null && !mArrayDeviceUserList.get(position).getAccount_name().equalsIgnoreCase("") && !mArrayDeviceUserList.get(position).getAccount_name().equalsIgnoreCase("null")) {
                holder.mTextViewUserName.setText(mArrayDeviceUserList.get(position).getAccount_name());
            } else {
                holder.mTextViewUserName.setText("NA");
            }
            holder.mTextViewMobileNo.setText(mArrayDeviceUserList.get(position).getMobile_number());
            if (mArrayDeviceUserList.get(position).getIsChecked()) {
                holder.mRadioButtonChecked.setChecked(true);
            } else {
                holder.mRadioButtonChecked.setChecked(false);
            }
            if (position == mArrayDeviceUserList.size() - 1) {
                holder.mViewDivider.setVisibility(View.GONE);
            } else {
                holder.mViewDivider.setVisibility(View.VISIBLE);
            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayDeviceUserList != null) {
                        if (position < mArrayDeviceUserList.size()) {
                            mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.frg_settings_switch_account), getResources().getString(R.string.frg_switch_account_confirmation), "Yes", "No", true, 3, new onAlertDialogCallBack() {
                                @Override
                                public void PositiveMethod(DialogInterface dialog, int id) {
                                    if (mArrayDeviceUserList != null) {
                                        if (position < mArrayDeviceUserList.size()) {
                                            if (mActivity.mUtility.haveInternet()) {
                                                checkAuthenticationAPI(true, position);
                                            } else {
                                                switchAccount(position);
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void NegativeMethod(DialogInterface dialog, int id) {

                                }
                            });
//                            for (int i = 0; i < mArrayDeviceUserList.size(); i++) {
//                                mArrayDeviceUserList.get(i).setIsChecked(false);
//                            }
//                            mArrayDeviceUserList.get(position).setIsChecked(true);
//                            notifyDataSetChanged();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayDeviceUserList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_switch_user_list_item_tv_username)
            TextView mTextViewUserName;
            @BindView(R.id.raw_switch_user_list_item_tv_mobile)
            TextView mTextViewMobileNo;
            @BindView(R.id.raw_switch_user_list_item_rb_checked)
            RadioButton mRadioButtonChecked;
            @BindView(R.id.raw_switch_user_list_item_view_divider)
            View mViewDivider;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Switch account from one user to another*/
    private void switchAccount(int position) {
        mActivity.mPreferenceHelper.ResetPrefData();
        mActivity.mPreferenceHelper.setUserFirstName(mArrayDeviceUserList.get(position).getUserName());
        mActivity.mPreferenceHelper.setAccountName(mArrayDeviceUserList.get(position).getAccount_name());
        mActivity.mPreferenceHelper.setUserId(mArrayDeviceUserList.get(position).getUser_id());
        mActivity.mPreferenceHelper.setUserEmail(mArrayDeviceUserList.get(position).getEmail());
        mActivity.mPreferenceHelper.setUserContactNo(mArrayDeviceUserList.get(position).getMobile_number());
        mActivity.mPreferenceHelper.setUserPassword(mArrayDeviceUserList.get(position).getPassword());
        mActivity.mPreferenceHelper.setDeviceToken(mActivity.mPreferenceHelper.getDeviceToken());
        try {
            String hash256 = BLEUtility.hash256Encryption(mArrayDeviceUserList.get(position).getMobile_number() + "~vith");
            String mHashSecretKey = (hash256.length() >= 32) ? hash256.substring(hash256.length() - 32, hash256.length()) : hash256;
            mActivity.mPreferenceHelper.setSecretKey(mHashSecretKey);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String isExistInDB;
        ContentValues mContentValues;
        for (int i = 0; i < 6; i++) {
            isExistInDB = CheckRecordExistInAlarmDB(i + "");
            if (isExistInDB.equalsIgnoreCase("-1")) {
                mContentValues = new ContentValues();
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmUserId, mActivity.mPreferenceHelper.getUserId());
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmName, "Alarm " + (i + 1));
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmTime, "");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmStatus, "1");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmDays, "6543210");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmLightOn, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmWakeUpSleep, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmColor, 0);
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmCountNo, i);
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmIsActive, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldAlarmIsSync, "0");
                mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableAlarm, mContentValues);
            }
        }
        mActivity.mPreferenceHelper.setIsDeviceSync(false);
        mActivity.mPreferenceHelper.setIsGroupSync(false);
        mActivity.mTextViewDrawerAccountName.setText(mActivity.mPreferenceHelper.getAccountName());
        mActivity.mTextViewDrawerMobileNo.setText(mActivity.mPreferenceHelper.getUserContactNo());
        mActivity.onBackPressed();
    }

    /*Check record exist in alarm table or not*/
    public String CheckRecordExistInAlarmDB(String alarmCount) {
        DataHolder mDataHolder;
        String url = "select * from " + mActivity.mDbHelper.mTableAlarm + " where " + mActivity.mDbHelper.mFieldAlarmUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'" + " and " + mActivity.mDbHelper.mFieldAlarmCountNo + "= '" + alarmCount + "'";
        System.out.println(" URL : " + url);
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" AlarmList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldAlarmLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Call Authentication API*/
    private void checkAuthenticationAPI(final boolean isShowProgress, final int position) {
        mActivity.mUtility.hideKeyboard(mActivity);
        if (isShowProgress) {
            mActivity.mUtility.ShowProgress("Please Wait..");
        }
        Map<String, String> params = new HashMap<String, String>();
        params.put("user_id", mArrayDeviceUserList.get(position).getUser_id());
        params.put("password", mArrayDeviceUserList.get(position).getPassword());
        Call<VoLogout> mLogin = mActivity.mApiService.authenticateUserCheck(params);
        mLogin.enqueue(new Callback<VoLogout>() {
            @Override
            public void onResponse(Call<VoLogout> call, Response<VoLogout> response) {
                if (isShowProgress) {
                    mActivity.mUtility.HideProgress();
                }
                VoLogout mLoginData = response.body();
                Gson gson = new Gson();
                String json = gson.toJson(mLoginData);
                if (mLoginData != null && mLoginData.getResponse().equalsIgnoreCase("true")) {
                    switchAccount(position);
                } else {
                    mActivity.mUtility.errorDialogWithCallBack(getResources().getString(R.string.frg_user_credentials_change), 3, true, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<VoLogout> call, Throwable t) {
                mActivity.mUtility.HideProgress();
                mActivity.onBackPressed();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerViewUser.setAdapter(null);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }
}
