package com.wwk.livetranslator.manager;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;

import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.Constants;

import java.util.UUID;

/**
 * Created by wwk on 8/15/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */

public class InstallationManager {

    private static final String TAG = InstallationManager.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private static volatile InstallationManager instance;

    private String deviceId;

    private InstallationManager() {
        // Prevent form the reflection api
        if (instance != null){
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
        deviceId = sharedPrefs.getString(Constants.PREF_DEVICE_ID, null);
    }

    public static InstallationManager getInstance() {
        // Double check locking pattern
        if (instance == null) { // Check for the first time

            synchronized (InstallationManager.class) {   // Check for the second time
                if (instance == null) instance = new InstallationManager();
            }
        }

        return instance;
    }

    public String getDeviceId() {
        if (deviceId == null) {
            deviceId = Settings.Secure.getString(Application.getInstance().getContentResolver(), Settings.Secure.ANDROID_ID);
            if (deviceId == null)
                deviceId = createUniqueID();
            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
            SharedPreferences.Editor editor = sharedPrefs.edit();
            editor.putString(Constants.PREF_DEVICE_ID, deviceId);
            editor.apply();
        }

        return deviceId;
    }

    private static String createUniqueID() {
        return UUID.randomUUID().toString();
    }

}
