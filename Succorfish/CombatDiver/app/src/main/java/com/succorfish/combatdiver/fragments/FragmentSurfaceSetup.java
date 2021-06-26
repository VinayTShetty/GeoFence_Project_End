package com.succorfish.combatdiver.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.Vo.VoAddressBook;
import com.succorfish.combatdiver.Vo.VoDiverMessage;
import com.succorfish.combatdiver.db.DataHolder;
import com.succorfish.combatdiver.helper.BLEUtility;
import com.succorfish.combatdiver.interfaces.onAlertDialogCallBack;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 06-02-2018.
 */

public class FragmentSurfaceSetup extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_surface_setup_recyclerview)
    RecyclerView mRecyclerViewDevice;
    ArrayList<String> mArrayListMenu = new ArrayList<>();
    SetupSurfaceAdapter mSetupSurfaceAdapter;
    Dialog myDialog;
    private TextView mTextViewNoContactFound;
    private RecyclerView mRecyclerViewContact;
    ArrayList<VoAddressBook> mArrayListAddressBook = new ArrayList<>();
    ContactAdapter mContactAdapter;
    boolean isFromSetting = false;
    ArrayList<VoDiverMessage> mArrayListSurfaceList = new ArrayList<>();
    ArrayList<VoDiverMessage> mArrayListDiverMessage = new ArrayList<>();
    int currentSyncPosition = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            isFromSetting = getArguments().getBoolean("intent_is_from_setting", false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_surface_setup, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_surface_setup_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.GONE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewPerson.setVisibility(View.GONE);
        mActivity.mTextViewActiveCount.setVisibility(View.GONE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mArrayListMenu = new ArrayList<>();
        mArrayListMenu.add("Sync Address Book");
        mArrayListMenu.add("Sync Surface Mode Messages");
        mArrayListMenu.add("Sync Diver Mode Messages");
        mArrayListMenu.add("Set Time");
        mArrayListMenu.add("Sync Modem Id");
        mArrayListMenu.add("Request Modem Id");
        mArrayListMenu.add("Get Location");
        mSetupSurfaceAdapter = new SetupSurfaceAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewDevice.setLayoutManager(mLayoutManager);
        mRecyclerViewDevice.setAdapter(mSetupSurfaceAdapter);

        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });
        getDbAddressContactList();
        getDbSurfaceMessage();
        getDbDiverMessageList();
        return mViewRoot;
    }

    //    private void syncAddressContact() {
