package com.succorfish.installer;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.succorfish.installer.Vo.VoQuestionAns;
import com.succorfish.installer.Vo.VoVessel;
import com.succorfish.installer.db.DBHelper;
import com.succorfish.installer.db.DataHolder;
import com.succorfish.installer.fragment.FragmentDashboard;
import com.succorfish.installer.fragment.FragmentHealthSafetyBase;
import com.succorfish.installer.fragment.FragmentHealthSafetyQuestionOne;
import com.succorfish.installer.fragment.FragmentHealthSafetyQuestionTwo;
import com.succorfish.installer.fragment.FragmentHelp;
import com.succorfish.installer.fragment.FragmentHistory;
import com.succorfish.installer.fragment.FragmentInspectionOne;
import com.succorfish.installer.fragment.FragmentInspectionPhoto;
import com.succorfish.installer.fragment.FragmentInspectionThree;
import com.succorfish.installer.fragment.FragmentInspectionTwo;
import com.succorfish.installer.fragment.FragmentInspections;
import com.succorfish.installer.fragment.FragmentInstallationPhoto;
import com.succorfish.installer.fragment.FragmentNewInstallation;
import com.succorfish.installer.fragment.FragmentNewInstallationOne;
import com.succorfish.installer.fragment.FragmentNewInstallationThree;
import com.succorfish.installer.fragment.FragmentNewInstallationTwo;
import com.succorfish.installer.fragment.FragmentSettings;
import com.succorfish.installer.fragment.FragmentSignature;
import com.succorfish.installer.fragment.FragmentUninstall;
import com.succorfish.installer.fragment.FragmentUninstallOne;
import com.succorfish.installer.fragment.FragmentUninstallTwo;
import com.succorfish.installer.helper.CustomDialog;
import com.succorfish.installer.helper.GPSTracker;
import com.succorfish.installer.helper.PreferenceHelper;
import com.succorfish.installer.helper.URLCLASS;
import com.succorfish.installer.helper.Utility;
import com.succorfish.installer.interfaces.API;
import com.succorfish.installer.interfaces.onAlertDialogCallBack;
import com.succorfish.installer.interfaces.onFragmentBackPress;
import com.succorfish.installer.interfaces.onNewInstallationBackNext;
import com.succorfish.installer.views.NonSwipeableViewPager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.leolin.shortcutbadger.ShortcutBadger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class MainActivity extends AppCompatActivity implements FragmentNewInstallationOne.OnStepOneListener, FragmentNewInstallationTwo.OnStepTwoListener, FragmentNewInstallationThree.OnStepThreeListener, FragmentUninstallOne.OnStepOneListener, FragmentUninstallTwo.OnStepTwoListener, FragmentInspectionOne.OnStepOneListener, FragmentInspectionTwo.OnStepTwoListener, FragmentInspectionThree.OnStepThreeListener, FragmentHealthSafetyQuestionOne.OnStepOneListener, FragmentHealthSafetyQuestionTwo.OnStepTwoListener {
    String TAG = MainActivity.class.getSimpleName();
    public DBHelper mDbHelper;
    public Utility mUtility;

    public RelativeLayout mRelativeLayoutActionBarMain;
    public ImageView mImageViewBack;
    public ImageView mImageViewAdd;
    public TextView mTextViewTitle;
    public TextView mTextViewDone;
    public GPSTracker mGpsTracker;
    @BindView(R.id.activity_main_relativelayout_main)
    public RelativeLayout mRelativeLayoutMain;
    @BindView(R.id.activity_main_appbar_header)
    public AppBarLayout appBarLayout;
    @BindView(R.id.activity_main_toolbar)
    public Toolbar mToolbar;
    @BindView(R.id.activity_main_bottom_navigation)
    public AHBottomNavigation mAhBottomNavigation;
    @BindView(R.id.activity_main_relativelayout_bottom)
    public RelativeLayout mRelativeLayoutBottomMenu;

    FragmentTransaction mFragmentTransaction;

    public SimpleDateFormat mSimpleDateFormatFullUTC;
    private boolean exit = false;
    public Retrofit mRetrofit;
    public API mApiService;
    //    public Retrofit mRetrofitNewAPI;
//    public API mApiServiceNewAPI;
    private static final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 123;

    /*Fragment New Installation Global*/
    public onNewInstallationBackNext mOnNewInstallationBackNext;
    public onFragmentBackPress mOnFragmentBackPress;
    public int mIntInstallationId = 0;
    public int mIntUnInstallationId = 0;
    public int mIntInspectionId = 0;
    public String mStringDeviceAccountId = "";
    public NonSwipeableViewPager mViewPagerQuestion;
    public NonSwipeableViewPager mViewPager;
    public NonSwipeableViewPager mViewPagerInspection;
    public NonSwipeableViewPager mViewPagerInstallation;

    public byte mTestDeviceStatus = 0;
    public byte mInstallUnInstallInspectStatus = 0; // 0-Install,1-Uninstall,2-Inspect
    public String mStringTestDeviceReportedDate = "";
    public boolean mIsTestDeviceCheck = false;
    public ArrayList<VoQuestionAns> mListQuestion = new ArrayList<>();
    //    public ArrayList<VoInstallationPhoto> mArrayListPhotoList = new ArrayList<>();

    //    ContextWrapper localeContextWrapper  = LocaleContextWrapper.wrap(newBase, new Locale("en", "US"));
//    ContextWrapper calligraphyContextWrapper = CalligraphyContextWrapper.wrap(localeContextWrapper);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ButterKnife.bind(MainActivity.this);
        /*Initialize database helper object*/
        mDbHelper = new DBHelper(MainActivity.this);
        /*Initialize utility class object*/
        mUtility = new Utility(MainActivity.this);
        mSimpleDateFormatFullUTC = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        mSimpleDateFormatFullUTC.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            mDbHelper.createDataBase();
            mDbHelper.openDatabase();
        } catch (Exception e) {
            e.printStackTrace();
        }

        initToolbar();
