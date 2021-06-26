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
import android.widget.TextView;
import android.widget.Toast;

import com.succorfish.eliteoperator.MainActivity;
import com.succorfish.eliteoperator.R;
import com.succorfish.eliteoperator.Vo.VoDiverMessage;
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

public class FragmentSurfaceMessage extends Fragment {

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
    ArrayList<VoDiverMessage> mArrayListSurfaceList = new ArrayList<>();
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

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_surface_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        isCalling = true;
        getDBSurfaceMessageList();

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
            AsyncTaskSurfaceMsgSync runner = new AsyncTaskSurfaceMsgSync();
            runner.execute("");
        } else {
            mActivity.showDisconnectedDeviceAlert(false);
        }
    }

    private class AsyncTaskSurfaceMsgSync extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            mActivity.showProgress("Syncing..", true);
        }

        @Override
        protected String doInBackground(String... params) {
            syncSurfaceMessageRequest();
            return "";
        }
        private void syncSurfaceMessageRequest() {
            if (currentSyncPosition >= mArrayListSurfaceList.size()) {
                mActivity.hideProgress();
                return;
            }
            System.out.println("CALL-" + currentSyncPosition);
            if (mActivity.isDevicesConnected) {
                try {
                    if (currentSyncPosition == 0) {
                        mActivity.startDeviceSync(02);
                    }
                    Thread.sleep(200);
                    System.out.println("STRING-" + mArrayListSurfaceList.get(currentSyncPosition).getSurface_message_id() + "." + mArrayListSurfaceList.get(currentSyncPosition).getMessage().trim());
                    String mHexString = BLEUtility.stringToHex(mArrayListSurfaceList.get(currentSyncPosition).getSurface_message_id() + "." + mArrayListSurfaceList.get(currentSyncPosition).getMessage().trim());
                    System.out.println("HEX STRING-" + mHexString);
                    mActivity.syncCannedSurfaceMessage(Integer.parseInt(mArrayListSurfaceList.get(currentSyncPosition).getSurface_message_id()), BLEUtility.hexStringToBytes(mHexString), true);

                    Timer innerTimer = new Timer();
                    innerTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            if (currentSyncPosition == mArrayListSurfaceList.size() - 1) {
                                mActivity.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mActivity.stopDeviceSync();
                                        System.out.println("FINISH" + currentSyncPosition);
                                        mActivity.hideProgress();
                                        mActivity.mUtility.errorDialogWithCallBack("Surface message sync successfully", 0, false ,new onAlertDialogCallBack() {
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
                                syncSurfaceMessageRequest();
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

    private void getDBSurfaceMessageList() {
        DataHolder mDataHolder;
        mArrayListSurfaceList = new ArrayList<>();
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableCannedSurfaceMessage;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);

            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
                    VoDiverMessage mVoDiverMessage = new VoDiverMessage();
                    mVoDiverMessage.setId(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSurfaceMessageId));
                    mVoDiverMessage.setSurface_message_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSurfaceMessageMSGId));
                    mVoDiverMessage.setMessage(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSurfaceMessageMessage));
                    mVoDiverMessage.setIs_emergency(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSurfaceMessageIsEmergency));
                    mVoDiverMessage.setCreated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSurfaceMessageCreatedDate));
                    mVoDiverMessage.setUpdated_date(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSurfaceMessageUpdatedDate));
                    mVoDiverMessage.setIs_sync(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldSurfaceMessageIsSync));
                    mArrayListSurfaceList.add(mVoDiverMessage);
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
            mTextViewTitle.setText(getResources().getString(R.string.frg_surface_txt_add_canned_msg));
        } else {
            mEditTextMsg.setText(mVoDiverMessage.getMessage());
            mTextViewTitle.setText(getResources().getString(R.string.frg_surface_txt_edit_canned_msg));
        }
        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here
                        String mStringMsg = mEditTextMsg.getText().toString().trim();
                        mActivity.mUtility.hideKeyboard(mActivity);
                        InputMethodManager im = (InputMethodManager) mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(mEditTextMsg.getWindowToken(), 0);
                        dialogBox.dismiss();
                        if (mStringMsg != null && !mStringMsg.equalsIgnoreCase("")) {
                            if (mStringMsg.length() <= 18) {
                                Calendar cal = Calendar.getInstance();
                                Date currentLocalTime = cal.getTime();
                                if (isAddRecord) {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageMSGId, "0");
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageMessage, mStringMsg);
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageCreatedDate, mDateFormatDb.format(currentLocalTime));
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageUpdatedDate, mDateFormatDb.format(currentLocalTime));
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageIsSync, "0");
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageIsEmergency, "0");
                                    int isInsert = mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableCannedSurfaceMessage, mContentValues);
                                    System.out.println("isInsert-" + isInsert);
                                    if (isInsert != -1) {
                                        System.out.println("Added In Local Db");
                                        ContentValues mContentValuesUpdate = new ContentValues();
                                        mContentValuesUpdate.put(mActivity.mDbHelper.mFieldSurfaceMessageMSGId, isInsert + "");
                                        String[] mArray = new String[]{isInsert + ""};
                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableCannedSurfaceMessage, mContentValuesUpdate, mActivity.mDbHelper.mFieldSurfaceMessageId + "=?", mArray);
                                        System.out.println("Updated In Local Db");
                                    } else {
                                        System.out.println("Failed Adding In Local DB");
                                    }
                                } else {
                                    ContentValues mContentValues = new ContentValues();
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageMessage, mStringMsg);
                                    mContentValues.put(mActivity.mDbHelper.mFieldSurfaceMessageUpdatedDate, mDateFormatDb.format(currentLocalTime));
                                    String[] mArray = new String[]{mVoDiverMessage.getId()};
                                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableCannedSurfaceMessage, mContentValues, mActivity.mDbHelper.mFieldSurfaceMessageId + "=?", mArray);
                                    System.out.println("Updated In Local Db");
                                }
                                getDBSurfaceMessageList();
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
            mViewHolder.mTextViewMessage.setText(mArrayListSurfaceList.get(position).getId() + ". " + mArrayListSurfaceList.get(position).getMessage());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListSurfaceList != null) {
                        if (position < mArrayListSurfaceList.size()) {
                            openAddEditDialog(false, mArrayListSurfaceList.get(position));
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListSurfaceList.size();
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
