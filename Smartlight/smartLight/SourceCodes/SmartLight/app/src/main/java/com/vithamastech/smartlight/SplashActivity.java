package com.vithamastech.smartlight;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.iid.FirebaseInstanceId;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.helper.PreferenceHelper;

/**
 * Created by Jaydeep on 21-12-2017.
 * Changed by Muataz Medini on 16-01-2021
 */

public class SplashActivity extends AppCompatActivity {

    TextView mTextView;
    DBHelper mDbHelper;
    private PreferenceHelper mPreferenceHelper;
    ImageView imageView;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_splash);

        mPreferenceHelper = new PreferenceHelper(SplashActivity.this);
        imageView = findViewById(R.id.activity_splash_imageview_bg);
        mTextView = findViewById(R.id.activity_splash_textview_tag);
        mDbHelper = DBHelper.getDBHelperInstance(SplashActivity.this);
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        FirebaseApp.initializeApp(this);

        Animation bounceLeftToRightAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_left_to_right);
        Animation bounceTopToBottomAnim = AnimationUtils.loadAnimation(this, R.anim.bounce_top_to_bottom);

        imageView.setAnimation(bounceTopToBottomAnim);
        mTextView.setAnimation(bounceLeftToRightAnim);

        try {
            FirebaseApp.initializeApp(this);
            String mStringDevicesFCMToken = FirebaseInstanceId.getInstance().getToken();
            mPreferenceHelper.setDeviceToken(mStringDevicesFCMToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            mDbHelper.createDataBase();
            mDbHelper.openDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        new CountDownTimer(4000, 1000) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                if (!isFinishing()) {
                    if (mPreferenceHelper.getIsSkipUser()) {
                        Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                        mIntent.putExtra("isFromLogin", false);
                        mIntent.putExtra("isFromNotification", false);
                        startActivity(mIntent);
                        finishAffinity();
                    } else {
                        if (mPreferenceHelper.getUserId().equalsIgnoreCase("")) {
                            if (mPreferenceHelper.getIsShowIntro()) {
                                Intent mIntent = new Intent(SplashActivity.this, AppIntroActivity.class);
                                mIntent.putExtra("isFirstTime", true);
                                startActivity(mIntent);
                                finishAffinity();
                            } else {
                                Intent mIntent = new Intent(SplashActivity.this, LoginExplainActivity.class);
                                mIntent.putExtra("is_from_add_account", false);
                                startActivity(mIntent);
                                finishAffinity();
                            }
                        } else {
                            Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                            mIntent.putExtra("isFromNotification", false);
                            mIntent.putExtra("isFromLogin", false);
                            startActivity(mIntent);
                            finishAffinity();
                        }
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MyApplication.activityResumed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        MyApplication.activityPaused();
    }
}