//        setScrollingBehavior(true);
        /*Initialize bottom menu*/
        AHBottomNavigationItem item1 = new AHBottomNavigationItem(R.string.activity_main_menu_dashboard, R.drawable.ic_menu_active_install, R.color.colorActiveMenu);
        AHBottomNavigationItem item2 = new AHBottomNavigationItem(R.string.activity_main_menu_history, R.drawable.ic_menu_active_history, R.color.colorActiveMenu);
        AHBottomNavigationItem item3 = new AHBottomNavigationItem(R.string.activity_main_menu_setting, R.drawable.ic_menu_active_settings, R.color.colorActiveMenu);
        AHBottomNavigationItem item4 = new AHBottomNavigationItem(R.string.activity_main_menu_help, R.drawable.ic_menu_active_help, R.color.colorActiveMenu);

        mAhBottomNavigation.addItem(item1);
        mAhBottomNavigation.addItem(item2);
        mAhBottomNavigation.addItem(item3);
        mAhBottomNavigation.addItem(item4);
        mAhBottomNavigation.setDefaultBackgroundColor(getResources().getColor(R.color.colorAppTheme));
        mAhBottomNavigation.setAccentColor(getResources().getColor(R.color.colorActiveMenu));
        mAhBottomNavigation.setInactiveColor(getResources().getColor(R.color.colorInActiveMenu));
        mAhBottomNavigation.setCurrentItem(0);
        mAhBottomNavigation.setColored(false);
        mAhBottomNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);

        removeAllFragmentFromBack();
        /*Open Dashboard screen*/
        FragmentDashboard mFragmentDashboard = new FragmentDashboard();
        replacesFragment(mFragmentDashboard, false, null, 1);

        /*Initialize retrofit*/
        mRetrofit = new Retrofit.Builder()
                .baseUrl(URLCLASS.SUCCORFISH_URL)
                .client(mUtility.getClientWithAutho())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        mApiService = mRetrofit.create(API.class);
