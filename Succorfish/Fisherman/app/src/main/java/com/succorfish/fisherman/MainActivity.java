package com.succorfish.fisherman;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.ArrayMap;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.succorfish.fisherman.Vo.VoVessel;
import com.succorfish.fisherman.adapter.SmartFragmentStatePagerAdapter;
import com.succorfish.fisherman.fragments.FragmentAsset;
import com.succorfish.fisherman.fragments.FragmentHelp;
import com.succorfish.fisherman.helper.CustomDialog;
import com.succorfish.fisherman.helper.PreferenceHelper;
import com.succorfish.fisherman.helper.URLCLASS;
import com.succorfish.fisherman.helper.Utility;
import com.succorfish.fisherman.interfaces.API;
import com.succorfish.fisherman.interfaces.onAlertDialogCallBack;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity {
    String TAG = MainActivity.class.getSimpleName();
    //    public View mViewMainContainer;
    public Utility mUtility;
    public PreferenceHelper mPreferenceHelper;

    public RelativeLayout mRelativeLayoutActionBarMain;
    public ImageView mImageViewBack;
    public ImageView mImageViewDrawer;
    public ImageView mImageViewHeaderLogo;
    public ImageView mImageViewAdd;
    public TextView mTextViewTitle;
    public TextView mTextViewDone;


    @BindView(R.id.activity_main_relativelayout_main)
    public RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.activity_main_appbar_header)
    public AppBarLayout appBarLayout;
    @BindView(R.id.activity_main_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.activity_main_rl_container)
    public RelativeLayout mRelativeLayoutContainer;
    public Retrofit mRetrofit;
    public API mApiService;
    //    public SimpleDateFormat mSimpleDateFormatFullUTC;
    FragmentTransaction mFragmentTransaction;
    private boolean exit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(MainActivity.this);
        mUtility = new Utility(MainActivity.this);
        mPreferenceHelper = new PreferenceHelper(MainActivity.this);
//        mSimpleDateFormatFullUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        mSimpleDateFormatFullUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        initToolbar();
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.SUCCORFISH_URL)
                .client(mUtility.getClientWithAutho())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);
        replacesFragment(new FragmentAsset(), false, null, 0);

    }

    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        mToolbar.setNavigationIcon(R.drawable.ic_drawer_icon);
        View actionBar = getLayoutInflater().inflate(R.layout.custome_actionbar, null);
        mRelativeLayoutActionBarMain = (RelativeLayout) findViewById(R.id.custom_actionbar_relativelayout_main);
        mImageViewBack = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_back);
        mImageViewDrawer = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_drawer);
        mImageViewHeaderLogo = (ImageView) actionBar.findViewById(R.id.custom_actionbar_iv_logo);
        mImageViewAdd = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_add);
        mTextViewTitle = (TextView) actionBar.findViewById(R.id.custom_actionbar_imageview_title);
        mTextViewDone = (TextView) actionBar.findViewById(R.id.custom_actionbar_textview_add);

        mImageViewBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mUtility.hideKeyboard(MainActivity.this);
                onBackPressed();
            }
        });

        mImageViewDrawer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUtility.hideKeyboard(MainActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this, R.style.AppCompatAlertDialogStyle);
                builder.setTitle(getResources().getString(R.string.str_logout));
                builder.setCancelable(false);
                builder.setMessage(getResources().getString(R.string.str_logout_confirmation));
                builder.setPositiveButton(getResources().getString(R.string.str_yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mPreferenceHelper.ResetPrefData();
                        Intent mIntent = new Intent(MainActivity.this, LoginActivity.class);
                        mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(mIntent);
                        finishAffinity();
                    }
                });
                builder.setNegativeButton(getResources().getString(R.string.str_no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });

        mToolbar.addView(actionBar);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(TAG + "-----requestCode " + requestCode);
        System.out.println("--ActivityResult RESULTTT--");
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_relativelayout_fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

//    private void removeNumberOfFragmnet(int num) {
//        for (int i = 0; i < num; ++i) {
//            getSupportFragmentManager().popBackStack();
//        }
//    }

    public void replacesFragment(Fragment mFragment, boolean isBackState, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragmentTransaction = fragmentManager.beginTransaction();

        if (isBackState)
            mFragmentTransaction.addToBackStack(null);

        if (mBundle != null)
            mFragment.setArguments(mBundle);

        mFragmentTransaction.replace(R.id.activity_main_relativelayout_fragment_container, mFragment);
        mFragmentTransaction.commitAllowingStateLoss();
    }

    public void replacesFragment(Fragment mOldFragment, Fragment mFragment, boolean isBackState, Bundle mBundle) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState)
            mFragmentTransaction.addToBackStack(null);
        if (mBundle != null)
            mFragment.setArguments(mBundle);
        // fragmentTransaction.replace(R.id.activity_main_fragment_container,
        // mFragment);
        // fragmentTransaction.commitAllowingStateLoss();
        mFragmentTransaction.hide(mOldFragment);
        mFragmentTransaction.add(R.id.activity_main_relativelayout_fragment_container,
                mFragment);
        mFragmentTransaction.addToBackStack(null);
        mFragmentTransaction
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        mFragmentTransaction.commitAllowingStateLoss();
    }

//    public void addFragment(Fragment mFragment, boolean isBackState,
//                            Bundle mBundle) {
//
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        mFragmentTransaction = fragmentManager.beginTransaction();
//
//        if (isBackState)
//            mFragmentTransaction.addToBackStack(null);
//
//        if (mBundle != null)
//            mFragment.setArguments(mBundle);
//
//        mFragmentTransaction.add(R.id.activity_main_relativelayout_fragment_container,
//                mFragment);
//        mFragmentTransaction.commitAllowingStateLoss();
//
//    }

//    public void onBackPressedDirect() {
//        getSupportFragmentManager().popBackStack();
//    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_relativelayout_fragment_container);
        if (count > 0) {
            if (mFragment instanceof FragmentAsset) {
                if (exit) {
                    CustomDialog.ExitDialog(MainActivity.this, getString(R.string.str_exit_confirmation));
                } else {
                    exit = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, 2000);
                }
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            if (mFragment instanceof FragmentAsset) {
                if (exit) {
                    CustomDialog.ExitDialog(MainActivity.this, getString(R.string.str_exit_confirmation));
                } else {
                    exit = true;
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            exit = false;
                        }
                    }, 2000);
                }
            } else {
                FragmentAsset mFragmentAsset = new FragmentAsset();
                replacesFragment(mFragmentAsset, false, null, 0);
            }
        }
    }
}
