package com.wwk.livetranslator.manager;

import android.animation.Animator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.wwk.livetranslator.Application;
import com.wwk.livetranslator.R;

/**
 * Created by wwk on 11/12/17.
 * Copyright © 2017 WWK, Inc. All rights reserved
 */

public class OverlayWindowManager
        implements View.OnTouchListener, View.OnClickListener {

    private static final String TAG = OverlayWindowManager.class.getSimpleName();

    private static final int BUTTON_HIDE_TIMEOUT = 5 * 1000;
    private static final String LAST_POSITION_X = "last_position_x";
    private static final String LAST_POSITION_Y = "last_position_y";

    private static volatile OverlayWindowManager instance;

    private View overlayView;
    private Button overlayButton;
    private float offsetX;
    private float offsetY;
    private int originalXPos;
    private int originalYPos;
    private boolean moving;
    private boolean interacting;
    private WindowManager windowManager;

    private Handler hideButtonHandler;

    // Private constructor
    private OverlayWindowManager() {

        // Prevent form the reflection api
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static OverlayWindowManager getInstance() {
        // Double check locking pattern
        if (instance == null) { //Check for the first time

            synchronized (OverlayWindowManager.class) {   //Check for the second time
                if (instance == null) instance = new OverlayWindowManager();
            }
        }

        return instance;
    }

    public void showButtonOverlay(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
            Log.i(TAG, "No permission for overlay");
            return;
        }
        if (overlayButton != null) {
            checkAndResetHiding();
            return;
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
        int lastX = sp.getInt(LAST_POSITION_X, Integer.MAX_VALUE);
        int lastY = sp.getInt(LAST_POSITION_Y, Integer.MAX_VALUE);

        windowManager = (WindowManager) Application.getInstance().getSystemService(Context.WINDOW_SERVICE);

//        overlayButton = new ImageButton(context);
//        overlayButton.setImageResource(R.drawable.ic_translate_white_24dp);
        overlayButton = new Button(context);
        overlayButton.setText(R.string.action_translate);
        overlayButton.setOnTouchListener(this);
//        overlayButton.setBackgroundColor(context.getResources().getColor(R.color.colorOverlay));
        overlayButton.setOnClickListener(this);

        int permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                permission,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.LEFT | Gravity.TOP;
        if (lastX != Integer.MAX_VALUE && lastY != Integer.MAX_VALUE) {
            params.x = lastX;
            params.y = lastY;
        }
        windowManager.addView(overlayButton, params);

        overlayView = new View(context);
        WindowManager.LayoutParams topLeftParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                permission,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT);
        topLeftParams.gravity = Gravity.LEFT | Gravity.TOP;
        topLeftParams.width = 10;
        topLeftParams.height = 10;
        windowManager.addView(overlayView, topLeftParams);

        // Animate to show
        overlayButton.setAlpha(0);
        overlayButton.animate()
                .alpha(1)
                .setDuration(100);

        // Hide button after some seconds
        hideButtonHandler = new Handler(Looper.getMainLooper());
        hideButtonHandler.postDelayed(this::hideButtonOverlay, BUTTON_HIDE_TIMEOUT);
    }

    public void hideButtonOverlay() {
        if (interacting) return; // Do not hide while user interacts with the view

        if (hideButtonHandler != null) {
            hideButtonHandler.removeCallbacksAndMessages(null);
            hideButtonHandler = null;
        }

        if (overlayButton != null) {
            overlayButton.animate()
                    .alpha(0)
                    .setDuration(100)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            if (overlayButton != null) {
                                overlayButton.setVisibility(View.INVISIBLE);
                                windowManager.removeView(overlayView);
                                windowManager.removeView(overlayButton);
                                overlayView = null;
                                overlayButton = null;
                            }
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            interacting = true;
            float x = event.getRawX();
            float y = event.getRawY();

            moving = false;

            int[] location = new int[2];
            overlayButton.getLocationOnScreen(location);

            originalXPos = location[0];
            originalYPos = location[1];

            offsetX = originalXPos - x;
            offsetY = originalYPos - y;

        } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
            if (overlayButton == null) {
                moving = false;
                return false;
            }
            int[] topLeftLocationOnScreen = new int[2];
            overlayView.getLocationOnScreen(topLeftLocationOnScreen);

            float x = event.getRawX();
            float y = event.getRawY();

            WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayButton.getLayoutParams();

            int newX = (int) (offsetX + x);
            int newY = (int) (offsetY + y);

            if (Math.abs(newX - originalXPos) < 10 && Math.abs(newY - originalYPos) < 10 && !moving) {
                return false;
            }

            params.x = newX - (topLeftLocationOnScreen[0]);
            params.y = newY - (topLeftLocationOnScreen[1]);

            windowManager.updateViewLayout(overlayButton, params);
            moving = true;
        } else if (event.getAction() == MotionEvent.ACTION_UP) {
            interacting = false;
            if (moving) {
                checkAndResetHiding();

                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(Application.getInstance());
                SharedPreferences.Editor editor = sp.edit();
                WindowManager.LayoutParams params = (WindowManager.LayoutParams) overlayButton.getLayoutParams();
                editor.putInt(LAST_POSITION_X, params.x);
                editor.putInt(LAST_POSITION_Y, params.y);
                editor.apply();
                return true;
            }
        } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
            interacting = false;
            checkAndResetHiding();
        }

        return false;
    }

    private void checkAndResetHiding() {
        if (hideButtonHandler != null) {
            hideButtonHandler.removeCallbacksAndMessages(null);
            hideButtonHandler.postDelayed(this::hideButtonOverlay, BUTTON_HIDE_TIMEOUT);
        }
    }

    @Override
    public void onClick(View v) {
        Toast.makeText(Application.getInstance(), "Overlay button click event", Toast.LENGTH_SHORT).show();
    }

}