//        mRetrofitNewAPI = new Retrofit.Builder()
//                .baseUrl(URLCLASS.SUCCORFISH_URL)
//                .client(mUtility.getClientWithAutho())
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
//                .build();
//
//        mApiServiceNewAPI = mRetrofitNewAPI.create(API.class);
        /*Bottom Menu Tab Selection listener*/
        mAhBottomNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                if (position == 0) {
                    removeAllFragmentFromBack();
                    FragmentDashboard mFragmentDashboard = new FragmentDashboard();
                    replacesFragment(mFragmentDashboard, false, null, 1);
                } else if (position == 1) {
                    FragmentHistory mFragmentHistory = new FragmentHistory();
                    replacesFragment(mFragmentHistory, false, null, 1);
                } else if (position == 2) {
                    FragmentSettings mFragmentSettings = new FragmentSettings();
                    replacesFragment(mFragmentSettings, false, null, 1);
                } else if (position == 3) {
                    FragmentHelp mFragmentHelp = new FragmentHelp();
                    replacesFragment(mFragmentHelp, false, null, 1);
                }
                return true;
            }
        });
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.activity_main_floating_button_bottom_add);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        setNewInstallationBackNext(new onNewInstallationBackNext() {
            @Override
            public void onInstallFirstBack(Fragment fragment) {

            }

            @Override
            public void onInstallFirstNext(Fragment fragment) {

            }

            @Override
            public void onInstallSecondNext(Fragment fragment) {

            }

            @Override
            public void onInstallSecondBack(Fragment fragment) {

            }

            @Override
            public void onInstallThirdBack(Fragment fragment) {

            }

            @Override
            public void onInstallThirdComplete(Fragment fragment) {

            }
        });
        setOnBackFrgPress(new onFragmentBackPress() {
            @Override
            public void onFragmentBackPress(Fragment mFragment) {

            }
        });
        if (mUtility.haveInternet()) {
//            String url = "SELECT max(" + mDbHelper.mFieldVesselServerId + ") from " + mDbHelper.mTableVesselAsset;
//            int mStringMaxCount = mDbHelper.getMaxVesselRecord(url);
            /*Get All vessel list*/
            getVesselAssetList();
//            getVesselSuccofishAssetList(mStringMaxCount + "");
        }

    }

    /*Get total count of un sync installation, uninstall and inspection*/
    public String getUnSyncedCount() {
        // select (select count(*) from tbl_install where tbl_install.inst_is_sync=0 AND tbl_install.inst_user_id=21)+(select count(*) from tbl_uninstall where tbl_uninstall.uninst_is_sync=0 AND tbl_uninstall.uninst_user_id=21)+(select count(*) from tbl_inspection where tbl_inspection.insp_is_sync=0 AND tbl_inspection.insp_user_id=21) as count;
        String result = "0";
        String urlString = "SELECT (SELECT count(*) from " + mDbHelper.mTableInstall + " where " + mDbHelper.mFieldInstallIsSync + "= 0" + " AND " + mDbHelper.mFieldInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + ")+(SELECT count(*) from " + mDbHelper.mTableUnInstall + " where " + mDbHelper.mFieldUnInstallIsSync + "= 0" + " AND " + mDbHelper.mFieldUnInstallUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + ")+(SELECT count(*) from " + mDbHelper.mTableInspection + " where " + mDbHelper.mFieldInspectionIsSync + "= 0" + " AND " + mDbHelper.mFieldInspectionUserId + "= '" + PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getUserId() + "'" + ") as count";
        result = mDbHelper.getCountRecordByQuery(urlString) + "";
        if (result != null && !result.equalsIgnoreCase("")) {
            if (Integer.parseInt(result) > 0) {
                boolean success = ShortcutBadger.applyCount(MainActivity.this,
                        Integer.parseInt(result));
                System.out.println("badgeCount=" + result + " success= " + success);
            } else {
                ShortcutBadger.removeCount(MainActivity.this);
            }
        } else {
            ShortcutBadger.removeCount(MainActivity.this);
        }
        return result;
    }

    public void setNewInstallationBackNext(onNewInstallationBackNext backNext) {
        this.mOnNewInstallationBackNext = backNext;
    }


    public void setOnBackFrgPress(onFragmentBackPress backpress) {
        this.mOnFragmentBackPress = backpress;
    }

    /*Initialize custom toolbar*/
    private void initToolbar() {
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        mToolbar.setNavigationIcon(R.drawable.ic_drawer_icon);
        View actionBar = getLayoutInflater().inflate(R.layout.custome_actionbar, null);
        mRelativeLayoutActionBarMain = (RelativeLayout) findViewById(R.id.custom_actionbar_relativelayout_main);
        mImageViewBack = (ImageView) actionBar.findViewById(R.id.custom_actionbar_imageview_back);
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
        mToolbar.addView(actionBar);
    }

