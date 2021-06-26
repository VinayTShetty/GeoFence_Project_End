package com.succorfish.installer;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.succorfish.installer.db.DBHelper;
import com.succorfish.installer.helper.PreferenceHelper;


/**
 * Created by Jaydeep on 21-12-2017.
 */

public class SplashActivity extends AppCompatActivity {
    DBHelper mDbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (DBHelper.DB_VESRION < 2) {
            // force fully logout version 1 for phase 2
            PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).ResetPrefData();
        }
        mDbHelper = new DBHelper(SplashActivity.this);

        try {
            mDbHelper.createDataBase();
            mDbHelper.openDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (!isFinishing()) {
                    /*Check User is login or not*/
                    if (PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId().equalsIgnoreCase("")) {
                        Intent mIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(mIntent);
                        finish();
                    } else {
                        Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                        mIntent.putExtra("isFromNotification", false);
                        startActivity(mIntent);
                        finish();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onResume() {
        super.onResume();
    }


}
