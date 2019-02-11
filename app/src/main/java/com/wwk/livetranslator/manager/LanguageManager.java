package com.wwk.livetranslator.manager;

import android.content.Context;

import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.R;
import com.wwk.livetranslator.model.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

/**
 * Created by Pei on 2/12/18.
 * Copyright © 2017 Unoceros, Inc. All rights reserved
 */

public class LanguageManager {

    private static final String TAG = TranslationManager.class.getSimpleName();

    private static volatile LanguageManager instance;

    private List<Language> allLanguages;
    private List<Language> myLanguages;

    // Private constructor
    private LanguageManager() {

        // Prevent form the reflection api
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        loadLanguages();
    }

    public static LanguageManager getInstance() {
        // Double check locking pattern
        if (instance == null) { //Check for the first time

            synchronized (LanguageManager.class) {   //Check for the second time
                if (instance == null) instance = new LanguageManager();
            }
        }

        return instance;
    }

    private void loadLanguages() {
        Context context = Application.getInstance();
        String[] languageCodes = context.getResources().getStringArray(R.array.language_codes);
        String[] languageNames = context.getResources().getStringArray(R.array.language_names);

        allLanguages = new ArrayList<>();
        for (int i = 0; i < languageCodes.length; i++) {
            String code = languageCodes[i];
            String name = languageNames[i];
            Language lang = new Language(code, name);
            allLanguages.add(lang);
        }

        myLanguages = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Language> objects = realm.where(Language.class)
                .equalTo("isFavorite", true)
                .sort("order")
                .findAll();
        if (objects.size() == 0) {
            Language firstLanguage = detectOSLanguage();
            if (firstLanguage != null) {
                firstLanguage.isFavorite = true;
                myLanguages.add(firstLanguage);
            }
            for (Language lang : allLanguages) {
                if (lang != firstLanguage) {
                    lang.isFavorite = true;
                    myLanguages.add(lang);
                }
            }
        }
        else {
            for (Language one : objects) {
                for (Language lang : allLanguages) {
                    if (lang.equals(one)) {
                        lang.isFavorite = true;
                        myLanguages.add(lang);
                    }
                }
            }
        }
    }

    public List<Language> getLanguagesForSelection() {
        List<Language> result = new ArrayList<>();
        result.addAll(myLanguages);
        for (Language lang : allLanguages) {
            if (!lang.isFavorite) {
                result.add(lang);
            }
        }

        return result;
    }

    public List<Language> getMyLanguages() {
        return myLanguages;
    }

    public void toggleLanguage(Language lang, boolean selected) {
        if (selected == lang.isFavorite) return;

        lang.isFavorite = !lang.isFavorite;
        if (lang.isFavorite) {
            myLanguages.add(lang);
        } else {
            myLanguages.remove(lang);
        }

        saveMyLanguages();
    }

    public void toggleLanguage(Language lang) {
        toggleLanguage(lang, !lang.isFavorite);
    }

    public void moveLanguage(Language lang, int position) {
        int oldPosition = myLanguages.indexOf(lang);
        if (oldPosition == position) return;

        myLanguages.remove(lang);
        myLanguages.add(position, lang);

        saveMyLanguages();
    }

    public boolean isAllSelected() {
        return myLanguages.size() == allLanguages.size();
    }

    public void selectAll() {
        for (Language lang : allLanguages) {
            if (!lang.isFavorite) {
                lang.isFavorite = true;
                myLanguages.add(lang);
            }
        }
    }

    public void clearSelection() {
        for (Language lang : allLanguages) {
            if (lang.isFavorite) {
                lang.isFavorite = false;
                myLanguages.remove(lang);
            }
        }
    }

    private void saveMyLanguages() {
        for (int i = 0; i < myLanguages.size(); i++) {
            Language language = myLanguages.get(i);
            language.order = i;
        }
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(myLanguages);
        realm.commitTransaction();
    }

    private Language detectOSLanguage() {
        String code = Locale.getDefault().getLanguage();
        for (Language language : allLanguages) {
            if (code.equals(language.code)) {
                return language;
            }
        }
        return null;
    }
}
