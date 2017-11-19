package com.wwk.livetranslator.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.wwk.livetranslator.R;

/**
 * Created by wwk on 11/19/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class LanguageListAdapter extends BaseAdapter {

    private boolean showAuto;
    private Context context;

    private String[] languageNames;
    private String[] languageCodes;

    public LanguageListAdapter(Context context) {
        this.context = context;
        this.showAuto = false;
        initLanguages();
    }

    public LanguageListAdapter(Context context, boolean showAuto) {
        this.context = context;
        this.showAuto = showAuto;
        initLanguages();
    }

    private void initLanguages() {
        languageCodes = context.getResources().getStringArray(R.array.language_codes);
        languageNames = context.getResources().getStringArray(R.array.language_names);
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

        if (showAuto) {
            if (position == 0)
                text.setText(R.string.language_auto);
            else
                text.setText(languageNames[position - 1]);
        }
        else {
            text.setText(languageNames[position]);
        }

        return view;
    }

    public String getItemCode(int position) {
        if (showAuto) {
            if (position == 0)
                return context.getString(R.string.language_auto);
            else
                return languageNames[position - 1];
        }
        else {
            return languageNames[position];
        }
    }
}
