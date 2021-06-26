package com.succorfish.fisherman;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.succorfish.fisherman.helper.PreferenceHelper;


/**
 * Created by Jaydeep on 21-12-2017.
 */

public class SplashActivity extends AppCompatActivity {
    private PreferenceHelper mPreferenceHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mPreferenceHelper = new PreferenceHelper(SplashActivity.this);

        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (!isFinishing()) {
                    if (mPreferenceHelper.getUserId().equalsIgnoreCase("")) {
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
