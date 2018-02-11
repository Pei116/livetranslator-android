package com.wwk.livetranslator.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;

import com.wwk.livetranslator.R;

/**
 * Created by Pei on 2/11/18.
 * Copyright © 2017 Unoceros, Inc. All rights reserved
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Load the Preferences from the XML file
        addPreferencesFromResource(R.xml.app_settings);

        setupShareApp();
    }

    private void setupShareApp() {
        Preference preference = findPreference("pref_share_app");
        preference.setOnPreferenceClickListener(preference1 -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "Check Live Translator app: https://play.google.com/store/apps/details?id=com.google.android.apps.plus");
            intent.setType("text/plain");
            startActivity(intent);
            return true;
        });
    }

}
