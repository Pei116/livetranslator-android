package com.wwk.livetranslator.manager;

import android.support.annotation.NonNull;

import com.wwk.livetranslator.Constants;
import com.wwk.livetranslator.model.Translation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Pei on 12/11/17.
 * Copyright © 2017 Unoceros, Inc. All rights reserved
 */

public class BookmarkManager {

    private static final String TAG = TranslationManager.class.getSimpleName();

    private static volatile BookmarkManager instance;

    // Private constructor
    private BookmarkManager() {

        // Prevent form the reflection api
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

    }

    public static BookmarkManager getInstance() {
        // Double check locking pattern
        if (instance == null) { //Check for the first time

            synchronized (BookmarkManager.class) {   //Check for the second time
                if (instance == null) instance = new BookmarkManager();
            }
        }

        return instance;
    }

    public boolean checkAndAdd(String sourceText, String sourceLang, String targetText, String targetLang) {
        Realm realm = Realm.getDefaultInstance();
        Translation object = realm.where(Translation.class)
                .equalTo("sourceText", sourceText)
                .equalTo("sourceLang", sourceLang)
                .equalTo("targetLang", targetLang)
                .findFirst();
        if (object == null) {
            object = new Translation(sourceText, sourceLang, targetText, targetLang);
            realm.beginTransaction();
            realm.copyToRealm(object);
            realm.commitTransaction();
            return true;
        }
        else {
            realm.beginTransaction();
            object.date = new Date();
            object.targetText = targetText;
            realm.commitTransaction();
            return false;
        }
    }

    public boolean remove(String sourceText, String sourceLang, String targetText, String targetLang) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        RealmResults<Translation> objects = realm.where(Translation.class)
                .equalTo("sourceText", sourceText)
                .equalTo("sourceLang", sourceLang)
                .equalTo("targetLang", targetLang)
                .findAll();
        boolean removed = objects.deleteAllFromRealm();
        realm.commitTransaction();
        return removed;
    }

    public void remove(int position) {
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.where(Translation.class)
                .sort("date", Sort.DESCENDING)
                .findAll()
                .deleteFromRealm(position);
        realm.commitTransaction();
    }

    public void reloadBookmarks(int limit, LoadBookmarkCallbacks callbacks) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Translation> objects = realm.where(Translation.class)
                .sort("date", Sort.DESCENDING)
                .findAllAsync();
        objects.addChangeListener(translations -> {
            int size = translations.size();
            boolean hasMore = false;
            if (size > limit) {
                hasMore = true;
                size = limit;
            }
            ArrayList<Translation> results = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                Translation translation = translations.get(i);
                results.add(translation);
            }
            callbacks.didLoadBookmarks(results, hasMore);
            objects.removeAllChangeListeners();
        });
    }

    public void loadMoreBookmarks(int limit, int skip, LoadBookmarkCallbacks callbacks) {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Translation> objects = realm.where(Translation.class)
                .sort("date", Sort.DESCENDING)
                .findAllAsync();
        objects.addChangeListener(translations -> {
            int size = translations.size();
            boolean hasMore = false;
            if (size > limit + skip) {
                hasMore = true;
                size = limit;
            }
            ArrayList<Translation> results = new ArrayList<>(size);
            for (int i = skip; i < limit + skip; i++) {
                Translation translation = translations.get(i);
                results.add(translation);
            }
            callbacks.didLoadBookmarks(results, hasMore);
            objects.removeAllChangeListeners();
        });
    }

    public interface LoadBookmarkCallbacks {
        void didLoadBookmarks(List<Translation> translations, boolean hasMore);
    }
}
