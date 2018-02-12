package com.wwk.livetranslator.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Pei on 2/12/18.
 * Copyright © 2017 Unoceros, Inc. All rights reserved
 */

public class Language extends RealmObject {
    @PrimaryKey
    public String code;
    public String name;
    public boolean isFavorite;
    public int order;

    public Language() {

    }

    public Language(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public boolean equals(Language other) {
        return code.equals(other.code);
    }

    public boolean equals(String code) {
        return this.code.equals(code);
    }
}
