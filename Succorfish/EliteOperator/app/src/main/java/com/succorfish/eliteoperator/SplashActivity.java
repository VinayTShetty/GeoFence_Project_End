package com.succorfish.eliteoperator;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.succorfish.eliteoperator.db.DBHelper;

import java.io.IOException;

/**
 * Created by Jaydeep on 16-01-2018.
 */

public class SplashActivity extends AppCompatActivity {

    DBHelper mDbHelper;
//    Animation mAnimation;
    ImageView mImageViewLogo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        mDbHelper = new DBHelper(SplashActivity.this);
//        mAnimation = AnimationUtils.loadAnimation(this, R.anim.alpha);
//        mAnimation.reset();
        mImageViewLogo = (ImageView) findViewById(R.id.activity_splash_imageview_logo);
        try {
            mDbHelper.createDataBase();
            mDbHelper.openDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {
//                if (mImageViewLogo.getVisibility() == View.GONE) {
//                    mImageViewLogo.setVisibility(View.VISIBLE);
//                    mImageViewLogo.startAnimation(mAnimation);
//                }
            }

            @Override
            public void onFinish() {
                Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(mIntent);
                finish();
            }
        }.start();
    }
}
