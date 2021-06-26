package com.succorfish.eliteoperator.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.succorfish.eliteoperator.MainActivity;
import com.succorfish.eliteoperator.R;
import com.succorfish.eliteoperator.Vo.VoAddressBook;
import com.succorfish.eliteoperator.db.DataHolder;
import com.succorfish.eliteoperator.interfaces.onAlertDialogCallBack;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Jaydeep on 19-01-2018.
 */

public class FragmentMessageHistory extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    MessageHistoryAdapter mMessageHistoryAdapter;
    @BindView(R.id.frg_message_history_recyclerview_history)
    RecyclerView mRecyclerViewMessage;
    @BindView(R.id.frg_message_history_textview_nohistory)
    TextView mTextViewNoMessageFound;
    @BindView(R.id.raw_msg_history_list_item_textview_sent)
    TextView mTextViewSentMessageCount;
    @BindView(R.id.raw_msg_history_list_item_textview_fail)
    TextView mTextViewFailMessageCount;
    ArrayList<VoAddressBook> mArrayListDeviceList = new ArrayList<>();
    private boolean isCalling = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_message_history, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_message_history_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setImageResource(R.drawable.ic_delete_icon);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        isCalling = true;
        getDBDeviceList();
        getDbDHistoryList();
        mActivity.mImageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mArrayListDeviceList != null && mArrayListDeviceList.size() > 0) {
                    mActivity.mUtility.errorDialogWithYesNoCallBack("Delete History","Are you sure you want to delete all history?", "YES", "NO", 1,new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.cancel();
                            String mStringQuery = "delete from " + mActivity.mDbHelper.mTableHistory;
                            mActivity.mDbHelper.exeQuery(mStringQuery);
                            String mStringQueryLastMessage = "update " + mActivity.mDbHelper.mTableAddressBook + " set " + mActivity.mDbHelper.mFieldAddressBookLastMessageId + "= ''," + mActivity.mDbHelper.mFieldAddressBookLastMessageName + "= ''," + mActivity.mDbHelper.mFieldAddressBookLastMessageTime + "= ''" + " where " + mActivity.mDbHelper.mFieldAddressBookLastMessageId + "!= ''" + " OR " + mActivity.mDbHelper.mFieldAddressBookLastMessageId + "!= 'null'";
                            mActivity.mDbHelper.exeQuery(mStringQueryLastMessage);
                            mActivity.mUtility.errorDialogWithCallBack("All history message deleted successfully", 0,false , new onAlertDialogCallBack() {
                                @Override
                                public void PositiveMethod(DialogInterface dialog, int id) {
                                    isCalling = true;
                                    getDBDeviceList();
                                    getDbDHistoryList();
                                }

                                @Override
                                public void NegativeMethod(DialogInterface dialog, int id) {

                                }
                            });
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                }
            }
        });
        return mViewRoot;
    }

    private void getDbDHistoryList() {
        DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableHistory;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                if (mDataHolder.get_Listholder().size() > 0) {
                    mTextViewSentMessageCount.setText(mDataHolder.get_Listholder().size() + "\nSent");
                } else {
                    mTextViewSentMessageCount.setText("0\nSent");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDBDeviceList() {
        DataHolder mDataHolder;
        mArrayListDeviceList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableAddressBook + " where " + mActivity.mDbHelper.mFieldAddressBookLastMessageId + " is not ''" + " AND " + mActivity.mDbHelper.mFieldAddressBookLastMessageId + " is not NULL";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);

            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoAddressBook mVoAddressBook = new VoAddressBook();
                    mVoAddressBook.setId(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookId));
                    mVoAddressBook.setUser_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookUserId));
                    mVoAddressBook.setUser_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookUserName));
                    mVoAddressBook.setUser_photo(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookUserPhoto));
                    mVoAddressBook.setUser_type(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookUserType));
                    mVoAddressBook.setDevice_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookDeviceId));
                    mVoAddressBook.setLast_msg_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookLastMessageId));
                    mVoAddressBook.setLast_msg_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookLastMessageName));
                    mVoAddressBook.setLast_msg_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookLastMessageTime));
                    mVoAddressBook.setCreated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookCreatedDate));
                    mVoAddressBook.setUpdated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookUpdatedDate));
                    mVoAddressBook.setIs_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookIsSync));
                    mVoAddressBook.setAir_life_percentage("50");
                    mArrayListDeviceList.add(mVoAddressBook);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMessageHistoryAdapter = new MessageHistoryAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewMessage.setLayoutManager(mLayoutManager);
        mRecyclerViewMessage.setAdapter(mMessageHistoryAdapter);

        mMessageHistoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkAdapterIsEmpty();
            }
        });
        checkAdapterIsEmpty();
        isCalling = false;
    }

    private void checkAdapterIsEmpty() {
        if (mMessageHistoryAdapter.getItemCount() == 0) {
            mTextViewNoMessageFound.setVisibility(View.VISIBLE);
            mRecyclerViewMessage.setVisibility(View.GONE);
        } else {
            mTextViewNoMessageFound.setVisibility(View.GONE);
            mRecyclerViewMessage.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mImageViewAdd.setImageResource(R.drawable.ic_add_icon);
        unbinder.unbind();
    }

    public class MessageHistoryAdapter extends RecyclerView.Adapter<MessageHistoryAdapter.ViewHolder> {

        @Override
        public MessageHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_device_list_item, parent, false);
            return new MessageHistoryAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MessageHistoryAdapter.ViewHolder mViewHolder, final int position) {

            if (isAdded()) {
//                mViewHolder.mTextViewPresser.setVisibility(View.GONE);
                if (mArrayListDeviceList.get(position).getAir_life_percentage() != null && !mArrayListDeviceList.get(position).getAir_life_percentage().equalsIgnoreCase("")) {
                    if (Integer.parseInt(mArrayListDeviceList.get(position).getAir_life_percentage()) <= 20) {
                        mViewHolder.mCircleImageView.setImageResource(R.drawable.red_warning);
                    } else {
                        mViewHolder.mCircleImageView.setImageResource(R.drawable.green_warning);
                    }
                } else {
                    mViewHolder.mCircleImageView.setImageResource(R.drawable.green_warning);
                }
                if (mArrayListDeviceList.get(position).getLast_msg_time() != null && !mArrayListDeviceList.get(position).getLast_msg_time().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewLastMessageTime.setText(mArrayListDeviceList.get(position).getLast_msg_time());
                    mViewHolder.mTextViewLastMessageTime.setVisibility(View.VISIBLE);
                } else {
                    mViewHolder.mTextViewLastMessageTime.setText("");
                    mViewHolder.mTextViewLastMessageTime.setVisibility(View.GONE);
                }
                mViewHolder.mTextViewDeviceName.setText(mArrayListDeviceList.get(position).getUser_name());
                mViewHolder.mTextViewDeviceId.setText("ID : " + mArrayListDeviceList.get(position).getId());
                String mStringAir = String.format(getResources().getString(R.string.frg_dashboard_txt_air), mArrayListDeviceList.get(position).getAir_life_percentage());
                mViewHolder.mTextViewPresser.setText(mStringAir);

                if (mArrayListDeviceList.get(position).getLast_msg_name() != null && !mArrayListDeviceList.get(position).getLast_msg_name().equalsIgnoreCase("")) {
                    mViewHolder.mTextViewLastMessage.setText("Last Message : " + mArrayListDeviceList.get(position).getLast_msg_name());
                    mViewHolder.mTextViewLastMessage.setVisibility(View.VISIBLE);
                } else {
                    mViewHolder.mTextViewLastMessage.setText("");
                    mViewHolder.mTextViewLastMessage.setVisibility(View.VISIBLE);
                }
                Glide.with(mActivity)
                        .load(mArrayListDeviceList.get(position).getUser_photo())
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .crossFade()
                        .dontAnimate()
                        .placeholder(R.drawable.ic_diver_default)
                        .into(mViewHolder.mImageViewDeviceIcon);
                mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mArrayListDeviceList != null) {
                            if (position < mArrayListDeviceList.size()) {
                                FragmentSentMessage mFragmentMessage = new FragmentSentMessage();
                                Bundle mBundle = new Bundle();
                                mBundle.putBoolean("intent_is_from_history", true);
                                mBundle.putSerializable("intent_user_data", mArrayListDeviceList.get(position));
                                mActivity.replacesFragment(mFragmentMessage, true, mBundle, 1);
                            }
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return mArrayListDeviceList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_device_list_item_textview_device_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_device_list_item_textview_device_id)
            TextView mTextViewDeviceId;
            @BindView(R.id.raw_device_list_item_textview_device_last_msg)
            TextView mTextViewLastMessage;
            @BindView(R.id.raw_device_list_item_textview_last_msg_time)
            TextView mTextViewLastMessageTime;
            @BindView(R.id.raw_device_list_item_textview_presser)
            TextView mTextViewPresser;
            @BindView(R.id.frg_dashboard_imageview_presser)
            CircleImageView mCircleImageView;
            @BindView(R.id.raw_device_list_item_imageview_device_icon)
            ImageView mImageViewDeviceIcon;
            @BindView(R.id.raw_device_list_item_linearlayout_main)
            LinearLayout mLinearLayoutMain;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
