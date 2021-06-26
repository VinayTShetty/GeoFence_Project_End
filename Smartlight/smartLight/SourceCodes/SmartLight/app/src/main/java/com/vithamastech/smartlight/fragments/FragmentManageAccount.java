package com.vithamastech.smartlight.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vithamastech.smartlight.LoginActivity;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoUserData;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;


public class FragmentManageAccount extends Fragment {

    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_setting_manage_account_rv)
    RecyclerView mRecyclerView;
    @BindView(R.id.fragment_setting_bridge_conn_tv_no_user)
    TextView mTextViewNoDeviceFound;
    @BindView(R.id.fragment_manage_account_button_add_user)
    FloatingActionButton mFloatingActionButtonAdd;
    UserListAdapter mUserListAdapter;
    ArrayList<VoUserData> mArrayDeviceUserList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_manage_account, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mTextViewTitle.setText(R.string.frg_settings_manage_account);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.showBackButton(true);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mArrayDeviceUserList = new ArrayList<>();
        getDBUserList();
        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });
        return mViewRoot;
    }

    @OnClick(R.id.fragment_manage_account_button_add_user)
    public void onFloatingAddButtonClick(View mView) {
        if (isAdded()) {
            Intent mIntent = new Intent(mActivity, LoginActivity.class);
            mIntent.putExtra("is_from_add_account", true);
            startActivity(mIntent);
        }
    }

    /*Get All user from the database*/
    private void getDBUserList() {
        DataHolder mDataHolder;
        mArrayDeviceUserList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableUserAccount + " where " + mActivity.mDbHelper.mFieldUserServerID + "!= '" + mActivity.mPreferenceHelper.getUserId() + "'";
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
                    mArrayDeviceUserList.add(mVoUserData);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
        mUserListAdapter = new UserListAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mUserListAdapter);
        mUserListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
    }

    @Override
    public void onResume() {
        super.onResume();
        getDBUserList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mRecyclerView.setAdapter(null);
        mArrayDeviceUserList = null;
        mTextViewNoDeviceFound = null;
        mUserListAdapter = null;
        unbinder.unbind();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mImageViewBack.setVisibility(View.GONE);
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
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_user_list_item, parent, false);
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
            holder.mImageViewDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayDeviceUserList != null) {
                        if (position < mArrayDeviceUserList.size()) {
                            mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.frg_manage_account_delete_user_confirmation), getResources().getString(R.string.frg_delete_user_confirmation), "Yes", "No", true, 2, new onAlertDialogCallBack() {
                                @Override
                                public void PositiveMethod(DialogInterface dialog, int id) {
                                    mActivity.mDbHelper.exeQuery("delete from " + mActivity.mDbHelper.mTableUserAccount + " where " + mActivity.mDbHelper.mFieldUserLocalID + "= '" + mArrayDeviceUserList.get(position).getUser_local_id() + "'");
                                    getDBUserList();
                                }

                                @Override
                                public void NegativeMethod(DialogInterface dialog, int id) {

                                }
                            });
                        }
                    }
                }
            });
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayDeviceUserList != null) {
                        if (position < mArrayDeviceUserList.size()) {

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
            @BindView(R.id.raw_user_list_item_tv_username)
            TextView mTextViewUserName;
            @BindView(R.id.raw_user_list_item_tv_mobile)
            TextView mTextViewMobileNo;
            @BindView(R.id.raw_user_list_item_iv_delete)
            ImageView mImageViewDelete;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
    /**
     * Test commint
     */
}

