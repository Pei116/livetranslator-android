package com.wwk.livetranslator.manager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.service.TranslationService;

/**
 * Created by wwk on 11/12/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class TranslationManager {

    private static final String TAG = TranslationManager.class.getSimpleName();

    private static volatile TranslationManager instance;

    // Private constructor
    private TranslationManager() {

        // Prevent form the reflection api
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static TranslationManager getInstance() {
        // Double check locking pattern
        if (instance == null) { //Check for the first time

            synchronized (TranslationManager.class) {   //Check for the second time
                if (instance == null) instance = new TranslationManager();
            }
        }

        return instance;
    }

    private ClipboardManager.OnPrimaryClipChangedListener changedListener = () -> {
        final ClipboardManager clipboardManager = (ClipboardManager) Application.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence chars = clipData.getItemAt(clipData.getItemCount() - 1).getText();
                if (chars != null) {
                    String text = clipData.getItemAt(clipData.getItemCount() - 1).getText().toString();
                    Toast.makeText(Application.getInstance(), "Copied: " + text, Toast.LENGTH_LONG).show();

                    OverlayWindowManager.getInstance().showButtonOverlay(Application.getInstance());
                }
            }
        }
    };

    public void registerClipboardListener() {
        ClipboardManager clipboardManager = (ClipboardManager) Application.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.addPrimaryClipChangedListener(changedListener);
            Log.d(TAG, "Registered clipboard listener");
        }
    }

    public void removeClipboardListener() {
        ClipboardManager clipboardManager = (ClipboardManager) Application.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            clipboardManager.removePrimaryClipChangedListener(changedListener);
            Log.d(TAG, "Removed clipboard listener");
        }
    }

    public void scheduleJob(Context context) {
        ComponentName serviceName = new ComponentName(context, TranslationService.class);
        JobInfo.Builder builder = new JobInfo.Builder(0, serviceName);

        builder.setMinimumLatency(1000);
        builder.setOverrideDeadline(100);
        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY);
        builder.setPersisted(true);

        // Schedule job
        JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        if (jobScheduler != null) {
            Log.d(TAG, "Scheduling translator job...");
            jobScheduler.schedule(builder.build());
        }
    }

    public interface ClipboardChangeListener{
        void clipboardChanged();
    }

}
