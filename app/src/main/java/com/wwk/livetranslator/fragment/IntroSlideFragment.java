package com.wwk.livetranslator.fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.paolorotolo.appintro.ISlideBackgroundColorHolder;
import com.github.paolorotolo.appintro.ISlidePolicy;
import com.wwk.livetranslator.Constants;
import com.wwk.livetranslator.R;

/**
 * Created by wwk on 11/13/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */

public class IntroSlideFragment extends Fragment
        implements ISlideBackgroundColorHolder, ISlidePolicy {

    private static final String ARG_SLIDE_NUMBER = "slide_number";

    private int slideNumber;
    private View containerView;
    private TextView titleView;
    private TextView detailView;
    private ImageView imageView;
    private Button permissionButton;

    private boolean permissionIgnored;

    public static IntroSlideFragment newInstance(int slideNumber) {
        IntroSlideFragment fragment = new IntroSlideFragment();

        Bundle args = new Bundle();
        args.putInt(ARG_SLIDE_NUMBER, slideNumber);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_SLIDE_NUMBER)) {
            slideNumber = getArguments().getInt(ARG_SLIDE_NUMBER);
        } else if (savedInstanceState != null && savedInstanceState.containsKey(ARG_SLIDE_NUMBER)) {
            slideNumber = savedInstanceState.getInt(ARG_SLIDE_NUMBER);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_intro_slide, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        containerView = view.findViewById(R.id.containerView);

        titleView = view.findViewById(R.id.titleView);
        detailView = view.findViewById(R.id.detailView);
        imageView = view.findViewById(R.id.imageView);
        permissionButton = view.findViewById(R.id.permissionButton);

        permissionButton.setOnClickListener(this::onRequestPermission);

        if (slideNumber == 1) {
            titleView.setText(R.string.intro_title_1);
            detailView.setText(R.string.intro_detail_1);
            imageView.setImageResource(R.drawable.img_globe);
        } else if (slideNumber == 2) {
            titleView.setText(R.string.intro_title_2);
            detailView.setText(R.string.intro_detail_2);
            imageView.setImageResource(R.drawable.img_languages);
        } else if (slideNumber == 3) {
            titleView.setText(R.string.intro_title_3);
            detailView.setText(R.string.intro_detail_3);
            imageView.setImageResource(R.drawable.img_headphone);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (slideNumber == 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext())) {
            permissionButton.setVisibility(View.VISIBLE);
        } else {
            permissionButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ARG_SLIDE_NUMBER, slideNumber);
    }

    @Override
    public int getDefaultBackgroundColor() {
        if (slideNumber == 1) {
            return getResources().getColor(R.color.intro_slide_bg_1);
        } else if (slideNumber == 2) {
            return getResources().getColor(R.color.intro_slide_bg_2);
        } else {
            return getResources().getColor(R.color.intro_slide_bg_3);
        }
    }

    @Override
    public void setBackgroundColor(@ColorInt int backgroundColor) {
        // Set the background color of the view within your slide to which the transition should be applied.
        if (containerView != null) {
            containerView.setBackgroundColor(backgroundColor);
            Window window = getActivity().getWindow();
            window.setStatusBarColor(backgroundColor);
            window.setNavigationBarColor(backgroundColor);
        }
    }

    @Override
    public boolean isPolicyRespected() {
        if (slideNumber == 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(getContext()) && !permissionIgnored) {
            return false;
        }

        return true;
    }

    @Override
    public void onUserIllegallyRequestedNextPage() {
        if (slideNumber == 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.permission_ontop_title);
            builder.setMessage(R.string.permission_ontop_rationale);
            builder.setPositiveButton(R.string.action_allow, (dialog, i) -> {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
                startActivityForResult(intent, Constants.INTENT_OVERLAY_SETTINGS);
            });
            builder.setNegativeButton(R.string.action_no_thanks, (dialog, i) -> {
                permissionIgnored = true;
            });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    public void onRequestPermission(View view) {
        if (slideNumber == 2 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getContext().getPackageName()));
            startActivityForResult(intent, Constants.INTENT_OVERLAY_SETTINGS);
        }
    }

}
