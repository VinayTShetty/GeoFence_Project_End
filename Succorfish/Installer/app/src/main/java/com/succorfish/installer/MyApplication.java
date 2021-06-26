package com.succorfish.installer;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.succorfish.installer.helper.TypefaceUtil;

import io.fabric.sdk.android.Fabric;

/**
 * Created by Jaydeep on 15-02-2018.
 */

public class MyApplication extends Application {

    private static int activityVisible = 0;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        Fabric.with(this, new Crashlytics());
        TypefaceUtil.overrideFont(appContext, "SERIF", "fonts/CenturyGothic.ttf");
    }

    public static Context getAppContext() {
        return appContext;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static int isActivityVisible() {
        return activityVisible;
    }

    public static void activityResumed() {
        ++activityVisible;
    }

    public static void activityPaused() {
        --activityVisible;
    }
}