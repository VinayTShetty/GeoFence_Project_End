package com.succorfish.combatdiver.fragments;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.Vo.VoDiverMessage;
import com.succorfish.combatdiver.db.DataHolder;
import com.succorfish.combatdiver.helper.BLEUtility;
import com.succorfish.combatdiver.interfaces.onAlertDialogCallBack;

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

public class FragmentDiverMessage extends Fragment {

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
    ArrayList<VoDiverMessage> mArrayListDiverMessage = new ArrayList<>();
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

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_diver_txt_title));
        mTextViewTitle.setText("Sync Diver messages to device by tapping on SYNC button.\n(Tap on any Message to Edit)");
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        isCalling = true;
        getDBDiverMessageList();
        mActivity.mImageViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VoDiverMessage message = new VoDiverMessage();
                openAddEditDialog(true, message);
            }
        });
        return mViewRoot;
    }

    @OnClick(R.id.frg_surface_msg_textview_sync_msg)
    public void onSyncMessageClick(View mView) {
        if (mActivity.isDevicesConnected) {
            currentSyncPosition=0;
            AsyncTaskDiverMsgSync runner = new AsyncTaskDiverMsgSync();
            runner.execute("");
        } else {
            mActivity.showDisconnectedDeviceAlert();
        }
    }

    private class AsyncTaskDiverMsgSync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            mActivity.showProgress("Syncing..", true);
        }

        @Override
        protected String doInBackground(String... params) {
            syncDiverMessageRequest();
            return "";
        }
        private void syncDiverMessageRequest() {
            if (currentSyncPosition >= mArrayListDiverMessage.size()) {
                mActivity.hideProgress();
                return;
            }
            System.out.println("CALL-" + currentSyncPosition);
            if (mActivity.isDevicesConnected) {
                try {
                    if (currentSyncPosition == 0) {
                        mActivity.startDeviceSync(03);
                    }
                    Thread.sleep(200);
                    System.out.println("STRING-" + mArrayListDiverMessage.get(currentSyncPosition).getDriver_message_id() + "." + mArrayListDiverMessage.get(currentSyncPosition).getMessage().trim());
                    String mHexString = BLEUtility.stringToHex(mArrayListDiverMessage.get(currentSyncPosition).getDriver_message_id() + "." + mArrayListDiverMessage.get(currentSyncPosition).getMessage().trim());
                    System.out.println("HEX STRING-" + mHexString);
                    mActivity.syncCannedSurfaceMessage(Integer.parseInt(mArrayListDiverMessage.get(currentSyncPosition).getDriver_message_id()), BLEUtility.hexStringToBytes(mHexString), false);

                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (currentSyncPosition == mArrayListDiverMessage.size() - 1) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mActivity.stopDeviceSync();
                                        System.out.println("FINISH" + currentSyncPosition);
                                        mActivity.hideProgress();
                                        mActivity.mUtility.errorDialogWithCallBack("Diver message sync successfully", 0, new onAlertDialogCallBack() {
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
                                syncDiverMessageRequest();
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
                        mActivity.showDisconnectedDeviceAlert();
                    }
                });
            }
        }
        @Override
        protected void onPostExecute(String result) {

        }
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

    public void openAddEditDialog(final boolean isAddRecord, final VoDiverMessage mVoDiverMessage) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        View mView = layoutInflaterAndroid.inflate(R.layout.user_input_dialog_box, null);
        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(mActivity,R.style.AppCompatAlertDialogStyle);
        alertDialogBuilderUserInput.setView(mView);
        final EditText mEditTextMsg = (EditText) mView.findViewById(R.id.user_input_dialog_edittext_msg);
        TextView mTextViewTitle = (TextView) mView.findViewById(R.id.user_input_dialog_textview_title);
        if (isAddRecord) {
            mTextViewTitle.setText(getResources().getString(R.string.frg_surface_txt_add_diver_msg));
        } else {
            mEditTextMsg.setText(mVoDiverMessage.getMessage());
            mTextViewTitle.setText(getResources().getString(R.string.frg_surface_txt_edit_diver_msg));
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here
                        mActivity.mUtility.hideKeyboard(mActivity);
                        InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
                        dialogBox.dismiss();
                        String mStringMsg = mEditTextMsg.getText().toString().trim();
                        if (mStringMsg != null && !mStringMsg.equalsIgnoreCase("")) {
                            if (mStringMsg.length() <= 18) {
                                Calendar cal = Calendar.getInstance();
                                Date currentLocalTime = cal.getTime();
                                if (isAddRecord) {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageMSGId, "0");
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageMessage, mStringMsg);
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageCreatedDate, mDateFormatDb.format(currentLocalTime));
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageUpdatedDate, mDateFormatDb.format(currentLocalTime));
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageIsSync, "0");
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageIsEmergency, "0");
                                    int isInsert = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableCannedDiverMessage, mContentValues);
                                    System.out.println("isInsert-" + isInsert);
                                    if (isInsert != -1) {
                                        System.out.println("Added In Local Db");
                                        ContentValues mContentValuesUpdate = new ContentValues();
                                        mContentValuesUpdate.put(mActivity.mDbHelper.mFieldDiverMessageMSGId, isInsert + "");
                                        String[] mArray = new String[]{isInsert + ""};
                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableCannedDiverMessage, mContentValuesUpdate, mActivity.mDbHelper.mFieldDiverMessageId + "=?", mArray);
                                        System.out.println("Updated In Local Db");
                                    } else {
                                        System.out.println("Failed Adding In Local DB");
                                    }
                                } else {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageMessage, mStringMsg);
                                    mContentValues.put(mActivity.mDbHelper.mFieldDiverMessageUpdatedDate, mDateFormatDb.format(currentLocalTime));
                                    String[] mArray = new String[]{mVoDiverMessage.getId()};
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableCannedDiverMessage, mContentValues, mActivity.mDbHelper.mFieldDiverMessageId + "=?", mArray);
                                    System.out.println("Updated In Local Db");
                                }
                                getDBDiverMessageList();
                            } else {
                                mActivity.mUtility.errorDialog("Message should have 18 character only",1);
                            }
                        } else {
                            mActivity.mUtility.errorDialog("Please enter canned message",1);
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                                mActivity.mUtility.hideKeyboard(mActivity);
                                InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
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
            mViewHolder.mTextViewMessage.setText(mArrayListDiverMessage.get(position).getId() + ". " + mArrayListDiverMessage.get(position).getMessage());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListDiverMessage != null) {
                        if (position < mArrayListDiverMessage.size()) {
                            openAddEditDialog(false, mArrayListDiverMessage.get(position));
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
            @BindView(R.id.raw_surface_msg_list_item_textview_message)
            TextView mTextViewMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

}
