package com.succorfish.combatdiver.fragments;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.Vo.VoAddressBook;
import com.succorfish.combatdiver.Vo.VoBluetoothDevices;
import com.succorfish.combatdiver.Vo.VoDiverMessage;
import com.succorfish.combatdiver.Vo.VoMessageHistory;
import com.succorfish.combatdiver.db.DataHolder;
import com.succorfish.combatdiver.helper.BLEUtility;
import com.succorfish.combatdiver.interfaces.onAlertDialogCallBack;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 17-01-2018.
 */

public class FragmentSentMessage extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;

    @BindView(R.id.frg_send_message_textview_message)
    TextView mTextViewSelectMessage;
    @BindView(R.id.frg_send_message_popup_select_message)
    RelativeLayout mRelativeLayoutSelectMessage;
    @BindView(R.id.frg_send_message_relativelayout_message)
    RelativeLayout mRelativeLayoutBottomMessage;
    @BindView(R.id.frg_send_message_imageview_message_send)
    ImageView mImageViewSendMessage;
    private TextView mTextViewNoMessageFound;

    MessageAdapter mMessageAdapter;
    MessageHistoryAdapter mMessageHistoryAdapter;
    private BottomSheetDialog mBottomSheetDialog;
    private BottomSheetBehavior mDialogBehavior;
    ArrayList<VoDiverMessage> mArrayListDiverMessage = new ArrayList<>();
    ArrayList<VoBluetoothDevices> mArrayListDevice = new ArrayList<>();
    ArrayList<VoMessageHistory> mArrayListMessageHistory = new ArrayList<>();
    RecyclerView mRecyclerViewMessage;
    private boolean isCalling = true;
    String mStringSelectedMessageId = "";
    String mStringSelectedMessageName = "";
    @BindView(R.id.frg_frg_send_msg_recyclerview_chat_msg)
    RecyclerView mRecyclerViewChatMessage;
    @BindView(R.id.frg_frg_send_msg_textview_no_msg)
    TextView mTextViewNoChatMessageFound;
    VoAddressBook mVoAddressBook;
    boolean isFromHistory = false;
    private SimpleDateFormat mDateFormatDb;
    private SimpleDateFormat mDateFormatDisplay;
    private SimpleDateFormat mDateFormatDisplayTime;
    Dialog myDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        mDateFormatDisplay = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        mDateFormatDisplayTime = new SimpleDateFormat("hh:mm a");
        if (getArguments() != null) {
            mVoAddressBook = (VoAddressBook) getArguments().getSerializable("intent_user_data");
            isFromHistory = getArguments().getBoolean("intent_is_from_history");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_send_message, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_message_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.VISIBLE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.mImageViewPerson.setImageResource(R.drawable.ic_delete_icon);
        if (mStringSelectedMessageName != null && !mStringSelectedMessageName.equalsIgnoreCase("")) {
            mTextViewSelectMessage.setText(mStringSelectedMessageName);
        }
        getDBMessageHistory();
        if (isFromHistory) {
            mRelativeLayoutBottomMessage.setVisibility(View.GONE);
        } else {
            mRelativeLayoutBottomMessage.setVisibility(View.VISIBLE);
        }
        mDialogBehavior = BottomSheetBehavior.from(mRelativeLayoutSelectMessage);
        mDialogBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
        for (int i = 0; i < 5; i++) {
            VoBluetoothDevices mVoBluetoothDevices = new VoBluetoothDevices();
            mVoBluetoothDevices.setDeviceName("SC4 " + i);
            mArrayListDevice.add(mVoBluetoothDevices);
        }
        mActivity.mImageViewPerson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mArrayListMessageHistory != null && mArrayListMessageHistory.size() > 0) {
                    mActivity.mUtility.errorDialogWithYesNoCallBack("Delete History", "Are you sure you want to delete history?", "YES", "NO", 1, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            dialog.cancel();
                            String mStringQuery = "delete from " + mActivity.mDbHelper.mTableHistory + " where " + mActivity.mDbHelper.mFieldHistoryFromId + "= '0'" + " AND " + mActivity.mDbHelper.mFieldHistoryToId + "= '" + mVoAddressBook.getUser_id() + "'";
                            mActivity.mDbHelper.exeQuery(mStringQuery);

                            String mStringQueryLastMessage = "update " + mActivity.mDbHelper.mTableAddressBook + " set " + mActivity.mDbHelper.mFieldAddressBookLastMessageId + "= ''," + mActivity.mDbHelper.mFieldAddressBookLastMessageName + "= ''," + mActivity.mDbHelper.mFieldAddressBookLastMessageTime + "= ''" + " where " + mActivity.mDbHelper.mFieldAddressBookUserId + "= '" + mVoAddressBook.getUser_id() + "'";
                            mActivity.mDbHelper.exeQuery(mStringQueryLastMessage);

                            mActivity.mUtility.errorDialogWithCallBack("All message deleted successfully", 0, new onAlertDialogCallBack() {
                                @Override
                                public void PositiveMethod(DialogInterface dialog, int id) {
                                    isCalling = true;
                                    getDBMessageHistory();
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

    @OnClick(R.id.frg_send_message_relativelayout_select_message)
    public void onMessageClick(View mView) {
        showMessagePopup();
    }

    @OnClick(R.id.frg_send_message_imageview_message_send)
    public void onMessageSendClick(View mView) {
        if (mStringSelectedMessageId == null || mStringSelectedMessageId.equalsIgnoreCase("")) {
            mActivity.mUtility.errorDialog("Please select message first", 1);
            return;
        } else {
            if (mActivity.isDevicesConnected) {
                System.out.println("Send ID-" + mVoAddressBook.getUser_id());
                System.out.println("Message ID-" + mStringSelectedMessageId);
                mActivity.sendMessageToDevice(BLEUtility.intToByte(Integer.parseInt(mVoAddressBook.getUser_id())), BLEUtility.intToByte(Integer.parseInt(mStringSelectedMessageId)), false);
                Calendar cal = Calendar.getInstance();
                String msgTIme = mDateFormatDb.format(cal.getTime());
                ContentValues mContentValues = new ContentValues();
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryFromId, "0");
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryFromName, "Me");
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryToId, mVoAddressBook.getUser_id());
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryToName, mVoAddressBook.getUser_name());
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryMessageId, mStringSelectedMessageId);
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryMessageName, mStringSelectedMessageName);
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryTime, msgTIme);
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryUpdatedDate, msgTIme);
                mContentValues.put(mActivity.mDbHelper.mFieldHistoryStatus, "0");
                int isInsert = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableHistory, mContentValues);
                System.out.println("isInsert-" + isInsert);
                if (isInsert != -1) {
                    System.out.println("Added In Local Db");

                    ContentValues mContentValuesUser = new ContentValues();
                    mContentValuesUser.put(mActivity.mDbHelper.mFieldAddressBookLastMessageId, mStringSelectedMessageId);
                    mContentValuesUser.put(mActivity.mDbHelper.mFieldAddressBookLastMessageName, mStringSelectedMessageName);
                    mContentValuesUser.put(mActivity.mDbHelper.mFieldAddressBookLastMessageTime, msgTIme);
                    String[] mArray = new String[]{mVoAddressBook.getUser_id()};
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAddressBook, mContentValuesUser, mActivity.mDbHelper.mFieldAddressBookUserId + "=?", mArray);
                    System.out.println("Updated In Local Db");
                    mActivity.mUtility.errorDialogWithCallBack("Message sent successfully", 0, new onAlertDialogCallBack() {
                        @Override
                        public void PositiveMethod(DialogInterface dialog, int id) {
                            isCalling = true;
                            getDBMessageHistory();
                        }

                        @Override
                        public void NegativeMethod(DialogInterface dialog, int id) {

                        }
                    });
                } else {
                    System.out.println("Failed Adding In Local DB");
                }
            } else {
                mActivity.showDisconnectedDeviceAlert();
            }
        }
