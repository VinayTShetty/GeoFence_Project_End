package com.succorfish.depthntemp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.succorfish.depthntemp.db.AppRoomDatabase;
import com.succorfish.depthntemp.db.TablePressureDepthCutOff;
import com.succorfish.depthntemp.helper.PreferenceHelper;
import com.succorfish.depthntemp.vo.VoHeatMap;

import java.util.ArrayList;


/**
 * Created by Jaydeep on 21-12-2017.
 */

public class SplashActivity extends AppCompatActivity {
    AppRoomDatabase mAppRoomDatabase;
    TablePressureDepthCutOff mTablePressureDepthCutOff;
    ArrayList<TablePressureDepthCutOff> mArrayList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mAppRoomDatabase = AppRoomDatabase.getDatabaseInstance(SplashActivity.this);
        mArrayList = new ArrayList<>();
        if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getFirstTimeInstall()) {
            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    // Add default entry in depth cut off table
                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.0);
                    mTablePressureDepthCutOff.setDepthInMillBar(1113);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.1);
                    mTablePressureDepthCutOff.setDepthInMillBar(1123);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.2);
                    mTablePressureDepthCutOff.setDepthInMillBar(1133);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.3);
                    mTablePressureDepthCutOff.setDepthInMillBar(1143);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.4);
                    mTablePressureDepthCutOff.setDepthInMillBar(1153);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.5);
                    mTablePressureDepthCutOff.setDepthInMillBar(1163);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.6);
                    mTablePressureDepthCutOff.setDepthInMillBar(1173);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.7);
                    mTablePressureDepthCutOff.setDepthInMillBar(1183);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.8);
                    mTablePressureDepthCutOff.setDepthInMillBar(1193);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(1.9);
                    mTablePressureDepthCutOff.setDepthInMillBar(1203);
                    mArrayList.add(mTablePressureDepthCutOff);

                    mTablePressureDepthCutOff = new TablePressureDepthCutOff();
                    mTablePressureDepthCutOff.setDepthInMeter(2.0);
                    mTablePressureDepthCutOff.setDepthInMillBar(1213);
                    mArrayList.add(mTablePressureDepthCutOff);
                    TablePressureDepthCutOff mDepthCutOff;
                    for (int i = 0; i < mArrayList.size(); i++) {
                        mDepthCutOff = mAppRoomDatabase.depthCutOffDao().checkDepthMeterIsExistOrNot(mArrayList.get(i).getDepthInMeter());
                        if (mDepthCutOff == null) {
                            mAppRoomDatabase.depthCutOffDao().insert(mArrayList.get(i));
                        }
                    }
                }
            });
            ArrayList<VoHeatMap> mArrayListHeatMap = new ArrayList<>();
            VoHeatMap voHeatMap;
            for (int i = 0; i < 5; i++) {
                voHeatMap = new VoHeatMap();
                voHeatMap.setIsSelectable(true);
                if (i == 0) {
                    voHeatMap.setHeatName("Very High Temperature");
                    voHeatMap.setMinValue(50);
                    voHeatMap.setMaxValue(85);
                    voHeatMap.setMinValueFahrenheit(122);
                    voHeatMap.setMaxValueFahrenheit(185);

                } else if (i == 1) {
                    voHeatMap.setHeatName("High Temperature");
                    voHeatMap.setMinValue(30);
                    voHeatMap.setMaxValue(50);
                    voHeatMap.setMinValueFahrenheit(86);
                    voHeatMap.setMaxValueFahrenheit(122);
                } else if (i == 2) {
                    voHeatMap.setHeatName("Medium Temperature");
                    voHeatMap.setMinValue(10);
                    voHeatMap.setMaxValue(30);
                    voHeatMap.setMinValueFahrenheit(50);
                    voHeatMap.setMaxValueFahrenheit(86);
                } else if (i == 3) {
                    voHeatMap.setHeatName("Low Temperature");
                    voHeatMap.setMinValue(-10);
                    voHeatMap.setMaxValue(10);
                    voHeatMap.setMinValueFahrenheit(14);
                    voHeatMap.setMaxValueFahrenheit(50);
                } else if (i == 4) {
                    voHeatMap.setHeatName("Very Low Temperature");
                    voHeatMap.setMinValue(-40);
                    voHeatMap.setMaxValue(-10);
                    voHeatMap.setMinValueFahrenheit(-40);
                    voHeatMap.setMaxValueFahrenheit(14);
                }
                mArrayListHeatMap.add(voHeatMap);
            }
            /*Add default heat map setting in local*/
            PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setAllHeatMapSettingData(mArrayListHeatMap);
            PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).setFirstTimeInstall(false);
        }
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (!isFinishing()) {
                    Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                    mIntent.putExtra("isFromNotification", false);
                    startActivity(mIntent);
                    finish();
                }
            }
        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
