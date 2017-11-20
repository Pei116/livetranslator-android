package com.wwk.livetranslator.activity;

import android.annotation.TargetApi;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.wwk.livetranslator.R;
import com.wwk.livetranslator.adapter.LanguageListAdapter;
import com.wwk.livetranslator.manager.TranslationManager;

public class MainActivity extends AppCompatActivity {

    private TextView sourceText;
    private TextView targetText;
    private Spinner sourceLanguageSpinner;
    private Spinner targetLanguageSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkAndRequestPermission();
        }

        initLayout();
    }

    private void initLayout() {
        sourceText = findViewById(R.id.sourceText);
        targetText = findViewById(R.id.targetText);
        sourceLanguageSpinner = findViewById(R.id.sourceLangSpinner);
        targetLanguageSpinner = findViewById(R.id.targetLangSpinner);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sourceLanguageSpinner.setAdapter(new LanguageListAdapter(this, true));
        targetLanguageSpinner.setAdapter(new LanguageListAdapter(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return false;
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkAndRequestPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, 0);
        }
    }

    public void onTranslate(View view) {
        TranslationManager.getInstance().translate(sourceText.getText().toString(), (success, translation) -> targetText.setText(translation));
    }
}
