package com.succorfish.installer;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;



/**
 * Created by Jaydeep on 26-03-2018.
 * Unused Activity
 */

public class CustomErrorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_error);
        Button restartButton = findViewById(R.id.restart_button);
//        final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());
//        if (config == null) {
//            //This should never happen - Just finish the activity to avoid a recursive crash.
//            finish();
//            return;
//        }
//
//        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
//            restartButton.setText(R.string.restart_app);
//            restartButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    CustomActivityOnCrash.restartApplication(CustomErrorActivity.this, config);
//                }
//            });
//        } else {
//            restartButton.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    CustomActivityOnCrash.closeApplication(CustomErrorActivity.this, config);
//                }
//            });
//        }
    }
}
