package com.vithamastech.smartlight;

import android.app.Application;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.content.Context;
import android.content.Intent;

import androidx.multidex.MultiDex;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatDelegate;

/**
 * Created by Jaydeep on 21-12-2017.
 */

public class MyApplication extends Application implements LifecycleObserver {

    private static int activityVisible = 0;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

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

    /* Check app stop or no*/
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onActivityOnStop() {
        final Intent intent = new Intent("ApplicationArch");
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        intent.putExtra("App_State", "ON_STOP");
        broadcastManager.sendBroadcast(intent);

    }

    /* Check app start or no*/
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onActivityOnStart() {
        final Intent intent = new Intent("ApplicationArch");
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        intent.putExtra("App_State", "ON_START");
        broadcastManager.sendBroadcast(intent);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onActivityDestroyed() {
        final Intent intent = new Intent("ApplicationArch");
        final LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(getApplicationContext());
        intent.putExtra("App_State", "ON_DESTROY");
        broadcastManager.sendBroadcast(intent);
    }
}