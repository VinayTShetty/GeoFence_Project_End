package com.succorfish.depthntemp.fragnments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;
import com.libRG.CustomTextView;
import com.succorfish.depthntemp.MainActivity;
import com.succorfish.depthntemp.MyApplication;
import com.succorfish.depthntemp.R;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.vo.VoHeatMap;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * Created by Jaydeep on 27-02-2018.
 */

public class FragmentSettingHeatMap extends Fragment {

    View mViewRoot;
    MainActivity mActivity;
    private Unbinder unbinder;

    ArrayList<VoHeatMap> mArrayListHeatMap = new ArrayList<>();
    HeatMapListSettingAdapter mHeatMapListSettingAdapter;

    @BindView(R.id.frg_heat_map_setting_list_rv)
    RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_setting_heat_map, container, false);
        unbinder = ButterKnife.bind(this, mViewRoot);

        mActivity.mToolbar.setVisibility(View.VISIBLE);
        mActivity.mTextViewTitle.setText(R.string.str_heat_map_setting);
        mActivity.mTextViewTitle.setVisibility(View.VISIBLE);
        mActivity.mImageViewBack.setVisibility(View.VISIBLE);
        mActivity.mImageViewAddDevice.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.GONE);
        mArrayListHeatMap = PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getAllHeatMapSettingData();

        mHeatMapListSettingAdapter = new HeatMapListSettingAdapter();
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mHeatMapListSettingAdapter);

        return mViewRoot;
    }

    /*Heat Map List Adapter*/
    public class HeatMapListSettingAdapter extends RecyclerView.Adapter<HeatMapListSettingAdapter.ViewHolder> {

        @Override
        public HeatMapListSettingAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.raw_heat_map_list, parent, false);
            return new HeatMapListSettingAdapter.ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(HeatMapListSettingAdapter.ViewHolder holder, final int position) {

            holder.mTextViewName.setText(mArrayListHeatMap.get(position).getHeatName());
            holder.mTextViewRange.setText("Temp - " + mArrayListHeatMap.get(position).getMinValue() + " °C to " + mArrayListHeatMap.get(position).getMaxValue() + " °C");
            if (position == 0) {
                holder.mCustomTextViewColor.setBackgroundColor(getResources().getColor(R.color.temp_high));
            } else if (position == 1) {
                holder.mCustomTextViewColor.setBackgroundColor(getResources().getColor(R.color.temp_high_medium));
            } else if (position == 2) {
                holder.mCustomTextViewColor.setBackgroundColor(getResources().getColor(R.color.temp_medium));
            } else if (position == 3) {
                holder.mCustomTextViewColor.setBackgroundColor(getResources().getColor(R.color.temp_low_medium));
            } else if (position == 4) {
                holder.mCustomTextViewColor.setBackgroundColor(getResources().getColor(R.color.temp_low));
            } else {
                holder.mCustomTextViewColor.setBackgroundColor(getResources().getColor(R.color.temp_medium));
            }
            if (mArrayListHeatMap.get(position).getIsSelectable()) {
                holder.mLinearLayoutMain.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
                holder.mTextViewRange.setText("Temp : " + mArrayListHeatMap.get(position).getMinValue() + " °C to " + mArrayListHeatMap.get(position).getMaxValue() + " °C");
            } else {
                holder.mLinearLayoutMain.setBackgroundColor(getResources().getColor(R.color.colorDisableList));
                holder.mTextViewRange.setText("Temp : " + "-NA-" + " to " + "-NA-");

            }
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        if (mArrayListHeatMap != null) {
                            if (position < mArrayListHeatMap.size()) {
                                if (mArrayListHeatMap.get(position).getIsSelectable()) {
                                    openHeatMapDialog(mArrayListHeatMap.get(position), position);
                                } else {
                                    System.out.println("Not Allow To change");
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mArrayListHeatMap.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.raw_heat_map_list_tv_range_name)
            TextView mTextViewName;
            @BindView(R.id.raw_heat_map_list_tv_range)
            TextView mTextViewRange;
            @BindView(R.id.raw_heat_map_list_tv_color)
            CustomTextView mCustomTextViewColor;
            @BindView(R.id.raw_heat_map_ll_main)
            LinearLayout mLinearLayoutMain;

            public ViewHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }
        }
    }

    /*Heat Map Dialog*/
    public void openHeatMapDialog(final VoHeatMap voHeatMap, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(mActivity);
        final View mView = layoutInflaterAndroid.inflate(R.layout.heat_alert_dialog, null);
        android.support.v7.app.AlertDialog.Builder alertDialogBuilderUserInput = new android.support.v7.app.AlertDialog.Builder(mActivity, R.style.AppCompatAlertDialogStyle);
        alertDialogBuilderUserInput.setView(mView);
        final CrystalSeekbar mCrystalRangeSeekBar = (CrystalSeekbar) mView.findViewById(R.id.heat_alert_seek_bar_temperature);
        TextView mTextViewTitle = (TextView) mView.findViewById(R.id.heat_alert_dialog_tv_title);
        final TextView mTextViewMinValue = (TextView) mView.findViewById(R.id.heat_alert_dialog_tv_min_value);
        final TextView mTextViewMaxValue = (TextView) mView.findViewById(R.id.heat_alert_dialog_tv_max_value);
        mTextViewTitle.setText(voHeatMap.getHeatName());
        mCrystalRangeSeekBar.setMinStartValue((float) voHeatMap.getMaxValue());
//        mCrystalRangeSeekBar.setMaxStartValue((float) voHeatMap.getMaxValue());
        mCrystalRangeSeekBar.setMinValue((float) voHeatMap.getMinValue());
        mCrystalRangeSeekBar.setMaxValue(85);
        mCrystalRangeSeekBar.apply();
        if (position == 4) {
            mTextViewMinValue.setText("-40 °C");
            mTextViewMaxValue.setText((int) mCrystalRangeSeekBar.getMinStartValue() + " °C");
        } else if (position == 0) {
            mTextViewMinValue.setText(voHeatMap.getMinValue() + " °C");
            mTextViewMaxValue.setText((int) mCrystalRangeSeekBar.getMinStartValue() + " °C");
        } else {
            mTextViewMinValue.setText(voHeatMap.getMinValue() + " °C");
            mTextViewMaxValue.setText((int) mCrystalRangeSeekBar.getMinStartValue() + " °C");
        }

//        mCrystalRangeSeekBar.setOnRangeSeekbarChangeListener(new OnRangeSeekbarChangeListener() {
//            @Override
//            public void valueChanged(Number minValue, Number maxValue) {
//                mTextViewMinValue.setText(minValue.intValue() + " °C");
//                mTextViewMaxValue.setText(maxValue.intValue() + " °C");
//
//            }
//        });
        mCrystalRangeSeekBar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {
                System.out.println("Selected Value-" + value.intValue());
                mTextViewMaxValue.setText(value.intValue() + " °C");
            }
        });
        alertDialogBuilderUserInput
                .setCancelable(true)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {
                        // ToDo get user input here

                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {
                            }
                        });
        final android.support.v7.app.AlertDialog alertDialogAndroid = alertDialogBuilderUserInput.create();
        alertDialogAndroid.show();
        alertDialogAndroid.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("JD-SELECTED-" + mCrystalRangeSeekBar.getSelectedMinValue().intValue());
                voHeatMap.setMinValue((int) mCrystalRangeSeekBar.getMinValue());
                voHeatMap.setIsSelectable(true);
                if (position == 4) {
                    //very low temperature
                    if (mCrystalRangeSeekBar.getSelectedMinValue().intValue() == voHeatMap.getMinValue()) {
                        showMessageRedAlert(mView, "Temperature range must be greater than -40 °C", "OK");
                        return;
                    }
                    mArrayListHeatMap.get(position - 1).setMinValue(mCrystalRangeSeekBar.getSelectedMinValue().intValue());
                    double minTempF = ((mCrystalRangeSeekBar.getSelectedMinValue().intValue() * 1.8) + 32);
                    mArrayListHeatMap.get(position - 1).setMinValueFahrenheit((int) minTempF);
                    System.out.println("JD-PREV MIN-" + mArrayListHeatMap.get(position - 1).getMinValue());
                    for (int i = 0; i < mArrayListHeatMap.size(); i++) {
                        if (i != position) {
                            if (mCrystalRangeSeekBar.getSelectedMinValue().intValue() == 85) {
                                mArrayListHeatMap.get(i).setIsSelectable(false);
                            } else {
                                mArrayListHeatMap.get(i).setIsSelectable(true);
                            }
                        }
                    }
                } else if (position == 0) {
                    //very High temperature
                    for (int i = 0; i < mArrayListHeatMap.size(); i++) {
                        if (i != position) {
                            mArrayListHeatMap.get(i).setIsSelectable(true);
                        }
                    }
                } else {
                    if (mCrystalRangeSeekBar.getSelectedMinValue().intValue() == voHeatMap.getMinValue()) {
                        showMessageRedAlert(mView, "Temperature range must be greater than " + voHeatMap.getMinValue() + " °C", "OK");
                        return;
                    }
                    for (int i = 0; i < position; i++) {
                        mArrayListHeatMap.get(position - 1).setMinValue(mCrystalRangeSeekBar.getSelectedMinValue().intValue());
                        double minTempF = ((mCrystalRangeSeekBar.getSelectedMinValue().intValue() * 1.8) + 32);
                        mArrayListHeatMap.get(position - 1).setMinValueFahrenheit((int) minTempF);
                        System.out.println("JD-PREV MIN-" + mArrayListHeatMap.get(position - 1).getMinValue());
                        if (i != position) {
                            if (mCrystalRangeSeekBar.getSelectedMinValue().intValue() == 85) {
                                mArrayListHeatMap.get(i).setIsSelectable(false);
                            } else {
                                mArrayListHeatMap.get(i).setIsSelectable(true);
                            }

                        }
                    }
                }
                voHeatMap.setMaxValue(mCrystalRangeSeekBar.getSelectedMinValue().intValue());
                double minTempF = ((voHeatMap.getMinValue() * 1.8) + 32);
                double maxTempF = ((voHeatMap.getMaxValue() * 1.8) + 32);
                voHeatMap.setMinValueFahrenheit((int) minTempF);
                voHeatMap.setMaxValueFahrenheit((int) maxTempF);
                mArrayListHeatMap.set(position, voHeatMap);

                System.out.println("JD-Current Position-" + position);
                for (int i = position; i >= 0; i--) {
                    System.out.println("JD-Checking Pos-" + i);
                    if (i != position) {
                        if (i != 4) {
                            System.out.println("JD-Current MAx-" + mArrayListHeatMap.get(i).getMaxValue());
                            System.out.println("JD-Previous MAx-" + mArrayListHeatMap.get(i + 1).getMaxValue());
                            mArrayListHeatMap.get(i).setMinValue(mArrayListHeatMap.get(i + 1).getMaxValue());
                            double minTempFi = ((mArrayListHeatMap.get(i).getMinValue() * 1.8) + 32);
                            mArrayListHeatMap.get(i).setMinValueFahrenheit((int) minTempFi);
                            if (mArrayListHeatMap.get(i).getMaxValue() <= mArrayListHeatMap.get(i + 1).getMaxValue()) {
                                if ((mArrayListHeatMap.get(i).getMinValue() + 1) >= 85) {
                                    mArrayListHeatMap.get(i).setMaxValue(85);
                                } else {
                                    mArrayListHeatMap.get(i).setMaxValue((mArrayListHeatMap.get(i).getMinValue() + 1));
                                }
                                double maxTempFi = ((mArrayListHeatMap.get(i).getMaxValue() * 1.8) + 32);
                                mArrayListHeatMap.get(i).setMaxValueFahrenheit((int) maxTempFi);
                            }
                            if (i != 0 && mArrayListHeatMap.get(i).getMaxValue() >= 85) {
                                mArrayListHeatMap.get(i - 1).setIsSelectable(false);
                            }
                        }
                    } else {
                        mArrayListHeatMap.get(i).setIsSelectable(true);
                    }
                }

                PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setAllHeatMapSettingData(mArrayListHeatMap);
                mArrayListHeatMap = PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getAllHeatMapSettingData();
                if (mHeatMapListSettingAdapter != null) {
                    mHeatMapListSettingAdapter.notifyDataSetChanged();
                }
                alertDialogAndroid.dismiss();
            }
        });
        alertDialogAndroid.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogAndroid.dismiss();
            }
        });
    }

    private void showMessageRedAlert(View mView, String mStringMessage, String mActionMessage) {
        mActivity.mUtility.hideKeyboard(mActivity);
        Snackbar mSnackBar = Snackbar.make(mView, mStringMessage, 5000);
        mSnackBar.setAction(mActionMessage, new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        mSnackBar.setActionTextColor(getResources().getColor(android.R.color.holo_red_light));
        mSnackBar.getView().setBackgroundColor(getResources().getColor(R.color.colorInActiveMenu));
        mSnackBar.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