//        showBottomSheetConnectDeviceDialog();
    }

    private void getDBMessageHistory() {
        DataHolder mDataHolder;
        mArrayListMessageHistory = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableHistory + " where " + mActivity.mDbHelper.mFieldHistoryFromId + "= '0'" + " AND " + mActivity.mDbHelper.mFieldHistoryToId + "= '" + mVoAddressBook.getId() + "'" + " ORDER BY date('" + mActivity.mDbHelper.mFieldHistoryTime + "'" + ") DESC";
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);

            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoMessageHistory mVoMessageHistory = new VoMessageHistory();
                    mVoMessageHistory.setHistory_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryID));
                    mVoMessageHistory.setHistory_from_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryFromId));
                    mVoMessageHistory.setHistory_from_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryFromName));
                    mVoMessageHistory.setHistory_to_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryToId));
                    mVoMessageHistory.setHistory_to_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryToName));
                    mVoMessageHistory.setHistory_message_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryMessageId));
                    mVoMessageHistory.setHistory_message_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryMessageName));
                    mVoMessageHistory.setHistory_time(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryTime));
                    mVoMessageHistory.setHistory_status(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryStatus));
                    mVoMessageHistory.setHistory_updated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldHistoryUpdatedDate));
                    mArrayListMessageHistory.add(mVoMessageHistory);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMessageHistoryAdapter = new MessageHistoryAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewChatMessage.setLayoutManager(mLayoutManager);

        mRecyclerViewChatMessage.setAdapter(mMessageHistoryAdapter);

        mMessageHistoryAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkHistoryAdapterIsEmpty();
            }

        });
        mRecyclerViewChatMessage.scrollToPosition(mArrayListMessageHistory.size() - 1);
        checkHistoryAdapterIsEmpty();
        isCalling = false;
    }

    private void checkHistoryAdapterIsEmpty() {
        if (mMessageHistoryAdapter.getItemCount() == 0) {
            mTextViewNoChatMessageFound.setVisibility(View.VISIBLE);
            mRecyclerViewChatMessage.setVisibility(View.GONE);
        } else {
            mTextViewNoChatMessageFound.setVisibility(View.GONE);
            mRecyclerViewChatMessage.setVisibility(View.VISIBLE);
        }
    }

    private void getDBDiverMessageList() {
//        int tableGroupCount = mActivity.mDbHelper.getTableCount(mActivity.mDbHelper.mTableGroup);
        DataHolder mDataHolder;
        mArrayListDiverMessage = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableCannedDiverMessage;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);

            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoDiverMessage mVoDiverMessage = new VoDiverMessage();
                    mVoDiverMessage.setId(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageId));
                    mVoDiverMessage.setDriver_message_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageMSGId));
                    mVoDiverMessage.setMessage(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageMessage));
                    mVoDiverMessage.setIs_emergency(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageIsEmergency));
                    mVoDiverMessage.setCreated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageCreatedDate));
                    mVoDiverMessage.setUpdated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageUpdatedDate));
                    mVoDiverMessage.setIs_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldDiverMessageIsSync));
                    mArrayListDiverMessage.add(mVoDiverMessage);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        mMessageAdapter = new MessageAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, true);
        mRecyclerViewMessage.setLayoutManager(mLayoutManager);
        mRecyclerViewMessage.setAdapter(mMessageAdapter);

        mMessageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
        if (mMessageAdapter.getItemCount() == 0) {
            mTextViewNoMessageFound.setVisibility(View.VISIBLE);
            mRecyclerViewMessage.setVisibility(View.GONE);
        } else {
            mTextViewNoMessageFound.setVisibility(View.GONE);
            mRecyclerViewMessage.setVisibility(View.VISIBLE);
        }
    }

    private void showMessagePopup() {
        if (myDialog != null && myDialog.isShowing()) {
            return;
        }
        myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_sent_message);
        myDialog.setCancelable(false);
        ColorDrawable back = new ColorDrawable(getResources().getColor(R.color.colorPopupTransparent));
        InsetDrawable inset = new InsetDrawable(back, 0);
        myDialog.getWindow().setBackgroundDrawable(inset);

        mTextViewNoMessageFound = (TextView) myDialog
                .findViewById(R.id.popup_sent_msg_textview_nomsg);
        TextView mTextViewDone = (TextView) myDialog.findViewById(R.id.popup_sent_msg_textview_done);
        TextView mTextViewCancel = (TextView) myDialog.findViewById(R.id.popup_sent_msg_textview_cancel);
        mRecyclerViewMessage = (RecyclerView) myDialog.findViewById(R.id.popup_sent_msg_recyclerview_msg);

        isCalling = true;
        getDBDiverMessageList();
        mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });
        mTextViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });


        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(myDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        myDialog.show();
        myDialog.getWindow().setAttributes(lp);
    }

    @SuppressLint("InflateParams")
    private void showBottomSheetDialog() {
        if (mDialogBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            mDialogBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

//        View view = getLayoutInflater().inflate(R.layout.popup_sent_message, null);
//        mRecyclerViewMessage = (RecyclerView) view.findViewById(R.id.frg_send_message_poopup_recyclerview_message);
//        mTextViewNoMessageFound = (TextView) view.findViewById(R.id.frg_send_message_poopup_textview_nomessage);
//        isCalling = true;
//        getDBDiverMessageList();
//        mBottomSheetDialog = new BottomSheetDialog(mActivity);
//        mBottomSheetDialog.setContentView(view);
//        mDialogBehavior = BottomSheetBehavior.from((View) view.getParent());
//
//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                mBottomSheetDialog = null;
//            }
//        });
    }

    @SuppressLint("InflateParams")
    private void showBottomSheetConnectDeviceDialog() {

        if (mStringSelectedMessageId == null || mStringSelectedMessageId.equalsIgnoreCase("")) {
            mActivity.mUtility.errorDialog("Please select message first", 1);
            return;
        }

//        if (mDialogBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//            mDialogBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        }
//
//        View view = getLayoutInflater().inflate(R.layout.popup_sent_message, null);
//        RecyclerView mRecyclerViewMessage = (RecyclerView) view.findViewById(R.id.frg_send_message_poopup_recyclerview_message);
//        TextView mTextViewNoMessageFound = (TextView) view.findViewById(R.id.frg_send_message_poopup_textview_nomessage);
//        TextView mTextViewCancel = (TextView) view.findViewById(R.id.frg_send_message_poopup_textview_cancel);
//        TextView mTextViewHeader = (TextView) view.findViewById(R.id.frg_send_message_poopup_textview_header);
//
//        RelativeLayout mRelativeLayoutConnectDevice = (RelativeLayout) view.findViewById(R.id.frg_send_message_poopup_relativelayout_connectdevice);
//        mRelativeLayoutConnectDevice.setVisibility(View.VISIBLE);
//        mTextViewHeader.setVisibility(View.GONE);
//        mConnectDevicAdapter = new ConnectDeviceAdapter();
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
//        mRecyclerViewMessage.setLayoutManager(mLayoutManager);
//        mRecyclerViewMessage.setAdapter(mConnectDevicAdapter);
//
//        mBottomSheetDialog = new BottomSheetDialog(mActivity);
//        mBottomSheetDialog.setContentView(view);
//        mDialogBehavior = BottomSheetBehavior.from((View) view.getParent());
//
//        mBottomSheetDialog.show();
//        mBottomSheetDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                mBottomSheetDialog = null;
//            }
//        });
//        mTextViewCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mDialogBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
//            }
//        });
    }

    public class MessageHistoryAdapter extends RecyclerView.Adapter<MessageHistoryAdapter.ViewHolder> {

        @Override
        public MessageHistoryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_message_chat_list_item, parent, false);
            return new MessageHistoryAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MessageHistoryAdapter.ViewHolder mViewHolder, final int position) {

//            if (position % 2 == 0) {
            mViewHolder.mTextViewMessage.setText(mArrayListMessageHistory.get(position).getHistory_message_name());
            mViewHolder.mFrameLayoutOutgoing.setVisibility(View.VISIBLE);
            mViewHolder.mFrameLayoutInComing.setVisibility(View.GONE);
            if (mArrayListMessageHistory.get(position).getHistory_time() != null && !mArrayListMessageHistory.get(position).getHistory_time().equalsIgnoreCase("")) {
                try {
                    Date mDate = mDateFormatDb.parse(mArrayListMessageHistory.get(position).getHistory_time());
                    long previousTs = 0;
                    if (position > 0) {
                        if (mArrayListMessageHistory.get(position - 1).getHistory_time() != null && !mArrayListMessageHistory.get(position - 1).getHistory_time().equalsIgnoreCase("")) {
                            Date mDatePrevious = mDateFormatDb.parse(mArrayListMessageHistory.get(position - 1).getHistory_time());
                            previousTs = mDatePrevious.getTime();
                        }
                    }
                    setTimeTextVisibility(mDate.getTime(), previousTs, mViewHolder.mTextViewHeaderDateTime);
                    mViewHolder.mTextViewMessageDate.setText(mDateFormatDisplayTime.format(mDate));
                } catch (ParseException e) {
                    mViewHolder.mTextViewMessageDate.setText("-NA-");
                    e.printStackTrace();
                }

            }
//            } else {
//                mViewHolder.mTextViewInComingMessage.setText(mArrayListMessageHistory.get(position).getHistory_message_name());
//                mViewHolder. mFrameLayoutOutgoing.setVisibility(View.GONE);
//                mViewHolder. mFrameLayoutInComing.setVisibility(View.VISIBLE);
//                if (mArrayListMessageHistory.get(position).getHistory_time() != null && !mArrayListMessageHistory.get(position).getHistory_time().equalsIgnoreCase("")) {
//                    try {
//                        Date mDate = mDateFormatDb.parse(mArrayListMessageHistory.get(position).getHistory_time());
//                        long previousTs = 0;
//                        if (position > 0) {
//                            if (mArrayListMessageHistory.get(position - 1).getHistory_time() != null && !mArrayListMessageHistory.get(position - 1).getHistory_time().equalsIgnoreCase("")) {
//                                Date mDatePrevious = mDateFormatDb.parse(mArrayListMessageHistory.get(position - 1).getHistory_time());
//                                previousTs = mDatePrevious.getTime();
//                            }
//                        }
//                        setTimeTextVisibility(mDate.getTime(), previousTs, mViewHolder.mTextViewHeaderDateTime);
//                        mViewHolder.mTextViewInComingMessageDate.setText(mDateFormatDisplayTime.format(mDate));
//                    } catch (ParseException e) {
//                        mViewHolder.mTextViewInComingMessageDate.setText("-NA-");
//                        e.printStackTrace();
//                    }
//
//                }
//            }

            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDiverMessage != null) {
                        if (position < mArrayListDiverMessage.size()) {

                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListMessageHistory.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.incoming_layout_bubble)
            FrameLayout mFrameLayoutInComing;
            @BindView(R.id.outgoing_layout_bubble)
            FrameLayout mFrameLayoutOutgoing;

            @BindView(R.id.textview_outgoing_message)
            TextView mTextViewMessage;
            @BindView(R.id.textview_outgoing_time)
            TextView mTextViewMessageDate;

            @BindView(R.id.textview_incoming_message)
            TextView mTextViewInComingMessage;
            @BindView(R.id.textview_incoming_time)
            TextView mTextViewInComingMessageDate;

            @BindView(R.id.textview_header_time)
            TextView mTextViewHeaderDateTime;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        private void setTimeTextVisibility(long ts1, long ts2, TextView timeText) {

            if (ts2 == 0) {
                Calendar calSendTime = Calendar.getInstance();
                Calendar now = Calendar.getInstance();

                calSendTime.setTimeInMillis(ts1);
                timeText.setVisibility(View.VISIBLE);
                if (now.get(Calendar.DATE) == calSendTime.get(Calendar.DATE)&&
                        now.get(Calendar.MONTH) == calSendTime.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == calSendTime.get(Calendar.DAY_OF_MONTH)) {
                    timeText.setText("Today");
                } else if (now.get(Calendar.DATE) - calSendTime.get(Calendar.DATE) == 1&&
                        now.get(Calendar.MONTH) == calSendTime.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == calSendTime.get(Calendar.DAY_OF_MONTH)) {
                    timeText.setText("Yesterday");
                }  else {
                    timeText.setText(mDateFormatDisplay.format(calSendTime.getTime()));
                }

            } else {
                Calendar cal1 = Calendar.getInstance();
                Calendar cal2 = Calendar.getInstance();
                cal1.setTimeInMillis(ts1);
                cal2.setTimeInMillis(ts2);

                boolean sameDay = cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                        cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) && cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
                if (sameDay) {
                    timeText.setVisibility(View.GONE);
                    timeText.setText("");
                } else {
                    timeText.setVisibility(View.VISIBLE);
//                    timeText.setText(mDateFormatDisplay.format(new Date(ts1)));
                    Calendar now = Calendar.getInstance();
                    if (now.get(Calendar.DATE) == cal1.get(Calendar.DATE)&&
                            now.get(Calendar.MONTH) == cal1.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == cal1.get(Calendar.DAY_OF_MONTH)) {
                        timeText.setText("Today");
                    } else if (now.get(Calendar.DATE) - cal1.get(Calendar.DATE) == 1&&
                            now.get(Calendar.MONTH) == cal1.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) == cal1.get(Calendar.DAY_OF_MONTH)) {
                        timeText.setText("Yesterday");
                    } else {
                        timeText.setText(mDateFormatDisplay.format(cal1.getTime()));
                    }

                }

            }
        }
    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        @Override
        public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_message_list_item, parent, false);
            return new MessageAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MessageAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mTextViewMessage.setText(mArrayListDiverMessage.get(position).getId() + ". " + mArrayListDiverMessage.get(position).getMessage());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDiverMessage != null) {
                        if (position < mArrayListDiverMessage.size()) {
                            if (myDialog != null) {
                                if (myDialog.isShowing()) {
                                    myDialog.dismiss();
                                }
                            }
                            mTextViewSelectMessage.setText(mArrayListDiverMessage.get(position).getMessage());
                            mStringSelectedMessageId = mArrayListDiverMessage.get(position).getId();
                            mStringSelectedMessageName = mArrayListDiverMessage.get(position).getMessage();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListDiverMessage.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_message_list_item_textview_message)
            TextView mTextViewMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

//    public class ConnectDeviceAdapter extends RecyclerView.Adapter<ConnectDeviceAdapter.ViewHolder> {
//
//        @Override
//        public ConnectDeviceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_message_list_item, parent, false);
//            return new ConnectDeviceAdapter.ViewHolder(itemView);
//        }
//
//        @Override
//        public void onBindViewHolder(final ConnectDeviceAdapter.ViewHolder mViewHolder, final int position) {
//            mViewHolder.mTextViewMessage.setText(mArrayListDevice.get(position).getDeviceName());
//            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
////                    if (mArrayListDeviceUser != null) {
////                        if (position < mArrayListDeviceUser.size()) {
//                    mDialogBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
////                        }
////                    }
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return mArrayListDevice.size();
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            @BindView(R.id.raw_message_list_item_textview_message)
//            TextView mTextViewMessage;
//
//            public ViewHolder(View itemView) {
//                super(itemView);
//                ButterKnife.bind(this, itemView);
//            }
//        }
//    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mImageViewPerson.setImageResource(R.drawable.ic_active_user);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
    }
}
