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
import com.wwk.livetranslator.manager.TranslationManager;

/**
 * Created by wwk on 11/19/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class LanguageListAdapter extends BaseAdapter {

    private boolean showAuto;
    private Context context;

    private static String[] languageNames = TranslationManager.getInstance().getLanguageNames();
    private static String[] languageCodes = TranslationManager.getInstance().getLanguageCodes();

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
        if (showAuto)
            return languageCodes.length + 1;

        return languageCodes.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
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

    public static String getItemCode(int position, boolean showAuto) {
        if (showAuto) {
            if (position == 0)
                return null;
            else
                return languageCodes[position - 1];
        }
        else {
            return languageCodes[position];
        }
    }

    public static String getItemName(int position, boolean showAuto) {
        if (showAuto) {
            if (position == 0)
                return Application.getInstance().getString(R.string.language_auto);
            else
                return languageNames[position - 1];
        }
        else {
            return languageNames[position];
        }
    }

    public static int getPositionOfLanguage(String code, boolean showAuto) {
        for (int i = 0; i < languageCodes.length; i++) {
            if (languageCodes[i].equals(code)) {
                return showAuto ? i + 1 : i;
            }
        }

        return 0;
    }
}
