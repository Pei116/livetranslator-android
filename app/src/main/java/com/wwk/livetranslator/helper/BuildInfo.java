package com.wwk.livetranslator.helper;

import android.os.Build;

import com.wwk.livetranslator.BuildConfig;

import java.util.Locale;

/**
 * Created by wwk on 11/20/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */

public enum BuildInfo {
    DEVELOPMENT("dev", 0),
    TEST("staging", 1),
    LIVE("live", 2);

    private final String stringValue;
    private final int intValue;

    BuildInfo(String stringValue, int intValue) {
        this.stringValue = stringValue;
        this.intValue = intValue;
    }

    @Override
    public String toString() {
        return stringValue;
    }

    public String stringFromValue(int value) {
        for (BuildInfo info : BuildInfo.values()) {
            if (info.intValue == value) {
                return info.stringValue;
            }
        }
        return null;
    }

    public static boolean isProduction() {
        return LIVE.stringValue.equals(BuildConfig.FLAVOR);
    }

    public static boolean isDevelopment() {
        return DEVELOPMENT.stringValue.equals(BuildConfig.FLAVOR);
    }

    public static String buildVersion() {
        return String.format(Locale.getDefault(), "%s (%d)", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE);
    }

    public static String osVersion() {
        return String.format(Locale.getDefault(), "%s (API %d)", Build.VERSION.RELEASE, Build.VERSION.SDK_INT);
    }
}
