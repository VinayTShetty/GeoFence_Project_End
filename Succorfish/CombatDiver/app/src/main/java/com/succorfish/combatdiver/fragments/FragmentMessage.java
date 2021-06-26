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
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.succorfish.combatdiver.interfaces.onAlertDialogCallBack;
import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.Vo.VoAddressBook;
import com.succorfish.combatdiver.Vo.VoDiverMessage;
import com.succorfish.combatdiver.db.DataHolder;
import com.succorfish.combatdiver.helper.BLEUtility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 17-01-2018.
 */

public class FragmentMessage extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;

    @BindView(R.id.frg_message_recyclerview_message)
    RecyclerView mRecyclerViewMessage;
    @BindView(R.id.frg_message_textview_nomessage)
    TextView mTextViewNoMessageFound;
//    @BindView(R.id.frg_message_popup_select_message)
//    RelativeLayout mRelativeLayoutContact;

    MessageAdapter mMessageAdapter;
    ContactAdapter mContactAdapter;

    ArrayList<VoAddressBook> mArrayListContact = new ArrayList<>();

    private BottomSheetDialog mBottomSheetDialog;
//    private BottomSheetBehavior mDialogBehavior;
    Dialog myDialog;
    ArrayList<VoDiverMessage> mArrayListDiverMessage = new ArrayList<>();
    private boolean isCalling = true;
    private TextView mTextViewNoContactFound;
    private RecyclerView mRecyclerViewContact;
    String mStringSelectedMessageId = "";
    String mStringSelectedMessageName = "";
    private SimpleDateFormat mDateFormatDb;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_message, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_message_txt_composed_msg));
        mActivity.mImageViewDrawer.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        isCalling = true;
        getDBDiverMessageList();
//        mDialogBehavior = BottomSheetBehavior.from(mRelativeLayoutContact);
//        mDialogBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
//            @Override
//            public void onStateChanged(@NonNull View bottomSheet, int newState) {
//
//            }
//
//            @Override
//            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
//
//            }
//        });
        return mViewRoot;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    private void getDBDiverMessageList() {
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
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
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

    private void getDbAddressContactList() {
//        int tableGroupCount = mActivity.mDbHelper.getTableCount(mActivity.mDbHelper.mTableGroup);
        DataHolder mDataHolder;
        mArrayListContact = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableAddressBook;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null && mDataHolder.get_Listholder().size() > 0) {
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
                    mArrayListContact.add(mVoAddressBook);
                }
//                Collections.sort(mArrayListTreatmentLists, new Comparator<TreatmentList>() {
//                    @Override
//                    public int compare(TreatmentList s1, TreatmentList s2) {
//                        return s1.getTreatment_title().compareToIgnoreCase(s1.getTreatment_title());
//                    }
//                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mContactAdapter = new ContactAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewContact.setLayoutManager(mLayoutManager);
        mRecyclerViewContact.setAdapter(mContactAdapter);
        mContactAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkContactAdapterIsEmpty();
            }
        });
        checkContactAdapterIsEmpty();
        isCalling = false;
    }

    private void checkContactAdapterIsEmpty() {
        if (mContactAdapter.getItemCount() == 0) {
            mTextViewNoContactFound.setVisibility(View.VISIBLE);
            mRecyclerViewContact.setVisibility(View.GONE);
        } else {
            mTextViewNoContactFound.setVisibility(View.GONE);
            mRecyclerViewContact.setVisibility(View.VISIBLE);
        }
    }

    private void showContactPopup() {
        if (myDialog != null && myDialog.isShowing()) {
            return;
        }
        myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_driver_message);
        myDialog.setCancelable(false);
        ColorDrawable back = new ColorDrawable(getResources().getColor(R.color.colorPopupTransparent));
        InsetDrawable inset = new InsetDrawable(back, 0);
        myDialog.getWindow().setBackgroundDrawable(inset);
        TextView mTextViewSendToAll = (TextView) myDialog
                .findViewById(R.id.poopup_contact_textview_send_to_all);
        mTextViewNoContactFound = (TextView) myDialog
                .findViewById(R.id.poopup_contact_textview_nocontact);
        TextView mTextViewDone = (TextView) myDialog.findViewById(R.id.poopup_contact_textview_done);
        TextView mTextViewCancel = (TextView) myDialog.findViewById(R.id.poopup_contact_textview_cancel);
        mRecyclerViewContact = (RecyclerView) myDialog.findViewById(R.id.poopup_contact_recyclerview_contact);

        getDbAddressContactList();
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
        mTextViewSendToAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
                if (mActivity.isDevicesConnected) {
                    mActivity.sendMessageToDevice(BLEUtility.intToByte(255), BLEUtility.intToByte(Integer.parseInt(mStringSelectedMessageId)), true);
                    Calendar cal = Calendar.getInstance();
                    Date currentLocalTime = cal.getTime();
                    String msgTIme = mDateFormatDb.format(currentLocalTime);
                    for (int i = 0; i < mArrayListContact.size(); i++) {
                        ContentValues mContentValues = new ContentValues();
                        mContentValues.put(mActivity.mDbHelper.mFieldHistoryFromId, "0");
                        mContentValues.put(mActivity.mDbHelper.mFieldHistoryFromName, "Me");
                        mContentValues.put(mActivity.mDbHelper.mFieldHistoryToId, mArrayListContact.get(i).getUser_id());
                        mContentValues.put(mActivity.mDbHelper.mFieldHistoryToName, mArrayListContact.get(i).getUser_name());
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
                            String[] mArray = new String[]{mArrayListContact.get(i).getUser_id()};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAddressBook, mContentValuesUser, mActivity.mDbHelper.mFieldAddressBookUserId + "=?", mArray);
                            System.out.println("Updated In Local Db");

                            if (i == mArrayListContact.size() - 1) {
                                mActivity.mUtility.errorDialogWithCallBack("Message sent successfully",0, new onAlertDialogCallBack() {
                                    @Override
                                    public void PositiveMethod(DialogInterface dialog, int id) {

                                    }

                                    @Override
                                    public void NegativeMethod(DialogInterface dialog, int id) {

                                    }
                                });
                            }
                        } else {
                            System.out.println("Failed Adding In Local DB");
                        }
                    }
                } else {
                    mActivity.showDisconnectedDeviceAlert();
                }
