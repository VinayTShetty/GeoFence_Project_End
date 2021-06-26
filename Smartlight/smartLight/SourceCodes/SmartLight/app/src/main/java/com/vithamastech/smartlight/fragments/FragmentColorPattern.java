package com.vithamastech.smartlight.fragments;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.graphics.BlurMaskFilter;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.evergreen.ble.advertisement.ManufactureData;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoPattern;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 25-12-2017.
 */

public class FragmentColorPattern extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_color_pattern_recyclerview)
    RecyclerView mRecyclerView;
    ArrayList<VoPattern> mArrayListPattern = new ArrayList<>();
    PatternListAdapter mPatternListAdapter;
    int mIntRandomNo = 0;
    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;
    String mStringLocalId = "";
    String mStringServerId = "";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        if (getArguments() != null) {
            mIntRandomNo = getArguments().getInt("intent_device_id", 0);
            mIsFromGroup = getArguments().getBoolean("intent_from_group", false);
            mIsFromAllGroup = getArguments().getBoolean("intent_from_all_group", false);
            if (!mIsFromAllGroup) {
                mStringLocalId = getArguments().getString("intent_local_id");
                mStringServerId = getArguments().getString("intent_server_id");
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_color_pattern, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mRecyclerView.setVisibility(View.VISIBLE);
        mArrayListPattern = new ArrayList<>();
        mActivity.mSwitchCompatOnOff.setOnCheckedChangeListener(powerChange);
        VoPattern mVoPattern;
        for (int i = 1; i <= 5; i++) {
            mVoPattern = new VoPattern();
            mVoPattern.setPattern_value(i + "");
            mVoPattern.setIsChecked(false);
            if (i == 1) {
                mVoPattern.setPattern_name("Dance Party");
                mVoPattern.setPattern_image(R.drawable.pattern_one_bg);
            } else if (i == 2) {
                mVoPattern.setPattern_name("Love Romance");
                mVoPattern.setPattern_image(R.drawable.pattern_two_bg);
            } else if (i == 3) {
                mVoPattern.setPattern_name("Soothing");
                mVoPattern.setPattern_image(R.drawable.pattern_three_bg);
            } else if (i == 4) {
                mVoPattern.setPattern_name("Strobe");
                mVoPattern.setPattern_image(R.drawable.pattern_four_bg);
            } else if (i == 5) {
                mVoPattern.setPattern_name("Disco Strobe");
                mVoPattern.setPattern_image(R.drawable.pattern_five_bg);
            } else {
                mVoPattern.setPattern_image(R.drawable.pattern_one_bg);
            }
            mArrayListPattern.add(mVoPattern);
        }
        mPatternListAdapter = new PatternListAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mPatternListAdapter);
        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });
        return mViewRoot;
    }

    /* Pattern List adapter*/
    public class PatternListAdapter extends RecyclerView.Adapter<PatternListAdapter.ViewHolder> {

        @Override
        public PatternListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_color_pattern_list_item, parent, false);
            return new PatternListAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(PatternListAdapter.ViewHolder holder, final int position) {

            holder.mTextViewPatternName.setText(mArrayListPattern.get(position).getPattern_name());
            applyBlurMaskFilter(holder.mTextViewPatternName, BlurMaskFilter.Blur.INNER);
            if (mArrayListPattern.get(position).getIsChecked()) {
                holder.mTextViewPatternName.setTextColor(getResources().getColor(R.color.colorAppTheam));
                holder.mImageViewPatternSelector.setImageResource(0);
                holder.mImageViewPatternSelector.setBackgroundColor(getResources().getColor(R.color.colorSelectorTransparent));
            } else {
                holder.mTextViewPatternName.setTextColor(getResources().getColor(R.color.colorWhite));
                holder.mImageViewPatternSelector.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
//                holder.mImageViewPatternSelector.setImageResource(0);
                if (mActivity.getIsSDKAbove21()) {
                    holder.mImageViewPatternSelector.setImageResource(R.drawable.gradient_pattern);
                } else {
                    holder.mImageViewPatternSelector.setImageResource(0);
                }
            }
            if (mActivity.getIsSDKAbove21()) {
                holder.mCardView.setMaxCardElevation(getResources().getDimension(R.dimen._10sdp));
                holder.mCardView.setRadius(getResources().getDimension(R.dimen._8sdp));
            } else {
                holder.mCardView.setMaxCardElevation(0f);
                holder.mCardView.setRadius(0f);
            }
            Glide.with(getActivity())
                    .load(mArrayListPattern.get(position).getPattern_image())
                    .centerCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .skipMemoryCache(false)
                    .crossFade()
                    .placeholder(R.drawable.pattern_one_bg)
                    .into(holder.mImageViewPattern);

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListPattern != null) {
                        if (position < mArrayListPattern.size()) {
                            for (int i = 0; i < mArrayListPattern.size(); i++) {
                                mArrayListPattern.get(i).setIsChecked(false);
                            }
                            mArrayListPattern.get(position).setIsChecked(true);
                            mPatternListAdapter.notifyDataSetChanged();
                            if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                mActivity.mSwitchCompatOnOff.setChecked(true);
                                if (mIsFromAllGroup) {
                                    mActivity.changePatternColor(BLEUtility.intToByte(100), BLEUtility.intToByte(position + 1), Short.parseShort(0 + ""), true);
                                } else {
                                    mActivity.changePatternColor(BLEUtility.intToByte(100), BLEUtility.intToByte(position + 1), Short.parseShort(mIntRandomNo + ""), false);
                                }
                            } else {
                                mActivity.connectDeviceWithProgress();
                            }
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return (mArrayListPattern == null) ? 0 : mArrayListPattern.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_color_pattern_list_item_textview_pattern_name)
            TextView mTextViewPatternName;
            @BindView(R.id.raw_color_pattern_list_item_imageview_device)
            ImageView mImageViewPattern;
            @BindView(R.id.raw_color_pattern_list_item_imageview_selector)
            ImageView mImageViewPatternSelector;
            @BindView(R.id.raw_color_pattern_list_item_relativelayout_main)
            RelativeLayout mRelativeLayoutMain;
            @BindView(R.id.raw_color_pattern_list_item_cardview_main)
            CardView mCardView;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    // Custom method to apply BlurMaskFilter to a TextView text
    protected void applyBlurMaskFilter(TextView tv, BlurMaskFilter.Blur style) {
        // Define the blur effect radius
        float radius = tv.getTextSize() / 50;
        // Initialize a new BlurMaskFilter instance
        BlurMaskFilter filter = new BlurMaskFilter(radius, style);
        // Set the TextView layer type
        tv.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // Finally, apply the blur effect on TextView text
        tv.getPaint().setMaskFilter(filter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mArrayListPattern = null;
        mPatternListAdapter = null;
        mRecyclerView.setAdapter(null);
        mRecyclerView = null;
        unbinder.unbind();
    }

    /**
     * Called when power button is pressed to on/off light.
     */
    private CompoundButton.OnCheckedChangeListener powerChange = new CompoundButton.OnCheckedChangeListener() {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            //    mController.setLightPower(isChecked ? PowerState.ON : PowerState.OFF);
            if (isChecked) {
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    String mSwitchStatus = "ON";
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(true);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(1), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                            String[] mArray = new String[]{mStringLocalId};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArray);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            } else {
                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                    ContentValues mContentValues = new ContentValues();
                    String mSwitchStatus = "OFF";
                    if (mIsFromAllGroup) {
                        mActivity.mPreferenceHelper.setIsAllDeviceOn(false);
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(0 + ""), true);
                        mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                        String[] mArray = new String[]{mActivity.mPreferenceHelper.getUserId()};
                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceUserId + "=?", mArray);
                        String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupUserId + "= '" + mActivity.mPreferenceHelper.getUserId() + "'";
                        mActivity.mDbHelper.exeQuery(url);
                    } else {
                        mActivity.setLightOnOff(BLEUtility.intToByte(100), BLEUtility.intToByte(0), Short.parseShort(mIntRandomNo + ""), false);
                        if (mIsFromGroup) {
                            String url = "update " + mActivity.mDbHelper.mTableGroup + " set " + mActivity.mDbHelper.mFieldGroupDeviceSwitchStatus + "= '" + mSwitchStatus + "' " + " where " + mActivity.mDbHelper.mFieldGroupCommId + "= '" + mIntRandomNo + "'";
                            mActivity.mDbHelper.exeQuery(url);
                        } else {
                            mContentValues.put(mActivity.mDbHelper.mFieldSwitchStatus, mSwitchStatus);
                            String[] mArray = new String[]{mStringLocalId};
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArray);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            }
        }
    };
}