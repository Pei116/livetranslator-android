package com.wwk.livetranslator.activity;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.Constants;
import com.wwk.livetranslator.R;
import com.wwk.livetranslator.adapter.LanguageListAdapter;
import com.wwk.livetranslator.helper.Utility;
import com.wwk.livetranslator.manager.BookmarkManager;
import com.wwk.livetranslator.manager.OverlayWindowManager;
import com.wwk.livetranslator.manager.TranslationManager;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressImageButton;

public class MainActivity extends AppCompatActivity {

    private TextView sourceText;
    private TextView targetText;
    private Spinner sourceLanguageSpinner;
    private Spinner targetLanguageSpinner;

    private boolean translated = false;
    private boolean bookmarked = false;
    private MenuItem addBookmarkItem;
    private MenuItem removeBookmarkItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermission();
        }

        initLayout();
        TranslationManager.getInstance().startService(this);
    }

    private void initLayout() {
        sourceText = findViewById(R.id.sourceText);
        targetText = findViewById(R.id.targetText);

        sourceText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                translated = bookmarked = false;
                invalidateOptionsMenu();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        sourceLanguageSpinner = findViewById(R.id.sourceLangSpinner);
        targetLanguageSpinner = findViewById(R.id.targetLangSpinner);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        targetText.setKeyListener(null);
    }

    private void setupLanguageSpinners() {
        sourceLanguageSpinner.setAdapter(new LanguageListAdapter(this, true));
        targetLanguageSpinner.setAdapter(new LanguageListAdapter(this));

        sourceLanguageSpinner.setSelection(LanguageListAdapter.getPositionOfLanguage(TranslationManager.getInstance().getSourceLanguage(), true));
        targetLanguageSpinner.setSelection(LanguageListAdapter.getPositionOfLanguage(TranslationManager.getInstance().getTargetLanguage(), false));

        sourceLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslationManager.getInstance().setSourceLanguage(LanguageListAdapter.getItemCode(position, true));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        targetLanguageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TranslationManager.getInstance().setTargetLanguage(LanguageListAdapter.getItemCode(position, false));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OverlayWindowManager.getInstance().isMainOverlayShown()) {
            OverlayWindowManager.getInstance().hideOverlay(true);
        }

        setupLanguageSpinners();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);

        addBookmarkItem = menu.findItem(R.id.action_add_bookmark);
        removeBookmarkItem = menu.findItem(R.id.action_remove_bookmark);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (bookmarked) {
            addBookmarkItem.setVisible(false);
            removeBookmarkItem.setVisible(true);
        }
        else {
            addBookmarkItem.setVisible(true);
            removeBookmarkItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_add_bookmark) {
            if (!translated) {
                Utility.makeSnackbar(targetText, R.string.error_translate_first, getResources().getInteger(android.R.integer.config_longAnimTime)).show();
                return true;
            }
            String sourceLang = TranslationManager.getInstance().shouldDetectSourceLanguage() ? TranslationManager.getInstance().getDetectedLanguage() : TranslationManager.getInstance().getSourceLanguage();
            BookmarkManager.getInstance().checkAndAdd(sourceText.getText().toString(), sourceLang, targetText.getText().toString(), TranslationManager.getInstance().getTargetLanguage());
            bookmarked = true;
            invalidateOptionsMenu();
        }
        else if (id == R.id.action_remove_bookmark) {
            String sourceLang = TranslationManager.getInstance().shouldDetectSourceLanguage() ? TranslationManager.getInstance().getDetectedLanguage() : TranslationManager.getInstance().getSourceLanguage();
            BookmarkManager.getInstance().remove(sourceText.getText().toString(), sourceLang, targetText.getText().toString(), TranslationManager.getInstance().getTargetLanguage());
            bookmarked = false;
            invalidateOptionsMenu();
        }
        else if (id == R.id.action_all_bookmarks) {
            Intent intent = new Intent(this, BookmarksActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        else {
            return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        if (settings.getBoolean(Constants.PREF_TRANSLATION_POPUP, true) && !Settings.canDrawOverlays(this)) {
            android.app.AlertDialog.Builder alert = new android.app.AlertDialog.Builder(this);
            alert.setTitle(R.string.permission_ontop_title);
            alert.setMessage(R.string.permission_ontop_rationale);
            alert.setPositiveButton(R.string.action_allow, (dialog, i) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, Constants.INTENT_OVERLAY_SETTINGS);
            });
            alert.setNegativeButton(R.string.action_no_thanks, (dialog, i) -> {
                SharedPreferences.Editor editor = settings.edit();
                editor.putBoolean(Constants.PREF_TRANSLATION_POPUP, false);
                editor.apply();
            });
            alert.create().show();
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.INTENT_OVERLAY_SETTINGS) {
            SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = settings.edit();
            editor.putBoolean(Constants.PREF_TRANSLATION_POPUP, Settings.canDrawOverlays(this));
            editor.apply();
        }
    }

    public void onTranslate(View view) {
        String text = sourceText.getText().toString();
        if (text.isEmpty()) {
            Utility.makeSnackbar(targetText, R.string.error_type_text_first, getResources().getInteger(android.R.integer.config_longAnimTime)).show();
            return;
        }

        CircularProgressImageButton button = (CircularProgressImageButton) view;
        button.startAnimation();
        TranslationManager.getInstance().translate(text, (success, translation, detectedLanguage) -> {
            button.revertAnimation();
            if (success) {
                targetText.setText(translation);
                translated = true;
            }
        });
    }

    public void onSpeechSource(View view) {
        TranslationManager.getInstance().speech(sourceText.getText().toString(), TranslationManager.getInstance().getSourceLanguage());
    }

    public void onSpeechTarget(View view) {
        TranslationManager.getInstance().speech(targetText.getText().toString(), TranslationManager.getInstance().getTargetLanguage());
    }

    public void onPasteSource(View view) {
        final ClipboardManager clipboardManager = (ClipboardManager) Application.getInstance().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboardManager != null) {
            ClipData clipData = clipboardManager.getPrimaryClip();
            if (clipData != null && clipData.getItemCount() > 0) {
                CharSequence chars = clipData.getItemAt(clipData.getItemCount() - 1).getText();
                if (chars != null) {
                    sourceText.setText(chars);
                }
            }
        }
    }
}
