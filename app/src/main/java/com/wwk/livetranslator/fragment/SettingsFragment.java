package com.wwk.livetranslator.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v14.preference.PreferenceFragment;
import android.support.v7.preference.Preference;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.wwk.livetranslator.Constants;
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
        Preference sharePreference = findPreference("pref_share_app");
        sharePreference.setOnPreferenceClickListener(preference1 -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, "Check Live Translator app: https://play.google.com/store/apps/details?id=com.google.android.apps.plus");
            intent.setType("text/plain");
            startActivity(intent);
            return true;
        });

        SwitchPreferenceCompat overlayPreference = (SwitchPreferenceCompat) findPreference(Constants.PREF_TRANSLATION_POPUP);
        overlayPreference.setOnPreferenceChangeListener((preference12, newValue) -> {
            boolean checked = (Boolean) newValue;
            if (checked && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.permission_ontop_title);
                builder.setMessage(R.string.permission_ontop_rationale);
                builder.setPositiveButton(R.string.action_allow, (dialog, i) -> {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                    startActivityForResult(intent, Constants.INTENT_OVERLAY_SETTINGS);
                });
                builder.setNegativeButton(R.string.action_no_thanks, (dialog, i) -> overlayPreference.setChecked(false));
                builder.setOnCancelListener(dialog -> overlayPreference.setChecked(false));
                AlertDialog dialog = builder.create();
                dialog.show();

            }
            return true;
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.INTENT_OVERLAY_SETTINGS && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            SwitchPreferenceCompat overlayPreference = (SwitchPreferenceCompat) findPreference(Constants.PREF_TRANSLATION_POPUP);
            overlayPreference.setChecked(false);
        }
    }
}
