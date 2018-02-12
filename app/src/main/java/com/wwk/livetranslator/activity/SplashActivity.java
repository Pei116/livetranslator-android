package com.wwk.livetranslator.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;

import com.wwk.livetranslator.Constants;

/**
 * Created by Pei on 7/25/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */

public class SplashActivity extends Activity {

    private static final String TAG = SplashActivity.class.getSimpleName();

    private static final int SPLASH_DURATION = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new Handler().postDelayed(this::checkAndShowMain, SPLASH_DURATION);
    }

    private void checkAndShowMain() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean introShown = sharedPrefs.getBoolean(Constants.PREF_SKIPPED_APP_INTRO, false);
        if (!introShown) {
            Intent intent = new Intent(this, IntroActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        } else {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(this).toBundle());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        checkAndShowMain();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

}