package com.wwk.livetranslator.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;

/**
 * Created by Pei on 12/11/17.
 * Copyright © 2017 Unoceros, Inc. All rights reserved
 */

public class Translation extends RealmObject {

    public Translation() {

    }

    public Translation(String sourceText, String sourceLang, String targetText, String targetLang) {
        this.sourceText = sourceText;
        this.sourceLang = sourceLang;
        this.targetText = targetText;
        this.targetLang = targetLang;
        this.date = new Date();
    }

    @Index
    public String sourceText;

    public String sourceLang;

    public String targetText;

    public String targetLang;

    @Index
    public Date date;

}
