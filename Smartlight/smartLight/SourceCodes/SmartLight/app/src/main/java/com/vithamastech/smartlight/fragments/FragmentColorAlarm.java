package com.vithamastech.smartlight.fragments;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.libRG.CustomTextView;
import com.vithamastech.smartlight.MainActivity;
import com.vithamastech.smartlight.R;
import com.vithamastech.smartlight.Views.ColorPicker;
import com.vithamastech.smartlight.helper.ColorUtil;
import com.vithamastech.smartlight.interfaces.onBackPressWithAction;
import com.warkiz.widget.IndicatorSeekBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FragmentColorAlarm extends Fragment {

    MainActivity mActivity;
    View mViewRoot;
    private Unbinder unbinder;
    IndicatorSeekBar mIndicatorSeekBar;
    @BindView(R.id.baseColorPicker)
    ColorPicker colorPicker;
    @BindView(R.id.frg_alarm_color_selected_color)
    CustomTextView mTextViewSelectedColor;
    int mIntSeekSelectedValue = 100;

    boolean mIsColorChange = false;
    int mAlarmColor = Color.rgb(255, 255, 255);
    onBackPressWithAction mOnBackPressWithAction;
    int mIntAlpha;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = (MainActivity) getActivity();
        mActivity.mToolbar.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        if (getArguments() != null) {
            mAlarmColor = getArguments().getInt("mIntent_selected_color", Color.rgb(255, 255, 255));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mViewRoot = inflater.inflate(R.layout.fragment_color_alarml, container, false);
        colorPicker = new ColorPicker(mActivity);
        unbinder = ButterKnife.bind(this, mViewRoot);
        mActivity.mImageViewBack.setVisibility(View.GONE);
        mActivity.mTextViewAdd.setVisibility(View.VISIBLE);
        mActivity.mTextViewAdd.setText("Save");
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mActivity.mToolbar.setBackgroundColor(getResources().getColor(R.color.colorBlack));
        mActivity.showBackButton(true);
        mActivity.mTextViewTitle.setText(R.string.frg_color_alarm_title);
        mIndicatorSeekBar = (IndicatorSeekBar) mViewRoot.findViewById(R.id.frg_alarm_color_indicator_seekbar);
        colorPicker.setGradientView(R.drawable.ic_wheel_two, true);
        colorPicker.setColorPicker(R.drawable.color_picker_circle);
        mIntSeekSelectedValue = 100;
        mIsColorChange = false;
//        int mCurrentColorWithoutAlpha = Color.rgb(Color.red(mAlarmColor),Color.green(mAlarmColor),Color.blue(mAlarmColor));
        mIndicatorSeekBar.setProgress(mIntSeekSelectedValue);
//        mIndicatorSeekBar.getBuilder().setIndicatorColor(mCurrentColorWithoutAlpha).setProgress(mIntSeekSelectedValue).setThumbColor(mCurrentColorWithoutAlpha).setProgressTrackColor(mCurrentColorWithoutAlpha).setTickColor(mCurrentColorWithoutAlpha).apply();
        mTextViewSelectedColor.setBackgroundColor(mAlarmColor);

        /*get color from touch area*/
        colorPicker.setColorSelectedListener(new ColorPicker.ColorSelectedListener() {
            @Override
            public void onColorSelected(int pixelColor, boolean isTapUp) {
                mAlarmColor = pixelColor;
                if (ColorUtil.isColorOverBlack(mAlarmColor)) {
                    mAlarmColor = Color.rgb(255, 255, 255);
                }

                mIsColorChange = true;
                mIndicatorSeekBar.setProgress(mIntSeekSelectedValue);
//                mIndicatorSeekBar.getBuilder().setIndicatorColor(mAlarmColor).setProgress(mIntSeekSelectedValue).setThumbColor(mAlarmColor).setProgressTrackColor(mAlarmColor).setTickColor(mAlarmColor).apply();
                mIntAlpha = (mIntSeekSelectedValue * 255) / 100;
                mTextViewSelectedColor.setBackgroundColor(Color.argb(mIntAlpha, Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor)));
            }
        });
        /*Change color brightness value*/
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
                mTextViewSelectedColor.setBackgroundColor(Color.argb(mIntAlpha, Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor)));
            }
        });
        /*Save Alarm Color and backŌ*/
        mActivity.mTextViewAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                mIntAlpha = (mIntSeekSelectedValue * 255) / 100;
//                mAlarmColor = Color.argb(mIntAlpha, Color.red(mAlarmColor), Color.green(mAlarmColor), Color.blue(mAlarmColor));
                float[] hsv = new float[3];
                float[] hsvReverse = new float[3];
                int color_to_send;
                Color.colorToHSV(mAlarmColor, hsv);
                hsv[2] = ((float) mIntSeekSelectedValue + 1) / 100.0f;
                color_to_send = Color.HSVToColor(hsv);

                Color.colorToHSV(color_to_send, hsvReverse);
//                String colorHex = String.format("#%06X", (0xFFFFFF & color_to_send));
                if (mOnBackPressWithAction != null) {
                    mOnBackPressWithAction.onBackWithAction(color_to_send);
                }
                mActivity.onBackPressed();
            }
        });
        return mViewRoot;
    }

    public void setOnColorResultSet(onBackPressWithAction mScanResultSet) {
        mOnBackPressWithAction = mScanResultSet;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        mActivity.mToolbar.setBackgroundColor(getResources().getColor(R.color.colorTransparent));
        mActivity.mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
        mActivity.mTextViewAdd.setText("Add");
    }
}
