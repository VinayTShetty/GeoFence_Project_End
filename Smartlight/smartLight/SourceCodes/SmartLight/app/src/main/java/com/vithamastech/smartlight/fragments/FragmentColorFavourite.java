package com.vithamastech.smartlight.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.evergreen.ble.advertisement.ManufactureData;
import com.libRG.CustomTextView;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.ColorPicker;
import com.vithamastech.smartlight.Views.SectionedRecyclerViewAdapter;
import com.vithamastech.smartlight.Vo.VoBluetoothDevices;
import com.vithamastech.smartlight.Vo.VoFavColor;
import com.vithamastech.smartlight.Vo.VoPattern;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.ColorUtil;
import com.vithamastech.smartlight.interfaces.onAlertDialogCallBack;
import com.vithamastech.smartlight.interfaces.onDeviceConnectionStatusChange;
import com.warkiz.widget.IndicatorSeekBar;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 25-12-2017.
 */

public class FragmentColorFavourite extends Fragment {
    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    @BindView(R.id.fragment_color_solid_recyclerview)
    RecyclerView mRecyclerView;

    ArrayList<VoFavColor> mArrayListPattern = new ArrayList<>();
    int mIntRandomNo = 0;
    boolean mIsFromGroup = false;
    boolean mIsFromAllGroup = false;
    String mStringLocalId = "";
    String mStringServerId = "";
    int oldCheckedPosition = -1;
    int selectedPosition = -1;
    ColorListAdapter mColorListAdapter;
    GridLayoutManager mLayoutManager;

    int sdk = android.os.Build.VERSION.SDK_INT;
    GradientDrawable g;
    int color;
    GradientDrawable mask;
    StateListDrawable foreground;
    int mIntSeekSelectedValue = 100;
    int mAlarmColor;
    Dialog myDialogFavColor;
    int mIntAlpha;
    String[] mArrayLocalID;

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
        mViewRoot = inflater.inflate(R.layout.fragment_color_solid, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mColorListAdapter = new ColorListAdapter();
        int spacingInPixels = getResources().getDimensionPixelSize(R.dimen._8sdp);
        ItemOffsetDecoration itemDecoration = new ItemOffsetDecoration(spacingInPixels);
        mRecyclerView.addItemDecoration(itemDecoration);
        mRecyclerView.setHasFixedSize(true);
        mArrayLocalID = new String[]{mStringLocalId};

        mLayoutManager = new GridLayoutManager(mActivity, 4);
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mColorListAdapter.getItemViewType(position)) {
                    case 0:
                        return mLayoutManager.getSpanCount();
                    case 1:
                        return 1;
                    default:
                        return -1;
                }
//                    return mPatternListAdapter.isHeader(position) ? mLayoutManager.getSpanCount() : 1;
            }
        });
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mColorListAdapter);
        /*Get Solid and favourite color from local database*/
        getDBSolidColorList();
        mActivity.mSwitchCompatOnOff.setOnCheckedChangeListener(powerChange);
        mActivity.mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.onBackPressed();
            }
        });

        return mViewRoot;
    }

    /*Show Color choose picker dialog*/
    private void showColorChooseDialog(int color) {
        myDialogFavColor = new Dialog(mActivity);
        myDialogFavColor.requestWindowFeature(Window.FEATURE_NO_TITLE);
        myDialogFavColor.setContentView(R.layout.popup_color_favourite_choose);
        myDialogFavColor.setCancelable(true);
        myDialogFavColor.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.colorAppTheam)));
        final IndicatorSeekBar mIndicatorSeekBar = (IndicatorSeekBar) myDialogFavColor
                .findViewById(R.id.frg_favourite_color_indicator_seekbar);
        ColorPicker colorPicker = (ColorPicker) myDialogFavColor
                .findViewById(R.id.baseColorPicker);
        final CustomTextView mTextViewSelectedColor = (CustomTextView) myDialogFavColor
                .findViewById(R.id.frg_favourite_color_selected_color);
        ImageView mImageViewBack = (ImageView) myDialogFavColor
                .findViewById(R.id.custom_action_img_back);
        ImageView mImageViewAdd = (ImageView) myDialogFavColor
                .findViewById(R.id.custom_actionbar_imageview_add);
        TextView mTextViewAdd = (TextView) myDialogFavColor
                .findViewById(R.id.custom_action_txt_add);
        TextView mTextViewTitle = (TextView) myDialogFavColor
                .findViewById(R.id.custom_action_txt_title);
        mImageViewBack.setVisibility(View.VISIBLE);
        mImageViewAdd.setVisibility(View.GONE);
        mTextViewAdd.setVisibility(View.VISIBLE);
        mTextViewAdd.setText(getResources().getString(R.string.str_save));
        mTextViewTitle.setText(R.string.frg_device_set_color_favourite_color);
        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                myDialogFavColor.dismiss();
            }
        });
        colorPicker.setGradientView(R.drawable.ic_wheel_two, true);
        colorPicker.setColorPicker(R.drawable.color_picker_circle);

        mAlarmColor = color;
        mIntSeekSelectedValue = 100;
