package com.wwk.livetranslator.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.R;
import com.wwk.livetranslator.manager.LanguageManager;
import com.wwk.livetranslator.model.Language;

import java.util.List;

/**
 * Created by wwk on 11/19/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class LanguageListAdapter extends BaseAdapter {

    private boolean showAuto;
    private Context context;

    public LanguageListAdapter(Context context) {
        this.context = context;
        this.showAuto = false;
    }

    public LanguageListAdapter(Context context, boolean showAuto) {
        this.context = context;
        this.showAuto = showAuto;
    }

    @Override
    public int getCount() {
        List<Language> languages = LanguageManager.getInstance().getMyLanguages();
        if (showAuto)
            return languages.size() + 1;

        return languages.size();
    }

    @Override
    public Object getItem(int position) {
        List<Language> languages = LanguageManager.getInstance().getMyLanguages();
        if (showAuto) {
            if (position == 0) return null;
            return languages.get(position - 1);
        }
        return languages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return createViewFromResource(position, convertView, parent, android.R.layout.simple_spinner_dropdown_item);
    }

    private View createViewFromResource(int position, View convertView, ViewGroup parent,
                                        int resource) {
        View view;
        TextView text;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(resource, parent, false);
        } else {
            view = convertView;
        }
        text = (TextView) view;
        text.setEllipsize(TextUtils.TruncateAt.MARQUEE);
//        text.setAutoSizeTextTypeWithDefaults();

        text.setText(getItemName(position, showAuto));

        return view;
    }

    public static int getPositionOfLanguage(Language language, boolean showAuto) {
        List<Language> languages = LanguageManager.getInstance().getMyLanguages();
        int index = languages.indexOf(language);
        return showAuto ? index + 1 : index;
    }

    public static int getPositionOfLanguage(String code, boolean showAuto) {
        List<Language> languages = LanguageManager.getInstance().getMyLanguages();
        int position = -1;
        for (int i = 0; i < languages.size(); i++) {
            Language language = languages.get(i);
            if (language.equals(code)) {
                position = i;
                break;
            }
        }

        return showAuto ? position + 1 : position;
    }

    public static String getItemCode(int position, boolean showAuto) {
        List<Language> languages = LanguageManager.getInstance().getMyLanguages();
        if (showAuto) {
            if (position == 0)
                return null;
            else
                return languages.get(position - 1).code;
        }
        else {
            return languages.get(position).code;
        }
    }

    public static String getItemName(int position, boolean showAuto) {
        List<Language> languages = LanguageManager.getInstance().getMyLanguages();
        if (showAuto) {
            if (position == 0)
                return Application.getInstance().getString(R.string.language_auto);
            else
                return languages.get(position - 1).name;
        }
        else {
            return languages.get(position).name;
        }
    }
}
