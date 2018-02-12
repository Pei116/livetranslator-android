package com.wwk.livetranslator;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.wwk.livetranslator.helper.BuildInfo;
import com.wwk.livetranslator.manager.TranslationManager;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Pei on 7/25/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class Application extends android.app.Application {

    private static final String TAG = Application.class.getSimpleName();

    private static Application instance;

    public static Application getInstance() {
        return instance ;
    }

    MyActivityLifecycleCallbacks lifecycleCallbacks;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        initApp();
    }

    private void initApp() {
        lifecycleCallbacks = new MyActivityLifecycleCallbacks();
        registerActivityLifecycleCallbacks(lifecycleCallbacks);

        TranslationManager.getInstance().scheduleJob(this);

        if (!BuildInfo.isDevelopment()) {
            Fabric.with(this, new Crashlytics());
        }

        Realm.init(this);

        if (!BuildInfo.isProduction()) {
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            Realm.setDefaultConfiguration(config);
        } else {
            // TODO: Realm migration in production builds if needed?
        }
    }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        if (manager == null) return false;

        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public Activity getCurrentActivity() {
        return lifecycleCallbacks.currentActivity;
    }

    public boolean isVisible() {
        return lifecycleCallbacks.started > lifecycleCallbacks.stopped;
    }

    public boolean isInForeground() {
        return lifecycleCallbacks.resumed > lifecycleCallbacks.paused;
    }

    private final class MyActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        private int resumed;
        private int paused;
        private int started;
        private int stopped;
        private Activity currentActivity;

        public void onActivityCreated(Activity activity, Bundle bundle) {
            Log.i(TAG, "onActivityCreated:" + activity.getLocalClassName());
            // Force activities in portrait mode only
            activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        public void onActivityDestroyed(Activity activity) {
            Log.i(TAG, "onActivityDestroyed:" + activity.getLocalClassName());
            if (activity == currentActivity) {
                currentActivity = null;
            }
        }

        public void onActivityPaused(Activity activity) {
            Log.i(TAG, "onActivityPaused:" + activity.getLocalClassName());
            if (activity == currentActivity) {
                currentActivity = null;
            }
            ++paused;
        }

        public void onActivityResumed(Activity activity) {
            currentActivity = activity;
            ++resumed;
            Log.i(TAG, "onActivityResumed:" + activity.getLocalClassName());
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
            Log.i(TAG, "onActivitySaveInstanceState:" + activity.getLocalClassName());
        }

        public void onActivityStarted(Activity activity) {
            Log.i(TAG, "onActivityStarted:" + activity.getLocalClassName());
            ++started;
        }

        public void onActivityStopped(Activity activity) {
            Log.i(TAG, "onActivityStopped:" + activity.getLocalClassName());
            ++stopped;
        }
    }
}
