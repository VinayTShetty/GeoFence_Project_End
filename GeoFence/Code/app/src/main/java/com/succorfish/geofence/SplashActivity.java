package com.succorfish.geofence;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import com.succorfish.geofence.helper.PreferenceHelper;

public class SplashActivity extends AppCompatActivity {
    PreferenceHelper preferenceHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        spalashScreenAnimation();
        intializePreferenceHelper();
    }

    private void intializePreferenceHelper() {
        preferenceHelper=PreferenceHelper.getPreferenceInstance(getApplicationContext());
    }

    private void spalashScreenAnimation(){
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {
            }
            @Override
            public void onFinish() {
                if (!isFinishing()) {
                    if((preferenceHelper.get_Remember_me_Checked())&&(preferenceHelper.get_PREF_remember_me_userName().length()>0)&&(preferenceHelper.get_PREF_remember_password().length()>0)){
                        /**
                         * Remember me checked,
                         */
                        Intent mIntent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(mIntent);
                        finish();
                    } else if((!preferenceHelper.get_Remember_me_Checked())&&(preferenceHelper.get_password().length()>0)&&(preferenceHelper.get_password().length()>0)){
                        /**
                         * Remenber me not cheked Just Login
                         */
                        Intent mIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(mIntent);
                        finish();
                    }else {
                        /**
                         * Normal Login
                         */
                        Intent mIntent = new Intent(SplashActivity.this, LoginActivity.class);
                        startActivity(mIntent);
                        finish();
                    }
                }
            }
        }.start();
    }

    private void reset_Preference_Helper(){
        PreferenceHelper.getPreferenceInstance(getApplicationContext()).resetPreferenceData();
    }
}