//    public void setScrollingBehavior(boolean isMapIndex) {
//        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();
//        RelativeLayout.LayoutParams appBarLayoutParams = (RelativeLayout.LayoutParams) appBarLayout.getLayoutParams();
//        if (isMapIndex) {
//            params.setScrollFlags(0);
////            appBarLayoutParams.setBehavior(null);
//            appBarLayout.setLayoutParams(appBarLayoutParams);
//        } else {
//            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
//            appBarLayoutParams.setBehavior(new AppBarLayout.Behavior());
//            appBarLayout.setLayoutParams(appBarLayoutParams);
//        }
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= 23) {
            // Marshmallow+ Permission APIs
            callMarshMallowParmession();
        }
        mGpsTracker = new GPSTracker(MainActivity.this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUtility.HideProgress();
    }

    /*Check required permission */
    @TargetApi(Build.VERSION_CODES.M)
    private void callMarshMallowParmession() {
        List<String> permissionsNeeded = new ArrayList<String>();

        final List<String> permissionsList = new ArrayList<String>();
        if (!addPermission(permissionsList, Manifest.permission.ACCESS_FINE_LOCATION))
            permissionsNeeded.add("Show Location");
        if (!addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE))
            permissionsNeeded.add("Write External Storage");
        if (!addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE))
            permissionsNeeded.add("Read External Storage");
        if (permissionsList.size() > 0) {
            requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                    REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            return;
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean addPermission(List<String> permissionsList, String permission) {

        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(permission);
            // Check for Rationale Option
            if (!shouldShowRequestPermissionRationale(permission))
                return false;
        }
        return true;
    }

    /*On Permission result*/
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS: {
                System.out.println("Granted");
                Map<String, Integer> perms = new HashMap<String, Integer>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++) {
                    perms.put(permissions[i], grantResults[i]);
                }
                // Check for ACCESS_FINE_LOCATION
                if (perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied
                    System.out.println("GrantedACCESS_FINE_LOCATION");


                }
                if (perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied
                }
                if (perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // All Permissions Granted
                    // Permission Denied#858585
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /*On Activity Result*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        System.out.println(TAG + "-----requestCode " + requestCode);
        System.out.println("--ActivityResult RESULTTT--");
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_relativelayout_fragment_container);
        fragment.onActivityResult(requestCode, resultCode, data);
    }

    /*Call API to get all vessel asset list from server and store in local database*/
    public void getVesselAssetList() {
        mUtility.ShowProgress();
        mUtility.hideKeyboard(MainActivity.this);
        Call<String> mLogin = mApiService.getAllAssetList(PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getAccountId(), PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).getAccountId());
        System.out.println("URL-" + mLogin.request().url().toString());
        mLogin.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (!isFinishing()) {
                    Gson gson = new Gson();
                    if (response.code() == 200 || response.isSuccessful()) {
                        System.out.println("response mVesselData---------" + response.body());
                        TypeToken<List<VoVessel>> token = new TypeToken<List<VoVessel>>() {
                        };
                        final List<VoVessel> mVoVesselList = gson.fromJson(response.body(), token.getType());
                        String json = gson.toJson(mVoVesselList);
                        System.out.println("response mLoginData---------" + json);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (mVoVesselList != null) {
                                    if (mVoVesselList.size() > 0) {
                                        String mStringQueryAsset = "delete from " + mDbHelper.mTableVesselAsset;
                                        mDbHelper.exeQuery(mStringQueryAsset);
                                        String mStringQuerySequence = "delete from sqlite_sequence where name" + "= '" + mDbHelper.mTableVesselAsset + "'";
                                        mDbHelper.exeQuery(mStringQuerySequence);
                                        System.out.println("response SIZE---------" + mVoVesselList.size());
                                        String mInsertQuery = "Insert or Replace into " + mDbHelper.mTableVesselAsset + "(" + mDbHelper.mFieldVesselServerId + "," + mDbHelper.mFieldVesselSuccorfishId + "," + mDbHelper.mFieldVesselAccountId + "," + mDbHelper.mFieldVesselName + "," + mDbHelper.mFieldVesselRegNo + "," + mDbHelper.mFieldVesselPortNo + "," + mDbHelper.mFieldVesselCreatedDate + "," + mDbHelper.mFieldVesselUpdatedDate + "," + mDbHelper.mFieldVesselIsSync + ") values(?,?,?,?,?,?,?,?,?)";
                                        mDbHelper.insertMultipleRecord(mInsertQuery, mVoVesselList);
                                        System.out.println("Success");
                                    }
                                }
                            }
                        });
