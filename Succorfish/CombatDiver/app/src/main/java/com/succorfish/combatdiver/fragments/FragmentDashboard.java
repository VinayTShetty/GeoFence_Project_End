package com.succorfish.combatdiver.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.succorfish.combatdiver.MainActivity;
import com.succorfish.combatdiver.R;
import com.succorfish.combatdiver.Vo.VoAddressBook;
import com.succorfish.combatdiver.Vo.VoBluetoothDevices;
import com.succorfish.combatdiver.db.DataHolder;
import com.succorfish.combatdiver.interfaces.onDeviceConnectionStatusChange;
import com.succorfish.combatdiver.interfaces.onDeviceMessegeSent;
import com.succorfish.combatdiver.views.RippleBackground;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Jaydeep on 16-01-2018.
 */

public class FragmentDashboard extends Fragment {
    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;
    @BindView(R.id.frg_dashboard_relativelayout_connect_device)
    RelativeLayout mRelativeLayoutConnectDevice;
    @BindView(R.id.frg_dashboard_relativelayout_battery)
    RelativeLayout mRelativeLayoutBattery;
    @BindView(R.id.frg_dashboard_relativelayout_surface_setup)
    RelativeLayout mRelativeLayoutSurfaceSetup;
    @BindView(R.id.frg_dashboard_relativelayout_compass)
    RelativeLayout mRelativeLayoutCompass;
    @BindView(R.id.frg_dashboard_recyclerview_device)
    RecyclerView mRecyclerViewDevice;

    @BindView(R.id.frg_dashboard_imageview_connect_device)
    CircleImageView mCircleImageViewConnect;

    @BindView(R.id.frg_dashboard_textview_connect_device)
    TextView mTextViewConnectDevice;
    @BindView(R.id.frg_dashboard_textview_nodevice)
    TextView mTextViewNoDeviceFound;
    @BindView(R.id.frg_dashboard_imageView_scanning)
    RippleBackground mRippleBackground;
    @BindView(R.id.frg_dashboard_relativelayout_list)
    RelativeLayout mRelativeLayoutList;

    ArrayList<VoAddressBook> mArrayListDeviceList = new ArrayList<>();
    DeviceUserAdapter mDeviceUserAdapter;
    private boolean isCalling = true;
    private Handler handlerAnimation;
    private Runnable runnableAnimation;

