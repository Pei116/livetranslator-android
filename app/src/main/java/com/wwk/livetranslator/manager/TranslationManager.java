package com.wwk.livetranslator.manager;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.Constants;
import com.wwk.livetranslator.api.APIClient;
import com.wwk.livetranslator.api.APIInterface;
import com.wwk.livetranslator.service.TranslationService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by wwk on 11/12/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class TranslationManager {

    private static final String TAG = TranslationManager.class.getSimpleName();

    private static final String PREF_SOURCE_LANGUAGE = "source_language";
    private static final String PREF_TARGET_LANGUAGE = "target_language";
    private static final String PREF_SERVICE_ENABLED = "service_enabled";

    private static volatile TranslationManager instance;

    private boolean serviceEnabled;
    private String sourceLanguage;
    private String targetLanguage;
    private String detectedLanguage;
    private String lastText;
    private MediaPlayer soundPlayer;

    // Private constructor
    private TranslationManager() {

        // Prevent form the reflection api
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        Context context = Application.getInstance();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        sourceLanguage = sharedPrefs.getString(PREF_SOURCE_LANGUAGE, "auto");
        targetLanguage = sharedPrefs.getString(PREF_TARGET_LANGUAGE, getCurrentLocale(context));
        serviceEnabled = sharedPrefs.getBoolean(PREF_SERVICE_ENABLED, false);

        checkServiceEnabled();
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
                    lastText = chars.toString();

                    if (OverlayWindowManager.getInstance().isMainOverlayShown()) {
                        OverlayWindowManager.getInstance().translateText(lastText);
                    }
                    else if (!Application.getInstance().isInForeground()) {
                        OverlayWindowManager.getInstance().showButtonOverlay(Application.getInstance());
                    }
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

    public String getLastText() {
        return lastText;
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

    public void startService(Context context) {
        Intent intent = new Intent(context, TranslationService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
//        Intent startIntent = new Intent(context, TranslationService.class);
//        startIntent.setAction(Constants.ACTION_START_SERVICE);
//        context.startService(startIntent);
    }

    public void stopService(Context context) {
        Intent intent = new Intent(context, TranslationService.class);
        context.stopService(intent);
    }

    public void setSourceLanguage(String newLanguage) {
        if (newLanguage == null) {
            newLanguage = "auto";
        }
        if (newLanguage.equals(sourceLanguage)) {
            return;
        }

        sourceLanguage = newLanguage;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_SOURCE_LANGUAGE, sourceLanguage);
        editor.apply();
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public boolean shouldDetectSourceLanguage() {
        return sourceLanguage.equals("auto");
    }

    public void setTargetLanguage(String newLanguage) {
        if (newLanguage == null || newLanguage.equals(sourceLanguage)) {
            return;
        }

        targetLanguage = newLanguage;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(PREF_TARGET_LANGUAGE, targetLanguage);
        editor.apply();
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public String getDetectedLanguage() {
        return detectedLanguage;
    }

    public void setServiceEnabled(boolean enabled) {
        serviceEnabled = enabled;
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putBoolean(PREF_SERVICE_ENABLED, serviceEnabled);
        editor.apply();
    }

    String getCurrentLocale(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0).getLanguage();
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale.getLanguage();
        }
    }

    public void translate(String text, TranslationCallback callback) {
        APIInterface apiInterface = APIClient.getClient(!serviceEnabled).create(APIInterface.class);
        if (serviceEnabled) {
            Call<JsonObject> apiCall = apiInterface.translateViaService(text, sourceLanguage, targetLanguage);
            apiCall.enqueue(new Callback<JsonObject>() {
                @Override
                public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                    if (response.isSuccessful()) {
                        StringBuilder builder = new StringBuilder();
                        JsonObject json = response.body();
                        if (json != null) {
                            JsonArray sentences = json.getAsJsonArray("sentences");
                            for (JsonElement element : sentences) {
                                builder.append(element.getAsString());
                            }
                            detectedLanguage = json.get("detected").getAsString();
                        }
                        callback.didFinishTranslation(true, builder.toString(), detectedLanguage);
                    }
                    else {
                        callback.didFinishTranslation(false, null, null);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                    callback.didFinishTranslation(false, null, null);
                }
            });
        }
        else {
            Call<JsonArray> apiCall = apiInterface.translateViaGoogle(text, sourceLanguage, targetLanguage);
            apiCall.enqueue(new Callback<JsonArray>() {
                @Override
                public void onResponse(@NonNull Call<JsonArray> call, @NonNull Response<JsonArray> response) {
                    if (response.isSuccessful()) {
                        StringBuilder builder = new StringBuilder();
                        JsonArray json = response.body();
                        if (json != null) {
                            try {
                                JsonArray sentences = json.get(0).getAsJsonArray();
                                for (JsonElement element : sentences) {
                                    JsonArray candidates = element.getAsJsonArray();
                                    builder.append(candidates.get(0).getAsString());
                                }
                                detectedLanguage = json.get(2).getAsString();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        callback.didFinishTranslation(true, builder.toString(), detectedLanguage);
                    }
                    else {
                        callback.didFinishTranslation(false, null, null);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<JsonArray> call, @NonNull Throwable t) {
                    callback.didFinishTranslation(false, null, null);
                }
            });
        }
    }

    public void speech(String text, String language) {
        if (language.equals("auto")) {
            if (detectedLanguage == null) {
                translate(text, (success, translation, detectedLanguage) -> {
                    if (detectedLanguage != null)
                        speech(text, detectedLanguage);
                });
                return;
            }
            language = detectedLanguage;
        }
        try {
            Uri uri = Uri.parse(Constants.GOOGLE_TTS_URL)
                    .buildUpon()
                    .appendQueryParameter("q", text)
                    .appendQueryParameter("textlen", String.valueOf(text.length()))
                    .appendQueryParameter("tl", language)
                    .build();
            if (soundPlayer != null) soundPlayer.release();
            soundPlayer = new MediaPlayer();
            soundPlayer.setDataSource(uri.toString());
            soundPlayer.prepareAsync();
            soundPlayer.setOnPreparedListener(MediaPlayer::start);
            checkAndSetVolume();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkAndSetVolume() {
        AudioManager audioManager = (AudioManager) Application.getInstance().getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null && audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) / 2, 0);
        }
    }

    private void checkServiceEnabled() {
        APIInterface apiInterface = APIClient.getClient(false).create(APIInterface.class);
        Call<JsonObject> apiCall = apiInterface.enabled();
        apiCall.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                if (response.isSuccessful()) {
                    JsonObject json = response.body();
                    if (json != null) {
                        setServiceEnabled(json.get("enabled").getAsBoolean());
                    }
                } else {
                    setServiceEnabled(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                setServiceEnabled(false);
            }
        });
    }

    public interface TranslationCallback {
        void didFinishTranslation(boolean success, String translation, String detectedLanguage);
    }
}
