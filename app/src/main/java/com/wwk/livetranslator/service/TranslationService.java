package com.wwk.livetranslator.service;

import android.app.Notification;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.wwk.livetranslator.R;
import com.wwk.livetranslator.manager.OverlayWindowManager;
import com.wwk.livetranslator.manager.TranslationManager;

/**
 * Created by wwk on 11/12/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class TranslationService extends JobService {

    private static final String TAG = TranslationService.class.getSimpleName();

    private static final String CHANNEL_TRANSLATION_SERVICE = "translation_service";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Service created");

        TranslationManager.getInstance().registerClipboardListener();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        TranslationManager.getInstance().removeClipboardListener();
        TranslationManager.getInstance().scheduleJob(this);
        OverlayWindowManager.getInstance().hideButtonOverlay();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Must start foreground service in Android Oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder builder = new Notification.Builder(this, CHANNEL_TRANSLATION_SERVICE)
                    .setContentTitle(getString(R.string.app_name))
                    .setAutoCancel(true);

            Notification notification = builder.build();
            startForeground(0, notification);
        }
        return START_STICKY;
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int jobId = params.getJobId();
            Log.i(TAG, "on start job: " + jobId);
        }
        else {
            Log.i(TAG, "on start job");
        }

        // Return true as there's more work to be done with this job.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int jobId = params.getJobId();
            Log.i(TAG, "on stop job: " + jobId);
        }
        else {
            Log.i(TAG, "on stop job");
        }

        // Return false to drop the job.
        return false;
    }
}
