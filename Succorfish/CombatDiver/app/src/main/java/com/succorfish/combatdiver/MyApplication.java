package com.succorfish.combatdiver;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

import com.succorfish.combatdiver.helper.TypefaceUtil;

/**
 * Created by Jaydeep on 16-01-2018.
 */

public class MyApplication extends Application {

    private static int activityVisible = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        TypefaceUtil.overrideFont(getApplicationContext(), "SERIF", "fonts/CenturyGothic.ttf");
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