//        int mCurrentColorWithoutAlpha = Color.rgb(Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor));
        mIndicatorSeekBar.setProgress(mIntSeekSelectedValue);
//        mIndicatorSeekBar.getBuilder().setIndicatorColor(mCurrentColorWithoutAlpha).setProgress(mIntSeekSelectedValue).setThumbColor(mCurrentColorWithoutAlpha).setProgressTrackColor(mCurrentColorWithoutAlpha).setTickColor(mCurrentColorWithoutAlpha).apply();
        mTextViewSelectedColor.setBackgroundColor(mAlarmColor);
        /*color selected value listener*/
        colorPicker.setColorSelectedListener(new ColorPicker.ColorSelectedListener() {
            @Override
            public void onColorSelected(int pixelColor, boolean isTapUp) {
                mAlarmColor = pixelColor;
                if (ColorUtil.isColorOverBlack(mAlarmColor)) {
                    mAlarmColor = Color.rgb(255, 255, 255);
                }
                mIndicatorSeekBar.setProgress(mIntSeekSelectedValue);
                mIntAlpha = (mIntSeekSelectedValue * 255) / 100;
//                mIndicatorSeekBar.getBuilder().setIndicatorColor(mAlarmColor).setProgress(mIntSeekSelectedValue).setThumbColor(mAlarmColor).setProgressTrackColor(mAlarmColor).setTickColor(mAlarmColor).apply();
                mTextViewSelectedColor.setBackgroundColor(Color.argb(mIntAlpha, Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor)));

            }
        });
        /*brightness value change listener*/
        mIndicatorSeekBar.setOnSeekChangeListener(new IndicatorSeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(IndicatorSeekBar seekBar, int progress, float progressFloat, boolean fromUserTouch) {
                mIntSeekSelectedValue = progress;
                mIntAlpha = (mIntSeekSelectedValue * 255) / 100;
                mTextViewSelectedColor.setBackgroundColor(Color.argb(mIntAlpha, Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor)));
            }

            @Override
            public void onSectionChanged(IndicatorSeekBar seekBar, int thumbPosOnTick, String tickBelowText, boolean fromUserTouch) {
            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar, int thumbPosOnTick) {
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {

            }

            @Override
            public void onTouchUp(IndicatorSeekBar seekBar, int progress) {
                mIntSeekSelectedValue = progress;
                mIntAlpha = (mIntSeekSelectedValue * 255) / 100;
//                mAlarmColor = Color.argb(mIntAlpha, Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor));
                mTextViewSelectedColor.setBackgroundColor(Color.argb(mIntAlpha, Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor)));
            }
        });
        /*Add color to local database*/
        mTextViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float[] hsv = new float[3];
                float[] hsvReverse = new float[3];
                int color_to_send;
                Color.colorToHSV(mAlarmColor, hsv);
                hsv[2] = ((float) mIntSeekSelectedValue + 1) / 100.0f;
                color_to_send = Color.HSVToColor(hsv);

                Color.colorToHSV(color_to_send, hsvReverse);

