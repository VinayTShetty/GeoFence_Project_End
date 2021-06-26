package com.succorfish.fisherman;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

/**
 * Created by Jaydeep on 15-02-2018.
 */

public class MyApplication extends Application {

    private static int activityVisible = 0;

    @Override
    public void onCreate() {
        super.onCreate();
//        Fabric.with(this, new Crashlytics());
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