    private SimpleDateFormat mDateFormatDb;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mDateFormatDb = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    }
    String lat="12.7644933",longi="77.5622606";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_dashboard, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mTextViewTitle.setText(getResources().getString(R.string.frg_dashboard_txt_title));
        mActivity.mImageViewDrawer.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mImageViewPerson.setVisibility(View.VISIBLE);
        mActivity.mTextViewActiveCount.setVisibility(View.VISIBLE);
        mActivity.mImageViewAdd.setVisibility(View.GONE);

        if (mActivity.isDevicesConnected) {
            if (mActivity.mBluetoothDevice != null) {
                mTextViewConnectDevice.setText("Connected to " + mActivity.mBluetoothDevice.getName());
            } else {
                mTextViewConnectDevice.setText("Connected");
            }

            mTextViewConnectDevice.setTextColor(getResources().getColor(R.color.colorRed));
            mCircleImageViewConnect.setImageResource(R.drawable.ic_disconnected_icon);
        } else {
            mTextViewConnectDevice.setText(getResources().getString(R.string.frg_dashboard_txt_connect_device));
            mTextViewConnectDevice.setTextColor(getResources().getColor(R.color.colorGreenActive));
            mCircleImageViewConnect.setImageResource(R.drawable.ic_connected_icon);
        }
        handlerAnimation = new Handler();
        runnableAnimation = new Runnable() {
            @Override
            public void run() {
                System.out.println("CALLLL");
                if (isAdded()) {
                    mRippleBackground.stopRippleAnimation();
                    mRippleBackground.setVisibility(View.GONE);
                    mRelativeLayoutList.setVisibility(View.VISIBLE);
                }
            }
        };
        handlerAnimation.postDelayed(runnableAnimation, 5000);

        isCalling = true;
        getDBDeviceList();
        mRippleBackground.startRippleAnimation();
        mActivity.setOnDeviceMessageSent(new onDeviceMessegeSent() {
            @Override
            public void onMessegeSentRefreshUI() {
                if (isAdded()) {
                    isCalling = true;
                    getDBDeviceList();
                }
            }
        });
        mActivity.setOnDevicesStatusChange(new onDeviceConnectionStatusChange() {
            @Override
            public void addScanDevices() {

            }

            @Override
            public void addScanDevices(VoBluetoothDevices mVoBluetoothDevices) {

            }

            @Override
            public void onConnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                    if (connectedDevice != null) {
                        mTextViewConnectDevice.setText("Connected to " + devicesName);
                    } else {
                        mTextViewConnectDevice.setText("Connected");
                    }
                    mTextViewConnectDevice.setTextColor(getResources().getColor(R.color.colorRed));
                    mCircleImageViewConnect.setImageResource(R.drawable.ic_disconnected_icon);
                }
            }

            @Override
            public void onDisconnect(BluetoothGattService service, String devicesName, BluetoothDevice connectedDevice) {
                if (isAdded()) {
                    mTextViewConnectDevice.setText(getResources().getString(R.string.frg_dashboard_txt_connect_device));
                    mTextViewConnectDevice.setTextColor(getResources().getColor(R.color.colorGreenActive));
                    mCircleImageViewConnect.setImageResource(R.drawable.ic_connected_icon);
                }
            }

            @Override
            public void onError() {

            }
        });
        mActivity.mTextViewActiveCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return mViewRoot;
    }

    @OnClick(R.id.frg_dashboard_relativelayout_connect_device)
    public void onDeviceConnectClick(View mView) {
        if (mActivity.isDevicesConnected) {
            if (mActivity.mBluetoothDevice != null) {
                System.out.println("mBluetoothDevice NOT NULL");
                mActivity.isManualDisconnect = true;
                mActivity.disconnectDevices(mActivity.mBluetoothDevice);
            } else {
                System.out.println("mBluetoothDevice NULL");
            }
//            FragmentDeviceConnect mFragmentDeviceConnect = new FragmentDeviceConnect();
//            Bundle mBundle = new Bundle();
//            mBundle.putBoolean("intent_is_from_device_setup", false);
//            mActivity.replacesFragment(mFragmentDeviceConnect, true, mBundle, 1);
        } else {
            FragmentDeviceConnect mFragmentDeviceConnect = new FragmentDeviceConnect();
            Bundle mBundle = new Bundle();
            mBundle.putBoolean("intent_is_from_device_setup", false);
            mActivity.replacesFragment(mFragmentDeviceConnect, true, mBundle, 1);
        }
    }

    @OnClick(R.id.frg_dashboard_relativelayout_battery)
    public void onBatteryClick(View mView) {
//        mActivity.connectDeviceWithProgress();
        final Intent intent=new Intent();
        intent.setAction("com.succorfish.eliteoperator");
        intent.putExtra("succorfish_latitude","12.7644933");
        intent.putExtra("succorfish_longitude","77.5622606");
//        intent.setComponent(new ComponentName("com.succorfish.locationreciver","com.succorfish.locationreciver.MyBroadcastReceiver"));
        mActivity.sendBroadcast(intent);


    }

    @OnClick(R.id.frg_dashboard_relativelayout_compass)
    public void onCompassClick(View mView) {
        FragmentCompass mFragmentCompass = new FragmentCompass();
        mActivity.replacesFragment(mFragmentCompass, true, null, 1);
    }

    @OnClick(R.id.frg_dashboard_relativelayout_surface_setup)
    public void onSurfaceSetupClick(View mView) {

        if (mActivity.isDevicesConnected) {
            FragmentSurfaceSetup mFragmentSurfaceSetup = new FragmentSurfaceSetup();
            Bundle mBundle = new Bundle();
            mBundle.putBoolean("intent_is_from_setting", false);
            mActivity.replacesFragment(mFragmentSurfaceSetup, true, mBundle, 1);
        } else {
            FragmentDeviceConnect mFragmentDeviceConnect = new FragmentDeviceConnect();
            Bundle mBundle = new Bundle();
            mBundle.putBoolean("intent_is_from_device_setup", true);
            mActivity.replacesFragment(mFragmentDeviceConnect, true, null, 1);
        }
    }

    private void getDBDeviceList() {
        DataHolder mDataHolder;
        mArrayListDeviceList = new ArrayList<>();
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
                    mVoAddressBook.setAir_life_percentage("50");
                    mArrayListDeviceList.add(mVoAddressBook);
                }
                if (mDataHolder.get_Listholder().size() > 0) {
                    mActivity.mTextViewActiveCount.setText(mDataHolder.get_Listholder().size() + "");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDeviceUserAdapter = new DeviceUserAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerViewDevice.setLayoutManager(mLayoutManager);
        mRecyclerViewDevice.setAdapter(mDeviceUserAdapter);
        mDeviceUserAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
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
        if (mDeviceUserAdapter.getItemCount() == 0) {
            mTextViewNoDeviceFound.setVisibility(View.VISIBLE);
            mRecyclerViewDevice.setVisibility(View.GONE);
        } else {
            mTextViewNoDeviceFound.setVisibility(View.GONE);
            mRecyclerViewDevice.setVisibility(View.VISIBLE);
        }
    }

    public class DeviceUserAdapter extends RecyclerView.Adapter<DeviceUserAdapter.ViewHolder> {

        @Override
        public DeviceUserAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_device_list_item, parent, false);
            return new DeviceUserAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final DeviceUserAdapter.ViewHolder mViewHolder, final int position) {
            if (isAdded()) {
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
                if (mArrayListDeviceList.get(position).getUser_name().contains("Boat")){
                    Glide.with(mActivity)
                            .load(mArrayListDeviceList.get(position).getUser_photo())
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade()
                            .dontAnimate()
                            .placeholder(R.drawable.ic_boat_icon)
                            .into(mViewHolder.mImageViewDeviceIcon);
                }else {
                    Glide.with(mActivity)
                            .load(mArrayListDeviceList.get(position).getUser_photo())
                            .centerCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .crossFade()
                            .dontAnimate()
                            .placeholder(R.drawable.ic_diver_default)
                            .into(mViewHolder.mImageViewDeviceIcon);
                }

                mViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mArrayListDeviceList != null) {
                            if (position < mArrayListDeviceList.size()) {
                                FragmentSentMessage mFragmentMessage = new FragmentSentMessage();
                                Bundle mBundle = new Bundle();
                                mBundle.putBoolean("intent_is_from_history", false);
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        if (handlerAnimation != null) {
            System.out.println("HandlerAnimCancel");
            handlerAnimation.removeCallbacks(runnableAnimation);
        }
    }

}