//        DataHolder mDataHolder;
//
//        try {
//            String url = "select * from " + mActivity.mDbHelper.mTableAddressBook;
//            System.out.println("Local url " + url);
//            mDataHolder = mActivity.mDbHelper.read(url);
//            if (mDataHolder != null) {
//                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
//                mActivity.showProgress("Syncing..", true);
//                mActivity.startDeviceSync(01);
//                Thread.sleep(200);
//                for (int i = 0; i < mDataHolder.get_Listholder().size(); i++) {
//                    VoAddressBook mVoAddressBook = new VoAddressBook();
//                    mVoAddressBook.setId(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookId));
//                    mVoAddressBook.setUser_id(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookUserId));
//                    mVoAddressBook.setUser_name(mDataHolder.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldAddressBookUserName));
//                    System.out.println("STRING-" + mVoAddressBook.getUser_id() + "." + mVoAddressBook.getUser_name().trim());
//                    String mHexString = BLEUtility.stringToHex(mVoAddressBook.getUser_id() + "." + mVoAddressBook.getUser_name().trim());
//                    System.out.println("HEX STRING-" + mHexString);
//                    mActivity.syncAddressBookContact(Integer.parseInt(mVoAddressBook.getUser_id()), BLEUtility.hexStringToBytes(mHexString));
//                    Thread.sleep(200);
//                }
//                Thread.sleep(200);
//                mActivity.stopDeviceSync();
//                mActivity.hideProgress();
//     mActivity.mUtility.errorDialogWithCallBack("Address contact sync successfully",0, new onAlertDialogCallBack() {
//        @Override
//        public void PositiveMethod(DialogInterface dialog, int id) {
//
//        }
//
//        @Override
//        public void NegativeMethod(DialogInterface dialog, int id) {
//
//        }
//    });
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    private void getDbAddressContactList() {
        DataHolder mDataHolder;
        mArrayListAddressBook = new ArrayList<>();
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
                    mArrayListAddressBook.add(mVoAddressBook);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDbSurfaceMessage() {
        mArrayListSurfaceList = new ArrayList<>();
        final DataHolder mDataHolder;
        try {
            String url = "select * from " + mActivity.mDbHelper.mTableCannedSurfaceMessage;
            System.out.println("Local url " + url);
            mDataHolder = mActivity.mDbHelper.read(url);
            if (mDataHolder != null) {
                System.out.println("Local Device List " + url + " : " + mDataHolder.get_Listholder().size());
                for (int j = 0; j < mDataHolder.get_Listholder().size(); j++) {
                    final int i = j;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDbDiverMessageList() {
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
                                        mActivity.mUtility.errorDialogWithCallBack("Address contact sync successfully", 0, new onAlertDialogCallBack() {
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
                        mActivity.showDisconnectedDeviceAlert();
                    }
                });
            }
        }

        @Override
        protected void onPostExecute(String result) {
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
                                        mActivity.mUtility.errorDialogWithCallBack("Surface message sync successfully", 0, new onAlertDialogCallBack() {
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
                        mActivity.showDisconnectedDeviceAlert();
                    }
                });
            }
        }

        @Override
        protected void onPostExecute(String result) {

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

    public class SetupSurfaceAdapter extends RecyclerView.Adapter<SetupSurfaceAdapter.ViewHolder> {

        @Override
        public SetupSurfaceAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_setup_surface_list_item, parent, false);
            return new SetupSurfaceAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final SetupSurfaceAdapter.ViewHolder mViewHolder, final int position) {
            if (position == 0) {
                mViewHolder.mImageViewIcon.setImageResource(R.drawable.ic_addres_book);
            } else if (position == 1) {
                mViewHolder.mImageViewIcon.setImageResource(R.drawable.ic_messages);
            } else if (position == 2) {
                mViewHolder.mImageViewIcon.setImageResource(R.drawable.ic_sync_diver);
            } else if (position == 3) {
                mViewHolder.mImageViewIcon.setImageResource(R.drawable.ic_set_time);
            } else if (position == 4) {
                mViewHolder.mImageViewIcon.setImageResource(R.drawable.ic_modem);
            } else if (position == 5) {
                mViewHolder.mImageViewIcon.setImageResource(R.drawable.ic_request_modem);
            }
            mViewHolder.mTextViewDeviceName.setText(mArrayListMenu.get(position));
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mActivity.isDevicesConnected) {
                        if (position == 0) { // Contact
                            currentSyncPosition = 0;
                            AsyncTaskAddressContactSync runner = new AsyncTaskAddressContactSync();
                            runner.execute("");
                        } else if (position == 1) {// SurfaceMessage
                            currentSyncPosition = 0;
                            AsyncTaskSurfaceMsgSync runner = new AsyncTaskSurfaceMsgSync();
                            runner.execute("");
                        } else if (position == 2) {// DiverMessage
                            currentSyncPosition = 0;
                            AsyncTaskDiverMsgSync runner = new AsyncTaskDiverMsgSync();
                            runner.execute("");
                        } else if (position == 3) {// Set Time
                            Calendar aGMTCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                            long currentTimeMillis = aGMTCalendar.getTimeInMillis() / 1000L;
                            String hexTime = Long.toHexString(currentTimeMillis);

                            // Reverse the hex string.
                            // Hex to int
                            int mInt = (int) Long.parseLong(hexTime, 16);
                            int mIntReverse = ((mInt >> 24) & 0xff) |       // byte 3 to byte 0
                                    ((mInt << 8) & 0xff0000) |    // byte 1 to byte 2
                                    ((mInt >> 8) & 0xff00) |      // byte 2 to byte 1
                                    ((mInt << 24) & 0xff000000);   // byte 0 to byte 3
                            String mStrReverseHex = Integer.toHexString(mIntReverse);
                            System.out.println("UTC-HEXX-" + hexTime);
                            System.out.println("UTC-HEXX-Reverse-" + mStrReverseHex);
                            System.out.println("UTC-currentTimeMillis-" + currentTimeMillis);
                            StringBuilder result = new StringBuilder();
                            for (int i = 0; i <= hexTime.length() - 2; i = i + 2) {
                                result.append(new StringBuilder(hexTime.substring(i, i + 2)).reverse());
                            }
                            System.out.println("UTC - HEXX-2-" + result.reverse().toString());
                            mActivity.setDeviceTime(BLEUtility.hexStringToBytes(mStrReverseHex));

                        } else if (position == 4) {
                            showContactPopup();
                        } else if (position == 5) { //Request ModemID
                            mActivity.setRequestModem(BLEUtility.intToByte(01));
                        } else if (position == 6) {
                            mActivity.getCurrentLocation();

                        }
                    } else {
//                        mActivity.onBackPressed();
                        mActivity.showDisconnectedDeviceAlert();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListMenu.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_setup_surface_list_item_textview_name)
            TextView mTextViewDeviceName;
            @BindView(R.id.raw_setup_surface_list_item_imageview)
            ImageView mImageViewIcon;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mActivity.isDevicesConnected) {
            if (mActivity.mBluetoothDevice != null) {
                System.out.println("mBluetoothDevice NOT NULL");
                mActivity.isManualDisconnect = true;
                mActivity.disconnectDevices(mActivity.mBluetoothDevice);
            } else {
                System.out.println("mBluetoothDevice NULL");
            }
        }
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        unbinder.unbind();
    }

    private void showContactPopup() {
        if (myDialog != null && myDialog.isShowing()) {
            return;
        }
        myDialog = new Dialog(mActivity);
        myDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialog.setContentView(R.layout.popup_contact);
        myDialog.setCancelable(false);
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        TextView mTextViewSendToAll = (TextView) myDialog
                .findViewById(R.id.poopup_contact_textview_send_to_all);
        mTextViewNoContactFound = (TextView) myDialog
                .findViewById(R.id.poopup_contact_textview_nocontact);
        TextView mTextViewDone = (TextView) myDialog.findViewById(R.id.poopup_contact_textview_done);
        mRecyclerViewContact = (RecyclerView) myDialog.findViewById(R.id.poopup_contact_recyclerview_contact);
        mTextViewSendToAll.setVisibility(View.GONE);
        mTextViewDone.setText("Close");
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
        mTextViewDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(myDialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        myDialog.show();
        myDialog.getWindow().setAttributes(lp);
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

    public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

        @Override
        public ContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_message_list_item, parent, false);
            return new ContactAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final ContactAdapter.ViewHolder mViewHolder, final int position) {
            mViewHolder.mTextViewMessage.setText(mArrayListAddressBook.get(position).getUser_id() + ". " + mArrayListAddressBook.get(position).getUser_name());
            mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mArrayListAddressBook != null) {
                        if (position < mArrayListAddressBook.size()) {
                            if (myDialog != null) {
                                myDialog.dismiss();
                                if (mActivity.isDevicesConnected) {
                                    mActivity.setSyncModem(BLEUtility.intToByte(Integer.parseInt(mArrayListAddressBook.get(position).getUser_id())));

                                }
                            }
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
            @BindView(R.id.raw_message_list_item_textview_message)
            TextView mTextViewMessage;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }
}
