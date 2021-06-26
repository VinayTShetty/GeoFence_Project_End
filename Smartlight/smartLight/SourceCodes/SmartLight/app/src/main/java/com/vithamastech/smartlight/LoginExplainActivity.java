package com.vithamastech.smartlight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.vithamastech.smartlight.DialogFragementHelper.ActionBottomDialogFragment;
import com.vithamastech.smartlight.db.DBHelper;
import com.vithamastech.smartlight.db.DataHolder;
import com.vithamastech.smartlight.helper.BLEUtility;
import com.vithamastech.smartlight.helper.PreferenceHelper;
import com.vithamastech.smartlight.helper.URLCLASS;

import java.security.NoSuchAlgorithmException;

import butterknife.OnClick;

public class LoginExplainActivity extends AppCompatActivity {

    private ImageView imageViewToolTipInfo;
    private Button buttonNext;
    private RadioButton radioButtonLoginUser;  // YES
    private RadioButton radioButtonSkipUser;   // NO
    private PreferenceHelper mPreferenceHelper;
    String mStringGuestUserId = "0000";
    DBHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_explain);
        mDbHelper = DBHelper.getDBHelperInstance(LoginExplainActivity.this);
        mPreferenceHelper = new PreferenceHelper(LoginExplainActivity.this);
        imageViewToolTipInfo = findViewById(R.id.imageViewToolTip);
        buttonNext = findViewById(R.id.buttonNext);
        radioButtonLoginUser = findViewById(R.id.radioButtonLoginUser);
        radioButtonSkipUser = findViewById(R.id.radioButtonSkipUser);

        imageViewToolTipInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBottomSheet(v);
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioButtonLoginUser.isChecked()) {
                    Intent mIntent = new Intent(LoginExplainActivity.this, LoginActivity.class);
                    mIntent.putExtra("is_from_add_account", false);
                    startActivity(mIntent);
                    finishAffinity();
                } else if (radioButtonSkipUser.isChecked()) {
                    mPreferenceHelper.setUserId(mStringGuestUserId);
                    mPreferenceHelper.setUserFirstName("Guest User");
                    mPreferenceHelper.setIsSkipUser(true);
                    try {
                        String hash256 = BLEUtility.hash256Encryption(URLCLASS.APP_SKIP_PW);
                        String mHashSecretKey = (hash256.length() >= 32) ? hash256.substring(hash256.length() - 32, hash256.length()) : hash256;
                        mPreferenceHelper.setSecretKey(mHashSecretKey);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    // Add Default 6 alarm entry
                    String isExistInDB;
                    for (int i = 0; i < 6; i++) {
                        isExistInDB = CheckRecordExistInAlarmDB(i + "");
                        if (isExistInDB.equalsIgnoreCase("-1")) {
                            ContentValues mContentValues = new ContentValues();
                            mContentValues.put(mDbHelper.mFieldAlarmUserId, mPreferenceHelper.getUserId());
                            mContentValues.put(mDbHelper.mFieldAlarmName, "Alarm " + (i + 1));
                            mContentValues.put(mDbHelper.mFieldAlarmTime, "");
                            mContentValues.put(mDbHelper.mFieldAlarmStatus, "1");
                            mContentValues.put(mDbHelper.mFieldAlarmDays, "6543210");
                            mContentValues.put(mDbHelper.mFieldAlarmLightOn, "0");
                            mContentValues.put(mDbHelper.mFieldAlarmWakeUpSleep, "0");
                            mContentValues.put(mDbHelper.mFieldAlarmColor, 0);
                            mContentValues.put(mDbHelper.mFieldAlarmCountNo, i);
                            mContentValues.put(mDbHelper.mFieldAlarmIsActive, "0");
                            mContentValues.put(mDbHelper.mFieldAlarmIsSync, "0");
                            mDbHelper.insertRecord(mDbHelper.mTableAlarm, mContentValues);
                        }
                    }

                    Intent mIntent = new Intent(getApplicationContext(), MainActivity.class);
                    mIntent.putExtra("isFromLogin", true);
                    startActivity(mIntent);
                    finish();
                }
            }
        });
    }

    /* Check record is exist in alarm table or not*/
    private String CheckRecordExistInAlarmDB(String alarmCount) {
        DataHolder mDataHolder;
        String url = "select * from " + mDbHelper.mTableAlarm + " where " + mDbHelper.mFieldAlarmUserId + "= '" + mPreferenceHelper.getUserId() + "'" + " and " + mDbHelper.mFieldAlarmCountNo + "= '" + alarmCount + "'";
        System.out.println(" URL : " + url);
        mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" AlarmList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldAlarmLocalID);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    public void showBottomSheet(View view) {
        ActionBottomDialogFragment addPhotoBottomDialogFragment =
                ActionBottomDialogFragment.newInstance();
        addPhotoBottomDialogFragment.show(getSupportFragmentManager(),
                ActionBottomDialogFragment.TAG);
    }
}