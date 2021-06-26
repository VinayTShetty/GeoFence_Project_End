package com.succorfish.depthntemp;

import android.app.Application;
import android.arch.lifecycle.LifecycleObserver;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Jaydeep on 21-12-2017.
 */

public class MyApplication extends Application implements LifecycleObserver {

    private static int activityVisible = 0;
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getAppContext() {
        return appContext;
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