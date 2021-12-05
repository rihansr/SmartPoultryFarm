package com.rs.smartpoultryfarm.controller;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

@SuppressLint("StaticFieldLeak")
public class AppController extends MultiDexApplication {

    public static Context   context = AppController.getContext();
    private static Activity activity;

    @Override
    public void onCreate() {
        super.onCreate();
        AppController.context = getApplicationContext();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    public static Context getContext() {
        return context;
    }

    public static void setActivity(Activity currentActivity) {
        activity = currentActivity;
    }

    public static Activity getActivity() {
        return activity;
    }
}
