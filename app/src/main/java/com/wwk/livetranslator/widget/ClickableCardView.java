package com.wwk.livetranslator.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.cardview.widget.CardView;

/**
 * Created by Pei on 8/9/17.
 * Copyright © 2017 WYFI, Inc. All rights reserved
 */

public class ClickableCardView extends CardView {

    private static final String TAG = ClickableCardView.class.getSimpleName();

    int lastTouchX;
    int lastTouchY;

    ClickableCardView(Context context) {
        super(context);
    }

    public ClickableCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ClickableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            lastTouchX = (int)event.getX();
            lastTouchY = (int)event.getY();
        }

        if (event.getAction() == MotionEvent.ACTION_UP) {
            lastTouchX = (int)event.getX();
            lastTouchY = (int)event.getY();
        }

        return super.onTouchEvent(event);
    }

    public int getLastTouchX() {
        return lastTouchX;
    }

    public int getLastTouchY() {
        return lastTouchY;
    }

    public Point touchPointInRootView() {
        Point pos = new Point(getLastTouchX(), getLastTouchY());
        int loc[] = new int[2];
        getLocationOnScreen(loc);
        // Offset point based on the location of the view in the screen for consistency between activities
        pos.offset(loc[0], loc[1]);
        return pos;
    }

}