//                        String urlString = "SELECT count(*) from " + mDbHelper.mTableVesselAsset;
//                        String count = mDbHelper.getCountRecordByQuery(urlString) + "";
//                        System.out.println("result-" + count);
                    } else if (response.code() == 401) {
                        mUtility.errorDialogWithCallBack(getResources().getString(R.string.str_logout_session_expired), new onAlertDialogCallBack() {
                            @Override
                            public void PositiveMethod(DialogInterface dialog, int id) {
                                PreferenceHelper.getPreferenceInstance(MyApplication.getAppContext()).ResetPrefData();
                                Intent mIntent = new Intent(MainActivity.this, LoginActivity.class);
                                startActivity(mIntent);
                                finish();
                            }

                            @Override
                            public void NegativeMethod(DialogInterface dialog, int id) {

                            }
                        });
                    } else {
                        mUtility.errorDialog(getResources().getString(R.string.str_server_error_someting_wrong));
                    }
                    mUtility.HideProgress();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                mUtility.HideProgress();
//                showMessageRedAlert(getCurrentFocus(), getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
            }
        });
    }

//    public void getVesselSuccofishAssetList(String maxCount) {
//        mUtility.hideKeyboard(MainActivity.this);
//        Call<List<VoSuccorfishVessel>> mLogin = mApiServiceNewAPI.getVesselList("f1d56000-23ac-11e8-a1c0-90b8d0f72797");
//        System.out.println("URL-" + mLogin.request().url().toString());
//        mLogin.enqueue(new Callback<List<VoSuccorfishVessel>>() {
//            @Override
//            public void onResponse(Call<List<VoSuccorfishVessel>> call, Response<List<VoSuccorfishVessel>> response) {
//                List<VoSuccorfishVessel> mVoVesselList = response.body();
//                Gson gson = new Gson();
//                String json = gson.toJson(mVoVesselList);
//                System.out.println("response mLoginData---------" + json);
//                Calendar cal = Calendar.getInstance();
//                Date currentLocalTime = cal.getTime();
//                for (int i = 0; i < mVoVesselList.size(); i++) {
//                    ContentValues mContentValues = new ContentValues();
////                    mContentValues.put(mDbHelper.mFieldVesselServerId, mVoVesselList.get(i).getId());
//                    mContentValues.put(mDbHelper.mFieldVesselName, mVoVesselList.get(i).getName());
//                    mContentValues.put(mDbHelper.mFieldVesselSuccorfishId, mVoVesselList.get(i).getId());
//                    mContentValues.put(mDbHelper.mFieldVesselAccountId, mVoVesselList.get(i).getAccount_Id());
//                    mContentValues.put(mDbHelper.mFieldVesselRegNo, "");
//                    mContentValues.put(mDbHelper.mFieldVesselPortNo, mVoVesselList.get(i).getReg_no());
//                    mContentValues.put(mDbHelper.mFieldVesselCreatedDate, mDateFormatDb.format(currentLocalTime));
//                    mContentValues.put(mDbHelper.mFieldVesselUpdatedDate, mDateFormatDb.format(currentLocalTime));
//                    mContentValues.put(mDbHelper.mFieldVesselIsSync, "1");
//                    String isExistInDB = CheckSuccorfishRecordExistInVesselDB(mVoVesselList.get(i).getId());
//                    if (isExistInDB.equalsIgnoreCase("-1")) {
//                        int isInsertSocket = mDbHelper.insertRecord(mDbHelper.mTableVesselAsset, mContentValues);
//                        if (isInsertSocket != -1) {
//                            System.out.println("Vessel Added In Local Db");
//                        } else {
//                            System.out.println("Vessel Failed Adding In Local DB");
//                        }
//                    } else {
//                        String[] mArray = new String[]{isExistInDB};
//                        mDbHelper.updateRecord(mDbHelper.mTableVesselAsset, mContentValues, mDbHelper.mFieldVesselServerId + "=?", mArray);
//                        System.out.println("Vessel updated In Local Db");
//                    }
//                }
//            }
//
//            @Override
//            public void onFailure(Call<List<VoSuccorfishVessel>> call, Throwable t) {
////                showMessageRedAlert(getCurrentFocus(), getResources().getString(R.string.str_server_error_try_again), getResources().getString(R.string.str_ok));
//            }
//        });
//    }

    /*Check vessel asset exist or not*/
    public String CheckSuccorfishRecordExistInVesselDB(String serverVesselId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mDbHelper.mTableVesselAsset + " where " + mDbHelper.mFieldVesselSuccorfishId + "= '" + serverVesselId + "'";
        mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" VesselList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldVesselSuccorfishId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    public String CheckRecordExistInVesselDB(String serverVesselId) {
        DataHolder mDataHolder = new DataHolder();
        String url = "select * from " + mDbHelper.mTableVesselAsset + " where " + mDbHelper.mFieldVesselServerId + "= '" + serverVesselId + "'";
        mDataHolder = mDbHelper.read(url);
        if (mDataHolder != null) {
            System.out.println(" VesselList : " + url + " : " + mDataHolder.get_Listholder().size());
            if (mDataHolder != null && mDataHolder.get_Listholder().size() != 0) {
                return mDataHolder.get_Listholder().get(0).get(mDbHelper.mFieldVesselServerId);
            } else {
                return "-1";
            }
        }
        return "-1";
    }

    @Override
    public void onBackOnePressed(Fragment fragment) {
        if (mOnNewInstallationBackNext != null) {
            mOnNewInstallationBackNext.onInstallFirstBack(fragment);
        }
    }

    @Override
    public void onNextPressed(Fragment fragment) {
        if (mOnNewInstallationBackNext != null) {
            mOnNewInstallationBackNext.onInstallFirstNext(fragment);
        }
    }

    @Override
    public void onBackTwoPressed(Fragment fragment) {
        if (mOnNewInstallationBackNext != null) {
            mOnNewInstallationBackNext.onInstallSecondBack(fragment);
        }
    }

    @Override
    public void onNextTwoPressed(Fragment fragment) {
        if (mOnNewInstallationBackNext != null) {
            mOnNewInstallationBackNext.onInstallSecondNext(fragment);
        }
    }

    @Override
    public void onBackThreePressed(Fragment fragment) {
        if (mOnNewInstallationBackNext != null) {
            mOnNewInstallationBackNext.onInstallThirdBack(fragment);
        }
    }

    @Override
    public void onNextThreePressed(Fragment fragment) {
        if (mOnNewInstallationBackNext != null) {
            mOnNewInstallationBackNext.onInstallThirdComplete(fragment);
        }
    }

    /*remove all fragment from back stack*/
    public void removeAllFragmentFromBack() {
        getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void removeAllFragmentFromBack(String tagName) {
        getSupportFragmentManager().popBackStack(tagName, FragmentManager.POP_BACK_STACK_INCLUSIVE);
    }

    public void removeNumberOfFragmnet(int num) {
        for (int i = 0; i < num; ++i) {
            getSupportFragmentManager().popBackStack();
        }
    }

    /*replace fragment to change fragment screen*/
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

    /*replace fragment to change fragment screen*/
    public void replacesFragment(Fragment mFragment, boolean isBackState, String tagName, Bundle mBundle, int animationType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragmentTransaction = fragmentManager.beginTransaction();
        if (isBackState) {
            mFragmentTransaction.addToBackStack(tagName);
        } else {
            mFragmentTransaction.addToBackStack(null);
        }
        if (mBundle != null)
            mFragment.setArguments(mBundle);

        mFragmentTransaction.replace(R.id.activity_main_relativelayout_fragment_container, mFragment);
        mFragmentTransaction.commitAllowingStateLoss();
    }

    /*replace fragment to change fragment screen*/
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

    public void addFragment(Fragment mFragment, boolean isBackState,
                            Bundle mBundle) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        mFragmentTransaction = fragmentManager.beginTransaction();

        if (isBackState)
            mFragmentTransaction.addToBackStack(null);

        if (mBundle != null)
            mFragment.setArguments(mBundle);

        mFragmentTransaction.add(R.id.activity_main_relativelayout_fragment_container,
                mFragment);
        mFragmentTransaction.commitAllowingStateLoss();

    }

    public void onBackPressedDirect() {
        getSupportFragmentManager().popBackStack();

    }

    /*Handle App back press event*/
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        Fragment mFragment = getSupportFragmentManager().findFragmentById(R.id.activity_main_relativelayout_fragment_container);
        if (count > 0) {
            if (mFragment instanceof FragmentDashboard) {
                if (mFragment.getChildFragmentManager().getBackStackEntryCount() > 0) {
                    mFragment.getChildFragmentManager().popBackStack();
                } else {
                    getSupportFragmentManager().popBackStack();
                }
            } else if (mFragment instanceof FragmentNewInstallation || mFragment instanceof FragmentUninstall || mFragment instanceof FragmentInspections || mFragment instanceof FragmentHealthSafetyBase) {
                if (mOnFragmentBackPress != null) {
                    mOnFragmentBackPress.onFragmentBackPress(mFragment);
                }
            } else if (mFragment instanceof FragmentInstallationPhoto || mFragment instanceof FragmentInspectionPhoto || mFragment instanceof FragmentSignature) {
                if (mOnFragmentBackPress != null) {
                    mOnFragmentBackPress.onFragmentBackPress(mFragment);
                }
            } else {
                getSupportFragmentManager().popBackStack();
            }
        } else {
            if (mFragment instanceof FragmentDashboard) {
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
                mAhBottomNavigation.setCurrentItem(0);
                FragmentDashboard mFragmentDashboard = new FragmentDashboard();
                replacesFragment(mFragmentDashboard, false, null, 0);
            }
        }
    }
}