//                String colorHex = String.format("#%06X", (0xFFFFFF & color_to_send));
                ContentValues mContentValues = new ContentValues();

                mContentValues.put(mActivity.mDbHelper.mFieldColorFavRGB, color_to_send);
                mContentValues.put(mActivity.mDbHelper.mFieldColorFavIsActive, "1");
                mContentValues.put(mActivity.mDbHelper.mFieldColorFavHasColor, "1");
                mContentValues.put(mActivity.mDbHelper.mFieldColorUserId, mActivity.mPreferenceHelper.getUserId());
                String isExistInDB = CheckRecordExistInFavColorDB(color_to_send + "");
                if (isExistInDB.equalsIgnoreCase("-1")) {
                    mActivity.mDbHelper.insertRecord(mActivity.mDbHelper.mTableColorFavourite, mContentValues);
                } else {
                    mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableColorFavourite, mContentValues, mActivity.mDbHelper.mFieldColorFavId + "=?", new String[]{isExistInDB});
                }
                getDBSolidColorList();
                myDialogFavColor.dismiss();
            }
        });

        myDialogFavColor.show();
//        myDialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window window = myDialogFavColor.getWindow();
        window.setLayout(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
    }

    /*Check favourite color is exist in database or not*/
    public String CheckRecordExistInFavColorDB(String colorRGB) {
        DataHolder mDataHolder;
        String url = "select * from " + mActivity.mDbHelper.mTableColorFavourite + " where " + mActivity.mDbHelper.mFieldColorFavRGB + "= '" + colorRGB + "'";
        mDataHolder = mActivity.mDbHelper.read(url);
        if (mDataHolder != null) {
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mActivity.mDbHelper.mFieldColorFavId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    /*Get solid color from local database*/
    private void getDBSolidColorList() {
        DataHolder mDataHolderLight;
        mArrayListPattern = new ArrayList<>();
        VoPattern mVoPattern;
        VoFavColor mVoFavColor;
        try {
            /*Get First 4 RGBW color from database*/
            String url = "select * from " + mActivity.mDbHelper.mTableColorSolid + " where " + mActivity.mDbHelper.mFieldColorId + "<= '4'";
            mDataHolderLight = mActivity.mDbHelper.read(url);
            if (mDataHolderLight != null) {
                // color first 4 RGB color
                mVoFavColor = new VoFavColor();
                mVoFavColor.setColorSection(getResources().getString(R.string.str_favourite_color_rgb_color));
                ArrayList<VoPattern> mVoColorList = new ArrayList<>();
                for (int i = 0; i < mDataHolderLight.get_Listholder().size(); i++) {
                    mVoPattern = new VoPattern();
                    mVoPattern.setPattern_value(i + "");
                    mVoPattern.setIsChecked(false);
                    mVoPattern.setIsFavColor(false);
                    mVoPattern.setIsVisibleAddOption(false);
                    mVoPattern.setPattern_image(hex2Rgb(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorRGB)));
                    mVoColorList.add(mVoPattern);
                }
                mVoFavColor.setColorLists(mVoColorList);
                mArrayListPattern.add(mVoFavColor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            /*Get All favourite color from database*/
            String url = "select * from " + mActivity.mDbHelper.mTableColorFavourite + " where " + mActivity.mDbHelper.mFieldColorFavIsActive + "= '1'";
            mDataHolderLight = mActivity.mDbHelper.read(url);
            mVoFavColor = new VoFavColor();
            mVoFavColor.setColorSection(getResources().getString(R.string.str_favourite_color_your_favourite));
            ArrayList<VoPattern> mVoColorList = new ArrayList<>();
            if (mDataHolderLight != null) {
                for (int i = 0; i < mDataHolderLight.get_Listholder().size(); i++) {
                    mVoPattern = new VoPattern();
                    mVoPattern.setPattern_value(i + "");
                    mVoPattern.setIsChecked(false);
                    mVoPattern.setIsFavColor(true);
                    mVoPattern.setIsVisibleAddOption(false);
                    mVoPattern.setFav_id(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorFavId));
//                    mVoPattern.setPattern_image(hex2Rgb(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorFavRGB)));
                    mVoPattern.setPattern_image(Integer.parseInt(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorFavRGB)));
                    mVoColorList.add(mVoPattern);
                }
            }
            mVoPattern = new VoPattern();
            mVoPattern.setPattern_value("-1");
            mVoPattern.setIsChecked(false);
            mVoPattern.setIsFavColor(true);
            mVoPattern.setIsVisibleAddOption(true);
            mVoPattern.setFav_id("-1");
            mVoPattern.setPattern_image(0);
            mVoColorList.add(mVoPattern);
            mVoFavColor.setColorLists(mVoColorList);
            mArrayListPattern.add(mVoFavColor);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            /*Get all default solid color from database*/
            String url = "select * from " + mActivity.mDbHelper.mTableColorSolid + " where " + mActivity.mDbHelper.mFieldColorId + "> '4'";
            mDataHolderLight = mActivity.mDbHelper.read(url);
            if (mDataHolderLight != null) {
                mVoFavColor = new VoFavColor();
                mVoFavColor.setColorSection(getResources().getString(R.string.str_favourite_color_palette));
                ArrayList<VoPattern> mVoColorList = new ArrayList<>();
                // color first 68
                for (int i = 0; i < 64; i++) {
                    mVoPattern = new VoPattern();
                    mVoPattern.setPattern_value(i + "");
                    mVoPattern.setIsChecked(false);
                    mVoPattern.setIsFavColor(false);
                    mVoPattern.setIsVisibleAddOption(false);
                    mVoPattern.setPattern_image(hex2Rgb(mDataHolderLight.get_Listholder().get(i).get(mActivity.mDbHelper.mFieldColorRGB)));
                    mVoColorList.add(mVoPattern);
                }
                mVoFavColor.setColorLists(mVoColorList);
                mArrayListPattern.add(mVoFavColor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mColorListAdapter != null) {
            mColorListAdapter.notifyDataSetChanged();
        }

    }

    @ColorInt
    public static int adjustAlpha(@ColorInt int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    /*Convert hex color to rgb color*/
    public int hex2Rgb(String colorStr) {
        return Color.rgb(Integer.valueOf(colorStr.substring(1, 3), 16),
                Integer.valueOf(colorStr.substring(3, 5), 16),
                Integer.valueOf(colorStr.substring(5, 7), 16));
    }

    /*Color list adapter*/
    public class ColorListAdapter extends SectionedRecyclerViewAdapter<RecyclerView.ViewHolder> {
        @Override
        public int getSectionCount() {
            return mArrayListPattern.size();
        }

        @Override
        public int getItemCount(int section) {
            if (mArrayListPattern.get(section).getColorLists() == null) {
                return 0;
            } else {
                return mArrayListPattern.get(section).getColorLists().size();
            }
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, boolean header) {
            if (header) {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.raw_device_section_list_item, parent, false);
                return new SectionViewHolder(v);
            } else {
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.raw_color_solid_list_item, parent, false);
                return new ViewHolder(v);
            }
        }

        @Override
        public void onBindHeaderViewHolder(RecyclerView.ViewHolder holder, int section) {
            String sectionName = mArrayListPattern.get(section).getColorSection();
            SectionViewHolder sectionViewHolder = (SectionViewHolder) holder;
            sectionViewHolder.sectionTitle.setText(sectionName);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder itemViewHolder, final int section, final int position, int absolutePosition) {
            final ViewHolder holder = (ViewHolder) itemViewHolder;
            color = mArrayListPattern.get(section).getColorLists().get(position).getPattern_image();
            if (mArrayListPattern.get(section).getColorLists().get(position).getIsVisibleAddOption()) {
                holder.mImageViewAddColor.setVisibility(View.VISIBLE);
                int color = getResources().getColor(R.color.colorBlueTheme);
                g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{color,  //#fff this is the start color of gradient
                        adjustAlpha(color, 1.0f)}); //#97712F this is the end color of gradient
                g.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    holder.mRelativeLayoutMain.setBackgroundDrawable(g);
                } else {
                    holder.mRelativeLayoutMain.setBackground(g);
                }
            } else {
                holder.mImageViewAddColor.setVisibility(View.GONE);
                g = new GradientDrawable(GradientDrawable.Orientation.TL_BR, new int[]{color,  //#fff this is the start color of gradient
                        adjustAlpha(color, 0.9f), adjustAlpha(color, 0.8f), adjustAlpha(color, 0.7f), adjustAlpha(color, 0.6f)}); //#97712F this is the end color of gradient
                g.setGradientType(GradientDrawable.LINEAR_GRADIENT);
                if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                    holder.mRelativeLayoutMain.setBackgroundDrawable(g);
                } else {
                    holder.mRelativeLayoutMain.setBackground(g);
                }
            }
            if (sdk >= Build.VERSION_CODES.M) {
                holder.mRelativeLayoutMain.setForeground(createForegroundDrawable(color));
            }
            holder.mImageViewSelected.setColorFilter(ColorUtil.isColorDark(mArrayListPattern.get(section).getColorLists().get(position).getPattern_image()) ? Color.WHITE : Color.BLACK);
            if (mArrayListPattern.get(section).getColorLists().get(position).getIsChecked()) {
                if (selectedPosition == position) {
                    holder.mImageViewSelected.setAlpha(0.0f);
                    holder.mImageViewSelected.setScaleX(0.0f);
                    holder.mImageViewSelected.setScaleY(0.0f);
                    holder.mImageViewSelected.setVisibility(View.VISIBLE);
                    holder.mImageViewSelected.animate()
                            .alpha(1.0f)
                            .scaleX(1.0f)
                            .scaleY(1.0f)
                            .setDuration(250)
                            .setListener(new AnimatorListenerAdapter() {

                                @Override
                                public void onAnimationEnd(Animator animation) {
                                    holder.mImageViewSelected.setAlpha(1.0f);
                                    holder.mImageViewSelected.setScaleX(1.0f);
                                    holder.mImageViewSelected.setScaleY(1.0f);
                                }
                            }).start();
                }
            } else {
                holder.mImageViewSelected.setVisibility(View.INVISIBLE);
                holder.mImageViewSelected.setAlpha(1.0f);
                holder.mImageViewSelected.setScaleX(1.0f);
                holder.mImageViewSelected.setScaleY(1.0f);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mArrayListPattern.get(section).getColorLists() != null) {
                        if (position < mArrayListPattern.get(section).getColorLists().size()) {
                            if (mArrayListPattern.get(section).getColorLists().get(position).getIsVisibleAddOption()) {
                                showColorChooseDialog(Color.rgb(255, 255, 255));
                            } else {
                                for (int j = 0; j < mArrayListPattern.size(); j++) {
                                    for (int i = 0; i < mArrayListPattern.get(j).getColorLists().size(); i++) {
                                        mArrayListPattern.get(j).getColorLists().get(i).setIsChecked(false);
                                    }
                                }
                                mArrayListPattern.get(section).getColorLists().get(position).setIsChecked(true);
                                oldCheckedPosition = selectedPosition;
                                selectedPosition = position;
                                mColorListAdapter.notifyDataSetChanged();
                                if (mActivity.isDevicesConnected || mActivity.getIsDeviceSupportedAdvertisment()) {
                                    mActivity.mSwitchCompatOnOff.setChecked(true);
                                    if (mIsFromAllGroup) {
                                        mActivity.setLightColorWithBrightness(BLEUtility.intToByte(100), mArrayListPattern.get(section).getColorLists().get(position).getPattern_image(), Short.parseShort(0 + ""), true);
                                    } else {
                                        mActivity.setLightColorWithBrightness(BLEUtility.intToByte(100), mArrayListPattern.get(section).getColorLists().get(position).getPattern_image(), Short.parseShort(mIntRandomNo + ""), false);
                                    }
                                    if (!mIsFromGroup) {
                                        ContentValues mContentValues = new ContentValues();
                                        mContentValues.put(mActivity.mDbHelper.mFieldDeviceColor, mArrayListPattern.get(section).getColorLists().get(position).getPattern_image());
                                        mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                                    }
                                } else {
                                    mActivity.connectDeviceWithProgress();
                                }
                            }
                        }
                    }
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mArrayListPattern.get(section).getColorLists() != null) {
                        if (position < mArrayListPattern.get(section).getColorLists().size()) {
                            if (mArrayListPattern.get(section).getColorLists().get(position).getIsVisibleAddOption()) {
                                showColorChooseDialog(Color.rgb(255, 255, 255));
                            } else {
                                if (mArrayListPattern.get(section).getColorLists().get(position).getIsFavColor()) {
                                    mActivity.mUtility.errorDialogWithYesNoCallBack(getResources().getString(R.string.str_favourite_color_delete_title), getResources().getString(R.string.str_favourite_color_delete_confirmation), "Yes", "No", true, 2, new onAlertDialogCallBack() {
                                        @Override
                                        public void PositiveMethod(DialogInterface dialog, int id) {
                                            if (mArrayListPattern.get(section).getColorLists() != null) {
                                                if (position < mArrayListPattern.get(section).getColorLists().size()) {
                                                    if (mArrayListPattern.get(section).getColorLists().get(position).getIsFavColor()) {
                                                        String mStringQuery = "delete from " + mActivity.mDbHelper.mTableColorFavourite + " where " + mActivity.mDbHelper.mFieldColorFavId + "= '" + mArrayListPattern.get(section).getColorLists().get(position).getFav_id() + "'";
                                                        mActivity.mDbHelper.exeQuery(mStringQuery);
                                                        getDBSolidColorList();
                                                    }
                                                }
                                            }
                                        }

                                        @Override
                                        public void NegativeMethod(DialogInterface dialog, int id) {

                                        }
                                    });
                                }
                            }
                        }
                    }
                    return true;
                }
            });
        }

        // SectionViewHolder Class for Sections
        public class SectionViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_device_section_list_item_textview_section)
            TextView sectionTitle;

            public SectionViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.selected_checkmark)
            ImageView mImageViewSelected;
            @BindView(R.id.image_add_color)
            ImageView mImageViewAddColor;
            @BindView(R.id.raw_solid_list_item_main)
            RelativeLayout mRelativeLayoutMain;

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
    }

    @Override
    public void onPause() {
        super.onPause();
        if (myDialogFavColor != null) {
            if (myDialogFavColor.isShowing()) {
                myDialogFavColor.dismiss();
            }
        }
    }

    /*For Foreground Drawable*/
    private Drawable createForegroundDrawable(int mColor) {
        if (sdk >= Build.VERSION_CODES.LOLLIPOP) {
            // Use a ripple drawable
            mask = new GradientDrawable();
            mask.setShape(GradientDrawable.RECTANGLE);
            mask.setColor(Color.BLACK);
            return new RippleDrawable(ColorStateList.valueOf(ColorUtil.getRippleColor(mColor)), null, mask);
        } else {
            // Use a translucent foreground
            foreground = new StateListDrawable();
            foreground.setAlpha(80);
            foreground.setEnterFadeDuration(250);
            foreground.setExitFadeDuration(250);

            mask = new GradientDrawable();
            mask.setShape(GradientDrawable.RECTANGLE);
            mask.setColor(ColorUtil.getRippleColor(mColor));
            foreground.addState(new int[]{android.R.attr.state_pressed}, mask);

            foreground.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));

            return foreground;
        }
    }
    /*Offset of the list*/
    private class ItemOffsetDecoration extends RecyclerView.ItemDecoration {

        private int mItemOffset;

        public ItemOffsetDecoration(int itemOffset) {
            mItemOffset = itemOffset;
        }

        public ItemOffsetDecoration(@NonNull Context context, @DimenRes int itemOffsetId) {
            this(context.getResources().getDimensionPixelSize(itemOffsetId));
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            super.getItemOffsets(outRect, view, parent, state);
            outRect.set(mItemOffset, mItemOffset, mItemOffset, mItemOffset);
        }
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
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
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
                            mActivity.mDbHelper.updateRecord(mActivity.mDbHelper.mTableDevice, mContentValues, mActivity.mDbHelper.mFieldDeviceLocalId + "=?", mArrayLocalID);
                        }
                    }
                } else {
                    mActivity.connectDeviceWithProgress();
                }
            }
        }
    };
}
