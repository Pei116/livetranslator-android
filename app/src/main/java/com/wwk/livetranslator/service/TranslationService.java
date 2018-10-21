package com.wwk.livetranslator.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.wwk.livetranslator.Constants;
import com.wwk.livetranslator.R;
import com.wwk.livetranslator.manager.OverlayWindowManager;
import com.wwk.livetranslator.manager.TranslationManager;

/**
 * Created by wwk on 11/12/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class TranslationService extends JobService {

    private static final String TAG = TranslationService.class.getSimpleName();

    private static final String CHANNEL_ID = "translation_service";
    private static final String CHANNEL_NAME = "Live Translator";
    private static final int ID_SERVICE = 101;

    @Override
    public void onCreate() {
        super.onCreate();

        buildNotification();
        TranslationManager.getInstance().registerClipboardListener();
        Log.i(TAG, "Service created");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        TranslationManager.getInstance().removeClipboardListener();
//        TranslationManager.getInstance().scheduleJob(this);
        OverlayWindowManager.getInstance().hideButtonOverlay();
        Log.i(TAG, "Service destroyed");
    }

    /**
     * When the app's MainActivity is created, it starts this service. This is so that the
     * activity and this service can communicate back and forth. See "setUiCallback()"
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        if (Constants.ACTION_OPEN_SERVICE.equals(action)) {
            OverlayWindowManager.getInstance().checkAndShowMainOverlay(this);
        } else if (Constants.ACTION_STOP_SERVICE.equals(action)) {
            stopSelf();
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

    @RequiresApi(Build.VERSION_CODES.O)
    private String prepareNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            NotificationChannel channel = notificationManager.getNotificationChannel(CHANNEL_ID);

            if (channel == null) {
                channel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW);
                notificationManager.createNotificationChannel(channel);
            }
            return CHANNEL_ID;
        }
        return "";
    }

    private void buildNotification() {
        // Create the Foreground Service
        String channelId = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ? prepareNotificationChannel() : "";
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, channelId);

        Intent openIntent = new Intent(this, TranslationService.class);
        openIntent.setAction(Constants.ACTION_OPEN_SERVICE);
        PendingIntent openPendingIntent = PendingIntent.getService(this, 0, openIntent, 0);

        Intent stopIntent = new Intent(this, TranslationService.class);
        stopIntent.setAction(Constants.ACTION_STOP_SERVICE);
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        Notification notification = notificationBuilder.setOngoing(true)
                .setSmallIcon(R.mipmap.ic_status_bar)
                .setContentTitle(getString(R.string.notification_title))
                .setContentText(getString(R.string.notification_open))
                .setContentIntent(openPendingIntent)
                .addAction(0, getString(R.string.action_stop), stopPendingIntent)
                .build();

        startForeground(ID_SERVICE, notification);
    }
}
