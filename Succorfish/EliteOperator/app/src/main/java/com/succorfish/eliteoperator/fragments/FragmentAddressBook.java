package com.succorfish.eliteoperator.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.succorfish.eliteoperator.MainActivity;
import com.succorfish.eliteoperator.R;
import com.succorfish.eliteoperator.Vo.VoAddressBook;
import com.succorfish.eliteoperator.db.DataHolder;
import com.succorfish.eliteoperator.helper.BLEUtility;
import com.succorfish.eliteoperator.interfaces.onAlertDialogCallBack;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 19-01-2018.
 */

public class FragmentAddressBook extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    MessageAdapter mMessageAdapter;
    @BindView(R.id.frg_surface_msg_recyclerview_device)
    RecyclerView mRecyclerViewMessage;
    @BindView(R.id.frg_surface_msg_textview_nomsg)
    TextView mTextViewNoMessageFound;
    @BindView(R.id.frg_surface_msg_textview_sync_msg)
    TextView mTextViewSyncMessage;
    @BindView(R.id.frg_surface_msg_title)
    TextView mTextViewTitle;
    ArrayList<VoAddressBook> mArrayListAddressBook = new ArrayList<>();
    private boolean isCalling = true;
    private SimpleDateFormat mDateFormatDb;
    int currentSyncPosition = 0;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_surface_message, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_address_book_txt_title));
        mTextViewTitle.setText("Sync Contacts to device by tapping on SYNC button.\n(Tap on any Contact to Edit)");
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.VISIBLE);
        mTextViewSyncMessage.setText(getResources().getString(R.string.frg_address_book_txt_sync_contact));
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        isCalling = true;
        getDBAddressBookList();
        mActivity.mImageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VoAddressBook mVoAddressBook = new VoAddressBook();
                openAddEditDialog(true, mVoAddressBook);
            }
        });
        return mViewRoot;
    }

    @OnClick(R.id.frg_surface_msg_textview_sync_msg)
    public void onSyncMessageClick(View mView) {
        if (mActivity.isDevicesConnected) {
            currentSyncPosition=0;
            AsyncTaskAddressContactSync runner = new AsyncTaskAddressContactSync();
            runner.execute("");
        } else {
            mActivity.showDisconnectedDeviceAlert(false);
        }
    }

    private class AsyncTaskAddressContactSync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            mActivity.showProgress("Syncing..", true);
        }

        @Override
        protected String doInBackground(String... params) {
            syncAddressBookRequest();
            return "";
        }
        private void syncAddressBookRequest() {
            if (currentSyncPosition >= mArrayListAddressBook.size()) {
                mActivity.hideProgress();
                return;
            }
            System.out.println("CALL-" + currentSyncPosition);
            if (mActivity.isDevicesConnected) {
                try {
                    if (currentSyncPosition == 0) {
                        mActivity.startDeviceSync(01);
                    }
                    Thread.sleep(200);
                    System.out.println("STRING-" + mArrayListAddressBook.get(currentSyncPosition).getUser_id() + "." + mArrayListAddressBook.get(currentSyncPosition).getUser_name().trim());
                    String mHexString = BLEUtility.stringToHex(mArrayListAddressBook.get(currentSyncPosition).getUser_id() + "." + mArrayListAddressBook.get(currentSyncPosition).getUser_name().trim());
                    System.out.println("HEX STRING-" + mHexString);
                    mActivity.syncAddressBookContact(Integer.parseInt(mArrayListAddressBook.get(currentSyncPosition).getUser_id()), BLEUtility.hexStringToBytes(mHexString));
                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (currentSyncPosition == mArrayListAddressBook.size() - 1) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mActivity.stopDeviceSync();
                                        System.out.println("FINISH" + currentSyncPosition);
                                        mActivity.hideProgress();
                                        mActivity.mUtility.errorDialogWithCallBack("Address contact sync successfully", 0,false , new onAlertDialogCallBack() {
                                            @Override
                                            public void PositiveMethod(DialogInterface dialog, int id) {

                                            }

                                            @Override
                                            public void NegativeMethod(DialogInterface dialog, int id) {

                                            }
                                        });
                                    }
                                });
                            } else {
                                currentSyncPosition++;
                                syncAddressBookRequest();
                            }
                        }
                    }, 250);
                } catch (Exception e) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mActivity.stopDeviceSync();
                            mActivity.hideProgress();
                        }
                    });
                    e.printStackTrace();
                }
            } else {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.hideProgress();
                        mActivity.showDisconnectedDeviceAlert(false);
                    }
                });
            }
        }
        @Override
        protected void onPostExecute(String result) {

        }
    }

    private void getDBAddressBookList() {
//        int tableGroupCount = mActivity.mDbHelper.getTableCount(mActivity.mDbHelper.mTableGroup);
        DataHolder mDataHolder;
        mArrayListAddressBook = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableAddressBook;
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
                    mArrayListAddressBook.add(mVoAddressBook);
                }
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
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public void openAddEditDialog(final boolean isAddRecord, final VoAddressBook mVoAddressBook) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mActivity,R.style.AppCompatAlertDialogStyle);
        alertDialogBuilderUserInput.setView(mView);
        final EditText mEditTextUserName = (EditText) mView.findViewById(R.id.user_input_dialog_edittext_msg);
        TextView mTextViewTitle = (TextView) mView.findViewById(R.id.user_input_dialog_textview_title);
        mEditTextUserName.setHint("Please enter name");
        if (isAddRecord) {
            mTextViewTitle.setText(getResources().getString(R.string.frg_address_book_txt_add_contact));
        } else {
            mTextViewTitle.setText(getResources().getString(R.string.frg_address_book_txt_edit_contact));
            mEditTextUserName.setText(mVoAddressBook.getUser_name());
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here
                        mActivity.mUtility.hideKeyboard(mActivity);
                        InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(mEditTextUserName.getWindowToken(), 0);
                        dialogBox.dismiss();
                        String mStringUserName = mEditTextUserName.getText().toString().trim();

                        if (mStringUserName != null && !mStringUserName.equalsIgnoreCase("")) {
                            if (mStringUserName.length() <= 8) {
                                Calendar cal = Calendar.getInstance();
                                Date currentLocalTime = cal.getTime();
                                if (isAddRecord) {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookUserId, "0");
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookUserName, mStringUserName);
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookUserPhoto, "");
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookDeviceId, "");
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookUserType, "0");
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookLastMessageId, "");
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookLastMessageName, "");
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookLastMessageTime, "");
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookCreatedDate, mDateFormatDb.format(currentLocalTime));
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookUpdatedDate, mDateFormatDb.format(currentLocalTime));
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookIsSync, "0");
                                    int isInsert = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableAddressBook, mContentValues);
                                    System.out.println("isInsert-" + isInsert);
                                    if (isInsert != -1) {
                                        System.out.println("Added In Local Db");
                                        ContentValues mContentValuesUpdate = new ContentValues();
                                        mContentValuesUpdate.put(mActivity.mDbHelper.mFieldAddressBookUserId, isInsert + "");
                                        String[] mArray = new String[]{isInsert + ""};
                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAddressBook, mContentValuesUpdate, mActivity.mDbHelper.mFieldAddressBookId + "=?", mArray);
                                        System.out.println("Updated In Local Db");
                                    } else {
                                        System.out.println("Failed Adding In Local DB");
                                    }
                                } else {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookUserName, mStringUserName);
                                    mContentValues.put(mActivity.mDbHelper.mFieldAddressBookUpdatedDate, mDateFormatDb.format(currentLocalTime));
                                    String[] mArray = new String[]{mVoAddressBook.getId()};
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableAddressBook, mContentValues, mActivity.mDbHelper.mFieldAddressBookId + "=?", mArray);
                                    System.out.println("Updated In Local Db");
                                }
                                getDBAddressBookList();
                            } else {
                                mActivity.mUtility.errorDialog("Name should have 8 character only",1);
                            }
                        } else {
                            mActivity.mUtility.errorDialog("Please enter contact name",1);
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                mActivity.mUtility.hideKeyboard(mActivity);
                                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                im.hideSoftInputFromWindow(mEditTextUserName.getWindowToken(), 0);
                                dialogBox.cancel();
                            }
                        });

        AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
    }

    public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

        @Override
        public MessageAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_surface_msg_list_item, parent, false);
            return new MessageAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MessageAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mImageViewContact.setImageResource(R.drawable.ic_diver_default_small);
            mViewHolder.mTextViewMessage.setText(mArrayListAddressBook.get(position).getUser_id() + ". " + mArrayListAddressBook.get(position).getUser_name());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListAddressBook != null) {
                        if (position < mArrayListAddressBook.size()) {
                            openAddEditDialog(false, mArrayListAddressBook.get(position));
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListAddressBook.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_surface_msg_list_item_textview_message)
            TextView mTextViewMessage;
            @BindView(R.id.raw_surface_msg_list_item_imageview)
            ImageView mImageViewContact;
            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