//                showBottomSheetConnectDeviceDialog();
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
    private void showBottomSheetConnectDeviceDialog() {
//        if (mDialogBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
//            mDialogBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
//        }

//        View view = getLayoutInflater().inflate(R.layout.popup_sent_message, null);
//        RecyclerView mRecyclerViewMessage = (RecyclerView) view.findViewById(R.id.frg_send_message_poopup_recyclerview_message);
//        TextView mTextViewNoMessageFound = (TextView) view.findViewById(R.id.frg_send_message_poopup_textview_nomessage);
//        TextView mTextViewCancel = (TextView) view.findViewById(R.id.frg_send_message_poopup_textview_cancel);
//        TextView mTextViewHeader = (TextView) view.findViewById(R.id.frg_send_message_poopup_textview_header);
//
//        RelativeLayout mRelativeLayoutConnectDevice = (RelativeLayout) view.findViewById(R.id.frg_send_message_poopup_relativelayout_connectdevice);
//        mRelativeLayoutConnectDevice.setVisibility(View.VISIBLE);
//        mTextViewHeader.setVisibility(View.GONE);
////        mConnectDevicAdapter = new ConnectDevicAdapter();
////        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
////        mRecyclerViewMessage.setLayoutManager(mLayoutManager);
////        mRecyclerViewMessage.setAdapter(mConnectDevicAdapter);
//
//        mBottomSheetDialog = new BottomSheetDialog(mActivity);
//        mBottomSheetDialog.setContentView(view);
//        mDialogBehavior = BottomSheetBehavior.from((View) view.getParent());
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

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        @Override
        public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_driver_message_list_item, parent, false);
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
                            mStringSelectedMessageId = mArrayListDiverMessage.get(position).getId();
                            mStringSelectedMessageName = mArrayListDiverMessage.get(position).getMessage();
                            showContactPopup();
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
            @BindView(R.id.raw_driver_msg_list_item_textview_name)
            TextView mTextViewMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

        @Override
        public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_message_list_item, parent, false);
            return new ContactAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ContactAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mTextViewMessage.setText(mArrayListContact.get(position).getUser_id() + ". " + mArrayListContact.get(position).getUser_name());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListContact != null) {
                        if (position < mArrayListContact.size()) {
                            if (myDialog != null) {
                                myDialog.dismiss();
                            }
                            if (mActivity.isDevicesConnected) {
                                mActivity.sendMessageToDevice(BLEUtility.intToByte(Integer.parseInt(mArrayListContact.get(position).getUser_id())), BLEUtility.intToByte(Integer.parseInt(mStringSelectedMessageId)), false);
                                Calendar cal = Calendar.getInstance();
                                Date currentLocalTime = cal.getTime();
                                String msgTIme = mDateFormatDb.format(currentLocalTime);
                                ContentValues mContentValues = new ContentValues();
                                mContentValues.put(mActivity.mDbHelper.mFieldHistoryFromId, "0");
                                mContentValues.put(mActivity.mDbHelper.mFieldHistoryFromName, "Me");
                                mContentValues.put(mActivity.mDbHelper.mFieldHistoryToId, mArrayListContact.get(position).getUser_id());
                                mContentValues.put(mActivity.mDbHelper.mFieldHistoryToName, mArrayListContact.get(position).getUser_name());
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
                                    String[] mArray = new String[]{mArrayListContact.get(position).getUser_id()};
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAddressBook, mContentValuesUser, mActivity.mDbHelper.mFieldAddressBookUserId + "=?", mArray);
                                    System.out.println("Updated In Local Db");
                                    mActivity.mUtility.errorDialogWithCallBack("Message sent successfully",0, new onAlertDialogCallBack() {
                                        @Override
                                        public void PositiveMethod(DialogInterface dialog, int id) {

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
//                            showBottomSheetConnectDeviceDialog();
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListContact.size();
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
}
