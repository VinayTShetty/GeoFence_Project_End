package com.succorfish.depthntemp.fragnments;
//
//import android.os.Bundle;
//import android.support.annotation.Nullable;
//import android.support.v4.app.Fragment;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.succorfish.depthntemp.MainActivity;
//import com.succorfish.depthntemp.R;
//import com.succorfish.depthntemp.vo.VoBluetoothDevices;
//
//import java.util.ArrayList;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import butterknife.Unbinder;
//
//public class FragmentDeviceMissionList extends Fragment {
//    MainActivity mActivity;
//    View mViewRoot;
//    private Unbinder unbinder;
//    @BindView(R.id.fragment_mission_list_recyclerview)
//    RecyclerView mRecyclerView;
//    @BindView(R.id.fragment_mission_list_textview_nodevice)
//    TextView mTextViewNoDeviceFound;
//    @BindView(R.id.fragment_mission_list_relativelayout_nodevice)
//    RelativeLayout mRelativeLayoutNoDevice;
//    ArrayList<VoBluetoothDevices> mArrayListAddDevice = new ArrayList<>();
//    DeviceMissionListAdapter mDeviceMissionListAdapter;
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mActivity = (MainActivity) getActivity();
//
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        mViewRoot = inflater.inflate(R.layout.fragment_mission_list, container, false);
//        unbinder = ButterKnife.bind(this, mViewRoot);
//        mActivity.mTextViewTitle.setText(R.string.str_mission);
//        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
//        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
//        mArrayListAddDevice = new ArrayList<>();
//        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                mActivity.onBackPressed();
//            }
//        });
//
//        mDeviceMissionListAdapter = new DeviceMissionListAdapter();
//        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        mRecyclerView.setAdapter(mDeviceMissionListAdapter);
//        mDeviceMissionListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                checkAdapterIsEmpty();
//            }
//        });
//        checkAdapterIsEmpty();
//
////        if (mDeviceListAdapter == null) {
////            mDeviceListAdapter = new DeviceListAdapter();
////            LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
////            mRecyclerView.setLayoutManager(mLayoutManager);
////            mRecyclerView.setAdapter(mDeviceListAdapter);
////            mDeviceListAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
////                @Override
////                public void onChanged() {
////                    super.onChanged();
////                    checkAdapterIsEmpty();
////                }
////            });
////            checkAdapterIsEmpty();
////        } else {
////            mDeviceListAdapter.notifyDataSetChanged();
////        }
////        checkAdapterIsEmpty();
//        return mViewRoot;
//    }
//
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
////        mArrayListAddDevice = null;
//        mRecyclerView.setAdapter(null);
//        unbinder.unbind();
//        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//    }
//
//    @Override
//    public void onPause() {
//        super.onPause();
//    }
//
//
//    private void checkAdapterIsEmpty() {
//        if (isAdded()) {
//            if (mDeviceMissionListAdapter != null) {
//                if (mDeviceMissionListAdapter.getItemCount() > 0) {
//                    mRelativeLayoutNoDevice.setVisibility(View.GONE);
//                } else {
//                    mRelativeLayoutNoDevice.setVisibility(View.VISIBLE);
//                }
//            } else {
//                mRelativeLayoutNoDevice.setVisibility(View.VISIBLE);
//            }
//        }
//    }
//
//    public class DeviceMissionListAdapter extends RecyclerView.Adapter<DeviceMissionListAdapter.ViewHolder> {
//
//        @Override
//        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_device_mission_list_item, parent, false);
//            return new DeviceMissionListAdapter.ViewHolder(itemView);
//        }
//
//        @Override
//        public void onBindViewHolder(ViewHolder holder, final int position) {
//            holder.mTextViewMissionName.setText("Mission " + position);
////            if (mArrayListAddDevice.get(position).getDeviceIEEE() != null && !mArrayListAddDevice.get(position).getDeviceIEEE().equalsIgnoreCase("")) {
////                holder.mTextViewDeviceId.setText(mArrayListAddDevice.get(position).getDeviceIEEE().replace(":", ""));
////            } else {
////                holder.mTextViewDeviceId.setText("");
////            }
////            holder.mTextViewDeviceId.setTextColor(getResources().getColor(R.color.colorWhite));
////            if (mArrayListAddDevice.get(position).getDeviceName() != null && !mArrayListAddDevice.get(position).getDeviceName().equalsIgnoreCase("")) {
////                holder.mTextViewDeviceName.setText(mArrayListAddDevice.get(position).getDeviceName());
////            }
//            holder.itemView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
////                    try {
////                        if (mArrayListAddDevice != null) {
////                            if (position < mArrayListAddDevice.size()) {
////                                FragmentDeviceList mFragmentDeviceList = new FragmentDeviceList();
////                                Bundle mBundle = new Bundle();
////                                mActivity.replacesFragment(mFragmentDeviceList, true, mBundle, 0);
////                            }
////                        }
////                    } catch (Exception e) {
////                        e.printStackTrace();
////                    }
//                    FragmentDeviceMissionDetail mFragmentDeviceMissionDetail = new FragmentDeviceMissionDetail();
//                    Bundle mBundle = new Bundle();
//                    mBundle.putString("intent_title", "Mission " + position);
//                    mActivity.replacesFragment(mFragmentDeviceMissionDetail, true, mBundle, 0);
//
//                }
//            });
//        }
//
//        @Override
//        public int getItemCount() {
//            return 10;
//        }
//
//        public class ViewHolder extends RecyclerView.ViewHolder {
//            @BindView(R.id.raw_mission_item_textview_mission_name)
//            TextView mTextViewMissionName;
//            @BindView(R.id.raw_mission_item_textview_mission_time)
//            TextView mTextViewMissionTime;
//
//            public ViewHolder(View itemView) {
//                super(itemView);
//                ButterKnife.bind(this, itemView);
//            }
//        }
//    }
//}
